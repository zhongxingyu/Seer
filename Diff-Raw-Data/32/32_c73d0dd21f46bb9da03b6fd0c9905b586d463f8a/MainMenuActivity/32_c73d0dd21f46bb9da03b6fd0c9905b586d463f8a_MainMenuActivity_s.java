 package com.parse.starter;
 
 import com.parse.ParseGeoPoint;
 import com.parse.ParseObject;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.LinearLayout;
 import android.widget.ScrollView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MainMenuActivity extends Activity {
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main_menu);
 
 		ImageButton nearMe = (ImageButton) findViewById(R.id.near_me_btn);
 		nearMe.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				if (!ParseApplication.checkConn(MainMenuActivity.this)) {
 					Toast.makeText(MainMenuActivity.this, "HoosEating needs an internet connection. Please enable wifi/data!", Toast.LENGTH_LONG).show();
 				}
 				else {
 					Intent myIntent = new Intent(MainMenuActivity.this, NearMeActivity.class);
 					MainMenuActivity.this.startActivity(myIntent);
 				}
 			}
 		});
 		ImageButton comingUp = (ImageButton) findViewById(R.id.coming_up_btn);
 		comingUp.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				if (!ParseApplication.checkConn(MainMenuActivity.this)) {
 					Toast.makeText(MainMenuActivity.this, "HoosEating needs an internet connection. Please enable wifi/data!", Toast.LENGTH_LONG).show();
 				}
 				else {
 					Intent myIntent = new Intent(MainMenuActivity.this, ListEventsActivity.class);
 					MainMenuActivity.this.startActivity(myIntent);
 				}
 			}
 		});
 
 		if (!ParseApplication.checkConn(this)) {
 			AlertDialog.Builder dialog = new AlertDialog.Builder(this);
 			dialog.setTitle("Turn on wifi/data");
 			dialog.setMessage("HoosEating needs an internet connection. Please enable wifi/data, and GPS if you're outside!");
 			dialog.setNeutralButton("OK", new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int id) {
 					dialog.cancel();
 				}
 			});
 			dialog.show();
 		}
 	}
 }
