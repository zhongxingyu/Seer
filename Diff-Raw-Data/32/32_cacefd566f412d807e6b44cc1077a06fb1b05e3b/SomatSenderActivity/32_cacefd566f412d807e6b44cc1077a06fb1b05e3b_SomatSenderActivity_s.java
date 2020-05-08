 package id.co.mondial.android.somatsender;
 
 
 import org.apache.http.HttpResponse;
 import org.apache.http.StatusLine;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.protocol.HTTP;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.Toast;
 
 public class SomatSenderActivity extends Activity  implements Runnable {
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.main, menu);
         return true;
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
         	case R.id.setting:
             
 	        	Intent settingIntent = new Intent(this, SomatSenderSetting.class);
 	            startActivity(settingIntent);                        
 	            
 	            return true;
 	        
         	case R.id.about:
 	        	
 	        	Intent aboutIntent = new Intent(this, SomatSenderAbout.class);
 	        	startActivity(aboutIntent);
 	
 	        	return true;
 
 	        default:
                 return super.onOptionsItemSelected(item);
         }
     }
 
     
     public void sendSms(View view) {
     	Thread thread = new Thread(this);
     	thread.start();
     	
     }
     
     private void showToast(String msg, int length) {
     	final String _msg = msg;
     	final int _length = length;
 
     	runOnUiThread(new Runnable() {
     		public void run() {
 	            Toast.makeText(
 						SomatSenderActivity.this, 
 						_msg,
 						_length
 					).show();
     		}
     	});
 
     }
 
     private void showToast(String msg) {
     	showToast(msg, Toast.LENGTH_SHORT);
     }
     
     public void run() {
     	showToast("Sending message");
     	EditText _destination = (EditText)findViewById(R.id.destination);
     	String destination = _destination.getText().toString();
     	
     	EditText _message = (EditText)findViewById(R.id.message);
     	String message = _message.getText().toString();
     	
     	SharedPreferences settings = getSharedPreferences(SomatSenderSetting.PREFS_NAME, 0);
         String apikey = settings.getString("apikey", "");
     	
         String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
         xml +="<smses>";
 		xml += "<apikey>" + apikey + "</apikey>";
 		xml += "<sms>";
 		xml += "<destination>";
 		xml += "<to>" + destination + "</to>";
 		xml += "</destination>";
 		xml += "<message>" + message + "</message>";
 		xml += "</sms>";
 		xml += "</smses>";
 
 		HttpClient httpclient = new DefaultHttpClient();
 		HttpResponse response = null;
 		
 		String url = "http://sms.mondial.co.id/rest/v4/sms.php";
 		HttpPost postmethod = new HttpPost(url);
 		
 		try {
 			
 			StringEntity se = new StringEntity(xml,HTTP.UTF_8);
 			postmethod.setEntity(se);
			postmethod.setHeader("User-Agent", "Somat Sender Android Client");
 			response = httpclient.execute(postmethod);
 			StatusLine statusLine = response.getStatusLine();
 			showToast("Message sent");
 			showToast(statusLine.getStatusCode() + " " + statusLine.getReasonPhrase());
 		} catch (Exception e) {
 			showToast("Send failed");
 		}	
 
     }
     
 }
