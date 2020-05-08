 package com.qc.camera;
 
 import java.io.File;
 
 import android.app.Activity;
 import android.content.ContentResolver;
 import android.content.ContentValues;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.MediaStore;
 import android.provider.MediaStore.Images.Media;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.Toast;
 
 import com.qc.R;
 import com.qc.SemiPrototypeActivity;
 
 public class CameraActivity extends Activity {
     // define the file-name to save photo taken by Camera activity
     String fileName = "muzhigirl.jpg";
     // create parameters for Intent with filename
     ContentValues values;
     Button buttonTakePhoto;
     Button buttonChoosePhoto;
     ImageView imgTakenPhoto;
     private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 0;
     private static final int PICK_IMAGE_ACTIVITY_REQUESRT_CODE = 1;
 
     class buttonChoosePhotoClicker implements Button.OnClickListener {
         public void onClick(View v) {
             Intent photoPickerIntent = new Intent(Intent.ACTION_PICK,
                     android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
             photoPickerIntent.setType("image/*");
             startActivityForResult(photoPickerIntent, PICK_IMAGE_ACTIVITY_REQUESRT_CODE);
         }
     }
 
     class buttonTakePhotoClicker implements Button.OnClickListener {
         public void onClick(View v) {
             ContentValues values = new ContentValues();
             values.put(Media.TITLE, fileName);
             values.put(Media.DESCRIPTION, "Image capture by camera, muzhi");
             Uri uri = getContentResolver().insert(Media.EXTERNAL_CONTENT_URI, values);
 
             Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
             intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
             intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
             startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
         }
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         Toast toast = Toast.makeText(getApplicationContext(), "onCreate", Toast.LENGTH_SHORT);
         toast.show();
         setContentView(R.layout.camera);
         buttonTakePhoto = (Button) findViewById(R.id.buttonTakePhoto);
         buttonChoosePhoto = (Button) findViewById(R.id.buttonChoosePhoto);
         imgTakenPhoto = (ImageView) findViewById(R.id.imageView);
 
         buttonTakePhoto.setOnClickListener(new buttonTakePhotoClicker());
         buttonChoosePhoto.setOnClickListener(new buttonChoosePhotoClicker());
     }
 
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         Toast.makeText(this, "on activity result is called", Toast.LENGTH_SHORT).show();
         switch (requestCode) {
         case CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE:
             if (resultCode == RESULT_OK) {
                 Toast.makeText(this, "photo taken ok", Toast.LENGTH_SHORT).show();
             } else if (resultCode == RESULT_CANCELED) {
                SemiPrototypeActivity.setTab(2);
                 Toast.makeText(this, "Picture was not taken", Toast.LENGTH_SHORT).show();
             } else {
                 Toast.makeText(this, "Picture was not taken", Toast.LENGTH_SHORT).show();
             }
             break;
 
         case PICK_IMAGE_ACTIVITY_REQUESRT_CODE:
             if ((resultCode == RESULT_OK) && (data.getData() != null)) {
                 if (data != null) {
                     Toast.makeText(this, "data not null", Toast.LENGTH_SHORT).show();
                 }
                 Toast.makeText(this, "chosen", Toast.LENGTH_SHORT).show();
                 ContentResolver cr = this.getContentResolver();
                 Log.i("photo, uri is ", data.getData().toString());
                 Log.i("scheme is", data.getData().getScheme());
                // Cursor cursor = cr.query(data.getData(), null, null, null,
                // null);
                // cursor.moveToFirst();
                // int idx = cursor.getColumnIndex(ImageColumns.DATA);
                // String fileSrc = cursor.getString(idx);

                // Bitmap bitmapPreview = BitmapFactory.decodeFile(fileSrc);
                // BitmapDrawable bmpDrawable = new
                // BitmapDrawable(bitmapPreview);
                // imgTakenPhoto.setBackgroundDrawable(bmpDrawable);
                // imgTakenPhoto.setImageBitmap(selectedImage );
                 imgTakenPhoto.setImageURI(data.getData());
 
             } else {
                 Toast.makeText(this, "not chosen", Toast.LENGTH_SHORT).show();
             }
         }
     }
 
     public static File convertImageUriToFile(Uri imageUri, Activity activity) {
         Cursor cursor = null;
         try {
             String[] proj = { MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID,
                     MediaStore.Images.ImageColumns.ORIENTATION };
             cursor = activity.managedQuery(imageUri, proj, // Which columns to
                                                            // return
                     null, // WHERE clause; which rows to return (all rows)
                     null, // WHERE clause selection arguments (none)
                     null); // Order-by clause (ascending by name)
             int file_ColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
             int orientation_ColumnIndex = cursor
                     .getColumnIndexOrThrow(MediaStore.Images.ImageColumns.ORIENTATION);
             if (cursor.moveToFirst()) {
                 String orientation = cursor.getString(orientation_ColumnIndex);
                 return new File(cursor.getString(file_ColumnIndex));
             }
             return null;
         } finally {
             if (cursor != null) {
                 cursor.close();
             }
         }
     }
 
 }
