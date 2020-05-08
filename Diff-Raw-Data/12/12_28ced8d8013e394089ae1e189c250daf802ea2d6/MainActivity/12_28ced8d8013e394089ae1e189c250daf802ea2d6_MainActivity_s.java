 package com.dekel.babysitter;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import com.google.android.gms.common.ConnectionResult;
 import com.google.android.gms.common.GooglePlayServicesUtil;
 
 import java.util.LinkedList;
 
 public class MainActivity extends Activity {
 
     private enum SliderState {
         SHOWING_TOS,
         TOS_APPROVED_SHOWING_INTRO,
     }
 
     // TODO add hours matching to avoid false positives. // TODO hours change registration as well.
     private SliderState sliderState = SliderState.SHOWING_TOS;
     private BabyRepo babyRepo = null;
 
 
     /**
      * Called when the activity is first created.
      */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         babyRepo = new BabyRepo(this);
 
         showRelevantHome();
 
         startService(new Intent(this, BabyMonitorService.class));
 
         Log.d(Config.MODULE_NAME, "MainActivity - Init done!");
     }
 
     private void showRelevantHome() {
         if (!babyRepo.isFirstTimeCompleted()) { // First time!
             showFirstTimeSlider();
 
         } else {
             showStatefulHome();
         }
     }
 
     private void showFirstTimeSlider() {
         setContentView(R.layout.terms_of_service);
 
         sliderState = SliderState.SHOWING_TOS;
 
         ((TextView) findViewById(R.id.mainTitleTextView)).setTypeface(FontUtils.getTypeface(this));
 
         final TextView bodyView = (TextView) findViewById(R.id.mainBodyTextView1);
         bodyView.setTypeface(FontUtils.getTypeface(this));
         final TextView subtitleView = (TextView) findViewById(R.id.mainSubtitleTextView);
         subtitleView.setTypeface(FontUtils.getTypefaceBold(this));
 
         if (isSmallScreen()) {
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
             lp.setMargins(20, 20, 20, 0);
             bodyView.setLayoutParams(lp);
         }
 
         final Button continueButton = (Button) findViewById(R.id.button);
         continueButton.setTypeface(FontUtils.getTypeface(this));
         continueButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View view) {
             switch (sliderState) {
                 case SHOWING_TOS:
                     sliderState = SliderState.TOS_APPROVED_SHOWING_INTRO;
                     // Live switching
                     subtitleView.setText(R.string.about_subtitle);
                     bodyView.setText(R.string.about_text);
                     continueButton.setText(R.string.about_continue);
                     break;
 
                 case TOS_APPROVED_SHOWING_INTRO:
                     babyRepo.setFirstTimeCompleted();
                     showStatefulHome();
                     break;
             }
             }
         });
     }
 
     private boolean isSmallScreen() {
        return getWindowManager().getDefaultDisplay().getHeight() <= 800;
     }
 
     private void showStatefulHome() {
         setContentView(R.layout.main);
 
         if (isSmallScreen()) {
             DisplayMetrics metrics = new DisplayMetrics();
             getWindowManager().getDefaultDisplay().getMetrics(metrics);
             float logicalDensity = metrics.density;
             int px = (int) (220 * logicalDensity + 0.5);
 
             findViewById(R.id.mainImageLayout).setLayoutParams(
                    new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, px));
         }
 
 
         ((TextView) findViewById(R.id.mainTitleTextView)).setTypeface(FontUtils.getTypeface(this));
 
         final TextView bodyView = (TextView) findViewById(R.id.mainBodyTextView1);
         bodyView.setTypeface(FontUtils.getTypeface(this));
         final TextView subtitleView = (TextView) findViewById(R.id.mainSubtitleTextView);
         subtitleView.setTypeface(FontUtils.getTypefaceBold(this));
 
         findViewById(R.id.mainImageReady).setOnClickListener(new View.OnClickListener() {
             public void onClick(View view) {
                 easterEggHandling();
             }
         });
 
         if (babyRepo.isRideInProgress()) {
             subtitleView.setText(R.string.started_title);
             bodyView.setText(babyRepo.isBabyInCar() ? R.string.started_baby_in_car_text : R.string.started_baby_not_in_car_text);
         }
 
         if (!isGPSAvailable()) {
             subtitleView.setText(R.string.error_title);
             bodyView.setText(R.string.error_text);
             ((ImageView) findViewById(R.id.mainImageReady)).setImageResource(R.drawable.icon_x);
         }
     }
 
     private boolean isGPSAvailable() {
         Log.d(Config.MODULE_NAME, "GooglePlay: " + GooglePlayServicesUtil.getErrorString(GooglePlayServicesUtil.isGooglePlayServicesAvailable(this)));
         return ((LocationManager) getSystemService(Context.LOCATION_SERVICE)).isProviderEnabled(LocationManager.GPS_PROVIDER) &&
                 GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS;
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         showRelevantHome();
     }
 
     LinkedList<Long> easterEggTimestamps = new LinkedList<Long>();
     private void easterEggHandling() {
         Log.d(Config.MODULE_NAME, "easter_egg " + System.currentTimeMillis());
         easterEggTimestamps.add(System.currentTimeMillis());
         if (easterEggTimestamps.size() < 7) {
             return;
         }
         if (easterEggTimestamps.poll() > System.currentTimeMillis() - 2*1000) {
             Log.w(Config.MODULE_NAME, "easter_egg");
             startActivity(new Intent(this, DebugActivity.class));
         }
     }
 }
