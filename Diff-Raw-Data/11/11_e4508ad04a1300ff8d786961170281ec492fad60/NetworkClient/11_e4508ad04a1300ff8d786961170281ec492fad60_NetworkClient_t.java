 package com.chess.genesis;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.os.Handler;
 import android.os.Message;
 import android.preference.PreferenceManager;
 import java.io.IOException;
 import java.net.SocketException;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 class NetworkClient implements Runnable
 {
 	public final static int NONE = 0;
 	public final static int LOGIN = 1;
 	public final static int REGISTER = 2;
 	public final static int JOIN_GAME = 3;
 	public final static int NEW_GAME = 4;
 	public final static int GAME_STATUS = 7;
 	public final static int GAME_INFO = 8;
 	public final static int SUBMIT_MOVE = 9;
 	public final static int SUBMIT_MSG = 10;
 	public final static int SYNC_GAMIDS = 11;
 	public final static int GAME_SCORE = 12;
 	public final static int GAME_DATA = 13;
 	public final static int RESIGN_GAME = 14;
 	public final static int SYNC_GAMES = 15;
 	public final static int SYNC_MSGS = 16;
 	public final static int GET_OPTION = 17;
 	public final static int SET_OPTION = 18;
 	public final static int POOL_INFO = 19;
 	public final static int NUDGE_GAME = 20;
 	public final static int IDLE_RESIGN = 21;
 	public final static int USER_STATS = 22;
 	public final static int GAME_DRAW = 23;
 
 	private final Context context;
 	private final Handler callback;
 	private final SocketClient socket;
 
 	private JSONObject json;
 	private int fid = NONE;
 	private boolean loginRequired;
 	private boolean error = false;
 
 	public NetworkClient(final Context _context, final Handler handler)
 	{
 		callback = handler;
 		context = _context;
 		socket = SocketClient.getInstance();
 	}
 
 	public NetworkClient(final SocketClient Socket, final Context _context, final Handler handler)
 	{
 		callback = handler;
 		context = _context;
 		socket = Socket;
 	}
 
 	// Crypto.LoginKey makes a network connection, but all
 	// network calls must be on a non-main thread. This finishes
 	// the json setup started in login_user.
 	private void login_setup()
 	{
 		final JSONObject json2 = new JSONObject();
 
 		try {
 			try {
 				json.put("passhash", Crypto.LoginKey(socket, json.getString("passhash")));
 			} catch (JSONException e) {
 				e.printStackTrace();
 				throw new RuntimeException();
 			}
 		} catch (SocketException e) {
 			try {
 				json2.put("result", "error");
 				json2.put("reason", "Can't contact server for sending data");
 			} catch (JSONException j) {
 				j.printStackTrace();
 				throw new RuntimeException();
 			}
 			error = true;
 		} catch (IOException e) {
 			try {
 				json2.put("result", "error");
				json2.put("reason", "Lost connection during sending data");
 			} catch (JSONException j) {
 				j.printStackTrace();
 				throw new RuntimeException();
 			}
 			error = true;
 		}
 		if (error)
 			callback.sendMessage(Message.obtain(callback, fid, json2));
 	}
 
 	private JSONObject send_request(final JSONObject data)
 	{
 		JSONObject json2 = new JSONObject();
 
 		try {
 			socket.write(data);
 		} catch (SocketException e) {
 			try {
 				json2.put("result", "error");
 				json2.put("reason", "Can't contact server for sending data");
 			} catch (JSONException j) {
 				j.printStackTrace();
 				throw new RuntimeException();
 			}
 			error = true;
 		} catch (IOException e) {
 			try {
 				json2.put("result", "error");
				json2.put("reason", "Lost connection during sending data");
 			} catch (JSONException j) {
 				j.printStackTrace();
 				throw new RuntimeException();
 			}
 			error = true;
 		}
 		if (error)
 			return json2;
 
 		try {
 			json2 = socket.read();
 		} catch (SocketException e) {
 			try {
 				json2.put("result", "error");
 				json2.put("reason", "Can't contact server for recieving data");
 			} catch (JSONException j) {
 				j.printStackTrace();
 				throw new RuntimeException();
 			}
 			error = true;
 		} catch (IOException e) {
 			try {
 				json2.put("result", "error");
				json2.put("reason", "Lost connection during recieving data");
 			} catch (JSONException j) {
 				j.printStackTrace();
 				throw new RuntimeException();
 			}
 			error = true;
 		} catch (JSONException e) {
 			try {
 				json2.put("result", "error");
 				json2.put("reason", "Server response illogical");
 			} catch (JSONException j) {
 				j.printStackTrace();
 				throw new RuntimeException();
 			}
 			error = true;
 		}
 		return json2;
 	}
 
 	private boolean relogin()
 	{
 		if (socket.getIsLoggedIn())
 			return true;
 
 		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
 		final String username = pref.getString("username", "!error!");
 		final String password = pref.getString("passhash", "!error!");
 
 		JSONObject json2 = new JSONObject();
 
 		try {
 			try {
 				json2.put("request", "login");
 				json2.put("username", username);
 				json2.put("passhash", Crypto.LoginKey(socket, password));
 			} catch (JSONException e) {
 				e.printStackTrace();
 				throw new RuntimeException();
 			}
 		} catch (SocketException e) {
 			try {
 				json2.put("result", "error");
 				json2.put("reason", "Can't contact server for sending data");
 			} catch (JSONException j) {
 				j.printStackTrace();
 				throw new RuntimeException();
 			}
 			error = true;
 		} catch (IOException e) {
 			try {
 				json2.put("result", "error");
				json2.put("reason", "Lost connection during sending data");
 			} catch (JSONException j) {
 				j.printStackTrace();
 				throw new RuntimeException();
 			}
 			error = true;
 		}
 		if (error) {
 			callback.sendMessage(Message.obtain(callback, fid, json2));
 			return false;
 		}
 
 		// Send login request
 		json2 = send_request(json2);
 
 		try {
 			if (!json2.getString("result").equals("ok")) {
 				callback.sendMessage(Message.obtain(callback, fid, json2));
 				return false;
 			}
 			socket.setIsLoggedIn(true);
 			return true;
 		} catch (JSONException e) {
 			e.printStackTrace();
 			throw new RuntimeException();
 		}
 	}
 
 	public void run()
 	{
 		if (fid == LOGIN)
 			login_setup();
 
 		if (error || (loginRequired && !relogin())) {
 			error = false;
 			socket.disconnect();
 			return;
 		}
 
 		final JSONObject json2 = send_request(json);
 		if (error) {
 			error = false;
 			socket.disconnect();
 		}
 
 		callback.sendMessage(Message.obtain(callback, fid, json2));
 	}
 
 	public void register(final String username, final String password, final String email)
 	{
 		fid = REGISTER;
 		loginRequired = false;
 
 		json = new JSONObject();
 
 		try {
 			json.put("request", "register");
 			json.put("username", username);
 			json.put("passhash", Crypto.HashPasswd(password));
 			json.put("email", email);
 		} catch (JSONException e) {
 			e.printStackTrace();
 			throw new RuntimeException();
 		}
 	}
 
 	public void login_user(final String username, final String password)
 	{
 		fid = LOGIN;
 		loginRequired = false;
 
 		json = new JSONObject();
 
 		try {
 			json.put("request", "login");
 			json.put("username", username);
 
 			// temporarily save password to passhash
 			// login_setup will finish the creation of
 			// the json in a new thread.
 			json.put("passhash", password);
 		} catch (JSONException e) {
 			e.printStackTrace();
 			throw new RuntimeException();
 		}
 	}
 
 	public void join_game(final String gametype)
 	{
 		fid = JOIN_GAME;
 		loginRequired = true;
 
 		json = new JSONObject();
 
 		try {
 			json.put("request", "joingame");
 			json.put("gametype", gametype);
 		} catch (JSONException e) {
 			e.printStackTrace();
 			throw new RuntimeException();
 		}
 	}
 
 	public void new_game(final String opponent, final String gametype, final String color)
 	{
 		fid = NEW_GAME;
 		loginRequired = true;
 
 		json = new JSONObject();
 
 		try {
 			json.put("request", "newgame");
 			json.put("opponent", opponent);
 			json.put("gametype", gametype);
 			json.put("color", color);
 		} catch (JSONException e) {
 			e.printStackTrace();
 			throw new RuntimeException();
 		}
 
 	}
 
 	public void submit_move(final String gameid, final String move)
 	{
 		fid = SUBMIT_MOVE;
 		loginRequired = true;
 
 		json = new JSONObject();
 
 		try {
 			json.put("request", "sendmove");
 			json.put("gameid", gameid);
 			json.put("move", move);
 		} catch (JSONException e) {
 			e.printStackTrace();
 			throw new RuntimeException();
 		}
 	}
 
 	public void resign_game(final String gameid)
 	{
 		fid = RESIGN_GAME;
 		loginRequired = true;
 
 		json = new JSONObject();
 
 		try {
 			json.put("request", "resign");
 			json.put("gameid", gameid);
 		} catch (JSONException e) {
 			e.printStackTrace();
 			throw new RuntimeException();
 		}
 	}
 
 	public void submit_msg(final String gameid, final String msg)
 	{
 		fid = SUBMIT_MSG;
 		loginRequired = true;
 
 		json = new JSONObject();
 
 		try {
 			json.put("request", "sendmsg");
 			json.put("gameid", gameid);
 			json.put("txt", msg);
 		} catch (JSONException e) {
 			e.printStackTrace();
 			throw new RuntimeException();
 		}
 	}
 
 	public void sync_msgs(final long time)
 	{
 		fid = SYNC_MSGS;
 		loginRequired = true;
 
 		json = new JSONObject();
 
 		try {
 			json.put("request", "syncmsgs");
 			json.put("time", time);
 		} catch (JSONException e) {
 			e.printStackTrace();
 			throw new RuntimeException();
 		}
 	}
 
 	public void game_status(final String gameid)
 	{
 		fid = GAME_STATUS;
 		loginRequired = true;
 
 		json = new JSONObject();
 
 		try {
 			json.put("request", "gamestatus");
 			json.put("gameid", gameid);
 		} catch (JSONException e) {
 			e.printStackTrace();
 			throw new RuntimeException();
 		}
 	}
 
 	public void game_info(final String gameid)
 	{
 		fid = GAME_INFO;
 		loginRequired = true;
 
 		json = new JSONObject();
 
 		try {
 			json.put("request", "gameinfo");
 			json.put("gameid", gameid);
 		} catch (JSONException e) {
 			e.printStackTrace();
 			throw new RuntimeException();
 		}
 	}
 
 	public void game_data(final String gameid)
 	{
 		fid = GAME_DATA;
 		loginRequired = true;
 
 		json = new JSONObject();
 
 		try {
 			json.put("request", "gamedata");
 			json.put("gameid", gameid);
 		} catch (JSONException e) {
 			e.printStackTrace();
 			throw new RuntimeException();
 		}
 	}
 
 	public void sync_gameids(final String type)
 	{
 		fid = SYNC_GAMIDS;
 		loginRequired = true;
 
 		json = new JSONObject();
 
 		try {
 			json.put("request", "gameids");
 			json.put("type", type);
 		} catch (JSONException e) {
 			e.printStackTrace();
 			throw new RuntimeException();
 		}
 	}
 
 	public void sync_games(final long time)
 	{
 		fid = SYNC_GAMES;
 		loginRequired = true;
 
 		json = new JSONObject();
 
 		try {
 			json.put("request", "syncgames");
 			json.put("time", time);
 		} catch (JSONException e) {
 			e.printStackTrace();
 			throw new RuntimeException();
 		}
 	}
 
 	public void game_score(final String gameid)
 	{
 		fid = GAME_SCORE;
 		loginRequired = true;
 
 		json = new JSONObject();
 
 		try {
 			json.put("request", "gamescore");
 			json.put("gameid", gameid);
 		} catch (JSONException e) {
 			e.printStackTrace();
 			throw new RuntimeException();
 		}
 	}
 
 	public <Type> void set_option(final String option, final Type value)
 	{
 		fid = SET_OPTION;
 		loginRequired = true;
 
 		json = new JSONObject();
 
 		try {
 			json.put("request", "setoption");
 			json.put("option", option);
 			json.put("value", value);
 		} catch (JSONException e) {
 			e.printStackTrace();
 			throw new RuntimeException();
 		}
 	}
 
 	public void get_option(final String option)
 	{
 		fid = GET_OPTION;
 		loginRequired = true;
 
 		json = new JSONObject();
 
 		try {
 			json.put("request", "getoption");
 			json.put("option", option);
 		} catch (JSONException e) {
 			e.printStackTrace();
 			throw new RuntimeException();
 		}
 	}
 
 	public void pool_info()
 	{
 		fid = POOL_INFO;
 		loginRequired = true;
 
 		json = new JSONObject();
 
 		try {
 			json.put("request", "poolinfo");
 		} catch (JSONException e) {
 			e.printStackTrace();
 			throw new RuntimeException();
 		}
 	}
 
 	public void nudge_game(final String gameid)
 	{
 		fid = NUDGE_GAME;
 		loginRequired = true;
 
 		json = new JSONObject();
 
 		try {
 			json.put("request", "nudge");
 			json.put("gameid", gameid);
 		} catch (JSONException e) {
 			e.printStackTrace();
 			throw new RuntimeException();
 		}
 	}
 
 	public void idle_resign(final String gameid)
 	{
 		fid = IDLE_RESIGN;
 		loginRequired = true;
 
 		json = new JSONObject();
 
 		try {
 			json.put("request", "idleresign");
 			json.put("gameid", gameid);
 		} catch (JSONException e) {
 			e.printStackTrace();
 			throw new RuntimeException();
 		}
 	}
 
 	public void user_stats(final String username)
 	{
 		fid = USER_STATS;
 		loginRequired = true;
 
 		json = new JSONObject();
 
 		try {
 			json.put("request", "userstats");
 			json.put("username", username);
 		} catch (JSONException e) {
 			e.printStackTrace();
 			throw new RuntimeException();
 		}
 	}
 
 	public void game_draw(final String gameid, final String action)
 	{
 		fid = GAME_DRAW;
 		loginRequired = true;
 
 		json = new JSONObject();
 
 		try {
 			json.put("request", "draw");
 			json.put("gameid", gameid);
 			json.put("action", action);
 		} catch (JSONException e) {
 			e.printStackTrace();
 			throw new RuntimeException();
 		}
 	}
 }
