 package com.drewschrauf.example.robotronic;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.drewschrauf.robotronic.activities.RobotronicActivity;
 import com.drewschrauf.robotronic.threads.ThreadHandler;
 
 public class ExampleSimple extends RobotronicActivity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		// set the layout
 		setContentView(R.layout.simple);
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 
 		// get the image
 		ImageView image = (ImageView) findViewById(R.id.image);
 		getThreadHandler()
 				.makeImageDownloader(
						"https://raw.github.com/drewschrauf/robotronic/master/src/com/drewschrauf/example/robotronic/smiley.jpg",
 						image);
 
 		// get the text
 		final TextView text = (TextView) findViewById(R.id.text);
 		getThreadHandler()
 				.makeDataDownloader(
 						"https://raw.github.com/drewschrauf/robotronic/master/src/com/drewschrauf/example/robotronic/example.json",
 						new Handler() {
 
 							/**
 							 * Define a handler to deal with the retrieved data
 							 */
 							@Override
 							public void handleMessage(Message msg) {
 
 								// check the return code (msg.what) to see if it
 								// isData or isError
 								if (ThreadHandler.isData(msg.what)) {
 
 									// parse the data (msg.obj) as a String
 									try {
 										JSONObject feed = new JSONObject(
 												(String) msg.obj);
 										text.setText(feed.getString("text"));
 									} catch (JSONException e) {
 										Toast.makeText(ExampleSimple.this,
 												"Error parsing result",
 												Toast.LENGTH_LONG).show();
 									}
 								} else {
 
 									// deal with any errors that arose while
 									// retrieving data
 									Toast.makeText(ExampleSimple.this,
 											"Error retriving text",
 											Toast.LENGTH_LONG).show();
 								}
 							}
 						});
 	}
 }
