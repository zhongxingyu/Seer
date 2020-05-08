 package com.huskysoft.eduki;
 
 import com.example.eduki.R;
 
 import android.net.*;
 import android.os.*;
 import android.app.Activity;
 import android.view.Menu;
 import android.widget.TextView;
 import android.view.View;
 import android.content.Context;
 
 import java.io.*;
 import java.net.*;
 
 
 public class MainActivity extends Activity {
    
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
 	//This is bad, these two lines should be removed before any further development
 	StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
 	StrictMode.setThreadPolicy(policy); 
	
 	setTextContent();
     }
    
    
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
    
     private void setTextContent() {
 	String textToDisplay = "";
 	if(isOnline()) {
 	    try {
 		URL eduki_api = new URL("http://eduki.herokuapp.com/api");
 		BufferedReader in = new BufferedReader(new InputStreamReader(eduki_api.openStream()));
 		String inputLine = in.readLine();
 		while(inputLine != null) {
 		    textToDisplay += inputLine;
 		    inputLine = in.readLine();
 		}
 	    } catch (Exception e) {
 		textToDisplay = e.toString();//"Connection error";
 	    }
 	} else {
 	    textToDisplay = "No internet connection";
 	}
 	TextView text = (TextView) findViewById(R.id.mainActivityTextBox);
 	text.setText(textToDisplay);
     }
     
     private boolean isOnline() {
 	ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 	NetworkInfo netInfo = cm.getActiveNetworkInfo();
 	if(netInfo != null && netInfo.isConnectedOrConnecting()) {
 	    return true;
 	}
 	return false;
    }   
 }
