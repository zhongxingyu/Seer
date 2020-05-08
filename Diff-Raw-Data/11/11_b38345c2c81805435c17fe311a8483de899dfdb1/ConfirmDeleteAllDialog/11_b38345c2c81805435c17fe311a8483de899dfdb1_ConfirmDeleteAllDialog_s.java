 package com.werebug.randomsequencegenerator;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 
 public class ConfirmDeleteAllDialog extends DialogFragment {
 	
 	public interface ConfirmDeleteAllListener {
 		public void onConfirmDeleteAllPositive(DialogFragment dialog);
 		public void onConfirmDeleteAllNegative(DialogFragment dialog);
 	}
 	
 	ConfirmDeleteAllListener mListener;
 	
 	// Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
     @Override
     public void onAttach(Activity activity) {
         super.onAttach(activity);
         // Verify that the host activity implements the callback interface
         try {
             // Instantiate the NoticeDialogListener so we can send events to the host
             mListener = (ConfirmDeleteAllListener) activity;
         } catch (ClassCastException e) {
             // The activity doesn't implement the interface, throw exception
             throw new ClassCastException(activity.toString()
                     + " must implement ConfirmDeleteAllListener");
         }
     }
 	
     @Override
     public Dialog onCreateDialog(Bundle savedInstanceState) {
         // Build the dialog and set up the button click handlers
         AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                 
        builder.setMessage(R.string.confirm_question)
               .setPositiveButton(R.string.confirm_deletion_all, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Send the positive button event back to the host activity
                        mListener.onConfirmDeleteAllPositive(ConfirmDeleteAllDialog.this);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Send the negative button event back to the host activity
                        mListener.onConfirmDeleteAllNegative(ConfirmDeleteAllDialog.this);
                    }
                });
         return builder.create();
     }
 	
 }
