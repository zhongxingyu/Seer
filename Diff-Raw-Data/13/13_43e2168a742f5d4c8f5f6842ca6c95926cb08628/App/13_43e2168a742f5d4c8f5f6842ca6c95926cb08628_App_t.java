 package org.door43.en.stories.compassionate;
 
 import android.app.Activity;
 import android.os.Bundle;
 import com.phonegap.*;
import android.webkit.DownloadListener;
import android.content.pm.ActivityInfo;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

 
 public class App extends DroidGap {
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         super.loadUrl("file:///android_asset/www/index.html");
         
         appView.setDownloadListener(new DownloadListener() 
         { 
             public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimeType, long size) 
             { 
                 Intent viewIntent = new Intent(Intent.ACTION_VIEW); 
                viewIntent.setDataAndType(Uri.parse(url), "video/mp4"); 
                 try 
                 { 
                     startActivity(viewIntent); 
                 } 
                 catch (ActivityNotFoundException ex) 
                 { 
                    Log.w("VideoError: ", "Couldn't find activity to view mimetype: " + mimeType); 
                 } 
             } 
         }); 
     }
 }
