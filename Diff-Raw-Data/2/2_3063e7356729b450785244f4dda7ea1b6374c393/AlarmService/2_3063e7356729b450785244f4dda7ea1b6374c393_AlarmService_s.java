 package ch.ffhs.esa.lifeguard.alarm;
 
 import android.app.Service;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.Binder;
 import android.os.IBinder;
 import android.util.Log;
 import ch.ffhs.esa.lifeguard.ActivityMessage;
 import ch.ffhs.esa.lifeguard.alarm.context.AlarmContext;
 import ch.ffhs.esa.lifeguard.alarm.context.ServiceAlarmContext;
 import ch.ffhs.esa.lifeguard.alarm.state.AlarmState;
 import ch.ffhs.esa.lifeguard.alarm.state.AlarmStateListener;
 import ch.ffhs.esa.lifeguard.alarm.state.AlarmingState;
 import ch.ffhs.esa.lifeguard.alarm.state.InitialState;
 
 /**
  * The background service of Lifeguard.
  * 
  * @author David Daniel <david.daniel@students.ffhs.ch>
  * @author Thomas Aregger <thomas.aregger@students.ffhs.ch>
  */
 public class AlarmService extends Service implements AlarmStateListener {
     private final IBinder binder = new AlarmBinder();
 //    private Contact lastNotifiedContact;
     
     
     /*//////////////////////////////////////////////////////////////////////////
      * PROPERTIES
      */
     
    private AlarmContext alarmContext = new ServiceAlarmContext (this.getBaseContext ());
 
     private BroadcastReceiver cancelReceiver = new BroadcastReceiver () {
         public void onReceive (Context context, Intent intent) {
             onManualCancel ();
         }
     };
 
     private BroadcastReceiver alarmReceiver = new BroadcastReceiver () {
         public void onReceive (Context context, Intent intent) {
             onManualAlarm ();
         }
     };
     
     
     /*//////////////////////////////////////////////////////////////////////////
      * PUBLIC INTERFACE
      */
     
     @Override
     public void onCreate ()
     {
         super.onCreate ();
         Log.d(AlarmService.class.toString(), "Service created");
 
         alarmContext.setNext (new InitialState ());
 
         registerReceiver (
                 alarmReceiver,
                 new IntentFilter (ActivityMessage.MANUAL_ALARM));
         registerReceiver (
                 cancelReceiver,
                 new IntentFilter (ActivityMessage.CANCEL_OPERATION));
     }
 
     public void onStart ()
     {
         Log.d(AlarmService.class.toString(), "Service on start");
         sendStatus ();
     }
 
     @Override
     public void onStateChanged (AlarmContext context)
     {
         sendStatus ();
     }
     
     @Override
     public IBinder onBind (Intent intent) {
         return binder;
     }
 
     @Override
     public void onDestroy ()
     {
         unregisterReceiver (alarmReceiver);
         unregisterReceiver (cancelReceiver);
         super.onDestroy ();
     }
 //    
 //    public AlarmState getState() {
 //        return alarmContext.getState();
 //    }
     
 //    public void doManualAlarm() {
 //        if ( isAlarmingState() ) {
 //            Log.d(this.getClass().toString(), "Already alarmed.");
 //        } else {
 //            alarmContext.setNext(new AlarmingState());
 //            alarmContext.getState().process(alarmContext);
 //            lastNotifiedContact = ((AlarmingState) alarmContext.getState()).getLastNotifiedContact();
 //            alarmContext.setNext(new AwaitingState(lastNotifiedContact.getPhone (), 0));
 //            alarmContext.getState().process(alarmContext);
 //        }
 //       
 //    }
 //    
 //    public String getAlarmButtonMsg() {
 //        if ( alarmContext.getState().getClass() == AlarmingState.class ) {
 //            return lastNotifiedContact.getName() + " wurde alarmiert.";
 //        } else if (alarmContext.getState().getClass() == AwaitingState.class ) {
 //            return lastNotifiedContact.getName() + " wurde bereits alarmiert.";
 //        } else {
 //            return "Halten Sie den Knopf f�r 5 Sekunden gedr�ckt um sofort einen Alarm auszul�sen.";
 //        }
 //        
 //    }
 //    
 //    private boolean isAlarmingState() {
 //        if ( alarmContext.getState().getClass() == AlarmingState.class
 //                || alarmContext.getState().getClass() == AwaitingState.class) {
 //            return true;
 //        } else {
 //            return false;
 //        }
 //        
 //    }
 //    
     
     /*//////////////////////////////////////////////////////////////////////////
      * PRIVATE OPERATIONS
      */
 
     private void sendStatus ()
     {
         Intent intent = new Intent (ServiceMessage.CURRENT_SERVICE_STATE);
         AlarmState state = alarmContext.getState ();
         intent.putExtra ("stateId", state.getId ().toString ());
         alarmContext.getBaseContext ().sendBroadcast (intent);
     }
     
     public class AlarmBinder extends Binder {
         public AlarmService getService() {
             return AlarmService.this;
         }
     }
 
     private void onManualCancel ()
     {
         alarmContext.cancel ();
         sendStatus ();
     }
 
     private void onManualAlarm ()
     {
         alarmContext.cancel ();
         alarmContext.setNext (new AlarmingState ());
         sendStatus ();
     }
 }
