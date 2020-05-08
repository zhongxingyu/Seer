 package com.chess.genesis;
 
 import android.content.Context;
 import android.database.sqlite.SQLiteCursor;
 import android.os.Bundle;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.ListAdapter;
 import android.widget.TableLayout;
 import android.widget.TextView;
 import java.util.Date;
 
 class GameListAdapter extends BaseAdapter implements ListAdapter
 {
 	private GameDataDB db;
 	private SQLiteCursor list;
 
 	private String username;
 	private int type;
 
 	public GameListAdapter(Context context, Bundle settings)
 	{
 		db = new GameDataDB(context);
 		username = settings.getString("username");
 		type = settings.getInt("type");
 
 		switch (type) {
 		case Enums.LOCAL_GAME:
 			list = db.getLocalGameList();
 			break;
 		case Enums.ONLINE_GAME:
 			list = db.getOnlineGameList(1);
 			break;
 		case Enums.ARCHIVE_GAME:
 			list = db.getArchiveGameList();
 			break;
 		}
 	}
 
 	public int getCount()
 	{
 		return list.getCount();
 	}
 
 	public void update()
 	{
		if (list.isClosed())
			return;
 		list.requery();
 		notifyDataSetChanged();
 	}
 
 	public void setYourturn(int yourturn)
 	{
 		list = db.getOnlineGameList(yourturn);
 		notifyDataSetChanged();
 	}
 
 	public void close()
 	{
 		db.close();
 	}
 
 	public long getItemId(int index)
 	{
 		return index;
 	}
 
 	public Object getItem(int index)
 	{
 		return GameDataDB.rowToBundle(list, index);
 	}
 
 	public View getView(int index, View cell, ViewGroup parent)
 	{
 		Bundle data = (Bundle) getItem(index);
 
 		if (cell == null) {
 			TableLayout newcell = new TableLayout(parent.getContext());
 			switch (type) {
 			case Enums.LOCAL_GAME:
 				newcell.inflate(parent.getContext(), R.layout.gamelist_cell_local, newcell);
 				break;
 			case Enums.ONLINE_GAME:
 				newcell.inflate(parent.getContext(), R.layout.gamelist_cell_online, newcell);
 				break;
 			case Enums.ARCHIVE_GAME:
 				newcell.inflate(parent.getContext(), R.layout.gamelist_cell_archive, newcell);
 				break;
 			}
 			cell = newcell;
 		}
 		switch (type) {
 		case Enums.LOCAL_GAME:
 			setupLocal(cell, data);
 			break;
 		case Enums.ONLINE_GAME:
 			setupOnline(cell, data);
 			break;
 		case Enums.ARCHIVE_GAME:
 			setupArchive(cell, data);
 			break;
 		}
 		return cell;
 	}
 
 	private void setupLocal(View cell, Bundle data)
 	{
 		TextView txt = (TextView) cell.findViewById(R.id.game_name);
 		txt.setText(data.getString("name"));
 
 		String date = (new PrettyDate(data.getString("stime"))).agoFormat();
 
 		txt = (TextView) cell.findViewById(R.id.game_date);
 		txt.setText(date);
 	}
 
 	private void setupOnline(View cell, Bundle data)
 	{
 		int ply = Integer.valueOf(data.getString("ply"));
 
 		String opponent = (username.equals(data.getString("white")))? data.getString("black") : data.getString("white");
 
 		TextView txt = (TextView) cell.findViewById(R.id.game_opp);
 		txt.setText(opponent);
 
 		String date = (new PrettyDate(data.getString("stime"))).agoFormat();
 
 		txt = (TextView) cell.findViewById(R.id.game_date);
 		txt.setText(date);
 	}
 
 	private void setupArchive(View cell, Bundle data)
 	{
 		int ply = Integer.valueOf(data.getString("ply"));
 
 		String opponent = (username.equals(data.getString("white")))? data.getString("black") : data.getString("white");
 
 		TextView txt = (TextView) cell.findViewById(R.id.game_opp);
 		txt.setText(opponent);
 
 		String date = (new PrettyDate(data.getString("stime"))).agoFormat();
 
 		txt = (TextView) cell.findViewById(R.id.game_date);
 		txt.setText(date);
 	}
 }
