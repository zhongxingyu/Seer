 package pro.kornev.kcar.cop.activities;
 
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.os.Bundle;
 import android.app.Activity;
 import android.os.Handler;
 import android.os.IBinder;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 
 import pro.kornev.kcar.cop.R;
 import pro.kornev.kcar.cop.Utils;
 import pro.kornev.kcar.cop.providers.ConfigDB;
 import pro.kornev.kcar.cop.services.CopService;
 import pro.kornev.kcar.cop.services.support.WakeUpService;
 
 public class MainActivity extends Activity {
     private Button runButton;
     private CopService copService;
     private Intent copServiceIntent;
     private boolean copBound = false;
     private Handler handler = new Handler();
     private ConfigDB configDB;
     private EditText proxy;
     private CheckBox logsEnabled;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         runButton = (Button)findViewById(R.id.maRunButton);
         updateServiceState();
         logsEnabled = (CheckBox)findViewById(R.id.maEnableLogsCheckBox);
         proxy = (EditText)findViewById(R.id.maProxy);
         configDB = new ConfigDB(this);
         logsEnabled.setChecked(configDB.isLogsEnabled());
         proxy.setText(configDB.getProxy());
        startService(new Intent(this, WakeUpService.class));
     }
 
     @Override
     protected void onStart() {
         super.onStart();
         copServiceIntent = new Intent(this, CopService.class);
         bindService(copServiceIntent, mConnection, Context.BIND_AUTO_CREATE);
     }
 
     @Override
     protected void onStop() {
         super.onStop();
         if (copBound) {
             unbindService(mConnection);
             copBound = false;
         }
     }
 
     @SuppressWarnings("unused")
     public void testProtocolClick(View v) {
         Intent intent = new Intent(this, TestProtocolActivity.class);
         startActivity(intent);
     }
 
     @SuppressWarnings("unused")
     public void chooseUsbDeviceClick(View v) {
         Intent intent = new Intent(this, UsbDevicesActivity.class);
         startActivity(intent);
     }
 
     @SuppressWarnings("unused")
     public void onShowLogsClick(View v) {
         Intent intent = new Intent(this, LogsActivity.class);
         startActivity(intent);
     }
 
     @SuppressWarnings("unused")
     public void onServiceRunClick(View v) {
         if (copBound && copService.isRunning()) {
             copService.stop();
         } else {
             startService(copServiceIntent);
         }
         updateServiceState();
     }
 
     @SuppressWarnings("unused")
     public void onEnableLogsClick(View v) {
         configDB.setLogsEnabled(logsEnabled.isChecked());
     }
 
     @SuppressWarnings("unused")
     public void onProxyButtonClick(View v) {
         assert proxy.getText() != null;
         configDB.setProxy(proxy.getText().toString());
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
 
     private void updateServiceState() {
         handler.post(new Runnable() {
             @Override
             public void run() {
                 Utils.sleep(200); //wait while service was started or stopped
                 if (copBound && copService.isRunning()) {
                     runButton.setText("Stop services");
 
                 }
                 else {
                     runButton.setText("Run services");
                 }
             }
         });
     }
 
     /** Defines callbacks for service binding, passed to bindService() */
     private ServiceConnection mConnection = new ServiceConnection() {
 
         @Override
         public void onServiceConnected(ComponentName className, IBinder service) {
             CopService.CopBinder binder = (CopService.CopBinder) service;
             copService = binder.getService();
             copBound = true;
             updateServiceState();
         }
 
         @Override
         public void onServiceDisconnected(ComponentName arg0) {
             copBound = false;
             updateServiceState();
         }
     };
 }
