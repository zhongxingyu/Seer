 package com.droidorb;
 
 import java.io.IOException;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.Intent;
 import android.os.Binder;
 import android.os.IBinder;
 import android.util.Log;
 import android.widget.Toast;
 
 import com.droidorb.observer.MissedCallsContentObserver;
 import com.droidorb.observer.OnMissedCallListener;
 import com.droidorb.server.Client;
 import com.droidorb.server.Server;
 import com.droidorb.server.ServerListener;
 
 /**
  * Main background service to monitor accessory
  * 
  * @author toby
  */
 public class DroidOrbService extends Service {
    public static final int SERVER_PORT = 4567;
    public static final int NOTIF_MAIN = 1;
 
    // Unique Identification Number for the Notification.
    // We use it on Notification start, and to cancel it.
    private int NOTIFICATION = 10;
 
    private static Server server = null;
    private static MissedCallsContentObserver mcco;
    private NotificationManager mNM;
 
    // This is the object that receives interactions from clients. See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();
 
    @Override
    public IBinder onBind(Intent arg0) {
       if (Debug.SERVICE) Log.d(Main.LOG_TAG, "Service onBind()");
       
       return mBinder;
    }
 
    /**
     * Class for clients to access. Because we know this service always runs in
     * the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
       DroidOrbService getService() {
          return DroidOrbService.this;
       }
    }
 
    /**
     * Send a DroidOrb command
     * 
     * @param deviceId
     * @param command
     * @param params
     * @throws IOException
     */
    public void sendCommand(byte deviceId, byte command, byte[] params) {
       if (Debug.SERVICE) Log.d(Main.LOG_TAG, "Service sendCommand()");
 
       int paramLen = (params == null ? 0 : params.length);
       byte[] data = new byte[paramLen + 3];
       data[0] = deviceId;
       data[1] = command;
       for (int i = 0; i < paramLen; i++)
          data[i + 2] = params[i];
       data[data.length - 1] = '\n';
 
       if (Debug.COMMS) {
          Log.d(Main.LOG_TAG, "Service sending ");
          String msg = "";
          for (int i = 0; i < data.length; i++)
             msg += String.valueOf(data[i]) + ",";
          Log.d(Main.LOG_TAG, msg);
       }
       
       server.send(data);
    }
 
    @Override
    public void onCreate() {
       super.onCreate();
       if (Debug.SERVICE) Log.d(Main.LOG_TAG, "Service onCreate()");
 
       mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
 
       // Display a notification about us starting. We put an icon in the status
       // bar.
       showNotification("Started");
 
       // missed call observer
       if (mcco == null) {
          if (Debug.SERVICE) Log.d(Main.LOG_TAG, "Service creating missed call observer");
          
          mcco = new MissedCallsContentObserver(this, new OnMissedCallListener() {
             @Override
             public void onMissedCall(int missedCalls) {
                if (Debug.SERVICE) Log.d(Main.LOG_TAG, "Service detected missed call");
                showNotification("Missed call detected");
                sendCommand((byte) 0, (byte) 1, new byte[] { 10, 0, 0, 50 });
             }
          });
          
          // start missed calls listener
          mcco.start();
       }
 
       // Create TCP server
       if (server == null) {
          if (Debug.SERVICE) Log.d(Main.LOG_TAG, "Service creating TCP/IP server");
          
          server = new Server(SERVER_PORT);
          server.addListener(new ServerListener() {
 
             @Override
             public void onServerStopped(Server server) {
                Log.d(Main.LOG_TAG, "server stopped");
             }
 
             @Override
             public void onServerStarted(Server server) {
                Log.d(Main.LOG_TAG, "server started");
             }
 
             @Override
             public void onReceive(Client client, final byte[] data) {
                Log.d(Main.LOG_TAG, "data received " + data[0] + data[1]);
               //Toast.makeText(DroidOrbService.this, "DroidOrb data received " + data.toString(), Toast.LENGTH_LONG).show();
             }
 
             @Override
             public void onClientDisconnect(Server server, Client client) {
                showNotification("Accessory disconnected");
                Log.d(Main.LOG_TAG, "accessory disconnected");
             }
 
             @Override
             public void onClientConnect(Server server, Client client) {
                showNotification("Accessory connected");
                Log.d(Main.LOG_TAG, "accessory connected");
             }
          });
          
          try {
             server.start();
          } catch (IOException e) {
             Log.e(Main.LOG_TAG, "Error starting TCP/IP server", e);
          }
       }
    }
 
    @Override
    public void onDestroy() {
       if (Debug.SERVICE) Log.d(Main.LOG_TAG, "Service onDestroy()");
      
       // Cancel the persistent notification.
       mNM.cancel(NOTIFICATION);
       server.stop();
       server = null;
       mcco.stop();
       mcco = null;
    }
 
    /**
     * Show a notification while this service is running.
     */
    private void showNotification(String text) {
       // In this sample, we'll use the same text for the ticker and the expanded
       // notification
       // CharSequence text = "DroidOrb waiting for accessory"; //
       // getText(R.string.local_service_started);
 
       // Set the icon, scrolling text and timestamp
       Notification notification = new Notification(R.drawable.icon, text, System.currentTimeMillis());
 
       // The PendingIntent to launch our activity if the user selects this
       // notification
       Intent intent = new Intent(this, Main.class);
       intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
       PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);
 
       // Set the info for the views that show in the notification panel.
       notification.setLatestEventInfo(this, "DroidOrb", text, contentIntent);
       notification.flags |= Notification.FLAG_ONGOING_EVENT;
 
       // Send the notification.
       mNM.notify(NOTIFICATION, notification);
    }
 }
