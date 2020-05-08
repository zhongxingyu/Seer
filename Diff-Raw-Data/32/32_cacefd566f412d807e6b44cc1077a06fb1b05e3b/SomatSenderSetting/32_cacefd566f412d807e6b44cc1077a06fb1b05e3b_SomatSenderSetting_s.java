 package id.co.mondial.android.somatsender;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import android.app.Activity;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.Toast;
 
 public class SomatSenderSetting extends Activity implements Runnable {
 	
 	public static final String PREFS_NAME = "id.co.mondial.android.somatsender.config";
 	public static String apikey = "";
 	
 	@Override
     public void onCreate(Bundle savedInstanceState) {
 		EditText apikeyEditText;
 		
         super.onCreate(savedInstanceState);
         setContentView(R.layout.setting);
         
         SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
         apikey = settings.getString("apikey", "");
         
         apikeyEditText = (EditText)findViewById(R.id.apikey);
         apikeyEditText.setText(apikey);
 
 	}
 	
 	@Override
 	public void onStop() {
 		EditText apikeyEditText;
 		
 		super.onStop();
 		
 		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
 		SharedPreferences.Editor editor = settings.edit();
 		
 		apikeyEditText = (EditText)findViewById(R.id.apikey);
         apikey = apikeyEditText.getText().toString();
 		editor.putString("apikey", apikey);
 		editor.commit();
 	}
 	
 	public void testApikey(View view) {
 		Thread thread = new Thread(this);
 		thread.start();
 	}
 
 	public void run() {
 		EditText apikeyEditText;
 		apikeyEditText = (EditText)findViewById(R.id.apikey);
         apikey = apikeyEditText.getText().toString();
 
 		HttpClient httpclient = new DefaultHttpClient();
 		HttpResponse response = null;
 
 		try {
 			String url = "http://sms.mondial.co.id/rest/v4/check_apikey.php?apikey=" + apikey;
 			HttpGet getmethod = new HttpGet(url);
 
 			response = httpclient.execute(getmethod);
 
 			String result = "";
 			 
 			BufferedReader reader = new BufferedReader(
 				new InputStreamReader(
 						response.getEntity().getContent()
 				)
 			);
 			 
 			String line = null;
 			while ((line = reader.readLine()) != null){
 			  result += line + "\n";
 			}
 			
 			showToast(result, Toast.LENGTH_LONG);
 
 		} catch(Exception e) {
 			showToast("Connection failed");
 		}
 
 	}
 
     private void showToast(String msg, int length) {
     	final String _msg = msg;
     	final int _length = length;
 
     	runOnUiThread(new Runnable() {
     		public void run() {
 	            Toast.makeText(
 						SomatSenderSetting.this, 
 						_msg,
 						_length
 					).show();
     		}
     	});
 
     }
 
     private void showToast(String msg) {
     	showToast(msg, Toast.LENGTH_SHORT);
     }
 	
 }
