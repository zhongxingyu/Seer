 package com.cloudspace.tilt.TiltShift;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 import com.cloudspace.tilt.MainActivity;
 import com.cloudspace.tilt.ImageManip.Blur;
 import com.cloudspace.tilt.ImageManip.MediaScannerWrapper;
 import com.cloudspace.tilt.ImageManip.Saturation;
 
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.LinearGradient;
 import android.graphics.Matrix;
 import android.graphics.Paint;
 import android.graphics.PorterDuffXfermode;
 import android.graphics.Bitmap.Config;
 import android.graphics.PorterDuff.Mode;
 import android.graphics.Shader.TileMode;
 import android.os.AsyncTask;
 import android.os.Environment;
 import android.text.format.Time;
 import android.view.View;
 import android.widget.ProgressBar;
 import android.widget.Toast;
 
 public class TiltShift {
 	public static MainActivity context;
 	public Integer width;
 	public Integer height;
 	public Integer currentXTop = 0;
 	public Integer currentXBottom = 0;
 	private static Matrix matrix = new Matrix();
 	
 	public Bitmap contrastImage;
 	public Bitmap topShift;
 	public Bitmap bottomShift;
 	private Bitmap blurImage;
 	private static Canvas topCanvas;
 	private static Canvas bottomCanvas;
 	public File sharePath;
 	
 	private ProgressBar progressbar;
 	
 	// Pass in the main activity
 	public TiltShift(MainActivity mainActivity) {
 		context = mainActivity;
 	}
 	
 	public void load(Bitmap baseImage){
 		// Get the dimensions of the image
 		width = baseImage.getWidth();
 		height = baseImage.getHeight();
 		
 		// Make the image reusable
 		BitmapFactory.Options options = new BitmapFactory.Options();
         options.inBitmap = contrastImage;
         
         // Apply contrast to make the colors more toy like
 		contrastImage = Saturation.adjustedHue(baseImage, width, height);
 		
 		// Make the image reusable
 		options.inBitmap = blurImage;
 		// Apply the blur with a base setting
 		blurImage = Bitmap.createBitmap(Blur.fastblur(contrastImage, 15), 0, 0, width, height, matrix, false);
 		
 		// Create top blur image
 		options.inBitmap = topShift;
 		topShift = Bitmap.createBitmap(width, height, Config.ARGB_8888);
 		topCanvas = new Canvas(topShift);
 	    
 		// Create bottom blur image
 		options.inBitmap = bottomShift;
 		bottomShift = Bitmap.createBitmap(width, height, Config.ARGB_8888);
 		bottomCanvas = new Canvas(bottomShift);
 	}
 	
 	public void reload(Bitmap contrastImage, Bitmap blurImage){
 		// Reapply all settings
 		this.contrastImage = contrastImage;
 		this.blurImage = blurImage;
 		width = contrastImage.getWidth();
 		height = contrastImage.getHeight();
 		BitmapFactory.Options options = new BitmapFactory.Options();
 		options.inBitmap = topShift;
 		topShift = Bitmap.createBitmap(width, height, Config.ARGB_8888);
 		options.inBitmap = bottomShift;
 	    bottomShift = Bitmap.createBitmap(width, height, Config.ARGB_8888);
 	}
 	
 	public void shiftTop(int x, int y){
 		topCanvas.drawBitmap(blurImage, 0, 0, null);
 	    int start = ((height/10)*3)-50;
 	    int stop = (height/10)*3;
 
 	    // Get the strength of the blur
 	    x = currentXTop+x;
 
 	    //Create a shader that is a linear gradient that covers the blur
 	    Paint paint = new Paint();
 	    if(x < height && x > 0){
 	    	currentXTop = x;
 	    	if(x < (y-50)){
 	    		start = x;
 	    	}
 	    	else{
 	    		start = y-50;
 	    	}
 	    }
 	    else if(x >= height){
 	    	start = y-50;
 	    }
	    else if(x <= 0){
	    	start = 0;
	    }
 	    if(y < height && y > 0){
 	    	stop = y;
 	    }
 	    
 	    LinearGradient shader = new LinearGradient(0, start, 0,
 	    		stop, 0xffffffff, 0x00ffffff,
 	            TileMode.CLAMP);
 
 	    paint.setShader(shader);
 
 	    paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
 
 	    topCanvas.drawRect(0, 0, width, height, paint);
 		return;
 	}
 	
 	public void shiftBottom(int x, int y){
 		bottomCanvas.drawBitmap(blurImage, 0, 0, null);
 		int new_y = height - y;
 		int start = height - ((height/10)*3);
 		int bottom = new_y-50;
 	    
 		// Get the strength of the blur
 		x = currentXBottom+x;
 		
 		//Create a shader that is a linear gradient that covers the blur
 	    if(new_y > 0 && new_y < height){
 	    	start = new_y;
 	    }
	    if(x > 0 && x < (height/5)){
 	    	currentXBottom = x;
 	    	if(x < (new_y-50)){
 	    		bottom = new_y-x;
 	    	}
 	    	else{
 	    		bottom = 0;
 	    	}
 	    }
 	    else if(x >= height){
 	    	bottom = 0;
 	    }
 	    else if(x<=0){
 	    	bottom = new_y-1;
 	    }
 	    if(new_y == (height-50)){
 	    	bottom = new_y-50;
 	    }
 
 		//Create a shader that is a linear gradient that covers the reflection
 		Paint paint = new Paint();
 		LinearGradient shader2 = new LinearGradient(0, start, 0,
 		  		bottom, 0xffffffff, 0x00ffffff,
 		          TileMode.CLAMP);
 
 		paint.setShader(shader2);
 
 		paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
 
 		bottomCanvas.drawRect(0, 0, width, height, paint);
 		return;
 	}
 	
 	public void save(ProgressBar progressbar){
 		this.progressbar = progressbar;
 		// Begin saving the images
 		new SaveAsync().execute();
 	}
 	
 	private class SaveAsync extends AsyncTask<Void, Integer, Void>{
 
 		@Override
     	protected void onPreExecute(){
     		super.onPreExecute();
 		}
 		
 		@Override
 		protected Void doInBackground(Void... params) {
 			// Create a new final bitmap
 	    	Bitmap saveBitmap = Bitmap.createBitmap(contrastImage.getWidth(), contrastImage.getHeight(), Config.ARGB_8888); 
 	    	Canvas saveCanvas = new Canvas(saveBitmap);
 	    	
 	    	// Draw user adjusted bitmaps
 	    	saveCanvas.drawBitmap(contrastImage, 0, 0, null);
 	    	saveCanvas.drawBitmap(topShift, 0, 0, null);
 	    	saveCanvas.drawBitmap(bottomShift, 0, 0, null);
 	    	
 	    	// Save bitmap
 	    	storeImage(saveBitmap);
 			return null;
 		}
 		
 		@Override
 		protected void onProgressUpdate(Integer... values) {
 			super.onProgressUpdate(values);
 		}
 		
 		@Override
 		protected void onPostExecute(Void result) {
 			super.onPostExecute(result);
 			// Notify user of completion
 			Toast.makeText(context,"Image Saved",Toast.LENGTH_SHORT).show();
 			// If there is an internet connection enable sharing
 			if(context.isNetworkAvailable() == true){
 				context.share.setEnabled(true);
 			}
 			progressbar.setVisibility(View.INVISIBLE);
 		}
 		
 		private void storeImage(Bitmap image) {
 			
 			// Get image folder location
 		    File pictureFile = getOutputMediaFile();
 		    sharePath = pictureFile;
 		    
 		    if (pictureFile == null) {
 		        return;
 		    } 
 		    try {
 		    	
 		    	// Create Output Stream to folder location
 		        FileOutputStream fos = new FileOutputStream(pictureFile);
 		        
 		        
 		        // Compress final bitmap to save at 100%
 		        image.compress(Bitmap.CompressFormat.PNG, 100, fos);
 		        
 		        
 		        // Flush and close the stream
 		     	fos.flush();
 		     	fos.close();
 		     	
 		     	
 		     	// Run media scanner to update gallery
 		        MediaScannerWrapper scan = new MediaScannerWrapper(context, String.valueOf(pictureFile), "image/jpeg");
 				scan.scan();
 				
 
 		    } catch (FileNotFoundException e) {
 		    } catch (IOException e) {
 		    }  
 		}
 		
 		/** Create a File for saving an image or video */
 		private File getOutputMediaFile(){
 		    // To be safe, you should check that the SDCard is mounted
 		    // using Environment.getExternalStorageState() before doing this. 
 		    File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
 		            + "/TiltShift"); 
 
 		    // This location works best if you want the created images to be shared
 		    // between applications and persist after your app has been uninstalled.
 
 		    // Create the storage directory if it does not exist
 		    if (! mediaStorageDir.exists()){
 		        if (! mediaStorageDir.mkdirs()){
 		            return null;
 		        }
 		    } 
 		    // Create a media file name
 		    Time today = new Time(Time.getCurrentTimezone());
 			today.setToNow();
 		    String mImageName="MI_"+ today.format("%Y%m%d%H%M%S") +".jpg";
 		    File mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName); 
 		    return mediaFile;
 		}
 	}
 }
