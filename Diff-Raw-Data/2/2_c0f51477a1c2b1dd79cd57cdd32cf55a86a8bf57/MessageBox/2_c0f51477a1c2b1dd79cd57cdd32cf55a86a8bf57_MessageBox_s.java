 package net.cyclestreets.util;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
import net.cyclestreets.maps.uk.R;
 
 public class MessageBox 
 {
   static final DialogInterface.OnClickListener NoAction = 
      new DialogInterface.OnClickListener() {
         public void onClick(DialogInterface arg0, int arg1) {  }
      };
   
   static public void YesNo(final Context context, 
                            final int msgId, 
                            final DialogInterface.OnClickListener yesAction,
                            final DialogInterface.OnClickListener noAction)
   {
     final AlertDialog.Builder alertbox = newBuilder(context);
     alertbox.setMessage(context.getString(msgId))
             .setPositiveButton(R.string.yes, yesAction)
             .setNegativeButton(R.string.no, noAction);
     show(alertbox);
   } // YesNo
   
   static AlertDialog.Builder newBuilder(final Context context)
   {
     final AlertDialog.Builder builder = new AlertDialog.Builder(context);
     builder.setTitle("CycleStreets");
     return builder;
   } // newBuilder
   
   static void show(final AlertDialog.Builder builder)
   {
     final AlertDialog ad = builder.create();
     ad.show();
   } // show
 
 } // class MessageBox
