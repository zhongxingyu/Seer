 package de.codenauts.hockeyapp;
 
 import java.util.List;
 
 import de.codenauts.hockeyapp.util.ActivityHelper;
 
 import net.hockeyapp.android.R;
 import android.app.Activity;
 import android.content.Intent;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.os.Bundle;
 import android.view.MenuItem;
 import android.webkit.WebView;
 
 public class DebugActivity extends Activity {
   final ActivityHelper activityHelper = ActivityHelper.createInstance(this);
   
   @Override
   public void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
 
     setContentView(R.layout.web_view);
     setTitle(null);
     
     activityHelper.setupHomeAsUp();
     activityHelper.hideTitle();
     
     loadListOfApps();
   }
 
   private void loadListOfApps() {
     StringBuilder builder = new StringBuilder();
     builder.append("<html><head><style type='text/css'>h1 { font-size: 110%; }\nh2 { font-size: 100% };</style></head></body>");
     
     PackageManager packageManager = getPackageManager();
     List<PackageInfo> packages = packageManager.getInstalledPackages(0);
     for (PackageInfo packageInfo : packages) {
       builder.append("<h2>" + packageInfo.packageName + "</h2>");
       builder.append("<dl>");
       builder.append("<dt>Version Name:</dt>");
       builder.append("<dd>" + packageInfo.versionName + "</dd>");
       builder.append("<dt>Version Code:</dt>");
       builder.append("<dd>" + packageInfo.versionCode + "</dd>");
       builder.append("</dl>");
     }
 
     builder.append("</body></html>");
     
     WebView webView = (WebView)findViewById(R.id.web_view);
     webView.loadData(builder.toString(), "text/html", "UTF-8");
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
