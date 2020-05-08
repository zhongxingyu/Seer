 package edu.unr.cse.paintmobile3d;
 
 import android.net.Uri;
 import android.os.Bundle;
 import android.preference.Preference;
 import android.preference.Preference.OnPreferenceClickListener;
 import android.preference.PreferenceActivity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.support.v4.app.NavUtils;
 
 public class SettingsActivity extends PreferenceActivity {
 	
 	final Context context = this;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		addPreferencesFromResource(R.xml.settings);
 		
 		Preference mypref = (Preference) findPreference("setting_github");
         mypref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
         	@Override
 			public boolean onPreferenceClick(Preference mypref) {
 				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/nathanjordan/PaintMobile3D/"));
 				startActivity(browserIntent);
 				return true;
 			}
         });
         
 		Preference mypref2 = (Preference) findPreference("setting_dev_info");
         mypref2.setOnPreferenceClickListener(new OnPreferenceClickListener() {
         	@Override
 			public boolean onPreferenceClick(Preference mypref) {
     			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
     					context);
     	 
     				// set title
     				alertDialogBuilder.setTitle("Developers");
     	 
     				// set dialog message
     				alertDialogBuilder
    					.setMessage("Nathan Jordan\nThomas Kelly\nHalim Cagri Ates")
     					.setCancelable(false)
     					.setPositiveButton("OK",new DialogInterface.OnClickListener() {
     						public void onClick(DialogInterface dialog,int id) {
     							// if this button is clicked, just close
     							// the dialog box and do nothing
     							dialog.cancel();
     						}
     					});
     	 
     					// create alert dialog
     					AlertDialog alertDialog = alertDialogBuilder.create();
     	 
     					// show it
     					alertDialog.show();
 				return true;
 			}
         });
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			// This ID represents the Home or Up button. In the case of this
 			// activity, the Up button is shown. Use NavUtils to allow users
 			// to navigate up one level in the application structure. For
 			// more details, see the Navigation pattern on Android Design:
 			//
 			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
 			//
 			NavUtils.navigateUpFromSameTask(this);
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 }
