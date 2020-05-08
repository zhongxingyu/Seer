 package com.worthwhilegames.cardgames.shared.wifi;
 
 import android.content.Context;
import android.os.Build;
 import com.worthwhilegames.cardgames.shared.GameFactory;
 import com.worthwhilegames.cardgames.shared.Util;
 import com.worthwhilegames.cardgames.shared.connection.*;
 
 import java.io.IOException;
 
 /**
  * The Wifi implementation of a ServerSocket
  */
 public class WifiServerSocket implements IServerSocket {
 
     /**
      * The ServerSocket
      */
     private java.net.ServerSocket mServerSocket;
 
     /**
      * The Wifi MulticastLock
      */
     private IDnsWrapper dnsWrapper;
 
     /**
      * Create a new WifiServerSocket
      */
     public WifiServerSocket(Context ctx) {
        if (Util.shouldUseNsd && android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
             dnsWrapper = new NsdWrapper(ctx);
         } else {
             dnsWrapper = new JmDnsWrapper(ctx);
         }
 
         try {
             mServerSocket = new java.net.ServerSocket(GameFactory.getPortNumber(ctx), 0, Util.getLocalIpAddress());
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     /* (non-Javadoc)
      * @see com.worthwhilegames.cardgames.shared.connection.IServerSocket#setup()
      */
     @Override
     public void setup() {
         if (dnsWrapper != null) {
             dnsWrapper.setup();
         }
     }
 
     /* (non-Javadoc)
      * @see com.worthwhilegames.cardgames.shared.connection.IServerSocket#accept()
      */
     @Override
     public ISocket accept() throws IOException {
         if (mServerSocket != null) {
             return new WifiSocket(mServerSocket.accept());
         } else {
             return null;
         }
     }
 
     /* (non-Javadoc)
      * @see com.worthwhilegames.cardgames.shared.connection.IServerSocket#close()
      */
     @Override
     public void close() throws IOException {
         if (dnsWrapper != null) {
             dnsWrapper.close();
         }
 
         if (mServerSocket != null) {
             mServerSocket.close();
         }
     }
 }
