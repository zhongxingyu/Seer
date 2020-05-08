 package net.mms_projects.copyit.ui.android;
 
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Map;
 import java.util.UUID;
 
 import net.mms_projects.copy_it.R;
 import net.mms_projects.copyit.AndroidClipboardUtils;
 import net.mms_projects.copyit.ClipboardUtils;
 import net.mms_projects.copyit.FileStreamBuilder;
 import net.mms_projects.copyit.android.tasks.CheckUpdateTask;
 import net.mms_projects.copyit.android.tasks.CopyItTask;
 import net.mms_projects.copyit.android.tasks.PasteItTask;
 import net.mms_projects.copyit.android.tasks.SendToAppTask;
 import net.mms_projects.copyit.api.ServerApi;
 import net.mms_projects.copyit.app.CopyItAndroid;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.View;
 
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 
 public class MainActivity extends SherlockFragmentActivity {
 
 	private CopyItAndroid app;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		SharedPreferences preferences = PreferenceManager
 				.getDefaultSharedPreferences(this);
 
 		if (!preferences.contains("device.id")) {
 			Intent intent = new Intent(this, WelcomeActivity.class);
 			startActivity(intent);
 			finish();
 			return;
 		}
 
 		this.app = new CopyItAndroid();
 		this.app.run(this);
 
 		setContentView(R.layout.activity_main);
 
 		// Get intent, action and MIME type
 		Intent intent = getIntent();
 		String action = intent.getAction();
 		String type = intent.getType();
 
 		if (Intent.ACTION_SEND.equals(action) && type != null) {
 			if ("text/plain".equals(type)) {
 				handleSendText(intent); // Handle text being sent
 			}
 		}
 
 		ServerApi api = new ServerApi();
 		api.apiUrl = this.getResources().getString(R.string.jenkins_joburl);
 
 		if (CopyItAndroid.getBuildNumber(this) != 0) {
 			CheckUpdateTask task = new CheckUpdateTask(this, api);
 			task.execute();
 		} else {
 			Log.i("update-check",
 					"Build number is 0. Not running Jenkins build. Ignoring update check!");
 		}
 	}
 
 	private void handleSendText(Intent intent) {
 		String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
 		if (sharedText != null) {
 			SharedPreferences preferences = PreferenceManager
 					.getDefaultSharedPreferences(this);
 
 			Map<String, ?> settings = preferences.getAll();
 			for (String key : settings.keySet()) {
 				System.out.println(key + ": " + settings.get(key));
 			}
 
 			if (preferences.getString("device.id", null) == null) {
 				AlertDialog.Builder builder = new AlertDialog.Builder(this);
 				builder.setMessage(
 						this.getResources().getString(
 								R.string.text_login_question))
 						.setPositiveButton(
 								this.getResources().getString(
 										R.string.dialog_button_yes),
 								new MainActivity.LoginYesNoDialog())
 						.setNegativeButton(
 								this.getResources().getString(
 										R.string.dialog_button_no),
 								new MainActivity.LoginYesNoDialog()).show();
 				return;
 			}
 
 			ServerApi api = new ServerApi();
 			api.deviceId = UUID.fromString(preferences.getString("device.id",
 					null));
 			api.devicePassword = preferences.getString("device.password", null);
 			api.apiUrl = preferences.getString("server.baseurl", this
 					.getResources().getString(R.string.default_baseurl));
 
 			CopyItTask task = new HandleShareTask(this, api);
 			task.execute(sharedText);
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getSupportMenuInflater();
 		inflater.inflate(R.menu.main, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		Intent intent = null;
 		switch (item.getItemId()) {
 		case R.id.action_settings:
 			intent = new Intent(this, SettingsActivity.class);
 			startActivity(intent);
 			return true;
 		case R.id.action_feedback:
 			intent = new Intent(this, DebugActivity.class);
 			intent.setAction(Intent.ACTION_SEND);
 			startActivity(intent);
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	public void copyIt(View view) {
 		SharedPreferences preferences = PreferenceManager
 				.getDefaultSharedPreferences(this);
 
 		Map<String, ?> settings = preferences.getAll();
 		for (String key : settings.keySet()) {
 			System.out.println(key + ": " + settings.get(key));
 		}
 
 		if (preferences.getString("device.id", null) == null) {
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			builder.setMessage(
 					this.getResources().getString(R.string.text_login_question))
 					.setPositiveButton(
 							this.getResources().getString(
 									R.string.dialog_button_yes),
 							new MainActivity.LoginYesNoDialog())
 					.setNegativeButton(
 							this.getResources().getString(
 									R.string.dialog_button_no),
 							new MainActivity.LoginYesNoDialog()).show();
 			return;
 		}
 
 		ServerApi api = new ServerApi();
 		api.deviceId = UUID
 				.fromString(preferences.getString("device.id", null));
 		api.devicePassword = preferences.getString("device.password", null);
 		api.apiUrl = preferences.getString("server.baseurl", this
 				.getResources().getString(R.string.default_baseurl));
 
 		ClipboardUtils clipboard = new AndroidClipboardUtils(MainActivity.this);
 
 		CopyItTask task = new CopyItTask(this, api);
 		task.setUseProgressDialog(true);
 		task.execute(clipboard.getText());
 	}
 
 	public void pasteIt(View view) {
 		SharedPreferences preferences = PreferenceManager
 				.getDefaultSharedPreferences(this);
 		if (preferences.getString("device.id", null) == null) {
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			builder.setMessage(
 					this.getResources().getString(R.string.text_login_question))
 					.setPositiveButton(
 							this.getResources().getString(
 									R.string.dialog_button_yes),
 							new MainActivity.LoginYesNoDialog())
 					.setNegativeButton(
 							this.getResources().getString(
 									R.string.dialog_button_no),
 							new MainActivity.LoginYesNoDialog()).show();
 			return;
 		}
 
 		ServerApi api = new ServerApi();
 		api.deviceId = UUID
 				.fromString(preferences.getString("device.id", null));
 		api.devicePassword = preferences.getString("device.password", null);
 		api.apiUrl = preferences.getString("server.baseurl", this
 				.getResources().getString(R.string.default_baseurl));
 
 		PasteItTask task = new PasteItTask(this, api);
 		task.setUseProgressDialog(true);
 		task.execute();
 	}
 
 	public void sendToApp(View view) {
 		SharedPreferences preferences = PreferenceManager
 				.getDefaultSharedPreferences(this);
 		if (preferences.getString("device.id", null) == null) {
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			builder.setMessage(
 					this.getResources().getString(R.string.text_login_question))
 					.setPositiveButton(
 							this.getResources().getString(
 									R.string.dialog_button_yes),
 							new MainActivity.LoginYesNoDialog())
 					.setNegativeButton(
 							this.getResources().getString(
 									R.string.dialog_button_no),
 							new MainActivity.LoginYesNoDialog()).show();
 			return;
 		}
 
 		ServerApi api = new ServerApi();
 		api.deviceId = UUID
 				.fromString(preferences.getString("device.id", null));
 		api.devicePassword = preferences.getString("device.password", null);
 		api.apiUrl = preferences.getString("server.baseurl", this
 				.getResources().getString(R.string.default_baseurl));
 
 		SendToAppTask task = new SendToAppTask(this, api);
 		task.execute();
 	}
 
 	public void doLogin(View view) {
 		Intent intent = new Intent(this, LoginActivity.class);
 		startActivity(intent);
 	}
 
 	public void gotoSettings(View view) {
 		Intent intent = new Intent(this, SettingsActivity.class);
 		startActivity(intent);
 	}
 
 	public void gotoAbout(View view) {
 		Intent intent = new Intent(this, AboutActivity.class);
 		startActivity(intent);
 	}
 
 	class StreamBuilder extends FileStreamBuilder {
 
 		private Activity activity;
 
 		public StreamBuilder(Activity activity) {
 			this.activity = activity;
 		}
 
 		@Override
 		public FileInputStream getInputStream() throws IOException {
 			return this.activity.openFileInput("settings");
 		}
 
 		@Override
 		public FileOutputStream getOutputStream() throws IOException {
 			return this.activity.openFileOutput("settings",
 					Context.MODE_PRIVATE);
 		}
 
 	}
 
 	class LoginYesNoDialog implements DialogInterface.OnClickListener {
 		@Override
 		public void onClick(DialogInterface dialog, int which) {
 			switch (which) {
 			case DialogInterface.BUTTON_POSITIVE:
 				Intent intent = new Intent(MainActivity.this,
 						LoginActivity.class);
 				MainActivity.this.startActivity(intent);
 				break;
 
 			case DialogInterface.BUTTON_NEGATIVE:
 				MainActivity.this.finish();
 				break;
 			}
 		}
 	}
 
 	private class HandleShareTask extends CopyItTask {
 		public HandleShareTask(Context context, ServerApi api) {
 			super(context, api);
 		}
 
 		@Override
 		protected void onPostExecute(Boolean result) {
 			super.onPostExecute(result);
 
 			MainActivity.this.finish();
 		}
 	}
 }
