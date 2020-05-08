 package net.mms_projects.copyit.ui.android;
 
 import java.util.UUID;
 
 import net.mms_projects.copy_it.R;
 import net.mms_projects.copyit.app.CopyItAndroid;
 import net.mms_projects.utils.InlineSwitch;
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.DisplayMetrics;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class DebugActivity extends Activity {
 
 	public final static String ACTION_SEND = "send";
 	private static final String TableRow = null;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		SharedPreferences preferences = PreferenceManager
 				.getDefaultSharedPreferences(this);
 
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_debug);
 
 		TextView baseUrl = (TextView) findViewById(R.id.info_server_baseurl);
 		baseUrl.setText(preferences.getString("server.baseurl", this
 				.getResources().getString(R.string.default_baseurl)));
 
 		TextView jenkinsBaseUrl = (TextView) findViewById(R.id.info_jenkins_baseurl);
 		jenkinsBaseUrl.setText(this.getResources().getString(
 				R.string.jenkins_baseurl));
 
 		TextView deviceId = (TextView) findViewById(R.id.info_device_id);
 		try {
 			UUID.fromString(preferences.getString("device.id", null));
 			deviceId.setText(this.getResources().getString(
 					R.string.debug_available));
 		} catch (Exception e) {
 			deviceId.setText(this.getResources().getString(
 					R.string.debug_not_available));
 		}
 
 		TextView devicePassword = (TextView) findViewById(R.id.info_device_password);
 		if (preferences.getString("device.password", null) != null) {
 			devicePassword.setText(this.getResources().getString(
 					R.string.debug_available));
 		} else {
 			devicePassword.setText(this.getResources().getString(
 					R.string.debug_not_available));
 		}
 
 		TextView buildNumber = (TextView) findViewById(R.id.info_build_number);
 		buildNumber
 				.setText(Integer.toString(CopyItAndroid.getBuildNumber(this)));
 
 		DisplayMetrics displayMetrics = new DisplayMetrics();
 		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
 
 		InlineSwitch<Integer, String> switcher = new InlineSwitch<Integer, String>();
 		switcher.addClause(DisplayMetrics.DENSITY_LOW, "ldpi");
 		switcher.addClause(DisplayMetrics.DENSITY_MEDIUM, "mdpi");
 		switcher.addClause(DisplayMetrics.DENSITY_HIGH, "hdpi");
 		switcher.addClause(DisplayMetrics.DENSITY_XHIGH, "xhdpi");
 		switcher.addClause(DisplayMetrics.DENSITY_XXHIGH, "xxhdpi");
 		switcher.setDefault(this.getResources().getString(
 				R.string.debug_unknown));
 
 		TextView screenDensity = (TextView) findViewById(R.id.info_screen_density);
 		screenDensity.setText(switcher.runSwitch(Integer
 				.valueOf(displayMetrics.densityDpi)));
 
		if (getIntent().getAction().equals(Intent.ACTION_SEND)) {
 			this.sendEmail(this
 					.exportTableLayout((TableLayout) findViewById(R.id.debug_table)));
 			finish();
 		}
 	}
 
 	protected String exportTableLayout(TableLayout table) {
 		String string = "";
 		TextView keyView;
 		TextView valueView;
 		String key;
 		String value;
 		for (int i = 1; i < table.getChildCount(); i++) {
 			TableRow row = (TableRow) table.getChildAt(i);
 			keyView = (TextView) row.getChildAt(0);
 			valueView = (TextView) row.getChildAt(1);
 			key = keyView.getText().toString();
 			value = valueView.getText().toString();
 			string += key + " - " + value + "\n";
 		}
 		return string;
 	}
 
 	protected void sendEmail(String text) {
 		Intent emailIntent = new Intent(Intent.ACTION_SEND);
 		emailIntent.setType("message/rfc822");
 		emailIntent.putExtra(Intent.EXTRA_EMAIL,
 				new String[] { "bitbucket@mms-projects.net" });
 		emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Copy It debug");
 		emailIntent.putExtra(Intent.EXTRA_TEXT, text);
 		try {
 			startActivity(Intent.createChooser(emailIntent, "Send mail..."));
 		} catch (android.content.ActivityNotFoundException ex) {
 			Toast.makeText(DebugActivity.this,
 					"There are no email clients installed.", Toast.LENGTH_SHORT)
 					.show();
 		}
 	}
 
 	public static class Launch extends BroadcastReceiver {
 
 		@Override
 		public void onReceive(Context context, Intent launchingIntent) {
 			Intent intent = new Intent(context, DebugActivity.class);
 			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 			context.startActivity(intent);
 		}
 
 	}
 
 }
