 package com.chess.genesis;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.database.sqlite.SQLiteCursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import java.util.Date;
 import org.json.JSONException;
 import org.json.JSONObject;
 
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
 
 	public Bundle newLocalGame(String gamename, int gametype, int opponent)
 	{
 		long time = (new Date()).getTime();
 		Object[] data = {gamename, time, time, gametype, opponent};
 		String[] data2 = {String.valueOf(time)};
 
 		db.execSQL("INSERT INTO localgames (name, ctime, stime, gametype, opponent) VALUES (?, ?, ?, ?, ?);", data);
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
 
 	public void deleteArchiveGame(String gameid)
 	{
 		Object[] data = {gameid};
 		db.execSQL("DELETE FROM archivegames WHERE gameid=?;", data);
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
 
 	public SQLiteCursor getOnlineGameList(int yourturn)
 	{
 		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
 
 		String username = pref.getString("username", "!error!");
 		String[] data = {username, username, String.valueOf(yourturn)};
 		String query = "SELECT * FROM onlinegames WHERE (white=? OR black=?) AND yourturn=? ORDER BY stime DESC";
 
 		return (SQLiteCursor) db.rawQuery(query, data);
 	}
 
 	public SQLiteCursor getArchiveGameList()
 	{
 		return (SQLiteCursor) db.rawQuery("SELECT * FROM archivegames ORDER BY stime DESC", null);
 	}
 
 	public ObjectArray<String> getOnlineGameIds()
 	{
 		ObjectArray<String> list = new ObjectArray<String>();
 
 		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
 		String username = pref.getString("username", "!error!");
 		String[] data = {username, username};
 
 		SQLiteCursor cursor = (SQLiteCursor) db.rawQuery("SELECT gameid FROM onlinegames WHERE white=? OR black=?", data);
 
 		cursor.moveToFirst();
 		for (int i = 0; i < cursor.getCount(); i++) {
 			list.push(cursor.getString(0));
 			cursor.moveToNext();
 		}
 		return list;
 	}
 
 	public ObjectArray<String> getArchiveGameIds()
 	{
 		ObjectArray<String> list = new ObjectArray<String>();
 
 		SQLiteCursor cursor = (SQLiteCursor) db.rawQuery("SELECT gameid FROM archivegames", null);
 
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
 
 	public void updateOnlineGame(String gameid, int status, long stime, String zfen, String history)
 	{
 		String[] data1 = {gameid};
 
 		SQLiteCursor cursor = (SQLiteCursor) db.rawQuery("SELECT * FROM onlinegames WHERE gameid=?", data1);
 		Bundle row = rowToBundle(cursor, 0);
 
 		GameInfo info = new GameInfo(context, status, history, row.getString("white"), row.getString("black"));
 
 		int ply = info.getPly(), yourturn = info.getYourTurn();
 
 		Object[] data2 = {stime, status, ply, yourturn, zfen, history, gameid};
 		db.execSQL("UPDATE onlinegames SET stime=?, status=?, ply=?, yourturn=?, zfen=?, history=? WHERE gameid=?;", data2);
 	}
 
 	public void insertOnlineGame(String gameid, int gametype, int eventtype, long ctime, String white, String black)
 	{
 		Object[] data = {gameid, gametype, eventtype, ctime, white, black};
 		db.execSQL("INSERT OR REPLACE INTO onlinegames (gameid, gametype, eventtype, ctime, white, black) VALUES (?, ?, ?, ?, ?, ?);", data);
 	}
 
 	public void insertArchiveGame(JSONObject json)
 	{
 	try {
 		String gameid = json.getString("gameid");
 		int gametype = Enums.GameType(json.getString("gametype"));
 		int eventtype = Enums.EventType(json.getString("eventtype"));
 		int status = Enums.GameStatus(json.getString("status"));
 		int w_psrfrom = json.getJSONObject("score").getJSONObject("white").getInt("from");
 		int w_psrto = json.getJSONObject("score").getJSONObject("white").getInt("to");
 		int b_psrfrom = json.getJSONObject("score").getJSONObject("black").getInt("from");
 		int b_psrto = json.getJSONObject("score").getJSONObject("black").getInt("to");
 		long ctime = json.getLong("ctime");
 		long stime = json.getLong("stime");
 		String white = json.getString("white");
 		String black = json.getString("black");
 		String zfen = json.getString("zfen");
 		String history = json.getString("history");
 
 		String tmp[] = zfen.split(":");
 		int ply = Integer.valueOf(tmp[tmp.length - 1]);
 
 		Object[] data = {gameid, gametype, eventtype, status, w_psrfrom, w_psrto, b_psrfrom, b_psrto,
 			ctime, stime, ply, white, black, zfen, history};
 
 		String q1 = "INSERT INTO archivegames ";
 		String q2 = "(gameid, gametype, eventtype, status, w_psrfrom, w_psrto, b_psrfrom, b_psrto, ";
 		String q3 = "ctime, stime, ply, white, black, zfen, history) ";
 		String q4 = "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
 
 		db.execSQL(q1 + q2 + q3 + q4, data);
 	} catch (JSONException e) {
 		e.printStackTrace();
 	}
 	}
 
 	public void archiveNetworkGame(String gameid, int w_from, int w_to, int b_from, int b_to)
 	{
 		String[] data = {gameid};
 
 		SQLiteCursor cursor = (SQLiteCursor) db.rawQuery("SELECT * FROM onlinegames WHERE gameid=?", data);
 		Bundle row = rowToBundle(cursor, 0);
 
 		String tnames = "(gameid, gametype, eventtype, status, w_psrfrom, w_psrto, b_psrfrom, b_psrto, ctime, stime, ply, white, black, zfen, history)";
		String dstring = "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
 
 		Object[] data2 = {row.get("gameid"), row.get("gametype"), row.get("eventtype"), row.get("status"),
 			w_from, w_to, b_from, b_to, row.get("ctime"), row.get("stime"), row.get("ply"),
 			row.get("white"), row.get("black"), row.get("zfen"), row.get("history")};
 
 		db.execSQL("INSERT OR REPLACE INTO archivegames " + tnames + " VALUES " + dstring + ";", data2);
 		db.execSQL("DELETE FROM onlinegames WHERE gameid=?", data);
 	}
 }
