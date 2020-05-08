 package mx.ferreyra.dogapp.ui;
 
 import android.app.AlertDialog;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.widget.Toast;
 
 import mx.ferreyra.dogapp.R;
 
 public class DialogHelper {
 
    public static DialogInterface.OnClickListener ONLY_DISMISS =
	new DialogInterface.OnClickListener() {
	    public void onClick(DialogInterface dialog, int which) {
		dialog.dismiss();
	    }
	};

     public static void showYesNoDialog(Context context,
                                        String message,
                                        DialogInterface.OnClickListener yesListener,
                                        DialogInterface.OnClickListener noListener) {
         AlertDialog alert = new AlertDialog.Builder(context)
             .setMessage(message)
             .setCancelable(false)
             .setPositiveButton(R.string.yes_message, yesListener)
             .setNegativeButton(R.string.no_message, noListener)
             .create();
         alert.setTitle(R.string.title_message);
         alert.setIcon(R.drawable.icon);
         alert.show();
     }
 
     public static void showOkDialog(Context context,
                                     String message,
                                     DialogInterface.OnClickListener okListener) {
         AlertDialog alert = new AlertDialog.Builder(context)
             .setMessage(message)
             .setCancelable(false)
             .setNeutralButton(R.string.ok_message, okListener)
             .create();
         alert.setTitle(R.string.title_message);
         alert.setIcon(R.drawable.icon);
         alert.show();
     }
 }
