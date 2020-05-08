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
 
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 /**
  * This generates the GUIs for ViewStoriesActivity
  *
  */
 public class StoryView {
 	private ApplicationController app;
 	private ViewStoriesActivity storyActivity;
 	
 
 	
 	public StoryView(ApplicationController app, ViewStoriesActivity storyActivity) {
 		super();
 		this.app = app;
 		this.storyActivity = storyActivity;
         
 	}
 	
 	/**
 	 * The menu you obtain from longclicking on a story.
 	 * @param story
 	 * @param eshandler
 	 * @param dbhandler
 	 */
 	protected void storyMenuGUI(final Story story, final Handler eshandler, final Handler dbhandler){	
 		AlertDialog.Builder builder = new AlertDialog.Builder(storyActivity);
 		final String[] titles;
 		final String[] titlesA;
 		final String[] titlesB;
 		if(story.getHandler() instanceof DBHandler) {
 			titlesA = new String[]{storyActivity.getString(R.string.upload),
 									//storyActivity.getString(R.string.changeTitle),
 									storyActivity.getString(R.string.edit), 
 									storyActivity.getString(R.string.delete), 
 									storyActivity.getString(R.string.cancel) };
 			
 			titlesB = new String[]{ storyActivity.getString(R.string.uploadCopy), 
 									storyActivity.getString(R.string.cancel) };
 		} else{
 			titlesA = new String[]{ storyActivity.getString(R.string.cache),
 									//storyActivity.getString(R.string.changeTitle),
 									storyActivity.getString(R.string.edit), 
 									storyActivity.getString(R.string.delete), 
 									storyActivity.getString(R.string.cancel) };
 			
 			titlesB = new String[]{ storyActivity.getString(R.string.cache), 
 									storyActivity.getString(R.string.cancel) };
 		}
 		final String myId = app.getAndroidID();
 		final String storyID = story.getAuthor();
 		if(myId.equals(storyID)){
 			titles = titlesA;
 			builder.setTitle(R.string.story_options_author);
 		}
 		else {
 			titles = titlesB;
 			builder.setTitle(R.string.story_options_user);
 		}
         builder.setItems(titles, new DialogInterface.OnClickListener() {
         	public void onClick(DialogInterface dialog, int item) {
             	switch(item){
 
             	case(0): 
             		if (story.getHandler() instanceof DBHandler) { //upload
             			story.setHandler(eshandler);
             			story.setAuthor(myId);
             			Story oldStory = new Story();
             			try {
 							oldStory = story.getHandler().getStory(story.getId());
 						} catch (HandlerException e1) {
 							e1.printStackTrace();
 						}
             			if (oldStory.getAuthor().equals(story.getAuthor())) {
             				story.updateStory();
             			} else {
                 			//create a new story because you have to change author ID
                 			//set it to be online initially
     						try {
     							eshandler.addStory(story);
     						} catch (HandlerException e) {
     							e.printStackTrace();
     						}	
             			}
 						storyActivity.refresh();
                 		break;
             		} else { //cache
                 		story.setHandler(dbhandler);
 	                	story.setAuthor(myId);
                 		try {
                 			story.getHandler().addStory(story);
                 		} catch (HandlerException e) {
                 			e.printStackTrace();
                 		}
                 		storyActivity.refresh();
                 		break;
             		}
             	/*case(1):
             		changeTitleGUI();
             		break;*/
             	case(1): //edit story
             		if(myId.equals(storyID)){          			
                 		app.jump(EditStoryActivity.class, story, null);
             		}
             		else{}
             		break;
             	case(2): //delete
             		try {
 						story.getHandler().deleteStory(story);
 					} catch (HandlerException e) {
 						e.printStackTrace();
 					}
            		storyActivity.deleteStory(story);
             		break;
             	}
             }
 
 			
 			
         });
         builder.show();
 	}
 	/*
 	protected void changeTitleGUI() {
 		
 	}*/
 	
 	/**
 	 * The menu for making a new story
 	 */
 	protected void createStoryGUI(){
 		AlertDialog.Builder builder = new AlertDialog.Builder(storyActivity);
     	builder.setTitle(storyActivity.getString(R.string.createNew));
     	final LinearLayout layout = new LinearLayout(storyActivity);
     	layout.setOrientation(LinearLayout.VERTICAL);
     	
     	final EditText alertEdit = new EditText(storyActivity);
     	alertEdit.setSingleLine(true);
     	layout.addView(alertEdit);
     	
     	final TextView alertText = new TextView(storyActivity);
     	alertText.setText(storyActivity.getString(R.string.useCountersAndCombat));
     	layout.addView(alertText);
     	
     	final CheckBox check = new CheckBox(storyActivity);
     	layout.addView(check);
     	
     	
     	builder.setView(layout);
     	builder.setMessage(storyActivity.getString(R.string.enterStoryTitle))
     	.setPositiveButton(storyActivity.getString(R.string.save), new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int id) {
             	
 					try {
 						Counters baseCount = new Counters();
 						baseCount.setBasic("0", "100");
 						app.initializeNewStory(alertEdit.getText().toString(), baseCount, check.isChecked());
 						
 						storyActivity.refresh();
 					} catch (HandlerException e) {
 						e.printStackTrace();
 					}
 
             }
         })
         .setNegativeButton(storyActivity.getString(R.string.cancel), null);
     	builder.show();
 	}
 }
 
 			
 	
      
 
