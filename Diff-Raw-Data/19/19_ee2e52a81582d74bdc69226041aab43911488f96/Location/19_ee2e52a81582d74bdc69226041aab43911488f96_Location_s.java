 package com.envsocial.android.api;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.Context;
 import android.database.sqlite.SQLiteException;
 import android.util.Log;
 
 import com.envsocial.android.api.exceptions.EnvSocialContentException;
 import com.envsocial.android.features.Feature;
 
 public class Location implements Serializable {
 	
 	private static final long serialVersionUID = -1700484963112613638L;
 	
 	public static final String ENVIRONMENT = "environment";
 	public static final String AREA = "area";
 	
 	private static final int TYPE_ENVIRONMENT = 0;
 	private static final int TYPE_AREA = 1;
 	
 	private String mJSONString;
 	
 	private String mId;
 	private String mResourceUri;
 	private int mType;
 	private String mName;
 	private Map<String, Feature> mFeatures;
 	private List<String> mTags;
 	
 	private String mParentUri;
 	private String mParentName;
 	
 	private String mOwnerFirstName;
 	private String mOwnerLastName;
 	private String mOwnerEmail;
 	private String mOwnerUri;
 	
 	/** Environment specific fields */
 	private String mLatitude;
 	private String mLongitude;
 	private String mLayoutUrl;
 	
 	/** Area specific fields */
 	private String mAreaType;
 	private int mLevel;
 	private String mAdminFirstName;
 	private String mAdminLastName;
 	private String mAdminEmail;
 	private String mAdminUri;
 	
 	/** Location context specific fields */
 	private LocationContextManager mContextManager;
 	
 	
 	private static Location mFullInstance = null;
 	private static String prevLocationString = null;
 	
 	public Location(String name, String uri) {
 		mName = name;
 		mResourceUri = uri;
 	}
 	
 	
 	//public Location(JSONObject data) throws JSONException {
 	private Location (String jsonString) throws JSONException {
 		//mJSONString = data.toString();
 		mJSONString = jsonString;
 		JSONObject data = new JSONObject(jsonString);
 
 		// Get location type
 		String type = data.getString("location_type");
 		if (type.compareTo(ENVIRONMENT) == 0) {
 			mType = TYPE_ENVIRONMENT;
 		} else if (type.compareTo(AREA) == 0) {
 			mType = TYPE_AREA;
 		}
 		
 		// Get location data
 		JSONObject locationData = data.getJSONObject("location_data");
 		
 		mId = locationData.getString("id");
 		mResourceUri = locationData.getString("resource_uri");
 		mName = locationData.getString("name");
 		
 		JSONObject owner = locationData.getJSONObject("owner");
 		mOwnerUri = owner.getString("resource_uri");
 		mOwnerFirstName = owner.getString("first_name");
 		mOwnerLastName = owner.getString("last_name");
 		mOwnerEmail = owner.optString("email", null);
 		
 		// Get features
 		mFeatures = new HashMap<String, Feature>();
 		JSONArray array = locationData.getJSONArray("features");
 		int len = array.length();
 		for (int i = 0; i < len; ++ i) {
 			JSONObject item = array.getJSONObject(i);
 			String category = item.getString("category");
 			int version = item.optInt("version", 1);
 			String environmentUri = item.optString("environment", null);
 			String areaUri = item.optString("area", null);
 			String resourceUri = item.optString("resource_uri", null);
 			String featureData = item.optString("data", null);
 			
 			
 			try {
 				Feature feat = Feature.getInstance(category, version, resourceUri, environmentUri, areaUri, featureData);
 				mFeatures.put(category, feat);
 			} catch (IllegalArgumentException ex) {
 				Log.d("Location", ex.getMessage());
 			} catch (EnvSocialContentException ex) {
 				Log.d("Location", "Failed to add feature of category :: " + category.toUpperCase() + ". Reason: " + ex.getMessage());
 			} catch (SQLiteException ex) {
 				Log.d("Location", "Failed to add feature of category ::" + category.toUpperCase() + ". Feature local DB error: " + ex.getMessage());
 			}
 		}
 		
 		// Get tags
 		mTags = new ArrayList<String>();
 		array = locationData.getJSONArray("tags");
 		len = array.length();
 		for (int i = 0; i < len; ++ i) {
 			mTags.add(array.getString(i));
 		}
 		
 		// Get parent data
 		JSONObject parentData = locationData.optJSONObject("parent");
 		if (parentData != null) {
 			mParentUri = parentData.optString("uri");
 			mParentName = parentData.optString("name");
 		}
 		
 		// Grab environment/area specific fields
 		if (isEnvironment()) {
 			mLatitude = locationData.getString("latitude");
 			mLongitude = locationData.getString("longitude");
 			mLayoutUrl = locationData.optString("layout_url", null);
 		} 
 		else if (isArea()) {
 			mAreaType = locationData.getString("areaType");
 			mLevel = locationData.getInt("level");
 			
			JSONObject admin = locationData.getJSONObject("admin");
			mAdminUri = admin.getString("resource_uri");
			mAdminFirstName = admin.getString("first_name");
			mAdminLastName = admin.getString("last_name");
			mAdminEmail = admin.optString("email", null);
 		}
 		
 		// lastly initialize the context manager
 		mContextManager = new LocationContextManager(this);
 	}
 	
 	
 	public static Location fromSerialized(String jsonString) throws JSONException {
 		if (mFullInstance == null) {
 			prevLocationString = jsonString;
 			mFullInstance = new Location(jsonString);
 		}
 		else {
 			// compare the hashcodes, it is much faster than big string comparison
 			if (prevLocationString.hashCode() != jsonString.hashCode()) {
 				prevLocationString = jsonString;
 				mFullInstance = new Location(jsonString);
 			}
 		}
 		
 		return mFullInstance;
 	}
 	
 	public void initFeatures() throws EnvSocialContentException {
 		for (String category : mFeatures.keySet()) {
 			Feature feat = mFeatures.get(category);
 			if (feat != null ) {
 				feat.init();
 			}
 		}
 	}
 	
 	
 	public void doCleanup(Context context) {
 		// call cleanup on all the features of this location doing things like
 		// closing all handlers to local support databases
 		for (String category : mFeatures.keySet()) {
 			Feature feat = mFeatures.get(category);
 			if (feat != null ) {
 				feat.doCleanup(context);
 			}
 		}
 	}
 	
 	
 	public void doClose(Context context) {
 		/* called only on checkout when we want to remove all data associated with this location
 		 TODO:	in the future we may want to CACHE the data for FAVORITE locations and implement
 		 		stronger consistency checks between server and client application
 		*/
 		
 		for (String category : mFeatures.keySet()) {
 			Feature feat = mFeatures.get(category);
 			if (feat != null ) {
 				feat.doClose(context);
 			}
 		}
 	}
 	
 	
 	
 	public String getId() {
 		return mId;
 	}
 	
 	public String getLocationUri() {
 		return mResourceUri;
 	}
 	
 	public int getType() {
 		return mType;
 	}
 	
 	public String getName() {
 		return mName;
 	}
 	
 	public String getOwnerName() {
 		return mOwnerFirstName + " " + mOwnerLastName;
 	}
 	
 	public String getOwnerEmail() {
 		return mOwnerEmail;
 	}
 	
 	public String getOwnerUri() {
 		return mOwnerUri;
 	}
 	
 	public boolean isOwnerByEmail(String email) {
 		if (mOwnerEmail == null) {
 			return false;
 		}
 		return mOwnerEmail.compareToIgnoreCase(email) == 0;
 	}
 	
 	public Feature getFeature(String category) {
 		return mFeatures.get(category);
 	}
 	
 	public boolean hasFeature(String category) {
 		return mFeatures.get(category) != null;
 	}
 	
 	public Map<String, Feature> getFeatures() {
 		return mFeatures;
 	}
 	
 	public List<String> getTags() {
 		return mTags;
 	}
 	
 	public String getParentUri() {
 		return mParentUri;
 	}
 	
 	public String getParentName() {
 		return mParentName;
 	}
 	
 	public String getLayoutUrl() {
 		return mLayoutUrl;
 	}
 	
 	public boolean isEnvironment() {
 		return mType == TYPE_ENVIRONMENT;
 	}
 	
 	public boolean isArea() {
 		return mType == TYPE_AREA;
 	}
 	
 	
 	/** Environment specific methods */
 	
 	public String getLatitude() {
 		return mLatitude;
 	}
 	
 	public String getLongitude() {
 		return mLongitude;
 	}
 	
 	
 	/** Area specific methods */
 	
 	public String getAdminName() {
 		return mAdminFirstName + " " + mAdminLastName;
 	}
 	
 	public String getAdminEmail() {
 		return mAdminEmail;
 	}
 	
 	public String getAdminUri() {
 		return mAdminUri;
 	}
 	
 	public String getAreaType() {
 		return mAreaType;
 	}
 	
 	public int getLevel() {
 		return mLevel;
 	}
 	
 	public String serialize() {
 		return mJSONString;
 	}
 	
 	@Override
 	public String toString() {
 		String info = "";
 		info += "name::" + mName + ", ";
 		info += "uri::" + mResourceUri + ", ";
 		info += "jsonString::" + mJSONString;
 		
 		return info;
 	}
 	
 	/** Location context specific methods */
 	public LocationContextManager getContextManager() {
 		return mContextManager;
 	}
 }
