 package com.janrain.android.quicksignin;
 
 import android.content.pm.ApplicationInfo;
 import android.text.TextUtils;
 import android.util.Config;
 import android.util.Log;
 import com.janrain.android.engage.types.JRDictionary;
 import com.janrain.android.engage.utils.Archiver;
 import com.janrain.android.engage.utils.ListUtils;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Hashtable;
 
 /**
  * Created by IntelliJ IDEA.
  * User: lillialexis
  * Date: 4/6/11
  * Time: 3:57 PM
  * To change this template use File | Settings | File Templates.
  */
 public class ProfileData {
     private static final String TAG = ProfileData.class.getSimpleName();
 
 	private static ProfileData sInstance;
 
 
     private static final String ARCHIVE_ALL_PROFILES = "allProfiles";
 
     public static ProfileData getInstance() {
 
         if (sInstance != null) {
             if (Config.LOGD) {
                 Log.d(TAG, "[getInstance] returning existing instance.");
                 //todo this should probably be an error, as it ignores the parameters instead of
                 //reinstantiating the library
             }
             return sInstance;
         }
 
         sInstance = new ProfileData();
         if (Config.LOGD) {
             Log.d(TAG, "[getInstance] returning new instance.");
         }
         return sInstance;
     }
 
     // ------------------------------------------------------------------------
     // FIELDS
     // ------------------------------------------------------------------------
 
     private ArrayList<LoginSnapshot> mLoginSnapshots;
     private HashMap<String, JRDictionary> mProfiles;
     private JRDictionary mCurrentProfile;
     // ------------------------------------------------------------------------
     // INITIALIZERS
     // ------------------------------------------------------------------------
 
     // ------------------------------------------------------------------------
     // CONSTRUCTORS
     // ------------------------------------------------------------------------
 
     private ProfileData() {
         if (Config.LOGD) {
             Log.d(TAG, "[ctor] creating instance.");
         }
 
 //            ApplicationInfo ai = JREngage.getContext().getApplicationInfo();
 //            String appName = JREngage.getContext().getPackageManager().getApplicationLabel(ai).toString();
 //            try { mUrlEncodedAppName = URLEncoder.encode(appName, "UTF-8"); }
 //            catch (UnsupportedEncodingException e) { Log.e(TAG, e.toString()); }
 //
 //            String libraryVersion = JREngage.getContext().getString(R.string.jr_engage_version);
 //            String diskVersion = Prefs.getAsString("JREngageVersion", "");
 //
 //            if (diskVersion.equals(libraryVersion)) {
             // Load the list of basic providers
 //            mProfiles = (ArrayList<String>) Archiver.load(ARCHIVE_ALL_PROFILES);
 //            if (Config.LOGD) {
 //                if (ListUtils.isEmpty(mProfiles)) {
 //                    Log.d(TAG, "[ctor] basic providers is empty");
 //                } else {
 //                    Log.d(TAG, "[ctor] basic providers: [" + TextUtils.join(",", mProfiles) + "]");
 //                }
 //            }
 //            }
 //            else {
 //                mProfiles = new ArrayList<String>();
 //            }
 
         mLoginSnapshots = new ArrayList<LoginSnapshot>();
         mProfiles = new HashMap<String, JRDictionary>();
 //        mProfiles.add("Alice");
 //        mProfiles.add("Bob");
 //        mProfiles.add("Carol");
 //        mProfiles.add("Dave");
 //        mProfiles.add("Edith");
     }
 
     // ------------------------------------------------------------------------
     // GETTERS/SETTERS
     // ------------------------------------------------------------------------
 
     public ArrayList<LoginSnapshot> getProfilesList() {
         return mLoginSnapshots;
     }
 
     public void addProfile(JRDictionary auth_info, String provider) {
         String timestamp = "12:00 PM April 14th, 2011";
 
         JRDictionary profile = (auth_info == null) ? null : auth_info.getAsDictionary("profile");
         String identifier = (auth_info == null) ? null : auth_info.getAsString("identifier");
         String displayName = (profile == null) ? null : profile.getAsString("displayName");
 
         LoginSnapshot snapshot = new LoginSnapshot(timestamp, identifier, provider, displayName);
         mLoginSnapshots.add(snapshot);
 
        mProfiles.put(identifier, auth_info);
     }
 
     public void setCurrentProfileByIdentifier(String identifier) {
         mCurrentProfile = mProfiles.get(identifier);
     }
 
     public JRDictionary getCurrentProfile() {
         return mCurrentProfile;
     }
 
    public JRDictionary getProfileForIdentifier(String identifier) {
        return mProfiles.get(identifier);
    }
 }
 
