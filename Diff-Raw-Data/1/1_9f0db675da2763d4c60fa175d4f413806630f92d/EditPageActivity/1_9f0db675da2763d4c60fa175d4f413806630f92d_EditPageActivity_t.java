 /*
  * Copyright (C) <2013>  <Justin Hoy>
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
 
 import java.io.ByteArrayOutputStream;
 import java.io.Serializable;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import c301.AdventureBook.Controllers.LibraryManager;
 import c301.AdventureBook.Controllers.StoryManager;
 import c301.AdventureBook.Models.Option;
 import c301.AdventureBook.Models.Page;
 import c301.AdventureBook.Models.RandomOption;
 import c301.AdventureBook.Models.Story;
 
 import com.example.adventurebook.R;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Color;
 import android.util.Base64;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.Toast;
 
 import c301.AdventureBook.TakePhotoActivity;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 
 import android.widget.TextView;
 
 /**
  * The edit page activity allows the author to edit the contents of a page
  * within a story.
  * 
  * @author Justin Hoy
  *
  */
 public class EditPageActivity extends Activity implements Serializable {
 
 	private static final int EDIT_OPTION = 0;
 	private static final int PHOTO_ACTIVITY_REQUEST = 1001;
 
 	private EditText mEditPageDes;
 	private EditText mEditPageTitle;
 	private Button mButtonCreateOption;
 	private Button mButtonRandomOption;
 	private Button mButtonSavePage;
 	//private CoverFlow coverFlow;
 
 	//private ImageAdapter coverImageAdapter;
 	private CustomAdapter adpt;
 	private ListView optionsList;
 
 	private String someTitle;
 	private String someDescription;
 
 	private StoryManager sManagerInst;
 	private Story currentStory;
 	private Page currentPage;
 	private Option clickedOption;
 	private List<Option> currentPageOptions;
 	private ImageView imageView;
 	private String show_path;
 	private String imageByte;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(com.example.adventurebook.R.layout.edit_page);
 
 		sManagerInst = StoryManager.getInstance();
 		currentStory = sManagerInst.getStory();
 		currentPage = sManagerInst.getPage();
 
 		mEditPageTitle = (EditText)findViewById(com.example.adventurebook.R.id.editPageTitle);
 		mEditPageDes = (EditText)findViewById(com.example.adventurebook.R.id.editPageDescription);
 		mButtonCreateOption = (Button) findViewById(R.id.new_option);
 		mButtonRandomOption = (Button) findViewById(R.id.random_option);
 		mButtonSavePage = (Button) findViewById(R.id.save_page);
 		imageView = (ImageView) findViewById(R.id.pageimage);
 		mEditPageTitle.setText(currentPage.getTitle());
 		mEditPageDes.setText(currentPage.getPageDescription());
 		if (currentPage.getImageByte() !=null){
 			byte[] decodedString = Base64.decode(currentPage.getImageByte(), Base64.DEFAULT);
 			Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
 			imageView.setImageBitmap(decodedByte);			
 		}
 
 		//coverFlow  = (CoverFlow) findViewById(com.example.adventurebook.R.id.gallery1);
 		//coverFlow.setAdapter(new ImageAdapter(this));
 		//coverImageAdapter =  new ImageAdapter(this);
 		//coverImageAdapter.createReflectedImages();
 		//coverFlow.setAdapter(coverImageAdapter);
 		//coverFlow.setSpacing(25);
 		//coverFlow.setSelection(2, true);
 		//coverFlow.setAnimationDuration(1000);
 
 		optionsList = (ListView) findViewById(R.id.options_list);
 		fillData();
 
 
 		imageView.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				Intent intent = new Intent(getApplicationContext(),
 						TakePhotoActivity.class);
 				startActivityForResult(intent, PHOTO_ACTIVITY_REQUEST);
 			}
 		});
 
 		mButtonCreateOption.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View view) {
 				Intent i = new Intent(EditPageActivity.this, EditOptionActivity.class);
 				startActivityForResult(i, EDIT_OPTION);
 			}
 		});
 
 
 		mButtonRandomOption.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View view) {
 				sManagerInst.createRandomOption();
 				fillData();
 			}
 		});
 
 
 		mButtonSavePage.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View view) {
 				someTitle = mEditPageTitle.getText().toString();
 				someDescription = mEditPageDes.getText().toString();
 
 
 				//currentPage = currentStory.getPage(currentPage);
 
 
 
 				currentPage.setTitle(someTitle);
 				currentPage.setPageDescription(someDescription);
 				currentPage.setImageByte(imageByte);
 
 
 				//We need to over write the previous page with the new one.
 				currentStory.replacePage(currentPage);
 				sManagerInst.setCurrentPage(currentPage);
 				sManagerInst.saveStory(currentStory, true);
 				finish();
 			}
 		});
 	}
 
 
 	@Override
 	//test image
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		// TODO Auto-generated method stub
 		super.onActivityResult(requestCode, resultCode, data);
 		if (requestCode == PHOTO_ACTIVITY_REQUEST && resultCode == RESULT_OK) {
 
 			show_path = data.getStringExtra("path");
 			imageByte = data.getStringExtra("imagebyte");
 			byte[] decodedString = Base64.decode(imageByte, Base64.DEFAULT);
 			Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length); 
 			imageView.setImageBitmap(decodedByte);
 
 
 
 		}
 	}
 
 	public void onResume()
 	{  // After a pause OR at startup
 		super.onResume();
 		//Refresh your stuff here
 		fillData();
 	}
 
 
 	/**
 	 * Populates the list view with a list of all the options in the page
 	 */
 	private void fillData() {
 		//load model here
 		currentPageOptions = sManagerInst.getPage().getOptions();
 		// grey out button to add a random option if there is already one in the page or no options exist
 		boolean randomOptionExists = false;
 		for(Option option:currentPageOptions){
 			if(option instanceof RandomOption){
 				randomOptionExists = true;
 			}
 		}
 		if(currentPageOptions.size() == 0 || randomOptionExists == true){
 			mButtonRandomOption.setEnabled(false);
 			//mButtonRandomOption.setBackgroundColor(Color.parseColor("#808080"));
 		}
 		else{
 			mButtonRandomOption.setEnabled(true);
 		}
 		adpt = new CustomAdapter(this, optionsList, currentPageOptions);
 		optionsList.setAdapter(adpt);
 	}
 
 	private class CustomAdapter extends ArrayAdapter<Option> {
 
 		public CustomAdapter(EditPageActivity editPageActivity, ListView optionsList, List<Option> options) {
 			super(EditPageActivity.this, R.layout.option_row,
 					currentPageOptions);
 		}
 
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 
 			View itemView = convertView;
 			if (itemView == null) {
 				itemView = getLayoutInflater().inflate(
 						R.layout.option_row, parent, false);
 			}
 
			clickedOption = (Option) optionsList.getItemAtPosition(position);
 			final Button edit = (Button) itemView.findViewById(R.id.option_description);
 			edit.setText(clickedOption.getDescription());
 			final Button delete = (Button) itemView.findViewById(R.id.delete_button);
 
 			delete.setTag(position);
 			edit.setTag(position);
 			
 			OnClickListener mClickListener = new OnClickListener() {
 				public void onClick(View v) {
 					int position = (Integer)v.getTag();
 					if(v.getId() == edit.getId()){
 						Toast.makeText(EditPageActivity.this,"Editing Page: "+v.getTag(), Toast.LENGTH_SHORT).show();
 						clickedOption = (Option) optionsList.getItemAtPosition(position);
 						sManagerInst.setCurrentOption(clickedOption);
 						
 					}
 					else if(v.getId() == delete.getId()){
 						clickedOption = (Option) optionsList.getItemAtPosition(position);
 						sManagerInst.setCurrentOption(clickedOption);
 						// Ask for a confirmation from user by:
 						// Instantiating an AlertDialog.Builder with its constructor
 						AlertDialog.Builder builder = new AlertDialog.Builder(EditPageActivity.this);
 
 						// Add buttons
 						builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog, int id) {
 								// User clicked OK button
 
 								sManagerInst.deleteOption(clickedOption);
 								fillData();
 							}
 						});
 						builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog, int id) {
 								// User cancelled the dialog
 							}
 						});
 
 						// Chain together various setter methods to set the dialog characteristics
 						builder.setMessage(R.string.delete_option_confirm);
 						// Get the AlertDialog from create()
 						AlertDialog dialog = builder.create();
 						dialog.show();
 					}
 				}
 			};
 
 			delete.setOnClickListener(mClickListener);
 			edit.setOnClickListener(mClickListener);
 
 			return itemView;
 		}
 	}
 }
