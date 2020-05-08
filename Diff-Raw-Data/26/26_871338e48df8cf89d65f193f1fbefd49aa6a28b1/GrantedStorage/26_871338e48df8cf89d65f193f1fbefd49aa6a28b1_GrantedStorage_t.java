 /*
  * Copyright 2013 Simon Brakhane
  *
  * This file is part of AnJaRoot.
  *
  * AnJaRoot is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License as published by the Free Software
  * Foundation, either version 3 of the License, or (at your option) any later
  * version.
  *
  * AnJaRoot is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along with
  * AnJaRoot. If not, see http://www.gnu.org/licenses/.
  */
 
 package org.failedprojects.anjaroot;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Context;
 import android.os.FileObserver;
 import android.util.Log;
 
 public class GrantedStorage {
 	private static final String LOGTAG = "AnJaRootStorage";
 	private final File dir;
 	private final String filename = "granted";
 	private final StorageObserver observer;
 	private final List<String> packages;
 	private OnChangeHandler handler;
 
 	public interface OnChangeHandler {
 		void onReload(boolean success);
 	}
 
 	private class StorageObserver extends FileObserver {
 		private final static int modes = CLOSE_WRITE | MOVED_FROM | MOVED_TO
 				| DELETE;
 
 		public StorageObserver() {
 			super(dir.toString(), modes);
 		}
 
 		@Override
 		public void onEvent(int event, String path) {
 			if (!filename.equals(path)) {
 				Log.v(LOGTAG,
 						String.format("Ignoring event %X on %s", event, path));
 				return;
 			}
 
 			switch (event) {
 			case CLOSE_WRITE:
 			case MOVED_FROM:
 			case MOVED_TO:
 			case DELETE:
 				Log.v(LOGTAG, String.format(
 						"Received event %d, reloading list", event));
 				readPackageList();
 				break;
 			default:
 				Log.e(LOGTAG, String.format("Unknown event %X received"));
 			}
 		}
 	}
 
 	public GrantedStorage(Context context) {
 		dir = context.getFilesDir();
 
 		observer = new StorageObserver();
 		packages = new ArrayList<String>();
 
 		readPackageList();
 	}
 
 	public void startObserving(OnChangeHandler handler) {
 		this.handler = handler;
 		observer.startWatching();
 		Log.v(LOGTAG, String.format("Started observing %s", dir.toString()));
 	}
 
 	public void stopObserving() {
 		this.handler = null;
 		observer.stopWatching();
 		Log.v(LOGTAG, String.format("Stoped observing %s", dir.toString()));
 	}
 
	public void forceReload() {
		readPackageList();
	}

 	private boolean readPackageList() {
 		boolean ret = true;
 		packages.clear();
 
 		try {
 			FileInputStream inputstream = new FileInputStream(new File(dir,
 					filename));
 			InputStreamReader inputreader = new InputStreamReader(inputstream);
 			BufferedReader bufreader = new BufferedReader(inputreader);
 
 			String line;
 			do {
 				line = bufreader.readLine();
 				if (line == null) {
 					break;
 				}
 				packages.add(line);
 			} while (true);
 
 			bufreader.close();
 			inputreader.close();
 			inputstream.close();
 		} catch (IOException e) {
 			packages.clear();
 			Log.e(LOGTAG, "Failed to read storage", e);
 			ret = false;
 		}
 
 		if (handler != null) {
 			handler.onReload(ret);
 		}
 		return ret;
 	}
 
 	public List<String> getPackages() {
 		// We create a copy because we clean our list from time to time
 		return new ArrayList<String>(packages);
 	}
 
 	private boolean write(List<String> granted) {
 		boolean ret = true;
 		try {
 			File tmp = File.createTempFile("granted", "tmp", dir);
 			FileWriter writer = new FileWriter(tmp);
 			BufferedWriter bufwriter = new BufferedWriter(writer);
 
 			for (String pkg : granted) {
 				bufwriter.write(pkg);
 				bufwriter.newLine();
 			}
 
 			bufwriter.flush();
 			bufwriter.close();
 			writer.close();
 
 			tmp.renameTo(new File(dir, filename));
 			tmp.setReadOnly();
 		} catch (IOException e) {
 			Log.e(LOGTAG, "Failed to write storage", e);
 			ret = false;
 		}
 
 		if (handler != null) {
 			handler.onReload(ret);
 		}
 
 		return ret;
 	}
 
 	public void addPackage(String name) {
 		List<String> granted = getPackages();
 		if (!granted.contains(name)) {
 			granted.add(name);
 			write(granted);
 		}
 	}
 
 	public boolean removePackage(String name) {
 		List<String> granted = getPackages();
 		if (granted.contains(name)) {
 			granted.remove(name);
 			write(granted);
 			return true;
 		}
 
 		return false;
 	}
 
 	public boolean isGranted(String pkgname) {
 		List<String> granted = getPackages();
 		return granted.contains(pkgname);
 	}
 }
