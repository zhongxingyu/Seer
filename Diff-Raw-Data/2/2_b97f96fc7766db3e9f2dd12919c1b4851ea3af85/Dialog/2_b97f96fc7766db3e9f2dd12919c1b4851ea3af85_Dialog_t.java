 package js.g;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 
 public class Dialog {
     public static String alert(final String message) {
         JSG.runOnUiThread(new Runnable() {
             public void run() {
                 new AlertDialog.Builder(JSGActivity.getInstance())
                 .setPositiveButton("OK",
                     new DialogInterface.OnClickListener() {
                         public void onClick(DialogInterface dlg, int which) {
                         }
                     }
                 ).setMessage(message)
                 .show();
             }
         });
         return null;
     }
 
     public static String confirm(final String callbackId_message) {
         JSG.runOnUiThread(new Runnable() {
             public void run() {
                 int commaPos = callbackId_message.indexOf(',');
                 final String callbackId = callbackId_message.substring(0, commaPos);
                final String message    = callbackId_message.substring(commaPos + 1);
 
                 new AlertDialog.Builder(JSGActivity.getInstance())
                 .setMessage(message)
                 .setPositiveButton("OK",
                     new DialogInterface.OnClickListener() {
                         public void onClick(DialogInterface dlg, int which) {
                             JSG.postJS(String.format("jsg.callback.invoke(%s, true)", callbackId));
                         }
                     }
                 )
                 .setNegativeButton("Cancel",
                     new DialogInterface.OnClickListener() {
                         public void onClick(DialogInterface dlg, int which) {
                             JSG.postJS(String.format("jsg.callback.invoke(%s, false)", callbackId));
                         }
                     }
                 )
                 .show();
             }
         });
         return null;
     }
 }
