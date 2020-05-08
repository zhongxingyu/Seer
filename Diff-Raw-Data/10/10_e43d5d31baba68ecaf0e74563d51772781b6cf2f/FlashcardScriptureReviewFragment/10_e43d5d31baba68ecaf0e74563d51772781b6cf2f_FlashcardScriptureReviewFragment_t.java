 package com.app.wordservant;
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Locale;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteException;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.ViewSwitcher;
 
 public class FlashcardScriptureReviewFragment extends Fragment {
 	private TextView mEditScriptureReference;
 	private TextView mEditCategory;
 	private TextView mEditScripture;
 	private Integer mFirstSelectedScriptureId;
 	private SQLiteDatabase mDatabaseConnection;
 	private Cursor mUnreviewedScriptureQuery;
 
 	public void onViewCreated(View view, Bundle savedInstanceState){
 		super.onViewCreated(view, savedInstanceState);
 
 		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
 		//Display flashcards if the setting is enabled.
 		if(sharedPreferences.getString("pref_key_review_select", "none").equals("showing_reference") ||
 				sharedPreferences.getString("pref_key_review_select", "none").equals("showing_scripture")){
 
 			//Set up click events, as well as fields.
 			final ViewSwitcher cardFlipper = (ViewSwitcher) getView().findViewById(R.id.cardSwitcher);
 			OnClickListener flipViewListener = new OnClickListener(){
 				@Override
 				public void onClick(View v) {
 					// TODO Auto-generated method stub
 					if(cardFlipper.getDisplayedChild()==0)
 						cardFlipper.setDisplayedChild(1);
 					else
 						cardFlipper.setDisplayedChild(0);
 				}
 			};
 			LinearLayout referenceLayout = (LinearLayout) getView().findViewById(R.id.referenceLayout);
 			TextView scriptureText = (TextView) getView().findViewById(R.id.scriptureText);
 			referenceLayout.setOnClickListener(flipViewListener);
 			scriptureText.setOnClickListener(flipViewListener);
 			((Button) getView().findViewById(R.id.flipCardButton)).setOnClickListener(flipViewListener);
 
 			mEditScriptureReference = (TextView) getView().findViewById(R.id.referenceText);
 			mEditCategory = (TextView) getView().findViewById(R.id.scriptureTags);
 			mEditScripture = (TextView) getView().findViewById(R.id.scriptureText);
 
 			//Set up coloring.
 			//Define the front and back of the cards.
 			LinearLayout frontCard;
 			LinearLayout backCard;
 			if(sharedPreferences.getString("pref_key_review_select", "none").equals("showing_scripture")){
 				cardFlipper.setDisplayedChild(1);
 				frontCard = (LinearLayout) cardFlipper.findViewById(R.id.scriptureLayout);
 				backCard = (LinearLayout) cardFlipper.findViewById(R.id.referenceLayout);
 			}else{
 				cardFlipper.setDisplayedChild(0);
 				frontCard = (LinearLayout) cardFlipper.findViewById(R.id.referenceLayout);
 				backCard = (LinearLayout) cardFlipper.findViewById(R.id.scriptureLayout);
 			}
 
 			//Change the color for these cards accordingly.
 			frontCard.setBackgroundColor(getResources().getColor(R.color.card_front_background_color));
 			for (int i=0;i<frontCard.getChildCount();i++){
 				TextView textView = (TextView) frontCard.getChildAt(i);
 				textView.setBackgroundColor(getResources().getColor(R.color.card_front_background_color));
 				textView.setTextColor(getResources().getColor(R.color.card_front_text_color));
 			}
 			backCard.setBackgroundColor(getResources().getColor(R.color.card_back_background_color));
 			for (int i=0;i<backCard.getChildCount();i++){
 				TextView textView = (TextView) backCard.getChildAt(i);
 				textView.setBackgroundColor(getResources().getColor(R.color.card_back_background_color));
 				textView.setTextColor(getResources().getColor(R.color.card_back_text_color));
 			}
 		}else{
 			mEditScriptureReference = (TextView) getView().findViewById(R.id.dueTodayScriptureReference);
 			mEditCategory = (TextView) getView().findViewById(R.id.dueTodayTags);
 			mEditScripture = (TextView) getView().findViewById(R.id.scriptureText);
 		}
 	}
 
 	public void onStart(){
 		super.onStart();
 
 		//Get the date for today.
 		SimpleDateFormat dbDateFormat = new SimpleDateFormat(getResources().getString(R.string.date_format), Locale.US);
 		dbDateFormat.format(Calendar.getInstance().getTime());
 
 		//Open the database.
 		mDatabaseConnection = new WordServantDbHelper(getActivity(), WordServantContract.DB_NAME, null, WordServantDbHelper.DATABASE_VERSION).getReadableDatabase();
 		Integer positionOnScreen = getActivity().getIntent().getIntExtra("positionOnScreen", 0);
 		final Bundle bundledScriptureList = getActivity().getIntent().getBundleExtra("bundledScriptureList");
 		mFirstSelectedScriptureId = bundledScriptureList.getInt(String.valueOf(positionOnScreen));
 
 		displayScriptureContent(mFirstSelectedScriptureId);
 
 		//Check all unreviewedScriptures.
 		String [] columnsToRetrieve = {WordServantContract.ScriptureEntry._ID,
 				WordServantContract.ScriptureEntry.COLUMN_NAME_NEXT_REVIEW_DATE};
 		mUnreviewedScriptureQuery = mDatabaseConnection.query(
 				WordServantContract.ScriptureEntry.TABLE_NAME, 
 				columnsToRetrieve, 
 				WordServantContract.ScriptureEntry.COLUMN_NAME_NEXT_REVIEW_DATE+"=date('now') OR "+
 						WordServantContract.ScriptureEntry._ID+"="+mFirstSelectedScriptureId, 
 						null, null, null, null);
 
 		//Set the row in the unreviewed scripture query to match the selected item in Today's Memory Verses.
 		mUnreviewedScriptureQuery.moveToFirst();
 		while(mUnreviewedScriptureQuery.getInt(0)!=bundledScriptureList.getInt(String.valueOf(positionOnScreen))){
 			mUnreviewedScriptureQuery.moveToNext();
 			if(mUnreviewedScriptureQuery.isAfterLast()){
 				mUnreviewedScriptureQuery.moveToFirst();
 			}
 		}
 
 		Button nextButton = (Button) getActivity().findViewById(R.id.dueTodayNextButton);
 		if(mUnreviewedScriptureQuery.getCount()==1){
 			nextButton.setVisibility(Button.GONE);
 		}else{
 			nextButton.setOnClickListener(new OnClickListener(){
 
 				@Override
 				public void onClick(View view) {
 					setNextScriptureId();
 					displayScriptureContent(mUnreviewedScriptureQuery.getInt(0));
 				}
 
 			});
 		}
 		Button reviewedButton = (Button) getActivity().findViewById(R.id.dueTodayFinishedButton);
 		reviewedButton.setOnClickListener(new OnClickListener(){
 
 			@Override
 			public void onClick(View view) {
 				//Update the database.
				Cursor dateQuery = mDatabaseConnection.rawQuery("SELECT date('now')",null);
				dateQuery.moveToFirst();
				if(mUnreviewedScriptureQuery.getString(mUnreviewedScriptureQuery.getColumnIndex(WordServantContract.ScriptureEntry.COLUMN_NAME_NEXT_REVIEW_DATE)).equals(dateQuery.getString(0))){
					updateReviewedScripture(getActivity(), mUnreviewedScriptureQuery.getInt(0), true);
				}
 				
 				if(mUnreviewedScriptureQuery.getInt(0) == mFirstSelectedScriptureId){
 					mFirstSelectedScriptureId = -1;
 				}
 				
 				if(mUnreviewedScriptureQuery.getCount()==1){
 					getActivity().finish();
 					return;
 				}
 				
 				String [] columnsToRetrieve = {WordServantContract.ScriptureEntry._ID};
 				mUnreviewedScriptureQuery = mDatabaseConnection.query(
 						WordServantContract.ScriptureEntry.TABLE_NAME, 
 						columnsToRetrieve, 
 						WordServantContract.ScriptureEntry.COLUMN_NAME_NEXT_REVIEW_DATE+"=date('now') OR "+
 								WordServantContract.ScriptureEntry._ID+"="+mFirstSelectedScriptureId,
 								null, null, null, null);
 				mUnreviewedScriptureQuery.moveToFirst();
 				Button nextButton = (Button) getActivity().findViewById(R.id.dueTodayNextButton);
 				if(mUnreviewedScriptureQuery.getCount()==1){
 					nextButton.setVisibility(Button.GONE);
 				}
 				
 				//Select the next.
 				setNextScriptureId();
 				displayScriptureContent(mUnreviewedScriptureQuery.getInt(0));
 				
 
 			}
 
 		});
 	}
 
 	public static void updateReviewedScripture(Context context, int scriptureId, boolean increment){
 		SQLiteDatabase wordservant_db = new WordServantDbHelper(context, WordServantContract.DB_NAME, null, WordServantDbHelper.DATABASE_VERSION).getWritableDatabase();
 		String plusMinus = increment?"+":"-";
 		wordservant_db.execSQL("update "+WordServantContract.ScriptureEntry.TABLE_NAME+" set "+
 				WordServantContract.ScriptureEntry.COLUMN_NAME_TIMES_REVIEWED+"="+
 				WordServantContract.ScriptureEntry.COLUMN_NAME_TIMES_REVIEWED+"+" +
 				plusMinus+
 				"1 where "+WordServantContract.ScriptureEntry._ID+"="+scriptureId);
 
 		wordservant_db.close();
 	}
 
 	protected void displayScriptureContent(int scriptureId) {
 		// TODO Auto-generated method stub
 		try{		
 			String [] columns_to_retrieve = {
 					WordServantContract.ScriptureEntry.COLUMN_NAME_REFERENCE,
 					WordServantContract.ScriptureEntry.COLUMN_NAME_TEXT,
					WordServantContract.ScriptureEntry._ID,
					WordServantContract.ScriptureEntry.COLUMN_NAME_NEXT_REVIEW_DATE};
 			Cursor scriptureQuery = mDatabaseConnection.query(
 					WordServantContract.ScriptureEntry.TABLE_NAME, 
 					columns_to_retrieve, 
 					WordServantContract.ScriptureEntry._ID+"="+scriptureId, 
 					null, null, null, null);
 			scriptureQuery.moveToFirst();
 			mEditScriptureReference.setText(scriptureQuery.getString(0));
 
 			//Set the tag text.
 			/*String tagText = "";
 			Cursor tagTextQuery = mDatabaseConnection.rawQuery("select "+WordServantContract.TagEntry.COLUMN_NAME_TAG_NAME+
 					" from "+WordServantContract.CategoryEntry.TABLE_NAME+" c join "+
 					WordServantContract.ScriptureEntry.TABLE_NAME+" s on "+
 					"(c."+WordServantContract.CategoryEntry.COLUMN_NAME_SCRIPTURE_ID+"=s."+
 					WordServantContract.ScriptureEntry._ID+") join "+
 					WordServantContract.TagEntry.TABLE_NAME+" t on (c."+WordServantContract.CategoryEntry.COLUMN_NAME_TAG_ID+
 					"=t."+WordServantContract.TagEntry._ID+") where "+
 					"s."+WordServantContract.ScriptureEntry._ID+"="+scriptureQuery.getInt(2),null);
 			for(int i=0;i<tagTextQuery.getCount();i++){
 				tagTextQuery.moveToPosition(i);
 				tagText += tagTextQuery.getString(0);
 				if(i<tagTextQuery.getCount()-1)
 					tagText += ", ";
 			}
 			editCategory.setText(tagText);*/
 			mEditScripture.setText(scriptureQuery.getString(1));
 		} catch(SQLiteException e){
 			System.err.println("Database issue..");
 			e.printStackTrace();
 		}
 	}
 
 	private void setNextScriptureId(){
 		//If there are no or just one we just need to display a message and return that scripture id.
 		if(mUnreviewedScriptureQuery.getCount()==0){
 			return;
 		}else{
 			mUnreviewedScriptureQuery.moveToNext();
 			if(mUnreviewedScriptureQuery.isAfterLast()){
 				mUnreviewedScriptureQuery.moveToFirst();
 			}
 		}
 	}
 
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
 		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
 		if(sharedPreferences.getString("pref_key_review_select", "none").equals("showing_reference") ||
 				sharedPreferences.getString("pref_key_review_select", "none").equals("showing_scripture")){
 			return inflater.inflate(R.layout.flashcard_layout, null);
 		}
 		return inflater.inflate(R.layout.layout_no_edit_scripture, null);
 	}
 
 	public void onDestroy(){
 		super.onDestroy();
 
 		//Close the DB connection.
 		mDatabaseConnection.close();
 	}
 }
