 package net.mms_projects.copyit.ui.android;
 
 import java.util.UUID;
 
 import net.mms_projects.copy_it.R;
 import net.mms_projects.copyit.LoginResponse;
 import net.mms_projects.copyit.android.tasks.SetupDeviceTask;
 import net.mms_projects.copyit.api.ServerApi;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.support.v4.app.NavUtils;
 import android.view.View;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.MenuItem;
 
 public class LoginActivity extends SherlockActivity {
 
 	private static final int ACTIVITY_LOGIN = 1;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_login);
 		// Show the Up button in the action bar.
 		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
 
 		Intent intent = new Intent(this, BrowserLoginActivity.class);
 		startActivityForResult(intent, LoginActivity.ACTIVITY_LOGIN);
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
 
 	public void openBrowserLogin(View view) {
 		Intent intent = new Intent(this, BrowserLoginActivity.class);
 		startActivityForResult(intent, LoginActivity.ACTIVITY_LOGIN);
 		finish();
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		switch (requestCode) {
 		case LoginActivity.ACTIVITY_LOGIN:
 			if (resultCode == RESULT_OK) {
 				LoginResponse response = new LoginResponse();
 				response.deviceId = UUID.fromString(data
 						.getStringExtra("device_id"));
 				response.devicePassword = data
 						.getStringExtra("device_password");
 
 				SharedPreferences preferences = PreferenceManager
 						.getDefaultSharedPreferences(this);
 
 				try {
 					Editor preferenceEditor = preferences.edit();
 					preferenceEditor.putString("device.id",
 							response.deviceId.toString());
 					preferenceEditor.putString("device.password",
 							response.devicePassword);
 					preferenceEditor.commit();
 				} catch (Exception event) {
 					// TODO Auto-generated catch block
 					event.printStackTrace();
 				}
 
 				ServerApi api = new ServerApi();
 				api.deviceId = response.deviceId;
 				api.devicePassword = response.devicePassword;
 				api.apiUrl = preferences.getString("server.baseurl", this
 						.getResources().getString(R.string.default_baseurl));
 
 				LoginTask task = new LoginTask(this, api);
 				task.execute();
 				break;
 			}
 		}
 	}
 
 	private class LoginTask extends SetupDeviceTask {
 		public LoginTask(Context context, ServerApi api) {
 			super(context, api);
 
 			this.setProgressDialigMessage(context.getResources().getString(
					R.string.text_login_question));
 		}
 
 		@Override
 		protected void onPostExecute(Boolean result) {
 			if (!result) {
 				AlertDialog alertDialog = new AlertDialog.Builder(this.context)
 						.create();
 				alertDialog.setTitle(this.context.getResources().getString(
 						R.string.dialog_title_error));
 				alertDialog.setMessage(this.context.getResources().getString(
 						R.string.error_device_setup_failed,
 						this.exception.getMessage()));
 				alertDialog.setButton(
 						DialogInterface.BUTTON_POSITIVE,
 						this.context.getResources().getString(
 								R.string.dialog_button_okay),
 						new DialogInterface.OnClickListener() {
 							@Override
 							public void onClick(DialogInterface dialog,
 									int which) {
 
 							}
 						});
 				alertDialog.setIcon(R.drawable.ic_launcher);
 				alertDialog.show();
 				return;
 			}
 			Toast.makeText(
 					this.context,
 					this.context.getResources().getString(
 							R.string.text_login_successful), Toast.LENGTH_SHORT)
 					.show();
 
 			super.onPostExecute(result);
 
 			LoginActivity.this.finish();
 		}
 	}
 
 }
