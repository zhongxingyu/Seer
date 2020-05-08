 package com.fleurey.android.androidremoteclient.service;
 
 import android.os.Handler;
 import android.os.Looper;
 import android.os.Message;
 import android.util.Log;
 import com.fleurey.android.androidremoteclient.network.NetworkManager;
 
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.nio.ByteBuffer;
 import java.util.regex.Pattern;
 
 public class NetworkHandler extends Handler {
 
     public static final String EXTRA_ADDRESS = NetworkHandler.class.getName() + ".EXTRA_ADDRESS";
     public static final String EXTRA_PORT = NetworkHandler.class.getName() + ".EXTRA_PORT";
 
     private static final String TAG = NetworkHandler.class.getSimpleName();
 
     public static final int MSG_CONNECT = 0;
     public static final int MSG_DISCONNECT = 1;
     public static final int MSG_EVENT = 2;
 
     private NetworkManager networkManager;
 
     public NetworkHandler(Looper looper, NetworkManager networkManager) {
         super(looper);
         this.networkManager = networkManager;
     }
 
     @Override
     public void handleMessage(Message msg) {
         switch (msg.what) {
             case MSG_CONNECT:
                String address = msg.getData().getString(EXTRA_ADDRESS);
                int port = msg.getData().getInt(EXTRA_PORT);
                networkManager.connect(buildInetAdress(address), port);
                 break;
             case MSG_DISCONNECT:
                 networkManager.disconnect();
                 getLooper().quit();
                 break;
             case MSG_EVENT:
                 networkManager.sendEvent(msg.arg1);
                 break;
         }
     }
 
     private InetAddress buildInetAdress(String address) {
         if (!Pattern.matches("(\\d{1,3}\\.){3}\\d{1,3}", address)) {
             Log.e(TAG, "The address does not follow the IP pattern");
             return null;
         }
         String[] ip = Pattern.compile("\\.").split(address);
         byte[] ipInByte = new byte[]{(byte) Integer.parseInt(ip[0]), (byte) Integer.parseInt(ip[1]), (byte) Integer.parseInt(ip[2]), (byte) Integer.parseInt(ip[3])};
         try {
             return InetAddress.getByAddress(ipInByte);
         } catch (UnknownHostException e) {
             Log.e(TAG, "Can't build InetAddress", e);
             return null;
         }
     }
 }
