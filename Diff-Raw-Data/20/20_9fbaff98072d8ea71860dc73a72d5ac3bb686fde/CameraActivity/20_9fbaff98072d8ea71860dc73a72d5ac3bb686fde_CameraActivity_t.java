 package com.example.cameracapture;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Matrix;
 import android.hardware.Camera;
 import android.hardware.Camera.CameraInfo;
 import android.hardware.Camera.Parameters;
 import android.hardware.Camera.Size;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.util.Log;
 import android.view.Display;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup.LayoutParams;
 import android.view.Window;
 import android.widget.Button;
 import android.widget.FrameLayout;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.support.v4.app.NavUtils;
 
 public class CameraActivity extends Activity {
 	
 	private boolean isOpen;
 	
 	private boolean isBack = true;
 	
 	private Camera mCamera; 
 
     private CameraPreview mPreview; 
     
     //private SurfaceView remoteView; 
     private ImageView remoteView;
     
     private FrameLayout preview;
     
     int height,width;
     
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
         
         setContentView(R.layout.activity_camera);
         
         preview= (FrameLayout) findViewById(R.id.camera_preview); 
         
         remoteView = (ImageView) findViewById(R.id.remote_preview);
         
         //remoteView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_GPU);
         
         Log.d("debug : ", "Create Activity");
         
         Display d = getWindowManager().getDefaultDisplay();
         height = d.getHeight();
         width = d.getWidth();
         
         height /= 2;
         width /= 2;
         
         Log.d("debug : ", "Width1 : " + width);
     	Log.d("debug : ", "Height1 : " + height);
         
         LayoutParams p1 = remoteView.getLayoutParams();
         p1.width = width;
         p1.height = height;
         remoteView.setLayoutParams(p1);
         
         LayoutParams p = preview.getLayoutParams();
         p.width = width;
         p.height = height;
         preview.setLayoutParams(p);
         
         Button b1 = (Button) findViewById(R.id.button_preview);
         b1.setOnClickListener(new OnClickListener(){
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				if(mCamera==null){
 					mCamera = getCamera(); 
 					if(mCamera!=null){
 				        // Preview viewΪactivityе			        
 				        mPreview = new CameraPreview(CameraActivity.this, mCamera);
 				        preview.addView(mPreview);
 				        isOpen = true;
 				        mCamera.setPreviewCallback(new PreviewFrameCallBack());
 				        
 				        Log.d("debug : ", "start camera ok!!!");
 					}
 				}
 				else if(isOpen==true){
 					
 					mCamera.stopPreview();
 					mCamera.setPreviewCallback(null);
 					mCamera.release();
 					mPreview.setCamera(null);
 					Log.d("debug : ", "close camera ok!!!");
 					isOpen = false;
 				}else{
 					mCamera = getCamera();
 					if(mCamera!=null){
 						mCamera.setPreviewCallback(new PreviewFrameCallBack());
 						mPreview.setCamera(mCamera);
 						Log.d("debug : ", "reconnect camera ok!!!");
 						isOpen = true;
 					}
 				}
 			}
         	
         });
         
         Button b2 = (Button) findViewById(R.id.button_toSecond);
         b2.setOnClickListener(new OnClickListener(){
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				Intent itent = new Intent(CameraActivity.this,SecondActivity.class);
 				startActivity(itent);
 			}
         	
         });
         
         Button b3 = (Button)findViewById(R.id.button_changeView);
         b3.setOnClickListener(new OnClickListener(){
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 					
 				if(isOpen){
					 
					String currentApiVersion = android.os.Build.VERSION.RELEASE;
					if(!currentApiVersion.startsWith("1")&&!currentApiVersion.startsWith("2.1")&&!currentApiVersion.startsWith("2.2"))	
					{	isBack = isBack==true?false:true;
 						if(mCamera!=null){
 							mCamera.stopPreview();
 							mCamera.setPreviewCallback(null);
 							mCamera.release();
 							mPreview.setCamera(null);
 						}					
 					
							mCamera = getCamera();
 							mCamera.setPreviewCallback(new PreviewFrameCallBack());
 							mPreview.setCamera(mCamera);	
 					}
 				}
 			
 			}
         	
         });
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_camera, menu);
         return true;
     }
 
     private Camera getCamera(){
     	Camera c = null;
     	if(isBack){
 	        try {
 	            c = Camera.open(); // attempt to get a Camera instance
 	            c.setDisplayOrientation(90); 
 	            Parameters p = c.getParameters();
 	            p.setPreviewSize(320, 240);
 	            c.setParameters(p);
 	            List<Size> ss = p.getSupportedPreviewSizes();
 	            
 	            for(Size s : ss){
 	            	Log.d("debug size : ", s.width +":"+s.height);
 	            }
 	            
 	            Log.d("debug : ", "open camera ok!!!");
 	        }
 	        catch (Exception e){ 
 	            // Camera is not available (in use or does not exist)
 	        	Log.d("debug : ", "open camera error!!! " + e.getMessage());
 	        }
     	} 
     	else{
     		//check api version, android 2.2 does not have the following api 
     		String currentApiVersion = android.os.Build.VERSION.RELEASE;
     		Log.d("debug running api : ", currentApiVersion+""); 
     		
     		if(!currentApiVersion.startsWith("1")&&!currentApiVersion.startsWith("2.1")&&!currentApiVersion.startsWith("2.2")){
 	    		int cameraNum = Camera.getNumberOfCameras(); 
 	    		if(cameraNum>1){
 	    			CameraInfo cinfo = new CameraInfo();
 	    			for(int i =0; i< cameraNum;i++){ 
 	    				Camera.getCameraInfo(i, cinfo);
 	    				if(cinfo.facing == CameraInfo.CAMERA_FACING_FRONT){
 	    					c = Camera.open(i);
 	    					c.setDisplayOrientation(90); 
 	    		            Parameters p = c.getParameters();
 	    		            p.setPreviewSize(320, 240);
 	    		            c.setParameters(p);
	    		            
 	    				}
 	    			}
 	    		}
     		}
     	}
         return c; // returns null if camera is unavailable
     }
     
     class PreviewFrameCallBack implements Camera.PreviewCallback{
 
 		@Override
 		public void onPreviewFrame(byte[] data, Camera camera) {
 			// TODO Auto-generated method stub
 				Log.d("debug : ", "begin draw frame");
 				int w = camera.getParameters().getPreviewSize().width;
 				int h = camera.getParameters().getPreviewSize().height;
 				Log.d("debug : ", "begin draw frame");
 				drawRemote(data,w,h);
 			
 			
 		}
  
     }
     
     private void drawRemote(byte[] data , int width, int height){
     	int[] rgb = decodeYUV420SP(data,width,height);
     	Log.d("debug : ", "decode 420sp ok");
     	Bitmap bmp = Bitmap.createBitmap(rgb, width, height, Bitmap.Config.ARGB_8888);
     	
     	float scaleWidth = ((float)this.width) / width;
     	float scaleHeight = ((float)this.height) / height;
     	
     	Log.d("debug : ", "Width : " + bmp.getWidth());
     	Log.d("debug : ", "Height : " + bmp.getHeight());
     	Log.d("debug : ", "scaleWidth : " + scaleWidth);
     	Log.d("debug : ", "scaleHeight : " + scaleHeight);
     	
     	if(isBack){
     	
 	    	Matrix matrix3 = new Matrix();
 	    	matrix3.postScale(1, 1);
 	        matrix3.setRotate(90);
 	        Bitmap nbmp2 = Bitmap.createBitmap(bmp,
 	        		0, 0, bmp.getWidth(),  bmp.getHeight(), matrix3, true);
 	        
 	        Log.d("debug : ", "Width2 : " + nbmp2.getWidth());
 	    	Log.d("debug : ", "Height2 : " + nbmp2.getHeight());
 	    	
 	    	if(bmp==null)
 	    		Log.d("debug :", "created bmp is null!!!!");
 	    	
 	    	remoteView.setImageBitmap(nbmp2);
     	}
     	else{
     		Matrix matrix3 = new Matrix();
 	    	matrix3.postScale(1, 1);
 	        matrix3.setRotate(270);
 	        Bitmap nbmp2 = Bitmap.createBitmap(bmp,
 	        		0, 0, bmp.getWidth(),  bmp.getHeight(), matrix3, true);
 	        
 	        Log.d("debug : ", "Width2 : " + nbmp2.getWidth());
 	    	Log.d("debug : ", "Height2 : " + nbmp2.getHeight());
 	    	
 	    	if(bmp==null)
 	    		Log.d("debug :", "created bmp is null!!!!");
 	    	
 	    	remoteView.setImageBitmap(nbmp2);
     		//remoteView.setImageBitmap(bmp);
     	}
     	
     	//ѹͼ
     	/*Matrix matrix = new Matrix();
     	scaleWidth = ((float)this.width) / nbmp2.getWidth();
     	scaleHeight = ((float)this.height) / nbmp2.getHeight();
     	//matrix.setRotate(90);
     	matrix.postScale(scaleWidth, scaleHeight);
     	Bitmap bitmap = Bitmap.createBitmap(nbmp2, 0, 0, (int) nbmp2.getWidth(),
                 (int) nbmp2.getHeight(), matrix, true); 
     	
     	Log.d("debug : ", "Width3 : " + bitmap.getWidth());
     	Log.d("debug : ", "Height3 : " + bitmap.getHeight());*/
         
         
     	
     	/*Canvas canvas = this.remoteView.getHolder().lockCanvas();  
         canvas.drawBitmap(bitmap, 0, 0, null);  
         remoteView.getHolder().unlockCanvasAndPost(canvas); */
     }
     
     public int[] decodeYUV420SP(byte[] yuv420sp, int width, int height) {  
     	  
         final int frameSize = width * height;  
   
         int rgb[] = new int[width * height];  
         for (int j = 0, yp = 0; j < height; j++) {  
             int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;  
             for (int i = 0; i < width; i++, yp++) {  
                 int y = (0xff & ((int) yuv420sp[yp])) - 16;  
                 if (y < 0) y = 0;  
                 if ((i & 1) == 0) {  
                     v = (0xff & yuv420sp[uvp++]) - 128;  
                     u = (0xff & yuv420sp[uvp++]) - 128;  
                 }  
   
                 int y1192 = 1192 * y;  
                 int r = (y1192 + 1634 * v);  
                 int g = (y1192 - 833 * v - 400 * u);  
                 int b = (y1192 + 2066 * u);  
   
                 if (r < 0) r = 0;  
                 else if (r > 262143) r = 262143;  
                 if (g < 0) g = 0;  
                 else if (g > 262143) g = 262143;  
                 if (b < 0) b = 0;  
                 else if (b > 262143) b = 262143;  
   
                 rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) &  
                     0xff00) | ((b >> 10) & 0xff);  
   
             }  
         }  
         return rgb;  
     }  
     public void onBackPressed(){
     	if(isOpen){
 	    	mCamera.stopPreview();
 	    	mCamera.setPreviewCallback(null);
     	}
     	if(mCamera!=null){
 	    	mCamera.release();
 	    	mPreview.setCamera(null);
     	}
     	
     	Log.d("debug : ", "exit program!!");
     	super.onBackPressed();
     }
 }
