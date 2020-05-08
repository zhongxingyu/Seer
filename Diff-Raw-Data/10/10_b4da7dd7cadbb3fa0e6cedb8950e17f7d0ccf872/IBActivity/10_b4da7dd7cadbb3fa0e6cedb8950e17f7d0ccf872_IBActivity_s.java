 /**
  * Copyright (c) <year> Keifer Miller
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *     * Redistributions of source code must retain the above copyright
  *       notice, this list of conditions and the following disclaimer.
  *     * Redistributions in binary form must reproduce the above copyright
  *       notice, this list of conditions and the following disclaimer in the
  *       documentation and/or other materials provided with the distribution.
  *     * Neither the name of Ink Bar nor the
  *       names of its contributors may be used to endorse or promote products
  *       derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
  * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  **/
 package com.keifermiller.inkbar.activities;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.preference.PreferenceManager;
 import android.support.v4.app.FragmentActivity;
 
 import com.keifermiller.inkbar.R;
 
 /**
  * @author keifer
  * 
  *         Takes care of displaying a license dialog at start if needed. Also
  *         manages theme changes.
  * 
  */
 public abstract class IBActivity extends FragmentActivity implements
 		SharedPreferences.OnSharedPreferenceChangeListener {
 	private Handler mHandler;
 	public final static String HIDDEN_PREFES_NAME = "hidden_preferences";
 	private String mTheme = "NIGHT";
 	private SharedPreferences mPrefs;
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
 	 */
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
 
 		if (mPrefs.getString("theme", "NIGHT").equals("NIGHT")) {
 			setTheme(R.style.Dark);
 			mTheme = "NIGHT";
 		} else {
 			setTheme(R.style.Light);
 			mTheme = "DAY";
 		}
 
 		super.onCreate(savedInstanceState);
 
 		setHandler(new Handler());
 		SharedPreferences hiddenPrefes = getSharedPreferences(HIDDEN_PREFES_NAME, 0);
 		boolean licenseAgreed = hiddenPrefes.getBoolean("license_agreed", false);
 		boolean usageShown = hiddenPrefes.getBoolean("usage_shown", false);
 
		if (!licenseAgreed) {
			showDialog(R.id.license_dialog);
		}
		
 		if (!usageShown) {
 			showDialog(R.id.usage_dialog);
 		}

 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.support.v4.app.FragmentActivity#onStart()
 	 */
 	@Override
 	protected void onStart() {
 		super.onStart();
 		mPrefs.registerOnSharedPreferenceChangeListener(this);
 		updateTheme();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.support.v4.app.FragmentActivity#onStop()
 	 */
 	@Override
 	protected void onStop() {
 		mPrefs.unregisterOnSharedPreferenceChangeListener(this);
 		super.onStop();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.app.Activity#onCreateDialog(int)
 	 */
 	@Override
 	protected Dialog onCreateDialog(int id) {
 		AlertDialog dialog;
 		switch (id) {
 		case R.id.license_dialog:
 			dialog = new AlertDialog.Builder(this)
 					.setCancelable(false)
 					.setTitle("License")
 					.setMessage(R.string.license_dialog_message)
 					.setNegativeButton(R.string.license_dialog_decline,
 							new OnClickListener() {
 
 								@Override
 								public void onClick(DialogInterface arg0,
 										int arg1) {
 									System.exit(0);
 								}
 							})
 					.setNeutralButton(R.string.license_dialog_viewsource,
 							new OnClickListener() {
 
 								@Override
 								public void onClick(DialogInterface dialog,
 										int which) {
 									startActivity(new Intent(
 											Intent.ACTION_VIEW,
 											Uri.parse("https://github.com/KeiferMiller/InkBar")));
 									System.exit(0);
 								}
 							})
 					.setPositiveButton(R.string.license_dialog_accept,
 							new OnClickListener() {
 
 								@Override
 								public void onClick(DialogInterface dialog,
 										int which) {
 									getSharedPreferences(HIDDEN_PREFES_NAME, 0)
 											.edit()
 											.putBoolean("license_agreed", true)
 											.commit();
 
 								}
 
 							}).create();
 			break;
 		case R.id.usage_dialog:
 			dialog = new AlertDialog.Builder(this)
 			.setCancelable(false)
 			.setTitle("Usage")
 			.setMessage(R.string.usage_dialog_message)
 
 			.setPositiveButton(R.string.usage_dialog_accept,
 					new OnClickListener() {
 
 						@Override
 						public void onClick(DialogInterface dialog,
 								int which) {
 							getSharedPreferences(HIDDEN_PREFES_NAME, 0)
 									.edit()
 									.putBoolean("usage_shown", true)
 									.commit();
 
 						}
 
 					}).create();
 			break;
 		default:
 			dialog = null;
 		}
 
 		return dialog;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.content.SharedPreferences.OnSharedPreferenceChangeListener#
 	 * onSharedPreferenceChanged(android.content.SharedPreferences,
 	 * java.lang.String)
 	 */
 	@Override
 	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
 			String key) {
 		if (key.equals("theme")) {
 			updateTheme();
 		}
 
 	}
 
 	/**
 	 * Fetches the user selected theme from mPrefs ("DAY" if none is selected).
 	 * 
 	 * If a new theme is selected, sets the new theme, updates mTheme to reflect
 	 * this, and forces redraw via recreate().
 	 */
 	private void updateTheme() {
 		String value = mPrefs.getString("theme", "NIGHT");
 		if (mTheme == null || !mTheme.equals(value)) {
 			if (value.equals("NIGHT")) {
 				setTheme(R.style.Dark);
 				mTheme = "NIGHT";
 			} else {
 				setTheme(R.style.Light);
 				mTheme = "DAY";
 			}
 			recreate();
 		}
 	}
 	
 	/**
 	 * @return the mHandler
 	 */
 	synchronized public Handler getHandler() {
 		return mHandler;
 	}
 
 	/**
 	 * @param mHandler the mHandler to set
 	 */
 	synchronized public void setHandler(Handler mHandler) {
 		this.mHandler = mHandler;
 	}
 
 }
