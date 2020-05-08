 /*
 * Copyright (c) 2013, TeamCMPUT301F13T02
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * 
 * Redistributions in binary form must reproduce the above copyright notice, this
 * list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 * 
 * Neither the name of the {organization} nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
 
 package ca.ualberta.CMPUT301F13T02.chooseyouradventure;
 
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 /**
  * The Activity in the application that is responsible for viewing and editing
  * a page within a story.  <br />
  * <br />
  * In this activity a reader can:
  * <ol>
  *     <li> Read the page </li>
  *     <li> Follow decisions at the bottom </li>
  *     <li> Comment on the page </li>
  * </ol>
  * In this activity an author can: 
  * <ol>
  *     <li> Edit the tiles on this page (add, edit, reorder, delete) </li>
  * </ol>
  * 
  * The ViewPageActivity is a view of the application.
  * 
  * @author James Cadek
  * @author James Moore
  * @author Karl Parkins
  */
 
 public class ViewPageActivity extends Activity {
 	
 	private boolean isEditing = false;
 	private boolean isFighting = false; 
 	private boolean onEntry = true;
 	
 	private static final int RESULT_LOAD_IMAGE = 1;
 	private final int TAKE_PHOTO = 2;
 	private final int GRAB_PHOTO = 3;
 	private final int ADD_PHOTO = 4;
 	
 	private final int EDIT_INDEX = 0;
 	private final int SAVE_INDEX = 1;
 	private final int HELP_INDEX = 2;
 	
 	private LinearLayout tilesLayout;
 	private LinearLayout decisionsLayout;
 	private LinearLayout commentsLayout;
 	private LinearLayout fightingLayout;
 	
 	private FightingLayoutBuilder fightBuilder = new FightingLayoutBuilder();
 	private TileLayoutBuilder tileBuilder;
 	private DecisionLayoutBuilder decisionBuilder;
 	private CommentLayoutBuilder commentBuilder;
 	private TileView guiTile;
 	private DecisionView guiDecision;
 	private CommentView guiComment;
 	private StoryController storyController; 
 	private PageController pageController; 
     private ApplicationController app;
     private CameraAdapter camera;
     private Menu menu;
     
 	@Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.view_page_activity);
     }
 
 	/**
 	 * Called when the Activity resumes. Handles setting up the entire activity
 	 */
 	@Override
 	public void onResume() {
 	    super.onResume();
 	    
 	    app = (ApplicationController) this.getApplication();
 	    
 	    storyController = app.getStoryController();
 	    pageController = app.getPageController();
 	    
 	    tileBuilder = new TileLayoutBuilder(this);
 	    decisionBuilder = new DecisionLayoutBuilder(storyController,this);
 	    commentBuilder = new CommentLayoutBuilder(this);
 	    
 	    camera = new CameraAdapter(this);
 	    
 	    guiComment = new CommentView(pageController, this, camera);
 	    guiTile = new TileView(pageController, this);
 	    guiDecision = new DecisionView(storyController, pageController, this);
 	    
 	    fightingLayout = (LinearLayout) findViewById(R.id.fightingLayout);
 	    tilesLayout = (LinearLayout) findViewById(R.id.tilesLayout);
 	    decisionsLayout = (LinearLayout) findViewById(R.id.decisionsLayout);
 	    commentsLayout = (LinearLayout) findViewById(R.id.commentsLayout);      
 	    pageController.setActivity(this);
 	    
 	
 	    update(pageController.getPage());
 	    
 	    /* Set up onClick listeners for buttons on screen, even if some aren't
 	     * shown at the time.
 	     */
 		Button addTileButton = (Button) findViewById(R.id.addTile);
 		addTileButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View view) {
 				guiTile.addTileMenuGUI(tilesLayout);
 			}
 		});
 		
 		Button addDecisionButton = (Button) findViewById(R.id.addDecision);
 		addDecisionButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View view) {
 				pageController.addDecision();
 			}
 		});
 		
 	    TextView addComment = (TextView) findViewById(R.id.addComment);
 	    addComment.setOnClickListener(new OnClickListener() {
 	    	@Override
 	    	public void onClick(View view) {
 	    		guiComment.onCallCommentGUI();
 	    		
 	    	}
 	    });
 	    
 	    TextView pageEnding = (TextView) findViewById(R.id.pageEnding);
 	    pageEnding.setOnClickListener(new OnClickListener() {
 	    	@Override
 	    	public void onClick(View view) {
 	    		if (getEditing()) {
 	    			guiTile.onEditPageEndingGUI(view);
 	    		}
 	    	}
 	    });
 	}
 
 	/**
 	 * Remove the activity from the pageController when we leave the activity.
 	 */
 	@Override
 	public void onPause() {
 		super.onPause();
 		pageController.deleteActivity();
 	}
 	
 	/**
 	 * Create an options menu.
 	 * 
 	 * @param menu The menu to create
 	 * @return Success
 	 */
 	@Override
     public boolean onCreateOptionsMenu(Menu menu) {
 		this.menu = menu;
 		super.onCreateOptionsMenu(menu);
         makeMenu(menu);
         return true;
     }
 	
 	/**
 	 * Sets the action bar up.
 	 * @param menu
 	 */
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		app = (ApplicationController) getApplication();
 		changeActionBarButtons();
 		return true;
 	}
 	
     /**
      * Puts button for changing to edit mode in the action bar.
      * @param menu The Menu to make
      */
 	public void makeMenu(Menu menu) {
 	
 		MenuItem editPage = menu.add(0, EDIT_INDEX, EDIT_INDEX, getString(R.string.edit));
 		MenuItem savePage = menu.add(0, SAVE_INDEX, SAVE_INDEX, getString(R.string.done));
 		MenuItem help = menu.add(0, HELP_INDEX, HELP_INDEX, getString(R.string.help));
 		
 		help.setEnabled(!HelpPlayer.getInstance().isPlaying());
 		
 		HelpPlayer.getInstance().trackHelpItem(help);
 
 		editPage.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
 		savePage.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
 		help.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
 
 	}
 	
 	/**
 	 * Callback for clicking an item in the menu.
 	 * 
 	 * @param item The item that was clicked
 	 * @return Success
 	 */
     public boolean onOptionsItemSelected(MenuItem item) 
     {
 
     	try {
 			return menuItemClicked(item);
 		} catch (HandlerException e) {
 			e.printStackTrace();
 		}
     	return true;
 
     }
 	
 	/**
 	 * Handles what to do when an item of the action bar is pressed.
 	 * @param item The clicked item
 	 * @return
 	 */
 	private boolean menuItemClicked(MenuItem item) throws HandlerException {
 		switch (item.getItemId()) {
 		case EDIT_INDEX:
 
 			final String myId = app.getAndroidID();
 			final String storyID = storyController.getStory().getAuthor();
 			if(myId.equals(storyID)){
 				setEditing(true);
 				pageController.reloadPage();
 				changeActionBarButtons();
 				setButtonVisibility();
 			}
 
 			break;
 
 		case SAVE_INDEX:
 
 			setEditing(false);
 			storyController.saveStory();
 			pageController.reloadPage();
 			changeActionBarButtons();
 			setButtonVisibility();
 
 			break;
 
 		case HELP_INDEX:
 
 			if (isEditing) 
 				HelpPlayer.getInstance().play(this, R.raw.edithelp);
 			else
 				HelpPlayer.getInstance().play(this, R.raw.readhelp);
 	        
 			break;
 		}
 		return true;
 	} 
 	
 	/**
 	 * Sets which buttons are visible in the action bar.
 	 */
 	public void changeActionBarButtons() {
 		MenuItem editButton = menu.findItem(EDIT_INDEX);
 		MenuItem saveButton = menu.findItem(SAVE_INDEX);
 		
 		final String myId = app.getAndroidID();
 		Story story = storyController.getStory();
 		final String storyID = story.getAuthor();
 		if(myId.equals(storyID)){
 			if (getEditing()) {
 				saveButton.setVisible(true);
 				editButton.setVisible(false);
 			} else {
 				saveButton.setVisible(false);
 				editButton.setVisible(true);
 			}
 		} else {
 			saveButton.setVisible(false);
 			editButton.setVisible(false);
 		}
 	}
 	
 	/**
 	 * Tells the View to get the most recent data from the model, and refresh
 	 * everything that needs to be refreshed.
 	 * @param page
 	 */
 	public void update(Page page) {
 		
 		setButtonVisibility();
 		setFighting(page.getFightingState());
 		Story story = storyController.getStory();
 		if (story.isUsesCombat() == true) {
 			fightBuilder.updateFightView(fightingLayout,this,story,page);
 		}
 		
 		if (pageController.haveTilesChanged()) {
 			tileBuilder.updateTiles(page, tilesLayout);
 		}
 		
 		if (pageController.haveDecisionsChanged()) {
 			decisionBuilder.updateDecisions(page, decisionsLayout);
 		}
 		
 		if (pageController.haveCommentsChanged()) {
 			commentBuilder.updateComments(page, commentsLayout);
 		}
 		
 		if (pageController.hasEndingChanged()) {
 			updateEnding(page);
 		}
 		
 		pageController.finishedUpdating();
 	}
 	
 	/**
 	 * Handles removing or showing the proper buttons in both the action bar
 	 * and the in the page.
 	 */
 	private void setButtonVisibility() {
 		Button addTileButton = (Button) findViewById(R.id.addTile);
 		Button addDecisionButton = (Button) findViewById(R.id.addDecision);
 		
 		final String myId = app.getAndroidID();
 		final String storyID = storyController.getStory().getAuthor();
 		if(myId.equals(storyID)){
 		
 			int visibility = View.VISIBLE;
 		
 			if (!getEditing()) {
 				visibility = View.GONE;
 			}
 				
 			addTileButton.setVisibility(visibility);
 			addDecisionButton.setVisibility(visibility);
 		} else {
 			addTileButton.setVisibility(View.GONE);
 			addDecisionButton.setVisibility(View.GONE);
 		}
 	}	
 	
 	/**
 	 * Updates the pageEnding from the passed page object.
 	 * @param page
 	 */
 	private void updateEnding(Page page) {
 		TextView pageEnding = (TextView) findViewById(R.id.pageEnding);
 		pageEnding.setText(page.getPageEnding());
 	}
 
 	/**
 	 * Brings up a menu with options of what to do to the decision.
 	 * @param view
 	 */
 	public void editTileMenu(final View view){
 		guiTile.editTileMenuGUI(view, tilesLayout);
         
     }
 	
 	/**
 	 * Brings up a menu with options of what to do to the decision.
 	 * @param view
 	 */
 	public void decisionMenu(final View view){
 		guiDecision.decisionMenuGUI(view, decisionsLayout);
     }
 
 	/**
 	 * Changes the view so that the next page is showing.
 	 * @param view
 	 */
 	protected void decisionClicked(View view) {
 		app.onDecisionClick(view, decisionsLayout);
 	}
 	
 	/**
 	 * Brings up a menu to make a new comment.
 	 */
 	protected void onEditComment() {
 		Story story = storyController.getStory();
     	guiComment.onEditCommentGUI(story);
        
 	}
 	
 	/**
 	 * The main interface for retrieving photos. Due to the nature of this code, it cannot be easily moved outside the activity
 	 * Even though it doesn't really belong here. This delegates off to other photo and layout methods after getting a picture.
 	 */
 	@Override
 	public void onActivityResult( final int requestCode, final int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		AlertDialog.Builder successChecker = new AlertDialog.Builder(this);
 		if (resultCode == RESULT_OK && null != data) {
 			
 			switch(requestCode) {
 				case (RESULT_LOAD_IMAGE):
 					final int whicTile = (Integer) camera.getTempSpace();
 					Bitmap fileImage = camera.loadImage(data);
 					PhotoTile tile = new PhotoTile();
 					tile.setContent(fileImage);
 					if (whicTile == -1) {
 						pageController.addTile(tile);
 					} else if (whicTile >= 0) {
 						pageController.updateTile(fileImage, whicTile);
 					}
 					break;
 				case (GRAB_PHOTO):
 					PhotoTile newTile = new PhotoTile();
 					newTile.setContent(camera.loadImage(data));
 					camera.setTempSpace(newTile);
 					onEditComment();
 					break;
 				case(TAKE_PHOTO):
 				case(ADD_PHOTO):
 					final Object tileNum = (Integer) camera.getTempSpace();	
 					final Bitmap cameraImage = camera.retrievePhoto(data);
 					successChecker.setView(camera.makeViewByPhoto(cameraImage));
 					successChecker.setTitle(getString(R.string.retakeQuestion));
 					successChecker.setPositiveButton(getString(R.string.save),
 							new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog, int id) {
 									PhotoTile tile = new PhotoTile();
 									tile.setContent(cameraImage);
 									if(requestCode == TAKE_PHOTO){
										int whichTile = (Integer) tileNum; 
 										if (whichTile == -1) {
 											pageController.addTile(tile);
 										} else if (whichTile >= 0) {
 											pageController.updateTile(cameraImage, whichTile);
 										}
 									} else {
 										camera.setTempSpace(tile);
										onEditComment();							
 									}
 								}
 					})
 					.setNegativeButton(getString(R.string.retake), new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog, int id) {
							int whichTile = (Integer) tileNum;
							takePhoto(whichTile);
 						}
 					});
 					successChecker.show();
 					break;
 			}}
 	}
 	
 	/**
 	 * The following 4 methods all call the camera with different intents.
 	 */
 	
 	/**
 	 * For taking a new photo for a the tile at whichTile
 	 * @param whichTile
 	 */
 	protected void takePhoto(int whichTile) {
 		camera.setTempSpace(whichTile);
 		camera.newPhoto(TAKE_PHOTO);
 	}
 
 	/**
 	 * For taking a new photo for a comment
 	 */
 	protected void addPhoto() {
         camera.newPhoto(ADD_PHOTO);
 	}
 	
 	/**
 	 * For getting a photo for a comment from the gallery
 	 */
 	public void grabPhoto(){
 		camera.getPhoto(GRAB_PHOTO); 
 	}
 
 	/**
 	 * For getting a photo for a the tile at position whichTile from the
 	 * gallery.
 	 * @param whichTile
 	 */
 	public void getPhoto(int whichTile){
 		camera.setTempSpace(whichTile);
 		camera.getPhoto(RESULT_LOAD_IMAGE);
 	}	
 	
 	/**
 	 * Sets whether the user wants to be in viewing mode or editing mode.
 	 * @param editing
 	 */
 	public void setEditing(boolean editing) {
 		isEditing = editing;
 	}
 
 	/**
 	 * Get whether the page being viewed by the user is in editing mode or not.
 	 * @return If the page is in editing mode.
 	 */
 	public boolean getEditing() {
 		return isEditing;
 	}
 
 	/**
 	 * Gets if the current page is a fighting page or not. 
 	 * @return the isFighting
 	 */
 	public boolean isFighting()
 	{
 
 		return isFighting;
 	}
 
 	/**
 	 * Sets if the current page is a fighting page or not.
 	 * @param isFighting the isFighting to set
 	 */
 	public void setFighting(boolean isFighting)
 	{
 		this.isFighting = isFighting;
 	}
 	
 	/**
 	 * Used to check if the player is entering a page from somewhere else of
 	 * from a decision on the page that leads back to itself.
 	 * @return if the player is entering a page from elsewhere
 	 */
 	public boolean isOnEntry() {
 		return onEntry;
 	}
 
 	/**
 	 * Sets if the page is a new entry.
 	 * @param onEntry
 	 */
 	public void setOnEntry(boolean onEntry) {
 		this.onEntry = onEntry;
 	}
 }
