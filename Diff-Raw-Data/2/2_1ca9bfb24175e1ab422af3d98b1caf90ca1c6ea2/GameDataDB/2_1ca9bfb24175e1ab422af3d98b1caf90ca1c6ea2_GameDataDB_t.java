 package com.chess.genesis;
 
 import android.content.Context;
 import android.database.sqlite.SQLiteCursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.Bundle;
 import java.util.Date;
 
 class GameDataDB
 {
 	private SQLiteDatabase db;
 
 	private Context context;
 
 	public GameDataDB(Context _context)
 	{
 		context = _context;
 		db = (new DatabaseOpenHelper(context)).getWritableDatabase();
 	}
 
 	public void close()
 	{
 		db.close();
 	}
 
 	public static Bundle rowToBundle(SQLiteCursor cursor, int index)
 	{
 		Bundle bundle = new Bundle();
 		String[] column = cursor.getColumnNames();
 
 		cursor.moveToPosition(index);
 		for (int i = 0; i < cursor.getColumnCount(); i++)
 			bundle.putString(column[i], cursor.getString(i));
 		return bundle;
 	}
 
 	public Bundle newLocalGame(int gametype, int opponent)
 	{
 		long time = (new Date()).getTime();
 		Object[] data = {time, time, gametype, opponent};
 		String[] data2 = {String.valueOf(time)};
 
 		db.execSQL("INSERT INTO localgames (ctime, stime, gametype, opponent) VALUES (?, ?, ?, ?);", data);
 		SQLiteCursor cursor = (SQLiteCursor) db.rawQuery("SELECT * FROM localgames WHERE ctime=?", data2);
 
 		return rowToBundle(cursor, 0);
 	}
 
 	public void saveLocalGame(int id, long stime, String zfen, String history)
 	{
 		Object[] data = {stime, zfen, history, id};
 		db.execSQL("UPDATE localgames SET stime=?, zfen=?, history=? WHERE id=?;", data);
 	}
 
 	public void deleteLocalGame(int id)
 	{
 		Object[] data = {id};
 		db.execSQL("DELETE FROM localgames WHERE id=?;", data);
 	}
 
 	public void renameLocalGame(int id, String name)
 	{
 		Object[] data = {name, id};
 		db.execSQL("UPDATE localgames SET name=? WHERE id=?;", data);
 	}
 
 	public SQLiteCursor getLocalGameList()
 	{
 		return (SQLiteCursor) db.rawQuery("SELECT * FROM localgames ORDER BY stime DESC", null);
 	}
 
 	public SQLiteCursor getOnlineGameList()
 	{
 		return (SQLiteCursor) db.rawQuery("SELECT * FROM onlinegames ORDER BY yourturn DESC, stime DESC", null);
 	}
 
 	public ObjectArray<String> getOnlineGameIds()
 	{
 		ObjectArray<String> list = new ObjectArray<String>();
 
 		SQLiteCursor cursor = (SQLiteCursor) db.rawQuery("SELECT gameid FROM onlinegames", null);
 
 		cursor.moveToFirst();
 		for (int i = 0; i < cursor.getCount(); i++) {
 			list.push(cursor.getString(0));
 			cursor.moveToNext();
 		}
 		return list;
 	}
 
 	public void insertMsg(String gameid, long time, String username, String msg)
 	{
 		Object[] data = {gameid, time, username, msg};
 		db.execSQL("INSERT INTO msgtable (gameid, time, username, msg) VALUES (?, ?, ?, ?);", data);
 	}
 
 	public void updateOnlineGame(String gameid, long stime, String zfen, String history)
 	{
 		String[] data1 = {gameid};
 
 		SQLiteCursor cursor = (SQLiteCursor) db.rawQuery("SELECT * FROM onlinegames WHERE gameid=?", data1);
 		Bundle row = rowToBundle(cursor, 0);
 
		GameInfo info = new GameInfo(context, history, row.getString("white"), row.getString("black"));
 
 		int ply = info.getPly(), yourturn = info.getYourTurn();
 
 		Object[] data2 = {stime, ply, yourturn, zfen, history, gameid};
 		db.execSQL("UPDATE onlinegames SET stime=?, ply=?, yourturn=?, zfen=?, history=? WHERE gameid=?;", data2);
 	}
 
 	public void insertOnlineGame(String gameid, int gametype, long ctime, String white, String black)
 	{
 		Object[] data = {gameid, gametype, ctime, white, black};
 		db.execSQL("INSERT INTO onlinegames (gameid, gametype, ctime, white, black) VALUES (?, ?, ?, ?, ?);", data);
 	}
 }
