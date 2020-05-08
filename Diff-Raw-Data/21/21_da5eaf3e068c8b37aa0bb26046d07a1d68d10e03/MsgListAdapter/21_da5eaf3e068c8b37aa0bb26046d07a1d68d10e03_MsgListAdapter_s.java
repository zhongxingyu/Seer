 package com.chess.genesis;
 
 import android.content.Context;
 import android.database.sqlite.SQLiteCursor;
 import android.os.Bundle;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.ListAdapter;
 import android.widget.TextView;
 
 class MsgListAdapter extends BaseAdapter implements ListAdapter
 {
 	private final Context context;
 	private final String gameID;
 
 	private GameDataDB db;
 	private SQLiteCursor list;
 
 	static final class MsgHolder
 	{
 		public TextView username;
 		public TextView time;
 		public TextView msg;
 	}
 
 	public MsgListAdapter(final Context _context, final String GameID)
 	{
 		super();
 
 		context = _context;
 		gameID = GameID;
 		initCursor();
 	}
 
 	private void initCursor()
 	{
 		db = new GameDataDB(context);
 		list = db.getMsgList(gameID);
 	}
 
 	public int getCount()
 	{
 		return list.getCount();
 	}
 
 	public void update()
 	{
 		if (list.isClosed())
 			initCursor();
 		else
 			list.requery();
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
 		return GameDataDB.rowToBundle(list, index);
 	}
 
 	public View getView(final int index, View cell, final ViewGroup parent)
 	{
 		final Bundle data = (Bundle) getItem(index);
 		final MsgHolder holder;
 
 		if (cell == null) {
 			holder = new MsgHolder();
 			cell = setupMsg(parent, holder);
 		} else {
 			holder = (MsgHolder) cell.getTag();
 		}
 		reloadMsg(data, holder);
 
 		return cell;
 	}
 
 	private View setupMsg(final ViewGroup parent, final MsgHolder holder)
 	{
 		final View cell = View.inflate(parent.getContext(), R.layout.msglist_cell, null);
 
 		holder.username = (TextView) cell.findViewById(R.id.username);
 		holder.time = (TextView) cell.findViewById(R.id.time);
 		holder.msg = (TextView) cell.findViewById(R.id.msg);
 		cell.setTag(holder);
 
 		return cell;
 	}
 
 	private void reloadMsg(final Bundle data, final MsgHolder holder)
 	{
 		holder.username.setText(data.getString("username"));
 		holder.time.setText(new PrettyDate(data.getString("time")).agoFormat());
 		holder.msg.setText(data.getString("msg"));
 	}
 
 	public View getEmptyView(final Context context)
 	{
		return View.inflate(context, R.layout.msglist_cell_empty, null);
 	}
 }
