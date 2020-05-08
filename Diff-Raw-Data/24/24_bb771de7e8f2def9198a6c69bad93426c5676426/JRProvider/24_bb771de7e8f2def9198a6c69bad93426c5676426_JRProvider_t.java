 /*
  *  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  *  Copyright (c) 2011, Janrain, Inc.
  *
  *  All rights reserved.
  *
  *  Redistribution and use in source and binary forms, with or without modification,
  *  are permitted provided that the following conditions are met:
  *
  *  * Redistributions of source code must retain the above copyright notice, this
  *    list of conditions and the following disclaimer.
  *  * Redistributions in binary form must reproduce the above copyright notice,
  *    this list of conditions and the following disclaimer in the documentation and/or
  *    other materials provided with the distribution.
  *  * Neither the name of the Janrain, Inc. nor the names of its
  *    contributors may be used to endorse or promote products derived from this
  *    software without specific prior written permission.
  *
  *
  *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
  *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
  *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  */
 package com.janrain.android.engage.session;
 
 import android.content.Context;
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.drawable.Drawable;
 import android.graphics.drawable.StateListDrawable;
 import android.text.TextUtils;
 import com.janrain.android.R;
 import com.janrain.android.engage.JREngage;
 import com.janrain.android.engage.types.JRDictionary;
 import com.janrain.android.utils.AndroidUtils;
 import com.janrain.android.utils.LogUtils;
 import com.janrain.android.utils.PrefUtils;
 import com.janrain.android.utils.ThreadUtils;
 import com.janrain.android.utils.WebViewUtils;
 
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.Serializable;
 import java.lang.ref.SoftReference;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import static com.janrain.android.utils.LogUtils.throwDebugException;
 
 /**
  * @internal
  * @class JRProvider
  */
 public class JRProvider implements Serializable {
     public static final String KEY_FRIENDLY_NAME = "friendly_name";
     public static final String KEY_INPUT_PROMPT = "input_prompt";
     public static final String KEY_OPENID_IDENTIFIER = "openid_identifier";
     public static final String KEY_SAML_PROVIDER = "saml_provider";
     public static final String KEY_OPX_BLOB = "opx_blob";
     public static final String KEY_URL = "url";
     public static final String KEY_REQUIRES_INPUT = "requires_input";
     public static final String KEY_SOCIAL_SHARING_PROPERTIES = "social_sharing_properties";
     public static final String KEY_COOKIE_DOMAINS = "cookie_domains";
     public static final String KEY_ANDROID_WEBVIEW_OPTIONS = "android_webview_options";
     public static final String KEY_ICON_RESOURCE_ID = "icon_resource_id";
 
     private static final String TAG = JRProvider.class.getSimpleName();
 
     private static Map<String, SoftReference<Drawable>> provider_list_icon_drawables =
             Collections.synchronizedMap(new HashMap<String, SoftReference<Drawable>>());
 
     // Suppressed because this inner class is not expected to be serialized
     @SuppressWarnings("serial")
     private final static Map<String, Integer> provider_list_icon_resources =
             new HashMap<String, Integer>() {
                 {
                     put("icon_bw_facebook", R.drawable.jr_icon_bw_facebook);
                     put("icon_bw_linkedin", R.drawable.jr_icon_bw_linkedin);
                     put("icon_bw_myspace", R.drawable.jr_icon_bw_myspace);
                     put("icon_bw_twitter", R.drawable.jr_icon_bw_twitter);
                     put("icon_bw_yahoo", R.drawable.jr_icon_bw_yahoo);
                     put("icon_bw_amazon", R.drawable.jr_icon_bw_amazon);
                     put("icon_bw_tumblr", R.drawable.jr_icon_bw_tumblr);
                     put("icon_bw_googleplus", R.drawable.jr_icon_bw_googleplus);
 
                     put("icon_aol", R.drawable.jr_icon_aol);
                     put("icon_blogger", R.drawable.jr_icon_blogger);
                     put("icon_facebook", R.drawable.jr_icon_facebook);
                     put("icon_flickr", R.drawable.jr_icon_flickr);
                     put("icon_foursquare", R.drawable.jr_icon_foursquare);
                     put("icon_google", R.drawable.jr_icon_google);
                     put("icon_hyves", R.drawable.jr_icon_hyves);
                     put("icon_linkedin", R.drawable.jr_icon_linkedin);
                     put("icon_live_id", R.drawable.jr_icon_live_id);
                     put("icon_livejournal", R.drawable.jr_icon_livejournal);
                     put("icon_myopenid", R.drawable.jr_icon_myopenid);
                     put("icon_myspace", R.drawable.jr_icon_myspace);
                     put("icon_netlog", R.drawable.jr_icon_netlog);
                     put("icon_openid", R.drawable.jr_icon_openid);
                     put("icon_orkut", R.drawable.jr_icon_orkut);
                     put("icon_paypal", R.drawable.jr_icon_paypal);
                     put("icon_salesforce", R.drawable.jr_icon_salesforce);
                     put("icon_twitter", R.drawable.jr_icon_twitter);
                     put("icon_verisign", R.drawable.jr_icon_verisign);
                     put("icon_vzn", R.drawable.jr_icon_vzn);
                     put("icon_wordpress", R.drawable.jr_icon_wordpress);
                     put("icon_yahoo", R.drawable.jr_icon_yahoo);
                     put("icon_amazon", R.drawable.jr_icon_amazon);
                     put("icon_tumblr", R.drawable.jr_icon_tumblr);
                     put("icon_googleplus", R.drawable.jr_icon_googleplus);
                 }
             };
 
     private static Map<String, SoftReference<Drawable>> provider_logo_drawables =
             Collections.synchronizedMap(new HashMap<String, SoftReference<Drawable>>());
 
     // Suppressed because this inner class is not expected to be serialized
     @SuppressWarnings("serial")
     private final static HashMap<String, Integer> provider_logo_resources =
             new HashMap<String, Integer>() {
                 {
                     put("logo_aol", R.drawable.jr_logo_aol);
                     put("logo_blogger", R.drawable.jr_logo_blogger);
                     put("logo_facebook", R.drawable.jr_logo_facebook);
                     put("logo_flickr", R.drawable.jr_logo_flickr);
                     put("logo_foursquare", R.drawable.jr_logo_foursquare);
                     put("logo_google", R.drawable.jr_logo_google);
                     put("logo_hyves", R.drawable.jr_logo_hyves);
                     put("logo_linkedin", R.drawable.jr_logo_linkedin);
                     put("logo_live_id", R.drawable.jr_logo_live_id);
                     put("logo_livejournal", R.drawable.jr_logo_livejournal);
                     put("logo_myopenid", R.drawable.jr_logo_myopenid);
                     put("logo_myspace", R.drawable.jr_logo_myspace);
                     put("logo_netlog", R.drawable.jr_logo_netlog);
                     put("logo_openid", R.drawable.jr_logo_openid);
                     put("logo_orkut", R.drawable.jr_logo_orkut);
                     put("logo_paypal", R.drawable.jr_logo_paypal);
                     put("logo_salesforce", R.drawable.jr_logo_salesforce);
                     put("logo_twitter", R.drawable.jr_logo_twitter);
                     put("logo_verisign", R.drawable.jr_logo_verisign);
                     put("logo_vzn", R.drawable.jr_logo_vzn);
                     put("logo_yahoo", R.drawable.jr_logo_yahoo);
                     put("logo_amazon", R.drawable.jr_logo_amazon);
                     put("logo_tumblr", R.drawable.jr_logo_tumblr);
                     put("logo_googleplus", R.drawable.jr_logo_googleplus);
                 }
             };
 
     private String mName;
     private String mFriendlyName;
     private String mInputHintText;
     private String mUserInputDescriptor;
     private boolean mRequiresInput;
     private String mOpenIdentifier;
     private String mSamlProvider;
     private String mOpxBlob;
     private String mStartAuthenticationUrl;
     private List<String> mCookieDomains;
     private JRDictionary mSocialSharingProperties;
     private JRDictionary mWebViewOptions;
     private Integer mIconResourceId;
 
     private transient boolean mForceReauthStartUrlFlag;   // <- these two user parameters get preserved
     private transient String mUserInput = ""; // <- across cached provider reloads
     private transient boolean mCurrentlyDownloading;
 
     public JRProvider(String name, JRDictionary dictionary) {
         mName = name;
         mFriendlyName = dictionary.getAsString(KEY_FRIENDLY_NAME);
         mInputHintText = dictionary.getAsString(KEY_INPUT_PROMPT);
         mOpenIdentifier = dictionary.getAsString(KEY_OPENID_IDENTIFIER);
         mSamlProvider = dictionary.getAsString(KEY_SAML_PROVIDER);
         mOpxBlob = dictionary.getAsString(KEY_OPX_BLOB);
         mStartAuthenticationUrl = dictionary.getAsString(KEY_URL);
         mRequiresInput = dictionary.getAsBoolean(KEY_REQUIRES_INPUT);
         mCookieDomains = dictionary.getAsListOfStrings(KEY_COOKIE_DOMAINS, true);
         mSocialSharingProperties = dictionary.getAsDictionary(KEY_SOCIAL_SHARING_PROPERTIES);
         mWebViewOptions = dictionary.getAsDictionary(KEY_ANDROID_WEBVIEW_OPTIONS, true);
         mIconResourceId = dictionary.getAsInt(KEY_ICON_RESOURCE_ID);
 
         loadDynamicVariables();
 
         if (mRequiresInput) {
             String[] arr = mInputHintText.split(" ");
             ArrayList<String> shortList = new ArrayList<String>();
             shortList.addAll(Arrays.asList(arr).subList((arr.length - 2), arr.length));
             mUserInputDescriptor = TextUtils.join(" ", shortList);
         } else {
             mUserInputDescriptor = "";
         }
 
         // Called in the constructor to preemptively download missing icons
         getProviderLogo(JREngage.getApplicationContext());
     }
 
     /**
      * Not null
      */
     public List<String> getCookieDomains() {
         return mCookieDomains;
     }
 
     public String getName() {
         return mName;
     }
 
     public String getFriendlyName() {
         return mFriendlyName;
     }
 
     public String getUserInputHint() {
         return mInputHintText;
     }
 
     public String getUserInputDescriptor() {
         return mUserInputDescriptor;
     }
 
     public boolean requiresInput() {
         return mRequiresInput;
     }
 
     public String getOpenIdentifier() {
         return mOpenIdentifier;
     }
 
     public String getSamlProvider() {
         return mSamlProvider;
     }
 
     public String getOpxBlob() {
         return mOpxBlob;
     }
 
     public String getStartAuthenticationUrl() {
         return mStartAuthenticationUrl;
     }
 
     public JRDictionary getSocialSharingProperties() {
         return mSocialSharingProperties;
     }
 
     public JRDictionary getWebViewOptions() {
         return mWebViewOptions;
     }
 
     public boolean getForceReauthUrlFlag() {
         return mForceReauthStartUrlFlag;
     }
 
     private void setForceReauthFlag(boolean forceReauth) {
         this.mForceReauthStartUrlFlag = forceReauth;
 
         PrefUtils.putBoolean(PrefUtils.KEY_JR_FORCE_REAUTH + this.mName, this.mForceReauthStartUrlFlag);
     }
 
     public String getUserInput() {
         return mUserInput;
     }
 
     public void setUserInput(String userInput) {
         mUserInput = userInput;
 
         PrefUtils.putString(PrefUtils.KEY_JR_USER_INPUT + this.mName, this.mUserInput);
     }
 
     public void clearForceReauth() {
         mForceReauthStartUrlFlag = false;
     }
 
     public void clearCookiesOnCookieDomains(Context c) {
         WebViewUtils.deleteWebViewCookiesForDomains(c, getCookieDomains());
     }
 
     public void forceReauth(Context c) {
         if (getCookieDomains().size() > 0) {
             clearCookiesOnCookieDomains(c);
         } else {
             setForceReauthFlag(true);
         }
     }
 
     private Drawable getDrawable(Context c,
                                  String drawableName,
                                  Map<String, SoftReference<Drawable>> drawableMap,
                                  Map<String, Integer> resourceMap) {
         if (drawableMap.containsKey(drawableName)) {
             Drawable d = drawableMap.get(drawableName).get();
             if (d != null) {
                 return d;
             } else {
                 drawableMap.remove(drawableName);
             }
         }
 
         if (resourceMap.containsKey(drawableName)) {
             Drawable r = c.getResources().getDrawable(resourceMap.get(drawableName));
             drawableMap.put(drawableName, new SoftReference<Drawable>(r));
             return r;
         } else if (mIconResourceId != null) {
             int id = mIconResourceId.intValue();
             try {
                 Drawable r = c.getResources().getDrawable(id);
                 drawableMap.put(drawableName, new SoftReference<Drawable>(r));
                 return r;
             } catch (Resources.NotFoundException e) {
                 LogUtils.logd("Could not find resource for " + drawableName);
             }
         }
 
         if (AndroidUtils.isCupcake()) {
             // 1.5 can't handle programmatic XHDPI resource instantiation
             return c.getResources().getDrawable(R.drawable.jr_icon_unknown);
         }
 
         try {
             String iconFileName = "providericon~" + drawableName + ".png";
 
             Bitmap icon = BitmapFactory.decodeStream(c.openFileInput(iconFileName));
             if (icon != null) {
                 // Downloaded icons are all at xhdpi, but Android 2.1 doesn't have the
                 // DENSITY_XHIGH constant defined yet. But it does the right thing
                 // if you pass in the DPI as an int
 
                 AndroidUtils.bitmapSetDensity(icon, 320);
                 return AndroidUtils.newBitmapDrawable(c, icon);
             } else {
                 c.deleteFile(iconFileName);
             }
         } catch (FileNotFoundException ignore) {
         }
 
         downloadIcons(c);
         return c.getResources().getDrawable(R.drawable.jr_icon_unknown);
     }
 
     public Drawable getProviderIcon(Context c) {
         return getDrawable(c, "icon_" + mName, provider_list_icon_drawables, provider_list_icon_resources);
     }
 
     public Drawable getProviderLogo(Context c) {
         return getDrawable(c, "logo_" + mName, provider_logo_drawables, provider_logo_resources);
     }
 
     public Drawable getTabSpecIndicatorDrawable(Context c) {
         Drawable colorIcon = getDrawable(c,
                 "icon_" + mName,
                 provider_list_icon_drawables,
                 provider_list_icon_resources);
 
         Drawable bwIcon = getDrawable(c,
                 "icon_bw_" + mName,
                 provider_list_icon_drawables,
                 provider_list_icon_resources);
 
         StateListDrawable sld = new StateListDrawable();
         sld.addState(new int[]{android.R.attr.state_selected}, colorIcon);
         sld.addState(new int[]{}, bwIcon);
 
         return sld;
     }
 
     @SuppressWarnings({"unchecked"})
     private void downloadIcons(final Context c) {
         LogUtils.logd(TAG, "downloadIcons: " + mName);
 
         synchronized (this) {
             if (mCurrentlyDownloading) return;
             else mCurrentlyDownloading = true;
         }
 
         final String[] iconFileNames = {
                 "icon_" + mName + ".png",
                 "icon_bw_" + mName + ".png",
                 "logo_" + mName + ".png"
         };
 
         ThreadUtils.executeInBg(new Runnable() {
             public void run() {
                 for (String iconFileName : iconFileNames) {
                     FileOutputStream fos = null;
                     if (Arrays.asList(c.fileList()).contains("providericon~" + iconFileName)) continue;
                 }
                 mCurrentlyDownloading = false;
             }
         });
     }
 
     public void loadDynamicVariables() {
         mUserInput = PrefUtils.getString(PrefUtils.KEY_JR_USER_INPUT + mName, "");
         mForceReauthStartUrlFlag = PrefUtils.getBoolean(PrefUtils.KEY_JR_FORCE_REAUTH + mName, false);
     }
 
     public String toString() {
         return mName;
     }
 
     private Resources getResources() {
         return JREngage.getApplicationContext().getResources();
     }
 
     /**
      * @param lighter If true then return the blend of the providers color at 20% alpha over a white
      *                background
      * @return The color
      * @internal
      */
     public int getProviderColor(boolean lighter) {
         Exception error;
         try {
             List colorList = (List) getSocialSharingProperties().get("color_values");
 
             int finalColor = 255; // Full alpha
             for (int i = 0; i < 3; i++) {
                 finalColor <<= 8;
                 int newByte = (int) (((Double) colorList.get(i)) * 255.0);
                 if (lighter) newByte = (int) (newByte * 0.2d + 255 * 0.8d);
 
                 // protect against floating point imprecision overflowing one byte:
                 finalColor += Math.min(newByte, 255);
             }
 
             return finalColor;
         } catch (ClassCastException e) {
             error = e;
         } catch (IndexOutOfBoundsException e) {
             error = e;
         }
 
         throwDebugException(new RuntimeException("Error parsing provider color", error));
         return getResources().getColor(R.color.jr_janrain_darkblue_lightened);
     }
 
     public static String getLocalizedName(String conflictingIdentityProvider) {
         if (conflictingIdentityProvider.equals("capture")) {
             return JREngage.getApplicationContext().getString(R.string.jr_traditional_account_name);
         }
         return JRSession.getInstance().getProviderByName(conflictingIdentityProvider).getFriendlyName();
     }
 }
