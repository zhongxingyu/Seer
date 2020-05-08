 package com.fps;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.TextView;
 
 import com.fps.flickr.resource.FlickrUser;
 import com.fps.tasks.LoadPhotoSetsTask;
 
 public class FlickrPhotoSync extends Activity {
	private static final int DO_OAUTH_REQUEST_CODE = 101;
 	
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
     }
     
     public void loadPhotoSets(View view){
    	startActivityForResult(new Intent().setClass(this, PrepareRequestTokenActivity.class), DO_OAUTH_REQUEST_CODE);
     }
     
     @Override
     public void onActivityResult(int reqCode, int resultCode, Intent data) {
    	Log.d(FPSContants.LOG_TAG, "Handling oauth activity response");
 		super.onActivityResult(reqCode, resultCode, data);
		if(reqCode == DO_OAUTH_REQUEST_CODE){
 			new LoadPhotoSetsTask(this).execute(getUsername());
 		}
     }
     
     private String getUsername(){
     	return ((EditText)findViewById(R.id.flickrUsername)).getText().toString();
     }
     
     public void setMessage(String message){
     	((TextView)findViewById(R.id.flickrId)).setText(message);
     }
     
     public void displayUsersPhotoSets(FlickrUser flickrUser){
     	Intent intent = new Intent(this, PhotoSetsActivity.class);
     	intent.putExtra(FPSContants.FLICKR_USER_EXTRA, flickrUser);
     	startActivityForResult(intent, 0);
     }
 }
