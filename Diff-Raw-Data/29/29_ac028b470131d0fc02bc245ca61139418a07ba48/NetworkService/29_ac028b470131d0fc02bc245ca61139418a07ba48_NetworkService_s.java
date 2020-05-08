 package pro.kornev.kcar.cop.services;
 
 import android.app.Service;
 import android.content.Intent;
 import android.os.IBinder;
 import android.util.Log;
 import android.widget.Toast;
 
 import com.google.gson.Gson;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.net.Socket;
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
                 try {
                     db.putLog("Connect to: " + State.getProxyServer() + ":" + 6780);
                     Log.d("DEBUG", "Connect to: " + State.getProxyServer() + ":" + 6780);
                     Socket s = new Socket(State.getProxyServer(), 6780);
                     Writer l = new Writer(s);
                     new Thread(l).start();
                     Reader r = new Reader(s);
                     new Thread(r).start();
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
             }
         }).start();
         return START_STICKY;
     }
 
     class Reader implements Runnable {
         Socket client;
         Queue<Data> queue;
 
         Reader(Socket s) {
             client = s;
             queue = State.getFromControlQueue();
         }
 
         @Override
         public void run() {
             try {
                InputStream input = client.getInputStream();
                 while (State.isServiceRunning()) {
 
                     if (input.available() == 0) {
                         sleep();
                         continue;
                     }
 
                    byte[] buf = new byte[Protocol.getMaxLength()];
                    int len = read(buf, input);
                    Data data = Protocol.fromByteArray(buf, len);
 
                     db.putLog(String.format("NR: id: %d; cmd: %d", data.id, data.cmd));
                     if (data.cmd == 1 && data.bData == 0) {
                         Data response = new Data();
                         response.id = data.id;
                         response.cmd = data.cmd;
                         response.type = data.type;
                         response.bData = 2;
                         State.getToControlQueue().add(response);
                     }
                     queue.add(data);
                 }
                 client.close();
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }

        private int read(byte[] buf, InputStream input) throws IOException {
            int len = 0;
            while (input.available() > 0) {
                len = input.read(buf, 0, buf.length-len);
                sleep();
            }
            return len;
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
             try {
                OutputStream output = client.getOutputStream();
                 while (State.isServiceRunning()) {
                     if (queue.isEmpty()) {
                         sleep();
                         continue;
                     }
                     Data data = queue.poll();
                     db.putLog(String.format("NR: id: %d; cmd: %d", data.id, data.cmd));
 
                    byte[] buf = new byte[Protocol.getMaxLength()];
                    int len = Protocol.toByteArray(data, buf);
                    output.write(buf, 0, len);
                    output.flush();
                 }
                 client.close();
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
     }
 
     @Override
     public void onDestroy() {
         Toast.makeText(this, "Network service done", Toast.LENGTH_SHORT).show();
         State.setServiceRunning(false);
     }
 
     private void sleep() {
         try {
             Thread.sleep(100);
         } catch (InterruptedException e) {
             e.printStackTrace();
         }
     }
 }
