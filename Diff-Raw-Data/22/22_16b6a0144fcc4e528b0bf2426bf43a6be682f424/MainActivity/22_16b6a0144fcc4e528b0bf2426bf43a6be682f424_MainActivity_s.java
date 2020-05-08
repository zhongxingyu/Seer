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
 package com.janrain.android.simpledemo;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.graphics.drawable.Drawable;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.text.Html;
 import android.text.TextUtils;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.Toast;
 import com.janrain.android.engage.*;
 import com.janrain.android.engage.net.async.HttpResponseHeaders;
 import com.janrain.android.engage.types.*;
 
 import com.janrain.android.engage.utils.Android;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.*;
 
import static com.janrain.android.simpledemo.SimpleDemoEnvironment.getAppId;
import static com.janrain.android.simpledemo.SimpleDemoEnvironment.getTokenUrl;

 public class MainActivity extends Activity implements View.OnClickListener, JREngageDelegate {
     private static final String TAG = MainActivity.class.getSimpleName();
 
     private static final int DIALOG_JRENGAGE_ERROR = 1;
 
    private static String ENGAGE_APP_ID = getAppId();
    private static String ENGAGE_TOKEN_URL = getTokenUrl();
 
     private JREngage mEngage;
     private JRActivityObject mActivity;
 
     private Button mBtnTestAuth;
     private Button mBtnTestPub;
     private Button mBtnTestLand;
     private String mDialogErrorMessage;
 
     // Blog fetching variables
     private String mTitleText = "title text";
     private String mActionLink = "http://www.janrain.com/feed/blogs";
     private String mDescriptionText = "description text";
     private String mImageUrl = "http://www.janrain.com/sites/default/themes/janrain/logo.png";
     private String mDescriptionHtml = "";
 
     private final Uri BLOGURL = Uri.parse("http://www.janrain.com/feed/blogs");
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         Log.d(TAG, "onCreate");
 
         setContentView(R.layout.main);
 
         mBtnTestAuth = (Button)findViewById(R.id.btn_test_auth);
         mBtnTestAuth.setOnClickListener(this);
 
         mBtnTestPub = (Button)findViewById(R.id.btn_test_pub);
         mBtnTestPub.setOnClickListener(this);
 
         mBtnTestLand = (Button)findViewById(R.id.btn_test_land);
         mBtnTestLand.setOnClickListener(this);
 
         // Test button code to test setAlwaysForceReauth
         //final ToggleButton tb = ((ToggleButton)findViewById(R.id.btn_always_force_reauth));
         //tb.setOnClickListener(new View.OnClickListener() {
         //    public void onClick(View v) {
         //        mEngage.setAlwaysForceReauthentication(tb.isChecked());
         //    }
         //});
 
        mEngage = JREngage.initInstance(this, ENGAGE_APP_ID, ENGAGE_TOKEN_URL, this);
 
         if (savedInstanceState != null && savedInstanceState.containsKey("a")) {
             mTitleText = savedInstanceState.getString("a");
             mDescriptionText = savedInstanceState.getString("b");
             mImageUrl = savedInstanceState.getString("c");
             mActionLink = savedInstanceState.getString("d");
             mDescriptionHtml = savedInstanceState.getString("e");
             mBtnTestPub.setText("Test Publishing");
             Log.d(TAG, "restoring savedInstanceState: (" + mTitleText + ", " + mDescriptionText +
                 ", " + mImageUrl + ", " + mActionLink + ")");
 
             buildActivity();
         } else {
             // Build an ~empty Activity
             buildActivity();
 
             asyncLoadJanrainBlog();
         }
     }
 
     void buildActivity() {
         mActivity = new JRActivityObject("shared an article from the Janrain Blog!",
             mActionLink);
         //mActivity = new JRActivityObject("shared an article from the Janrain Blog!", "");
         mActivity.setTitle(mTitleText);
         mActivity.setDescription(mDescriptionText);
         mActivity.setMedia(new JRImageMediaObject(mImageUrl, mImageUrl));
 
         //List<String> urls = new ArrayList<String>();
         //Pattern urlPattern = Pattern.compile("(https?://[\\S&&[^\"']]{4,})");
         //Matcher urlMatcher = urlPattern.matcher(mDescriptionHtml);
         //while (urls.size() < 5 && urlMatcher.find()) urls.add(urlMatcher.group());
         //
         //String smsBody = mDescriptionText;
         //if (urls.size() > 0) smsBody = (urls.get(0) + smsBody);
         //smsBody = smsBody.substring(0, Math.min(139, smsBody.length()));
 
         String smsBody = "Check out this article!\n" + mActionLink;
         String emailBody = mActionLink + "\n" + mDescriptionText;
 
         JRSmsObject sms = new JRSmsObject(smsBody);
         JREmailObject email = new JREmailObject("Check out this article!", emailBody);
         //sms.setUrls(urls);
         //email.setUrls(urls);
         sms.addUrl(mActionLink);
         email.addUrl(mActionLink);
         mActivity.setEmail(email);
         mActivity.setSms(sms);
         //mActivity.setUserGeneratedContent("Now is the time for all great men to come to the aid of their country.");
     }
 
     @Override
     protected void onStart() {
         super.onStart();
         Log.d(TAG, "onStart");
     }
 
     private void asyncLoadJanrainBlog() {
         mBtnTestPub.setText("loading blog");
 
         new AsyncTask<Void, Void, Boolean>() {
             protected Boolean doInBackground(Void... v) {
                 Exception error;
                 try {
                     Log.d(TAG, "blogload");
                     URL u = (new URL(BLOGURL.toString()));
                     URLConnection uc = u.openConnection();
                     InputStream is = uc.getInputStream();
                     Log.d(TAG, "blogload stream open");
                     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                     dbf.setCoalescing(true);
                     dbf.setValidating(false);
                     dbf.setNamespaceAware(false);
                     DocumentBuilder db = dbf.newDocumentBuilder();
                     Log.d(TAG, "blogload factory instantiated");
 
                     // The following parse call takes ten seconds on a fast phone.
                     // XMLPullParser is said to be a faster way to go.
                     // sample code here: http://groups.google.com/group/android-developers/msg/ddc6a8e83963a6b5
                     // another thread: http://stackoverflow.com/questions/4958973/3rd-party-android-xml-parser
                     Document d = db.parse(is);
                     Log.d(TAG, "blogload parsed");
 
                     Element rss = (Element) d.getElementsByTagName("rss").item(0);
                     Element channel = (Element) rss.getElementsByTagName("channel").item(0);
                     Element item = (Element) channel.getElementsByTagName("item").item(0);
                     Element title = (Element) item.getElementsByTagName("title").item(0);
                     Element link = (Element) item.getElementsByTagName("link").item(0);
                     Element description = (Element) item.getElementsByTagName("description")
                             .item(0);
                     Log.d(TAG, "blogload walked");
 
                     mTitleText = title.getFirstChild().getNodeValue();
                     mActionLink = link.getFirstChild().getNodeValue();
                     Log.d(TAG, "Action Link: " + mActionLink);
 
                     // We need to concatenate all the children of the description element (which has
                     // ~100s of TextElement children) in order to come up with the complete
                     // description
                     mDescriptionHtml = "";
                     NodeList nl = description.getChildNodes();
                     for (int x=0; x<nl.getLength(); x++) {
                         mDescriptionHtml += nl.item(x).getNodeValue();
                     }
 
                     // The description is in html, so we decode it to display it as plain text
                     // And while decoding it we pull out a link to an image if there is one.
                     mDescriptionText = Html.fromHtml(mDescriptionHtml, new Html.ImageGetter() {
                         public Drawable getDrawable(String s) {
                             mImageUrl = BLOGURL.getScheme() + "://" + BLOGURL.getHost() + s;
                             return null;
                         }
                     }, null).toString();
                     // Strip out the Unicode "Object replacement character" that Html.fromHtml
                     // inserts in place of <img ... > tags.
                     mDescriptionText = mDescriptionText.replaceAll("\ufffc", "");
 
                     // No exceptions -> success
                     return true;
                 }
                 catch (MalformedURLException e) { error = e; }
                 catch (IOException e) { error = e; }
                 catch (ParserConfigurationException e) { error = e; }
                 catch (SAXException e) { error = e; }
                 catch (NullPointerException e) { error = e; }
 
                 // Exceptions -> failure
                 Log.e(TAG, "Error loading Janrain blog", error);
                 return false;
             }
 
             protected void onPostExecute(Boolean loadSuccess) {
                 Log.d(TAG, "blog loader onPostExecute");
                 if (loadSuccess) mBtnTestPub.setText("Test Publishing");
                 else mBtnTestPub.setText("Failed to load blog");
 
                 // Rebuild the Activity now that the blog is loaded.
                 buildActivity();
             }
         }.execute();
     }
 
     public Dialog onCreateDialog(int dialogId) {
         switch (dialogId) {
             case DIALOG_JRENGAGE_ERROR:
                 return new AlertDialog.Builder(this)
                     .setPositiveButton("Dismiss", null)
                     .setCancelable(false)
                     .setMessage(mDialogErrorMessage)
                     .create();
         }
 
         throw new RuntimeException("unknown dialogId");
     }
 
     public void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
 
         Log.d(TAG, "onSaveInstanceState");
 
         if (!mBtnTestPub.getText().toString().equals("loading blog")) {
             outState.putString("a", mTitleText);
             outState.putString("b", mDescriptionText);
             outState.putString("c", mImageUrl);
             outState.putString("d", mActionLink);
             outState.putString("e", mDescriptionHtml);
         }
     }
 
     public void onClick(View view) {
         if (view == mBtnTestAuth) {
             mEngage.showAuthenticationDialog();
         } else if (view == mBtnTestPub) {
             //JRActivityObject jra = new JRActivityObject("shared an article from the Janrain Blog!",
             //  mActionLink);
             //JRActivityObject jra = new JRActivityObject("shared an article from the Janrain Blog!",
             //        "");
             //jra.setTitle(mTitleText);
             //jra.setDescription(mDescriptionText);
             //jra.setMedia(new JRImageMediaObject(mImageUrl, mImageUrl));
             mEngage.showSocialPublishingDialog(mActivity);
         }
     }
 
     // ------------------------------------------------------------------------
     // JREngage DELEGATE METHODS
     // ------------------------------------------------------------------------
     public void jrEngageDialogDidFailToShowWithError(JREngageError error) {
         mDialogErrorMessage = "JREngage dialog failed to show, error: " +
                 ((error == null) ? "unknown" : error.getMessage());
 
         showDialog(DIALOG_JRENGAGE_ERROR);
     }
 
     public void jrAuthenticationDidSucceedForUser(JRDictionary authInfo, String provider) {
         JRDictionary profile = (authInfo == null) ? null : authInfo.getAsDictionary("profile");
         String displayName = (profile == null) ? null : profile.getAsString("displayName");
         String message = "Authentication successful" + ((TextUtils.isEmpty(displayName))
                 ? "" : (" for user: " + displayName));
 
         Toast.makeText(this, message, Toast.LENGTH_LONG).show();
     }
 
     public void jrAuthenticationDidReachTokenUrl(String tokenUrl,
                                                  HttpResponseHeaders response,
                                                  String tokenUrlPayload,
                                                  String provider) {
         Toast.makeText(this, "Authentication did reach token URL: " + tokenUrlPayload,
                 Toast.LENGTH_LONG).show();
     }
 
     public void jrAuthenticationDidNotComplete() {
         Toast.makeText(this, "Authentication did not complete", Toast.LENGTH_LONG).show();
     }
 
     public void jrAuthenticationDidFailWithError(JREngageError error, String provider) {
         String message = "Authentication failed, error: " +
                 ((error == null) ? "unknown" : error.getMessage());
 
         Toast.makeText(this, message, Toast.LENGTH_LONG).show();
     }
 
     public void jrAuthenticationCallToTokenUrlDidFail(String tokenUrl,
                                                       JREngageError error,
                                                       String provider) {
         Toast.makeText(this, "Authentication failed to reach token URL", Toast.LENGTH_LONG).show();
     }
 
     public void jrSocialDidNotCompletePublishing() {
         Toast.makeText(this, "Sharing did not complete", Toast.LENGTH_LONG).show();
     }
 
     public void jrSocialDidCompletePublishing() {
         //Toast.makeText(this, "Sharing dialog did complete", Toast.LENGTH_LONG).show();
     }
 
     public void jrSocialDidPublishJRActivity(JRActivityObject activity, String provider) {
         Toast.makeText(this, "Activity shared", Toast.LENGTH_LONG).show();
     }
 
     public void jrSocialPublishJRActivityDidFail(JRActivityObject activity,
                                                  JREngageError error,
                                                  String provider) {
         Toast.makeText(this, "Activity failed to share", Toast.LENGTH_LONG).show();
     }
 }
