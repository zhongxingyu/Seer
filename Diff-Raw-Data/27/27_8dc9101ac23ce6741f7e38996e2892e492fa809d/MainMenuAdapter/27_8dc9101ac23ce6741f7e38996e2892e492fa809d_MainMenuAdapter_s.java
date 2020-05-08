 package com.mntnorv.wrdl_holo;
 
 import java.util.List;
 import java.util.Locale;
 
 import com.mntnorv.wrdl_holo.views.TileGridView;
 
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.TextView;
 
 public class MainMenuAdapter extends BaseAdapter {
 	
 	private List<GameState> gameStateList;
 	private LayoutInflater inflater;
 	private Context context;
 	
 	private String gamemodeString;
 	private String pointsString;
 	private String wordsString;
 	
 	public MainMenuAdapter(Context context, List<GameState> objects) {
 		this.context = context;
 		this.gameStateList = objects;
 		
 		inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		
 		gamemodeString = context.getResources().getString(GAMEMODE_STRING_ID);
 		pointsString = context.getResources().getString(POINTS_STRING_ID);
 		wordsString = context.getResources().getString(WORDS_STRING_ID);
 	}
 
 	@Override
 	public int getCount() {
 		return gameStateList.size() + 1;
 	}
 
 	@Override
 	public Object getItem(int index) {
		return gameStateList.get(index - 1);
 	}
 
 	@Override
 	public long getItemId(int index) {
		return index - 1;
 	}
 
 	@Override
 	public View getView(int index, View convertView, ViewGroup parentView) {
 		View returnView = null;
 		
 		switch (getItemViewType(index)) {
 		case NEW_GAME_BUTTON_TYPE:
 			returnView = getNewGameButton(index, convertView, parentView);
 			break;
 		case GAME_STATE_ITEM_TYPE:
 			returnView = getGameStateView(index, convertView, parentView);
 			break;
 		}
 		
 		return returnView;
 	}
 	
 	private View getGameStateView(int index, View convertView, ViewGroup parentView) {
 		TileGridView grid = null;
		GameState currentGameState = gameStateList.get(index - 1);
 		
 		if (convertView == null) {
 			convertView = inflater.inflate(ITEM_LAYOUT_ID, parentView, false);
 			grid = (TileGridView)convertView.findViewById(GRID_ID);
			grid.create(gameStateList.get(index - 1).getSize());
 		} else if (convertView.getId() != ITEM_ID) {
 			convertView = inflater.inflate(ITEM_LAYOUT_ID, parentView, false);
 			grid = (TileGridView)convertView.findViewById(GRID_ID);
			grid.create(gameStateList.get(index - 1).getSize());
 		}
 		
 		if (grid == null) {
 			grid = (TileGridView)convertView.findViewById(GRID_ID);
 		}
 		
 		ViewGroup propContainer = (ViewGroup) convertView.findViewById(PROP_CONTAINER_ID);
 		ViewGroup bigPropContainer = (ViewGroup) convertView.findViewById(BIG_PROP_CONTAINER_ID);
 		
 		String gamemodeName = context.getResources().getString(
 				GameModes.getGamemodeNameResource(currentGameState.getGamemode()));
 		String points = Integer.toString(currentGameState.getScore());
 		String words = String.format(Locale.US, "%d/%d", currentGameState.getGuessedWordCount(),
 				currentGameState.getWordCount());
 		
 		grid.setLetters(currentGameState.getLetterArray());
 		
 		propContainer.removeAllViews();
 		propContainer.addView(getNewProp(gamemodeString, gamemodeName, propContainer));
 		propContainer.addView(getNewProp(pointsString, points, propContainer));
 		bigPropContainer.removeAllViews();
 		bigPropContainer.addView(getNewBigProp(wordsString, words, bigPropContainer));
 		
 		//FontUtils.setRobotoFont(context, name, true);
 		
 		return convertView;
 	}
 	
 	private View getNewGameButton(int index, View convertView, ViewGroup parentView) {
 		if (convertView == null) {
 			convertView = inflater.inflate(MENU_BUTTON_LAYOUT_ID, parentView, false);
 		} else if (convertView.getId() != MENU_BUTTON_TEXT_ID) {
 			convertView = inflater.inflate(MENU_BUTTON_LAYOUT_ID, parentView, false);
 		}
 		
 		TextView buttonName = (TextView) convertView.findViewById(MENU_BUTTON_TEXT_ID);
 		buttonName.setText(context.getResources().getString(NEW_GAME_STRING));
 		
 		return convertView;
 	}
 	
 	private View getNewProp(String name, String content, ViewGroup parent) {
 		View propView = inflater.inflate(PROP_LAYOUT_ID, parent, false);
 		TextView nameView = (TextView) propView.findViewById(ITEM_PROP_NAME_ID);
 		TextView contentView = (TextView) propView.findViewById(ITEM_PROP_CONTENT_ID);
 		
 		nameView.setText(name);
 		contentView.setText(content);
 		
 		return propView;
 	}
 	
 	private View getNewBigProp(String name, String content, ViewGroup parent) {
 		View propView = inflater.inflate(BIG_PROP_LAYOUT_ID, parent, false);
 		TextView nameView = (TextView) propView.findViewById(ITEM_PROP_NAME_ID);
 		TextView contentView = (TextView) propView.findViewById(ITEM_PROP_CONTENT_ID);
 		
 		nameView.setText(name);
 		contentView.setText(content);
 		
 		return propView;
 	}
 
 	@Override
 	public boolean hasStableIds() {
 		return true;
 	}
 
 	@Override
 	public int getItemViewType(int pos) {
 		if (pos > 0) {
 			return GAME_STATE_ITEM_TYPE;
 		} else {
 			return NEW_GAME_BUTTON_TYPE;
 		}
 	}
 
 	@Override
 	public int getViewTypeCount(){
 		return VIEW_TYPE_COUNT;
 	}
 	
 	private static final int VIEW_TYPE_COUNT = 2;
 	
 	public static final int NEW_GAME_BUTTON_TYPE = 0;
 	public static final int GAME_STATE_ITEM_TYPE = 1;
 	
 	private static final int ITEM_LAYOUT_ID = R.layout.game_state_item;
 	private static final int PROP_LAYOUT_ID = R.layout.game_state_item_property;
 	private static final int BIG_PROP_LAYOUT_ID = R.layout.game_state_item_big_property;
 	private static final int MENU_BUTTON_LAYOUT_ID = R.layout.menu_button;
 	private static final int GRID_ID = R.id.gameGridView;
 	private static final int ITEM_ID = R.id.game_state_item;
 	private static final int ITEM_PROP_NAME_ID = R.id.item_property_name;
 	private static final int ITEM_PROP_CONTENT_ID = R.id.item_property_content;
 	private static final int PROP_CONTAINER_ID = R.id.game_state_item_properties;
 	private static final int BIG_PROP_CONTAINER_ID = R.id.game_state_item_big_properties;
 	private static final int MENU_BUTTON_TEXT_ID = R.id.menu_button_text;
 	
 	private static final int GAMEMODE_STRING_ID = R.string.item_prop_gamemode;
 	private static final int POINTS_STRING_ID = R.string.item_prop_points;
 	private static final int WORDS_STRING_ID = R.string.item_prop_words;
 	private static final int NEW_GAME_STRING = R.string.new_game_button;
 }
