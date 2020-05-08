 package com.magichat;
 
 import java.io.IOException;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.widget.Toast;
 
 public class Splash extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.splash);
 
 		new setupDb().execute();
 
 		Thread timer = new Thread() {
 			public void run() {
 				try {
 					sleep(3000);
 				} catch (InterruptedException iE) {
 					iE.printStackTrace();
 				} finally {
 					Intent openMagicHatMain = new Intent(
 							"com.magichat.MAGICHATMAIN");
 					startActivity(openMagicHatMain);
 				}
 			}
 		};
 		timer.start();
 	}
 
 	private class setupDb extends AsyncTask<String, Integer, String> {
 		boolean isCreated = false, isUpgrade = false;
 
 		@Override
 		protected void onPreExecute() {
 			super.onPreExecute();
 
 			if (!MagicHatDB.isCreated()) {
 				Toast.makeText(Splash.this,
 						"Initializing database... Please wait...",
 						Toast.LENGTH_SHORT).show();
 				isCreated = true;
			} else {
				Toast.makeText(Splash.this, "Checking database...",
 						Toast.LENGTH_SHORT).show();
 			}
 		}
 
 		@Override
 		protected String doInBackground(String... params) {
			MagicHatDB mhDb = new MagicHatDB(Splash.this);
 
 			mhDb.openReadableDB();
			isUpgrade = mhDb.isUpgrade();
 			mhDb.closeDB();

 			try {
 				CardDbUtil.initCardDb(Splash.this);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(String result) {
 			super.onPostExecute(result);
 
 			if (isCreated) {
 				Toast.makeText(Splash.this,
 						"Database Initialization Complete.", Toast.LENGTH_SHORT)
 						.show();
 			} else if (isUpgrade) {
 				Toast.makeText(Splash.this, "Database Upgrade Complete.",
 						Toast.LENGTH_SHORT).show();
 			}
 		}
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		finish();
 	}
 
 }
