 package com.appygram.android.example;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.net.ssl.HttpsURLConnection;
 
 import android.app.Activity;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.TextView;
 
import com.google.gson.Gson;

 public class MainActivity extends Activity {
 
 	private final static String APPYGRAM_API_KEY = "your-api-key-here";
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 	private class SendAppygramTask extends AsyncTask<URL, Integer, Long> {
 		private Map<String,String> params;
 
 		private String join(List<String> aArr, String sSep) {
 			StringBuilder sbStr = new StringBuilder();
 			for (int i = 0, il = aArr.size(); i < il; i++) {
 				if (i > 0)
 					sbStr.append(sSep);
 				sbStr.append(aArr.get(i));
 			}
 			return sbStr.toString();
 		}
 
 		protected SendAppygramTask(Map<String,String> params){
 			super();
 			this.params = params;
 			params.put("api_key", APPYGRAM_API_KEY);
 		}
 
 		protected Long doInBackground(URL... urls) {
 			Long result = 0L;
 			try{
 				URL url = new URL("https://arecibo.appygram.com/appygrams");
 				HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
 				conn.setDoOutput(true);
 				conn.setRequestMethod("POST");
				conn.setRequestProperty("Content-Type", "application/json");
				String input = new Gson().toJson(params);
 				Log.i("Appygram Example","Sending: "+input);
 				OutputStream os = conn.getOutputStream();
 				os.write(input.getBytes());
 				os.flush();	
 				os.close();
 				result = (long) conn.getResponseCode();
 				Log.i("Appygram Example","Appygram sent with result "+result);
 			} catch (IOException x) {
 				Log.e("Appygram Example","Error sending appygram", x);
 			}
 			return result;
 		}
 
 		protected void onPostExecute(Long result) {
 			// TODO: show that something happened
 		}
 	}
 
 	private String getUIString(int id){
 		return ((TextView) findViewById(id)).getText().toString();
 	}
 
 	public void send(View source) {
 		// Collect parameters from the UI
 		Map<String, String> params = new HashMap<String, String>();
 		params.put("name", getUIString(R.id.editName));
 		params.put("email", getUIString(R.id.editEmail));
 		params.put("message", getUIString(R.id.editMessage));
 
 		// Send them to Appygram
 		new SendAppygramTask(params).execute();
 	}
 
 }
