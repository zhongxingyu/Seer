 package edu.umich.noteable;
 
 import java.io.File;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.provider.MediaStore;
 import android.support.v4.app.NavUtils;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ImageView;
 
 public class SheetSnapshot extends Activity {
 	private static final int CAMERA_PIC_REQUEST = 1337;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_sheet_snapshot);
         getActionBar().setDisplayHomeAsUpEnabled(true);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_sheet_snapshot, menu);
         return true;
     }
 
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case android.R.id.home:
                 NavUtils.navigateUpFromSameTask(this);
                 return true;
         }
         return super.onOptionsItemSelected(item);
     }
     public void takephoto(View view){
     
     //Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
     
    // startActivityForResult(cameraIntent, CAMERA_PIC_REQUEST);
     	
     	Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
     	intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
     	//File pictureFile = new File(Environment.getExternalStoragePublicDirectory(
     	//		                         Environment.DIRECTORY_PICTURES), "MyCameraApp");
     	//File picture = new File(pictureFile.getPath()+File.separator + "new" + ".bmp");
     	File picFile = getOutputMediaFile();
     	intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(picFile));
     	startActivityForResult(intent, CAMERA_PIC_REQUEST);
     
     }
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {  
         if (requestCode == CAMERA_PIC_REQUEST) {  
         	//Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
         	
         	//Bitmap.createScaledBitmap(thumbnail, 1280, 720, true);
         	//ImageView image = (ImageView) findViewById(R.id.viewer);
         	//image.setImageBitmap(thumbnail);
         	//Intent intent = getIntent();
         	//File picFile = intent.getExtras(File(MediaStore.EXTRA_OUTPUT));
         	
         	
        	Uri u = data.getData();
        	Bitmap bitmap = BitmapFactory.decodeFile(u.getPath());
        	Bitmap bit2 = Bitmap.createScaledBitmap(bitmap, 2048, 2048, true);
        	
        	ImageView image = (ImageView) findViewById(R.id.viewer);
        	image.setImageBitmap(bit2);
         }  
     }
     /** Create a File for saving the image */
	private static File getOutputMediaFile(){
 
 	    File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
 	              Environment.DIRECTORY_PICTURES), "MyCameraApp");
 
 	    if (! mediaStorageDir.exists()){
 	        if (! mediaStorageDir.mkdirs()){
 	            Log.d("MyCameraApp", "failed to create directory");
 	            return null;
 	        }
 	    }
 
 	    // Create a media file name
 	    File mediaFile;
 	        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
 	        "IMG_"+ "new" + ".jpg");
 
 	    return mediaFile;
 	}
 
  
 }
