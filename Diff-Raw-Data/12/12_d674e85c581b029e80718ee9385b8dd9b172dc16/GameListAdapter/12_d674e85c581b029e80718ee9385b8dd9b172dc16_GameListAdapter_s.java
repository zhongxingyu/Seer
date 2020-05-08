 package com.reddit.worddit.adapters;
 
 import com.reddit.worddit.R;
 import com.reddit.worddit.api.response.Game;
 
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.TextView;
 
 public class GameListAdapter extends BaseAdapter {
 	protected Game[] mGames;
 	protected LayoutInflater mInflater;
 	
 	private int mStatusField, mNextPlayerField, mLastMoveField; 
 	
 	public GameListAdapter(Context ctx, Game[] games) {
 		mGames = games;
 		mInflater = (LayoutInflater)ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		mNextPlayerField = R.id.item_game_nextup;
 		mLastMoveField = R.id.item_game_lastplay;
 	}
 	
 	public GameListAdapter(Context ctx, Game[] games, 
 			int statusField, int nextPlayerField, int lastMoveField) {
 		this(ctx, games);
 		mStatusField = statusField;
 		mNextPlayerField = nextPlayerField;
 		mLastMoveField = lastMoveField;
 	}
 
 	@Override
 	public int getCount() {
 		return mGames.length;
 	}
 
 	@Override
 	public Game getItem(int n) {
 		return mGames[n];
 	}
 
 	@Override
 	public long getItemId(int n) {
 		Game g = mGames[n];
 		return Long.parseLong(g.id, 16);
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		View gameItem;
 		Game gameForView = mGames[position];
 		
 		
 		if(convertView == null) {
 			gameItem = mInflater.inflate(R.layout.item_gameitem, null);
 		} else {
 			gameItem = convertView;
 		}
 		
 		TextView nextUp = (TextView) gameItem.findViewById(mNextPlayerField);
 		TextView lastPlay = (TextView) gameItem.findViewById(mLastMoveField);
 		
 		if(gameForView.current_player != null) {
			nextUp.setText(gameForView.players[Integer.parseInt(gameForView.current_player) + 1].id);
 		} else {
			nextUp.setText("");
 		}
		lastPlay.setText("YAHTZEE");
 		
 		
 		return gameItem;
 	}
 
 }
