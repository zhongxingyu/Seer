 package com.example.ydp;
 
 
 import java.io.UnsupportedEncodingException;
 
 import org.apache.commons.codec.binary.Base64;
 
 import android.app.Activity;
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
 
 /* Import ZBar Class files */
 import net.sourceforge.zbar.ImageScanner;
 import net.sourceforge.zbar.Image;
 import net.sourceforge.zbar.Symbol;
 import net.sourceforge.zbar.SymbolSet;
 import net.sourceforge.zbar.Config;
 
 
 
 public class YDPCAREPLAN extends Activity{
 	
 	protected static final byte[] NULL = null;
 	private Camera mCamera;
     private CameraPreview mPreview;
     private Handler autoFocusHandler;
     String scanData;
 
     String scanText;
     
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
        String[] scan = data.split(":");
	    username.setText(scan[8].trim());
		 password.setText(scan[9].trim());
 		
 	}
 	
 	private String decryptScanData(String data){
 		   	Cipher cipher = new Cipher("A7Q6DyH0LW9VF7G55TEyFw==");
 		   	byte[] encryptinput=null;
 		   	try {
 			
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
 
 
 				return new String(resultData);
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
 				
 //				String scanData = "FirstName:John:\nLastName:Smith:\nTel:9000292930:\nPatientID:78878:\nadikadapa:Medico!8:";
//				fillUserIdAndPasswordWithScanData(scanData);
                 // TODO Auto-generated method stub
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
                         scanText = "barcode result " + sym.getData();
                         barcodeScanned = true;
                     }
                     
                    //Toast.makeText(YDPCAREPLAN.this, scanText, Toast.LENGTH_SHORT).show();
                    
                      scanData = decryptScanData(scanText);
                     //scanData = "FirstName:John:LastName:Smith:Tel:9000292930:PatientID:78878:adikadapa:Medico!8:";
                     fillUserIdAndPasswordWithScanData(scanData);
                     
                     login.performClick();
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
 
