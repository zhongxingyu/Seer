 package com.zvelo.walletcracker;
 
 import android.app.Dialog;
 import android.app.DialogFragment;
 import android.app.ProgressDialog;
 import android.os.Bundle;
 
 public class ProgressDialogFragment extends DialogFragment {
   static public final String TAG = "ProgressDialogFragment";
 
   public static ProgressDialogFragment newInstance(Integer messageId, Integer progress, Integer numSteps) {
     ProgressDialogFragment fragment = new ProgressDialogFragment();
 
     Bundle args = new Bundle();
     args.putInt("messageId", messageId);
     args.putInt("progress", progress);
     args.putInt("numSteps", numSteps);
     fragment.setArguments(args);
 
     return fragment;
   }
 
   @Override public Dialog onCreateDialog(Bundle savedInstanceState) {
     super.onCreateDialog(savedInstanceState);
 
     final Integer messageId = getArguments().getInt("messageId");
     final Integer progress = getArguments().getInt("progress");
     final Integer numSteps = getArguments().getInt("numSteps");
 
    ZveloProgressDialog dialog = new ZveloProgressDialog(getActivity());
 
     dialog.setTitle(R.string.loading);
     dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
     dialog.setCancelable(false);
     dialog.update(messageId, progress, numSteps);
     dialog.setIcon(R.drawable.ic_dialog_time);
 
     return (Dialog) dialog;
   }
 
   public void update(Integer messageId, Integer progress, Integer numSteps) {
     ZveloProgressDialog dialog = (ZveloProgressDialog) getDialog();
     if (dialog == null) {
       return;
     }
     dialog.update(messageId, progress, numSteps);
   }
 }
