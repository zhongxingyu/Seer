 package ceu.marten.ui;
 
 import java.sql.SQLException;
 import java.text.DateFormat;
 import java.util.Date;
 
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
 import ceu.marten.model.DeviceRecording;
 import ceu.marten.model.io.DatabaseHelper;
 import ceu.marten.services.BiopluxService;
 
 import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
 import com.j256.ormlite.dao.Dao;
 import com.jjoe64.graphview.GraphView.GraphViewData;
 
 /**
  * Used to record a session based on a configuration and display the
  * corresponding channels or if only one is to be displayed it shows the
  * configuration' details. Connects to a Bioplux service
  * 
  * @author Carlos Marten
  * 
  */
 public class NewRecordingActivity extends OrmLiteBaseActivity<DatabaseHelper> {
 
 	private static final String TAG = NewRecordingActivity.class.getName();
 	private static int maxDataCount = 0;
 
 	// Android's widgets
 	private static TextView uiRecordingName, uiConfigurationName, uiNumberOfBits,
 			uiReceptionFrequency, uiSamplingFrequency, uiActiveChannels,
 			uiMacAddress;
 	private static Button uiStartStopbutton;
 	private static Chronometer chronometer;
 
 	// DIALOGS
 	private static AlertDialog connectionErrorDialog;
 	private static ProgressDialog savingDialog;
 	
 	// AUX VARIABLES
 	private Context classContext = this;
 	private Bundle extras;
 	private LayoutInflater inflater;
 	private static DeviceConfiguration recordingConfiguration;
 	private static DeviceRecording recording;
 	private Graph[] graphs;
 	private double  timeCounter = 0;
 	private String duration = null; 
 	
 	private boolean isServiceBounded = false;
 	private static boolean isChronometerRunning = false;
 	private static boolean stopServiceAndFinishActivity = false;
 	
 	// ERROR VARIABLES
 	private int bpErrorCode   = 0;
 	private boolean serviceError = false;
 	private boolean connectionError = false;
 	
 	// MESSENGERS USED TO COMMUNICATE ACTIVITY AND SERVICE
 	private Messenger serviceMessenger = null;
 	private final Messenger activityMessenger = new Messenger(new IncomingHandler());
 
 	/**
 	 * Handler that receives messages from the service. It receives frames data,
 	 * error messages and a saved message if service stops correctly
 	 * 
 	 * @author Carlos Marten
 	 * 
 	 */
 	 @SuppressLint("HandlerLeak")
 	class IncomingHandler extends Handler {
 		@Override
 		public void handleMessage(Message msg) {
 			switch (msg.what) {
 			case BiopluxService.MSG_DATA:
 				appendDataToGraphs(msg.getData().getShortArray("frame"));//TODO HARD CODED
 				break;
 			case BiopluxService.MSG_CONNECTION_ERROR:
 				serviceError = true;
 				displayConnectionErrorDialog(msg.arg1);
 				break;
 			case BiopluxService.MSG_SAVED:
 				savingDialog.dismiss();
 				if(stopServiceAndFinishActivity){
 					stopServiceAndFinishActivity = false; //TODO not done properly. Better move some code to onStart()
 					finish();
 					overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
 				}
 				displayInfoToast(getString(R.string.nr_info_rec_saved));
 				break;
 			default:
 				super.handleMessage(msg);
 			}
 		}
 	}
 
 	/**
 	 * Bind connection used to bind and unbind with service
 	 * onServiceConnected() called when the connection with the service has been established,
 	 * giving us the object we can use to interact with the service. We are
 	 * communicating with the service using a Messenger, so here we get a
 	 * client-side representation of that from the raw IBinder object.
 	 */
 	private ServiceConnection bindConnection = new ServiceConnection() {
 		public void onServiceConnected(ComponentName className, IBinder service) {
 			serviceMessenger = new Messenger(service);
 			isServiceBounded = true;
 			Log.i(TAG, "service binded");
 			try {
 				Message msg = Message.obtain(null, BiopluxService.MSG_REGISTER_AND_START);
 				msg.replyTo = activityMessenger;
 				serviceMessenger.send(msg);
 			} catch (RemoteException e) {
 				Log.e(TAG, "service conection failed", e);
 				//TODO not informing the user
 			}
 		}
 		/**
 		 *  This is called when the connection with the service has been
 		 *  unexpectedly disconnected -- that is, its process crashed.
 		 */
 		public void onServiceDisconnected(ComponentName className) {
 			serviceMessenger = null;
 			isServiceBounded = false;
 			Log.i(TAG, "service disconnected");
 		}
 	};
 
 	/**
 	 * Appends x and y values received from service to all active graphs. The
 	 * graph always moves to the last value added
 	 * 
 	 * @param data
 	 */
 	 void appendDataToGraphs(short[] data) {
 		if(!serviceError){
 			timeCounter++;
 			for (int i = 0; i < graphs.length; i++) {
 				graphs[i].getSerie().appendData(
 						new GraphViewData(timeCounter / recordingConfiguration.getSamplingFrequency()*1000,
 								data[i]), true, maxDataCount);
 			}
 		}
 	}
 
 	/**
 	 * Sends recording duration to the service by message when recording is
 	 * stopped
 	 */
 	private void sendRecordingDuration() {
 		if (isServiceBounded && serviceMessenger != null) {
 			try {
 				Message msg = Message.obtain(null, BiopluxService.MSG_RECORDING_DURATION, 0, 0);
 				Bundle extras = new Bundle();
 				extras.putString("duration", duration); // TODO HARD CODED
 				msg.setData(extras);
 				msg.replyTo = activityMessenger;
 				serviceMessenger.send(msg);
 
 			} catch (RemoteException e) {
 				Log.e(TAG, "Error sending duration to service", e);
 				// TODO not informing the user
 			}
 		}else{}//TODO not catching the error or informing the user
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.ly_new_recording);
 		Log.i(TAG, "onCreate()");
 
 		// GETTING EXTRA INFO FROM INTENT
 		extras = getIntent().getExtras();
 		recordingConfiguration = (DeviceConfiguration) extras.getSerializable("configSelected");//TODO HARD CODED
 		recording = new DeviceRecording();
 		recording.setName(extras.getString("recordingName").toString()); //TODO HARD CODED
 		
 
 		// INIT GLOBAL VARIABLES
 		savingDialog = new ProgressDialog(classContext);
 		inflater = this.getLayoutInflater();
 		//TODO max data count fixed in 5 seconds max
 		maxDataCount = Integer.parseInt((getResources().getString(R.string.graph_max_data_count)));
 		graphs = new Graph[recordingConfiguration.getDisplayChannelsNumber()];
 		
 		// INIT ANDROID' WIDGETS
 		uiRecordingName = (TextView) findViewById(R.id.nr_txt_recordingName);
 		uiRecordingName.setText(recording.getName());
 		uiStartStopbutton = (Button) findViewById(R.id.nr_bttn_StartPause);
 		chronometer = (Chronometer) findViewById(R.id.nr_chronometer);
 		
 		initActivityContentLayout();
 		
 		// SETUP DIALOG
 		setupConnectionErrorDialog();
 	}
 	
 	private void initActivityContentLayout() {
 		
 		LayoutParams graphParams, detailParameters;
 		View graphsView = findViewById(R.id.nr_graphs);
 		
 		// Initializes layout parameters
 		graphParams = new LayoutParams(LayoutParams.MATCH_PARENT,
 				Integer.parseInt((getResources()
 						.getString(R.string.graph_height))));
 		detailParameters = new LayoutParams(LayoutParams.MATCH_PARENT,
 				LayoutParams.WRAP_CONTENT);
 
 		// Initializes graphs layout
 		for (int i = 0; i < recordingConfiguration.getDisplayChannelsNumber(); i++) {
 			graphs[i] = new Graph(this, getString(R.string.nc_dialog_channel)
 					+ " "
 					+ recordingConfiguration.getDisplayChannels().get(i)
 							.toString());
 			LinearLayout graph = (LinearLayout) inflater.inflate(
 					R.layout.in_ly_graph, null);
 			((ViewGroup) graph).addView(graphs[i].getGraphView());
 			((ViewGroup) graphsView).addView(graph, graphParams);
 		}
 
 		// If just one channel is being displayed, show configuration details
 		if (recordingConfiguration.getDisplayChannelsNumber() == 1) {
 			View details = inflater.inflate(R.layout.in_ly_graph_details, null);
 			((ViewGroup) graphsView).addView(details, detailParameters);
 			
 			// get views
 			uiConfigurationName = (TextView) findViewById(R.id.nr_txt_configName);
 			uiNumberOfBits = (TextView) findViewById(R.id.nr_txt_config_nbits);
 			uiReceptionFrequency = (TextView) findViewById(R.id.nr_reception_freq);
 			uiSamplingFrequency = (TextView) findViewById(R.id.nr_sampling_freq);
 			uiActiveChannels = (TextView) findViewById(R.id.nr_txt_channels_active);
 			uiMacAddress = (TextView) findViewById(R.id.nr_txt_mac);
 
 			// fill them
 			uiConfigurationName.setText(recordingConfiguration.getName());
 			uiReceptionFrequency.setText(String.valueOf(recordingConfiguration
 					.getReceptionFrequency()) + " Hz");
 			uiSamplingFrequency.setText(String.valueOf(recordingConfiguration
 					.getSamplingFrequency()) + " Hz");
 			uiNumberOfBits.setText(String.valueOf(recordingConfiguration
 					.getNumberOfBits()) + " bits");
 			uiMacAddress.setText(recordingConfiguration.getMacAddress());
 			uiActiveChannels.setText(recordingConfiguration.getActiveChannels()
 					.toString());
 		}
 	}
 	
 	/**
 	 * called when the back button is pressed and the recording is still
 	 * running. On positive click, Stops and saves the recording, finishes
 	 * activity so that parent gets focus
 	 */
 	private void showBackDialog() {
 		// Sets a custom title view
 		TextView customTitleView = (TextView) inflater.inflate(R.layout.dialog_custom_title, null);
 		customTitleView.setText(R.string.nr_back_dialog_title);
 		customTitleView.setBackgroundColor(getResources().getColor(R.color.waring_dialog));
 		
 		// dialog builder
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setCustomTitle(customTitleView)
 				.setView(inflater.inflate(R.layout.dialog_newrecording_backbutton_content, null))
 				.setPositiveButton(
 						getString(R.string.nr_back_dialog_positive_button),
 						new DialogInterface.OnClickListener() {
 							// stops, saves and finishes recording
 							public void onClick(DialogInterface dialog, int id) {
 								stopRecording();
 								stopServiceAndFinishActivity = true;
 								// dialog gets closed
 							}
 						});
 		builder.setNegativeButton(
 				getString(R.string.nc_dialog_negative_button),
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int id) {
 						// dialog gets closed
 					}
 				});
 
 		AlertDialog backDialog = builder.create();
 		backDialog.show();
 
 	}
 
 	/**
 	 * Creates and shows a bluetooth error dialog if mac address is other than
 	 * 'test' and the bluetooth adapter is turned off. On positive click it
 	 * sends the user to android' settings for the user to turn bluetooth on
 	 * easily
 	 */
 	private void showBluetoothDialog() {
 		// Initializes custom title
 		TextView customTitleView = (TextView) inflater.inflate(R.layout.dialog_custom_title, null);
 		customTitleView.setText(R.string.nr_bluetooth_dialog_title);
 		customTitleView.setBackgroundColor(getResources().getColor(R.color.error_dialog));
 		
 		// dialogs builder
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setCustomTitle(customTitleView)
 				.setMessage(getResources().getString(R.string.nr_bluetooth_dialog_message))
 				.setPositiveButton(getString(R.string.nr_bluetooth_dialog_positive_button),
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int id) {
 						Intent intentBluetooth = new Intent();
 						intentBluetooth.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
 						classContext.startActivity(intentBluetooth);
 					}
 				});
 		builder.setNegativeButton(
 				getString(R.string.nc_dialog_negative_button),
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int id) {
 						// dialog gets closed
 					}
 				});
 
 		// creates and shows bluetooth dialog
 		(builder.create()).show();
 	}
 	
 	/**
 	 * Sets up a connection error dialog with custom title. This is used to add
 	 * custom message '.setMessage()' and display different possible connection
 	 * errors it '.show()'
 	 * 
 	 */
 	private void setupConnectionErrorDialog() {
 		
 		// Initializes custom title
 		TextView customTitleView = (TextView) inflater.inflate(R.layout.dialog_custom_title, null);
 		customTitleView.setText(R.string.nr_bluetooth_dialog_title);
 		customTitleView.setBackgroundColor(getResources().getColor(R.color.error_dialog));
 		
 		// builder
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setCustomTitle(customTitleView).setPositiveButton(
 				getString(R.string.bp_positive_button),
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int id) {
 						if(serviceError){
 							stopRecording();
 							stopServiceAndFinishActivity = true;
 						}
 					}
 				});
 		connectionErrorDialog = builder.create();
 	}
 
 	/**
 	 * Called when 'start recording' button is twice pressed On positive click,
 	 * the current recording is removed and graph variables and views are reset.
 	 * The overwrite recording starts right away
 	 * 
 	 */
 	private void showOverwriteDialog() {
 		
 		// initializes custom title view
 		TextView customTitleView = (TextView) inflater.inflate(R.layout.dialog_custom_title, null);
 		customTitleView.setText(R.string.nr_overwrite_dialog_title);
 		customTitleView.setBackgroundColor(getResources().getColor(R.color.waring_dialog));
 		
 		// dialog' builder
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setCustomTitle(customTitleView)
 				.setMessage(R.string.nr_overwrite_dialog_message)
 				.setPositiveButton(
 						getString(R.string.nr_overwrite_dialog_positive_button),
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog, int id) {
 								// deletes current recording from Android's internal Database
 								try {
 									Dao<DeviceRecording, Integer> dao = getHelper().getRecordingDao();
 									dao.delete(recording);
 								} catch (SQLException e) {
 									Log.e(TAG, "saving recording exception", e);
 								}
 								
 								// Reset activity content
 								timeCounter = 0;
 								View graphsView = findViewById(R.id.nr_graphs);
 								((ViewGroup) graphsView).removeAllViews();
 								initActivityContentLayout();
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
 
 		(builder.create()).show();
 	}
 
 	/**
 	 * If recording is running, shows save and quit confirmation dialog. If
 	 * service is stopped. destroys activity
 	 */
 	@Override
 	public void onBackPressed() {
 		if (isChronometerRunning)
 			showBackDialog();
 		else {
 			super.onBackPressed();
 			overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
 		}
 	}
 
 	/**
 	 * Stops and saves the recording in database and data as zip file
 	 */
 	private void stopRecording(){
 		stopChronometer();
 		sendRecordingDuration();
 		saveRecording();
 		unbindFromService();
 		stopService(new Intent(NewRecordingActivity.this,BiopluxService.class));
 		uiStartStopbutton.setText(getString(R.string.nr_button_start));
 		
 		savingDialog.setTitle(getString(R.string.nr_compressing_dialog_title));
 		savingDialog.setMessage(getString(R.string.nr_compressing_dialog_message)); 
 		savingDialog.setCancelable(false);
 		savingDialog.setIndeterminate(true);
 		savingDialog.show();
 	}
 	
 	/**
 	 * Main button of activity. Starts, overwrites and stops recording depending
 	 * of whether the recording was never started, was started or was started
 	 * and stopped
 	 * 
 	 * @param view
 	 */
 	public void onClickedStartStop(View view) { //TODO change name
 		// Starts recording
 		if (!isServiceRunning() && timeCounter == 0) {
 			startRecording();
 		// Overwrites recording
 		} else if (!isServiceRunning() && timeCounter != 0) {
 			showOverwriteDialog();
 		// Stops recording
 		} else if (isServiceRunning()) {
 			stopRecording();
 		}
 	}
 	
 
 	/**
 	 * Starts the recording if mac address is 'test' and recording is not
 	 * running OR if bluetooth is supported by the device, bluetooth is enabled,
 	 * mac is other than 'test' and recording is not running. Returns always
 	 * false for the main thread to be stopped and thus be available for the
 	 * progress dialog  spinning circle when we test the connection
 	 * 
 	 * @return boolean
 	 */
 	private boolean startRecording() {
 		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
 		final ProgressDialog progress;
 		if(recordingConfiguration.getMacAddress().compareTo("test")!= 0){ //TODO HARD CODE
 			if (mBluetoothAdapter == null) {
 				displayInfoToast("bluetooth not supported");//TODO HARD CODE
 				return false;
 			}
 			if (!mBluetoothAdapter.isEnabled()){
 				showBluetoothDialog();
 				return false;
 			}
 		}
 		
 		progress = ProgressDialog.show(this,getResources().getString(R.string.nr_progress_dialog_title),getResources().getString(R.string.nr_progress_dialog_message), true);
 		Thread connectionThread = 
 				new Thread(new Runnable() {
 			@Override
 			public void run() {
 				try {
 					Device connectionTest = Device.Create(recordingConfiguration.getMacAddress());
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
 							startService(new Intent(classContext, BiopluxService.class));
 							bindToService();
 							startChronometer();
 							uiStartStopbutton.setText(getString(R.string.nr_button_stop));
 							displayInfoToast(getString(R.string.nr_info_started));
 						}
 				    }
 				});
 			}
 		});
 		
 		if(recordingConfiguration.getMacAddress().compareTo("test")==0 && !isServiceRunning() && timeCounter == 0)
 			connectionThread.start();
 		else if(mBluetoothAdapter.isEnabled() && !isServiceRunning() && timeCounter == 0) {
 			connectionThread.start();
 		}
 		return false;
 	}
 	
 	/**
 	 * Displays an error dialog with corresponding message based on the
 	 * errorCode it receives. If code is unknown it displays FATAL ERROR message
 	 * 
 	 * @param errorCode
 	 */
 	private void displayConnectionErrorDialog(int errorCode) {
 		switch(errorCode){
 		case 1:
 			connectionErrorDialog.setMessage(getResources().getString(R.string.bp_address_incorrect));
 			break;
 		case 2:
 			connectionErrorDialog.setMessage(getResources().getString(R.string.bp_adapter_not_found));
 			break;
 		case 3:
 			connectionErrorDialog.setMessage(getResources().getString(R.string.bp_device_not_found));
 			break;
 		case 4:
 			connectionErrorDialog.setMessage(getResources().getString(R.string.bp_contacting_device));
 			break;
 		case 5:
 			connectionErrorDialog.setMessage(getResources().getString(R.string.bp_port_could_not_be_opened));
 			break;
 		case 6:
 			connectionErrorDialog.setMessage(getResources().getString(R.string.bp_error_processing_frames));
 			break;
 		case 7:
 			connectionErrorDialog.setMessage(getResources().getString(R.string.bp_error_saving_recording));
 			break;
 		default:
 			connectionErrorDialog.setMessage("FATAL ERROR"); //TODO HARD CODED
 			break;
 		}
 		connectionErrorDialog.show();
 	}
 
 	/**
 	 * Displays a custom view information toast with the message it receives as
 	 * parameter
 	 * 
 	 * @param messageToDisplay
 	 */
 	private void displayInfoToast(String messageToDisplay) {
 		Toast infoToast = new Toast(classContext);
 		View toastView = inflater.inflate(R.layout.toast_info, null);
 		infoToast.setView(toastView);
 		((TextView) toastView.findViewById(R.id.display_text)).setText(messageToDisplay);
 		infoToast.show();
 	}
 
 	/**
 	 * Starts Android' chronometer widget to display the recordings duration
 	 */
 	private void startChronometer() {
 		chronometer.setBase(SystemClock.elapsedRealtime());
 		chronometer.start();
 		isChronometerRunning = true;
 	}
 
 	/**
 	 * Stops the chronometer and calculates the duration of the recording
 	 */
 	private void stopChronometer() {
 		chronometer.stop();
		long elapsedMiliseconds = SystemClock.elapsedRealtime()
				- chronometer.getBase();
		duration = String.format("%02d:%02d:%02d",
				(int) ((elapsedMiliseconds / (1000 * 60 * 60)) % 24), 	// hours
				(int) ((elapsedMiliseconds / (1000 * 60)) % 60),	  	// minutes
				(int) (elapsedMiliseconds / 1000) % 60);				// seconds
 		isChronometerRunning = false;
 	}
 
 	/**
 	 * Saves the recording on Android's internal Database with ORMLite
 	 */
 	public void saveRecording() {
 		DateFormat dateFormat = DateFormat.getDateTimeInstance();
		Date date = new Date();
 
 		recording.setConfiguration(recordingConfiguration);
 		recording.setSavedDate(dateFormat.format(date));
 		recording.setDuration(duration);
 		try {
 			Dao<DeviceRecording, Integer> dao = getHelper().getRecordingDao();
 			dao.create(recording);
 		} catch (SQLException e) {
 			Log.e(TAG, "saving recording exception", e);
 			//TODO not informing the user
 		}
 	}
 
 	/**
 	 * Gets all the processes that are running on the OS and checks whether the
 	 * bioplux service is running. Returns true if it is running and false
 	 * otherwise
 	 * 
 	 * @return boolean
 	 */
 	private boolean isServiceRunning() {
 		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
 		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
 			if (BiopluxService.class.getName().equals(service.service.getClassName())) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Attaches connection with the service and passes the recording name and
 	 * the correspondent configuration to it on its intent
 	 */
 	void bindToService() {
 		Intent intent = new Intent(classContext, BiopluxService.class);
 		intent.putExtra("recordingName", recording.getName());//TODO HARD CODE
 		intent.putExtra("configSelected", recordingConfiguration);//TODO HARD CODE
 		bindService(intent, bindConnection, Context.BIND_AUTO_CREATE);
 	}
 
 	/**
 	 * Detach our existing connection with the service
 	 */
 	void unbindFromService() {
 		if (isServiceBounded) {
 			unbindService(bindConnection);
 			isServiceBounded = false;
 			Log.i(TAG, "service unbinded");
 		}
 	}
 	
 	/**
 	 * Widens the graphs' view port
 	 * @param view
 	 */
 	public void zoomIn(View view){
 		for (int i = 0; i < graphs.length; i++)
 			graphs[i].getGraphView().zoomIn(300); //TODO HARD CODE, fixed zoom
 	}
 	
 	/**
 	 * Shortens the graphs' view port
 	 * @param view
 	 */
 	public void zoomOut(View view){
 		for (int i = 0; i < graphs.length; i++)
 			graphs[i].getGraphView().zoomOut(300); //TODO HARD CODE, fixed zoom
 	}
 	
 	/**
 	 * Destroys activity
 	 */
 	@Override
 	protected void onDestroy() {
 		try {
 			unbindFromService();
 		} catch (Throwable t) {
 			Log.e(TAG,"failed to unbind from service when activity is destroyed", t);
 			//TODO not notifying the user
 		}
 		super.onDestroy();
 		Log.i(TAG, "onDestroy()");
 	}
 
 }
