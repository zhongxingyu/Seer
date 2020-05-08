 package com.cs301w01.meatload.activities;
 
 import com.cs301w01.meatload.R;
 import com.cs301w01.meatload.controllers.GalleryManager;
 import com.cs301w01.meatload.controllers.PhotoManager;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 import android.widget.TextView;
 
 public class GalleryActivity extends Skindactivity {
 	
 	
 	ListView photoListView;
 	SimpleAdapter adapter;
 	
 	GalleryManager galleryManager;
 	
 	/** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.gallery);  //That xml doesn't exist yet.
         
         Bundle b = getIntent().getExtras();
         galleryManager = (GalleryManager) b.getSerializable("manager");
         
         TextView albumTitle = (TextView) findViewById(R.id.albumTitle);
         albumTitle.setText(galleryManager.getTitle());
 
         galleryManager = new GalleryManager(this);
         
         /**TODO
          * map objects created as variables to real objects in the XML R.layout.main
          */
         final Button editAlbumButton = (Button) findViewById(R.id.editAlbum);
         editAlbumButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				setResult(RESULT_OK);
 				editAlbum();
 			}
 		});
         final Button takePictureButton = (Button) findViewById(R.id.takePic);
         takePictureButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				setResult(RESULT_OK);
 				takePicture();
 			}
 		});
         
         /**TODO
          * we should add all the other listeners here.
          */
         
         /**TODO
          * should we call update here... we probably want to update the screen right away
          */
     }
 
     @Override
     public void update(Object model) {
         
     }
     
     private void editAlbum(){
     	
     }
     
     private void takePicture(){
     	
     }
 }
