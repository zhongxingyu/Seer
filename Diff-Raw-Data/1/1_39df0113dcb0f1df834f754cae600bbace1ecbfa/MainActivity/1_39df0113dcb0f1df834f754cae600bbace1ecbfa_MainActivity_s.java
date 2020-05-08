 package com.martynhaigh.sms.consumer;
 
 import android.app.Activity;
 import android.app.Service;
 import android.content.ComponentName;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.os.*;
 import android.util.Log;
 import android.widget.TextView;
 import android.widget.Toast;
 import com.martynhaigh.sms.lib.ISmsConsumer;
 import com.martynhaigh.sms.lib.ISmsProvider;
 import com.martynhaigh.sms.lib.SmsData;
 
 import java.util.List;
 
 public class MainActivity extends Activity {
     /**
      * Called when the activity is first created.
      */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         if (mService == null) {
             Intent it = new Intent();
             it.setAction("com.martynhaigh.permission.service.SMS");
             //binding to remote service
             bindService(it, mServiceConnection, Service.BIND_AUTO_CREATE);
         }
 
     }
 
     protected void onDestroy() {
 
         super.onDestroy();
         unbindService(mServiceConnection);
     }
 
     ISmsProvider mService;
     /**
      * Service connection is used to know the status of the remote service
      */
     ServiceConnection mServiceConnection = new ServiceConnection() {
 
         @Override
         public void onServiceDisconnected(ComponentName name) {
             // TODO Auto-generated method stub
             mService = null;
             Log.d("ISmsProvider", "Binding - Service disconnected");
         }
 
         @Override
         public void onServiceConnected(ComponentName name, IBinder service) {
             // TODO Auto-generated method stub
             mService = ISmsProvider.Stub.asInterface((IBinder) service);
             try {
                 mService.getSmsDetails(mCallback);
             } catch (RemoteException e) {
                 e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
             }
             Log.d("ISmsProvider", "Binding is done - Service connected");
         }
     };
 
 
     /**
      * This implementation is used to receive callbacks from the remote
      * service.
      */
     private ISmsConsumer mCallback = new ISmsConsumer.Stub() {
 
         @Override
         public void returnSmsDetails(List<SmsData> data) throws RemoteException {
             mHandler.sendMessage(mHandler.obtainMessage(BUMP_MSG, data));
         }
     };
 
     private static final int BUMP_MSG = 1;
 
     private Handler mHandler = new Handler() {
         @Override public void handleMessage(Message msg) {
             switch (msg.what) {
                 case BUMP_MSG:
                     List<SmsData> smsData = (List<SmsData>) msg.obj;
                     TextView view = (TextView) findViewById(R.id.place_holder);
                     for(SmsData data : smsData)   {
                         view.setText(view.getText() + data.getBody());
                     }
 
                    //Toast.makeText(MainActivity.this.getBaseContext(), "Sms Data Received: " + (((SmsData)msg.obj).getText()), Toast.LENGTH_LONG).show();
                     break;
                 default:
                     super.handleMessage(msg);
             }
         }
 
     };
 
 }
