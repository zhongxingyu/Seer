 
 package com.scribdroid.android;
 
 import java.io.File;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.TypedArray;
 import android.graphics.BitmapFactory;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.BaseAdapter;
 import android.widget.Button;
 import android.widget.Gallery;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class PictureGalleryActivity extends Activity {
 
     // Debugging
     private static final String TAG = "PictureGalleryActivity";
     private static final boolean D = true;
 
     private Gallery gallery;
     private ImageAdapter adapter;
     
     private String selectedName;
     
     private TextView header;
     private Button buttonSaveSD;
     private Button buttonDelete;
     private Button buttonTakeAnother;
     private MyApp appState;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         setContentView(R.layout.picturegallery);
         
         appState = (MyApp) getApplicationContext();
 
         // populate ui elements
         header = (TextView) findViewById(R.id.textView_gallery_header);
         buttonSaveSD = (Button)findViewById(R.id.button_save_picture_SD);
         buttonDelete = (Button) findViewById(R.id.button_delete_picture);
         buttonTakeAnother = (Button) findViewById(R.id.button_take_another_picture);
         gallery = (Gallery) findViewById(R.id.gallery_picture);
         
         // create adapter for gallery
         adapter = new ImageAdapter(this);
         
         // set adapter and onselect listener for gallery
         gallery.setAdapter(adapter);
         gallery.setOnItemSelectedListener(new OnItemSelectedListener() {
 
             @Override
             public void onItemSelected(AdapterView parent, View view, int position, long id) {
              selectedName = fileList()[position];
              header.setText(selectedName);
              
              // show save and delete buttons
              buttonDelete.setVisibility(View.VISIBLE);
              buttonSaveSD.setVisibility(View.VISIBLE);
             }
 
             @Override
             public void onNothingSelected(AdapterView parent) {
              selectedName = "";
              header.setText(getResources().getString(R.string.no_pictures));
              
              // hide save and delete buttons
              buttonDelete.setVisibility(View.INVISIBLE);
              buttonSaveSD.setVisibility(View.INVISIBLE);
             }
         });
         
         // onclick listener for deleting picture
         buttonDelete.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 // make sure we have something selected before deleting
                 if (selectedName != "") {
                     Boolean deleted = deleteFile(selectedName);
                     if (deleted) {
                         Toast.makeText(getBaseContext(), getResources().getString(R.string.successful_deleting), Toast.LENGTH_LONG).show();
                         adapter.notifyDataSetChanged();
                         gallery.dispatchSetSelected(true); // needed to update header
                     } else {
                         Toast.makeText(getBaseContext(), getResources().getString(R.string.error_deleting), Toast.LENGTH_LONG).show();
                     }
                 } else {
                     Log.e(TAG, "Nothing selected to delete");
                     Toast.makeText(getBaseContext(), getResources().getString(R.string.error_deleting), Toast.LENGTH_LONG).show();
                 }
             }
         });
         
         buttonTakeAnother.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 if (appState.getScribbler().isConnected()) {
                     Intent pictureIntent = new Intent(getBaseContext(), PictureActivity.class);
                     startActivity(pictureIntent);
                 } else {
                     MainTabWidget.emphasizeConnectivity();
                 }
             }
         });
     }  
     
     @Override
     protected void onResume() {
        super.onResume();
         
         if (fileList().length <= 0) {
             // let the user know there is nothing to see yet
             header.setText(getResources().getString(R.string.no_pictures));
             selectedName = "";
             buttonDelete.setVisibility(View.INVISIBLE);
             buttonSaveSD.setVisibility(View.INVISIBLE);
         } else {
             // Update the adapter
             adapter.notifyDataSetChanged();           
         }
 
     }
     
     public class ImageAdapter extends BaseAdapter {
         int mGalleryItemBackground;
         private Context mContext;
         
         public ImageAdapter(Context c) {
             mContext = c;
             TypedArray attr = mContext.obtainStyledAttributes(R.styleable.PictureGallery);
             mGalleryItemBackground = attr.getResourceId(
                     R.styleable.PictureGallery_android_galleryItemBackground, 0);
             attr.recycle();
         }
         
         public int getCount() {
             return fileList().length;
         }
 
         public Object getItem(int position) {
             return position;
         }
 
         public long getItemId(int position) {
             return position;
         }
         
         public View getView(int position, View convertView, ViewGroup parent) {
             ImageView imageView = new ImageView(mContext);
 
             imageView.setImageBitmap(BitmapFactory.decodeFile(getFilesDir().toString() + File.separator + fileList()[position].toString()));
             imageView.setLayoutParams(new Gallery.LayoutParams(256, 192));
             imageView.setBackgroundResource(mGalleryItemBackground);
 
             return imageView;
         }
     }
     
 }
