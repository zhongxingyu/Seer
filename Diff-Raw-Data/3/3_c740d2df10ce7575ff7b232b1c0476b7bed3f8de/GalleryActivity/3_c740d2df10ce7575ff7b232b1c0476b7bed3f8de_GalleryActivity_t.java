 /**
  * @author Niklas Bauer
  */
 package com.uc.memeapp;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ImageView;
 
 /**
  * Loads an PhotoPicker Intent to get an image
  * working on getting it from all locations:gallery, dropbox, google drive, etc.
  * once picture is selected, it displays in activity
  * either accept and send it to be edited, or select new picture
  * @author Niklas
  *
  */
 
 public class GalleryActivity extends Activity implements OnClickListener {
 
     Button acceptButton;
     Button redoButton;
     Bundle instance;
     String path;
     /** Called when the activity is first created. */
      @Override
      public void onCreate(Bundle savedInstanceState) {
     	 super.onCreate(savedInstanceState);
     	 instance = savedInstanceState;
     	 
     	 /* Creates the intent to select content from the device*/
     	 Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
     	 /*Wants to select an image*/
     	 photoPickerIntent.setType("image/*");
     	 /**
     	  * Launches activity to then select an image
     	  * Done in onActivityResult
     	  */
     	 startActivityForResult(photoPickerIntent,1); 
     	 setContentView(R.layout.activity_gallery);
     	 
     	 acceptButton = (Button) findViewById(R.id.accept_image);
     	 redoButton = (Button) findViewById(R.id.get_new_image);
     	 acceptButton.setOnClickListener(this);
     	 redoButton.setOnClickListener(this);
    	 
      }
      /**
       * Activity for photograph selection is launched
       * @param requestCode , what type of action is taken, in this case ACTION_GET_CONTENT
       * @param resultCode , if the action is completed
       * @param imageReturnedIntent , the image that the user has selected 
       *  */
      @Override
      protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) 
      { 
          super.onActivityResult(requestCode, resultCode, imageReturnedIntent); 
              if(resultCode == RESULT_OK)
              {
             	 /**opens the selection activity for photographs*/
                 Uri selectedImage = imageReturnedIntent.getData();
                 path = selectedImage.toString();
                 // InputStream imageStream = null;
                 Cursor cursor = getContentResolver().query(selectedImage, new String[] { android.provider.MediaStore.Images.ImageColumns.DATA }, null, null, null);
                 cursor.moveToFirst();
                 cursor.close();
                 ImageView displayImage = (ImageView) findViewById(R.id.targetimage);
                 displayImage.setImageURI(selectedImage);
                
              }
              //if result is not ok... kinda self explanatory
              else{
             	 throw new IllegalArgumentException("Activity did not return a picture");
              }
             }
          
      
     /** 
      * for the two buttons, accept, and take another photo
      * */
     public void onClick(View v) {
 		switch(v.getId()){
 			/* Select, or loads, the image on to the PhotoEditActivity so that the user can add text*/
 			case (R.id.accept_image):
 				Intent intent = new Intent(this, PhotoEditActivity.class);
 				intent.putExtra("path", path);
 				intent.putExtra("caller", "Gallery");
 				startActivity(intent);
 				break;
 			/* If the user did not like the selected image, then it will relaunch the selection*/
 			case(R.id.get_new_image):
 				this.onCreate(instance);
 				break;
 		
 		}
     }
 }
     
