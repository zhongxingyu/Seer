 package com.android.tecla.addon;
 
 import ca.idrc.tecla.R;
 import ca.idrc.tecla.framework.Persistence;
 import ca.idrc.tecla.framework.TeclaStatic;
 import android.bluetooth.BluetoothAdapter;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Handler;
 import android.preference.CheckBoxPreference;
 import android.preference.Preference;
 import android.preference.PreferenceFragment;
 import android.preference.Preference.OnPreferenceChangeListener;
 import android.preference.Preference.OnPreferenceClickListener;
 
 public class TeclaPreferenceFragment extends PreferenceFragment
 	implements OnPreferenceClickListener
 	, OnPreferenceChangeListener  {
 
 	private final static String CLASS_TAG = "TeclaPreferenceFragment";
 
 	private static TeclaPreferenceFragment sInstance;
 
 	private boolean mConnectionCancelled;
 	
 	private CheckBoxPreference mFullscreenMode;
 	private CheckBoxPreference mPrefSelfScanning;
 	private CheckBoxPreference mPrefInverseScanning;
 	private CheckBoxPreference mPrefConnectToShield;
 	private CheckBoxPreference mPrefTempDisconnect;
 	Preference mScanSpeedPref;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.tecla_prefs);
 		init();
 	}
 
 	private void init() {
 		sInstance = this;
 		
 		addPreferencesFromResource(R.xml.tecla_prefs);
 
 		mFullscreenMode = (CheckBoxPreference) findPreference(Persistence.PREF_FULLSCREEN_MODE);
 		mPrefSelfScanning = (CheckBoxPreference) findPreference(Persistence.PREF_SELF_SCANNING);
 		mPrefInverseScanning = (CheckBoxPreference) findPreference(Persistence.PREF_INVERSE_SCANNING);
 		mScanSpeedPref = findPreference(Persistence.PREF_SCAN_DELAY_INT);
 		mPrefConnectToShield = (CheckBoxPreference) findPreference(Persistence.PREF_CONNECT_TO_SHIELD);
 		mPrefTempDisconnect = (CheckBoxPreference) findPreference(Persistence.PREF_TEMP_SHIELD_DISCONNECT);
 		
 		mFullscreenMode.setOnPreferenceChangeListener(sInstance);
 		mPrefSelfScanning.setOnPreferenceChangeListener(sInstance);
 		mPrefInverseScanning.setOnPreferenceChangeListener(sInstance);
 		mScanSpeedPref.setOnPreferenceClickListener(sInstance);
 		mPrefConnectToShield.setOnPreferenceChangeListener(sInstance);
 		mPrefTempDisconnect.setOnPreferenceChangeListener(sInstance);
 
 	}
 
 	@Override
 	public boolean onPreferenceClick(Preference pref) {	
 		if(pref.equals(mScanSpeedPref)) {
 			((TeclaSettingsActivity)getActivity()).showScanSpeedDialog();
 			return true;
 		}
 		return false;
 	}
 
 	/* (non-Javadoc)
 	 * @see android.preference.Preference.OnPreferenceChangeListener#onPreferenceChange(android.preference.Preference, java.lang.Object)
 	 */
 	@Override
 	public boolean onPreferenceChange(Preference pref, Object newValue) {
 		if(pref.equals(mFullscreenMode)) {
 			TeclaStatic.logD(CLASS_TAG, "FullscreenMode pressed!");
 			if (newValue.toString().equals("true")) {
 				TeclaApp.getInstance().turnFullscreenOn();
 				mPrefSelfScanning.setChecked(true);
 			} else {
 				TeclaApp.getInstance().turnFullscreenOff();
 				mPrefSelfScanning.setChecked(false);
 			}
 			return true;
 		}
 		if(pref.equals(mPrefSelfScanning)) {
 			TeclaStatic.logD(CLASS_TAG, "Self scanning preference changed!");
 			if (newValue.toString().equals("true")) {
 				TeclaApp.persistence.setSelfScanningEnabled(true);
 				if(TeclaApp.persistence.isFullscreenEnabled() )
 					AutomaticScan.startAutoScan();
 			} else {
 				TeclaApp.persistence.setSelfScanningEnabled(false);
 				if(TeclaApp.persistence.isFullscreenEnabled() )
 					AutomaticScan.stopAutoScan();
 			}
 			return true;
 		}
 		if(pref.equals(mPrefInverseScanning)) {
 			TeclaStatic.logD(CLASS_TAG, "Inverse scanning preference changed!");
 			if (newValue.toString().equals("true")) {
 				TeclaApp.persistence.setInverseScanningEnabled(true);
 				TeclaApp.setFullscreenSwitchLongClick(false);
 				if(TeclaApp.persistence.isFullscreenEnabled() 
 						&& TeclaApp.persistence.isSelfScanningEnabled()) {
 					AutomaticScan.stopAutoScan();
 				}
 			} else {
 				TeclaApp.persistence.setInverseScanningEnabled(false);
 				TeclaApp.setFullscreenSwitchLongClick(true);
 				if(TeclaApp.persistence.isFullscreenEnabled() 
 						&& TeclaApp.persistence.isSelfScanningEnabled()) {
 					AutomaticScan.startAutoScan();
 				}
 			}
 			return true;
 		}
 		if(pref.equals(mPrefConnectToShield)) {
 			TeclaStatic.logD(CLASS_TAG, "Connect to shield preference changed!");
 			if (newValue.toString().equals("true")) {
 				mConnectionCancelled = false;
 				
 				if (!TeclaSettingsActivity.getTeclaShieldConnect().getBluetoothAdapter().isEnabled()) {
 					startActivity(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
 				}else{	
 				
 					if(!TeclaSettingsActivity.getTeclaShieldConnect().discoverShield())
 						mPrefConnectToShield.setChecked(false);
 					else
 						((TeclaSettingsActivity)getActivity()).showDiscoveryDialog();
 				}
 			} else {
 				((TeclaSettingsActivity)getActivity()).dismissDialog();
 				if (!mFullscreenMode.isChecked()) {
 					mPrefTempDisconnect.setChecked(false);
 					mPrefTempDisconnect.setEnabled(false);
 					mPrefSelfScanning.setChecked(false);
 					mPrefInverseScanning.setChecked(false);
 //					mPrefPersistentKeyboard.setChecked(false);
 				}
 				TeclaSettingsActivity.getTeclaShieldConnect().stopShieldService();
 			}
 			return true;
 		}
 		if(pref.equals(mPrefTempDisconnect)) {
 			TeclaStatic.logD(CLASS_TAG, "Temp shield disconnect preference changed!");
 			if (newValue.toString().equals("true")) {
 				mPrefConnectToShield.setEnabled(false);
 				TeclaSettingsActivity.getTeclaShieldConnect().stopShieldService();
 				Handler mHandler = new Handler();
 				Runnable mReconnect = new Runnable() {
 					
 					public void run() {
 						TeclaStatic.logD(CLASS_TAG, "Re-enabling discovery");
 						TeclaSettingsActivity.getTeclaShieldConnect().discoverShield();
 						mPrefConnectToShield.setEnabled(true);
 					}
 				};
 				
 				// See if the handler was posted
 				if(mHandler.postDelayed(mReconnect, 90000))	// 90 second delay
 				{
 					TeclaStatic.logD(CLASS_TAG, "Posted Runnable");
 				}
 				else
 				{
 					TeclaStatic.logD(CLASS_TAG, "Could not post Runnable");
 				}
 			}
 			return true;
 		}
 		return false;
 	}
 
 	public void uncheckFullScreenMode() {
 		if(!TeclaApp.persistence.isFullscreenEnabled()) {
 			mFullscreenMode.setChecked(false);
 		}
 	}
 	
 	public void onResumeSettingsActivityUpdatePrefs() {
 		mFullscreenMode.setChecked(TeclaApp.persistence.isFullscreenEnabled());
 		mPrefSelfScanning.setChecked(TeclaApp.persistence.isSelfScanningEnabled());
 		mPrefInverseScanning.setChecked(TeclaApp.persistence.isInverseScanningEnabled());;
 	}
 
 	public void onCancelDiscoveryDialogUpdatePrefs() {
 		mConnectionCancelled = true;
 		mPrefConnectToShield.setChecked(false);
 		mPrefTempDisconnect.setChecked(false);
 		mPrefTempDisconnect.setEnabled(false);
 	}
 
 	public void onTeclaShieldDiscoveryFinishedUpdatePrefs() {
 		mPrefConnectToShield.setChecked(false);
 		mPrefTempDisconnect.setChecked(false);
 		mPrefTempDisconnect.setEnabled(false);
 		if (!mConnectionCancelled) 
 			TeclaApp.getInstance().showToast(R.string.no_shields_inrange);
 	}
 
 	public void onTeclaShieldConnectedUpdatePrefs() {
 		mPrefTempDisconnect.setEnabled(true);
 		mPrefConnectToShield.setChecked(true);
 //		mPrefMorse.setEnabled(true);
 //		mPrefPersistentKeyboard.setChecked(true);
 	}
 
 	public void onTeclaShieldDisconnectedUpdatePrefs() {
 		mPrefTempDisconnect.setChecked(false);
 		mPrefTempDisconnect.setEnabled(false);
 	}
 	
 
 }
