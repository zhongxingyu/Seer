 /*
  * Copyright (C) <2013>  <Terence Yin Kiu Leung>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package c301.AdventureBook;
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.Bundle;
 import android.util.Base64;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 import c301.AdventureBook.Controllers.StoryManager;
 
 import com.example.adventurebook.R;
 
 /**
  * The create story activity allows the author to create a new story. When the
  * story is created successfully, it is stored as a file in the phone's internal
  * storage. *
  * 
  * @author Terence
  * @author Justin Hoy - Minor Editor
  * @author Zhao Zhang - Minor Editor
  */
 public class CreateStoryActivity extends Activity {
 	private static final int ACTIVITY_EDIT_STORY = 0;
 
 	private static final int PHOTO_ACTIVITY_REQUEST = 1001;	
 
 	private EditText mStoryTitle;
 	private EditText mStoryAuthor;
 	private EditText mStoryDescription;
 	private TextView mDate;
 	
 	StoryManager sManagerInst;
 
 	String storyTitle;
 	String storyAuthor;
 	String storyDescription;
 	String formattedDate;
 	ImageView image;
 	String show_path;
 	String imageByte;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.create_story);
 
 		image = (ImageView) findViewById(R.id.imageView1);
 		mStoryTitle = (EditText) findViewById(R.id.editStoryTitle);
 		mStoryDescription = (EditText) findViewById(R.id.editStoryDescription);
 		mStoryAuthor = (EditText) findViewById(R.id.authorText);
 		Button createStoryButton = (Button) findViewById(R.id.createStoryButton);
 
 		setDate();
 		image.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				Intent intent = new Intent(getApplicationContext(),
 						TakePhotoActivity.class);
 				startActivityForResult(intent, PHOTO_ACTIVITY_REQUEST);
 			}
 		});
 		createStoryButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View view) {
 				createStory();
 			}
 		});
 	}
 
 	/**
 	 * Set the story creation date
 	 */
 	@SuppressLint("SimpleDateFormat")
 	private void setDate() {
 		mDate = (TextView) findViewById(R.id.dateText);
 		Calendar c = Calendar.getInstance();
 		SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
 		formattedDate = df.format(c.getTime());
 		mDate.setText("Date: " + formattedDate);
 	}
 
 	/**
 	 * Creates a new story and starts the edit story activity.
 	 */
 	private void createStory() {
 		getUserText();
 		
 		sManagerInst = StoryManager.getInstance();
 		sManagerInst.initContext(this);
 		
 		boolean saveSuccess = sManagerInst.createStory(storyTitle, storyDescription, storyAuthor, 
 			 formattedDate, show_path, imageByte, false);
 
 		if (saveSuccess == true) {
 			Toast.makeText(this, "Story Created: " + storyTitle,
 					Toast.LENGTH_LONG).show();
 			Intent i = new Intent(this, EditStoryActivity.class);
 			startActivityForResult(i, ACTIVITY_EDIT_STORY);
			finish();
 		} else {
			
 			// 1. Instantiate an AlertDialog.Builder with its constructor
 			AlertDialog.Builder builder = new AlertDialog.Builder(
 					CreateStoryActivity.this);
 
 			// Add the buttons
 			builder.setNegativeButton(R.string.cancel,
 					new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog, int id) {
 							// User clicked Cancel button
 						}
 					});
 
 			builder.setPositiveButton(R.string.ok,
 					new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog, int id) {
 							
 							sManagerInst.createStory(storyTitle, storyDescription, storyAuthor, 
 									 formattedDate, show_path, imageByte, true);
 							
 							Toast.makeText(CreateStoryActivity.this,
 									"Story Created: " + storyTitle,
 									Toast.LENGTH_LONG).show();
 							Intent i = new Intent(CreateStoryActivity.this,
 									EditStoryActivity.class);
 							startActivityForResult(i, ACTIVITY_EDIT_STORY);
 						}
 					});
 
 			// 2. Chain together various setter methods to set the dialog
 			// characteristics
 			builder.setMessage(R.string.alert_story_exists);
 
 			// 3. Get the AlertDialog from create()
 			AlertDialog dialog = builder.create();
 			dialog.show();
			
 		}
 
 	}
 
 	/**
 	 * Get the user inputed story title, description, and author name, and
 	 * stores them into class variables
 	 */
 
 	private void getUserText() {
 
 		storyTitle = mStoryTitle.getText().toString();
 		storyAuthor = mStoryAuthor.getText().toString();
 		storyDescription = mStoryDescription.getText().toString();
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		// TODO Auto-generated method stub
 		super.onActivityResult(requestCode, resultCode, data);
 		if (requestCode == PHOTO_ACTIVITY_REQUEST && resultCode == RESULT_OK) {
 
 			show_path = data.getStringExtra("path");
 			imageByte = data.getStringExtra("imagebyte");
 			
 			byte[] decodedString = Base64.decode(imageByte, Base64.DEFAULT);
 			Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length); 
 			image.setImageBitmap(decodedByte);
 
 		}
 	}
 }
