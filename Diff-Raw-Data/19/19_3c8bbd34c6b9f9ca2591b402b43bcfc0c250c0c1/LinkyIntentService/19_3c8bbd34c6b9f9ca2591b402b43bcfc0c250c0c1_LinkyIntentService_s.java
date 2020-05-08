 package com.hn.linky;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 
 import android.annotation.SuppressLint;
 import android.app.IntentService;
 import android.app.PendingIntent;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Bitmap.CompressFormat;
 import android.net.Uri;
 import android.os.Environment;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Vibrator;
 import android.preference.PreferenceManager;
 import android.provider.MediaStore;
 import android.provider.MediaStore.Images.Media;
 import android.telephony.PhoneNumberUtils;
 import android.telephony.SmsManager;
 import android.telephony.TelephonyManager;
 import android.util.Log;
 import android.widget.Toast;
 
 import com.bugsense.trace.BugSenseHandler;
 import com.hn.linky.valueobjects.Constants;
 import com.hn.linky.valueobjects.ISharedPreferences;
 import com.hn.linky.GsmAlphabet;
 
 public class LinkyIntentService extends IntentService implements ISharedPreferences
 {
 	public static final String TAG = "LinkyIntentService";
 	
 	private final IBinder mBinder = new LocalBinder<LinkyIntentService>(this);
 	private final int VIBRATION_DURATION = 250;
 	private String mLinkedNumber;	
 	private Handler mHandler;	
 	private String mMessage;	
 		
 	public LinkyIntentService() 
 	{
 		super("LinkyIntentService");		
 	}	
 
 	@Override
 	public void onCreate() 
 	{	
 		super.onCreate();
 		
 		mHandler = new Handler();		
 		TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
 		setSourceNumber(telephonyManager.getLine1Number());		
 		mLinkedNumber = getLinkedNumber();
 		
 		BugSenseHandler.setup(this, "abc580a4");
 	}	
 	
 	@Override
 	public IBinder onBind(Intent intent) 
 	{
 		//return super.onBind(intent);
 		//return new LocalBinder<LinkyIntentService>(this);
 		return mBinder;
 	}
 	
 	private String getLinkedNumber()
     {
         SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
         return sharedPreferences.getString(SHARED_PREF_LINKED_NUMBER, null);
     }
     
     private void setSourceNumber(String phoneNumber)
     {
         SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
         SharedPreferences.Editor editor = sharedPreferences.edit();
         editor.putString(SHARED_PREF_SOURCE_NUMBER, phoneNumber);
         editor.commit();    
     }
 	
     public void updateDrunkLevel(int drunkLevel)
     {
     	mMessage = "Drunk Level: " + drunkLevel + "!";
     	sendMessage(mMessage);
     }
     
     public void updateSleepyLevel(int sleepyLevel)
     {
     	mMessage = "Sleepy Level: " + sleepyLevel + "!";
     	sendMessage(mMessage);
     }
     
     public void updateMwahLevel(int mwahLevel)
     {
     	mMessage = "Mwah Level: " + mwahLevel + "!";
     	sendMessage(mMessage);
     }
     
     public void updateHuggleLevel(int huggleLevel)
     {
     	mMessage = "Huggle Level: " + huggleLevel + "!";
     	sendMessage(mMessage);
     }
     
     public void updateAllLevels(int drunkLevel, int sleepyLevel, int mwahLevel, int huggleLevel)
     {
     	mMessage = "Drunk: " + drunkLevel + "! Sleepy: " + sleepyLevel + "! Mwahs: " + mwahLevel + "! Huggles: " + huggleLevel + "!";
     	sendMessage(mMessage);
     }
     
     public void sendBuzz()
     {   
     	try
     	{
     		mMessage = Constants.BUZZ_KEY;
         	PendingIntent pendingIntent = PendingIntent.getService(this, 0, new Intent(this, LinkyIntentService.class), 0);
             SmsManager smsManager = SmsManager.getDefault();
             smsManager.sendTextMessage(mLinkedNumber, null, mMessage, pendingIntent, null);
     	}
     	catch (Exception e)
     	{
     		Log.e(TAG, "Buzz Failed.", e);
     	}    	
     	        	
         vibrate();
         displayToast("BUZZING " + mLinkedNumber + "!");
     	    	
     }    
 	
 	@Override
 	protected void onHandleIntent(Intent intent) 
 	{
 		if (intent.getAction() != null)
 		{
 			String action = intent.getAction();
 
 			if (action.equals(Constants.ACTION_SEND_WIDGET_POKE))
 			{
 				mMessage = "*poke!* (w)";
 		    	sendMessage(mMessage);
 			}
 			else if (action.equals(Constants.ACTION_SEND_WIDGET_HUGGLE))
 			{
 				mMessage = "*huggle!* (w)";
 		    	sendMessage(mMessage);
 			}
 			else if (action.equals(Constants.ACTION_SEND_WIDGET_MWAH))
 			{
 				mMessage = "*mwah!* (w)";
 		    	sendMessage(mMessage);
 			}
 			else if (action.equals(Constants.ACTION_SEND_BUZZ))
 			{
 				sendBuzz();
 			}
 			else if (action.equals(Constants.ACTION_AUTHENTICATE_BUZZ))
             {
                 String message = intent.getStringExtra(Constants.EXTRA_SMS_MESSAGE);
                 String origin = intent.getStringExtra(Constants.EXTRA_ORIGINATING_ADDRESS);
                 if (isBuzzAuthorized(message))
                 {
                     buzzSelf(true, origin);
                 }               
             }
 			else if (action.equals(Constants.ACTION_FORWARD_SMS))
 			{
 			    String message = intent.getStringExtra(Constants.EXTRA_SMS_MESSAGE);
                 String origin = intent.getStringExtra(Constants.EXTRA_ORIGINATING_ADDRESS);
                 forwardSms(message, origin);          
 			}
 			else if (action.equals(Constants.ACTION_INSERT_SMS))
             {
                 String message = intent.getStringExtra(Constants.EXTRA_SMS_MESSAGE);
                 String origin = intent.getStringExtra(Constants.EXTRA_ORIGINATING_ADDRESS);
                 insertSms(message, origin);      
                 //broadcastSmsReceived(this.getApplicationContext(), message, origin);
             }
 		}
 	}
     
 	private void insertSms(String message, String originNumber)
     {
 	    try 
         {  
             // store the sent sms in the inbox folder
             ContentValues values = new ContentValues();
             values.put("address", originNumber);
             values.put("body", message);
             this.getContentResolver().insert(Uri.parse("content://sms/inbox"), values);
             
             //displayToast(message);
             vibrate();
         } 
         catch (Exception e) 
         {
             Log.e(TAG, "SMS Outgoing failed.", e);            
         }   
     }
     
 	private void broadcastSmsReceived(Context context, String body, String sender) 
 	{
         byte [] pdu = null ;
         byte [] scBytes = PhoneNumberUtils.networkPortionToCalledPartyBCD( "0000000000" );
         byte [] senderBytes = PhoneNumberUtils.networkPortionToCalledPartyBCD(sender);
         int lsmcs = scBytes.length;
         byte [] dateBytes = new byte [ 7 ];
         Calendar calendar = new GregorianCalendar();
         dateBytes[ 0 ] = reverseByte(( byte ) (calendar.get(Calendar.YEAR)));
         dateBytes[ 1 ] = reverseByte(( byte ) (calendar.get(Calendar.MONTH) + 1 ));
         dateBytes[ 2 ] = reverseByte(( byte ) (calendar.get(Calendar.DAY_OF_MONTH)));
         dateBytes[ 3 ] = reverseByte(( byte ) (calendar.get(Calendar.HOUR_OF_DAY)));
         dateBytes[ 4 ] = reverseByte(( byte ) (calendar.get(Calendar.MINUTE)));
         dateBytes[ 5 ] = reverseByte(( byte ) (calendar.get(Calendar.SECOND)));
         dateBytes[ 6 ] = reverseByte(( byte ) ((calendar.get(Calendar.ZONE_OFFSET) + 
                 calendar.get(Calendar.DST_OFFSET)) / ( 60 * 1000 * 15 )));
         try 
         {
             ByteArrayOutputStream bo = new ByteArrayOutputStream();
             bo.write(lsmcs);
             bo.write(scBytes);
             bo.write( 0x04 );
             bo.write(( byte ) sender.length());
             bo.write(senderBytes);
             bo.write( 0x00 );
             bo.write( 0x00 );  // encoding: 0 for default 7bit
             bo.write(dateBytes);
             
             try 
             {
                 byte[] bodybytes  = GsmAlphabet.stringToGsm7BitPacked(body);
                 bo.write(bodybytes);
             } 
             catch(Exception e) 
             {
                 //Do nothing
             }
 
             pdu = bo.toByteArray();
         } 
         catch (IOException e) 
         {
             //Do nothing
         }
 
         Intent intent = new Intent();
         intent.setAction("android.provider.Telephony.SMS_RECEIVED");
         intent.setClassName("com.android.mms", "com.android.mms.transaction.SmsReceiverService");
         intent.putExtra("pdus", new Object[] {pdu});
         intent.putExtra("format", "3gpp");
         
         context.startService(intent);
 	}
 
     private byte reverseByte( byte b) {
             return ( byte ) ((b & 0xF0 ) >> 4 | (b & 0x0F ) << 4 );
     }
     
     private void forwardSms(String message, String originNumber)
     {
         try 
         {   
             String messageToSend = "[LINKY](" + originNumber + ")" + message;
             
             PendingIntent pendingIntent = PendingIntent.getService(this, 0, new Intent(this, LinkyIntentService.class), 0);
             SmsManager smsManager = SmsManager.getDefault();
             smsManager.sendTextMessage(mLinkedNumber, null, messageToSend, pendingIntent, null);
             
             // store the sent sms in the sent folder
             ContentValues values = new ContentValues();
             values.put("address", mLinkedNumber);
             values.put("body", messageToSend);
             this.getContentResolver().insert(Uri.parse("content://sms/sent"), values);
             
             displayToast(message);
             //vibrate();
         } 
         catch (Exception e) 
         {
             Log.e(TAG, "SMS Outgoing failed.", e);            
         } 
     }
 	
     private void sendMessage(String message)
     {
         try 
         {	
         	PendingIntent pendingIntent = PendingIntent.getService(this, 0, new Intent(this, LinkyIntentService.class), 0);
         	SmsManager smsManager = SmsManager.getDefault();
         	smsManager.sendTextMessage(mLinkedNumber, null, message, pendingIntent, null);
         	
         	// store the sent sms in the sent folder
         	ContentValues values = new ContentValues();
         	values.put("address", mLinkedNumber);
         	values.put("body", message);
         	this.getContentResolver().insert(Uri.parse("content://sms/sent"), values);
         	
         	displayToast(message);
         	//vibrate();
         } 
         catch (Exception e) 
         {
             Log.e(TAG, "SMS Outgoing failed.", e);            
         } 
     }
     
     private boolean isBuzzAuthorized(String message)
     {    
     	if (message.contentEquals(Constants.BUZZ_KEY))
     	{
     		return true;
     	}    	
     	else
     	{
     		return false;
     	}
     }
 
     private void buzzSelf(boolean shouldBuzz, String originNumber)
     {    
     	if (shouldBuzz)
     	{	
     		int dot = 50;      
     		int dash = 125;     
     		int short_gap = 50;
     		int medium_gap = 125;
     		int long_gap = 500;
     		long[] pattern = {
     		    2000,  // Start immediately
     		    dash, medium_gap, 
     		    dot, short_gap, dot, short_gap, dash, medium_gap, dot, long_gap, 
     		    dash, short_gap, dash
     		};
     		
     		Vibrator vibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
     		vibrator.vibrate(pattern, -1);
     		
     		displayToast("BUZZ from " + originNumber + "!", Toast.LENGTH_LONG);
     	}    	
     }
     
     private void vibrate()
     {
 		Vibrator vibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
 		vibrator.vibrate(VIBRATION_DURATION);
     }
     
     private void displayToast(String message)
     {
     	mHandler.post(new DisplayToast(message));
     }
     
     private void displayToast(String message, int duration)
     {
     	mHandler.post(new DisplayToast(message, duration));
     }
         
 	private class DisplayToast implements Runnable
 	{
 		  String mText;
 		  int mDuration = Toast.LENGTH_SHORT;
 
 		  public DisplayToast(String text)
 		  {
 			  mText = text;
 		  }
 		  
 		  public DisplayToast(String text, int duration)
 		  {
 			  mText = text;
 			  mDuration = duration;
 		  }
 
 		  public void run()
 		  {
 			  Toast.makeText(LinkyIntentService.this, mText, mDuration).show();
 		  }
 	}
 	
 	public void sendInstapic(Uri instapicUri)
     {
 	    Intent intent = new Intent(Intent.ACTION_SEND); 
 	    intent.putExtra("address", mLinkedNumber);
         intent.putExtra("sms_body", "Linky Instapic!");
         intent.putExtra(Intent.EXTRA_STREAM, instapicUri); 
         intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
         intent.setType("image/png"); 
         startActivity(intent);
         
         addPicToGallery(instapicUri);
     }
 	
     private void addPicToGallery(Uri uri) 
     {
         Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
         mediaScanIntent.setData(uri);
         this.sendBroadcast(mediaScanIntent);
     }
 }
