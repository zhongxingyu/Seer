 package com.mbcdev.nextluas.activities;
 
 import com.mbcdev.nextluas.R;
 
 import android.app.AlertDialog;
 import android.app.AlertDialog.Builder;
 import android.content.DialogInterface;
 import android.os.Bundle;
 
 public abstract class AbstractActivity extends RoboSherlockActivity  {
 
   protected static final int UPDATE_MS = 15000;
   protected static final int UPDATE_DISTANCE = 10;
   
   @Override
   protected void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
   }
   
   protected void makeInfoDialogue(String error) {
     Builder alertDialog = new AlertDialog.Builder(this);
     alertDialog.setTitle(R.string.connectorDialogTitle);
 
     StringBuilder sb = new StringBuilder();
     sb.append(getString(R.string.connectorDialogMessage)).append("\n").append(error);
 
     alertDialog.setMessage(sb.toString());
 
     alertDialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
 
       @Override
       public void onClick(DialogInterface dialog, int which) {
         // OK!
       }
     });
   }
 }
