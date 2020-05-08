 package com.dailyinvention.wordpressthemechanger;
 
 import java.net.URI;
import java.util.HashMap;
 
 import org.xmlrpc.android.XMLRPCClient;
 import org.xmlrpc.android.XMLRPCException;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.util.Log;
 import android.widget.TextView;
 

 
 public class Client extends Activity {
     
     private XMLRPCClient client;
     private URI uri;
      
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_client);
          
         uri = URI.create("http://test-wp.apache.local/xmlrpc.php");
       client = new XMLRPCClient(uri);
          
       TextView TextViewtextView = (TextView) findViewById(R.id.text_view);
       TextViewtextView.setText(getDataMethod("admin","password"));
     }
      
     private String getDataMethod(String username, String password) {
         String text = "";
        Object[] result = null;
         Object[] params = {username, password};
         try {
            result = (Object[]) client.call("themes.getThemes",params);
             if (result != null) {
 				if (result != null) {
					for(Object o: result) {
						@SuppressWarnings("unchecked")
						HashMap<String, String> map = (HashMap<String, String>) o;
						String items[] = map.keySet().toArray(new String[0]);
						text = items[0];
			          
					}
 				}
             }
         } catch (XMLRPCException e) {
             Log.w("XMLRPC Test", "Error", e);
             text = "XMLRPC error" + e;
         }       
         return text;
     }
 }
