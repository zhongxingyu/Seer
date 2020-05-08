 package com.project.latviankeyboard;
 
 import android.app.Dialog;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.graphics.Color;
 import android.graphics.Typeface;
 import android.graphics.drawable.PaintDrawable;
 import android.os.Bundle;
import android.preference.CheckBoxPreference;
 import android.preference.EditTextPreference;
 import android.preference.Preference;
 import android.preference.Preference.OnPreferenceClickListener;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.ScrollView;
 import android.widget.SeekBar;
 import android.widget.TextView;
 
 public class PrefsExtraRow extends PreferenceActivity {
 
 	OnPreferenceClickListener myListener;
 	SeekBar redBar, greenBar, blueBar, alphaBar, seekBar;
 	View colorTest;
 	LinearLayout colorChooser, seekBarDialog;
 	ScrollView fontChooser;
 	SeekBar.OnSeekBarChangeListener seekBarChangeListener, sbChangeListener;
 	Button btnOk, btnCancel, sbBtnCancel, sbBtnOk, fcB1, fcB2, fcB3, fcB4, fcB5, fcB6, fcB7;
 	Dialog d, sbD, fcD;
 	SharedPreferences prefs;
 	String prefKey;
 	TextView sbText, textRed, textGreen, textBlue, textAlpha;
 	Typeface font;
 	Button.OnClickListener btnActionListener;
	CheckBoxPreference isHapticOn;
 
 	@Override
 	protected void onCreate(final Bundle savedInstanceState) {
 
 		super.onCreate(savedInstanceState);
 
 		// inflates preferences
 		addPreferencesFromResource(R.xml.prefs_extra_row);
 
 		// inflates colorChooser and its objects
 		colorChooser = (LinearLayout) getLayoutInflater().inflate(R.layout.color_chooser, null);
 		redBar = (SeekBar) colorChooser.findViewById(R.id.sbRed);
 		greenBar = (SeekBar) colorChooser.findViewById(R.id.sbGreen);
 		blueBar = (SeekBar) colorChooser.findViewById(R.id.sbBlue);
 		alphaBar = (SeekBar) colorChooser.findViewById(R.id.sbAlpha);
 		textRed = (TextView) colorChooser.findViewById((R.id.textRed));
 		textGreen = (TextView) colorChooser.findViewById((R.id.textGreen));
 		textBlue = (TextView) colorChooser.findViewById((R.id.textBlue));
 		textAlpha = (TextView) colorChooser.findViewById((R.id.textAlpha));
 		btnOk = (Button) colorChooser.findViewById(R.id.btnOk);
 		btnCancel = (Button) colorChooser.findViewById(R.id.btnCancel);
 
 		// inflates seekBarDialog and its objects
 		seekBarDialog = (LinearLayout) getLayoutInflater().inflate(R.layout.seek_bar_dialog, null);
 		seekBar = (SeekBar) seekBarDialog.findViewById(R.id.seekBar);
 		sbText = (TextView) seekBarDialog.findViewById(R.id.textRed);
 		sbBtnOk = (Button) seekBarDialog.findViewById(R.id.sbBtnOk);
 		sbBtnCancel = (Button) seekBarDialog.findViewById(R.id.sbBtnCancel);
 
 		// inflates fontChooser
 		fontChooser = (ScrollView) getLayoutInflater().inflate(R.layout.font_chooser, null);
 		fcB1 = (Button) fontChooser.findViewById(R.id.fcBtnFont1);
 		fcB2 = (Button) fontChooser.findViewById(R.id.fcBtnFont2);
 		fcB3 = (Button) fontChooser.findViewById(R.id.fcBtnFont3);
 		fcB4 = (Button) fontChooser.findViewById(R.id.fcBtnFont4);
 		fcB5 = (Button) fontChooser.findViewById(R.id.fcBtnFont5);
 		fcB6 = (Button) fontChooser.findViewById(R.id.fcBtnFont6);
 		fcB7 = (Button) fontChooser.findViewById(R.id.fcBtnFont7);
 
 		// sets fonts to buttons in fontChooser
 		font = Typeface.createFromAsset(getAssets(), "fonts/Xolonium-Regular.otf");
 		fcB1.setTypeface(font);
 		font = Typeface.createFromAsset(getAssets(), "fonts/GrandHotel-Regular.otf");
 		fcB2.setTypeface(font);
 		font = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Regular.ttf");
 		fcB3.setTypeface(font);
 		font = Typeface.createFromAsset(getAssets(), "fonts/Pecita.otf");
 		fcB4.setTypeface(font);
 		font = Typeface.createFromAsset(getAssets(), "fonts/d-puntillas-D-to-tiptoe.ttf");
 		fcB5.setTypeface(font);
 		font = Typeface.createFromAsset(getAssets(), "fonts/Megrim.ttf");
 		fcB6.setTypeface(font);
 		font = Typeface.createFromAsset(getAssets(), "fonts/Jura-Medium.ttf");
 		fcB7.setTypeface(font);
 
 		// gets shared preferences to edit them later
 		prefs = PreferenceManager.getDefaultSharedPreferences(this);
 		final SharedPreferences.Editor editor = prefs.edit();
 
 		// inflates Dialog context and adds colorChooser layout
 		d = new Dialog(PrefsExtraRow.this);
 		d.setContentView(colorChooser);
 		d.setCanceledOnTouchOutside(true);
 
 		// inflates Dialog context and adds seekBar layout
 		sbD = new Dialog(PrefsExtraRow.this);
 		sbD.setContentView(seekBarDialog);
 		sbD.setCanceledOnTouchOutside(true);
 
 		// inflates Dialog context and adds fontChooser layout
 		fcD = new Dialog(PrefsExtraRow.this);
 		fcD.setContentView(fontChooser);
 		fcD.setCanceledOnTouchOutside(true);
 
 		// fontChooser button click listener
 		btnActionListener = new Button.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				if (v.getId() == fcB1.getId()) {
 					editor.putString("erTextFont", "fonts/Xolonium-Regular.otf");
				} else if (v.getId() == fcB2.getId()) {
 					editor.putString("erTextFont", "fonts/GrandHotel-Regular.otf");
 				} else if (v.getId() == fcB3.getId()) {
 					editor.putString("erTextFont", "fonts/Roboto-Regular.ttf");
 				} else if (v.getId() == fcB4.getId()) {
 					editor.putString("erTextFont", "fonts/Pecita.otf");
 				} else if (v.getId() == fcB5.getId()) {
 					editor.putString("erTextFont", "fonts/d-puntillas-D-to-tiptoe.ttf");
 				} else if (v.getId() == fcB6.getId()) {
 					editor.putString("erTextFont", "fonts/Megrim.ttf");
 				} else if (v.getId() == fcB7.getId()) {
 					editor.putString("erTextFont", "fonts/Jura-Medium.ttf");
 				} else {
 					Log.i("Bļum bļum", "DANGER!");
 				}
 				editor.commit();
 				fcD.dismiss();
 			}
 		};
 
 		// ColorChooser action listener
 		seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
 
 			@Override
 			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
 				updateBackground();
 			}
 
 			// upgrades view background color from seekBar values
 			private void updateBackground() {
 				textRed.setText("" + redBar.getProgress());
 				textGreen.setText("" + greenBar.getProgress());
 				textBlue.setText("" + blueBar.getProgress());
 				textAlpha.setText("" + alphaBar.getProgress());
 				colorTest.setBackgroundColor(Color.argb(alphaBar.getProgress(), redBar.getProgress(), greenBar.getProgress(), blueBar.getProgress()));
 			}
 
 			@Override
 			public void onStartTrackingTouch(SeekBar seekBar) {
 			}
 
 			@Override
 			public void onStopTrackingTouch(SeekBar seekBar) {
 			}
 		};
 
 		// SeekBar action listener
 		sbChangeListener = new SeekBar.OnSeekBarChangeListener() {
 
 			@Override
 			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
 				sbText.setText("" + progress);
 			}
 
 			@Override
 			public void onStartTrackingTouch(SeekBar seekBar) {
 			}
 
 			@Override
 			public void onStopTrackingTouch(SeekBar seekBar) {
 			}
 		};
 
 		// adds actionlistener to "Ok" button [ColorChooser]
 		btnOk.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View arg0) {
 				editor.putInt(prefKey, Color.argb(alphaBar.getProgress(), redBar.getProgress(), greenBar.getProgress(), blueBar.getProgress()));
 				editor.commit();
 				d.dismiss();
 			}
 		});
 
 		// adds actionListener to "Cancel" button [ColorChooser]
 		btnCancel.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View arg0) {
 				d.dismiss();
 			}
 		});
 
 		// adds actionlistener to "Ok" button [SeekBarDialog]
 		sbBtnOk.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				editor.putInt(prefKey, seekBar.getProgress());
 				editor.commit();
 				sbD.dismiss();
 			}
 		});
 
 		// adds actionListener to "Cancel" button [SeekBarDialog]
 		sbBtnCancel.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				sbD.dismiss();
 			}
 		});
 
 		// adds actionListener to seekbars
 		redBar.setOnSeekBarChangeListener(seekBarChangeListener);
 		greenBar.setOnSeekBarChangeListener(seekBarChangeListener);
 		blueBar.setOnSeekBarChangeListener(seekBarChangeListener);
 		alphaBar.setOnSeekBarChangeListener(seekBarChangeListener);
 		seekBar.setOnSeekBarChangeListener(sbChangeListener);

		// set click listener to fontChooser buttons
 		fcB1.setOnClickListener(btnActionListener);
 		fcB2.setOnClickListener(btnActionListener);
 		fcB3.setOnClickListener(btnActionListener);
 		fcB4.setOnClickListener(btnActionListener);
 		fcB5.setOnClickListener(btnActionListener);
 		fcB6.setOnClickListener(btnActionListener);
 		fcB7.setOnClickListener(btnActionListener);
 
 		// inflates view that is used to test color
 		colorTest = colorChooser.findViewById(R.id.testColor);
 
 		// creates actionlistener for preferences
 		myListener = new OnPreferenceClickListener() {
 
 			@Override
 			public boolean onPreferenceClick(Preference preference) {
 				prefKey = preference.getKey();
 				if (prefKey.equals("erVerticalHeight")) {
 					sbD.setTitle(R.string.titleVerticalHeight);
 					seekBar.setProgress(prefs.getInt(prefKey, 50));
 					sbText.setText("" + prefs.getInt(prefKey, 50));
 					seekBar.setMax(75);
 					sbD.show();
 				} else if (prefKey.equals("erHorizontalHeight")) {
 					sbD.setTitle(R.string.titleHorizontalKeyboard);
 					seekBar.setProgress(prefs.getInt(prefKey, 60));
 					sbText.setText("" + prefs.getInt(prefKey, 60));
 					seekBar.setMax(75);
 					sbD.show();
 				} else if (prefKey.equals("erWaitTime")) {
 					sbD.setTitle(R.string.titleWaitTime);
 					seekBar.setProgress(prefs.getInt(prefKey, 200));
 					sbText.setText("" + prefs.getInt(prefKey, 200));
 					seekBar.setMax(1000);
 					sbD.show();
 				} else if (prefKey.equals("erBackgroundColor")) {
 					d.setTitle(R.string.titleBackgroundColor);
 					int bkColor = prefs.getInt(prefKey, Color.argb(255, 30, 30, 30));
 					colorTest.setBackgroundColor(bkColor);
 					redBar.setProgress(Color.red(bkColor));
 					greenBar.setProgress(Color.green(bkColor));
 					blueBar.setProgress(Color.blue(bkColor));
 					alphaBar.setProgress(Color.alpha(bkColor));
 					textRed.setText("" + redBar.getProgress());
 					textGreen.setText("" + greenBar.getProgress());
 					textBlue.setText("" + blueBar.getProgress());
 					textAlpha.setText("" + alphaBar.getProgress());
 					d.show();
 				} else if (prefKey.equals("erBtnBackground")) {
 					d.setTitle(R.string.titleBtnBackground);
 					int bkColor = prefs.getInt(prefKey, Color.argb(255, 60, 60, 60));
 					colorTest.setBackgroundColor(bkColor);
 					redBar.setProgress(Color.red(bkColor));
 					greenBar.setProgress(Color.green(bkColor));
 					blueBar.setProgress(Color.blue(bkColor));
 					alphaBar.setProgress(Color.alpha(bkColor));
 					textRed.setText("" + redBar.getProgress());
 					textGreen.setText("" + greenBar.getProgress());
 					textBlue.setText("" + blueBar.getProgress());
 					textAlpha.setText("" + alphaBar.getProgress());
 					d.show();
 				} else if (prefKey.equals("erBtnShadow")) {
 					d.setTitle(R.string.titleBtnShadow);
 					int bkColor = prefs.getInt(prefKey, Color.argb(170, 0, 0, 0));
 					colorTest.setBackgroundColor(bkColor);
 					redBar.setProgress(Color.red(bkColor));
 					greenBar.setProgress(Color.green(bkColor));
 					blueBar.setProgress(Color.blue(bkColor));
 					alphaBar.setProgress(Color.alpha(bkColor));
 					textRed.setText("" + redBar.getProgress());
 					textGreen.setText("" + greenBar.getProgress());
 					textBlue.setText("" + blueBar.getProgress());
 					textAlpha.setText("" + alphaBar.getProgress());
 					d.show();
 				} else if (prefKey.equals("erBtnHoverColor")) {
 					d.setTitle(R.string.titleBtnHoverColor);
 					int bkColor = prefs.getInt(prefKey, Color.argb(255, 80, 80, 80));
 					colorTest.setBackgroundColor(bkColor);
 					redBar.setProgress(Color.red(bkColor));
 					greenBar.setProgress(Color.green(bkColor));
 					blueBar.setProgress(Color.blue(bkColor));
 					alphaBar.setProgress(Color.alpha(bkColor));
 					textRed.setText("" + redBar.getProgress());
 					textGreen.setText("" + greenBar.getProgress());
 					textBlue.setText("" + blueBar.getProgress());
 					textAlpha.setText("" + alphaBar.getProgress());
 					d.show();
 				} else if (prefKey.equals("erBtnBorderColor")) {
 					d.setTitle(R.string.titleBtnBorderColor);
 					int bkColor = prefs.getInt(prefKey, Color.argb(255, 51, 181, 229));
 					colorTest.setBackgroundColor(bkColor);
 					redBar.setProgress(Color.red(bkColor));
 					greenBar.setProgress(Color.green(bkColor));
 					blueBar.setProgress(Color.blue(bkColor));
 					alphaBar.setProgress(Color.alpha(bkColor));
 					textRed.setText("" + redBar.getProgress());
 					textGreen.setText("" + greenBar.getProgress());
 					textBlue.setText("" + blueBar.getProgress());
 					textAlpha.setText("" + alphaBar.getProgress());
 					d.show();
 				} else if (prefKey.equals("erBtnTextColor")) {
 					d.setTitle(R.string.titleBtnTextColor);
 					int bkColor = prefs.getInt(prefKey, Color.argb(255, 255, 255, 255));
 					colorTest.setBackgroundColor(bkColor);
 					redBar.setProgress(Color.red(bkColor));
 					greenBar.setProgress(Color.green(bkColor));
 					blueBar.setProgress(Color.blue(bkColor));
 					alphaBar.setProgress(Color.alpha(bkColor));
 					textRed.setText("" + redBar.getProgress());
 					textGreen.setText("" + greenBar.getProgress());
 					textBlue.setText("" + blueBar.getProgress());
 					textAlpha.setText("" + alphaBar.getProgress());
 					d.show();
 				} else if (prefKey.equals("erTextShadow")) {
 					d.setTitle(R.string.titleTextShadow);
 					int bkColor = prefs.getInt(prefKey, Color.argb(135, 0, 0, 0));
 					colorTest.setBackgroundColor(bkColor);
 					redBar.setProgress(Color.red(bkColor));
 					greenBar.setProgress(Color.green(bkColor));
 					blueBar.setProgress(Color.blue(bkColor));
 					alphaBar.setProgress(Color.alpha(bkColor));
 					textRed.setText("" + redBar.getProgress());
 					textGreen.setText("" + greenBar.getProgress());
 					textBlue.setText("" + blueBar.getProgress());
 					textAlpha.setText("" + alphaBar.getProgress());
 					d.show();
 				} else if (prefKey.equals("erTextFont")) {
 					fcD.setTitle(R.string.titleTextFont);
 					fcD.show();
 				} else if (prefKey.equals("erTextSize")) {
 					sbD.setTitle(R.string.titleTextSize);
 					seekBar.setProgress(prefs.getInt(prefKey, 25));
 					sbText.setText("" + prefs.getInt(prefKey, 25));
 					seekBar.setMax(60);
 					sbD.show();
 				} else if (prefKey.equals("erBtnPadding")) {
 					sbD.setTitle(R.string.titleBtnPadding);
 					seekBar.setProgress(prefs.getInt(prefKey, 4));
 					sbText.setText("" + prefs.getInt(prefKey, 4));
 					seekBar.setMax(15);
 					sbD.show();
 				} else if (prefKey.equals("erBtnRoundness")) {
 					sbD.setTitle(R.string.titleBtnRoundness);
 					seekBar.setProgress(prefs.getInt(prefKey, 8));
 					sbText.setText("" + prefs.getInt(prefKey, 8));
 					seekBar.setMax(35);
 					sbD.show();
 				} else if (prefKey.equals("callKeyboard")) {
 					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
 					imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_NOT_ALWAYS);
 				} else if (prefKey.equals("resetDefaults")) {
 					editor.remove("erIsHapticOn");
 					editor.remove("erVerticalHeight");
 					editor.remove("erHorizontalHeight");
 					editor.remove("erWaitTime");
 					editor.remove("erBackgroundColor");
 					editor.remove("erBtnBackground");
 					editor.remove("erBtnShadow");
 					editor.remove("erBtnHoverColor");
 					editor.remove("erBtnBorderColor");
 					editor.remove("erBtnTextColor");
 					editor.remove("erTextShadow");
 					editor.remove("erTextFont");
 					editor.remove("erTextSize");
 					editor.remove("erBtnPadding");
 					editor.remove("erBtnRoundness");
 					editor.commit();
 				}
 				return false;
 			}
 		};
 
 		// adds actionListeners for preferences
 		findPreference("erVerticalHeight").setOnPreferenceClickListener(myListener);
 		findPreference("erHorizontalHeight").setOnPreferenceClickListener(myListener);
 		findPreference("erWaitTime").setOnPreferenceClickListener(myListener);
 		findPreference("erBackgroundColor").setOnPreferenceClickListener(myListener);
 		findPreference("erBtnBackground").setOnPreferenceClickListener(myListener);
 		findPreference("erBtnShadow").setOnPreferenceClickListener(myListener);
 		findPreference("erBtnHoverColor").setOnPreferenceClickListener(myListener);
 		findPreference("erBtnBorderColor").setOnPreferenceClickListener(myListener);
 		findPreference("erBtnTextColor").setOnPreferenceClickListener(myListener);
 		findPreference("erTextShadow").setOnPreferenceClickListener(myListener);
 		findPreference("erTextFont").setOnPreferenceClickListener(myListener);
 		findPreference("erTextSize").setOnPreferenceClickListener(myListener);
 		findPreference("erBtnPadding").setOnPreferenceClickListener(myListener);
 		findPreference("erBtnRoundness").setOnPreferenceClickListener(myListener);
 		findPreference("callKeyboard").setOnPreferenceClickListener(myListener);
 		findPreference("resetDefaults").setOnPreferenceClickListener(myListener);
 
		// isHapticOn bugfix
		isHapticOn = (CheckBoxPreference) findPreference("erIsHapticOn");
		if (prefs.getBoolean("erIsHapticOn", true)) {
			isHapticOn.setChecked(true);
		} else {
			isHapticOn.setChecked(false);
		}

 	}
 }
