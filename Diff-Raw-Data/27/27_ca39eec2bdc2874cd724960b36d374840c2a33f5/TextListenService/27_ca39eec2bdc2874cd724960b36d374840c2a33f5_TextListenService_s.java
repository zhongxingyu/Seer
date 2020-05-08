 package com.david.where;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Timer;
 
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.ComponentName;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.location.Address;
 import android.content.SharedPreferences;
 import android.os.Message;
 import android.os.Handler;
 import android.os.Messenger;
 import android.location.Geocoder;
 import android.content.ServiceConnection;
 import android.location.Location;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.widget.Toast;
 import android.telephony.SmsManager;
 import android.telephony.SmsMessage;
 import android.util.Log;
 import android.os.RemoteException;
 
 
 
 public class TextListenService extends Service {
 
     private SQLiteDatabase mDatabase;
     private Location mLocation;
     private IntentFilter textFilter = null;
     public ArrayList<String> permissionList = new ArrayList<String>();
     private Messenger mLocationService;
     private String number;
     private boolean mBound;
     private static Timer timer = new Timer(); 
     
     public final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
     public final String ADD_PERMISSION = "com.david.where.action.ADD_PERMISSION";
     public String LOCATION_REQUEST = "Where u at?";
 
     
     @Override
     public void onCreate() {
         super.onCreate();
     }
 
     @Override
     public IBinder onBind(Intent intent) {
         return mMessenger.getBinder();
     }
 
     @Override
     public void onDestroy() {
         doUnbind();
         unregisterReceiver(incomingReciever);
         super.onDestroy();
     }
 
     public int onStartCommand(Intent intent, int flags, int startID) {
 
         textFilter = new IntentFilter(ADD_PERMISSION);
         textFilter.addAction(SMS_RECEIVED);
         registerReceiver(incomingReciever,textFilter);
 
         doBind();
         
         return Service.START_STICKY;
     }
 
     BroadcastReceiver incomingReciever = new BroadcastReceiver() {
 
         @Override
         public void onReceive(Context context, Intent _intent) {
 
 
             if(_intent.getAction().equals(SMS_RECEIVED)) {
                 Bundle bundle = _intent.getExtras();    
                 if(bundle != null) {
                     Object[] pdus = (Object[]) bundle.get("pdus");
 
                     SmsMessage[] messages = new SmsMessage[pdus.length];
 
                     for(int i = 0; i < pdus.length; i++)
                         messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]); 
 
                     String msg;
                     for (SmsMessage message : messages) {
                         SharedPreferences settings = getSharedPreferences(Constants.PREFERENCES, MODE_PRIVATE);  
                         LOCATION_REQUEST = settings.getString(Constants.PREFERENCES_KEYWORD, "Where u at?");
                         msg = message.getMessageBody();
                         if(msg.startsWith(LOCATION_REQUEST) && hasPermission(message.getOriginatingAddress())) {
                             number = message.getOriginatingAddress();
                             number = number.replace("-","");
                             updateLocation();
                         }
                     }   
                 }   
             }   
         }
     }; 
 
     private boolean hasPermission(String number) {
 
         mDatabase = openOrCreateDatabase(Constants.DB_NAME, SQLiteDatabase.OPEN_READONLY, null);
         Cursor c = mDatabase.query(Constants.TABLE_NAME, null, null, null, null, null, null);
         
         if(c.moveToFirst()) {
             do {
                 if(c.getString(Constants.NUMBER_COLUMN).equals(number))
                     return true;
             } while (c.moveToNext()); 
         }
         mDatabase.close();
         return false;
     }
 
     private String geoCodeToString(Location loc) {
 
         Geocoder gcoder = new Geocoder(this);
         StringBuilder result = new StringBuilder();
 
         try {
             List<Address> add = gcoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 3);
             Address address =  add.get(0);
             int maxIndex = address.getMaxAddressLineIndex();
             for (int x = 0; x <= maxIndex; x++ ){
                 result.append(address.getAddressLine(x));
                 if(x != maxIndex)
                     result.append(", ");
             }   
             return result.toString();
 
 
         } catch (IOException e) {
             e.printStackTrace();
         }
         return "My GPS is not available.";
 
     }
 
 
     public boolean sendBackLocation() {
         Toast.makeText(getBaseContext(),geoCodeToString(mLocation), Toast.LENGTH_LONG).show();
         sendSMS(number,"I'm near " + geoCodeToString(mLocation));
         return true;
     }
 
     private void sendSMS(String phoneNumber, String message)
     {        
         PendingIntent pi = PendingIntent.getActivity(this, 0,
             new Intent(this, TextListenService.class), 0);                
         SmsManager sms = SmsManager.getDefault();
         sms.sendTextMessage(phoneNumber, null, message, pi, null);        
     }   
 
     private ServiceConnection mLocationServiceConnection = new ServiceConnection() {
 
             public void onServiceConnected(ComponentName aName, IBinder aIBinder) {
                 mLocationService = new Messenger(aIBinder);
                 try {
                     Message msg = Message.obtain(null, LocationService.MSG_REGISTER_CLIENT);
                     msg.replyTo = mMessenger;
                     mLocationService.send(msg);
                 } catch (RemoteException e) {
                     e.printStackTrace();
                 }
             }
 
             public void onServiceDisconnected(ComponentName aName) {
                 mLocationService = null;
             }
         };
 
     Messenger mMessenger = new Messenger(new Handler() {
         @Override
         public void handleMessage(Message aMsg) {
             switch (aMsg.what) {
                 case LocationService.MSG_LOCATION_RESPONSE:
                     handleResult(aMsg);
                     break;
                 case LocationService.MSG_LOCATION_STATUS:
                     handleStatus();
                     break;
                 default:
                     super.handleMessage(aMsg);
             }
         }
     });
 
     private void handleStatus() {
     }
 
     private void handleResult(Message aMsg) {
         int type = aMsg.arg1;
         int result = aMsg.arg2;
 
         if (type == LocationService.Type.BY_GPS) {
             switch (result) {
                 case LocationService.Status.SUCCESS:
                     if(aMsg.obj != null) {
                         mLocation = (Location) aMsg.obj;
                         sendBackLocation();
                         number = "";
                     }
                     break;
                 case LocationService.Status.FAILURE:
                     break;
                 case LocationService.Status.ERROR:
                     break;
             }
         }
     }
 
     private void doBind() {
         Intent i = new Intent(this, com.david.where.LocationService.class);
         mBound = this.bindService(i, mLocationServiceConnection, Context.BIND_AUTO_CREATE);
     }
 
     private void doUnbind() {
         if (mBound) {
             if (mLocationService != null) {
                 try {
                     Message msg = Message.obtain(null, LocationService.MSG_UNREGISTER_CLIENT);
                     msg.replyTo = mMessenger;
                     mLocationService.send(msg);
                 } catch (RemoteException e) {
                     e.printStackTrace();
                 }
             }
 
             this.unbindService(mLocationServiceConnection);
             mBound = false;
         }
     }
 
     public void updateLocation() {
         if (mLocationService != null) {
             try {
                 Message msg = Message.obtain(null, LocationService.MSG_LOCATION);
                 msg.replyTo = mMessenger;
                 mLocationService.send(msg);
             } catch (RemoteException e) {
                 e.printStackTrace();
             }
         }
     }
 
 }
