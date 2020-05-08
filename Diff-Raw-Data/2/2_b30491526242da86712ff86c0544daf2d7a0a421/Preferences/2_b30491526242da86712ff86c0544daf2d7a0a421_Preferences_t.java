 /*******************************************************************************
  * Copyright 2012-2013 Trento RISE
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *        http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either   express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  ******************************************************************************/
 package eu.trentorise.smartcampus.common;
 
 import eu.trentorise.smartcampus.android.common.GlobalConfig;
 import eu.trentorise.smartcampus.protocolcarrier.exceptions.ProtocolException;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.content.pm.PackageManager.NameNotFoundException;
 
 /**
  * Utility class that allows Smart Campus applications to have a common cloud
  * where sharing data.
  * 
  * @author Simone Casagranda
  * 
  */
 public final class Preferences {
 	
 	// App token
 	private static final String APP_TOKEN = "portfolio_android";
 	
 	// Service
	private static final String SERVICE = "/portfolio/rest/";
 	
 	// Shared package path
 	private static final String SHARED_PACKAGE = "eu.trentorise.smartcampus.launcher";
 	
 	// Name for preferences
 	private static final String COMMON_PREF = "COMMON_PREF";
 	
 	// Access mode (private to application and other ones with same Shared UID)
 	private static final int ACCESS = Context.MODE_PRIVATE|Context.CONTEXT_RESTRICTED;
 	
 	private static SharedPreferences getPrefs(Context context) throws NameNotFoundException {
 		Context sharedContext = context.createPackageContext(SHARED_PACKAGE, ACCESS);
 		return sharedContext.getSharedPreferences(COMMON_PREF, ACCESS);
 	}
 	
 	// ======================================================================= //
 	// GETTERS & SETTERS
 	// ======================================================================= //
 
 	/**
 	 * Retrieves app token
 	 */
 	public static String getAppToken() {
 		return APP_TOKEN;
 	}
 	
 	/**
 	 * Retrieves host
 	 * @throws ProtocolException 
 	 */
 	public static String getHost(Context ctx) throws ProtocolException {
 		return GlobalConfig.getAppUrl(ctx);
 	}
 	
 	/**
 	 * Retrieves service
 	 */
 	public static String getService() {
 		return SERVICE;
 	}
 	
 	// ======================================================================= //
 	// OTHERS
 	// ======================================================================= //
 	
 	/**
 	 * Clears all stored preferences
 	 * @throws NameNotFoundException 
 	 */
 	public static void clear(Context context) throws NameNotFoundException{
 		SharedPreferences prefs = getPrefs(context);
 		Editor edit = prefs.edit();
 		edit.clear();
 		edit.commit();
 	}
 	
 }
