 package com.nullsys.smashsmash;
 
 import android.app.AlertDialog;
 import android.app.AlertDialog.Builder;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.os.Bundle;
 
 import com.badlogic.gdx.backends.android.AndroidApplication;
import com.noobs2d.smashsmash.SmashSmash;
 
 public class SmashSmashAndroid extends AndroidApplication {
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
 	super.onCreate(savedInstanceState);
 	initialize(new SmashSmash(), false);
     }
 
     @Override
     protected Dialog onCreateDialog(int id) {
 	String message = "Are you sure you want to exit?";
 	Builder builder = new AlertDialog.Builder(this);
 	builder.setMessage(message);
 	builder.setCancelable(true);
 	builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
 
 	    @Override
 	    public void onClick(DialogInterface dialog, int which) {
 		SmashSmashAndroid.this.finish();
 	    }
 	});
 	builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
 
 	    @Override
 	    public void onClick(DialogInterface dialog, int which) {
 		// TODO Auto-generated method stub
 
 	    }
 	});
 	AlertDialog dialog = builder.create();
 	dialog.show();
 	return super.onCreateDialog(id);
     }
 }
