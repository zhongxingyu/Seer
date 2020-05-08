 package com.qrpasswds;
 
 import java.io.IOException;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.widget.Toast;
 
 import com.aes.AESRandomKey;
 
 public class ExportKey extends FragmentActivity {
 	
 	protected void onCreate(Bundle savedInstanceState){
 		
 		super.onCreate(savedInstanceState);
         
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setTitle(R.string.export_key_title)
         	   .setMessage(R.string.export_key_message)
                .setPositiveButton(R.string.export, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        
                 	   AESRandomKey ran_key = new AESRandomKey(ExportKey.this);
                        
                 	   try {
                 		   ran_key.exportKey();
                 		   Toast.makeText(ExportKey.this, R.string.key_exported, Toast.LENGTH_LONG).show();
                		   finish();
                 		   
                        } catch (IOException e) {
                     	                       	   
                     	   AlertDialog.Builder builder = new AlertDialog.Builder(ExportKey.this);
                	        	builder.setTitle(R.string.error)
                	        	   .setMessage(R.string.error_exporting_key)
                	               .setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
                	                   public void onClick(DialogInterface dialog, int id) {
               	                	   finish();
                	                   }
                	               })
                	               .show();
                     	   
                        }
                	   
                 	   
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
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
