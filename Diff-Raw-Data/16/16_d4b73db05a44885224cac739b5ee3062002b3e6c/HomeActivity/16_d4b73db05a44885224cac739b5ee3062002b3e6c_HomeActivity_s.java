 /**
  * Copyright (C) 2012 Jesse Wilson
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.publicobject.rounds;
 
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.os.Bundle;
 import android.text.format.DateUtils;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.ActionMode;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Set;
 
 public final class HomeActivity extends SherlockActivity {
     private GameDatabase database;
     private ListView gameList;
 
     private final View.OnClickListener newGameListener = new View.OnClickListener() {
         @Override public void onClick(View view) {
             Intent newGameIntent = new Intent(HomeActivity.this, SetUpActivity.class);
             newGameIntent.putExtra(IntentExtras.IS_NEW_GAME, true);
             startActivity(newGameIntent);
             overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
         }
     };
 
     @Override public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         setRequestedOrientation(Device.isTablet(this)
                 ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                 : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
 
         database = GameDatabase.getInstance(getApplicationContext());
 
         View layout = getLayoutInflater().inflate(R.layout.home, null);
         setContentView(layout);
 
         Button newGame = (Button) layout.findViewById(R.id.newGame);
         if (newGame != null) {
             newGame.setOnClickListener(newGameListener);
         }
 
         gameList = (ListView) findViewById(R.id.gameList);
         gameList.setItemsCanFocus(true);
 
         ActionBar actionBar = getSupportActionBar();
         actionBar.setTitle("Rounds");
 
         gameList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
     }
 
     @Override public void onResume() {
         super.onResume();
         gameList.setAdapter(new GameListAdapter(database.allGames()));
     }
 
     @Override protected void onPause() {
         super.onPause();
     }
 
     @Override protected void onDestroy() {
         super.onDestroy();
         database = null;
     }
 
     private void launchGame(Game game) {
         Intent intent = new Intent(getApplicationContext(), GameActivity.class);
         intent.putExtra(IntentExtras.GAME_ID, game.getId());
         startActivity(intent);
         overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
     }
 
     private class GameListAdapter extends BaseAdapter {
         private final View.OnClickListener resumeListener = new View.OnClickListener() {
             @Override public void onClick(View view) {
                 int position = gameList.getPositionForView(view);
                 Game game = (Game) gameList.getAdapter().getItem(position);
                 launchGame(game);
             }
         };
 
         private final View.OnClickListener rematchListener = new View.OnClickListener() {
             @Override public void onClick(View button) {
                 int position = gameList.getPositionForView((View) button.getParent());
                 Game game = (Game) gameList.getAdapter().getItem(position);
                 Game replay = game.replay();
                 database.save(replay);
                 launchGame(replay);
             }
         };
 
         private final ActionMode.Callback batchModeCallback = new ActionMode.Callback() {
             @Override public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                 switch (item.getItemId()) {
                 case R.id.delete:
                     Set<Game> toDelete = new LinkedHashSet<Game>();
                     for (int i = 0; i < getCount(); i++) {
                         Game game = (Game) getItem(i);
                         if (game != null && gameList.isItemChecked(i)) {
                             toDelete.add(game);
                         }
                     }
                     database.deleteGames(toDelete);
                     games.clear();
                     games.addAll(database.allGames());
                     notifyDataSetChanged();
                     mode.finish();
                     return true;
                 default:
                     return false;
                 }
             }
             @Override public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                 getSupportMenuInflater().inflate(R.menu.home_context, menu);
                 return true;
             }
             @Override public void onDestroyActionMode(ActionMode mode) {
                 gameList.clearChoices();
                 notifyDataSetChanged();
                 batchMode = null;
             }
             @Override public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                 return false;
             }
         };
 
         private View.OnLongClickListener contextListener = new View.OnLongClickListener() {
             @Override public boolean onLongClick(View view) {
                 if (batchMode == null) {
                     // check the context-selected item
                     int position = gameList.getPositionForView(view);
                     gameList.setItemChecked(position, true);
 
                     // switch to delete mode
                     batchMode = HomeActivity.this.startActionMode(batchModeCallback);
                     notifyDataSetChanged();
                     updateContextMenuTitle();
                 }
                 return true;
             }
         };
 
         private final View.OnClickListener checkListener = new View.OnClickListener() {
             @Override public void onClick(View view) {
                 CheckBox checked = (CheckBox) view.findViewById(R.id.checked);
                 checked.setChecked(!checked.isChecked());
             }
         };
 
         private final OnCheckedChangeListener checkBoxToListModel = new OnCheckedChangeListener() {
             @Override public void onCheckedChanged(CompoundButton button, boolean isChecked) {
                 int position = gameList.getPositionForView((View) button.getParent());
                 gameList.setItemChecked(position, isChecked);
                 updateContextMenuTitle();
             }
         };
 
         private ActionMode batchMode;
         private final List<Game> games;
 
         private GameListAdapter(List<Game> games) {
             this.games = games;
         }
 
         @Override public int getCount() {
             return games.size() + 1;
         }
 
         @Override public Object getItem(int i) {
             if (i < 1) {
                 return null;
             }
             return games.get(i - 1);
         }
 
         @Override public long getItemId(int i) {
             if (i < 1) {
                 return i;
             }
             return games.get(i - 1).getDateStarted();
         }
 
         @Override public View getView(int position, View recycle, ViewGroup parent) {
             if (position == 0) {
                 return getOverview(recycle, parent);
             }
             return getGame(position, recycle, parent);
         }
 
         @Override public int getViewTypeCount() {
             return 2; // header, games
         }
 
         @Override public int getItemViewType(int position) {
             if (position == 0) {
                 return 0;
             } else {
                 return 1;
             }
         }
 
         private View getOverview(View recycle, ViewGroup parent) {
             if (recycle != null) {
                 return recycle;
             }
             View overview = getLayoutInflater().inflate(R.layout.overview_item, parent, false);
             Button newGame = (Button) overview.findViewById(R.id.newGame);
             if (newGame != null) {
                 newGame.setOnClickListener(newGameListener);
             }
             return overview;
         }
 
         private View getGame(int position, View recycle, ViewGroup parent) {
             LinearLayout layout = (LinearLayout) ((recycle == null)
                     ? getLayoutInflater().inflate(R.layout.game_item, parent, false)
                     : recycle);
             CheckBox checked = (CheckBox) layout.findViewById(R.id.checked);
             TextView players = (TextView) layout.findViewById(R.id.players);
             TextView summary = (TextView) layout.findViewById(R.id.summary);
             Button rematch = (Button) layout.findViewById(R.id.rematch);
             Game game = (Game) getItem(position);
 
             players.setText(Names.styleScores(game));
 
             StringBuilder summaryText = new StringBuilder();
            if (game.getName() != null && !game.getName().isEmpty()) {
                 summaryText.append(game.getName());
                 summaryText.append(". ");
             }
             if (game.roundCount() == 1) {
                 summaryText.append("1 round. ");
             } else {
                 summaryText.append(Integer.toString(game.roundCount()));
                 summaryText.append(" rounds. ");
             }
             summaryText.append("Started ");
             summaryText.append(DateUtils.getRelativeTimeSpanString(getApplicationContext(),
                     game.getDateStarted(), true));
             summaryText.append(".");
             summary.setText(summaryText);
 
             // Remove listeners before changing any observed state. Necessary for recycling.
             checked.setOnCheckedChangeListener(null);
             layout.setOnLongClickListener(null);
             layout.setOnClickListener(null);
             rematch.setOnClickListener(null);
 
             checked.setChecked(gameList.isItemChecked(position));
 
             // Add the listeners we care about in this state.
             if (batchMode == null) {
                 checked.setVisibility(View.GONE);
                 rematch.setVisibility(View.VISIBLE);
                 layout.setOnClickListener(resumeListener);
                 layout.setOnLongClickListener(contextListener);
                 rematch.setOnClickListener(rematchListener);
             } else {
                 checked.setVisibility(View.VISIBLE);
                 rematch.setVisibility(View.GONE);
                 checked.setOnCheckedChangeListener(checkBoxToListModel);
                 layout.setOnClickListener(checkListener);
             }
 
             layout.setFocusable(true);
             layout.setBackgroundResource(android.R.drawable.list_selector_background);
             return layout;
         }
 
         private void updateContextMenuTitle() {
             if (batchMode == null) {
                 throw new IllegalStateException();
             }
 
             int count = gameList.getCheckedItemIds().length; // no getCheckedItemCount 'til API 11
             switch (count) {
             case 0:
                 batchMode.setTitle("0 Games");
                 break;
             case 1:
                 batchMode.setTitle("1 Game");
                 break;
             default:
                 batchMode.setTitle(count + " Games");
                 break;
             }
         }
     }
 }
