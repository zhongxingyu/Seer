 /*
  *  Copyright (C) 2012 Simon Robinson
  * 
  *  This file is part of Com-Me.
  * 
  *  Com-Me is free software; you can redistribute it and/or modify it 
  *  under the terms of the GNU Lesser General Public License as 
  *  published by the Free Software Foundation; either version 3 of the 
  *  License, or (at your option) any later version.
  *
  *  Com-Me is distributed in the hope that it will be useful, but WITHOUT 
  *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
  *  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General 
  *  Public License for more details.
  *
  *  You should have received a copy of the GNU Lesser General Public
  *  License along with Com-Me.
  *  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package ac.robinson.mediatablet;
 
 import java.io.File;
 import java.lang.ref.WeakReference;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import ac.robinson.mediatablet.provider.PersonManager;
 import ac.robinson.mediautilities.MediaUtilities;
import ac.robinson.mediatablet.R;
 import ac.robinson.service.ImportingService;
 import ac.robinson.util.IOUtilities;
 import android.app.Application;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.os.Messenger;
 import android.os.RemoteException;
 import android.preference.PreferenceManager;
 
 public class MediaTabletApplication extends Application {
 
 	// for communicating with the importing service
 	private Messenger mImportingService = null;
 	private boolean mImportingServiceIsBound;
 
 	private static WeakReference<MediaTabletActivity> mCurrentActivity = null;
 	private static List<MessageContainer> mSavedMessages = Collections
 			.synchronizedList(new ArrayList<MessageContainer>());
 
 	// because messages are reused we need to save their contents instead
 	private static class MessageContainer {
 		public int what;
 		public String data;
 	}
 
 	// for clients to communicate with the ImportingService
 	private final Messenger mImportingServiceMessenger = new Messenger(new ImportingServiceMessageHandler());
 
 	@Override
 	public void onCreate() {
 		super.onCreate();
 		PersonManager.lockAllPeople(getContentResolver());
 		initialiseDirectories();
 	}
 
 	private void initialiseDirectories() {
 
 		// make sure we use the right storage location regardless of whether the user has moved the application between
 		// SD card and phone.
 		// we check for missing files in each activity, so no need to do so here
 		boolean useSDCard;
 		String storageDirectoryName = MediaTablet.APPLICATION_NAME + "_storage";
 		SharedPreferences mediaPhoneSettings = getSharedPreferences(MediaTablet.APPLICATION_NAME, Context.MODE_PRIVATE);
 		if (mediaPhoneSettings.contains(MediaTablet.KEY_USE_EXTERNAL_STORAGE)) {
 			// setting has previously been saved
 			useSDCard = mediaPhoneSettings.getBoolean(MediaTablet.KEY_USE_EXTERNAL_STORAGE,
 					IOUtilities.isInstalledOnSdCard(this));
 			if (useSDCard) {
 				MediaTablet.DIRECTORY_STORAGE = IOUtilities.getExternalStoragePath(this, storageDirectoryName);
 			} else {
 				MediaTablet.DIRECTORY_STORAGE = IOUtilities.getNewStoragePath(this, storageDirectoryName, false);
 			}
 		} else {
 			// first run
 			useSDCard = IOUtilities.isInstalledOnSdCard(this) || IOUtilities.externalStorageIsWritable();
 			MediaTablet.DIRECTORY_STORAGE = IOUtilities.getNewStoragePath(this, storageDirectoryName, useSDCard);
 
 			SharedPreferences.Editor prefsEditor = mediaPhoneSettings.edit();
 			prefsEditor.putBoolean(MediaTablet.KEY_USE_EXTERNAL_STORAGE, useSDCard);
 			prefsEditor.commit(); // apply is better, but only in API > 8
 		}
 
 		// use cache directories for thumbnails and temp (outgoing) files
 		MediaTablet.DIRECTORY_THUMBS = IOUtilities.getNewCachePath(this, MediaTablet.APPLICATION_NAME + "_thumbs",
 				false); // don't clear
 
 		// temporary directory must be world readable to be able to send files
 		if (IOUtilities.mustCreateTempDirectory(this)) {
 			if (IOUtilities.externalStorageIsWritable()) {
 				MediaTablet.DIRECTORY_TEMP = new File(Environment.getExternalStorageDirectory(),
 						MediaTablet.APPLICATION_NAME + "_temp");
 				MediaTablet.DIRECTORY_TEMP.mkdirs();
 				if (!MediaTablet.DIRECTORY_TEMP.exists()) {
 					MediaTablet.DIRECTORY_TEMP = null;
 				} else {
 					IOUtilities.setFullyPublic(MediaTablet.DIRECTORY_TEMP);
 					for (File child : MediaTablet.DIRECTORY_TEMP.listFiles()) {
 						IOUtilities.deleteRecursive(child);
 					}
 				}
 			} else {
 				MediaTablet.DIRECTORY_TEMP = null;
 			}
 		} else {
 			MediaTablet.DIRECTORY_TEMP = IOUtilities
 					.getNewCachePath(this, MediaTablet.APPLICATION_NAME + "_temp", true); // delete existing
 		}
 	}
 
 	public void registerActivityHandle(MediaTabletActivity activity) {
 		if (mCurrentActivity != null) {
 			mCurrentActivity.clear();
 			mCurrentActivity = null;
 		}
 		mCurrentActivity = new WeakReference<MediaTabletActivity>(activity);
 		for (MessageContainer msg : mSavedMessages) {
 			// must duplicate the data here, or we crash
 			Message clientMessage = Message.obtain(null, msg.what, 0, 0);
 			Bundle messageBundle = new Bundle();
 			messageBundle.putString(MediaUtilities.KEY_FILE_NAME, msg.data);
 			clientMessage.setData(messageBundle);
 
 			activity.processIncomingFiles(clientMessage);
 		}
 	}
 
 	public void removeActivityHandle(MediaTabletActivity activity) {
 		if (mCurrentActivity != null) {
 			if (mCurrentActivity.get().equals(activity)) {
 				mCurrentActivity.clear();
 				mCurrentActivity = null;
 			}
 		}
 	}
 
 	private static class ImportingServiceMessageHandler extends Handler {
 
 		@Override
 		public void handleMessage(final Message msg) {
 			if (mCurrentActivity != null) {
 				MediaTabletActivity currentActivity = mCurrentActivity.get();
 				if (currentActivity != null) {
 					currentActivity.processIncomingFiles(msg);
 				}
 			} else {
 				MessageContainer clientMessage = new MessageContainer();
 				clientMessage.what = msg.what;
 				clientMessage.data = msg.peekData().getString(MediaUtilities.KEY_FILE_NAME);
 				mSavedMessages.add(clientMessage);
 			}
 		}
 	}
 
 	private ServiceConnection mConnection = new ServiceConnection() {
 		public void onServiceConnected(ComponentName className, IBinder service) {
 			mImportingService = new Messenger(service);
 			try {
 				Message msg = Message.obtain(null, MediaUtilities.MSG_REGISTER_CLIENT);
 				msg.replyTo = mImportingServiceMessenger;
 				mImportingService.send(msg);
 
 			} catch (RemoteException e) {
 				// service has crashed before connecting; will be disconnected, restarted & reconnected automatically
 			}
 		}
 
 		public void onServiceDisconnected(ComponentName className) {
 			mImportingService = null; // unexpectedly disconnected/crashed
 		}
 	};
 
 	public void startWatchingBluetooth() {
 		SharedPreferences mediaPhoneSettings = PreferenceManager
 				.getDefaultSharedPreferences(MediaTabletApplication.this);
 		String watchedDirectory = getString(R.string.default_bluetooth_directory);
 		if (!(new File(watchedDirectory).exists())) {
 			watchedDirectory = getString(R.string.default_bluetooth_directory_alternative);
 		}
 		try {
 			String settingsDirectory = mediaPhoneSettings.getString(getString(R.string.key_bluetooth_directory),
 					watchedDirectory);
 			watchedDirectory = settingsDirectory;
 		} catch (Exception e) {
 		}
 		if (!watchedDirectory.equals(MediaTablet.IMPORT_DIRECTORY)) {
 			stopWatchingBluetooth();
 			MediaTablet.IMPORT_DIRECTORY = watchedDirectory;
 		}
 		if (!mImportingServiceIsBound) {
 			final Intent bindIntent = new Intent(MediaTabletApplication.this, ImportingService.class);
 			bindIntent.putExtra(MediaUtilities.KEY_OBSERVER_CLASS,
 					"ac.robinson.mediatablet.importing.BluetoothObserver");
 			bindIntent.putExtra(MediaUtilities.KEY_OBSERVER_PATH, MediaTablet.IMPORT_DIRECTORY);
 			bindService(bindIntent, mConnection, Context.BIND_AUTO_CREATE);
 			mImportingServiceIsBound = true;
 		}
 	}
 
 	public void stopWatchingBluetooth() {
 		if (mImportingServiceIsBound) {
 			if (mImportingService != null) {
 				try {
 					Message msg = Message.obtain(null, MediaUtilities.MSG_DISCONNECT_CLIENT);
 					msg.replyTo = mImportingServiceMessenger;
 					mImportingService.send(msg);
 				} catch (RemoteException e) {
 				}
 			}
 			unbindService(mConnection);
 			mImportingServiceIsBound = false;
 		}
 	}
 }
