 package edu.hawaii.systemh.android.menu;
 
 import java.io.IOException;
 import org.apache.http.HttpResponse;
 import org.apache.http.StatusLine;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 import edu.hawaii.systemh.android.R;
 import edu.hawaii.systemh.android.aquaponics.Aquaponics;
 import edu.hawaii.systemh.android.dialogs.FeedbackDialog;
 import edu.hawaii.systemh.android.energy.Energy;
 import edu.hawaii.systemh.android.help.Help;
 import edu.hawaii.systemh.android.hvac.Hvac;
 import edu.hawaii.systemh.android.lighting.Lighting;
 import edu.hawaii.systemh.android.settings.Settings;
 
 /**
  * The main activity of this application, called on startup.
  * 
  * @author Group H
  * 
  */
 public class Menu extends Activity {
 
   // success identifier for connection checking thread
   private static final int CONNECTION_SUCCESS_IDENTIFIER = 1;
   // fail identifier for connection checking thread
   private static final int CONNECTION_FAIL_IDENTIFIER = 2;
 
   private static final int AQUAPONICS_PAGE = 0;
   private static final int LIGHTING_PAGE = 1;
   private static final int HVAC_PAGE = 2;
   private static final int ENERGY_PAGE = 3;
 
   private int destPage;
 
   private static SharedPreferences preferences;
   private ProgressDialog dialog;
 
   private Handler handler = new Handler() {
     @Override
     public void handleMessage(Message msg) {
 
       switch (msg.what) {
       case CONNECTION_SUCCESS_IDENTIFIER:
         dialog.dismiss();
 
         if (destPage == AQUAPONICS_PAGE) {
           Intent intent = new Intent(Intent.ACTION_VIEW);
           intent.setClassName(Menu.this, Aquaponics.class.getName());
           startActivity(intent);
         }
 
         if (destPage == LIGHTING_PAGE) {
           Intent intent = new Intent(Intent.ACTION_VIEW);
           intent.setClassName(Menu.this, Lighting.class.getName());
           startActivity(intent);
         }
 
         if (destPage == HVAC_PAGE) {
           Intent intent = new Intent(Intent.ACTION_VIEW);
           intent.setClassName(Menu.this, Hvac.class.getName());
           startActivity(intent);
         }
 
         if (destPage == ENERGY_PAGE) {
           Intent intent = new Intent(Intent.ACTION_VIEW);
           intent.setClassName(Menu.this, Energy.class.getName());
           startActivity(intent);
         }
 
         break;
 
       case CONNECTION_FAIL_IDENTIFIER:
         dialog.dismiss();
         FeedbackDialog feedback = new FeedbackDialog(Menu.this);
         feedback.show();
         break;
 
       default:
         // do nothing
       }
     }
   };
 
   // to check for internet connection
   private boolean success = false;
 
   /**
    * Called when the activity is first created.
    * 
    * @param savedInstanceState - A mapping from String values to various Parcelable types.
    */
   @Override
   public void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
 
     // requesting to turn the title OFF
     requestWindowFeature(Window.FEATURE_NO_TITLE);
 
     // making it full screen
     getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
         WindowManager.LayoutParams.FLAG_FULLSCREEN);
 
     setContentView(R.layout.menu);
 
     preferences = getSharedPreferences("USER_SETTINGS", Context.MODE_WORLD_READABLE);
 
   }
 
   /**
    * Check for the internet connection outside the main thread.
    */
   public void checkConnection() {
     dialog = ProgressDialog.show(this, "Please wait...", "Connecting to the house system...");
     dialog.setCancelable(true);
     new ConnectionThread().start();
   }
 
   /**
    * Take user to the aquaponics page.
    * 
    * @param view The view
    */
   public void showAquaponicsPage(View view) {
     // Intent intent = new Intent(Intent.ACTION_VIEW);
     // intent.setClassName(this, Aquaponics.class.getName());
     // startActivity(intent);
     destPage = AQUAPONICS_PAGE;
     checkConnection();
   }
 
   /**
    * Take user to the lighting page.
    * 
    * @param view The view
    */
   public void showLightingPage(View view) {
     destPage = LIGHTING_PAGE;
     checkConnection();
   }
 
   /**
    * Take user to the Hvac page.
    * 
    * @param view The view
    */
   public void showHvacPage(View view) {
     destPage = HVAC_PAGE;
     checkConnection();
   }
 
   /**
    * Take user to the Energy page.
    * 
    * @param view The view
    */
   public void showEnergyPage(View view) {
     destPage = ENERGY_PAGE;
     checkConnection();
   }
 
   /**
    * Take user to the Settings page.
    * 
    * @param view The view
    */
   public void showSettingsPage(View view) {
     Intent intent = new Intent(Intent.ACTION_VIEW);
     intent.setClassName(this, Settings.class.getName());
     startActivity(intent);
   }
 
   /**
    * Take user to the Help page.
    * 
    * @param view The view
    */
   public void showHelpPage(View view) {
     Intent intent = new Intent(Intent.ACTION_VIEW);
     intent.setClassName(this, Help.class.getName());
     startActivity(intent);
   }
 
   /**
    * Destroys this activity when onStop is called.
    */
   @Override
   protected void onStop() {
     finish();
     super.onDestroy();
   }
 
   /**
    * Accessor method for preferences.
    * 
    * @return The preferences associated with app.
    */
   public static SharedPreferences getPreferences() {
     return preferences;
   }
 
   /**
    * A thead that checks for connection ensure we have a running frontend and a correct IP address.
    * 
    * @author Group H
    * 
    */
   public class ConnectionThread extends Thread {
     public void run() {
 
       // boolean success = false;
       String ipAddress;
       String getUrl;
       SharedPreferences preferences = Menu.getPreferences();
 
       String ip = preferences.getString("ip_address", null);
       if (ip == null) {
         ipAddress = "127.0.0.1";
       }
       else {
         ipAddress = ip;
       }
 
       getUrl = "http://" + ipAddress + ":8111/AQUAPONICS/state";
 
       HttpClient client = new DefaultHttpClient();
       HttpGet request = new HttpGet(getUrl);
       try {
         HttpResponse response = client.execute(request);
         // Check if server response is valid
         StatusLine status = response.getStatusLine();
         if (status.getStatusCode() == 200) {
           success = true;
         }
         else {
           success = false;
         }
       }
       catch (IOException e) {
         success = false;
       }
       finally {
         // dialog.dismiss();
         // tester.cancel(true);
 
         Message msg = new Message();
         if (success) {
           msg.what = Menu.CONNECTION_SUCCESS_IDENTIFIER;
         }
         else {
           msg.what = Menu.CONNECTION_FAIL_IDENTIFIER;
         }
         handler.sendMessage(msg);
       }
     }
   }
 
 }
