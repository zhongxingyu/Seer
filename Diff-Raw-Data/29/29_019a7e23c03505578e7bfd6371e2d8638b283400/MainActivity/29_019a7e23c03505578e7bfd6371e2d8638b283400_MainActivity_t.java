/*
    Copyright 2013 Jacob Stuart

    This file is part of QuickBTC.

    QuickBTC is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Foobar is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Foobar.  If not, see <http://www.gnu.org/licenses/>. */

 package com.spacejake.quickbtc;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.text.DecimalFormat;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.ContextThemeWrapper;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.webkit.WebView;
 import android.view.View;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MainActivity extends Activity {
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         update();
     
     }
     public void update (){
         TextView display = (TextView) findViewById(R.id.displayPrice);
         display.setText(R.string.ellipsis);
        new RetrieveFeedTask().execute();
         return;
     }
     public boolean updateButtonClicked(MenuItem item) {
     	update();
 		return false;
     }
     public void textClicked(View view) {
         update();
     }
     
     public boolean infoButtonClicked(MenuItem item){
     	//displays html formatted alert dialog - change assets/info.html to change content
         ContextThemeWrapper wrapper = new ContextThemeWrapper(this, android.R.style.Theme_Holo);
     	AlertDialog.Builder ad = new AlertDialog.Builder(wrapper);
     	WebView wv = new WebView (this);
     	wv.loadUrl("file:///android_asset/info.html");
     	wv.getSettings().setDefaultTextEncodingName("utf-8");
     	ad
     		.setTitle("Would you like to donate?")
     		.setView(wv);
     		//.setCancelable(true)
     		ad.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
 
                 public void onClick(DialogInterface dialog, int which) {
                 	Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://donate.spacejake.com"));
                 	startActivity(browserIntent);
 
                 }
             })
     		.setNegativeButton("No", new DialogInterface.OnClickListener() {
 
                 public void onClick(DialogInterface dialog, int which) {
                 	dialog.cancel();
                 }
             });
     	AlertDialog alertDialog = ad.create();
     	alertDialog.show();
     	return false;
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
     @Override
     protected void onResume () {
     	//updates price when app is resumed
         update();
     	super.onResume();
     	return;
     }
 
     protected void onPause(){
     	//cancels AsyncTask when app is paused
        if(!new RetrieveFeedTask().isCancelled()){
    		new RetrieveFeedTask().cancel(true);
     	}
     	super.onPause();
     }
   
public class RetrieveFeedTask extends AsyncTask<String, Void, Void> {
 	
 	public String price = "0.00";
     	@Override
     	protected Void doInBackground(String... arg0) {
     		URL url;
     		try {
     			// get url content
     			url = new URL("http://blockchain.info/q/24hrprice");
     			URLConnection conn = url.openConnection();
      
     			// open  stream and put  in br
     			BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
     			StringBuilder total = new StringBuilder();
     			String value;
     			while ((value = reader.readLine()) != null) {
     			    total.append(value);
     			}
                 //format double to two decimals
     			DecimalFormat twoDs = new DecimalFormat("#.##");
     			price = String.valueOf(twoDs.format(Double.valueOf(total.toString())));
     			
     		} catch (MalformedURLException e) {
     			e.printStackTrace();
     		} catch (IOException e) {
     			e.printStackTrace();
     			runOnUiThread(new Runnable() {
     				public void run() {
     				    Toast.makeText(MainActivity.this, "Error! Check your internet connection.", Toast.LENGTH_SHORT).show();
     				    }
     				});
     		}
     		return null;
     		
     		}
     	@Override
         protected void onPostExecute(Void result) {
             //update price on ui thread
     	  TextView display = (TextView) findViewById(R.id.displayPrice);
           display.setText(price);
         }
 
 
     }
 
     
     
 }
