 package com.cs411.packman;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 import android.support.v4.view.ViewPager;
 import android.view.LayoutInflater;
 import android.widget.EditText;
 
 import com.example.packman.R;
 
 public class LoginDialogFragment extends DialogFragment {
 	@Override
 	public Dialog onCreateDialog(Bundle savedInstanceState) {
 	    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
 	    // Get the layout inflater
 	    LayoutInflater inflater = getActivity().getLayoutInflater();
 
 	    // Inflate and set the layout for the dialog
 	    // Pass null as the parent view because its going in the dialog layout
 	    builder.setView(inflater.inflate(R.layout.login_dialog, null))
 	    // Add action buttons
 	           .setPositiveButton(R.string.login, new DialogInterface.OnClickListener() {
 	               @Override
 	               public void onClick(DialogInterface dialog, int id) {
 	            	// Send the positive button event back to the host activity
                        mListener.loginUser(LoginDialogFragment.this);
 	               }
 	           })
 	           .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
 	               public void onClick(DialogInterface dialog, int id) {
 	                   LoginDialogFragment.this.getDialog().cancel();
 	               }
 	           });      
 	    return builder.create();
 	}
 	
 	/* The activity that creates an instance of this dialog fragment must
      * implement this interface in order to receive event callbacks.
      * Each method passes the DialogFragment in case the host needs to query it. */
     public interface LoginDialogListener {
         public void loginUser(DialogFragment dialog);
     }
     
     // Use this instance of the interface to deliver action events
     LoginDialogListener mListener;
     
    // Override the Fragment.onAttach() method to instantiate the LoginDialogListener
     @Override
     public void onAttach(Activity activity) {
         super.onAttach(activity);
         // Verify that the host activity implements the callback interface
         try {
            // Instantiate the LoginDialogListener so we can send events to the host
             mListener = (LoginDialogListener) activity;
         } catch (ClassCastException e) {
             // The activity doesn't implement the interface, throw exception
             throw new ClassCastException(activity.toString()
                    + " must implement LoginDialogListener");
         }
     }
 }
