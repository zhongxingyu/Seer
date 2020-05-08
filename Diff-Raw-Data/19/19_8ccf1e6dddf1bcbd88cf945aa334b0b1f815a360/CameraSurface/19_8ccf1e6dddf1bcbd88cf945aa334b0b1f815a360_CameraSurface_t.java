 package com.android.interview;
 
 
 import java.io.File;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.view.Display;
 import android.view.View;
 import android.widget.ImageView;
 import android.widget.Toast;
 
 
 public class CameraSurface extends Activity {	
 	private ImageView img;	
 	private int CAMERA_RESULT = 1;
 	private int displayWidth;
 	private int displayHeight;
 	private String imageFilePath;
 
 	
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.camera);
         
         imageFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/image.jpg";
     }
 	
 	
 	public void takePhoto(View view) {		    	    
         showToast(this,imageFilePath);       	               
                 
         Uri imageFileUri = Uri.fromFile(new File(this.imageFilePath));
         
         Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
         intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, imageFileUri);
         
         startActivityForResult(intent, this.CAMERA_RESULT);               		
 	}
 
 	
     protected void onActivityResult(int requestCode, int resultCode, Intent image) {
         super.onActivityResult(requestCode, resultCode, image);
                 
         if(requestCode == this.CAMERA_RESULT){
         	showImage();	
         }
                
     }
    
 
     private void showImage() {    	
 		Display currentDisplay = getWindowManager().getDefaultDisplay();		
 		
 		this.displayWidth = currentDisplay.getWidth();
 		this.displayHeight = currentDisplay.getHeight();			
         this.img =  (ImageView)findViewById(R.id.image);  
         
         
     	BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
     	bmpFactoryOptions.inJustDecodeBounds = true;
     	    
 		Bitmap bmp = BitmapFactory.decodeFile(imageFilePath, bmpFactoryOptions);
     	
     	int heightRatio = (int)Math.ceil(bmpFactoryOptions.outHeight/(float)displayHeight);
     	int widthRatio = (int)Math.ceil(bmpFactoryOptions.outWidth/(float)displayWidth);
     	
     	if(heightRatio > 1 && widthRatio > 1) {
     		if(heightRatio > widthRatio) {
     			bmpFactoryOptions.inSampleSize = heightRatio;
     		} else {
     			bmpFactoryOptions.inSampleSize = widthRatio;
     		}
     	}
     	
     	bmpFactoryOptions.inJustDecodeBounds = false;
     	bmp = BitmapFactory.decodeFile(imageFilePath, bmpFactoryOptions);
     	
     	img.setImageBitmap(bmp);
     }
     
     private void showToast(Context mContext, String text) {
     	Toast.makeText(mContext, text, Toast.LENGTH_SHORT).show();
     }
 }
