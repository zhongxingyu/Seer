 package org.subsurface;
 
 import org.subsurface.controller.DiveController;
 import org.subsurface.controller.UserController;
 import org.subsurface.ws.WsClient;
 import org.subsurface.ws.WsClientStub;
 
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.text.InputType;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.Toast;
 
 public class AccountLinkActivity extends ListActivity implements OnSharedPreferenceChangeListener {
 
 	private static final String TAG = "AccountLinkActivity";
 
 	private final WsClient wsClient = new WsClientStub();
 
 	private void createAccount(final String email) {
 		final ProgressDialog waitDialog = ProgressDialog.show(
 				AccountLinkActivity.this, "", getString(R.string.wait_dialog), true, true);
 		new AsyncTask<Void, Void, Boolean>() {
 			@Override
 			protected Boolean doInBackground(Void... params) {
 				Boolean success = Boolean.FALSE;
 				try {
 					String user = wsClient.createUser(UserController.instance.getBaseUrl(), email);
 					if (user != null) {
 						success = Boolean.TRUE;
 						UserController.instance.setUser(user);
 					}
 				} catch (Exception e) {
 					Log.d(TAG, "Could not create user", e);
 				}
 				return success;
 			}
 			@Override
 			protected void onPostExecute(Boolean result) {
 				waitDialog.dismiss();
 				if (result) {
 					Toast.makeText(AccountLinkActivity.this, R.string.success_user_creation, Toast.LENGTH_LONG).show();
 				} else {
 					Toast.makeText(AccountLinkActivity.this, R.string.error_generic, Toast.LENGTH_SHORT).show();
 				}
 			}
 		}.execute();
 	}
 
 	private void retrieveAccount(final String email) {
 		final ProgressDialog waitDialog = ProgressDialog.show(
 				AccountLinkActivity.this, "", getString(R.string.wait_dialog), true, true);
 		new AsyncTask<Void, Void, Boolean>() {
 			@Override
 			protected Boolean doInBackground(Void... params) {
 				Boolean success = Boolean.FALSE;
 				try {
 					wsClient.resendUser(UserController.instance.getBaseUrl(), email);
 					success = Boolean.TRUE;
 				} catch (Exception e) {
 					Log.d(TAG, "Could not retrieve user", e);
 				}
 				return success;
 			}
 			@Override
 			protected void onPostExecute(Boolean result) {
 				waitDialog.dismiss();
 				if (result) {
 					Toast.makeText(AccountLinkActivity.this, R.string.success_user_creation, Toast.LENGTH_LONG).show();
 				} else {
 					Toast.makeText(AccountLinkActivity.this, R.string.error_generic, Toast.LENGTH_SHORT).show();
 				}
 			}
 		}.execute();
 	}
 
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         // Initialize controllers
         UserController.instance.setContext(this);
         DiveController.instance.setContext(this);
 
         setContentView(R.layout.login_choices);
     	setListAdapter(new ArrayAdapter<String>(this, R.layout.login_choice_item, android.R.id.text1, getResources().getStringArray(R.array.account_link_choices)));
 
         PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
 	}
 
 	@Override
 	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
 		if ("user_id".equals(key)) { // Show dives
 			startActivity(new Intent(this, HomeActivity.class));
 		}
 	}
 
 	@Override
     public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.general, menu);
         return true;
     }
 
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		// Link to account
 		final EditText edit = new EditText(this);
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setView(edit);
 		builder.setNegativeButton(android.R.string.cancel, null);
 		if (position == 0) { // Creation
 			edit.setHint(getString(R.string.hint_email));
 			edit.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
 			builder.setTitle(getString(R.string.account_link_create))
 					.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
 						@Override
 						public void onClick(DialogInterface dialog, int which) {
 							createAccount(edit.getText().toString());
 						}
 					}).create().show();
 		} else if (position == 1) {
 			edit.setHint(getString(R.string.hint_email));
 			edit.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
 			builder.setTitle(getString(R.string.account_link_retrieve))
 					.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
 						@Override
 						public void onClick(DialogInterface dialog, int which) {
 							retrieveAccount(edit.getText().toString());
 						}
 					}).create().show();
 		} else if (position == 2) { // Set ID
 			edit.setHint(getString(R.string.hint_id));
			edit.setInputType(InputType.TYPE_TEXT_VARIATION_NORMAL);
 			builder.setTitle(getString(R.string.account_link_existing))
 					.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
 						@Override
 						public void onClick(DialogInterface dialog, int which) {
 							UserController.instance.setUser(edit.getText().toString());
 						}
 					}).create().show();
 		}
 	}
 
 	@Override
     public boolean onMenuItemSelected(int featureId, MenuItem item) {
     	if (item.getItemId() == R.id.menu_settings) { // Settings
     		startActivity(new Intent(this, Preferences.class));
     		finish();
     		return true;
     	}
     	return false;
 	}
 }
