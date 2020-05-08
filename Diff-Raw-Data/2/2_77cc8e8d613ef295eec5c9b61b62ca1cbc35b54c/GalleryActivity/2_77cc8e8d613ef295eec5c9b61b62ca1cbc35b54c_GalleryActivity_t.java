 package com.cs301w01.meatload.activities;
 
 
 import java.util.Collection;
 
 import android.widget.*;
 import com.cs301w01.meatload.R;
 import com.cs301w01.meatload.adapters.GridViewGalleryAdapter;
 import com.cs301w01.meatload.controllers.GalleryManager;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.AdapterView.OnItemClickListener;
 import com.cs301w01.meatload.model.AlbumGallery;
 import com.cs301w01.meatload.model.GalleryData;
 import com.cs301w01.meatload.model.Picture;
 import com.cs301w01.meatload.model.querygenerators.PictureQueryGenerator;
 
 /**
 * Shows all pictures in a gridview as denoted by the GalleryManager object passed in to the
 * Activity through the Intent Extras with key "manager".
 * @author Isaac Matichuk
 * @see GalleryManager
 */
 public class GalleryActivity extends Skindactivity {
 
     //private ListView pictureListView;
     //private SimpleAdapter adapter;
 
     private GridView gridview;
     private GridViewGalleryAdapter adapter;
 
     private GalleryManager galleryManager;
 
     // private int[] adapterIDs = { R.id.itemName, R.id.itemValue };
     // private String[] adapterCols = { "date", "id" };
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         setContentView(R.layout.gallery);
 
         Bundle b = getIntent().getExtras();
         GalleryData gallerydata = (GalleryData) b.getSerializable("gallery");
         galleryManager = new GalleryManager(gallerydata);
         galleryManager.setContext(this);
 
         adapter = new GridViewGalleryAdapter(this, galleryManager.getPictureGallery());
 
         gridview = (GridView) findViewById(R.id.viewAlbumGridView);
         gridview.setAdapter(adapter);
                 
         populateTextFields(galleryManager.getTitle());
         if(!galleryManager.isAlbum()){
         	hideButtons();
         }
 
         refreshScreen();
         
     }
     
     protected void populateTextFields(String title){
 
         TextView albumTitle = (TextView) findViewById(R.id.albumTitle);
         albumTitle.setText(title);
 
     }
     
     protected void hideButtons(){
     	 final Button editAlbumButton = (Button) findViewById(R.id.editAlbum);
     	 editAlbumButton.setEnabled(false);
     	 editAlbumButton.setVisibility(Button.INVISIBLE);
     	 
     	 final Button takePictureButton = (Button) findViewById(R.id.takePic);
     	 takePictureButton.setEnabled(false);
     	 takePictureButton.setVisibility(Button.INVISIBLE);
     }
     
     protected void createListeners(){
         // TODO: Map objects created as variables to real objects in the XML R.layout.main
 
         final Button editAlbumButton = (Button) findViewById(R.id.editAlbum);
         editAlbumButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 setResult(RESULT_OK);
                 editAlbum(galleryManager);
             }
         });
 
         final Button takePictureButton = (Button) findViewById(R.id.takePic);
         takePictureButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 setResult(RESULT_OK);
                 takePicture();
             }
         });
 
        final Button comparePictureButton = (Button) findViewById(R.id.comparePic);
        
         gridview.setOnItemClickListener(new OnItemClickListener() {
             public void onItemClick(AdapterView parent, View v, int position, long id) {
                 Picture selectedPic = adapter.getItem(position);
                 Log.d("Info For PictureListener", selectedPic.toString());
                 int pictureID = selectedPic.getPictureID();
                 openPicture(pictureID);
             }
         });
     }
     
     @Override
     protected void onResume() {
         super.onResume();
 
      galleryManager.setContext(this);
         //galleryManager.updateAlbum();
 
         refreshScreen();
     }
 
     @Override
     public void update(Object model) {
 
     }
     
     /**
 * @see GalleryManager
 */
     public void refreshScreen() {
     
      //TODO: MERGE WITH UPDATE
 
         adapter.notifyDataSetInvalidated();
 
         createListeners();
 
         Collection<Picture> albumPictures = galleryManager.getPictureGallery();
 
         //adapter = new SimpleAdapter(this, albumPictures, R.layout.list_item, adapterCols, adapterIDs);
         adapter = new GridViewGalleryAdapter(this, albumPictures);
 
         gridview.setAdapter(adapter);
 
 
     }
     
     private void editAlbum(GalleryManager gm) {
     	
     	AlbumGallery aGal = (AlbumGallery) galleryManager.getGallery();
 
      //Launch the EditAlbumActivity with a given GalleryManager
      Intent myIntent = new Intent();
      myIntent.setClassName("com.cs301w01.meatload",
              "com.cs301w01.meatload.activities.EditAlbumActivity");
      Log.d("GalleryActivity", "EDITING ALBUM, NAME:" + aGal.getAlbum().getName());
     
      //NEED TO SET TAGS AS WELL!
     
      myIntent.putExtra("gridview", aGal);
     
      startActivity(myIntent);
 
     }
     
     /**
 * Starts a new TakePictureActivity using the Album referred to by the GalleryManager object
 * in the GalleryActivity state. Can only be used if a true album is selected.
 * @see TakePictureActivity
 */
     private void takePicture() {
 
         Intent myIntent = new Intent();
         
         AlbumGallery aGal = (AlbumGallery) galleryManager.getGallery();
     
         myIntent.setClassName("com.cs301w01.meatload",
              "com.cs301w01.meatload.activities.TakePictureActivity");
 
         Log.d("Taking Picture", "ALBUM NAME:" + aGal.getAlbum().getName());
 
         myIntent.putExtra("album", aGal.getAlbum());
 
         startActivity(myIntent);
 
     }
     
     /**
 * Starts a new EditPictureActivity using the Picture object referred to by the pictureID
 * argument.
 * <p>
 * Passes a PictureManager as part of the Intent.
 * @param pictureID The tuple ID of the picture to be opened.
 */
     private void openPicture(int pictureID) {
 
         Intent myIntent = new Intent();
         myIntent.setClassName("com.cs301w01.meatload",
         "com.cs301w01.meatload.activities.EditPictureActivity");
         myIntent.putExtra("picture", new PictureQueryGenerator(this).selectPictureByID(pictureID));
 
         startActivity(myIntent);
     }
 
 
 
 }
