 /*
  * MUSTARD: Android's Client for StatusNet
  * 
  * Copyright (C) 2009-2010 macno.org, Michele Azzolari
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
  * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
  * for more details.
  *
  * You should have received a copy of the GNU General Public License along
  * with this program; if not, write to the Free Software Foundation, Inc.,
  * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  * 
  */
 package org.mumod.android.core;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.mumod.android.MustardApplication;
 import org.mumod.android.MustardDbAdapter;
 import org.mumod.android.R;
 import org.mumod.android.provider.OAuthInstance;
 import org.mumod.android.provider.OAuthLoader;
 import org.mumod.util.HttpManager;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.util.Log;
 
 public class OAuthKeyFetcher {
 
 	private SharedPreferences mSharedPreferences;
     private static final String TAG = "mumod";
     
 	public int execute(Context context,MustardDbAdapter dbHelper, URL url) throws Exception {
 		String ownInstance = mSharedPreferences.getString("ownInstance", "");
 		String ownConsumerSecret = mSharedPreferences.getString("ownConsumerSecret", "");
 		String ownConsumerKey = mSharedPreferences.getString("ownConsumerKey", "");
 		
 		boolean reserved = false;
 		try {
 			if (url == null) {
 				url = new URL(context.getString(R.string.mustardoauthkeyurl)+"?ver="+MustardApplication.sVersionName);
 				reserved = true;
 			}
 		} catch(MalformedURLException e) {
 			return 0;
 		}
 		String host = url.getHost();
 		HttpManager mHttpManager = new HttpManager(context,host);
 		ArrayList<OAuthInstance> oauths = new ArrayList<OAuthInstance>();
 
 		try {
			JSONObject o = mHttpManager.getJsonObject(url.toExternalForm());
			JSONArray keys = o.getJSONArray("keys");

 			if( !ownInstance.equals("") && !ownConsumerSecret.equals("") && !ownConsumerKey.equals("") ) {
 				OAuthInstance oi= new OAuthInstance();
 				oi.instance = ownInstance;
 				oi.key = ownConsumerKey;
 				oi.secret = ownConsumerSecret;
 				oauths.add(oi);
 			}
 			else {
 				Log.v(TAG, "No oauth key defined in config");
 			}

 			if (keys != null) {
 				for (int i=0;i<keys.length();i++) {
 					JSONObject enclosure = keys.getJSONObject(i);
 					OAuthInstance oi= new OAuthInstance();
 					oi.instance = enclosure.getString("instance");
 					oi.key = enclosure.getString("key");
 					oi.secret = enclosure.getString("secret");
 					oauths.add(oi);
 				}
 			}
			else {
				OAuthInstance oi= new OAuthInstance();
				oi.instance = "";
				oi.key = "";
				oi.secret = "";
				oauths.add(oi);
			}
 		} 
 		catch(JSONException e) {} 
 		catch(Exception e) {}
 		Log.v(TAG, "OAuths: " + oauths.size());
 		
 		if (oauths.size() == 0) {
 			return 0;
 		}
 		OAuthLoader om = new OAuthLoader(dbHelper);
 		return om.load(oauths,reserved);
     }
 }
