 package eu.trentorise.smartcampus.ac;
 
 
 import java.io.IOException;
 
 import android.accounts.AuthenticatorException;
 import android.accounts.OperationCanceledException;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import eu.trentorise.smartcampus.ac.authenticator.AMSCAccessProvider;
 
 
 public class UserRegistration {
 	static AlertDialog.Builder builder;
 	public static void upgradeuser(final Context ctx, final Activity activity) {
 			builder = new AlertDialog.Builder(ctx);
 			final AMSCAccessProvider accessprovider = new AMSCAccessProvider();
 			//
 				// dialogbox for registration
 				DialogInterface.OnClickListener updateDialogClickListener;
 
 				updateDialogClickListener = new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 
 
 
 							switch (which) {
 							case DialogInterface.BUTTON_POSITIVE:
 								//upgrade the user
 								AMSCAccessProvider ac = new AMSCAccessProvider();
 								ac.promote(activity, null, ac.readToken(ctx, null));
 								break;
 
 							case DialogInterface.BUTTON_NEGATIVE:
 								//CLOSE
 								
 								break;
 							
 							}
 
 					}
 				};
 				
				builder.setCancelable(false).setMessage(ctx.getString(R.string.auth_question))
 						.setPositiveButton(android.R.string.yes, updateDialogClickListener)
 						.setNegativeButton(android.R.string.no, updateDialogClickListener).show();
 			
 		
 }
 }
