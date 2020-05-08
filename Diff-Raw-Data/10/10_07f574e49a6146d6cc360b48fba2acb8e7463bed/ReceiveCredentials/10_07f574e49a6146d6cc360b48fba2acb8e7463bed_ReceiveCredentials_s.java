 /**
  * 
  */
 package org.vimeoid;
 
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Context;
 import android.net.Uri;
 import android.util.Log;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 import org.vimeoid.connection.JsonOverHttp;
 import org.vimeoid.connection.VimeoApiUtils;
 import org.vimeoid.connection.VimeoConfig;
 import org.vimeoid.util.Dialogs;
 
 /**
  * <dl>
  * <dt>Project:</dt> <dd>vimeoid</dd>
  * <dt>Package:</dt> <dd>org.vimeoid</dd>
  * </dl>
  *
  * <code>ReceiveCredentials</code>
  *
  * <p>Description</p>
  *
  * @author Ulric Wilfred <shaman.sir@gmail.com>
  * @date Aug 31, 2010 9:04:48 PM 
  *
  */
 public class ReceiveCredentials extends Activity {
     
     public static final String TAG = "ReceiveCredentials";
     
     @Override
     protected void onResume() {
     	super.onResume(); 
         Uri uri = getIntent().getData(); // came here from browser with OAuth    
         Log.d(TAG, "Uri is " + uri);
         if (uri != null) {
             try {
                 Log.d(TAG, "Got credentials from browser, checking and saving");
                 // TODO: use AbstractAccountAuthenticator (see SampleSyncAdapter) to store credentials 
                 VimeoApiUtils.ensureOAuthCallbackAndSaveToken(uri,
                         getSharedPreferences(VimeoApiUtils.OAUTH_API_PREFERENCES_ID, Context.MODE_PRIVATE));
                 Log.d(TAG, "Checking finished");
                 
                 List<NameValuePair> params = new ArrayList<NameValuePair>();
                 params.add(new BasicNameValuePair("user_id", "shamansir"));
                params.add(new BasicNameValuePair("method", "vimeo.activity.happenedToUser"));                
                 JsonOverHttp.use().signedAskForObject(new URI(VimeoConfig.VIMEO_ADVANCED_API_ROOT), params);
             } catch (Exception e) {
                 Log.e(TAG, e.getLocalizedMessage());
                 e.printStackTrace();
                 Dialogs.makeExceptionToast(getApplicationContext(), "Failure: ", e);                
             }
         } else {
             Dialogs.makeToast(getApplicationContext(), "Failed to get OAuth token");
         }
     }
     
 }
