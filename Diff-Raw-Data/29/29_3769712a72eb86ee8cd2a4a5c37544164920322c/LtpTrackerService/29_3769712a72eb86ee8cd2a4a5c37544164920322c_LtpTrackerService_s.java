 package fr.univsavoie.src.s4.ltp;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 
 import org.apache.http.HttpException;
 import org.apache.http.HttpHost;
 import org.apache.http.HttpRequest;
 import org.apache.http.HttpRequestInterceptor;
 import org.apache.http.auth.AuthScope;
 import org.apache.http.auth.AuthState;
 import org.apache.http.auth.Credentials;
 import org.apache.http.auth.UsernamePasswordCredentials;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.CredentialsProvider;
 import org.apache.http.client.methods.HttpPut;
 import org.apache.http.client.protocol.ClientContext;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.auth.BasicScheme;
 import org.apache.http.impl.client.BasicCredentialsProvider;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.protocol.ExecutionContext;
 import org.apache.http.protocol.HttpContext;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.widget.Toast;
 
 public class LtpTrackerService extends Service implements LocationListener {
 	
 	private LocationManager lm;
 	private DefaultHttpClient httpClient;
 	private JSONObject locationRequest;
 	private String login;
 	private String url; 
 
 	/* (non-Javadoc)
 	 * @see android.app.Service#onCreate()
 	 */
 	@Override
 	public void onCreate() {
 		super.onCreate();
 		lm = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
 		
         SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
         login = preferences.getString("ltp_login", "testuser1");
         String password = preferences.getString("ltp_password", "test");
         url = preferences.getString("ltp_url", "http://jibiki.univ-savoie.fr/ltp");
         
         HttpRequestInterceptor preemptiveAuth = new HttpRequestInterceptor() {
            @Override
             public void process( final HttpRequest request, final HttpContext context) throws HttpException, IOException {
                 AuthState authState = (AuthState) context.getAttribute(ClientContext.TARGET_AUTH_STATE);
                 CredentialsProvider credsProvider = (CredentialsProvider) context.getAttribute(
                         ClientContext.CREDS_PROVIDER);
                 HttpHost targetHost = (HttpHost) context.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
 
                 if (authState.getAuthScheme() == null) {
                     AuthScope authScope = new AuthScope(targetHost.getHostName(), targetHost.getPort());
                     Credentials creds = credsProvider.getCredentials(authScope);
                     if (creds != null) {
                         authState.setAuthScheme(new BasicScheme());
                         authState.setCredentials(creds);
                     }
                 }
             }
         };
 
         httpClient = new DefaultHttpClient();
 
         CredentialsProvider authCred = new BasicCredentialsProvider();
         Credentials creds = new UsernamePasswordCredentials(login, password);
         authCred.setCredentials(AuthScope.ANY, creds);
         
         httpClient.addRequestInterceptor(preemptiveAuth, 0);
         httpClient.setCredentialsProvider(authCred);
         
         try {
 			locationRequest = new JSONObject(this.getString(R.string.ltp_update_request));
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 
 	/* (non-Javadoc)
 	 * @see android.app.Service#onDestroy()
 	 */
 	@Override
 	public void onDestroy() {
 		Log.e("LTP", "arret du service");
 		lm.removeUpdates(this);
 		super.onDestroy();
 	}
 
 	/* (non-Javadoc)
 	 * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
 	 */
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		Log.e("LTP", "demarrage du service");
 		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,900000,500, this);
 		return Service.START_STICKY;
 	}
 
 	@Override
 	public IBinder onBind(Intent arg0) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
	@Override
 	public void onLocationChanged(Location location) {
 		Log.e("ltp", "location changed");
 		double lat = location.getLatitude();
 		double lon = location.getLongitude();
 		try {
 			locationRequest.getJSONObject("ltp").getJSONObject("gpx_file").put("@lat", lat).put("@lon", lon).put("@user", login);
 			Log.e("ltp", locationRequest.toString(2));
 			
 			HttpPut httpReq = new HttpPut(url+"/api/localisations/"+login+"?format=json");
 			httpReq.setEntity(new StringEntity(locationRequest.toString()));
 			httpClient.execute(httpReq);
 			
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (UnsupportedEncodingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ClientProtocolException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
	@Override
 	public void onProviderDisabled(String provider) {
 		// TODO Auto-generated method stub
 		
 	}
 
	@Override
 	public void onProviderEnabled(String provider) {
 		// TODO Auto-generated method stub
 		
 	}
 
	@Override
 	public void onStatusChanged(String provider, int status, Bundle extras) {
 		// TODO Auto-generated method stub
 		
 	}
 
 }
