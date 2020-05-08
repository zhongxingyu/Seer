 package edu.gatech.oad.rocket.findmythings.util;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 
 /**
  * CS 2340 - FindMyStuff Android App
  * Extremely simple class for creating a quick error dialog 
  * @author TeamRocket
  */
 public class ErrorDialog {
 	private String message, buttonpos, buttonneg;
 		
 	/**
 	 * constructor1
 	 * @param message 
 	 */
 	public ErrorDialog(String message) {
 		this.message = message;
 		buttonpos = "Ok";
 		buttonneg = "Cancel";
 	}
 	
 	/**
 	 * constructor2
 	 * @param message
 	 * @param pos
 	 * @param neg
 	 */
 	public ErrorDialog(String message, String pos, String neg) {
 		this.message = message;
 		buttonpos = pos;
 		buttonneg = neg;
 	}
 	/**
 	 * Returns an AlertDialog with the message given in the constructor
 	 * @param context
 	 * @return
 	 */
 	public AlertDialog.Builder getDialog(Context context) {
 		AlertDialog.Builder noConnection = new AlertDialog.Builder(context);
 		noConnection.setMessage(message);
 		noConnection.setPositiveButton(buttonpos, new DialogInterface.OnClickListener() {
 		@Override
 		public void onClick(DialogInterface dialog, int id) {
             	//close	
             }
 		});
 		
 		return noConnection;
 	}
 	/**
 	 * Returns dialog with custom onclicklistener for OK button
 	 * @param context
 	 * @param temp
 	 * @return
 	 */
 	public AlertDialog.Builder getDialog(Context context, DialogInterface.OnClickListener pos) {
 		AlertDialog.Builder noConnection = new AlertDialog.Builder(context);
 		
 		noConnection.setMessage(message);
 		noConnection.setPositiveButton(buttonpos,pos);
 		
 		return noConnection;
 	}
 	/**
 	 * returns dialog with custom onclicklistener for ok and cancel buttons
 	 * @param context
 	 * @param pos 
 	 * @param neg
 	 * @return AlertDialog.Builder
 	 */
 	public AlertDialog.Builder getDialog(Context context, DialogInterface.OnClickListener pos, DialogInterface.OnClickListener neg) {
 		AlertDialog.Builder noConnection = new AlertDialog.Builder(context);
 		
 		noConnection.setMessage(message);
 		noConnection.setPositiveButton(buttonpos,pos);
 		noConnection.setNegativeButton(buttonneg,neg);
 		
 		return noConnection;
 	}
 	
 	/**
 	 * returns the message about error
 	 * @return message
 	 */
 	public String getMessage() {
 		return message;
 	}
	
	
 
 }
