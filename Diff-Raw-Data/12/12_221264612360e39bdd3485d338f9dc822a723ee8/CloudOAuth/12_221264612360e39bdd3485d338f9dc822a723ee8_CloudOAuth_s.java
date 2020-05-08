 package com.cloudappstudio.utility;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
import java.util.Iterator;
 import java.util.Map;
import java.util.Set;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.params.HttpClientParams;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.params.BasicHttpParams;
 
 import android.accounts.Account;
 import android.accounts.AccountManager;
 import android.accounts.AccountManagerFuture;
 import android.accounts.AuthenticatorException;
 import android.accounts.OperationCanceledException;
 import android.app.Activity;
 import android.os.Bundle;
 import android.util.Log;
 
 import com.cloudappstudio.data.CloudAuthId;
 
 public class CloudOAuth {
 	private CloudAuthId id;
 	
 	public CloudOAuth(CloudAuthId id) {
 		this.id = id;
 	}
 	
 	public CloudOAuth(Account account, Activity activity) throws OperationCanceledException, AuthenticatorException, IOException {
 		id = new CloudAuthId(account, getToken(activity, account));
 	}
 	
 	private String getToken(Activity activity, Account account) throws OperationCanceledException, AuthenticatorException, IOException{
 		AccountManager mgr = AccountManager.get(activity);
 		String authToken = null;
         AccountManagerFuture<Bundle> accountManagerFuture = mgr.getAuthToken(account, "oauth2:https://www.googleapis.com/auth/userinfo.email", null, activity, null, null);
         
         Bundle authTokenBundle = accountManagerFuture.getResult();
         authToken = authTokenBundle.get(AccountManager.KEY_AUTHTOKEN).toString();
         Log.v("authToken", authToken);
         return authToken;
 	}
 	
 	public String getContent(String apiUrl) throws IllegalStateException, IOException{	
 		// HttpClient (without following redirects!)
         DefaultHttpClient httpClient = new DefaultHttpClient();
         BasicHttpParams params = new BasicHttpParams();
         HttpClientParams.setRedirecting(params, false);
         httpClient.setParams(params);
 
         // Execute Http GET
         //https://cloudappstudio360.appspot.com/api/json/v2/apps.json?userInfoOAuth=ya29.AHES6ZT4Qh27Q8I1LK7onoQpxwNXw2Czp2Wndg8nk0TIzYc
         HttpGet httpget = new HttpGet(apiUrl + "?userInfoOAuth=" + id.getToken());
         HttpResponse response = httpClient.execute(httpget);
         Log.d("debug", ""+response.getStatusLine().getStatusCode());  
         return this.readTextFromHttpResponse(response);
         
 	}
 	
	@SuppressWarnings("rawtypes")
 	public String getContent(String apiUrl, Map<String, String> params) throws IllegalStateException, IOException{	
 		// HttpClient (without following redirects!)
         DefaultHttpClient httpClient = new DefaultHttpClient();
         BasicHttpParams httpParams = new BasicHttpParams();
         HttpClientParams.setRedirecting(httpParams, false);
         httpClient.setParams(httpParams);
 
         // Execute Http GET
         //https://cloudappstudio360.appspot.com/api/json/v2/apps.json?userInfoOAuth=ya29.AHES6ZT4Qh27Q8I1LK7onoQpxwNXw2Czp2Wndg8nk0TIzYc
         String webUrl = apiUrl + "?userInfoOAuth=" + id.getToken();
         
         for (Map.Entry<String, String> entry : params.entrySet()) {
 			webUrl += entry.getKey() + entry.getValue();
 		}
         
         Log.i("CLOUD", webUrl);
         
         HttpGet httpget = new HttpGet(webUrl);
         HttpResponse response = httpClient.execute(httpget);
         Log.d("debug", " "+response.getStatusLine().getStatusCode());  
         return this.readTextFromHttpResponse(response);
         
 	}
 	
 	private String readTextFromHttpResponse(HttpResponse response) throws IllegalStateException, IOException {
          BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
          StringBuffer sb = new StringBuffer();
          String line;
          while ((line = reader.readLine()) != null) {
                  sb.append(line);
          }
          reader.close();
          return sb.toString();
 	}
 	
 	public CloudAuthId getCloudAuthId() {
 		return id;
 	}
 }
