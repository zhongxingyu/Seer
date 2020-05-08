 package jiunling.pass.push;
 
 import static jiunling.pass.config.config.RegisterUrl;
 import static jiunling.pass.config.config.RegisterWifiUrl;
 import static jiunling.pass.config.config.RenewWifiUrl;
 import static jiunling.pass.config.option.SleepTime;
 import static jiunling.pass.push.PushService.Register;
 import static jiunling.pass.push.PushService.RegisterWifi;
 import static jiunling.pass.push.PushService.Renew;
 
 import java.io.IOException;
 import java.util.List;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.protocol.HTTP;
 import org.apache.http.util.EntityUtils;
 
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 
 public class PushServer implements Runnable {
 	
 	/***	Debugging	***/
 	private static final String TAG = "PushServer";
 	private static final boolean D = true;
 	
 	private String Url;
 	
 	private Handler mHandler;
 	private int Kind;
 	private List<NameValuePair> mParams;
 	
 	public PushServer(int kind, List<NameValuePair> params, Handler handler ) {
 
 		ServerUrl(kind);
 		if(D)Log.e(TAG, "URL: "+ Url);
 		this.mHandler = handler;
 		this.Kind = kind;
 		this.mParams = params;
 		
 		Thread mThread = new Thread(this); // Thread
 		mThread.start();
 	}
 	
 	private void ServerUrl(int kind) {
 		switch(kind) {
 		case Register:
 			Url = RegisterUrl;
 			break;
 		case RegisterWifi:
 			Url = RegisterWifiUrl;
 			break;
 		case Renew:
 			Url = RenewWifiUrl;
 			break;
 		}
 	}
 	
 	@Override
 	public void run() {
 		// TODO Auto-generated method stub
 //		int Error = ServerHasError;
 		String result = "";
 		for(int i=1;i<=3;i++) {
 			HttpPost httpRequest = new HttpPost(Url);
 			try {
 				httpRequest.setEntity(new UrlEncodedFormEntity(mParams, HTTP.UTF_8));
 				HttpResponse mHttpResponse = new DefaultHttpClient().execute(httpRequest);
 				
 				if(mHttpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
 					result = EntityUtils.toString(mHttpResponse.getEntity());
 					if(D) Log.e(TAG, "-- result:"+result);
 //					Error = ServerNoError;
 					break;
 				}
 				Thread.sleep( SleepTime );
 			} catch (ClientProtocolException e) {
 				if(D) Log.e(TAG,"ERROR - ClientProtocolException");
 //				Error = ServerHasError;
 				e.printStackTrace();
 			} catch (IOException e) {
 				if(D) Log.e(TAG,"ERROR - IOException "+e);
 //				Error = ServerHasError;
 				e.printStackTrace();
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 //		if(D)Log.e(TAG, "Error: "+Error);
 //		sendMessage(result, Error);
 	}
 	
 	private void sendMessage(String result, int Error) {
 		Message m = Message.obtain(mHandler , Kind);  
 		m.arg1 = Error;
 		m.obj = (String) result;
 		
 		mHandler.sendMessage(m);
 	}
 }
