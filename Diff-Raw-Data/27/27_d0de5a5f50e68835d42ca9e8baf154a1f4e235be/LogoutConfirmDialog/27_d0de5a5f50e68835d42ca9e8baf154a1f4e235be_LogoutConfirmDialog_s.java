 package nl.sense_os.app.dialogs;
 
 import nl.sense_os.app.R;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.os.Build;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 
 public class LogoutConfirmDialog extends DialogFragment {
 
     public static interface LogoutActivity {
         void logout();
     }
 
     private LogoutActivity listener;
 
     public LogoutConfirmDialog(LogoutActivity listener) {
         this.listener = listener;
     }
 
     @Override
     public Dialog onCreateDialog(Bundle savedInstanceState) {
 
         Bundle args = getArguments();
         String username = args.getString("username");
 
         // create builder
         AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
             // specifically set dark theme for Android 3.0+
             builder = new AlertDialog.Builder(getActivity(), AlertDialog.THEME_HOLO_DARK);
         }
 
         builder.setIcon(android.R.drawable.ic_dialog_alert);
         builder.setMessage(R.string.dialog_logout_msg);
         builder.setTitle(getString(R.string.dialog_logout_title, username));
         builder.setPositiveButton(R.string.button_logout, new OnClickListener() {
 
             @Override
             public void onClick(DialogInterface dialog, int which) {
                 listener.logout();
             }
         });
         builder.setNegativeButton(android.R.string.cancel, null);
         return builder.create();
     }
 }
