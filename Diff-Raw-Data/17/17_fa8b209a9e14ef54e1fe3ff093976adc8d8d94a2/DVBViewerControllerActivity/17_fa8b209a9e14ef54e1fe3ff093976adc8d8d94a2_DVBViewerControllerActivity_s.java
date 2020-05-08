 package de.bennir.DVBViewerController;
 
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.os.Vibrator;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.view.View;
 import android.widget.TextView;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.MenuItem;
 import com.androidquery.AQuery;
 import com.androidquery.callback.AjaxStatus;
 import com.slidingmenu.lib.SlidingMenu;
 import de.keyboardsurfer.android.widget.crouton.Crouton;
 import de.keyboardsurfer.android.widget.crouton.Style;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 public class DVBViewerControllerActivity extends SherlockFragmentActivity {
     private final String TAG = DVBViewerControllerActivity.class.toString();
 
     public static String dvbHost = "";
     public static String dvbIp = "";
     public static String dvbPort = "";
     public static String recIp = "";
     public static String recPort = "";
     private Fragment mContent;
     private SlidingMenu menu;
 
     @Override
     protected void onDestroy() {
         // Workaround until there's a way to detach the Activity from Crouton while
         // there are still some in the Queue.
         Crouton.clearCroutonsForActivity(this);
         super.onDestroy();
     }
 
     @Override
     public void onBackPressed() {
         if (menu.isMenuShowing()) {
             Log.d(TAG, "Back with menu");
             menu.showContent(true);
             return;
         }
 
         super.onBackPressed();
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         setContentView(R.layout.main);
 
        Typeface robotoThin = Typeface.createFromAsset(getAssets(), "Roboto-Thin.ttf");
        Typeface robotoLight = Typeface.createFromAsset(getAssets(), "Roboto-Light.ttf");
        Typeface robotoCondensed = Typeface.createFromAsset(getAssets(), "RobotoCondensed-Bold.ttf");
 
         Bundle extras = getIntent().getExtras();
 
         if (extras != null) {
             dvbHost = extras.getString("dvbHost");
             dvbIp = extras.getString("dvbIp");
             dvbPort = extras.getString("dvbPort");
         }
 
         getSupportActionBar().setDisplayHomeAsUpEnabled(true);
         getSupportActionBar().setTitle(R.string.remote);
         getSupportActionBar().setIcon(R.drawable.ic_action_remote);
 
         /**
          * Above View
          */
         if (savedInstanceState != null)
             mContent = getSupportFragmentManager().getFragment(savedInstanceState, "mContent");
         if (mContent == null)
             mContent = new RemoteFragment();
 
         getSupportFragmentManager()
                 .beginTransaction()
                 .replace(R.id.content_frame, mContent)
                 .commit();
 
 
         /**
          * Behind View
          */
         menu = new SlidingMenu(this);
         menu.setMenu(R.layout.menu);
         TextView activeProfile = (TextView) menu.findViewById(R.id.active_profile);
         activeProfile.setTypeface(robotoLight);
         activeProfile.setText(dvbHost);
         activeProfile.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 ((Vibrator) getSystemService(Context.VIBRATOR_SERVICE)).vibrate(50);
                 Intent mIntent = new Intent(getApplicationContext(), DeviceSelectionActivity.class);
                 startActivity(mIntent);
 
                 DVBViewerControllerActivity.this.finish();
                 overridePendingTransition(R.anim.fadein, R.anim.slide_to_right);
             }
         });
 
         /**
          * Menu Customize
          */
         menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
         menu.setShadowWidthRes(R.dimen.shadow_width);
         menu.setShadowDrawable(R.drawable.shadow);
         menu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
         menu.setFadeDegree(0.35f);
         menu.setBehindScrollScale(0.5f);
         menu.attachToActivity(this, SlidingMenu.SLIDING_WINDOW);
 
         /**
          * Recording Service Loading
          */
         if (DVBViewerControllerActivity.recIp.isEmpty() || DVBViewerControllerActivity.recPort.isEmpty()) {
             Log.d(TAG, "Getting Recording Service");
             AQuery aq = new AQuery(this);
 
             String url = "http://" +
                     DVBViewerControllerActivity.dvbIp + ":" +
                     DVBViewerControllerActivity.dvbPort +
                     "/?getRecordingService";
             Log.d(TAG, "URL=" + url);
             aq.ajax(url, JSONObject.class, this, "getRecordingServiceCallback");
         }
     }
 
     public void getRecordingServiceCallback(String url, JSONObject json, AjaxStatus ajax) {
         try {
             if (json != null) {
                 JSONObject recordingService = json.getJSONObject("recordingService");
 
                 DVBViewerControllerActivity.recIp = recordingService.getString("ip");
                 DVBViewerControllerActivity.recPort = recordingService.getString("port");
 
                 Log.d(TAG, "RecordingService: " + DVBViewerControllerActivity.recIp + ":" + DVBViewerControllerActivity.recPort);
             }
         } catch (JSONException e) {
             Crouton.makeText(this, R.string.recservicefailed, Style.ALERT).show();
 
             e.printStackTrace();
         }
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case android.R.id.home:
                 menu.toggle();
                 return true;
         }
         return super.onOptionsItemSelected(item);
     }
 
     @Override
     public void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
         getSupportFragmentManager().putFragment(outState, "mContent", mContent);
     }
 
     public void switchContent(Fragment fragment, int titleRes, int icon) {
         getSupportActionBar().setTitle(titleRes);
         getSupportActionBar().setIcon(icon);
         mContent = fragment;
         getSupportFragmentManager()
                 .beginTransaction()
                 .replace(R.id.content_frame, fragment)
                 .commit();
         menu.showContent();
     }
     public void switchContent(Fragment fragment, int titleRes, int icon, boolean addToBackStack) {
         if(addToBackStack) {
             getSupportActionBar().setTitle(titleRes);
             getSupportActionBar().setIcon(icon);
             mContent = fragment;
             getSupportFragmentManager()
                     .beginTransaction()
                     .replace(R.id.content_frame, fragment)
                     .addToBackStack(null)
                     .commit();
             menu.showContent();
         } else {
             switchContent(fragment, titleRes, icon);
         }
     }
 
     public void switchContent(Fragment fragment, String title, int icon) {
         getSupportActionBar().setTitle(title);
         getSupportActionBar().setIcon(icon);
         mContent = fragment;
         getSupportFragmentManager()
                 .beginTransaction()
                 .replace(R.id.content_frame, fragment)
                 .commit();
         menu.showContent();
     }
 
     public void switchContent(Fragment fragment, String title, int icon, boolean addToBackStack) {
         if(addToBackStack) {
             Log.d(TAG, "switchContent addToBackStack");
             getSupportActionBar().setTitle(title);
             getSupportActionBar().setIcon(icon);
             mContent = fragment;
             getSupportFragmentManager()
                     .beginTransaction()
                     .replace(R.id.content_frame, fragment)
                     .addToBackStack(null)
                     .commit();
             menu.showContent();
         } else {
             switchContent(fragment, title, icon);
         }
     }
 }
