 package com.teamramrod.solarpanelmanager;
 
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 import com.example.solarpanelmanager.R;
 import com.teamramrod.Constants;
 import com.teamramrod.bluetooth.Callback;
 import com.teamramrod.bluetooth.LocationUpdateHandler;
 import com.teamramrod.bluetooth.PINUpdateHandler;
 import com.teamramrod.bluetooth.SetChargeConstraintsHandler;
 import com.teamramrod.bluetooth.TimeUpdateHandler;
 import com.teamramrod.bluetooth.ViewChargeConstraintsHandler;
 import com.teamramrod.solarpanelmanager.RangeSeekBar.OnRangeSeekBarChangeListener;
 import com.teamramrod.solarpanelmanager.api.responses.BaseResponse;
 import com.teamramrod.solarpanelmanager.api.responses.ViewChargeConstraintsResponse;
 
 /**
  * @author seanhurley
  * 
  *         This activity will be used to allow the user to set device specific
  *         activities
  */
 public class DevicePreferencesActivity extends BaseActivity {
 
 	private View passRow;
 	private View minMaxRow;
 	private View timeRow;
 	private View locationRow;
 	private View recoverRow;
 	private View activityIndicator;
 	private View scroller;
 	private int minValue = 0;
 	private int maxValue = 100;
 	private String oldPassPhrase;
 	private String deviceId;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_device_settings);
 
 		getUI();
 		getData();
 		setupUI();
 	}
 
 	private void getUI() {
 		activityIndicator = findViewById(R.id.activityIndicator);
 		passRow = findViewById(R.id.row_password_pref);
 		timeRow = findViewById(R.id.row_time_update);
 		locationRow = findViewById(R.id.row_location_update);
 		minMaxRow = findViewById(R.id.row_min_max_values);
 		scroller = findViewById(R.id.scroll_view);
 		recoverRow = findViewById(R.id.row_recover);
 	}
 
 	private void setupUI() {
 		passRow.setOnClickListener(passphraseRowOnClick);
 		minMaxRow.setOnClickListener(minMaxRowOnClick);
 		timeRow.setOnClickListener(timeRowOnClick);
 		locationRow.setOnClickListener(locationRowOnClick);
 		recoverRow.setOnClickListener(recoverRowOnClick);
 	}
 
 	private void getData() {
 		deviceId = PreferenceManager.getDefaultSharedPreferences(this).getString(Constants.CURRENT_DEVICE, null);
 		oldPassPhrase = PreferenceManager.getDefaultSharedPreferences(this).getString(
 				Constants.PASS_PHRASE_PREFERENCE + deviceId, null);
 
 		ViewChargeConstraintsHandler handler = new ViewChargeConstraintsHandler(
 				new Callback<ViewChargeConstraintsResponse>() {
 
 					@Override
 					public void onComplete(ViewChargeConstraintsResponse response) {
						if (response == null) {
							showDeviceNotAvailable();
							return;	
						}
 						hideLoadingSpinner();
 						if (response.getResult() == 200) {
 							minValue = response.getMin();
 							maxValue = response.getMax();
 						}
 					}
 				}, deviceId, oldPassPhrase);
 		showLoadingSpinner();
 		handler.performAction();
 	}
 
 	private void showLoadingSpinner() {
 		this.activityIndicator.setVisibility(View.VISIBLE);
 		this.scroller.setVisibility(View.GONE);
 	}
 
 	private void hideLoadingSpinner() {
 		this.activityIndicator.setVisibility(View.GONE);
 		this.scroller.setVisibility(View.VISIBLE);
 	}
 
 	private View.OnClickListener passphraseRowOnClick = new View.OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			// Set an EditText view to get user input
 			final EditText input = new EditText(DevicePreferencesActivity.this);
 			DialogInterface.OnClickListener positiveListener = new DialogInterface.OnClickListener() {
 
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					final String value = input.getText().toString();
 					PINUpdateHandler handler = new PINUpdateHandler(new Callback<BaseResponse>() {
 
 						@Override
 						public void onComplete(BaseResponse response) {
							if (response == null) {
								showDeviceNotAvailable();
								return;	
							}
 							hideLoadingSpinner();
 							if (response.getResult() == 200) {
 								oldPassPhrase = value;
 								PreferenceManager.getDefaultSharedPreferences(DevicePreferencesActivity.this).edit()
 										.putString(Constants.PASS_PHRASE_PREFERENCE + deviceId, value).commit();
 							} else if (response.getResult() == 403) {
 								showForbiddenErrorDialog();
 							} else {
 								showDeviceNotAvailable();
 							}
 						}
 					}, deviceId, oldPassPhrase, value);
 					showLoadingSpinner();
 					handler.performAction();
 				}
 			};
 
 			showDialogNoNegative(R.string.set_pass_phrase_title, R.string.set_pass_phrase_message, positiveListener,
 					input);
 		}
 	};
 
 	private RangeSeekBar<Integer> buildSeekBar(final TextView minValueText, final TextView maxValueText) {
 		final RangeSeekBar<Integer> seekBar = new RangeSeekBar<Integer>(0, 100, DevicePreferencesActivity.this);
 		seekBar.setNotifyWhileDragging(true);
 		seekBar.setSelectedMinValue(minValue);
 		seekBar.setSelectedMaxValue(maxValue);
 		minValueText.setText(minValue + "");
 		maxValueText.setText(maxValue + "");
 
 		seekBar.setOnRangeSeekBarChangeListener(new OnRangeSeekBarChangeListener<Integer>() {
 			@Override
 			public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Integer minValue, Integer maxValue) {
 				minValueText.setText(minValue + "");
 				maxValueText.setText(maxValue + "");
 			}
 		});
 		return seekBar;
 	}
 
 	private View.OnClickListener minMaxRowOnClick = new View.OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			// add RangeSeekBar to pre-defined layout
 			LayoutInflater inflater = getLayoutInflater();
 			View dialoglayout = inflater.inflate(R.layout.dialog_min_max, (ViewGroup) getCurrentFocus());
 			LinearLayout seekArea = (LinearLayout) dialoglayout.findViewById(R.id.range_seek_area);
 			final TextView minValueText = (TextView) dialoglayout.findViewById(R.id.min_value);
 			final TextView maxValueText = (TextView) dialoglayout.findViewById(R.id.max_value);
 			final RangeSeekBar<Integer> seekBar = buildSeekBar(minValueText, maxValueText);
 			seekArea.addView(seekBar);
 			DialogInterface.OnClickListener positiveListener = new DialogInterface.OnClickListener() {
 
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					SetChargeConstraintsHandler handler = new SetChargeConstraintsHandler(new Callback<BaseResponse>() {
 
 						@Override
 						public void onComplete(BaseResponse response) {
							if (response == null) {
								showDeviceNotAvailable();
								return;	
							}
 							hideLoadingSpinner();
 							if (response.getResult() == 200) {
 								minValue = seekBar.getSelectedMinValue();
 								maxValue = seekBar.getSelectedMaxValue();
 							} else if (response.getResult() == 403) {
 								showForbiddenErrorDialog();
 							}
 						}
 					}, deviceId, oldPassPhrase, seekBar.getSelectedMaxValue(), seekBar.getSelectedMinValue());
 					showLoadingSpinner();
 					handler.performAction();
 				}
 			};
 			showDialogNoNegative(R.string.set_min_max_title, R.string.set_min_max_row, positiveListener, dialoglayout);
 		}
 	};
 
 	private View.OnClickListener timeRowOnClick = new View.OnClickListener() {
 
 		@Override
 		public void onClick(View arg0) {
 			DialogInterface.OnClickListener positiveListener = new DialogInterface.OnClickListener() {
 
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					TimeUpdateHandler handler = new TimeUpdateHandler(new Callback<BaseResponse>() {
 
 						@Override
 						public void onComplete(BaseResponse response) {
							if (response == null) {
								showDeviceNotAvailable();
								return;	
							}
 							hideLoadingSpinner();
 							if (response.getResult() == 200) {
 								// TODO Everything is fine, tell user
 							} else if (response.getResult() == 403) {
 								showForbiddenErrorDialog();
 							} else {
 								showDeviceNotAvailable();
 							}
 						}
 					}, deviceId, oldPassPhrase, System.currentTimeMillis());
 					showLoadingSpinner();
 					handler.performAction();
 				}
 			};
 			showDialog(R.string.set_time_title, R.string.set_time_message, positiveListener);
 		}
 	};
 
 	private View.OnClickListener locationRowOnClick = new View.OnClickListener() {
 
 		@Override
 		public void onClick(View arg0) {
 			DialogInterface.OnClickListener positiveListener = new DialogInterface.OnClickListener() {
 
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					LocationUpdateHandler handler = new LocationUpdateHandler(new Callback<BaseResponse>() {
 
 						@Override
 						public void onComplete(BaseResponse response) {
							if (response == null) {
								showDeviceNotAvailable();
								return;	
							}
 							hideLoadingSpinner();
 							if (response.getResult() == 200) {
 								// TODO Everything is fine, tell user
 							} else if (response.getResult() == 403) {
 								showForbiddenErrorDialog();
 							} else {
 								showDeviceNotAvailable();
 							}
 						}
 						// TODO Get the real values
 					}, deviceId, oldPassPhrase, 0, 0);
 					showLoadingSpinner();
 					handler.performAction();
 				}
 			};
 			showDialog(R.string.set_location_update_title, R.string.set_location_update_message, positiveListener);
 		}
 	};
 
 	private View.OnClickListener recoverRowOnClick = new View.OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			// Set an EditText view to get user input
 			final EditText input = new EditText(DevicePreferencesActivity.this);
 
 			DialogInterface.OnClickListener positiveListener = new DialogInterface.OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int whichButton) {
 					final String value = input.getText().toString();
 					PINUpdateHandler handler = new PINUpdateHandler(new Callback<BaseResponse>() {
 
 						@Override
 						public void onComplete(BaseResponse response) {
							if (response == null) {
								showDeviceNotAvailable();
								return;	
							}
 							hideLoadingSpinner();
 							if (response.getResult() == 200) {
 								oldPassPhrase = value;
 								PreferenceManager.getDefaultSharedPreferences(DevicePreferencesActivity.this).edit()
 										.putString(Constants.PASS_PHRASE_PREFERENCE + deviceId, value).commit();
 							} else if (response.getResult() == 403) {
 								showForbiddenErrorDialog();
 							} else {
 								showDeviceNotAvailable();
 							}
 						}
 					}, deviceId, value, value);
 					showLoadingSpinner();
 					handler.performAction();
 
 				}
 			};
 
 			showDialogNoNegative(R.string.set_pass_phrase_title, R.string.set_pass_phrase_message, positiveListener,
 					input);
 		}
 	};
 
 }
