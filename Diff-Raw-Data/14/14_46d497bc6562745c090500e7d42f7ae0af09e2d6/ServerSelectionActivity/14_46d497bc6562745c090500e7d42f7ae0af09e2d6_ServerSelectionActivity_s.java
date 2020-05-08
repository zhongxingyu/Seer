 package mgp.client;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
 
 import java.io.*;
 import java.util.ArrayList;
 
 public class ServerSelectionActivity extends Activity {
 
     private static final String TAG = "MGP.ServerSelection";
     private static final String SERVERS_FILE = "server_list";
     ArrayList<ServerEntry> serverList = new ArrayList<ServerEntry>();
     private boolean listAltered = false;
     private String playerName = null;
 
 
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.select_server);
 
         playerName = getIntent().getStringExtra("name");
         // TODO: put extra in intent when launching game
 
         getServerList();
     }
 
     @Override
     protected void onStop() {
         super.onStop();
         if (listAltered) {
             saveServerList();
         }
     }
 
     private void getServerList() {
         FileInputStream fis = null;
         try {
             fis = openFileInput(SERVERS_FILE);
             ObjectInputStream ois = new ObjectInputStream(fis);
             Object obj;
             while ((obj = ois.readObject()) != null) {
                 if (obj instanceof ServerEntry) {
                     serverList.add((ServerEntry) obj);
                 }
             }
         } catch (FileNotFoundException e) {
             Log.i(TAG,"File not found. No server list available.");
         } catch (StreamCorruptedException e) {
             Log.e(TAG, e.getMessage());
         } catch (IOException e) {
            e.printStackTrace();
         } catch (ClassNotFoundException e) {
             Log.e(TAG, e.getMessage());
         } finally {
             if (fis != null) {
                 try {
                     fis.close();
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             }
         }
 
         updateTable();
     }
 
     /**
      * Server list is only saved when we close the application.
      */
     private void saveServerList() {
         FileOutputStream fis = null;
         try {
            deleteFile(SERVERS_FILE);
             fis = openFileOutput(SERVERS_FILE, MODE_PRIVATE);
             ObjectOutputStream oos = new ObjectOutputStream(fis);
             for (ServerEntry entry : serverList) {
                 oos.writeObject(entry);
             }
         } catch (FileNotFoundException e) {
             Log.e(TAG, e.getMessage());
         } catch (IOException e) {
             Log.e(TAG, e.getMessage());
         } finally {
             if (fis != null) {
                 try {
                     fis.close();
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             }
         }
         String files[] = fileList();
         for (String s : files) {
             Log.i(TAG,"FILE IN INTERNAL STORAGE: " + s);
         }
     }
 
     public void updateTable() {
         for (ServerEntry entry : serverList) {
             Log.i(TAG,"name: " + entry.getName() + ", ip: " + entry.getServerIp());
             // fill table layout with server entries
         }
     }
 
     public void connect(View view) {  // TODO: rename for clarity? this is onButton click
         if (isAlive(/*server*/)) {
             Intent intent = new Intent(ServerSelectionActivity.this,GameActivity.class);
             intent.putExtra("name",playerName);
             startActivity(intent);
         } else {
             Toast.makeText(this,"Server is offline or full",Toast.LENGTH_SHORT).show();
         }
     }
 
     public boolean isAlive(/*server*/) {
         return true;
     }
 
     public void refresh(View view) {
         // refreshes server availability
     }
 
     public void addServer(View view) {
         final Dialog dialog = new Dialog(this);
         dialog.setTitle(R.string.add_server_label_text);
         dialog.setContentView(R.layout.add_server);
         Button addButton = (Button) dialog.findViewById(R.id.add_server_button_add);
         Button cancelButton = (Button) dialog.findViewById(R.id.add_server_button_cancel);
         addButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 EditText nameInput = (EditText) dialog.findViewById(R.id.add_server_edit_name);
                 EditText ipInput = (EditText) dialog.findViewById(R.id.add_server_edit_ip);
                 String name = nameInput.getText().toString();
                 // TODO: parse ip
                 int ip = Integer.parseInt(ipInput.getText().toString());
                 boolean unique = true;
                 for (ServerEntry entry : serverList) {
                     if (entry.getName().equals(name) || entry.getServerIp() == ip) {
                         Log.e(TAG,"Server with this name or ip already exists: " + name + ", " + ip);
                         Toast.makeText(ServerSelectionActivity.this,"Server with this name or ip already exists",Toast.LENGTH_LONG).show();
                    } else {
                         unique = false;
                         break;
                     }
                 }
 
                 if (unique) {
                     serverList.add(new ServerEntry(name,ip));
                     listAltered = true;
                     updateTable();
                 }
 
                 dialog.dismiss();
 
             }
         });
 
         cancelButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 dialog.dismiss();
             }
         });
 
         dialog.show();
 
     }
 
     public void removeServer(View view) {
         listAltered = true;
         updateTable();
     }
 
 }
