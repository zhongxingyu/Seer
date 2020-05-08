 package org.fNordeingang;
 
 // java
 import java.io.*;
 
 // android
 import android.app.Activity;
 import android.app.*;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ImageButton;
 import android.widget.Toast;
 import android.content.Context;
 import android.view.View.OnClickListener;
 import android.content.Intent;
 import android.widget.TextView;
 import android.widget.EditText;
 import android.text.method.PasswordTransformationMethod;
 // json
 import org.json.*;
 
 // http
 import org.apache.http.impl.client.*;
 import org.apache.http.client.*;
 import org.apache.http.HttpResponse;
 import de.mastacode.http.Http;
 
 public class fNordeingangActivity extends Activity implements OnClickListener {
     
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 		
 		// update label of fNordStatus
 		updatefNordStatusLabel();
         
         ImageButton tweetButton = (ImageButton)findViewById(R.id.fNordTweet);
         ImageButton doorButton = (ImageButton)findViewById(R.id.fNordDoor);
 		ImageButton statusButton = (ImageButton)findViewById(R.id.fNordStatus);
         tweetButton.setOnClickListener(this);
         doorButton.setOnClickListener(this);
 		statusButton.setOnClickListener(this);
     }
     
     public void onClick(View v) {
         int id = v.getId();
         if (id == R.id.fNordTweet) {
             // start fNordTweet
             this.startActivity(new Intent(this, fNordTweetActivity.class));
 		} else if (id == R.id.fNordStatus) {
 			// Status Action
 			togglefNordStatusDialog();
 			
         } else if (id == R.id.fNordDoor) {
             // Door Action here
             print("not yet implemented!");
         } else {
             // Error here
             print("Error: Unknown Button pressed!");
         }
     }
 	
 	// updates the fNordStatus label
 	public void updatefNordStatusLabel() {
 		int status = getfNordStatus();
 		TextView statusView = (TextView)findViewById(R.id.fNordStatusLabel);
 		switch (status) {
 			case 0:
 				statusView.setText(R.string.fNordStatusClosed);
 				break;
 			case 1:
 				statusView.setText(R.string.fNordStatusOpen);
 				break;
 			default: // on error (f.e. no internet connection) just display the label
 				statusView.setText(R.string.fNordStatus);
 				break;
 		}
 	}
 	
 	public int getfNordStatus() {
 		try {
 			// get json string
 			HttpClient client = new DefaultHttpClient();
 			String jsonstring = Http.get("http://fnordeingang.de:4242/status").use(client).asString();
 			
 			// get status
 			JSONObject status = new JSONObject(jsonstring);
 			if (status.getBoolean("open")) {
 				return 1; // open
 			} else {
 				return 0; // closed
 			}
 
 		} catch (IOException ioe) {
 			print(ioe.toString());
 			return -1;
 		} catch (JSONException jsone) {
 			print(jsone.toString());
 			return -1;
 		}
 	}
 	
 	public void togglefNordStatusDialog() {
 		
 		int status = getfNordStatus();
 		
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		if (status == 1) { // open
 			builder.setMessage("Do you want to close?");
 		} else if (status == 0) { // closed
 			builder.setMessage("Do you want to open?");
 		} else {
 			print("Error: couldn't get fNordStatus");
 			return;
 		}
 		
 		builder.setCancelable(false);
 		
 		// toggle fNordStatus at yes
 		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int id) {
 				togglefNordStatus();
 			}
 		});
 		
 		// cancel dialog at no
 		builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int id) {
 				dialog.cancel();
 			}
 		});
 		
 		AlertDialog dialog = builder.create();
 		if (status == 1) { // open
 			dialog.setTitle("fnord is open");
 		} else if (status == 0) { // closed
 			dialog.setTitle("fnord is closed");
 		}
 		
 		dialog.show();
 	}
 	
 	public void togglefNordStatus() {
 		
 		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
 		dialog.setMessage("Password:");
 		
 		// Set an EditText view to get user input
 		final EditText input = new EditText(this);
 		input.setTransformationMethod(new PasswordTransformationMethod());
 		dialog.setView(input);  
 		   
 		dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int whichButton) {
 				String password = input.getText().toString();
 				String tosend = "http://fnordeingang.de:4242/toggle/" + password;
 				
 				// send toggle command to webserver
 				try {
 					HttpClient client = new DefaultHttpClient();
 					String response = Http.post(tosend).use(client).asString();
 					
 					// get status
 					// if this throws a JSONException - no json object returned
 					// => maybe wrong password
 					JSONObject status = new JSONObject(response);
 					
 				} catch (IOException ioe) {
 					print(ioe.toString());
 					return;
 				} catch (JSONException jsone) {
 					print("Wrong Password?");
 					return;
 				}
 				
 				// update label of fNordStatus
 				updatefNordStatusLabel();
 			}
 		});
 		
 		dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int whichButton) {
 				dialog.cancel();
 			}
 		});  
 		   
 		dialog.show();
 	}
 	
 	// helper function
 	void print(String input) {
         Context context = getApplicationContext();
         CharSequence text = input;
         int duration = Toast.LENGTH_SHORT;
 		
         Toast toast = Toast.makeText(context, text, duration);
         toast.show();
     }
 
 }
