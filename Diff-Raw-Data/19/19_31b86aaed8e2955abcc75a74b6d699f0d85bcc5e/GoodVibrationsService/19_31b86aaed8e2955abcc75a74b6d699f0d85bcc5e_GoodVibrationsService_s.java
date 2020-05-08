 package teamwork.goodVibrations;
 
 import teamwork.goodVibrations.persistence.PersistentStorage;
 import teamwork.goodVibrations.triggers.*;
 import teamwork.goodVibrations.functions.*;
 
 import android.app.*;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.util.Log;
 
 public class GoodVibrationsService extends Service
 {
   private static String TAG = "GoodVibrationsService";
 
   private volatile TriggerQueue triggers; // Queue that holds all of the
                                           // triggers
   private volatile FunctionList functions; // List that holds all of the
                                            // functions
   private int maxFunctionID = 1;
   private int maxTriggerID = 1;
   private int maxPriority = Integer.MAX_VALUE;
 
   private SettingsChanger changer;
   
   public static Context c;
   
   private class SettingsChanger extends Thread
   {
     public void run()
     {
       Trigger t = null;
       
       while(!Thread.currentThread().isInterrupted())
       {
         try
         {
           synchronized(triggers)
           {
             t = triggers.getNextTrigger();
           }
           if(t != null) // Make sure we have a trigger to process
           {
             // Sleep for the time until the trigger will execute
             Log.d(TAG, "Sleeping for " + t.getSleepTime());
             Thread.sleep(t.getSleepTime());
             // Execute all of the functions for this trigger
             
             if(t.canExecute())
             {
               //set the max priority since if we get to this point, this has the best priority;
               Log.d(TAG, "Executing trigger: " + t.id + "  " + t.name + " with priority: " + t.priority);
               // Execute functions
               synchronized(triggers)
               {
                 //added boolean for empty function lists
                 //we don't want to switch the state of a function if it doesn't
                 //have any functions to execute 
                 if (t.canExecute(maxPriority))
                 {
                   maxPriority = t.priority;
                   for(Integer fID : t.getFunctions())
                   {
                     Log.d(TAG,"FID: " + fID.intValue());
                     Function inverse = functions.get(fID.intValue()).execute();
                     if(t.isStarting())
                     {
                       functions.add(inverse);
                       t.addFunction(new Integer(inverse.id),Constants.INVERSE_FUNCTION);
                     }
                     else
                     {
                       functions.remove(fID.intValue());
                       t.removeFunction(fID);
                       //reset the max priority since the trigger has ended
                       maxPriority = Integer.MAX_VALUE;
                     }
                   }
                 }
                 triggers.switchState(t.id);
               }
             }
           }
           else
           // t is null because no triggers are in system
           {
             try
             {
               Log.d(TAG, "No triggers Sleeping for 10000");
               Thread.sleep(10000);
             }
             catch(InterruptedException e)
             {
               Log.d(TAG, "Sleep while no triggers interrupted");
             }
           }
         }
         catch(InterruptedException e)
         {
           Log.d(TAG, "Sleep while waiting for trigger was interrupted");
         }
       } // End While
     } // End run()
   }
 
   @Override
   public void onCreate()
   {
     Log.d(TAG, "Calling onCreate()");
     
     c = getApplicationContext();
        
     functions = new FunctionList(PersistentStorage.loadFunctions());
     triggers = new TriggerQueue(PersistentStorage.loadTriggers());
     
     int maxID = 0;
     int Fids[] = functions.getIDs();
     for(int loc = 0; loc < Fids.length; loc++)
     {
       maxID = (maxID < Fids[loc])? Fids[loc] : maxID;
     }
     maxFunctionID = maxID + 1;    
     
     int Tids[] = triggers.getIDs();
     maxID = 0;
     for (int loc = 0; loc < Tids.length; loc++)
     {
       maxID = (maxID < Tids[loc])? Tids[loc] : maxID;
     }   
     maxTriggerID = maxID + 1;
 
     Log.d(TAG, "Added Function");
 
     changer = new SettingsChanger();
     changer.start();
 
     Log.d(TAG, "Settings Changer Started");
 
     Log.d(TAG, "Finished onCreate");
   }
 
   @Override
   public int onStartCommand(Intent intent, int flags, int startId)
   {
     Log.d(TAG, "Starting onStartCommand");
     
     
     Log.d(TAG,"MANUALLY MAKING TRIGGER");
 //    TimeTrigger tt = new TimeTrigger("name@12@1#2#3#@4#5#6#@1234567890@2345678901@28");
 //    LocationTrigger tt = new LocationTrigger(getApplicationContext(),"name@13@1#2#3#@4#5#6#@-41.324@713.234@40.2");
     Log.d(TAG,"DONE MANUALLY MAKING TRIGGER");
     
 
     Bundle b = intent.getExtras();
     final int intentType = b.getInt(Constants.INTENT_TYPE);
     final int type = b.getInt(Constants.INTENT_KEY_TYPE);
 
     Log.d(TAG, "Bundle Created " + intentType + "  " + type);
 
     if(intentType == Constants.FUNCTION_TYPE)
     {
       if(b.getBoolean(Constants.INTENT_KEY_EDITED_BOOL))
       {
         int id = b.getInt(Constants.INTENT_KEY_EDITED_ID);
         Log.d(TAG,"IS EDITING: " + id);
        removeFunction(id);
       }
 
       switch(type)
       {
         // Add a new volume function
         case Constants.FUNCTION_TYPE_VOLUME:
           functions.add(new SetVolumeFunction(b, maxFunctionID++));
           break;
 
         // Add a new ring tone function
         case Constants.FUNCTION_TYPE_RINGTONE:
           Log.d(TAG, "New Ringtone Function");
           functions.add(new RingtoneFunction(b, maxFunctionID++));
           break;
           
         case Constants.FUNCTION_TYPE_WALLPAPER:
           Log.d(TAG, "New Wallpaper Function");
           functions.add(new WallpaperFunction(b, maxFunctionID++));
           break;
 
         default:
           Log.d(TAG, "Default Function");
           break;
       }
       PersistentStorage.saveFunctions(functions.functions);
       Log.d(TAG,"Functions Saved");
     }
     else if(intentType == Constants.TRIGGER_TYPE)
     {
       synchronized(triggers)
       {
         changer.interrupt();
         Log.d(TAG,"TRIGGER TYPE");
         Trigger t = null;
         switch(type)
         {
           case Constants.TRIGGER_TYPE_TIME:
             Log.d(TAG,"TRIGGER TYPE TIME");
             t = new TimeTrigger(b, maxTriggerID++);
             break;
   
           case Constants.TRIGGER_TYPE_LOCATION:
             t = new LocationTrigger(this, b, maxTriggerID++);
             break;
   
           default:
             // Should never happen
         }
   
         Log.d(TAG, "Submitting TimeTrigger To queue");
         // Submit the trigger into the queue
         triggers.add(t);
       }
 
       // Restart the settings changer
       SettingsChanger.interrupted();
 
       PersistentStorage.saveTriggers(triggers.getTriggers());
 
       Log.d(TAG, "Trigger submitted");
     }
     else if(intentType == Constants.GET_DATA)
     {
       Intent i;
       int id;
       switch(type)
       {
         case Constants.INTENT_KEY_FUNCTION_LIST:
           i = new Intent(Constants.SERVICE_MESSAGE);
           i.putExtra(Constants.INTENT_TYPE, Constants.INTENT_KEY_FUNCTION_LIST);
           int[] ids = functions.getIDs();
           i.putExtra(Constants.INTENT_KEY_DATA_LENGTH, ids.length);
           i.putExtra(Constants.INTENT_KEY_FUNCTION_NAMES, functions.getNames());
           i.putExtra(Constants.INTENT_KEY_FUNCTION_IDS, ids);
           sendBroadcast(i);
           Log.d(TAG, "GET FUNCTION LIST");
           break;
 
         case Constants.INTENT_KEY_TRIGGER_LIST:
           i = new Intent(Constants.SERVICE_MESSAGE);
           i.putExtra(Constants.INTENT_TYPE, Constants.INTENT_KEY_TRIGGER_LIST);
           i.putExtra(Constants.INTENT_KEY_DATA_LENGTH, triggers.size());
           Log.d(TAG, "NT: " + triggers.size());
           i.putExtra(Constants.INTENT_KEY_TRIGGER_NAMES, triggers.getNames());
           i.putExtra(Constants.INTENT_KEY_TRIGGER_IDS, triggers.getIDs());
           sendBroadcast(i);
           Log.d(TAG, "GET TRIGGER LIST");
           break;
           
         case Constants.INTENT_KEY_FUNCTION:
           Log.d(TAG, "--Sending function to activity");
           id = b.getInt(Constants.INTENT_KEY_EDITED_ID);
           i = functions.get(id).getFunctionAsIntent();
           i.putExtra(Constants.INTENT_TYPE, Constants.INTENT_KEY_FUNCTION);
           sendBroadcast(i);
           break;
           
         case Constants.INTENT_KEY_TRIGGER:
           id = b.getInt(Constants.INTENT_KEY_EDITED_ID);
           i = triggers.get(id).getTriggerAsIntent();
           
           
           break;
       }
     }
     else if(intentType == Constants.DELETE_TRIGGER)
     {
       // Get the id to delete
       int id = b.getInt(Constants.INTENT_KEY_DELETED_ID);
       synchronized(triggers)
       {
         changer.interrupt();
         triggers.remove(id);
       }
       
       // Restart the settings changer
       SettingsChanger.interrupted();
 
       PersistentStorage.saveTriggers(triggers.getTriggers());
 
       Log.d(TAG, "Trigger deleted");
     }
     else if(intentType == Constants.DELETE_FUNCTION)
     {
       // Get id to delete
       int id = b.getInt(Constants.INTENT_KEY_DELETED_ID);
      removeFunction(id);
     }
 
     Log.d(TAG, "onStartCommand() Finished");
 
     // If we get killed, after returning from here, restart
     return START_STICKY;
   }
 
   @Override
   public IBinder onBind(Intent arg0)
   {
     return null;
   }
   
  private void removeFunction(int id)
   {
     synchronized(triggers)
     {
       changer.interrupt();
       
      // Go through all the triggers and remove the function ID if it is in the trigger
      for(Trigger t : triggers.getTriggers())
       {
        t.removeFunction(new Integer(id));
       }
       
       // Now remove from the functions list
       functions.remove(id);
       
       Log.d(TAG,"REMOVED FUNCTION " + id);
     }
     
     // Restart the settings changer
     SettingsChanger.interrupted();
     
     PersistentStorage.saveFunctions(functions.functions);
     PersistentStorage.saveTriggers(triggers.getTriggers());
   }
 
 }
