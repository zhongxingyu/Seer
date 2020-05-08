 package edu.berkeley.eecs.ruzenafit.activity;
 
 import android.app.Activity;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.EditText;
 import android.widget.RadioButton;
 import android.widget.TextView;
 import android.widget.Toast;
 import edu.berkeley.eecs.ruzenafit.R;
 import edu.berkeley.eecs.ruzenafit.model.PrivacyPreferenceEnum;
 import edu.berkeley.eecs.ruzenafit.util.Constants;
 
 // TODO: Change Preferences so that you cannot change them
 // while mid-workout.
 public class PreferencesActivity extends Activity {
 	// Initializing variables
 	RadioButton rbLow, rbMedium, rbHigh;
 	TextView textOut;
 	EditText getInput;
 
 	private static String t = "Message";
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.preferences);
 
 		Log.d(t, "Preferences Screen !!!!");
 
 		rbLow = (RadioButton) findViewById(R.id.radioButton1);
 		rbMedium = (RadioButton) findViewById(R.id.radioButton2);
 		rbHigh = (RadioButton) findViewById(R.id.radioButton3);
 
 		textOut = (TextView) findViewById(R.id.textView2);
 
 		// FIXME: pSetting.setText not working.
 		/*
 		 * Explanation: You're getting a NullPointerException because line 39
 		 * can't find the actual TextView that corresponds to R.id.pset (when
 		 * the method findViewById() can't find any views that correspond to the
 		 * argument you've given, it simply returns null).
 		 * 
 		 * The reason why this method won't work in this Activity is because the
 		 * R.id.pset view you're looking for actually exists in a different
 		 * Activity altogether -- *not* in the layout you specified on line 32.
 		 * 
 		 * The solution is to simply use Android's SharedPreferences to "save"
 		 * the currently selected privacy setting. This already happens in the
 		 * private method setPrivacySetting(). So, you can just remove the
 		 * pSetting.setText() from this Activity.
 		 * 
 		 * Instead, you want to put the call to pSetting.setText() in the
 		 * Activity that contains the R.id.pset you used to reference pSetting
 		 * -- you want to call pSetting.setText() from the
 		 * WorkoutTrackerActivity.java class -- specifically, inside onResume().
 		 */
 		// Listening to button event
 		rbLow.setOnClickListener(new OnClickListener() {
 
 			public void onClick(View v) {
 				setPrivacySetting(PrivacyPreferenceEnum.lowPrivacy);
 				textOut.setText("    With this setting you will earn a 1.8x multiplier to your points. "
 						+ "You will also share the maximum amount of data about yourself possible. "
 						+ "For example anyone will be able to see exactly where you are working out "
 						+ "at whichever time your data is saved.  "
 						+ "\n\n"
 						+ "With this preference, your data will update ONCE EVERY 5 SECONDS.");
 			}
 		});
 
 		// Listening to button event
 		rbMedium.setOnClickListener(new OnClickListener() {
 
 			public void onClick(View v) {
 				setPrivacySetting(PrivacyPreferenceEnum.mediumPrivacy);
 				textOut.setText("    With this setting you will earn a 1.2x multiplier to "
 						+ "your points. "
 						+ "You will also share a moderate amount of data about yourself. "
 						+ "For example people will be able to see the area where you are working out "
 						+ "but not the exact street or location "
 						+ "at whichever time your data is saved."
 						+ "\n\n"
 						+ "With this preference, your data will update ONCE EVERY 5 MINUTES.");
 			}
 		});
 
 		// Listening to button event
 		rbHigh.setOnClickListener(new OnClickListener() {
 
 			public void onClick(View v) {
 				setPrivacySetting(PrivacyPreferenceEnum.highPrivacy);
 				textOut.setText("    With this setting you will earn a 0.8x multiplier to your points. "
 						+ "You will also share a minimal amount of data about yourself."
 						+ "\n\n"
 						+ "With this preference, your data will update ONCE EVERY HOUR.");
 			}
 		});
 
 	}
 
 	/**
 	 * Saves the selected privacy setting into the phone's internal
 	 * SharedPreferences storage.
 	 * 
 	 * @param privacyPreference
 	 */
 	private void setPrivacySetting(PrivacyPreferenceEnum privacyPreference) {
 		SharedPreferences.Editor preferences = getSharedPreferences(
 				Constants.PREFS_NAMESPACE, 0).edit();
 		preferences.putString(Constants.PRIVACY_SETTING,
 				privacyPreference.toString());
 		preferences.commit();
 
 		switch (privacyPreference) {
 		case highPrivacy:
 			Constants.setUPDATE_FREQUENCY(3600000); // one hour
 			break;
 		case mediumPrivacy:
 			Constants.setUPDATE_FREQUENCY(300000); // 5 minutes
 			break;
 		case lowPrivacy:
 			Constants.setUPDATE_FREQUENCY(5000); // 5 seconds
 			break;
 		default:
 			Constants.setUPDATE_FREQUENCY(3000); // 3 seconds if undefined.
 			break;
 		}
 
 		Toast.makeText(getApplicationContext(),
 				"Saved privacy setting: " + privacyPreference.toString(), 3)
 				.show();
 	}
 }
