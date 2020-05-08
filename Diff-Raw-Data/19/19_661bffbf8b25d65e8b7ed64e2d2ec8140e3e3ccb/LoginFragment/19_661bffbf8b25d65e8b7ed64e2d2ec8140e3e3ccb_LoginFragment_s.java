 package com.storassa.android.scuolasci;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.DialogFragment;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.EditText;
 
 public class LoginFragment extends DialogFragment {
    Dialog dialog;
    String username, password;
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.login_fragment, null);
 
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(view)
        // Add action buttons
               .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int id) {
                      String usr = ((EditText)view.findViewById(R.id.username)).getText().toString();
                      String pwd = ((EditText)view.findViewById(R.id.password)).getText().toString();
                     ((MainActivity)getActivity()).loginUser(usr, pwd, true);
                   }
               })
               .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       LoginFragment.this.getDialog().cancel();
                   }
               });      
        return builder.create();
    }
 }
