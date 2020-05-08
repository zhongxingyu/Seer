 /*
  * Copyright 2013 ToureNPlaner
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 
 package de.uni.stuttgart.informatik.ToureNPlaner.Net.Handler;
 
 import android.util.Log;
 import de.uni.stuttgart.informatik.ToureNPlaner.ClientSideCompute.ClientGraph;
 import de.uni.stuttgart.informatik.ToureNPlaner.ClientSideCompute.NullGraph;
 import de.uni.stuttgart.informatik.ToureNPlaner.ClientSideCompute.SimpleGraph;
 import de.uni.stuttgart.informatik.ToureNPlaner.Net.JacksonManager;
 import de.uni.stuttgart.informatik.ToureNPlaner.ToureNPlanerApplication;
 import org.apache.commons.io.input.TeeInputStream;
 
 import java.io.*;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Date;
 
 /**
  * @author Niklas Schnelle
  */
 public class SyncCoreLoader {
 
 	public int coreLevel = 40;
 	private static final String corePrefix = "core";
 	private static final String coreSuffix = ".json";
 	private static final String pathPrefix = "/cores/";
 	private String coreURL = null;
 	private ClientGraph core = null;
 
 	public SyncCoreLoader() {
 	}
 
 
 	public void setURL(String url) {
 		coreURL = url;
 	}
 
 	public void setLevel(int level) {
 		coreLevel = level;
 	}
 
 	private InputStream readCoreFileFromNetAndCache(String cacheDir) throws IOException {
 		HttpURLConnection con = null;
 		try {
 			URL url = new URL(coreURL + pathPrefix + corePrefix + coreLevel + coreSuffix);
 			con = (HttpURLConnection) url.openConnection();
 			File cacheDirFile = ToureNPlanerApplication.getContext().getExternalCacheDir();
 			Log.d("TP", "Trying to download core to " + cacheDirFile.getAbsolutePath() + corePrefix + coreLevel + coreSuffix);
 			FileOutputStream coreFileStream = new FileOutputStream(new File(cacheDirFile, corePrefix + coreLevel + coreSuffix));
 			Log.d("TP", "Content-Length: " + con.getContentLength());
 			InputStream in = new BufferedInputStream(con.getInputStream());
 			TeeInputStream teeStream = new TeeInputStream(in, coreFileStream, true);
 			return teeStream;
 
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 			if (con != null) {
 				con.disconnect();
 			}
 			return null;
 		}
 	}
 
 	private long getLastModifiedOnServer() throws IOException {
 		HttpURLConnection con = null;
 		long result = new Date().getTime();
 		try {
			URL url = new URL(coreURL + "/" + corePrefix + coreLevel + coreSuffix);
 			con = (HttpURLConnection) url.openConnection();
 			con.setRequestMethod("HEAD");
 			result = con.getHeaderFieldDate("Last-Modified", result);
			Log.d("TP", "Last modified is " + new Date(result));
 
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 			return result;
 		} finally {
 			if (con != null) {
 				con.disconnect();
 			}
 		}
 		return result;
 	}
 
 	private InputStream readCoreFileCached(File coreFile, String cacheDir) throws IOException {
 		if (getLastModifiedOnServer() > coreFile.lastModified() - 1000 * 60 * 10) {
 			return readCoreFileFromNetAndCache(cacheDir);
 		}
 		return new FileInputStream(coreFile);
 	}
 
 	public synchronized SimpleGraph getCoreGraph() throws IOException {
 		if (core == null) {
 			assert coreURL != null : "Core URL was not set before trying to getCoreGraph";
 			File cacheDirFile = ToureNPlanerApplication.getContext().getExternalCacheDir();
 			String cacheDir = cacheDirFile.getAbsolutePath();
 			File coreFile = new File(cacheDirFile, corePrefix + coreLevel + coreSuffix);
 			InputStream coreFileStream = null;
 			if (!coreFile.exists()) {
 				Log.d("TP", "Core does not exist");
 				coreFileStream = readCoreFileFromNetAndCache(cacheDir);
 			} else {
 				Log.d("TP", "Core exists");
 				coreFileStream = readCoreFileCached(coreFile, cacheDir);
 			}
 			try {
 				core = ClientGraph.readClientGraph(new NullGraph(), JacksonManager.ContentType.JSON, coreFileStream);
 			} catch (Exception ex) {
 				// We might have messed up our cached Core, delete it
 				// so we don't stumble upon it
 				if (coreFile.exists()) {
 					Log.e("TP", "Deleting core because of Exception");
 					coreFile.delete();
 				}
 				core = null;
 			}
 		} else {
 			Log.d("TP", "Core is loaded");
 		}
 
 		return core;
 	}
 }
