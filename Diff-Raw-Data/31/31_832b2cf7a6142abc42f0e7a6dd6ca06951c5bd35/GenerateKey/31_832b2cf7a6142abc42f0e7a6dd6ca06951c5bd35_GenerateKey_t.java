 package com.qrpasswds;
 
 import java.io.IOException;
 import java.security.NoSuchAlgorithmException;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.widget.Toast;
 
 import com.aes.AESRandomKey;
 
 public class GenerateKey extends FragmentActivity {
 	
 	protected void onCreate(Bundle savedInstanceState){
 		
 		super.onCreate(savedInstanceState);
 		
 		AESRandomKey ran_key = new AESRandomKey(this);
 		
 		try {
 			ran_key.generateKey();
 			Toast.makeText(this, R.string.key_file_created, Toast.LENGTH_LONG).show();
 			
 		} catch (NoSuchAlgorithmException e) {
 			
 		} catch (IOException e) {
 			
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 	        	builder.setTitle(R.string.error)
	        	   .setIcon(R.drawable.ic_alerts_and_states_error)
 	        	   .setMessage(R.string.error_creating_file)
 	               .setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
 	                   public void onClick(DialogInterface dialog, int id) {
 	                      
 	                   }
 	               })
 	               .show();
 		}
 		finish();
 		
         
     }
 
 }
