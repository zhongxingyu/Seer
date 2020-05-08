 package com.r0adkll.sparc.pillalarm.ui;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.View;
 import android.view.Window;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.EditText;
 import android.widget.ImageView;
 
 import com.r0adkll.sparc.pillalarm.R;
 import com.r0adkll.sparc.pillalarm.server.model.Schedule;
 
 /**
  * Created by r0adkll on 8/24/13.
  */
 public class AddNewScheduleDialog extends DialogFragment {
 
     /**
      * Create a new instance of this dialog fragment
      * @return      shits weak.
      */
     public static AddNewScheduleDialog createInstance(int quantity){
         AddNewScheduleDialog frag =  new AddNewScheduleDialog();
         frag.quantity = quantity;
         return frag;
     }
 
     /*************************************************
      *
      * Variables
      *
      */
 
     private int quantity;
     private int amt = -1;
     private int freq = -1;
     private int dur;
     private EditText mAmt, mFreq, mDur;
     private OnAddActionListener listener;
     private InputMethodManager imm;
 
     /*************************************************
      *
      * Lifecycle Methods
      *
      */
 
     @Override
     public void onActivityCreated(Bundle savedInstanceState) {
         super.onActivityCreated(savedInstanceState);
         setStyle(STYLE_NO_TITLE, R.style.Theme_PillAlarm_Dialog);
         imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
     }
 
     @Override
     public Dialog onCreateDialog(Bundle savedInstanceState) {
         View layout = getActivity().getLayoutInflater().inflate(R.layout.layout_addschedule_dialog, null, false);
         mAmt = (EditText) layout.findViewById(R.id.field_amount);
         mFreq = (EditText) layout.findViewById(R.id.field_frequency);
         mDur = (EditText) layout.findViewById(R.id.field_duration);
 
         // add text watchers
         mAmt.addTextChangedListener(new TextWatcher() {
             @Override
             public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
 
             }
 
             @Override
             public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                 try{
                     amt = Integer.valueOf(mAmt.getText().toString());
                     adjustDuration();
                 } catch(NumberFormatException e){
 
                 }
             }
 
             @Override
             public void afterTextChanged(Editable editable) {
 
             }
         });
 
         mFreq.addTextChangedListener(new TextWatcher() {
             @Override
             public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
 
             }
 
             @Override
             public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                 try{
                     freq = Integer.valueOf(mFreq.getText().toString());
                     adjustDuration();
                 } catch(NumberFormatException e){
 
                 }
             }
 
             @Override
             public void afterTextChanged(Editable editable) {
 
             }
         });
 
         Dialog dialog = new AlertDialog.Builder(getActivity())
                 .setView(layout)
                 .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialogInterface, int i) {
                         int amt = Integer.valueOf(mAmt.getText().toString());
                         int freq = Integer.valueOf(mFreq.getText().toString());
                         int dur = Integer.valueOf(mDur.getText().toString());
 
                         Schedule newSched = new Schedule(amt, freq, dur);
 
                         // Kill the Keyboard
                         imm.hideSoftInputFromWindow(mAmt.getWindowToken(), 0);
                         imm.hideSoftInputFromWindow(mFreq.getWindowToken(), 0);
                         imm.hideSoftInputFromWindow(mDur.getWindowToken(), 0);
 
                         // KILL THIS BITCH AGAIN!!!!!
                         if(listener != null) listener.onScheduleOk(newSched);
 
                         dialogInterface.dismiss();
                     }
                 })
                 .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialogInterface, int i) {
                         // Kill this bitch
                         // Kill the Keyboard
                         imm.hideSoftInputFromWindow(mAmt.getWindowToken(), 0);
                         imm.hideSoftInputFromWindow(mFreq.getWindowToken(), 0);
                         imm.hideSoftInputFromWindow(mDur.getWindowToken(), 0);
 
                         dialogInterface.dismiss();
                         if(listener != null) listener.onCancelClick();
                     }
                 })
                 .create();
         dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
         dialog.setOnShowListener(new DialogInterface.OnShowListener() {
             @Override
             public void onShow(DialogInterface dialogInterface) {
                 imm.showSoftInput(mAmt, 0);
             }
         });
         return dialog;
     }
 
     public void adjustDuration(){
         if(amt != -1 && freq != -1){
             // Compute duration in days
             float rate = (24f / (float)freq);
             int ppd = (int) rate * amt;
            if(ppd == 0) ppd = 1;
             dur = (quantity / ppd);
 
             // Update text view
             mDur.setText(dur + "");
         }
 
 
     }
 
     public void setAddActionListener(OnAddActionListener listener){
         this.listener = listener;
     }
 
 
     public static interface OnAddActionListener{
         public void onScheduleOk(Schedule newSched);
         public void onCancelClick();
     }
 
 }
