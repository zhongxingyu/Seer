 package com.yoosufnabeel.mvscholarships;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.text.Html;
 import android.webkit.WebView;
 import android.content.Intent;
 import android.widget.AbsListView;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 
 public class DocumentViewActivity extends Activity {
 
    //created by nabeel:
    //date: 30th August 2013

     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         Intent documentIntent = getIntent();
         String url = documentIntent.getStringExtra("url");;
 
         WebView mWebView = new WebView(DocumentViewActivity.this);
         mWebView.getSettings().setJavaScriptEnabled(true);
         mWebView.getSettings().setPluginsEnabled(true);
         mWebView.loadUrl("https://docs.google.com/gview?embedded=true&url=" + url);
         setContentView(mWebView);
     }
 
 
 }
 
