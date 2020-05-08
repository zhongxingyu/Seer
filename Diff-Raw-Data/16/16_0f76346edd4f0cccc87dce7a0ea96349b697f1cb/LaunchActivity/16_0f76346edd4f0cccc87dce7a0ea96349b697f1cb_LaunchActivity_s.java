 package net.danopia.mobile.laundryview;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.location.LocationManager;
 import android.net.Uri;
 import android.os.Bundle;
 
 import net.danopia.mobile.laundryview.data.AssistClient;
 import net.danopia.mobile.laundryview.data.LvClient;
 
 /**
  * Routes intents to where they should go.
  * Created by daniel on 8/30/13.
  */
 public class LaunchActivity extends Activity {
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         final LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
         Cache.location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
 
         final Uri data = getIntent().getData();
         if (data == null) {
             // TODO: Check for wifi, otherwise, show FindCampusActivity
             startActivity(new Intent(this, FindCampusActivity.class));
         } else {
             new Thread(new Runnable() {
                 @Override
                 public void run() {
                     Cache.bust();
                     LvClient.resetCookies();
                    LvClient.getPage(data.getPath());
                    AssistClient.submitPath(data.getPath(), Cache.location);
 
                     runOnUiThread(new Runnable() {
                         @Override
                         public void run() {
                             Intent intent = new Intent(LaunchActivity.this, RoomListActivity.class);
                            intent.putExtra("path", data.getPath());
                             startActivity(intent);
                         }
                     });
                 }
             }).start();

            // TODO: Auth as link, see what it offers
            System.out.println(data.getPath());
         }
     }
 }
