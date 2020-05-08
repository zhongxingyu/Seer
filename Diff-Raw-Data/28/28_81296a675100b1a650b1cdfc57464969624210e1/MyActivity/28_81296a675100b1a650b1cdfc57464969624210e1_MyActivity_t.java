 package com.example.Nomad;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.pm.ApplicationInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.ResolveInfo;
 import android.graphics.drawable.Drawable;
import android.location.*;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ImageButton;
 import android.widget.TextView;
 
import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 public class MyActivity extends Activity
 {
    private LocationManager locationManager;
     private TextView address, cityStateZip;
    private Context activityContext;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
        activityContext = this.getApplicationContext();
        locationManager  = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);

         // Creation and instantiation of ImageButtons (hot pads)
         ImageButton topLeft, topCenter, topRight, bottomLeft, bottomCenter, bottomRight;
 
         topLeft = (ImageButton)this.findViewById(R.id.imageButton_home_topLeft);
         topCenter = (ImageButton)this.findViewById(R.id.imageButton_home_topCenter);
         topRight = (ImageButton)this.findViewById(R.id.imageButton_home_topRight);
 
         bottomLeft = (ImageButton)this.findViewById(R.id.imageButton_home_bottomLeft);
         bottomCenter = (ImageButton)this.findViewById(R.id.imageButton_home_bottomCenter);
         bottomRight = (ImageButton)this.findViewById(R.id.imageButton_home_bottomRight);
 
         // Binds the onLongClickListener to each hot pad
         topLeft.setOnLongClickListener(onLongClickListener);
         topCenter.setOnLongClickListener(onLongClickListener);
         topRight.setOnLongClickListener(onLongClickListener);
 
         bottomLeft.setOnLongClickListener(onLongClickListener);
         bottomCenter.setOnLongClickListener(onLongClickListener);
         bottomRight.setOnLongClickListener(onLongClickListener);
 
         // Instantiation of TextViews
         address = (TextView)this.findViewById(R.id.textView_home_address);
         cityStateZip = (TextView)this.findViewById(R.id.textView_home_cityStateZip);
 
         // Register listener with locationManager
         locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
     }
 
     private View.OnLongClickListener onLongClickListener = new View.OnLongClickListener() {
         @Override
         public boolean onLongClick(View v) {
 
             // Package contains App info
             PackageManager packageManager = getPackageManager();
             // App info contains....app info
             List<ApplicationInfo> installedApplications = packageManager.getInstalledApplications(0);
 
             // List of apps we care about
             ArrayList<ApplicationInfo> nonSystemApplications = new ArrayList<>();
 
             // Find the app we care about
             for (ApplicationInfo applicationInfo : installedApplications){
                 // TODO: These flag filers work for now, but they need to be refined to further limit which applications can be chosen
                 // Apps like G-mail and maps are updated system apps, I want those.
                 if (applicationInfo.flags == ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)
                     nonSystemApplications.add(applicationInfo);
                     // Any other applications that are non system applications
                 else if (applicationInfo.flags != ApplicationInfo.FLAG_SYSTEM)
                     nonSystemApplications.add(applicationInfo);
             }
 
             // Collections holding application names and icons
             CharSequence[] applicationNames = new CharSequence[nonSystemApplications.size()];
             ArrayList<Drawable> applicationIcons = new ArrayList<>();
 
             int appCount = 0;
             // Add names and icons of non system applications to respective collections for display
             for (ApplicationInfo applicationInfo : nonSystemApplications){
                 applicationNames[appCount++] = (packageManager.getApplicationLabel(applicationInfo));
                 applicationIcons.add(packageManager.getApplicationIcon(applicationInfo));
             }
 
             AlertDialog.Builder appListDialogBuilder = new AlertDialog.Builder(v.getContext());
             appListDialogBuilder.setTitle("Choose Application");
 
             for (int i = 0; i < nonSystemApplications.size(); i++){
                 // TODO: For each non system application, construct a list item for the dialog consisting of the application icon and name
             }
 
             appListDialogBuilder.setItems(applicationNames, new DialogInterface.OnClickListener() {
                 @Override
                 public void onClick(DialogInterface dialog, int which) {
                     // TODO: This is where we need to do something with the app that the user has chosen to bind to a hot pad
                     // Need to find a way to get the hot pad that triggered this long press event
                 }
             });
 
             appListDialogBuilder.show();
             return true;
         }
     };
 
     LocationListener locationListener = new LocationListener() {

        Geocoder addressGeocoder;
        List<Address> addresses;
         @Override
         public void onLocationChanged(Location location) {
             // TODO: Translate the Location object/information into a geoCoded address, set the fields that correspond on the home screen.
            addressGeocoder = new Geocoder(activityContext);
            try{
            addresses = addressGeocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            } catch (IOException e){
                System.out.println("SOMETHING WENT WRONG WITH GETTING THE ADDRESS FROM GEOCODER");
            }
            for (Address address : addresses) System.out.println(address.getAddressLine(0));
             address = null;
             cityStateZip = null;
         }
 
         // TODO: Decide what to do with the rest of these update methods.
         @Override
         public void onStatusChanged(String provider, int status, Bundle extras) {
             //To change body of implemented methods use File | Settings | File Templates.
         }
 
         @Override
         public void onProviderEnabled(String provider) {
             //To change body of implemented methods use File | Settings | File Templates.
         }
 
         @Override
         public void onProviderDisabled(String provider) {
             //To change body of implemented methods use File | Settings | File Templates.
         }
     };
 }
