 /*
  * Copyright (c) 2012. Ansvia Inc.
  * Author: Robin Syihab.
  */
 
 package com.ansvia.mindchat;
 
 import android.app.IntentService;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.util.Log;
 
 /**
  * Main chat service.
  */
 public class ChatService extends IntentService {
 
     private static final String TAG = "ChatService";
 
    private static final String GETHUB_HOST = "www.gethub.us";
     //private static final int GETHUB_PORT = 6060;
 
    //private static final String GETHUB_HOST = "10.0.2.2";
     private static final int GETHUB_PORT = 6060;
 
 
     // @TODO(*): jangan di hard-coded.
     private static final String CHANNEL = "www.gethub.us";
     private LogoutEventReceiver logoutEventReceiver;
 
 
     public ChatService(String name) {
         super(name);
     }
 
     public ChatService(){
         super(TAG);
     }
 
 
 
     private class LogoutEventReceiver extends BroadcastReceiver {
 
         @Override
         public void onReceive(Context context, Intent intent) {
             GethubClient gethub = GethubClient.getInstance();
             gethub.logout();
         }
     }
 
     @Override
     protected void onHandleIntent(Intent intent) {
 
         Log.d(TAG, "in onHandleIntent()");
 
         this.logoutEventReceiver = new LogoutEventReceiver();
 
         registerReceiver(this.logoutEventReceiver, new IntentFilter("logout"));
 
         GethubClient gethub = GethubClient.getInstance();
 
         // set your gethub server address here.
         gethub.connect(GETHUB_HOST, GETHUB_PORT);
 
         try {
 
 
             String sessid = gethub.authorize(intent.getStringExtra("userName"), intent.getStringExtra("password"));
 
             if(sessid == null){
                 Log.i(TAG, "Cannot authorize user");
                 showError("Cannot authorize user, check your connection.");
                 return;
             }
 
             if(!gethub.join(CHANNEL, sessid)){
                 Log.i(TAG, "Cannot join to channel");
                 showError("Cannot join to channel " + CHANNEL);
                 return;
             }
 
             Intent chatRoomInitial = new Intent("chatroom.init");
             chatRoomInitial.putExtra("sessid", sessid);
             chatRoomInitial.putExtra("userName", intent.getStringExtra("userName"));
             chatRoomInitial.putExtra("channel", CHANNEL);
             sendBroadcast(chatRoomInitial);
 
             //DataReceiver dataRec = new DataReceiver(gethub, sessid, intent.getStringExtra("userName"));
 
             PacketHandler handler = new PacketHandler(this, gethub, sessid, intent.getStringExtra("userName"));
 
             gethub.bind(CHANNEL, sessid, handler);
 
         }catch (Exception e){
             showError(e.getMessage());
         }
 
     }
 
     private void showError(String msg){
         Intent errorIntent = new Intent("error");
         errorIntent.putExtra("data", msg);
         sendBroadcast(errorIntent);
     }
 
     @Override
     public void onDestroy() {
         super.onDestroy();
         unregisterReceiver(logoutEventReceiver);
         GethubClient gethub = GethubClient.getInstance();
         gethub.close();
     }
 
 
 }
 
