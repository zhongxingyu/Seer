 package ceu.marten.services;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.text.DateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipOutputStream;
 
 import plux.android.bioplux.BPException;
 import plux.android.bioplux.Device;
 import plux.android.bioplux.Device.Frame;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.Intent;
 import android.os.Environment;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.os.Messenger;
 import android.os.RemoteException;
 import android.support.v4.app.NotificationCompat;
 import android.util.Log;
 import ceu.marten.bplux.R;
 import ceu.marten.model.Configuration;
 import ceu.marten.ui.NewRecordingActivity;
 
 public class BiopluxService extends Service {
 
 	private static final String TAG = BiopluxService.class.getName();
 
 	public static final int MSG_REGISTER_CLIENT = 1;
 	public static final int MSG_UNREGISTER_CLIENT = 2;
 	public static final int MSG_RECORDING_DURATION = 3;
 	public static final int MSG_FIRST_DATA = 4;
 	public static final int MSG_SECOND_DATA = 5;
 
 	private String formatFileCollectedData = "%-4s %-4s %-4s %-4s %-4s %-4s %-4s %-4s %-4s%n";
 
 	static ArrayList<Messenger> mClients = new ArrayList<Messenger>();
 	private NotificationManager notificationManager;
 	private Timer timer = new Timer();
 	private Configuration configuration;
 	private String recordingName;
 	private static String duration;
 	private ArrayList<Integer> channelsToDisplay;
 	private int numberOfChannelsToDisplay;
 
 	private Device connection;
 	private Device.Frame[] frames;
 	private int frameCounter;
 	private short[] frameTmp;
 	private ArrayList<Integer> activeChannels;
 
 	final Messenger mMessenger = new Messenger(new IncomingHandler());
 
 	static class IncomingHandler extends Handler { // Handler of incoming messages from
 											// clients.
 		@Override
 		public void handleMessage(Message msg) {
 			switch (msg.what) {
 			case MSG_REGISTER_CLIENT:
 				mClients.add(msg.replyTo);
 				break;
 			case MSG_UNREGISTER_CLIENT:
 				mClients.remove(msg.replyTo);
 				break;
 			case MSG_RECORDING_DURATION:
 				duration = msg.getData().getString("duration");
 				break;
 			default:
 				super.handleMessage(msg);
 			}
 		}
 	}
 
 	private void compressFile() {
 		BufferedInputStream origin = null;
 		ZipOutputStream out = null;
 		try {
 			String zipFileName = recordingName + ".zip";
 			String file = recordingName + ".txt";
			String appDirectory=Environment.getExternalStorageDirectory().toString()+"/Bioplux/";
			File root = new File(appDirectory);
			root.mkdirs();
 			int BUFFER = 500;
 			
			FileOutputStream dest = new FileOutputStream(root +"/"+ zipFileName);
					
 			out = new ZipOutputStream(new BufferedOutputStream(
 					dest));
 			byte data[] = new byte[BUFFER];
 
 			FileInputStream fi = new FileInputStream(getFilesDir() + "/" + file);
 			origin = new BufferedInputStream(fi, BUFFER);
 
 			ZipEntry entry = new ZipEntry(
 					file.substring(file.lastIndexOf("/") + 1));
 			out.putNextEntry(entry);
 			int count;
 
 			while ((count = origin.read(data, 0, BUFFER)) != -1) {
 				out.write(data, 0, count);
 			}
 			deleteFile(recordingName + ".txt");
 
 		} catch (Exception e) {
 			Log.e(TAG, "exception while zipping", e);
 		}
 		finally{
 			try {
 				origin.close();
 				out.close();
 			} catch (IOException e) {
 				Log.e(TAG, "Exception while closing streams", e);
 			}
 			
 		}
 	}
 
 	@Override
 	public void onCreate() {
 		super.onCreate();
 		frames = new Device.Frame[20];
 		for (int i = 0; i < frames.length; i++)
 			frames[i] = new Frame();
 
 		Log.i(TAG, "Service created");
 	}
 
 	@Override
 	public IBinder onBind(Intent intent) {
 		getInfoFromActivity(intent);
 		connectToBiopluxDevice();
 		frameTmp = new short[8];
 		frameCounter = 0;
 		timer.scheduleAtFixedRate(new TimerTask() {
 			public void run() {
 				processFrames();
 			}
 		}, 0, 100L);
 		showNotification(intent);
 		return mMessenger.getBinder();
 	}
 
 	private void writeTextFile() {
 
 		DateFormat dateFormat = DateFormat.getDateTimeInstance();		
 		Date date = new Date();
 		OutputStreamWriter out = null;
 		BufferedInputStream origin = null;
 		BufferedOutputStream dest = null;
 		try {
 			out = new OutputStreamWriter(openFileOutput(
 					recordingName + ".txt", MODE_PRIVATE));
 			out.write(String.format("%-10s %-10s%n", "# "+getString(R.string.bs_header_name),
 					configuration.getName()));
 			out.write(String.format("%-10s %-14s%n", "# "+getString(R.string.bs_header_date),
 					dateFormat.format(date)));
 			out.write(String.format("%-10s %-4s%n", "# "+getString(R.string.bs_header_frequency),
 					configuration.getFrequency() + " Hz"));
 			out.write(String.format("%-10s %-10s%n", "# "+getString(R.string.bs_header_bits),
 					configuration.getNumberOfBits() + " bits"));
 			out.write(String.format("%-10s %-14s%n", "# "+getString(R.string.bs_header_duration), duration
 					+ " seconds"));
 			out.write(String.format("%-10s %-14s%n%n", "# "+getString(R.string.bs_header_active_channels),
 					configuration.getActiveChannelsAsString()));
 			out.write(String.format(formatFileCollectedData, "#num", "ch 1",
 					"ch 2", "ch 3", "ch 4", "ch 5", "ch 6", "ch 7", "ch 8"));
 			out.flush();
 			out.close();
 
 			// APPEND DATA
 			FileOutputStream outBytes = new FileOutputStream(getFilesDir()
 					+ "/" + recordingName + ".txt", true);
 			dest = new BufferedOutputStream(outBytes);
 			FileInputStream fi = new FileInputStream(getFilesDir() + "/"
 					+ "tmp.txt");
 			 
 			origin = new BufferedInputStream(fi, 1000);
 			int count;
 			byte data[] = new byte[1000];
 			while ((count = origin.read(data, 0, 1000)) != -1) {
 				dest.write(data, 0, count);
 			}
 
 		} catch (FileNotFoundException e) {
 			Log.e(TAG, "file to write header on, not found", e);
 		} catch (IOException e) {
 			Log.e(TAG, "write header stream exception", e);
 		}
 		finally{
 			try {
 				out.close();
 				origin.close();
 				dest.close();
 				deleteFile("tmp.txt");
 			} catch (IOException e) {
 				Log.e(TAG, "closing streams exception", e);
 			}
 			
 		}
 	}
 
 	public void writeFramesToTmpFile(Frame f) {
 		frameCounter++;
 		int index = 0;
 		for (int i = 0; i < activeChannels.size(); i++) {
 			index = activeChannels.get(i) - 1;
 			frameTmp[index] = f.an_in[i];
 		}
 
 		try {
 			OutputStreamWriter out = new OutputStreamWriter(openFileOutput(
 					"tmp.txt", MODE_APPEND));
 
 			out.write(String.format(formatFileCollectedData, frameCounter,
 					String.valueOf(frameTmp[0]), String.valueOf(frameTmp[1]),
 					String.valueOf(frameTmp[2]), String.valueOf(frameTmp[3]),
 					String.valueOf(frameTmp[4]), String.valueOf(frameTmp[5]),
 					String.valueOf(frameTmp[6]), String.valueOf(frameTmp[7])));
 			out.flush();
 			out.close();
 		} catch (FileNotFoundException e) {
 			Log.e(TAG, "file to write frames on, not found", e);
 		} catch (IOException e) {
 			Log.e(TAG, "write frames stream exception", e);
 		}
 	}
 
 	private void getInfoFromActivity(Intent intent) {
 		recordingName = intent.getStringExtra("recordingName").toString();
 		configuration = (Configuration) intent
 				.getSerializableExtra("configSelected");
 		activeChannels = configuration.getActiveChannels();
 		numberOfChannelsToDisplay = configuration.getNumberOfChannelsToDisplay();
 		channelsToDisplay = configuration.getChannelsToDisplay();
 	}
 
 	private void connectToBiopluxDevice() {
 
 		// bioPlux initialization
 		try {
 			connection = Device.Create(configuration.getMacAddress());
 			// Device mac addr 00:07:80:4C:2A:FB
 			connection.BeginAcq(configuration.getFrequency(),
 					configuration.getActiveChannelsAsInteger(),
 					configuration.getNumberOfBits());
 		} catch (BPException e) {
 			Log.e(TAG, "bioplux connection exception", e);
 		}
 
 	}
 
 	private void processFrames() {
 		
 			getFrames(20);
 			for (Frame f : frames) {
 				writeFramesToTmpFile(f);
 				sendFirstGraphData(f.an_in[(channelsToDisplay.get(0) - 1)]);
 				if(numberOfChannelsToDisplay==2)
 						sendSecondGraphData(f.an_in[(channelsToDisplay.get(1) - 1)]);
 			}
 		try {
 				
 		} catch (Throwable t) {
 			Log.e(TAG, "error processing frames", t);
 		}
 	}
 
 	private void getFrames(int nFrames) {
 		try {
 			connection.GetFrames(nFrames, frames);
 		} catch (BPException e) {
 			Log.e(TAG, "exception getting frames", e);
 		}
 	}
 
 	private void showNotification(Intent parentIntent) {
 
 		// SET THE BASICS
 		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
 				this).setSmallIcon(R.drawable.notification)
 				.setContentTitle("Device Connected")
 				.setContentText("service running, receiving data..");
 
 		Intent newRecordingIntent = new Intent(this, NewRecordingActivity.class);
 		newRecordingIntent.putExtra("recordingName", recordingName);
 		newRecordingIntent.putExtra("configSelected", configuration);
 		/*
 		 * TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
 		 * stackBuilder.addParentStack(NewRecordingActivity.class);
 		 * stackBuilder.addNextIntent(newRecordingIntent); PendingIntent
 		 * resultPendingIntent = stackBuilder.getPendingIntent(0,
 		 * PendingIntent.FLAG_UPDATE_CURRENT);
 		 */
 		PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0,
 				newRecordingIntent, 0);
 		mBuilder.setContentIntent(resultPendingIntent);
 
 		// mBuilder.setAutoCancel(true);
 		mBuilder.setOngoing(true);
 		Notification notification = mBuilder.build();
 		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
 		notificationManager.notify(R.string.service_id, notification);
 	}
 
 	private void sendFirstGraphData(int intvaluetosend) {
 		for (int i = mClients.size() - 1; i >= 0; i--) {
 			try {
 				mClients.get(i).send(
 						Message.obtain(null, MSG_FIRST_DATA, intvaluetosend, 0));
 			} catch (RemoteException e) {
 				Log.e(TAG, "client is dead. Removing from clients list", e);
 				mClients.remove(i);
 			}
 		}
 	}
 	private void sendSecondGraphData(int intvaluetosend) {
 		for (int i = mClients.size() - 1; i >= 0; i--) {
 			try {
 				mClients.get(i).send(
 						Message.obtain(null, MSG_SECOND_DATA, intvaluetosend, 0));
 			} catch (RemoteException e) {
 				Log.e(TAG, "client is dead. Removing from clients list", e);
 				mClients.remove(i);
 			}
 		}
 	}
 
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		return START_NOT_STICKY; // run until explicitly stopped.
 	}
 
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		if (timer != null)
 			timer.cancel();
 		try {
 			Thread.sleep(100);// for sync with main thread
 		} catch (InterruptedException e) {
 			Log.e(TAG, "interrupted sleep of thread", e);
 		}
 		try {
 			connection.EndAcq();
 		} catch (BPException e) {
 			Log.e(TAG, "error ending ACQ", e);
 		}
 		writeTextFile();
 		compressFile();
 
 		notificationManager.cancel(R.string.service_id);
 		Log.i(TAG, "service stopped");
 	}
 }
