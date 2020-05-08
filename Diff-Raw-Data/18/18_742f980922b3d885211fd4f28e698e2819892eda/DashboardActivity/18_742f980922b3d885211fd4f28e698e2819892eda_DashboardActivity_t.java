 package com.example.sprint;
 
 import com.compuware.apm.uem.mobile.android.CompuwareUEM;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Build;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.Menu;
 import android.view.View;
 import android.widget.EditText;
 
 public class DashboardActivity  extends ABSFragmentActivity {
 	private static final int EDIT_ID = Menu.FIRST+2;
 	private final static int BARCODE_SCAN_REQUEST = 2345;
 
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);       
         setContentView(R.layout.dashboard); 
     }
     
     
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
 		String user_name = prefs.getString(getResources().getString(R.string.pref_sprintUsername_key), "");
 
 		/* if the username is "" */
 		if (user_name.length()==0){
 
 			displayUserNameAlert();
 		}
 
 		if( hazTehWifiz() == false) {
 
 			createNetworkDisabledAlert();
 		}
 		
 	}
 
 	private boolean hazTehWifiz() {
 
 		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
 		
 		//check and see first if the network has connectivity at all.  
 		if (networkInfo != null && networkInfo.isConnected()) {
 
 			ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 			
 			//next if the phone has connectivity, make sure it is connected via wi-fi
 			if( ! cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting() ) {
 
 				//there is a connection but it is not wifi...
 				return false;
 			}	
 
 			//connection exists and itsa-da wifi
 			return true;
 		} else {
 
 			//no connection
 			return false;
 		}
 	}
 
 	private void createNetworkDisabledAlert(){
 
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setMessage(
 				"Your wi-fi connection is not active!  You will need to connect to the Compuware network to use Sprint.  ")
 				.setCancelable(false)
 				.setPositiveButton("Enable Wi-Fi",
 						new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int id) {
 						showNetworkOptions();
 					}
 				});
 		builder.setNegativeButton("Cancel",
 				new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int id) {
 				dialog.cancel();
 			}
 		});
 		AlertDialog alert = builder.create();
 		alert.show();
 	}
 
 	private void showNetworkOptions() {
 
 		Intent i = new Intent(android.provider.Settings.ACTION_SETTINGS);
 		startActivityForResult(i, 5);
 	}
 
 	private void displayUserNameAlert() {
 		AlertDialog.Builder alert = new AlertDialog.Builder(this);
 
 		alert.setTitle(getResources().getString(R.string.pref_sprintUsername_Title));
 		alert.setMessage(getResources().getString(R.string.pref_sprintUsername_summary));
 
 		// Set an EditText view to get user input 
 		final EditText input = new EditText(this);
 		input.setHint(R.string.sprintUsername_hint);
 		alert.setView(input);
 
 		alert.setPositiveButton(getResources().getString(R.string.confirm), new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int whichButton) {
 				String value = input.getText().toString();
 				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
 				Editor editor = prefs.edit();
 				editor.putString(getResources().getString(R.string.pref_sprintUsername_key), value);
 				editor.commit();
 			}
 		});
 
 		alert.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int whichButton) {
 				// Canceled.
 			}
 		});
 
 		alert.show();
 
 	}
 
 	public void startScanner(View view) {
 
 		Intent i = new Intent();	
 		/* setting the action string.  No other apps should respond to this request
 		 * because of the unique action string
 		 */
 		i.setAction("com.compuware.pdp.sprint");
 		i.putExtra("SCAN_MODE", "QR_CODE_MODE");
 		startActivityForResult(i, BARCODE_SCAN_REQUEST);
 		
 		//dynaTrace Metric for scanning
 		CompuwareUEM.enterAction("scannerActivity");
 	}
 
 
 	public boolean startPreferences(View view){
 		if (Build.VERSION.SDK_INT<Build.VERSION_CODES.HONEYCOMB) {
 			startActivity(new Intent(this, EditPreferences.class));
 			return true;
 		}
 		else {
 			Intent intent = new Intent( this, EditPreferencesHC.class );
 			/* Adding extras to skip showing headers in the preference activity */
 			intent.putExtra( EditPreferencesHC.EXTRA_SHOW_FRAGMENT, StockPreferenceFragment.class.getName() );
 			intent.putExtra( EditPreferencesHC.EXTRA_NO_HEADERS, true );
 			startActivity(intent);
 			return true;
 		}
 	}
 
 	public void startPrinterListActivity(View view){
 		Intent intent = new Intent(this, PrinterListActivity.class);
 		startActivity(intent);
 	}
 
 	public void startJobListActivity(View view){
 		Intent intent = new Intent(this, JobListActivity.class);
 		startActivity(intent);
 	}
 
 	@Override
 	protected void goHome(){
 
 	}
 }
