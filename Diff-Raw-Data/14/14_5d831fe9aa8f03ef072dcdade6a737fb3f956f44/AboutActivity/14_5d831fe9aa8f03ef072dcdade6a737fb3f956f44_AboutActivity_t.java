 package de.codenauts.hockeyapp;
 
 import net.hockeyapp.android.R;
 import android.app.Activity;
 import android.content.Intent;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
import android.graphics.Color;
 import android.os.Bundle;
 import android.view.MenuItem;
 import android.webkit.WebView;
 import de.codenauts.hockeyapp.util.ActivityHelper;
 
 public class AboutActivity extends Activity {
   final ActivityHelper activityHelper = ActivityHelper.createInstance(this);
   
   @Override
   public void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
 
     setContentView(R.layout.web_view);
     setTitle(null);
     
    activityHelper.setupActionBar(null, Color.BLACK);
     activityHelper.setupHomeAsUp();
     activityHelper.hideTitle();
     
     loadAppInfo();
   }
 
   private void loadAppInfo() {
     StringBuilder builder = new StringBuilder();
     builder.append("<html><head><style type='text/css'>h1 { font-size: 110%; }\ndd { padding-bottom: 10px; }\nbody { padding: 10px }</style></head></body>");
     
     builder.append("<h1>About</h1>");
     builder.append("<dl>");
     builder.append("<dt>Package:</dt>");
     builder.append("<dd>" + getPackageName() + "</dd>");
     
     PackageManager packageManager = getPackageManager();
     try {
       PackageInfo packageInfo = packageManager.getPackageInfo(getPackageName(), 0);
       builder.append("<dt>Version Name:</dt>");
       builder.append("<dd>" + packageInfo.versionName + "</dd>");
       builder.append("<dt>Version Code:</dt>");
       builder.append("<dd>" + packageInfo.versionCode + "</dd>");
     } 
     catch (Exception e) {
     }
 
     builder.append("<h1>License</h1>");
     builder.append("<p>");
     builder.append(getResources().getString(R.string.license).replaceAll("\n", "<br>"));
     builder.append("</p>");
 
     WebView webView = (WebView)findViewById(R.id.web_view);
    webView.loadDataWithBaseURL("https://rink.hockeyapp.net/", builder.toString(), "text/html", "UTF-8", null);
   }
 
   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
     switch (item.getItemId()) {
     case android.R.id.home:
       Intent intent = new Intent(this, MainActivity.class);
       intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
       startActivity(intent);
       return true;
     default:
       return super.onOptionsItemSelected(item);
     }
   }
 }
