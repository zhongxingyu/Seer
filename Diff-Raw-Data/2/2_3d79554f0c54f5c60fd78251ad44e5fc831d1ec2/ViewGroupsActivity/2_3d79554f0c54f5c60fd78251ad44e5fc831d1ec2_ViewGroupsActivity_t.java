 package com.cs301w01.meatload.activities;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 
 import com.cs301w01.meatload.R;
 import com.cs301w01.meatload.controllers.GalleryManager;
 import com.cs301w01.meatload.controllers.MainManager;
 import com.cs301w01.meatload.controllers.PhotoManager;
 import com.cs301w01.meatload.model.Album;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 import android.widget.TextView;
 import android.widget.AdapterView.OnItemClickListener;
 
 
 public class ViewGroupsActivity extends Skindactivity {
 	
 	private MainManager mainManager;
 	ListView albumListView;
 	SimpleAdapter adapter;
 	
 	private int[] adapterIDs = { R.id.itemName, R.id.itemValue };
 	private String[] adapterCols = { "name", "numPhotos" };
 
     //@Override
     public void update(Object model) {
         //To change body of implemented methods use File | Settings | File Templates.
     	refreshScreen();
     }
     
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.viewgroups);
        mainManager = new MainManager();
         
         refreshScreen();
         
       //on click listener 
     	final Button takePicButton = (Button) findViewById(R.id.takePic);
     	takePicButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 // Perform action on click
             	takePicture();
             }
         });
         
         final Button addAlbumButton = (Button) findViewById(R.id.newAlbum);
         addAlbumButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 // Perform action on click
             	addAlbum();
             }
         });
         
         // Below is the listener for a list button click. It takes the id
         // of the clicked item and passes it to the FlogEdit activity so
         // you can edit the selected log.
         
         albumListView.setOnItemClickListener(new OnItemClickListener() {
             public void onItemClick(AdapterView<?> parent, View view,
                 int position, long id) {
              HashMap<String, String> temp = (HashMap<String, String>) adapter.getItem(position);
              String clickedName = temp.get("name");
              openGalleryFromAlbum(clickedName);
             }
         }
         );
     }
     
     @Override
     protected void onResume(){
     	super.onResume();
     	refreshScreen();
     }
     
     public void refreshScreen(){
     	albumListView = (ListView) findViewById(R.id.albumListView);
         
         ArrayList<HashMap<String, String>> albumList = mainManager.getAllAlbums();
         
         adapter = new SimpleAdapter(this, albumList, R.layout.list_item, adapterCols, adapterIDs);
 		albumListView.setAdapter(adapter);
     }
     
     /**TODO
      * Display a message asking if the user wants to create a new album
      * populate the boolean with true is the user wants a new album
      * otherwise assume the user wants to choose an album
      * if they hit cancel, exit out of this function
      */
     protected void takePicture(){
     	//Display prompt
     	boolean wantsNewAlbum = false;
     	String newAlbumName = "";
     	if(wantsNewAlbum){
     		//newAlbumName = addAlbum();
     	}
     	else{
     		/**TODO
     		 * Prompt the user to pick an album from the list
     		 * Populate this string with the name
     		 */
     		newAlbumName = "";
     	}
     	
     	/**TODO
     	 * we need to start the gallery activity then have the gallery activity
     	 * immediately switch to takePicture, so that when the user goes back, they'll
     	 * end up at the gallery activity.
     	 * Use openGalleryFromAlbum for this maybe?
     	 */
     	switchToTakePicture(newAlbumName);
     }
     
     private void switchToTakePicture(String album){
     	Intent myIntent = new Intent();
     	myIntent.setClassName("com.cs301w01.meatload", "com.cs301w01.meatload.activities.TakePictureActivity");
     	PhotoManager pMan = new PhotoManager(this, album);
     	myIntent.putExtra("manager", pMan);
     	
     	startActivity(myIntent); 
     }
     
     /**TODO
      * Prompt the user to enter an album name and pick a bunch of tags
      * When the user presses OK, gather the name and tags the user entered, 
      * and call photoManager.addNewAlbum();
      * @return the name of the new album (for use in takePicture)
      */
     private void addAlbum(){
     	//Alert code snippet taken from http://www.androidsnippets.com/prompt-user-input-with-an-alertdialog
 		AlertDialog.Builder alert = new AlertDialog.Builder(this);
 
 		alert.setTitle("New Album");
 		alert.setMessage("Enter the name of the new album");
 
 		// Set an EditText view to get user input 
 		final EditText input = new EditText(this);
 		alert.setView(input);
 
 		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 		public void onClick(DialogInterface dialog, int whichButton) {
 			  String newAlbumName = input.getText().toString();
 			  mainManager.addAlbum(newAlbumName, new ArrayList<String>());
 			  refreshScreen();
 		  }
 		});
 
 		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 		  public void onClick(DialogInterface dialog, int whichButton) {
 		    // Canceled.
 		  }
 		});
 
 		alert.show();
     }
    
     
     /**TODO
      * start a new gallery activity and pass it pMan
      * @param pMan - the PhotoManager object to be used in this activity
      */
     private void openGallery(GalleryManager gMan){
     	Intent myIntent = new Intent();
     	myIntent.setClassName("com.cs301w01.meatload", "com.cs301w01.meatload.activities.GalleryActivity");
     	myIntent.putExtra("manager", gMan);
     	
     	startActivity(myIntent); 
     }
     
     private void openGalleryFromAlbum(String albumName){
     	openGallery(new GalleryManager(albumName, this));
     }
     
     private void openGalleryFromTags(Collection<String> tags){
     	openGallery(new GalleryManager(tags, this));
     }
     
     private void openGalleryAllPhotos(){
     	openGallery(new GalleryManager(this));
     }
 }
