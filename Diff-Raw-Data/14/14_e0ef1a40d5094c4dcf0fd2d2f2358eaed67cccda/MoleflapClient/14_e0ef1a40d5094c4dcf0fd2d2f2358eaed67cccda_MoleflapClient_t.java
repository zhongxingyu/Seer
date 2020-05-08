 package de.c3d2.blitz.moleflap;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.net.URLEncoder;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.os.Environment;
 import android.preference.PreferenceManager;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 
 public class MoleflapClient extends Activity implements OnClickListener {
 	static final int DIALOG_NET_FAIL = 0;
 	static final int DIALOG_IMPORTED = 1;
 	static final int DIALOG_NO_TOKEN = 2;
 	
 	static final int TOKEN_LENGTH = 164;
 	
 	private Dialog createAlertDialog(String msg, String btnmsg) {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setMessage(msg);;
 		builder.setNeutralButton(btnmsg, new DialogInterface.OnClickListener() {
 
 			public void onClick(DialogInterface dialog, int arg1) {
 				dialog.dismiss();
 			}			
 		});
 		return builder.create();
 	}
 	
 	
     /* (non-Javadoc)
 	 * @see android.app.Activity#onCreateDialog(int)
 	 */
 	@Override
 	protected Dialog onCreateDialog(int id) {
 		// TODO Auto-generated method stub
 		switch (id) {
 		case DIALOG_NET_FAIL:
 			return createAlertDialog("Network error. Check your settings.", "Yes, Sir!");
 		case DIALOG_IMPORTED:
 			return createAlertDialog("Imported token from card. You can safely remove token.txt now.", "Okay");
 		case DIALOG_NO_TOKEN:
 			return createAlertDialog("No valid token. Put one on your card as token.txt and hit Import.", "I'll do that.");
 		default:
 			return null;
 		}		
 	}
 
 		
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onStart()
 	 */
 	@Override
 	protected void onStart() {
 		super.onStart();
 		checkDefaults();
 	}
 
 	private void checkDefaults() {
 		SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
 		SharedPreferences.Editor e = p.edit();
 		
 		if (p.getString("url", null) == null)
 			e.putString("url",  "http://moleflap.hq.c3d2.de/open?");
 		e.commit();
 	}
 	
 	private void checkTokenFile() {
 		SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
 		
 		String state = Environment.getExternalStorageState();
 		if (state.equals(Environment.MEDIA_MOUNTED) ||
 			state.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
 			File path = Environment.getExternalStorageDirectory();
 			File tokenfile = new File(path, "token.txt");
 			if (tokenfile.exists() && tokenfile.canRead()) {
 				try {
 					FileReader fr = new FileReader(tokenfile);					
 					BufferedReader in = new BufferedReader(fr);
 			        String str = in.readLine().trim();
 					in.close();
 					fr.close();
 					
 					if (str.length() == TOKEN_LENGTH) {
 						SharedPreferences.Editor e = p.edit();
 						e.putString("token", str);
 						e.commit();
 						showDialog(DIALOG_IMPORTED);
 						return;
 					}
 					// Fall through to dialog.
 				} catch (IOException e) {
 					// Race?
 				}
 			}			
 		} 
 		
 		showDialog(DIALOG_NO_TOKEN);
 	}
 
 
 
 	/** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);        
         setContentView(R.layout.main);
         
                 
         
         findViewById(R.id.open_btn).setOnClickListener(this);
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
     	getMenuInflater().inflate(R.menu.options_menu, menu);
 		return true;
     }
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.quit:
 			finish();
 			return true;
 		case R.id.settings:			
 			Intent intent = new Intent();
 			intent.setClassName("de.c3d2.blitz.moleflap", "de.c3d2.blitz.moleflap.Preferences");			
 			startActivity(intent);
 			return true;
 		case R.id.import_token:
 			checkTokenFile();
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);	
 		}	
 	}
 
 	private String openDoor(String baseurl, String token) throws IOException {
            URL url = new URL( baseurl + URLEncoder.encode(token) );
         
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String str = in.readLine();

            if (str.length() != TOKEN_LENGTH)
         	throw new IOException();
         
            return str;
 	}
 	
 	private void openDoor() {
 		
 		SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
 		String url   = p.getString("url", null);
 		String token = p.getString("token", "");
 	
 		if (token.length() != TOKEN_LENGTH) {
 			showDialog(DIALOG_NO_TOKEN);
 			return;
 		}
 		
 		try {
 			SharedPreferences.Editor e = p.edit();
 			e.putString("token", openDoor(url, token));
 			e.commit();
 		} catch (IOException e) {
 			showDialog(DIALOG_NET_FAIL);
 		}
 	}
 	
 	public void onClick(View v) {
 		switch (v.getId()) {
 		case R.id.open_btn:
 			openDoor();
 			break;
 		default:
 			// Do nothing.
 		}		
 	}
 }
