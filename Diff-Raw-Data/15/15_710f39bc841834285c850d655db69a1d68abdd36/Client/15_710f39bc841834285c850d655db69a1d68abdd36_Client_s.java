 package com.dailyinvention.wordpressthemechanger;
 
 import java.net.URI;
 
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
       TextViewtextView.setText(getDataMethod());
     }
      
     private String getDataMethod() {
         String text = "";
         //Object[] params = {username, password};
         try {
            String result = (String) client.call("themes.getThemes","admin","Uncledat03");
            
            	text = result;
                
            
            
            
           
            
            
             
         } catch (XMLRPCException e) {
             Log.w("XMLRPC Test", "Error", e);
             text = "XMLRPC error" + e;
         }       
         return text;
     }
 }
