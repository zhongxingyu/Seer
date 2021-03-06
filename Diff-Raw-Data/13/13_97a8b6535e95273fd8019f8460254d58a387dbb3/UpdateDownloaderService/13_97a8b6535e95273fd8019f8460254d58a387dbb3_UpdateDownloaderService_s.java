 package cmupdaterapp.service;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.URI;
 import java.util.List;
 import java.util.Random;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpUriRequest;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.res.Resources;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.net.Uri;
 import android.net.wifi.WifiManager;
 import android.net.wifi.WifiManager.WifiLock;
 import android.os.Binder;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Handler;
 import android.os.HandlerThread;
 import android.os.IBinder;
 import android.os.Looper;
 import android.os.Message;
 import android.util.Log;
 import android.widget.RemoteViews;
 import android.widget.Toast;
 import cmupdaterapp.ui.ApplyUploadActivity;
 import cmupdaterapp.ui.IUpdateProcessInfo;
 import cmupdaterapp.ui.R;
 import cmupdaterapp.ui.UpdateProcessInfo;
 import cmupdaterapp.utils.IOUtils;
 import cmupdaterapp.utils.Preferences;
 
 public class UpdateDownloaderService extends Service
 {
 	//public static UpdateDownloaderService INSTANCE;
 
 	private static final String TAG = "<CM-Updater> UpdateDownloader";
 
 	public static final String KEY_REQUEST = "cmupdaterapp.request";
 	public static final String KEY_UPDATE_INFO = "cmupdaterapp.updateInfo";
 
 	public static final int REQUEST_DOWNLOAD_UPDATE = 1;
 
 	public static final int NOTIFICATION_DOWNLOAD_STATUS = 100;
 	public static final int NOTIFICATION_DOWNLOAD_FINISHED = 200;
 	
 	private static NotificationManager mNotificationManager;
 	private Notification mNotification;
 	private RemoteViews mNotificationRemoteView;
 	private Intent mNotificationIntent;
 	private PendingIntent mNotificationContentIntent;
 	
 	private boolean prepareForDownloadCancel;
 
 	private final BroadcastReceiver mConnectivityChangesReceiver = new BroadcastReceiver()
 	{
 		@Override
 		public void onReceive(Context context, Intent intent)
 		{
 			NetworkInfo netInfo = (NetworkInfo) intent.getSerializableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
 			if(netInfo != null && netInfo.isConnected())
 			{
 				synchronized (mConnectivityManager)
 				{
 					mConnectivityManager.notifyAll();
 					mWaitingForDataConnection = false;
 					unregisterReceiver(this);
 				}
 			}
 		}
 	};
 	/*
 	private final PhoneStateListener mDataStateListener = new PhoneStateListener(){
 
 		@Override
 		public void onDataConnectionStateChanged(int state) {
 			if(state == TelephonyManager.DATA_CONNECTED) {
 				synchronized (mTelephonyManager) {
 					mTelephonyManager.notifyAll();
 					mTelephonyManager.listen(mDataStateListener, PhoneStateListener.LISTEN_NONE);
 					mWaitingForDataConnection = false;
 				}
 			}
 		}
 	};*/
 
 	private Looper mServiceLooper;
 	private ServiceHandler mServiceHandler;
 	private HandlerThread mHandlerThread;
 	private NotificationManager mNM;
 	private boolean mWaitingForDataConnection = false;
 	private File mDestinationFile;
 	private File mDestinationMD5File;
 	//private TelephonyManager mTelephonyManager;
 	private DefaultHttpClient mHttpClient;
 	private DefaultHttpClient mMD5HttpClient;
 	private Random mRandom;
 	private boolean mMirrorNameUpdated;
 	private String mMirrorName;
 	private String mFileName;
 
 	private boolean mDownloading = false;
 	private UpdateInfo mCurrentUpdate;
 	private ConnectivityManager mConnectivityManager;
 	private IntentFilter mConectivityManagerIntentFilter;
 	private final IBinder mBinder = new LocalBinder();
 
 	private static IUpdateProcessInfo UPDATE_PROCESS_INFO;
 
 	private WifiLock mWifiLock;
 	private WifiManager mWifiManager;
 
 	private String mUpdateFolder;
 	private String mDownloadedMD5;
 
 	private int mSpeed;
 	private long mRemainingTime;
 	String mstringDownloaded;
 	String mstringSpeed;
 	String mstringRemainingTime;
 	
 	private Message mMsg;
 	
 	public class LocalBinder extends Binder
 	{
 		public UpdateDownloaderService getService()
 		{
 			return UpdateDownloaderService.this;
 		}
 	}
 
 	private final class ServiceHandler extends Handler
 	{
 		public ServiceHandler(Looper looper)
 		{
 			super(looper);
 		}
 
 		@Override
 		public void handleMessage(Message msg)
 		{
 			mMsg = msg;
 			
 			Bundle arguments = (Bundle)msg.obj;
 
 			int request = arguments.getInt(KEY_REQUEST); 
 			switch(request)
 			{
 				case REQUEST_DOWNLOAD_UPDATE:
 					Log.d(TAG, "Request Download Update Message was recieved");
 					mDownloading = true;
 					try
 					{
 						UpdateInfo ui = mCurrentUpdate = (UpdateInfo) arguments.getSerializable(KEY_UPDATE_INFO);
 						File downloadedUpdate = checkForConnectionAndUpdate(ui);
 						notifyUser(ui, downloadedUpdate);
 					}
 					finally
 					{
 						mDownloading = false;
 					}
 					break;
 				default:
 					Log.e(TAG, "Unknown request ID:" + request);
 			}
 
 			Log.i(TAG, "Done with #" + msg.arg1);
 			stopSelf(msg.arg1);
 		}
 	}
 
 	public static void setUpdateProcessInfo(IUpdateProcessInfo iupi)
 	{
 		UPDATE_PROCESS_INFO = iupi;
 	}
 
 	@Override
 	public void onCreate()
 	{
 		Log.d(TAG, "Download Service Created");
 		mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
 		//mTelephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
 		mConnectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
 		mConectivityManagerIntentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
 
 		mHandlerThread = new HandlerThread(TAG);
 		mHandlerThread.start();
 		mServiceLooper = mHandlerThread.getLooper();
 		mServiceHandler = new ServiceHandler(mServiceLooper);
 
 		mHttpClient = new DefaultHttpClient();
 		mMD5HttpClient = new DefaultHttpClient();
 		mRandom = new Random();
 
 		mWifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
 		mWifiLock = mWifiManager.createWifiLock("CM Updater");
 
 		mUpdateFolder = Preferences.getPreferences(this).getUpdateFolder();
 	}
 
 	@Override
 	public void onStart(Intent intent, int startId)
 	{
 		prepareForDownloadCancel = false;
 		Log.d(TAG, "Download Service started");
 		synchronized (mConnectivityManager)
 		{
 			if(mWaitingForDataConnection)
 			{
 				Log.w(TAG, "Another update process is waiting for data connection. This should not happen");
 				return;
 			}
 		}
 		Log.i(TAG, "Starting #" + startId + ": " + intent.getExtras());
 
 		// Shows Downloadstatus in Notificationbar. Initialize the Variables
 		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
 		mNotification = new Notification(R.drawable.icon_notification, getResources().getString(R.string.notification_tickertext), System.currentTimeMillis());
 		mNotification.flags = Notification.FLAG_NO_CLEAR;
 		mNotification.flags = Notification.FLAG_ONGOING_EVENT;
 		mNotificationRemoteView = new RemoteViews(getPackageName(), R.layout.notification);
 		mNotificationIntent = new Intent(this, UpdateProcessInfo.class);
 		mNotificationContentIntent = PendingIntent.getActivity(this, 0, mNotificationIntent, 0);
 		mNotification.contentView = mNotificationRemoteView;
 		mNotification.contentIntent = mNotificationContentIntent;
 		
 		Message msg = mServiceHandler.obtainMessage();
 		msg.arg1 = startId;
 		msg.obj = intent.getExtras();
 		Log.d(TAG, "Sending: " + msg);
 		mServiceHandler.sendMessage(msg);
 	}
 
 	private boolean isDataConnected()
 	{
 		if (mConnectivityManager.getActiveNetworkInfo() == null)
 		{
 			return false;
 		}
 		else
 		{
 			return mConnectivityManager.getActiveNetworkInfo().isConnected();
 		}
 	}
 
 	/**
 	private boolean isWifiNetwork()
 	{
 		return mConnectivityManager.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_WIFI;
 	}
 	 */
 
 	@Override
 	public void onDestroy()
 	{
 		Log.d(TAG, "Download Service onDestroy Called");
 		mHandlerThread.getLooper().quit();
 		try
 		{
 			Thread.currentThread().interrupt();
 			mHandlerThread.interrupt();
 			Thread.currentThread().join();
 			mHandlerThread.join();
 		}
 		catch (InterruptedException e)
 		{
 			Log.e(TAG, "Exception on Thread closing", e);
 		}
 		mDownloading = false;
 		DeleteDownloadStatusNotification(NOTIFICATION_DOWNLOAD_STATUS);
 		Log.d(TAG, "Download Service Destroyed");
 	}
 
 	@Override
 	public IBinder onBind(Intent intent)
 	{
 		Log.d(TAG, "Download Service onBind was called");
 		return mBinder;
 	}
 
 	/**
 	 * @return the downloading
 	 */
 	public boolean isDownloading()
 	{
 		return mDownloading;
 	}
 
 	/**
 	 * @return the mCurrentUpdate
 	 */
 	public UpdateInfo getCurrentUpdate()
 	{
 		return mCurrentUpdate;
 	}
 
 	private File checkForConnectionAndUpdate(UpdateInfo updateToDownload)
 	{
 		Log.d(TAG, "Called CheckForConnectionAndUpdate");
 		File downloadedFile;
 
 		//wait for a data connection
 		while(!isDataConnected())
 		{
 			Log.d(TAG, "No data connection, waiting for a data connection");
 			registerDataListener();
 			synchronized (mConnectivityManager)
 			{
 				try
 				{
 					Log.d(TAG, "No Data Connection. Waiting...");
 					mConnectivityManager.wait();
 					break;
 				}
 				catch (InterruptedException e)
 				{
 					Log.e(TAG, "Exception in ConnectivityManager.wait", e);
 				}
 			}
 		}
 
 		mWifiLock.acquire();
 
 		try
 		{
 			Log.i(TAG, "Downloading update...");
 			downloadedFile = downloadFile(updateToDownload);
 		}
 		catch (RuntimeException ex)
 		{
 			Log.e(TAG, "RuntimeEx while checking for updates", ex);
 			notificateDownloadError();
 			return null;
 		}
 		finally
 		{
 			mWifiLock.release();
 		}
 		return downloadedFile;
 	}
 
 	private void registerDataListener()
 	{
 		synchronized (mConnectivityManager)
 		{
 			//mTelephonyManager.listen(mDataStateListener, PhoneStateListener.LISTEN_DATA_CONNECTION_STATE);
 
 			registerReceiver(mConnectivityChangesReceiver, mConectivityManagerIntentFilter);
 			mWaitingForDataConnection = true;
 		}
 	}
 
 	private void notificateDownloadError()
 	{
 		Resources res = getResources();
 		Intent i = new Intent(this, UpdateProcessInfo.class)
 		.putExtra(UpdateProcessInfo.KEY_REQUEST, UpdateProcessInfo.REQUEST_DOWNLOAD_FAILED);
 
 		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, i,
 				PendingIntent.FLAG_ONE_SHOT);
 
 		Notification notification = new Notification(android.R.drawable.stat_notify_error,
 				res.getString(R.string.not_update_download_error_ticker),
 				System.currentTimeMillis());
 
 		notification.flags = Notification.FLAG_AUTO_CANCEL;
 		notification.setLatestEventInfo(
 				this,
 				res.getString(R.string.not_update_download_error_title),
 				res.getString(R.string.not_update_download_error_body),
 				contentIntent);
 
 		Uri notificationRingtone = Preferences.getPreferences(this).getConfiguredRingtone();
 		if(Preferences.getPreferences(this).getVibrate())
 			notification.defaults = Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS;
 		else
 			notification.defaults = Notification.DEFAULT_LIGHTS;
 		if(notificationRingtone == null)
 		{
 			notification.sound = null;
 		}
 		else
 		{
 			notification.sound = notificationRingtone;
 		}
 
 		//Use a resourceId as an unique identifier
 		mNM.notify(R.string.not_update_download_error_title, notification);
 	}
 
 	private synchronized File downloadFile(UpdateInfo updateInfo)
 	{
 		Log.d(TAG, "Called downloadFile");
 		HttpClient httpClient = mHttpClient;
 		HttpClient MD5httpClient = mMD5HttpClient;
 
 		HttpUriRequest req, md5req;
 		HttpResponse response, md5response;
 
 		List<URI> updateMirrors = updateInfo.updateFileUris;
 		int size = updateMirrors.size();
 		int start = mRandom.nextInt(size);
 		URI updateURI;
 		//For every Mirror
 		for(int i = 0; i < size; i++)
 		{
 			if (!prepareForDownloadCancel)
 			{
 				updateURI = updateMirrors.get((start + i)% size);
 				mMirrorName = updateURI.getHost();
 	
 				mFileName = updateInfo.fileName; 
 				if (null == mFileName || mFileName.length() < 1)
 				{
 					mFileName = "update.zip";
 				}
 				Log.d(TAG, "mFileName: " + mFileName);
 				
 				boolean md5Available = true;
 	
 				mMirrorNameUpdated = false;
 				//mUpdateProcessInfo.updateDownloadMirror(updateURI.getHost());
 				try
 				{
 					req = new HttpGet(updateURI);
 					md5req = new HttpGet(updateURI+".md5sum");
 	
 					// Add no-cache Header, so the File gets downloaded each time
 					req.addHeader("Cache-Control", "no-cache");
 					md5req.addHeader("Cache-Control", "no-cache");
 	
 					Log.i(TAG, "Trying to download md5sum file from " + md5req.getURI());
 					md5response = MD5httpClient.execute(md5req);
 					Log.i(TAG, "Trying to download update zip from " + req.getURI());
 					response = httpClient.execute(req);
 	
 					int serverResponse = response.getStatusLine().getStatusCode();
 					int md5serverResponse = md5response.getStatusLine().getStatusCode();
 					
 	
 					if (serverResponse == 404)
 					{
 						Log.e(TAG, "File not found on Server. Trying next one.");
 					}
 					else if(serverResponse != 200)
 					{
 						Log.e(TAG, "Server returned status code " + serverResponse + " for update zip trying next mirror");
 	
 					}
 					else
 					{
 						if (md5serverResponse != 200)
 						{
 							md5Available = false;
 							Log.e(TAG, "Server returned status code " + md5serverResponse + " for update zip md5sum trying next mirror");
 						}
 						//If directory not exists, create it
 						File directory = new File(Environment.getExternalStorageDirectory()+"/"+mUpdateFolder);
 						if (!directory.exists())
 						{
 							directory.mkdirs();
 							Log.d(TAG, "UpdateFolder created");
 						}
 	
 						mDestinationFile = new File(Environment.getExternalStorageDirectory()+"/"+mUpdateFolder, mFileName);
 						if(mDestinationFile.exists()) mDestinationFile.delete();
 	
 						if (md5Available)
 						{
 							mDestinationMD5File = new File(Environment.getExternalStorageDirectory()+"/"+mUpdateFolder, mFileName + ".md5sum");
 							if(mDestinationMD5File.exists()) mDestinationMD5File.delete();
 	
 							try
 							{
 								Log.i(TAG, "Trying to Read MD5 hash from response");
 								HttpEntity temp = md5response.getEntity();
 								InputStreamReader isr = new InputStreamReader(temp.getContent());
 								BufferedReader br = new BufferedReader(isr);
 								mDownloadedMD5 = br.readLine().split("  ")[0];
 								Log.d(TAG, "MD5: " + mDownloadedMD5);
 								br.close();
 								isr.close();
 	
 								if (temp != null)
 									temp.consumeContent();
 	
 								//Write the String in a .md5 File
 								if (mDownloadedMD5 != null || !mDownloadedMD5.equals(""))
 								{
 									writeMD5(mDestinationMD5File, mDownloadedMD5);
 								}
 							}
 							catch (Exception e)
 							{
 								Log.e(TAG, "Exception while reading MD5 response: ", e);
 								throw new IOException("MD5 Response cannot be read");
 							}
 						}
 	
 						// Download Update ZIP if md5sum went ok
 						HttpEntity entity = response.getEntity();
 						dumpFile(entity, mDestinationFile);
 						if (entity != null && !prepareForDownloadCancel)
 						{
 							Log.d(TAG, "Consuming entity....");
 							entity.consumeContent();
 							Log.d(TAG, "Entity consumed");
 						}
 						else
 						{
 							Log.d(TAG, "Entity resetted to NULL");
 							entity = null;
 						}
 						Log.i(TAG, "Update download finished");
 						
 						if (md5Available)
 						{
 							Log.i(TAG, "Performing MD5 verification");
 							if(!IOUtils.checkMD5(mDownloadedMD5, mDestinationFile))
 							{
 								throw new IOException("MD5 verification failed");
 							}
 						}
 	
 						//If we reach here, download & MD5 check went fine :)
 						return mDestinationFile;
 					}
 				}
 				catch (Exception ex)
 				{
 					Log.w(TAG, "An error occured while downloading the update file. Trying next mirror", ex);
 				}
 				if(Thread.currentThread().isInterrupted() || !Thread.currentThread().isAlive())
 					break;
 			}
 			else
 				Log.d(TAG, "Not trying any more mirrors, download canceled");
 		}
 		Log.e(TAG, "Unable to download the update file from any mirror");
 
 		if (null != mDestinationFile && mDestinationFile.exists())
 		{
 			mDestinationFile.delete();
 		}
 		if (null != mDestinationMD5File && mDestinationMD5File.exists())
 		{
 			mDestinationMD5File.delete();
 		}
 
 		return null;
 	}
 
 	private synchronized void dumpFile(HttpEntity entity, File destinationFile) throws IOException
 	{
 		Log.d(TAG, "DumpFile Called");
 		if(!prepareForDownloadCancel)
 		{
 			long contentLength = entity.getContentLength();
 			if(contentLength <= 0)
 			{
 				Log.w(TAG, "unable to determine the update file size, Set ContentLength to 1024");
 				contentLength = 1024;
 			}
 			else
 				Log.i(TAG, "Update size: " + (contentLength/1024) + "KB" );
 	
 			long StartTime = System.currentTimeMillis(); 
 	
 			byte[] buff = new byte[64 * 1024];
 			int read = 0;
 			int totalDownloaded = 0;
 			FileOutputStream fos = new FileOutputStream(destinationFile);
 			InputStream is = entity.getContent();
 			try
 			{
 				while(!Thread.currentThread().isInterrupted() && (read = is.read(buff)) > 0 && !prepareForDownloadCancel)
 				{
 					fos.write(buff, 0, read);
 					totalDownloaded += read;
 					onProgressUpdate(totalDownloaded, (int)contentLength, StartTime);
 				}
 	
 				if(read > 0)
 				{
 					throw new IOException("Download Canceled");
 				}
 	
 				fos.flush();
 				fos.close();
 				is.close();
 			}
 			catch(IOException e)
 			{
 				fos.close();
 				try
 				{
 					destinationFile.delete();
 				}
 				catch (Exception ex)
 				{
 					Log.e(TAG, "Unable to delete downlaoded File. Continue anyway.", ex);
 				}
 			}
 			catch(Exception e)
 			{
 				fos.close();
 				try
 				{
 					destinationFile.delete();
 				}
 				catch (Exception ex)
 				{
 					Log.e(TAG, "Unable to delete downlaoded File. Continue anyway.", ex);
 				}
 				Log.e(TAG, "Exception in DumpFile", e);
 			}
 			finally
 			{
 				buff = null;
 			}
 		}
 		else
 			Log.d(TAG, "Download Cancel in Progress. Don't start Downloading");
 	}
 
	private void writeMD5(File md5File, String md5) throws IOException
 	{
 		Log.d(TAG, "Writing the calculated MD5 to disk");
 		FileWriter fw = new FileWriter(md5File);
 		try
 		{
 			fw.write(md5);
 			fw.flush();
 		}
 		catch (Exception e)
 		{
 			Log.e(TAG, "Exception while writing MD5 to disk", e);
 		}
 		finally
 		{
 			fw.close();
 		}
 	}
 
	private void onProgressUpdate(int downloaded, int total, long StartTime)
 	{
 		//Only update the Notification and DownloadLayout, when no downloadcancel is in progress, so the notification will not pop up again
 		if (!prepareForDownloadCancel)
 		{
 			mSpeed = (downloaded/(int)(System.currentTimeMillis() - StartTime));
 			mSpeed = (mSpeed > 0) ? mSpeed : 1;
 			mRemainingTime = ((total - downloaded)/mSpeed)/1000;
 			mstringDownloaded = (downloaded/(1024*1024)) + "/" + (total/(1024*1024)) + " MB";
 			mstringSpeed = Integer.toString(mSpeed) + " kB/s";
 			mstringRemainingTime = Long.toString(mRemainingTime) + " seconds";
 			
 			mNotificationRemoteView.setTextViewText(R.id.notificationTextDownloading, mstringDownloaded);
 			mNotificationRemoteView.setTextViewText(R.id.notificationTextSpeed, mstringSpeed);
 			mNotificationRemoteView.setTextViewText(R.id.notificationTextRemainingTime, mstringRemainingTime);
 			mNotificationRemoteView.setProgressBar(R.id.notificationProgressBar, total, downloaded, false);
 			mNotificationManager.notify(NOTIFICATION_DOWNLOAD_STATUS, mNotification);
 			
 			if(UPDATE_PROCESS_INFO == null) return;
 			
 			if(!mMirrorNameUpdated)
 			{
 				UPDATE_PROCESS_INFO.updateDownloadMirror(mMirrorName);
 				mMirrorNameUpdated = true;
 			}
 			UPDATE_PROCESS_INFO.updateDownloadProgress(downloaded, total, StartTime);
 		}
 		else
 			Log.d(TAG, "Downloadcancel in Progress. Not updating the Notification and DownloadLayout");
 	}
 
 	private void notifyUser(UpdateInfo ui, File downloadedUpdate)
 	{
 		Log.d(TAG, "Called Notify User");
 		if(downloadedUpdate == null)
 		{
 			Toast.makeText(this, R.string.exception_while_downloading, Toast.LENGTH_SHORT).show();
 //			mHandlerThread.interrupt();
 //			UpdateProcessInfo upi = new UpdateProcessInfo();
 //			upi.switchToUpdateChooserLayout(null);
 			return;
 		}
 
 		Intent i = new Intent(this, ApplyUploadActivity.class);
 		i.putExtra(ApplyUploadActivity.KEY_UPDATE_INFO, ui);
 		
 		//Set the Notification to finished
 		DeleteDownloadStatusNotification(NOTIFICATION_DOWNLOAD_STATUS);
 		mNotification = new Notification(R.drawable.icon, getResources().getString(R.string.notification_finished), System.currentTimeMillis());
 		mNotification.flags = Notification.FLAG_AUTO_CANCEL;
 		mNotificationContentIntent = PendingIntent.getActivity(getApplicationContext(), 0, i, 0);
 		mNotification.setLatestEventInfo(getApplicationContext(), getResources().getString(R.string.app_name), getResources().getString(R.string.notification_finished), mNotificationContentIntent);
 		Uri notificationRingtone = Preferences.getPreferences(getApplicationContext()).getConfiguredRingtone();
 		if(Preferences.getPreferences(getApplicationContext()).getVibrate())
 			mNotification.defaults = Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS;
 		else
 			mNotification.defaults = Notification.DEFAULT_LIGHTS;
 		if(notificationRingtone == null)
 		{
 			mNotification.sound = null;
 		}
 		else
 		{
 			mNotification.sound = notificationRingtone;
 		}
 		mNotificationManager.notify(NOTIFICATION_DOWNLOAD_FINISHED, mNotification);
 		
 		if(UPDATE_PROCESS_INFO != null)
 		{
 			//app is active, switching layout
 			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 			startActivity(i);
 		}
 	}
 
 	public void cancelDownload()
 	{
 		//Thread.currentThread().interrupt();
 		prepareForDownloadCancel = true;
 		Log.d(TAG, "Download Service CancelDownload was called");
 		DeleteDownloadStatusNotification(NOTIFICATION_DOWNLOAD_STATUS);
 		mDownloading = false;
 		Log.i(TAG, "Done with #" + mMsg.arg1);
 		stopSelf(mMsg.arg1);
 		Log.d(TAG, "Download Cancel StopSelf was called");
 		stopSelf();
 	}
 	
 	private void DeleteDownloadStatusNotification(int id)
 	{
 		if(mNotificationManager != null)
 		{
 			//Delete the Downloading in Statusbar Notification
 			mNotificationManager.cancel(id);
 			Log.d(TAG, "Download Notification removed");
 		}
 		else
 		{
 			Log.d(TAG, "Download Service mNotificationManger is NULL. Notification not deleted");
 		}
 	}
 }
