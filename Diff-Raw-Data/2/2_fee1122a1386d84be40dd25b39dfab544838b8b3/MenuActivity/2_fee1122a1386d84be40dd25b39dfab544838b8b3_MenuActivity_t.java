 package com.jsanc623.shabo.shell;
 
 
 /*
 To get al installed apps you can use Package Manager..
 
     List<PackageInfo> apps = getPackageManager().getInstalledPackages(0);
 To run you can use package name
 
 Intent LaunchApp = getPackageManager().getLaunchIntentForPackage(package name)
 startActivity( LaunchApp );
 For more detail you can read this blog 
 http://blog.wisecells.com/2012/05/30/get-list-of-all-installed-apps-android/
 */
 
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.util.Random;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Intent;
 //import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.net.Uri;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.Environment;
 //import android.provider.MediaStore;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ImageView;
 
 public class MenuActivity extends Activity {
     private static final int CAMERA_REQUEST = 1337;
     private static final int REQUEST_FILE = 1338;
 	@SuppressWarnings("unused")
 	private static String lastImageSaved = "";
 	private static String Folder = "aaShaboShell";
     @SuppressWarnings("unused")
 	private ImageView imageView;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_menu);
         
         // Assign the take picture button an action (open camera, get picture return)
         Button take_picture = (Button) findViewById(R.id.take_picture);
         take_picture.setOnClickListener(onClickListener);
         
         // Assign the screen capture button an action (take screen shot)
         Button screen_capture = (Button) findViewById(R.id.screen_capture);
         screen_capture.setOnClickListener(onClickListener);
         
         // Assign the my files button an action (open file manager, return file)
         Button my_files = (Button) findViewById(R.id.my_files);
         my_files.setOnClickListener(onClickListener);
         
         // Assign the app lock button an action (define password, save password)
         Button app_lock = (Button) findViewById(R.id.app_lock);
         app_lock.setOnClickListener(onClickListener);
         
         // Assign the app sound button an action (open file manager, return sound)
         Button app_sound = (Button) findViewById(R.id.app_sound);
         app_sound.setOnClickListener(onClickListener);
         
         // Assign the app paint button an action (send intent for paint program)
         Button app_paint = (Button) findViewById(R.id.app_paint);
         app_paint.setOnClickListener(onClickListener);
         
         // Assign the faq button an action (send intent for faq activity)
         Button app_faq = (Button) findViewById(R.id.app_faq);
         app_faq.setOnClickListener(onClickListener);
     }
     
     
 	public OnClickListener onClickListener = new OnClickListener() {
 	    public void onClick(final View v) {
 	             switch(v.getId()){
 	                 case R.id.take_picture: {
 	                	 Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
 	                	 String imageFileLoc = "/" + MenuActivity.Folder + "/photos/" + String.valueOf(System.currentTimeMillis()) + ".jpg";
 	                	 MenuActivity.lastImageSaved = Environment.getExternalStorageDirectory().toString() + imageFileLoc;
 	                     Uri mImageCaptureUri1 = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), imageFileLoc));
 	                     cameraIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mImageCaptureUri1);                     
 	                     cameraIntent.putExtra("return-data", true);
 	                     startActivityForResult(cameraIntent, CAMERA_REQUEST);
 	                 } break;
 	                 case R.id.screen_capture: {
 	                	 if(Build.VERSION.SDK_INT >= 14){
 		                	 Bitmap bitmap;
 		                	 View v1 = v.getRootView();
 		                	 v1.setDrawingCacheEnabled(true);
 		                	 bitmap = Bitmap.createBitmap(v1.getDrawingCache());
 		                	 v1.setDrawingCacheEnabled(false);
 		                	 saveImage(bitmap, "Screenshot");
 	                	 } else {
 	                		 showDialog("Function not supported", "This function requires Android 4.0 (SDK 14) and up. Your version is Android " + Build.VERSION.RELEASE + " (SDK " + Build.VERSION.SDK_INT + ")");
 	                	 }
 	                 } break;
 	                 case R.id.my_files: {
 	                	 openFileDialog(false, false, "");
 	                 } break;
 	                 case R.id.app_lock: {
	                	Intent lockIntent = new Intent(MenuActivity.this, LockActivity.class);
 	                	MenuActivity.this.startActivity(lockIntent);
 	                 } break;
 	                 case R.id.app_sound: {
 	                	openFileDialog(false, true, "/Music");
 	                 } break;
 	                 case R.id.app_paint: {
 	                 	Intent scribblerIntent = new Intent(MenuActivity.this, Scribbler.class);
 	                	MenuActivity.this.startActivity(scribblerIntent);
 	                 } break;
 	                 case R.id.app_faq: {
 	                	Intent faqIntent = new Intent(MenuActivity.this, FAQActivity.class);
 	                	MenuActivity.this.startActivity(faqIntent);
 	                 } break;
 	              }
 
 	    }
 	};
 	
 	private void openFileDialog(Boolean canSelectDirectories, Boolean setOnlyMP3, String additionalPath){
    	 	Intent intent = new Intent(getBaseContext(), FileDialog.class);
    	 	intent.putExtra(FileDialog.START_PATH, Environment.getExternalStorageDirectory().toString() + additionalPath);
      
      	//can user select directories or not
      	if(canSelectDirectories == true){
    	 	    intent.putExtra(FileDialog.CAN_SELECT_DIR, true);
      	} else {
    	 	    intent.putExtra(FileDialog.CAN_SELECT_DIR, false);
      	}
      	
      	//alternatively you can set file filter
      	if(setOnlyMP3 == true){
      	    intent.putExtra(FileDialog.FORMAT_FILTER, new String[] { "mp3" });
      	}
      	
      	startActivityForResult(intent, REQUEST_FILE);
 	}
 	
 	public void saveImage(Bitmap finalBitmap, String filepreFix){
 		File baseDirectory = Environment.getExternalStorageDirectory();
 		File directory = new File(baseDirectory, "/" + MenuActivity.Folder + "/screenshots/");
 	    if (!directory.exists()) {
 	        if (!directory.mkdirs()) {
 	        	Log.e("ShaboShell :: ", "Problem creating Screenshots folder, probably exists");
 	        }
 	    }
 	    
 		Random generator = new Random();
 		int n = 10000;
 		n = generator.nextInt(n);
 		String fileName = filepreFix + "-" + n + ".jpg";
 		File file = new File(directory, fileName);
 		MenuActivity.lastImageSaved = file.toString();
 		if(file.exists()) file.delete();
 		try{
 			FileOutputStream out = new FileOutputStream(file);
 			finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
 			out.flush();
 			out.close();
 			showDialog("New file created!", "Your image has been saved at: " + baseDirectory.toString() + "/" + MenuActivity.Folder + "/screenshots/");
 		} catch (Exception e){
 			e.printStackTrace();
 		}
 	}
 	
 	public void showDialog(String title, String message){
 		 AlertDialog alertDialog = new AlertDialog.Builder(MenuActivity.this).create();
 		 alertDialog.setTitle(title);
 		 alertDialog.setMessage(message);
 		 alertDialog.show();
 	}
 	
 	public void showImage(String imageLocation){
 		Intent intent = new Intent();  
 		intent.setAction(Intent.ACTION_VIEW);  
 		Uri imgUri = Uri.parse("file://" + imageLocation);  
 		intent.setDataAndType(imgUri, "image/*");  
 		startActivity(intent);
 	}
     
 	protected void onActivityResult(int requestCode, int resultCode, Intent data){
 	    if (requestCode == CAMERA_REQUEST){
 		}
 	    
 	    if (requestCode == REQUEST_FILE){
 	    	if (resultCode == Activity.RESULT_OK) {
                 @SuppressWarnings("unused")
 				String filePath = data.getStringExtra(FileDialog.RESULT_PATH);
 	    	}
         }
 	}    
 	
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
 }
