 package com.zvelo.walletcracker;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.DialogFragment;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.util.Log;
 
 public class ErrorDialogFragment extends DialogFragment {
   static public final String TAG = "ErrorDialogFragment";
 
   public static ErrorDialogFragment newInstance(int messageId) {
     ErrorDialogFragment fragment = new ErrorDialogFragment();
 
     Bundle args = new Bundle();
     args.putInt("messageId", messageId);
     fragment.setArguments(args);
 
     return fragment;
   }
 
   @Override public void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
 
     // savedInstanceState could be null
     if (savedInstanceState != null) {
       // TODO
     }
 
     Log.i(TAG, "onCreate");
   }
 
   @Override public void onActivityCreated(Bundle savedInstanceState) {
     super.onActivityCreated(savedInstanceState);
     Log.i(TAG, "onActivityCreated");
   }
 
   @Override public void onSaveInstanceState(Bundle outState) {
     super.onSaveInstanceState(outState);
     Log.i(TAG, "onSaveInstanceState");
   }
 
   @Override public Dialog onCreateDialog(Bundle savedInstanceState) {
     super.onCreateDialog(savedInstanceState);
     final int messageId = getArguments().getInt("messageId");
     Log.i(TAG, "onCreateDialog");
 
     if (messageId == 0) {
       Log.d(TAG, "Not creating invalid error");
       return null;
     }
 
     setCancelable(false);
 
    return new AlertDialog.Builder(getActivity())
             .setTitle(R.string.error)
             .setMessage(messageId)
             .setCancelable(false)
             .setIcon(R.drawable.ic_dialog_alert_holo_light)
             .setNeutralButton(R.string.error_dialog_ok, new DialogInterface.OnClickListener() {
               @Override public void onClick(DialogInterface dialog, int which) {
                 // What to do?
               }
             })
             .create();
   }
 
   @Override public void onCancel(DialogInterface dialog) {
     super.onCancel(dialog);
     Log.i(TAG, "onCancel");
   }
 
   @Override public void onDestroyView() {
     super.onDestroyView();
     Log.i(TAG, "onDestroyView");
   }
 
   @Override public void onDetach() {
     super.onDetach();
     Log.i(TAG, "onDetach");
   }
 
   @Override public void onDismiss(DialogInterface dialog) {
     super.onDismiss(dialog);
     Log.i(TAG, "onDismiss");
   }
 }
