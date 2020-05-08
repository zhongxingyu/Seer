 package ru.yap.mobile_old;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.Window;
 import android.webkit.WebView;
 
 public class GifActivity extends Activity {
 
 	@SuppressLint("SetJavaScriptEnabled")
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
 		//requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
 
 		setContentView(R.layout.gif_activity);
 	        
 		WebView webview = (WebView) findViewById(R.id.webview);
 
 		webview.getSettings().setJavaScriptEnabled(true);
 
		webview.loadData(
 			"<!DOCTYPE html>" +
 			"<html style='background-color:#000;margin:0;padding:0;height:100%;width:100%;'>" +
 				"<head></head>" + 
 				"<body style='margin:0;padding:0;width:100%;height:100%;'>" +
 				"<table style='width:100%;height:100%;'><tr style='padding:0;margin:0;'><td align=center style='margin:0;padding:0;'>" +
					"<img src='" + getIntent().getStringExtra("url") + "' id='img' style='display:block;margin:0;padding:0;width:100%;height:auto' onclick='var x = document.getElementById(\"img\").style.height;document.getElementById(\"img\").style.height = document.getElementById(\"img\").style.width; document.getElementById(\"img\").style.width = x'>" +
 				"</td></tr></table>" +
 				"</body>" + 
 			"</html>",
			"text/html", "UTF-8"
 		);
 	}
 	
 }
 
 //EOF
