 package com.augmentum.note.fragment;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 import android.widget.EditText;
 import android.widget.Toast;
 import com.augmentum.note.NoteApplication;
 import com.augmentum.note.R;
 import com.augmentum.note.util.DialogUtil;
 import com.augmentum.note.util.Md5Util;
 
 public class LoginDialogFragment extends DialogFragment {
 
     public interface OnClickListener {
         public void onClick();
     }
 
     private OnClickListener mOnClickListener;
 
     public void setOnClickListener(OnClickListener onClickListener) {
         mOnClickListener = onClickListener;
     }
 
     @Override
     public Dialog onCreateDialog(Bundle savedInstanceState) {
 
         AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
         builder.setMessage(R.string.note_password_input_title);
         final EditText editText = new EditText(getActivity());
         builder.setView(editText);
         builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialog, int which) {
                 SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
                 String password = sharedPref.getString("password", "no password");
 
                if (password.equals(Md5Util.getMD5(editText.getText().toString()))) {
                     NoteApplication.sIsLogin = true;
                     DialogUtil.openDismiss(dialog);
                     dismiss();
                 } else {
                     Toast.makeText(getActivity(), "your password is wrong", Toast.LENGTH_SHORT).show();
                     DialogUtil.closeDismiss(dialog);
                 }
 
                 if (null != mOnClickListener) {
                     mOnClickListener.onClick();
                 }
             }
         });
 
         AlertDialog dialog = builder.create();
         dialog.setCanceledOnTouchOutside(false);
         return dialog;
     }
 }
