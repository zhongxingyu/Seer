 package com.osastudio.newshub.data;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.text.TextUtils;
 
 import com.osastudio.newshub.data.user.UserStatus;
 
 public class AppProperties extends NewsBaseObject implements UserStatus {
 
    public static final String JSON_KEY_APK_URL = "android_url";
    public static final String JSON_KEY_MIN_VERSION_CODE = "min_version_code";
    public static final String JSON_KEY_SPLASH_IMAGE_URL = "picture_url";
    public static final String JSON_KEY_RELEASE_NOTES = "update_note";
    public static final String JSON_KEY_USER_IDS = "student_ids";
    public static final String JSON_KEY_USER_STATUS = "user_status";
    public static final String JSON_KEY_VERSION_CODE = "version_code";
    public static final String JSON_KEY_VERSION_NAME = "version_id";
 
    private String apkUrl;
    private int minVersionCode;
    private String releaseNotes;
    private String splashImageUrl;
    private List<String> userIds;
    private int userStatus;
    private int versionCode;
    private String versionName;
 
    public AppProperties() {
 
    }
 
    public AppProperties(JSONObject jsonObject) {
       super(jsonObject);
 
       if (isSuccess()) {
          try {
             if (!jsonObject.isNull(JSON_KEY_APK_URL)) {
                setApkUrl(jsonObject.getString(JSON_KEY_APK_URL).trim());
             }
             if (!jsonObject.isNull(JSON_KEY_MIN_VERSION_CODE)) {
                setMinVersionCode(jsonObject.getInt(JSON_KEY_MIN_VERSION_CODE));
             }
             if (!jsonObject.isNull(JSON_KEY_SPLASH_IMAGE_URL)) {
                setSplashImageUrl(jsonObject
                      .getString(JSON_KEY_SPLASH_IMAGE_URL).trim());
             }
             if (!jsonObject.isNull(JSON_KEY_RELEASE_NOTES)) {
                setReleaseNotes(jsonObject.getString(JSON_KEY_RELEASE_NOTES)
                      .trim());
             }
             if (!jsonObject.isNull(JSON_KEY_USER_IDS)) {
                String idsString = jsonObject.getString(JSON_KEY_USER_IDS);
                if (!TextUtils.isEmpty(idsString)) {
                   String[] ids = idsString.split(",");
                   if (ids != null && ids.length > 0) {
                      ArrayList<String> list = new ArrayList<String>();
                      Collections.addAll(list, ids);
                     setUserIds(userIds);
                   }
                }
             }
             if (!jsonObject.isNull(JSON_KEY_USER_STATUS)) {
                setUserStatus(jsonObject.getInt(JSON_KEY_USER_STATUS));
             }
             if (!jsonObject.isNull(JSON_KEY_VERSION_CODE)) {
                setVersionCode(jsonObject.getInt(JSON_KEY_VERSION_CODE));
             }
             if (!jsonObject.isNull(JSON_KEY_VERSION_NAME)) {
                setVersionName(jsonObject.getString(JSON_KEY_VERSION_NAME)
                      .trim());
             }
          } catch (JSONException e) {
 
          }
       }
    }
 
    public String getApkUrl() {
       return this.apkUrl;
    }
 
    public AppProperties setApkUrl(String apkUrl) {
       this.apkUrl = apkUrl;
       return this;
    }
 
    public int getMinVersionCode() {
       return minVersionCode;
    }
 
    public AppProperties setMinVersionCode(int minVersionCode) {
       this.minVersionCode = minVersionCode;
       return this;
    }
 
    public String getReleaseNotes() {
       return this.releaseNotes;
    }
 
    public AppProperties setReleaseNotes(String releaseNotes) {
       this.releaseNotes = releaseNotes;
       return this;
    }
 
    public String getSplashImageUrl() {
       return this.splashImageUrl;
    }
 
    public AppProperties setSplashImageUrl(String imageUrl) {
       this.splashImageUrl = imageUrl;
       return this;
    }
 
    public List<String> getUserIds() {
       return userIds;
    }
 
    public AppProperties setUserIds(List<String> userIds) {
       this.userIds = userIds;
       return this;
    }
 
    public int getUserStatus() {
       return this.userStatus;
    }
 
    public AppProperties setUserStatus(int userStatus) {
       this.userStatus = userStatus;
       return this;
    }
    
    public int getVersionCode() {
       return versionCode;
    }
 
    public AppProperties setVersionCode(int versionCode) {
       this.versionCode = versionCode;
       return this;
    }
 
    public String getVersionName() {
       return this.versionName;
    }
 
    public AppProperties setVersionName(String versionName) {
       this.versionName = versionName;
       return this;
    }
 
 }
