 package teamwork.goodVibrations;
 
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 
 public class TimeTriggerEditActivity extends Activity
 {
   private static final String TAG = "TimeTriggerEditActivity";
   Intent mIntent;
   EditText txtName;
   EditText txtPriority;
   private boolean firstTime = true;
   private boolean beingEdited = false;
   
   public void onCreate(Bundle savedInstanceState)
   {
     super.onCreate(savedInstanceState);
     Log.d(TAG, "onCreate()");
     setContentView(R.layout.add_time_trigger);
     mIntent = new Intent();
     // Set defaults in case the user does not click on "Repeat?" button
     mIntent.putExtra(Constants.INTENT_KEY_REPEAT_DAYS_BOOL, false);
     mIntent.putExtra(Constants.INTENT_KEY_REPEAT_DAYS_BYTE, (byte) 0);
     // Set defaults in case user does not select start or end functions
     int[] emptyInts = new int[0];
     // mIntent.putExtra(Constants.INTENT_KEY_START_FUNCTION_IDS, emptyInts);
     // mIntent.putExtra(Constants.INTENT_KEY_STOP_FUNCTION_IDS, emptyInts);
     mIntent.putExtra(Constants.INTENT_KEY_FUNCTION_IDS, emptyInts);
   }
 
   protected void onStart()
   {
     super.onStart();
     Log.d(TAG, "onStart()");
 
     // set intent type to time trigger
     mIntent.putExtra(Constants.INTENT_TYPE, Constants.TRIGGER_TYPE);
     mIntent.putExtra(Constants.INTENT_KEY_TYPE, Constants.TRIGGER_TYPE_TIME);
 
     // name and priority text boxes
     txtName = (EditText) findViewById(R.id.editTextTriggerName);
     txtPriority = (EditText) findViewById(R.id.editTextPriority);
     // button to set times
     final Button buttonSetTimes = (Button) findViewById(R.id.buttonTimeTriggerSetTimes);
     buttonSetTimes.setOnClickListener(new View.OnClickListener()
     {
       public void onClick(View v)
       {
         Intent TimeTriggerSetTimesIntent = new Intent(getApplicationContext(), TimeTriggerSetTimesActivity.class);
         try
         {
           Bundle b = mIntent.getExtras();
           if(firstTime)
           {
             TimeTriggerSetTimesIntent.putExtra(Constants.INTENT_KEY_START_TIME, Utils.getTimeOfDayInMillis());
             TimeTriggerSetTimesIntent.putExtra(Constants.INTENT_KEY_END_TIME, Utils.getTimeOfDayInMillis());
           }
           else
           {
             TimeTriggerSetTimesIntent.putExtra(Constants.INTENT_KEY_START_TIME, b.getLong(Constants.INTENT_KEY_START_TIME));
             TimeTriggerSetTimesIntent.putExtra(Constants.INTENT_KEY_END_TIME, b.getLong(Constants.INTENT_KEY_END_TIME)); 
           }
             TimeTriggerSetTimesIntent.putExtra(Constants.INTENT_KEY_REPEAT_DAYS_BOOL, b.getBoolean(Constants.INTENT_KEY_REPEAT_DAYS_BOOL));
             TimeTriggerSetTimesIntent.putExtra(Constants.INTENT_KEY_REPEAT_DAYS_BYTE, b.getByte(Constants.INTENT_KEY_REPEAT_DAYS_BYTE));
         }
         catch(NullPointerException e)
         {
           // If we get a NullPointerException that means that this hasn't been
           // called so there is no data to be passed anyway.
         }
 
         startActivityForResult(TimeTriggerSetTimesIntent, Constants.REQUEST_CODE_SET_TIMES_ACTIVITY);
       }
     });
 
     final Button buttonSetFunction = (Button) findViewById(R.id.buttonTimeTriggerSetFunctions);
     buttonSetFunction.setOnClickListener(new View.OnClickListener()
     {
 
       public void onClick(View v)
       {
         // Add the selected functions to the bundle so they can be automatically
         // checked
         Intent TimeTriggerSetFunctionsIntent = new Intent(getApplicationContext(), SetFunctionsActivity.class);
         try
         {
           Bundle b = mIntent.getExtras();
           TimeTriggerSetFunctionsIntent.putExtra(Constants.INTENT_KEY_FUNCTION_IDS, b.getIntArray(Constants.INTENT_KEY_FUNCTION_IDS));
         }
         catch(NullPointerException e)
         {
           // If we get a NullPointerException that means that this hasn't been
           // called so there is no data to be passed anyway.
         }
         startActivityForResult(TimeTriggerSetFunctionsIntent, Constants.REQUEST_CODE_SET_FUNCTION_IDS);
       }
     });
 
     // final Button buttonSetFunctions =
     // (Button)findViewById(R.id.buttonTimeTriggerSetFunctions);
     final Button buttonDone = (Button) findViewById(R.id.buttonTimeTriggerDone);
     buttonDone.setOnClickListener(new View.OnClickListener()
     {
       public void onClick(View v)
       {
         // sets the name in the intent
         mIntent.putExtra(Constants.INTENT_KEY_NAME, txtName.getText().toString());
         mIntent.putExtra(Constants.INTENT_TYPE, Constants.TRIGGER_TYPE);
         mIntent.putExtra(Constants.INTENT_KEY_TYPE, Constants.TRIGGER_TYPE_TIME);
         // Check the priority value to make sure it is a number
         try
         {
           String p = txtPriority.getText().toString();
           int priorityInt = new Integer(p).intValue();
           mIntent.putExtra(Constants.INTENT_KEY_PRIORITY, priorityInt);
         }
         catch(Exception e)
         {
           mIntent.putExtra(Constants.INTENT_KEY_PRIORITY, 1);
         }
         
         // start
         setResult(RESULT_OK, mIntent);
         finish(); // Returns to FunctionDisplayActivity.onActivityResult()
       }
     });
     
     Bundle b = getIntent().getExtras();
     beingEdited = b.getBoolean(Constants.INTENT_KEY_EDITED_BOOL);
     mIntent.putExtra(Constants.INTENT_KEY_EDITED_BOOL, beingEdited);
     Log.d(TAG, "beingEdited = " + beingEdited);
    if(beingEdited){
       firstTime = false;
       txtName.setText(b.getString(Constants.INTENT_KEY_NAME));
       mIntent.putExtra(Constants.INTENT_KEY_NAME, b.getString(Constants.INTENT_KEY_NAME));
       txtPriority.setText(new Integer(b.getInt(Constants.INTENT_KEY_PRIORITY)).toString());
       mIntent.putExtra(Constants.INTENT_KEY_EDITED_ID, b.getInt(Constants.INTENT_KEY_EDITED_ID));
       mIntent.putExtra(Constants.INTENT_KEY_PRIORITY, b.getInt(Constants.INTENT_KEY_PRIORITY));
       mIntent.putExtra(Constants.INTENT_KEY_START_TIME, b.getLong(Constants.INTENT_KEY_START_TIME));
       mIntent.putExtra(Constants.INTENT_KEY_END_TIME, b.getLong(Constants.INTENT_KEY_END_TIME));
       mIntent.putExtra(Constants.INTENT_KEY_REPEAT_DAYS_BYTE, b.getByte(Constants.INTENT_KEY_REPEAT_DAYS_BYTE));
       mIntent.putExtra(Constants.INTENT_KEY_FUNCTION_IDS, b.getIntArray(Constants.INTENT_KEY_FUNCTION_IDS));
     }
   }
 
   @Override
   protected void onActivityResult(int requestCode, int resultCode, Intent data)
   {
     super.onActivityResult(requestCode, resultCode, data);
     if(resultCode == RESULT_OK)
     {
       firstTime = false;
       Log.d(TAG, "onActivityResult()");
       // If the TimeTriggerSetTimesActivity was returned
       if(requestCode == Constants.REQUEST_CODE_SET_TIMES_ACTIVITY)
       {
         Bundle b = data.getExtras();
         mIntent.putExtra(Constants.INTENT_KEY_START_TIME, b.getLong(Constants.INTENT_KEY_START_TIME));
         mIntent.putExtra(Constants.INTENT_KEY_END_TIME, b.getLong(Constants.INTENT_KEY_END_TIME));
         // If there is also repeat days information
         mIntent.putExtra(Constants.INTENT_KEY_REPEAT_DAYS_BOOL, b.getBoolean(Constants.INTENT_KEY_REPEAT_DAYS_BOOL));
         mIntent.putExtra(Constants.INTENT_KEY_REPEAT_DAYS_BYTE, b.getByte(Constants.INTENT_KEY_REPEAT_DAYS_BYTE));
       }
       else if(requestCode == Constants.REQUEST_CODE_SET_FUNCTION_IDS)
       {
         Bundle b = data.getExtras();
         mIntent.putExtra(Constants.INTENT_KEY_FUNCTION_IDS, b.getIntArray(Constants.INTENT_KEY_FUNCTION_IDS));
       }
     }
     else
     {
       Log.d(TAG, "onActivityResult() Failed");
     }
   }
 
   public class DataReceiver extends BroadcastReceiver
   {
     @Override
     public void onReceive(Context context, Intent intent) //PUT IT IN THE MANIFEST
     {
       Log.d(TAG, "RECEIVED BROADCAST MESSAGE");
       
       Bundle b = intent.getExtras();
       
       if(b.getInt(Constants.INTENT_KEY_TYPE)== Constants.TRIGGER_TYPE_TIME)
       {
         txtName.setText(b.getString(Constants.INTENT_KEY_NAME));
       }
       
     }
   }
 }
 
