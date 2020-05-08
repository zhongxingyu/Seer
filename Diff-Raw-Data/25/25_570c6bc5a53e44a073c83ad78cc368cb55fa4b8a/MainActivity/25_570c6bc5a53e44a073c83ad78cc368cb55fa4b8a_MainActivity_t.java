 //Ronaldo Barnes
 //Java 1 week 3
 //January 2013
 //Full Sail University
 package com.barnes.ronaldo.javaapp1;
 
 import java.net.MalformedURLException;
 import java.net.URL;

 
 import com.rbarnes.lib.WebInterface;
 

 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Context;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 
 public class MainActivity extends Activity {
 
 	Context _context;
 	LinearLayout _thisLayout;
 	Boolean _connected = false;
 	EditText _inputField;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		_context = this;
 		_thisLayout = new LinearLayout(this);
 		
 		InputForm input = new InputForm(_context, "test", "test");
 		
 		//Detect form elements
 		_inputField = input.getField();
 		Button inputButton = input.getButton();
 		
 		//Detect button click
 		inputButton.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				Log.i("Click Handler",_inputField.getText().toString());
 				getLocations("cookies", "98404");
 			}
 		});
 		
 		
 		//Check network connection
 		_connected = WebInterface.getConnectionStatus(_context);
 		if(_connected){
 			Log.i("NETWORK CONNECTION", WebInterface.getConnectionType(_context));
 		}
 		
 		_thisLayout.addView(input);
 		
 		setContentView(_thisLayout);
 		
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 	
 	private void getLocations(String dessert, String zipCode){
		String baseUrl = "http://local.yahooapis.com/LocalSearchService/V3/localSearch?appid=qJIjRlbV34GJZfg2AwqSWVV03eeg8SpTQKy5PZqSfjlRrItt5hS2n3PIysdPU_CCIQlCGXIGjoTDESp3l42Ueic3O1EaYXU-&query="+dessert+"&zip="+zipCode+"&results=1&output=json";
 		URL finalURL;
 		try{
 			finalURL = new URL(baseUrl);
 			LocationRequest lr = new LocationRequest();
 			lr.execute(finalURL);
			
 		}catch(MalformedURLException e){
 			Log.e("BAD URL","MALFORMED URL");
 			finalURL = null;
 		}
 	}
 	
 	private class LocationRequest extends AsyncTask<URL, Void, String>{
 		@Override
 		protected String doInBackground(URL... urls){
 			String response = "";
 			
 			for(URL url: urls){
				
 				response = WebInterface.getUrlStringResponse(url);
 			}
 			return response;
 		}
 		
 		@Override
 		protected void onPostExecute(String result){
			Log.i("URL RESPONSE", result);
 		}
 	}
 
 }
