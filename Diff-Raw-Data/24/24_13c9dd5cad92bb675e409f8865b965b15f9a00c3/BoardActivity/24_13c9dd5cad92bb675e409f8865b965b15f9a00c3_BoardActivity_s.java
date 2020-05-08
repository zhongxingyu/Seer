 package edu.rochester.nbook.monopdroid;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import edu.rochester.nbook.monopdroid.BoardView.BoardViewListener;
 import edu.rochester.nbook.monopdroid.BoardView.DrawState;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.pm.PackageManager.NameNotFoundException;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.Keyboard.Key;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Looper;
 import android.os.Message;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.util.SparseArray;
import android.view.KeyCharacterMap;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.View.OnKeyListener;
import android.webkit.WebView;
 import android.widget.EditText;
 import android.widget.ListView;
 
 public class BoardActivity extends Activity {
 	private static final int MSG_NETEX = 1;
 	private static final int MSG_NICK = 2;
 	private static final int MSG_CLIENT = 3;
 	private static final int MSG_GAMESTATUS = 4;
 	private static final int MSG_STOP = 5;
 	private static final int MSG_RECV = 6;
 	private static final int MSG_COMMAND = 7;
 	private static final int MSG_CONFIGUPDATE = 8;
 	
 	private enum GameStatus {
 		ERROR, CONFIG, INIT, RUN;
 
 		public static GameStatus fromString(String strStatus) {
 			if (strStatus.equals("config")) {
 				return CONFIG;
 			} else if (strStatus.equals("init")) {
 				return INIT;
 			} else if (strStatus.equals("run")) {
 				return RUN;
 			} else {
 				return ERROR;
 			}
 		}
 	}
 
 
 	/**
 	 * The Board UI. Do not access from networking thread.
 	 */
 	private BoardView bv = null;
 	/**
 	 * The chat log. Do not access from networking thread.
 	 */
 	private ListView lv = null;
 	/**
 	 * The chat send box. Do not access from networking thread.
 	 */
 	private EditText et = null;
 	/**
 	 * The networking thread (created in UI thread). Do not access from networking thread.
 	 */
 	private Thread netThread = null;
 	
 	// cross-thread objects 
 	/**
 	 * Used to send messages to networking thread.
 	 */
 	private static volatile Handler netHandler = null;
 	/**
 	 * Used to send messages from networking thread.
 	 */
 	private static volatile Handler mainHandler = null;
 	
 	/**
 	 * This game item.
 	 */
 	private volatile GameItem gameItem = null;
 	/**
 	 * Whether we joined or created this game.
 	 */
 	private volatile boolean isJoin = false;
 	/**
 	 * List of players.
 	 */
 	private volatile SparseArray<Player> players = new SparseArray<Player>();
 	/**
 	 * List of estates.
 	 */
 	private volatile HashMap<String, Estate> estates = new HashMap<String, Estate>();
 	/**
 	 * List of options.
 	 */
 	private volatile List<Configurable> configurables = new ArrayList<Configurable>();
 	/**
 	 * Current player ID.
 	 */
 	private volatile int playerId = 0;
 	/**
 	 * Current player cookie.
 	 */
 	private volatile String cookie = null;
 	/**
 	 * Game status.
 	 */
 	private volatile GameStatus status = GameStatus.ERROR;
 	/**
 	 * Client name.
 	 */
 	private volatile String clientNameSetting;
 	/**
 	 * Client version.
 	 */
 	private volatile String versionSetting;
 	/**
 	 * Current nick name.
 	 */
 	private volatile String nickSetting;
 	
 	// network thread only
 	/**
 	 * The monopd protocol handler.
 	 */
 	private MonoProtocolHandler monopd = null;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
     	Log.d("monopd", "board: BoardActivity start");
 
         setContentView(R.layout.board);
         
         Intent i = getIntent();
 		int game_id = i.getIntExtra("edu.rochester.nbook.game_id", 0);
 		String host = i.getStringExtra("edu.rochester.nbook.host");
 		int port = i.getIntExtra("edu.rochester.nbook.port", 0);
 		String version = i.getStringExtra("edu.rochester.nbook.version");
 		String type = i.getStringExtra("edu.rochester.nbook.type");
 		String type_name = i.getStringExtra("edu.rochester.nbook.type_name");
 		String descr = i.getStringExtra("edu.rochester.nbook.descr");
 		int players = i.getIntExtra("edu.rochester.nbook.players", 0);
 		boolean can_join = i.getBooleanExtra("edu.rochester.nbook.can_join", false);
 		boolean is_join = i.getBooleanExtra("edu.rochester.nbook.act_is_join",false);
 		if (!can_join || game_id < -1 || game_id == 0 || host == "" || port <= 0) {
 			finish();
 			return;
 		}
 		gameItem = new GameItem(game_id, host, port, version, type, type_name, descr, players, can_join);
 		isJoin = is_join;
 		SharedPreferences shPrefs = PreferenceManager.getDefaultSharedPreferences(this);
 		clientNameSetting = shPrefs.getString("ClientName", "monopdroid");
 		try {
 			versionSetting = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
 		} catch (NameNotFoundException e) {
 			versionSetting = "0.0.0";
 		}
 		versionSetting = shPrefs.getString("ClientVersion", versionSetting); 
 		nickSetting = shPrefs.getString("ClientName", "anonymous");
 		
 		mainHandler = new Handler() {
 			/**
 			 * Where network-to-UI messages are received and processed.
 			 */
 			public void handleMessage(Message msg) {
 	    		Bundle rState = msg.getData();
 	    		switch (msg.what) {
 	    		// a network exception has been received
 	    		case MSG_NETEX:
 	    	    	Log.d("monopd", "board: Received MSG_NETEX from NetThread");
 	    			break;
 	    		// the cookie and playerId has been received
 	    		case MSG_CLIENT:
 	    	    	Log.d("monopd", "board: Received MSG_CLIENT from NetThread");
 	    			playerId = rState.getInt("playerId");
 	    			cookie = rState.getString("cookie");
 	    			break;
 	    		// the game status has changed
 	    		case MSG_GAMESTATUS:
 	    	    	Log.d("monopd", "board: Received MSG_GAMESTATUS from NetThread");
 	    			String strStatus = rState.getString("status");
 	    			status = GameStatus.fromString(strStatus);
 	    			switch (status) {
 	    			case CONFIG:
 	    				bv.setState(DrawState.CONFIG);
 	    				break;
 	    			case INIT:
 	    				bv.setState(DrawState.INIT);
 	    				break;
 	    			case RUN:
 	    				bv.setState(DrawState.RUN);
 	    				break;
 	    			}
 	    			break;
 	    		case MSG_CONFIGUPDATE:
 	    	    	Log.d("monopd", "board: Received MSG_CONFIGUPDATE from NetThread");
 	    			bv.setConfigurables(configurables);
 	    			break;
 	    		}
 	    		msg.recycle();
 			}
 		};
 		
 		netThread = new Thread(new NetThreadStart());
 		netThread.start();
 		
 		setTitle(String.format(getString(R.string.title_activity_board), descr));
 		
 		bv = (BoardView) findViewById(R.id.board_ui);
 		et = (EditText) findViewById(R.id.chat_box);
 		lv = (ListView) findViewById(R.id.chat_contents);
 		
 		bv.setBoardViewListener(new BoardViewListener() {
 			@Override
 			public void onConfigChange(String command, String value) {
 				sendCommand(command + value);
 			}
 		});
 		
 		if (is_join) {
 			bv.setState(DrawState.WAIT_JOIN);
 		} else {
 			bv.setState(DrawState.WAIT_CREATE);
 		}
 		
 		et.setOnKeyListener(new OnKeyListener() {
 			@Override
 			public boolean onKey(View v, int keyCode, KeyEvent event) {
 				if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {
 					String text = et.getText().toString();
 					if (text.length() > 0) {
 						sendCommand(text);
 						et.setText("");
 					}
 				}
 				return false;
 			}
 		});
 		
 		Log.d("monopd", "board: Completed activity set-up");
     }
 
 	private void sendCommand(String text) {
 		Bundle state = new Bundle();
 		state.putString("text", text);
 		sendToNetThread(MSG_COMMAND, state);
 	}
     
     @Override
     protected void onPause() {
     	super.onPause();
     	bv.onPause();
     }
 
     @Override
     protected void onResume() {
     	super.onResume();
     	bv.onResume();
     }
 
     @Override
     protected void onStop() {
     	super.onStop();
     	sendToNetThread(MSG_STOP, null);
     }
     
     /**
      * Sends a message to the network thread. Use this and not nethandler.dispatchMessage()
      * @param what The message ID.
      * @param state State data. Can be null if it isn't going to be read.
      */
 	private void sendToNetThread(int what, Bundle state) {
 		Message msg = Message.obtain(netHandler, what);
 		msg.setData(state);
 		netHandler.dispatchMessage(msg);
     }
 	
     private void sendToMainThread(int what, Bundle state) {
 		Message msg = Message.obtain(mainHandler, what);
 		msg.setData(state);
 		mainHandler.dispatchMessage(msg);
     }
     
     private class NetThreadStart implements Runnable {
 		private boolean continueReceive = true;
 		
 		@Override
 		public void run() {
 			Log.d("monopd", "net: Network thread start.");
 			
 			/**
 			 * An object to call to cause a monopd.doReceive() after 250ms.
 			 */
 			final Runnable doDelayedReceive = new Runnable() {
 				@Override
 				public void run() {
 					if (continueReceive) {
 						sendToNetThread(MSG_RECV, null);
 					}
 				}
 			};
 			
 			Looper.prepare();
 			netHandler = new Handler() {
 				/**
 				 * Where UI-to-network messages are received and processed.
 				 */
 				public void handleMessage(Message msg) {
 		    		Bundle rState = msg.getData();
 		    		switch (msg.what) {
 		    		// request to change nick by UI
 		    		case MSG_NICK:
 		    			Log.d("monopd", "net: Received MSG_NICK from BoardActivity");
 		    			String newNick = rState.getString("nick");
 		    			monopd.changeNick(newNick);
 		    			nickSetting = newNick;
 		    			break;
 		    		// stop thread
 		    		case MSG_STOP:
 		    			Log.d("monopd", "net: Received MSG_STOP from BoardActivity");
 		    			continueReceive = false;
 			    		monopd.disconnect();
 		    			netHandler.getLooper().quit();
 		    			break;
 		    		// receive message callback
 		    		case MSG_RECV:
 		    			Log.v("monopd", "net: Received MSG_RECV from BoardActivity");
 		    			if (continueReceive) {
 			    			// receive if there is data
 		    				monopd.doReceive();
 			        		// the only place we message ourself:
 			        		// receive every 250ms
 		    				netHandler.postDelayed(doDelayedReceive, 250);
 		    			}
 		    			break;
 		    		// send message
 		    		case MSG_COMMAND:
 		    			Log.d("monopd", "net: Received MSG_COMMAND from BoardActivity");
 		    			String text = rState.getString("text");
 		    			monopd.sendCommand(text);
 			    	}
 	    			msg.recycle();
 			    }
 			};
 			String host = gameItem.getHost();
 			int port = gameItem.getPort();
 			String type = gameItem.getType();
 			boolean is_join = isJoin;
 			String client = clientNameSetting;
 			String version = versionSetting;
 			String nick = nickSetting;
 			
     		monopd = new MonoProtocolHandler(new MonoProtocolGameListener() {
     			@Override
     			public void onException(String description, Exception ex) {
 	    			Log.d("monopd", "net: Received onException() from MonoProtocolHandler");
     				Bundle state = new Bundle();
     				state.putString("descr", description);
     				state.putString("ex", ex.getMessage());
     				sendToMainThread(MSG_NETEX, state);
     			}
 
     			@Override
     			public void onClose() { }
 
     			@Override
     			public void onServer(String version) {
 	    			Log.d("monopd", "net: Received onServer() from MonoProtocolHandler");
     			}
 
     			@Override
     			public void onClient(int playerId, String cookie) {
 	    			Log.d("monopd", "net: Received onClient() from MonoProtocolHandler");
     				Bundle state = new Bundle();
     				state.putInt("playerId", playerId);
     				state.putString("cookie", cookie);
     				sendToMainThread(MSG_CLIENT, state);
     			}
 
 				@Override
 				public void onPlayerUpdate(int playerId, String key,
 						Object value) {
 			    	Log.d("monopd", "net: Received onPlayerUpdate() from MonoProtocolHandler");
 					Player player = null;
 					player = players.get(playerId, new Player(playerId));
 					if (key.equals("name")) {
 						player.setNick(value.toString());
 					} else if (key.equals("host")) {
 						player.setHost(value.toString());
 					} else if (key.equals("master")) {
 						player.setMaster(value.toString() != "0");
 					} else if (key.equals("money")) {
 						player.setMoney(Integer.parseInt(value.toString()));
 					} else if (key.equals("doublecount")) {
 						player.setDoubleCount(Integer.parseInt(value.toString()));
 					} else if (key.equals("jailcount")) {
 						player.setJailCount(Integer.parseInt(value.toString()));
 					} else if (key.equals("bankrupt")) {
 						player.setBankrupt(value.toString() != "0");
 					} else if (key.equals("jailed")) {
 						player.setJailed(value.toString() != "0");
 					} else if (key.equals("hasturn")) {
 						player.setHasTurn(value.toString() != "0");
 					} else if (key.equals("spectator")) {
 						player.setSpectator(value.toString() != "0");
 					} else if (key.equals("can_roll")) {
 						player.setRoll(value.toString() != "0");
 					} else if (key.equals("canrollagain")) {
 						player.setRollAgain(value.toString() != "0");
 					} else if (key.equals("can_buyestate")) {
 						player.setBuyEstate(value.toString() != "0");
 					} else if (key.equals("canauction")) {
 						player.setAuction(value.toString() != "0");
 					} else if (key.equals("hasdebt")) {
 						player.setHasDebt(value.toString() != "0");
 					} else if (key.equals("canusecard")) {
 						player.setCanUseCard(value.toString() != "0");
 					} else if (key.equals("location")) {
 						player.setLocation(Integer.parseInt(value.toString()));
 					} else if (key.equals("directmove")) {
 						player.setDirectMove(value.toString() != "0");
 					} else if (key.equals("game")) {
 						// ignore
 					} else if (key.equals("cookie")) {
 						// ignore
 					} else if (key.equals("image")) {
 						// ignore
 					} else {
 						Log.w("monopd", "playerupdate with unknown attribute " + key);
 					}
 					players.put(playerId, player);
 				}
 
 				@Override
 				public void onGameUpdate(int gameId, String status) {
 			    	Log.d("monopd", "net: Received onGameUpdate() from MonoProtocolHandler");
 					Bundle state = new Bundle();
 					state.putInt("gameId", gameId);
 					state.putString("status", status);
 					sendToMainThread(MSG_GAMESTATUS, state);
 				}
 
 				@Override
 				public void onConfigUpdate(List<Configurable> configList) {
 			    	Log.d("monopd", "net: Received onConfigUpdate() from MonoProtocolHandler");
 					nextItem:
 					for (Configurable toAdd : configList) {
 						for (int i = 0; i < configurables.size(); i++) {
 							if (toAdd.getCommand().equals(configurables.get(i).getCommand())) {
 								configurables.set(i, toAdd);
 								continue nextItem;
 							}
 						}
 						configurables.add(toAdd);
 						sendToMainThread(MSG_CONFIGUPDATE, null);
 					}
 				}
     		}, host, port, client, version);
     		monopd.sendClientHello();
     		if (is_join) {
     			monopd.changeNick(nick);
     		} else {
     			monopd.changeNick(nick, false);
     			monopd.sendGameType(type);
     		}
     		// do not continuously receive so that we can get messages from
     		// the ui thread to send
     		monopd.doReceive();
 			Log.d("monopd", "postDelay result = " + netHandler.postDelayed(doDelayedReceive, 250));
 			Log.d("monopd", "net: Completed thread set-up");
 			// await messages on network thread
 			Looper.loop();
 		}
     }
 }
