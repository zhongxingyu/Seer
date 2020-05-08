 package jp.gr.java_conf.neko_daisuki.android.nexec.client;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Queue;
 import java.util.concurrent.ConcurrentLinkedQueue;
 
 import android.app.Service;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.os.Messenger;
 import android.os.RemoteException;
 import android.util.JsonReader;
 import android.util.Log;
 import android.widget.Toast;
 
 import jp.gr.java_conf.neko_daisuki.fsyscall.Logging;
 import jp.gr.java_conf.neko_daisuki.fsyscall.slave.Links;
 import jp.gr.java_conf.neko_daisuki.fsyscall.slave.Permissions;
 import jp.gr.java_conf.neko_daisuki.nexec.client.NexecClient;
 import jp.gr.java_conf.neko_daisuki.nexec.client.ProtocolException;
 
 public class MainService extends Service {
 
     private static class Destination implements Logging.Destination {
 
         public void verbose(String message) {
             Log.v(TAG, message);
         }
 
         public void debug(String message) {
             Log.d(TAG, message);
         }
 
         public void info(String message) {
             Log.i(TAG, message);
         }
 
         public void warn(String message) {
             Log.w(TAG, message);
         }
 
         public void err(String message) {
             Log.e(TAG, message);
         }
     }
 
     private static class Input extends InputStream {
 
         private Queue<Byte> mQueue;
 
         public Input() {
             mQueue = new ConcurrentLinkedQueue<Byte>();
         }
 
         public int read() throws IOException {
             Byte b = mQueue.poll();
             return b != null ? b.byteValue() : -1;
         }
     }
 
     private static class Output extends OutputStream {
 
         private Queue<Byte> mQueue;
 
         public Output() {
             mQueue = new ConcurrentLinkedQueue<Byte>();
         }
 
         public void write(int b) throws IOException {
             mQueue.add(Byte.valueOf((byte)b));
         }
 
         public Byte read() {
             return mQueue.poll();
         }
     }
 
     private class Task extends AsyncTask<Void, Void, Void> {
 
         private class ExceptionProcessor implements Runnable {
 
             private String mMessage;
 
             public ExceptionProcessor(String message) {
                 mMessage = message;
             }
 
             public void run() {
                 showToast(mMessage);
             }
         }
 
         private Session mSession;
         private Input mStdin;
         private Output mStdout;
         private Output mStderr;
 
         public Task(Session session, Input stdin, Output stdout, Output stderr) {
             super();
 
             mSession = session;
             mStdin = stdin;
             mStdout = stdout;
             mStderr = stderr;
         }
 
         public Input getStdin() {
             return mStdin;
         }
 
         public Output getStdout() {
             return mStdout;
         }
 
         public Output getStderr() {
             return mStderr;
         }
 
         protected Void doInBackground(Void... params) {
             run();
             return null;
         }
 
         private void run() {
             Permissions perm = new Permissions();
             for (String path: mSession.files) {
                 perm.allowPath(path);
             }
 
             NexecClient nexec = new NexecClient();
             try {
                 nexec.run(
                         mSession.host, mSession.port, mSession.args,
                         mStdin, mStdout, mStderr,
                         perm, mSession.links);
             }
             catch (ProtocolException e) {
                 showException("protocol error", e);
             }
             catch (InterruptedException e) {
                 showException("interrupted", e);
             }
             catch (IOException e) {
                 showException("I/O error", e);
             }
         }
 
         private void showException(String msg, Exception e) {
             e.printStackTrace();
 
             String s = String.format("%s: %s", msg, e.getMessage());
             mHandler.post(new ExceptionProcessor(s));
         }
     }
 
     private static class IncomingHandler extends Handler {
 
         private Task mTask;
 
         public IncomingHandler(Task task) {
             mTask = task;
         }
 
         public void handleMessage(Message msg) {
             if (msg.what != MessageWhat.TELL_STATUS) {
                 super.handleMessage(msg);
                 return;
             }
 
            Byte b = mTask.getStdout().read();
            if (b == null) {
                if (mTask.getStatus() == AsyncTask.Status.FINISHED) {
                    sendReply(msg, Message.obtain(null, MessageWhat.FINISHED));
                }
                 return;
             }
 
            int type = MessageWhat.STDOUT;
            int arg1 = b.byteValue();
            sendReply(msg, Message.obtain(null, type, arg1, 0, null));
         }
 
         private void sendReply(Message msg, Message reply) {
             try {
                 msg.replyTo.send(reply);
             }
             catch (RemoteException e) {
                 e.printStackTrace();
                 // TODO: Show error.
             }
         }
     }
 
     private static class Session {
 
         public String host = "";
         public int port;
         public String[] args = new String[0];
         public String[] files = new String[0];
         public Links links;
     }
 
     private static final String TAG = "nexec client";
 
     private Handler mHandler = new Handler();
 
     public void onCreate() {
         super.onCreate();
         Logging.setDestination(new Destination());
         Log.i(TAG, "MainService was created.");
     }
 
     @Override
     public IBinder onBind(Intent intent) {
         String sessionId = intent.getStringExtra("SESSION_ID");
         Session session;
         try {
             session = readSessionFile(sessionId);
         }
         catch (IOException e) {
             String fmt = "failed to read session information: %s: %s";
             showToast(String.format(fmt, sessionId, e.getMessage()));
             e.printStackTrace();
             return null;
         }
         if (!removeSessionFile(sessionId)) {
             return null;
         }
 
         Input stdin = new Input();
         Output stdout = new Output();
         Output stderr = new Output();
         Task task = new Task(session, stdin, stdout, stderr);
         task.execute();
 
         return new Messenger(new IncomingHandler(task)).getBinder();
     }
 
     private boolean removeSessionFile(String sessionId) {
         String dir = getFilesDir().getAbsolutePath();
         String path = String.format("%s/%s", dir, sessionId);
         boolean result = new File(path).delete();
         if (!result) {
             showToast(String.format("failed to delete %s.", path));
         }
         return result;
     }
 
     private void showToast(String message) {
         Toast.makeText(this, message, Toast.LENGTH_LONG).show();
     }
 
     private Links readLinks(JsonReader reader) throws IOException {
         Links links = new Links();
 
         reader.beginArray();
         while (reader.hasNext()) {
             String dest = null;
             String src = null;
 
             reader.beginObject();
             while (reader.hasNext()) {
                 String name = reader.nextName();
                 if (name.equals("dest")) {
                     dest = reader.nextString();
                 }
                 else if (name.equals("src")) {
                     src = reader.nextString();
                 }
             }
             reader.endObject();
 
             links.put(dest, src);
         }
         reader.endArray();
 
         return links;
     }
 
     private String[] readArray(JsonReader reader) throws IOException {
         List<String> list = new LinkedList<String>();
 
         reader.beginArray();
         while (reader.hasNext()) {
             list.add(reader.nextString());
         }
         reader.endArray();
 
         return list.toArray(new String[0]);
     }
 
     private Session readSessionFile(String sessionId) throws IOException {
         Session session = new Session();
 
         JsonReader reader = new JsonReader(
                 new InputStreamReader(openFileInput(sessionId)));
         try {
             reader.beginObject();
             while (reader.hasNext()) {
                 String name = reader.nextName();
                 if (name.equals("host")) {
                     session.host = reader.nextString();
                 }
                 else if (name.equals("port")) {
                     session.port = reader.nextInt();
                 }
                 else if (name.equals("args")) {
                     session.args = readArray(reader);
                 }
                 else if (name.equals("files")) {
                     session.files = readArray(reader);
                 }
                 else if (name.equals("links")) {
                     session.links = readLinks(reader);
                 }
             }
             reader.endObject();
         }
         finally {
             reader.close();
         }
 
         return session;
     }
 }
 
 // vim: tabstop=4 shiftwidth=4 expandtab softtabstop=4
