 package edu.rit.csh.androidwebnews;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.DialogFragment;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 
 /**
  * The dialog object used when the stored API is invalid.
  * This provides a settings button, which goes to settings
  * and a cancel button.
  */
 public class InvalidApiKeyDialog extends AlertDialog {
 	Context c;
 	protected InvalidApiKeyDialog(Context context) {
 		super(context);
 		c = context;
		setTitle("Warning");
 		setMessage("Invalid API key");
 		setCancelable(true);
 		setButton(BUTTON_POSITIVE, "Settings", new settingsListener());
 		setButton(BUTTON_NEGATIVE, "Cancel", new cancelListener());
 		
 	}
 	
 	private final class settingsListener implements DialogInterface.OnClickListener {
 
 		@Override
 		public void onClick(DialogInterface dialog, int which) {
 			c.startActivity(new Intent(c, SettingsActivity.class));
 			
 		}
 		
 	}
 	
 	private final class cancelListener implements DialogInterface.OnClickListener {
 
 		@Override
 		public void onClick(DialogInterface dialog, int which) {
 			dialog.cancel();
 		}
 		
 	}
 
 }
