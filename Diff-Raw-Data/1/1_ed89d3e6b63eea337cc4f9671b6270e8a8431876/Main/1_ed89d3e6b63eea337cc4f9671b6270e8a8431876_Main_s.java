 /*
  *  Copyright 2013-2014 Jeroen Gorter <Lowerland@hotmail.com>
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 
 package nl.dreamkernel.s4.tweaker;
 
 import nl.dreamkernel.s4.tweaker.R;
 import nl.dreamkernel.s4.tweaker.bugs.BugsReporter;
 import nl.dreamkernel.s4.tweaker.util.FileCheck;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceFragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.FrameLayout;
 import android.widget.Toast;
 
 public class Main extends Activity {
 	static final String TAG = "S4Tweaker";
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		getFragmentManager().beginTransaction()
 				.replace(android.R.id.content, new MyPreferenceFragment())
 				.commit();
 		// setContentView(R.layout.main);
 		// getActionBar().hide();
 
 		// Usage counter
 		SharedPreferences sharedPreferences = getSharedPreferences(
 				"MY_SHARED_PREF", 0);
 		int usage_counter = sharedPreferences.getInt("usage_counter", 0);
 		SharedPreferences.Editor editor = sharedPreferences.edit();
 		editor.putInt("usage_counter", usage_counter + 1);
 		editor.commit();
 		Log.d(TAG, "Runned This App " + usage_counter + " Times now");
 	}
 
 	/*
 	 * @Override public boolean onCreateOptionsMenu(Menu menu) { // Inflate the
 	 * menu; this adds items to the action bar if it is present.
 	 * getMenuInflater().inflate(R.menu.main, menu); return true; }
 	 */
 
 	public static class MyPreferenceFragment extends PreferenceFragment {
 		@Override
 		public void onCreate(final Bundle savedInstanceState) {
 			super.onCreate(savedInstanceState);
 			addPreferencesFromResource(R.xml.main_pref);
 		}
 	}
 
 	@Override
 	protected void onResume() {
 		// TODO Auto-generated method stub
 		super.onResume();
 		// Shows a notice if bug report was succesfull
 		if (BugsReporter.bugrecieved == true) {
 			Toast.makeText(Main.this, "Bug Report Successful",
 					Toast.LENGTH_LONG).show();
 			Toast.makeText(Main.this, "Thank You For Your Support  :)",
 					Toast.LENGTH_LONG).show();
 			BugsReporter.bugrecieved = false;
 		}
 		Log.d(TAG, "onResume() " + FileCheck.isRootEnabled());
 		if (!FileCheck.isRootEnabled() == true) {
 			Log.d(TAG,
 					"FileCheck.isRootEnabled() = " + FileCheck.isRootEnabled());
 			// Show Root required alert
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			final FrameLayout frameView = new FrameLayout(this);
 			builder.setView(frameView);
 			final AlertDialog norootDialog = builder.create();
 			norootDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK",
 					new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog,
 								int whichButton) {
 							// Button OK Clicked
 							// Exit App
 							finish();
 						}
 					});
 			LayoutInflater inflater = norootDialog.getLayoutInflater();
 			@SuppressWarnings("unused")
 			View dialoglayout = inflater.inflate(R.layout.no_root_alert,
 					frameView);
 			norootDialog.show();
 		} else {
 			Log.d(TAG, "Got root access");
 		}
 	}
 }
