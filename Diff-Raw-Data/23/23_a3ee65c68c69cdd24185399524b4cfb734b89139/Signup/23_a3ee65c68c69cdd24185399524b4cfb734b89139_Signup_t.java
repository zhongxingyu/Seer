 package net.djmacgyver.bgt;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 
 import net.djmacgyver.bgt.http.HttpClient;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.StringEntity;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.Preference;
 import android.preference.PreferenceManager;
 import android.preference.Preference.OnPreferenceClickListener;
 import android.preference.PreferenceActivity;
 import android.view.Window;
 import android.widget.TextView;
 
 public class Signup extends PreferenceActivity {
 	static final int DIALOG_SIGNUP_RUNNING = 0;
 	static final int DIALOG_SIGNUP_SUCCESSFUL = 1;
 	static final int DIALOG_SIGNUP_FAILED = 2;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
         requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
         super.onCreate(savedInstanceState);
         
         getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header);
         TextView t = (TextView) findViewById(R.id.title);
         t.setText(R.string.new_user);
         addPreferencesFromResource(R.xml.signup);
 
         Preference signup = findPreference("signup");
 
 		final SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
         
         signup.setOnPreferenceClickListener(new OnPreferenceClickListener() {
 			@Override
 			public boolean onPreferenceClick(Preference preference) {
				Bundle b = new Bundle();
				if (p.getString("username", "").equals("")) {
					b.putString("message", getResources().getString(R.string.username_must_not_be_empty));
					showDialog(DIALOG_SIGNUP_FAILED, b);
					return false;
				}
				if (p.getString("password", "").equals("")) {
					b.putString("message", getResources().getString(R.string.password_must_not_be_empty));
					showDialog(DIALOG_SIGNUP_FAILED, b);
					return false;
				}
				if (!p.getString("password", "a").equals(p.getString("password_confirm", "b"))) {
					b.putString("message", getResources().getString(R.string.password_mismatch));
					showDialog(DIALOG_SIGNUP_FAILED, b);
					return false;
				}
 				
 				showDialog(DIALOG_SIGNUP_RUNNING);
 				
 				HttpClient c = new HttpClient(getApplicationContext());
 				HttpPost req = new HttpPost(Config.baseUrl + "signup");
 				try {
 					req.setEntity(new StringEntity("user=" + p.getString("username", null) + "&pass=" + p.getString("password", "")));
 					HttpResponse res = c.execute(req);
 					dismissDialog(DIALOG_SIGNUP_RUNNING);
 					if (res.getStatusLine().getStatusCode() != 200) {
 						HttpEntity e = res.getEntity();
 						BufferedReader in = new BufferedReader(new InputStreamReader(e.getContent()));
 						String line = null;
 						String message = getResources().getString(R.string.signup_server_message) + "\n\n";
 						while ((line = in.readLine()) != null) message = message.concat(line);
 						b.putString("message", message);
 						showDialog(DIALOG_SIGNUP_FAILED, b);
 					} else {
 						showDialog(DIALOG_SIGNUP_SUCCESSFUL);
 					}
 				} catch (Exception e) {
 					dismissDialog(DIALOG_SIGNUP_RUNNING);
 					
 					b.putString("message", getResources().getString(R.string.server_down));
 					showDialog(DIALOG_SIGNUP_FAILED, b);
 					
 					e.printStackTrace();
 				}
 				
 				return true;
 			}
 		});
 	}
 
 	@Override
 	protected Dialog onCreateDialog(int id) {
 		Dialog d;
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		switch (id) {
 			case DIALOG_SIGNUP_RUNNING:
 				d = new ProgressDialog(this);
 				d.setTitle(R.string.signup_progress);
 				break;
 			case DIALOG_SIGNUP_SUCCESSFUL:
 				builder.setMessage(R.string.signup_successful)
 					   .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
 							@Override
 							public void onClick(DialogInterface dialog, int which) {
 								dialog.dismiss();
 							}
 					   });
 				d = builder.create();
 				break;
 			case DIALOG_SIGNUP_FAILED:
 				builder.setMessage(R.string.signup_failed)
 					   .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
 							@Override
 							public void onClick(DialogInterface dialog, int which) {
 								dialog.dismiss();
 							}
 					   });
 				d = builder.create();
 				break;
 			default:
 				d = super.onCreateDialog(id);
 		}
 		return d;
 	}
 
 	@Override
 	protected void onPrepareDialog(int id, Dialog dialog, Bundle args) {
 		switch (id) {
 			case DIALOG_SIGNUP_FAILED:
 				AlertDialog d = (AlertDialog) dialog;
 				String message = getResources().getString(R.string.signup_failed);
 				d.setMessage(message.concat("\n\n" + args.getString("message")));
 		}
 		super.onPrepareDialog(id, dialog, args);
 	}
 }
