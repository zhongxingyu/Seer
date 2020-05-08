 /*
  *    Copyright 2012 Michael Potter
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * 
  */
 
 /*
  * RecipeViewer.java - Shows a single recipe, with all its parts: metadata,
  * ingredients, directions, and images. 
  */
 
 package net.potterpcs.recipebook;
 
 import java.util.HashMap;
 
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentTransaction;
 import android.support.v4.view.MenuItemCompat;
 import android.support.v4.widget.SimpleCursorAdapter;
 import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
 import android.text.format.DateUtils;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.FrameLayout;
 import android.widget.GridView;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.RatingBar;
 import android.widget.TextView;
 
 public class RecipeViewer extends FragmentActivity {
 	// Tag for logging
 //	static final String TAG = "RecipeViewer";
 	
 	// Projections for the cursor adapter
 	static final String[] INGREDIENTS_FIELDS = { RecipeData.IT_NAME };
 	static final String[] DIRECTIONS_FIELDS = { RecipeData.DT_STEP, RecipeData.DT_SEQUENCE, RecipeData.DT_PHOTO };
 	static final int[] INGREDIENTS_IDS = { android.R.id.text1 };
 	static final int[] DIRECTIONS_IDS = { R.id.direction, R.id.number, R.id.directionphoto };
 	
 	// The helpfile name
 	private static final String HELP_FILENAME = "viewer";
 
 	private RecipeData data;
 	private long rid;
 	RecipeBook app;
 	TextView rvname;
 	TextView rvcreator;
 	TextView rvserving;
 	TextView rvtime;
 	RatingBar rvrating;
 	GridView lvingredients;
 	ListView lvdirections;
 	FrameLayout rvphoto;
 	String photoUri;
 	
 	@SuppressWarnings("deprecation")
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.recipeviewer);
 
 		app = (RecipeBook) getApplication();
 		data = app.getData();
 
 		// Fill in the UI
 		rvname = (TextView) findViewById(R.id.rvname);
 		rvcreator = (TextView) findViewById(R.id.rvcreator);
 		rvserving = (TextView) findViewById(R.id.rvserving);
 		rvtime = (TextView) findViewById(R.id.rvtime);
 		rvrating = (RatingBar) findViewById(R.id.rvrating);
 		
 		lvingredients = (GridView) findViewById(R.id.ingredients);
 		lvdirections = (ListView) findViewById(R.id.directions);
 		
 		rid = Long.parseLong(getIntent().getData().getLastPathSegment());
 		
 		Cursor mdc = data.getSingleRecipe(rid);
 		startManagingCursor(mdc);
 		mdc.moveToPosition(0);
 		rvname.setText(mdc.getString(mdc.getColumnIndex(RecipeData.RT_NAME)));
 		rvcreator.setText(mdc.getString(mdc.getColumnIndex(RecipeData.RT_CREATOR)));
 		rvserving.setText(mdc.getString(mdc.getColumnIndex(RecipeData.RT_SERVING)));
 		rvtime.setText(DateUtils.formatElapsedTime(
 				mdc.getLong(mdc.getColumnIndex(RecipeData.RT_TIME))));
 		rvrating.setRating(mdc.getFloat(mdc.getColumnIndex(RecipeData.RT_RATING)));
 		
 		photoUri = mdc.getString(mdc.getColumnIndex(RecipeData.RT_PHOTO));
 		if (photoUri != null && !photoUri.equals("")) {
 			rvphoto = (FrameLayout) findViewById(R.id.photofragment);
 			ImageView iv = new ImageView(this);
 			setOrDownloadImage(iv, photoUri);
 			iv.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
 					PhotoDialog pd = PhotoDialog.newInstance(photoUri);
 					pd.show(ft, "dialog");
 				}
 			});
 			rvphoto.addView(iv);
 		}
 
 		Cursor dirc = data.getRecipeDirections(rid);
 		startManagingCursor(dirc);
 		SimpleCursorAdapter directions = new SimpleCursorAdapter(this, R.layout.recipedirectionrow,
 				dirc, DIRECTIONS_FIELDS, DIRECTIONS_IDS, 0);
 		directions.setViewBinder(new DirectionViewBinder(this));
 		
 		lvdirections.setAdapter(directions);
 		lvdirections.setDividerHeight(0);
 		
 		Cursor ingc = data.getRecipeIngredients(rid);
 		startManagingCursor(ingc);
 		SimpleCursorAdapter ingredients = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, 
 				ingc, INGREDIENTS_FIELDS, INGREDIENTS_IDS, 0);
 		lvingredients.setAdapter(ingredients);
 		
 	}
 	
 	@Override
 	protected void onResume() {
 		super.onResume();
 		lvdirections.setOnItemClickListener(new OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view,
 					int position, long id) {
 				Cursor c = data.getRecipeDirections(rid);
 				c.moveToPosition(position);
 				String uri = c.getString(c.getColumnIndex(RecipeData.DT_PHOTO));
				if (uri != null) {
 					FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
 					PhotoDialog pd = PhotoDialog.newInstance(uri);
 					pd.show(ft, "dialog");
 				}
 				c.close();
 			}
 		});
 
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.viewermenu, menu);
 		
 		// Set up the action bar if we have one
 		MenuItemCompat.setShowAsAction(menu.findItem(R.id.viewertimer), 
 				MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
 		MenuItemCompat.setShowAsAction(menu.findItem(R.id.viewerhelp), 
 				MenuItem.SHOW_AS_ACTION_IF_ROOM);
 		return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.viewertimer:
 			onTimerSelected(item);
 			return true;
 		case R.id.viewerhelp:
 			onHelpItemSelected(item);
 			return true;
 		case android.R.id.home:
 			Intent intent = new Intent(this, RecipeBookActivity.class);
             intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
             startActivity(intent);
             return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 	
 	public void onHelpItemSelected(MenuItem item) {
 		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
 		DialogFragment helpFragment = HelpDialog.newInstance(HELP_FILENAME);
 		helpFragment.show(ft, "help");
 	}
 
 	public void onTimerSelected(MenuItem item) {
     	FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
     	Fragment timerFragment = new TimerFragment();
     	transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
     	transaction.replace(R.id.timerfragment, timerFragment);
     	transaction.addToBackStack(null);
     	transaction.commit();
 	}
 	
 	private void setOrDownloadImage(ImageView iv, String photoUri) {
 		RecipeBook.setImageViewBitmapDecoded(this, iv, photoUri);
 	}
 	
 	public class DirectionViewBinder implements ViewBinder {
 		// This class helps set up the directions. The main point of the class
 		// is to prevent repeated loads, downloads, and scales of direction
 		// photos. This lowers data usage and memory usage.
 		FragmentActivity parent;
 		HashMap<View, Boolean> boundViews;
 		
 		public DirectionViewBinder(FragmentActivity a) {
 			parent = a;
 			boundViews = new HashMap<View, Boolean>();
 		}
 
 		@Override
 		public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
 			boolean isBound = false;
 			if (boundViews.get(view) != null) {
 				isBound = boundViews.get(view);
 			}
 			
 			switch (view.getId()) {
 			case R.id.direction:
 				if (!isBound && view instanceof TextView) {
 					((TextView) view).setText(cursor.getString(columnIndex));
 					boundViews.put(view, true);
 				}
 				break;
 			case R.id.number:
 				if (!isBound && view instanceof TextView) {
 					((TextView) view).setText(Integer.toString(cursor.getInt(columnIndex)));
 					boundViews.put(view, true);
 				}
 			case R.id.directionphoto:
 				if (!isBound && view instanceof ImageView) {
 					RecipeBook.setImageViewBitmapDecoded(parent, (ImageView) view, 
 							cursor.getString(columnIndex), 160);
 					boundViews.put(view, true);
 				}
 				break;
 			default:
 				return false;
 			}
 			return true;
 		}		
 	}
 }
