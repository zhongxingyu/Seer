 /* 
   Copyright 2010 Duncan Mac-Vicar P. <dmacvicar@suse.de>
  
   This file is part of the openSUSE Build Service Android Client.
 
   openSUSE Build Service Android Client is free software: you
   can  redistribute it and/or modify it under the terms of the GNU
   General Public License as published by the Free Software Foundation,
   either version 3 of the License, or (at your option) any later
   version.
 
   openSUSE Build Service Android Client is distributed in the hope
   that it will be useful, but WITHOUT ANY WARRANTY; without even the
   implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
   See the GNU General Public License for more details.
 
   You should have received a copy of the GNU General Public License
   along with QuiteSleep.  If not, see <http://www.gnu.org/licenses/>.
  
   Gravatar routines Taken from Hubroid - A GitHub app for Android
   
   Copyright (c) 2010 Eddie Ringle <eddie@eringle.net>.
   All rights reserved.
  
   Redistribution and use in source and binary forms, with or
   without modification, are permitted provided that the following
   conditions are met:
    * Redistributions of source code must retain the above
      copyright notice, this list of conditions and the
      following disclaimer.
    * Redistributions in binary form must reproduce the above
      copyright notice, this list of conditions and the
      following disclaimer in the documentation and/or other
      materials provided with the distribution.
    * Neither the name of Idlesoft nor the names of
      its contributors may be used to endorse or promote
      products derived from this software without specific
      prior written permission.
   
   THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
   CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
   INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
   MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
   DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
   CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
   SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
   NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
   LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
   HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
   CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
   OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
   EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. 
 */
 
 package org.opensuse.android.obs;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLEncoder;
 
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.opensuse.android.HttpCoreRestClient;
 import org.opensuse.android.obs.data.Collection;
 import org.opensuse.android.obs.data.Distribution;
 import org.opensuse.android.obs.data.DistributionList;
 import org.opensuse.android.obs.data.Project;
 import org.opensuse.android.obs.data.Package;
 import org.opensuse.android.obs.data.Request;
 import org.opensuse.android.obs.data.User;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Bitmap.CompressFormat;
 import android.os.Environment;
 import android.preference.PreferenceManager;
 import android.text.TextUtils;
 import android.util.Log;
 
 public class Client extends HttpCoreRestClient {
 	
 	public static final String PREFERENCES = "preferences";
 	
 	private static final String DEFAULT_API_HOST = "api.opensuse.org";
 	private static final String DEFAULT_USERNAME = "";
 	private static final String DEFAULT_PASSWORD = "";
 	
 	private static final String OBSCLIENT = "OBSCLIENT";
 	
 	/* 
 	 * Constructor from context, which allows the client to access the preferences of
 	 * the application
 	 */
 	public Client(Context context) {
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
 		// then you use
 		String username = prefs.getString("username", DEFAULT_USERNAME);
 		String password = prefs.getString("password", DEFAULT_PASSWORD);
 		setProtocol("https://");
 		setUsername(username);
 		setPassword(password);
 		setHost(DEFAULT_API_HOST);
 	}
 	
 	public List<Project> getProjects() {
 		return getProjectsMatching("");
 	}
 	
 	public List<Project> getProjectIds() {
 		return getProjectIdsMatching("");
 	}
 	
 	public List<Project> getProjectIdsMatching(String match) {
 		String path = "search/project_id" + "?match=" + encodeString(match);
 		return get(path, Collection.class).getProjects();
 	}
 	
 	public List<Project> getProjectsMatching(String match) {
 		String path = "search/project" + "?match=" + encodeString(match);
 		return get(path, Collection.class).getProjects();
 	}
 	
 	public List<Package> getPackagesMatching(String match) {
 		String path = "search/package" + "?match=" + encodeString(match);
 		return get(path, Collection.class).getPackages();
 	}
 	
 	public List<Package> getPackageIdsMatching(String match) {
 		String path = "search/package_id" + "?match=" + encodeString(match);
 		return get(path, Collection.class).getPackages();
 	}
 	
 	public List<Distribution> getDistributions() {
 		return get("distributions", DistributionList.class).getDistributions();
 	}
 	
 	public Request getRequest(String id) {
 		return get("request/" + id, Request.class);
 	}
 		
 	public String getDiff(String sproject, String spackage, String tproject, String tpackage, String rev) {
 		Log.i(OBSCLIENT, String.format("source: %s %s", sproject, spackage));
 		Log.i(OBSCLIENT, String.format("target: %s %s", tproject, tpackage));
 		String path = String.format("source/%s/%s?oproject=%s&opackage=%s&cmd=diff&expand=1",
 				encodeString(sproject),
 				encodeString(spackage),
 				encodeString(tproject),
 				encodeString(tpackage));
 		if (rev != null) {
 			path += String.format("&rev=%s", rev);
 		}
 		return postPlain(path, "");
 	}
 	
 	public List<Request> getRequestsMatching(String match) {
 		String path = "search/request";
 		path = path + "?match=" + encodeString(match);		
 		return get(path, Collection.class).getRequests();
 	}
 	
 	public List<Request> getRequests() {
 		return getRequestsMatching("");
 	}
 	
 	public List<Request> getMyRequests() {
 		List<String> xpaths = new ArrayList<String>();
 		List<String> projects = new ArrayList<String>();
 		String query = "(state/@name='new') and (";
 		List<Project> projectIds = getProjectIdsMatching("person/@userid = '" + getUsername() + "' and person/@role = 'maintainer'");
 		for (Project project: projectIds) {
 			projects.add(project.getName());
 			xpaths.add("action/target/@project='" + project.getName() + "'");
 		}
 		
 		List<Package> packages = getPackageIdsMatching("person/@userid = '" + getUsername() + "' and person/@role = 'maintainer'");
 		for (Package pkg: packages) {
 			// if the project is already in the list, no need to add this clause
 			// replace this with a real xPath join
 			if (projects.contains(pkg.getProject()))
 				continue;
 			xpaths.add("(action/target/@project='" + pkg.getProject() + "' and " + "action/target/@package='" + pkg.getName() + "')");
 		}
 
 		query += TextUtils.join(" or ", xpaths);
 		query += ")";
 		return getRequestsMatching(query);
 	}
 	
 	public User getUser(String userid) {
 		return get("person/" + userid, User.class);
 	}
 	
 	/* Encode or return the same if not possible */
 	private String encodeString(String str) {
 		try {
 			return URLEncoder.encode(str, "UTF-8");
 		} catch (UnsupportedEncodingException e) {
 			Log.w("URI", "Can't encode: " + str);
 			return str;
 		}
 	}
 	
 	/**
 	 * Returns a Gravatar ID associated with the provided name
 	 * 
 	 * @param name
 	 * @return	the gravatar ID associated with the name
 	 */
 	public String getGravatarID(String name) {
 		String id = "";
 		try {
 			// Get SD card directory and check to see if it is writable
 			File root = Environment.getExternalStorageDirectory();
 			if (root.canWrite()) {
 				// Create the "obsclientdir" sub-directory if it doesn't already exist
 				File obsclientdir = new File(root, "opensuse-obs-client-android");
 				if (!obsclientdir.exists() && !obsclientdir.isDirectory()) {
 					obsclientdir.mkdir();
 				}
 				// Create the "gravatars" sub-directory if it doesn't already exist
 				File gravatars = new File(obsclientdir, "gravatars");
 				if (!gravatars.exists() && !gravatars.isDirectory()) {
 					gravatars.mkdir();
 				}
 				// Create the image file on the disk
 				File image = new File(gravatars, name + ".id");
 				if (image.exists() && image.isFile()) {
 					FileReader fr = new FileReader(image);
 					BufferedReader in = new BufferedReader(fr);
 					id = in.readLine();
 					in.close();
 				} else {
 					try {
 						id = getUser(name).getEmail();
 						FileWriter fw = new FileWriter(image);
 						BufferedWriter bw = new BufferedWriter(fw);
 						bw.write(id);
 						bw.flush();
 						bw.close();
 					} catch (NullPointerException e) {
 						// do nothing, we don't like null pointers
 					}
 				}
 			}
 		} catch (FileNotFoundException e) {
 			Log.e("debug", "Error saving bitmap", e);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		return id;
 	}
 	
 	/**
 	 * Returns a Bitmap of the Gravatar associated with the provided ID.
 	 * This image will be scaled according to the provided size.
 	 * 
 	 * @param id
 	 * @param size
 	 * @return	a scaled Bitmap
 	 */
 	static Bitmap getGravatar(String id, int size) {
 		// Check to see if a gravatar of the correct size already exists
 		Bitmap bm = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory()
 				+ "/opensuse-obs-client-android/gravatars/"
 				+ id + ".png");
 		// If not, fetch one
 		if (bm == null) {
 			try {
 				URL aURL = new URL(
 				"http://www.gravatar.com/avatar.php?gravatar_id="
						+ URLEncoder.encode(id) + "&size=50&d="
 						// Get the default gravatar from build service if ID doesn't exist
 						+ URLEncoder.encode("http://static.opensuse.org/hosts/build2.o.o/images/local/default_face.png"));
 				URLConnection conn = aURL.openConnection();
 				Log.i("HTTPCLIENT", "fetching gravatar for " + id + " from " + conn.getURL());
 				conn.connect();
 				InputStream is = conn.getInputStream();
 				BufferedInputStream bis = new BufferedInputStream(is);
 				bm = BitmapFactory.decodeStream(bis);
 				bis.close();
 				is.close();
 			} catch (IOException e) {
 				Log.e("debug", "Error getting bitmap", e);
 			}
 			// Save the gravatar onto the SD card for later retrieval
 			try {
 				File root = Environment.getExternalStorageDirectory();
 				if (root.canWrite()) {
 					File obsclientdir = new File(root, "opensuse-obs-client-android");
 					if (!obsclientdir.exists() && !obsclientdir.isDirectory()) {
 						obsclientdir.mkdir();
 					}
 					File gravatars = new File(obsclientdir, "gravatars");
 					if (!gravatars.exists() && !gravatars.isDirectory()) {
 						gravatars.mkdir();
 					}
 					// Add .nomedia so the Gallery doesn't pick up our gravatars
 					File nomedia = new File(gravatars, ".nomedia");
 					if (!nomedia.exists()) {
 						nomedia.createNewFile();
 					}
 					File image = new File(gravatars, id + ".png");
 					bm.compress(CompressFormat.PNG, 100, new FileOutputStream(image));
 				}
 			} catch (FileNotFoundException e) {
 				Log.e("debug", "Error saving bitmap", e);
 			} catch (IOException e) {
 				e.printStackTrace();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 		// Scale the image to the desired size
 		bm = Bitmap.createScaledBitmap(bm, size, size, true);
 		return bm;
 	}
 }
