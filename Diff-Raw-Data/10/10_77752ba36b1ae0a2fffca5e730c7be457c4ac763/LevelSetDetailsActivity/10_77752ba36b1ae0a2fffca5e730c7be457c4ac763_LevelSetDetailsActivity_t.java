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
  */
 package com.galactogolf;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.database.sqlite.SQLiteDatabase;
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.os.Environment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.galactogolf.R;
 import com.galactogolf.controllers.GalacticPoolController;
 import com.galactogolf.database.DatabaseAdapter;
 import com.galactogolf.database.DatabaseException;
 import com.galactogolf.genericobjectmodel.levelloader.LevelLoadingException;
 import com.galactogolf.genericobjectmodel.levelloader.LevelSavingException;
 import com.galactogolf.genericobjectmodel.levelloader.LevelSet;
 import com.galactogolf.specificobjectmodel.GalactoGolfLevelSetResult;
 import com.galactogolf.specificobjectmodel.TestLevels;
 
 /*
  * Shown when a user has selected a level from the level select screen
  * Has information on the level set, e.g. description, top scores etc.
  */
 public class LevelSetDetailsActivity extends Activity {
 
 	private static final int MENU_EDIT_DETAILS_ID = 0;
 	private static final int MENU_EDIT_LEVELS_ID = 1;
 	private static final int MENU_DUMP_TO_SD_CARD = 2;
 
 	private LevelSet _levelSet;
 	ArrayList<GalactoGolfLevelSetResult> _scores = new ArrayList<GalactoGolfLevelSetResult>();
 	ArrayList<GalactoGolfLevelSetResult> _stars = new ArrayList<GalactoGolfLevelSetResult>();
 	Typeface typeface;
 	private LevelSetDetailsActivity thisActivity;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
 				WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 
 		Bundle extras = getIntent().getExtras();
 
 		String _levelSetFilename = extras != null ? extras
 				.getString(GameActivity.LEVEL_SET_FILENAME) : null;
 		Integer _levelSetResourceId = extras != null ? extras
 				.getInt(GameActivity.LEVEL_SET_RESOURCE_ID) : null;
 
 		try {
 			if (_levelSetFilename != null) {
 				_levelSet = LevelSet.loadLevelSetFromInternalStorage(
 						_levelSetFilename, this);
 			} else if (_levelSetResourceId != 0) {
 				_levelSet = LevelSet.loadLevelSetFromRaw(this,
 						_levelSetResourceId);
 			}
 		} catch (LevelLoadingException ex) {
 			Log.e("Error selecting level set", ex.getMessage());
 		}
 
 		setContentView(R.layout.level_set_details_layout);
 		TextView levelSetTitle = (TextView) findViewById(R.id.level_set_details_name);
 		levelSetTitle.setText(_levelSet.getName());
 		Button playButton = (Button) findViewById(R.id.level_set_details_play_button);
 
 		playButton.setOnClickListener(new View.OnClickListener() {
 
 			public void onClick(View v) {
 				playLevelSet();
 			}
 		});
 
 		Button exitButton = (Button) findViewById(R.id.level_set_details_exit_button);
 
 		exitButton.setOnClickListener(new View.OnClickListener() {
 
 			public void onClick(View v) {
 				Intent i = new Intent();
 
 				finish();
 			}
 		});
 
 		loadScores();
 		Log.i("Scores", "scores");
 
 		// registerForContextMenu(getView());
 		thisActivity = this;
 	}
 
 	private void loadScores() {
 		DatabaseAdapter adapter = new DatabaseAdapter(this);
 		adapter.open();
 		SQLiteDatabase db;
 		try {
 			db = adapter.getOpenDB();
 			_scores.clear();
 			_stars.clear();
 			_scores.addAll(GalactoGolfLevelSetResult.loadScoresFromDatabase(db,
 					_levelSet.getId()));
 			_stars.addAll(GalactoGolfLevelSetResult.loadStarsFromDatabase(db,
 					_levelSet.getId()));
 		} catch (DatabaseException ex) {
 			Log.e("Database exception", ex.getMessage());
 		}
 
 		for (int i = _scores.size(); i < 5; i++) {
 			_scores.add(null);
 		}
 		for (int i = _stars.size(); i < 5; i++) {
 			_stars.add(null);
 		}
 		ListView scoreList = (ListView) findViewById(R.id.level_score_list);
 		if (_stars != null && _stars.size() > 0) {
 			LinearLayout starLayout = (LinearLayout) findViewById(R.id.level_set_details_stars);
 			starLayout.removeAllViews();
 			if (_stars.get(0) != null) {
 				for (int i = 0; i < _stars.get(0).bonus; i++) {
 					ImageView starImage = new ImageView(this);
 					starImage.setImageResource(R.drawable.bonus_small_star);
 					starLayout.addView(starImage);
 				}
 			}
 		}
 		scoreList.setAdapter(new LevelSetScoreAdapter(this,
 				R.layout.level_set_select_row, _scores));
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		if (UserPreferences.EditorEnabled) {
 			menu.add(0, MENU_EDIT_DETAILS_ID, 0, "Edit details");
 			menu.add(0, MENU_EDIT_LEVELS_ID, 0, "Edit levels");
 			menu.add(0, MENU_DUMP_TO_SD_CARD, 0, "Dump to SD Card");
 		}
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 
 		switch (item.getItemId()) {
 		case MENU_EDIT_DETAILS_ID:
 			editLevelSetDetails();
 			break;
 		case MENU_EDIT_LEVELS_ID:
 			editLevels();
 			break;
 		case MENU_DUMP_TO_SD_CARD:
 			dumpToSDCard();
 			break;
 		}
 		return true;
 	}
 
 	private void dumpToSDCard() {
 		String state = Environment.getExternalStorageState();
 
 		if (Environment.MEDIA_MOUNTED.equals(state)) {
 			// We can read and write the media
 			try {
 				_levelSet.saveToExternalStorage(this);
 			} catch (LevelSavingException ex) {
 				Log.e("Error dumping to SD Card", ex.getMessage());
 			}
 		}
 	}
 
 	private void editLevels() {
 		Intent i = new Intent(this, GameActivity.class);
 		if (_levelSet.getFileResourceId() != null) {
 			i.putExtra(GameActivity.LEVEL_SET_RESOURCE_ID,
 					_levelSet.getFileResourceId());
 
 		} else {
 			i.putExtra(GameActivity.LEVEL_SET_FILENAME, _levelSet.getFilename());
 		}
 		i.putExtra(GameActivity.EDIT_LEVEL_FLAG, true);
 
 		startActivityForResult(i, UIConstants.GAME_ACTIVITY);
 	}
 
 	private void editLevelSetDetails() {
 		Intent i = new Intent(this, LevelSetDetailsEditActivity.class);
 		if (_levelSet.getFileResourceId() != null) {
 			i.putExtra(GameActivity.LEVEL_SET_RESOURCE_ID,
 					_levelSet.getFileResourceId());
 
 		} else {
 			i.putExtra(GameActivity.LEVEL_SET_FILENAME, _levelSet.getFilename());
 		}
 
 		startActivityForResult(i, UIConstants.LEVEL_SET_EDIT_DETAILS_ACTIVITY);
 
 	}
 
 	private void playLevelSet() {
 		Intent i = new Intent(this, GameActivity.class);
 		if (_levelSet.getFileResourceId() != null) {
 			i.putExtra(GameActivity.LEVEL_SET_RESOURCE_ID,
 					_levelSet.getFileResourceId());
 
 		} else {
 			i.putExtra(GameActivity.LEVEL_SET_FILENAME, _levelSet.getFilename());
 		}
 
 		startActivityForResult(i, UIConstants.GAME_ACTIVITY);
 
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		if (requestCode == UIConstants.GAME_ACTIVITY) {
 			Log.i("Scores", "scores");
 			ListView scoreList = (ListView) findViewById(R.id.level_score_list);
 			// get the previous star score
 			int prevLevelStars = 0;
			loadScores();
 			if(_stars!=null && _stars.size()>0){
 				prevLevelStars = _stars.get(0).bonus;
 			}
 
 			if (data != null) {
 
 				if (data.getBooleanExtra(UIConstants.EXIT_TO_MENU, false)) {
 					Intent i = new Intent();
 
 					i.putExtra(UIConstants.EXIT_TO_MENU, true);
 					setResult(RESULT_OK, i);
 
 					finish();
 				} else
 
 				if (data.getBooleanExtra(UIConstants.LEVEL_SET_COMPLETED, false)) {
 					Button playButton = (Button) findViewById(R.id.level_set_details_play_button);
 					playButton.setText("Replay");
 					ArrayList<LevelSet> allLevels = LevelSet
 							.loadLevels(thisActivity);
 					try {
 						if (_levelSet.isCompleted(thisActivity)) {
 							TextView message = (TextView) findViewById(R.id.level_set_details_message);
 							
 							// check if we are on the last level
 							if (!allLevels.get(allLevels.size() - 1).getId()
 									.equals(_levelSet.getId())) {
 								Button exitButton = (Button) findViewById(R.id.level_set_details_exit_button);
 								exitButton.setText("Next level");
 								exitButton
 										.setOnClickListener(new View.OnClickListener() {
 
 											public void onClick(View v) {
 												Intent i = new Intent();
 												i.putExtra(
 														UIConstants.MOVE_TO_NEXT_LEVEL,
 														true);
 												i.putExtra(
 														UIConstants.CURRENT_LEVEL_ID,
 														_levelSet.getId()
 																.toString());
 												setResult(RESULT_OK, i);
 
 												finish();
 											}
 										});
 								
 								/// if the level was previously locked
 								if(prevLevelStars<6) {
 									message.setText("Course completed! Next course unlocked ");
 								}
 
 							}
 							else { // we are on the last level, the game is completed!
 								/// if the level was previously locked
 								if(prevLevelStars<6) {
 									message.setText("You've completed the game, now try and get a high score!");
 								}
 							}
 						}
 					} catch (DatabaseException e) {
 						Log.i("Exception", e.getMessage());
 					}
 				}
 
 			}
 
 		}
 
 	}
 
 	private class LevelSetScoreAdapter extends
 			ArrayAdapter<GalactoGolfLevelSetResult> {
 
 		public LevelSetScoreAdapter(Context context, int textViewResourceId,
 				ArrayList<GalactoGolfLevelSetResult> objects) {
 			super(context, textViewResourceId, objects);
 			this.mData = objects;
 			mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		}
 
 		private ArrayList<GalactoGolfLevelSetResult> mData = new ArrayList<GalactoGolfLevelSetResult>();
 		private LayoutInflater mInflater;
 
 		public void addItem(final GalactoGolfLevelSetResult item) {
 			mData.add(item);
 			notifyDataSetChanged();
 		}
 
 		@Override
 		public void clear() {
 			mData.clear();
 		}
 
 		@Override
 		public int getCount() {
 			return mData.size();
 		}
 
 		@Override
 		public GalactoGolfLevelSetResult getItem(int position) {
 			return (GalactoGolfLevelSetResult) mData.get(position);
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
 				v = vi.inflate(R.layout.level_set_score_row, null);
 			}
 			GalactoGolfLevelSetResult o = (GalactoGolfLevelSetResult) mData
 					.get(position);
 			TextView title = (TextView) v
 					.findViewById(R.id.level_set_score_number_label);
 			title.setTypeface(typeface);
 			if (title != null) {
 				title.setText("" + (position + 1));
 			}
 			if (o != null) {
 
 				TextView score = (TextView) v
 						.findViewById(R.id.level_set_score_score_label);
 				score.setTypeface(typeface);
 				if (score != null) {
 					score.setText(o.score + " strokes");
 				}
 				LinearLayout starLayout = (LinearLayout) v
 						.findViewById(R.id.level_set_score_row_stars);
 				starLayout.removeAllViews();
 				if (starLayout != null) {
 					for (int i = 0; i < o.bonus; i++) {
 						ImageView starImage = new ImageView(thisActivity);
 						starImage.setImageResource(R.drawable.bonus_small_star);
 
 						starLayout.addView(starImage);
 					}
 				}
 			}
 
 			return v;
 		}
 
 	}
 }
