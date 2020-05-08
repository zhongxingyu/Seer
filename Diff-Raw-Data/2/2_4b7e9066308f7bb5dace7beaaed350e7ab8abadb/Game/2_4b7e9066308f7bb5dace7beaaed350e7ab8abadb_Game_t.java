 /*
         Keep Score: keep track of player scores during a card game. 
         Copyright (C) 2009 Michael Elsdrfer <http://elsdoerfer.name>
         
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
 
 package com.elsdoerfer.keepscore;
 
 import java.lang.reflect.Array;
 
 import android.app.Activity;
 import android.database.Cursor;
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.text.Editable;
 import android.text.method.DigitsKeyListener;
 import android.view.Gravity;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ScrollView;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 
 public class Game extends Activity {	
 	
 	// resources
 	protected Typeface mBoldFace;
 	protected int mCellPadding;
 	
 	// menu items
 	public static final int REMOVE_LAST_ROW_ID = Menu.FIRST;
 	public static final int END_GAME_ID = Menu.FIRST + 1;	
 	protected MenuItem mRemoveLastRowItem;
 	protected MenuItem mEndGameItem;		
 	
 	// static views
 	protected ScrollView mGameScrollView;
 	protected TableLayout mGameTable;
 	protected Button mAddNewScoresButton;
 	
 	// dynamic views
 	protected TableRow mHeaderRow;
 	protected TableRow mFooterRow;
 	protected EditText[] mNewScoreEdits;
 	
 	DbAdapter mDb = new DbAdapter(this);
 	
 	// the session we are currently playing
 	protected Long mSessionId;	
 	// the sessions data, stored temporary in local memory
 	protected CharSequence[] mPlayers = 
 	{"Michael", "Peter", "Paul", "Sebastian"};
 	
 	// Holds the user-entered or automatically calculated 
 	// values for the next new row scores.
 	protected Integer[] mNewScoreValues;
 	
 	// The value the user previously entered into a score field. 
 	// Used  to prefill the fields he might enter next with a default.
 	protected CharSequence mLastEnteredValue = null;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState)  {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.game);
         
         // setup database connection
         mDb = new DbAdapter(this);
         mDb.open();        
         
         // initialize data from state bundle or passed row
         mSessionId = savedInstanceState != null 
         	? savedInstanceState.getLong(DbAdapter.SESSION_ID_KEY)
         	: null;        
         if (mSessionId == null) {
         	Bundle extras = getIntent().getExtras();
         	mSessionId = extras.getLong(DbAdapter.SESSION_ID_KEY);
         }
         
         // load list of players for this game
         mPlayers = mDb.fetchSessionPlayerNames(mSessionId);        
         
         // get views
         mGameScrollView = (ScrollView)findViewById(R.id.game_container);
         mGameTable = (TableLayout)findViewById(R.id.game);
         mAddNewScoresButton = (Button)findViewById(R.id.add_new_scores);
         TableRow.LayoutParams params = (TableRow.LayoutParams)mAddNewScoresButton.getLayoutParams();
         params.span = mPlayers.length;
         mAddNewScoresButton.setLayoutParams(params);       
                 
         // load resources
         mBoldFace = Typeface.defaultFromStyle(Typeface.BOLD);
         mCellPadding = getResources().getDimensionPixelSize(R.dimen.game_table_padding);
         
         // create header row, listing the names of the players
         mHeaderRow = makeTextRow(mPlayers, true);
         mGameTable.addView(mHeaderRow, 0);
         
         // create the edit row, allows adding new scores
         TableRow editRow = new TableRow(this);        
         mNewScoreEdits = (EditText[]) Array.newInstance(EditText.class, mPlayers.length);
         mNewScoreValues = (Integer[]) Array.newInstance(Integer.class, mPlayers.length);
         for (int i=0; i<mPlayers.length; i++) {
         	EditText edit = new EditText(this);
         	edit.setGravity(Gravity.CENTER);
         	// We'd want single-line, most importantly since <enter> would then
         	// jump to the submit button automatically, but alas, there seems to
         	// be a bug in Android 1.0 which causes the hint-text not to show 
         	// if single line is enabled. So for now, don't.
         	// edit.setSingleLine(true);         
         	
         	// Use a DigitsKeyListener to only allow digits, plus add some 
         	// custom key handling. 
         	edit.setKeyListener(new DigitsKeyListener(true, false) {
 
 				@Override
 				public boolean onKeyDown(View view, Editable text, int keyCode,
 						KeyEvent event) {
 					return super.onKeyDown(view, text, keyCode, event);
 				}
 
 				@Override
 				public boolean onKeyUp(View view, Editable text, int keyCode,
 						KeyEvent event) {
 					// If the user presses enter, jump to the submit button 
 					// below. This is basically copied from 
 					// android.widget.TextView.java. It would be normal 
 					// behavior if single-line where true, but we cannot 
 					// use that due to a bug in Android (hints would not show).
 					switch (keyCode) {
 					case KeyEvent.KEYCODE_ENTER:
 						View v = view.focusSearch(View.FOCUS_DOWN);
 						if (v!=null) v.requestFocus(View.FOCUS_DOWN);
 						return true;
 					}
 										
 					boolean result = super.onKeyUp(view, text, keyCode, event);
 					// update user interface to this change
 					updateUI();
 					return result;
 				}
         		
         	});
         	edit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
 				@Override
 				public void onFocusChange(View v, boolean hasFocus) {
 					// When the user enters an edit field, we prefill 
 					// it with the value of the previous field the user
 					// entered text in. This allows for example the 
 					// following workflow: Players A, B, C, D. A and B
 					// get awarded 50 points, C and D lose 50 points.
 					// User focuses edit A, types 50, focuses field B,
 					// field will be set to 50 by this code, user can
 					// add the score right away.
 					EditText edit = (EditText)v;
 					if (edit.isEnabled()) {  // apparently this gets triggered for disabled fields as well?!
 						if (!edit.hasFocus()) {
 							mLastEnteredValue = edit.getText();
 						} else if (mLastEnteredValue != null) {
 							if (edit.getText().length() == 0) {
 								edit.setText(mLastEnteredValue);
 								// TODO: selectAll doesn't have an effect
 								// when entering the field via touch, 
 								// probably because it gets overridden 
 								// right afterwards.
 								edit.selectAll();
 							}
 						}
 					}
 					// Some error messages need to update when the
 					// focus changes, see updateUI comments for more info.
 					updateUI();					
 				}        		
         	});
         	editRow.addView(edit);
         	mNewScoreEdits[i] = edit;
         	mNewScoreValues[i] = null;
         }
         mGameTable.addView(editRow, 1); 
         
         // add existing data rows
         Cursor scoreStream = mDb.fetchSessionScores(mSessionId);
         if (scoreStream.getCount() > 0) {
 	        scoreStream.moveToFirst();
 	        Integer[] currentRow = new Integer[mPlayers.length];
 	        Integer currentPos = 0;
 	        do {
 	        	currentRow[currentPos] = scoreStream.getInt(0);
 	        	currentPos++;
 	        	if (currentPos == currentRow.length) {
 	        		insertScoreRow(makeTextRow(currentRow, false));
 	        		currentPos = 0;
 	        	}
 	        } while (scoreStream.moveToNext());
 	        if (currentPos != 0) {
 	        	// Something is wrong with the database, 
 	        	// there was an invalid number of tokens 
 	        	// in the stream. This shouldn't happen.
 	        	throw new java.lang.IndexOutOfBoundsException(
 	        			"Score stream ended unexpectedly. The " +
 	        			"session data is faulty.");       	
 	        }
         }        
         
         // setup event handlers     
         mAddNewScoresButton.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				// Add new row of scores.
 				// We trust that mNewScoreValues contains no null values.
 				// The submit button should not be enabled if this is not
 				// the case.
 				mDb.addSessionScores(mSessionId, mNewScoreValues);
 				insertScoreRow(makeTextRow(mNewScoreValues, false));		        
 		        
 		        // clear existing input values.
 		        for (int i=0; i<mNewScoreValues.length; i++)	{
 		        	mNewScoreValues[i] = null;
 		        	mNewScoreEdits[i].setText("");
 		        	mLastEnteredValue = null;
 		        }
 		        
 		        // update UI - for some reason, this is one of the few 
 		        // ways we actually managed to scroll to the very bottom.
 		        // In particular, using "fullScroll(FOCUS_DOWN)" never
 		        // scrolled the submit button fully into view, and neither
 		        // did the non-smooth scrolling methods. 
 		        mGameScrollView.smoothScrollBy(0, mGameScrollView.getHeight());
 		        mNewScoreEdits[0].requestFocus();
 		        updateUI();
 			}
         });       
         
         // initial UI initialization
         updateUI();
 	}
 	
     @Override
     protected void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
         outState.putLong(DbAdapter.SESSION_ID_KEY, mSessionId);
         // TODO: save edit field state
     }    
 		
 		
     @Override
     protected void onPause() {   
     	super.onPause();
 		mDb.updateSessionTimestamp(mSessionId);
     }
     
     @Override
     protected void onResume() {   
     	super.onResume();
 		mDb.updateSessionTimestamp(mSessionId);
     }
 			
 	/**
 	 * Make a new row of TextView objects that can be added to the
 	 * table.
 	 * 
 	 * Used to create the score rows for each round, as well as 
 	 * header rows. 
 	 */
 	protected TableRow makeTextRow(Object[] values, boolean header) {
 		TableRow newRow = new TableRow(this);
         for (Object value : values) {
         	TextView text = new TextView(this);
         	text.setText(value.toString());
         	text.setGravity(Gravity.CENTER);
         	if ( header) text.setTypeface(mBoldFace);
         	text.setPadding(mCellPadding, mCellPadding, mCellPadding, mCellPadding);
         	newRow.addView(text);        	
         }
         return newRow;
 	}
 	
 	protected void insertScoreRow(TableRow row) {
 		mGameTable.addView(row, mGameTable.getChildCount()-2);
 		// TODO: add player name header once there are X rows
 	}
 	
 	protected void updateUI() {
 		// Calculate automatic values for the player 
 		// the user did not give a score himself.
 		int numManualScores = 0;  
 		int sumManualScores = 0;  
 		for (int i=0; i<mNewScoreEdits.length; i++) {
 			EditText scoreEdit = mNewScoreEdits[i];
 			String stringValue = scoreEdit.getText().toString();
         	if (stringValue.length() != 0)
 	        	try {
 	        		int intValue = Integer.parseInt(stringValue);
 	        		mNewScoreValues[i] = intValue;
 	        		sumManualScores += intValue;
 	        		numManualScores++;
 	        	} catch (NumberFormatException e) {
 	        		// Android's error message functionality is nice,
 					// but because it is reset every time the user 
 					// changes the text, and this could would immediately
 					// set the error again, you'd see the error popup
 					// constantly hiding/showing as the user types, which
 					// doesn't look good and is slow. So our hacky solution
 					// is to only show set the error if the edit does not
 					// have focus, i.e. once it loses focus.
 					if (!scoreEdit.hasFocus())						
 						scoreEdit.setError(getResources().getString(R.string.not_a_valid_number)); 
 	        	}
         	else {
         		// This particular field has no explicit value, indicate 
         		// as much so that it will later be calculated. 
         		mNewScoreValues[i] = null;
         	}
 		}
 		// Provide default values for the fields currently empty.
 		int numAutomaticValues = mNewScoreValues.length-numManualScores;
 		int sumAutomaticValues = 0;
 		Integer lastSetIndex = null;
 		for (int i=0; i<mNewScoreEdits.length; i++) {
 			EditText scoreEdit = mNewScoreEdits[i];			
 			if (!scoreEdit.isEnabled())
 				scoreEdit.setEnabled(true);
 			
 			// Nothing to suggest if not a single value was provided 
 			// by the user; simply clear all automatic values.
 			if (numManualScores<=0) {
 				scoreEdit.setHint(null);
 				mNewScoreValues[i] = null;
 			}			
 			else {
 				// if this is an empty field, provide it with an automatic value 
 				if (scoreEdit.getText().length() == 0) {					
 					int suggestedValue = -(sumManualScores / numAutomaticValues);					
 					scoreEdit.setHint(String.valueOf(suggestedValue));								
 					mNewScoreValues[i] = suggestedValue;
 					sumAutomaticValues += suggestedValue;
 					lastSetIndex = i;
 					// If there is only one automatic field left, disable it.
 					// This prevents the user from entering unbalanced values.
 					if (numAutomaticValues==1)
 						scoreEdit.setEnabled(false);
 				}
 			}
 		}
 		// We want rows to be balanced, so to account for rounding errors, we
 		// will adjust the value of the last field that we provided a value for.
 		int roundingError = sumAutomaticValues + sumManualScores;
 		if (roundingError != 0) {
			mNewScoreValues[lastSetIndex] -= roundingError;
 			mNewScoreEdits[lastSetIndex].setHint(String.valueOf(mNewScoreValues[lastSetIndex]));
 		}
 
 		// enable submit button if the user provided at least one score
 		mAddNewScoresButton.setEnabled(numManualScores>0);					
 	}
 	
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
     	super.onCreateOptionsMenu(menu);
     	mRemoveLastRowItem = menu.add(0, REMOVE_LAST_ROW_ID, 0, R.string.remove_last_row);
     	mEndGameItem = menu.add(0, END_GAME_ID, 0, R.string.end_game);
     	// setup initial visibilities
         updateUI();
     	return true;
     }
     
     public boolean onOptionsItemSelected(MenuItem item) {
     	switch (item.getItemId()) {
     	case REMOVE_LAST_ROW_ID:
     		return true;
     	case END_GAME_ID:
     		finish();
     		return true;
     	}    	
     	return false;
     }
 
 }
