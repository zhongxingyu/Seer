 package com.messedagliavr.messeapp;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.net.ConnectivityManager;
import android.net.Uri;
 import android.os.Bundle;
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
 
 	public void social(View view) {
 		setContentView(R.layout.social);
 	}
 	
	public void voti(View view) {
		Intent voti = new Intent(Intent.ACTION_VIEW);
		voti.setData(Uri.parse("http://atv.infoschool.eu/VRLS0003"));
		startActivity(voti);
	}
	
	public void youtube(View view) {
		Intent youtube = new Intent(Intent.ACTION_VIEW);
		youtube.setData(Uri.parse("http://www.youtube.com/user/MessedagliaWeb"));
		startActivity(youtube);
	}
	
	public void facebook(View view) {
		Intent facebook = new Intent(Intent.ACTION_VIEW);
		facebook.setData(Uri.parse("http://www.facebook.com/groups/110918169016604/"));
		startActivity(facebook);
	}
	
 	public void news(View view) {
 		if (CheckInternet() == true) {
 			setContentView(R.layout.list_item);
 			startActivity(new Intent(this, news.class));
 		} else {
 			Toast.makeText(MainActivity.this, R.string.noconnection,
 					Toast.LENGTH_LONG).show();
 		}
 	}
 }
