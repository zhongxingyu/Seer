 /*
  * This file is part of TaskMan
  *
  * Copyright (C) 2012 Jed Barlow, Mark Galloway, Taylor Lloyd, Braeden Petruk
  *
  * TaskMan is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * TaskMan is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with TaskMan.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package ca.cmput301.team13.taskman;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.InputStream;
 
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.provider.MediaStore;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ImageView;
 import utils.Notifications;
 
 /**
  * ImageCaptureActivity is the activity that allows the user
 * take a photo or select one from the devices' library to 
 * fulfill an image requirement.
  * This activity should be launched with an intent created
  * by FulfillmentActivityFactory.
  */
 public class ImageCaptureActivity extends FulfillmentActivity implements OnClickListener {
 
     private Uri imageFileUri;
     private Bitmap selectedImage;
     public boolean photoTaken = false;
     private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
     private static final int GALLERY_IMAGE_ACTIVITY_REQUEST_CODE = 200;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.image_fulfillment);
 
         //setup our listeners
         ((Button)findViewById(R.id.take_button)).setOnClickListener(this);
         ((Button)findViewById(R.id.gallery_button)).setOnClickListener(this);
         ((Button)findViewById(R.id.save_button)).setOnClickListener(this);
         ((Button)findViewById(R.id.cancel_button)).setOnClickListener(this);
     }
     
     @Override
     public void onResume() {
         super.onResume();
     }
 
     @Override
     public void onPause() {
         super.onPause();
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.image_fulfillment, menu);
         return true;
     }
 
     /**
      * Delegates action based on which listener has been clicked
      * @param source 
      */
     public void onClick(View source) {
         if(source.equals(findViewById(R.id.take_button))) {
             takeAPhoto();
         }
         else if (source.equals(findViewById(R.id.gallery_button))) {
             selectAPhoto();
         }
         else if (source.equals(findViewById(R.id.save_button))) {
             save();
         }
         else if (source.equals(findViewById(R.id.cancel_button))) {
             cancel();
         }
     }
 
     /**
      * Send the taken/selected photo to our parent and exit the Activity
      */
     public void save() {
         //test if a photo has been selected.
         if (photoTaken) {
             fulfillment.setImage(selectedImage);
             successful = true;
             finish();
         } else {
             Notifications.showToast(getApplicationContext(), "No photo selected");
         }
     }
     
     /**
      * Cancel the Activity
      */
     public void cancel() {
         successful = false;
         finish();
     }
     
     /**
      * Takes the user to the gallery where a previously taken photo can
      * be selected for use
      */
     public void selectAPhoto(){
         Intent intent = new Intent(Intent.ACTION_PICK);
         intent.setType("image/*");
         startActivityForResult(intent, GALLERY_IMAGE_ACTIVITY_REQUEST_CODE);
     }
 
     /**
      * Sets up the filepath for a new photo and launches the built-in camera application to 
      * get a photo.
      */
     public void takeAPhoto() {
         //set a file path for the new photo
         String folder = Environment.getExternalStorageDirectory().getAbsolutePath() + "/tmp";
         File folderF = new File(folder);
         if (!folderF.exists()) {
             folderF.mkdir();
         }
         String imageFilePath = folder + "/" + String.valueOf(System.currentTimeMillis()) + ".jpg";
         File imageFile = new File(imageFilePath);
         imageFileUri = Uri.fromFile(imageFile);
         
         //Start the built-in camera application to get our photo
         Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
         intent.putExtra(MediaStore.EXTRA_OUTPUT, imageFileUri);
         startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
     }
 
     /**
      * Handles the result condition and/or returned data from the Android built-in 
      * camera and also from the Photo Gallery selection. Converts returned photo's
      * into bitmaps and sets the screen to show a preview of the selected image.
      * 
      * @param requestCode specifies which type of activity we are returning from
      * @param resultCode signifies the success or fail of the intent
      * @param data The data returned from the intent
      */
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         ImageView preview = (ImageView)findViewById(R.id.image_view);
 
         if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
             if (resultCode == RESULT_OK) {
                 //Photo Taking was a success
                 Notifications.showToast(getApplicationContext(), "Photo Taken");
                 photoTaken = true;
                 
                 InputStream imageStream = null;
                 try {
                     imageStream = getContentResolver().openInputStream(imageFileUri);
                 } catch (FileNotFoundException e) {
                     // TODO Auto-generated catch block
                     e.printStackTrace();
                 }
                 //convert the image to a bitmap
                 Bitmap bm = BitmapFactory.decodeStream(imageStream);
 
                 //set the preview to show the image
                 preview.setImageBitmap(bm);
                 setSelectedImage(bm);
             } else if (resultCode == RESULT_CANCELED) {
                 //Photo Taking was Cancelled
                 Notifications.showToast(getApplicationContext(), "Photo Cancelled");
             } else {
                 //Photo Taking had an error
                 Notifications.showToast(getApplicationContext(), "Error taking Photo" + resultCode);
             }
         } else if (requestCode == GALLERY_IMAGE_ACTIVITY_REQUEST_CODE){
             if (resultCode == RESULT_OK) {
                 //Photo was successfully chosen from Gallery
                 Notifications.showToast(getApplicationContext(), "Photo Selected");
                 photoTaken = true;
                 
                 //get the returned image from the Intent
                 imageFileUri = data.getData();
                 InputStream imageStream = null;
                 try {
                     imageStream = getContentResolver().openInputStream(imageFileUri);
                 } catch (FileNotFoundException e) {
                     // TODO Auto-generated catch block
                     e.printStackTrace();
                 }
                 //convert the image to a bitmap
                 Bitmap bm = BitmapFactory.decodeStream(imageStream);
                 
                 //set the preview to show the image
                 preview.setImageBitmap(bm);
                 setSelectedImage(bm);
             } else if (resultCode == RESULT_CANCELED) {
                 //Photo selection was cancelled
                 Notifications.showToast(getApplicationContext(), "Photo Selection Cancelled");
             } else {
                 //Photo selection had an error
                 Notifications.showToast(getApplicationContext(), "Error choosing Photo" + resultCode);
             }
         }
     }
 
     /**
     * Returns the bitmap to be saved
      * @return the bitmap to be saved
      */
     public Bitmap getSelectedImage() {
         return selectedImage;
     }
 
     /**
     * Sets the bitmap to be saved
      * @param selectedImage the chosen bitmap image
      */
     public void setSelectedImage(Bitmap selectedImage) {
         this.selectedImage = selectedImage;
     }
 }
