 package com.dreamtale.pintrestlike.share;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
 import java.util.UUID;
 
 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothDevice;
 import android.bluetooth.BluetoothServerSocket;
 import android.bluetooth.BluetoothSocket;
 import android.os.Handler;
 import android.util.Log;
 
 public class BluetoothService
 {
     public static final int MSG_BLUETOOTH_READ = 0x01;
     public static final int MSG_BLUTHTOOTH_STATE_CHANGE = 0x02;
     
     private static final String TAG = "BluetoothService";
     
     private static final String SDP_RECORD_NAME = "Pinterest";
     
     private static final UUID SDP_UUID = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");
     
     public static final int STATE_NONE = 0x01;
     public static final int STATE_CONNECTING = 0x02;
     public static final int STATE_CONNECTED = 0x03;
     
     private int state = STATE_NONE;
     
     private static BluetoothService service = null;
     private BluetoothAdapter adapter = null;
     
     private ArrayList<Handler> handlers = new ArrayList<Handler>();
     
     private AcceptThread acceptThread = null;
     private ConnectThread connectThread = null;
     private DataThread dataThread = null;
     
     private BluetoothService()
     {
         adapter = BluetoothAdapter.getDefaultAdapter();
     }
     
     public static BluetoothService getInstance()
     {
         if (null == service)
         {
             service = new BluetoothService();
         }
         return service;
     }
     
     public void addHandler(Handler handler)
     {
         handlers.add(handler);
     }
     
     public void removeHandler(Handler handler)
     {
         handlers.remove(handler);
     }
     
     public void connect(BluetoothDevice device)
     {
         if (null != connectThread)
         {
             connectThread.cancel();
             connectThread = null;
         }
         
         connectThread = new ConnectThread(device);
         connectThread.start();
         setState(STATE_CONNECTING);
     }
     
     public void start()
     {
         Log.d(TAG, "-------bluetooth service started--------");
         Log.d(TAG, "start bluetooth service");
         clearThread();
         acceptThread = new AcceptThread();
         acceptThread.start();
     }
     
     private synchronized void manageSocket(BluetoothSocket socket)
     {
         Log.d(TAG, "-------bluetooth service manage data socket--------");
         
         if (null != acceptThread)
         {
             acceptThread.cancel();
             acceptThread = null;
         }
         if (null != connectThread)
         {
             connectThread.cancel();
             connectThread = null;
         }
         
         dataThread = new DataThread(socket);
         dataThread.start();
         setState(STATE_CONNECTED);
     }
     
     public synchronized void clearThread()
     {
         if (null != acceptThread)
         {
             acceptThread.cancel();
             acceptThread = null;
         }
         if (null != connectThread)
         {
             connectThread.cancel();
             connectThread = null;
         }
         if (null != dataThread)
         {
             dataThread.cancel();
             dataThread = null;
         }
         setState(STATE_NONE);
     }
     
     public synchronized void write(byte[] data)
     {
         if (state == STATE_CONNECTED)
         {
             String log = new String(data);
             Log.d(TAG, "write data " + log);
             dataThread.write(data);
         }
     }
     
     public int getState()
     {
         return state;
     }
     
     public void setState(int state)
     {
         this.state = state;
         
         if (!handlers.isEmpty())
         {
             for (Handler handler : handlers)
             {
                 handler.obtainMessage(MSG_BLUTHTOOTH_STATE_CHANGE, state, -1).sendToTarget();
             }
         }
     }
     
     class AcceptThread extends Thread
     {
         BluetoothServerSocket serverSocket = null;
         
         AcceptThread()
         {
             BluetoothServerSocket tmp = null;
             setName("AcceptThread");
             try
             {
                 tmp = adapter.listenUsingInsecureRfcommWithServiceRecord(SDP_RECORD_NAME, SDP_UUID);
             }
             catch (IOException e)
             {
                 Log.d(TAG, "-------AcceptThread listenUsingInsecureRfcommWithServiceRecord cause exception--------" + e.toString());
                 e.printStackTrace();
             }
             serverSocket = tmp;
         }
         
         @Override
         public void run()
         {
             BluetoothSocket socket = null;
             try
             {
                if (null != serverSocket)
                {
                    socket = serverSocket.accept();
                }
             }
             catch (IOException e)
             {
                 Log.d(TAG, "-------AcceptThread accept cause exception--------" + e.toString());
                 e.printStackTrace();
             }
             
             if (null != socket)
             {
                 // Manage the socket.
                 manageSocket(socket);
 //                cancel();
             }
         }
         
         public void cancel()
         {
             Log.d(TAG, "-------AcceptThread calceled--------");
             if (null != serverSocket)
             {
                 try
                 {
                     serverSocket.close();
                 }
                 catch (IOException e)
                 {
                     Log.d(TAG, "-------AcceptThread cancel cause exception--------" + e.toString());
                     e.printStackTrace();
                 }
             }
         }
     }
     
     class ConnectThread extends Thread
     {
         BluetoothDevice device = null;
         BluetoothSocket socket = null;
         
         ConnectThread(BluetoothDevice device)
         {
            setName("ConnectThread");
            BluetoothSocket tmp = null;
            this.device = device;
            try
            {
               tmp = device.createInsecureRfcommSocketToServiceRecord(SDP_UUID);
            }
            catch (IOException e)
            {
                Log.d(TAG, "-------ConnectThread createInsecureRfcommSocketToServiceRecord cause exception--------" + e.toString());
                e.printStackTrace();
            }
            socket = tmp;
         }
         
         @Override
         public void run()
         {
             adapter.cancelDiscovery();
             
             try
             {
                 socket.connect();
                 Log.d(TAG, "-------ConnectThread connect success--------");
             }
             catch (IOException e)
             {
                 Log.d(TAG, "-------ConnectThread connect cause exception--------" + e.toString());
                 e.printStackTrace();
                 setState(STATE_NONE);
                 cancel();
                 return;
             }
             
             synchronized (BluetoothService.this)
             {
                 connectThread = null;
             }
             // Manage the socket.
             manageSocket(socket);
         }
         
         public void cancel()
         {
             Log.d(TAG, "-------ConnectThread calceled--------");
             if (null != socket)
             {
                 try
                 {
                     socket.close();
                 }
                 catch (IOException e)
                 {
                     Log.d(TAG, "-------ConnectThread cancel cause exception--------" + e.toString());
                     e.printStackTrace();
                 }
             }
         }
     }
     
     class DataThread extends Thread
     {
         BluetoothSocket socket = null;
         InputStream is = null;
         OutputStream os = null;
         
         DataThread(BluetoothSocket socket)
         {
             setName("DataThread");
             this.socket = socket;
             
             try
             {
                 is = socket.getInputStream();
                 os = socket.getOutputStream();
             }
             catch (IOException e)
             {
                 Log.d(TAG, "-------DataThread getInputStream/getOutputStream cause exception--------" + e.toString());
                 e.printStackTrace();
                 cancel();
             }
         }
         
         @Override
         public void run()
         {
             while (true)
             {
                 try
                 {
                     if (is.available() > 0)
                     {
                         Log.d(TAG, "-------DataThread read--------");
                         ByteBuffer data = ByteBuffer.allocate(1024);
                         int count = is.read(data.array());
                         if (!handlers.isEmpty())
                         {
                             for (Handler handler : handlers)
                             {
                                 handler.obtainMessage(MSG_BLUETOOTH_READ, count, -1, data).sendToTarget();
                             }
                         }
                     }
                 }
                 catch (IOException e)
                 {
                     Log.d(TAG, "-------DataThread read cause exception--------" + e.toString());
                     e.printStackTrace();
                     cancel();
                     break;
                 }
             }
         }
         
         public void write(byte[] data)
         {
             Log.d(TAG, "-------DataThread write--------");
             try
             {
                 os.write(data);
             }
             catch (IOException e)
             {
                 Log.d(TAG, "-------DataThread write cause exception--------" + e.toString());
                 e.printStackTrace();
                 cancel();
             }
         }
         
         public void cancel()
         {
             Log.d(TAG, "-------DataThread calceled--------");
             if (null != socket)
             {
                 try
                 {
                     socket.close();
                 }
                 catch (IOException e)
                 {
                     Log.d(TAG, "-------DataThread cancel cause exception--------" + e.toString());
                     e.printStackTrace();
                 }
             }
         }
     }
 }
