 package com.mehmetakiftutuncu.textinstead.activities;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnCancelListener;
 import android.content.DialogInterface.OnClickListener;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.widget.Toast;
 
 import com.mehmetakiftutuncu.textinstead.Constants;
 import com.mehmetakiftutuncu.textinstead.R;
 
 /**
  * Activity for showing the question dialog
  * 
  * @author Mehmet Akif Ttnc
  */
 public class QuestionActivity extends Activity
 {
 	/** Tag for logging */
 	public static final String DEBUG_TAG = "TextInstead_QuestionActivity";
 	
 	/** Cancel listener of the dialog */
 	private OnCancelListener mDialogCancelListener = new OnCancelListener()
 	{
 		@Override
 		public void onCancel(DialogInterface dialog)
 		{
 			Log.d(DEBUG_TAG, "Cancelling upon user request...");
 			
 			finish();
 		}
 	};
 	
     @Override
     protected void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         
         // Get intent extras
         Bundle extras = getIntent().getExtras();
         
         // If there are some extras
         if(extras != null)
         {
         	// Get name and the number of the last call
         	final String name = extras.getString(Constants.EXTRA_NAME);
         	final String number = extras.getString(Constants.EXTRA_NUMBER);
         	
         	Log.d(DEBUG_TAG, "Sent information [Name: " + name + ", Number: " + number + "]");
         	
         	AlertDialog dialog;
         	AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
         	
         	Log.d(DEBUG_TAG, "Creating question dialog...");
         	
         	// Start preparing dialog
     		dialogBuilder.setTitle(getString(R.string.dialog_title));
     		dialogBuilder.setMessage(getQuestion(name, number));
     		dialogBuilder.setPositiveButton(getString(R.string.dialog_writeMessage), new OnClickListener()
     		{
     			@Override
     			public void onClick(DialogInterface dialog, int which)
     			{
     				Log.d(DEBUG_TAG, "Going to send an sms to [Name: " + name + ", Number: " + number + "]");
     				
     				QuestionActivity.sendSms(QuestionActivity.this, number, "");
     			}
     		});
     		dialogBuilder.setNegativeButton(getString(R.string.dialog_cancel), new OnClickListener()
     		{
     			@Override
     			public void onClick(DialogInterface dialog, int which)
     			{
     				dialog.cancel();
     			}
     		});
     		dialogBuilder.setNeutralButton(getString(R.string.dialog_selectMessage), new OnClickListener()
     		{
 				@Override
 				public void onClick(DialogInterface dialog, int which)
 				{
 					Log.d(DEBUG_TAG, "Reading the list of custom messages...");
 					
 					// Read defined custom messages
 					ArrayList<String> messages = new ArrayList<String>();
 					
 					for(String i : Constants.PREFERENCE_MESSAGES_LIST)
 					{
 						String j = PreferenceManager.getDefaultSharedPreferences(QuestionActivity.this).getString(i, "");
 						
 						if(!j.equals(""))
 						{
 							messages.add(j);
 						}
 					}
 					
 					// There are defined messages
 					if(messages.size() > 0)
 					{
 						Log.d(DEBUG_TAG, "Creating list of messages dialog...");
 						
 						final CharSequence[] items = new CharSequence[messages.size()];
 						for(int i = 0; i < messages.size(); i++)
 						{
 							items[i] = messages.get(i);
 						}
 						
 						AlertDialog listDialog;
 			        	AlertDialog.Builder listDialogBuilder = new AlertDialog.Builder(QuestionActivity.this);
 			        	
 			        	listDialogBuilder.setTitle(getString(R.string.listDialog_title));
 			        	listDialogBuilder.setItems(items, new OnClickListener()
 			        	{
 							@Override
 							public void onClick(DialogInterface dialog, int which)
 							{
 								Log.d(DEBUG_TAG, "Going to send an sms to [Name: " + name + ", Number: " + number + ", Message: " + items[which] + "]");
 			    				
 								QuestionActivity.sendSms(QuestionActivity.this, number, items[which].toString());
 							}
 						});
 			        	
 			        	listDialog = listDialogBuilder.create();
 			        	listDialog.setOnCancelListener(mDialogCancelListener);
 			        	listDialog.show();
 					}
 					else
 					{
 						// No messages, show toast, start message activity
 						
 						Toast.makeText(QuestionActivity.this, getString(R.string.listDialog_error), Toast.LENGTH_LONG).show();
 						
 						Log.d(DEBUG_TAG, "Going to send an sms to [Name: " + name + ", Number: " + number + "]");
 	    				
 						QuestionActivity.sendSms(QuestionActivity.this, number, "");
 					}
 				}
 			});
    		dialogBuilder.setIcon(R.drawable.app_icon);
     		
     		dialog = dialogBuilder.create();
     		dialog.setOnCancelListener(mDialogCancelListener);
     		dialog.show();
         }
     }
     
     /**
      * Starts an sms activity with provided information
      * 
      * @param context Context of the activity
      * @param number Number to send the message
      * @param message Message to be sent
      */
     private static void sendSms(Context context, String number, String message)
     {
     	// Create an intent for sending an sms to a specified number
 		Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse(Constants.URI_SMS + number));
 		
 		// Optional, text to send
 		intent.putExtra(Constants.URI_SMS_BODY, message);
 		
 		((Activity) context).finish();
 		context.startActivity(intent);
     }
     
     /**
      * Makes a human readable question using name and the number given<br><br>
      * 
      * Format: Name (Number)
      * 
      * @param name		Name of the person
      * @param number	Phone number of the person
      * 
      * @return A formatted human readable question
      */
     private String getQuestion(String name, String number)
     {
     	return String.format(getString(R.string.dialog_message, ((name != null && !name.equals("")) ? name : getString(R.string.unknown_name)), number));
     }
 }
