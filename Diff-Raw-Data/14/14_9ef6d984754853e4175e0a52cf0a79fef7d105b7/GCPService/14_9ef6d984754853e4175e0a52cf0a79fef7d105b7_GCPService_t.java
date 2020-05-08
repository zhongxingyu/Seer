 /*
  * Copyright (C) 2012 http://andlabs.eu
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package eu.andlabs.studiolounge.gcp;
 
 import java.util.Iterator;
 
 import io.socket.IOAcknowledge;
 import io.socket.IOCallback;
 import io.socket.SocketIO;
 import io.socket.SocketIOException;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Service;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.os.Messenger;
 import android.os.RemoteException;
 import android.util.Log;
 import android.widget.Toast;
 
 
 public class GCPService extends Service {
 
     private String mName;
     private Handler mHandler;
     private SocketIO mSocketIO;
     private Messenger mChatGame;
 
     public static final int LOGIN = 1;
     public static final int CHAT = 2;
     public static final int LEAVE = 3;
 	public static final int HOST = 4;
 	public static final int JOIN = 5;
     protected static final int CUSTOM = 6;
     
     @Override
     public void onCreate() {
         super.onCreate();
        mName = "Flori";
         mHandler = new Handler();
         
         log("starting GCP Service");
         try {
             mSocketIO = new SocketIO("http://may.base45.de:7777", new IOCallback() {
                 
                 @Override
                 public void onConnect() { // auto login
                     log("connected to GCP game server!"); 
                     mSocketIO.emit("login", "I am " + mName);
                 }
                 
                 @Override
                 public void onMessage(String text, IOAcknowledge ack) {
                     dispatchMessage(CHAT, text);
                 }
                 
                 @Override
                 public void on(String type, IOAcknowledge ack, Object... data) {
                     //                    log("incoming message:" + type + " --- " + data);
                     try {
                         if (type.equals("login")) {
                             dispatchMessage(LOGIN, data[0].toString());
                         } else if (type.equals("players")) {
                             JSONArray json = (JSONArray) data[0];
                             for (int i = 0; i < json.length(); i++) {
                                 dispatchMessage(LOGIN, json.getString(i));
                             }
                         } else if (type.equals("games")) {
                             JSONArray json = (JSONArray) data[0];
                             for (int i = 0; i < json.length(); i++) {
                                 JSONObject o = json.getJSONObject(i);
                                 Bundle b = new Bundle();
                                 b.putString("game", o.getString("game"));
                                 b.putString("host", o.getString("host"));
                                 dispatchMessage(HOST, b);
                             }
                         } else if (type.equals("host")){
                             JSONObject json = (JSONObject) data[0];
                             Bundle b = new Bundle();
                             b.putString("game", json.getString("game"));
                             b.putString("host", json.getString("host"));
                             dispatchMessage(HOST, b);
                         } else if (type.equals("join")){
                             JSONObject json = (JSONObject) data[0];
                             dispatchMessage(JOIN, json.get("guest"));
                         } else if (type.equals("move")){
                             JSONObject json = (JSONObject) data[0];
                             
                             
                             Bundle b = new Bundle();
                             
                             for ( Iterator<String> i = json.keys(); i.hasNext(); )
                             {
                             	String key = i.next();
                             	b.putString(key, json.getString(key));
                             	Log.i("json", "converting -  key:"+key + "  /  Value: "+json.getString(key));
                             }
                             
 //                            b.putString("who", json.getString("who"));
 //                            b.putString("color", json.getString("color"));
 //                            b.putLong("x", json.getLong("x"));
 //                            b.putLong("y", json.getLong("y"));
                             dispatchMessage(CUSTOM, b);
                         } else {
                             dispatchMessage(CHAT, "BAD protocol message: " + type);
                             Log.d("GCP", ""+data[0].getClass().getName());
                         }
                     } catch (JSONException e) {
                         e.printStackTrace();
                     }
                 }
                 
                 @Override
                 public void onMessage(JSONObject json, IOAcknowledge ack) { }
                 
                 @Override
                 public void onDisconnect() { log("lost game server."); }
                 
                 @Override
                 public void onError(SocketIOException error) { log(error); }
             });
         } catch (Exception e) {
             log(e);
             dispatchMessage(CHAT, "GCP Service Error:   " + e.toString());
             e.printStackTrace();
         }
     }
 
     // bind game app(s)
     @Override
     public IBinder onBind(Intent intent) {
         log("on bind");
         mChatGame = (Messenger) intent.getParcelableExtra("messenger");
         return mMessenger.getBinder();
     }
     
     // send android system IPC message to game apps
     private void dispatchMessage(int what, Object thing) {
         try {
             mChatGame.send(Message.obtain(mHandler, what, thing));
         } catch (RemoteException e) {
             log("Error: " + e.toString());
         } 
     }
     
     // receive android system IPC messages from game apps
     final Messenger mMessenger = new Messenger(new Handler() {
 
         @Override
         public void handleMessage(Message msg) {
             if (mSocketIO.isConnected()) {
                 try {
                     JSONObject json = new JSONObject();
                     switch (msg.what) {
                     case CHAT:
                         mSocketIO.send(((String) msg.obj));
                         break;
                     case HOST:
                         json.put("host", mName);
                         json.put("game", "my.game");
                         mSocketIO.emit("host", json);
                         break;
                     case JOIN:
                         json.put("host", msg.obj);
                         json.put("game", "my.game");
                         mSocketIO.emit("join", json);
                         break;
                     case CUSTOM:
                         Bundle b = (Bundle) msg.obj;
                         json.put("who", mName);
                         json.put("color", b.getString("color"));
                         
                         json.put("x", b.getLong("x"));
                         json.put("y", b.getLong("y"));
                         mSocketIO.emit("move", json);
                         break;
                     }
                 } catch (JSONException e) {
                     // TODO Auto-generated catch block
                     e.printStackTrace();
                 }
             }
         }});
 
     @Override
     public void onDestroy() {
         log("on destroy");
         mSocketIO.disconnect();
         super.onDestroy();
     }
 
 
 
     private void log(final Object ding) {
         mHandler.post(new Runnable() {
             
             @Override
             public void run() {
                 Toast.makeText(GCPService.this, ding.toString(),
                         1000).show();
             }
         });
         Log.d("GCP-Service", ding.toString());
     }
 }
