 package com.gimranov.zandy.client;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 
 public class ServerCredentials {
 	/** Application key -- available from Zotero */
 	public static final String CONSUMERKEY = "93a5aac13612aed2a236";
 	public static final String CONSUMERSECRET = "196d86bd1298cb78511c";
 	
 	/** This is the zotero:// protocol we intercept
 	 * It probably shouldn't be changed. */
 	public static final String CALLBACKURL = "zotero://";
 	
 	/** This is the Zotero API server. Those who set up independent
 	 * Zotero installations will need to change this. */
 	public static final String APIBASE = "https://api.zotero.org";
 	
 	/** These are the API GET-only methods */
 	public static final String ITEMFIELDS = "/itemFields";
 	public static final String ITEMTYPES = "/itemTypes";
 	public static final String ITEMTYPECREATORTYPES = "/itemTypeCreatorTypes";
 	public static final String CREATORFIELDS = "/creatorFields";
 	public static final String ITEMNEW = "/items/new";
 
 	/* These are the manipulation methods */
 	// /users/1/items GET, POST, PUT, DELETE
 	public static final String ITEMS = "/users/USERID/items";
 	public static final String COLLECTIONS = "/users/USERID/collections";
 	
 	public static final String TAGS = "/tags";
 	public static final String GROUPS = "/groups";	
 	
 	/** And these are the OAuth endpoints we talk to */
 	public static final String OAUTHREQUEST = "https://www.zotero.org/oauth/request";
 	public static final String OAUTHACCESS = "https://www.zotero.org/oauth/access";
 	public static final String OAUTHAUTHORIZE = "https://www.zotero.org/oauth/authorize";
 	
 	public static String prep(Context c, String in) {
		SharedPreferences settings = c.getSharedPreferences("zotero_prefs", 0);
 		String userID = settings.getString("user_id", null);
 		return prep(userID, in);
 	}
 	
 	public static String prep(String id, String in) {
 		return in.replace("USERID", id);
 	}
 	
 	public static boolean check(Context c) {
		SharedPreferences settings = c.getSharedPreferences("zotero_prefs", 0);
 		if (settings.getString("user_id", null) != null
 				&& settings.getString("user_key", null) != null)
 			return true;
 		else return false;
 	}
 }
