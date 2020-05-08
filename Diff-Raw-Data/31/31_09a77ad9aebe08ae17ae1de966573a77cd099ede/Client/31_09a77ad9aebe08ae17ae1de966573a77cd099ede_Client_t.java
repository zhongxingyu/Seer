 package com.dailyinvention.wordpressthemechanger;
 
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
 
 import org.xmlrpc.android.XMLRPCClient;
 import org.xmlrpc.android.XMLRPCException;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.util.Log;
 import android.widget.TextView;
 
 
 
 public class Client extends Activity {
     
      
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_client);
          
        //uri = URI.create("http://test-wp.apache.local/xmlrpc.php");
          
       TextView TextViewtextView = (TextView) findViewById(R.id.text_view);
       TextViewtextView.setText(getDataMethod());
     }
      
     private String getDataMethod() {
         String text = "";
 
         try {
        	XMLRPCClient client = new XMLRPCClient(new URL("http://test-wp.apache.local/xmlrpc.php"));
            HashMap<String,Object> result = (HashMap<String,Object>) client.call("themes.getThemes","admin","password");
            for (String key : result.keySet()){
            	text = key;
            }
             
         } catch (XMLRPCException e) {
             Log.w("XMLRPC Test", "Error", e);
             text = "XMLRPC error" + e;
        } catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}       
         return text;
     }
 }
