 package app.merchantLocalization;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpPut;
 import org.apache.http.entity.ByteArrayEntity;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 import org.apache.http.protocol.BasicHttpContext;
 import org.apache.http.protocol.HttpContext;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.AlertDialog.Builder;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.LinearLayout.LayoutParams;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 
 
 
 /**
  * Customerlist.  Pulls subscribed or nearby customer names from the database. 
  * Need service-oriented architecture and needs three elements: 
  * external database, web-service, mobile web-service client. 
  * @author Chihiro
  * 
  * 
  * Notes:
  * For connection to work, Apache server must be handled to start PHP. 
  * Also, make sure NAU Wi-Fi is connected on the device. 
  * 
  * IP address changes for each Wi-Fi access! 
  *
  */
 
 @SuppressLint("NewApi")
 public class Customers extends Activity {
 	/** Called when the activity is first created. */
 
 	TextView username;
 	TextView result; 
 	
 	String dbResult; 
 	
 	LinearLayout customerLayout;
 	
 	Customers currentThis = this; 
 	
 	static int TIMEOUT_MILLISEC = 3000; 
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		setContentView(R.layout.customers);
 		
 		customerLayout = (LinearLayout)findViewById(R.id.customerLayout);
 		
 		getData(); 
 	}
 
 	/**
 	 * Connect to webservice (database) 
 	 */
 	public void getData() {
 		new LongRunningGetIO().execute(); 
 	}
 
 	private class LongRunningGetIO extends AsyncTask <Void, Void, String> {
 
 		protected String getASCIIContentFromEntity(HttpEntity entity) throws IllegalStateException, IOException {
 			InputStream in = entity.getContent();
 			StringBuffer out = new StringBuffer();
 			int n = 1;
 			while (n>0) {
 				byte[] b = new byte[4096];
 				n =  in.read(b);
 				if (n>0) out.append(new String(b, 0, n));
 			}
 			return out.toString();
 		}
 
 		@Override
 		protected String doInBackground(Void... params) {
 			HttpClient httpClient = new DefaultHttpClient();
 			HttpContext localContext = new BasicHttpContext();
 
 			// can't use localhost, since localhost refers to the device itself 
 			// (in this case the android device being tested). 
 			// Use 10.0.2.2 for emulator 
 			// Use own IP for device: 192.168.0.9
 			// cd C:\Windows\System32
 			// ipconfig 
 			// Look at 10.1.64.169
 			//HttpGet httpGet = new HttpGet("http://10.1.64.169/PHPQuery.php");
 			HttpGet httpGet = new HttpGet("http://dana.ucc.nau.edu/~cs854/PHPGetNearbyCustomers.php");
 			String text = null;
 			try {
 
 				HttpResponse response = httpClient.execute(httpGet, localContext);
 				HttpEntity entity = response.getEntity();
 				text = getASCIIContentFromEntity(entity);
 
 			} catch (Exception e) {
 				return e.getLocalizedMessage();
 			}
 			return text;
 		}	
 
 		protected void onPostExecute(final String results) {
 			if (results!=null) {
 				
 				runOnUiThread(new Runnable() {
 					
 					@Override
 					public void run() {
 						// EditText et = (EditText)findViewById(R.id.databaseText);
 						// et.setText("Database connection worked!: " + results);
 						/**
 						 * Gets customers names from JSON and creates buttons that charge the users if 
 						 * pressed.
 						 */
 						try {							
 							JSONArray jsonArray = new JSONArray(results);
 
 								for (int i = 0; i < jsonArray.length(); i++){
 									Button tempButton = new Button(currentThis);
 									final String customerName = jsonArray.getJSONObject(i).getString("userName");
 									tempButton.setText(customerName);
 									tempButton.setOnClickListener(new View.OnClickListener(){
 
 										public void onClick(View arg0) {
 												AlertDialog.Builder builder = new AlertDialog.Builder(currentThis);
												builder.setMessage("Are you sure you want to charge " + customerName + "?")
 											       .setTitle("Charge Customer");
 												builder.setPositiveButton("Charge", new DialogInterface.OnClickListener() {
 											           public void onClick(DialogInterface dialog, int id) {
 											               // User clicked OK button
 											        	   try{
 											        	   JSONObject json = new JSONObject();
 															json.put("userName", customerName);
 															HttpParams httpParams = new BasicHttpParams();
 													        HttpConnectionParams.setConnectionTimeout(httpParams,
 													                TIMEOUT_MILLISEC);
 													        HttpConnectionParams.setSoTimeout(httpParams, TIMEOUT_MILLISEC);
 													        HttpClient client = new DefaultHttpClient(httpParams);
 													        //
 													        //String url = "http://10.0.2.2:8080/sample1/webservice2.php?" + 
 													        //             "json={\"UserName\":1,\"FullName\":2}";
 													        String url = "http://dana.ucc.nau.edu/~cs854/PHPToggleCustomerNotification.php";
 
 													        HttpPost request = new HttpPost(url);
 															request.setEntity(new ByteArrayEntity(json.toString().getBytes(
 																        "UTF8")));														
 													        request.setHeader("json", json.toString());
 													        HttpResponse response;
 															response = client.execute(request);
 													        HttpEntity entity = response.getEntity();
 											        	   
 											           } catch (Throwable t) {
 													        //Toast.makeText(this, "Request failed: " + t.toString(),
 													        //        Toast.LENGTH_LONG).show();
 													    }
 											        	    Builder builder2 = new AlertDialog.Builder(currentThis); 
 												    		builder2.setMessage("You have successfully charged " + customerName + "." );
 												    		builder2.setCancelable(false); 
 												    		builder2.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 														           public void onClick(DialogInterface dialog, int id) {
 														               // User clicked OK button
 														           }}); 
 												    		AlertDialog dialog2 = builder2.create();
 												    		dialog2.show();
 											           }
 											       });
 											builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 											           public void onClick(DialogInterface dialog, int id) {
 											               // User cancelled the dialog
 											           }
 											       });
 										        // If the response does not enclose an entity, there is no need
 											AlertDialog dialog = builder.create();
 											dialog.show();
 										}
 									});
 									LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
 									customerLayout.addView(tempButton,lp);
 								}
 							
 							
 						} catch (JSONException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 											
 						
 						/*adapter = new ArrayAdapter<String>(currentThis, 
 								android.R.layout.simple_list_item_1, listContents); 
 						adapter.setNotifyOnChange(true); 
 						myListView.setAdapter(adapter); */
 						
 					}
 				});
 				
 			} else {
 				//TODO: Error notification of some sort 
 				//EditText et = (EditText)findViewById(R.id.databaseText);
 				//et.setText("Database connection failed");
 			}
 		}
 	}
 }
