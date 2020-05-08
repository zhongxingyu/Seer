 package com.rucks.testlib;
 
 import android.app.Service;
 import android.content.ContentResolver;
 import android.content.SharedPreferences;
 import android.database.ContentObserver;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Handler;
 import android.widget.Toast;
 
 public class SMSListenerOut extends ContentObserver 
 {
 	private Service listeningService;
 	
 	private static int count = 0;
 	private static final int MESSAGE_TYPE_SENT = 2;
 	public SMSListenerOut(Handler handler, Service parentService) 
 	{
 		super(handler);
 		listeningService = parentService;
 	}
 	
 	@Override
     public void onChange(boolean selfChange) 
 	{
         super.onChange(selfChange);
 
         Uri uriSMSURI = Uri.parse("content://sms/");
         
         Cursor cur = listeningService.getContentResolver().query(uriSMSURI, null, null, null, null);
         
         if (cur.moveToNext()) 
         {
 			int type = cur.getInt(cur.getColumnIndex("type"));
 			// Only processing outgoing sms event & only when it
 			// is sent successfully (available in SENT box).
			if (/*protocol != null &&*/ type == MESSAGE_TYPE_SENT) 
 			{
				Toast.makeText(listeningService, "Detected outgoing SMS", Toast.LENGTH_LONG).show();
 				SharedPreferences countDiffs = listeningService.getSharedPreferences(TestLibMain.COUNT_DIFFS, 0);   	
             	long smsReceived = countDiffs.getLong(TestLibMain.SMS_SENT, 0);
             	SharedPreferences.Editor editor = countDiffs.edit();
             	editor.putLong(TestLibMain.SMS_SENT, ++smsReceived);
             	editor.commit();
 			}
 		}
         cur.close();
     }
 }
