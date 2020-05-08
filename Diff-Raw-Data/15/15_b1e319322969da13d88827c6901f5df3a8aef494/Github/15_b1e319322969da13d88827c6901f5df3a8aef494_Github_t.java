 package org.trbecker.oauth2.samples.github;
 
import java.lang.reflect.Method;
 import java.net.URI;
 import java.net.URISyntaxException;
 
 import org.apache.http.client.HttpClient;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.trbecker.samples.salesforce.GitHubFlow;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.util.Log;
 
 public class Github {
     public static final int AUTHORIZATION_REQUEST_CODE = 0x001001;
     private static final String TAG = Github.class.getSimpleName();
     private GitHubFlow oauth2;
 
     public Github() {
         HttpClient connector = null;
         try {
             ClassLoader cl = getClass().getClassLoader();
             Class<?> clazz = cl.loadClass("android.net.http.AndroidHttpClient");
            Method newInstance = clazz.getMethod("newInstance",
                    new Class<?>[] { String.class });
            connector = (HttpClient) newInstance.invoke(clazz,
                    new Object[] { "android" });
         } catch (Exception e) {
             connector = new DefaultHttpClient();
         }
 
         Log.d(TAG, "Will use " + connector.getClass().getCanonicalName()
                 + " as the connector");
         oauth2 = GitHubFlow.getInstance(connector);
 
         try {
             oauth2.setRedirectUri(new URI("http://localhost/test/callback"));
         } catch (URISyntaxException e1) {
             e1.printStackTrace();
         }
         oauth2.setClientId("YOUR CLIENT ID");
         oauth2.setClientSecret("YOUR CLIENT SECRET");
     }
 
     public void authorize(final Activity activity) {
         oauth2.authorize(new Runnable() {
             public void run() {
                 activity.startActivityForResult(new Intent(activity,
                         AuthorizationActivity.class),
                         AUTHORIZATION_REQUEST_CODE);
             }
         });
 
     }
 }
