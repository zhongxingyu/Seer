 package com.messedagliavr.messeapp;
 
 
 import android.net.ConnectivityManager;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Toast;
 
 public class MainActivity extends Activity {
 	
 	public boolean CheckInternet() {
 		ConnectivityManager connec = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 		android.net.NetworkInfo wifi = connec
 				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
 		android.net.NetworkInfo mobile = connec
 				.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
 
 		if (wifi.isConnected()) {
 			return true;
 		} else if (mobile.isConnected()) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
     public void news(View view) {
 		if (CheckInternet() == true) {
 			startActivity(new Intent(this, news.class));
 		} else {
 			Toast.makeText(MainActivity.this, R.string.noconnection,
 					Toast.LENGTH_LONG).show();
 		}
 	}
 }
