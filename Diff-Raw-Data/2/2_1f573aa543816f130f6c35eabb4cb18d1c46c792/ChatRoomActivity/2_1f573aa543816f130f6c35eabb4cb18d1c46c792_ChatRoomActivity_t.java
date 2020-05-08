 /*
  * Copyright (c) 2012. Ansvia Inc.
  * Author: Robin Syihab.
  */
 
 package com.ansvia.mindchat;
 
 import android.app.Activity;
 import android.app.IntentService;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.*;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 public class ChatRoomActivity extends  Activity implements View.OnClickListener {
 
     private static final String TAG = "ChatRoomActivity";
     private static final String NS_MESSAGE = "chat::message";
 
     // @TODO(*): jangan di hard-coded.
     //private static final String CHANNEL = "localhost";
 
     private String sessid = null;
     private String channel = null;
 
     LinearLayout chatContainer = null;
 
 
 
     private class EventReceiver extends BroadcastReceiver {
 
         @Override
         public void onReceive(Context context, Intent intent) {
             String text = intent.getStringExtra("data");
             appendMessage(text);
         }
     }
 
 
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.chat_room);
 
         chatContainer = (LinearLayout)findViewById(R.id.chatContainer);
 
         Bundle extras = getIntent().getExtras();
 
         this.sessid = extras.getString("sessid");
         this.channel = extras.getString("channel");
 
 //        GethubClient gethub = GethubClient.getInstance();
 
         appendMessage("Welcome " + extras.getString("userName"));
 
         registerReceiver(new EventReceiver(), new IntentFilter("new.message"));
 
         Button btnSend = (Button)findViewById(R.id.btnSend);
         btnSend.setOnClickListener(this);
     }
 
     public void appendMessage(String message){
 
         TextView text = new TextView(this);
         text.setId((int)System.currentTimeMillis());
         text.setText(message);
         text.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                 ViewGroup.LayoutParams.WRAP_CONTENT));
 
         chatContainer.addView(text);
 
         chatContainer.refreshDrawableState();
     }
 
     @Override
     public void onClick(View view) {
         EditText text = (EditText)findViewById(R.id.inputMessage);
 
         GethubClient gethub = GethubClient.getInstance();
 
         gethub.message(channel, text.getText().toString(), sessid);

        text.setText("");
     }
 }
