 package com.chess.genesis;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.os.Handler;
 import android.os.Message;
 import android.preference.PreferenceManager;
 import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 class SyncClient implements Runnable
 {
 	public final static int MSG = 101;
 
 	public final static int FULL_SYNC = 0;
 	public final static int REGULAR_SYNC = 1;
 	public final static int ACTIVE_SYNC = 2;
 	public final static int ARCHIVE_SYNC = 3;
 	public final static int MSG_SYNC = 4;
 
 	private final Context context;
 	private final Handler callback;
 	private final NetworkClient net;
 
 	private int lock = 0;
 	private int syncType = REGULAR_SYNC;
 	private int gameType = Enums.ONLINE_GAME;
 	private boolean error = false;
 
 	private final Handler handle = new Handler()
 	{
 		public void handleMessage(final Message msg)
 		{
 			final JSONObject json = (JSONObject) msg.obj;
 
 		try {
 			if (json.getString("result").equals("error")) {
 				callback.sendMessage(Message.obtain(callback, MSG, json));
 				error = true;
 				return;
 			}
 		} catch (JSONException e) {
 			e.printStackTrace();
 			throw new RuntimeException();
 		}
 
 			switch (msg.what) {
 			case NetworkClient.GAME_STATUS:
 				game_status(json);
 				break;
 			case NetworkClient.GAME_INFO:
 				game_info(json);
 				break;
 			case NetworkClient.GAME_DATA:
 				game_data(json);
 				break;
 			case NetworkClient.SYNC_GAMIDS:
 				if (gameType == Enums.ONLINE_GAME)
 					sync_active(json);
 				else
 					sync_archive(json);
 				break;
 			case NetworkClient.SYNC_GAMES:
 				sync_recent(json);
 				break;
 			case NetworkClient.SYNC_MSGS:
 				saveMsgs(json);
 				break;
 			}
 			// release lock
 			lock--;
 		}
 	};
 
 	public SyncClient(final Context _context, final Handler handler)
 	{
 		context = _context;
 		callback = handler;
 
 		syncType = REGULAR_SYNC;
 		net = new NetworkClient(context, handle);
 	}
 
 	private void trylock()
 	{
 	try {
 		lock++;
 		while (lock > 0 && !error)
 			Thread.sleep(16);
 		lock = 0;
 	} catch (java.lang.InterruptedException e) {
 		e.printStackTrace();
 		throw new RuntimeException();
 	}
 	}
 
 	public void setSyncType(final int Type)
 	{
 		syncType = Type;
 	}
 
 	public void run()
 	{
 		switch (syncType) {
 		case FULL_SYNC:
 			gameType = Enums.ONLINE_GAME;
 			net.sync_gameids("active");
 			net.run();
 			trylock();
 
 			gameType = Enums.ARCHIVE_GAME;
 			net.sync_gameids("archive");
 			net.run();
 			trylock();
 
 			net.sync_msgs(0);
 			net.run();
 			trylock();
 			break;
 		case REGULAR_SYNC:
 			final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
 			final long mtime = pref.getLong("lastmsgsync", 0);
 			final long gtime = pref.getLong("lastgamesync", 0);
 
 			net.sync_games(gtime);
 			net.run();
 			trylock();
 
 			net.sync_msgs(mtime);
 			net.run();
 			trylock();
 			break;
 		case ACTIVE_SYNC:
 			gameType = Enums.ONLINE_GAME;
 			net.sync_gameids("active");
 			net.run();
 			trylock();
 			break;
 		case ARCHIVE_SYNC:
 			gameType = Enums.ARCHIVE_GAME;
 			net.sync_gameids("archive");
 			net.run();
 			trylock();
 			break;
 		case MSG_SYNC:
 			net.sync_msgs(0);
 			net.run();
 			trylock();
 			break;
 		}
 
 		if (!error) {
 			final JSONObject json = new JSONObject();
 			try {
 				json.put("result", "ok");
 				json.put("reason", "gamelist updated");
 			} catch (JSONException e) {
 				e.printStackTrace();
 				throw new RuntimeException();
 			}
 			callback.sendMessage(Message.obtain(callback, MSG, json));
 		}
 	}
 
 	private void sync_recent(final JSONObject json)
 	{
 	try {
 		final JSONArray ids = json.getJSONArray("gameids");
 		final long time = json.getLong("time");
		final ExecutorService pool = Executors.newCachedThreadPool();
 
 		for (int i = 0; i < ids.length(); i++) {
 			if (error)
 				return;
 			final NetworkClient nc = new NetworkClient(context, handle);
 			nc.game_status(ids.getString(i));
			pool.submit(nc);
 
 			lock++;
 		}
 		// Save sync time
 		final Editor pref = PreferenceManager.getDefaultSharedPreferences(context).edit();
 		pref.putLong("lastgamesync", time);
 		pref.commit();
 	} catch (JSONException e) {
 		e.printStackTrace();
 		throw new RuntimeException();
 	}
 	}
 
 	private void sync_active(final JSONObject json)
 	{
 	try {
 		final ArrayList<String> list_need = getNeedList(json.getJSONArray("gameids"));
		final ExecutorService pool = Executors.newCachedThreadPool();
 
 		for (int i = 0; i < list_need.size(); i++) {
 			if (error)
 				return;
 			final NetworkClient nc = new NetworkClient(context, handle);
 			nc.game_info(list_need.get(i));
			pool.submit(nc);
 
 			lock++;
 		}
 		// don't save time if only syncing active
 		if (syncType == ACTIVE_SYNC)
 			return;
 		// Save sync time
 		final long time = json.getLong("time");
 		final Editor pref = PreferenceManager.getDefaultSharedPreferences(context).edit();
 		pref.putLong("lastgamesync", time);
 		pref.commit();
 	} catch (JSONException e) {
 		e.printStackTrace();
 		throw new RuntimeException();
 	}
 	}
 
 	private void sync_archive(final JSONObject json)
 	{
 	try {
 		final ArrayList<String> list_need = getNeedList(json.getJSONArray("gameids"));
		final ExecutorService pool = Executors.newCachedThreadPool();
 
 		for (int i = 0; i < list_need.size(); i++) {
 			if (error)
 				return;
 			final NetworkClient nc = new NetworkClient(context, handle);
 			nc.game_data(list_need.get(i));
			pool.submit(nc);
 
 			lock++;
 		}
 	} catch (JSONException e) {
 		e.printStackTrace();
 		throw new RuntimeException();
 	}
 	}
 
 	private ArrayList<String> getNeedList(final JSONArray ids)
 	{
 	try {
 		final ArrayList<String> list_need = new ArrayList<String>();
 		for (int i = 0; i < ids.length(); i++)
 			list_need.add(ids.getString(i));
 
 		if (syncType == ACTIVE_SYNC || syncType == ARCHIVE_SYNC)
 			return list_need;
 
 		final GameDataDB db = new GameDataDB(context);
 		final ObjectArray<String> list;
 		if (gameType == Enums.ONLINE_GAME)
 			list = db.getOnlineGameIds();
 		else
 			list = db.getArchiveGameIds();
 		db.close();
 
 		final ArrayList<String> list_have = new ArrayList<String>();
 		for (int i = 0; i < list.size(); i++)
 			list_have.add(list.get(i));
 
 		list_need.removeAll(list_have);
 		return list_need;
 	} catch (JSONException e) {
 		e.printStackTrace();
 		throw new RuntimeException();
 	}
 	}
 
 	private void game_info(final JSONObject json)
 	{
 		final GameDataDB db = new GameDataDB(context);
 		db.insertOnlineGame(json);
 		db.close();
 	}
 
 	private void game_status(final JSONObject json)
 	{
 		final GameDataDB db = new GameDataDB(context);
 		db.updateOnlineGame(json);
 		db.close();
 	}
 
 	private void game_data(final JSONObject json)
 	{
 		final GameDataDB db = new GameDataDB(context);
 		db.insertArchiveGame(json);
 		db.close();
 	}
 
 	private void saveMsgs(final JSONObject data)
 	{
 	try {
 		final JSONArray msgs = data.getJSONArray("msglist");
 		final long time = data.getLong("time");
 		final GameDataDB db = new GameDataDB(context);
 
 		for (int i = 0; i < msgs.length(); i++) {
 			final JSONObject item = msgs.getJSONObject(i);
 			db.insertMsg(item);
 		}
 		db.close();
 
 		// Save sync time
 		final Editor pref = PreferenceManager.getDefaultSharedPreferences(context).edit();
 		pref.putLong("lastmsgsync", time);
 		pref.commit();
 	}  catch (JSONException e) {
 		e.printStackTrace();
 		throw new RuntimeException();
 	}
 	}
 }
