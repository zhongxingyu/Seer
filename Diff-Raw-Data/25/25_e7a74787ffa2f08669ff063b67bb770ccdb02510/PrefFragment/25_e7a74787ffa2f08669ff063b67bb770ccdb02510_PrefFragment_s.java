 package com.github.ironjan.photodrop.fragments;
 
import android.widget.EditText;
import android.widget.ToggleButton;

import com.actionbarsherlock.app.SherlockFragment;
import com.github.ironjan.photodrop.R;
import com.github.ironjan.photodrop.StartActivity_;
import com.github.ironjan.photodrop.dbwrap.SessionKeeper;
import com.github.ironjan.photodrop.helper.Prefs_;
import com.googlecode.androidannotations.annotations.AfterInject;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.Bean;
import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.ViewById;
import com.googlecode.androidannotations.annotations.sharedpreferences.Pref;
 
 @EFragment(R.layout.frgmt_pref)
 public class PrefFragment extends SherlockFragment {
 
 	private static final int MINUTE = 60 * 1000;
 
 	@ViewById
 	ToggleButton tglAutomaticSync, tglSyncOnlyWifi;
 
 	@ViewById
 	EditText txtSyncInterval;
 
 	@Pref
 	Prefs_ prefs;
 
 	@Bean
 	SessionKeeper sessionKeeper;
 
 	private boolean mAutomaticSync, mSyncOnlyOnWifi;
 
 	private int mSyncInterval;
 
 	@AfterInject
 	void loadPreferences() {
 		mAutomaticSync = prefs.automaticSync().get();
 		mSyncOnlyOnWifi = prefs.syncOnlyOnWifi().get();
 		mSyncInterval = prefs.syncIntervall().get() / MINUTE;
 	}
 
 	@AfterViews
 	void bindPreferences() {
 		txtSyncInterval.setText(mSyncInterval + ""); //$NON-NLS-1$
 		tglAutomaticSync.setChecked(mAutomaticSync);
 		tglSyncOnlyWifi.setChecked(mSyncOnlyOnWifi);
 		updateSyncOnlyOnWifiEnabled();
 	}
 
 	@Click(R.id.tglAutomaticSync)
 	void updateSyncOnlyOnWifiEnabled() {
 		mAutomaticSync = tglAutomaticSync.isChecked();
 		tglSyncOnlyWifi.setEnabled(mAutomaticSync);
 	}
 
 	@Override
 	public void onPause() {
 		mAutomaticSync = tglAutomaticSync.isChecked();
 		mSyncOnlyOnWifi = tglSyncOnlyWifi.isChecked();
 		mSyncInterval = MINUTE
 				* Integer.parseInt(txtSyncInterval.getText().toString());
 
 		prefs.edit().automaticSync().put(mAutomaticSync).syncOnlyOnWifi()
 				.put(mSyncOnlyOnWifi).syncIntervall().put(mSyncInterval)
 				.apply();
 
 		super.onPause();
 	}
 
 	@Click(R.id.btnUnlink)
 	void unlinkDropbox() {
 		sessionKeeper.unlink();
 		StartActivity_.intent(getActivity()).start();
 	}
 }
