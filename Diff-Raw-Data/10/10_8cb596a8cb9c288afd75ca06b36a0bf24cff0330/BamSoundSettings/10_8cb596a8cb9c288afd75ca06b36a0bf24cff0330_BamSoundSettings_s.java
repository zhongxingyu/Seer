 package com.android.settings.jellybam;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.PowerManager;
 import android.preference.CheckBoxPreference;
 import android.preference.ListPreference;
 import android.preference.Preference;
 import android.preference.PreferenceManager;
 import android.preference.PreferenceScreen;
 import android.preference.Preference.OnPreferenceChangeListener;
 import android.provider.Settings;
 
 import com.android.settings.jellybam.FlipService;
 import com.android.settings.jellybam.HeadphoneService;
 
 import com.android.settings.R;
 import com.android.settings.SettingsPreferenceFragment;
 import com.android.settings.Utils;
 
 public class BamSoundSettings extends SettingsPreferenceFragment implements
         Preference.OnPreferenceChangeListener {
 
     private static final String PREF_HEADPHONES_PLUGGED_ACTION = "headphone_audio_mode";
     private static final String PREF_BT_CONNECTED_ACTION = "bt_audio_mode";
     private static final String PREF_FLIP_ACTION = "flip_mode";
     private static final String PREF_USER_TIMEOUT = "user_timeout";
     private static final String PREF_USER_DOWN_MS = "user_down_ms";
     private static final String PREF_PHONE_RING_SILENCE = "phone_ring_silence";
     private static final String PREF_LESS_NOTIFICATION_SOUNDS = "less_notification_sounds";
     private static final String KEY_SWAP_VOLUME_BUTTONS = "swap_volume_buttons";
 
     SharedPreferences prefs;
     ListPreference mHeadphonesPluggedAction;
     ListPreference mBTPluggedAction;
     ListPreference mFlipAction;
     ListPreference mUserDownMS;
     ListPreference mFlipScreenOff;
     ListPreference mPhoneSilent;
     ListPreference mAnnoyingNotifications;
 
     private CheckBoxPreference mSwapVolumeButtons;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setTitle(R.string.title_sound);
         addPreferencesFromResource(R.xml.jellybam_sound_settings);
         PreferenceManager.setDefaultValues(mContext, R.xml.jellybam_sound_settings, true);
         prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
 
         mAnnoyingNotifications = (ListPreference) findPreference(PREF_LESS_NOTIFICATION_SOUNDS);
         mAnnoyingNotifications.setOnPreferenceChangeListener(this);
         mAnnoyingNotifications.setValue(Integer.toString(Settings.System.getInt(getActivity()
                 .getContentResolver(), Settings.System.MUTE_ANNOYING_NOTIFICATIONS_THRESHOLD,
                 0)));
 
         mFlipAction = (ListPreference) findPreference(PREF_FLIP_ACTION);
         mFlipAction.setOnPreferenceChangeListener(this);
         mFlipAction.setValue((prefs.getString(PREF_FLIP_ACTION, "-1")));
 
         mUserDownMS = (ListPreference) findPreference(PREF_USER_DOWN_MS);
         mUserDownMS.setEnabled(Integer.parseInt(prefs.getString(PREF_FLIP_ACTION, "-1")) != -1);
 
         mFlipScreenOff = (ListPreference) findPreference(PREF_USER_TIMEOUT);
         mFlipScreenOff.setEnabled(Integer.parseInt(prefs.getString(PREF_FLIP_ACTION, "-1")) != -1);
 
         mPhoneSilent = (ListPreference) findPreference(PREF_PHONE_RING_SILENCE);
         mPhoneSilent.setValue((prefs.getString(PREF_PHONE_RING_SILENCE, "0")));
         mPhoneSilent.setOnPreferenceChangeListener(this);
 
         mSwapVolumeButtons = (CheckBoxPreference) findPreference(KEY_SWAP_VOLUME_BUTTONS);
        mSwapVolumeButtons.setChecked(Settings.System.getInt(resolver,
                 Settings.System.SWAP_VOLUME_KEYS, 0) == 1);
 
         if (HeadphoneService.DEBUG)
             mContext.startService(new Intent(mContext, HeadphoneService.class));
 
         if (FlipService.DEBUG)
             mContext.startService(new Intent(mContext, FlipService.class));
     }
 
     @Override
     public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
             Preference preference) {
 	if (preference == mSwapVolumeButtons) {
             Settings.System.putInt(getActivity().getContentResolver(), Settings.System.SWAP_VOLUME_KEYS,
                     mSwapVolumeButtons.isChecked() ? 1 : 0);
 	}
         return super.onPreferenceTreeClick(preferenceScreen, preference);
     }
 
     private void toggleFlipService() {
         if (FlipService.isStarted()) {
             mContext.stopService(new Intent(mContext, FlipService.class));
         }
         mContext.startService(new Intent(mContext, FlipService.class));
     }
 
     @Override
     public boolean onPreferenceChange(Preference preference, Object newValue) {
         if (preference == mFlipAction) {
             int val = Integer.parseInt((String) newValue);
             if (val != -1) {
                 mUserDownMS.setEnabled(true);
                 mFlipScreenOff.setEnabled(true);
                 AlertDialog.Builder ad = new AlertDialog.Builder(getActivity());
                 ad.setTitle(getResources().getString(R.string.flip_dialog_title));
                 ad.setMessage(getResources().getString(R.string.flip_dialog_msg));
                 ad.setPositiveButton(
                         getResources().getString(R.string.flip_action_positive),
                         new DialogInterface.OnClickListener() {
                             @Override
                             public void onClick(DialogInterface dialog, int which) {
                                 dialog.dismiss();
                             }
                         });
                 ad.show();
                 toggleFlipService();
             } else {
                 mUserDownMS.setEnabled(false);
                 mFlipScreenOff.setEnabled(false);
             }
             return true;
 
         } else if (preference == mAnnoyingNotifications) {
             int val = Integer.parseInt((String) newValue);
             Settings.System.putInt(getActivity().getContentResolver(),
                     Settings.System.MUTE_ANNOYING_NOTIFICATIONS_THRESHOLD, val);
             return true;
 
         } else if (preference == mPhoneSilent) {
             int val = Integer.parseInt((String) newValue);
             if (val != 0) {
                 toggleFlipService();
             }
             return true;
         }
         return false;
     }
 }
