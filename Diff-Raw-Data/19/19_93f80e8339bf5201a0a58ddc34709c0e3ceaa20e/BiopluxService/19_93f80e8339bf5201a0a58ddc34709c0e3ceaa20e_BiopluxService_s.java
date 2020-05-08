 package ceu.marten.services;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.util.ArrayList;
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
 	public static final int MSG_RECORDING_DURATION = 0;
 	public static final int MSG_VALUE = 5;
 
 	private String formatFileCollectedData = "%-4s %-4s %-4s %-4s %-4s %-4s %-4s %-4s %-4s%n";
 
 	ArrayList<Messenger> mClients = new ArrayList<Messenger>();
 	private NotificationManager notificationManager;
 	private Timer timer = new Timer();
 	private Configuration configuration;
 	private String recordingName;
 	private String duration;
 	private int channelToDisplay = 0;
 
 	private Device connection;
 	private Device.Frame[] frames;
 	private int counter;
 
 	final Messenger mMessenger = new Messenger(new IncomingHandler());
 
 	class IncomingHandler extends Handler { // Handler of incoming messages from
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
 		try {
 
 			String zipFileName = recordingName + ".zip";
 			String file = recordingName + ".txt";
 			File root = Environment.getExternalStorageDirectory();
 			int BUFFER = 500;
 			BufferedInputStream origin = null;
 			FileOutputStream dest = new FileOutputStream(root + "/"
 					+ zipFileName);
 			ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(
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
 			origin.close();
 			out.close();
 
 			deleteFile(recordingName + ".txt");
 
 		} catch (Exception e) {
 			Log.e(TAG, "exception while zipping", e);
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
 		counter = 0;
 		timer.scheduleAtFixedRate(new TimerTask() {
 			public void run() {
 				processFrames();
 			}
 		}, 0, 100L);
 		showNotification(intent);
 		return mMessenger.getBinder();
 	}
 
 	private void writeTextFile() {
 
 		try {
 			OutputStreamWriter out = new OutputStreamWriter(openFileOutput(
 					recordingName + ".txt", MODE_PRIVATE));
 			out.write(String.format("%-10s %-10s%n", "#configuration name: ",
 					configuration.getName()));
 			out.write(String.format("%-6s %-4s %-6s %-3s%n", "#freq: ",
 					configuration.getFrequency(), "nbits: ",
 					configuration.getNumberOfBits()));
 			out.write(String.format("%-12s %-14s%n", "#start date: ",
					configuration.getCreateDate()));
 			out.write(String.format("%-10s %-14s%n", "#duration: ", duration));
 			out.write(String.format("%-12s %n %-14s%n", "#active channels: ",
 					configuration.getActiveChannelsAsString()));
 			out.write("#num ");
 			for(int i: configuration.getActiveChannels())
 				out.write("ch "+i+" ");
 			out.write("\n");
 			//out.write(String.format(formatFileCollectedData, "#num", "ch 1",
 			//		"ch 2", "ch 3", "ch 4", "ch 5", "ch 6", "ch 7", "ch 8"));
 			out.flush();
 			out.close();
 
 			// APPEND DATA
 			FileOutputStream outBytes = new FileOutputStream(getFilesDir()
 					+ "/" + recordingName + ".txt", true);
 			BufferedOutputStream dest = new BufferedOutputStream(outBytes);
 			FileInputStream fi = new FileInputStream(getFilesDir() + "/"
 					+ "tmp.txt");
 			BufferedInputStream origin = new BufferedInputStream(fi, 1000);
 			int count;
 			byte data[] = new byte[1000];
 			while ((count = origin.read(data, 0, 1000)) != -1) {
 				dest.write(data, 0, count);
 			}
 			origin.close();
 			dest.close();
 
 			deleteFile("tmp.txt");
 
 		} catch (FileNotFoundException e) {
 			Log.e(TAG, "file to write header on, not found", e);
 		} catch (IOException e) {
 			Log.e(TAG, "write header stream exception", e);
 		}
 	}
 
 	public void writeFramesToTmpFile(Frame f) {
 		counter++;
 		try {
 			OutputStreamWriter out = new OutputStreamWriter(openFileOutput(
 					"tmp.txt", MODE_APPEND));
 			
 			out.write(String.format(formatFileCollectedData, counter,
 					String.valueOf(f.an_in[0]), String.valueOf(f.an_in[1]),
 					String.valueOf(f.an_in[2]), String.valueOf(f.an_in[3]),
 					String.valueOf(f.an_in[4]), String.valueOf(f.an_in[5]),
 					String.valueOf(f.an_in[6]), String.valueOf(f.an_in[7])));
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
 		
 		boolean[] channelsToDisplayTmp;
 		channelsToDisplayTmp = configuration.getChannelsToDisplay();
 		for (int i = 0; i < channelsToDisplayTmp.length; i++)
 			if (channelsToDisplayTmp[i]) {
 				channelToDisplay = (i + 1);
 			}
 	}
 
 	private void connectToBiopluxDevice() {
 
 		// bioPlux initialization
 		try {
 			connection = Device.Create(configuration.getMacAddress());
 			// Device mac addr 00:07:80:4C:2A:FB
 			connection.BeginAcq(configuration.getFrequency(), configuration.getActiveChannelsAsInteger(),
 					configuration.getNumberOfBits());
 		} catch (BPException e) {
 			Log.e(TAG, "bioplux connection exception", e);
 		}
 
 	}
 
 	private void processFrames() {
 		try {
 			getFrames(20);
 			for (Frame f : frames) {
 				sendMessageToUI(f.an_in[(channelToDisplay - 1)]);
 				writeFramesToTmpFile(f);
 			}
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
 		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
 		stackBuilder.addParentStack(NewRecordingActivity.class);
 		stackBuilder.addNextIntent(newRecordingIntent);
 		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
 				PendingIntent.FLAG_UPDATE_CURRENT);
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
 
 	private void sendMessageToUI(int intvaluetosend) {
 		for (int i = mClients.size() - 1; i >= 0; i--) {
 			try {
 				mClients.get(i).send(
 						Message.obtain(null, MSG_VALUE, intvaluetosend, 0));
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
