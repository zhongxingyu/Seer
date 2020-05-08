 package org.booncode.android.loco;
 
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.content.BroadcastReceiver;
 import android.telephony.SmsMessage;
 import android.util.Log;
 
 public class MsgReceiver extends BroadcastReceiver
 {
   public static final String RECEIVED_ACTION = "android.provider.Telephony.SMS_RECEIVED";
   public static final String LOCO_CMD_START = "@+";
   public static final String LOCO_CMD_LOCATE = LOCO_CMD_START + "locate";
   public static final String LOCO_CMD_VIEW_POSITION = LOCO_CMD_START + "geo:";
  public static final String LOCO_CMD_VIEW_CELLS = LOCO_CMD_START + "cell-";
   
   protected static final String TAG = "loco.MsgReceiver";
   private Context m_context = null;
   
   
   protected boolean processMessage(SmsMessage msg)
   {
     String text = msg.getMessageBody();
     if (text != null)
     {
       Log.d(TAG, "Inspecting Message...");
       
       if (text.startsWith(LOCO_CMD_START))
       {
         String number = Utils.formatTelephoneNumber(msg.getOriginatingAddress());
         if (number != null)
         {
           StalkerDatabase db = new StalkerDatabase(m_context.getApplicationContext());
           StalkerDatabase.Person person = db.getPersonFromNumber(number);
           
           if (person != null)
           {
             return startStalker(text, number);
           }
           else
           {
             Log.d(TAG, "Person not permitted " + number);
           }
         }
         else
         {
           Log.w(TAG, "Couldn't retrieve number...");
         }
       }
     }
     
     return false;
   }
   
   protected boolean scanPDU(Object pdu)
   {
     byte[] data = null;
     SmsMessage msg = null;
     
     try
     {
       data = (byte[])pdu;
       msg = SmsMessage.createFromPdu(data);
     }
     catch(Exception e)
     {
       return false;
     }
     
     return this.processMessage(msg);
   }
   
   protected boolean inspectIntent(Intent intent)
   {
     Bundle bundle = intent.getExtras();
     boolean is_cmd = false;
     
     if (bundle != null)
     {
       Object[] pdus = null;
       try
       {
         pdus = (Object[]) bundle.get("pdus");
       }
       catch(Exception e)
       {
         // DEBUG: remove this!
         //Toast.makeText(this.m_context, "Couldn't retrieve PDU's", Toast.LENGTH_LONG).show();
         
         // silently ignore this error...
         Log.w(TAG, "Couldn't retrieve PDU's");
       }
       
       if (pdus != null)
       {
         for (int i = 0; i < pdus.length; ++i)
         {
           is_cmd |= this.scanPDU(pdus[i]);
         }
       }
     }
     
     return is_cmd;
   }
   
   protected boolean startStalker(String cmd, String number)
   {
     Intent intent = new Intent(this.m_context, Stalker.class);
     intent.setFlags(Intent.FLAG_FROM_BACKGROUND);
     //intent.setAction(Stalker.STALKING_ACTION);
     
     intent.putExtra(Stalker.EXTRA_KEY_NUMBER, number);
     
     if (cmd.startsWith(LOCO_CMD_LOCATE))
     {
        intent.putExtra(Stalker.EXTRA_KEY_CMD, Stalker.CMD_LOCATE);
     }
     else if(cmd.startsWith(LOCO_CMD_VIEW_POSITION))
     {
       intent.putExtra(Stalker.EXTRA_KEY_CMD, Stalker.CMD_RECEIVE_RESULT_POSITION);
       intent.putExtra(Stalker.EXTRA_KEY_MESSAGE, cmd.substring(LOCO_CMD_VIEW_POSITION.length()));
     }
     else if(cmd.startsWith(LOCO_CMD_VIEW_CELLS))
     {
       intent.putExtra(Stalker.EXTRA_KEY_CMD, Stalker.CMD_RECEIVE_RESULT_CELLS);
       intent.putExtra(Stalker.EXTRA_KEY_MESSAGE, cmd.substring(LOCO_CMD_VIEW_CELLS.length()));
     }
     else
     {
       Log.d(TAG, "Unknown command " + cmd);
       return false;
     }
     
     this.m_context.startService(intent);
     return true;
   }
   
   @Override
   public void onReceive(Context context, Intent intent)
   {
     String action = intent.getAction();
     
     if (RECEIVED_ACTION.equals(action))
     {
       this.m_context = context;
       
       if (this.inspectIntent(intent))
       {
         abortBroadcast();
       }
     }
     else
     {
       // silently ignore unexpected intent
       //Toast.makeText(context, "Strange Intent...", Toast.LENGTH_LONG).show();
     }
   }
 }
