 package com.twormobile.mytravelasia;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.Build;
 import android.os.Bundle;
 import android.view.View;
 import android.webkit.WebSettings;
 import android.webkit.WebView;
 import android.widget.Button;
 import com.twormobile.mytravelasia.http.FeedIntentService;
 
 /**
  * An activity which serves as the startup entry point of the app. It shows an overview about Philippines, and some ads at
  * the bottom.
  *
  * @author avendael
  */
 public class StartUpActivity extends Activity {
     private static final String TAG = StartUpActivity.class.getSimpleName();
     private static final String ADS_URL = "http://www.mytravel-asia.com/mobile/adsense";
 
     private WebView mWvAds;
 
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.startup_activity);
 
         mWvAds = (WebView) findViewById(R.id.wv_ads);
         Button enterButton = (Button) findViewById(R.id.btn_enter);
 
         enterButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 Intent mainIntent = new Intent(StartUpActivity.this, MainActivity.class);
 
                 startActivity(mainIntent);
             }
         });
 
         initAds();
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         mWvAds.loadUrl(ADS_URL);
 
         Intent getFeedIntent = new Intent(this, FeedIntentService.class);
         startService(getFeedIntent);
     }
 
     private void initAds() {
         WebSettings settings = mWvAds.getSettings();
         mWvAds.setSaveEnabled(true);
         mWvAds.setBackgroundColor(Color.TRANSPARENT);
 
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
             mWvAds.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
         }
 
         settings.setLoadsImagesAutomatically(true);
         settings.setBlockNetworkLoads(false);
         settings.setBlockNetworkImage(false);
         settings.setJavaScriptEnabled(true);
     }
 }
