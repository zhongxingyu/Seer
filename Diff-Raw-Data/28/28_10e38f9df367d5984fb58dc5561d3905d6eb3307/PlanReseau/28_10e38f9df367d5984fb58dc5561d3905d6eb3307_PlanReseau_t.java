 package fr.lemet;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
import android.text.Layout;
import android.view.Display;
 import android.view.View;
 import android.widget.Button;
 import android.webkit.WebSettings;
 import android.webkit.WebView;
 
 public class PlanReseau extends Activity {
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.plan_reseau);
         WebView planweb = (WebView)findViewById(R.id.webview);
        planweb.setInitialScale(70);
        //Display display = getWindowManager().getDefaultDisplay();
        //int width=display.getWidth();
         WebSettings planSettings = planweb.getSettings();
         planSettings.setSupportZoom(true);
         planSettings.setBuiltInZoomControls(true);
        //planSettings.setLoadWithOverviewMode(true);
         planSettings.setUseWideViewPort(true);
        //planSettings.setDefaultZoom(WebSettings.ZoomDensity.FAR);
         planweb.loadUrl("file:///android_asset/planlemet.png");
         //planSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
         //planweb.loadDataWithBaseURL("file:///android_asset/","<html><center><img src=\"planlemet.png\" ali></html>","text/html","utf-8","");
        //planweb.loadDataWithBaseURL("file:///android_asset/","<html><head><style type='text/css'>body{margin:O 0; text-align:center;} img{border:0px; text-align:center;} </style></head><body><img src=\"planlemet.png\" /></body></html>" ,"text/html",  "UTF-8","");

     }


 }
