 package com.example.qrrcodedatastreaming;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.hardware.Camera;
 import android.hardware.Camera.PictureCallback;
 import android.hardware.Camera.PreviewCallback;
 import android.os.Bundle;
 import android.os.Environment;
 import android.util.Log;
 import android.view.Menu;
 import android.widget.FrameLayout;
 import android.widget.ImageView;
 
 import com.example.qrrcodedatastreaming.R.id;
 import com.google.zxing.BarcodeFormat;
 import com.google.zxing.WriterException;
 import com.google.zxing.common.BitMatrix;
 import com.google.zxing.qrcode.QRCodeWriter;
 
 public class MainActivity extends Activity {
 	public CameraPreview mPreview;
 	public Camera c;
 	private QRCodeWriter qw=new QRCodeWriter();
 	public static final int MEDIA_TYPE_IMAGE = 1;
 	private static final int MEDIA_TYPE_VIDEO = 2;
 	private static final int WHITE = 0xFFFFFFFF;
 	  private static final int BLACK = 0xFF000000;
 	@SuppressLint("NewApi")
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		c = null;
         
         try {
             c = Camera.open(1); // attempt to get a Camera instance
             
         }
         catch (Exception e){
             // Camera is not available (in use or does not exist)
         }
         Camera.Size csize = c.getParameters().getPreviewSize();
         int mPreviewHeight = csize.height; //
         int mPreviewWidth = csize.width;
         Camera.Parameters parameters = c.getParameters();
         parameters.setPictureSize(800, 800);
        // parameters.setPreviewSize(700, 700);
         parameters.set("orientation", "portrait");
         c.setDisplayOrientation(90);
         c.setParameters(parameters);
         mPreview = new CameraPreview(this, c);
         FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
         
         preview.addView(mPreview);
         final PreviewCallback mPicture = new PreviewCallback() {
 
         	public void onPreviewFrame(byte[] data, Camera camera)  
             { 
                     try 
                     { 
                             BitmapFactory.Options opts = new BitmapFactory.Options(); 
                             Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);//,opts); 
                             Log.i("file","Bitmap captured");
                     } 
                     catch(Exception e) 
                     {
                     	Log.i("file","not captured");
                     } 
             } 
 
         };
         Log.i("file","good1");
        c.setPreviewCallback(mPicture);
         Log.i("file","good2");
         String s="This is a test";
         Bitmap b= encode(s);
         ImageView iv=(ImageView) this.findViewById(id.qrcode_image);
         iv.setImageBitmap(b);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 	 private static File getOutputMediaFile(int type){
 	        // To be safe, you should check that the SDCard is mounted
 	        // using Environment.getExternalStorageState() before doing this.
 
 	        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
 	                  Environment.DIRECTORY_PICTURES), "MyCameraApp");
 	        // This location works best if you want the created images to be shared
 	        // between applications and persist after your app has been uninstalled.
 
 	        // Create the storage directory if it does not exist
 	        if (! mediaStorageDir.exists()){
 	            if (! mediaStorageDir.mkdirs()){
 	                Log.d("MyCameraApp", "failed to create directory");
 	                return null;
 	            }
 	        }
 
 	        // Create a media file name
 	        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
 	        File mediaFile;
 	        if (type == MEDIA_TYPE_IMAGE){
 	            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
 	            "IMG_123.jpg");
 	        } else if(type == MEDIA_TYPE_VIDEO) {
 	            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
 	            "VID_"+ timeStamp + ".mp4");
 	        } else {
 	            return null;
 	        }
 
 	        return mediaFile;
 	    }
 	 public Bitmap encode(String s){
 	    	
          BitMatrix result=null;
          
          try {
            try {
   			result = qw.encode(s, BarcodeFormat.QR_CODE, 300, 300);
   		} catch (WriterException e) {
   			// TODO Auto-generated catch block
   			//e.printStackTrace();
   		}
          } catch (IllegalArgumentException iae) {
            // Unsupported format
            
          }
          Log.i("123","good5");
          int width = result.getWidth();
          int height = result.getHeight();
          int[] pixels = new int[width * height];
          for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
              pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
            }
          }
          
          Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
          bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
          Log.i("123","should change image");
          return bitmap;
         // ImageView newiv=new ImageView(this);
          //newiv.setImageBitmap(bitmap);
          //this.setContentView(newiv);
     }
 }
