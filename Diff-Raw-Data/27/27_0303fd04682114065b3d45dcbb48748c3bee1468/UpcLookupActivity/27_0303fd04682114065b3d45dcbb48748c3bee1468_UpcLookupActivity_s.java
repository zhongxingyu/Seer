 package com.UPCLookup.com;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.text.util.Linkify;
 import android.util.Log;
 import android.widget.TextView;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.json.JSONTokener;
 
 
 public class UpcLookupActivity extends Activity {
 	
 	private static final String TAG = "MyActivity";
 	private static final int INFO = 4;
 	
 	private static final String base_url = "http://www.searchupc.com/handlers/upcsearch.ashx?request_type=3&access_token=2C7851AA-DF85-4B0E-AF2A-6CDDCF503159&upc=";
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
     	
     	
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         Intent sender = getIntent();
         String upcData = sender.getExtras().getString("upc");
         
         httpRetriever values = new httpRetriever();
         
         //024100440801
         //set the url with the upc entered by the user
         
         String url = base_url + upcData;
         //call the method in the web service from upcsearch.com
         
         String str = values.callWebService(url);
         
         try {
         	
         	JSONObject object = (JSONObject) new JSONTokener(str).nextValue();
         	
         	String productName = "";
         	
         	if(object.length() == 0 ){
         		productName = "Product not found";
         	}else {
         		JSONObject product = new JSONObject(object.getString("0"));
        		productName = product.getString("productname");
         	}
         	
         	TextView pname = new TextView(this);
         	pname.setText(productName);            
         	/*
         	TextView tv = new TextView(this);
             tv.setText("http://www.google.com");
             Linkify.addLinks(tv , Linkify.WEB_URLS);
             */
             setContentView(pname);
             
 		} catch (JSONException e) {
 			Log.getStackTraceString(e);
 		}
         
         //Log.println(INFO,TAG, str); 
     }
 }
