 package com.newsrss.Feed;
 
 import android.app.Activity;
 import android.content.pm.ActivityInfo;
 import android.content.res.Resources;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.view.View;
 import android.webkit.WebView;
 import android.widget.ImageButton;
 import android.widget.TextView;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 
 /**
  * Created with IntelliJ IDEA.
  * User: AndrewTivodar
  * Date: 18.04.13
  * Time: 15:21
  * To change this template use File | Settings | File Templates.
  */
 public class Legal extends Activity {
 
     WebView legalDescription;
     ImageButton backBtn;
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
         setContentView(R.layout.legal);
         backBtn = (ImageButton) findViewById(R.id.legal_AB_backButton);
         legalDescription = (WebView) findViewById(R.id.legal_description);
 
         legalDescription.getSettings().setJavaScriptEnabled(true);
        legalDescription.loadUrl("file:///android_res/raw/legal.html");
         // legalDescription.getSettings().setJavaScriptEnabled(true);
        //String legalDoc = "android.resource://" + getPackageName() + "/raw/"+R.raw.legal;
         //legalDescription.loadData("<html><body>" + legalDoc + "</body></html>", "text/html", "UTF-8");
         //File file = new File(Environment.getExternalStorageDirectory()
         //       + "/legal.html");
         //Uri uri = Uri.fromFile(file);
         //legalDescription.loadUrl(uri.toString());
        //legalDescription.loadUrl(legalDoc);
        ImageButton.OnClickListener ocBack = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        } ;
 
         backBtn.setOnClickListener(ocBack);
     }
 }
