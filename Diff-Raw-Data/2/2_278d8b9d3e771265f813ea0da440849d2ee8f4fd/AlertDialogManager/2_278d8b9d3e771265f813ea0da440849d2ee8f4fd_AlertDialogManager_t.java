 package edu.ucsb.cs176b.models;
 
 import edu.ucsb.cs176b.aggregator.R;
//import edu.ucsb.cs176b.aggregator.R.drawable;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 
 public class AlertDialogManager {
 
 	public void showAlertDialog(Context context, String title, String message,
 			Boolean status) {
 
 		AlertDialog.Builder builder = new AlertDialog.Builder(context);
 		
 		builder.setTitle(title)
 			.setMessage(message)
 			.setPositiveButton("OK", new OnClickListener() {
 				
 				@Override
 				public void onClick(DialogInterface dialog, int which) {}
 			});
 
 		if(status != null)
 			builder.setIcon((status) ? R.drawable.success : R.drawable.fail);
 		
 		AlertDialog alert = builder.create();
 		alert.show();
 	}
 }
