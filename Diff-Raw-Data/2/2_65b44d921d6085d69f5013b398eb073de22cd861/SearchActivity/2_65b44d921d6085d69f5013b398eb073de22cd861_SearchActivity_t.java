 package com.example.test2app;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.URL;
 
 import android.os.Bundle;
 import android.os.StrictMode;
 import android.app.Activity;
 import android.content.Intent;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.TextView;
 
 public class SearchActivity extends Activity {
 EditText inputBox;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);
 		
 		inputBox = (EditText)findViewById(R.id.searchEditText);
 		
 		inputBox.setOnKeyListener(
 			new View.OnKeyListener() {
 
 				@Override
 				public boolean onKey(View v, int keyCode, KeyEvent event) {
 					if(keyCode == 66){
 						getData(inputBox.getText().toString());
 						return true;
 					}	
 					return false;
 				}		
 			});
 	}
 	
 	public void getData(String inputValue) {
 	    try {
 	        StrictMode.ThreadPolicy policy = new StrictMode.
 	          ThreadPolicy.Builder().permitAll().build();
 	        StrictMode.setThreadPolicy(policy); 
 	        URL url = new URL("http://directory.uci.edu/index.php?uid=" + inputValue + "&form_type=plaintext");
 	        HttpURLConnection con = (HttpURLConnection) url
 	          .openConnection();
 	        TextView httptextview = (TextView)findViewById(R.id.httpTextView);
 	        httptextview.setText(readStream(con.getInputStream()));
 	        //readStream(con.getInputStream());
 	    } catch (Exception e) {
 	        e.printStackTrace();
 	    }
 	}     
 
 	private String readStream(InputStream in) {
 	  BufferedReader reader = null;
 	  String output = "";
 	  try {
 	    reader = new BufferedReader(new InputStreamReader(in));
 	    String line = "";
 	    while ((line = reader.readLine()) != null) {
 	      output+=line;
 	    }
 	    return output;
 	  } catch (IOException e) {
 	    e.printStackTrace();
 	  } finally {
 	    if (reader != null) {
 	      try {
 	        reader.close();
 	      } catch (IOException e) {
 	        e.printStackTrace();
 	      }
 	    }
 	  }
 	return output;
 	} 
 	
 	public void goToMap(View view) { 
 		Intent intent = new Intent(this,MainActivity.class);
 		startActivity(intent);
 	}
  
 	public void goToEmergencyInfo(View view) { 
 		Intent intent = new Intent(this,EmergencyInfoActivity.class);
 		startActivity(intent);
 	}
  
 	public void goToEmergencyDialer(View view) { 
 		Intent intent = new Intent(this,DialerActivity.class);
 		startActivity(intent);
 	}
 	
 	 public void goToSearch(View view) { 
 			Intent intent = new Intent(this,SearchActivity.class);
 			startActivity(intent);
 		}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.search, menu);
 		return true;
 	}
 }
