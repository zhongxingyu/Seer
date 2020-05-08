 package com.example.homeautomation;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 import android.view.View;
 import android.widget.TextView;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONObject;
 
 public class MainActivity extends Activity {
 
     private HttpClient client = new DefaultHttpClient();
     private boolean locked;
     private TextView isLocked;
     private TextView isUnlocked;
     private TextView problem;	
 	
 	@Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);        
         try {
 			sendGet();
 		} catch (Exception e) {
 			System.out.println(e.toString());
 		}
         if(locked==true){
        	isLocked = (TextView)findViewById(R.id.locked_message);
         	isLocked.setVisibility(View.VISIBLE);        	
         }
         else if(locked==false){
        	isUnlocked = (TextView)findViewById(R.id.unlocked_message);
         	isUnlocked.setVisibility(View.VISIBLE);
         }
         else{
        	problem = (TextView)findViewById(R.id.problem_message);
         	problem.setVisibility(View.VISIBLE);
         }
         
     }
 
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
     
     /** Called when the user clicks the Key button */
     public void buttonPress(View view) throws Exception {
         if(locked==true)
         	sendPost("unlock");
         else if(locked==false)
         	sendPost("lock");
         else
         	System.out.println("Boolean not instantiated.");
     }
     
     private void sendGet() throws Exception{
         String url = "http://10.0.1.4:8888";
         HttpGet getRequest = new HttpGet(url);
         
         HttpResponse response = client.execute(getRequest);
         
         System.out.println("GET Request sent.");
         System.out.println("Response Code: "+response.getStatusLine().getStatusCode());
         
         BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
         StringBuffer result = new StringBuffer();
         String line = "";
         while((line = reader.readLine())!=null)
         	result.append(line);
         JSONObject jsonObject = new JSONObject(result.toString());
         
         if(jsonObject.get("status").equals("locked")){
         		locked = true;
         		System.out.println("Door is locked.");
         }
         else if(jsonObject.get("status").equals("unlocked")){
         		locked = false;
         		System.out.println("Door is unlocked.");
         }
         else{
     		System.out.println("JSONObject Format Error");
     		problem.setVisibility(View.VISIBLE);
     	}
     }
     
     private void sendPost(String cmd) throws Exception{
     	String url = "http://10.0.1.4:8888/locker";
     	HttpPost postRequest = new HttpPost(url);
     	
     	List<NameValuePair> things = new ArrayList<NameValuePair>();
     	things.add(new BasicNameValuePair("cmd",cmd));
     	things.add(new BasicNameValuePair("message",""));
     	things.add(new BasicNameValuePair("status",""));
     	
     	postRequest.setEntity(new UrlEncodedFormEntity(things));
     	
     	HttpResponse response = client.execute(postRequest);
     	
     	System.out.println("POST Request sent.");
     	System.out.println("Post parameters: "+postRequest.getEntity());
     	System.out.println("Response Code: "+response.getStatusLine().getStatusCode());
     	
     	BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
     	StringBuffer result = new StringBuffer();
     	String line = "";
     	while((line = reader.readLine())!=null)
     		result.append(line);
     	JSONObject jsonObject = new JSONObject(result.toString());
     	
     	if(jsonObject.get("status").equals("locked")){
     		locked = true;
     		isUnlocked.setVisibility(View.GONE);
     		isLocked.setVisibility(View.VISIBLE);
     		System.out.println("Door has been locked.");
     	}
     	else if(jsonObject.get("status").equals("unlocked")){
     		locked = false;
     		isLocked.setVisibility(View.GONE);
     		isUnlocked.setVisibility(View.VISIBLE);
     		System.out.println("Door has been unlocked.");
     	}
     	else{
     		System.out.println("JSONObject Format Error");
     		problem.setVisibility(View.VISIBLE);
     	}
     }
    
     
 }
