 package com.micktaiwan.ptmdroid;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class PTMDroid extends Activity {
 
 	private Credentials credentials;
 	static final int SETTINGS_ACTIVITY = 1;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		credentials = new Credentials();
 		loadConfig();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.main, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle item selection
 		switch (item.getItemId()) {
 			case R.id.settings :
 				startActivityForResult(new Intent(this, Settings.class),
 						SETTINGS_ACTIVITY);
 				return true;
 			default :
 				return super.onOptionsItemSelected(item);
 		}
 	}
 
 	private void showAlert(String string) {
 		new AlertDialog.Builder(this).setTitle("Message").setMessage(string)
 				.setNeutralButton("Close",
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dlg, int sumthin) {
 								// do nothing – it will close on its own
 							}
 						}).show();
 	}
 
 	// why is it never called ???
	protected void onActivityResult(int requestCode, int resultCode,
			String data, Bundle extras) {
		loadConfig();
		showAlert("Coming back from Settings");
		if (requestCode == SETTINGS_ACTIVITY) {
			/*
			 * startActivity(new Intent(Intent.ACTION_VIEW, Uri
			 * .parse("http://google.com")));
			 */
 		}
 	}
 
 	protected void showToast(CharSequence text) {
 		Context context = getApplicationContext();
 		int duration = Toast.LENGTH_SHORT;
 		Toast toast = Toast.makeText(context, text, duration);
 		toast.show();
 	}
 
 	protected void loadConfig() {
 		new Config().load(getApplicationContext(), credentials);
 		TextView t = (TextView) findViewById(R.id.status);
 		if (credentials.login.equals("")) {
 			t.setText("Click on the MENU button to set your credentials");
 		} else {
 			t.setText("Logged as " + credentials.login);
 		}
 	}
 
 }
