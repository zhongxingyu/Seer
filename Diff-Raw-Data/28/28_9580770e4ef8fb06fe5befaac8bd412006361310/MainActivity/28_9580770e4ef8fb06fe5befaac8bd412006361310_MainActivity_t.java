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
 package com.janrain.android.simpledemo;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.text.Editable;
 import android.text.TextUtils;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.Toast;
 import com.janrain.android.engage.net.JRConnectionManager;
 import com.janrain.android.engage.ui.JRCustomUiConfiguration;
 import com.janrain.android.engage.ui.JRCustomUiView;
 import com.janrain.android.engage.JREngage;
 import com.janrain.android.engage.JREngageDelegate;
 import com.janrain.android.engage.JREngageError;
 import com.janrain.android.engage.net.async.HttpResponseHeaders;
 import com.janrain.android.engage.types.JRActionLink;
 import com.janrain.android.engage.types.JRActivityObject;
 import com.janrain.android.engage.types.JRDictionary;
 import com.janrain.android.engage.types.JREmailObject;
 import com.janrain.android.engage.types.JRImageMediaObject;
 import com.janrain.android.engage.types.JRSmsObject;
 import com.janrain.android.engage.utils.AndroidUtils;
 import com.janrain.android.engage.utils.Prefs;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 public class MainActivity extends FragmentActivity {
     private static final String TAG = MainActivity.class.getSimpleName();
 
     private static final int DIALOG_JR_ENGAGE_ERROR = 1;
     public static final String ACTION_LINK_KEY = "action_link";
 
     private String readAsset(String fileName) {
         try {
             InputStream is = getAssets().open(fileName);
             byte[] buffer = new byte[is.available()];
             //noinspection ResultOfMethodCallIgnored
             is.read(buffer); // buffer is exactly the right size, a guarantee of asset files
             return new String(buffer);
         } catch (IOException e) {
             return null;
         }
     }
 
     private JREngage mEngage;
     private JRActivityObject mActivity;
 
     private Button mBtnTestAuth;
     private Button mBtnTestPub;
     private EditText mUrlEditText;
     private Button mBtnTestSpecificProvider;
 
     // Activity object variables
     private String mTitleText = "title text";
     private String mActionLink;
     private String mDescriptionText = "Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam " +
             "nonummy nibh euismod tincidunt ut laoreet dolore magna aliquam erat volutpat. Ut wisi enim ad " +
             "minim veniam, quis nostrud exerci tation ullamcorper suscipit lobortis nisl ut aliquip ex ea " +
             "commodo consequat. Duis autem vel eum iriure dolor in hendrerit in vulputate velit esse " +
             "molestie consequat, vel illum dolore eu feugiat nulla facilisis at vero eros et accumsan et " +
             "iusto odio dignissim qui blandit praesent luptatum zzril delenit augue duis dolore te feugait " +
             "nulla facilisi. Nam liber tempor cum soluta nobis eleifend option congue nihil imperdiet " +
             "doming id quod mazim placerat facer possim assum. Typi non habent claritatem insitam; est " +
             "usus legentis in iis qui facit eorum claritatem. Investigationes demonstraverunt lectores " +
             "legere me lius quod ii legunt saepius. Claritas est etiam processus dynamicus, qui sequitur " +
             "mutationem consuetudium lectorum. Mirum est notare quam littera gothica, quam nunc putamus " +
             "parum claram, anteposuerit litterarum formas humanitatis per seacula quarta decima et quinta " +
             "decima. Eodem modo typi, qui nunc nobis videntur parum clari, fiant sollemnes in futurum.";
     private String mImageUrl = "http://www.janrain.com/sites/default/themes/janrain/logo.png";
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         Log.d(TAG, "onCreate");
 
         setContentView(R.layout.main);
 
         if (!initEngage()) return;
         
         mBtnTestAuth = (Button)findViewById(R.id.btn_test_auth);
         mUrlEditText = (EditText) findViewById(R.id.share_url);
         mBtnTestSpecificProvider = (Button) findViewById(R.id.btn_test_specific_provider);
 
         mBtnTestAuth.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
 //                mEngage.setEnabledAuthenticationProviders(new String[]{"facebook"});
                 mEngage.showAuthenticationDialog(CustomUi.class);
             }
         });
 //        mBtnTestAuth.setOnLongClickListener(new View.OnLongClickListener() {
 //            public boolean onLongClick(View v) {
 //                if (findViewById(R.id.jr_signin_fragment) != null) {
 //                    mEngage.showSocialSignInFragment(
 //                            MainActivity.this,
 //                            com.janrain.android.engage.R.id.jr_publish_fragment,
 //                            false,
 //                            null,
 //                            null,
 //                            null,
 //                            null);
 //                }
 //
 //                return true;
 //            }
 //        });
 
         mBtnTestPub = (Button)findViewById(R.id.btn_test_pub);
         mBtnTestPub.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
 //                buildActivity();
 //                mEngage.showSocialPublishingDialog(mActivity);
                 JRConnectionManager.createConnection("https://login.qype.com", null, null);
             }
         });
         mBtnTestPub.setOnLongClickListener(new View.OnLongClickListener() {
             public boolean onLongClick(View v) {
                 if (findViewById(R.id.jr_publish_fragment) != null) {
                     buildActivity();
                     mEngage.showSocialPublishingFragment(
                             mActivity,
                             MainActivity.this,
                             com.janrain.android.engage.R.id.jr_publish_fragment,
                             false,
                             null,
                             null,
                             null,
                             null);
                 }
 
                 return true;
             }
         });
 
         mUrlEditText.setText(Prefs.getString(ACTION_LINK_KEY, "http://www.janrain.com/feed/blogs"));
         mUrlEditText.addTextChangedListener(new TextWatcher() {
             public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
 
             public void onTextChanged(CharSequence s, int start, int before, int count) {}
 
             public void afterTextChanged(Editable s) {
                 mActionLink = s.toString();
                 Prefs.putString(ACTION_LINK_KEY, mActionLink);
             }
         });
         
         mBtnTestSpecificProvider.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 mEngage.showAuthenticationDialog("facebook");
             }
         });
     }
 
     public static class CustomUi extends JRCustomUiConfiguration {
        public CustomUi() {
             mProviderListHeader = new CustomSignin();
 //            mAuthenticationBackgroundDrawable = c.getResources().getDrawable(R.drawable.custom_signin_bg);
             mProviderListTitle = "Custom title";
         }
 
         @Override
         public void onProviderListViewCreate(ListView providerListView) {
             super.onProviderListViewCreate(providerListView);
 //            providerListView.setDividerHeight(AndroidUtils.scaleDipToPixels(20));
         }
     }
 
     public static class CustomSignin extends JRCustomUiView {
         @Override
         public View onCreateView(Context context,
                 LayoutInflater inflater,
                 ViewGroup container,
                 Bundle savedInstanceState) {
             View v = inflater.inflate(R.layout.custom_signin_example, container, false);
             final EditText userName = (EditText) v.findViewById(R.id.username_edit);
             final EditText password = (EditText) v.findViewById(R.id.password_edit);
             Button signIn = (Button) v.findViewById(R.id.custom_signin_button);
             signIn.setOnClickListener(new View.OnClickListener() {
                 public void onClick(View v) {
                     Toast.makeText(getActivity(), "ZOMG CUSTOM SIGNIN!\n" + userName.getText() + "\n" + 
                             password.getText(), Toast.LENGTH_LONG).show();
                     finishJrSignin();
                 }
             });
 
             return v;
         }
     }
     
     private boolean initEngage() {
         String engageAppId = null;
         String engageTokenUrl = null;
         try {
             engageAppId = readAsset("app_id.txt").trim();
             engageTokenUrl = readAsset("token_url.txt").trim();
         } catch (NullPointerException e) {
             // Only check for app ID, token URL is optional
             if (engageAppId == null) {
                 new AlertDialog.Builder(this).setTitle("Configuration error")
                         .setMessage("You need to create assets/app_id.txt, then recompile and reinstall.")
                         .create().show();
                 return false;
             }
         }
 
         JREngage.sLoggingEnabled = true;
         mEngage = JREngage.initInstance(this, engageAppId, engageTokenUrl, mJREngageDelegate);
         if (mEngage == null) return false;
         
         return true;
     }
 
     private void buildActivity() {
         mActivity = new JRActivityObject("shared an article from the Janrain Blog!",
             mActionLink);
         //mActivity = new JRActivityObject("did a barrel roll!");
 
         mActivity.setTitle(mTitleText);
         mActivity.setDescription(mDescriptionText);
         mActivity.addMedia(new JRImageMediaObject(mImageUrl, "http://developer.android.com"));
 
         String smsBody = "Check out this article!\n" + mActionLink;
         String emailBody = mActionLink + "\n" + mDescriptionText;
 
         mActivity.addActionLink(new JRActionLink("test action", "http://android.com"));
 
         JRSmsObject sms = new JRSmsObject(smsBody);
         JREmailObject email = new JREmailObject("Check out this article!", emailBody);
         if (!TextUtils.isEmpty(mActionLink)) {
             sms.addUrl(mActionLink);
             email.addUrl(mActionLink);
         }
         mActivity.setEmail(email);
         mActivity.setSms(sms);
     }
 
     @Override
     protected void onStart() {
         super.onStart();
         Log.d(TAG, "onStart");
     }
 
     private JREngageDelegate mJREngageDelegate  = new JREngageDelegate() {
         public void jrEngageDialogDidFailToShowWithError(JREngageError error) {
             String message = "Simpledemo:\nJREngage dialog failed to show.\nError: " +
                     ((error == null) ? "unknown" : error.getMessage());
 
             Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
         }
 
         public void jrAuthenticationDidSucceedForUser(JRDictionary authInfo, String provider) {
             JRDictionary profile = (authInfo == null) ? null : authInfo.getAsDictionary("profile");
             String displayName = (profile == null) ? null : profile.getAsString("displayName");
             String message = "Authentication successful" + ((TextUtils.isEmpty(displayName))
                     ? "" : (" for user: " + displayName));
 
 //            Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
             showResultDialog(message);
         }
 
         public void jrAuthenticationDidReachTokenUrl(String tokenUrl,
                                                      HttpResponseHeaders response,
                                                      String tokenUrlPayload,
                                                      String provider) {
             org.apache.http.Header[] headers = response.getHeaders();
             org.apache.http.cookie.Cookie[] cookies = response.getCookies();
             String firstCookieValue = response.getHeaderField("set-cookie");
             showResultDialog("Token URL response", tokenUrlPayload);
         }
         
         private void showResultDialog(String title, String message) {
             (new AlertDialog.Builder(MainActivity.this)).setTitle(title)
                     .setMessage(message)
                     .setNeutralButton("OK", null)
                     .show();
         }
         
         private void showResultDialog(String title) {
             showResultDialog(title, null);
         }
 
         public void jrAuthenticationDidNotComplete() {
             showResultDialog("Authentication did not complete");
         }
 
         public void jrAuthenticationDidFailWithError(JREngageError error, String provider) {
             String message = ((error == null) ? "unknown" : error.getMessage());
 
             showResultDialog("Authentication Failed.", message);
         }
 
         public void jrAuthenticationCallToTokenUrlDidFail(String tokenUrl,
                                                           JREngageError error,
                                                           String provider) {
             showResultDialog("Failed to reach token URL");
         }
 
         public void jrSocialDidNotCompletePublishing() {
             showResultDialog("Sharing did not complete");
         }
 
         public void jrSocialDidCompletePublishing() {
             showResultDialog("Sharing did complete");
         }
 
         public void jrSocialDidPublishJRActivity(JRActivityObject activity, String provider) {
             Toast.makeText(MainActivity.this, "Activity shared", Toast.LENGTH_LONG).show();
         }
 
         public void jrSocialPublishJRActivityDidFail(JRActivityObject activity,
                                                      JREngageError error,
                                                      String provider) {
             Toast.makeText(MainActivity.this, "Activity failed to share", Toast.LENGTH_LONG).show();
         }
     };
 }
