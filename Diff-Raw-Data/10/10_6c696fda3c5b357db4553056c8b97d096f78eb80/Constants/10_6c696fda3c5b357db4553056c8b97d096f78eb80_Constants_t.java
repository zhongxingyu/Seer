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
 package eu.trentorise.smartcampus.ac;
 
 import android.content.Context;
 import android.content.pm.ApplicationInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.PackageManager.NameNotFoundException;
 
 /**
  * Constants and their read methods for security APIs
  * @author raman
  *
  */
 public class Constants {
 
 	/** AAC service url */
 	public static final String KEY_AUTH_URL = "eu.trentorise.smartcampus.account.AUTH_URL";
 	
 	/** Account type */
 	public static final String KEY_ACCOUNT_TYPE = "eu.trentorise.smartcampus.account.ACCOUNT_TYPE";
 
 	/** Account label */
 	public static final String KEY_ACCOUNT_LABEL = "eu.trentorise.smartcampus.account.ACCOUNT_LABEL";
 
 	/** Account label */
 	public static final String KEY_ACCOUNT_NAME = "eu.trentorise.smartcampus.account.ACCOUNT_NAME";
 
 	/** Account label */
 	private static final String KEY_ACCOUNT_BASED_ACCESS = "eu.trentorise.smartcampus.account.ACCOUNT_BASED_ACCESS";
 	
 	/** Account label */
 	private static final String KEY_CLIENT_ID = "eu.trentorise.smartcampus.account.CLIENT_ID";
 	/** Account label */
 	private static final String KEY_CLIENT_SECRET = "eu.trentorise.smartcampus.account.CLIENT_SECRET";
 
 	/**
      * App authority key
      */
 	public static final String KEY_AUTHORITY = "eu.trentorise.smartcampus.account.AUTHORITY";
     /**
      * App scope key
      */
 	public static final String KEY_SCOPE = "eu.trentorise.smartcampus.account.SCOPE";
     /**
      * App redirect URI key: the uri to which the authorization will redirect
      */
 	public static final String KEY_REDIRECT_URI = "eu.trentorise.smartcampus.account.REDIRECT_URI";
 	/**
 	 * Failure result code
 	 */
 	public static final int RESULT_FAILURE = 2;
 	
 	private static final String DEF_AUTH_BASE_URL = "https://vas-dev.smartcampuslab.it/aac";
 
 	public static final String KEY_EXPIRES_IN = "eu.trentorise.smartcampus.account.EXPIRES_IN";
 
 	public static final String KEY_REFRESH_TOKEN = "eu.trentorise.smartcampus.account.REFRESH_TOKEN";
 
 
 	/**
 	 * Read the authentication base URL from application metadata
 	 * @param context
 	 * @return
 	 * @throws NameNotFoundException
 	 */
 	public static String getAuthUrl(Context context) throws NameNotFoundException {
		String URL = null;
 		ApplicationInfo info = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
 		if (info != null && info.metaData != null && info.metaData.containsKey(KEY_AUTH_URL)) {
			URL = info.metaData.getString(KEY_AUTH_URL);
 		}
		else URL=DEF_AUTH_BASE_URL;
		if (!URL.endsWith("/"))
			URL+="/";
		return URL;
 	}
 
 	/**
 	 * Read the account type from application metadata
 	 * @param context
 	 * @return
 	 * @throws NameNotFoundException
 	 */
 	public static String getAccountType(Context context) throws NameNotFoundException {
 		ApplicationInfo info = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
 		if (info != null && info.metaData != null && info.metaData.containsKey(KEY_ACCOUNT_TYPE)) {
 			return info.metaData.getString(KEY_ACCOUNT_TYPE);
 		}
 		throw new NameNotFoundException("Account type should be specified in application metadata");
 	}
 
 	/**
 	 * Read the account label from application metadata
 	 * @param context
 	 * @return
 	 * @throws NameNotFoundException
 	 */
 	public static String getAccountLabel(Context context) throws NameNotFoundException {
 		ApplicationInfo info = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
 		if (info != null && info.metaData != null && info.metaData.containsKey(KEY_ACCOUNT_LABEL)) {
 			return info.metaData.getString(KEY_ACCOUNT_LABEL);
 		}
 		throw new NameNotFoundException("Account label should be specified in application metadata");
 	}
 	
 	/**
 	 * Read the account name from application metadata
 	 * @param context
 	 * @return
 	 * @throws NameNotFoundException
 	 */
 	public static String getAccountName(Context ctx) throws NameNotFoundException {
 		ApplicationInfo info = ctx.getPackageManager().getApplicationInfo(ctx.getPackageName(), PackageManager.GET_META_DATA);
 		if (info != null && info.metaData != null && info.metaData.containsKey(KEY_ACCOUNT_NAME)) 
 			return info.metaData.getString(KEY_ACCOUNT_NAME);
 		throw new NameNotFoundException("Account name should be specified in application metadata");
 	}
 
 	/**
 	 * Read the client ID from application metadata
 	 * @param context
 	 * @return
 	 * @throws NameNotFoundException
 	 */
 	public static String getClientId(Context ctx) {
 		try {
 			ApplicationInfo info = ctx.getPackageManager().getApplicationInfo(ctx.getPackageName(), PackageManager.GET_META_DATA);
 			if (info != null && info.metaData != null && info.metaData.containsKey(KEY_CLIENT_ID)) 
 				return info.metaData.getString(KEY_CLIENT_ID);
 		} catch (NameNotFoundException e) {
 		}
 		return null;
 	}
 	/**
 	 * Read the client secret from application metadata
 	 * @param context
 	 * @return
 	 * @throws NameNotFoundException
 	 */
 	public static String getClientSecret(Context ctx) {
 		try {
 			ApplicationInfo info = ctx.getPackageManager().getApplicationInfo(ctx.getPackageName(), PackageManager.GET_META_DATA);
 			if (info != null && info.metaData != null && info.metaData.containsKey(KEY_CLIENT_SECRET)) 
 				return info.metaData.getString(KEY_CLIENT_SECRET);
 		} catch (NameNotFoundException e) {
 		}
 		return null;
 	}
 
 	
 	/**
 	 * Read the account token type 
 	 * @param context
 	 * @return
 	 * @throws NameNotFoundException
 	 */
 	public static String getAccountTokenType(Context ctx) {
 		return "DEFAULT";
 	}
 
 	/**
 	 * Whether account-based 
 	 * @param context
 	 * @return
 	 * @throws NameNotFoundException
 	 */
 	public static boolean isAccountBasedAccess(Context ctx) {
 		try {
 			ApplicationInfo info = ctx.getPackageManager().getApplicationInfo(ctx.getPackageName(), PackageManager.GET_META_DATA);
 			if (info != null && info.metaData != null && info.metaData.containsKey(KEY_ACCOUNT_BASED_ACCESS)) 
 				return info.metaData.getBoolean(KEY_ACCOUNT_BASED_ACCESS);
 		} catch (NameNotFoundException e) {
 		}
 		return false;
 	}
 
 }
