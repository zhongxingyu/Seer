 package com.example.myapplication;
 
 import android.content.ClipData;
 import android.content.ClipboardManager;
 
 /**
  * Created by greenja01 on 9/11/13.
  */
 public class ClipBoardMessageSender implements MessageSender {
 
     ClipboardManager clipboardManager;
     public ClipBoardMessageSender(Object service)
     {
        clipboardManager = (ClipboardManager)service;
     }
 
 
     public void SendMessage(String message){
 
         ClipData clip = ClipData.newPlainText("status", message);
         clipboardManager.setPrimaryClip(clip);
     }
 }
