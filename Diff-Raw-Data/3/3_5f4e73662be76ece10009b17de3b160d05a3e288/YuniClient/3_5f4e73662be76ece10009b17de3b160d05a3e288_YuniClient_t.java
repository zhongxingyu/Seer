 package com.yuniclient;
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.util.Set;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothDevice;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.content.pm.ActivityInfo;
 import android.content.res.Configuration;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.os.PowerManager.WakeLock;
 import android.preference.PreferenceManager;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.animation.Animation;
 import android.view.animation.LinearInterpolator;
 import android.view.animation.TranslateAnimation;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.ScrollView;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.ViewFlipper;
 
 import com.yuni.client.R;
 
 public class YuniClient extends Activity
 {
     private static final int REQUEST_ENABLE_BT = 2;
 
     public static final int FILE_LOADED = 3;
     public static final String DEVICE_NAME = "device_name";
     public static final String TOAST = "toast";
     public static final String EXTRA_DEVICE_ADDRESS = "device_address";
     
     private static final byte ACCELEROMETER_REQ_CODE = 1;
 
     public static final byte STATE_CONNECTED        = 0x01;
     public static final byte STATE_CONTROLS         = 0x02;
     public static final byte STATE_STOPPING         = 0x04;
     public static final byte STATE_STOPPED          = 0x08;
     public static final byte STATE_WAITING_ID       = 0x10;
     public static final byte STATE_FLASHING         = 0x20;
     public static final byte STATE_SCROLL           = 0x40;
     public static final short STATE_ACCELEROMETER   = 0x80;
     public static final short STATE_JOYSTICK        = 0x100;
     public static final short STATE_TERMINAL        = 0x200;
 
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         context = this;
         eeprom_part = 1;
         eeprom_write_part = 1;
         controlAPI.InitInstance();
         terminal = new Terminal();
         
         setContentView(R.layout.device_list);
         IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
         IntentFilter filterBTChange = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
         registerReceiver(mReceiver, filter);
         registerReceiver(mBTStateChangeReceiver, filterBTChange);
         
         mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
         if (mBluetoothAdapter == null)
             ShowAlert("This device does not have bluetooth adapter");
         else if(!mBluetoothAdapter.isEnabled())
             EnableBT();
         System.loadLibrary("jni_functions");
         init();
     }
 
     public void onDestroy() 
     {
         super.onDestroy();
         if(isFinishing())
         {
             Disconnect(false);
             unregisterReceiver(mReceiver);
             unregisterReceiver(mBTStateChangeReceiver);
         }
     }
 
     public void onActivityResult(int requestCode, int resultCode, Intent data)
     {
         if(requestCode != REQUEST_ENABLE_BT)
             return;
 
         if (resultCode == Activity.RESULT_OK)
         {
             switch(btTurnOn)
             {
                 case 1:
                     FindDevices();
                     break;
                 case 2:
                     Connect(connectView);
                     break;
                 case 0:
                     Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices(); 
                     if (pairedDevices.size() > 0) {
                         mPairedDevices.clear();
                         findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
                         for (BluetoothDevice device : pairedDevices) {
                             mPairedDevices.add(device.getName() + "\n" + device.getAddress());
                         }
                     }
                     break;
             }
             btTurnOn = 0;
             connectView = null;
         }
         else if(btTurnOn != 0)
            ShowAlert("Bluetooth is disabled!");
     } 
     
     @Override
     public void onConfigurationChanged(Configuration newConfig)
     {
         super.onConfigurationChanged(newConfig);
            
         // need to reload layout, because it is different
         // see /res/layout/controls.xml and /res/layout-land/controls.xml
         if((state & STATE_CONTROLS) != 0)
         {
             setContentView(R.layout.controls);     
             InitControls();
             ((TextView) findViewById(R.id.output)).setText(terminal.GetText());
         }
         if((state & STATE_CONTROLS) != 0 || (state & STATE_TERMINAL) != 0)
             state |= STATE_SCROLL;
     }
     
     @Override
     public boolean onKeyDown(int keyCode, KeyEvent event)
     {
         if ((keyCode == KeyEvent.KEYCODE_BACK))
         {
               if((state & STATE_CONTROLS) != 0 || (state & STATE_JOYSTICK) != 0 || (state & STATE_TERMINAL) != 0 || (state & STATE_ACCELEROMETER) != 0)
               {
                   if((state & STATE_JOYSTICK) != 0)
                       this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                   InitMain();
               }
               else if((state & STATE_CONNECTED) != 0)
                   Disconnect(true);
               else
                   finish();
               return true;
         }
         else if(keyCode == KeyEvent.KEYCODE_MENU)
         {
             if((state & STATE_TERMINAL) != 0 || state == 0 || (state & STATE_CONTROLS) != 0)
                 return super.onKeyDown(keyCode, event);
             else if((state & STATE_JOYSTICK) != 0)
                 ShowAPIDialog(true);
             else
                 startActivity(new Intent(this, Settings.class));
             return true;
         }
         return super.onKeyDown(keyCode, event);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu)
     {
         prepareMenu(menu);
         return true;
     }
 
     @Override
     public boolean onPrepareOptionsMenu (Menu menu)
     {
         prepareMenu(menu);
         return true;
     }
 
     private void prepareMenu(Menu menu)
     {
         MenuInflater inflater = getMenuInflater();
         int id = -1;
         if((state & STATE_TERMINAL) != 0 && menu.findItem(R.id.send_byte) == null)           
             id = R.menu.menu_terminal;
         else if((state & STATE_CONTROLS) != 0 && menu.findItem(R.id.protocol) == null)
             id = R.menu.menu_controls;
         else if(state == 0 && menu.findItem(R.id.exit) == null)
             id = R.menu.menu2;
         else return;
         menu.clear();
         inflater.inflate(id, menu);
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item)
     {
         // Handle item selection
         switch (item.getItemId())
         {
             case R.id.exit:
                 finish();
                 return true;
             case R.id.settings:
                 startActivity(new Intent(this, Settings.class));
                 return true;
             case R.id.protocol:
                 ShowAPIDialog(false);
                 return true;
             case R.id.clear:
                 ((TextView)findViewById(R.id.output_terminal)).setText("");
                 terminal.SetText(null);
                 return true;
             case R.id.send_string:
             {
                 AlertDialog.Builder builder;
                 alertDialog = null;
                 LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
                 View layout = inflater.inflate(R.layout.save_data,
                                                (ViewGroup) findViewById(R.id.layout_root));
                 builder = new AlertDialog.Builder(context);
                 builder.setView(layout);
                 builder.setNeutralButton("Send", new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface arg0, int arg1) {
                        EditText text = (EditText)alertDialog.findViewById(R.id.data_file_save);
                        byte[] out = new byte[text.getText().length()];
                        for(short i = 0; i < text.getText().length(); ++i)
                            out[i] = (byte) text.getText().charAt(i);
                        Connection.GetInst().write(out.clone());
                        Toast.makeText(context, "Text \"" + text.getText().toString() + "\" sent",
                                Toast.LENGTH_SHORT).show();
                    }
                 });
                 builder.setTitle("Send string");
                 alertDialog = builder.create();
                 alertDialog.setCancelable(true);
                 alertDialog.show();
                 return true;
             }
             case R.id.send_byte:
             {
                 AlertDialog.Builder builder;
                 alertDialog = null;
                 LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
                 View layout = inflater.inflate(R.layout.save_data,
                                                (ViewGroup) findViewById(R.id.layout_root));
                 builder = new AlertDialog.Builder(context);
                 builder.setView(layout);
                 builder.setNeutralButton("Send", new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface arg0, int arg1) {
                        EditText text = (EditText)alertDialog.findViewById(R.id.data_file_save);
                        try
                        {
                            byte[] out = { Integer.valueOf(text.getText().toString()).byteValue() };
                            Connection.GetInst().write(out.clone());
                            Toast.makeText(context, "Byte number \"" + text.getText().toString() + "\" sent",
                                    Toast.LENGTH_SHORT).show();
                        }
                        catch(NumberFormatException e)
                        {
                            Toast.makeText(context, "Wrong format!", Toast.LENGTH_SHORT).show();
                        }
                    }
                 });
                 builder.setTitle("Send byte");
                 alertDialog = builder.create();
                 alertDialog.setCancelable(true);
                 alertDialog.show();
                 return true;
             }
             case R.id.terminal_parse:
             {
                 final CharSequence[] items = {"Text", "Hex", "Byte", "Packets"};
 
                 AlertDialog.Builder builder = new AlertDialog.Builder(this);
                 builder.setTitle("Choose parser");
                 builder.setSingleChoiceItems(items, terminal.GetCurrentParser(), new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int item) {
                         terminal.SetCurrentParser((byte) item);
                         SetTerminalText(terminal.GetText(), false);
                         dialog.dismiss();
                     }
                 });
                 AlertDialog alert = builder.create();
                 alert.show();
                 return true;
             }
             case R.id.save_log:
             {
                 if(terminal.GetText() == null || terminal.GetText() == "")
                 {
                     Toast.makeText(context, "Terminal is empty!", Toast.LENGTH_SHORT).show();
                     return true;
                 }
                 AlertDialog.Builder builder;
                 alertDialog = null;
                 LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
                 View layout = inflater.inflate(R.layout.save_data,
                                                (ViewGroup) findViewById(R.id.layout_root));
                 builder = new AlertDialog.Builder(context);
                 builder.setView(layout);
                 builder.setNeutralButton("Save", saveLogFile);
                 builder.setTitle("Chose filename");
                 alertDialog = builder.create();
                 alertDialog.show();
                 return true;
             }
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
     
     private void Disconnect(boolean resetUI)
     {
         // finish possible accelerometer activity, it would crash because of access to connection
         finishActivity(ACCELEROMETER_REQ_CODE);
         state = 0;
         curFolder = null;
         curFolder = null;
         keyTouch = null;
         fileSelect = null;
         dialog = null;
         autoScrollThread = null;
         alertDialog = null;
         if(log != null)
             log.close();
         log = null;
         if(lock != null)
             lock.release();
 
         controlAPI.GetInst().SetDefXY(0, 0);
         joystick = null;
         if(Connection.GetInst() != null)
             Connection.GetInst().cancel();
         Connection.Destroy();
         if(resetUI)
         {
             setContentView(R.layout.device_list);
             init();
         }
         else
         {
             context = null;
             mBluetoothAdapter = null;
         }
     }
         
     private void EnableConnect(boolean enable)
     {
         if(!enable)
         {
             dialog= new ProgressDialog(this);
             dialog.setCancelable(true);
             dialog.setMessage("Connecting...");
             dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);    
             dialog.setMax(0);
             dialog.setProgress(0);
             dialog.setOnCancelListener(new Dialog.OnCancelListener()
             {
                 public void onCancel(DialogInterface dia)
                 {
                     if(Connection.GetInst() != null)
                         Connection.GetInst().cancel();
                     Connection.Destroy();
                     EnableConnect(true);
                 }
             });
             dialog.show();
         }
         else
         {
             Connection.Destroy();
             dialog.dismiss();
         }
         Button button = (Button) findViewById(R.id.button_scan);
         button.setEnabled(enable);
         ListView listView = (ListView) findViewById(R.id.paired_devices);
         listView.setEnabled(enable);
     }
     
     private void EnableBT()
     {
         Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
         startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
     }
     
     private void FindDevices()
     {
         if(!mBluetoothAdapter.isEnabled())
         {
             btTurnOn = 1;
             EnableBT();
             return;
         }
         if (mBluetoothAdapter.isDiscovering())
             mBluetoothAdapter.cancelDiscovery();
 
         mArrayAdapter.clear();
         mPairedDevices.clear();
         
         Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices(); 
         if (pairedDevices.size() > 0)
         {
             findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
             for (BluetoothDevice device : pairedDevices)
                 mPairedDevices.add(device.getName() + "\n" + device.getAddress());
         }
         mBluetoothAdapter.startDiscovery();
     } 
     
     private void Connect(View v)
     {
         if(Connection.GetInst() != null)
             return;
         
         EnableConnect(false);
         
         // Get the device MAC address, which is the last 17 chars in the View
         String info = ((TextView) v).getText().toString();
         String address = info.substring(info.length() - 17);
 
         BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
         if(device != null)
             Connection.InitInstance(connectionHandler, device);
     }
 
     private void ShowAlert(CharSequence text)
     {
         if(dialog != null)
             dialog.dismiss();
         AlertDialog.Builder builder2 = new AlertDialog.Builder(context);
         builder2.setMessage(text)
                .setTitle("Error")
                .setPositiveButton("Dismiss", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        dialog.dismiss();
                    }
                });
         AlertDialog alert = builder2.create();
         alert.show();
     }
 
     // INITS
     public void init()
     {
         eeprom_part = 1;
         mPairedDevices = new ArrayAdapter<String>(this, R.layout.device_name);
         mArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
         ListView pairedListView = (ListView) findViewById(R.id.paired_devices);
         pairedListView.setAdapter(mPairedDevices);
         pairedListView.setOnItemClickListener(mDeviceClickListener);
         
         Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices(); 
         if (pairedDevices.size() > 0) {
             findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
             for (BluetoothDevice device : pairedDevices)
                 mPairedDevices.add(device.getName() + "\n" + device.getAddress());
         }
         
         final Button button = (Button) findViewById(R.id.button_scan);
         button.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v)
             {
                 mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                 if (mBluetoothAdapter != null)
                     FindDevices();
             }
         });
         if(EEPROMTouchLastX == null)
         {
             EEPROMTouchLastX = new int[50];
             EEPROMTouchLastY = new int[50];
         }
         pairedListView.setOnTouchListener(new View.OnTouchListener()
         {
             public boolean onTouch(View v, MotionEvent event)
             {
                 if(event.getAction() == MotionEvent.ACTION_MOVE)
                 {
                     if(EEPROMTouchItr >= 50)
                         EEPROMTouchItr = 0;
                     EEPROMTouchLastX[EEPROMTouchItr] = (int) event.getX();
                     EEPROMTouchLastY[EEPROMTouchItr] = (int) event.getY();
                     ++EEPROMTouchItr;
                 }
                 else if(event.getAction() == MotionEvent.ACTION_DOWN)
                     EEPROMTouchItr = 0;
                 else if(event.getAction() == MotionEvent.ACTION_UP && EEPROMTouchItr != 0)
                 {
                     boolean right = false;
                     boolean correct = fabs(EEPROMTouchLastX[0] - EEPROMTouchLastX[EEPROMTouchItr-1]) > 30 && // X movement must be bigger than 30px
                       // and x movement must be bigger than Y movement
                       fabs(EEPROMTouchLastY[0] - EEPROMTouchLastY[EEPROMTouchItr-1]) < fabs(EEPROMTouchLastX[0] - EEPROMTouchLastX[EEPROMTouchItr-1]);
                     
                     for(byte i = 1; i < EEPROMTouchItr && correct; ++i)
                     {
                         if(i == 1 && EEPROMTouchLastX[i-1] < EEPROMTouchLastX[i])
                             right = true;
                         else if((EEPROMTouchLastX[i-1] < EEPROMTouchLastX[i] && !right) || 
                                 (EEPROMTouchLastX[i-1] > EEPROMTouchLastX[i] && right))
                             correct = false;
                     }
                     if(correct)
                         ChangeDevicesPart(true, right);
                 }
                 return false;
             }
 
             private int fabs(int i) {
                 if(i >= 0) return i;
                 return -i;
             }
         });
         
     }
     
     private static void ButtonTouched(CharSequence button, boolean down)
     {
         byte[] out = new byte[2];
         out[0] = (byte)button.charAt(0);
 
         if(down)
             out[1] = (byte)'d';
         else
             out[1] = (byte)'u';
         Connection.GetInst().write(out);
     }
     
     private void ChangeDevicesPart(boolean animation, boolean right)
     {
         final ViewFlipper flipper = (ViewFlipper) findViewById(R.id.flipper_devices);
         flipper.setInAnimation(right? inFromLeftAnimation() : inFromRightAnimation());
         flipper.showNext(); 
         final ListView list = (ListView) findViewById(R.id.paired_devices); 
         TextView header = (TextView)findViewById(R.id.title_paired_devices);
         Button button = (Button) findViewById(R.id.button_scan);
         
         if(eeprom_part == 1)
         {
             eeprom_part = 2;
             button.setVisibility(Button.VISIBLE);
             header.setText("New devices");
             list.setAdapter(mArrayAdapter);
         }
         else
         {
             eeprom_part = 1;
             button.setVisibility(Button.GONE);
             header.setText("Paired devices");
             list.setAdapter(mPairedDevices);
         }
     }
 
     private static void SendMovementKey(byte button, boolean down)
     {
         byte[] out = controlAPI.GetInst().BuildMovementPacket(button, down, (byte) 0);
         if(out != null)
             Connection.GetInst().write(out);     
     }
     
     private void InitMain()
     {
         if(autoScrollThread != null)
             autoScrollThread.cancel();
         autoScrollThread = null;
         joystick = null;
         state &= ~(STATE_CONTROLS);
         state &= ~(STATE_JOYSTICK);
         state &= ~(STATE_TERMINAL);  
 
         context = this;
         if(lock != null)
         {
             lock.release();
             lock = null;
         }
         SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
         curFolder = new File(sp.getString("hex_folder", getString(R.string.hex_folder_def)));
         fileSelect = new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int item) {
                 dialog.dismiss();
                 FilenameFilter filter = new HexFilter();
                 final CharSequence[] items = curFolder.list(filter);
                 Toast.makeText(getApplicationContext(), items[item], Toast.LENGTH_SHORT).show();
                 File file = new File(curFolder, items[item].toString());
                 Message msg = new Message();
                 msg.obj = file;
                 fileClick.sendMessage(msg);
             }
         };
         setContentView(R.layout.main);
         
         StartStop((Button) findViewById(R.id.Start_b), ((state & STATE_STOPPED) == 0), true);
         
         Button button = (Button) findViewById(R.id.Disconnect_b);
         button.setOnClickListener(new View.OnClickListener() {
              public void onClick(View v) {
                 Disconnect(true);
              }
         });
 
         button = (Button) findViewById(R.id.joystick_b);
         button.setOnClickListener(new View.OnClickListener() {
              public void onClick(View v) {
                 if((state & STATE_STOPPED) == 0)
                     InitJoystick();
              }
         });
         button = (Button) findViewById(R.id.Controls_b);
         button.setOnClickListener(new View.OnClickListener() {
              public void onClick(View v) {
                 if((state & STATE_STOPPED) == 0)
                     InitControls();
              }
         });
         button = (Button) findViewById(R.id.Start_b);
         button.setOnClickListener(new View.OnClickListener() {
              public void onClick(View v) {
                 StartStop((Button)v, ((state & STATE_STOPPED) != 0), false);
              }
         });
         button = (Button) findViewById(R.id.Terminal_b);
         button.setOnClickListener(new View.OnClickListener() {
              public void onClick(View v)
              {
                  InitTerminal();
              }
         });
         button = (Button) findViewById(R.id.Flash_b);
         button.setOnClickListener(new View.OnClickListener() {
              public void onClick(View v) {
                 TextView error = (TextView)findViewById(R.id.hex_file);
                 File hex = new File(error.getText().toString());
                 error = (TextView)findViewById(R.id.error);
                 if(hex.exists() && hex.canRead())
                 {
                     error.setText("Hex file exists\n");
                     Connection.GetInst().setHexFile(hex);
                     final byte[] out = { 0x12 };
                     Connection.GetInst().write(out.clone());
                     state |= STATE_WAITING_ID;
                     error.append("Waiting for ID and preparing hex file...");
                 }
                 else
                     error.setText("Hex file does not exists or can not be read\n");
              }
         });
 
         button = (Button) findViewById(R.id.List_b);
         button.setOnClickListener(new View.OnClickListener() {
              public void onClick(View v) {
                     final AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                     SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
                     curFolder = new File(sp.getString("hex_folder", getString(R.string.hex_folder_def)));
                     FilenameFilter filter = new HexFilter();
                     final CharSequence[] items = curFolder.list(filter);
                                         
                     builder.setTitle("Chose file");
                     builder.setItems(items, fileSelect);
                     final AlertDialog alert = builder.create();
                     alert.show();
              }
         });
         
         button = (Button) findViewById(R.id.accelerometer_b);
         button.setOnClickListener(new View.OnClickListener() {
              public void onClick(View v) {
                  startActivityForResult(new Intent(context, Accelerometer.class), ACCELEROMETER_REQ_CODE);
              }
         });
     }
         
     private void StartStop(Button v, boolean start, boolean visualOnly)
     {
         byte[] out = null;
         if(start)
         {
             out = new byte[1];
             out[0] = 0x11;
             state &= ~(STATE_STOPPED);
             v.setText("Stop");
         }
         else
         {
             if(!visualOnly)
             {
                 out = new byte[4];
                 out[0] = 0x74; out[1] = 0x7E; out[2] = 0x7A; out[3] = 0x33;
                 Connection.GetInst().write(out.clone());
                 try {
                     Thread.sleep(300);
                 } catch (InterruptedException e) {
                     e.printStackTrace();
                 }
                 TextView error = (TextView)findViewById(R.id.error);
                 error.setText("Stopping...");
                 state |= STATE_STOPPING;
             }
             v.setText("Start");
         }
         v = (Button) findViewById(R.id.Controls_b);
         v.setEnabled(start);
         v.setClickable(start);
         v = (Button) findViewById(R.id.joystick_b);
         v.setEnabled(start);
         v.setClickable(start);
         v = (Button) findViewById(R.id.accelerometer_b);
         v.setEnabled(start);
         v.setClickable(start);
         v = (Button) findViewById(R.id.Flash_b);
         v.setEnabled(!start);
         v.setClickable(!start);
         if(!visualOnly)
             Connection.GetInst().write(out.clone());
     }
     
     private void InitControls()
     {
         state |= STATE_CONTROLS;
         setContentView(R.layout.controls);
         
         Button button = (Button) findViewById(R.id.Forward_b);
         button.setOnTouchListener(keyTouch); 
        
         button = (Button) findViewById(R.id.Backward_b);
         button.setOnTouchListener(keyTouch); 
         button = (Button) findViewById(R.id.Left_b);
         button.setOnTouchListener(keyTouch); 
         button = (Button) findViewById(R.id.Right_b);
         button.setOnTouchListener(keyTouch);
         button = (Button) findViewById(R.id.LeftForw_b);
         button.setOnTouchListener(keyTouch);
         button = (Button) findViewById(R.id.RightForw_b);
         button.setOnTouchListener(keyTouch);
         
         button = (Button) findViewById(R.id.Speed1_b);
         button.setOnTouchListener(keyTouch);
         button = (Button) findViewById(R.id.Speed2_b);
         button.setOnTouchListener(keyTouch);
         button = (Button) findViewById(R.id.Speed3_b);
         button.setOnTouchListener(keyTouch);
         
         button = (Button) findViewById(R.id.Space_b);
         button.setOnTouchListener(keyTouch);
         button = (Button) findViewById(R.id.Record_b);
         button.setOnTouchListener(keyTouch);
         button = (Button) findViewById(R.id.Play_b);
         button.setOnTouchListener(keyTouch);
         button = (Button) findViewById(R.id.Regulator_b);
         button.setOnTouchListener(keyTouch);
         
         button = (Button) findViewById(R.id.Clear_b);
         button.setOnClickListener(new View.OnClickListener() {
              public void onClick(View v) {
                 TextView out = (TextView) findViewById(R.id.output);
                 out.setText("");
                 terminal.SetText(null);
              }
            });
         
         
         if(autoScrollThread != null)
             autoScrollThread.cancel();
         autoScrollThread = new ScrollThread((TextView) findViewById(R.id.output), (ScrollView) findViewById(R.id.ScrollView01));
         autoScrollThread.setPriority(1);
         autoScrollThread.start();
         
         if(terminal.GetText() != null)
         {
             TextView out = (TextView) findViewById(R.id.output);
             out.setText(terminal.GetText());
             state |= STATE_SCROLL;
         }
     }
 
     private void ShowAPIDialog(boolean packetOnly)
     {
         final CharSequence[] items;
         if(packetOnly)
         {
             items = new CharSequence[3];
             items[0] = "Packets"; items[1] = "Chessbot"; items[2] = "Quorra";
         }
         else
         {
             items = new CharSequence[5];
             items[0] = "Keyboard"; items[1] = "YuniRC"; items[2] = "Packets"; items[3] = "Chessbot"; items[4] = "Quorra";
         }
 
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setTitle("Choose control API");
         int selected = packetOnly ? controlAPI.GetInst().GetAPIType()-2 : controlAPI.GetInst().GetAPIType(); 
         builder.setSingleChoiceItems(items, selected, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int item) {
                 controlAPI.GetInst().SetAPIType(controlAPI.GetAPITypeFromString(items[item]));
                 Toast.makeText(context, items[item] + " has been chosen as control API.", Toast.LENGTH_SHORT).show();
                 dialog.dismiss();
                 
                 if(!controlAPI.IsTargetSpeedDefined(controlAPI.GetAPITypeFromString(items[item])))
                     OpenSpeedDialog();
             }
         });
         AlertDialog alert = builder.create();
         alert.show();
     }
     
     private void OpenSpeedDialog()
     {
         AlertDialog.Builder builder = new AlertDialog.Builder(context);
         builder.setTitle("Set speed");
         LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
         View layout = inflater.inflate(R.layout.save_data,
                                        (ViewGroup) findViewById(R.id.layout_root));
         ((TextView)layout.findViewById(R.id.data_file_save)).setText("255");
         builder.setView(layout);
         builder.setNeutralButton("Set", new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface arg0, int arg1) {
                EditText text = (EditText)alertDialog.findViewById(R.id.data_file_save);
                int speed = 255;
                try
                {
                    speed = Integer.valueOf(text.getText().toString());
                }
                catch(NumberFormatException e)
                {
                    Toast.makeText(context, "Wrong format!", Toast.LENGTH_SHORT).show();
                }
 
                controlAPI.GetInst().SetQuarraSpeed(speed);
            }
         });
         alertDialog = builder.create();
         alertDialog.show();
     }
     
     private void InitJoystick()
     {
         joystick = new Joystick();
         setContentView(joystick.new MTView(this));
         state |= STATE_JOYSTICK;
         if(!controlAPI.HasPacketStructure(controlAPI.GetInst().GetAPIType()))
         {
             controlAPI.GetInst().SetAPIType(controlAPI.API_PACKETS);
             Toast.makeText(context, "Packets has been chosen as control API.", Toast.LENGTH_SHORT).show();
         }
         this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
     }
 
     private void InitTerminal()
     {
         state |= STATE_TERMINAL;
         setContentView(R.layout.terminal);
         
         autoScrollThread = new ScrollThread((TextView) findViewById(R.id.output_terminal), (ScrollView) findViewById(R.id.ScrollViewTerminal));
         autoScrollThread.setPriority(1);
         autoScrollThread.start();
         
         if(terminal.GetText() != null)
         {
             TextView out = (TextView) findViewById(R.id.output_terminal);
             out.setText(terminal.GetText());
             state |= STATE_SCROLL;
         }
     }
     
     private void WriteTerminalText(String text)
     {
         if(text == null)
             return;
         if((state & STATE_CONTROLS) != 0 || (state & STATE_TERMINAL) != 0)
         {
             final TextView out = (TextView) findViewById(((state & STATE_CONTROLS) != 0) ? R.id.output : R.id.output_terminal);
             if(out != null)
             {
                 out.append(Terminal.Parse(text));
                 state |= STATE_SCROLL;
             }
         }
         terminal.Append(text);
     }
 
     private void SetTerminalText(String text, boolean toClass)
     {
         if((state & STATE_CONTROLS) != 0 || (state & STATE_TERMINAL) != 0)
         {
             final TextView out = (TextView) findViewById(((state & STATE_CONTROLS) != 0) ? R.id.output : R.id.output_terminal);
             if(out != null)
             {
                 out.setText(text);
                 state |= STATE_SCROLL;
             }
         }
         if(toClass)
             terminal.SetText(text);
     }
  
     // ============================================ HANDLERS ============================================  
     private View.OnTouchListener keyTouch = new View.OnTouchListener()
     {
         public boolean onTouch(View v, MotionEvent event)
         {
             if(event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_UP)
             {
                 boolean down = event.getAction() == MotionEvent.ACTION_DOWN;
                 switch(((Button)v).getId())
                 {
                     case R.id.LeftForw_b:
                         if(controlAPI.HasPacketStructure(controlAPI.GetInst().GetAPIType()))
                             SendMovementKey((byte) (controlAPI.MOVE_FORWARD | controlAPI.MOVE_LEFT), down);
                         else
                         {
                             SendMovementKey(controlAPI.MOVE_FORWARD, down);
                             SendMovementKey(controlAPI.MOVE_LEFT, down);
                         }
                         return false;
                     case R.id.RightForw_b:
                         if(controlAPI.HasPacketStructure(controlAPI.GetInst().GetAPIType()))
                             SendMovementKey((byte) (controlAPI.MOVE_FORWARD | controlAPI.MOVE_RIGHT), down);
                         else
                         {
                             SendMovementKey(controlAPI.MOVE_FORWARD, down);
                             SendMovementKey(controlAPI.MOVE_RIGHT, down);
                         }
                         return false;
                     case R.id.Forward_b:  SendMovementKey(controlAPI.MOVE_FORWARD, down);  return false;
                     case R.id.Backward_b: SendMovementKey(controlAPI.MOVE_BACKWARD, down); return false;
                     case R.id.Left_b:     SendMovementKey(controlAPI.MOVE_LEFT, down);     return false;
                     case R.id.Right_b:    SendMovementKey(controlAPI.MOVE_RIGHT, down);    return false;
                     case R.id.Space_b:    YuniClient.ButtonTouched(" ", down);             return false;
                     default:              YuniClient.ButtonTouched(((Button)v).getText(), down); return false;        
                 }
             }
             return false;
         }
     };
     
     private final OnClickListener saveLogFile = new OnClickListener()
     {
         public void onClick(DialogInterface dialog, int id) {
             EditText text = (EditText)alertDialog.findViewById(R.id.data_file_save);
             String filename = text.getText().toString();
             if(filename == null || filename == "")
             {
                 Toast.makeText(context, "Filename is empty!", Toast.LENGTH_SHORT).show();
                 return;
             }
             SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
             File folder = new File(sp.getString("log_folder", getString(R.string.log_folder_def)));
             if(!folder.exists())
                 folder.mkdirs();
             try {
                 terminal.toFile(filename, toastHandler);
             } catch (IOException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
        }
     };
 
     final BroadcastReceiver mReceiver = new BroadcastReceiver() {
         public void onReceive(Context context, Intent intent) {
             String action = intent.getAction();
             // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action) && mArrayAdapter != null) {
                 // Get the BluetoothDevice object from the Intent
                 BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                 // Add the name and address to an array adapter to show in a ListView
                 mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
             }
         }
     };
     
     final BroadcastReceiver mBTStateChangeReceiver = new BroadcastReceiver()
     {
         public void onReceive(Context context, Intent intent)
         {
             int stateBT = intent.getExtras().getInt(BluetoothAdapter.EXTRA_STATE);
             if((state & STATE_CONNECTED) != 0 && stateBT == BluetoothAdapter.STATE_TURNING_OFF)
                 Disconnect(true);
         }
     };
     
     private final OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
         public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
             if(!mBluetoothAdapter.isEnabled())
             {
                 btTurnOn = 2;
                 connectView = v;
                 EnableBT();
                 return;
             }
             Connect(v);
         }
     }; 
     
     private final Handler fileClick = new Handler() {
         @Override
         public void handleMessage(Message msg) {
             File file = (File)msg.obj;
             if(!file.isDirectory())
             {
                 TextView error = (TextView)findViewById(R.id.hex_file);
                 error.setText(file.getAbsolutePath());
             }
             else
             {
                 curFolder = file;
                 AlertDialog.Builder builder = new AlertDialog.Builder(context);
                 FilenameFilter filter = new HexFilter();
                 final CharSequence[] items = curFolder.list(filter);              
                 builder.setTitle("Chose file");
                 builder.setItems(items, fileSelect);
                 AlertDialog alert = builder.create();
                 alert.show();
             }
         }
     };
 
     public final Handler ballHandler = new Handler() {
         @Override
         public void handleMessage(Message msg)
         {
             MotionEvent event = (MotionEvent)msg.obj;
             float y = event.getRawY() - (getWindowManager().getDefaultDisplay().getHeight() - msg.arg2*2);
             
             byte[] flags = joystick.touchEvent(event.getAction(), event.getX(), y, msg.arg1, msg.arg2);
             
             if(flags == null)
                 return;
             byte[] data = controlAPI.GetInst().BuildMovementPacket(flags[0], true, flags[1]);
             if(data != null)
                 Connection.GetInst().write(data.clone());
         }
     };
     
     public final Handler toastHandler = new Handler() {
         @Override
         public void handleMessage(Message msg)
         {
             final String text = msg.getData().getString(TOAST);
             if(text == null)
                 return;
             Toast.makeText(context, text,
                 Toast.LENGTH_SHORT).show();
         }
     };
 
     private final Handler connectionHandler = new Handler() {
         public void handleMessage(Message msg)
         {
             switch(msg.what)
             {
                 case Connection.CONNECTION_STATE:
                 {
                     switch(msg.arg1)
                     {
                         case BluetoothChatService.STATE_CONNECTED:
                             dialog.dismiss();
                             InitMain();
                             state |= STATE_CONNECTED;
                             return;
                         case Connection.CONNECTION_FAILED:
                         {
                             Toast.makeText(context, "Unable to connect",
                                 Toast.LENGTH_SHORT).show();
                             EnableConnect(true);
                             return;
                         }
                         case Connection.CONNECTION_LOST:
                         {
                             Toast.makeText(context, "Connection lost!",
                                 Toast.LENGTH_SHORT).show();
                             Disconnect(true);
                             return;
                         }
                     }
                     return;
                 }
                 case Connection.CONNECTION_DATA:
                 {
                     switch(msg.arg1)
                     {
                         case Connection.DATA_TEXT:
                             WriteTerminalText((String)msg.obj);
                             return;
                         case Connection.DATA_STOPPED:
                         {
                             state &= ~(STATE_STOPPING);
                             final TextView error = (TextView)findViewById(R.id.error);
                             error.setText("");
                             state |= STATE_STOPPED;
                             return;
                         }
                         case Connection.DATA_ID_RESPONSE:
                             dialog.dismiss();
                             dialog= new ProgressDialog(context);
                             dialog.setCancelable(false);
                             dialog.setMessage("Flashing into " + ((DeviceInfo)msg.obj).name + "...");
                             dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                             dialog.setMax(100);
                             dialog.setProgress(0);
                             dialog.show();
                             state &= ~(STATE_WAITING_ID);
                             return;
                         case Connection.DATA_FLASH:
                             if(msg.arg2 == 1)
                             {
                                 dialog.setMax(msg.getData().getInt("pagesCount"));
                                 state |= STATE_FLASHING;
                             }
                             else
                             {
                                 final String text = msg.getData().getString("text");
                                 ShowAlert(text);
                                 dialog.dismiss();
                             }
                             return;
                         case Connection.DATA_FLASH_PAGE:
                         {
                             if(msg.arg2 == 1)
                                 dialog.incrementProgressBy(1);
                             else
                             {
                                 state &= ~(STATE_FLASHING);
                                 TextView error = (TextView)findViewById(R.id.error);
                                 error.setText("Flashing done");
                                 dialog.dismiss();
                             }
                             return;
                         }
                     } // switch(msg.arg1)
                     return;
                 }
             } // switch(msg.what)
         }
     };
     
     private class ScrollThread extends Thread
     {
         private TextView out;
         private ScrollView scroll;
         private boolean run;
         
         public ScrollThread(TextView outView, ScrollView scrollView)
         {
             out = outView;
             scroll = scrollView;
             run = true;
         }
         
         public void run()
         {
             setName("AutoScrollThread");
             while(run)
             {
                 if((state & STATE_SCROLL) != 0 && scroll != null && out != null && scroll.getScrollY() != out.getHeight())
                 {
                     out.post(new Runnable() {
                         public void run()
                         {
                             if(scroll != null && out != null)
                                 scroll.scrollTo(0, out.getHeight());
                         }
                       });
                     state &= ~(STATE_SCROLL);
                 }
                 try {
                     Thread.sleep(300);
                 } catch (InterruptedException e) {
                     // TODO Auto-generated catch block
                     e.printStackTrace();
                 }
             }
         }
         
         public void cancel()
         {
             run = false;
         }
     }
     
     public static int getState() { return state; }
     
     private Joystick joystick;
     private Terminal terminal;
     private LogFile log;
 
     private WakeLock lock;
     private View connectView;
     private BluetoothAdapter mBluetoothAdapter;
     private ArrayAdapter<String> mArrayAdapter;
     private ArrayAdapter<String> mPairedDevices;
     private DialogInterface.OnClickListener fileSelect;
     private AlertDialog alertDialog;
     private ProgressDialog dialog;
     private File curFolder; 
     private Context context;
     private ScrollThread autoScrollThread;
     
     private static int state;
     
     private byte btTurnOn;
     private int[] EEPROMTouchLastX;
     private int[] EEPROMTouchLastY;
     private byte EEPROMTouchItr;
     public byte eeprom_part;
     public byte eeprom_write_part;
 
     private Animation inFromRightAnimation() {
         Animation inFromRight = new TranslateAnimation(
             Animation.RELATIVE_TO_PARENT,  +1.0f, Animation.RELATIVE_TO_PARENT,  0.0f,
             Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,   0.0f
         );
         inFromRight.setDuration(100);
         inFromRight.setInterpolator(new LinearInterpolator());
         return inFromRight;
     }
 
     private Animation inFromLeftAnimation() {
     
         Animation inFromLeft = new TranslateAnimation(
             Animation.RELATIVE_TO_PARENT,  -1.0f, Animation.RELATIVE_TO_PARENT,  0.0f,
             Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,   0.0f
         );
         inFromLeft.setDuration(100);
         inFromLeft.setInterpolator(new LinearInterpolator());
         return inFromLeft;
     }
 }
