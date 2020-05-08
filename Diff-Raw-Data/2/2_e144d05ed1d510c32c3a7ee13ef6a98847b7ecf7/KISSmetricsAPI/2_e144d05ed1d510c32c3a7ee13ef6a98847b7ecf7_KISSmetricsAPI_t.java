 /*
  * 
  * Copyright 2012 Steve Chan, http://80steve.com
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *     http://www.apache.org/licenses/LICENSE-2.0
  *     
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * 
  */
 package com.eightysteve.KISSmetrics;
 
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.telephony.TelephonyManager;
 import android.util.Log;
 
 public class KISSmetricsAPI implements KISSmetricsURLConnectionCallbackInterface {
 	public static final String BASE_URL = "//trk.KISSmetrics.com";
 	public static final String EVENT_PATH = "/e";
 	public static final String PROP_PATH = "/s";
 	public static final String ALIAS_PATH = "/a";
 	public static final int RETRY_INTERVAL = 5;
 	public static final String ACTION_FILE = "KISSmetricsAction";
 	public static final String IDENTITY_PREF = "KISSmetricsIdentityPreferences";
 	public static final String PROPS_PREF = "KISSmetricsPropsPreferences";
 	public static final String HTTP = "http";
 	public static final String HTTPS = "https";
 
 	private String _apiKey;
 	private String _identity;
 	private List<String> _sendQueue;
 	private Context _context;
 	private HashMap<String, String> propsToSend;
 	private String currentScheme;
 
 
 	private static KISSmetricsAPI sharedAPI = null;
 
 	private KISSmetricsAPI(String apiKey, Context context, boolean secure) {
 		this._apiKey = apiKey;
 		this._context = context;
 		if (this._context == null) return;
 		this.currentScheme = (secure)?HTTPS:HTTP;
 		SharedPreferences pref = this._context.getSharedPreferences(IDENTITY_PREF, Activity.MODE_PRIVATE);
 		SharedPreferences.Editor prefEditor = null;
 		this._identity = pref.getString("identity", null);
 		if (this._identity == null) {
 			TelephonyManager tm = (TelephonyManager) this._context.getSystemService(Context.TELEPHONY_SERVICE);
 			this._identity = tm.getDeviceId();
 			prefEditor = pref.edit();
 			prefEditor.putString("identity", this._identity);
 			prefEditor.commit();
 		}
 
 		boolean shouldSendProps = true;
 		pref = this._context.getSharedPreferences(PROPS_PREF, Activity.MODE_PRIVATE);
 		propsToSend = new HashMap<String, String>();
 		for (String s : pref.getAll().keySet()) {
 			propsToSend.put(s, pref.getString(s, null));
 		}
 		if (!propsToSend.isEmpty()) {
 			shouldSendProps = false;
			if (!android.os.Build.VERSION.RELEASE.equals(propsToSend.get("systemVersion"))) {
 				shouldSendProps = true;
 			}
 		}
 
 		if (shouldSendProps) {
 			propsToSend.clear();
 			propsToSend.put("systemName", "android");
 			propsToSend.put("systemVersion", android.os.Build.VERSION.RELEASE);
 			prefEditor = pref.edit();
 			for (String s : propsToSend.keySet()) {
 				prefEditor.putString(s, propsToSend.get(s));
 			}
 			prefEditor.commit();
 		} else {
 			propsToSend = null;
 		}
 
 		this.unarchiveData();
 		this.setProperties(propsToSend);
 	}
 
 	public static synchronized KISSmetricsAPI sharedAPI(String apiKey, Context context) {
 		if (sharedAPI == null) {
 			sharedAPI = new KISSmetricsAPI(apiKey, context, true);
 		}
 		return sharedAPI;
 	}
 	
 	public static synchronized KISSmetricsAPI sharedAPI(String apiKey, Context context, boolean secure) {
 		if (sharedAPI == null) {
 			sharedAPI = new KISSmetricsAPI(apiKey, context, secure);
 		}
 		return sharedAPI;
 	}
 
 	public static synchronized KISSmetricsAPI sharedAPI() {
 		if (sharedAPI == null) {
 			Log.e("KISSmetricsAPI", "KISSmetricsAPI has not been initialized, please call the method new KISSmetricsAPI(<API_KEY>)");
 		}
 		return sharedAPI;
 	}
 
 	public void send() {
 		synchronized (this) {
 			if (this._sendQueue.size() == 0)
 				return;
 
 			String nextAPICall = this._sendQueue.get(0);
 			KISSmetricsURLConnection connector = KISSmetricsURLConnection.initializeConnector(this);
 			connector.connectURL(nextAPICall);
 		}
 	}
 
 	public void recordEvent(String name, HashMap<String, String> properties) {
 		if (name == null || name.length() == 0) {
 			Log.w("KISSmetricsAPI", "Name cannot be null");
 			return;
 		}
 
 		long timeOfEvent = System.currentTimeMillis()/1000;
 		String query = String.format("_k=%s&_p=%s&_d=0&_t=%d&_n=%s", this._apiKey, this._identity, timeOfEvent, name);
 
 		if (properties != null) {
 			String additionalURL = "";
 			for (int i = 0; i < properties.keySet().size(); i++){
 				String key = (String) properties.keySet().toArray()[i];
 				additionalURL += key + "=" + properties.get(key);
 				if(i < properties.keySet().size() - 1)
 					additionalURL += "&";
 			}
 			if (additionalURL != "" && additionalURL.length() > 0) {
 				query += "&" + additionalURL;
 			}
 		}
 		
 		String theURL = null;
 		try {
 			theURL = new URI(this.currentScheme, null, BASE_URL + EVENT_PATH, query, null).toASCIIString();
 		} catch (URISyntaxException e) {
 			Log.w("KISSmetricsAPI", "KISSmetricsAPI failed to record event");
 			return;
 		}
 
 		synchronized (this) {
 			addUrlToQueue(theURL);
 			this.archiveData();
 		}
 		this.send();
 	}
 
 	public void alias(String firstIdentity, String secondIdentity) {
 		if (firstIdentity == null || firstIdentity.length() == 0 || secondIdentity == null || secondIdentity.length() == 0) {
 			Log.w("KISSmetricsAPI", String.format("Attempted to use nil or empty identities in alias (%s and %s). Ignoring.", firstIdentity, secondIdentity));
 		}
 		
 		String query = String.format("_k=%s&_p=%s&_n=%s", this._apiKey, firstIdentity, secondIdentity);
 		String theURL = null;
 		try {
 			theURL = new URI(this.currentScheme, null, BASE_URL + ALIAS_PATH, query, null).toASCIIString();
 		} catch (URISyntaxException e) {
 			Log.w("KISSmetricsAPI", "KISSmetricsAPI failed to alias");
 			return;
 		}
 		
 		synchronized (this) {
 			addUrlToQueue(theURL);
 			this.archiveData();
 		}
 		this.send();
 	}
 
 	public void identify(String identity) {
 		if (identity == null || identity.length() == 0) {
 			Log.w("KISSmetricsAPI", "Attempted to use nil or empty identity. Ignoring.");
 			return;
 		}
 		
 		String query = String.format("_k=%s&_p=%s&_n=%s", this._apiKey, this._identity, identity);
 		String theURL = null;
 		try {
 			theURL = new URI(this.currentScheme, null, BASE_URL + ALIAS_PATH, query, null).toASCIIString();
 		} catch (URISyntaxException e) {
 			Log.w("KISSmetricsAPI", "KISSmetricsAPI failed to identify");
 			return;
 		}
 		
 		synchronized (this) {
 			this._identity = identity;
 
 			SharedPreferences pref = this._context.getSharedPreferences(IDENTITY_PREF, Activity.MODE_PRIVATE);
 			SharedPreferences.Editor prefEditor = pref.edit();
 			prefEditor.putString("identity", this._identity);
 			prefEditor.commit();
 
 			addUrlToQueue(theURL);
 			this.archiveData();
 		}
 		this.send();
 	}
 
 	public void setProperties(HashMap<String, String> properties) {
 		if (properties == null || properties.size() == 0) {
 			Log.w("KISSmetricsAPI", "Tried to set properties with no properties in it..");
 			return;
 		}
 
 		String additionalURL = "";
 		for (int i = 0; i < properties.keySet().size(); i++){
 			String key = (String) properties.keySet().toArray()[i];
 			additionalURL += key + "=" + properties.get(key).toString();
 			if(i < properties.keySet().size() - 1)
 				additionalURL += "&";
 		}
 		if (additionalURL.length() == 0) {
 			Log.w("KISSmetricsAPI", "No valid properties in setProperties:. Ignoring call");
 			return;
 		}
 
 		long timeOfEvent = System.currentTimeMillis()/1000;
 		
 		String query = String.format("_k=%s&_p=%s&_d=0&_t=%d", this._apiKey, this._identity, timeOfEvent);
 		query += "&" + additionalURL;
 		String theURL = null;
 		try {
 			theURL = new URI(this.currentScheme, null, BASE_URL + PROP_PATH, query, null).toASCIIString();
 		} catch (URISyntaxException e) {
 			Log.w("KISSmetricsAPI", "Failed to set properties");
 			return;
 		}
 
 		synchronized (this) {
 			addUrlToQueue(theURL);
 			this.archiveData();
 		}
 		this.send();
 	}
 
 	private void addUrlToQueue(String url) {
 	    if (null != url) {
 	        this._sendQueue.add(url);
 	    }
 	}
 
 	public void archiveData() {
 		try {
 			FileOutputStream fos = this._context.openFileOutput(ACTION_FILE, Context.MODE_PRIVATE);
 			ObjectOutputStream oos = new ObjectOutputStream(fos);
             oos.writeObject(this._sendQueue);
             oos.close();
 		} catch (Exception e) {
 			Log.w("KISSmetricsAPI", "Unable to archive data");
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	public void unarchiveData() {
 		try {
 			FileInputStream fis = this._context.openFileInput(ACTION_FILE);
 			ObjectInputStream ois = new ObjectInputStream(fis);
 			this._sendQueue = (List<String>) ois.readObject();
 			ois.close();
 			fis.close();
 		} catch (Exception e) {
 			Log.w("KISSmetricsAPI", "Unable to unarchive data");
 		}
 
 		if (this._sendQueue == null)
 			this._sendQueue = new ArrayList<String>();
 		else {
 		    this._sendQueue.removeAll(Collections.singleton(null));
             for (String url : this._sendQueue) {
                 url.replace("&_d=0", "&_d=1");
             }
 			this.send();
         }
 	}
 
 	public void finished(int statusCode) {
 		this.send();
 	}
 	
 	public void setSecure(boolean secure) {
 		this.currentScheme = (secure)?HTTPS:HTTP;
 	}
 
 	public List<String> getSendQueue() {
 		return _sendQueue;
 	}
 
 	public Object clone() throws CloneNotSupportedException {
 		throw new CloneNotSupportedException();
 	}
 }
