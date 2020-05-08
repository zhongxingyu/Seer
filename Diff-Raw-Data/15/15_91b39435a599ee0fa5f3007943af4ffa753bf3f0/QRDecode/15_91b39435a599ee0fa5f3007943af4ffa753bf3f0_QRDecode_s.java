  package com.shining.qrcode;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import jp.sourceforge.qrcode.QRCodeDecoder;
 
 
 import android.app.Activity;
 import android.content.Intent;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.ImageFormat;
 
 import android.graphics.PixelFormat;
 import android.graphics.Rect;
 import android.graphics.YuvImage;
 import android.hardware.Camera;
 import android.hardware.Camera.CameraInfo;
 import android.hardware.Camera.Parameters;
 import android.hardware.Camera.Size;
 import android.media.MediaPlayer;
 import android.net.Uri;
 import android.os.Build;
 import android.os.Bundle;
 import android.provider.MediaStore;
 import android.util.DisplayMetrics;
 
 import android.view.Surface;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.view.View;
 
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 
 import android.widget.Toast;
 
 public class QRDecode extends Activity /* implements SurfaceHolder.Callback*/{
 	
 	//private Timer mTimer;  
 	//private MyTimerTask mTimerTask; 
 	//private ImageView imgView;  
 	private View centerView;  
 	private Camera mCamera;
 	private Button decode,flashlight;
 	private ImageButton decodePicture;
 	private MediaPlayer mMediaPlayer;
 	private SurfaceView mSurfaceView;
 	private SurfaceHolder mSurfaceHolder;
 	
 	private Size mPreviewSize;
 	private List<Size> mSupportedPreviewSizes;
 	private String focusMode;
 	private String strQR;
 	
 	private boolean flEnable=false;
 	private  int dstLeft, dstTop,dstRight,dstBottom;   
 	   
 	public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         setContentView(R.layout.acitvity_qrdecode);
         
         DisplayMetrics dm=new DisplayMetrics();
         getWindowManager().getDefaultDisplay().getMetrics(dm);
         
      
 
        //imgView = (ImageView) this.findViewById(R.id.ImageView01);  
 	        
     //   centerView = (View) this.findViewById(R.id.centerView); 
        
   //     mSurfaceView=(SurfaceView)findViewById(R.id.mSurfaceView);
         
   //    mSurfaceHolder=mSurfaceView.getHolder();
     //   mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
     //   mSurfaceHolder.addCallback(this);
     
         
     //	mSurfaceHolder.setFixedSize(960, 540);
        
     //   flashlight=(Button)findViewById(R.id.flashlight);
         
         /*
        
        flashlight.setOnClickListener(new Button.OnClickListener(){
        	
        	public void onClick(View view){
        		
        		
        		flEnable=(!flEnable);
   
        		Camera.Parameters parameters =  mCamera.getParameters();
 
 		    if(flEnable)
        			parameters.setFlashMode(Parameters.FLASH_MODE_TORCH);
        		else
        			parameters.setFlashMode(Parameters.FLASH_MODE_OFF);
 		    
 		    mCamera.setParameters(parameters);
        		
        	}
        });
        
        
        */
         
         decode=(Button)findViewById(R.id.decode);
         
         decode.setOnClickListener(new Button.OnClickListener(){
         	
         	public void onClick(View view){
         		
         		Toast.makeText(QRDecode.this, "ʼ", Toast.LENGTH_LONG).show();
         		
         		/*
         		mTimer = new Timer();  
     	        mTimerTask = new MyTimerTask();  
     	        mTimer.schedule(mTimerTask, 0, 8000); 
     	        
     	        */
         		
         		 Intent intent = new Intent("com.google.zxing.client.android.SCAN");
         	     intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
         	     startActivityForResult(intent, 1);
         		
         		
         	}
         });
         
         
 		decodePicture=(ImageButton)findViewById(R.id.decodep);
 		
 		decodePicture.setOnClickListener(new Button.OnClickListener(){
 	     	
 	     	public void onClick(View view){
 	     			decodepicture();
 	     		}
 			});
 	}
 	
 	void decodepicture(){
 		Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI); 
 		startActivityForResult(intent, 0);
 	}
 	
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {  
         super.onActivityResult(requestCode, resultCode, data);  
         
         if(requestCode==0){
         
        if (null != data) {  
             Uri selectedImage = data.getData();  
             String[] filePathColumn = { MediaStore.Images.Media.DATA };  
       
             Cursor cursor = getContentResolver().query(selectedImage,  
                     filePathColumn, null, null, null);  
             cursor.moveToFirst();  
       
             int columnIndex = cursor.getColumnIndex(filePathColumn[0]);  
             String picturePath = cursor.getString(columnIndex);  
             cursor.close();  
             
             Bitmap bm=BitmapFactory.decodeFile(picturePath);
             
             if(strQR!="")
             	strQR="";
             
             strQR=decodeQRImage(bm);
           
             
             if(strQR=="")
             	Toast.makeText(QRDecode.this, "δݣ", Toast.LENGTH_LONG).show();
             else
             {
             	
             	Bundle bundle=new Bundle();
             	bundle.putString("qrcode", strQR);
             	Intent intent = new Intent(QRDecode.this,DecodeResult.class);
             	intent.putExtras(bundle);
           
             	startActivityForResult(intent,0);
             }
            	
          
        }  
        }
         
         else if(requestCode==1){
         	
         	strQR=data.getStringExtra("SCAN_RESULT");
         	
         	Bundle bundle=new Bundle();
             bundle.putString("qrcode", strQR);
             Intent intent = new Intent(QRDecode.this,DecodeResult.class);
             intent.putExtras(bundle);
             startActivity(intent);
         }
 	}
 	
 	
 	public String decodeQRImage(Bitmap mBmp){
 		
 		String strDecodedData="";
 		try{
 			QRCodeDecoder decoder =new QRCodeDecoder();
 			strDecodedData =new String(decoder.decode(new AndroidQRCodeImage(mBmp)));
 		}
 		catch(Exception e){
 			e.printStackTrace();
 		}
 		return strDecodedData;
 	}
 	
 	/*
 	
 	 class MyTimerTask extends TimerTask {  
 	       
 	        public void run() {  
 	       
 	        	if (dstLeft == 0) {
 	            	
 	            	dstLeft = centerView.getLeft();
 		            dstTop = centerView.getTop();
 		            dstRight = centerView.getRight();  
 		            dstBottom = centerView.getBottom();
 		         }  
 	            AutoFocusAndPreviewCallback();
 	        }  
 	    }   
 	 
 	 
 	 
 	 public void AutoFocusAndPreviewCallback()  {  
 		 
 		 if(mCamera!=null && (focusMode.equals(Camera.Parameters.FOCUS_MODE_MACRO)||focusMode.equals(Camera.Parameters.FOCUS_MODE_AUTO))) {
 	    	   
 			 	mCamera.autoFocus(mAutoFocusCallBack);  
 	        }
 	  	}  
 	 
 	 private Camera.AutoFocusCallback mAutoFocusCallBack = new Camera.AutoFocusCallback() {    
          
 	     
 	       public void onAutoFocus(boolean success, Camera camera) {        
 	           if (success) {  
 	               mCamera.setOneShotPreviewCallback(previewCallback);   
 	           }    
 	       }    
 	   };  
 				
 
 	
 	private Camera.PreviewCallback  previewCallback = new Camera.PreviewCallback() {
 		
 		public void onPreviewFrame(byte[] data, Camera camera) {
 		
 		
 			
 			Size previewSize = camera.getParameters().getPreviewSize(); 
 			
 			YuvImage yuvimage=new YuvImage(data, ImageFormat.NV21, previewSize.width, previewSize.height, null);
 			ByteArrayOutputStream baos = new ByteArrayOutputStream();
 			//yuvimage.compressToJpeg(new Rect(0, 0, previewSize.width, previewSize.height), 80, baos);
 			
 			yuvimage.compressToJpeg(new Rect(dstTop+50,dstLeft,  dstBottom+150, dstRight), 80, baos);
 			
 			//yuvimage.compressToJpeg(new Rect(280,70,  680, 470), 80, baos);
 			
 			byte[] jdata = baos.toByteArray();
 			
 		    Bitmap bm = BitmapFactory.decodeByteArray(jdata, 0, jdata.length);
 			
 		//	imgView.setImageBitmap(bm);  
 			
 			if(strQR!="")
 				strQR="";
 			
 			strQR=decodeQRImage(bm);
 			
 			
 			if(strQR==null)
 			{
 	            mMakeTextToast("δݣ",true);
 			}
 			else
 	        {
 			
 				mMediaPlayer = MediaPlayer.create(QRDecode.this, R.raw.prompt);  
 				mMediaPlayer.start();  
 				
 				try {
 					Thread.sleep(1000);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 				
 				mMediaPlayer.stop(); 
 				mCamera.cancelAutoFocus();  
 			
 				Bundle bundle=new Bundle();
 	            bundle.putString("qrcode", strQR);
 	            Intent intent = new Intent(QRDecode.this,DecodeResult.class);
 	            intent.putExtras(bundle);
 	            startActivity(intent);
 	           
 	        }
 	        
 			
 		}
 	};
 	
 
 	
 	
 	public void decodeYUV(int[] out, byte[] fg, int width, int height)
 	        throws NullPointerException, IllegalArgumentException {
 	    int sz = width * height;
 	    if (out == null)
 	        throw new NullPointerException("buffer out is null");
 	    if (out.length < sz)
 	        throw new IllegalArgumentException("buffer out size " + out.length
 	                + " < minimum " + sz);
 	    if (fg == null)
 	        throw new NullPointerException("buffer 'fg' is null");
 	    if (fg.length < sz)
 	        throw new IllegalArgumentException("buffer fg size " + fg.length
 	                + " < minimum " + sz * 3 / 2);
 	    int i, j;
 	    int Y, Cr = 0, Cb = 0;
 	    for (j = 0; j < height; j++) {
 	        int pixPtr = j * width;
 	        final int jDiv2 = j >> 1;
 	        for (i = 0; i < width; i++) {
 	            Y = fg[pixPtr];
 	            if (Y < 0)
 	                Y += 255;
 	            if ((i & 0x1) != 1) {
 	                final int cOff = sz + jDiv2 * width + (i >> 1) * 2;
 	                Cb = fg[cOff];
 	                if (Cb < 0)
 	                    Cb += 127;
 	                else
 	                    Cb -= 128;
 	                Cr = fg[cOff + 1];
 	                if (Cr < 0)
 	                    Cr += 127;
 	                else
 	                    Cr -= 128;
 	            }
 	            int R = Y + Cr + (Cr >> 2) + (Cr >> 3) + (Cr >> 5);
 	            if (R < 0)
 	                R = 0;
 	            else if (R > 255)
 	                R = 255;
 	            int G = Y - (Cb >> 2) + (Cb >> 4) + (Cb >> 5) - (Cr >> 1)
 	                    + (Cr >> 3) + (Cr >> 4) + (Cr >> 5);
 	            if (G < 0)
 	                G = 0;
 	            else if (G > 255)
 	                G = 255;
 	            int B = Y + Cb + (Cb >> 1) + (Cb >> 2) + (Cb >> 6);
 	            if (B < 0)
 	                B = 0;
 	            else if (B > 255)
 	                B = 255;
 	            out[pixPtr++] = 0xff000000 + (B << 16) + (G << 8) + R;
 	        }
 	    }
 
 	}
 	
 	
 	public static boolean eregi(String strPat,String strUnknow){
 		
 		String strPattern="(?i)"+strPat;
 		
 		Pattern p=Pattern.compile(strPattern);
 		Matcher m=p.matcher(strUnknow);
 		return m.find();
 	}
 	
 	
 	
 	
 	public void mMakeTextToast(String str,boolean isLong){
 		if(isLong==true){
 			Toast.makeText(QRDecode.this, str, Toast.LENGTH_LONG).show();
 		}
 		else
 		{
 			Toast.makeText(QRDecode.this, str, Toast.LENGTH_SHORT).show();
 		}
 	}
 	
 
 	
 
 	
 	
 	public void surfaceCreated(SurfaceHolder surfaceholder){
 		
 		CameraInfo cameraInfo = new CameraInfo();
 		
 		if(Build.VERSION.SDK_INT>=9){
 			
 			int numberOfCameras = Camera.getNumberOfCameras();
 			
 			int	defaultCameraId=0;
 			
 			for (int i = 0; i < numberOfCameras; i++) {
 	              
 	        	Camera.getCameraInfo(i, cameraInfo);
 	                
 	        	if (cameraInfo.facing == CameraInfo.CAMERA_FACING_BACK) {
 	                    
 	        		defaultCameraId = i;
 	            }
 	        }
 	        mCamera=Camera.open(defaultCameraId);
 	    }
 		else
 			mCamera = Camera.open();
 		
 		try{
 			
 			
 			
 			 int rotation = this.getWindowManager().getDefaultDisplay().getRotation();
 			 int degrees = 0;
 			 
 			 switch (rotation) {
 		         
 			 	case Surface.ROTATION_0: degrees = 0; break;
 		 	    case Surface.ROTATION_90: degrees = 90; break;
 		        case Surface.ROTATION_180: degrees = 180; break;
 		        case Surface.ROTATION_270: degrees = 270; break;
 		     
 			 }			  
 			 
 			 int result;
 			 
 			 if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
 		         result = (cameraInfo.orientation + degrees) % 360;
 		         result = (360 - result) % 360;  
 		     } else {  
 		         result = (cameraInfo.orientation - degrees + 360) % 360;
 		     }
 						  
 			 mCamera.setDisplayOrientation(result);
 			 mCamera.setPreviewDisplay(surfaceholder);
 		}catch(IOException e){
 			mCamera.setPreviewCallback(null) ;
 			mCamera.setOneShotPreviewCallback(null);
 			mCamera.release();
 			e.printStackTrace();
 		}
 	}
 	
 	public void surfaceChanged(SurfaceHolder surfaceholder,int format,int width,int height){
 
 		if (mCamera != null)
 		{
 		  
 			Camera.Parameters parameters =  mCamera.getParameters();
 			
 		    focusMode=parameters.getFocusMode();
 		      
 		    parameters.setPictureFormat(PixelFormat.JPEG);
 		
 		    List<Size> sizes = parameters.getSupportedPictureSizes();
 		    Size size = sizes.get(0);
 		    
 		    parameters.setPictureSize(size.width, size.height);
 		    
 		    mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
 		    
 		    if (mSupportedPreviewSizes != null) {
 	            mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height);
 	        }
 		    
 		    parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
 		    
 		    mCamera.setParameters(parameters);
 		    
 		    try
 		    {
 		    	mCamera.startPreview();
 		    }
 		    catch(Exception e)
 		    {
 		        e.printStackTrace();
 		        mCamera.setPreviewCallback(null) ;
 		        mCamera.release();
 	
 		    }
 		
 		}
 	}
 	
 		private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
 	        final double ASPECT_TOLERANCE = 0.1;
 	        double targetRatio = (double) w / h;
 	        if (sizes == null) return null;
 
 	        Size optimalSize = null;
 	        double minDiff = Double.MAX_VALUE;
 
 	        int targetHeight = h;
 
 	       
 	        for (Size size : sizes) {
 	            double ratio = (double) size.width / size.height;
 	            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
 	            if (Math.abs(size.height - targetHeight) < minDiff) {
 	                optimalSize = size;
 	                minDiff = Math.abs(size.height - targetHeight);
 	            }
 	        }
 
 	       
 	        if (optimalSize == null) {
 	            minDiff = Double.MAX_VALUE;
 	            for (Size size : sizes) {
 	                if (Math.abs(size.height - targetHeight) < minDiff) {
 	                    optimalSize = size;
 	                    minDiff = Math.abs(size.height - targetHeight);
 	                }
 	            }
 	        }
 	        return optimalSize;
 	    }
 	
 	
 	public void surfaceDestroyed(SurfaceHolder surfaceholder){
 		
 		if (mMediaPlayer != null) {  
 	            mMediaPlayer.release();  
 	            mMediaPlayer = null;  
 	        }  
 		
 		mCamera.cancelAutoFocus();  
 		mCamera.setPreviewCallback(null) ;
 		mCamera.setOneShotPreviewCallback(null);
 		mCamera.stopPreview();
 		mCamera.release();
 		mCamera=null;
 	}
 	
 	*/
 }
