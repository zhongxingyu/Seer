 package uk.ac.sussex.asegr3.tracker.client.ui;
 
 import java.net.MalformedURLException;
 import java.net.URISyntaxException;
 import java.util.concurrent.Executor;
 
 import uk.ac.sussex.asegr3.tracker.client.service.login.LoginGrantedListener;
 import uk.ac.sussex.asegr3.tracker.client.service.login.LoginService;
 import uk.ac.sussex.asegr3.tracker.client.sytem.NetworkInfoProvider;
 import uk.ac.sussex.asegr3.tracker.client.transport.HttpTransportClientApiFactory;
 import uk.ac.sussex.asegr3.tracker.client.util.Logger;
 import uk.ac.sussex.asegr3.transport.beans.Base64Encoder;
 import android.app.Activity;
 import android.content.Context;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.util.Base64;
 
 public class LoginServiceFactory {
 
	private static final String DEFAULT_HOSTNAME = "ec2-54-247-19-238.eu-west-1.compute.amazonaws.com:5050";
 
 	
 	public LoginService create(LoginGrantedListener loginGrantedListener, Activity activity, Executor executor, Logger logger) throws MalformedURLException, URISyntaxException{
 		return create(DEFAULT_HOSTNAME, loginGrantedListener, activity, executor, logger, new AndroidBase64Encoder());
 	}
 	
 	public LoginService create(String hostname, LoginGrantedListener loginGrantedListener, Activity activity, Executor executor, Logger logger, Base64Encoder encoder) throws MalformedURLException, URISyntaxException{
 		HttpTransportClientApiFactory apiFactory = HttpTransportClientApiFactory.create(hostname, logger, createNetworkInfoProvider(activity), encoder);
 		
 		LoginService loginService = new LoginService(loginGrantedListener, executor, apiFactory, logger);
 
 		return loginService;
 	}
 	private NetworkInfo getNetworkInfo(Activity activity) {
 		 ConnectivityManager connectivityManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
 		 return connectivityManager.getActiveNetworkInfo();
 	}
 	
 	private NetworkInfoProvider createNetworkInfoProvider(final Activity activity){
 		return new NetworkInfoProvider(){
 
 			@Override
 			public NetworkInfo getNetworkInfo() {
 				return LoginServiceFactory.this.getNetworkInfo(activity);
 			}
 			
 		};
 	}
 	
 	private static class AndroidBase64Encoder implements Base64Encoder {
 
 		@Override
 		public String encode(byte[] bytes) {
 			return Base64.encodeToString(bytes, Base64.DEFAULT);
 		}
 		
 	}
 }
