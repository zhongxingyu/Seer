 /*
  * Copyright (C) 2007 The Android Open Source Project
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
  * 
  *  This code has been extensively modified by P.J.Lawrence (August 2011) to incorporate  
  *  1) Spellchecking functionality (Currently using francois.pessaux.droidic.android dictionary)
  *  2) Text to Speech functionality
  *  3) Speech to text functionality
  * 
  */
 
 package pj.lawrence.android.notepad;
 
 import java.util.Locale;
 
 
 import pj.lawrence.android.notepad.R;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.ComponentName;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.DialogInterface;
 
 import android.content.Intent;
 
 import android.database.Cursor;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.Rect;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.WindowManager;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.Spinner;
 import android.widget.Button;
 
 import android.speech.tts.TextToSpeech;
 
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import francois.pessaux.droidic.android.Exists;
 import francois.pessaux.droidic.android.CLikeStringUtils;
 import francois.pessaux.droidic.android.SuggBasics;
 import francois.pessaux.droidic.android.Suggest;
 
 import java.io.UnsupportedEncodingException ;
 import android.os.Handler;
 import android.os.Message;
 
 // Speech Recogniser
 import android.content.pm.PackageManager;
 import android.content.pm.ResolveInfo;
 import android.speech.RecognizerIntent;
 import java.util.ArrayList;
 import java.util.List;
 
 import pj.lawrence.android.notepad.NotePad.Notes;
 
 // double tap
 import android.view.GestureDetector;  
 import android.view.GestureDetector.OnDoubleTapListener;  
 import android.view.GestureDetector.OnGestureListener;  
 import android.view.MotionEvent; 
 
 /**
  * A generic activity for editing a note in a database.  This can be used
  * either to simply view a note {@link Intent#ACTION_VIEW}, view and edit a note
  * {@link Intent#ACTION_EDIT}, or create a new note {@link Intent#ACTION_INSERT}.  
  */
 public class NoteEditor extends Activity implements TextToSpeech.OnInitListener, OnGestureListener {
     private static final String TAG = "Notes";
 
     /**
      * Standard projection for the interesting columns of a normal note.
      */
     private static final String[] PROJECTION = new String[] {
             Notes._ID, // 0
             Notes.NOTE, // 1
     };
     /** The index of the note column */
     private static final int COLUMN_INDEX_NOTE = 1;
     
     // This is our state data that is stored when freezing.
     private static final String ORIGINAL_CONTENT = "origContent";
 
     // Identifiers for our menu items.
     private static final int REVERT_ID = Menu.FIRST;
     private static final int DISCARD_ID = Menu.FIRST + 1;
     private static final int DELETE_ID = Menu.FIRST + 2;
     private static final int SPEAK_ID = Menu.FIRST + 3;
     private static final int  SPELL_ID = Menu.FIRST + 4;
     private static final int  RECOGNIZER_ID = Menu.FIRST + 5;
     private static final int INSTALL_SPEECH_APK_ID = Menu.FIRST + 6;
 
     // The different distinct states the activity can be run in.
     private static final int STATE_EDIT = 0;
     private static final int STATE_INSERT = 1;
 
     private int mState;
     private boolean mNoteOnly = false;
     private Uri mUri;
     private Cursor mCursor;
     private EditText mText;
     private String mOriginalContent;
     
     // spell checker
     private int mSpellWordStart;
     private int mSpellWordEnd;
     private Dialog mWordListDialog;
     private ProgressDialog mProgressDialog = null;
     private String[] mSuggestedWords; // needs to be global since used by dialogue box
     private String mWordListResultAsString=null;
     private String mSelectedWord;
     
     // text to speech
     TextToSpeech mTTS;
     private static final int SPEECH_RECOGNITION_REQUEST_CODE = 1985;
     boolean mSpeechRecongiserPresent;
     private static final int VOICE_RECOGNITION_REQUEST_CODE  = 1984;
     private ArrayList<String> mRegognitionMatches; // list of possible word matches returned
     boolean mBlockResume=false; // used to block the resume function when recognition is carried out
     private static boolean mHasAskedSpeechInstalledQuestion=false;
     
     //creates for Gesture Detector double click  
     private GestureDetector gd;
     
     
     /**
      * A custom EditText that draws lines between each line of text that is displayed.
      */
     public static class LinedEditText extends EditText {
         private Rect mRect;
         private Paint mPaint;
         
         private ArrayList<Integer> mUnknownWordsBoundaries;
         private int mSearchRangeEnd=-1;
 
         // we need this constructor for LayoutInflater
         public LinedEditText(Context context, AttributeSet attrs) {
             super(context, attrs);
             
             mRect = new Rect();
             mPaint = new Paint();
             mPaint.setStyle(Paint.Style.STROKE);
             mPaint.setColor(0x800000FF);
             //mPaint.setColor(0x80FF0000);
         }
         
         @Override
         protected void onDraw(Canvas canvas) {
         	
             int count = getLineCount();
             Rect r = mRect;
             Paint paint = mPaint;
 
             for (int i = 0; i < count; i++) {
                 int baseline = getLineBounds(i, r);
                 canvas.drawLine(r.left, baseline + 1, r.right, baseline + 1, paint);
             }
             super.onDraw(canvas);
         }
         
         public int NumberOfUnknowWords()
         {
         	if (mUnknownWordsBoundaries!=null)
         		return (mUnknownWordsBoundaries.size()/2);
         	return (0);
         }
         
         private boolean isLetter(char aLetter) {
         	return (aLetter>='a' && aLetter<='z' ||	aLetter>='A' && aLetter<='Z');
         }
         private boolean isAnApostrophe(char aLetter) {
         	return (aLetter=='\'' || aLetter=='');
         }
         private boolean isHyphens(char aLetter) {
         	return (aLetter=='-');
         }
         
         public void ResetSearchRangeEnd() {
         	mSearchRangeEnd=-1;
         }
         public void SetSearchRangeEnd(int aLocation) {
         	mSearchRangeEnd=aLocation;
         }
         public void IncreaseSearchRangeEnd(int aLocation) {
         	mSearchRangeEnd+=aLocation;
         }
         // Checks a single work 
         // WordStart and WordEnd used to highlight text
         // note MaxLength should be  (text.length()-1) 
         // return true if word is unknown
         private boolean CheckWord(String aWord,int WordStart, int WordEnd, int MaxLength, boolean apostrophe, boolean hyphen)
         {
         	if (aWord.length()> 1) {
 				if (apostrophe) {
 					if (isAnApostrophe(aWord.charAt(aWord.length()-1))) {
 						// remove apostrophe at end of word
 						aWord = aWord.substring(aWord.length()-1);
 						WordEnd--;
 					}
 				}
 				if (hyphen) {
 					if (isHyphens(aWord.charAt(aWord.length()-1))) {
 						// remove hyphen at end of word
 						aWord = aWord.substring(aWord.length()-1);
 						WordEnd--;
 					}
 				}
 				// check spelling
 	    		if (Exists.IsDefined (aWord)==false)
 	    		{
 	    			// mark as a unknown word
 	    			if (mUnknownWordsBoundaries==null)	{
 	    				mUnknownWordsBoundaries= new ArrayList<Integer> ();
 	    			}
 	    			mUnknownWordsBoundaries.add(WordStart);
 	    			if (WordEnd<MaxLength) WordEnd+=1;
 	    			mUnknownWordsBoundaries.add(WordEnd);
 	    			setSelection(WordStart, WordEnd);
 	    			
 	    			return (true);
 	    		}
 			}
         	return (false);
         }
         
         // Check the words in the document from cursor
         public void CheckSpellings(boolean OnlyFirstUnknownWork)
         {
         	String text = getText().toString();
         	// Scan for words and assign type (known/unknown)
         	boolean ScanningWord=false,apostrophe=false,hyphen=false;
         	int WordStart=-1;
         	int WordEnd=-2;
         	String aWord = "";
         	
         	int SectionPos   =getSelectionStart();
         	int SectionPosEnd=getSelectionEnd();
         	if  (mSearchRangeEnd<0)
         	{
         		if (SectionPos>SectionPosEnd) {
         			int Temp=SectionPos;
         			SectionPos=SectionPosEnd;
         			SectionPosEnd=Temp;
         		}
         		if (mUnknownWordsBoundaries!=null)	{
             		mUnknownWordsBoundaries.clear();
             	}
 	        	if (SectionPosEnd<0 || SectionPosEnd==SectionPos) {
 	        		SectionPosEnd=text.length();
 	        	}
 	        	else {
 	        		// scan to end of next word
 	        		char LetterAtPos;
 	        		boolean doloop=true;
 	        		while (doloop)
 	        		{
 	        			LetterAtPos=text.charAt(SectionPosEnd);
 	        			if (isLetter(LetterAtPos))
 	        			{
 	        				if (SectionPosEnd>0)
 	        					SectionPosEnd++;
 	        				else
 	        					doloop=false;
 	        			}
 	        			else {
 	        				doloop=false;
 	        			}
 	        		}
 	        		doloop=true;
 	        		if (SectionPosEnd>text.length()) {
 	        			SectionPosEnd=text.length();
 	        		}
 	        		
 	        	}
 	        	if (SectionPos<0 || SectionPos>=text.length()) 
 	        		SectionPos=0;
 	        	else if (SectionPos>0) {
 	        		// scan back to start of word
 	        		char LetterAtPos;
 	        		boolean doloop=true;
 	        		while (doloop)
 	        		{
 	        			LetterAtPos=text.charAt(SectionPos);
 	        			if (isLetter(LetterAtPos))
 	        			{
 	        				if (SectionPos>0)
 	        					SectionPos--;
 	        				else
 	        					doloop=false;
 	        			}
 	        			else {
 	        				doloop=false;
 	        			}
 	        		}
 	        		doloop=true;
 	        	}
 	        	// store finish.
 	        	mSearchRangeEnd=SectionPosEnd;
         	}
         	else {
         		// continue search from current location 
         		// to specified stopping point
         		if (mSearchRangeEnd>text.length()) {
         			mSearchRangeEnd=text.length();
         		}
         		SectionPosEnd=mSearchRangeEnd;
         		if (SectionPos<0 || SectionPos>text.length()) 
 	        		SectionPos=0;
         	}
         	
         	// Now check document
         	for (int i=SectionPos;i<SectionPosEnd;i++)
         	{
         		char LetterAtPos=text.charAt(i);
     			if (isLetter(LetterAtPos))
     			{
     				if (ScanningWord) {
     					WordEnd=i;
     				}
     				else {
     					WordStart=i;
     					ScanningWord=true;
     				}
     			}
     			else {
     				if (isAnApostrophe(LetterAtPos)) { 
     					if (apostrophe) {
     						// word already has an apostrophe
     						// so end word
     						ScanningWord=false;
     					}
     					else {
     						// continue on
     						WordEnd=i;
     						apostrophe=true;
     					}
     				}
     				else if (isHyphens(LetterAtPos)) {
     					if (hyphen) {
     						// word already has a hyphens
     						// so end word
     						ScanningWord=false;
     					}
     					else {
     						// continue on
     						WordEnd=i;
     						hyphen=true;
     					}
     				}
     				else {
     					ScanningWord=false; // end of word
     				}
     			}
     			if (ScanningWord) {
     				aWord+=LetterAtPos; // append letter
     			}
     			if (ScanningWord==false)
     			{
     				if (CheckWord(aWord,WordStart,WordEnd,text.length(),apostrophe,hyphen) && OnlyFirstUnknownWork) {
     					return;
     				}
 			    	aWord=""; // clear the word
     				if (apostrophe)
     					apostrophe=false;
     				if (hyphen)
     					hyphen=false;
     			}
         	}
         	if  (ScanningWord==true) {
         		// the last entry is a work so check it
         		CheckWord(aWord,WordStart,WordEnd,text.length(),apostrophe,hyphen);
         	}
         }
     }
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         final Intent intent = getIntent();
 
         // Do some setup based on the action being performed.
 
         final String action = intent.getAction();
         if (Intent.ACTION_EDIT.equals(action)) {
             // Requested to edit: set that state, and the data being edited.
             mState = STATE_EDIT;
             mUri = intent.getData();
         } else if (Intent.ACTION_INSERT.equals(action)) {
             // Requested to insert: set that state, and create a new entry
             // in the container.
             mState = STATE_INSERT;
             mUri = getContentResolver().insert(intent.getData(), null);
 
             // If we were unable to create a new note, then just finish
             // this activity.  A RESULT_CANCELED will be sent back to the
             // original activity if they requested a result.
             if (mUri == null) {
                 Log.e(TAG, "Failed to insert new note into " + getIntent().getData());
                 finish();
                 return;
             }
             // The new entry was created, so assume all will end well and
             // set the result to be returned.
             setResult(RESULT_OK, (new Intent()).setAction(mUri.toString()));
 
         } else {
             // Whoops, unknown action!  Bail.
             Log.e(TAG, "Unknown action, exiting");
             finish();
             return;
         }
 
         // Set the layout for this activity.  You can find it in res/layout/note_editor.xml
         setContentView(R.layout.note_editor);
         
         // The text view for our note, identified by its ID in the XML file.
         mText = (EditText) findViewById(R.id.note);
 
         // Get the note!
         mCursor = managedQuery(mUri, PROJECTION, null, null, null);
 
         // If an instance of this activity had previously stopped, we can
         // get the original text it started with.
         if (savedInstanceState != null) {
             mOriginalContent = savedInstanceState.getString(ORIGINAL_CONTENT);
         }
         
         // Speech
         CheckForSpeechSystem();
         SetUpSpeechSystem();
         
         // Speech recognition
         PackageManager pm = getPackageManager();
         List<ResolveInfo> activities = pm.queryIntentActivities(
                 new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
         if (activities.size() != 0) {
         	mSpeechRecongiserPresent=true;
         } else {
         	mSpeechRecongiserPresent=false;
         }
         
         //Initialise the Gesture Detector  
         gd = new GestureDetector(this);  
         
        //set the on Double tap listener  
         gd.setOnDoubleTapListener(new OnDoubleTapListener()  
         {  
             public boolean onDoubleTap(MotionEvent e)  
             {  
             	//mText.setText("The screen hass been double tapped.");  
                 return false;  
             }  
   
             public boolean onDoubleTapEvent(MotionEvent e)  
             {  
                 //if the second tap hadn't been released and it's being moved  
                 if(e.getAction() == MotionEvent.ACTION_MOVE)  
                 {  
                     //print a confirmation message and the position  
                 	//mText.setText("Double tap with movement. Position:\n"  
                     //        + "X:" + Float.toString(e.getRawX()) +  
                     //        "\nY: " + Float.toString(e.getRawY()));  
                 }  
                 else if(e.getAction() == MotionEvent.ACTION_UP)//user released the screen  
                 {  
                 	SpellCheckDocument(true);
                 }  
                 return false;  
             }  
    
             public boolean onSingleTapConfirmed(MotionEvent e)  
             {  
             	//mText.setText("Double tap failed. Please try again.");  
                 return false;  
             }  
         });  
     }
     
     @Override
     protected void onResume() {
         super.onResume();
         if (mBlockResume==false)
         {
 	        // If we didn't have any trouble retrieving the data, it is now
 	        // time to get at the stuff.
 	        if (mCursor != null) {
 	            // Make sure we are at the one and only row in the cursor.
 	            mCursor.moveToFirst();
 	
 	            // Modify our overall title depending on the mode we are running in.
 	            if (mState == STATE_EDIT) {
 	                setTitle(getText(R.string.title_edit));
 	            } else if (mState == STATE_INSERT) {
 	                setTitle(getText(R.string.title_create));
 	            }
	
	            // This is a little tricky: we may be resumed after previously being
	            // paused/stopped.  We want to put the new text in the text view,
	            // but leave the user where they were (retain the cursor position
	            // etc).  This version of setText does that for us.
	            String note = mCursor.getString(COLUMN_INDEX_NOTE);
	            mText.setTextKeepState(note);
	            
 	            // If we hadn't previously retrieved the original text, do so
 	            // now.  This allows the user to revert their changes.
 	            if (mOriginalContent == null) {
 	                mOriginalContent = note;
 	            }
 	
 	        } else {
 	            setTitle(getText(R.string.error_title));
 	            mText.setText(getText(R.string.error_message));
 	        }
         }
         else {
         	mBlockResume=false; // allow the next call to this function
         }
     }
 
     @Override
     protected void onSaveInstanceState(Bundle outState) {
         // Save away the original text, so we still have it if the activity
         // needs to be killed while paused.
         outState.putString(ORIGINAL_CONTENT, mOriginalContent);
     }
 
     @Override
     protected void onPause() {
         super.onPause();
 
         // The user is going somewhere else, so make sure their current
         // changes are safely saved away in the provider.  We don't need
         // to do this if only editing.
         if (mCursor != null) {
             String text = mText.getText().toString();
             int length = text.length();
 
             // If this activity is finished, and there is no text, then we
             // do something a little special: simply delete the note entry.
             // Note that we do this both for editing and inserting...  it
             // would be reasonable to only do it when inserting.
             if (isFinishing() && (length == 0) && !mNoteOnly) {
                 setResult(RESULT_CANCELED);
                 deleteNote();
 
             // Get out updates into the provider.
             } else {
                 ContentValues values = new ContentValues();
 
                 // This stuff is only done when working with a full-fledged note.
                 if (!mNoteOnly) {
                     // Bump the modification time to now.
                     values.put(Notes.MODIFIED_DATE, System.currentTimeMillis());
 
                     // If we are creating a new note, then we want to also create
                     // an initial title for it.
                     if (mState == STATE_INSERT) {
                         String title = text.substring(0, Math.min(30, length));
                         if (length > 30) {
                             int lastSpace = title.lastIndexOf(' ');
                             if (lastSpace > 0) {
                                 title = title.substring(0, lastSpace);
                             }
                         }
                         values.put(Notes.TITLE, title);
                     }
                 }
 
                 // Write our text back into the provider.
                 values.put(Notes.NOTE, text);
 
                 // Commit all of our changes to persistent storage. When the update completes
                 // the content provider will notify the cursor of the change, which will
                 // cause the UI to be updated.
                 getContentResolver().update(mUri, values, null, null);
             }
         }
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
 
         // Build the menus that are shown when editing.
         if (mState == STATE_EDIT) {
             menu.add(0, REVERT_ID, 0, R.string.menu_revert)
                     .setShortcut('0', 'r')
                     .setIcon(android.R.drawable.ic_menu_revert);
             if (!mNoteOnly) {
                 menu.add(0, DELETE_ID, 0, R.string.menu_delete)
                         .setShortcut('1', 'd')
                         .setIcon(android.R.drawable.ic_menu_delete);
             }
 
         // Build the menus that are shown when inserting.
         } else {
             menu.add(0, DISCARD_ID, 0, R.string.menu_discard)
                     .setShortcut('2', 'd')
                     .setIcon(android.R.drawable.ic_menu_delete);
         }
         if (mTTS!=null) {
         	menu.add(0, SPEAK_ID, 0, R.string.menu_speak)  
         	.setShortcut('3', 's')
             .setIcon(android.R.drawable.ic_menu_rotate);
         }
         menu.add(0, SPELL_ID, 0, R.string.menu_spellstr)  
     	.setShortcut('4', 's')
         .setIcon(android.R.drawable.ic_menu_compass);
         
         if (mSpeechRecongiserPresent) 
         {
         	menu.add(0, RECOGNIZER_ID, 0, R.string.menu_recognition)  
         	.setShortcut('5', 'r')
             .setIcon(android.R.drawable.ic_media_play);	
         }
 
         // If we are working on a full note, then append to the
         // menu items for any other activities that can do stuff with it
         // as well.  This does a query on the system for any activities that
         // implement the ALTERNATIVE_ACTION for our data, adding a menu item
         // for each one that is found.
         if (!mNoteOnly) {
             Intent intent = new Intent(null, getIntent().getData());
             intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
             menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
                     new ComponentName(this, NoteEditor.class), null, intent, 0, null);
         }
         return true;
     }
     
     public void HandleMessageDialogue(int aMessageID)
     {
 	    AlertDialog.Builder builder;
 		builder = new AlertDialog.Builder (this) ;
 	    builder.setMessage (aMessageID)
 	      .setCancelable (false)
 	      .setPositiveButton (R.string.button_ok, new DialogInterface.OnClickListener () {
 	        public void onClick (DialogInterface dialog, int id)
 	        {
 	          
 	        }}) ;
 	    builder.create () ;
 	    builder.show();
     }
     
     private int mActionVerify;
     public void HandleQuestionMessageDialogue(int aMessageID,int anAction)
     {
     	mActionVerify=anAction;
     	AlertDialog.Builder builder;
     	builder= new AlertDialog.Builder(this);
         //.setIconAttribute(android.R.attr.)
     	builder.setTitle(aMessageID);
         builder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int whichButton) {
             	switch (mActionVerify)
             	{
             	case DELETE_ID:
             		deleteNote();
             		finish();
             		break;
             	case REVERT_ID:
             	case DISCARD_ID:
             		cancelNote();
             		break;
             	case INSTALL_SPEECH_APK_ID:
 	            	{
 	            		Intent installIntent = new Intent();
 	                    installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
 	                    startActivity(installIntent);
 	            	}
             		break;
             	}
             }
         });
         builder.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int whichButton) {
             }
         });
         builder.create();
     	builder.show();
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         // Handle all of the possible menu actions.
         switch (item.getItemId()) {
         case DELETE_ID:
         	HandleQuestionMessageDialogue(R.string.menu_delete,DELETE_ID);
             break;
         case DISCARD_ID:
         	HandleQuestionMessageDialogue(R.string.menu_discard,DISCARD_ID);
             break;
         case REVERT_ID:
         	HandleQuestionMessageDialogue(R.string.menu_revert,REVERT_ID);
             break;
         case SPEAK_ID:
 	        {
 	        	String TheText =  mText.getText().toString();
 	        	int SectionStart= mText.getSelectionStart();
 	        	if (SectionStart>-1){
 	        		int SectionEnd= mText.getSelectionEnd();
 	        		if (SectionEnd<SectionStart) {
 	        			int Temp=SectionEnd;
 	        			SectionEnd=SectionStart;
 	        			SectionStart=Temp;
 	        		}
 	        		if (SectionEnd>SectionStart) {
 	        			TheText=TheText.substring(SectionStart, SectionEnd);
 	        		}
 	        	}
 	        	DoSpeech(TheText);
 	        }
         	break;
         case SPELL_ID:
 	        {
 	        	SpellCheckDocument(true);
         	}
             break;
         case RECOGNIZER_ID:
         	//SpeechInputSelection(); // DEBUG 11/11/11
         	startVoiceRecognitionActivity(); 
         	break;
         }
         return super.onOptionsItemSelected(item);
     }
 
     /**
      * Take care of canceling work on a note.  Deletes the note if we
      * had created it, otherwise reverts to the original text.
      */
     private final void cancelNote() {
         if (mCursor != null) {
             if (mState == STATE_EDIT) {
                 // Put the original note text back into the database
                 mCursor.close();
                 mCursor = null;
                 ContentValues values = new ContentValues();
                 values.put(Notes.NOTE, mOriginalContent);
                 getContentResolver().update(mUri, values, null, null);
             } else if (mState == STATE_INSERT) {
                 // We inserted an empty note, make sure to delete it
                 deleteNote();
             }
         }
         setResult(RESULT_CANCELED);
         finish();
     }
 
     /**
      * Take care of deleting a note.  Simply deletes the entry.
      */
     private final void deleteNote() {
         if (mCursor != null) {
             mCursor.close();
             mCursor = null;
             getContentResolver().delete(mUri, null, null);
             mText.setText("");
         }
     }
     
     // speech
     
     @Override
     public void onDestroy() {
         // Don't forget to shutdown!
         if (mTTS != null) {
         	mTTS.stop();
         	mTTS.shutdown();
         }
 
         super.onDestroy();
     }
 
     public void SetUpSpeechSystem() {
     	mTTS = new TextToSpeech(this,this);
     }
     
     public void onInit(int status) {
     	if (status == TextToSpeech.SUCCESS) {
     		int result = mTTS.setLanguage(Locale.UK);
     		if (result == TextToSpeech.LANG_MISSING_DATA ||
                 result == TextToSpeech.LANG_NOT_SUPPORTED) {
     			result = mTTS.setLanguage(Locale.US);
     			if (result == TextToSpeech.LANG_MISSING_DATA ||
     		        result == TextToSpeech.LANG_NOT_SUPPORTED) {
     				mTTS.shutdown();
     				mTTS=null;
     			}
     		}
     	}
     }
     
     private void DoSpeech(String SomeText) {
     	if (SomeText==null) {
     		SomeText = "No text entered";
     	}
         mTTS.speak(SomeText,TextToSpeech.QUEUE_FLUSH,  // Drop all pending entries in the playback queue.
             null);
     }
     
     private void CheckForSpeechSystem()
     {
     	Intent checkIntent = new Intent();
     	checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
     	startActivityForResult(checkIntent, SPEECH_RECOGNITION_REQUEST_CODE);
     }
     
     // Speech Recognition
     protected void onActivityResult(int requestCode, int resultCode, Intent data) 
     {
         if (requestCode == SPEECH_RECOGNITION_REQUEST_CODE) {
             if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                 // success, create the TTS instance
                 //mTts = new TextToSpeech(this, this);
             } else {
                 // missing data, ask to install it
             	if (mHasAskedSpeechInstalledQuestion==false)
             	{
             		HandleQuestionMessageDialogue(R.string.message_install,INSTALL_SPEECH_APK_ID);
             		mHasAskedSpeechInstalledQuestion=true;
             	}
             }
         }
         else if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
             // Fill the list view with the strings the recogniser thought it could have heard
         	mRegognitionMatches=data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
         	SpeechInputSelection();
         }
     } 
      
     protected void SpeechInputSelection()
     {
     	// Test Code
     	mBlockResume=true; // enable the resume function
     	mSpellWordStart = mText.getSelectionStart();
     	mSpellWordEnd = mSpellWordStart;
 
     	if (mRegognitionMatches==null) {
     		mRegognitionMatches = new ArrayList<String> ();
     		// running in debug
         	mRegognitionMatches.add(0,"Test String");
         	mRegognitionMatches.add(0,"Test String2");
     	}
     	
     	mRegognitionMatches.add(0,"Ignore Input");
     	
     	AlertDialog.Builder builder;
  		builder = new AlertDialog.Builder (this) ;
  		mSuggestedWords = new String[mRegognitionMatches.size()];
  		mSuggestedWords = mRegognitionMatches.toArray(mSuggestedWords) ;
  	    builder.setItems(mSuggestedWords, new DialogInterface.OnClickListener() {
               public void onClick(DialogInterface dialog, int which) {
 
             	  if (which>-1) {
             		  switch (which)
             		  {
             		  case 0: // Skip
             			  break;
             		  default:
 	    	    		String TheText =  mText.getText().toString();
 	    	    		
 	    	    		boolean addExtraSpace=false;
 	    	    		if (mSpellWordStart>0) {
 	    	    			char c = TheText.charAt(mSpellWordStart-1);
 	    	    			if (c!=' ' && c!='\n') {
 	    	    				addExtraSpace=true;
 	    	    			}
 	    	    		}
 	    	    		if (addExtraSpace) {
 	    	    			TheText = TheText.substring(0,mSpellWordStart) + ' ' + mSuggestedWords[which] + ' ' +TheText.substring(mSpellWordEnd);
 	    	    		}
 	    	    		else {
 	    	    			TheText = TheText.substring(0,mSpellWordStart) + mSuggestedWords[which] + ' ' +TheText.substring(mSpellWordEnd);
 	    	    		}
 	    	    		mText.setText(TheText);
 	    	    		int aTextPos=mSpellWordStart+mSuggestedWords[which].length()+1;
 	    	    		if (addExtraSpace) aTextPos++;
 	    	    		mText.setSelection(aTextPos);
 	    	    		break;
     	    		}
             	  }
               }
           });
  	    AlertDialog alert=builder.create () ;
 	    alert.show();
     }
 
     /**
      * Fire an intent to start the speech recognition activity.
      */
     private void startVoiceRecognitionActivity() {
     	mBlockResume=true; // don't run the resume function
         Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
         intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                 RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
         intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "NotePad 2 Speech recognition input");
         startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
     }
 
     
     // spelling
     private void SpellCheckDocument(boolean ResetSpellCheckLocation)
     {
 		if (mText instanceof LinedEditText)
 		{
 			int CurrentCount=0;
 			if (ResetSpellCheckLocation) {
 				((LinedEditText) mText).ResetSearchRangeEnd();
 			}
 			else {
 				CurrentCount= ((LinedEditText) mText).NumberOfUnknowWords();
 			}
 			((LinedEditText) mText).CheckSpellings(true);
 			if (((LinedEditText) mText).NumberOfUnknowWords()==CurrentCount) {
 				// no more unknown words found
 				HandleMessageDialogue(R.string.reached_end);
 			}
 			else {
 				// ok handle unknown word, which should be the highlighted word
 		    	int SectionStart= mText.getSelectionStart();
 		    	if (SectionStart>-1){
 		    		int SectionEnd= mText.getSelectionEnd();
 		    		if (SectionEnd<SectionStart) {
 		    			int Temp=SectionEnd;
 		    			SectionEnd=SectionStart;
 		    			SectionStart=Temp;
 		    		}
 		    		if (SectionEnd>SectionStart) // && SectionEnd>(SectionStart+2)) 
 		    		{
 		    			String TheText =  mText.getText().toString();
 		    			TheText=TheText.substring(SectionStart, SectionEnd);
 		    			mSpellWordStart=SectionStart;
 		    			mSpellWordEnd=SectionEnd;
 		    			HandleChoices(TheText);
 		    		}
 		    	}
 			}
 		}
     }
     
     final Handler mthread_handler = new Handler() {
   	   public void handleMessage(Message msg) 
   	   {
   		   ShowChoicesList();
   	   };
      };
     
     private void HandleChoices(String aWord) 
     {	
 		if (Exists.IsDefined(aWord)) 
 		{
 			HandleMessageDialogue(R.string.message_valid_word);
 			return;
 		}
 		else
     	{
 			mSelectedWord=aWord;
 	        mProgressDialog = ProgressDialog.show(this, "Please wait...", "Checking spelling...", true);
 	    	new Thread() {
 	            public void run() {
 	            	try 
 	            	{
 	            		byte[] aTestWord =CLikeStringUtils.bytesCStringFromString(mSelectedWord.toLowerCase());
 	            		Suggest.suggest (aTestWord) ;
 	            		byte[] result = SuggBasics.getStringFromSuggestions () ;
 	    	    		
 	    		        if (result != null) {
 	    		        	mWordListResultAsString = new String (result, "ISO-8859-1") ;
 	    		        }
 	    		        else
 	    		        { 
 	    		        	mWordListResultAsString=null;
 	    		        }
 	    		        SuggBasics.releaseSuggestions () ;
 	            	}
 	            	catch (UnsupportedEncodingException e) {
 	            		mWordListResultAsString=null;
 	        		}
 	            	mProgressDialog.dismiss();
 	            	mthread_handler.sendEmptyMessage(0);
 	            }
 	       }.start();  
 		}
     }
     
     // This function uses an Alert "list" dialogue box to display the list of choices
     public void ShowChoicesList()
     {
 	    if (mWordListResultAsString==null)
 	    {
 	    	HandleMessageDialogue(R.string.message_nothing_to_check);
 	    }
 	    else
 		{
 	    	mWordListResultAsString = "Add:"+ mSelectedWord +" Skip " + mWordListResultAsString;
 	    	mSuggestedWords = mWordListResultAsString.split(" ");
 	    	AlertDialog.Builder builder;
 	 		builder = new AlertDialog.Builder (this) ;
 	 	    builder.setItems(mSuggestedWords, new DialogInterface.OnClickListener() {
 	              public void onClick(DialogInterface dialog, int which) {
 	
 	            	  if (which>-1) {
 	            		  switch (which)
 	            		  {
 	            		  case 0: // Add
 	            			  Exists.AddWordUserDictionary(mSelectedWord);
 	            			  mText.setSelection(mSpellWordEnd);
 	  		        		  SpellCheckDocument(false);
 	            			  break;
 	            		  case 1: // Skip
 	            			  mText.setSelection(mSpellWordEnd);
 	            			  SpellCheckDocument(false);
 	            			  break;
 	            		  default:
 	    	    			int TheWordDiffLength=mSuggestedWords[which].length()-mSelectedWord.length();
 		    	    		String TheText =  mText.getText().toString();
 		    	    		TheText = TheText.substring(0,mSpellWordStart) + mSuggestedWords[which] + TheText.substring(mSpellWordEnd);
 		    	    		mText.setText(TheText);
 		    	    		int aTextPos=mSpellWordStart+mSuggestedWords[which].length();
 		    	    		mText.setSelection(aTextPos);
 		    	    		
 		    	    		if (mText instanceof LinedEditText)
 		    	    		{
 		    	    			// adjust new max search by length of word
 		    	    			((LinedEditText) mText).IncreaseSearchRangeEnd(TheWordDiffLength);
 		    	    		}
 		    	    		SpellCheckDocument(false);
 		    	    		break;
 	    	    		}
 	            	  }
 	              }
 	          });
 	
 	 	     AlertDialog alert=builder.create () ;
 	 	     alert.show();
 		}
     }
     
     // Gesture routines
     
     @Override
     public boolean onTouchEvent(MotionEvent event)  
     {  
         return gd.onTouchEvent(event);//return the double tap events  
     }  
     
   
     public boolean onDown(MotionEvent e)  
     {  
         return false;  
     }  
   
     public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)  
     {  
         return false;  
     }  
   
     public void onLongPress(MotionEvent e)  
     {  
     }  
   
     public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)  
     {  
         return false;  
     }  
   
     public void onShowPress(MotionEvent e)  
     {  
     }  
   
     public boolean onSingleTapUp(MotionEvent e)  
     {  
         return false;  
     } 
 }
