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
  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
 package com.janrain.android.engage.types;
 
 import android.text.Html;
 import android.util.Log;
 import com.janrain.android.engage.net.JRConnectionManager;
 import com.janrain.android.engage.net.JRConnectionManagerDelegate;
 import com.janrain.android.engage.session.JRSessionData;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.json.JSONStringer;
 import org.json.JSONTokener;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 
 /*
  * @file
  * @brief Interface for creating and populating activities that you wish to publish.
  *
  * Interface for creating and populating activities that you wish to publish
  * to your user's social networks.  Create an activity object, fill in the
  * object's fields, and pass the object to the JREngage library when you
  * are ready to share.
  */
 
 /**
  * @brief An activity object you create, populate, and post to the user's activity stream.
  *
  * Create an activity object, fill in the object's fields, and pass the object to
  * the JREngage library when you are ready to publish. Currently supported providers are:
  *   - Facebook
  *   - LinkedIn
  *   - Twitter
  *   - MySpace
  *   - Yahoo!
  *
  * Janrain Engage will make a best effort to use all of the fields submitted in the activity
  * request, but note that how they get presented (and which ones are used) ultimately depends on
  * the provider.
  *
  * This API will work if and only if:
  *   - Your Janrain Engage application has been configured with the given provider
  *   - The user has already authenticated and has given consent to publish activity
  *
  * Otherwise, you will be given an error response indicating what was wrong. Detailed error
  * responses will also be given if the activity parameter does not meet the formatting requirements
  * described below.
  *
  * @sa For more information of Janrain Engage's activity api, see <a
  * href="https://rpxnow.com/docs#api_activity">the activity section</a> of our API Documentation.
  *
  * @nosubgrouping
  **/
 public class JRActivityObject {
     private static final String TAG = JRActivityObject.class.getSimpleName();
 
 /**
  * @name Private Attributes
  * The various properties of the JRActivityObject that you can access and configure through the
  * object's constructor, getters, and setters
  **/
 /*@{*/
     /**
      * A string describing what the user did, written in the third person (e.g.,
      * "wrote a restaurant review", "posted a comment", "took a quiz").
      *
      * @par Getter:
      *      #getAction()
      **/
     private String mAction;
 
     /**
      * The URL of the resource being mentioned in the activity update.
      *
      * @par Getter:
      *      #getUrl()
      **/
     private String mUrl; //url associated with the action
 
     /**
      * A string containing user-supplied content, such as a comment or the first paragraph of
      * an article that the user wrote.
      *
      * @note
      *      Some providers (Twitter in particular) may truncate this value.
      *
      * @par Getter/Setter:
      *      #getUserGeneratedContent(), #setUserGeneratedContent()
      **/
     private String mUserGeneratedContent;
 
     /**
      * The title of the resource being mentioned in the activity update.
      *
      * @note No length restriction on the status is imposed by Janrain Engage, however Yahoo
      * truncates this value to 256 characters.
      *
      * @par Getter/Setter:
      *      #getTitle(), #setTitle()
      **/
     private String mTitle = "";
 
     /**
      * A description of the resource mentioned in the activity update.
      *
      * @par Getter/Setter:
      *      #getDescription(), #setDescription()
      **/
     private String mDescription = "";
 
     /**
      * An array of JRActionLink objects, each having two attributes: \e text and \e href.
      * An action link is a link a user can use to take action on an activity update on the provider.
      *
      * @par Example:
      * @code
      * action_links:
      * [
      *   {
      *     "text": "Rate this quiz result",
      *     "href": "http://example.com/quiz/12345/result/6789/rate"
      *   },
      *   {
      *     "text": "Take this quiz",
      *     "href": "http://example.com/quiz/12345/take"
      *   }
      * ]
      * @endcode
      *
      * @note
      *      Any objects added to this array that are not of type JRActionLink will
      *      be ignored.
      *
      * @par Getter/Setter:
      *      #getActionLinks(), #setActionLinks(), #addActionLink()
      **/
     private List<JRActionLink> mActionLinks = new ArrayList<JRActionLink>();
 
     /**
      * An array of objects with base class \e JRMediaObject (i.e. JRImageMediaObject,
      * JRFlashMediaObject, JRMp3MediaObject).
      *
      * To publish attached media objects with your activity, create the preferred
      * object, populate the object's fields, then add the object to the #mMedia array.
      * You can attach pictures, videos, and mp3s to your activity, although how the
      * media objects get presented and whether or not they are used, depend on the provider.
      *
      * If you include more than one media type in the array, JREngage will
      * choose only one of these types, in this order:
      *   -# image
      *   -# flash
      *   -# mp3
      *
      * Also, any objects added to this array that do not inherit from \e JRMediaObject
      * will be ignored.
      *
      * @sa Media object format and rules are identical to those described on the <a
      * href="http://developers.facebook.com/docs/guides/attachments">Facebook Developer page
      * on Attachments</a>.
      *
      * @par Getter/Setter:
      *      #getMedia(), #setMedia(List<JRMediaObject>), #setMedia(JRMediaObject)
      **/
     private List<JRMediaObject> mMedia = new ArrayList<JRMediaObject>();
 
     /**
      * An object with attributes describing properties of the update. An attribute \e value can be
      * a string or an object with two attributes, \e text and \e href.
      *
      * @par Example:
      * @code
      *   properties:
      *   {
      *       "Time": "05:00",
      *       "Location":
      *       {
      *           "text": "Portland",
      *           "href": "http://en.wikipedia.org/wiki/Portland,_Oregon"
      *       }
      *   }
      * @endcode
      *
      * @par Getter/Setter:
      *      #getProperties(), #setProperties()
      **/
     private Map<String, Object> mProperties = new HashMap<String, Object>();
 
     /**
      * A JREmailObject to use to prefill a sharing email sent by the user
      */
     private JREmailObject mEmail;
 
     /**
      * A JRSmsObject to use to prefill a sharing SMS sent by the user
      */
     private JRSmsObject mSms;
 /*@}*/
 
 /**
  * @name Constructors
  * Constructor for JRActivityObject
  **/
 /*@{*/
     /**
      * Returns a JRActivityObject initialized with the given action and url.
      *
      * @param action
      *   A string describing what the user did, written in the third person.  This value cannot
      *   be null
      *
      * @param url
      *   The URL of the resource being mentioned in the activity update
      *
      * @throws IllegalArgumentException
      *   if text or href is null
      **/
     public JRActivityObject(String action, String url) {
         if (action == null) throw new IllegalArgumentException("illegal null action or null href");
 
         Log.d(TAG, "created with action: " + action + " url: " + url);
         mAction = action;
         mUrl = url;
     }
 /*@}*/
 
 /**
  * @name Getters/Setters
  * Getters and setters for the JRActivityObject's private properties
  **/
 /*@{*/
 
     /**
      * Getter for the activity object's #mAction property.
      *
      * @return
      *      A string describing what the user did, written in the third person (e.g.,
      *      "wrote a restaurant review", "posted a comment", "took a quiz")
      **/
     public String getAction() {  /* (readonly) */
         return mAction;
     }
 
     /**
      * Getter for the activity object's #mUrl property.
      *
      * @return
      *       The URL of the resource being mentioned in the activity update
      **/
     public String getUrl() {  /* (readonly) */
         return mUrl;
     }
 
     /**
      * Getter for the activity object's #mUserGeneratedContent property.
      *
      * @return
      *      A string containing user-supplied content, such as a comment or the first paragraph of
      *      an article that the user wrote
      **/
     public String getUserGeneratedContent() {
         return mUserGeneratedContent;
     }
 
     /**
      * Setter for the activity object's #mUserGeneratedContent property.
      *
      * @param userGeneratedContent
      *      A string containing user-supplied content, such as a comment or the first paragraph of
      *      an article that the user wrote
      **/
     public void setUserGeneratedContent(String userGeneratedContent) {
         mUserGeneratedContent = userGeneratedContent;
     }
 
     /**
      * Getter for the activity object's #mTitle property.
      *
      * @return
      *      The title of the resource being mentioned in the activity update
      **/
     public String getTitle() {
         return mTitle;
     }
 
     /**
      * Setter for the activity object's #mTitle property.
      *
      * @param title
      *      The title of the resource being mentioned in the activity update
      **/
     public void setTitle(String title) {
         mTitle = title;
     }
 
     /**
      * Getter for the activity object's #mDescription property.
      *
      * @return
      *      A description of the resource mentioned in the activity update
      **/
     public String getDescription() {
         return mDescription;
     }
 
     /**
      * Setter for the activity object's #mDescription property.
      *
      * @param description
      *      A description of the resource mentioned in the activity update
      **/
     public void setDescription(String description) {
         mDescription = description;
     }
 
     /**
      * Getter for the activity object's #mActionLinks property.
      *
      * @return
      *      An array of JRActionLink objects, each having two attributes: \e text and \e href.
      *      An action link is a link a user can use to take action on an activity update on the provider
      **/
     public List<JRActionLink> getActionLinks() {
         return mActionLinks;
     }
 
     /**
      * Setter for the activity object's #mActionLinks property.
      *
      * @param actionLinks
      *      An array of JRActionLink objects, each having two attributes: \e text and \e href.
      *      An action link is a link a user can use to take action on an activity update on the provider
      **/
     public void setActionLinks(List<JRActionLink> actionLinks) {
         mActionLinks = actionLinks;
     }
 
     /**
      * Setter for the activity object's #mActionLinks property.
      *
      * @param actionLink
      *      A single JRActionLink to be added to the array of action links, creating the array if
      *      it hasn't already been created
      **/
     public void addActionLink(JRActionLink actionLink) {
         if (mActionLinks == null)
             mActionLinks = new ArrayList<JRActionLink>();
         mActionLinks.add(actionLink);
     }
 
     /**
      * Getter for the activity object's #mMedia property.
      *
      * @return
      *      An array of objects with base class \e JRMediaObject (i.e. JRImageMediaObject,
      *      JRFlashMediaObject, JRMp3MediaObject)
      **/
     public List<JRMediaObject> getMedia() {
         return mMedia;
     }
 
     /**
      * Setter for the activity object's #mMedia property.
      *
      * @param media
      *      An array of objects with base class \e JRMediaObject (i.e. JRImageMediaObject,
      *      JRFlashMediaObject, JRMp3MediaObject)
      **/
     public void setMedia(List<JRMediaObject> media) {
         mMedia = media;
     }
 
     /**
      * Setter for the activity object's #mMedia property.
      *
      * @param mediaObject
      *        An single JRImageMediaObject, JRFlashMediaObject, or JRMp3MediaObject to be added to
      *        the array of media objects, creating the array if it hasn't already been created
      **/
     public void setMedia(JRMediaObject mediaObject) {
         if (mMedia == null)
             mMedia = new ArrayList<JRMediaObject>();
         mMedia.add(mediaObject);
     }
 
     /**
      * Getter for the activity object's #mProperties property.
      *
      * @return
      *      An object with attributes describing properties of the update. An attribute value can be
      *      a string or an object with two attributes, \e text and \e href.
      **/
     public Map<String, Object> getProperties() {
         return mProperties;
     }
 
     /**
      * Setter for the activity object's #mProperties property.
      *
      * @param properties
      *      An object with attributes describing properties of the update. An attribute value can be
      *      a string or an object with two attributes, \e text and \e href.
      **/    
     public void setProperties(Map<String, Object> properties) {
         mProperties = properties;
     }
 
     public void setEmail(JREmailObject email) {
         mEmail = email;
     }
 
     public JREmailObject getEmail() {
         return mEmail;
     }
 
     public void setSms(JRSmsObject sms) {
         mSms = sms;
     }
 
     public JRSmsObject getSms() {
         return mSms;
     }    
 /*@}*/
 
     /**
      * @internal
      * Returns a HashMap (Dictionary) representing the JRActivityObject.
      *
      * @return
      *   An HashMap (Dictionary) of String objects representing the JRActivityObject.
      *
      * NOTE: This function should not be used directly.  It is intended only for use by the
      * JREngage library.
      *
      */
     public JRDictionary toJRDictionary() {
         JRDictionary map = new JRDictionary();
         map.put("url", mUrl);
         map.put("action", mAction);
         map.put("user_generated_content", mUserGeneratedContent);
         map.put("title", mTitle);
         map.put("description", mDescription);
         map.put("action_links", mActionLinks);
         map.put("media", mMedia);
         map.put("properties", mProperties);
         return map;
     }
 
     public interface ShortenedUrlCallback {
         public void setShortenedUrl(String shortenedUrl);
     }
 
     public void shortenUrls(final ShortenedUrlCallback callback) {
         final JRSessionData sessionData = JRSessionData.getInstance();
         try {
             // todo invoke when the activity object is created (or maybe when publish is called?)
             List<String> emptyList = new ArrayList<String>();
             final List<String> emailUrls = mEmail == null ? emptyList : mEmail.getUrls();
             final List<String> smsUrls = mSms == null ? emptyList : mSms.getUrls();
 
             // Make the JSON string
             JSONStringer jss = (new JSONStringer())
                     .object()
                         .key("activity")
                         .array()
                             .value(getUrl())
                         .endArray()
                        .key("email")
                         .array();
                             for (String url : emailUrls) jss.value(url);
                         jss.endArray()
                        .key("sms")
                         .array();
                             for (String url : smsUrls) jss.value(url);
                         jss.endArray()
                     .endObject();
 
             // Make the URL
             final String jsonEncodedActivityUrl = jss.toString();
            String urlEncodedJson = URLEncoder.encode(jsonEncodedActivityUrl, "UTF8");
             final String getUrlsUrl =
                     sessionData.getBaseUrl() + "/openid/get_urls?"
                    + "urls=" + urlEncodedJson
                     + "&app_name=" + sessionData.getUrlEncodedAppName()
                     + "&device=android";
 
             // Define the network callback
             JRConnectionManagerDelegate jrcmd =
                     new JRConnectionManagerDelegate.SimpleJRConnectionManagerDelegate() {
                 public void connectionDidFinishLoading(String payload,
                                                        String requestUrl,
                                                        Object userdata) {
                     String shortUrl = getUrl();
 
                     try {
                         Log.d(TAG, "fetchShortenedURLs connectionDidFinishLoading: " + payload);
                         JSONObject jso = (JSONObject) (new JSONTokener(payload)).nextValue();
                         jso = jso.getJSONObject("urls");
                         JSONObject jsonActivityUrls = jso.getJSONObject("activity");
                        JSONObject jsonSmsUrls = jso.getJSONObject("sms");
                        JSONObject jsonEmailUrls = jso.getJSONObject("email");
 
                         shortUrl = jsonActivityUrls.getString(getUrl());
                         if (mEmail != null) {
                             List<String> shortEmailUrls = new ArrayList<String>();
                             for (String longUrl : emailUrls)
                                 shortEmailUrls.add(jsonEmailUrls.getString(longUrl));
                             mEmail.setShortUrls(shortEmailUrls);
                         }
                         if (mSms != null) {
                             List<String> shortSmsUrls = new ArrayList<String>();
                             for (String longUrl : smsUrls)
                                 shortSmsUrls.add(jsonSmsUrls.getString(longUrl));
                             mSms.setShortUrls(shortSmsUrls);
                         }
                     } catch (JSONException e) {
                         Log.e(TAG, "URL shortening JSON parse error", e);
                     } catch (ClassCastException e) {
                         Log.e(TAG, "URL shortening JSON parse error", e);
                     }
 
                     updateUI(shortUrl);
                 }
 
                 void updateUI(String shortenedUrl) {
                     callback.setShortenedUrl(shortenedUrl);
                 }
 
                 public void connectionDidFail(Exception ex, String requestUrl, Object userdata) {
                     updateUI(getUrl());
                 }
 
                 public void connectionWasStopped(Object userdata) {
                     updateUI(getUrl());
                 }
             };
 
             // Invoke the network call
             JRConnectionManager.createConnection(getUrlsUrl, jrcmd, false, null);
         } catch (JSONException e) {
             Log.e(TAG, "URL shortening JSON error", e);
         } catch (UnsupportedEncodingException e) {
             Log.e(TAG, "URL shortening error", e);
         }
     }
 }
