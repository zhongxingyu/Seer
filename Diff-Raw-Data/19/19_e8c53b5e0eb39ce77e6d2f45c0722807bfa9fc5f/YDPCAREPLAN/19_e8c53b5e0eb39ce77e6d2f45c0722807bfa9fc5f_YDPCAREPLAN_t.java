 package com.alakinfotech.ydp;
 
 
 import java.io.UnsupportedEncodingException;
 
 import org.apache.commons.codec.binary.Base64;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.hardware.Camera;
 import android.hardware.Camera.AutoFocusCallback;
 import android.hardware.Camera.PreviewCallback;
 import android.hardware.Camera.Size;
 import android.os.Bundle;
 import android.os.Handler;
 
import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.FrameLayout;
import android.widget.Toast;

 import com.alakinfotech.ydp.R;
 
 
 
 
 /* Import ZBar Class files */
 import net.sourceforge.zbar.ImageScanner;
 import net.sourceforge.zbar.Image;
 import net.sourceforge.zbar.Symbol;
 import net.sourceforge.zbar.SymbolSet;
 import net.sourceforge.zbar.Config;
 
 
 
 
 
 
 
 public class YDPCAREPLAN extends Activity{
 	public static final boolean YDP_WEBVIEW_LOGIN =false;
 	protected static final byte[] NULL = null;
 	private Camera mCamera;
     private CameraPreview mPreview;
     private Handler autoFocusHandler;
     Context context= this;
 
     
     ImageScanner scanner;
 
     private boolean barcodeScanned = false;
     private boolean previewing = true;
 
     int classCreateStatus;
     static {
         System.loadLibrary("iconv");
     } 
     
 	EditText username;
 	EditText password;
 	Button login;
 	public String uname,pword;
 	//Boolean false,true;
 	private void fillUserIdAndPasswordWithScanData(String data){
		Log.d(data, "Scanned data");
 	 String[] scan = data.split(":");
	 if(scan.length>=10){  
		 for(int i=1;(9+i)<scan.length;i++){
			 scan[9] = scan[9]+":"+scan[9+i];
		 }
		    username.setText(scan[8].trim());
			 password.setText(scan[9].trim()); 		 
	 }
	 else{
		 Toast.makeText(getApplicationContext(), "Invalid UserName and Password",3000).show();
	 }
 		
 	}
 	
 	private String decryptScanData(String data){
 		   	Cipher cipher = new Cipher("Adi&Revanth");
 		   	byte[] encryptinput=null;
 		   	try {
 			//scanText.trim();
 			byte[] dect=data.getBytes("UTF-8");
 			encryptinput = Base64.decodeBase64(dect);
 		   	} catch (UnsupportedEncodingException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		   	}
 	
 			byte[] reseltByts = null;
 
 			try {
 			reseltByts = cipher.decrypt(encryptinput);
 			} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			}
 			String resultData = null;
 
 
 			if(reseltByts != null){
 
 	
 	
 				try {
 			resultData = new String(reseltByts, "UTF-8");
 				} catch (UnsupportedEncodingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			}
 			}
 			if(resultData != null)
 			{
 
 			return new String(resultData);
 			}
 			else 
 				{ return null;
 				}
 				}
 	
 	
  	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
  			super.onCreate(savedInstanceState);
  			setContentView(R.layout.ydpcareplan);
 		
 		
 		
  			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
 		 
  			autoFocusHandler = new Handler();
 	        mCamera = getCameraInstance();
 
 	        /* Instance barcode scanner */
 	        scanner = new ImageScanner();
 	        scanner.setConfig(0, Config.X_DENSITY, 3);
 	        scanner.setConfig(0, Config.Y_DENSITY, 3);
 
 	        mPreview = new CameraPreview(this, mCamera, previewCb, autoFocusCB);
 	        FrameLayout preview = (FrameLayout)findViewById(R.id.cameraPreview);
 	        preview.addView(mPreview);
 
 	        
 	        username = (EditText) findViewById(R.id.username);
 			 password =(EditText) findViewById(R.id.password);
 			 
 			 login =(Button) findViewById(R.id.login);
 			 login.setOnClickListener(new OnClickListener() {
 			
 			@Override
 				public void onClick(View v) {
 				
 				if(username.length()==0||password.length()==0)
 				{
 					
 				
 					AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
 					alertDialogBuilder.setTitle("Warning");
 					alertDialogBuilder.setMessage("Enter Username and Password  to login");
 					alertDialogBuilder.setNegativeButton("ok",new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog,int id) {
 							// if this button is clicked, just close
 							// the dialog box and do nothing
 							dialog.cancel();
 						}
 					});
 					// create alert dialog
 					AlertDialog alertDialog = alertDialogBuilder.create();
 	 
 					// show it
 					alertDialog.show();
 				
 				}
 				else
 				{
 						if(YDP_WEBVIEW_LOGIN==true)
 						{
 						Intent innt =new Intent(getApplicationContext(),YDPWEBVIEW.class );
 						
 						uname = username.getText().toString();
 						pword = password.getText().toString();
 					   innt.putExtra("username",uname);
 					   innt.putExtra("password",pword);
 					   
 					   classCreateStatus = 10;
 					    	onPause();
 		
 					    	FrameLayout preview = (FrameLayout)findViewById(R.id.cameraPreview);
 					    	preview.removeView(mPreview);
 					    	
 					    	startActivity(innt);
 						}
 						else
 						{
 						Intent careplanaccess = new Intent(getApplicationContext(),UIFORCAREPLAN.class);
 						uname = username.getText().toString();
 						pword = password.getText().toString();
 					   careplanaccess.putExtra("username",uname);
 					   
 					   careplanaccess.putExtra("password",pword);
 					   startActivity(careplanaccess);
 					  // finis
 						try {
 							finalize();
 						} catch (Throwable e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 						}
 			}				
 			}
 		});
 		
 		
 	}
  	
 
  	@Override
     protected void onResume(){
  	
  			super.onResume();
  		
  		
  			if(classCreateStatus == 10)
  			{
  				autoFocusHandler = new Handler();
  				mCamera = getCameraInstance();
 
  				/* Instance barcode scanner */
  				scanner = new ImageScanner();
  				scanner.setConfig(0, Config.X_DENSITY, 3);
  				scanner.setConfig(0, Config.Y_DENSITY, 3);
 
  				mPreview = new CameraPreview(getApplicationContext(), mCamera, previewCb, autoFocusCB);
  				FrameLayout preview = (FrameLayout)findViewById(R.id.cameraPreview);
  				preview.addView(mPreview);
  			}
         
  	}
  
  	@Override
  	
  	public void onPause() {
  			super.onPause();
  			releaseCamera();
  			classCreateStatus = 10;
         	FrameLayout preview = (FrameLayout)findViewById(R.id.cameraPreview);
         	preview.removeView(mPreview);
       
     }
 
     /** A safe way to get an instance of the Camera object. */
     public static Camera getCameraInstance(){
     		Camera c = null;
     		try {
             c = Camera.open();
     		} catch (Exception e){
     		}
     		return c;
     }
 
     private void releaseCamera() {
     		if (mCamera != null) {
             previewing = false;
             mCamera.setPreviewCallback(null);
             mCamera.release();
             mCamera = null;
     		}
     }
 
     private Runnable doAutoFocus = new Runnable() {
             public void run() {
                 if (previewing)
                     mCamera.autoFocus(autoFocusCB);
             	}
         };
 
     PreviewCallback previewCb = new PreviewCallback() {
     	String scanText = null;
     			public void onPreviewFrame(byte[] data, Camera camera) {
     				
                 Camera.Parameters parameters = camera.getParameters();
                 Size size = parameters.getPreviewSize();
 
                 Image barcode = new Image(size.width, size.height, "Y800");
                 barcode.setData(data);
 
                 int result = scanner.scanImage(barcode);
                 
                 if (result != 0) {
                     previewing = false;
                     mCamera.setPreviewCallback(null);
                     mCamera.stopPreview();
                     
                     SymbolSet syms = scanner.getResults();
                     for (Symbol sym : syms) {
                         scanText = sym.getData();
                         barcodeScanned = true;
                     }
                   
                     
                     String scanData = decryptScanData(scanText);
                     if(scanData != null)
                     {
                      fillUserIdAndPasswordWithScanData(scanData);
                     
                      login.performClick();
                     }
                     else{
                     	   if (barcodeScanned) {
                                barcodeScanned = false;
                                mCamera.setPreviewCallback(previewCb);
                                mCamera.startPreview();
                                previewing = true;
                                mCamera.autoFocus(autoFocusCB);
                            }
                     }
                 }     
             }
         };
 
     // Mimic continuous auto-focusing
     AutoFocusCallback autoFocusCB = new AutoFocusCallback() {
             public void onAutoFocus(boolean success, Camera camera) {
                 autoFocusHandler.postDelayed(doAutoFocus, 1000);
             }
         	};
 
 }
 
