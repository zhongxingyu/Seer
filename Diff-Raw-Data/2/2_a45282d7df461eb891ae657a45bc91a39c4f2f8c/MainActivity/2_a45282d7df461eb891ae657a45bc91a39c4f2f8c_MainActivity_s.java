 package com.messedagliavr.messeapp;
 
 import android.app.Activity;
 import android.content.ActivityNotFoundException;
 import android.content.Context;
 import android.content.Intent;
 import android.net.ConnectivityManager;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Toast;
 
 public class MainActivity extends Activity {
 	int layoutid;
 
 	public boolean CheckInternet() {
 		boolean connected = false;
 		ConnectivityManager connec = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 		android.net.NetworkInfo wifi = connec
 				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
 		android.net.NetworkInfo mobile = connec
 				.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
 
 		if (wifi.isConnected()) {
 			connected = true;
 		} else {
 			try {
 				if (mobile.isConnected())
 					connected = true;
 			} catch (Exception e) {
 			}
 
 		}
 
 		return connected;
 
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		layoutid = R.id.activity_main;
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 
 	public void send(View v) {
 		Intent intent = new Intent(Intent.ACTION_SEND);
 		intent.setType("message/rfc822");
 		int id = Integer.parseInt((String) v.getTag()); 
 		switch(id){
 		case 0:
			intent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{getString(R.string.dir_email),""});
 		case 1:
 			intent.putExtra(android.content.Intent.EXTRA_EMAIL, getString(R.string.vice_email));
 			break;
 		case 2:
 			intent.putExtra(Intent.EXTRA_EMAIL, getString(R.string.did_email));
 			break;
 		default:
 			break;
 		}
 
 		startActivity(Intent.createChooser(intent, "Send Email"));
 	}
 
 	public void onBackPressed() {
 
 		if (layoutid == R.id.info || layoutid == R.id.social) {
 			setContentView(R.layout.activity_main);
 			layoutid = R.id.activity_main;
 		} else {
 			super.finish();
 		}
 
 	}
 
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.info:
 			setContentView(R.layout.info);
 			layoutid = R.id.info;
 			break;
 		case R.id.exit:
 			super.finish();
 			break;
 		case R.id.contatti:
 
 			setContentView(R.layout.contatti);
 			layoutid = R.id.contatti;
 
 			break;
 		case R.id.orario:
 			startActivity(new Intent(this, timetable.class));
 			break;
 		}
 		return true;
 	}
 
 	public void social(View view) {
 		setContentView(R.layout.social);
 		layoutid = R.id.social;
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
 		String fbapp = "fb://group/110918169016604";
 		Intent fbappi = new Intent(Intent.ACTION_VIEW, Uri.parse(fbapp));
 		try {
 			startActivity(fbappi);
 		} catch (ActivityNotFoundException ex) {
 			String uriMobile = "http://touch.facebook.com/groups/110918169016604";
 			Intent fb = new Intent(Intent.ACTION_VIEW, Uri.parse(uriMobile));
 			startActivity(fb);
 		}
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
 
 	public void calendar(View view) {
 		if (CheckInternet() == true) {
 			setContentView(R.layout.list_item);
 			startActivity(new Intent(this, calendar.class));
 		} else {
 			Toast.makeText(MainActivity.this, R.string.noconnectioncalendar,
 					Toast.LENGTH_LONG).show();
 		}
 	}
 
 	public void notavailable(View view) {
 		Toast.makeText(MainActivity.this, R.string.notavailable,
 				Toast.LENGTH_LONG).show();
 	}
 }
