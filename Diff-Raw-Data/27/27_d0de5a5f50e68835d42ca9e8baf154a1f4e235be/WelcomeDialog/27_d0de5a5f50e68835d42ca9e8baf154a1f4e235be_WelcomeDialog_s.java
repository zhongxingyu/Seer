 package nl.sense_os.app.dialogs;
 
 import nl.sense_os.app.R;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.os.Build;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 
 public class WelcomeDialog extends DialogFragment {
 
     public static interface WelcomeActivity {
         void startLogin();
 
         void startRegister();
 
         void showFaq();
     }
 
     private WelcomeActivity listener;
 
     public static WelcomeDialog newInstance(WelcomeActivity listener) {
         WelcomeDialog dialog = new WelcomeDialog();
         dialog.setListener(listener);
         return dialog;
     }
 
     private void setListener(WelcomeActivity listener) {
         this.listener = listener;
     }
 
     @Override
     public Dialog onCreateDialog(Bundle savedInstanceState) {
         // create builder
         AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
             // specifically set dark theme for Android 3.0+
             builder = new AlertDialog.Builder(getActivity(), AlertDialog.THEME_HOLO_DARK);
         }
 
         builder.setIcon(android.R.drawable.ic_dialog_info);
         builder.setTitle(R.string.dialog_welcome_title);
         builder.setMessage(R.string.dialog_welcome_msg);
         builder.setPositiveButton(R.string.button_login, new OnClickListener() {
 
             @Override
             public void onClick(DialogInterface dialog, int which) {
                 listener.startLogin();
             }
         });
         builder.setNeutralButton(R.string.button_reg, new OnClickListener() {
 
             @Override
             public void onClick(DialogInterface dialog, int which) {
                 listener.startRegister();
             }
         });
         builder.setNegativeButton(R.string.button_faq, new OnClickListener() {
 
             @Override
             public void onClick(DialogInterface dialog, int which) {
                 listener.showFaq();
             }
         });
         return builder.create();
     }
 }
