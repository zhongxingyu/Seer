 package jp.senchan.android.wasatter.model.api;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 public class WassrUser {
 
	//XXX WasssrはTwitterのnameがsceent_naemになってる矛盾仕様
 	public static final String KEY_NAME = "screen_name";
 	public static final String KEY_PROFILE_IMAGE = "profile_image_url";
 	public static final String KEY_PROTECTED = "protected";
 	
 	public String name;
 	public String screenName;
 	public String profileImageUrl;
 	public boolean isProtected;
 	
 	public WassrUser(JSONObject object, String loginId) throws JSONException {
 		screenName = loginId;
 		name = object.getString(KEY_NAME);
 		profileImageUrl = object.getString(KEY_PROFILE_IMAGE);
 		isProtected = object.getBoolean(KEY_PROTECTED);
 	}
 }
