 package com.mntnorv.wrdl_holo;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.ContentValues;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 
 import com.mntnorv.wrdl_holo.db.GameStateSource;
 import com.mntnorv.wrdl_holo.db.GameStateSource.OnLoadFinishedListener;
 import com.mntnorv.wrdl_holo.db.WrdlContentProvider;
 import com.mntnorv.wrdl_holo.dict.DictionaryProvider;
 import com.mntnorv.wrdl_holo.util.StringGenerator;
 
 public class MenuActivity extends Activity implements OnLoadFinishedListener {
 
 	public final static String GAME_STATE_ID = "com.mntnorv.wrdl_holo.GAME_STATE_ID";
 	
 	private MainMenuAdapter menuAdapter;
 	private GameStateSource gameStateSource;
 	private List<GameState> gameList = new ArrayList<GameState>();
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_menu);
 		
 		DictionaryProvider.loadDictionary(this);
 		
 		ListView menuListView = (ListView)findViewById(R.id.menu_list_view);
 		menuAdapter = new MainMenuAdapter(this, gameList);
 		menuListView.setAdapter(menuAdapter);
 		menuListView.setOnItemClickListener(mainMenuListener);
 		
 		gameStateSource = new GameStateSource(this, getLoaderManager(), getContentResolver());
 		gameStateSource.getAllStates(this);
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		gameStateSource.getAllStates(this);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_menu, menu);
 		return true;
 	}
 
 	public void startNewGame() {
 		GameState newGame = new GameState(4, StringGenerator.randomString(4 * 4), GameModes.INFINITY);
         gameList.add(newGame);
         
         menuAdapter.notifyDataSetChanged();
         
         Uri uri = WrdlContentProvider.GAME_STATES_URI;
         ContentValues gameStateValues = newGame.toContentValues();
         Uri newUri = getContentResolver().insert(uri, gameStateValues);
         newGame.setId(Integer.parseInt(newUri.getLastPathSegment()));
         
         startGameWithStateId(newGame.getId());
 	}
 	
 	private void startGameWithStateId(int stateId) {
 		Intent intent = new Intent(this, GameActivity.class);
 		intent.putExtra(GAME_STATE_ID, stateId);
 		startActivity(intent);
 	}
 	
 	private OnItemClickListener mainMenuListener = new OnItemClickListener() {
 		@Override
 		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 			switch (menuAdapter.getItemViewType(position)) {
 			case MainMenuAdapter.GAME_STATE_ITEM_TYPE:
				int gameStateId = (int) menuAdapter.getItemId(position);
				startGameWithStateId(gameStateId);
 				break;
 			case MainMenuAdapter.NEW_GAME_BUTTON_TYPE:
 				startNewGame();
 				break;
 			}
 		}
 	};
 	
 	@Override
 	public void onLoadFinished(List<GameState> result) {
 		gameList.clear();
 		gameList.addAll(result);
 		menuAdapter.notifyDataSetChanged();
 	}
 }
