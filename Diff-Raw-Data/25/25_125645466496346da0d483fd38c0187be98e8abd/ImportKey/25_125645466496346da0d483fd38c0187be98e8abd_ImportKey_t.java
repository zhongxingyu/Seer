 package com.qrpasswds;
 
 import java.io.IOException;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.xml.sax.SAXException;
 
 import com.aes.AESRandomKey;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.ActivityNotFoundException;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.widget.Toast;
 
 public class ImportKey extends Activity {
 
 	private final int FIND_FILE = 1;
	private boolean error = false;
 	
 	protected void onCreate(Bundle savedInstanceState){
 		super.onCreate(savedInstanceState);
 		
 		Intent intent = new Intent();
         intent.setType("file/*");
         intent.setAction(Intent.ACTION_GET_CONTENT);
         try {
         	startActivityForResult(intent, FIND_FILE);
         }
         catch(ActivityNotFoundException e){
         
        	error = true;
        	
         	AlertDialog.Builder builder = new AlertDialog.Builder(this);
 	        	builder.setTitle(R.string.error)
 	        	   .setIcon(R.drawable.ic_alerts_and_states_error)
 	        	   .setMessage(R.string.file_manager_not_found)
 	               .setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
 	                   public void onClick(DialogInterface dialog, int id) {
 	                      finish();
 	                   }
 	               })
	               .setOnCancelListener(new DialogInterface.OnCancelListener() {         
            	   		@Override
            	   		public void onCancel(DialogInterface dialog) {
            	   			finish();
            	   	}})
 	               .show();
         	
         }
 	}
 	
 	public void onActivityResult(int requestCode, int resultCode, Intent result){
		System.out.println("Result");
 		if ( requestCode == FIND_FILE && resultCode == RESULT_OK ){
 			
 			AESRandomKey ranKey = new AESRandomKey(this);
 			String resultFile = result.getData().getPath();
 			
 			try{
 				
 				boolean flag = ranKey.validateKey(resultFile);
 				
 				if (flag) {
 					ranKey.importKey(resultFile, this);
 					Toast.makeText(this, R.string.key_imported, Toast.LENGTH_LONG).show();
 					finish();
 				}
 				else {
 
 					AlertDialog.Builder builder = new AlertDialog.Builder(this);
        	        	builder.setTitle(R.string.error)
        	        	   .setIcon(R.drawable.ic_alerts_and_states_error)
        	        	   .setMessage(R.string.not_valid_file)
        	               .setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
        	                   public void onClick(DialogInterface dialog, int id) {
        	                	finish();  
        	                   }
        	               })
        	               .show();
 				}
 				
 			} catch(IOException e){ 
 
 				AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	        	builder.setTitle(R.string.error)
    	        	   .setIcon(R.drawable.ic_alerts_and_states_error)
    	        	   .setMessage(R.string.error_reading_file)
    	               .setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
    	                   public void onClick(DialogInterface dialog, int id) {
    	                	finish();
    	                   }
    	               })
    	               .show();
    	        	
 			} catch (SAXException e) {
 				
 				AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	        	builder.setTitle(R.string.error)
    	        	   .setIcon(R.drawable.ic_alerts_and_states_error)
    	        	   .setMessage(R.string.not_valid_file)
    	               .setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
    	                   public void onClick(DialogInterface dialog, int id) {
    	                	finish();
    	                   }
    	               })
    	               .show();
    	        	
 			} catch (ParserConfigurationException e) {
 
 				AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	        	builder.setTitle(R.string.error)
    	        	   .setIcon(R.drawable.ic_alerts_and_states_error)
    	        	   .setMessage(R.string.not_valid_file)
    	               .setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
    	                   public void onClick(DialogInterface dialog, int id) {
    	                	finish();
    	                   }
    	               })
    	               .show();
 
 			}
 		}
		else if ( requestCode == FIND_FILE && resultCode != RESULT_OK && !error){
 			finish();
 		}
 		
 	}
 }
