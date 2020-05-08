 package com.zvelo.walletcracker;
 
 import android.app.ProgressDialog;
 import android.content.Context;
 
 public class ZveloProgressDialog extends ProgressDialog {
   public ZveloProgressDialog(Context context) {
     super(context);
   }
 
   public void update(Integer messageId, Integer progress, Integer numSteps) {
     if (messageId != null) {
       setMessage(getContext().getString(messageId));
     }
 
     if ((progress == null) || (numSteps == null) || (numSteps == 0)) {
       setIndeterminate(true);
     } else {
       setIndeterminate(false);
       setMax(numSteps);
       setProgress(progress);
     }
   }
 }
