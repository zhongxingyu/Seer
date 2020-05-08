 package net.gicode.android.autoresetblocker;
 
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.content.pm.ActivityInfo;
 import android.content.pm.ResolveInfo;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.text.ClipboardManager;
 import android.view.View;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.EditText;
 import android.widget.LinearLayout.LayoutParams;
 import android.widget.TextView;
 
 public class AutoResetBlockerActivity extends Activity {
 
 	private SharedPreferences SP;
 	private Intent dialIntent;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.blocker_activity);
 
 		CheckBox autoDialOption = (CheckBox) findViewById(R.id.auto_dial);
 		autoDialOption.setOnCheckedChangeListener(new AutoDialListener());
 
 		SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
 		autoDialOption.setChecked(SP.getBoolean("auto_dial", false));
 
 		Intent intent = getIntent();
 		String number = intent.getData().getSchemeSpecificPart();
 		number = number.replaceFirst("^//", "");
 
 		boolean safe = number.matches("[0-9.\\-() +]*");
 
 		TextView descriptionView = (TextView) findViewById(R.id.number_description);
 		descriptionView.setText(safe ? R.string.safe_number_description
 				: R.string.malicious_number_description);
 
 		EditText numberView = (EditText) findViewById(R.id.number);
 		numberView.setText(number);
 
 		Button copyNumber = (Button) findViewById(R.id.copy_number);
 		copyNumber.setOnClickListener(new CopyListener(numberView));
 
 		Button dialButton = (Button) findViewById(R.id.dial);
 		LayoutParams dialButtonParams = (LayoutParams) dialButton
 				.getLayoutParams();
 		dialButtonParams.weight = safe ? 1 : 0;
 		dialButton.setText(safe ? R.string.safe_dial : R.string.malicious_dial);
 		dialButton.setLines(safe ? 3 : 1);
 		dialButton.setOnClickListener(new DialListener());
 
 		Button dismissButton = (Button) findViewById(R.id.dismiss);
 		LayoutParams dismissButtonParams = (LayoutParams) dismissButton
 				.getLayoutParams();
 		dismissButtonParams.weight = safe ? 0 : 1;
 		dismissButton.setText(safe ? R.string.safe_dismiss
 				: R.string.malicious_dismiss);
 		dismissButton.setLines(safe ? 1 : 3);
 		dismissButton.setOnClickListener(new DismissListener());
 
 		Intent cleanIntent = new Intent(intent.getAction(), intent.getData());
 		List<ResolveInfo> activities = getPackageManager()
 				.queryIntentActivities(cleanIntent, 0);
 
 		if (!safe || (activities.size() != 2)) {
 			CharSequence title = getText(safe ? R.string.safe_launch_title
 					: R.string.malicious_launch_title);
 			dialIntent = Intent.createChooser(intent, title);
 		} else {
 			ActivityInfo info = activities.get(0).activityInfo;
 			if (getClass().getCanonicalName().equals(info.name)) {
 				info = activities.get(1).activityInfo;
 			}
			dialIntent = cleanIntent.setClassName(info.packageName, info.name);
 		}
 
 		if (safe && autoDialOption.isChecked()) {
 			startActivity(dialIntent);
 		}
 	}
 
 	private class CopyListener implements View.OnClickListener {
 		private TextView contents;
 
 		public CopyListener(TextView contents) {
 			this.contents = contents;
 		}
 
 		public void onClick(View v) {
 			ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
 			clipboard.setText(contents.getText());
 		}
 	}
 
 	private class DialListener implements View.OnClickListener {
 		public void onClick(View v) {
 			startActivity(dialIntent);
 		}
 	}
 
 	private class DismissListener implements View.OnClickListener {
 		public void onClick(View v) {
 			finish();
 		}
 	}
 
 	private class AutoDialListener implements
 			CompoundButton.OnCheckedChangeListener {
 		public void onCheckedChanged(CompoundButton c, boolean isChecked) {
 			Editor editor = SP.edit();
 			editor.putBoolean("auto_dial", isChecked);
 			editor.commit();
 		}
 	}
 }
