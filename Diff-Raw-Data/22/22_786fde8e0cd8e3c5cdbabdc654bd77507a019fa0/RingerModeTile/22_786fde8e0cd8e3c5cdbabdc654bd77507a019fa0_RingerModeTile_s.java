 package com.android.systemui.statusbar.quicksettings.quicktile;
 
 import android.content.BroadcastReceiver;
 import android.content.ContentResolver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.media.AudioManager;
 import android.net.Uri;
 import android.os.Handler;
 import android.os.Vibrator;
 import android.preference.MultiSelectListPreference;
 import android.provider.Settings;
 import android.text.TextUtils;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnLongClickListener;
 
 import com.android.systemui.R;
 import com.android.systemui.statusbar.quicksettings.QuickSettingsController;
 import com.android.systemui.statusbar.quicksettings.QuickSettingsContainerView;
 
 public class RingerModeTile extends QuickSettingsTile {
 
     private static final int VIBRATE_DURATION = 250; // 0.25s
 
     // Define the available ringer modes
     private final Ringer mSilentRinger = new Ringer(false, AudioManager.VIBRATE_SETTING_OFF,
             AudioManager.RINGER_MODE_SILENT, false);
     private final Ringer mVibrateRinger = new Ringer(true, AudioManager.VIBRATE_SETTING_ONLY_SILENT,
             AudioManager.RINGER_MODE_VIBRATE, true);
     private final Ringer mSoundRinger = new Ringer(true, AudioManager.VIBRATE_SETTING_ONLY_SILENT,
             AudioManager.RINGER_MODE_NORMAL, false);
     private final Ringer mSoundVibrateRinger = new Ringer(true, AudioManager.VIBRATE_SETTING_ON,
             AudioManager.RINGER_MODE_NORMAL, true);
     private final Ringer[] mRingers = new Ringer[] {
             mSilentRinger, mVibrateRinger, mSoundRinger, mSoundVibrateRinger
     };
     private int mRingersIndex = 2;
 
     private int[] mRingerValues = new int[] {
             0, 1, 2, 3
     };
     private int mRingerValuesIndex = 2;
 
     private AudioManager mAudioManager;
 
     public RingerModeTile(Context context, LayoutInflater inflater,
             QuickSettingsContainerView container, QuickSettingsController qsc) {
         super(context, inflater, container, qsc);
 
         mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
 
         // Load the available ringer modes
         updateSettings(context.getContentResolver());
 
         // Make sure we show the initial state correctly
         updateState();
 
         // Tile actions
         mOnClick = new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 toggleState();
                 applyVibrationChanges();
             }
         };
 
         mOnLongClick = new OnLongClickListener() {
             @Override
             public boolean onLongClick(View v) {
                 startSettingsActivity(android.provider.Settings.ACTION_SOUND_SETTINGS);
                 return true;
             }
         };
         qsc.registerAction(AudioManager.RINGER_MODE_CHANGED_ACTION, this);
         qsc.registerAction(AudioManager.VIBRATE_SETTING_CHANGED_ACTION, this);
         qsc.registerObservedContent(Settings.System.getUriFor(Settings.System.EXPANDED_RING_MODE)
                 , this);
         qsc.registerObservedContent(Settings.System.getUriFor(Settings.System.VIBRATE_IN_SILENT)
                 , this);
     }
 
     @Override
     public void onReceive(Context context, Intent intent) {
         applyVibrationChanges();
     }
 
     @Override
     public void onChangeUri(ContentResolver resolver, Uri uri) {
         updateSettings(resolver);
         applyVibrationChanges();
     }
 
     private void applyVibrationChanges(){
         updateState();
         updateQuickSettings();
     }
 
     protected void updateState() {
 
         // The icon will change depending on index
         findCurrentState();
         switch (mRingersIndex) {
             case 0:
                 mDrawable = R.drawable.ic_qs_ring_off;
                 mLabel = mContext.getString(R.string.quick_settings_ringer_offf);
                 break;
             case 1:
                 mDrawable = R.drawable.ic_qs_vibrate_on;
                 mLabel = mContext.getString(R.string.quick_settings_vibrate);
                 break;
             case 2:
                mDrawable = R.drawable.ic_qs_ring_on;
                 mLabel = mContext.getString(R.string.quick_settings_ringer_onn);
                 break;
             case 3:
                mDrawable = R.drawable.ic_qs_ring_vibrate_on;
                 mLabel = mContext.getString(R.string.quick_settings_ringer_vibrate);
                 break;
         }
 
         for (int i = 0; i < mRingerValues.length; i++) {
             if (mRingersIndex == mRingerValues[i]) {
                 mRingerValuesIndex = i;
                 break;
             }
         }
     }
 
     protected void toggleState() {
         mRingerValuesIndex++;
         if (mRingerValuesIndex > mRingerValues.length - 1) {
             mRingerValuesIndex = 0;
         }
 
         mRingersIndex = mRingerValues[mRingerValuesIndex];
         if (mRingersIndex > mRingers.length - 1) {
             mRingersIndex = 0;
         }
 
         Ringer ringer = mRingers[mRingersIndex];
         ringer.execute(mContext);
     }
 
     private void updateSettings(ContentResolver resolver) {
         String[] modes = MultiSelectListPreference.parseStoredValue(Settings.System.getString(
                 resolver, Settings.System.EXPANDED_RING_MODE));
         if (modes == null || modes.length == 0) {
             mRingerValues = new int[] {
                     0, 1, 2, 3
             };
         } else {
             mRingerValues = new int[modes.length];
             for (int i = 0; i < modes.length; i++) {
                 mRingerValues[i] = Integer.valueOf(modes[i]);
             }
         }
     }
 
     private void findCurrentState() {
         boolean vibrateInSilent = Settings.System.getInt(mContext.getContentResolver(),
                 Settings.System.VIBRATE_IN_SILENT, 0) == 1;
         int vibrateSetting = mAudioManager.getVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER);
         int ringerMode = mAudioManager.getRingerMode();
         // Sometimes the setting don't quite match up to the states we've defined.
         // In that case, override the reported settings to get us "close" to the
         // defined settings. This bit is a little ugly but oh well.
         if (!vibrateInSilent && ringerMode == AudioManager.RINGER_MODE_SILENT) {
             vibrateSetting = AudioManager.VIBRATE_SETTING_OFF; // match Silent ringer
         } else if (!vibrateInSilent && ringerMode == AudioManager.RINGER_MODE_NORMAL) {
             vibrateInSilent = true; // match either Sound or SoundVibrate ringer
             if (vibrateSetting == AudioManager.VIBRATE_SETTING_OFF) {
                 vibrateSetting = AudioManager.VIBRATE_SETTING_ONLY_SILENT; // match Sound ringer
             }
         } else if (vibrateInSilent && ringerMode == AudioManager.RINGER_MODE_VIBRATE) {
             vibrateSetting = AudioManager.VIBRATE_SETTING_ONLY_SILENT; // match Vibrate ringer 
         }
 
         Ringer ringer = new Ringer(vibrateInSilent, vibrateSetting, ringerMode, false);
         for (int i = 0; i < mRingers.length; i++) {
             if (mRingers[i].equals(ringer)) {
                 mRingersIndex = i;
                 break;
             }
         }
     }
 
     private class Ringer {
         final boolean mVibrateInSilent;
         final int mVibrateSetting;
         final int mRingerMode;
         final boolean mDoHapticFeedback;
 
         Ringer(boolean vibrateInSilent, int vibrateSetting, int ringerMode, boolean doHapticFeedback) {
             mVibrateInSilent = vibrateInSilent;
             mVibrateSetting = vibrateSetting;
             mRingerMode = ringerMode;
             mDoHapticFeedback = doHapticFeedback;
         }
 
         void execute(Context context) {
             ContentResolver resolver = context.getContentResolver();
             Settings.System.putInt(resolver, Settings.System.VIBRATE_IN_SILENT,
                     (mVibrateInSilent ? 1 : 0));
 
             mAudioManager.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER, mVibrateSetting);
             mAudioManager.setRingerMode(mRingerMode);
             if (mDoHapticFeedback && mHapticFeedback) {
                 mVibrator.vibrate(VIBRATE_DURATION);
             }
         }
 
         @Override
         public boolean equals(Object o) {
             if (o == null) {
                 return false;
             }
             if (o.getClass() != getClass()) {
                 return false;
             }
 
             Ringer r = (Ringer) o;
             return r.mVibrateInSilent == mVibrateInSilent && r.mVibrateSetting == mVibrateSetting
                     && r.mRingerMode == mRingerMode;
         }
 
     }
 }
