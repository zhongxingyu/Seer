 package com.rnm.keepintouch;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.ActivityNotFoundException;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 import android.util.Log;
 
 import com.rnm.keepintouch.data.Contact;
 
 public class ContactDialog extends DialogFragment {
 	
 	Contact contact;
 	
 	public ContactDialog(Contact contact){
 		this.contact = contact;
 	}
 	
 	@Override
     public Dialog onCreateDialog(Bundle savedInstanceState) {
         // Use the Builder class for convenient dialog construction
         AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
         builder.setMessage("Would you like to call or text this person?")
                .setPositiveButton("Call", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        call(contact.phonenumber.get(0));
                    }
                })
                .setNeutralButton("SMS", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        sms(contact.phonenumber.get(0));
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dismiss();
                    }
                });
         // Create the AlertDialog object and return it
         return builder.create();
     }
 	
 	private void call(String phone) {
 	    try {
 	        Intent callIntent = new Intent(Intent.ACTION_CALL);
 	        callIntent.setData(Uri.parse("tel:" + phone));
 	        startActivity(callIntent);
 	    } catch (ActivityNotFoundException e) {
 	        Log.e("helloandroid dialing example", "Call failed", e);
 	    }
 	}
 	
 	private void sms(String phone){
 		Intent sendIntent = new Intent(Intent.ACTION_VIEW);         
 		sendIntent.setData(Uri.parse("sms:" + phone));
 	}
 }
