 /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * 
  Copyright (c) 2010, Janrain, Inc.
  
  All rights reserved.
  
  Redistribution and use in source and binary forms, with or without modification,
  are permitted provided that the following conditions are met:
  
  * Redistributions of source code must retain the above copyright notice, this
  list of conditions and the following disclaimer. 
  * Redistributions in binary form must reproduce the above copyright notice, 
  this list of conditions and the following disclaimer in the documentation and/or
  other materials provided with the distribution. 
  * Neither the name of the Janrain, Inc. nor the names of its
  contributors may be used to endorse or promote products derived from this
  software without specific prior written permission.
  
  
  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. 
 */
 package com.janrain.android.engage.session;
 
 import android.content.pm.ApplicationInfo;
 import android.text.TextUtils;
 import android.util.Config;
 import android.util.Log;
 import android.webkit.CookieManager;
 import android.webkit.CookieSyncManager;
 import com.janrain.android.engage.JREngage;
 import com.janrain.android.engage.JREngageError;
 import com.janrain.android.engage.JREngageError.AuthenticationError;
 import com.janrain.android.engage.JREngageError.ConfigurationError;
 import com.janrain.android.engage.JREngageError.ErrorType;
 import com.janrain.android.engage.JREngageError.SocialPublishingError;
 import com.janrain.android.engage.JREnvironment;
 import com.janrain.android.engage.R;
 import com.janrain.android.engage.net.JRConnectionManager;
 import com.janrain.android.engage.net.JRConnectionManagerDelegate;
 import com.janrain.android.engage.net.async.HttpResponseHeaders;
 import com.janrain.android.engage.prefs.Prefs;
 import com.janrain.android.engage.types.JRActivityObject;
 import com.janrain.android.engage.types.JRDictionary;
 import com.janrain.android.engage.utils.Archiver;
 import com.janrain.android.engage.utils.ListUtils;
 import com.janrain.android.engage.utils.StringUtils;
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.util.EncodingUtils;
 
 import java.io.IOException;
 import java.io.InvalidClassException;
 import java.io.UnsupportedEncodingException;
 import java.net.*;
 import java.util.ArrayList;
 import java.util.List;
 
 public class JRSessionData implements JRConnectionManagerDelegate {
 
     // ------------------------------------------------------------------------
     // TYPES
     // ------------------------------------------------------------------------
 
     // ------------------------------------------------------------------------
     // STATIC FIELDS
     // ------------------------------------------------------------------------
 
     private static final String TAG = JRSessionData.class.getSimpleName();
     
 	private static JRSessionData sInstance;
 
     private static final JREnvironment ENVIRONMENT = JREnvironment.PRODUCTION;
 //    private static final JREnvironment ENVIRONMENT = JREnvironment.STAGING;
 //    private static final JREnvironment ENVIRONMENT = JREnvironment.LOCAL;
 
     private static final String ARCHIVE_ALL_PROVIDERS = "allProviders";
     private static final String ARCHIVE_BASIC_PROVIDERS = "basicProviders";
     private static final String ARCHIVE_SOCIAL_PROVIDERS = "socialProviders";
     private static final String ARCHIVE_AUTH_USERS_BY_PROVIDER = "jrAuthenticatedUsersByProvider";
 
     private static final String FMT_CONFIG_URL =
             "%s/openid/mobile_config_and_baseurl?appId=%s&device=android&app_name=%s";
 
     private boolean mGetConfigDone = false;
     private String mOldEtag;
     private String mUrlEncodedAppName;
 
     // ------------------------------------------------------------------------
     // STATIC INITIALIZERS
     // ------------------------------------------------------------------------
 
     // ------------------------------------------------------------------------
     // STATIC METHODS
     // ------------------------------------------------------------------------
 
 	public static JRSessionData getInstance() {
 //        if (sInstance == null) {
 //            String appId = Prefs.getAsString("JR~appId", "");
 //            String tokenUrl = Prefs.getAsString("JR~tokenUrl", "");
 //            sInstance = new JRSessionData(appId, tokenUrl, null);
 //        }
 
         return sInstance;
 	}
 	
 	public static JRSessionData getInstance(String appId,
                                             String tokenUrl,
                                             JRSessionDelegate delegate) {
         if (sInstance != null) Log.e(TAG, "reinitializing JRSessionData");
 
 		sInstance = new JRSessionData(appId, tokenUrl, delegate);
         if (Config.LOGD) {
             Log.d(TAG, "[getInstance] returning new instance.");
         }
 		return sInstance;
 	}
 	
     // ------------------------------------------------------------------------
     // FIELDS
     // ------------------------------------------------------------------------
 
 	private ArrayList<JRSessionDelegate> mDelegates;
 	
 	private JRProvider mCurrentlyAuthenticatingProvider;
     private JRProvider mCurrentlyPublishingProvider;
 
     private String mReturningBasicProvider;
 	private String mReturningSocialProvider;
 	
 	private JRDictionary mAllProviders;
 	private ArrayList<String> mBasicProviders;
 	private ArrayList<String> mSocialProviders;
 	private JRDictionary mAuthenticatedUsersByProvider;
 	
 	private String mSavedConfigurationBlock = "";
 	private String mNewEtag;
     private String mGitCommit;
 
 	private JRActivityObject mActivity;
 	
 	private String mTokenUrl;
 	private String mBaseUrl;
 	private String mAppId;
     private String mDevice;
 
     private boolean mHidePoweredBy;
 
 	private boolean mAlwaysForceReauth;
 
     //private boolean mForceReauth;
 
 	private boolean mSocialSharing;
 
     private boolean mDialogIsShowing = false;
 
     private JREngageError mError;
 	
     // ------------------------------------------------------------------------
     // INITIALIZERS
     // ------------------------------------------------------------------------
 
     // ------------------------------------------------------------------------
     // CONSTRUCTORS
     // ------------------------------------------------------------------------
 
 	private JRSessionData(String appId, String tokenUrl, JRSessionDelegate delegate) {
         if (Config.LOGD) {
             Log.d(TAG, "[ctor] creating instance.");
         }
 
 		mDelegates = new ArrayList<JRSessionDelegate>();
 		mDelegates.add(delegate);
 		
 		mAppId = appId;
 		mTokenUrl = tokenUrl;
 
         ApplicationInfo ai = JREngage.getContext().getApplicationInfo();
         String appName = JREngage.getContext().getPackageManager().getApplicationLabel(ai)
                 .toString();
         try { mUrlEncodedAppName = URLEncoder.encode(appName, "UTF-8"); }
         catch (UnsupportedEncodingException e) { Log.e(TAG, e.toString()); }
 
         String libraryVersion = JREngage.getContext().getString(R.string.jr_engage_version);
         String diskVersion = Prefs.getAsString("JREngageVersion", "");
         if (diskVersion.equals(libraryVersion)) {
             // Load dictionary of authenticated users.  If the dictionary is not found, the
             // archiver will return a new (empty) list.
             mAuthenticatedUsersByProvider = JRDictionary.unarchive(ARCHIVE_AUTH_USERS_BY_PROVIDER);
 
             // Load the list of all providers
             mAllProviders = JRDictionary.unarchive(ARCHIVE_ALL_PROVIDERS);
 
             for (Object provider : mAllProviders.values()) {
                 ((JRProvider)provider).loadDynamicVariables();
             }
 
             // Load the list of basic providers
             mBasicProviders = (ArrayList<String>)Archiver.load(ARCHIVE_BASIC_PROVIDERS);
             if (Config.LOGD) {
                 if (ListUtils.isEmpty(mBasicProviders)) {
                     Log.d(TAG, "[ctor] basic providers is empty");
                 } else {
                     Log.d(TAG, "[ctor] basic providers: [" + TextUtils.join(",", mBasicProviders)
                             + "]");
                 }
             }
 
             // Load the list of social providers
             mSocialProviders = (ArrayList<String>)Archiver.load(ARCHIVE_SOCIAL_PROVIDERS);
             if (Config.LOGD) {
                 if (ListUtils.isEmpty(mSocialProviders)) {
                     Log.d(TAG, "[ctor] social providers is empty");
                 } else {
                     Log.d(TAG, "[ctor] social providers: [" + TextUtils.join(",", mSocialProviders)
                             + "]");
                 }
             }
 
             // Load the base url
             mBaseUrl = Prefs.getAsString(Prefs.KEY_JR_BASE_URL, "");
 
             // Figure out of we're suppose to hide the powered by line
             mHidePoweredBy = Prefs.getAsBoolean(Prefs.KEY_JR_HIDE_POWERED_BY, false);
 
             // And load the last used basic and social providers
             mReturningSocialProvider = Prefs.getAsString(Prefs.KEY_JR_LAST_USED_SOCIAL_PROVIDER,
                     "");
             mReturningBasicProvider = Prefs.getAsString(Prefs.KEY_JR_LAST_USED_BASIC_PROVIDER, "");
 
             /* If the configuration for this rp has changed, the etag will have changed, and we need
              * to update our current configuration information. */
             mOldEtag = Prefs.getAsString(Prefs.KEY_JR_CONFIGURATION_ETAG, "");
         }
         else {
             mAuthenticatedUsersByProvider = new JRDictionary();
             mAllProviders = new JRDictionary();
             mBasicProviders = new ArrayList<String>();
             mSocialProviders = new ArrayList<String>();
             mBaseUrl = "";
             mHidePoweredBy = false;
             mReturningSocialProvider = "";
             mReturningBasicProvider = "";
             mOldEtag = "";
         }
 
         mError = startGetConfiguration();
 	}
 	
     // ------------------------------------------------------------------------
     // GETTERS/SETTERS
     // ------------------------------------------------------------------------
 
     public JREngageError getError() {
         return mError;
     }
 
 //    public void setError(JREngageError mError) {
 //        this.mError = mError;
 //    }
 
     public JRActivityObject getJRActivity() {
         return mActivity;
     }
 
     public void setJRActivity(JRActivityObject activity) {
         mActivity = activity;
     }
 
     public boolean getAlwaysForceReauth() {
         return mAlwaysForceReauth;
     }
 
     public void setAlwaysForceReauth(boolean force) {
         mAlwaysForceReauth = force;
     }
 
     public String getTokenUrl() {
         return mTokenUrl;
     }
 
     public void setTokenUrl(String tokenUrl) {
         mTokenUrl = tokenUrl;
     }
 
     public JRProvider getCurrentlyAuthenticatingProvider() {
         return mCurrentlyAuthenticatingProvider;
     }
 
     public void setCurrentlyAuthenticatingProvider(JRProvider provider) {
         Log.d(TAG, "setCurrentlyAuthenticatingProvider: " +
                 (provider != null ? provider.getName() : null));
         
         mCurrentlyAuthenticatingProvider = provider;
     }
 
     public void setCurrentlyAuthenticatingProvider(String providerName) {
         Object provider = mAllProviders.get(providerName);
 
         setCurrentlyAuthenticatingProvider((JRProvider) provider);
     }
 
     public ArrayList<JRProvider> getBasicProviders() {
         ArrayList<JRProvider> providerList = new ArrayList<JRProvider>();
         if ((mBasicProviders != null) && (mBasicProviders.size() > 0)) {
             for (String name : mBasicProviders) {
                 JRProvider provider = mAllProviders.getAsProvider(name);
                 providerList.add(provider);
             }
         }
         return providerList;
     }
 
     public ArrayList<JRProvider> getSocialProviders() {
         ArrayList<JRProvider> providerList = new ArrayList<JRProvider>();
 
         if ((mSocialProviders != null) && (mSocialProviders.size() > 0)) {
             for (String name : mSocialProviders)
                 providerList.add(mAllProviders.getAsProvider(name));
         }
 
         return providerList;
     }
 
     public String getReturningBasicProvider() {
         //if (mAlwaysForceReauth)
         //    return null;
 
         return mReturningBasicProvider;
     }
 
     public void setReturningBasicProvider(String mReturningBasicProvider) {
         this.mReturningBasicProvider = mReturningBasicProvider;
     }
 
     public String getReturningSocialProvider() {
         return mReturningSocialProvider;
     }
 
     public void setReturningSocialProvider(String mReturningSocialProvider) {
         this.mReturningSocialProvider = mReturningSocialProvider;
     }
 
 
     public String getBaseUrl() {
         return mBaseUrl;
     }
 
     public boolean getSocial() {
         return mSocialSharing;
     }
 
     public void setSocial(boolean value) {
         mSocialSharing = value;
     }
 
     public boolean getHidePoweredBy() {
         return mHidePoweredBy;
     }
 
     public void setDialogIsShowing(boolean mDialogIsShowing) {
         this.mDialogIsShowing = mDialogIsShowing;
         if (!mDialogIsShowing & !mSavedConfigurationBlock.equals("")) {
             String s = mSavedConfigurationBlock;
             mSavedConfigurationBlock = "";
             mError = finishGetConfiguration(s);
         }
     }
 
     // ------------------------------------------------------------------------
     // DELEGATE METHODS
     // ------------------------------------------------------------------------
 
     public void connectionDidFail(Exception ex, String requestUrl, Object userdata) {
         Log.i(TAG, "[connectionDidFail]");
 
         if (userdata == null) {
             Log.e(TAG, "[connectionDidFail] unexpected null userdata");
         } else if (userdata instanceof String) {
             String s = (String)userdata;
             if (s.equals("getConfiguration")) {
                 Log.e(TAG, "connectionDidFail for getConfiguration");
                 mError = new JREngageError(
                         "There was a problem communicating with the Janrain server while configuring authentication.",
                         ConfigurationError.CONFIGURATION_INFORMATION_ERROR,
                         JREngageError.ErrorType.CONFIGURATION_FAILED);
                 mGetConfigDone = true;
                 triggerMobileConfigDidFinish();
             } else if (s.equals("emailSuccess")) {
                 // TODO: Implement notifications for email/sms sharing
             } else if (s.equals("smsSuccess")) {
                 // TODO: Implement notifications for email/sms sharing
             } else {
 
             }
         } else if (userdata instanceof JRDictionary) {
             JRDictionary dictionary = (JRDictionary) userdata;
             // TODO: Should "tokenUrl" be a key, and if so, to what?  In iPhone lib, it's a value to key "action"
             //todo
             if (dictionary.containsKey("tokenUrl")) {
                 List<JRSessionDelegate> delegatesCopy = getDelegatesCopy();
                 for (JRSessionDelegate delegate : delegatesCopy) {
                     JREngageError error = new JREngageError(
                         "Session error", JREngageError.CODE_UNKNOWN, "", ex
                     );
                     delegate.authenticationCallToTokenUrlDidFail(
                             dictionary.getAsString("tokenUrl"),
                             error,
                             dictionary.getAsString("providerName"));
                 }
             } else if (dictionary.getAsString("action").equals("shareActivity")) {
                 List<JRSessionDelegate> delegatesCopy = getDelegatesCopy();
                 JREngageError error = new JREngageError("Session error", JREngageError.CODE_UNKNOWN, "", ex);
                 for (JRSessionDelegate delegate : delegatesCopy) {
                     delegate.publishingJRActivityDidFail(
                             (JRActivityObject) dictionary.get("activity"),
                             error,
                             dictionary.getAsString("providerName"));
                 }
             }
         }
 	}
 
     public void connectionDidFinishLoading(String payload, String requestUrl, Object userdata) {
         Log.i(TAG, "[connectionDidFinishLoading]");
 
         if (Config.LOGD) {
             Log.d(TAG, "[connectionDidFinishLoading] payload: " + payload);
         }
 
         if (userdata == null) {
             Log.e(TAG, "[connectionDidFinishLoading] unexpected null userdata");
         } else if (userdata instanceof String) {
             String tag = (String) userdata;
 
             if (tag.equals("emailSuccess")) {
                 //todo
             } else if (tag.equals("smsSuccess")) {
                 //todo
             } else {
                 //todo
             }
         } else if (userdata instanceof JRDictionary) {
             JRDictionary dictionary = (JRDictionary) userdata;
 
             if (dictionary.getAsString("action").equals("shareActivity"))
                 processShareActivityResponse(payload, dictionary);
             //else todo
         }
 	}
 
     private void processShareActivityResponse(String payload, JRDictionary userDataTag) {
         String providerName = userDataTag.getAsString("providerName");
 
         JRDictionary responseDict = JRDictionary.fromJSON(payload);
 
         if (responseDict == null) {
             setCurrentlyPublishingProvider(null);
             for (JRSessionDelegate delegate : getDelegatesCopy()) {
                 delegate.publishingJRActivityDidFail(
                         (JRActivityObject) userDataTag.get("activity"),
                         new JREngageError(payload,
                                 SocialPublishingError.FAILED,
                                 ErrorType.PUBLISH_FAILED),
                         providerName);
             }
 
         } else if (responseDict.containsKey("stat") && ("ok".equals(responseDict.get("stat")))) {
             saveLastUsedSocialProvider();
             setCurrentlyPublishingProvider(null);
             for (JRSessionDelegate delegate : getDelegatesCopy()) {
                 delegate.publishingJRActivityDidSucceed(
                         mActivity,
                         providerName);
             }
 
         } else {
             setCurrentlyPublishingProvider(null);
             JRDictionary errorDict = responseDict.getAsDictionary("err");
             JREngageError publishError;
 
             if (errorDict == null) {
                 publishError = new JREngageError(
                         "There was a problem publishing with this activity",
                         SocialPublishingError.FAILED,
                         ErrorType.PUBLISH_FAILED);
             } else {
                 int code = (errorDict.containsKey("code"))
                         ? errorDict.getAsInt("code") : 1000;
 
                 String msg = errorDict.getAsString("msg", "");
 
                 switch (code) {
                     case 0: /* "Missing parameter: apiKey" */
                         publishError = new JREngageError(
                                 msg,
                                 SocialPublishingError.MISSING_API_KEY,
                                 ErrorType.PUBLISH_NEEDS_REAUTHENTICATION);
                         break;
                     case 4: /* "Facebook Error: Invalid OAuth 2.0 Access Token" */
                         if (msg.matches(".*nvalid ..uth.*"))
                             publishError = new JREngageError(
                                     msg,
                                     SocialPublishingError.INVALID_OAUTH_TOKEN,
                                     ErrorType.PUBLISH_NEEDS_REAUTHENTICATION);
                         else if (msg.matches(".*eed action request limit.*"))
                             publishError = new JREngageError(
                                     msg,
                                     SocialPublishingError.FEED_ACTION_REQUEST_LIMIT,
                                     ErrorType.PUBLISH_FAILED);
                         else
                             publishError = new JREngageError(
                                     msg,
                                     SocialPublishingError.FAILED,
                                     ErrorType.PUBLISH_FAILED);
                         break;
                     case 100: // TODO LinkedIn character limit error
                         publishError = new JREngageError(
                                 msg,
                                 SocialPublishingError.LINKEDIN_CHARACTER_EXCEEDED,
                                 ErrorType.PUBLISH_INVALID_ACTIVITY);
                         break;
                     case 6:
                         if (msg.matches(".*witter.*")) publishError = new JREngageError(
                                 msg,
                                 SocialPublishingError.DUPLICATE_TWITTER,
                                 ErrorType.PUBLISH_INVALID_ACTIVITY
                         ); else publishError = new JREngageError(
                                 msg,
                                 JREngageError.SocialPublishingError.FAILED,
                                 ErrorType.PUBLISH_INVALID_ACTIVITY
                         );
                         break;
                     case 1000: /* Extracting code failed; Fall through. */
                     default: // TODO Other errors (find them)
                         publishError = new JREngageError(
                                 "There was a problem publishing this activity",
                                 SocialPublishingError.FAILED,
                                 ErrorType.PUBLISH_FAILED);
                         break;
                 }
             }
 
             for (JRSessionDelegate delegate : getDelegatesCopy()) {
                 delegate.publishingJRActivityDidFail((JRActivityObject) userDataTag.get("activity"), publishError, providerName);
             }
         }
     }
 
     //todo unify http connection callbacks?
     public void connectionDidFinishLoading(HttpResponseHeaders headers, byte[] payload,
                                            String requestUrl, Object userdata) {
         Log.i(TAG, "[connectionDidFinishLoading-full]");
 
         if (userdata == null) {
             // TODO:  this case is not handled on iPhone, is it possible?
         } else if (userdata instanceof JRDictionary) {
             JRDictionary dictionary = (JRDictionary) userdata;
             if (dictionary.containsKey("tokenUrl")) {
                 List<JRSessionDelegate> delegatesCopy = getDelegatesCopy();
                 for (JRSessionDelegate delegate : delegatesCopy) {
                     delegate.authenticationDidReachTokenUrl(
                             dictionary.getAsString("tokenUrl"),
                             headers,
                             new String(payload),
                             dictionary.getAsString("providerName"));
                 }
             } else Log.e(TAG, "unexpected userdata found in ConnectionDidFinishLoading full");
         } else if (userdata instanceof String) {
             String s = (String)userdata;
 
             if (s.equals("getConfiguration")) {
                 //if the ETag matched, we're done.
                 if (headers.getResponseCode() == HttpURLConnection.HTTP_NOT_MODIFIED) {
                     Log.d(TAG,
                             "[connectionDidFinishLoading] found HTTP_NOT_MODIFIED -> matched ETag");
                     return;
                 }
 
                 //todo this is probably UTF-8 encoded, not ASCII encoded, review this function
                 String payloadString = EncodingUtils.getAsciiString(payload);
                 Log.d(TAG, "[connectionDidFinishLoading-full] payload string: " + payloadString);
 
                 if (payloadString.contains("\"provider_info\":{")) {
                     mError = finishGetConfiguration(payloadString, headers.getETag());
                 } else {
                     Log.e(TAG, "failed to parse response for getConfiguration");
                     mError = new JREngageError(
                             "There was a problem communicating with the Janrain server while configuring authentication.",
                             ConfigurationError.CONFIGURATION_INFORMATION_ERROR,
                             ErrorType.CONFIGURATION_FAILED);
                 }
             } else Log.e(TAG, "unexpected userdata found in ConnectionDidFinishLoading full");
         }
 	}
 
     public void connectionWasStopped(Object userdata) {
         Log.i(TAG, "[connectionWasStopped]");
         // nothing to do here...
 	}
 
     // ------------------------------------------------------------------------
     // METHODS
     // ------------------------------------------------------------------------
 
     public void addDelegate(JRSessionDelegate delegate) {
         if (Config.LOGD) {
             Log.d(TAG, "[addDelegate]");
         }
         mDelegates.add(delegate);
     }
 
     public void removeDelegate(JRSessionDelegate delegate) {
         if (Config.LOGD) {
             Log.d(TAG, "[removeDelegate]");
         }
         mDelegates.remove(delegate);
     }
 
     public void tryToReconfigureLibrary() {
         if (Config.LOGD) {
             Log.d(TAG, "[tryToReconfigureLibrary]");
         }
 
         mError = null;
         mError = startGetConfiguration();
     }
 
     private JREngageError startGetConfiguration() {
         String urlString = String.format(FMT_CONFIG_URL, ENVIRONMENT.getServerUrl(), mAppId, mUrlEncodedAppName);
         if (Config.LOGD) {
             Log.d(TAG, "[startGetConfiguration] url: " + urlString);
         }
 
         final String tag = "getConfiguration";
 
         BasicNameValuePair etagHeader = new BasicNameValuePair("If-None-Match", mOldEtag);
 //                "\"" + mOldEtag + "\"");
         List<NameValuePair> headerList = new ArrayList<NameValuePair>();
         headerList.add(etagHeader);
 
         if (!JRConnectionManager.createConnection(urlString, this, true, headerList, tag)) {
             Log.w(TAG, "[startGetConfiguration] createConnection failed.");
             return new JREngageError(
                     "There was a problem connecting to the Janrain server while configuring authentication.",
                     ConfigurationError.URL_ERROR,
                     JREngageError.ErrorType.CONFIGURATION_FAILED);
         }
 
         return null;
     }
 
     private JREngageError finishGetConfiguration(String dataStr) {
         if (Config.LOGD) {
             Log.d(TAG, "[finishGetConfiguration]");
         }
 
         // Attempt to parse the return string as JSON.
         JRDictionary jsonDict = null;
         Exception jsonEx = null;
         try {
             jsonDict = JRDictionary.fromJSON(dataStr);
         } catch (Exception e) {
             Log.w(TAG, "[finishGetConfiguration] json error: ", e);
             jsonEx = e;
         }
 
         // If the parsed dictionary object is null or an exception has occurred, return.
         if ((jsonDict == null) || (jsonEx != null)) {
             Log.e(TAG, "[finishGetConfiguration] failed.");
             return new JREngageError(
                     "There was a problem communicating with the Janrain server while configuring authentication.",
                     ConfigurationError.JSON_ERROR,
                     ErrorType.CONFIGURATION_FAILED,
                     jsonEx);
         }
 
         // Check to see if the base url has changed
         String baseUrl = jsonDict.getAsString("baseurl", "");
         if (!baseUrl.equals(mBaseUrl)) {
             // Save the new base url
             mBaseUrl = StringUtils.chomp(baseUrl, "/");
             Prefs.putString(Prefs.KEY_JR_BASE_URL, mBaseUrl);
         }
 
         // Get the providers out of the provider_info section.  These are likely to have changed.
         JRDictionary providerInfo = jsonDict.getAsDictionary("provider_info");
         mAllProviders = new JRDictionary();
 
         // For each provider
         for (String name : providerInfo.keySet()) {
             // Get its dictionary
             JRDictionary dictionary = providerInfo.getAsDictionary(name);
             // Use this to create a provider object
             JRProvider provider = new JRProvider(name, dictionary);
             // and finally add the object to our dictionary of providers
             mAllProviders.put(name, provider);
         }
 
         // Get the ordered list of basic providers
         mBasicProviders = jsonDict.getAsListOfStrings("enabled_providers");
         // Get the ordered list of social providers
         mSocialProviders = jsonDict.getAsListOfStrings("social_providers");
 
         // Done!
 
         // Save data to local store
         JRDictionary.archive(ARCHIVE_ALL_PROVIDERS, mAllProviders);
         Archiver.save(ARCHIVE_BASIC_PROVIDERS, mBasicProviders);
         Archiver.save(ARCHIVE_SOCIAL_PROVIDERS, mSocialProviders);
 
         // Figure out of we're suppose to hide the powered by line
         mHidePoweredBy = jsonDict.getAsBoolean("hide_tagline", false);
         Prefs.putBoolean(Prefs.KEY_JR_HIDE_POWERED_BY, mHidePoweredBy);
 
         // Once we know everything is parsed and saved, save the new etag
         Prefs.putString(Prefs.KEY_JR_CONFIGURATION_ETAG, mNewEtag);
 
         // 'git-tag'-like library version tag to prevent reloading stale data from disk
         String libraryVersion = JREngage.getContext().getString(R.string.jr_engage_version);
         Prefs.putString("JREngageVersion", libraryVersion);
 
         mGetConfigDone = true;
         triggerMobileConfigDidFinish();
 
         return null;
     }
 
     private JREngageError finishGetConfiguration(String dataStr, String etag) {
         if (Config.LOGD) {
             Log.d(TAG, "[finishGetConfiguration-etag]");
         }
 
         if (!mOldEtag.equals(etag)) {
             mNewEtag = etag;  //todo verify that this is written out
 
             /* We can only update all of our data if the UI isn't currently using that
              * information.  Otherwise, the library may crash/behave inconsistently.  If a
              * dialog isn't showing, go ahead and update that information.  Or, in the case
              * where a dialog is showing but there isn't any data that it could be using (that
              * is, the lists of basic and social providers are nil), go ahead and update it too.
              * The dialogs won't try and do anything until we're done updating the lists. */
             if (!mDialogIsShowing ||
                     (ListUtils.isEmpty(mBasicProviders) && ListUtils.isEmpty(mSocialProviders))) {
                 return finishGetConfiguration(dataStr);
             }
 
             /* Otherwise, we have to save all this information for later.  The
              * UserInterfaceMaestro sends a signal to sessionData when the dialog closes (by
              * setting the boolean dialogIsShowing to "NO". In the setter function, sessionData
              * checks to see if there's anything stored in the savedConfigurationBlock, and
              * updates it then. */
             mSavedConfigurationBlock = dataStr;
         }
 
         mGetConfigDone = true;
         triggerMobileConfigDidFinish();
 
         return null;
     }
 
     private String getWelcomeMessageFromCookieString(String cookieString) {
         if (Config.LOGD) {
             Log.d(TAG, "[getWelcomeMessageFromCookieString]");
         }
 
         String retval = "Welcome, user!";
 
         if (!TextUtils.isEmpty(cookieString)) {
             String[] parts = cookieString.split("%22");
             if (parts.length > 5) {
                 String part;
                 try {
                     part = URLDecoder.decode(parts[5], "UTF-8");
                 } catch (UnsupportedEncodingException e) {
                     part = null;
                 }
                 if (!TextUtils.isEmpty(part)) {
                     retval = "Sign in as " + part;
                 }
             }
         }
 
         return retval;
     }
 
 //    private void loadLastUsedSocialProvider() {
 //        if (Config.LOGD) {
 //            Log.d(TAG, "[loadLastUsedSocialProvider]");
 //        }
 //        mReturningSocialProvider = Prefs.getAsString(Prefs.KEY_JR_LAST_USED_SOCIAL_PROVIDER, "");
 //    }
 //
 //    private void loadLastUsedBasicProvider() {
 //        if (Config.LOGD) {
 //            Log.d(TAG, "[loadLastUsedBasicProvider]");
 //        }
 //        mReturningBasicProvider = Prefs.getAsString(Prefs.KEY_JR_LAST_USED_BASIC_PROVIDER, "");
 //    }
 
     private void saveLastUsedSocialProvider() {
         if (Config.LOGD) {
             Log.d(TAG, "[saveLastUsedSocialProvider]");
         }
         mReturningSocialProvider = getCurrentlyPublishingProvider().getName();
         Prefs.putString(Prefs.KEY_JR_LAST_USED_SOCIAL_PROVIDER, mReturningSocialProvider);
     }
 
     private void saveLastUsedBasicProvider() {
         if (Config.LOGD) {
             Log.d(TAG, "[saveLastUsedBasicProvider]");
         }
 
         //CookieHelper doesn't interact with our WebView's cookies :(
         String cookies = CookieManager.getInstance().getCookie(getBaseUrl());
         String welcome_info = cookies.replaceAll(".*welcome_info=([^;]*).*", "$1");
         mCurrentlyAuthenticatingProvider.setWelcomeString(
                 getWelcomeMessageFromCookieString(welcome_info));
 
         mReturningBasicProvider = mCurrentlyAuthenticatingProvider.getName();
         Prefs.putString(Prefs.KEY_JR_LAST_USED_BASIC_PROVIDER, mReturningBasicProvider);
     }
 
     public URL startUrlForCurrentlyAuthenticatingProvider() {
         if (Config.LOGD) {
             Log.d(TAG, "[startUrlForCurrentlyAuthenticatingProvider]");
         }
 
         if (mCurrentlyAuthenticatingProvider == null) {
             return null;
         }
 
         String oid;  //open identifier
 
         if (!TextUtils.isEmpty(mCurrentlyAuthenticatingProvider.getOpenIdentifier())) {
             oid = String.format("openid_identifier=%s&", mCurrentlyAuthenticatingProvider.getOpenIdentifier());
             if (mCurrentlyAuthenticatingProvider.requiresInput()) {
                 oid = oid.replaceAll("%@", mCurrentlyAuthenticatingProvider.getUserInput());
             } else {
                 oid = oid.replaceAll("%@", "");
             }
         } else {
             oid = "";
         }
 
         String str;
 
         //todo check on forcereauth
         if (mAlwaysForceReauth || mCurrentlyAuthenticatingProvider.getForceReauth()) {
             for (String domain : mCurrentlyAuthenticatingProvider.getCookieDomains())
                 deleteWebviewCookiesForDomain(domain);
         }
 
         //str = String.format("%s%s?%s%sversion=android_one&device=android",
         str = String.format("%s%s?%s%sdevice=android&extended=true",
                 mBaseUrl,
                 mCurrentlyAuthenticatingProvider.getStartAuthenticationUrl(),
                 oid,
                 ((mAlwaysForceReauth || mCurrentlyAuthenticatingProvider.getForceReauth()) ? "force_reauth=true&" : "")
         );
 
 
         if (Config.LOGD) {
             Log.d(TAG, "[startUrlForCurrentlyAuthenticatingProvider] startUrl: " + str);
         }
 
         URL url = null;
         try {
             url = new URL(str);
         } catch (MalformedURLException e) {
             Log.e(TAG, "[startUrlForCurrentlyAuthenticatingProvider] URL create failed for string: " + str);
         }
         return url;
     }
 
     /* This function is used to determine whether the library should retain first responderness for the
      * UI purposes of making sure that the on screen keyboard stays on screen
     private boolean weShouldBeFirstResponder() {
         if (Config.LOGD) {
             Log.d(TAG, "[weShouldBeFirstResponder]");
         }
 
         /* If we're authenticating with a provider for social publishing, then don't worry about the return experience
          * for basic authentication. *//*
         if (mSocialSharing)
             return mCurrentlyAuthenticatingProvider.requiresInput();
 
         /* If we're authenticating with a basic provider, then we don't need to gather infomation if we're displaying
          * return screen. *//*
         if (mCurrentlyAuthenticatingProvider.isEqualToReturningProvider(mReturningBasicProvider))
             return false;
 
         return mCurrentlyAuthenticatingProvider.requiresInput();
     }*/
 
     public JRAuthenticatedUser getAuthenticatedUserForProvider(JRProvider provider) {
         if (Config.LOGD) {
             Log.d(TAG, "[getAuthenticatedUserForProvider]");
         }
 
         return (JRAuthenticatedUser) mAuthenticatedUsersByProvider.get(provider.getName());
     }
 
     public JRAuthenticatedUser authenticatedUserForProviderNamed(String provider) {
         if (Config.LOGD) {
             Log.d(TAG, "[authenticatedUserForProviderNamed]");
         }
 
         return (JRAuthenticatedUser) mAuthenticatedUsersByProvider.get(provider);
     }
 
     public void forgetAuthenticatedUserForProvider(String providerName) {
         if (Config.LOGD) {
             Log.d(TAG, "[forgetAuthenticatedUserForProvider]");
         }
 
         JRProvider provider = mAllProviders.getAsProvider(providerName);
         if (provider == null) {
             Log.e(TAG, "[forgetAuthenticatedUserForProvider] provider not found: " + providerName);
             //throw new RuntimeException(); /? if they hit the signout button twice, this could happen I guess
         } else {
             provider.setForceReauth(true);
             mAuthenticatedUsersByProvider.remove(provider.getName());
 
             List<String> domains = provider.getCookieDomains();
             for (String d : domains) deleteWebviewCookiesForDomain(d);
 
             JRDictionary.archive(ARCHIVE_AUTH_USERS_BY_PROVIDER, mAuthenticatedUsersByProvider);
         }
     }
 
     private void deleteWebviewCookiesForDomain(String domain) {
         CookieSyncManager csm = CookieSyncManager.createInstance(JREngage.getContext());
         CookieManager cm = CookieManager.getInstance();
 
         //cookies are stored by domain, and are not different for different schemes (i.e. http vs
         //https) (although they do have an optional 'secure' flag.)
         if (domain.startsWith(".")) domain = domain.substring(1);
 
         String cookieGlob = cm.getCookie("http://" + domain);
         if (cookieGlob != null) {
             String[] cookies = cookieGlob.split(";");
             for (String cookieTuple : cookies) {
                 String[] cookieParts = cookieTuple.split("=");
 
                 // setCookie changed a lot in the context of how it handles cookies like we're
                 //setting in order to clear a specific cookie, so spam all reasonable similar
                 //request in an effort to be compatible with different versions of setCookie
                 cm.setCookie(domain, cookieParts[0] + "=;");
 //                cm.setCookie(domain, cookieParts[0] + "=");
 //                cm.setCookie(domain, cookieParts[0]);
             }
             csm.sync();
         }
     }
 
     public void forgetAllAuthenticatedUsers() {
         if (Config.LOGD) {
             Log.d(TAG, "[forgetAllAuthenticatedUsers]");
         }
 
         for (String providerName : mAllProviders.keySet())
             forgetAuthenticatedUserForProvider(providerName);
     }
 
 //    public JRProvider getBasicProviderAtIndex(int index) {
 //        return getProviderAtIndex(index, mBasicProviders);
 //    }
 //
 //    public JRProvider getSocialProviderAtIndex(int index) {
 //        return getProviderAtIndex(index, mSocialProviders);
 //    }
 
     public JRProvider getProviderByName(String name) {
         return mAllProviders.getAsProvider(name);
     }
 
     public void shareActivityForUser(JRAuthenticatedUser user) {
         if (Config.LOGD) {
             Log.d(TAG, "[shareActivityForUser]");
         }
 
         setCurrentlyPublishingProvider(user.getProviderName());
         setSocial(true);
 
         StringBuilder body = new StringBuilder();
         String deviceToken = user.getDeviceToken();
 
         String activityContent;
         JRDictionary activityDictionary = mActivity.dictionaryForObject();
         try {
             String activityJSON = activityDictionary.toJSON();
             activityContent = URLEncoder.encode(activityJSON, "UTF-8");
             body.append("activity=").append(activityContent);
 
             //these are undocumented parameters available to the mobile library.
             body.append("&device_token=").append(deviceToken);
             body.append("&url_shortening=true");
             body.append("&provider=").append(user.getProviderName());
             body.append("&device=android");
             body.append("&app_name=").append(mUrlEncodedAppName);
         } catch (UnsupportedEncodingException e) { throw new RuntimeException(e); }
 
         String url = ENVIRONMENT.getServerUrl() + "/api/v2/activity";
 
         Log.d(TAG, "[shareActivityForUser]: " + url + " data: " + body.toString());
 
         JRDictionary tag = new JRDictionary();
         tag.put("action", "shareActivity");
         tag.put("activity", mActivity);
         tag.put("providerName", mCurrentlyPublishingProvider.getName());
         JRConnectionManager.createConnection(url, body.toString().getBytes(), this, false, tag);
 
         if (Config.LOGD) {
             Log.d(TAG, "[shareActivityForUser] connection started for url: " + url);
         }
     }
 
     public void setStatusForUser(JRAuthenticatedUser user) {
         if (Config.LOGD) {
             Log.d(TAG, "[shareActivityForUser]");
         }
 
         setCurrentlyPublishingProvider(user.getProviderName());
         setSocial(true);
 
         String deviceToken = user.getDeviceToken();
         String status = mActivity.getUserGeneratedContent();
         //todo this should also include other pieces of the activity
 
         StringBuilder body = new StringBuilder();
         //TODO include truncate parameter here?
         body.append("status=").append(status);
 
         //these are undocumented parameters available to the mobile library.
         body.append("&device_token=").append(deviceToken);
         body.append("&device=android");
         body.append("&app_name=").append(mUrlEncodedAppName);
 
         String url = ENVIRONMENT.getServerUrl() + "/api/v2/set_status";
 
         Log.d(TAG, "[setStatusForUser]: " + url + " data: " + body.toString());
 
         //todo same callback handler for status as activity?
         JRDictionary tag = new JRDictionary();
         tag.put("action", "shareActivity");
         tag.put("activity", mActivity);
         tag.put("providerName", mCurrentlyPublishingProvider.getName());
         JRConnectionManager.createConnection(url, body.toString().getBytes(), this, false, tag);
 
         if (Config.LOGD) {
             Log.d(TAG, "[shareActivityForUser] connection started for url: " + url);
         }
     }
 
     private void makeCallToTokenUrl(String tokenUrl, String token, String providerName) {
         if (Config.LOGD) {
             Log.d(TAG, "[makeCallToTokenUrl] token: " + token);
             Log.d(TAG, "[makeCallToTokenUrl] tokenUrl: " + tokenUrl);
         }
 
         String body = "token=" + token;
         byte[] postData = body.getBytes();  //todo URL encode necessary?
         //it probably isn't required, the token is all alphanumerics or something.
 
         JRDictionary tag = new JRDictionary();
         tag.put("action", "callTokenUrl");
         tag.put("tokenUrl", tokenUrl);
         tag.put("providerName", providerName);
 
         if (!JRConnectionManager.createConnection(tokenUrl, postData, this, true, tag)) {
             JREngageError error = new JREngageError(
                     "Problem initializing the connection to the token url",
                     AuthenticationError.AUTHENTICATION_FAILED,
                     ErrorType.AUTHENTICATION_FAILED);
 
             for (JRSessionDelegate delegate : getDelegatesCopy()) {
                 delegate.authenticationCallToTokenUrlDidFail(tokenUrl, error, providerName);
             }
         }
     }
 
     public void triggerAuthenticationDidCompleteWithPayload(JRDictionary rpx_result) {
         if (Config.LOGD) {
             Log.d(TAG, "[triggerAuthenticationDidCompleteWithPayload]");
         }
 
         if (mCurrentlyAuthenticatingProvider == null) {
             return;
         }
 
         JRAuthenticatedUser user = new JRAuthenticatedUser(rpx_result, mCurrentlyAuthenticatingProvider.getName());
         mAuthenticatedUsersByProvider.put(mCurrentlyAuthenticatingProvider.getName(), user);
         JRDictionary.archive(ARCHIVE_AUTH_USERS_BY_PROVIDER, mAuthenticatedUsersByProvider);
 
         if (!mSocialSharing) saveLastUsedBasicProvider();
         //todo
         //else saveLastUsedSocialProvider();
 
 
         for (JRSessionDelegate delegate : getDelegatesCopy()) {
             delegate.authenticationDidComplete(
                     rpx_result.getAsDictionary("auth_info"),
                     mCurrentlyAuthenticatingProvider.getName());
         }
 
        String auth_info_token_for_token_url = rpx_result.getAsString("token");
         if (!TextUtils.isEmpty(mTokenUrl)) {
             makeCallToTokenUrl(mTokenUrl, auth_info_token_for_token_url, mCurrentlyAuthenticatingProvider.getName());
         }
 
         mCurrentlyAuthenticatingProvider.setForceReauth(false);
         setCurrentlyAuthenticatingProvider((String) null);
     }
 
     public void triggerAuthenticationDidFail(JREngageError error) {
         if (Config.LOGD) {
             Log.d(TAG, "[triggerAuthenticationDidFailWithError]");
         }
 
         String providerName = mCurrentlyAuthenticatingProvider.getName();
 
         setCurrentlyAuthenticatingProvider((String) null);
         mReturningBasicProvider = null;
         mReturningSocialProvider = null;
 
         //This method is only called by JRWebViewActivity at the moment, when Engage returns an
         //error or there is a networking problem.  I think that clearing the cookies might be a
         //good thing to do here.
         forgetAuthenticatedUserForProvider(providerName);
 
         for (JRSessionDelegate delegate : getDelegatesCopy()) {
             delegate.authenticationDidFail(error, providerName);
         }
     }
 
     public void triggerAuthenticationDidCancel() {
         if (Config.LOGD) {
             Log.d(TAG, "[triggerAuthenticationDidCancel]");
         }
 
         setCurrentlyAuthenticatingProvider((String) null);
         mReturningBasicProvider = null;
 
         for (JRSessionDelegate delegate : getDelegatesCopy()) {
             delegate.authenticationDidCancel();
         }
     }
 
     public void triggerAuthenticationDidRestart() {
         if (Config.LOGD) {
             Log.d(TAG, "[triggerAuthenticationDidRestart]");
         }
 
         for (JRSessionDelegate delegate : getDelegatesCopy()) {
             delegate.authenticationDidRestart();
         }
     }
 
     //todo nothing is triggering this?
     public void triggerPublishingDidComplete() {
         if (Config.LOGD) {
             Log.d(TAG, "[triggerPublishingDidComplete]");
         }
 
         for (JRSessionDelegate delegate : getDelegatesCopy()) {
             delegate.publishingDidComplete();
         }
 
         mSocialSharing = false;
     }
 
     public void triggerPublishingJRActivityDidFail(JREngageError error) {
         if (Config.LOGD) {
             Log.d(TAG, "[triggerPublishingJRActivityDidFail]");
         }
 
         for (JRSessionDelegate delegate : getDelegatesCopy()) {
             String provider = "";
 
             if (mCurrentlyPublishingProvider != null)
                 provider = mCurrentlyPublishingProvider.getName();
 
             delegate.publishingJRActivityDidFail(mActivity, error, provider);
         }
     }
 
     public void triggerPublishingDialogDidFail(JREngageError err) {
        if (Config.LOGD) {
             Log.d(TAG, "[triggerPublishingDialogDidFail]");
         }
 
         for (JRSessionDelegate delegate : getDelegatesCopy()) delegate.publishingDialogDidFail(err);
     }
 
     public void triggerPublishingDidCancel() {
         if (Config.LOGD) {
             Log.d(TAG, "[triggerPublishingDidCancel]");
         }
 
         for (JRSessionDelegate delegate : getDelegatesCopy()) delegate.publishingDidCancel();
     }
 
     private void triggerMobileConfigDidFinish() {
         for (JRSessionDelegate d : getDelegatesCopy()) d.mobileConfigDidFinish();
     }
 
     /*
     TODO:  This method appears to have gone away.
     TODO RESPONSE: Um... pretty sure it just moved to a spot higher in the iPhone version of the file?
 
     private String appNameAndVersion() {
         final String FMT = "mUrlEncodedAppName=%s.%s&version=%d_%s";
 
         Context ctx = JREngage.getContext();
         PackageManager pkgMgr = ctx.getPackageManager();
         PackageInfo pkgInfo = null;
         try {
             pkgInfo = pkgMgr.getPackageInfo(ctx.getPackageName(), 0);
         } catch (PackageManager.NameNotFoundException e) {
             Log.w(TAG, "[appNameAndVersion] package manager issue -> ", e);
         }
 
         return (pkgInfo != null)
                 ? String.format(FMT,
                     pkgInfo.packageName,
                     pkgInfo.applicationInfo.nonLocalizedLabel,
                     pkgInfo.versionCode,
                     pkgInfo.versionName)
                 : "";
     }
      */
 
     private synchronized List<JRSessionDelegate> getDelegatesCopy() {
         return (mDelegates == null)
                 ? new ArrayList<JRSessionDelegate>()
                 : new ArrayList<JRSessionDelegate>(mDelegates);
     }
 
     public JRProvider getCurrentlyPublishingProvider() {
         return mCurrentlyPublishingProvider;
     }
 
     public void setCurrentlyPublishingProvider(String provider) {
         Log.d(TAG, "[setCurrentlyPublishingProvider]: " + provider);
         mCurrentlyPublishingProvider = getProviderByName(provider);
     }
 
     public boolean isGetMobileConfigDone() {
         return mGetConfigDone;
     }
 
     public static JREnvironment getEnvironment() {
         return ENVIRONMENT;
     }
 
     public String getUrlEncodedAppName() {
         return mUrlEncodedAppName;
     }
 }
