 package com.example.virtual_graffiti;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.ListView;
 
 import com.parse.FindCallback;
 import com.parse.Parse;
 import com.parse.ParseAnalytics;
 import com.parse.ParseException;
 import com.parse.ParseGeoPoint;
 import com.parse.ParseObject;
 import com.parse.ParseQuery;
 
 public class MainActivity extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		Parse.initialize(this, "f09jNI2OB82MUvz0iHP8X8ZkKFgjnsC03IGHW240", "s5BkfxQRp75PUNjlKRIew7XieumzS0CcZMBdeIwF");
 		ParseAnalytics.trackAppOpened(getIntent());
 		
 		// Test Parse SDK functionality
 		ParseObject testObject = new ParseObject("TestObject");
 		testObject.put("foo", "bar");
 		testObject.saveInBackground();
 		
 		// set up the messageViewer
 //		ListView messages = (ListView)findViewById(R.id.listViewMessages);
 		
 		
 		getMessages();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 	
 	public void submitMessage(View view) {
 		// get user's coordinates
 		ParseGeoPoint geoPoint = new ParseGeoPoint();
 		// etc
 		
 		// get user's message
 		EditText etMessage = (EditText)findViewById(R.id.editTextInput);
 		String message = etMessage.getText().toString();
 		
 		// Ignore if the input is blank
 		if (message.length() == 0) return;
 		
 		// create and store geomessage
 		ParseObject geoMessage = new ParseObject("GeoMessage");
 		geoMessage.put("geoPoint", geoPoint);
 		geoMessage.put("message", message);
 		geoMessage.saveInBackground();
 		
 		// clear input
 		etMessage.setText("");
 		
 		/*// put message in GeoMessage viewer
 		EditText etMessageViewer = (EditText)findViewById(R.id.editTextMessages);
 		etMessageViewer.getText().insert(0, message + "\n");*/
 	}
 	
 	public void getMessages(View view) {
 		getMessages();
 	}
 	
 	public void getMessages() {
 		// get user's current location
 		ParseGeoPoint currentLocation = new ParseGeoPoint();
 		
 		// Get messages within 50 meters of the current location
 		ParseQuery query = new ParseQuery("GeoMessage");
 		query.whereWithinKilometers("geoPoint", currentLocation, 1000);
 //		query.setLimit(10);
 		query.findInBackground(new FindCallback() {
 			public void done(List<ParseObject> objects, ParseException e) {
 				if (e == null) {
 					populateMessageView(objects);
 				} else {
 					displayError();
 				}
 			}
 		});
 	}
 	
 	public void populateMessageView(List<ParseObject> messages) {
 		ListView messageViewer = (ListView)findViewById(R.id.listViewMessages);
 //		messageViewer.setText("Loaded " + messages.size() + ":\n");
 		
 		ArrayList<String> msgList = new ArrayList<String>();
 		
 		for (ParseObject msg : messages) {
 			msgList.add(msg.getString("message"));
 		}
 		
 		ArrayAdapter<String> adapter =
 				new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, msgList);
 		messageViewer.setAdapter(adapter);
 		
 		/*for (ParseObject msg : messages) {
 			messageViewer.getText().append(msg.getString("message") + "\n");
 		}*/
 		
 	}
 	
 	public void displayError() {
 		/*EditText messageViewer = (EditText)findViewById(R.id.editTextMessages);
 		messageViewer.setText("Something went wrong...");*/
 	}
 
 
 }
 
