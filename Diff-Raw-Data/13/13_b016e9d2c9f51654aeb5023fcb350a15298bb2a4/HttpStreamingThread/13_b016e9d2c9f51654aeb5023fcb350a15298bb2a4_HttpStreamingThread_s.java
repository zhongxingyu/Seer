 package net.djmacgyver.bgt.upstream;
 
 import java.io.IOException;
 
 import net.djmacgyver.bgt.R;
 import net.djmacgyver.bgt.http.HttpClient;
 
 import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.StringEntity;
 
 import android.content.Context;
 
 public class HttpStreamingThread extends Thread {
 	private Context context;
 	private HttpClient client;
 	private boolean terminate = false;
 	private StreamingHttpEntity entity;
 	private String userName;
 	private String password;
 	
 	public HttpStreamingThread(Context context, String userName, String password) {
 		this(context);
 		this.userName = userName;
 		this.password = password;
 	}
 
 	public HttpStreamingThread(Context context) {
 		this.context = context;
 	}
 
 	private HttpClient getClient() {
 		if (client == null) {
 			client = new HttpClient(this.context);
 		}
 		return client;
 	}
 	
 	private synchronized StreamingHttpEntity getEntity() {
 		if (entity == null) {
 			entity = new StreamingHttpEntity(this);
 		}
 		return entity;
 	}
 	
 	@Override
 	public void run() {
 		while (!terminate) {
 			try {
 				if (userName != null && password != null) {
 					HttpPost login = new HttpPost(context.getResources().getString(R.string.base_url) + "login");
 					login.setEntity(new StringEntity("user=" + userName + "&pass=" + password));
 					HttpResponse res = getClient().execute(login);
 					if (res.getStatusLine().getStatusCode() != 200) {
 						System.out.println("login failed");
 						terminate();
 						continue;
 					}
 					res.getEntity().consumeContent();
 				}
 				
 				HttpPost req = new HttpPost(context.getResources().getString(R.string.base_url) + "log");
 				req.setEntity(getEntity());
 				getClient().execute(req).getEntity().consumeContent();
				//entity = null;
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
 			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
 			}	
 			entity = null;
 		}
 		getClient().getConnectionManager().shutdown();
 	}
 	
 	public synchronized void sendData(String data) {
 		getEntity().sendData(data);
 	}
 	
 	public synchronized void sendData(byte[] data) {
 		getEntity().sendData(data);
 	}
 	
 	public void terminate() {
 		terminate = true;
 		getEntity().terminate();
 		interrupt();
 	}
 }
