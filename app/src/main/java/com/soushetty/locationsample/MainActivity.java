package com.soushetty.locationsample;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Build;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.text.MessageFormat;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity  implements GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener, LocationListener
{

    private static final int ALL_PERMISSIONS_RESULTS = 111;
    private GoogleApiClient client;
    private FusedLocationProviderClient fusedLocationProviderClient;
    public ArrayList<String> permissionsToRequest;
    public ArrayList<String> permissions=new ArrayList<>(); //to hold all the permissions we are going to req with the user
    public ArrayList<String> permissionsRejected=new ArrayList<>();
    private TextView locationtext;
    private LocationRequest locationrequest;
    public static final long UPDATE_INTERVAL=5000;
    public static final long FAST_INTERVAL=5000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        locationtext=findViewById(R.id.location_text);

        fusedLocationProviderClient=LocationServices.getFusedLocationProviderClient(MainActivity.this);

        //permissions we are going to req  the user to grant
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);

        permissionsToRequest=permissionsToRequest(permissions);
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            if(permissionsToRequest.size()>0){
                requestPermissions(permissionsToRequest.toArray(new String[permissionsToRequest.size()]),ALL_PERMISSIONS_RESULTS);
            }
        }


        //just inisiating everything here (just like in buttons)
        client=new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    //method to make an array list of permissions to request
    private ArrayList<String> permissionsToRequest(ArrayList<String> requiredPermissions) {
        ArrayList<String> results=new ArrayList<>();

        for(String perms:requiredPermissions){
            if(!haspermission(perms)){
                //if the permission is not yet requested,then add to arraylist results
                results.add(perms);
            }
        }

        return results;
    }

    //method to cross check whether that particular permission is already existing/requested
    private boolean haspermission(String perms) {
        //checking if the android is of lower versions
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){ //when we have lower version of android,this check works
            return checkSelfPermission(perms)== PackageManager.PERMISSION_GRANTED; //to check whether the required permission is already granted ones
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /*in order to check google API is working */
    @Override
    protected void onPostResume() {
        super.onPostResume();
        int error_code= GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);

        if(error_code!= ConnectionResult.SUCCESS){
            Dialog error_dialog=GoogleApiAvailability.getInstance().getErrorDialog(this, error_code, error_code, new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    Toast.makeText(MainActivity.this,"error occured",Toast.LENGTH_SHORT).show();
                }
            });
            error_dialog.show();
        }
        else{
            Toast.makeText(MainActivity.this,"All is well and working",Toast.LENGTH_SHORT).show();

        }

    }

    @Override
    public void onLocationChanged(Location location) {
        if(location!=null){
            locationtext.setText("Latitude: "+location.getLatitude() +"&& Longitude: "+location.getLongitude());
        }

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }


    //once the google api connections and is working fine,we need to look whether the user has granted the permissions or not
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED)
        {
            return;

        }

        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            //get the last known location-which can be null too
            public void onSuccess(Location location) {
                if(location!=null){
                    locationtext.setText(MessageFormat.format("latitude: {0}Longitude: {1}", location.getLatitude(), location.getLongitude()));
                }
            }
        });
        startLocationUpdates(); //to track the location as it keeps changing(when the user moves)
    }

    private void startLocationUpdates() {
        //to gauge and configure our location requests ,creating an obj
        // it's too much load to the device to handle the location each time.hence it prioritize's the req
        locationrequest=new LocationRequest();
        locationrequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);//this req will have the highest and accurate priority
        locationrequest.setInterval(UPDATE_INTERVAL); //here every 5 sec ,we will be placing an update request for location
        locationrequest.setFastestInterval(FAST_INTERVAL);

        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED&&
        ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            Toast.makeText(MainActivity.this,"Please enable the required permissions",Toast.LENGTH_LONG).show();
        }

        LocationServices.getFusedLocationProviderClient(MainActivity.this).requestLocationUpdates(locationrequest,new LocationCallback(){
            //callbacks that override to get the actual location that has changed
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                //now locationResult has all the locations
                if(locationResult!=null){
                    Location location=locationResult.getLastLocation(); //to get the recent changed location
                    locationtext.setText(MessageFormat.format("latitude {0}Longitude: {1}", location.getLatitude(), location.getLongitude()));
                }

            }

            @Override
            public void onLocationAvailability(LocationAvailability locationAvailability) {
                super.onLocationAvailability(locationAvailability);
            }
        },null);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case ALL_PERMISSIONS_RESULTS:
                for(String perm:permissionsToRequest){
                    if(!haspermission(perm)){
                        permissionsRejected.add(perm);
                    }
                }

                /*if the permissions are rejected by the user,then we can not get the location.Hence creating an alert dialog to
                 make user realize ,with "OK" or "Cancel" options to choose further*/
                if(permissionsRejected.size()>0){
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                        //to show the user more details due to rejected permission
                        if(shouldShowRequestPermissionRationale(permissionsRejected.get(0))){
                            new AlertDialog.Builder(MainActivity.this)
                                    .setMessage("These permissions are mandatory to get your location")
                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
                                                requestPermissions(permissionsRejected.toArray(new String[permissionsRejected.size()]),ALL_PERMISSIONS_RESULTS);
                                            }

                                        }
                                    }).setNegativeButton("Cancel",null).create().show();

                        }


                    }

                }
                else{
                    if(client!=null){
                        client.connect();
                    }
                }
            break;

        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onStart() {
        super.onStart();
        if(client!=null){
            client.connect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        client.disconnect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //we need to make sure that the activity is disconnected inorder to avoid memory issues,battery drains as getting user's location is a long task
        if(client!=null && client.isConnected()){
            LocationServices.getFusedLocationProviderClient(this).removeLocationUpdates(new LocationCallback(){
            });
            client.disconnect();
        }
    }
}
