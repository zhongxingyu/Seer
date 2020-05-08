 package ceu.marten.ui;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.Window;
 import android.widget.AbsListView;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 import ceu.marten.bplux.R;
 import ceu.marten.model.DeviceConfiguration;
 import ceu.marten.model.DeviceRecording;
 import ceu.marten.model.io.DatabaseHelper;
 import ceu.marten.ui.adapters.ConfigurationsListAdapter;
 
 import com.haarman.listviewanimations.itemmanipulation.OnDismissCallback;
 import com.haarman.listviewanimations.itemmanipulation.SwipeDismissAdapter;
 import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
 import com.j256.ormlite.dao.Dao;
 import com.j256.ormlite.stmt.QueryBuilder;
 
 /**
  * Loads, from Android's internal Database, and displays a list of the device
  * configurations, previously created in NewConfigurationsActivity
  * 
  * @author Carlos Marten
  * 
  */
 public class ConfigurationsActivity extends OrmLiteBaseActivity<DatabaseHelper>
 		implements OnDismissCallback {
 
 	public static final String CONFIGURATIONS_KEY = "configurations";
 	public static final String OLD_CONFIGURATION_KEY = "oldConfiguration";
 	public static final String CONFIGURATION_POSITION_KEY = "position";
 	
 	// Used for debug purposes
 	private static final String TAG = ConfigurationsActivity.class.getName();
 	private static final int NEW_CONFIGURATION_CODE_REQUEST = 1;
 	private static final int MODIFY_CONFIGURATION_CODE_REQUEST = 2;
 	
 	private Context classContext = this;
 	
 	// to save 'do not ask again' preference on confirmation dialog
 	private SharedPreferences.Editor prefEditor = null;
 	
 	// to inflate dialogs title and content views
 	private LayoutInflater inflater;
 
 	
 	private ConfigurationsListAdapter baseAdapter;
 
 	private ArrayList<DeviceConfiguration> configurations = null;
 	// to compare new rec. name with existing rec. names to avoid duplicates
 	private ArrayList<DeviceRecording> recordings = null;
 
 	private int configurationClickedPosition = 0;
 	
 	// to delete swiped configurations
 	private int[] reverseSortedPositions;
 
 	private AlertDialog recordingNameDialog, confirmationDialog;
 
 	/**
 	 * Loads configurations from Android internal Database and sets up the
 	 * configuration's list view, the recording name dialog and the confirmation
 	 * dialog
 	 */
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.ly_configurations);
 
 		inflater = this.getLayoutInflater();
 		if (loadConfigurations()) {
 			setupConfigurationsListView();
 			setupRecordingNameDialog();
 			setupConfirmationDialog();
 		}
 	}
 
 	/**
 	 * Loads configurations from database and orders them by date
 	 * @return true if loads configurations successfully and false otherwise
 	 */
 	private boolean loadConfigurations() {
 		//Init configuration
 		configurations = new ArrayList<DeviceConfiguration>();
 		
 		//query to database with ORMLite language
 		Dao<DeviceConfiguration, Integer> dao;
 		try {
 			dao = getHelper().getDeviceConfigDao();
 			QueryBuilder<DeviceConfiguration, Integer> builder = dao.queryBuilder();
 			// order by date of creation and max rows 100
 			builder.orderBy(DeviceConfiguration.DATE_FIELD_NAME, false).limit(100L); 
 			configurations = (ArrayList<DeviceConfiguration>) dao.query(builder.prepare());
 		} catch (SQLException e) {
 			Log.e(TAG, "exception loading configurations from database ", e);
 			showErrorDialog(getResources().getString(R.string.ca_error_loading_configs_message));
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Sets up the configurations list view which is the main view of the activity
 	 * Adds short and long press listeners
 	 * Creates and sets the swipe to dismiss adapter
 	 */
 	private void setupConfigurationsListView() {
 
 		// SHORT PRESS LISTENER
 		final OnItemClickListener shortPressListener = new OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> AdapterView, View v,int position, long id) {
 				configurationClickedPosition = position;
 				recordingNameDialog.show();
 			}
 		};
 
 		// LONG PRESS LISTENER
 		final OnItemLongClickListener longPressListener = new OnItemLongClickListener() {
 
 			@Override
 			public boolean onItemLongClick(AdapterView<?> adapterView,View view, int position, long id) {
 				Intent modifyConfigurationIntent = new Intent(classContext, NewConfigurationActivity.class);
 				modifyConfigurationIntent.putExtra(CONFIGURATIONS_KEY, configurations);
 				modifyConfigurationIntent.putExtra(CONFIGURATION_POSITION_KEY, position);
 				startActivityForResult(modifyConfigurationIntent, MODIFY_CONFIGURATION_CODE_REQUEST);
 				overridePendingTransition(R.anim.slide_in_bottom,R.anim.slide_out_top);
 				return true;
 			}
 		};
 
 		// gets the listView, sets the listeners and set emptyView
 		ListView configurationsListView = (ListView) findViewById(R.id.lvConfigs);
 		configurationsListView.setOnItemClickListener(shortPressListener);
 		configurationsListView.setOnItemLongClickListener(longPressListener);
 		configurationsListView.setEmptyView(findViewById(R.id.empty_list_configurations));
 
 		// SETTING UP THE ADAPTER
 		baseAdapter = new ConfigurationsListAdapter(this, configurations);
 		setSwipeToDismissAdapter(configurationsListView);
 
 	}
 
 	/**
 	 * Sets up the recording name dialog shown when a configuration of the list is clicked
 	 * adds custom title and content
 	 * disables 'close by touching outside dialog' feature
 	 */
 	private void setupRecordingNameDialog() {
 
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 
 		TextView customTitleView = (TextView) inflater.inflate(R.layout.dialog_custom_title, null);
 		customTitleView.setText(R.string.ca_name_dialog_title);
 		builder.setCustomTitle(customTitleView).setView(inflater.inflate(R.layout.dialog_recording_name_content, null));
 				
 		recordingNameDialog = builder.create();
 		recordingNameDialog.setCanceledOnTouchOutside(false);
 	}
 
 	/**
 	 * Sets up delete confirmation dialog used when user swipes configuration away to delete it
 	 * On positive click it deletes the configuration
 	 * On negative click cancels deletion 
 	 * adds custom title and content
 	 * adds positive and negative buttons
 	 * disables default 'close by touching outside dialog' feature
 	 */
 	private void setupConfirmationDialog() {
 		
 		// prepares dialog's custom title
 		TextView customTitleView = (TextView) inflater.inflate(R.layout.dialog_custom_title, null);
 		customTitleView.setText(R.string.ca_confirm_dialog_title);
 		customTitleView.setBackgroundColor(getResources().getColor(R.color.waring_dialog));
 
 		// prepares dialog's content view
 		View contentView = inflater.inflate(R.layout.dialog_confirmation_content, null);
 		((TextView) contentView.findViewById(R.id.confirmation_message)).setText(getResources().getString(R.string.ca_confirm_dialog_message));
 		
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setCustomTitle(customTitleView)
 				.setView(contentView)
 				.setPositiveButton(
 						getString(R.string.ca_confirm_dialog_positive),
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog, int id) {
 								
 								removeConfiguration();
 								
 								//checks if don't ask again checkbox is checked if so
 								//commit the changes and reset the dialog because the view must change
 								if (prefEditor != null) {
 									prefEditor.commit();
 									setupConfirmationDialog();
 									prefEditor = null;
 								}
 							}
 						});
 		builder.setNegativeButton(getString(R.string.ca_confirm_dialog_negative),
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int id) {
 						// to regain swipe to dismiss listener
 						setupConfigurationsListView();
 						// to clear dialog check box 
 						setupConfirmationDialog();
 					}
 				});
 
 		confirmationDialog = builder.create();
 		confirmationDialog.setCanceledOnTouchOutside(false);
 	}
 
 	/**
 	 * On back pressed the stack gets cleared and the father activity is called
 	 * for proper navigation
 	 */
 	@Override
 	public void onBackPressed() {
 		Intent backIntent = new Intent(this, HomeActivity.class);
 		backIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 		backIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 		startActivity(backIntent);
 		overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
 		super.onBackPressed();
 	}
 
 	/**
 	 * Called when user comes back from NewConfigurationActivity after
 	 * creating/modifying a configuration
 	 * 
 	 */
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (resultCode == RESULT_OK) {
 			if (requestCode == NEW_CONFIGURATION_CODE_REQUEST) {
 
 				DeviceConfiguration newConfiguration = ((DeviceConfiguration) data
 						.getSerializableExtra(CONFIGURATIONS_KEY));
 				if (saveConfiguration(newConfiguration)) {
 					if (loadConfigurations())
 						setupConfigurationsListView();
 				}
 
 			} else if (requestCode == MODIFY_CONFIGURATION_CODE_REQUEST) {
 
 				DeviceConfiguration newConfiguration = ((DeviceConfiguration) data
 						.getSerializableExtra(CONFIGURATIONS_KEY));
 				DeviceConfiguration oldConfiguration = ((DeviceConfiguration) data
 						.getSerializableExtra(OLD_CONFIGURATION_KEY));
 				updateConfiguration(oldConfiguration, newConfiguration);
 				if (loadConfigurations())
 					setupConfigurationsListView();
 
 			}
 		} else {
 			// new configuration cancelled
 		}
 	}
 
 	/**
 	 * Creates and shows an error dialog with custom message when various exceptions are caught
 	 * @param errorMessage the message that will appear on the dialog's content view
 	 */
 	private void showErrorDialog(String errorMessage) {
 		
 		// Sets a custom title
 		TextView customTitleView = (TextView) inflater.inflate(R.layout.dialog_custom_title, null);
 		customTitleView.setText(R.string.ca_error_dialog_title);
 		customTitleView.setBackgroundColor(getResources().getColor(R.color.error_dialog));
 
 		// Sets a custom content with errorMessage parameter
 		View contentView = inflater.inflate(R.layout.dialog_confirmation_content, null);
 		((TextView) contentView.findViewById(R.id.confirmation_message)).setText(errorMessage);
 		
 		// builds the dialog
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setCustomTitle(customTitleView)
 				.setView(contentView)
 				.setPositiveButton(getString(R.string.bp_positive_button),
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog, int id) {
 								// do nothing. Needed so that the dialog has a button that closes the dialog
 							}
 						});
 		AlertDialog errorDialog = builder.create();
 		errorDialog.setCanceledOnTouchOutside(false);
 		errorDialog.show();
 	}
 
 	/**
 	 * called when configuring list view, this method sets the wipe to dismiss
 	 * adapter with the baseAdapter
 	 * 
 	 * @param configurationsListView
 	 */
 	private void setSwipeToDismissAdapter(ListView configurationsListView) {
 		SwipeDismissAdapter swipeAdapter = new SwipeDismissAdapter(baseAdapter, this);
 		swipeAdapter.setAbsListView(configurationsListView);
 		configurationsListView.setAdapter(baseAdapter);
 	}
 
 	/**
 	 * Callback called when user swipes configuration out of the screen
 	 */
 	@Override
 	public void onDismiss(AbsListView listView, int[] reverseSortedPositions) {
 		// needed to delete configuration on confirmation dialog positive button pressed
 		this.reverseSortedPositions = reverseSortedPositions;
 		
 		// getting shared preference to know whether or not to show confirmation dialog
 		boolean dontAskForConfrmation = false;
 		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
 		dontAskForConfrmation = sharedPref.getBoolean(SettingsActivity.KEY_PREF_CONF_CONFIG, false);
 
 		if (!dontAskForConfrmation) {
 			confirmationDialog.show();
 		} else {
 			removeConfiguration();
 		}
 	}
 	
 	/**
 	 * Removes configuration from Android's internal Database and displays a
 	 * removed toast
 	 * 
 	 * @return true if successfully deleted. False if SQLexception was caught
 	 */
 	private boolean removeConfiguration(){
 		for (int position : reverseSortedPositions) {
 			baseAdapter.remove(position);
 			Dao<DeviceConfiguration, Integer> dao = null;
 
 			try {
 				dao = getHelper().getDeviceConfigDao();
 				dao.delete(configurations.get(position));
 			} catch (SQLException e) {
 				Log.e(TAG, "exception removing configuration from database by swiping", e);
 				showErrorDialog(getResources().getString(R.string.ca_error_deleting_configuration_message));
 				return false;
 			}
 			configurations.remove(position);
 		}
 		displayInfoToast(getString(R.string.ca_configuration_removed));
 		return true;
 	}
 
 	/**
 	 * creates an info toast showing the message of the parameter it receives
 	 * @param messageToDisplay message to be display in the toast
 	 */
 	private void displayInfoToast(String messageToDisplay) {
 		Toast infoToast = new Toast(getApplicationContext());
 
 		LayoutInflater inflater = getLayoutInflater();
 		View toastView = inflater.inflate(R.layout.toast_info, null);
 		infoToast.setView(toastView);
 		((TextView) toastView.findViewById(R.id.display_text)).setText(messageToDisplay);
 
 		infoToast.show();
 	}
 
 	/**
 	 * Deletes the old configuration from the Database and creates the modified one (update process)
 	 * @param oldConfig the configuration before the user' modification
 	 * @param newConfig new configuration sent from the newConfigurationActivity
 	 */
 	private boolean updateConfiguration(DeviceConfiguration oldConfig,DeviceConfiguration newConfig) {
 		Dao<DeviceConfiguration, Integer> dao;
 		try {
 			dao = getHelper().getDeviceConfigDao();
 			dao.delete(oldConfig);
 			dao.create(newConfig);
 		} catch (SQLException e) {
 			Log.e(TAG, "exception updating configuration on database", e);
 			showErrorDialog(getResources().getString(R.string.ca_error_updating_configuration_message));
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Saves configuration in Android's internal Database. Called when a new configuration is submitted
 	 * @param config The new configuration we wish to save
 	 * @return true if saved configuration successfully. False if exception catch
 	 */
 	private boolean saveConfiguration(DeviceConfiguration config) {
 		Dao<DeviceConfiguration, Integer> dao;
 		try {
 			dao = getHelper().getDeviceConfigDao();
 			dao.create(config);
 		} catch (SQLException e) {
 			Log.e(TAG, "exception saving configuration on database", e);
 			showErrorDialog(getResources().getString(R.string.ca_error_saving_configs_message));
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Loads recordings from Android's internal Database
 	 * @return true if loading was successful. false when exception is caught 
 	 */
 	private boolean loadRecordings() {
 		Dao<DeviceRecording, Integer> dao;
 		recordings = new ArrayList<DeviceRecording>();
 		try {
 			dao = getHelper().getRecordingDao();
 			QueryBuilder<DeviceRecording, Integer> builder = dao.queryBuilder();
 			builder.orderBy(DeviceRecording.DATE_FIELD_NAME, false).limit(100L);
 			recordings = (ArrayList<DeviceRecording>) dao.query(builder.prepare());
 		} catch (SQLException e) {
 			Log.e(TAG, "exception loading recordings from database ", e);
 			showErrorDialog(getResources().getString(R.string.ca_error_loading_recordings_message));
 			return false;
 		}
 		return true;
 	}
 
 	/*************************** BUTTON EVENTS ****************************/
 
 	/**
 	 * Callback called when 'new configuration' button is clicked. Starts
 	 * newConfigurationActivity for a result passing it all the configurations
 	 * so that it can later compare the new recording name with the existing
 	 * ones avoiding duplicates
 	 * 
 	 * @param buttonView
 	 */
 	public void onClickedNewConfig(View buttonView) {
 		Intent intent = new Intent(this, NewConfigurationActivity.class);
 		intent.putExtra(CONFIGURATIONS_KEY, configurations);
 		startActivityForResult(intent, NEW_CONFIGURATION_CODE_REQUEST);
 		overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_top);
 	}
 
 	/**
 	 * Callback called when 'don't ask again' checkbox of confirmation dialog is
 	 * clicked If check box change its state to checked, puts the global
 	 * configuration confirmation preference to true. If its state changed to
 	 * unChecked, puts the preference to false
 	 * 
 	 * @param checkBoxView
 	 */
 	public void onDialogCheckBoxClicked(View checkBoxView) {
 		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
 		prefEditor = sharedPref.edit();
 
 		if (((CheckBox) checkBoxView).isChecked())
 			prefEditor.putBoolean(SettingsActivity.KEY_PREF_CONF_CONFIG, true);
 		else {
 			prefEditor.putBoolean(SettingsActivity.KEY_PREF_CONF_CONFIG, false);
 			// Used to check on confirmation dialog setup whether the preference has been checked to commit changes
 			prefEditor = null;
 		}
 	}
 
 	/**
 	 * Starts a new recording activity passing the recording name entered and
 	 * the configuration selected to display its fields on the activity
 	 * 
 	 * Recording name dialog' positive button. Implemented as follows and not as
 	 * {@code .setpositivebutton()} as other dialogs to prevent dialog from
 	 * closing when the recording name introduced already exists.
 	 * 
 	 * @param positiveButton view of the positive button
 	 */
 	public void onPositiveClick(View positiveButton) {
 		EditText recordingNameEText = (EditText) recordingNameDialog.findViewById(R.id.dialog_txt_new_recording_name);
 		String newRecordingName = recordingNameEText.getText().toString();
 		
 		if (loadRecordings()) {
 			boolean recordingNameExists = false;
 			// check whether there is at least one loaded
 			if (recordings.size()>= 1) {
 				for (DeviceRecording r : recordings) {
 					if (r.getName().compareTo(newRecordingName) == 0)
 						recordingNameExists = true;
 				}
 			}
			// check whether the user entered nothing or something and then deleted it
 			if (newRecordingName == null || newRecordingName.compareTo("") == 0) {
 				recordingNameEText.setError(getString(R.string.ca_dialog_null_name));
 			} else if (recordingNameExists) {
 				recordingNameEText.setError(getString(R.string.ca_dialog_duplicate_name));
 			} else {
 				recordingNameDialog.dismiss();
 				Intent newRecordingIntent = new Intent(classContext, NewRecordingActivity.class);
 				newRecordingIntent.putExtra("recordingName", newRecordingName);//TODO hardcode
 				newRecordingIntent.putExtra("configSelected", configurations.get(configurationClickedPosition));//TODO hardcode
 				startActivity(newRecordingIntent);
 				overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
 			}
 		}
 	}
 
 	/**
 	 * Clears the error set by positive button code (if there were any) and
 	 * dismisses the dialog
 	 * 
 	 * @param negativeButton
 	 */
 	public void onNegativeClick(View negativeButton) {
 		EditText recordingNameEText = (EditText) recordingNameDialog.findViewById(R.id.dialog_txt_new_recording_name);
 		recordingNameEText.setError(null);
 		recordingNameDialog.dismiss();
 	}
 }
