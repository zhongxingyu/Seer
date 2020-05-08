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
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
 import android.graphics.drawable.StateListDrawable;
 import android.os.AsyncTask;
 import android.text.TextUtils;
 import android.util.Config;
 import android.util.Log;
 import com.janrain.android.engage.JREnvironment;
 import com.janrain.android.engage.R;
 import com.janrain.android.engage.prefs.Prefs;
 import com.janrain.android.engage.types.JRDictionary;
 
 import java.io.*;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.*;
 
 /**
  * TODO:DOC
  */
 public class JRProvider implements Serializable {
 	
     // ------------------------------------------------------------------------
     // TYPES
     // ------------------------------------------------------------------------
 
     // ------------------------------------------------------------------------
     // STATIC FIELDS
     // ------------------------------------------------------------------------
 
 //	public static final String KEY_NAME = "name";
 	public static final String KEY_FRIENDLY_NAME = "friendly_name";
 //	public static final String KEY_PLACEHOLDER_TEXT = "placeholder_text";
 //	public static final String KEY_SHORT_TEXT = "short_text";
 	public static final String KEY_INPUT_PROMPT = "input_prompt";
 	public static final String KEY_OPENID_IDENTIFIER = "openid_identifier";
 	public static final String KEY_URL = "url";
 	public static final String KEY_REQUIRES_INPUT = "requires_input";
 
     private static final String TAG = JRProvider.class.getSimpleName();
 //
 //    private static HashMap<String, Drawable> provider_tabspec_drawables = new HashMap<String, Drawable>();
 //
 //    private final static HashMap<String, Integer> provider_tabspec_resources = new HashMap<String, Integer>(){
 //        {
 //            //put("facebook", getResources().getDrawable(R.drawable.ic_facebook_tab));
 //            put("linkedin", R.drawable.ic_linkedin_tab);
 //            put("myspace", R.drawable.ic_myspace_tab);
 //            put("twitter", R.drawable.ic_twitter_tab);
 //            put("yahoo", R.drawable.ic_yahoo_tab);
 //        }
 //    };
     
     private static HashMap<String, Drawable> provider_list_icon_drawables =
             new HashMap<String, Drawable>();
 
     private final static HashMap<String, Integer> provider_list_icon_resources
             = new HashMap<String, Integer>(){
                     {
                         put("icon_bw_facebook_30x30", R.drawable.icon_bw_facebook_30x30);
                         put("icon_bw_linkedin_30x30", R.drawable.icon_bw_linkedin_30x30);
                         put("icon_bw_myspace_30x30", R.drawable.icon_bw_myspace_30x30);
                         put("icon_bw_twitter_30x30", R.drawable.icon_bw_twitter_30x30);
                         put("icon_bw_yahoo_30x30", R.drawable.icon_bw_yahoo_30x30);
                         put("icon_aol_30x30", R.drawable.icon_aol_30x30);
                         put("icon_blogger_30x30", R.drawable.icon_blogger_30x30);
                         put("icon_facebook_30x30", R.drawable.icon_facebook_30x30);
                         put("icon_flickr_30x30", R.drawable.icon_flickr_30x30);
                         put("icon_google_30x30", R.drawable.icon_google_30x30);
                         put("icon_hyves_30x30", R.drawable.icon_hyves_30x30);
                         put("icon_linkedin_30x30", R.drawable.icon_linkedin_30x30);
                         put("icon_live_id_30x30", R.drawable.icon_live_id_30x30);
                         put("icon_livejournal_30x30", R.drawable.icon_livejournal_30x30);
                         put("icon_myopenid_30x30", R.drawable.icon_myopenid_30x30);
                         put("icon_myspace_30x30", R.drawable.icon_myspace_30x30);
                         put("icon_netlog_30x30", R.drawable.icon_netlog_30x30);
                         put("icon_openid_30x30", R.drawable.icon_openid_30x30);
                         put("icon_paypal_30x30", R.drawable.icon_paypal_30x30);
                         put("icon_twitter_30x30", R.drawable.icon_twitter_30x30);
                         put("icon_verisign_30x30", R.drawable.icon_verisign_30x30);
                         put("icon_wordpress_30x30", R.drawable.icon_wordpress_30x30);
                         put("icon_yahoo_30x30", R.drawable.icon_yahoo_30x30);
                     }
     };
 
     private static HashMap<String, Drawable> provider_logo_drawables = new HashMap<String, Drawable>();
 
     private final static HashMap<String, Integer> provider_logo_resources =
             new HashMap<String, Integer>(){
                     {
                         put("logo_aol_280x65", R.drawable.logo_aol_280x65);
                         put("logo_blogger_280x65", R.drawable.logo_blogger_280x65);
                         put("logo_facebook_280x65", R.drawable.logo_facebook_280x65);
                         put("logo_flickr_280x65", R.drawable.logo_flickr_280x65);
                         put("logo_google_280x65", R.drawable.logo_google_280x65);
                         put("logo_hyves_280x65", R.drawable.logo_hyves_280x65);
                         put("logo_linkedin_280x65", R.drawable.logo_linkedin_280x65);
                         put("logo_live_id_280x65", R.drawable.logo_live_id_280x65);
                         put("logo_livejournal_280x65", R.drawable.logo_livejournal_280x65);
                         put("logo_myopenid_280x65", R.drawable.logo_myopenid_280x65);
                         put("logo_myspace_280x65", R.drawable.logo_myspace_280x65);
                         put("logo_netlog_280x65", R.drawable.logo_netlog_280x65);
                         put("logo_openid_280x65", R.drawable.logo_openid_280x65);
                         put("logo_paypal_280x65", R.drawable.logo_paypal_280x65);
                         put("logo_twitter_280x65", R.drawable.logo_twitter_280x65);
                         put("logo_verisign_280x65", R.drawable.logo_verisign_280x65);
                         put("logo_yahoo_280x65", R.drawable.logo_yahoo_280x65);
                     }
     };
 
 //    private static HashMap<String, Drawable> provider_button_short_drawables = new HashMap<String, Drawable>();
 
 //    private final static HashMap<String, Integer> provider_button_short_resources = new HashMap<String, Integer>(){
 //        {
 ////            put("button_facebook_135x40", R.drawable.button_facebook_135x40);
 //            put("button_linkedin_135x40", R.drawable.button_linkedin_135x40);
 //            put("button_myspace_135x40", R.drawable.button_myspace_135x40);
 //            put("button_twitter_135x40", R.drawable.button_twitter_135x40);
 //            put("button_yahoo_135x40", R.drawable.button_yahoo_135x40);
 //        }
 //    };
 
 //    private static HashMap<String, Drawable> provider_button_long_drawables = new HashMap<String, Drawable>();
 
 //    private final static HashMap<String, Integer> provider_button_long_resources = new HashMap<String, Integer>(){
 //        {
 ////            put("button_facebook_280x40", R.drawable.button_facebook_280x40);
 //            put("button_linkedin_280x40", R.drawable.button_linkedin_280x40);
 //            put("button_myspace_280x40", R.drawable.button_myspace_280x40);
 //            put("button_twitter_280x40", R.drawable.button_twitter_280x40);
 //            put("button_yahoo_280x40", R.drawable.button_yahoo_280x40);
 //        }
 //    };
 
 
     // ------------------------------------------------------------------------
     // STATIC INITIALIZERS
     // ------------------------------------------------------------------------
 
     // ------------------------------------------------------------------------
     // STATIC METHODS
     // ------------------------------------------------------------------------
 
     // ------------------------------------------------------------------------
     // FIELDS
     // ------------------------------------------------------------------------
 
     private String mName;
     private String mFriendlyName;
     private String mPlaceholderText;
     private String mShortText;
     private boolean mRequiresInput;
     private String mOpenIdentifier;
     private String mStartAuthenticationUrl;
     private List<String> mCookieDomains;
     private JRDictionary mSocialSharingProperties;
 
     //private transient JRDictionary dictionary;
     private transient boolean mForceReauth;   // <- these three user parameters get preserved
     private transient String mUserInput;      // <- across cached provider reloads
     private transient String mWelcomeString;  // <-
     private transient boolean mCurrentlyDownloading;
     // ------------------------------------------------------------------------
     // INITIALIZERS
     // ------------------------------------------------------------------------
 
     // ------------------------------------------------------------------------
     // CONSTRUCTORS
     // ------------------------------------------------------------------------
 
     private JRProvider() {}
 
     public JRProvider(String name, JRDictionary dictionary) {
         mName = name;
         mFriendlyName = dictionary.getAsString(KEY_FRIENDLY_NAME);
         mPlaceholderText = dictionary.getAsString(KEY_INPUT_PROMPT);
         mOpenIdentifier = dictionary.getAsString(KEY_OPENID_IDENTIFIER);
         mStartAuthenticationUrl = dictionary.getAsString(KEY_URL);
         mRequiresInput = dictionary.getAsBoolean(KEY_REQUIRES_INPUT);
         mCookieDomains = dictionary.getAsListOfStrings("cookie_domains", true);
         mSocialSharingProperties = dictionary.getAsDictionary("social_sharing_properties");
 
 //        if (mCookieDomains.size() == 0) {
 //            mCookieDomains.add(mName + ".com");
 //            mCookieDomains.add("www." + mName + ".com");
 //        }
 
         loadDynamicVariables();
 
         if (mRequiresInput) {
             String[] arr = mPlaceholderText.split(" ");
             ArrayList<String> shortList = new ArrayList<String>();
            for (int i = 2; i < (arr.length - 2); i++) {
                 shortList.add(arr[i]);
             }
             mShortText = TextUtils.join(" ", shortList);
         } else {
             mShortText = "";
         }
     }
 
     // ------------------------------------------------------------------------
     // GETTERS/SETTERS
     // ------------------------------------------------------------------------
 
     public List<String> getCookieDomains () { /* (readonly) */
         return mCookieDomains;
     }
 
     public String getName() {  /* (readonly) */
         return mName;
     }
 
     public String getFriendlyName() {  /* (readonly) */
         return mFriendlyName;
     }
 
     public String getPlaceholderText() {  /* (readonly) */
         return mPlaceholderText;
     }
 
     public String getShortText() {  /* (readonly) */
         return mShortText;
     }
 
     public boolean requiresInput() {  /* (readonly) */
         return mRequiresInput;
     }
 
     public String getOpenIdentifier() { /* (readonly) */
         return mOpenIdentifier;
     }
 
     public String getStartAuthenticationUrl() { /* (readonly) */
         return mStartAuthenticationUrl;
     }
 
     public JRDictionary getSocialSharingProperties() { /* (readonly) */
         return mSocialSharingProperties;
     }
 
     public boolean getForceReauth() {
         return mForceReauth;
     }
 
     public void setForceReauth(boolean forceReauth) {
         this.mForceReauth = forceReauth;
 
         //XXX shouldn't we clear the users cookie too?
 
         Prefs.putBoolean(Prefs.KEY_JR_FORCE_REAUTH + this.mName, this.mForceReauth);
     }
 
     public String getUserInput() {
         return mUserInput;
     }
 
     public void setUserInput(String userInput) {
         if (Config.LOGD) {
             Log.d("JRProvider", "[prov] user input: [" + Prefs.KEY_JR_USER_INPUT + mName + "]");
             }
 
         this.mUserInput = userInput;
 
         Prefs.putString(Prefs.KEY_JR_USER_INPUT + this.mName, this.mUserInput);
     }
 
     public String getWelcomeString() {
         return mWelcomeString;
     }
 
     public void setWelcomeString(String welcomeString) {
         this.mWelcomeString = welcomeString;
 
         Prefs.putString(Prefs.KEY_JR_WELCOME_STRING + this.mName, this.mWelcomeString);
     }
 
     private Drawable getDrawable(Context c, String drawableName, HashMap<String, Drawable> drawableHashMap, HashMap<String, Integer> resourceHashMap) {
         if (drawableHashMap.containsKey(drawableName)) return drawableHashMap.get(drawableName);
 
         if (resourceHashMap.containsKey(drawableName)) {
             Drawable r = c.getResources().getDrawable(resourceHashMap.get(drawableName));
             drawableHashMap.put(drawableName, r);
             return r;
         }
 
         try {
             String iconFileName = "providericon~" + drawableName + ".png";
 
             Bitmap icon = BitmapFactory.decodeStream(c.openFileInput(iconFileName));
             if (icon != null) {
                 icon.setDensity(android.util.DisplayMetrics.DENSITY_MEDIUM);
             }
             else {
                 c.deleteFile(iconFileName);
                 downloadIcons(c);
                 return c.getResources().getDrawable(R.drawable.icon_unknown);
             }
 
             return new BitmapDrawable(c.getResources(), icon);
         }
         catch (FileNotFoundException e) {
             downloadIcons(c);
 
             return c.getResources().getDrawable(R.drawable.icon_unknown);
         }
     }
 
 
     public Drawable getProviderListIconDrawable(Context c) {
         return getDrawable(c, "icon_" + mName + "_30x30", provider_list_icon_drawables, provider_list_icon_resources);
     }
 
     public Drawable getProviderLogo(Context c) {
         return getDrawable(c, "logo_" + mName + "_280x65", provider_logo_drawables, provider_logo_resources);
 
     }
 
 //    public Drawable getProviderButtonShort (Context c) {
 //        return getDrawable(c, "button_" + mName + "_135x40.png", provider_button_short_drawables, provider_button_short_resources);
 //    }
 //
 //    public Drawable getProviderButtonLong (Context c) {
 //        return getDrawable(c, "button_" + mName + "_280x40.png", provider_button_long_drawables, provider_button_long_resources);
 //    }
 
     public Drawable getTabSpecIndicatorDrawable(Context c) {
         Drawable colorIcon = getDrawable(c, "icon_" + mName + "_30x30", provider_list_icon_drawables, provider_list_icon_resources);
         Drawable bwIcon = getDrawable(c, "icon_bw_" + mName + "_30x30", provider_list_icon_drawables, provider_list_icon_resources);
 
         StateListDrawable sld = new StateListDrawable();
         sld.addState(new int[]{android.R.attr.state_selected}, colorIcon);
         sld.addState(new int[]{}, bwIcon);
 
         return sld;
     }
 
 
 //        if (provider_tabspec_drawables.containsKey(mName)) return provider_tabspec_drawables.get(mName);
 //
 //        if (provider_tabspec_resources.containsKey(mName)) {
 //            Drawable r = c.getResources().getDrawable(provider_tabspec_resources.get(mName));
 //            provider_tabspec_drawables.put(mName, r);
 //            return r;
 //        }
 //
 //        StateListDrawable sld = new StateListDrawable();
 //        provider_tabspec_drawables.put(mName, sld);
 //
 //        try{
 //            String colorIconFileName = "providericon~" + "icon_" + mName + "_30x30.png";
 //            String bwIconFileName = "providericon~" + "icon_bw_" + mName + "_30x30.png";
 //
 //            Bitmap colorIcon = BitmapFactory.decodeStream(c.openFileInput(colorIconFileName));
 //            if (colorIcon == null) c.deleteFile(colorIconFileName);
 //
 //            Bitmap bwIcon = BitmapFactory.decodeStream(c.openFileInput(bwIconFileName));
 //            if (bwIcon == null) c.deleteFile(bwIconFileName);
 //
 //            sld.addState(new int[]{android.R.attr.state_selected}, new BitmapDrawable(colorIcon));
 //            sld.addState(new int[]{}, new BitmapDrawable(bwIcon));
 //
 //            return sld;
 //        } catch (FileNotFoundException e) {
 //            Drawable missingIconIcon = c.getResources().getDrawable(R.drawable.icon_unknown);
 //            sld.addState(new int[]{android.R.attr.state_selected}, missingIconIcon);
 //            sld.addState(new int[]{}, missingIconIcon);
 //
 //            downloadIcons(c);
 //
 //            return sld;
 //        }
 //    }
 
     private void downloadIcons(final Context c) {
         Log.d(TAG, "downloadIcons: " + mName);
 
         synchronized (this) {
             if (mCurrentlyDownloading) return;
             else mCurrentlyDownloading = true;
         }
 
         final String[] iconFileNames = {
             "icon_" + mName + "_30x30.png",
             "icon_" + mName + "_30x30@2x.png",
             "logo_" + mName + "_280x65.png",
             "logo_" + mName + "_280x65@2x.png",
             "icon_bw_" + mName + "_30x30.png",
             "icon_bw_" + mName + "_30x30@2x.png",
             "button_" + mName + "_135x40.png",
             "button_" + mName + "_135x40@2x.png",
             "button_" + mName + "_280x40.png",
             "button_" + mName + "_280x40@2x.png"
         };
 
         new AsyncTask<Void, Void, Void>(){
             public Void doInBackground(Void... s) {
                 for (String iconFileName : iconFileNames) {
                     try {
                         if (Arrays.asList(c.fileList()).contains("providericons~" + iconFileName)) continue;
 
                         Log.d(TAG, "Downloading icon: " + iconFileName);
                         //todo fixme to use the library compile time configured baseurl
                         URL url = new URL(JRSessionData.getEnvironment().getServerUrl()
                                 + "/cdn/images/mobile_icons/android/" + iconFileName);
                         InputStream is = url.openStream();
                         FileOutputStream fos = c.openFileOutput("providericon~" + iconFileName, Context.MODE_PRIVATE);
                         while (is.available() > 0) fos.write(is.read());
                         fos.close();
                     } catch (MalformedURLException e) {
                         Log.d(TAG, e.toString());
                     } catch (IOException e) {
                         Log.d(TAG, e.toString());
                     }
                 }
                 mCurrentlyDownloading = false;
                 return null;
             }
         }.execute();
     }
 
     // ------------------------------------------------------------------------
     // METHODS
     // ------------------------------------------------------------------------
 
 //	public void initWithCoder(JRDictionary coder) {
 //		mName = coder.getAsString(KEY_NAME);
 //		mFriendlyName = coder.getAsString(KEY_FRIENDLY_NAME);
 //		mPlaceholderText = coder.getAsString(KEY_PLACEHOLDER_TEXT);
 //		mShortText = coder.getAsString(KEY_SHORT_TEXT);
 //		mOpenIdentifier = coder.getAsString(KEY_OPENID_IDENTIFIER);
 //		mStartAuthenticationUrl = coder.getAsString(KEY_URL);
 //		mRequiresInput = coder.getAsBoolean(KEY_REQUIRES_INPUT);
 //
 //        this.loadDynamicVariables();
 //    }
 //
 //    public void encodeWithCoder(JRDictionary coder) {
 //		coder.put(KEY_NAME, mName);
 //		coder.put(KEY_FRIENDLY_NAME, mFriendlyName);
 //		coder.put(KEY_PLACEHOLDER_TEXT, mPlaceholderText);
 //		coder.put(KEY_SHORT_TEXT, mShortText);
 //		coder.put(KEY_OPENID_IDENTIFIER, mOpenIdentifier);
 //		coder.put(KEY_URL, mStartAuthenticationUrl);
 //		coder.put(KEY_REQUIRES_INPUT, mRequiresInput);
 //    }
 //
 //    public boolean isEqualToProvider(JRProvider provider) {
 //    	boolean isEqual = false;
 //    	if (provider != null) {
 //    		isEqual = ((!TextUtils.isEmpty(mName)) && (mName.equals(provider.getName())));
 //    	}
 //    	return isEqual;
 //    }
 
     public void loadDynamicVariables() {
         if (Config.LOGD) {
             Log.d("JRProvider", "[prov] user input: [" + Prefs.KEY_JR_USER_INPUT + mName + "]");
         }
 
     	mUserInput = Prefs.getAsString(Prefs.KEY_JR_USER_INPUT + mName, "");
     	mWelcomeString = Prefs.getAsString(Prefs.KEY_JR_WELCOME_STRING + mName, "");
     	mForceReauth = Prefs.getAsBoolean(Prefs.KEY_JR_FORCE_REAUTH + mName, false);
     }
 }
 
