 package org.fNordeingang;
 
 // java
 import java.io.*;
 
 // android
 import android.app.*;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.Toast;
 import android.content.Context;
 import android.view.View.OnClickListener;
 import android.content.Intent;
 import android.widget.TextView;
 import android.widget.EditText;
 import android.text.method.PasswordTransformationMethod;
 import android.text.InputType;
 import android.os.Bundle;
 import android.os.Message;
 import android.os.Handler;
 
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
 		
 		// check for updates
 		(new CheckForUpdates(this)).check();
 		
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
             // maybe in future
             print("not yet implemented!");
         } else {
             // Error here
             print("Error: Unknown Button pressed!");
         }
     }
 	
 	// updates the fNordStatus label
 	public void updatefNordStatusLabel() {
 		updatefNordStatusLabelThread ufslt = new updatefNordStatusLabelThread(updatefNordStatusLabelHandler);
 		ufslt.start();
 	}
 	
     final Handler updatefNordStatusLabelHandler = new Handler() {
 		public void handleMessage(Message msg) {
 			int status = msg.getData().getInt("status");
 			TextView statusView = (TextView)findViewById(R.id.fNordStatusLabel);
 			ImageView imageView = (ImageView)findViewById(R.id.fNordStatusIcon);
 			switch (status) {
 				case 0:
 					statusView.setText(R.string.fNordStatusClosed);
					imageView.setImageResource(R.drawable.closed);
 					break;
 				case 1:
 					statusView.setText(R.string.fNordStatusOpen);
					imageView.setImageResource(R.drawable.open);
 					break;
 				default: // on error (f.e. no internet connection) just display the label
					statusView.setText(R.string.fNordStatus);
					imageView.setImageResource(R.drawable.unknown);
 					break;
 			}
 		}
 	};
 
 	private class updatefNordStatusLabelThread extends Thread {
 		Handler handler;
 		
 		updatefNordStatusLabelThread(Handler h) {
 			handler = h;
 		}
 		
 		public void run() {
 			// get status
 			int status = org.fNordeingang.fNordStatusInterface.getfNordStatus();
 			
 			// send status to main thread
 			Message msg = handler.obtainMessage();
 			Bundle b = new Bundle();
 			b.putInt("status", status);
 			msg.setData(b);
 			handler.sendMessage(msg);
 			
 		}
 	}
 	
 	
 	public void togglefNordStatusDialog() {
 		
 		int status = org.fNordeingang.fNordStatusInterface.getfNordStatus();
 		updatefNordStatusLabel();
 		Log.v("Status:",Integer.toString(status));
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		if (status == 1) { // open
 			builder.setMessage("Do you want to close?");
 		} else if (status == 0) { // closed
 			builder.setMessage("Do you want to open?");
 		} else if (status == -1) {
 			print("Error: IO or JSON Exception!");
 			return;
 		} else {
 			print("Error: couldn't get fNordStatus");
 			return;
 		}
 		
 		builder.setCancelable(false);
 		
 		// toggle fNordStatus at yes
 		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int id) {
 				startActivity(new Intent(fNordeingangActivity.this , fNordToggleActivity.class));
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
 	
 	
 	
 	// helper function
 	void print(String input) {
         Context context = getApplicationContext();
         CharSequence text = input;
         int duration = Toast.LENGTH_SHORT;
 		
         Toast toast = Toast.makeText(context, text, duration);
         toast.show();
     }
 
 }
