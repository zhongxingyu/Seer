 /*
  *  Copyright (C) 2013 The OmniROM Project
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 package com.bel.android.dspmanager.modules.soundcontrol;
 
 import android.content.Context;
 import android.os.Bundle;
 import android.preference.ListPreference;
 import android.preference.Preference;
 import android.preference.PreferenceFragment;
 import android.widget.Toast;
 
 import com.bel.android.dspmanager.R;
 import com.bel.android.dspmanager.activity.DSPManager;
 import com.bel.android.dspmanager.preference.SeekBarPreference;
 
 import java.util.Locale;
 
 /**
  * Controls Faux123's SoundControl Kernel Modules
  */
 public class SoundControl extends PreferenceFragment
         implements Preference.OnPreferenceChangeListener {
 
     //=========================
     // Fields
     //=========================
     public static final String NAME = "SoundControl";
     private ListPreference mPresets;
     private Toast mToast;
     // Headphone Normal
     private SeekBarPreference mHeadphoneLeft;
     private SeekBarPreference mHeadphoneRight;
     // Headphone PowerAmp
     private SeekBarPreference mHeadphonePaLeft;
     private SeekBarPreference mHeadphonePaRight;
     // Microphone
     private SeekBarPreference mMicrophoneHandset;
     private SeekBarPreference mMicrophoneCamcorder;
     // Speaker
     private SeekBarPreference mSpeaker;
     //=========================
     // Preference Keys
     //=========================
     private static final String PREF_VERSION = "sc_version";
     private static final String PREF_PRESETS = "sc_presets";
     private static final String PREF_HEADPHONE_LEFT = "sc_headphone_left";
     private static final String PREF_HEADPHONE_RIGHT = "sc_headphone_right";
     private static final String PREF_HEADPHONE_PA_LEFT = "sc_headphone_pa_left";
     private static final String PREF_HEADPHONE_PA_RIGHT = "sc_headphone_pa_right";
     private static final String PREF_MICROPHONE_HANDSET = "sc_microphone_handset";
     private static final String PREF_MICROPHONE_CAMCORDER = "sc_microphone_camcorder";
     private static final String PREF_SPEAKER = "sc_speaker";
 
     //=========================
     // Overridden Methods
     //=========================
 
     @Override
     public void onCreate(Bundle bundle) {
         super.onCreate(bundle);
 
         getPreferenceManager().setSharedPreferencesName(
                 DSPManager.SHARED_PREFERENCES_BASENAME + ".soundcontrol");
         getPreferenceManager().setSharedPreferencesMode(Context.MODE_MULTI_PROCESS);
 
         addPreferencesFromResource(R.xml.soundcontrol_preferences);
 
         // Version
         Preference mVersion = findPreference(PREF_VERSION);
         mVersion.setTitle(R.string.sc_version_title);
         mVersion.setSummary((
                 getScHelper().getVersion() ?
                         getScHelper().getVersionFormatted() :
                         "---").toUpperCase(Locale.getDefault()));
 
         // Presets
         mPresets = (ListPreference) findPreference(PREF_PRESETS);
         if (mPresets.getValue() == null) mPresets.setValueIndex(0);
         mPresets.setSummary(R.string.sc_preset_summary);
         mPresets.setOnPreferenceChangeListener(this);
 
         // Headphone
         mHeadphoneLeft = (SeekBarPreference) findPreference(PREF_HEADPHONE_LEFT);
         mHeadphoneRight = (SeekBarPreference) findPreference(PREF_HEADPHONE_RIGHT);
         updateHeadphone();
         mHeadphoneLeft.setOnPreferenceChangeListener(this);
         mHeadphoneRight.setOnPreferenceChangeListener(this);
 
         // Headphone PowerAmp
         mHeadphonePaLeft = (SeekBarPreference) findPreference(PREF_HEADPHONE_PA_LEFT);
         mHeadphonePaRight = (SeekBarPreference) findPreference(PREF_HEADPHONE_PA_RIGHT);
         updateHeadphonePa();
         mHeadphonePaLeft.setOnPreferenceChangeListener(this);
         mHeadphonePaRight.setOnPreferenceChangeListener(this);
 
         // Microphone Handset
         mMicrophoneHandset = (SeekBarPreference) findPreference(PREF_MICROPHONE_HANDSET);
         updateMicrophoneHandset();
         mMicrophoneHandset.setOnPreferenceChangeListener(this);
 
         // Microphone Camcorder
         mMicrophoneCamcorder = (SeekBarPreference) findPreference(PREF_MICROPHONE_CAMCORDER);
         updateMicrophoneCamcorder();
         mMicrophoneCamcorder.setOnPreferenceChangeListener(this);
 
         // Speaker
         mSpeaker = (SeekBarPreference) findPreference(PREF_SPEAKER);
         updateSpeaker();
         mSpeaker.setOnPreferenceChangeListener(this);
 
         makeToast(getString(R.string.sc_value_changed));
     }
 
     @Override
     public boolean onPreferenceChange(Preference preference, Object newValue) {
 
         boolean changed = false;
 
         if (preference == mPresets) {
             loadPreset(Integer.parseInt(newValue.toString()));
             changed = true;
         } else if (preference == mHeadphoneLeft) {
             getScHelper().applyHeadphoneLeft(newValue.toString());
             changed = true;
         } else if (preference == mHeadphoneRight) {
             getScHelper().applyHeadphoneRight(newValue.toString());
             changed = true;
         } else if (preference == mHeadphonePaLeft) {
             getScHelper().applyHeadphonePowerampLeft(newValue.toString());
             changed = true;
         } else if (preference == mHeadphonePaRight) {
             getScHelper().applyHeadphonePowerampRight(newValue.toString());
             changed = true;
         } else if (preference == mMicrophoneHandset) {
             getScHelper().applyMicrophoneHandset(newValue.toString());
             changed = true;
         } else if (preference == mMicrophoneCamcorder) {
             getScHelper().applyMicrophoneCamcorder(newValue.toString());
             changed = true;
         } else if (preference == mSpeaker) {
             getScHelper().applySpeaker(newValue.toString());
             changed = true;
         }
 
         getScHelper().switchPreset(0);
 
         return changed;
     }
 
     //=========================
     // Methods
     //=========================
 
     /**
      * Creates a short living Toast
      */
     private void makeToast(String message) {
         if (mToast != null) {
             mToast.cancel();
         }
         mToast = Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT);
         mToast.show();
     }
 
     /**
      * Update values for <b>Headphone</b> or disable if not available
      */
     private void updateHeadphone() {
         if (getScHelper().getHeadphone()) {
             mHeadphoneLeft.setValue(getScHelper().readHeadphoneLeft() - 40);
             mHeadphoneRight.setValue(getScHelper().readHeadphoneRight() - 40);
         } else {
             mHeadphoneLeft.setEnabled(false);
             mHeadphoneRight.setEnabled(false);
         }
     }
 
     /**
      * Update values for <b>Headphone PowerAmp</b> or disable if not available
      */
     private void updateHeadphonePa() {
         if (getScHelper().getHeadphonePa()) {
             mHeadphonePaLeft.setValue(getScHelper().readHeadphonePowerampLeft() - 12);
             mHeadphonePaRight.setValue(getScHelper().readHeadphonePowerampRight() - 12);
         } else {
             mHeadphonePaLeft.setEnabled(false);
             mHeadphonePaRight.setEnabled(false);
         }
     }
 
     /**
      * Update values for <b>Microphone Handset</b> or disable if not available
      */
     private void updateMicrophoneHandset() {
         if (getScHelper().getMicrophoneHandset()) {
             mMicrophoneHandset.setValue(getScHelper().readMicrophoneHandset() - 40);
         } else {
             mMicrophoneHandset.setEnabled(false);
         }
     }
 
     /**
      * Update values for <b>Microphone Camcorder</b> or disable if not available
      */
     private void updateMicrophoneCamcorder() {
         if (getScHelper().getMicrophoneCam()) {
             mMicrophoneCamcorder.setValue(getScHelper().readMicrophoneCamcorder() - 40);
         } else {
             mMicrophoneCamcorder.setEnabled(false);
         }
     }
 
     /**
      * Update values for <b>Speaker</b> or disable if not available
      */
     private void updateSpeaker() {
         if (getScHelper().getSpeaker()) {
            mSpeaker.setValue(getScHelper().readSpeaker() - 40);
         } else {
             mSpeaker.setEnabled(false);
         }
     }
 
     /**
      * Switches presets and updates values
      *
      * @param i The id of the preset
      */
     private void loadPreset(int i) {
         getScHelper().switchPreset(i);
         updateHeadphone();
         updateHeadphonePa();
         updateMicrophoneHandset();
         updateMicrophoneCamcorder();
         updateSpeaker();
         String mPresetName;
         switch (i) {
             default:
             case 0:
                 mPresetName = getString(R.string.sc_preset_custom);
                 break;
             case 1:
                 mPresetName = getString(R.string.sc_preset_quality);
                 break;
             case 2:
                 mPresetName = getString(R.string.sc_preset_loudness);
                 break;
             case 3:
                 mPresetName = getString(R.string.sc_preset_quiet);
                 break;
             case 4:
                 mPresetName = getString(R.string.sc_preset_stock);
                 break;
         }
         makeToast(getString(R.string.sc_preset_loaded, mPresetName));
     }
 
     /**
      * Everyone hates typing much, so we created a method for doing the same with less typing.
      *
      * @return An instance of the Sound Control Helper
      */
     private SoundControlHelper getScHelper() {
         return SoundControlHelper.getSoundControlHelper(getActivity());
     }
 }
