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
 
 package ac.robinson.mediatablet.importing;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.LineNumberReader;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import ac.robinson.mediatablet.MediaTablet;
 import ac.robinson.mediautilities.MediaUtilities;
 import ac.robinson.mediautilities.SMILUtilities;
 import ac.robinson.util.DebugUtilities;
 import ac.robinson.util.IOUtilities;
 import android.bluetooth.BluetoothAdapter;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.FileObserver;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 
 public class BluetoothObserver extends FileObserver {
 
 	// synchronized because onEvent() runs on a separate thread
 	private Map<String, Map<String, Boolean>> mSMILContents = Collections
 			.synchronizedMap(new HashMap<String, Map<String, Boolean>>());
 	private List<String> mIgnoredFiles = Collections.synchronizedList(new ArrayList<String>());
 
 	private final Handler mHandler;
 	private final String mBluetoothDirectoryPath;
 
 	/**
 	 * Create a new BluetoothObserver for a directory
 	 * 
 	 * @param path The directory to monitor
 	 * @param mask Currently ignored; will only monitor FileObserver.CLOSE_WRITE
 	 * @param handler The handler used to send messages when media files are received
 	 */
 	public BluetoothObserver(String path, int mask, Handler handler) {
 		// path *MUST* end with '/'
		super((path.endsWith("/") ? path : path + "/"), FileObserver.CLOSE_WRITE);
		mBluetoothDirectoryPath = (path.endsWith("/") ? path : path + "/");
 		mHandler = handler;
 	}
 
 	/**
 	 * Equivalent to BluetoothObserver(path, FileObserver.ALL_EVENTS, handler);
 	 */
 	public BluetoothObserver(String path, Handler handler) {
 		this(path, FileObserver.ALL_EVENTS, handler);
 	}
 
 	public static void setBluetoothVisibility(Context context, boolean visible) {
 		// TODO: preserve previous state?
 		BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
 		int currentBluetoothMode = bluetoothAdapter.getScanMode(); // TODO: save initially and restore?
 		if (visible && currentBluetoothMode != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
 			Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
 			discoverableIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 			discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0); // forever
 			context.startActivity(discoverableIntent);
 		} else if (!visible && currentBluetoothMode == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
 			// TODO: turn off
 		}
 	}
 
 	private void sendMessage(int messageId, String storyFilePath) {
 		Message msg = mHandler.obtainMessage(messageId);
 		Bundle bundle = new Bundle();
 		bundle.putString(MediaUtilities.KEY_FILE_NAME, storyFilePath);
 		msg.setData(bundle);
 		mHandler.sendMessage(msg);
 	}
 
 	private String fileIsRequiredForSMIL(String filePath) {
 		for (String smilFile : mSMILContents.keySet()) {
 			if (mSMILContents.get(smilFile).containsKey(filePath)) {
 				return smilFile;
 			}
 		}
 		return null;
 	}
 
 	private boolean checkAndSendSMILContents(String smilParent, Map<String, Boolean> smilContents) {
 
 		// check that the files themselves exist (i.e. haven't been deleted since we first received them)
 		for (String mediaFile : smilContents.keySet()) {
 			if (new File(mediaFile).exists()) {
 				smilContents.put(mediaFile, true);
 			} else {
 				smilContents.put(mediaFile, false);
 			}
 		}
 
 		// check that we have all the required files
 		boolean allContentsComplete = true;
 		for (String mediaFile : smilContents.keySet()) {
 			if (!smilContents.get(mediaFile)) {
 				allContentsComplete = false;
 			}
 		}
 
 		// send the message and reset
 		if (allContentsComplete) {
 			sendMessage(MediaUtilities.MSG_RECEIVED_SMIL_FILE, smilParent);
 			mSMILContents.remove(smilParent);
 			if (MediaTablet.DEBUG)
 				Log.d(DebugUtilities.getLogTag(this), "Sending SMIL");
 			return true;
 		}
 
 		if (MediaTablet.DEBUG)
 			Log.d(DebugUtilities.getLogTag(this), "SMIL not yet complete - waiting");
 		return false;
 	}
 
 	@Override
 	public void onEvent(int event, String path) {
 		// see: http://developer.android.com/reference/android/os/FileObserver.html
 		switch (event) {
 			case CLOSE_WRITE:
 
 				File receivedFile = new File(mBluetoothDirectoryPath, path);
 				if (receivedFile.length() <= 0) { // on some platforms the file is created before permission is granted
 					break;
 				}
 
 				// handle key files - html and smil
 				String fileAbsolutePath = receivedFile.getAbsolutePath();
 				if (IOUtilities.fileExtensionIs(fileAbsolutePath, MediaUtilities.HTML_FILE_EXTENSION)) {
 
 					// html is the simplest way to import - preferred
 					// an html file is also sent for smil - ignore it
 					if (fileIsRequiredForSMIL(fileAbsolutePath) == null) {
 
 						FileReader fileReader;
 						try {
 							fileReader = new FileReader(receivedFile);
 							LineNumberReader lineNumberReader = new LineNumberReader(fileReader);
 
 							// only send if it's an html5 player file
 							String firstLine = lineNumberReader.readLine();
 							if ("<!DOCTYPE html>".equals(firstLine)) { // hack!
 								sendMessage(MediaUtilities.MSG_RECEIVED_HTML_FILE, fileAbsolutePath);
 								if (MediaTablet.DEBUG)
 									Log.d(DebugUtilities.getLogTag(this), "Sending HTML");
 								break;
 							}
 						} catch (FileNotFoundException e) {
 						} catch (IOException e) {
 						}
 					}
 
 				} else if (IOUtilities.fileExtensionIs(fileAbsolutePath, MediaUtilities.SYNC_FILE_EXTENSION)) {
 
 					// don't add the same key twice - could confuse things a lot
 					if (!mSMILContents.containsKey(fileAbsolutePath)) {
 
 						Map<String, Boolean> smilContents = Collections.synchronizedMap(new HashMap<String, Boolean>());
 
 						ArrayList<String> smilUnparsedContents = SMILUtilities
 								.getSimpleSMILFileList(receivedFile, true);
 
 						if (smilUnparsedContents != null) {
 							for (String mediaFile : smilUnparsedContents) {
 								File smilMediaFile = new File(mBluetoothDirectoryPath, mediaFile);
 								String smilMediaPath = smilMediaFile.getAbsolutePath();
 
 								// in case the file has already been received
 								if (mIgnoredFiles.contains(smilMediaPath)) {
 									mIgnoredFiles.remove(smilMediaPath);
 									smilContents.put(smilMediaPath, true);
 									if (MediaTablet.DEBUG)
 										Log.d(DebugUtilities.getLogTag(this),
 												"SMIL component found (previously recorded): " + smilMediaPath);
 								} else if (smilMediaFile.exists()) {
 									smilContents.put(smilMediaPath, true);
 									if (MediaTablet.DEBUG)
 										Log.d(DebugUtilities.getLogTag(this), "SMIL component found (file exists): "
 												+ smilMediaPath);
 								} else {
 									smilContents.put(smilMediaPath, false);
 									if (MediaTablet.DEBUG)
 										Log.d(DebugUtilities.getLogTag(this), "SMIL component not yet sent: "
 												+ smilMediaPath);
 								}
 							}
 
 							mSMILContents.put(fileAbsolutePath, smilContents);
 						} else {
 							// error - couldn't parse the smil file
 							if (MediaTablet.DEBUG)
 								Log.d(DebugUtilities.getLogTag(this), "SMIL parse error: " + fileAbsolutePath);
 						}
 
 						checkAndSendSMILContents(fileAbsolutePath, smilContents);
 						break;
 
 					} else {
 						// error - tried to import the same file twice; ignored
 						if (MediaTablet.DEBUG)
 							Log.d(DebugUtilities.getLogTag(this), "SMIL already sent - ignoring: " + fileAbsolutePath);
 					}
 				}
 
 				// handle any other files
 				String smilParent = fileIsRequiredForSMIL(fileAbsolutePath);
 				if (smilParent != null) {
 					Map<String, Boolean> smilContents = mSMILContents.get(smilParent);
 
 					if (MediaTablet.DEBUG)
 						Log.d(DebugUtilities.getLogTag(this), "SMIL component received: " + fileAbsolutePath);
 
 					// update the list - we now have this file
 					smilContents.remove(fileAbsolutePath);
 					smilContents.put(fileAbsolutePath, true);
 
 					checkAndSendSMILContents(smilParent, smilContents);
 				} else {
 					if (MediaTablet.DEBUG)
 						Log.d(DebugUtilities.getLogTag(this), "Importing non-SMIL component: " + fileAbsolutePath);
 
 					sendMessage(MediaUtilities.MSG_RECEIVED_IMPORT_FILE, fileAbsolutePath);
 				}
 
 				break;
 
 			case ACCESS:
 			case ALL_EVENTS:
 			case ATTRIB:
 			case CLOSE_NOWRITE:
 			case CREATE:
 			case DELETE:
 			case DELETE_SELF:
 			case MODIFY:
 			case MOVED_FROM:
 			case MOVED_TO:
 			case MOVE_SELF:
 			case OPEN:
 			default:
 				break;
 		}
 	}
 
 	@Override
 	public void startWatching() {
 		super.startWatching();
 		if (MediaTablet.DEBUG)
			Log.d(DebugUtilities.getLogTag(this), "Initialising - watching " + mBluetoothDirectoryPath);
 	}
 
 	@Override
 	public void stopWatching() {
 		super.stopWatching();
 		mSMILContents.clear();
 		mIgnoredFiles.clear();
 		if (MediaTablet.DEBUG)
 			Log.d(DebugUtilities.getLogTag(this), "Stopping - no longer watching " + mBluetoothDirectoryPath);
 	}
 }
