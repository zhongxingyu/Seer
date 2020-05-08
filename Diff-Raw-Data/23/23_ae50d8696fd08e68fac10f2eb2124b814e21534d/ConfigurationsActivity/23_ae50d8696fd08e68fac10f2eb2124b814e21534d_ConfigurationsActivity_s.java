 package ceu.marten.ui;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.Window;
 import android.widget.AbsListView;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 import ceu.marten.bplux.R;
 import ceu.marten.model.Configuration;
 import ceu.marten.model.io.DatabaseHelper;
 import ceu.marten.ui.adapters.RecordingConfigListAdapter;
 
 import com.haarman.listviewanimations.itemmanipulation.OnDismissCallback;
 import com.haarman.listviewanimations.itemmanipulation.SwipeDismissAdapter;
 import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
 import com.j256.ormlite.dao.Dao;
 import com.j256.ormlite.stmt.QueryBuilder;
 
 public class ConfigurationsActivity extends OrmLiteBaseActivity<DatabaseHelper>
 		implements OnDismissCallback {
 
 	private static final String TAG = ConfigurationsActivity.class.getName();
 
 	private AlertDialog recordingNameDialog;
 	private ListView configurationsListView;
 	private RecordingConfigListAdapter baseAdapter;
 	private ArrayList<Configuration> configurations = null;
 	private Context classContext = this;
 	private int currentConfigurationsPosition = 0;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.ly_configurations);
 
 		loadConfigurations();
		setupRecordingNameDialog();
 		setupConfigurationsListView();
 	}
 
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (requestCode == 1) {
 			if (resultCode == RESULT_OK) {
 
 				Configuration configuration = ((Configuration) data
 						.getSerializableExtra("configuration"));
 				saveConfiguration(configuration);
 				loadConfigurations();
 				setupConfigurationsListView();
 			}
 			if (resultCode == RESULT_CANCELED) {
 
 			}
 		}
 	}
 
 	private void setupRecordingNameDialog() {
 
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		LayoutInflater inflater = this.getLayoutInflater();
 
 		builder.setView(inflater.inflate(R.layout.dialog_recording_name, null))
 				.setTitle(getString(R.string.ca_dialog_title))
 				.setPositiveButton(getString(R.string.nc_dialog_positive_button),
 						new DialogInterface.OnClickListener() {
 							@Override
 							public void onClick(DialogInterface dialog, int id) {
 								String newRecordingName = ((EditText) recordingNameDialog
 										.findViewById(R.id.dialog_txt_new_recording_name)).getText().toString();
 								
 								if (newRecordingName == null
 										|| newRecordingName.compareTo("") == 0) {
 									displayErrorToast(" *"+getString(R.string.ca_dialog_error_name)+"\n");
 								}else{
 									
 								
 								Intent intent = new Intent(classContext,
 										NewRecordingActivity.class);
 								intent.putExtra("recordingName",
 										newRecordingName);
 								intent.putExtra(
 										"configSelected",
 										configurations
 												.get(currentConfigurationsPosition));
 								startActivity(intent);
 								}
 							}
 						})
 				.setNegativeButton(getString(R.string.nc_dialog_negative_button),
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog, int id) {
 								displayInfoToast(getString(R.string.ca_info_recording_canceled));
 							}
 						});
 		recordingNameDialog = builder.create();
 
 	}
 
 	private void setupConfigurationsListView() {
 
 		final OnItemClickListener shortPressListener = new OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> AdapterView, View v,
 					int position, long id) {
 				currentConfigurationsPosition = position;
 				recordingNameDialog.show();
 
 			}
 		};
 
 		configurationsListView = (ListView) findViewById(R.id.lvConfigs);
 		configurationsListView.setOnItemClickListener(shortPressListener);
 		configurationsListView
 				.setEmptyView(findViewById(R.id.empty_list_configurations));
 
 		/** SETTING UP THE ADAPTER */
 		baseAdapter = new RecordingConfigListAdapter(this, configurations);
 		setSwipeToDismissAdapter();
 
 	}
 
 	private void setSwipeToDismissAdapter() {
 		SwipeDismissAdapter swipeAdapter = new SwipeDismissAdapter(baseAdapter,
 				this);
 		swipeAdapter.setAbsListView(configurationsListView);
 		configurationsListView.setAdapter(baseAdapter);
 	}
 
 	@Override
 	public void onDismiss(AbsListView listView, int[] reverseSortedPositions) {
 		for (int position : reverseSortedPositions) {
 			baseAdapter.remove(position);
 			Dao<Configuration, Integer> dao = null;
 
 			try {
 				dao = getHelper().getDeviceConfigDao();
 				dao.delete(configurations.get(position));
 			} catch (SQLException e) {
 				Log.e(TAG,
 						"exception removing configuration from database by swiping",
 						e);
 			}
 			configurations.remove(position);
 		}
 		displayInfoToast(getString(R.string.ca_configuration_removed));
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
 	
 	private void displayErrorToast(String messageToDisplay) {
 		Toast errorToast = new Toast(getApplicationContext());
 
 		LayoutInflater inflater = getLayoutInflater();
 		View toastView = inflater.inflate(R.layout.toast_error, null);
 		errorToast.setView(toastView);
 		((TextView) toastView.findViewById(R.id.display_text))
 				.setText(messageToDisplay);
 
 		errorToast.show();
 	}
 
 	public void saveConfiguration(Configuration config) {
 		Dao<Configuration, Integer> dao;
 		try {
 			dao = getHelper().getDeviceConfigDao();
 			dao.create(config);
 		} catch (SQLException e) {
 			Log.e(TAG, "exception saving configuration on database", e);
 		}
 	}
 
 	public void loadConfigurations() {
 		configurations = new ArrayList<Configuration>();
 		Dao<Configuration, Integer> dao;
 		try {
 			dao = getHelper().getDeviceConfigDao();
 			QueryBuilder<Configuration, Integer> builder = dao.queryBuilder();
 			builder.orderBy("createDate", false).limit(30L);
 			configurations = (ArrayList<Configuration>) dao.query(builder
 					.prepare());
 		} catch (SQLException e) {
 			Log.e(TAG, "exception loading configurations from database ", e);
 		}
 	}
 
 	/* BUTTON EVENTS */
 
 	public void onClickedNewConfig(View v) {
 		Intent intent = new Intent(this, NewConfigurationActivity.class);
 		startActivityForResult(intent, 1);
 	}
 
 }
