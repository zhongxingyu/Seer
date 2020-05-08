 package nl.sense_os.app.dialogs;
 
 import nl.sense_os.app.R;
import android.annotation.TargetApi;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.os.Build;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 
 public class UpdateAlertDialog extends DialogFragment {
 
    @TargetApi(11)
     @Override
     public Dialog onCreateDialog(Bundle savedInstanceState) {
         // create builder
         AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
             // specifically set dark theme for Android 3.0+
             builder = new AlertDialog.Builder(getActivity(), AlertDialog.THEME_HOLO_DARK);
         }
 
         builder.setIcon(android.R.drawable.ic_dialog_alert);
         builder.setMessage(R.string.dialog_update_msg);
         builder.setTitle(R.string.dialog_update_title);
         builder.setPositiveButton(android.R.string.ok, null);
         builder.setCancelable(false);
         return builder.create();
     }
 }
