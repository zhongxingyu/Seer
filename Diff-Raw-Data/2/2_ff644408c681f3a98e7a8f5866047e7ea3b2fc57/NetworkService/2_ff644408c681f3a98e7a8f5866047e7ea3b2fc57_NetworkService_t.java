 package pro.kornev.kcar.cop.services;
 
 import android.app.Service;
 import android.content.Intent;
 import android.os.IBinder;
 import android.widget.Toast;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Queue;
 
 import pro.kornev.kcar.cop.State;
 import pro.kornev.kcar.cop.providers.LogsDB;
 import pro.kornev.kcar.protocol.Data;
 import pro.kornev.kcar.protocol.Protocol;
 
 /**
  * @author vkornev
  * @since 17.10.13
  */
 public class NetworkService extends Service {
     private LogsDB db;
     private static List<NetworkListener> listeners = new ArrayList<NetworkListener>();
 
     @Override
     public IBinder onBind(Intent intent) {
         return null;
     }
 
     @Override
     public void onCreate() {
         super.onCreate();
         db = new LogsDB(getApplicationContext());
     }
 
     @Override
     public int onStartCommand(Intent intent, int flags, int startId) {
         super.onStartCommand(intent, flags, startId);
         Toast.makeText(this, "Network service starting", Toast.LENGTH_SHORT).show();
         new Thread(new Runnable() {
             @Override
             public void run() {
                 while (State.isServiceRunning()) {
                     db.putLog("Connect to: " + State.getProxyServer() + ":" + 6780);
                     Socket s;
                     try {
                         s = new Socket(State.getProxyServer(), 6780);
                     } catch (IOException e) {
                         db.putLog("Failed connect to server: " + e.getMessage());
                         sleep(1000); // wait 1 seconds and if isServiceRunning then try reconnect
                         continue;
                     }
 
                     Writer writer = new Writer(s);
                     new Thread(writer).start();
 
                     Reader reader = new Reader(s);
                     Thread readerThread = new Thread(reader);
                     readerThread.start();
                     db.putLog("Connect to " +s.getInetAddress().toString() + " is successful");
                     try {
                         readerThread.join(); // Work wile reader is working
                         s.close(); // Close socket and try reconnect
                     } catch (InterruptedException e) {
                         e.printStackTrace();
                     } catch (IOException e) {
                         e.printStackTrace();
                     }
                 }
             }
         }).start();
         return START_STICKY;
     }
 
     class Reader implements Runnable {
         Socket client;
 
         Reader(Socket s) {
             client = s;
         }
 
         @Override
         public void run() {
             db.putLog("Start network reader");
             try {
                 DataInputStream input = new DataInputStream(client.getInputStream());
                 while (State.isServiceRunning() && !client.isClosed()) {
                     /*if (input.available() == 0) {
                         input.readByte();
                         sleep(1);
                         continue;
                     }*/
 
                     Data data = Protocol.fromInputStream(input);
 
                     db.putLog(String.format("NR: id: %d; cmd: %d", data.id, data.cmd));
                     if (data.cmd == 1 && data.bData == 0) {
                         Data response = new Data();
                         response.id = data.id;
                         response.cmd = data.cmd;
                         response.type = data.type;
                         response.bData = 2;
                         State.getToControlQueue().add(response);
                     }
                     for (NetworkListener l: listeners) {
                         l.onDataReceived(data);
                     }
                 }
             } catch (IOException e) {
                 e.printStackTrace();
             } finally {
                 db.putLog("Stop network reader");
                 try {
                     client.close();
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             }
         }
     }
 
     class Writer implements Runnable {
         Socket client;
         Queue<Data> queue;
         Writer(Socket s) {
             client = s;
             queue = State.getToControlQueue();
         }
 
         @Override
         public void run() {
             db.putLog("Start network writer");
             try {
                 DataOutputStream output = new DataOutputStream(client.getOutputStream());
                 while (State.isServiceRunning() && !client.isClosed()) {
                     if (queue.isEmpty()) {
                         sleep(1);
                         continue;
                     }
                     Data data = queue.poll();
                    db.putLog(String.format("NW: id: %d; cmd: %d", data.id, data.cmd));
 
                     Protocol.toOutputStream(data, output);
                 }
                 client.close();
             } catch (IOException e) {
                 e.printStackTrace();
             }
             db.putLog("Stop network writer");
         }
     }
 
     @Override
     public void onDestroy() {
         Toast.makeText(this, "Network service done", Toast.LENGTH_SHORT).show();
         State.setServiceRunning(false);
     }
 
     private void sleep(int ms) {
         try {
             Thread.sleep(ms);
         } catch (InterruptedException e) {
             e.printStackTrace();
         }
     }
 
     public static void addListener(NetworkListener listener) {
         listeners.add(listener);
     }
 
     public static void removeAllListeners() {
         listeners.clear();
     }
 }
