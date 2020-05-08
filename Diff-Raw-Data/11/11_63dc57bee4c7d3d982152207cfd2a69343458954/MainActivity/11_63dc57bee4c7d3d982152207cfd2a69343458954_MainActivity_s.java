 package com.moroze.snapchat;
 
 import java.io.File;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.os.Environment;
 import android.text.Html;
 import android.text.Spanned;
 import android.text.method.LinkMovementMethod;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.TextView;
 
 import com.xdatv.xdasdk.Shell;
 
 /*
  * Snapchat Keeper by Noah Moroze (https://github.com/nmoroze)
  * Shell class borrowed from Adam Outler (see class for licensing info)
  * You are free to use this code in a non-commercial context and with attribution to the creator
  */
 
 public class MainActivity extends Activity {
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 	}
 
 	public void keepSnaps(View v) {
 		//keep track of any errors that may occur
 		boolean error=false;
 		String errorText="";
 		File storageRoot = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
 		String storagePath = storageRoot.getAbsolutePath();
 		storagePath+="/Kept_Snaps/";
 		File dirToMake = new File(storagePath);
 		dirToMake.mkdir();
 		
 		//when button pressed, sends series of shell commands one by one and prints their output to console for debugging
 		//this could probably be done a lot nicer, but it works
 		Shell shell = new Shell();		
 
 		String cmds[] = {"su","-c","cp /data/data/com.snapchat.android/cache/received_image_snaps/* "+storagePath}; //copies files from cached snaps to the new folder
 		String out = shell.sendShellCommand(cmds);
 		System.out.println(out);
 		if(!out.equals("")) {
 			error=true;
 			errorText=out;
 		}
 		
 		String stripFileCmds[] = {"su","-c","for f in "+storagePath+"*.nomedia; do mv $f "+storagePath+"`basename $f .nomedia`; done;"}; //strips files of ".nomedia" extension, leaving plain jpegs
 		out = shell.sendShellCommand(stripFileCmds);
 		System.out.println(out);
 		if(out.equals("\nPermission denied")||out.equals("CritERROR!!!")) {
 			error=true;
 			errorText="root";
 		}
 		else if(!out.equals("")) {
 			error=true;
 			errorText=out;
 		}
 		
 		if(error&&errorText.equals("root")) {
 			alert("Error!", "You do not have root access to your phone, so this app is incompatible. Please do not give a poor rating, as the description states this app will not work if you don't have root.");
 		}
 		else if(error) {
			alert("Error!","An error occurred! For help, please email the developer (nzmtechcontact@gmail.com) with the following error message: \n"+errorText);
 		}
 		else {
 			alert("Success!", "Check in your Pictures/Kept_Snaps/ folder to view any snaps you have kept!");
 		}		
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		//automatically generated function for menu
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 	    //no need to check the item, there's only one (a little improper, but okay)
 		//show about dialog
 	    switch (item.getItemId()) {
 	        case R.id.about:
 	        	 alert("About",
 	     	        	"Snapchat Keeper by Noah Moroze (This app requires root to function)\n\n" +
 	     	        	"This app allows you to permanently keep Snapchat images. To use, simply ensure that snaps have been loaded but not opened (it will say 'press and hold to view' below the snap)." +
 	     	        	"Open this app and press the button. Your unopened snaps will automatically be stored as standard jpeg files in the Kept_Snaps folder under Pictures.\n" +
 	     	        	"As snaps are expected to be erased, please do not violate someone's privacy and warn them ahead of time if you are storing the image they sent you.");		
 	            return true;
	        case R.id.donate:
	            linkAlert("Donate",
	            		Html.fromHtml("Please visit <a href=\"http://goo.gl/IvHcR\">goo.gl/IvHcR</a> if you wish to donate via Paypal to the development of this app. If you don't wish to donate money, a 5-star review on Google Play is highly appreciated. Thanks!"));
	            return true;
 	        default:
 	            return super.onOptionsItemSelected(item);
 	    }
 
 	}
 	
 	private void alert(String title, String msg) {
         new AlertDialog.Builder(this)
         .setTitle(title)
         .setMessage(msg)
         .setPositiveButton("Okay", new DialogInterface.OnClickListener()
         {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				//nothing to do here
 			}
 		}).show();
 	}
 	private void linkAlert(String title, Spanned msg) {
 		final AlertDialog a = 
 		new AlertDialog.Builder(this)
         .setTitle(title)
         .setMessage(msg)
         .setPositiveButton("Okay", new DialogInterface.OnClickListener()
         {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				//nothing to do here
 			}
 		}).create();
 		a.show();
 		((TextView) a.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
 	}
 }
