 /* 
  * Copyright 2011 Google Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */package com.galactogolf;
 
 import java.util.ArrayList;
 import java.util.UUID;
 
 import com.galactogolf.R;
 import com.galactogolf.database.DatabaseAdapter;
 import com.galactogolf.database.DatabaseException;
 import com.galactogolf.genericobjectmodel.levelloader.LevelLoadingException;
 import com.galactogolf.genericobjectmodel.levelloader.LevelSet;
 import com.galactogolf.specificobjectmodel.GalactoGolfLevelSetResult;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.ColorStateList;
 import android.content.res.Resources.NotFoundException;
 import android.graphics.Color;
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.AdapterView.OnItemClickListener;
 
 /*
  * Shows a list of the available levels for the user to pick from.
  */
 public class LevelSetSelectActivity extends Activity {
 
 	private static final int MENU_CREATE_ID = 0;
 
 	private ArrayList<LevelSet> _levelSets = null;
 	DatabaseAdapter adapter;
 	Typeface typeface;
 	ListView levelList;
 	final Activity thisActivity = this;
 
 	public void onCreate(Bundle icicle) {
 		super.onCreate(icicle);
 		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
 				WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.level_select_layout);
 		// Create an array of Strings, that will be put to our ListActivity
 		adapter = new DatabaseAdapter(this);
 		adapter.open();
 		_levelSets = new ArrayList<LevelSet>();
 		_levelSets.addAll(loadLevels());
 		adapter.close();
 		// Create an ArrayAdapter, that will actually make the Strings above
 		// appear in the ListView
 		levelList = (ListView) findViewById(R.id.level_set_list);
 		if (_levelSets != null) {
 			levelList.setAdapter(new LevelSetAdapter(this,
 					R.layout.level_set_select_row, _levelSets));
 		}
 		levelList.setOnItemClickListener(new OnItemClickListener() {
 
 			public void onItemClick(AdapterView<?> l, View v, int position,
 					long id) {
 				LevelSet levelSet = _levelSets.get((int) id);
 				LevelSet prevLevelSet = null;
 				if (id >= 2) {
 					prevLevelSet = _levelSets.get((int) id - 1);
 				}
 				try {
 					if (prevLevelSet == null
 							|| prevLevelSet.isCompleted(thisActivity)
 							|| UserPreferences.EditorEnabled) {
 						startDetailsActivity(levelSet);
 					}
 				} catch (DatabaseException e) {
 					Log.e("Exception", e.getMessage());
 				}
 			}
 
 		});
 
 	}
 	private void startDetailsActivity(LevelSet levelSet) {
 		Intent i = new Intent(thisActivity,
 				LevelSetDetailsActivity.class);
 		if (levelSet.getFileResourceId() != null) {
 			i.putExtra(GameActivity.LEVEL_SET_RESOURCE_ID,
 					levelSet.getFileResourceId());
 
 		} else {
 			i.putExtra(GameActivity.LEVEL_SET_FILENAME,
 					levelSet.getFilename());
 		}
 		startActivityForResult(i,
 				UIConstants.LEVEL_SET_DETAILS_ACTIVITY);
 		
 	}
 
 
 	private ArrayList<LevelSet> loadLevels() {
 		ArrayList<LevelSet> levels = new ArrayList<LevelSet>();
 		try {
 			levels.add(LevelSet.loadLevelSetFromRaw(this, R.raw.training));
 
 			levels.add(LevelSet.loadLevelSetFromRaw(this, R.raw.amateur));
 			levels.add(LevelSet.loadLevelSetFromRaw(this, R.raw.expert));
 			levels.add(LevelSet.loadLevelSetFromRaw(this, R.raw.veteran));
 			levels.addAll(LevelSet.loadAllLevelSetsFromInternalStorage(this));
 		} catch (NotFoundException e) {
 			Log.e("Not found", e.getMessage());
 		} catch (LevelLoadingException e) {
 			Log.e("Level loading issue", e.getMessage());
 		}
 		return levels;
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		if (UserPreferences.EditorEnabled) {
 			menu.add(0, MENU_CREATE_ID, 0, "Create level set");
 		}
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 
 		switch (item.getItemId()) {
 		case MENU_CREATE_ID:
 			createLevelSet();
 			return true;
 		}
 
 		return super.onOptionsItemSelected(item);
 	}
 
 	private void createLevelSet() {
 
 		Intent i = new Intent(this, LevelSetDetailsEditActivity.class);
 
 		startActivityForResult(i, UIConstants.LEVEL_SET_EDIT_DETAILS_ACTIVITY);
 
 	}
 
 	private class LevelSetAdapter extends ArrayAdapter<LevelSet> {
 
 		public LevelSetAdapter(Context context, int textViewResourceId,
 				ArrayList<LevelSet> objects) {
 			super(context, textViewResourceId, objects);
 			this.mData = objects;
 		}
 
 		private ArrayList<LevelSet> mData = new ArrayList<LevelSet>();
 
 		@Override
 		public int getCount() {
 			return mData.size();
 		}
 
 		@Override
 		public LevelSet getItem(int position) {
 			return (LevelSet) mData.get(position);
 		}
 
 		@Override
 		public long getItemId(int position) {
 			return position;
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			View v = convertView;
 
 			if (v == null) {
 				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 				v = vi.inflate(R.layout.level_set_select_row, null);
 			}
 			LevelSet levelSet = (LevelSet) mData.get(position);
 			LevelSet prevLevelSet = null;
 			if (position > 0) {
 				prevLevelSet = mData.get(position - 1);
 
 			}
 			if (levelSet != null) {
 				TextView title = (TextView) v
 						.findViewById(R.id.level_set_select_row_title);
 				if (title != null) {
 					title.setText(levelSet.getName());
 				}
 				TextView coursePar = (TextView) v
 						.findViewById(R.id.level_set_select_course_par);
 				coursePar.setText("Par: " + levelSet.getCoursePar());
 
 				TextView progress = (TextView) v
 						.findViewById(R.id.level_set_select_row_progress);
 
 				TextView starLabel = (TextView) v
 						.findViewById(R.id.level_set_select_row_star_label);
 				if (progress != null) {
 					ArrayList<GalactoGolfLevelSetResult> scores = null;
 					ArrayList<GalactoGolfLevelSetResult> stars = null;
 					try {
 						adapter.open();
 						scores = GalactoGolfLevelSetResult
 								.loadScoresFromDatabase(adapter.getOpenDB(),
 										levelSet.getId());
 						stars = GalactoGolfLevelSetResult
 								.loadStarsFromDatabase(adapter.getOpenDB(),
 										levelSet.getId());
 						adapter.close();
 
 					} catch (DatabaseException e) {
 						Log.e("Exception", e.getMessage());
 					}
 					if (scores != null && scores.size() > 0) {
 						progress.setText("Top round: " + scores.get(0).score);
 						LinearLayout starLayout = (LinearLayout) v
 								.findViewById(R.id.level_set_select_row_stars);
 						starLayout.removeAllViews();
 
 						for (int i = 0; i < stars.get(0).bonus; i++) {
 							ImageView starImage = new ImageView(thisActivity);
 							starImage
 									.setImageResource(R.drawable.bonus_small_star);
 							starLayout.addView(starImage);
 						}
 					} else {
 						progress.setText("Top Round: -");
 						TextView blankText = new TextView(thisActivity);
 						blankText.setText("-");
 					}
 				}
 				try {
 					if (prevLevelSet == null
 							|| prevLevelSet.isCompleted(thisActivity)) {
 						ImageView lockImage = (ImageView) v
 								.findViewById(R.id.level_set_select_lock_image);
 						lockImage.setVisibility(View.INVISIBLE);
 						int scoreColor = Color.argb(255, 255, 194, 96);
 						int parColor = Color.argb(255, 178, 196, 205);
 						coursePar.setTextColor(scoreColor);
 						progress.setTextColor(parColor);
 						starLabel.setTextColor(scoreColor);
 
 					} else {
 						int lockedColor = Color.argb(255, 150, 150, 170);
 						title.setTextColor(lockedColor);
 						coursePar.setTextColor(lockedColor);
 						progress.setTextColor(lockedColor);
 						starLabel.setTextColor(lockedColor);
 
 					}
 				} catch (DatabaseException ex) {
 					Log.e("Exeption", ex.getMessage());
 				}
 
 			}
 			return v;
 		}
 
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		if (requestCode == UIConstants.LEVEL_SET_DETAILS_ACTIVITY) {
 			if (data != null)
 
 				if (data.getBooleanExtra(UIConstants.EXIT_TO_MENU, false)) {
 					Intent i = new Intent();
 
 					i.putExtra(UIConstants.EXIT_TO_MENU, true);
 					setResult(RESULT_OK, i);
 
 					finish();
 				} else if (data.getBooleanExtra(UIConstants.MOVE_TO_NEXT_LEVEL,
 						false)) {
 					UUID currentLevelNumber = UUID.fromString(data
 							.getStringExtra(UIConstants.CURRENT_LEVEL_ID));
 					boolean currentLevelFound = false;
 					LevelSet nextLevel = null;
					// loop through levels looking for the current one, then pick the next one
 					for(LevelSet levelSet : _levelSets) {
 						if(currentLevelFound) {
 							nextLevel = levelSet;
							currentLevelFound=false;
 						}
 						if(levelSet.getId().equals(currentLevelNumber)) {
 							currentLevelFound = true;
 						}
 						
 					}
 					
 					if(nextLevel!=null) {
 						startDetailsActivity(nextLevel);
 					}
 				}
 		} else {
 			_levelSets.clear();
 			_levelSets.addAll(loadLevels());
 			if (_levelSets != null) {
 				levelList.setAdapter(new LevelSetAdapter(this,
 						R.layout.level_set_select_row, _levelSets));
 			}
 
 		}
 	}
 }
