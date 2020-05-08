 package com.cs301w01.meatload.activities;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 
 import android.app.Dialog;
 import android.content.Intent;
 import android.view.View;
 import com.cs301w01.meatload.R;
 import com.cs301w01.meatload.adapters.AlbumAdapter;
 import com.cs301w01.meatload.controllers.MainManager;
 import com.cs301w01.meatload.controllers.PictureManager;
 import com.cs301w01.meatload.model.Album;
 import com.cs301w01.meatload.model.Picture;
 
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.Spinner;
 import android.widget.TextView;
 
 /**
  * Takes a picture and displays it in exploded view along with important metadata including tags, 
  * date, etc.
  * <p>
  * Gives the user an exploded view of the picture being edited.
  * <p>
  * Allows user to change certain metadata such as tags and album.
  * @author Blake Bouchard
  */
 public class EditPictureActivity extends Skindactivity {
 
 	private MainManager mainManager;
 	private PictureManager pictureManager;
 	private Picture picture;
 
 	@Override
 	public void update(Object model) {
 		
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.edit_picture);
 		
 		// Set up MainManager
 		mainManager = new MainManager();
 		mainManager.setContext(this);
 		
 		// Get picture object from Intent's extras bundle
 		Intent intent = getIntent();
 		Bundle extras = intent.getExtras();
 		picture = (Picture) extras.getSerializable("picture");
         
 		// Set up a new PictureManager using the Picture object passed via the intent 
 		pictureManager = new PictureManager(this, picture);
 
 		// TODO: DELETE THESE LINES WHEN picture.getTags() is working!!!
 		ArrayList<String> testTagList = new ArrayList();
 		testTagList.add("brown");testTagList.add("moldy");testTagList.add("bruise");
 		testTagList.add("taco sauce");testTagList.add("herp");testTagList.add("derp");
 		//END DELETE, AND UPDATE ARGS IN FOLLOWING FUNCTION CALL
 		
 		populateTextFields(picture.getAlbumName(),
                 picture.getDate().toString(),
                 picture.getPath(),
                 testTagList);
 
 		createListeners();
 	}
 	
 	@Override
     protected void onResume() {
     	super.onResume();
     	pictureManager.setContext(this);
     }
 	
 	/**
 	 * Fills the text and image fields on the screen with a current picture.
 	 */
 	
 	//USED http://codehenge.net/blog/2011/05/customizing-android-listview-item-layout/
 	protected void populateTextFields(String albumName, String date, String path, Collection<String> tags) {
 		// Set pictureView to path provided by Picture object
 		ImageView pictureView = (ImageView) findViewById(R.id.pictureView);
 		pictureView.setImageDrawable(Drawable.createFromPath(path));
 		
 		// Set dateView to toString representation of Date in Picture object
 		TextView dateView = (TextView) findViewById(R.id.dateView);
 		dateView.setText(date);
 		
 		// Set albumView to string representation of Album in Picture object
 		// TODO: This spinner also needs to be populated with other albums in drop down!!
 		Spinner albumView = (Spinner) findViewById(R.id.albumView);
 		albumView.setTag(albumName);
 		
 		// TODO: populate picTags with this pictures Tags
 		ListView tagListView = (ListView) findViewById(R.id.picTagList);
 		Iterator<String> tagIter = tags.iterator();
 		ArrayList<String> tagList = new ArrayList<String>();
 		while(tagIter.hasNext()){
 			tagList.add(tagIter.next());
 		}
 		ArrayAdapter<String> arrAdapt = new ArrayAdapter<String>(this, R.layout.tag_list_item, tagList);
 		tagListView.setAdapter(arrAdapt);
 		
 	}
 
 	protected void createListeners() {
 		//JOEL DELETED THIS BUTTON, THE BELOW CODE CAN EVENTUALLY BE DELETED
 		//IF WE DO IN FACT NEVER USE THE BUTTON
 		/*
 		Button changeAlbumButton = (Button) findViewById(R.id.changeAlbumButton);
 		// TODO: Add Change Album functionality to EditPicture
 		changeAlbumButton.setOnClickListener(new View.OnClickListener() {
 			
 			public void onClick(View view) {
 				openChangeAlbumDialog();
 			}
 		});
 		
         Button sendEmailButton = (Button) findViewById(R.id.sendEmailButton);
         
         sendEmailButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View view) {
                 openSendEmailActivity();
             }
         });
         */
         
         //Edit Tags button
         Button editTagsButton = (Button) findViewById(R.id.editTagsButton);
         
         editTagsButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View view) {
                 openEditTagsDialogue();
             }
         });
 	}
 	private void openEditTagsDialogue(){
 		
 	}
 	
 	/**
 	 * Opens a dialog that will allow the user to select an album from a list of album names,
 	 * which will move the selected picture to that album.
 	 */
 	private void openChangeAlbumDialog() {
 		final Dialog changeAlbumDialog = new Dialog(this);
 		changeAlbumDialog.setContentView(R.layout.change_album);
 		changeAlbumDialog.setCancelable(true);
 		
 		ArrayList<Album> albumList = mainManager.getAllAlbums();
 		AlbumAdapter adapter = new AlbumAdapter(this, R.layout.list_item, albumList);
 		
 		ListView albumListView = (ListView) findViewById(R.id.albumListView);
 		albumListView = (ListView) findViewById(R.id.changeAlbumListView);
 		albumListView.setAdapter(adapter);
 		albumListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
 		
 		Button moveToAlbumButton = (Button) findViewById(R.id.moveToAlbumButton);
 		moveToAlbumButton.setOnClickListener(new View.OnClickListener() {
 			
 			public void onClick(View v) {
 				// TODO: Make this move the picture into the selected album.
 				changeAlbumDialog.dismiss();
 			}
 		});
 		
 		Button cancelDialogButton = (Button) findViewById(R.id.cancelDialogButton);
 		cancelDialogButton.setOnClickListener(new View.OnClickListener() {
 			
 			public void onClick(View v) {
 				changeAlbumDialog.dismiss();
 			}
 		});
 		
 		changeAlbumDialog.show();
 	}
     
     private void openSendEmailActivity() {
         
         Intent sendEmail = new Intent();
         sendEmail.setClassName("com.cs301w01.meatload", 
         		"com.cs301w01.meatload.activities.SendEmailActivity");
         sendEmail.putExtra("picture", picture);
         
         startActivity(sendEmail);
 
     }
     
 }
