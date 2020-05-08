 /**
  * Copyright 2013 Alex Wong, Ashley Brown, Josh Tate, Kim Wu, Stephanie Gil
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package ca.ualberta.cs.c301f13t13.gui;
 
 import java.io.File;
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.provider.MediaStore;
 import android.view.Gravity;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.Toast;
 import ca.ualberta.cmput301f13t13.storyhoard.R;
 import ca.ualberta.cs.c301f13t13.backend.Chapter;
 import ca.ualberta.cs.c301f13t13.backend.Choice;
 import ca.ualberta.cs.c301f13t13.backend.Media;
 import ca.ualberta.cs.c301f13t13.backend.ObjectType;
 import ca.ualberta.cs.c301f13t13.backend.SHController;
 import ca.ualberta.cs.c301f13t13.backend.Story;
 import ca.ualberta.cs.c301f13t13.backend.Utilities;
 
 //import android.view.Menu; *Not sure if needed
 
 /**
  * Add Chapter Activity
  * 
  * Purpose: - To add a chapter to an existing story. - The author can: - Add
  * images through the use of the image button - Set the text of the chapter
  * through the Edit text space - View all chapters in the story upon pressing
  * the 'View All Chapters' button - Add a choice by pressing the 'Add Choice'
  * button. - This activity will also display the choices that exist or have been
  * added.
  * 
  * author: Alexander Wong
  */
 
 public class EditChapterActivity extends Activity {
 
 	private Context context = this;
 	private Chapter chapt;
 	private Story story;
 	private ArrayList<Choice> choices = new ArrayList<Choice>();
 	private Button saveButton;
 	private Button addIllust;
 	private Button addChoice;
 	private ListView viewChoices;
 	private EditText chapterContent;
 	private boolean isEditing;
 	private SHController gc;
 	private AdapterChoices choiceAdapter;
 	private AlertDialog illustDialog;
 //	private ImageView illustration;
 	private ArrayList<Media> photoList;
 	private ArrayList<Media> illList;
 	private LinearLayout illustrations;
 //	private LinearLayout photos;
 	private static int BROWSE_GALLERY_ACTIVITY_REQUEST_CODE = 1;
 	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
 	Uri imageFileUri;	
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_edit_chapter);
 
 		chapterContent = (EditText) findViewById(R.id.chapterEditText);
 		saveButton = (Button) findViewById(R.id.chapterSaveButton);
 		addChoice = (Button) findViewById(R.id.addNewChoice);
 		viewChoices = (ListView) findViewById(R.id.chapterEditChoices);
 		addIllust = (Button) findViewById(R.id.chapterAddIllust);
 //		illustration = (ImageView) findViewById(R.id.chaptIllust);
 		gc = SHController.getInstance(getBaseContext());
 		
 		illustrations = (LinearLayout) findViewById(R.id.horizontalIllustraions2);
 //		photos = (LinearLayout) findViewById(R.id.horizontalPhotos2);	
 
 		// Get the story that chapter is being added to
 		Bundle bundle = this.getIntent().getExtras();
 		isEditing = bundle.getBoolean("isEditing");
 		if (isEditing) {
 			story = (Story) bundle.get("Story");
 			chapt = (Chapter) bundle.get("Chapter");
 		} else {
 			story = (Story) bundle.get("New Story");
 			chapt = new Chapter(story.getId(), "");
 		}
 		
 		// Setup the adapter
 		choiceAdapter = new AdapterChoices(this, R.layout.browse_choice_item,
 				choices);
 		viewChoices.setAdapter(choiceAdapter);
 
 		// Save the chapter to the database, or update if editing
 		saveButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				chapt.setText(chapterContent.getText().toString());
 				if (isEditing) {
 					gc.updateObject(chapt, ObjectType.CHAPTER);
 				} else {
 					story.addChapter(chapt);
 					gc.addObject(story, ObjectType.CREATED_STORY);
 					gc.addObject(chapt, ObjectType.CHAPTER);
 				}
 				finish();
 			}
 		});
 
 		addIllust.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				AlertDialog.Builder alert = new AlertDialog.Builder(context);
 
 				// Set dialog title
 				alert.setTitle("Choose method:");
 
 				// Options that user may choose to add illustration
 				final String[] methods = { "Take Photo", "Choose from Gallery" };
 
 				alert.setSingleChoiceItems(methods, -1,
 						new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int item) {
 						switch (item) {
 						case 0:
 							takePhoto();
 							break;
 						case 1:
 							browseGallery();
 							break;
 						}
 						illustDialog.dismiss();
 					}
 				});
 				illustDialog = alert.create();
 				illustDialog.show();
 				// chapt.addIllustration(null);
 			}
 
 		});
 
 		// Add a choice to this chapter
 		addChoice.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				if (isEditing) {
 					Intent intent = new Intent(getBaseContext(),
 							EditChoiceActivity.class);
 					intent.putExtra("chapter", chapt);
 					intent.putExtra("story", story);
 					startActivity(intent);
 				} else {
 					Toast.makeText(getBaseContext(),
 							"Save story before adding first choice",
 							Toast.LENGTH_SHORT).show();
 				}
 			}
 		});
 		// Set the chapter text, if new Chapter will simply be blank
 		chapterContent.setText(chapt.getText());
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		choices.clear();
 		choices.addAll(gc.getAllChoices(chapt.getId()));
 		choiceAdapter.notifyDataSetChanged();
 		
 		// Getting illustrations
 		illList = gc.getAllIllustrations(chapt.getId());
 
 		// Not sure if photos need to be displayed here?
 		
 		// Insert Illustrations
 		for (Media ill : illList) {
 			illustrations.addView(insertImage(ill));
 		}					
 	}
 
 	/**
 	 * CODE REUSE URL:
 	 * http://android-er.blogspot.ca/2012/07/implement-gallery-like.html Date:
 	 * Nov. 7, 2013 Author: Andr.oid Eric
 	 */
 	public View insertImage(Media ill) {
 		Bitmap bm = Utilities.decodeSampledBitmapFromUri(ill.getUri(), 220, 220);
 		LinearLayout layout = new LinearLayout(getApplicationContext());
 
 		layout.setLayoutParams(new LayoutParams(250, 250));
 		layout.setGravity(Gravity.CENTER);
 
 		ImageView imageView = new ImageView(getApplicationContext());
 		imageView.setLayoutParams(new LayoutParams(220, 220));
 		imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
 		imageView.setImageBitmap(bm);
 
 		layout.addView(imageView);
 		return layout;
 	}	
 
 	/**
 	 * Code for taking a photo
 	 */
 	public void takePhoto() {
 		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
 
 		String folder =
 				Environment.getExternalStorageDirectory().getAbsolutePath() + "/tmp";
 		File folderF = new File(folder);
 		if (!folderF.exists()) {
 			folderF.mkdir();
 		}
 
 		String imageFilePath = folder + "/" +
 				String.valueOf(System.currentTimeMillis()) + "jpg";
 		File imageFile = new File(imageFilePath);
 		imageFileUri = Uri.fromFile(imageFile);
 
 		intent.putExtra(MediaStore.EXTRA_OUTPUT, imageFileUri);
 		startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
 	}
 
 	/** 
 	 * Code for browsing the gallery
 	 */
 	public void browseGallery() {
 		Intent intent = new Intent(Intent.ACTION_PICK,
 				android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
 
 		String folder =
 				Environment.getExternalStorageDirectory().getAbsolutePath() + "/tmp";
 		File folderF = new File(folder);
 		if (!folderF.exists()) {
 			folderF.mkdir();
 		}
 
 		String imageFilePath = folder + "/" +
 				String.valueOf(System.currentTimeMillis()) + "jpg";
 		File imageFile = new File(imageFilePath);
 		imageFileUri = Uri.fromFile(imageFile);
 
 		intent.putExtra(MediaStore.EXTRA_OUTPUT, imageFileUri);
 
 		startActivityForResult(intent, BROWSE_GALLERY_ACTIVITY_REQUEST_CODE);
 
 	}
 	
 	protected void onActivityResult(int requestCode, int resultCode, Intent
 			data) {
 		if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE 
 				|| requestCode == BROWSE_GALLERY_ACTIVITY_REQUEST_CODE) {
 			if (resultCode == RESULT_OK) {
 
 				Media ill = new Media(chapt.getId(), imageFileUri, Media.ILLUSTRATION);
 				gc.addObject(ill, ObjectType.MEDIA);
				illustrations.addView(insertImage(ill));
 
 			} else if (resultCode == RESULT_CANCELED) {
 				System.out.println("cancelled taking a photo" );
 			} else {
 				System.err.println("Error in taking a photo" + resultCode);
 			}
 		} 
 	}	
 }
 
