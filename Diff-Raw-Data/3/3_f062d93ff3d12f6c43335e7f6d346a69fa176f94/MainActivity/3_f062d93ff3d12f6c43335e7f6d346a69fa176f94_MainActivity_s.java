 package jp.gr.java_conf.neko_daisuki.android.nexec.client;
 
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.JsonWriter;
 import android.view.Menu;
 import android.view.View.OnClickListener;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MainActivity extends Activity {
 
     private class OkButtonOnClickListener implements OnClickListener {
 
         private String mHost;
         private int mPort;
         private String[] mArgs;
         private String[] mFiles;
 
         public OkButtonOnClickListener(String host, int port, String[] args, String[] files) {
             mHost = host;
             mPort = port;
             mArgs = args;
             mFiles = files;
         }
 
         public void onClick(View view) {
             String sessionId;
             try {
                 sessionId = createSessionId();
             }
             catch (NoSuchAlgorithmException e) {
                 showException("algorithm not found", e);
                 return;
             }
             try {
                 saveSession(sessionId);
             }
             catch (IOException e) {
                 showException("failed to save session", e);
                 return;
             }
 
             Intent intent = getIntent();
             intent.putExtra("SESSION_ID", sessionId);
             setResult(RESULT_OK, intent);
             finish();
         }
 
         private void showException(String message, Throwable e) {
             showToast(String.format("%s: %s", message, e.getMessage()));
             e.printStackTrace();
         }
 
         private String createSessionId() throws NoSuchAlgorithmException {
             MessageDigest md = MessageDigest.getInstance("SHA-256");
             md.update(mHost.getBytes());
             for (int width: new int[] { 0, 8, 16, 24 }) {
                 md.update((byte)((mPort >>> width) & 0xff));
             }
             for (String a: mArgs) {
                 md.update(a.getBytes());
             }
             long now = System.currentTimeMillis();
             for (int width: new int[] { 0, 8, 16, 24, 32, 40, 48, 56 }) {
                 md.update((byte)((now >>> width) & 0xff));
             }
             byte[] bytes = new byte[32];
             mRandom.nextBytes(bytes);
             md.update(bytes);
             return md.toString();
         }
 
         private void writeArray(JsonWriter writer, String name, String[] sa) throws IOException {
             writer.name(name);
             writer.beginArray();
             for (String a: sa) {
                 writer.value(a);
             }
             writer.endArray();
         }
 
         private void saveSession(String sessionId) throws IOException {
             OutputStream out = openFileOutput(sessionId, 0);
             JsonWriter writer = new JsonWriter(
                     new BufferedWriter(new OutputStreamWriter(out, "UTF-8")));
             try {
                 writer.beginObject();
                 writer.name("host").value(mHost);
                 writer.name("port").value(mPort);
                 writeArray(writer, "args", mArgs);
                 writeArray(writer, "files", mFiles);
                 writer.endObject();
             }
             finally {
                 writer.close();
             }
         }
     }
 
     private class CancelButtonOnClickListener implements OnClickListener {
 
         public void onClick(View view) {
             setResult(RESULT_CANCELED, getIntent());
             finish();
         }
     }
 
     private Random mRandom = new Random();
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
 
         Intent intent = getIntent();
         String host = intent.getStringExtra("HOST");
         int port = intent.getIntExtra("PORT", 57005);
         String[] args = intent.getStringArrayExtra("ARGS");
         String[] files = intent.getStringArrayExtra("FILES");
         setStringText(R.id.host, host);
         setIntText(R.id.port, port);
         setStringArrayText(R.id.args, args);
         setPermissionList(files);
 
         Button okButton = (Button)findViewById(R.id.ok_button);
         okButton.setOnClickListener(
                 new OkButtonOnClickListener(host, port, args, files));
         Button cancelButton = (Button)findViewById(R.id.cancel_button);
         cancelButton.setOnClickListener(new CancelButtonOnClickListener());
     }
 
     private void setPermissionList(String[] files) {
         AdapterView view = (AdapterView)findViewById(R.id.permission_list);
         int id = android.R.layout.simple_list_item_1;
         List<String> list = new ArrayList<String>();
         view.setAdapter(new ArrayAdapter(this, id, list));
     }
 
     private TextView getTextView(int id) {
         return (TextView)findViewById(id);
     }
 
     private void setIntText(int id, int n) {
         getTextView(id).setText(Integer.toString(n));
     }
 
     private void setStringArrayText(int id, String[] sa) {
         StringBuffer buffer = new StringBuffer(sa[0]);
         for (int i = 1; i < sa.length; i++) {
             buffer.append(String.format(" %s", sa[i]));
         }
         getTextView(id).setText(buffer.toString());
     }
 
     private void setStringText(int id, String s) {
         getTextView(id).setText(s);
     }
 
     private void showToast(String message) {
         Toast.makeText(this, message, Toast.LENGTH_LONG).show();
     }
 }
 
 // vim: tabstop=4 shiftwidth=4 expandtab softtabstop=4
