 package com.example.mobileindia;
 
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Build;
 import android.os.Bundle;
 import android.support.v4.app.NavUtils;
 import android.text.TextUtils;
 import android.text.format.DateFormat;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.DatePicker;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.parse.FindCallback;
 import com.parse.ParseObject;
 import com.parse.ParseQuery;
 
 public class SearchPostActivity extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_search_post);
 		// Show the Up button in the action bar.
 		setupActionBar();
 	}
 
 	/**
 	 * Set up the {@link android.app.ActionBar}, if the API is available.
 	 */
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
 	private void setupActionBar() {
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
 			getActionBar().setDisplayHomeAsUpEnabled(true);
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.search_post, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			// This ID represents the Home or Up button. In the case of this
 			// activity, the Up button is shown. Use NavUtils to allow users
 			// to navigate up one level in the application structure. For
 			// more details, see the Navigation pattern on Android Design:
 			//
 			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
 			//
 			NavUtils.navigateUpFromSameTask(this);
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 	
 	public void performSearch(View view) {
 		ListViewCategory.forceSearch = false;
 		TextView mPostNumberField = (TextView) findViewById(R.id.search_post_number_field);
 		TextView mPostDateField = (TextView) findViewById(R.id.search_post_date_field);
 		TextView mPostKeywordsField = (TextView) findViewById(R.id.search_post_keywords_field);
 		TextView mPostAuthorField = (TextView) findViewById(R.id.search_post_author_field);
 		List<ParseQuery> queryList = new ArrayList<ParseQuery>();
 		boolean cancel = false;
 		View focusView = null;
 		if(!TextUtils.isEmpty(mPostNumberField.getText().toString())){
 			ParseQuery numberQuery = new ParseQuery("Post");
 			numberQuery.whereEqualTo("post_num", Integer.parseInt(mPostNumberField.getText().toString()));
 			queryList.add(numberQuery);
 		}
 		if(!TextUtils.isEmpty(mPostDateField.getText().toString())){
 			new DateFormat();
 			java.text.DateFormat format = DateFormat.getDateFormat(getApplicationContext());
 			try {
 				java.util.Date parsedDate = format.parse(mPostDateField.getText().toString());
 				ParseQuery dateQuery = new ParseQuery("Post");
 				dateQuery.whereEqualTo("date", parsedDate);
 				queryList.add(dateQuery);
 			} catch (ParseException e) {
 				mPostDateField.setError("Not a valid date. Plese specify a valid date.");
 				focusView = mPostDateField;
 				cancel = true;
 			}
 			
 		}
 		if(!TextUtils.isEmpty(mPostKeywordsField.getText().toString())){
 			String keywords = mPostKeywordsField.getText().toString();
 			String[] splited = keywords.split(",");
			ParseQuery summaryQuery = new ParseQuery("Post");
			ParseQuery titleQuery = new ParseQuery("Post");
			summaryQuery.whereMatches("summary", TextUtils.join("|", splited),"i");
			titleQuery.whereMatches("title", TextUtils.join("|", splited), "i");
			queryList.add(summaryQuery);
			queryList.add(titleQuery);
 		}
 		if(!TextUtils.isEmpty(mPostAuthorField.getText().toString())){
 			String author = mPostAuthorField.getText().toString();
 			ParseQuery authorQuery = new ParseQuery("Post");
 			authorQuery.whereContains("author", author);
 			queryList.add(authorQuery);
 		}
 		if (cancel) {
 			focusView.requestFocus();
 		} else {
 			findViewById(R.id.btnSearchPosts).setClickable(false);
 			if (queryList.size() == 0) {
 				 final Context context = getApplicationContext();
 				 final int duration = Toast.LENGTH_LONG;
 				 Toast toast = Toast.makeText(context, "No Query Provided. Plase fill a search field above.", duration);
 				 toast.show();
 				 findViewById(R.id.btnSearchPosts).setClickable(true);
 			}else{
 				ParseQuery orQuery = ParseQuery.or(queryList);
 				ListViewCategory.parsePostList = null;
 				final Intent i = new Intent(this,ListViewCategory.class);
 				orQuery.findInBackground(new FindCallback() {
 					@Override
 					public void done(List<ParseObject> objects, com.parse.ParseException e) {
 						if (e == null) {
 							if (objects.size() == 0) {
 								final Context context = getApplicationContext();
 								final int duration = Toast.LENGTH_LONG;
 								Toast toast = Toast.makeText(context, "No posts found with that search query. Please loosen your query.", duration);
 								toast.show();
 								findViewById(R.id.btnSearchPosts).setClickable(true);
 							} else {
 								ListViewCategory.hideAdd = true;
 								ListViewCategory.parsePostList = objects;
 								ListViewCategory.forceSearch = true;
 								startActivity(i);
 								findViewById(R.id.btnSearchPosts).setClickable(true);
 							}
 						} else {
 							 final Context context = getApplicationContext();
 							 final int duration = Toast.LENGTH_LONG;
 							 Toast toast = Toast.makeText(context, "There was an error while searching posts. Please try again.", duration);
 							 toast.show();
 							 findViewById(R.id.btnSearchPosts).setClickable(true);
 						}
 						findViewById(R.id.btnSearchPosts).setClickable(true);
 					}
 				});
 			}
 		}
 	}
 	public void firePicker(View view){
//		DatePicker d = new DatePicker(getApplicationContext());
//		d.setEnabled(true);
 	}
     @Override
     public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if(keyCode == KeyEvent.KEYCODE_BACK){
         	Intent back = new Intent(this,MainActivity.class);
             startActivity(back);
         }
         return super.onKeyDown(keyCode, event);
     }
 }
