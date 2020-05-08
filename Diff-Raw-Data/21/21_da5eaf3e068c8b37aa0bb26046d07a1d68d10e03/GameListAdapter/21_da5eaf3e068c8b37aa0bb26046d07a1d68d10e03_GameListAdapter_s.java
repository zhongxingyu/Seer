 package com.chess.genesis;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.database.sqlite.SQLiteCursor;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.ListAdapter;
 import android.widget.TextView;
 
 class GameListAdapter extends BaseAdapter implements ListAdapter
 {
 	private final Context context;
 	private final Bundle settings;
 	private final String username;
 	private final int type;
 
 	private GameDataDB db;
 	private SQLiteCursor list;
 	private int yourturn;
 
 	static class GameHolder
 	{
 		public MyImageView icon;
 		public TextView name;
 		public TextView type;
 		public TextView time;
 		public TextView msgs;
 		public TextView with;
 		public TextView idle;
 	}
 
 	public GameListAdapter(final Context _context, final int Type, final int yourTurn)
 	{
 		super();
 		context = _context;
 
 		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
 		username = prefs.getString("username", "!error!");
 		type = Type;
 		yourturn = yourTurn;
 
 		settings = new Bundle();
 		settings.putString("username", username);
 		settings.putInt("type", type);
 
 		initCursor();
 	}
 
 	private void initCursor()
 	{
 		db = new GameDataDB(context);
 		switch (type) {
 		case Enums.LOCAL_GAME:
 			list = db.getLocalGameList();
 			break;
 		case Enums.ONLINE_GAME:
 			list = db.getOnlineGameList(yourturn);
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
 
 	public Bundle getExtras()
 	{
 		return settings;
 	}
 
 	public void update()
 	{
 		if (list.isClosed())
 			initCursor();
 		else
 			list.requery();
 		notifyDataSetChanged();
 	}
 
 	public void setYourturn(final int yourTurn)
 	{
 		yourturn = yourTurn;
 		list = db.getOnlineGameList(yourturn);
 		notifyDataSetChanged();
 	}
 
 	public void close()
 	{
 		list.close();
 		db.close();
 	}
 
 	public long getItemId(final int index)
 	{
 		return index;
 	}
 
 	public Object getItem(final int index)
 	{
 		final Bundle data = GameDataDB.rowToBundle(list, index);
 		data.putAll(settings);
 
 		return data;
 	}
 
 	public View getView(final int index, View cell, final ViewGroup parent)
 	{
 		final Bundle data = (Bundle) getItem(index);
 		final GameHolder holder;
 
 		if (cell == null) {
 			holder = new GameHolder();
 
 			switch (type) {
 			case Enums.LOCAL_GAME:
 				cell = setupLocal(parent, holder);
 				break;
 			case Enums.ONLINE_GAME:
 				cell = setupOnline(parent, holder);
 				break;
 			case Enums.ARCHIVE_GAME:
 				cell = setupArchive(parent, holder);
 				break;
 			}
 		} else {
 			holder = (GameHolder) cell.getTag();
 		}
 
 		switch (type) {
 		case Enums.LOCAL_GAME:
 			reloadLocal(data, holder);
 			break;
 		case Enums.ONLINE_GAME:
 			reloadOnline(data, holder);
 			break;
 		case Enums.ARCHIVE_GAME:
 			reloadArchive(data, holder);
 			break;
 		}
 		return cell;
 	}
 
 	private View setupLocal(final ViewGroup parent, final GameHolder holder)
 	{
 		final View cell = View.inflate(parent.getContext(), R.layout.gamelist_cell_local, null);
 
 		holder.icon = (MyImageView) cell.findViewById(R.id.cell_icon);
 		holder.name = (TextView) cell.findViewById(R.id.game_name);
 		holder.type = (TextView) cell.findViewById(R.id.game_type);
 		holder.time = (TextView) cell.findViewById(R.id.game_time);
 		cell.setTag(holder);
 
 		return cell;
 	}
 
 	private void reloadLocal(final Bundle data, final GameHolder holder)
 	{
 		holder.icon.setImageResource(R.drawable.white_pawn_dark);
 		holder.name.setText(data.getString("name"));
 		holder.time.setText(new PrettyDate(data.getString("stime")).agoFormat());
 		holder.type.setText(Enums.OpponentType(Integer.valueOf(data.getString("opponent"))) + " " +
 			Enums.GameType(Integer.valueOf(data.getString("gametype"))));
 	}
 	
 	private void setupNetworkCommon(final View cell, final GameHolder holder)
 	{
 		holder.with = (TextView) cell.findViewById(R.id.game_with);
 		holder.type = (TextView) cell.findViewById(R.id.game_type);
 		holder.time = (TextView) cell.findViewById(R.id.game_time);		
 		holder.msgs = (TextView) cell.findViewById(R.id.new_msgs);
 		holder.icon = (MyImageView) cell.findViewById(R.id.cell_icon);
 		cell.setTag(holder);
 	}
 
 	private void reloadNetworkCommon(final Bundle data, final GameHolder holder)
 	{
 		holder.with.setText((username.equals(data.getString("white")))?
 			data.getString("black") : data.getString("white"));
 		holder.type.setText(Enums.EventType(Integer.valueOf(data.getString("eventtype")))
 			+ " " + Enums.GameType(Integer.valueOf(data.getString("gametype"))));
 		holder.time.setText(new PrettyDate(data.getString("stime")).agoFormat());
 		holder.msgs.setText((data.getString("unread") == null)?
 			"" : (data.getString("unread").equals("1")? "[new msg]" : ""));
 	}
 	
 	private View setupOnline(final ViewGroup parent, final GameHolder holder)
 	{
 		final View cell = View.inflate(parent.getContext(), R.layout.gamelist_cell_online, null);
 
 		setupNetworkCommon(cell, holder);
 		holder.idle = (TextView) cell.findViewById(R.id.idle);
 
 		return cell;
 	}
 
 	private void reloadOnline(final Bundle data, final GameHolder holder)
 	{
 		reloadNetworkCommon(data, holder);
 
 		holder.icon.setImageResource((Integer.valueOf(data.getString("ply")) % 2 == 0)?
 			R.drawable.white_pawn_dark : R.drawable.black_pawn_light);
 
 		switch (Integer.valueOf(data.getString("idle"))) {
 		case Enums.NOTIDLE:
 			holder.idle.setText("");
 			break;
 		case Enums.IDLE:
 			holder.idle.setText("[idle]");
 			holder.idle.setTextColor(0xff3465a4);
 			break;
 		case Enums.NUDGED:
 			holder.idle.setText("[nudged]");
 			holder.idle.setTextColor(0xfff57900);
 			break;
 		case Enums.CLOSE:
 			holder.idle.setText("[close]");
 			holder.idle.setTextColor(0xffcc0000);
 			break;
 		}
 	}
 
 	private View setupArchive(final ViewGroup parent, final GameHolder holder)
 	{
 		final View cell = View.inflate(parent.getContext(), R.layout.gamelist_cell_archive, null);
 
 		setupNetworkCommon(cell, holder);
 
 		return cell;
 	}
 
 	private void reloadArchive(final Bundle data, final GameHolder holder)
 	{
 		reloadNetworkCommon(data, holder);
 
 		final int icon;
 		switch (Integer.valueOf(data.getString("status"))) {
 		case Enums.WHITEMATE:
 		case Enums.BLACKRESIGN:
 		case Enums.BLACKIDLE:
 			icon = R.drawable.white_pawn_dark;
 			break;
 		case Enums.BLACKMATE:
 		case Enums.WHITERESIGN:
 		case Enums.WHITEIDLE:
 			icon = R.drawable.black_pawn_light;
 			break;
 		case Enums.STALEMATE:
 		case Enums.IMPOSSIBLE:
 		default:
 			icon = R.drawable.square_dark;
 			break;
 		}
 		holder.icon.setImageResource(icon);
 	}
 
 	public View getEmptyView(final Context context)
 	{
 		final View cell = View.inflate(context, R.layout.gamelist_cell_empty, null);
 		final TextView txt = (TextView) cell.findViewById(R.id.message);
 
 		switch (type) {
 		case Enums.LOCAL_GAME:
 		case Enums.ONLINE_GAME:
 			txt.setText("Click the plus button to create a game");
 			break;
 		case Enums.ARCHIVE_GAME:
 			txt.setText("Finished online games will appear here");
 			break;
 		}
 		return cell;
 	}
 }
