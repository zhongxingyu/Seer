 package com.cs456.a2;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.os.AsyncTask;
 import android.os.Handler;
 
 /**
  * An abstract base class containing common methods and data for both the server and client sockets
  * @author Janson
  *
  */
 public abstract class SocketBase extends AsyncTask {
 	protected final int SOCKET_PORT = 62009; // The port we are connecting on
 	protected final String EXIT_MESSAGE = "Goodbye Cruel World"; // The exit message for the protocol
 	protected final String START_MESSAGE = "Hello Cruel World"; // The initialization message for the protocol
 	protected final String SEND_FILE_LIST_MESSAGE = "File List Cruel World"; // The request for the file list message for the protocol
 	protected final String END_FILE_LIST_MESSAGE = "No More File List Cruel World"; // A message indicating the transfer of the file list is complete
 	
 	protected Handler handler = new Handler(); // A common handler used to post handle new runnables
 	
 	protected Context context;
 	
 	protected boolean shouldQuit = false;
 	
 	@Override
 	protected abstract Object doInBackground(Object... params); // Must be implemented in derived classes
     
     public void killThread() {
     	this.shouldQuit = true;
    	handleKillThread();
     }
     
     // Creates a dialog box stating there was an error, and prints the text which the calling code provides it
     protected void handleError(final String error) {
     	handler.post(new Runnable() {
 			
 			@Override
 			public void run() {
 				AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
 				alertBuilder.setMessage(error);
 				alertBuilder.setCancelable(false);
 				
 				alertBuilder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int id) {
 						dialog.cancel();
 					}
 				});
 				
 				AlertDialog alert = alertBuilder.create();
 				alert.setTitle("Error!");
 				alert.show();
 			}
 		});
 	}
     
     protected abstract void handleKillThread();
     
     protected boolean hasQuit() {
     	return this.shouldQuit;
     }
 }
