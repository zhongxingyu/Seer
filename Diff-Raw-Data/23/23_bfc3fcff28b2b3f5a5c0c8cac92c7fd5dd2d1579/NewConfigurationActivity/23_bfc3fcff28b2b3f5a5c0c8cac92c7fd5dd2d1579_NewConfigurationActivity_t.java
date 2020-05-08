 package ceu.marten.ui;
 
 import java.text.DateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.List;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.Window;
 import android.view.inputmethod.EditorInfo;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.RadioButton;
 import android.widget.SeekBar;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 import android.widget.TextView;
 import android.widget.TextView.OnEditorActionListener;
 import android.widget.Toast;
 import ceu.marten.bplux.R;
 import ceu.marten.model.DeviceConfiguration;
 import ceu.marten.ui.adapters.ActiveChannelsListAdapter;
 import ceu.marten.ui.adapters.ChannelsToDisplayListAdapter;
 
 public class NewConfigurationActivity extends Activity {
 
 	private static final int RECEPTION_FREQ_MAX = 1000;
 	private static final int RECEPTION_FREQ_MIN = 36;
 	private static final int DEFAULT_RECEPTION_FREQ = 500;
 	private static final int SAMPLING_FREQ_MAX = 100;
 	private static final int SAMPLING_FREQ_MIN = 1;
 	private static final int DEFAULT_SAMPLING_FREQ = 50;
 	private static final int DEFAULT_NUMBER_OF_BITS = 8;
 
 	private SeekBar receptionfreqSeekbar;
 	private SeekBar samplingfreqSeekbar;
 	private EditText configurationName, macAddress, receptionFreqEditor,
 			samplingFreqEditor;
 	private TextView activeChannels, channelsToDisplay;
 	private LayoutInflater inflater;
 
 	private String[] channelsActivated = null;
 	private ArrayList<DeviceConfiguration> configurations;
 	private boolean[] channelsSelected = null;
 	private boolean isEditingConfiguration = false;
 
 	private DeviceConfiguration newConfiguration;
 	private DeviceConfiguration configurationToEdit;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.ly_new_configuration);
 
 		new InitActivity().execute("");
 	}
 
 	@Override
 	public void onBackPressed() {
 		super.onBackPressed();
 		overridePendingTransition(R.anim.slide_in_top, R.anim.slide_out_bottom);
 		displayInfoToast(getString(R.string.nc_info_canceled));
 	}
 
 	private void initializeVariables() {
 		newConfiguration = new DeviceConfiguration();
 		newConfiguration.setReceptionFrequency(DEFAULT_RECEPTION_FREQ);
 		newConfiguration.setSamplingFrequency(DEFAULT_SAMPLING_FREQ);
 		newConfiguration.setNumberOfBits(DEFAULT_NUMBER_OF_BITS);
 		inflater = (LayoutInflater) getApplicationContext().getSystemService(
 				Context.LAYOUT_INFLATER_SERVICE);
 	}
 
 	private void findViews() {
 		receptionfreqSeekbar = (SeekBar) findViewById(R.id.nc_reception_seekbar);
 		samplingfreqSeekbar = (SeekBar) findViewById(R.id.nc_sampling_seekbar);
 		receptionFreqEditor = (EditText) findViewById(R.id.nc_reception_freq_view);
 		samplingFreqEditor = (EditText) findViewById(R.id.nc_sampling_freq_view);
 		configurationName = (EditText) findViewById(R.id.dev_name);
 		macAddress = (EditText) findViewById(R.id.nc_mac_address);
 		activeChannels = (TextView) findViewById(R.id.nc_txt_active_channels);
 		channelsToDisplay = (TextView) findViewById(R.id.nc_txt_channels_to_show);
 	}
 
 	private void initializeReceptionFrequencyComponents() {
 		receptionfreqSeekbar
 				.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
 					public void onProgressChanged(SeekBar seekBar,
 							int progress, boolean changedByUser) {
 						if (changedByUser) {
 							receptionFreqEditor.setText(String.valueOf(progress
 									+ RECEPTION_FREQ_MIN));
 							newConfiguration.setReceptionFrequency(progress
 									+ RECEPTION_FREQ_MIN);
 						}
 					}
 
 					public void onStartTrackingTouch(SeekBar seekBar) {
 					}
 
 					public void onStopTrackingTouch(SeekBar seekBar) {
 					}
 				});
 		receptionFreqEditor
 				.setOnEditorActionListener(new OnEditorActionListener() {
 					@Override
 					public boolean onEditorAction(TextView currentView,
 							int actionId, KeyEvent event) {
 						boolean handled = false;
 
 						if (actionId == EditorInfo.IME_ACTION_DONE) {
 							String frequencyString = currentView.getText()
 									.toString();
 							if (frequencyString.compareTo("") == 0)
 								frequencyString = "0";
 							int newFrequency = Integer
 									.parseInt(frequencyString);
 
 							setReceptionFrequency(newFrequency);
 							closeKeyboardAndClearFocus();
 							handled = true;
 						}
 						return handled;
 					}
 
 					private void closeKeyboardAndClearFocus() {
 						receptionFreqEditor.clearFocus();
 						InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
 
 						inputManager.hideSoftInputFromWindow(getCurrentFocus()
 								.getWindowToken(),
 								InputMethodManager.HIDE_NOT_ALWAYS);
 
 					}
 				});
 	}
 
 	private void setReceptionFrequency(int newFrequency) {
 		if (newFrequency >= RECEPTION_FREQ_MIN
 				&& newFrequency <= RECEPTION_FREQ_MAX) {
 			receptionfreqSeekbar
 					.setProgress((newFrequency - RECEPTION_FREQ_MIN));
 			newConfiguration.setReceptionFrequency(newFrequency);
 		} else if (newFrequency > RECEPTION_FREQ_MAX) {
 			receptionfreqSeekbar.setProgress(RECEPTION_FREQ_MAX);
 			receptionFreqEditor.setText(String.valueOf(RECEPTION_FREQ_MAX));
 			newConfiguration.setReceptionFrequency(RECEPTION_FREQ_MAX);
 			displayErrorToast(getString(R.string.nc_error_max_frequency) + " "
 					+ RECEPTION_FREQ_MAX + "Hz");
 		} else {
 			receptionfreqSeekbar.setProgress(0);
 			receptionFreqEditor.setText(String.valueOf(RECEPTION_FREQ_MIN));
 			newConfiguration.setReceptionFrequency(RECEPTION_FREQ_MIN);
 			displayErrorToast(getString(R.string.nc_error_min_frequency) + " "
 					+ RECEPTION_FREQ_MIN + " Hz");
 		}
 	}
 
 	private void initializeSamplingFrequencyComponents() {
 		samplingfreqSeekbar
 				.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
 					public void onProgressChanged(SeekBar seekBar,
 							int progress, boolean changedByUser) {
 						if (changedByUser) {
 							samplingFreqEditor.setText(String.valueOf(progress
 									+ SAMPLING_FREQ_MIN));
 							newConfiguration.setSamplingFrequency(progress
 									+ SAMPLING_FREQ_MIN);
 						}
 					}
 
 					public void onStartTrackingTouch(SeekBar seekBar) {
 					}
 
 					public void onStopTrackingTouch(SeekBar seekBar) {
 					}
 				});
 
 		samplingFreqEditor
 				.setOnEditorActionListener(new OnEditorActionListener() {
 					@Override
 					public boolean onEditorAction(TextView currentView,
 							int actionId, KeyEvent event) {
 						boolean handled = false;
 
 						if (actionId == EditorInfo.IME_ACTION_DONE) {
 							String frequencyString = currentView.getText()
 									.toString();
 							if (frequencyString.compareTo("") == 0)
 								frequencyString = "0";
 							int newFrequency = Integer
 									.parseInt(frequencyString);
 
 							setFrequency(newFrequency);
 							closeKeyboardAndClearFocus();
 							handled = true;
 						}
 						return handled;
 					}
 
 					private void setFrequency(int newFrequency) {
 						if (newFrequency >= SAMPLING_FREQ_MIN
 								&& newFrequency <= SAMPLING_FREQ_MAX) {
 							samplingfreqSeekbar
 									.setProgress((newFrequency - SAMPLING_FREQ_MIN));
 							newConfiguration.setSamplingFrequency(newFrequency);
 						} else if (newFrequency > SAMPLING_FREQ_MAX) {
 							samplingfreqSeekbar.setProgress(SAMPLING_FREQ_MAX);
 							samplingFreqEditor.setText(String
 									.valueOf(SAMPLING_FREQ_MAX));
 							newConfiguration
 									.setSamplingFrequency(SAMPLING_FREQ_MAX);
 							displayErrorToast(getString(R.string.nc_error_max_frequency)
 									+ " " + SAMPLING_FREQ_MAX + "Hz");
 						} else {
 							samplingfreqSeekbar.setProgress(0);
 							samplingFreqEditor.setText(String
 									.valueOf(SAMPLING_FREQ_MIN));
 							newConfiguration
 									.setSamplingFrequency(SAMPLING_FREQ_MIN);
 							displayErrorToast(getString(R.string.nc_error_min_frequency)
 									+ " " + SAMPLING_FREQ_MIN + " Hz");
 						}
 					}
 
 					private void closeKeyboardAndClearFocus() {
 						samplingFreqEditor.clearFocus();
 						InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
 
 						inputManager.hideSoftInputFromWindow(getCurrentFocus()
 								.getWindowToken(),
 								InputMethodManager.HIDE_NOT_ALWAYS);
 					}
 				});
 	}
 
 	private void setupActiveChannelsDialog() {
 
 		String[] myResArray = getResources().getStringArray(R.array.channels);
 		List<String> myResArrayList = Arrays.asList(myResArray);
 		final ActiveChannelsListAdapter activeChannelsListAdapter = new ActiveChannelsListAdapter(
 				this, myResArrayList, newConfiguration.getActiveChannelsWithNullFill());
 
 		AlertDialog.Builder activeChannelsBuilder;
 		AlertDialog activeChannelsDialog;
 		ListView activeChannelsListView;
 
 		TextView customTitleView = (TextView) inflater.inflate(
 				R.layout.dialog_custom_title, null);
 		customTitleView.setText(R.string.nc_dialog_title_channels_to_activate);
 
 		// ACTIVE CHANNELS BUILDER
 		activeChannelsBuilder = new AlertDialog.Builder(this);
 		activeChannelsBuilder.setCustomTitle(customTitleView).setView(
 				getLayoutInflater().inflate(R.layout.dialog_channels_listview,
 						null));
 
 		activeChannelsBuilder.setPositiveButton(
 				getString(R.string.nc_dialog_positive_button),
 				new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 
 						channelsActivated = activeChannelsListAdapter
 								.getChecked();
 
 						newConfiguration.setActiveChannels(channelsActivated);
 						
 						if (noChannelsActivated(channelsActivated))
 							activeChannels.setText("");
 						else
 							printActivatedChannels(channelsActivated);
 					}
 
 					private void printActivatedChannels(
 							String[] channelsActivated) {
 						activeChannels.setError(null);
 						activeChannels.setTextColor(getResources().getColor(
 								R.color.blue));
 						activeChannels
 								.setText(getString(R.string.nc_channels_to_activate));
 						String si = "";
 						for (int i = 0; i < channelsActivated.length; i++) {
 							if (channelsActivated[i] != null)
 								si += (" " + String.valueOf(i + 1) + ",");
 						}
 						si = (si.substring(0, si.length() - 1));
 						activeChannels.append(si);
 
 					}
 
 				});
 		activeChannelsBuilder.setNegativeButton(
 				getString(R.string.nc_dialog_negative_button),
 				new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						dialog.dismiss();
 					}
 				});
 
 		activeChannelsDialog = activeChannelsBuilder.create();
 		activeChannelsDialog.show();
 
 		activeChannelsListView = (ListView) activeChannelsDialog
 				.findViewById(R.id.lv_channelsSelection);
 		activeChannelsListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
 		activeChannelsListView.setItemsCanFocus(false);
 		activeChannelsListView.setAdapter(activeChannelsListAdapter);
 	}
 
 	private boolean noChannelsActivated(String[] channelsActivated) {
 		int counter = 0;
 		for (int i = 0; i < channelsActivated.length; i++) {
 			if (channelsActivated[i] != null)
 				counter++;
 		}
 		if (counter == 0)
 			return true;
 		else
 			return false;
 	}
 
 	private void setupChannelsToDisplay() {
 
 		final ArrayList<String> channels = new ArrayList<String>();
 		final ArrayList<String> sensors = new ArrayList<String>();
 
 		String[] debug = newConfiguration.getActiveChannelsWithNullFill();
 		// FILL THE TWO ARRAYS
 		for (int i = 0; i < debug.length; i++) {
 			if (newConfiguration.getActiveChannelsWithNullFill()[i]
 					.compareTo("null") != 0) {
 				channels.add(getString(R.string.nc_dialog_channel) + " "
 						+ (i + 1));
 				sensors.add(newConfiguration.getActiveChannelsWithNullFill()[i]);
 			}
 		}
 
 		final ChannelsToDisplayListAdapter channelsToDisplayListAdapter = new ChannelsToDisplayListAdapter(
 				this, channels, sensors, newConfiguration.getChannelsToDisplay());
 		AlertDialog.Builder channelsToDisplayBuilder;
 		AlertDialog channelsToDisplayDialog;
 
 		TextView customTitleView = (TextView) inflater.inflate(
 				R.layout.dialog_custom_title, null);
 		customTitleView.setText(R.string.nc_dialog_title_channels_to_display);
 
 		// BUILDER
 		channelsToDisplayBuilder = new AlertDialog.Builder(this);
 		channelsToDisplayBuilder.setCustomTitle(customTitleView).setView(
 				getLayoutInflater().inflate(R.layout.dialog_channels_listview,
 						null));
 
 		channelsToDisplayBuilder.setPositiveButton(
 				getString(R.string.nc_dialog_positive_button),
 				new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						channelsSelected = channelsToDisplayListAdapter
 								.getChecked();
 						boolean[] channelsToDisplayArray = new boolean[8];
 
 						if (numberOfChannelsSelected(channelsSelected) == 0) 
 							channelsToDisplay.setText("");
 							else{
 							channelsToDisplay.setError(null);
 							channelsToDisplay.setTextColor(getResources()
 									.getColor(R.color.blue));
 							channelsToDisplay
 									.setText(R.string.nc_channels_to_display);
 							printChannelsToDisplay(channelsToDisplayArray,
 									channelsSelected);
 							newConfiguration
 									.setChannelsToDisplay(channelsToDisplayArray);
 						}
 					}
 
 					private void printChannelsToDisplay(
 							boolean[] channelsToDisplayArray,
 							boolean[] channelsSelected) {
 						String si = "";
 						for (int i = 0; i < channelsSelected.length; i++) {
 							if (channelsSelected[i]) {
 								int in = Character.getNumericValue((channels
 										.get(i).toString().charAt(channels
 										.get(i).toString().length() - 1)) - 1);
 								channelsToDisplayArray[in] = true;
 								si = si
 										+ "\n\t"
 										+ channels.get(i).toString()
 										+ " "
 										+ getString(R.string.nc_dialog_with_sensor)
 										+ " " + sensors.get(i).toString();
 							}
 						}
 						channelsToDisplay.append(si);
 					}
 
 				});
 		channelsToDisplayBuilder.setNegativeButton(
 				getString(R.string.nc_dialog_negative_button),
 				new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						dialog.dismiss();
 					}
 				});
 
 		// CREATE DIALOG
 		channelsToDisplayDialog = channelsToDisplayBuilder.create();
 		channelsToDisplayDialog.show();
 
 		// LIST VIEW CONFIGURATION
 		ListView channelsToDisplayListView;
 		channelsToDisplayListView = (ListView) channelsToDisplayDialog
 				.findViewById(R.id.lv_channelsSelection);
 		channelsToDisplayListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
 		channelsToDisplayListView.setItemsCanFocus(false);
 		channelsToDisplayListView.setAdapter(channelsToDisplayListAdapter);
 
 	}
 
 	private int numberOfChannelsSelected(boolean[] channelsSelected) {
 		int counter = 0;
 		for (int i = 0; i < channelsSelected.length; i++) {
 			if (channelsSelected[i]) {
 				counter++;
 			}
 		}
 		return counter;
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle savedInstanceState) {
 		super.onSaveInstanceState(savedInstanceState);
 
 		newConfiguration.setName(configurationName.getText().toString());
 		newConfiguration.setMacAddress(macAddress.getText().toString());
 
 		savedInstanceState.putSerializable("configuration", newConfiguration);
 		savedInstanceState.putString("activeChannels", activeChannels.getText()
 				.toString());
 		savedInstanceState.putString("channelsToDisplay", channelsToDisplay
 				.getText().toString());
 
 	}
 
 	@Override
 	protected void onRestoreInstanceState(Bundle savedInstanceState) {
 		super.onRestoreInstanceState(savedInstanceState);
 
 		newConfiguration = (DeviceConfiguration) savedInstanceState
 				.getSerializable("configuration");
 		activeChannels.setText(savedInstanceState.getString("activeChannels"));
 		channelsToDisplay.setText(savedInstanceState
 				.getString("channelsToDisplay"));
 	}
 
 	private void displayErrorToast(String messageToDisplay) {
 		Toast errorToast = new Toast(getApplicationContext());
 
 		LayoutInflater inflater = getLayoutInflater();
 		View toastView = inflater.inflate(R.layout.toast_error, null);
 		errorToast.setView(toastView);
 		((TextView) toastView.findViewById(R.id.display_text))
 				.setText(messageToDisplay);
 
 		errorToast.show();
 	}
 
 	private void displayInfoToast(String messageToDisplay) {
 		Toast infoToast = new Toast(getApplicationContext());
 
 		LayoutInflater inflater = getLayoutInflater();
 		View toastView = inflater.inflate(R.layout.toast_info, null);
 		infoToast.setView(toastView);
 		((TextView) toastView.findViewById(R.id.display_text))
 				.setText(messageToDisplay);
 
 		infoToast.show();
 	}
 
 	public void onClickedSubmit(View view) {
 
 		
 		if(!isEditingConfiguration){
 			DateFormat dateFormat = DateFormat.getDateTimeInstance();
 		Date date = new Date();
 
 		newConfiguration.setCreateDate(dateFormat.format(date));}
 		newConfiguration.setName(configurationName.getText().toString());
 		newConfiguration.setMacAddress(macAddress.getText().toString());
 		newConfiguration.setReceptionFrequency(Integer
 				.parseInt(receptionFreqEditor.getText().toString()));
 		newConfiguration.setSamplingFrequency(Integer
 				.parseInt(samplingFreqEditor.getText().toString()));
 		
 		
 		
 
 		if (validateFields()) {
 			Intent returnIntent = new Intent();
 			returnIntent.putExtra("configuration", newConfiguration);
 			
 			
 			if(!isEditingConfiguration)
 				displayInfoToast(getString(R.string.nc_info_created));
 			else{
 				returnIntent.putExtra("edited", true);
 				returnIntent.putExtra("oldConfiguration",configurationToEdit);
 				displayInfoToast(getString(R.string.nc_info_modified));
 			}
 			setResult(RESULT_OK, returnIntent);
 			finish();
 			overridePendingTransition(R.anim.slide_in_top,
 					R.anim.slide_out_bottom);
 		}
 	}
 
 	public void onClickedCancel(View view) {
 		finish();
 		overridePendingTransition(R.anim.slide_in_top, R.anim.slide_out_bottom);
 		displayInfoToast(getString(R.string.nc_info_canceled));
 	}
 
 	public void onRadioButtonClicked(View radioButtonView) {
 		boolean checked = ((RadioButton) radioButtonView).isChecked();
 
 		switch (radioButtonView.getId()) {
 		case R.id.radioBttn8:
 			if (checked)
 				newConfiguration.setNumberOfBits(8);
 			break;
 		case R.id.radioBttn12:
 			if (checked)
 				newConfiguration.setNumberOfBits(12);
 			break;
 		}
 	}
 
 	public void onClickedChannelPickDialog(View view) {
 		setupActiveChannelsDialog();
 	}
 
 	public void onClickedChannelDisplayPickDialog(View view) {
 		//CHECK IF CHANNELS TO ACTIVATE ARE FILLED ALREADY
 		if(newConfiguration.getActiveChannelsWithNullFill() !=null && newConfiguration.getNumberOfChannelsActivated() !=0)
 			setupChannelsToDisplay();
		else
			displayErrorToast(getString(R.string.nc_error_button_channels_to_display));
 	}
 
 	private boolean validateFields() {
 		boolean validated = true;
 
 		// VALIDATE NAME FIELD ALREADY EXISTS 
 			for (DeviceConfiguration c : configurations) {
 				if (c.getName().compareTo(configurationName.getText().toString()) == 0) {
 					configurationName
 							.setError(getString(R.string.nc_error_message_name_duplicate));
 					configurationName.requestFocus();
 					validated = false;
 				}
 			}
 		
 		// VALIDATE NAME FIELD IS NOT NULL
 		if (configurationName.getText().toString() == null
 				|| configurationName.getText().toString().compareTo("") == 0) {
 			configurationName
 					.setError(getString(R.string.nc_error_message_name_null));
 			configurationName.requestFocus();
 			validated = false;
 		}
 
 		String regex = "^([0-9A-F]{2}[:-]){5}([0-9A-F]{2})$";
 		// VALIDATE MAC FIELD
 		if (macAddress.getText().toString() == null
 				|| macAddress.getText().toString().compareTo("") == 0
 				|| !macAddress.getText().toString().matches(regex)
 				&& macAddress.getText().toString().compareTo("test") != 0) {
 			macAddress.setError(getString(R.string.nc_error_message_mac));
 			if (validated)
 				macAddress.requestFocus();
 			validated = false;
 		}
 		
 		// VALIDATE FREQUENCIES
 		if (receptionFreqEditor.getText().toString() == null || 
 				receptionFreqEditor.getText().toString().compareTo("") == 0 
 				|| Integer.parseInt(receptionFreqEditor.getText().toString()) < RECEPTION_FREQ_MIN
 				|| Integer.parseInt(receptionFreqEditor.getText().toString()) > RECEPTION_FREQ_MAX) {
 			receptionFreqEditor.setError("invalid frequency");
 			if (validated)
 				receptionFreqEditor.requestFocus();
 			validated = false;
 		}
 		
 		if (samplingFreqEditor.getText().toString() == null || 
 				receptionFreqEditor.getText().toString().compareTo("") == 0 
 				|| Integer.parseInt(samplingFreqEditor.getText().toString()) < SAMPLING_FREQ_MIN
 				|| Integer.parseInt(samplingFreqEditor.getText().toString()) > SAMPLING_FREQ_MAX) {
 			samplingFreqEditor.setError("invalid frequency");
 			if (validated)
 				samplingFreqEditor.requestFocus();
 			validated = false;
 		}
 			
 		// VALIDATE ACTIVE CHANNELS
 		if (channelsActivated == null || noChannelsActivated(channelsActivated)) {
 			activeChannels.setError("");
 			activeChannels.setTextColor(Color.RED);
 			activeChannels
 					.setText(getString(R.string.nc_error_message_active_channels)
 							+ "  ");
 			validated = false;
 		}
 
 		// VALIDATE CHANNELS TO DISPLAY
 		if (channelsSelected == null
 				|| numberOfChannelsSelected(channelsSelected) == 0) {
 			channelsToDisplay.setError("");
 			channelsToDisplay.setTextColor(Color.RED);
 			channelsToDisplay
 					.setText(getString(R.string.nc_error_message_channels_to_display)
 							+ "  ");
 			validated = false;
 		}
 		return validated;
 	}
 
 	private class InitActivity extends AsyncTask<String, Void, String> {
 
 		private int configurationToEditPosition = 0;
 		@SuppressWarnings("unchecked")
 		@Override
 		protected String doInBackground(String... params) {
 			configurations = new ArrayList<DeviceConfiguration>();
 			configurations = (ArrayList<DeviceConfiguration>) getIntent()
 					.getExtras().getSerializable("configurations");
 			initializeVariables();
 			findViews();
 			initializeReceptionFrequencyComponents();
 			initializeSamplingFrequencyComponents();
 			configurationToEditPosition = getIntent().getExtras().getInt("position");
 			if(!configurations.isEmpty())
 				configurationToEdit = configurations.get(configurationToEditPosition);
 			if(getIntent().getExtras().containsKey("position"))
 				isEditingConfiguration = true;
 			return "execuded";
 		}
 
 		@Override
 		protected void onPostExecute(String result) {
 			if(isEditingConfiguration){
 				//FILL FIELDS WITH CURRENT CONFIGURATION DETAILS
 				configurationName.setText(configurationToEdit.getName());
 				macAddress.setText(configurationToEdit.getMacAddress());
 				receptionfreqSeekbar.setProgress(configurationToEdit.getReceptionFrequency()- RECEPTION_FREQ_MIN);
 				receptionFreqEditor.setText(String.valueOf(configurationToEdit.getReceptionFrequency()));
 				samplingfreqSeekbar.setProgress(configurationToEdit.getSamplingFrequency());
 				samplingFreqEditor.setText(String.valueOf(configurationToEdit.getSamplingFrequency()));
 				activeChannels.setText(getString(R.string.nc_channels_to_activate)+" "+configurationToEdit.getActiveChannels());
 				channelsToDisplay.setText(getString(R.string.nc_channels_to_display)+" "+configurationToEdit.getChannelsToDisplay());
 				
 				if(configurationToEdit.getNumberOfBits() == 12){
 					((RadioButton)findViewById(R.id.radioBttn12)).setChecked(true);
 					((RadioButton)findViewById(R.id.radioBttn8)).setChecked(false);
 				}
 				
 				//MODIFY VARIABLES FOR VALIDATION PURPOSES
 				channelsActivated = configurationToEdit.getActiveChannelsWithNullFill();
 				boolean[] boolArray = {true};
 				channelsSelected = boolArray;
 				configurations.remove(configurationToEditPosition);
 				newConfiguration = configurationToEdit;
 			}
 		}
 
 		@Override
 		protected void onPreExecute() {
 		}
 
 		@Override
 		protected void onProgressUpdate(Void... values) {
 		}
 	}
 }
