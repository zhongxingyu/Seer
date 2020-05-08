 package se.chalmers.chessfeud;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 
 import android.app.Activity;
 import android.os.Bundle;
 
 
 public class ServerTest extends Activity {
     
 	private HttpClient client;
 	private HttpPost httpPost;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		client = new DefaultHttpClient();
		httpPost = new HttpPost("servlet");
 		List pairs = new ArrayList();
 		String username = "twister";
 		String pass = "12345";
 		pairs.add(new BasicNameValuePair("username", username));
 		pairs.add(new BasicNameValuePair("password", pass));
 		try {
 			httpPost.setEntity(new UrlEncodedFormEntity(pairs));
 		} catch (UnsupportedEncodingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		try {
 			HttpResponse response = client.execute(httpPost);
 		} catch (ClientProtocolException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 	}
 }
