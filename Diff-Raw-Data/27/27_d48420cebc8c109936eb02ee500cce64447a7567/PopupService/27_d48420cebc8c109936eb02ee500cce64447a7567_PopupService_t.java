 package com.dhbw.dvst.helper;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 
 import com.dhbw.dvst.R;
 import com.dhbw.dvst.model.Spieler;
 
 public abstract class PopupService {
 
 	//Fehlermeldung
 	public PopupService(Activity activity, String fehler) {
 		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
 		setzeNegativenButton(fehler, builder);
 		AlertDialog alert = builder.create();
 		alert.show();
 	}
 	
 	//Loeschdialog
 	public PopupService(Activity activity, String fehler, Spieler spieler) {
 		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
 		setzeZweiButtons(fehler, builder);
 		AlertDialog alert = builder.create();
 		alert.show();
 	}
 	
	protected void setzeNegativenButton(String fehler, AlertDialog.Builder builder) {
 		builder.setMessage(fehler)
 			.setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					dialog.cancel();											
 				}
 			});
 	}
 
	protected void setzeZweiButtons(String fehler, AlertDialog.Builder builder) {
 		builder.setMessage(fehler)
 			.setPositiveButton(R.string.ja, new DialogInterface.OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					klickePositivenButton();
 				}
 			})
 			.setNegativeButton(R.string.nein, new DialogInterface.OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					dialog.cancel();											
 				}
 			});
 	}
 
 	protected void klickePositivenButton() {
 	}
 }
