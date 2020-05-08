 package com.augmentum.note.fragment;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 import android.view.View;
 import android.widget.EditText;
 import com.augmentum.note.R;
 import com.augmentum.note.util.DialogUtil;
 import com.augmentum.note.util.Md5Util;
 
 public class SetPasswordDialogFragment extends DialogFragment {
 
     public static final String TAG = "setPasswordDialogFragment";
 
     @Override
     public Dialog onCreateDialog(Bundle savedInstanceState) {
         AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
         builder.setMessage(R.string.set_password_setting_title);
         View view = getActivity().getLayoutInflater().inflate(R.layout.set_password_dialog, null);
         builder.setView(view);
 
         final EditText password = (EditText) view.findViewById(R.id.note_list_set_password);
         final EditText confirm = (EditText) view.findViewById(R.id.note_list_set_password_confirm);
 
         builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int id) {
 
                if (null != password.getText() && null != confirm.getText()
                        && password.getText().toString().equals(confirm.getText().toString())) {
                     SharedPreferences sharePref = getActivity().getPreferences(Context.MODE_PRIVATE);
                     SharedPreferences.Editor editor = sharePref.edit();
                     editor.putString("password", Md5Util.getMD5(password.getText().toString()));
                     editor.commit();
                     DialogUtil.openDismiss(dialog);
                     dismiss();
                 } else {
                     DialogUtil.closeDismiss(dialog);
                 }
 
             }
         });
 
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int id) {
                DialogUtil.openDismiss(dialog);
             }
         });
 
         return builder.create();
     }
 }
