 /*
  * Copyright (C) 2008 Google Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.android.demo.jnotepad;
 
 import java.io.IOException;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.pm.PackageManager;
 import android.content.pm.ResolveInfo;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.speech.RecognizerIntent;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 
 import com.android.demo.jnotepad.transfers.SDWriter;
 import com.android.demo.jnotepad.transfers.TextSender;
 
 public class EditNote extends Activity {
 	
 	private static final int VOICE_RECOGNITION_REQUEST_CODE = 8675309;
 	
 	private static final int MAX_SUBJECT_LENGTH = 15;
 	
     private NotesDbAdapter mDbHelper;
     
     private EditText mBodyText;
     private Long mRowId;
 
     /**
      * Called on the creation of the Intent
      */
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);

         if (mDbHelper == null) {
             mDbHelper = new NotesDbAdapter(this);
 
             mDbHelper.open();
         }
         
         /* set the layout */
         setContentView(R.layout.edit_layout);
 
         /* store the text component */
         mBodyText = (EditText) findViewById(R.id.edit_layout_body);
 
         /* retrieve the buttons for configuration and tying to events */
         Button confirmButton = (Button) findViewById(R.id.edit_layout_confirm);
         Button cancelButton = (Button) findViewById(R.id.edit_layout_cancel);
         Button deleteButton = (Button) findViewById(R.id.edit_layout_delete);
         Button dictateButton = (Button) findViewById(R.id.edit_layout_dictate);
 
         mRowId = null;
         
         /*  see if we have a old message stored (usually from a back or home 
          * button press) */
         if (savedInstanceState != null) {
             mRowId = (Long) savedInstanceState.getSerializable(NotesDbAdapter.KEY_ROWID);
         }
         
         /* No message there */
         if (mRowId == null || mRowId == 0) {
         	/* check to see if we are editing an old message */
             Bundle extras = getIntent().getExtras();
             
             if (extras != null) {
                 mRowId = extras.getLong(NotesDbAdapter.KEY_ROWID);
             }
         }
 
         /* Populate the body object with old test (if there is any) */
         populateFields();
         
         /* Configures what happens on a "click" event on the delete button */
         deleteButton.setOnClickListener(new View.OnClickListener() {
 
             public void onClick(View view) {
 
             	deleteNote();
             }
 
         });        
         
         /* Configures what happens on a "click" event on the confirm button */
         confirmButton.setOnClickListener(new View.OnClickListener() {
 
             public void onClick(View view) {
             	doneEdit();
             }
 
         });
         
         /* Configures what happens on a "click" event on the cancel button */
         cancelButton.setOnClickListener(new View.OnClickListener() {
 
             public void onClick(View view) {
             	cancelEdit();
             }
 
         });
         
         dictateButton.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				dictateNote();
 			}
 		});
     }
 
     /**
      * Clean up any objects the need to be cleaned
      */
     private void cleanUp() {
     	if (mDbHelper != null){
             mDbHelper.close();
             mDbHelper = null;
     	}
     }
     
     /**
      * what to do when the intent is told to delete (either by button press or 
      * menu press)
      */
     private void deleteNote(){
     	if (mRowId != null && mRowId != 0) {
     	    mDbHelper.deleteNote(mRowId);
     	}
     	
     	/* be a good citizen and clean up */
     	cleanUp();
     	
     	/* intent is done */
         finish();
     }
 
     /**
      * what to do when the intent is told to commit the data (either by button 
      * press or menu press)
      */    
     private void doneEdit(){
     	saveState();
     	
     	/* be a good citizen and clean up */
     	cleanUp();
     	
         finish();
     }
 
     /**
      * what to do when the intent is told to ignore the new data (either by 
      * button press or menu press)
      */    
     private void cancelEdit(){
     	/* be a good citizen and clean up */
     	cleanUp();
     	
         finish();
     	
     }
   
     /**
      * Pulls the data from the DB and places it on screen (in the text box)
      */
     private void populateFields() {
     	/* Ensure that we are still ok to do stuff with the db*/
     	if (mDbHelper == null){
     		Log.e("NoteEdit","db is closed");
     		return;
     	}
     	
         if (mRowId != null && mRowId != 0) {
         	/* This is an edit request */
         	
             Cursor note = mDbHelper.fetchNote(mRowId);
             
             if (note != null)
             {
 
             	/* Set the text in the message body object*/
                 mBodyText.setText(note.getString(
                     note.getColumnIndexOrThrow(NotesDbAdapter.KEY_BODY)));
                 
                 /* Set the position of the cursor when loading, otherwise the 
                  * cursor is set to the beginning of the string */
                 mBodyText.setSelection(mBodyText.getText().length());
                 
                 /* Set the delete button to enabled (As this message is in the 
                  * DB, we CAN delete it) */
                 Button deleteButton = (Button) findViewById(R.id.edit_layout_delete);
                 setButtonPressable(deleteButton, true);
             }
         } else {
         	
             /* Set the delete button to disabled (As this message is not in the 
              * DB, we CANNOT delete it) */
         	Button deleteButton = (Button) findViewById(R.id.edit_layout_delete);
         	setButtonPressable(deleteButton, false);
         }
         
         // ensure that we can listen before giving the user this option
         Button dictateButton = (Button) findViewById(R.id.edit_layout_dictate);
         setButtonPressable(dictateButton, canListen());
     }
 
     /**
      * Configures a button to look and behave like its unavailable
      * @param button the button object to set
      * @param pressable the enable/disable value to set
      */
     private void setButtonPressable(Button button, boolean pressable) {
     	button.setFocusable(pressable);
     	button.setClickable(pressable);
     	button.setEnabled(pressable);
 	}
 
     
     /**
      * Called when told to save the state (usually when the home button is 
      * pressed).
      */
     @Override
     protected void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
         
         /* Save the data in the DB */
         saveState();
         
         /* If this is a edited message, store the rowId in the state object */
         if (mRowId != null && mRowId != 0) {
             outState.putSerializable(NotesDbAdapter.KEY_ROWID, mRowId);
         }
     }
     
     /**
      * Called when the application is paused. This seems to happen quite 
      * often and in cases where you wouldn't expect.
      */
     @Override
     protected void onPause() {
     	super.onPause();
     	saveState();
     }
     
     /**
      * Called when the application is resumed.
      */
     @Override
     protected void onResume() {
         super.onResume();
         populateFields();
     }
     
     /* Saves the current text to the DB */
     private void saveState() {
     	
     	if (mDbHelper == null){
     		Log.e("NoteEdit","db is closed");
     		return;
     	}    	
     	
     	/* Pulls the text out and removes leading and following whitespace */
         String body = getBody();
         
         /* note that android version 8 does not (apparently) have isEmpty() 
          * in the string class. Later versions do. */
         if (body.length() == 0) {
         	
             /* They've committed an empty string, we assume they want to 
              * delete the message */
         	if (mRowId != null && mRowId != 0) {
         		mDbHelper.deleteNote(mRowId);
         	}
         	
         } else if (mRowId == null || mRowId == 0) {
         	
         	/* This is a new note and we need an ID number for the row*/
             long id = mDbHelper.createNote(body);
             
             if (id > 0) {
             	/* Keep the id around */
                 mRowId = id;
             }
         } else {
         	
         	/* This was an edit, just save the data */
         	Cursor note = mDbHelper.fetchNote(mRowId);
         	
             if (note != null)
             {
             	/* Get the string from the DB for comparison */
                 String origStr = note.getString(
                     note.getColumnIndexOrThrow(NotesDbAdapter.KEY_BODY));
 
                 /* If the two strings are the same, there is no need to edit 
                  * anything or change the time stamp */
                 if (!origStr.equals(body)){
                 	mDbHelper.updateNote(mRowId, body);
                 }
                 
             }
         }
     } 
     
     /**
      * Handles creating the option menu (the one resulting from a menu button 
      * press)
      */
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
        
         /* Standard pull from menu code */
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.edit_menu, menu);
         
         /* Enable or disable the delete item*/
         MenuItem delete = menu.findItem(R.id.edit_menu_delete);
         
         if (delete != null){
         	delete.setEnabled(mRowId != null && mRowId != 0);
         }
         
         return true;        
     }
 
     /**
      * Called when a menu item is selected
      */
     @Override
     public boolean onMenuItemSelected(int featureId, MenuItem item) {
         switch(item.getItemId()) {
             case R.id.edit_menu_done:
             	doneEdit();
                 break;
             case R.id.edit_menu_cancel:
             	cancelEdit();
                 break;
             case R.id.edit_menu_delete:
                 deleteNote();
                 break;
             case R.id.edit_menu_save:
             	saveToSDCard();
             	break;
             case R.id.edit_menu_sendnote:
             	sendNote();
             	break;
             default:
                 return super.onMenuItemSelected(featureId, item);
                 
         }
 
         return true;
     }
 
     /**
      * Handles the call for saving to the SD card
      */
 	private void saveToSDCard() {
 		
         String body = getBody();
         String fileName = buildSubject(body);
         
         if (fileName.length() == 0){
         	fileName = getResources().getString(R.string.defaultFileName);
         }
 		
 		try {
 			SDWriter sdw = new SDWriter("", fileName);
 			sdw.addData(body);
 			sdw.close();
 		} catch (IOException ex) {
 			Log.e("Could not write to SD card", ex.getMessage());
 		}
 		
 	}
 	
 	/**
 	 * Handles the sending of a note as well as the new intent to handle the 
 	 * sending of notes.
 	 * 
 	 * @param needsSubject boolean indicating if this send uses the subject 
 	 * line (email will want it and sms messages may block on it).
 	 */
 	private void sendNote() {
 		
 		TextSender ea = new TextSender();
 		
         String body = getBody();
         String subject = buildSubject(body);
 		
         ea.setTitle(subject);
 		ea.setBody(body);
 		
         startActivity(Intent.createChooser(ea.getIntent(), 
         		getResources().getString(R.string.select_note_sender)));
 		
 	}
 
 	/**
 	 * Creates a title or a Subject line for the note
 	 * 
 	 * @param body the body of the note
 	 * @return an abbreviated version of the body string
 	 */
 	private String buildSubject(String body) {
 		String trimBody = body.trim();
 		
 		int maxLen = Math.min(MAX_SUBJECT_LENGTH, trimBody.length());
 		
 		String subString = trimBody.substring(0, maxLen).trim();
 		
 		return subString;
 	}
 
 	/**
 	 * cleans up the body string and then returns it
 	 * 
 	 * @return
 	 */
 	private String getBody() {
 		return mBodyText.getText().toString().trim();
 	}
 	
 	/**
 	 * Called to dictate the note into the application 
 	 * (yes this duplicates the speech button on the key board)
 	 */
 	private void dictateNote() {
         // Check to see if a recognition activity is present
 		if (!canListen()){
 			Log.e("Dictator", "failed to find a dictator");
 			return;
 		}
 
         Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
         
         /* Identify our package*/
         intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass().getPackage().getName());
         
         /* We can hint the interpreter */
         intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
         
         /* Set the dialog text */
         intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Please speak now");
         
         /* Set the number of results */
         intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
         
         startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);        		
 	}
 	
 	/**
 	 * use this to id if the device can be dictated to
 	 * @return	
 	 */
 	public boolean canListen() {
         // Check to see if a recognition activity is present
         PackageManager pm = getPackageManager();
         List<ResolveInfo> activities = pm.queryIntentActivities(
                 new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
         
         return (activities.size() != 0);
 	}
 	
     /**
      * Handle the results from the recognition activity.
      */
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
             // Fill the list view with the strings the recognizer thought it could have heard
             List<String> results = data.getStringArrayListExtra(
                     RecognizerIntent.EXTRA_RESULTS);
             
             if (results != null && results.size() > 0) {
             	
             	StringBuffer tempBuf = new StringBuffer();
             	tempBuf.append(mBodyText.getText());
             	if (tempBuf.length() > 0){
             	    tempBuf.append(' ');
             	}
             	tempBuf.append(results.get(0));
             	
     		    mBodyText.setText(tempBuf.toString());
     		    
     		    saveState();
             }
             
             
         }
     }	     
 }
