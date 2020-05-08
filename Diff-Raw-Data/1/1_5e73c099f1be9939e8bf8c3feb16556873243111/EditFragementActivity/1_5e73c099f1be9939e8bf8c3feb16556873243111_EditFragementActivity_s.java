 /*
 	Adventure App - Allows you to create an Adventure Book, or Download
  	books from other authors.
     Copyright (C) Fall 2013 Team 5 CMPUT 301 University of Alberta
 
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.uofa.adventure_app.activity;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.provider.MediaStore;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.uofa.adventure_app.R;
 import com.uofa.adventure_app.application.AdventureApplication;
 import com.uofa.adventure_app.interfaces.AdventureActivity;
 import com.uofa.adventure_app.model.Choice;
 import com.uofa.adventure_app.model.Fragement;
 import com.uofa.adventure_app.model.Media;
 import com.uofa.adventure_app.model.Story;
 /**
  * This class deals with users editing fragements.  It interacts with the majority of
  * the model and allows the user to access the camera and the existing photo library in
  * order to allow the user to add images to fragements.
  * 
  * @author Kevin Lafond, Joel Malina
  *
  */
 
 public class EditFragementActivity extends AdventureActivity {
 	View currentView;
 	Uri imageFileUri;
 	Bundle extras;
 	String title;
 	String user;
 	String body;
 	Uri chosenImageUri;
 	Story s;
 	Story currentStory;
 	Fragement currentFragement;
 	String s_id;
 	String old_frag;
 	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
 	private static final int PICK_IMAGE = 1111; 
 	boolean choice = false;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_edit_fragement);
 		Fragement currentFragement = AdventureApplication.getStoryController()
 				.currentFragement();
 		Story currentStory = AdventureApplication.getStoryController()
 		.currentStory();
 		currentView = this.findViewById(android.R.id.content);
 		TextView newauthor = (TextView) findViewById(R.id.newauthor);
 		// This is wrong.......
 		
 		if (!currentStory.users().contains(AdventureApplication.user())){
 			currentStory = AdventureApplication.getStoryController().currentStory().localCopy();
 			AdventureApplication.getStoryController().setCurrentStory(currentStory);
 			AdventureApplication.getStoryController().addStory(currentStory);
 			AdventureApplication.getStoryController().saveStories();
 			currentStory.addUser(AdventureApplication.user());
 		}
 		String authors = "Author: " + currentStory.users().get(0).toString();
 		if (currentStory.users().size() > 1){
 			authors += "\nEdited by: ";
 		}
 		for(int i = 1; i<currentStory.users().size(); i++){
 			authors +=  currentStory.users().get(i);
 			if (i != currentStory.users().size()-1 ){
 				authors  += ", ";
 			}
 		}
 		newauthor.setText(authors);
 		EditText newTitle = (EditText) findViewById(R.id.newtitle);
 		newTitle.setText(currentFragement.getTitle());
 		EditText newBody = (EditText) findViewById(R.id.newbody);
 		newBody.setText(currentFragement.body());
 		currentFragement = AdventureApplication.getStoryController()
 				.currentFragement();
 		// Generic Watcher Title
 		newTitle.addTextChangedListener(new GenericTextWatcher(newTitle));
 		newBody.addTextChangedListener(new GenericTextWatcher(newBody));
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.edit, menu);
 		return true;
 	}
 
 	// We want to create a context Menu when the user long click on an item
 	/**
 	 * Opens the menu of possible choices to add to the Fragment.
 	 * 
 	 * @param View v
 	 */
 	public void openChoices(View v) {
 		choice = true;
 		registerForContextMenu(v);
 		openContextMenu(v);
 
 	}
 
 	/**
 	 * Updates the view.
 	 */
 	public void updateView() {
 
 	}
 
 	@Override
 	public void dataReturn(ArrayList result, String method) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle presses on the action bar items
 		switch (item.getItemId()) {
 		case R.id.addmedia:
 			openMediaContext(currentView);
 			break;
 		case R.id.save:
 			save();
 			break;
 		case R.id.help:
 			String helpText = new String();
 			helpText="Touch Add Media, to add images to the fragement\n\n";
 			helpText=helpText+"Touch save, to save the fragement\n\n";
 			helpText=helpText+"Touch Add choice to add a fragement as a choice\n\n";
 			Toast.makeText(this, helpText, Toast.LENGTH_LONG).show();
 			break;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	/**
 	 * Method that is called to open the context view to allow the user to open
 	 * the camera or choose an existing piece of media.
 	 * 
 	 * @param View v
 	 */
 	public void openMediaContext(View v) {
 		choice = false;
 		registerForContextMenu(v);
 		openContextMenu(v);
 	}
 
 	// We want to create a context Menu when the user long click on an item
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v,
 			ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu(menu, v, menuInfo);
 
 		menu.clearHeader();
 		menu.clear();
 		// Style our context menu
 		menu.setHeaderIcon(android.R.drawable.ic_input_get);
 		menu.setHeaderTitle("Options");
 		MenuInflater inflater = getMenuInflater();
 		if (choice == false) {
 			// Open Menu
 			inflater.inflate(R.menu.addphoto, menu);
 		} else {
 			menu.clearHeader();
 			menu.clear();
 			// menu.removeGroup(R.id.annotategroup);
 			// Style our context menu
 			menu.setHeaderIcon(android.R.drawable.ic_input_get);
 			menu.setHeaderTitle("Add a Choice");
 			int counter = 0;
 			for (int j = 0; j < AdventureApplication.getStoryController()
 					.currentStory().getFragements().size(); j++) {
 				if (!AdventureApplication.getStoryController().currentStory().getFragements().get(j).equals(AdventureApplication.getStoryController().currentFragement())) {
 					Choice aChoice = new Choice(AdventureApplication.getStoryController().currentStory().getFragements().get(j));
 					if(!AdventureApplication.getStoryController().currentFragement().choices().contains(aChoice)) {
 					menu.add(0, counter, 0, "Add: "
 							+ AdventureApplication.getStoryController()
 									.currentStory().getFragements().get(j)
 									.getTitle());
 					}
 				}
 				counter++;
 			}
 			inflater.inflate(R.menu.createchoice, menu);
 			// choice = false;
 		}
 	}
 
 	/**
 	 * Takes the photo with the camera
 	 */
 	public void takeAPhoto() {
 		 Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
 	        
 	     String folder = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Adventure_App";
 	     File folderF = new File(folder);
 	     
 	     if (!folderF.exists()) {
 	            folderF.mkdir();
 	     }
 	        
 	     String imageFilePath = folder + "/" + "Adventure_App" + String.valueOf(System.currentTimeMillis()) + "jpg";
 	     File imageFile = new File(imageFilePath);
 	     imageFileUri = Uri.fromFile(imageFile);
 	     intent.putExtra(MediaStore.EXTRA_OUTPUT, imageFileUri);
 		startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
 	}
 
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
 			// TextView tv = (TextView) findViewById(R.id.status);
 			if (resultCode == RESULT_OK) {
 				System.out.println("Photo OK!");
                 try {
                     Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                                     this.getContentResolver(), imageFileUri);
                     Bitmap resizedBitmap = Media.resizeImage(bitmap);
                     String image = Media.encodeToBase64(resizedBitmap);
 
                     AdventureApplication.getStoryController().currentFragement().addMedia(new Media(image));
                 } catch (FileNotFoundException e) {
                 	e.printStackTrace();
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
 			} else if (resultCode == RESULT_CANCELED) {
 				System.out.println("Photo canceled");
 			} else {
 				System.out.println("Not sure what happened!" + resultCode);
 			}
 		}
 		 // handles selecting an image from app of users choice (usually gallery).
         if ((requestCode == PICK_IMAGE) && (resultCode == RESULT_OK) && (data != null))
         {
             chosenImageUri = data.getData();
 
             
             
             String folder = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Adventure_App/";
             File folderF = new File(folder);
    	     
             if (!folderF.exists()) {
    	            folderF.mkdir();
             }
 
             try {
             // copyfile from gallery location to our app!
                 Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                         this.getContentResolver(), chosenImageUri);
                 Bitmap resizedBitmap = Media.resizeImage(bitmap);
                 String image = Media.encodeToBase64(resizedBitmap);
                 AdventureApplication.getStoryController().currentFragement().addMedia(new Media(image));
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
   
         }
 	}
 
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		if (item.getGroupId() == 0 && item.getItemId() != R.id.cancel
 				&& item.getItemId() != R.id.takepic
 				&& item.getItemId() != R.id.choosemedia
 				&& item.getItemId() != R.id.takepic
 				&& item.getItemId() != R.id.newchoice
 				&& item.getItemId() != R.id.randomchoice) {
 			// TODO: This needs to be refactored....
 			Choice choice = new Choice(AdventureApplication
 					.getStoryController().currentStory().getFragements()
 					.get(item.getItemId()));
 			AdventureApplication.getStoryController().currentFragement()
 					.addChoice(choice);
 			AdventureApplication.getStoryController().saveStories();
 		} else {
 
 			switch (item.getItemId()) {
 			case R.id.takepic:
 				takeAPhoto();
 				break;
 			case R.id.choosemedia:
 				chooseImage();
 				break;
 			case R.id.newchoice:
 				save();
 				newChoice();
 				break;
 			case R.id.randomchoice:
 				AdventureApplication.getStoryController().currentFragement()
 						.setRandomFlag(true);
 				AdventureApplication.getStoryController().saveStories();
 
 				break;
 			default:
 				return super.onContextItemSelected(item);
 
 			}
 		}
 		return super.onContextItemSelected(item);
 
 	}
     /**
      * creates a new fragement and adds it to the current fragment as a choice.
      */
 	private void newChoice() {
 		Fragement currentFragement = AdventureApplication.getStoryController()
 				.currentFragement();
 		Fragement newFragement = new Fragement();
 		Choice newChoice = new Choice(newFragement);
 		
 		AdventureApplication.getStoryController().currentStory().addFragement(newFragement);
 		
 		AdventureApplication.getStoryController().currentFragement()
 				.addChoice(newChoice);
 		AdventureApplication.getStoryController().addPreviousFragement(
 				currentFragement);
 		AdventureApplication.getStoryController().setCurrentFragement(
 				newFragement);
 		
 		//if (!AdventureApplication.getStoryController().currentStory().getFragements().contains(currentFragement))
 
 		EditText newTitle = (EditText) findViewById(R.id.newtitle);
 		newTitle.setText("");
 		EditText newBody = (EditText) findViewById(R.id.newbody);
 		newBody.setText("");
 	}
 	/**
 	 * Saves the users current work done.
 	 */
 	public void save() {
 		EditText newTitle = (EditText) findViewById(R.id.newtitle);
 		// EditText newAuthor = (EditText) findViewById(R.id.newauthor);
 		EditText newBody = (EditText) findViewById(R.id.newbody);
 
 		// Update the current window fragement
 		// We should setup a text listner, and do this automatically, this is
 		// clunky.
 		Fragement currentFragement = AdventureApplication.getStoryController()
 				.currentFragement();
 		currentFragement.setBody(newBody.getText().toString());
 		currentFragement.setTitle(newTitle.getText().toString());
 
 		AdventureApplication.getStoryController().saveStories();
 
 	}
 	/**
 	 * opens the last fragement visited.
 	 */
 	protected void openLastFragement() {
 		Fragement currentFragement = AdventureApplication.getStoryController()
 				.lastFragement();
 		AdventureApplication.getStoryController().setCurrentFragement(
 				currentFragement);
 		AdventureApplication.getStoryController().popPreviousFragement();
 		EditText newTitle = (EditText) findViewById(R.id.newtitle);
 		// EditText newAuthor = (EditText) findViewById(R.id.newauthor);
 		EditText newBody = (EditText) findViewById(R.id.newbody);
 		newTitle.setText(currentFragement.getTitle());
 		newBody.setText(currentFragement.body());
 	}
 
 	protected void saveTextForView(View v, String text) {
 
 		switch (v.getId()) {
 		case R.id.newtitle:
 			AdventureApplication.getStoryController().currentFragement()
 					.setTitle(text);
 			break;
 		case R.id.newbody:
 			AdventureApplication.getStoryController().currentFragement()
 					.setBody(text);
 			break;
 		default:
 			break;
 		}
 
 		AdventureApplication.getActivityController().update();
 
 	}
 
 
 	@Override
 	public void onBackPressed() {
 		// TODO Auto-generated method stub
 		super.onBackPressed();
 		AdventureApplication.getStoryController().saveStories();
 		
 	}
 	
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 	    if (keyCode == KeyEvent.KEYCODE_BACK) {
 	        AdventureApplication.getStoryController().saveStories();
 	        return super.onKeyDown(keyCode, event);
 	    }
 	    return super.onKeyDown(keyCode, event);
 	}
 	/**
 	 * called when the user chooses an existing photo and adds it to the fragement
 	 * passes the chosen fragement to the onActivityResult method.
 	 */
 	  public void chooseImage()
 	    {
 	    	Intent pickImage = new Intent();
 	    	pickImage.setType("image/*");
 	    	pickImage.setAction(Intent.ACTION_GET_CONTENT);
 	    	startActivityForResult(Intent.createChooser(pickImage, "Select Picture"), PICK_IMAGE);
 	    }
 
 
 
 }
