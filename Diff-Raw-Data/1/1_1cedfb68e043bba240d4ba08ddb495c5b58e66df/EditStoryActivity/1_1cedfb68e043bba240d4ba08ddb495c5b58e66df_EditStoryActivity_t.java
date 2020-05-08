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
 
 import java.io.Serializable;
 import java.util.List;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.text.method.ScrollingMovementMethod;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ExpandableListView;
 import android.widget.ExpandableListView.OnGroupExpandListener;
 import android.widget.PopupMenu;
 import android.widget.PopupMenu.OnMenuItemClickListener;
 import android.widget.TextView;
 import c301.AdventureBook.Controllers.StoryManager;
 import c301.AdventureBook.Models.Page;
 import c301.AdventureBook.Models.Story;
 
 import com.example.adventurebook.R;
 
 /**
  * The edit story activity allows the author to edit the contents of a story by
  * adding or removing story fragments.
  * 
  * @author Justin
  *
  */
 public class EditStoryActivity extends Activity implements OnMenuItemClickListener, Serializable{
 
 	private final static int EDIT_PAGE = 1;
 	private final static int DELETE_PAGE = 2;
 
 	private ExpandableListAdapter adpt;
 	private ExpandableListView lstView;
 	private TextView title;
 	private TextView author;
 	private TextView description;
 	private EditText editTitle;
 	private EditText editAuthor;
 	private EditText editDescription;
 	private TextView date;
 	
 	private Button createPage;
 	private Button returnLocalLib;
 	private PopupMenu popupMenu;
 
 	StoryManager sManagerInst;
 	
 	private Story someStory;
 	private Page clickedPage;
 	private Typeface font;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(com.example.adventurebook.R.layout.edit_story_pages);
 		
 		font = Typeface.createFromAsset(getAssets(), "fonts/straightline.ttf");
 
 		title = (TextView)findViewById(com.example.adventurebook.R.id.title);
 		title.setTypeface(font);
 		author = (TextView)findViewById(com.example.adventurebook.R.id.author);
 		author.setTypeface(font);
 		description = (TextView)findViewById(com.example.adventurebook.R.id.description);
 		description.setTypeface(font);
 		
 		editTitle = (EditText)findViewById(com.example.adventurebook.R.id.editTitle);
 		editAuthor = (EditText)findViewById(com.example.adventurebook.R.id.editAuthor);
 		editDescription = (EditText)findViewById(com.example.adventurebook.R.id.editDescription);
 		editDescription.setMovementMethod(new ScrollingMovementMethod());
 		date  = (TextView)findViewById(com.example.adventurebook.R.id.date);
 		date.setTypeface(font);
 		
 		lstView = (ExpandableListView)findViewById(R.id.expList);
 		createPage = (Button) findViewById(R.id.create_new_page);
 		returnLocalLib = (Button) findViewById(R.id.return_local_lib);
 
 		popupMenu = new PopupMenu(this, findViewById(R.id.expList));
 		popupMenu.getMenu().add(Menu.NONE, EDIT_PAGE, Menu.NONE, "Edit Page");
 		popupMenu.getMenu().add(Menu.NONE, DELETE_PAGE, Menu.NONE, "Delete Page");
 		popupMenu.setOnMenuItemClickListener(this);
 
 		sManagerInst = StoryManager.getInstance();
 		sManagerInst.initContext(this);
 		fillData();
 
 		createPage.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View view) {
 				sManagerInst.createPage();
 				fillData();
 			}
 		});
 
 		returnLocalLib.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View view) {
 				saveState();
 				Intent i = new Intent(EditStoryActivity.this, OfflineLibraryActivity.class);
 				startActivity(i);
 			}
 		});
 
 		lstView.setOnGroupExpandListener(new OnGroupExpandListener()
 		{  
 			@Override
 			public void onGroupExpand(int position) {
                clickedPage = (Page)adpt.getGroup(position);
 				popupMenu.show();
 
 
 			}
 		});
 
 
 		/*
         lstView.setOnChildClickListener(new OnChildClickListener() {
             public boolean onChildClick(ExpandableListView parent, View v,
                     int groupPosition, int childPosition, long id) {
 
                 Option subPage= (Option)adpt.getChild(groupPosition, childPosition);
                 // update the text view with the country
                 return true;
 
             }
         });
 		 */
 	}
 	
 	/**
 	 * Provides actions to take upon clicking an option on the popup menu
 	 * 
 	 * @param item in popup menu clicked
 	 * @return a boolean indicating task handled
 	 */
 	@Override
 	public boolean onMenuItemClick(MenuItem item) {
 		switch (item.getItemId()) {
 
 		// User selects edit page
 		case EDIT_PAGE:
 			saveState();
 			sManagerInst.setCurrentPage(clickedPage);
 			Intent i = new Intent(EditStoryActivity.this, EditPageActivity.class);
 			startActivityForResult(i, EDIT_PAGE);
 			break;
 
 		// User selects delete page
 		case DELETE_PAGE:
 			// Ask for a confirmation from user by:
 			// Instantiating an AlertDialog.Builder with its constructor
 			AlertDialog.Builder builder = new AlertDialog.Builder(EditStoryActivity.this);
 
 			// Add buttons
 			builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int id) {
 					// User clicked OK button
 					sManagerInst.deletePage(clickedPage);
 					fillData();
 				}
 			});
 			builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int id) {
 					// User cancelled the dialog
 				}
 			});
 
 			// Chain together various setter methods to set the dialog characteristics
 			builder.setMessage(R.string.delete_page_confirm);
 			// Get the AlertDialog from create()
 			AlertDialog dialog = builder.create();
 			dialog.show();
 			break;
 		}
 		return false;
 
 	}
 
 	/**
 	 * Populates the list view with a list of all the pages in the story
 	 */
 	private void fillData() {
 		//load model here
 
 		someStory = sManagerInst.getCurrentStory();
 		editTitle.setText(someStory.getTitle());
 		editAuthor.setText(someStory.getDescription());
 		editDescription.setText(someStory.getAuthor());
 		date.setText(someStory.getDate());
 		List<Page> storyPages = someStory.getPages();
 		adpt = new ExpandableListAdapter(this, lstView, storyPages);
 		lstView.setAdapter(adpt);
 	}
 	
 	/**
 	 * Refills view for all pages upon resuming activity
 	 * 
 	 */
 	@Override
     public void onResume(){
 		super.onResume();
         fillData();
     }
 
 	private void saveState() {
 
         String title = editTitle.getText().toString();
         String author = editAuthor.getText().toString();
         String description = editDescription.getText().toString();
         someStory.setTitle(title);
         someStory.setAuthor(author);
         someStory.setDescription(description);
     }
 	
 	
 	/* Do we want a context menu instead?
     @Override
     public void onCreateContextMenu(ContextMenu menu, View v,
             ContextMenuInfo menuInfo) {
         super.onCreateContextMenu(menu, v, menuInfo);
         menu.add(0, DELETE_ID, 0, R.string.menu_delete);
     }
 
     @Override
     public boolean onContextItemSelected(MenuItem item) {
         switch(item.getItemId()) {
             case DELETE_ID:
                 //AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
                 //mDbHelper.deletePage(info.id);
                 fillData();
                 return true;
         }
         return super.onContextItemSelected(item);
     }
 	 */
 
 }
