 package com.cannedapps.cachat.app.views;
 
 import roboguice.activity.RoboActivity;
 import roboguice.inject.InjectView;
import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 
 import com.cannedapps.cachat.R;
 import com.cannedapps.cachat.app.presenters.ChatPresenter;
 import com.cannedapps.cachat.app.views.interfaces.IChatView;
 
 public class ChatView extends RoboActivity implements IChatView, OnClickListener {
 
   private ChatPresenter chatPresenter;
   @InjectView(R.id.chat)
   TextView chatText;
   @InjectView(R.id.message)
   EditText messageEditText;
   @InjectView(R.id.send)
   Button sendButton;
 
   @Override
   protected void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
     setContentView(R.layout.main);
     chatPresenter = new ChatPresenter(this);
     sendButton.setOnClickListener(this);
   }
 
   @Override
   public void appendNewMessage(String message) {
     chatText.append(message);
   }
 
   @Override
   public void onClick(View view) {
     switch (view.getId()) {
       case R.id.send :
         chatPresenter.addMessage(messageEditText.getText().toString());
         break;
     }
   }
 }
