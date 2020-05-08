 package teamwork.goodVibrations;
 
 import java.util.ArrayList;
 
 import teamwork.goodVibrations.functions.Function;
 import teamwork.goodVibrations.triggers.TimeTrigger;
 import teamwork.goodVibrations.triggers.Trigger;
 import teamwork.goodVibrations.functions.*;
 
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Handler;
 import android.os.HandlerThread;
 import android.media.AudioManager;
 import android.os.IBinder;
 import android.os.Looper;
 import android.os.Message;
 import android.util.Log;
 
 public class GoodVibrationsService extends Service
 {
 	private TriggerQueue triggers;          // Queue that holds all of the triggers
 	private ArrayList<Function> functions;  // List that holds all of the functions
 	
 	private Looper mServiceLooper;          // Looper to handle the messages
 	private ServiceHandler mServiceHandler;
 		
 	private SettingsChanger changer;
 	
 	// Handler that receives messages from the thread
 	private final class ServiceHandler extends Handler
 	{
       public ServiceHandler(Looper looper)
       {
           super(looper);
       }
       
       // Handles the message to add a trigger or a function
       @Override
       public void handleMessage(Message msg)
       {
         synchronized (this)
         {
           synchronized(triggers)
           {
             changer.interrupt();
             if(msg.arg2 == 1)
             {
               triggers.push((Trigger)msg.obj);
             }
             else
             {
               functions.add((Function)msg.obj);
             }
           }
          try
          {
            changer.join();
          }
          catch()
          {
            
          }
           changer = new SettingsChanger();
           changer.start();
          //changer.run();  // TODO Should this create a new SettingsChanger and start it?
         }
         Log.d("vorsth","Stopping handle message");
         // Stop the service using the startId, so that we don't stop
         // the service in the middle of handling another job
         stopSelf(msg.arg1);
       }
 	}
 	
 	private class SettingsChanger extends Thread
 	{
     public void run()
     {
       Trigger t = null;
       while(!currentThread().isInterrupted())
       {
         try
         {
           synchronized(triggers)
           {
             // Grab the next trigger that will execute
             t = triggers.pop();
           }
           if(t != null) // Make sure we have a trigger to process
           {
             // Sleep for the time until the trigger will execute
             Thread.sleep(t.getNextExecutionTime() - System.currentTimeMillis());
             // Execute all of the functions for this trigger
             for(Integer fID : t.getFunctions() )
             {
               functions.get(fID.intValue()).execute();
             }
             // Re-insert the trigger so that it gets executed again
             synchronized(triggers)
             {
               t.switchState();
               triggers.push(t);
             }
           }
           else // t is null because no trigger are in system
           {
             try
             {
               Thread.sleep(10000); 
             }
             catch(InterruptedException e)
             {         
             }
           }
         }
         catch(InterruptedException e)
         {
         }
       } // End While
       
       // If we were interrupted, re-insert the trigger so it is not lost
       synchronized(triggers)
       {
         if(t != null)  // Only push if a trigger was popped off earlier
         {
           triggers.push(t);
         }
       }
     } // End run()
 	}
 
 	@Override
 	public void onCreate()
 	{
 		Log.d("vorsth","Calling onCreate");
 		
 		triggers  = new TriggerQueue();
 		functions = new ArrayList<Function>();
 		
 		// Only samples, need to be removed
 		functions.add(new LowerFunction((AudioManager) getSystemService(Context.AUDIO_SERVICE)));
 		functions.add(new RaiseFunction((AudioManager) getSystemService(Context.AUDIO_SERVICE)));
 				
 		Log.d("vorsth","Added Function");
 		
 		// Start up the thread running the service.  Note that we create a
 	  // separate thread because the service normally runs in the process's
 	  // main thread, which we don't want to block.  We also make it
 	  // background priority so CPU-intensive work will not disrupt our UI.
 	  HandlerThread thread = new HandlerThread("ServiceStartArguments", 10);//Process.THREAD_PRIORITY_BACKGROUND);
 	  thread.start();
 	  
 	  Log.d("vorsth","Handler Started");
 	  
 	  changer = new SettingsChanger();
 	  changer.start();
 	  
 	  Log.d("vorsth","Settings Changer Started");
 	  
 	  // Get the HandlerThread's Looper and use it for our Handler 
 	  mServiceLooper = thread.getLooper();
 	  mServiceHandler = new ServiceHandler(mServiceLooper);
 	  Log.d("vorsth","Finished onCreate");
 	}
 	
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId)
 	{
     Log.d("vorsth","Starting onStartCommand");
     // For each start request, send a message to start a job and deliver the
 	  // start ID so we know which request we're stopping when we finish the job
     
     // TODO Parse the intent to determine message
     
 	  Message msg = mServiceHandler.obtainMessage();
 	  msg.arg1 = startId;
 	  msg.arg2 = 1; // 1 - Trigger 2 - Function
 	  
 	  Log.d("vorsth","Messaged recieved");
 	  
 	  if(msg.arg2 == 1)
 	  {
 	    // Making a trigger
 	    // TODO Build trigger from parsed message
 	    TimeTrigger t = new TimeTrigger();
 	    t.addFunction(TimeTrigger.START, new Integer(0));
 	    t.addFunction(TimeTrigger.STOP,  new Integer(1));
 	    msg.obj = t;
 	  }
 	  else
 	  {
 		  //TODO: fix this
 	    //msg.obj = new NULLFunction();  
 	  }
 	  
 	  Log.d("vorsth","Created Trigger");
 	  
 	  mServiceHandler.sendMessage(msg);
 	     
 	  Log.d("vorsth","Finished onStartCommand");
 	  
 	  // If we get killed, after returning from here, restart
 	  return START_STICKY;
 	}
 	
 	@Override
 	public IBinder onBind(Intent arg0)
 	{
 		return null;
 	}
 
 }
