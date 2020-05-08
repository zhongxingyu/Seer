 package ceu.marten.ui;
 
 import java.sql.SQLException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Locale;
 
 import plux.android.bioplux.BPException;
 import plux.android.bioplux.Device;
 
 import android.annotation.SuppressLint;
 import android.app.ActivityManager;
 import android.app.ActivityManager.RunningServiceInfo;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.bluetooth.BluetoothAdapter;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.os.Messenger;
 import android.os.RemoteException;
 import android.os.SystemClock;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.ViewGroup.LayoutParams;
 import android.view.Window;
 import android.widget.Button;
 import android.widget.Chronometer;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 import ceu.marten.bplux.R;
 import ceu.marten.model.DeviceConfiguration;
 import ceu.marten.model.Recording;
 import ceu.marten.model.io.DatabaseHelper;
 import ceu.marten.services.BiopluxService;
 
 import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
 import com.j256.ormlite.dao.Dao;
 import com.jjoe64.graphview.GraphView.GraphViewData;
 
 public class NewRecordingActivity extends OrmLiteBaseActivity<DatabaseHelper> {
 
 	private static final String TAG = NewRecordingActivity.class.getName();
 	private static int maxDataCount = 0;
 
 	//UI COMPONENTS
 	private TextView uiRecordingName, uiConfigurationName, uiNumberOfBits,
 			uiReceptionFrequency, uiSamplingFrequency, uiActiveChannels,
 			uiMacAddress;
 	private Button uiStartStopbutton;
 	private static Chronometer chronometer;
 	private boolean isChronometerRunning;
 
 	private static DeviceConfiguration currentConfiguration;
 	private Recording recording;
 	private String recordingName;
 	private String duration;
 	private Bundle extras;
 	
 	//ERROR MESSAGES
 	private static String errorMessageAddress;
 	private static String errorMessageDevice;
 	private static String errorMessageContacting;
 	private static String errorMessageAdapter;
 	private static String errorMessagePort;
 	private static String errorMessageProcessingFrames;
 	private static String errorMessageSavingRecording;
 
 	//DIALOGS
 	private AlertDialog backDialog, bluetoothConnectionDialog, overwriteDialog;
 	private static AlertDialog connectionErrorDialog;
 	private static ProgressDialog savingDialog;
 	
 	private Messenger mService = null;
	private static Graph[] graphs;
	private static double timeValue;
 	private boolean isServiceBounded;
	private static boolean serviceError = false;
 	private boolean connectionError;
 	private int bpErrorCode;
 	private Context context = this;
 	private LayoutInflater inflater;
 	private final Messenger mActivity = new Messenger(new IncomingHandler());
 
 	 @SuppressLint("HandlerLeak")
 	class IncomingHandler extends Handler {
 		@Override
 		public void handleMessage(Message msg) {
 			switch (msg.what) {
 			case BiopluxService.MSG_DATA:
 				appendDataToGraphs(msg.getData().getShortArray("frame"));
 				break;
 			case BiopluxService.MSG_CONNECTION_ERROR:
 				serviceError = true;
 				displayConnectionErrorDialog(msg.arg1);
 				break;
 			case BiopluxService.MSG_SAVED:
 				savingDialog.dismiss();
 				displayInfoToast(getString(R.string.nr_info_rec_saved));
 				break;
 			default:
 				super.handleMessage(msg);
 			}
 		}
 	}
 
 	private ServiceConnection mConnection = new ServiceConnection() {
 		// This is called when the connection with the service has been
         // established, giving us the object we can use to
         // interact with the service.  We are communicating with the
         // service using a Messenger, so here we get a client-side
         // representation of that from the raw IBinder object.
 		public void onServiceConnected(ComponentName className, IBinder service) {
 			mService = new Messenger(service);
 			isServiceBounded = true;
 			try {
 				Message msg = Message.obtain(null,
 						BiopluxService.MSG_REGISTER_CLIENT);
 				msg.replyTo = mActivity;
 				mService.send(msg);
 			} catch (RemoteException e) {
 				Log.e(TAG, "service conection failed", e);
 			}
 		}
 		
 		// This is called when the connection with the service has been
         // unexpectedly disconnected -- that is, its process crashed.
 		public void onServiceDisconnected(ComponentName className) {
 			mService = null;
 			isServiceBounded = false;
 			Log.i(TAG, "service disconnected");
 		}
 	};
 
	static void appendDataToGraphs(short[] data) {
 		if(!serviceError){
 			timeValue++;
 			for (int i = 0; i < graphs.length; i++) {
 				graphs[i].getSerie().appendData(
 						new GraphViewData(timeValue / currentConfiguration.getSamplingFrequency()*1000,
								data[currentConfiguration.getChannelsToDisplay()
										.get(i) - 1]), true, maxDataCount);
 			}
 		}
 	}
 
 	private void sendRecordingDuration() {
 		if (isServiceBounded) {
 			if (mService != null) {
 				try {
 					Message msg = Message.obtain(null,
 							BiopluxService.MSG_RECORDING_DURATION, 0, 0);
 					Bundle extras = new Bundle();
 					extras.putString("duration", duration);
 					msg.setData(extras);
 					msg.replyTo = mActivity;
 					mService.send(msg);
 	
 				} catch (RemoteException e) {
 					Log.e(TAG, "Error sending duration to service", e);
 				}
 			}
 		}
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.ly_new_recording);
 
 		// GETTING EXTRA INFO FROM INTENT
 		extras = getIntent().getExtras();
 		currentConfiguration = (DeviceConfiguration) extras
 				.getSerializable("configSelected");
 		recordingName = extras.getString("recordingName").toString();
 
 		// INIT LOCAL VARIABLES
 		int numberOfChannelsToDisplay = currentConfiguration
 				.getNumberOfChannelsToDisplay();
 		LayoutParams graphParams, detailParameters;
 		View graphsView;
 
 		// INIT GLOBAL VARIABLES
 		errorMessageAddress = getResources().getString(R.string.bp_address_incorrect);
 		errorMessageAdapter = getResources().getString(R.string.bp_adapter_not_found);
 		errorMessageDevice = getResources().getString(R.string.bp_device_not_found);
 		errorMessageContacting = getResources().getString(R.string.bp_contacting_device);
 		errorMessagePort = getResources().getString(R.string.bp_port_could_not_be_opened);
 		errorMessageProcessingFrames = getResources().getString(R.string.bp_error_processing_frames);
 		errorMessageSavingRecording = getResources().getString(R.string.bp_error_saving_recording);
 		savingDialog = new ProgressDialog(context);
 		inflater = (LayoutInflater) getLayoutInflater();
 		maxDataCount = Integer.parseInt((getResources()
 				.getString(R.string.graph_max_data_count)));
 		graphs = new Graph[numberOfChannelsToDisplay];
 		isChronometerRunning = false;
 		isServiceBounded = false;
 		bpErrorCode = 0;
 		timeValue = 0;
 		
 
 		// INIT LAYOUT
 		graphParams = new LayoutParams(LayoutParams.MATCH_PARENT,
 				Integer.parseInt((getResources()
 						.getString(R.string.graph_height))));
 		detailParameters = new LayoutParams(LayoutParams.MATCH_PARENT,
 				LayoutParams.WRAP_CONTENT);
 		graphsView = findViewById(R.id.nr_graphs);
 
 		// ON SCREEN ROTATION, RESTORE GRAPH VIEWS
 		@SuppressWarnings("deprecation")
 		final Object data = getLastNonConfigurationInstance();
 		if (data != null) {
 			graphs = (Graph[]) data;
 			for (int i = 0; i < graphs.length; i++) {
 				((ViewGroup) (graphs[i].getGraphView().getParent()))
 						.removeView(graphs[i].getGraphView());
 				View graph = inflater.inflate(R.layout.in_ly_graph, null);
 				((ViewGroup) graph).addView(graphs[i].getGraphView());
 				((ViewGroup) graphsView).addView(graph, graphParams);
 			}
 		}
 		// ELSE, NORMAL GRAPHS INITIALIZATION
 		else {
 			for (int i = 0; i < numberOfChannelsToDisplay; i++) {
 				graphs[i] = new Graph(this,
 						getString(R.string.nc_dialog_channel)
 								+ " "
 								+ currentConfiguration.getChannelsToDisplay()
 										.get(i).toString());
 				LinearLayout graph = (LinearLayout) inflater.inflate(
 						R.layout.in_ly_graph, null);
 				((ViewGroup) graph).addView(graphs[i].getGraphView());
 				((ViewGroup) graphsView).addView(graph, graphParams);
 			}
 		}
 
 		// FIND ACTIVITY GENERAL VIEWS
 		uiRecordingName = (TextView) findViewById(R.id.nr_txt_recordingName);
 		uiRecordingName.setText(recordingName);
 		uiStartStopbutton = (Button) findViewById(R.id.nr_bttn_StartPause);
 		chronometer = (Chronometer) findViewById(R.id.nr_chronometer);
 
 		// IF ONE CHANNEL IS BEING DISPLAY SHOW ITS DETAILS
 		if (numberOfChannelsToDisplay == 1) {
 			View details = inflater.inflate(R.layout.in_ly_graph_details, null);
 			((ViewGroup) graphsView).addView(details, detailParameters);
 			findDetailViews();
 
 			uiConfigurationName.setText(currentConfiguration.getName());
 			uiReceptionFrequency.setText(String.valueOf(currentConfiguration
 					.getReceptionFrequency()) + " Hz");
 			uiSamplingFrequency.setText(String.valueOf(currentConfiguration
 					.getSamplingFrequency()) + " Hz");
 			uiNumberOfBits.setText(String.valueOf(currentConfiguration
 					.getNumberOfBits()) + " bits");
 			uiMacAddress.setText(currentConfiguration.getMacAddress());
 			uiActiveChannels.setText(currentConfiguration
 					.getActiveChannelsAsString());
 		}
 
 		// IF SERVICE WAS RUNNING BIND TO IT
 		if (isServiceRunning()) {
 			bindToService();
 			uiStartStopbutton.setText(getString(R.string.nr_button_stop));
 		}
 
 		setupBackDialog();
 		setupBluetoothDialog();
 		setupConnectionErrorDialog();
 		setupOverwriteDialog();
 	}
 
 	private void setupBackDialog() {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		TextView customTitleView = (TextView) inflater.inflate(
 				R.layout.dialog_custom_title, null);
 		customTitleView.setText(R.string.nr_back_dialog_title);
 		customTitleView.setBackgroundColor(getResources().getColor(R.color.waring_dialog));
 		builder.setCustomTitle(customTitleView)
 				.setView(
 						inflater.inflate(
 								R.layout.dialog_newrecording_backbutton_content,
 								null))
 				.setPositiveButton(
 						getString(R.string.nr_back_dialog_positive_button),
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog, int id) {
 								stopChronometer();
 								sendRecordingDuration();
 								saveRecording();
 								unbindOfService();
 								stopService(new Intent(NewRecordingActivity.this,
 										BiopluxService.class));
 								Intent backIntent = new Intent(context,
 										ConfigurationsActivity.class);
 								startActivity(backIntent);
 								overridePendingTransition(R.anim.slide_in_left,
 										R.anim.slide_out_right);
 								finish();
 							}
 						});
 		builder.setNegativeButton(
 				getString(R.string.nc_dialog_negative_button),
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int id) {
 						// dialog gets closed
 					}
 				});
 
 		backDialog = builder.create();
 
 	}
 
 	private void setupBluetoothDialog() {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		TextView customTitleView = (TextView) inflater.inflate(
 				R.layout.dialog_custom_title, null);
 		customTitleView.setText(R.string.nr_bluetooth_dialog_title);
 		customTitleView.setBackgroundColor(getResources().getColor(R.color.error_dialog));
 		builder.setCustomTitle(customTitleView).setPositiveButton(
 				getString(R.string.nr_bluetooth_dialog_positive_button),
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int id) {
 						Intent intentBluetooth = new Intent();
 						intentBluetooth
 								.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
 						context.startActivity(intentBluetooth);
 					}
 				});
 		builder.setNegativeButton(
 				getString(R.string.nc_dialog_negative_button),
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int id) {
 						// dialog gets closed
 					}
 				});
 
 		bluetoothConnectionDialog = builder.create();
 	}
 	
 	private void setupConnectionErrorDialog() {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		TextView customTitleView = (TextView) inflater.inflate(R.layout.dialog_custom_title, null);
 		customTitleView.setText(R.string.nr_bluetooth_dialog_title);
 		customTitleView.setBackgroundColor(getResources().getColor(R.color.error_dialog));
 		builder.setCustomTitle(customTitleView).setPositiveButton(
 				getString(R.string.bp_positive_button),
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int id) {
 						if(serviceError){
 							stopChronometer();
 							sendRecordingDuration();
 							saveRecording();
 							unbindOfService();
 							stopService(new Intent(NewRecordingActivity.this,
 									BiopluxService.class));
 							Intent backIntent = new Intent(context,
 									ConfigurationsActivity.class);
 							startActivity(backIntent);
 							overridePendingTransition(R.anim.slide_in_left,
 									R.anim.slide_out_right);
 							finish();
 						}
 						
 					}
 				});
 		connectionErrorDialog = builder.create();
 	}
 
 	private void setupOverwriteDialog() {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		TextView customTitleView = (TextView) inflater.inflate(
 				R.layout.dialog_custom_title, null);
 		customTitleView.setText(R.string.nr_overwrite_dialog_title);
 		customTitleView.setBackgroundColor(getResources().getColor(R.color.waring_dialog));
 		builder.setCustomTitle(customTitleView)
 				.setMessage(R.string.nr_overwrite_dialog_message)
 				.setPositiveButton(
 						getString(R.string.nr_overwrite_dialog_positive_button),
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog, int id) {
 								timeValue = 0;
 								try {
 									Dao<Recording, Integer> dao = getHelper()
 											.getRecordingDao();
 									dao.delete(recording);
 								} catch (SQLException e) {
 									Log.e(TAG, "saving recording exception", e);
 								}
 								
 								//RESET GRAPHS AND GRAPH VIEWS
 								LayoutParams graphParams = new LayoutParams(LayoutParams.MATCH_PARENT,
 										Integer.parseInt((getResources()
 												.getString(R.string.graph_height))));
 								LayoutParams detailParameters = new LayoutParams(LayoutParams.MATCH_PARENT,
 										LayoutParams.WRAP_CONTENT);
 								
 								View graphsView = findViewById(R.id.nr_graphs);
 								((ViewGroup) graphsView).removeAllViews();
 								for (int i = 0; i < graphs.length; i++) {
 									graphs[i] = new Graph(context,
 											getString(R.string.nc_dialog_channel)
 													+ " "
 													+ currentConfiguration.getChannelsToDisplay()
 															.get(i).toString());
 									LinearLayout graph = (LinearLayout) inflater.inflate(
 											R.layout.in_ly_graph, null);
 									((ViewGroup) graph).addView(graphs[i].getGraphView());
 									((ViewGroup) graphsView).addView(graph, graphParams);
 								}
 							
 								if (currentConfiguration
 										.getNumberOfChannelsToDisplay() == 1) {
 									View details = inflater.inflate(R.layout.in_ly_graph_details, null);
 									((ViewGroup) graphsView).addView(details, detailParameters);
 									findDetailViews();
 
 									uiConfigurationName.setText(currentConfiguration.getName());
 									uiReceptionFrequency.setText(String.valueOf(currentConfiguration
 											.getReceptionFrequency()) + " Hz");
 									uiSamplingFrequency.setText(String.valueOf(currentConfiguration
 											.getSamplingFrequency()) + " Hz");
 									uiNumberOfBits.setText(String.valueOf(currentConfiguration
 											.getNumberOfBits()) + " bits");
 									uiMacAddress.setText(currentConfiguration.getMacAddress());
 									uiActiveChannels.setText(currentConfiguration
 											.getActiveChannelsAsString());
 								}
 									
 								startRecording();
 							}
 						});
 		builder.setNegativeButton(
 				getString(R.string.nc_dialog_negative_button),
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int id) {
 						// dialog gets closed
 					}
 				});
 
 		overwriteDialog = builder.create();
 	}
 
 	@Override
 	public void onBackPressed() {
 		if (isChronometerRunning)
 			backDialog.show();
 		else {
 			Intent backIntent = new Intent(context,
 					ConfigurationsActivity.class);
 			startActivity(backIntent);
 			overridePendingTransition(R.anim.slide_in_left,
 					R.anim.slide_out_right);
 			super.onBackPressed();
 		}
 
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle savedInstanceState) {
 		super.onSaveInstanceState(savedInstanceState);
 		if (isChronometerRunning) {
 			extras.putLong("chronometerBase", chronometer.getBase());
 		} else
 			extras.putLong("chronometerBase", 0);
 		extras.putSerializable("recording", recording);
 		extras.putDouble("xcounter", timeValue);
 		savedInstanceState.putAll(extras);
 	}
 
 	@Override
 	public Object onRetainNonConfigurationInstance() {
 		return graphs;
 	}
 
 	@Override
 	protected void onRestoreInstanceState(Bundle savedInstanceState) {
 		super.onRestoreInstanceState(savedInstanceState);
 		if (savedInstanceState.getLong("chronometerBase") != 0) {
 			chronometer.setBase(savedInstanceState.getLong("chronometerBase"));
 			chronometer.start();
 			isChronometerRunning = true;
 		}
 		recording = (Recording) savedInstanceState.getSerializable("recording");
 		currentConfiguration = (DeviceConfiguration) savedInstanceState
 				.getSerializable("configSelected");
 		recordingName = savedInstanceState.getString("recordingName")
 				.toString();
 	}
 
 	private void findDetailViews() {
 		uiConfigurationName = (TextView) findViewById(R.id.nr_txt_configName);
 		uiNumberOfBits = (TextView) findViewById(R.id.nr_txt_config_nbits);
 		uiReceptionFrequency = (TextView) findViewById(R.id.nr_reception_freq);
 		uiSamplingFrequency = (TextView) findViewById(R.id.nr_sampling_freq);
 		uiActiveChannels = (TextView) findViewById(R.id.nr_txt_channels_active);
 		uiMacAddress = (TextView) findViewById(R.id.nr_txt_mac);
 	}
 
 	public void onClickedStartStop(View view) {
 		if (!isServiceRunning() && timeValue == 0) {
 			checkBluetoothConnection();
 		} else if (!isServiceRunning() && timeValue != 0) {
 			overwriteDialog.show();
 		} else if (isServiceRunning()) {
 			stopChronometer();
 			sendRecordingDuration();
 			saveRecording();
 			unbindOfService();
 			stopService(new Intent(NewRecordingActivity.this,BiopluxService.class));
 			uiStartStopbutton.setText(getString(R.string.nr_button_start));
 			
 			savingDialog.setTitle("Compressing file...");
 			savingDialog.setMessage("Please wait. This may take a few seconds");
 			savingDialog.setCancelable(false);
 			savingDialog.setIndeterminate(true);
 			savingDialog.show();
 		}
 	}
 	
 	private void startRecording() {
 		startService(new Intent(context, BiopluxService.class));
 		bindToService();
 		displayInfoToast(getString(R.string.nr_info_started));
 		uiStartStopbutton.setText(getString(R.string.nr_button_stop));
 		startChronometer();
 	}
 
 	private boolean checkBluetoothConnection() {
 		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter
 					.getDefaultAdapter();
 			final ProgressDialog progress;
 		if(currentConfiguration.getMacAddress().compareTo("test")!=0){
 			if (mBluetoothAdapter == null) {
 				displayInfoToast("bluetooth not supported");
 				return false;
 			}
 			if (!mBluetoothAdapter.isEnabled()){
 				bluetoothConnectionDialog.setMessage(getResources().getString(R.string.nr_bluetooth_dialog_message));
 				bluetoothConnectionDialog.show();
 				return false;
 			}
 		}
 		
 		progress = ProgressDialog.show(this,getResources().getString(R.string.nr_progress_dialog_title),getResources().getString(R.string.nr_progress_dialog_message), true);
 		Thread connectionThread = new Thread(new Runnable() {
 			@Override
 			public void run() {
 				try {
 					Device connectionTest = Device.Create(currentConfiguration.getMacAddress());
 					connectionTest.Close();
 				} catch (BPException e) {
 					connectionError = true;
 					bpErrorCode = e.code;
 					Log.e(TAG, "bioplux connection exception", e);
 				}
 				
 				runOnUiThread(new Runnable(){
 				    public void run(){
 				    	progress.dismiss();
 						if(connectionError){
 							displayConnectionErrorDialog(bpErrorCode);
 						}else{
 							startRecording();
 						}
 				    }
 				});
 			}
 		});
 		
 		if(currentConfiguration.getMacAddress().compareTo("test")==0 && !isServiceRunning() && timeValue == 0)
 			connectionThread.start();
 		else if(mBluetoothAdapter.isEnabled() && !isServiceRunning() && timeValue == 0) {
 			connectionThread.start();
 		}
 		
 		return false;
 	}
 	
 	private static void displayConnectionErrorDialog(int errorCode) {
 		switch(errorCode){
 		case 1:
 			connectionErrorDialog.setMessage(errorMessageAddress);
 			break;
 		case 2:
 			connectionErrorDialog.setMessage(errorMessageAdapter);
 			break;
 		case 3:
 			connectionErrorDialog.setMessage(errorMessageDevice);
 			break;
 		case 4:
 			connectionErrorDialog.setMessage(errorMessageContacting);
 			break;
 		case 5:
 			connectionErrorDialog.setMessage(errorMessagePort);
 			break;
 		case 6:
 			connectionErrorDialog.setMessage(errorMessageProcessingFrames);
 			break;
 		case 7:
 			connectionErrorDialog.setMessage(errorMessageSavingRecording);
 			break;
 		default:
 			connectionErrorDialog.setMessage("FATAL ERROR");
 			break;
 		}
 		connectionErrorDialog.show();
 	}
 
 	private void displayInfoToast(String messageToDisplay) {
 		Toast infoToast = new Toast(context);
 
 		LayoutInflater inflater = getLayoutInflater();
 		View toastView = inflater.inflate(R.layout.toast_info, null);
 		infoToast.setView(toastView);
 		((TextView) toastView.findViewById(R.id.display_text))
 				.setText(messageToDisplay);
 
 		infoToast.show();
 	}
 
 	private void startChronometer() {
 		chronometer.setBase(SystemClock.elapsedRealtime());
 		chronometer.start();
 		isChronometerRunning = true;
 	}
 
 	private void stopChronometer() {
 		chronometer.stop();
 		Date elapsedMiliseconds = new Date(SystemClock.elapsedRealtime()
 				- chronometer.getBase());
 		DateFormat formatter = new SimpleDateFormat("HH:mm:ss",
 				Locale.getDefault());
 		duration = formatter.format(elapsedMiliseconds);
 		isChronometerRunning = false;
 	}
 
 	public void saveRecording() {
 		DateFormat dateFormat = DateFormat.getDateTimeInstance();
 		Date date = new Date();
 
 		recording = new Recording();
 		recording.setName(recordingName);
 		recording.setConfig(currentConfiguration);
 		recording.setSavedDate(dateFormat.format(date));
 		recording.setDuration(duration);
 		try {
 			Dao<Recording, Integer> dao = getHelper().getRecordingDao();
 			dao.create(recording);
 		} catch (SQLException e) {
 			Log.e(TAG, "saving recording exception", e);
 		}
 
 	}
 
 	private boolean isServiceRunning() {
 		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
 		for (RunningServiceInfo service : manager
 				.getRunningServices(Integer.MAX_VALUE)) {
 			if (BiopluxService.class.getName().equals(
 					service.service.getClassName())) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	void bindToService() {
 		Intent intent = new Intent(context, BiopluxService.class);
 		intent.putExtra("recordingName", recordingName);
 		intent.putExtra("configSelected", currentConfiguration);
 		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
 	}
 
 	
 	
 	/** Detach our existing connection with the service **/
 	void unbindOfService() {
 		if (isServiceBounded) {
 			unbindService(mConnection);
 			isServiceBounded = false;
 		}
 	}
 	
 	public void zoomIn(View view){
 		for (int i = 0; i < graphs.length; i++)
 			graphs[i].getGraphView().zoomIn(300);
 	}
 	public void zoomOut(View view){
 		for (int i = 0; i < graphs.length; i++)
 			graphs[i].getGraphView().zoomOut(300);
 	}
 	@Override
 	protected void onStop() {
 		super.onStop();
 		try {
 			unbindOfService();
 		} catch (Throwable t) {
 			Log.e(TAG,"failed to unbind from service when activity is destroyed",t);
 		}
 	}
 	@Override
 	protected void onDestroy() {
 		Log.d(TAG, "destroying recording activity");
 		super.onDestroy();
 	}
 
 }
