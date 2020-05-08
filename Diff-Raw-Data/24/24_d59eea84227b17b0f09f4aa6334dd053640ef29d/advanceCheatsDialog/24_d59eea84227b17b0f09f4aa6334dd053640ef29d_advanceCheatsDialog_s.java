 package com.omerjerk.cheatbox.utils;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 import android.widget.Toast;
 
 public class advanceCheatsDialog extends DialogFragment {
 	
 	 Context c;
 	 SharedPreferences settings;
	 final Boolean patched_templeRun2 = settings.getBoolean("PATCHED_TMEPLERUN2", false);
	 final Boolean patched_templeRun1 = settings.getBoolean("PATCHED_TMEPLERUN1", false);
	 final Boolean patched_templeRunOz = settings.getBoolean("PATCHED_TMEPLERUNOZ", false);
	 SharedPreferences.Editor editor = settings.edit();
 	 @Override
 	 public Dialog onCreateDialog(Bundle savedInstanceState) {
 		 //Extracting the arguments
 		 final int gameID = getArguments().getInt("GAME_ID");
 		 
 	     // Use the Builder class for convenient dialog construction
 	     AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
 	     c = getActivity(); // Context object to use at various places
	     settings = c.getSharedPreferences("SETTINGS_MAIN", 0);
 	     
 	     builder.setMessage("Advance Patching")
 	            .setPositiveButton("Patch", new DialogInterface.OnClickListener() {
 	                public void onClick(DialogInterface dialog, int id) {
 	                    // PATCH ZE GAME
 	                	patchTask(gameID);
	                	
	                	
 	                }
 	            })
 	            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 	                public void onClick(DialogInterface dialog, int id) {
 	                    // User cancelled the dialog
 	                }
 	            })
 	            .setNeutralButton("Restore", new DialogInterface.OnClickListener(){
 	            	public void onClick(DialogInterface dialog, int id){
 	            		restoreTask(gameID);
	                	SharedPreferences.Editor editor = settings.edit();
	                	editor.putBoolean("Restore", false);
	                	editor.commit();
 	            	}
 	            });
 	     // Create the AlertDialog object and return it
 	     return builder.create();
 	 }
 	 copy cp; boolean result = true;
 	 private void patchTask(int i){
 		 switch(i){
 		 case 1:
 			 if(patched_templeRun2 == false){
 				 cp = new copy("/Android/data/com.imangi.templerun2/files", "gamedata.txt", 1, c); 
 		    	 result = cp.task();
 		    	 	if (result == true){
 		    	 		editor.putBoolean("PATCHED_TMEPLERUN2", true);
 	                	editor.commit();
 		    			Toast.makeText(c, "Patched ...   :D", Toast.LENGTH_SHORT).show();
 		    		} else {
 		    			Toast.makeText(c, "Shit Happened!\nMake sure your temple run is installed.", Toast.LENGTH_SHORT).show();
 		    		}  
 			 } else {
 				 Toast.makeText(c, "You Temple Run 2 is already patched.", Toast.LENGTH_SHORT);
 			 }
 			 break;
 		 case 2:
 			 if(patched_templeRunOz == false){
 				 cp = new copy("/Android/data/com.disney.TempleRunOz.goo/files", "android_settings.oz", 2, c);
 				 result = cp.task();
 				 copy cp1 = new copy("/Android/data/com.disney.TempleRunOz.goo/files", "stats.sav", 3, c);
 				 boolean result2 = cp1.task();
 				 if(result == false || result2 == false){
 				 	Toast.makeText(getActivity(), "Shit Happened!\nMake sure your Temple Run Oz is installed.", Toast.LENGTH_SHORT).show();
 				 } else {
 					 editor.putBoolean("PATCHED_TMEPLERUNOZ", true);
 	                 editor.commit();
 					 Toast.makeText(getActivity(), "Patched :D", Toast.LENGTH_SHORT).show();
 				 }
 			 } else {
 				 Toast.makeText(c, "You Temple Run Oz is already patched.", Toast.LENGTH_SHORT);
 			 }
 			 break;
 		 case 3:
 			 if(patched_templeRun1 == false){
 				 cp = new copy("/Android/data/com.imangi.templerun/files", "recordmanager.dat", 4, c);
 				 result = cp.task();
 				 if (result == true){
 					 editor.putBoolean("PATCHED_TMEPLERUN1", true);
 	                 editor.commit();
 		    		 Toast.makeText(getActivity(), "Patched ...   :D", Toast.LENGTH_SHORT).show();
 		    	 } else {
 		    		 showToast("Shit Happened!\nMake sure your temple run is installed.");
 		    	 }
 			 } else {
 				 Toast.makeText(c, "Your Temple Run 1 is already Patched.", Toast.LENGTH_LONG).show();
 			 }
 			 break;
 		 }
 	 }	 
 	 
 	 private void restoreTask(int i){
 		 switch(i){
 		 case 1:
 			 if(patched_templeRun2 == true){
 				 cp = new copy("/Android/data/com.imangi.templerun2/files", "gamedata.txt", 5, c);
 				 result = cp.task();
 				 if(result == false){
 					 showToast("Unable to restore you previous saves.");
 				 } else {
 					 editor.putBoolean("PATCHED_TMEPLERUN2", false);
 	                 editor.commit();
 					 showToast("Restore Successful.");
 				 }
 			 } else {
 				 showToast("Please patch before restoring.");
 			 }			 
 			 break;
 		 case 2:
 			 if(patched_templeRunOz == true){
 				 cp = new copy("/Android/data/com.disney.TempleRunOz.goo/files", "android_settings.oz", 5, c);
 				 result = cp.task();
 				 copy cp1 = new copy("/Android/data/com.disney.TempleRunOz.goo/files", "stats.sav", 5, c);
 				 boolean result1 = cp1.task();
 				 if(result == false || result1 == false){
 					 showToast("Unable to restore you previous saves.");
 				 } else {
 					 editor.putBoolean("PATCHED_TMEPLERUNOZ", false);
 	                 editor.commit();
 					 showToast("Restore Successful.");
 				 }
 			 } else {
 				 showToast("Please patch before restoring.");
 			 }
 			 break;
 		 case 3:
 			 if(patched_templeRun1 == true){
 				 cp = new copy("/Android/data/com.imangi.templerun/files", "recordmanager.dat", 5, c);
 				 result = cp.task();
 				 if(result == false){
 					 showToast("Unable to restore you previous saves.");
 				 } else {
 					 editor.putBoolean("PATCHED_TMEPLERUN1", false);
 	                 editor.commit();
 					 showToast("Restore Successful.");
 				 }
 			 } else {
 				 showToast("Please patch before restoring.");
 			 }
 			 break;
 		 }
 	 }
 	 
 	 private void showToast(String toast){
 		 Toast.makeText(c, toast, Toast.LENGTH_SHORT).show();
 	 }
 }
