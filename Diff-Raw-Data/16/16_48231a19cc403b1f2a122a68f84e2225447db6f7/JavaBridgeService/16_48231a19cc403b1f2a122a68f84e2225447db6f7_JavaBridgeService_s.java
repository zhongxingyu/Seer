 package uk.me.desert_island.theorbtwo.bridge;
 
 import android.app.IntentService;
 import android.content.Intent;
 import android.os.IBinder;
 import android.os.Binder;
 import android.os.Handler;
 import android.os.Messenger;
 import android.os.Message;
 import android.os.RemoteException;
 import android.widget.Toast;
 import android.util.Log;
 import java.net.*;
 import java.io.*;
 import uk.me.desert_island.theorbtwo.bridge.TcpIpConnection;
 import uk.me.desert_island.theorbtwo.bridge.PrintyThingAndroid;
 
 // This should probably be an IntentService (see http://developer.android.com/guide/components/services.html)
 // Then we would need to store our ids and suchlike outside of the
 // memory (sqlitedb?) so that the individual handlers can continue from
 // each other.
 
 public class JavaBridgeService extends IntentService {
     private TcpIpConnection my_connection;
     private static final String LOGTAG = "JavaBridgeService";
     // It would probably be handy if Core were a proper object then we
     // can create one here and re-use it for new tcpconnections
     // private Core my_jb_core;
 
     public JavaBridgeService() {
         super("JavaBridgeService");
        AndroidServiceStash.set_service(this);
        //        AndroidServiceStash.set_activity(new Activity());
     }
 
     @Override
     protected void onHandleIntent(Intent intent) {
       Toast.makeText(this, "Starting server ... ", Toast.LENGTH_SHORT).show();
       if(my_connection != null) {
           Toast.makeText(this, "TCP-IP server already running", Toast.LENGTH_SHORT).show();
           return;
       }
 
       // Would prefer that this not be 0.0.0.0 !
       InetAddress bind_address;
       try {
           //          bind_address = InetAddress.getLocalHost();
           bind_address = Inet4Address.getByName("0.0.0.0");
       } catch (UnknownHostException e) {
           Log.e(LOGTAG, "Failed to find InetAddress 0.0.0.0");
           return;
       }
 
       ServerSocket server_socket;
       try {
           server_socket = new ServerSocket(9849, 1, bind_address);
       } catch(IOException e) {
           Log.e(LOGTAG, "Failed to accept create server socket on 9849");
           return;
       }
       
       /* FIXME: Where is the proper androidy place to put this mainloop? */
       while (true) {
         try {
             Socket connected_socket = server_socket.accept();
             my_connection = new TcpIpConnection(connected_socket, new PrintyThingAndroid("JavaBridgeService"));
             Log.d(LOGTAG, "Accepted new connection and started TcpIpConnection");
             my_connection.start();
         } catch (IOException e) {
             Log.e(LOGTAG, "Failed to accept socket connection!");
         }
       }
       
       //      return START_STICKY;
     }
     
     // This is the object that receives interactions from clients.  See
     // RemoteService for a more complete example.
     static final int MSG_SETUP = 1;
     static final int MSG_ACK = 2;
     class IncomingHandler extends Handler {
         @Override
         public void handleMessage (Message msg) {
             System.err.println("(Service)Message time: " + msg.getWhen());
             System.err.println("(Service)Message what: " + msg.what);
             if (msg.obj != null) {
                 System.err.println("(Service)Message content: "+msg.obj);
             } else {
                 System.err.println("(Service)Message content: null");
             }
 
             switch (msg.what) {
             case MSG_SETUP:
                 activityMessenger = msg.replyTo;
                 Message ack = Message.obtain(null, MSG_ACK, "Got MSG_SETUP");
                 try {
                     activityMessenger.send(ack);
                 }
                 catch (RemoteException e) {
                     e.printStackTrace();
                 }
                 break;
             default:
                 super.handleMessage(msg);
             }
         }
     }
 
     final Messenger serviceMessenger = new Messenger(new IncomingHandler());
     Messenger activityMessenger;
 
     @Override
     public IBinder onBind (Intent intent) {
         return serviceMessenger.getBinder();
     }
 
 
 }
