 package tw.edu.ncu.weather;
 
 import java.util.ArrayList;
 import java.util.List;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.protocol.HTTP;
 import org.apache.http.util.EntityUtils;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.json.JSONArray;
 import android.app.Activity;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.webkit.WebView;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class NCUweatherActivity extends Activity {
 	private TextView data;
 	private Button refresh, getWeb;
 	private WebView web;
 	private HttpPost httprequest;
 	private List<NameValuePair> SendData;
 	private String HttpResponseText = "", uri = "http://jerry54010.webuda.com/get_data.php";
 	
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         data = (TextView) findViewById(R.id.result);
         refresh = (Button) findViewById(R.id.refresh);
         getWeb = (Button) findViewById(R.id.getWeb);
         web = (WebView) findViewById(R.id.web);
         web.getSettings().setDefaultTextEncodingName("Big5");
         
         data.setText("請點refresh");
         data.setText("");
         refresh.setOnClickListener(new OnClickListener(){
 			public void onClick(View v) {
 				//get data from web
 				Thread http = new Thread(new httpcon());
 				http.start();
 			}
         });
         
         getWeb.setOnClickListener(new OnClickListener(){
 			public void onClick(View v) {
 				web.loadUrl("http://pblap.atm.ncu.edu.tw/ncucwb/indexReal.asp");
 			}
         });
     }
 
     protected void onStop(){
     	super.onStop();
     	web.clearCache(true);
     	//Toast.makeText(this, "Cleared cache and Leave", Toast.LENGTH_SHORT).show();
     }
     
     // put http connection to thread
 	private class httpcon implements Runnable {
 		public void run() {
 			try {
 				SendData = new ArrayList<NameValuePair>();
 				SendData.add(new BasicNameValuePair("user_agent", "Android"));
 				httprequest = new HttpPost(uri);
 				
 				httprequest.setEntity(new UrlEncodedFormEntity(SendData, HTTP.UTF_8));
 				HttpResponse httpResponse = new DefaultHttpClient().execute(httprequest);
 
 				if (httpResponse.getStatusLine().getStatusCode() == 200) {
 					HttpResponseText = EntityUtils.toString(httpResponse
 							.getEntity());
 				}
 			} catch (Exception e) {
 				runOnUiThread(new Runnable() {
 					public void run() {
 						Toast.makeText(NCUweatherActivity.this, "沒有連線",Toast.LENGTH_LONG).show();
 						data.setText("沒有連線");
 					}
 				});
 			}
 			
 			runOnUiThread(new Runnable() {
 				public void run() {
 					if (HttpResponseText != null) {
 						String test="";
 						try {
							//bugggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggg
							//cannot to json
							
 							//JSONObject jsonObj = new JSONObject(HttpResponseText);
 							//jsonObj.getJSONObject("responseData").opt("translatedText");
 							JSONArray result = new JSONArray(HttpResponseText);
 							//取出陣列內所有物件
 							for(int i = 0;i < result.length(); i++)
 							{
 								//取出JSON物件
 								JSONObject stock_data = result.getJSONObject(i);
 								//取得物件內資料
 								test += "Title:"+stock_data.getString("Title")+"\n";
 								test += "Data:"+stock_data.getString("Data")+"\n";
 								test += "PP:"+stock_data.getString("PP")+"\n";
 							}
 						} catch (JSONException e) {
 							Toast.makeText(NCUweatherActivity.this, e.toString(),Toast.LENGTH_SHORT).show();
 							data.setText("沒有連線");
 						}
 						data.setText(test);
 					}
 					HttpResponseText = null;
 					SendData = null;
 				}
 			});
 		}
 	}
 }
