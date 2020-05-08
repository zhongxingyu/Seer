 package edu.berkeley.cs160.groupp;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.content.ActivityNotFoundException;
 import android.content.Context;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class IntroPage extends Activity {
 	Button searchName, searchNum, recentSearches, tutorial, favorites;
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         favorites = (Button) findViewById(R.id.preferencesButton);
         tutorial = (Button) findViewById(R.id.tutorialButton);
         recentSearches = (Button) findViewById(R.id.recentButton);
         searchName = (Button) findViewById(R.id.searchNameButton);
         searchNum = (Button) findViewById(R.id.searchNumberButton);
         final Context c = this;
         //TODO: move all these on click listeners somewhere more elegant
         tutorial.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				// Bring up Dialog containing necessary overview + help for application.
 				final Dialog dialog = new Dialog(IntroPage.this);
 				dialog.setContentView(R.layout.help);
 				dialog.setTitle("Tutorial");
 				dialog.setCancelable(true);
 		
 				Button button = (Button) dialog
 						.findViewById(R.id.returntoapp);
 				button.setOnClickListener(new OnClickListener() {
 					@Override
 					public void onClick(View v) {
 						dialog.cancel();
 					}
 				});
 
 				dialog.show();
 			}});
         
         recentSearches.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Intent i = new Intent(c, RecentSearches.class);
 				startActivityForResult(i, 1);
 			}});
         
         searchName.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
				Intent myIntent = new Intent(c, PhoneListView.class);
 				startActivityForResult(myIntent, 0);
 			}});
         
         searchNum.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Intent i = new Intent(IntroPage.this, SearchPage.class);
 				startActivityForResult(i, 1);
 			}});
         
         favorites.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Intent i = new Intent(c, FavoriteSearches.class);
 				startActivityForResult(i, 1);
 			}});
     }
     
     /**
      * This doesn't actually belong here right now but I wanted to
      * note the code for later. JS
      * 
      * When called, starts a phone call with the provided argument
      * as the phone number to call.
      * 
      * @param A string representing a telephone number, in the format
      * 		  "tel:[number]", e.g. "tel:1234567890".
      */
     private void call(String number) {
     	try {
     		Intent callIntent = new Intent(Intent.ACTION_CALL);
     		callIntent.setData(Uri.parse(number));
     		startActivity(callIntent);
     	} catch (ActivityNotFoundException e) {
     		e.printStackTrace();
     	}
     }
 
 }
