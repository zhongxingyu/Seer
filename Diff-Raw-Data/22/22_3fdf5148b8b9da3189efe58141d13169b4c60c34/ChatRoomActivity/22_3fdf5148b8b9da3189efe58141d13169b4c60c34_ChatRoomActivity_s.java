 /*
  * Copyright (c) 2012. Ansvia Inc.
  * Author: Robin Syihab.
  */
 
 package com.ansvia.mindchat;
 
 import android.app.Activity;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.*;
 import android.content.res.Configuration;
 import android.graphics.Color;
 import android.media.AudioManager;
 import android.media.MediaPlayer;
 import android.media.RingtoneManager;
 import android.net.Uri;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.AttributeSet;
 import android.util.DisplayMetrics;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.*;
 
 import java.io.IOException;
 import java.util.LinkedList;
 
 
 
 public class ChatRoomActivity extends  Activity implements View.OnClickListener {
 
     private static final String TAG = "ChatRoomActivity";
     private static final String NS_MESSAGE = "chat::message";
 
     private LinkedList<String> chatMessages = new LinkedList<String>();
 
     // @TODO(*): jangan di hard-coded.
     //private static final String CHANNEL = "localhost";
 
     private String sessid = null;
     private String channel = null;
 
     LinearLayout chatContainer = null;
     private LinkedList<String> participants = null;
     private String userName;
     private BroadcastReceiver initHandler;
     private boolean initialized = false;
     private NewMessageReceiver messageReceiver;
     private ParticipantReceiver participantReceiver;
     private ScrollView chatContainerFrame;
     private Intent svc;
    private ServiceConnection connHandler;
     private boolean back = false;
     private ErrorHandler errorReceiver;
     private GlobalState globalState = GlobalState.getInstance();
 
 
     private class NewMessageReceiver extends BroadcastReceiver {
         @Override
         public void onReceive(Context context, Intent intent) {
             String text = intent.getStringExtra("data");
             Boolean self = intent.getBooleanExtra("self", false);
            //chatMessages.add(text);
             appendMessage(text);
             if(!self){
 
                 NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
                 Notification ntf = new Notification(R.drawable.icon, text, System.currentTimeMillis());
                 ntf.flags |= Notification.FLAG_ONLY_ALERT_ONCE;
                 //ntf.number += 1;
 
                 PendingIntent activity = PendingIntent.getActivity(ChatRoomActivity.this, 0, ChatRoomActivity.this.getIntent(), 0);
                 ntf.setLatestEventInfo(ChatRoomActivity.this, "New chat messages", text, activity);
                 nm.notify(0, ntf);
 
                 playSound();
             }
         }
     }
 
     private class ParticipantReceiver extends BroadcastReceiver {
         @Override
         public void onReceive(Context context, Intent intent) {
             String userName = intent.getStringExtra("userName");
             appendStatus("joined: " + userName);
         }
     }
 
     private class MainLinearLayout extends LinearLayout {
 
         public MainLinearLayout(Context context, AttributeSet attrs) {
             super(context, attrs);
             LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
             inflater.inflate(R.layout.chat_room, this);
         }
 
         @Override
         protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
             super.onMeasure(widthMeasureSpec, heightMeasureSpec);
 
             final int proposedheight = MeasureSpec.getSize(heightMeasureSpec);
             final int actualHeight = getHeight();
 
             if (actualHeight > proposedheight){
                 // keyboard is shown
 
                 DisplayMetrics metrics = new DisplayMetrics();
                 getWindowManager().getDefaultDisplay().getMetrics(metrics);
 
                 ScrollView chatContainerFrame = (ScrollView)findViewById(R.id.chatContainerFrame);
 
                 chatContainerFrame.setLayoutParams(new RelativeLayout.LayoutParams(
                         metrics.widthPixels, proposedheight - 100));
 
             } else {
                 // keyboard is hidden
 
                 DisplayMetrics metrics = new DisplayMetrics();
                 getWindowManager().getDefaultDisplay().getMetrics(metrics);
 
                 ScrollView chatContainerFrame = (ScrollView)findViewById(R.id.chatContainerFrame);
                 chatContainerFrame.setLayoutParams(new RelativeLayout.LayoutParams(
                         metrics.widthPixels, metrics.heightPixels - 200));
             }
             super.onMeasure(widthMeasureSpec, heightMeasureSpec);
         }
     }
 
     private class MyErrorHandler extends ErrorHandler {
 
         public MyErrorHandler(Activity act) {
             super(act);
         }
 
         @Override
         void onOk() {
             super.onOk();
             onBackPressed();
             globalState.authFailed = true;
             ChatRoomActivity.this.finish();
         }
 
     }
 
 
     private void playSound() {
         Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
         if (alert!=null){
             MediaPlayer mplayer = new MediaPlayer();
             try {
                 mplayer.setDataSource(this, alert);
                 //final AudioManager audman = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
                 mplayer.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);
                 mplayer.setLooping(false);
                 mplayer.prepare();
                 mplayer.start();
             } catch (IOException e) {
                 e.printStackTrace();
             }
 
         }
     }
 
 
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         //setContentView(R.layout.chat_room);
         setContentView(new MainLinearLayout(this, null));
 
         this.messageReceiver = new NewMessageReceiver();
         this.participantReceiver = new ParticipantReceiver();
         this.initHandler = new BroadcastReceiver() {
             @Override
             public void onReceive(Context context, Intent intent) {
 
                 GethubClient gethub = GethubClient.getInstance();
 
                 sessid = intent.getStringExtra("sessid");
                 channel = intent.getStringExtra("channel");
                 userName = intent.getStringExtra("userName");
 
                 participants = gethub.participants(channel, sessid);
 
                 chatMessages = gethub.messages(channel, sessid);
 
                 findViewById(R.id.lblPleaseWait).setVisibility(View.INVISIBLE);
                 findViewById(R.id.inputMessage).setVisibility(View.VISIBLE);
                 findViewById(R.id.btnSend).setVisibility(View.VISIBLE);
 
                 initializeUi();
 
                 initialized = true;
 
             }
         };
 
         this.errorReceiver = new MyErrorHandler(this);
 

         registerReceiver(this.messageReceiver, new IntentFilter("new.message"));
         registerReceiver(this.errorReceiver, new IntentFilter("error"));
         registerReceiver(this.participantReceiver, new IntentFilter("participant"));
         registerReceiver(this.initHandler, new IntentFilter("chatroom.init"));
 
         findViewById(R.id.lblPleaseWait).setVisibility(View.VISIBLE);
         findViewById(R.id.inputMessage).setVisibility(View.INVISIBLE);
         findViewById(R.id.btnSend).setVisibility(View.INVISIBLE);
 
         if(svc != null){
             stopService(svc);
         }
 
         svc = new Intent(this, ChatService.class);
 
         Intent intent = getIntent();
 
         String userName = intent.getStringExtra("userName");
         String password = intent.getStringExtra("password");
 
         svc.putExtra("userName", userName);
         svc.putExtra("password", password);
 
 //        this.connHandler = new ServiceConnection() {
 //            @Override
 //            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
 //                Log.d(TAG, "Service connected");
 //            }
 //
 //            @Override
 //            public void onServiceDisconnected(ComponentName componentName) {
 //                Log.d(TAG, "Service disconnected");
 //            }
 //        };
 
         startService(svc);
 
         //bindService(svc, connHandler,BIND_AUTO_CREATE | BIND_DEBUG_UNBIND);
 
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         this.back = false;
     }
 
     @Override
     protected void onPause() {
         super.onPause();
         if(!this.back){
             SharedPreferences.Editor sp = PreferenceManager.getDefaultSharedPreferences(this).edit();
             sp.putString("last_activity", getClass().getSimpleName());
             sp.commit();
         }
     }
 
     @Override
     protected void onDestroy() {
         unregisterReceiver(this.messageReceiver);
         unregisterReceiver(this.participantReceiver);
         unregisterReceiver(this.initHandler);
         unregisterReceiver(this.errorReceiver);
         //unbindService(this.connHandler);
         stopService(svc);
         super.onDestroy();
     }
 
     @Override
     public void onConfigurationChanged(Configuration newConfig) {
         super.onConfigurationChanged(newConfig);
         //setContentView(R.layout.chat_room);
         setContentView(new MainLinearLayout(this, null));
         if(initialized){
             initializeUi();
         }
     }
 
     @Override
     public void onBackPressed() {
         super.onBackPressed();
         SharedPreferences.Editor sp = PreferenceManager.getDefaultSharedPreferences(this).edit();
         sp.putString("last_activity", MainActivity.class.getSimpleName());
         sp.commit();
         globalState.authFailed = false;
         Intent intent = new Intent("logout");
         sendBroadcast(intent);
         this.back = true;
     }
 
     private void initializeUi() {
         chatContainerFrame = (ScrollView)findViewById(R.id.chatContainerFrame);
         chatContainer = (LinearLayout)findViewById(R.id.chatContainer);
 
         Button btnSend = (Button)findViewById(R.id.btnSend);
         btnSend.setOnClickListener(this);
 
 //        GethubClient gethub = GethubClient.getInstance();
 
         appendStatus("Welcome " + userName);
 
         if(participants.size() > 0){
             StringBuilder strb = new StringBuilder();
             strb.append("Participants: ");
             for(String participant : participants){
                 strb.append(participant).append(", ");
             }
             appendStatus(strb.toString().substring(0, strb.toString().length()-2));
         }
 
         DisplayMetrics metrics = new DisplayMetrics();
         getWindowManager().getDefaultDisplay().getMetrics(metrics);
 
         //final ScrollView chatContainer = (ScrollView)findViewById(R.id.chatContainer);
         //LinearLayout mainContainer = (LinearLayout)findViewById(R.id.mainContainer);
 
 //        Log.d(TAG, "outer: " + mainContainer.getLayoutParams().width + ", " + mainContainer.getLayoutParams().height);
 //        Log.d(TAG, "inner: " + chatContainerFrame.getLayoutParams().width + ", " + chatContainerFrame.getLayoutParams().height);
 //        Log.d(TAG, "chatCont: " + chatContainer.getLayoutParams().width + ", " + chatContainer.getLayoutParams().height);
 
 //        chatContainer.setLayoutParams(new ScrollView.LayoutParams(
 //                metrics.widthPixels, metrics.heightPixels - 200));
 
         // fill with chat messages
         for(String msg : chatMessages){
             appendMessage(msg);
         }
 
         chatContainerFrame.post(new Runnable() {
             @Override
             public void run() {
                 chatContainerFrame.smoothScrollTo(0, chatContainer.getBottom());
             }
         });
 
     }
 
     /**
      * Add status to chat room.
      * @param status text to add.
      */
     private void appendStatus(String status) {
 
         TextView text = new TextView(this);
         text.setId((int)System.currentTimeMillis());
         text.setText(status);
         text.setTextColor(Color.parseColor("#8AE6B8"));
         text.setGravity(Gravity.BOTTOM);
 
         text.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                 ViewGroup.LayoutParams.WRAP_CONTENT));
 
 
         chatContainer.addView(text);
         chatContainer.refreshDrawableState();
 
         chatContainerFrame.scrollTo(0, chatContainer.getBottom());
 
     }
 
     /**
      * Add new chat message to chatroom.
      * @param message text to add.
      */
     public void appendMessage(String message){
 
         TextView text = new TextView(this);
         text.setId((int)System.currentTimeMillis());
         text.setText(message);
         text.setGravity(Gravity.BOTTOM);
         text.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                 ViewGroup.LayoutParams.WRAP_CONTENT));
 
         chatContainer.addView(text);
         chatContainer.refreshDrawableState();
 
         chatContainerFrame.scrollTo(0, chatContainer.getBottom());
 
         chatContainerFrame.post(new Runnable() {
             @Override
             public void run() {
                 chatContainerFrame.smoothScrollTo(0, chatContainer.getBottom());
             }
         });
     }
 
     /**
      * Method ini dipanggil ketika user tap button send.
      * untuk mengirim pesan.
      * @param view view.
      */
     @Override
     public void onClick(View view) {
         EditText text = (EditText)findViewById(R.id.inputMessage);
 
         GethubClient gethub = GethubClient.getInstance();
 
         String message = text.getText().toString();
 
         if (message.length() > 0){
             gethub.message(channel, message, sessid);
 
             InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
             imm.hideSoftInputFromWindow(text.getWindowToken(), 0);
         }
 
         text.setText("");
     }
 }
