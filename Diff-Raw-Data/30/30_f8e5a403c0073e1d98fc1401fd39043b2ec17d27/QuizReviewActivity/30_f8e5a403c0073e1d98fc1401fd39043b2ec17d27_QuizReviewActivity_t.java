 package com.app.wordservant.ui;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 import android.content.ContentValues;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.support.v4.app.LoaderManager;
 import android.support.v4.app.NavUtils;
 import android.support.v4.content.CursorLoader;
 import android.support.v4.content.Loader;
 import android.text.Html;
 import android.util.Log;
 
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.app.wordservant.R;
 import com.app.wordservant.provider.WordServantContract;
 
 public class QuizReviewActivity extends SherlockFragmentActivity implements LoaderManager.LoaderCallbacks<Cursor>{
 	private ArrayList<Integer> mScriptureIds;
 	private Cursor mQuizData;
 	private Integer mCurrentScriptureId;
 	private int mCurrentScriptureReviewCount;
 	private int mCurrentScriptureSkipCount;
 	private int mCurrentScriptureIncorrectCount;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_quiz_review);
 		// Show the Up button in the action bar.
 		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
 
 		mQuizData = new CursorLoader(this){
 			public Cursor loadInBackground(){
 				//Open the database.
 
 				String [] columns = {
 						WordServantContract.ScriptureEntry._ID,
 				};
 				String selection = WordServantContract.ScriptureEntry.COLUMN_NAME_NEXT_REVIEW_DATE+"<=date('now','localtime') OR "+
 						WordServantContract.ScriptureEntry.COLUMN_NAME_TIMES_REVIEWED+">0";
 				return getContentResolver().query(
 						WordServantContract.ScriptureEntry.CONTENT_URI, 
 						columns, selection, null, null);
 			}
 		}.loadInBackground();
 
 		if(mQuizData.getCount()==0)
 			finish();
 		
 		//Pick a random card.
 		
 
 		//Get all scriptures for the quiz.
 		mScriptureIds = new ArrayList<Integer>();
 		for(int i=0;i<mQuizData.getCount();i++){
 			mQuizData.moveToPosition(i);
 			mScriptureIds.add(mQuizData.getInt(0));
 		}
 		this.invalidateOptionsMenu();
 	}
 
 	public void onStart(){
 		super.onStart();
 		
 		//Randomly pick a scripture to display.
 		mCurrentScriptureId = mScriptureIds.get((int) getRandomIdIndex(mScriptureIds.size()));
 		getSupportLoaderManager().restartLoader(0, null, this);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getSupportMenuInflater().inflate(R.menu.activity_quiz_review, menu);
 		return true;
 	}
 	public boolean onPrepareOptionsMenu(Menu menu){
 		super.onPrepareOptionsMenu(menu);
 		if(mScriptureIds!=null && mScriptureIds.size()<=1){
 			menu.getItem(2).setEnabled(false);
 		}
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		
 		//Update missed.
 		ContentValues contentValues = new ContentValues();
 		Intent intent;
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			// This ID represents the Home or Up button. In the case of this
 			// activity, the Up button is shown. Use NavUtils to allow users
 			// to navigate up one level in the application structure. For
 			// more details, see the Navigation pattern on Android Design:
 			//
 			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
 			NavUtils.navigateUpFromSameTask(this);
 			return true;
 		case R.id.reviewed:
 			//Put the new values in.
 			contentValues.put(
 					WordServantContract.ScriptureEntry.COLUMN_NAME_CORRECTLY_REVIEWED_COUNT,
 					mCurrentScriptureReviewCount+1);
 			mScriptureIds.remove(mCurrentScriptureId);
 			getContentResolver().update(
 					Uri.withAppendedPath(WordServantContract.ScriptureEntry.CONTENT_ID_URI_BASE,String.valueOf(mCurrentScriptureId)),
 					contentValues, null, null);
 			if(mScriptureIds.size()==0){
 				finish();
 				return super.onOptionsItemSelected(item);
 			}
 			mCurrentScriptureId = mScriptureIds.get((int) getRandomIdIndex(mScriptureIds.size()));
 			getSupportLoaderManager().restartLoader(0, null, this);
 			this.invalidateOptionsMenu();
 			break;
 		case R.id.skip:
 			contentValues.put(
 					WordServantContract.ScriptureEntry.COLUMN_NAME_SKIPPED_REVIEW_COUNT,
 					mCurrentScriptureSkipCount+1);
 			getContentResolver().update(
 					Uri.withAppendedPath(WordServantContract.ScriptureEntry.CONTENT_ID_URI_BASE,String.valueOf(mCurrentScriptureId)),
 					contentValues, null, null);
 			if(mScriptureIds.size()==0){
 				finish();
 				return super.onOptionsItemSelected(item);
 			}
 			mCurrentScriptureId = mScriptureIds.get((int) getRandomIdIndex(mScriptureIds.size()));
 			getSupportLoaderManager().restartLoader(0, null, this);
 			this.invalidateOptionsMenu();
 			break;
 		case R.id.missed:
 			contentValues.put(
 					WordServantContract.ScriptureEntry.COLUMN_NAME_INCORRECTLY_REVIEWED_COUNT,
 					mCurrentScriptureIncorrectCount+1);
 			mScriptureIds.remove(mCurrentScriptureId);
 			getContentResolver().update(
 					Uri.withAppendedPath(WordServantContract.ScriptureEntry.CONTENT_ID_URI_BASE,String.valueOf(mCurrentScriptureId)),
 					contentValues, null, null);
 			if(mScriptureIds.size()==0){
 				finish();
 				return super.onOptionsItemSelected(item);
 			}
 			mCurrentScriptureId = mScriptureIds.get((int) getRandomIdIndex(mScriptureIds.size()));
 			getSupportLoaderManager().restartLoader(0, null, this);
 			this.invalidateOptionsMenu();
 		case R.id.statistics:
 			intent = new Intent(this, StatisticsActivity.class);
 			startActivity(intent);
 		}
 
 		return super.onOptionsItemSelected(item);
 	}
 
 	private Integer getRandomIdIndex(Integer size) {
 		// TODO Auto-generated method stub
 		if(size<=1)
 			return 0;
 		return ((Integer) Math.abs(new Random().nextInt()) % size);
 	}
 
 	@Override
 	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
 		// TODO Auto-generated method stub
 		String [] columns_to_retrieve = {WordServantContract.ScriptureEntry._ID, 
 				WordServantContract.ScriptureEntry.COLUMN_NAME_REFERENCE,
 				WordServantContract.ScriptureEntry.COLUMN_NAME_TEXT,
 				WordServantContract.ScriptureEntry.COLUMN_NAME_CORRECTLY_REVIEWED_COUNT,
 				WordServantContract.ScriptureEntry.COLUMN_NAME_SKIPPED_REVIEW_COUNT,
 				WordServantContract.ScriptureEntry.COLUMN_NAME_INCORRECTLY_REVIEWED_COUNT};
 		return new CursorLoader(this, 
 				Uri.withAppendedPath(WordServantContract.ScriptureEntry.CONTENT_ID_URI_BASE,String.valueOf(mCurrentScriptureId)), 
 				columns_to_retrieve, 
 				null, null, null);
 	}
 
 	@Override
 	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
 		// TODO Auto-generated method stub
 		data.moveToFirst();
 		ReviewFragment reviewFragment = (ReviewFragment) getSupportFragmentManager().findFragmentById(R.id.quizReviewFragment);
		reviewFragment.resetView();
 		reviewFragment.setScriptureReference(data.getString(1));
 		reviewFragment.setScriptureText(Html.fromHtml(data.getString(2)));
 		mCurrentScriptureReviewCount = data.getInt(3);
 		mCurrentScriptureSkipCount = data.getInt(4);
 		mCurrentScriptureIncorrectCount = data.getInt(5);
 	}
 
 	@Override
 	public void onLoaderReset(Loader<Cursor> loader) {
 		// TODO Auto-generated method stub
 		Log.d("WordServant","loader reset");
 	}
 
 }
