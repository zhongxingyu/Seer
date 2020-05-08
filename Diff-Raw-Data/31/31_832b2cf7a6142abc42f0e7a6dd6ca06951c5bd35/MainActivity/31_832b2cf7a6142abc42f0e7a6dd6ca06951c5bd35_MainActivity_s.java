 package com.qrpasswds;
 
 import java.io.File;
 
 import android.app.AlertDialog;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.support.v4.app.FragmentActivity;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.LinearLayout;
 import android.widget.Toast;
 
 import com.aes.AESEncryption;
 import com.google.zxing.integration.android.IntentIntegrator;
 import com.google.zxing.integration.android.IntentResult;
 
 public class MainActivity extends FragmentActivity {
 	
 	private final String FILENAME = "QRPass.key";
 	private final String ACTION_RESP = "com.QRPasswds.MESSAGE_PROCESSED";
 	
 	private CredentialsFragment scroll = null;
 	private View scrollView = null;
 	private LinearLayout main = null;
 	private LinearLayout loadingView = null;
 	private boolean isLoading;
 	
 	private EncryptEncodeReceiver receiver = null;
 	private IntentFilter filter = null;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		scroll = (CredentialsFragment) getSupportFragmentManager().findFragmentById(R.id.scroll_fragment);
 		scrollView = findViewById(R.id.scroll_view);
 		loadingView = (LinearLayout) findViewById(R.id.loading);
 				
 		if (savedInstanceState!=null && savedInstanceState.getBoolean("isLoading")){
 			loading(true);
 		}
 		
 		filter = new IntentFilter(ACTION_RESP);
 		filter.setPriority(1);
 		receiver = new EncryptEncodeReceiver();
 		
 	}
 	
 	public void onResume(){
 		super.onResume();
 
 		File keyfile = this.getFileStreamPath(FILENAME);
 		
 		if (!keyfile.exists()){
 			
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 	        builder.setTitle(R.string.key_file_missing_title)
 	        	   .setMessage(R.string.key_file_missing_message)
 	               .setPositiveButton(R.string.generate, new DialogInterface.OnClickListener() {
 	                   public void onClick(DialogInterface dialog, int id) {
 	                       Intent intent = new Intent(MainActivity.this,GenerateKey.class);
 	                       startActivity(intent);
 	                   }
 	               })
 	               .setNegativeButton(R.string.import_dialog, new DialogInterface.OnClickListener() {
 	                   public void onClick(DialogInterface dialog, int id) {
 	                       Intent intent = new Intent(MainActivity.this,ImportKey.class);
 	                       startActivity(intent);
 	                   }
 	               })
 	               .setCancelable(false)
 	               .show();
 		}
 		
 		
 		registerReceiver(receiver,filter);
 		
 	}
 	
 	public void onPause(){
 		super.onPause();
 		unregisterReceiver(receiver);
 	}
 	
 	@Override
 	 public void onSaveInstanceState(Bundle outState) {
 	        super.onSaveInstanceState(outState);
 	        outState.putBoolean("isLoading", isLoading);
 	    }
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 	
 	@Override
     public boolean onOptionsItemSelected(MenuItem item) {
         
 		if (!isLoading){
 		
 			switch (item.getItemId()) {
             	case R.id.scan:
             		IntentIntegrator integrator = new IntentIntegrator(this);
             		integrator.initiateScan();
             		break;
             	case R.id.actionbar_create_qr:
             		createPressed(new View(this));
             		break;
             	case R.id.clear:
             		if (scroll.getIdCounter()>0){
             			AlertDialog.Builder builder = new AlertDialog.Builder(this);
             			builder.setTitle(R.string.clear_dialog_title)
     	        	   		.setMessage(R.string.clear_message)
     	        	   		.setPositiveButton(R.string.clear, new DialogInterface.OnClickListener() {
     	        	   			public void onClick(DialogInterface dialog, int id) {
     	        	   				scroll.setIdCounter(0);
     	        	   				main.removeAllViews();
     	        	   			}
     	        	   		})
     	        	   		.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
     	        	   			public void onClick(DialogInterface dialog, int id) {
     	                    
     	        	   			}
     	        	   		})
     	        	   		.show();
             		}
             		break;
             	case R.id.import_key:
             		Intent importKey = new Intent(this, ImportKey.class);
                 	this.startActivity(importKey);
                 	break;
             	case R.id.export_key:
             		Intent exportKey = new Intent(this, ExportKey.class);
             		this.startActivity(exportKey);
             		break;
             	default:
             		return super.onOptionsItemSelected(item);
 			}
         }
 		
         return true;
     }
 	
 	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
 		  IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
 		  if (scanResult != null) {
 			  scroll.setIdCounter(0);
 			  AESEncryption aes = new AESEncryption(this);
 			  loading(true);
 			  
 			  String[] retainedData = null;
 			
 			  try {
 				  
 				retainedData = aes.aes_decrypt(scanResult.getContents()).split("\n");
 				for (int f=0;f<retainedData.length;f+=3) scroll.addCredential(retainedData[f],retainedData[f+1],retainedData[f+2]);
 				
 			  } catch (Exception e) {
 				  
 				  AlertDialog.Builder builder = new AlertDialog.Builder(this);
 	   	        	builder.setTitle(R.string.error)
 	   	        	   .setMessage(R.string.error_scanning)
 	   	               .setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
 	   	                   public void onClick(DialogInterface dialog, int id) {
 	   	                      
 	   	                   }
 	   	               })
 	   	               .show();
 				  
 			  } 
 			
 			  loading(false);
 		   
 		  }
 	}
 	
 	public void createPressed(View v){
 		
 		if (!isLoading) {
 		
 			if (scroll.getIdCounter() > 0)	{
 			
 				loading(true);
 			
 				Intent toReceiver = new Intent(this,EncryptEncode.class);
 				toReceiver.putExtra("Data", scroll.getInput());
 				startService(toReceiver);
 			
 			}
 			else {
 				
 				AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	        	builder.setTitle(R.string.error)
    	        	   .setMessage(R.string.no_credential_error)
    	               .setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
    	                   public void onClick(DialogInterface dialog, int id) {
    	                      
    	                   }
    	               })
    	               .show();
 			}
 		}
 	}
 	
 	public void scrollToBottom(){
 
 		if(main!=null){
 			scrollView.post(new Runnable() {            
             @Override
             public void run() {
             	scrollView.scrollTo(0, main.getBottom());
             }
         });
 		} 
 	}
 	
 	public void encryptionEncodingFinished(boolean success){
 				
 		if (success){		
 			Toast.makeText(this, R.string.qr_created, Toast.LENGTH_SHORT).show();
 			sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://"+ Environment.getExternalStorageDirectory())));
 		}
 		else {
 
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 	        	builder.setTitle(R.string.error)
 	        	   .setMessage(R.string.error_creating_file)
 	               .setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
 	                   public void onClick(DialogInterface dialog, int id) {
 	                      
 	                   }
 	               })
 	               .show();
 		}
 	}
 	
 	public void setMain(LinearLayout in){
 		main = in;
 	}
 	
 	public void loading(boolean flag){
 		
 		if (flag) {
 			scrollView.setVisibility(View.INVISIBLE);
 			loadingView.setVisibility(View.VISIBLE);
 			isLoading = true;
 		}
 		else {
 			scrollView.setVisibility(View.VISIBLE);
 			loadingView.setVisibility(View.INVISIBLE);
 			isLoading = false;
 		}
 	}
 	
 	private class EncryptEncodeReceiver extends BroadcastReceiver{
 
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			boolean executed = intent.getBooleanExtra("Executed", false);
 			if (executed) {
 				loading(false);
 				encryptionEncodingFinished(intent.getBooleanExtra("Success", false));
 			}
 			abortBroadcast();
 			
 		}
 		
 	}
 }
