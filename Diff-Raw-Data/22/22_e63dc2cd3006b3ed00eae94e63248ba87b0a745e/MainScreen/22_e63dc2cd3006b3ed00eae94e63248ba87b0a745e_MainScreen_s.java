 package ua.com.imalur.authographeer;
 
 
 import ua.com.imalur.authographeer.utils.PhotoFileHelper;
 import android.app.Activity;
 import android.content.Intent;
 import android.content.pm.PackageManager;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.MediaStore;
 import android.view.View;
 
 public class MainScreen extends Activity {
 
 	public static final int TAKE_IMAGE_ACTION_CODE = 1010;
 	public static final String EXTRA_PHOTOPATH = "photoPath";
 	
 	private Uri photoFileUri;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main_screen);
         
         if (!isCameraAvaliable())
         	findViewById(R.id.btnCamera).setEnabled(false);
     }
     
     public void onButtonClick(View v){
     	switch(v.getId()){
     	case R.id.btnCamera:
     		launchCamera();
     		break;
     	case R.id.btnBlank:
     		launchDrawing(null);
     		break;
     	case R.id.btnSettings:
     		launchSettingsScreen();
     		break;
     	}
     }
 
 	/*
      *        
      */
     private boolean isCameraAvaliable(){
     	return getPackageManager()
         		.hasSystemFeature(
         				PackageManager.FEATURE_CAMERA);
     }
     
     /*
      *    
      */
     private void launchDrawing(String photoPath) {
     	Intent intent = new Intent(this, DrawActivity.class);    	
     	intent.putExtra(EXTRA_PHOTOPATH, photoPath);
 		startActivity(intent);		
 	}
     
     /*
      *       
      *    
      */
     private void launchCamera() {
     	Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
 		photoFileUri = PhotoFileHelper.prepareRawPhotoToSave();
 		intent.putExtra(MediaStore.EXTRA_OUTPUT, photoFileUri);
		startActivityForResult(intent, TAKE_IMAGE_ACTION_CODE);
		
 	}
 	/*
 	 *     -    
 	 *      
 	 */
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
     	if (requestCode == TAKE_IMAGE_ACTION_CODE && resultCode == RESULT_OK){    		
     		PhotoFileHelper.addPhotoToGallery(this, photoFileUri);
     		String photoPath = photoFileUri.getEncodedPath();
     		launchDrawing(photoPath);
     	}
     }
     
 
 	private void launchSettingsScreen() {
 		// TODO Auto-generated method stub
 		
 	}
 
 
 }
