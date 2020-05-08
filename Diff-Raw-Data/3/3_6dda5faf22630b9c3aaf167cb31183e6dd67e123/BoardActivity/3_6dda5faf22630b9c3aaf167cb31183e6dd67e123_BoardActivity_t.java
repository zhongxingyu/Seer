 package edu.rochester.nbook.monopdroid;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.text.DateFormat;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.DialogInterface.OnClickListener;
 import android.content.SharedPreferences.Editor;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.graphics.Color;
 import android.graphics.drawable.GradientDrawable;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.util.SparseArray;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnKeyListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class BoardActivity extends Activity {
     
     private static final HashMap<String, XmlAttribute> playerAttributes = new HashMap<String, XmlAttribute>() {
         private static final long serialVersionUID = 1431923100451372984L;
 
         {
             this.put("name", new XmlAttribute(Player.class, "setNick", XmlAttributeType.STRING));
             this.put("host", new XmlAttribute(Player.class, "setHost", XmlAttributeType.STRING));
             this.put("master", new XmlAttribute(Player.class, "setMaster", XmlAttributeType.BOOLEAN));
             this.put("money", new XmlAttribute(Player.class, "setMoney", XmlAttributeType.INT));
             this.put("doublecount", new XmlAttribute(Player.class, "setDoubleCount", XmlAttributeType.INT));
             this.put("jailcount", new XmlAttribute(Player.class, "setJailCount", XmlAttributeType.INT));
             this.put("bankrupt", new XmlAttribute(Player.class, "setBankrupt", XmlAttributeType.BOOLEAN));
             this.put("jailed", new XmlAttribute(Player.class, "setJailed", XmlAttributeType.BOOLEAN));
             this.put("hasturn", new XmlAttribute(Player.class, "setHasTurn", XmlAttributeType.BOOLEAN));
             this.put("spectator", new XmlAttribute(Player.class, "setSpectator", XmlAttributeType.BOOLEAN));
             this.put("can_roll", new XmlAttribute(Player.class, "setCanRoll", XmlAttributeType.BOOLEAN));
             this.put("canrollagain", new XmlAttribute(Player.class, "setCanRollAgain", XmlAttributeType.BOOLEAN));
             this.put("can_buyestate", new XmlAttribute(Player.class, "setCanBuyEstate", XmlAttributeType.BOOLEAN));
             this.put("canauction", new XmlAttribute(Player.class, "setCanAuction", XmlAttributeType.BOOLEAN));
             this.put("canusecard", new XmlAttribute(Player.class, "setCanUseCard", XmlAttributeType.BOOLEAN));
             this.put("hasdebt", new XmlAttribute(Player.class, "setHasDebt", XmlAttributeType.BOOLEAN));
             this.put("location", new XmlAttribute(Player.class, "setLocation", XmlAttributeType.INT));
             this.put("directmove", new XmlAttribute(Player.class, "setDirectMove", XmlAttributeType.BOOLEAN));
             this.put("game", new XmlAttribute(Player.class, null, XmlAttributeType.INT));
             this.put("cookie", new XmlAttribute(Player.class, null, XmlAttributeType.STRING));
             this.put("image", new XmlAttribute(Player.class, null, XmlAttributeType.STRING));
         }
     };
     
     private static final HashMap<String, XmlAttribute> estateAttributes = new HashMap<String, XmlAttribute>() {
         private static final long serialVersionUID = -1649097477143814788L;
 
         {
             this.put("name", new XmlAttribute(Estate.class, "setName", XmlAttributeType.STRING));
             this.put("houses", new XmlAttribute(Estate.class, "setHouses", XmlAttributeType.INT));
             this.put("money", new XmlAttribute(Estate.class, "setMoney", XmlAttributeType.INT));
             this.put("price", new XmlAttribute(Estate.class, "setPrice", XmlAttributeType.INT));
             this.put("mortgageprice", new XmlAttribute(Estate.class, "setMortgagePrice", XmlAttributeType.INT));
             this.put("unmortgageprice", new XmlAttribute(Estate.class, "setUnmortgagePrice", XmlAttributeType.INT));
             this.put("sellhouseprice", new XmlAttribute(Estate.class, "setSellHousePrice", XmlAttributeType.INT));
             this.put("mortgaged", new XmlAttribute(Estate.class, "setMortgaged", XmlAttributeType.BOOLEAN));
             this.put("color", new XmlAttribute(Estate.class, "setColor", XmlAttributeType.COLOR));
             this.put("bgcolor", new XmlAttribute(Estate.class, "setBgColor", XmlAttributeType.COLOR));
             this.put("owner", new XmlAttribute(Estate.class, "setOwner", XmlAttributeType.INT));
             this.put("houseprice", new XmlAttribute(Estate.class, "setHousePrice", XmlAttributeType.INT));
             this.put("groupid", new XmlAttribute(Estate.class, "setEstateGroup", XmlAttributeType.INT));
             this.put("can_be_owned", new XmlAttribute(Estate.class, "setCanBeOwned", XmlAttributeType.BOOLEAN));
             this.put("can_toggle_mortgage", new XmlAttribute(Estate.class, "setCanToggleMortgage", XmlAttributeType.BOOLEAN));
             this.put("can_buy_houses", new XmlAttribute(Estate.class, "setCanBuyHouses", XmlAttributeType.BOOLEAN));
             this.put("can_sell_houses", new XmlAttribute(Estate.class, "setCanSellHouses", XmlAttributeType.BOOLEAN));
             this.put("rent0", new XmlAttribute(Estate.class, "setRent0", XmlAttributeType.RENT));
             this.put("rent1", new XmlAttribute(Estate.class, "setRent1", XmlAttributeType.RENT));
             this.put("rent2", new XmlAttribute(Estate.class, "setRent2", XmlAttributeType.RENT));
             this.put("rent3", new XmlAttribute(Estate.class, "setRent3", XmlAttributeType.RENT));
             this.put("rent4", new XmlAttribute(Estate.class, "setRent4", XmlAttributeType.RENT));
             this.put("rent5", new XmlAttribute(Estate.class, "setRent5", XmlAttributeType.RENT));
             this.put("passmoney", new XmlAttribute(Estate.class, "setPassMoney", XmlAttributeType.INT));
             this.put("taxpercentage", new XmlAttribute(Estate.class, "setTaxPercentage", XmlAttributeType.INT));
             this.put("tax", new XmlAttribute(Estate.class, "setTax", XmlAttributeType.INT));
             this.put("icon", new XmlAttribute(Estate.class, "setIcon", XmlAttributeType.STRING));
             this.put("jail", new XmlAttribute(Estate.class, "setIsJail", XmlAttributeType.BOOLEAN));
             this.put("payamount", new XmlAttribute(Estate.class, "setPayAmount", XmlAttributeType.INT));
             this.put("tojail", new XmlAttribute(Estate.class, "setIsToJail", XmlAttributeType.BOOLEAN));
         }
     };
     
     /**
      * The Board UI. Do not access from networking thread.
      */
     private BoardView boardView = null;
     /**
      * The chat log. Do not access from networking thread.
      */
     private ListView chatList = null;
     /**
      * The chat log adapter. Do not access from networking thread.
      */
     private ChatListAdapter chatListAdapter = null;
     /**
      * The chat send box. Do not access from networking thread.
      */
     private EditText chatSendBox = null;
     /**
      * The player views. Do not access from networking thread.
      */
     private LinearLayout[] playerView = new LinearLayout[4];
     /**
      * The networking handler. Used to send messages to the networking thread.
      */
     private Handler netHandler = null;
     /**
      * The networking thread runner (created in UI thread). Do not access from
      * networking thread.
      */
     private BoardActivityNetworkThread netRunnable = null;
 
     /**
      * This game item.
      */
     private GameItem gameItem = null;
     /**
      * Array of players to show in the 4 slots.
      */
     private int[] playerIds = new int[4];
     /**
      * List of players.
      */
     private SparseArray<Player> players = new SparseArray<Player>();
     /**
      * List of estates.
      */
     private ArrayList<Estate> estates = new ArrayList<Estate>(40);
     /**
      * List of options.
      */
     private List<Configurable> configurables = new ArrayList<Configurable>();
     /**
      * Current player ID.
      */
     private int playerId = 0;
     /**
      * Current player cookie.
      */
     private String cookie = null;
     /**
      * Game status.
      */
     private GameStatus status = GameStatus.ERROR;
     /**
      * Client name.
      */
     private String clientName;
     /**
      * Client version.
      */
     private String clientVersion;
     /**
      * Current nick name.
      */
     private String nickname;
     /**
      * Whether we are the master of this game lobby.
      */
     private boolean isMaster = false;
     /**
      * Whether this onDestroy() occured after saving state.
      */
     private boolean savingState = false;
     /**
      * Whether this onResume() occured with intent info (true) or saved state data (false).
      */
     private boolean firstInit = false;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         Log.d("monopd", "board: BoardActivity start");
         
         this.setContentView(R.layout.board);
         
         storeViews();
         
         BoardActivityState state = (BoardActivityState) getLastNonConfigurationInstance();
         
         firstInit = (state == null);
         
         if (firstInit) {
             Intent i = this.getIntent();
             int game_id = i.getIntExtra("edu.rochester.nbook.game_id", 0);
             String host = i.getStringExtra("edu.rochester.nbook.host");
             int port = i.getIntExtra("edu.rochester.nbook.port", 0);
             String version = i.getStringExtra("edu.rochester.nbook.version");
             String type = i.getStringExtra("edu.rochester.nbook.type");
             String type_name = i.getStringExtra("edu.rochester.nbook.type_name");
             String descr = i.getStringExtra("edu.rochester.nbook.descr");
             int playerCount = i.getIntExtra("edu.rochester.nbook.players", 0);
             boolean can_join = i.getBooleanExtra("edu.rochester.nbook.can_join", false);
             GameItemType item_type = GameItemType.fromInt(i.getIntExtra("edu.rochester.nbook.act_type", 0));
             // check can_join value
             if (!can_join) {
                 this.finish();
                 return;
             }
             // check item type valid
             switch (item_type) {
             default:
             case ERROR:
             case READY:
             case LOADING:
             case EMPTY:
                 // failure
                 this.finish();
                 return;
             case CREATE:
             case JOIN:
             case RECONNECT:
                 // success
                 break;
             }
             List<ServerItem> servers = new ArrayList<ServerItem>();
             servers.add(new ServerItem(host, port, version, playerCount));
             this.gameItem = new GameItem(item_type, game_id, servers, type, type_name, descr, playerCount, can_join);
             this.isMaster = gameItem.getItemType() == GameItemType.CREATE;
             SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
             this.clientName = "monopdroid";
             try {
                 this.clientVersion = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
             } catch (NameNotFoundException e) {
                 this.clientVersion = "0.0.0";
             }
             this.nickname = prefs.getString("player_nick", "anonymous");
             
             this.netRunnable = new BoardActivityNetworkThread();
         } else {
             boardView.setSurfaceRunner(state.surfaceRunner);
             chatListAdapter.restoreState(state);
             playerIds = state.playerIds;
             netRunnable = state.netRunnable;
             gameItem = state.gameItem;
             estates = state.estates;
             players = state.players;
             configurables = state.configurables;
             playerId = state.playerId;
             cookie = state.cookie;
             status = state.status;
             clientName = state.clientName;
             clientVersion = state.clientVersion;
             nickname = state.nickname;
             isMaster = state.isMaster;
         }
         
         attachListeners();
 
         Log.d("monopd", "board: Completed activity set-up");
     }
 
     @Override
     public Object onRetainNonConfigurationInstance() {
         BoardActivityState state = new BoardActivityState();
         state.surfaceRunner = boardView.getSurfaceRunner();
         state.chat = chatListAdapter.saveState();
         state.playerIds = playerIds;
         state.netRunnable = netRunnable;
         state.gameItem = gameItem;
         state.players = players;
         state.estates = estates;
         state.configurables = configurables;
         state.playerId = playerId;
         state.cookie = cookie;
         state.status = status;
         state.clientName = clientName;
         state.clientVersion = clientVersion;
         state.nickname = nickname;
         state.isMaster = isMaster;
         savingState = true;
         return state;
     }
     
     public class BoardActivityState {
         public BoardViewSurfaceThread surfaceRunner;
         
         public List<ChatItem> chat;
         public int[] playerIds = new int[4];
         public BoardActivityNetworkThread netRunnable;
         public GameItem gameItem;
         public SparseArray<Player> players;
         public ArrayList<Estate> estates;
         public List<Configurable> configurables;
         public int playerId;
         public String cookie;
         public GameStatus status;
         public String clientName;
         public String clientVersion;
         public String nickname;
         public boolean isMaster;
     }
     
     private void attachListeners() {
         this.boardView.setBoardViewListener(new BoardViewListener() {
 
             @Override
             public void onConfigChange(String command, String value) {
                 Log.d("monopd", "BoardView tapped config change " + command + " = " + value);
                 Bundle state = new Bundle();
                 state.putString("command", command);
                 state.putString("value", value);
                 BoardActivity.this.sendToNetThread(BoardNetworkAction.MSG_CONFIG, state);
             }
 
             @Override
             public void onStartGame() {
                 Log.d("monopd", "BoardView tapped start game");
                 BoardActivity.this.sendToNetThread(BoardNetworkAction.MSG_GAME_START, null);
             }
 
             @Override
             public void onResize(int width, int height) {
                 Log.d("monopd", "BoardView resized to " + width + "," + height);
                 redrawRegions();
             }
         });
         this.chatSendBox.setOnKeyListener(new OnKeyListener() {
 
             @Override
             public boolean onKey(View v, int keyCode, KeyEvent event) {
                 if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {
                     String text = BoardActivity.this.chatSendBox.getText().toString();
                     if (text.length() > 0) {
                         BoardActivity.this.sendCommand(text);
                         BoardActivity.this.chatSendBox.setText("");
                     }
                 }
                 return false;
             }
         });
         this.chatList.setOnItemClickListener(new OnItemClickListener() {
 
             @Override
             public void onItemClick(AdapterView<?> listView, View itemView, int position, long id) {
                 ChatItem item = BoardActivity.this.chatListAdapter.getItem(position);
                 if (item.getPlayerId() > 0) {
                     BoardActivity.this.boardView.overlayPlayerInfo(BoardActivity.this.players.get(item.getPlayerId()));
                 } else if (item.getEstateId() > 0) {
                     BoardActivity.this.boardView.overlayEstateInfo(BoardActivity.this.estates.get(item.getEstateId()));
                 }
             }
         });
         
         this.netRunnable.setActivity(this, new MonoProtocolGameListener() {
             @Override
             public void onException(String description, Exception ex) {
                 Log.v("monopd", "net: Received onException() from MonoProtocolHandler");
                 Bundle state = new Bundle();
                 state.putString("error", description + ": " + ex.getMessage());
                 showDialog(R.id.dialog_conn_error, state);
             }
 
             @Override
             public void onClose(boolean remote) {
                 Log.d("monopd", "net: onClose(" + remote + ")");
                 if (remote) {
                     Bundle state = new Bundle();
                     state.putString("error", "Connection lost.");
                     showDialog(R.id.dialog_conn_error, state);
                 }
             }
 
             @Override
             public void onServer(String version) {
                 Log.v("monopd", "net: Received onServer() from MonoProtocolHandler");
             }
 
             @Override
             public void onClient(int playerId, String cookie) {
                 Log.v("monopd", "net: Received onClient() from MonoProtocolHandler");
                 BoardActivity.this.playerId = playerId;
                 BoardActivity.this.cookie = cookie;
             }
 
             @Override
             public void onPlayerUpdate(final int playerId, HashMap<String, String> data) {
                 Log.v("monopd", "net: Received onPlayerUpdate() from MonoProtocolHandler");
                 final HashMap<String, String> map = new HashMap<String, String>(data);
                 runOnUiThread(new Runnable() {
 
                     @Override
                     public void run() {
                         boolean changingLocation = false;
                         boolean changingMaster = false;
                         boolean wasMaster = false;
                         Player player = players.get(playerId);
                         if (player == null) {
                             player = new Player(playerId);
                             switch (status) {
                             case ERROR:
                             case RECONNECT:
                             case CREATE:
                             case JOIN:
                                 // ignore playerupdates before game state change!
                                 return;
                             case CONFIG:
                             case RUN:
                             case INIT:
                                 // add to player list
                                 for (int i = 0; i < 4; i++) {
                                     if (playerIds[i] == 0) {
                                         playerIds[i] = playerId;
                                         break;
                                     }
                                 }
                                 break;
                             }
                         }
                         for (String key : map.keySet()) {
                             String value = map.get(key);
                             XmlAttribute attr = playerAttributes.get(key);
                             if (attr == null) {
                                 Log.w("monopd", "player." + key + " was unknown. Value = " + value);
                             } else {
                                 if (key.equals("location")) {
                                     changingLocation = true;
                                 }
                                 if (key.equals("master")) {
                                     changingMaster = true;
                                     wasMaster = player.isMaster();
                                 }
                                 attr.set(player, value);
                             }
                         }
                         players.put(playerId, player);
                         updatePlayerView();
                         boardView.drawEstateOwnerRegions(estates, playerIds, players);
                         if (changingLocation) {
                             animateMove(player);
                         }
                         if (changingMaster) {
                             if (player.getPlayerId() == BoardActivity.this.playerId && player.isMaster() != isMaster) {
                                 isMaster = player.isMaster();
                             }
                             //if (player.isMaster() && !wasMaster) {
                             //    writeMessage(player.getNick() + " is now game master.", Color.YELLOW, playerId, -1, false);
                             //}
                         }
                     }
                 });
             }
 
             @Override
             public void onPlayerDelete(final int playerId) {
                 runOnUiThread(new Runnable() {
 
                     @Override
                     public void run() {
                         players.delete(playerId);
                         boolean deleted = false;
                         for (int i = 0; i < 4; i++) {
                             if (playerIds[i] == playerId) {
                                 deleted = true;
                             }
                             if (deleted) {
                                 if (i < 3) {
                                     playerIds[i] = playerIds[i + 1];
                                 } else {
                                     playerIds[i] = 0;
                                     /*for (int j = 0; j < players.size(); j++) {
                                         int playerIdJ = players.keyAt(j);
                                         boolean foundPlayerJ = false;
                                         for (int k = 0; k < 4; k++) {
                                             if (playerIdJ == playerIds[k] || players.get(playerIdJ).getNick().equals("_metaserver_")) {
                                                 foundPlayerJ = true;
                                             }
                                         }
                                         if (!foundPlayerJ) {
                                             playerIds[i] = playerIdJ;
                                             break;
                                         }
                                     }*/
                                 }
                             }
                         }
                         updatePlayerView();
                     }
                 });
             }
 
             @Override
             public void onEstateUpdate(final int estateId, final HashMap<String, String> data) {
                 Log.v("monopd", "net: Received onEstateUpdate() from MonoProtocolHandler");
                 final HashMap<String, String> map = new HashMap<String, String>(data);
                 runOnUiThread(new Runnable() {
 
                     @Override
                     public void run() {
                         Estate estate;
                         boolean isNew = false;
                         if (estateId < estates.size()) {
                             estate = estates.get(estateId);
                         } else {
                             isNew = true;
                             estate = new Estate(estateId);
                         }
                         for (String key : map.keySet()) {
                             String value = map.get(key);
                             XmlAttribute attr = BoardActivity.estateAttributes.get(key);
                             if (attr == null) {
                                 Log.w("monopd", "estate." + key + " was unknown. Value = " + value);
                             } else {
                                 attr.set(estate, value);
                             }
                         }
                         if (isNew) {
                             if (estateId > estates.size()) {
                                 estates.add(estates.size(), estate);
                             } else {
                                 estates.add(estateId, estate);
                             }
                         } else {
                             estates.set(estateId, estate);
                         }
                         boardView.drawEstateOwnerRegions(estates, playerIds, players);
                     }
                 });
             }
 
             @Override
             public void onGameUpdate(final int gameId, final String status) {
                 Log.v("monopd", "net: Received onGameUpdate() from MonoProtocolHandler");
                 BoardActivity.this.runOnUiThread(new Runnable() {
 
                     @Override
                     public void run() {
                         if (gameId > 0) {
                             gameItem.setGameId(gameId);
                         }
                         
                         BoardActivity.this.status = GameStatus.fromString(status);
                         BoardActivity.this.boardView.setStatus(BoardActivity.this.status);
                         redrawRegions();
                         
                         switch (BoardActivity.this.status) {
                         case CONFIG:
                             writeMessage("Entered lobby. The game master can now choose game configuration.", Color.YELLOW, -1, -1, false);
                             break;
                         case INIT:
                             writeMessage("Starting game...", Color.YELLOW, -1, -1, false);
                             saveCookie();
                             break;
                         case RUN:
                            initPlayerColors();
                             break;
                         }
                     }
                 });
             }
 
             @Override
             public void onConfigUpdate(final List<Configurable> configList) {
                 Log.v("monopd", "net: Received onConfigUpdate() from MonoProtocolHandler");
                 nextItem: for (final Configurable toAdd : configList) {
                     for (int i = 0; i < BoardActivity.this.configurables.size(); i++) {
                         if (toAdd.getCommand().equals(BoardActivity.this.configurables.get(i).getCommand())) {
                             final int iClosure = i;
                             BoardActivity.this.runOnUiThread(new Runnable() {
 
                                 @Override
                                 public void run() {
                                     BoardActivity.this.configurables.set(iClosure, toAdd);
                                 }
                             });
                             continue nextItem;
                         }
                     }
                     BoardActivity.this.runOnUiThread(new Runnable() {
 
                         @Override
                         public void run() {
                             BoardActivity.this.configurables.add(toAdd);
                         }
                     });
                 }
                 BoardActivity.this.runOnUiThread(new Runnable() {
 
                     @Override
                     public void run() {
                         if (boardView.isRunning()) {
                             boardView.drawConfigRegions(configurables, isMaster);
                         }
                     }
                 });
             }
 
             @Override
             public void onChatMessage(final int playerId, final String author, final String text) {
                 Log.v("monopd", "net: Received onChatMessage() from MonoProtocolHandler");
                 BoardActivity.this.runOnUiThread(new Runnable() {
 
                     @Override
                     public void run() {
                         if (author.length() > 0) {
                             writeMessage("<" + author + "> " + text, Color.WHITE, playerId, -1, false);
                             // handle !ping, !date, !version
                             if (text.startsWith("!")) {
                                 boolean doCommand = false;
                                 int spacePos = text.indexOf(' ');
                                 if (spacePos > 0) {
                                     String requestName = text.substring(spacePos).trim();
                                     if (requestName.length() == 0 || requestName.equals(nickname)) {
                                         doCommand = true;
                                     }
                                 } else {
                                     doCommand = true;
                                     spacePos = text.length();
                                 }
                                 if (doCommand) {
                                     String command = text.substring(1, spacePos);
                                     if (command.equals("ping")) {
                                         sendCommand("pong");
                                     } else if (command.equals("version")) {
                                         sendCommand(clientName + " " + clientVersion);
                                     } else if (command.equals("date")) {
                                         sendCommand(DateFormat.getDateTimeInstance().format(new Date()));
                                     }
                                 }
                             }
                         }
                     }
                 });
             }
 
             @Override
             public void onErrorMessage(final String text) {
                 Log.v("monopd", "net: Received onErrorMessage() from MonoProtocolHandler");
                 BoardActivity.this.runOnUiThread(new Runnable() {
 
                     @Override
                     public void run() {
                         writeMessage("ERROR: " + text, Color.RED, -1, -1, false);
                     }
                 });
             }
 
             @Override
             public void onDisplayMessage(final int estateId, final String text, final boolean clearText,
                     final boolean clearButtons) {
                 Log.v("monopd", "net: Received onDisplayMessage() from MonoProtocolHandler");
                 if (text.length() == 0) {
                     return;
                 }
                 BoardActivity.this.runOnUiThread(new Runnable() {
 
                     @Override
                     public void run() {
                         writeMessage("GAME: " + text, Color.CYAN, -1, ((estateId == -1) ? 0 : estateId), clearButtons);
                     }
                 });
             }
 
             @Override
             public void onPlayerListUpdate(String type, List<Player> list) {
                 Log.v("monopd", "net: Received onPlayerListUpdate() from MonoProtocolHandler");
                 /*if (type.equals("full")) {
                     //Log.d("monopd", "players: Full list update");
                     final int[] newPlayerIds = new int[4];
                     for (int i = 0; i < list.size() && i < 4; i++) {
                         newPlayerIds[i] = list.get(i).getPlayerId();
                     }
                     BoardActivity.this.runOnUiThread(new Runnable() {
 
                         @Override
                         public void run() {
                             setPlayerView(newPlayerIds);
                         }
                     });
                 } else if (type.equals("edit")) {
                     // Log.d("monopd", "players: Edit " +
                     // list.get(0).getNick());
                     BoardActivity.this.runOnUiThread(new Runnable() {
 
                         @Override
                         public void run() {
                             BoardActivity.this.updatePlayerView();
                         }
                     });
                 } else if (type.equals("add")) {
                     // Log.d("monopd", "players: Add " +
                     // list.get(0).getNick());
                     final int[] newPlayerIds = BoardActivity.this.playerIds;
                     for (int i = 0; i < list.size(); i++) {
                         for (int j = 0; j < 4; j++) {
                             final int playerId = list.get(i).getPlayerId();
                             if (newPlayerIds[j] == 0 || newPlayerIds[j] == playerId) {
                                 BoardActivity.this.runOnUiThread(new Runnable() {
 
                                     @Override
                                     public void run() {
                                         writeMessage(players.get(playerId).getNick() + " joined the game.", Color.YELLOW, playerId, -1, false);
                                     }
                                 });
                                 newPlayerIds[j] = playerId;
                                 break;
                             }
                         }
                     }
                     BoardActivity.this.runOnUiThread(new Runnable() {
 
                         @Override
                         public void run() {
                             BoardActivity.this.setPlayerView(newPlayerIds);
                         }
                     });
                 } else if (type.equals("del")) {
                     //Log.d("monopd", "players: Delete "
                     //        + BoardActivity.this.players.get(list.get(0).getPlayerId()).getNick());
                     final int[] newPlayerIds = BoardActivity.this.playerIds;
                     for (int i = 0; i < list.size(); i++) {
                         boolean moveBack = false;
                         for (int j = 0; j < 4; j++) {
                             if (!moveBack && newPlayerIds[j] == list.get(i).getPlayerId()) {
                                 final int playerIdClosure = newPlayerIds[j];
                                 BoardActivity.this.runOnUiThread(new Runnable() {
 
                                     @Override
                                     public void run() {
                                         writeMessage(players.get(playerIdClosure).getNick() + " left the game.", Color.YELLOW, playerIdClosure, -1, false);
                                     }
                                 });
                                 moveBack = true;
                             }
                             if (moveBack) {
                                 newPlayerIds[j] = j < 3 ? newPlayerIds[j + 1] : 0;
                             }
                         }
                     }
                     BoardActivity.this.runOnUiThread(new Runnable() {
 
                         @Override
                         public void run() {
                             BoardActivity.this.setPlayerView(newPlayerIds);
                         }
                     });
                 } else {
                     Log.w("monopd", "unrecognized playerlistupdate type: " + type + " " + list.get(0).getNick());
                 }*/
             }
 
             @Override
             public void setHandler(Handler netHandler) {
                 BoardActivity.this.netHandler = netHandler;
             }
 
             @Override
             public void onGameItemUpdate(GameItem item) {
                 if (gameItem.getGameId() == item.getGameId()) {
                     if (item.getPlayers() > 0) {
                         gameItem.setPlayers(item.getPlayers());
                     }
                     if (item.getType() != null) {
                         gameItem.setType(item.getType());
                     }
                     if (item.getTypeName() != null) {
                         gameItem.setTypeName(item.getTypeName());
                     }
                     if (item.getDescription() != null) {
                         gameItem.setDescription(item.getDescription());
                     }
                     gameItem.setCanJoin(item.canJoin());
                 }
             }
         });
         
         this.setTitle(String.format(this.getString(R.string.title_activity_board), gameItem.getDescription()));
     }
 
     @Override
     protected void onStart() {
         super.onStart();
     }
     
 
     @Override
     protected void onPause() {
         super.onPause();
         this.boardView.onPause();
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         this.boardView.onResume();
         if (firstInit) {
             // first init
             switch (gameItem.getItemType()) {
             case JOIN:
                 status = GameStatus.JOIN;
                 this.boardView.setStatus(GameStatus.JOIN);
                 writeMessage("Joining " + gameItem.getDescription() + "...", Color.YELLOW, -1, -1, false);
                 break;
             case CREATE:
                 status = GameStatus.CREATE;
                 this.boardView.setStatus(GameStatus.CREATE);
                 writeMessage("Creating " + gameItem.getTypeName() + " game...", Color.YELLOW, -1, -1, false);
                 break;
             case RECONNECT:
                 status = GameStatus.RECONNECT;
                 this.boardView.setStatus(GameStatus.RECONNECT);
                 writeMessage("Reconnecting to " + gameItem.getDescription(), Color.YELLOW, -1, -1, false);
                 break;
             }
             
             Thread netThread = new Thread(this.netRunnable);
             netThread.start();
             firstInit = false;
         } else {
             // re-init
             this.sendToNetThread(BoardNetworkAction.MSG_RESUME, null);
             this.updatePlayerView();
         }
         redrawRegions();
     }
 
     private void initPlayerColors() {
         int[] colors = { Color.GREEN, Color.RED, Color.CYAN, Color.YELLOW };
         int index = 0;
         for (int playerId : playerIds) {
             if (playerId > 0) {
                 players.get(playerId).setDrawColor(colors[index]);
             }
             index++;
         }
     }
     
     private void redrawRegions() {
         switch (this.boardView.getStatus()) {
         case CREATE:
             boardView.createTextRegion("Creating game...");
             break;
         case JOIN:
             boardView.createTextRegion("Joining game...");
             break;
         case INIT:
             boardView.createTextRegion("Starting game...");
             break;
         case CONFIG:
             boardView.drawConfigRegions(configurables, isMaster);
             break;
         case RUN:
             boardView.drawBoardRegions(estates);
             boardView.drawEstateOwnerRegions(estates, playerIds, players);
             boardView.drawPieces(estates, playerIds, players);
             break;
         }
     }
 
     private void animateMove(Player player) {
         int start = player.getLastLocation();
         int end = player.getLocation();
         boolean directMove = player.getDirectMove();
         player.setDrawLocation(start);
         players.put(player.getPlayerId(), player);
         boardView.drawPieces(estates, playerIds, players);
         if (directMove) {
             Bundle args = new Bundle();
             args.putInt("estateId", end);
             sendToNetThread(BoardNetworkAction.MSG_TURN, args);
             player.setDrawLocation(end);
             players.put(player.getPlayerId(), player);
             boardView.drawPieces(estates, playerIds, players);
         } else {
             if (start > end) {
                 end += 40;
             }
             for (int i = start + 1; i <= end; i++) {
                 Bundle args = new Bundle();
                 args.putInt("estateId", (i % 40));
                 sendToNetThread(BoardNetworkAction.MSG_TURN, args);
                 player.setDrawLocation(i % 40);
                 players.put(player.getPlayerId(), player);
                 boardView.drawPieces(estates, playerIds, players);
             }
         }
     }
 
     @Override
     protected void onDestroy() {
         super.onDestroy();
         if (netHandler != null) {
             if (savingState) {
                 this.sendToNetThread(BoardNetworkAction.MSG_PAUSE, null);
             } else {
                 this.sendToNetThread(BoardNetworkAction.MSG_STOP, null);
             }
         }
     }
     
     private void storeViews() {
         this.boardView = (BoardView) this.findViewById(R.id.board_ui);
         this.chatSendBox = (EditText) this.findViewById(R.id.chat_box);
         this.chatListAdapter = new ChatListAdapter(this, R.layout.chat_item);
         this.chatList = (ListView) this.findViewById(R.id.chat_contents);
         this.chatList.setAdapter(this.chatListAdapter);
         this.playerView[0] = (LinearLayout) this.findViewById(R.id.player_item1);
         this.playerView[1] = (LinearLayout) this.findViewById(R.id.player_item2);
         this.playerView[2] = (LinearLayout) this.findViewById(R.id.player_item3);
         this.playerView[3] = (LinearLayout) this.findViewById(R.id.player_item4);
     }
 
     private void sendCommand(String text) {
         Bundle state = new Bundle();
         state.putString("text", text);
         this.sendToNetThread(BoardNetworkAction.MSG_COMMAND, state);
     }
 
     /**
      * Sends a message to the network thread. Use this and not
      * nethandler.dispatchMessage()
      * 
      * @param action
      *            The message ID.
      * @param arguments
      *            Named arguments of the message. Can be null to specify zero arguments.
      */
     public void sendToNetThread(BoardNetworkAction action, Bundle arguments) {
         Message msg = Message.obtain(netHandler, action.getWhat());
         msg.setData(arguments);
         netHandler.dispatchMessage(msg);
     }
 
     /**
      * Write a message to the chat list. The chat item will be tappable if
      * either playerId or estateId is positive.
      * 
      * @param msgText
      *            The text.
      * @param color
      *            The color.
      * @param playerId
      *            A player ID associated with this message. Set to 0 or negative to ignore.
      * @param estateId
      *            An estate ID associated with this message. Set to 0 or negative to ignore.
      * @param clearButtons
      *            Whether the buttons should be cleared, if any. TODO: move to anotehr function.
      */
     private void writeMessage(String msgText, int color, int playerId, int estateId, boolean clearButtons) {
         BoardActivity.this.chatListAdapter.add(new ChatItem(msgText, color, playerId, estateId, clearButtons));
         BoardActivity.this.chatListAdapter.notifyDataSetChanged();
     }
 
     /**
      * Set the player list to show the specified 4 players. Player ID 0 means
      * that slot is empty.
      * 
      * @param playerIds
      *            Player IDs of the players to show.
      */
     private void setPlayerView(int[] playerIds) {
         this.playerIds = playerIds;
         this.updatePlayerView();
     }
 
     /**
      * Updates the player view with new data from the player list.
      */
     private void updatePlayerView() {
         for (int i = 0; i < 4; i++) {
             if (this.playerIds[i] == 0) {
                 this.playerView[i].setVisibility(View.GONE);
             } else {
                 Player player = this.players.get(this.playerIds[i]);
                 if (player == null) {
                     this.playerView[i].setVisibility(View.GONE);
                     Log.w("monopd", "players: Unknown player ID " + this.playerIds[i]);
                 } else {
                     this.playerView[i].setVisibility(View.VISIBLE);
                     TextView text1 = (TextView) this.playerView[i].findViewById(R.id.player_text_1);
                     TextView text2 = (TextView) this.playerView[i].findViewById(R.id.player_text_2);
                     text1.setText(player.getNick());
                     switch (status) {
                     case CONFIG:
                         if (player.isMaster()) {
                             text1.setTextColor(Color.YELLOW);
                         } else {
                             text1.setTextColor(Color.WHITE);
                         }
                         text2.setText(player.getHost());
                         break;
                     case RUN:
                         if (player.isTurn()) {
                             text1.setTextColor(Color.YELLOW);
                         } else {
                             text1.setTextColor(Color.WHITE);
                         }
                         
                         GradientDrawable grad = BoardViewSurfaceThread.createRadialGradient(player.getDrawColor());
                         
                         text2.setText("$" + player.getMoney());
                         text2.setCompoundDrawablePadding(5);
                         text2.setCompoundDrawablesWithIntrinsicBounds(grad, null, null, null);
                         break;
                     default:
                         text1.setTextColor(Color.WHITE);
                         text2.setText("");
                         break;
                     }
                 }
             }
         }
     }
 
     public GameStatus getGameStatus() {
         return status;
     }
 
     public GameItem getGameItem() {
         return gameItem;
     }
 
     public String getClientName() {
         return clientName;
     }
 
     public String getClientVersion() {
         return clientVersion;
     }
 
     public String getNickname() {
         return nickname;
     }
 
     public String getSavedCookie() {
         SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
         return prefs.getString("saved_cookie", null);
     }
     
     private void clearCookie() {
         SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
         Editor editor = prefs.edit();
         
         editor.remove("saved_cookie");
         editor.remove("saved_server");
         editor.remove("saved_port");
         editor.remove("saved_version");
         editor.remove("saved_type");
         editor.remove("saved_type_name");
         editor.remove("saved_descr");
         editor.remove("saved_players");
         
         editor.commit();
     }
 
     private void saveCookie() {
         SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
         Editor editor = prefs.edit();
         
         int gameId = gameItem.getGameId();
         String server = gameItem.getServer().getHost();
         int port = gameItem.getServer().getPort();
         String version = gameItem.getServer().getVersion();
         String type = gameItem.getType();
         String type_name = gameItem.getTypeName();
         String descr = gameItem.getDescription();
         int players = gameItem.getPlayers();
         
         editor.putInt("saved_game_id", gameId);
         editor.putString("saved_cookie", cookie);
         editor.putString("saved_server", server);
         editor.putInt("saved_port", port);
         editor.putString("saved_version", version);
         editor.putString("saved_type", type);
         editor.putString("saved_type_name", type_name);
         editor.putString("saved_descr", descr);
         editor.putInt("saved_players", players);
         
         editor.commit();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         this.getMenuInflater().inflate(R.menu.board_activity, menu);
         return true;
     }
 
     @Override
     public boolean onMenuItemSelected(int featureId, MenuItem item) {
         switch (item.getItemId()) {
         case R.id.menu_bankrupt:
             sendToNetThread(BoardNetworkAction.MSG_DECLARE_BANKRUPCY, null);
             break;
         case R.id.menu_settings:
             break;
         }
         return true;
     }
 
     @Override
     protected Dialog onCreateDialog(int id) {
         AlertDialog.Builder bldr = new AlertDialog.Builder(this);
         OnClickListener doQuit = new OnClickListener() {
             @Override
             public void onClick(DialogInterface dialog, int which) {
                 dialog.dismiss();
                 finish();
             }
         };
         OnClickListener doReconnect = new OnClickListener() {
             @Override
             public void onClick(DialogInterface dialog, int which) {
                 dialog.dismiss();
                 sendToNetThread(BoardNetworkAction.MSG_SOCKET_RECONNECT, null);
             }
         };
         switch (id) {
         case R.id.dialog_conn_error:
             bldr.setTitle(R.string.dialog_conn_error);
             bldr.setMessage(R.string.empty);
             bldr.setPositiveButton(R.string.reconnect, doReconnect);
             bldr.setNegativeButton(R.string.quit, doQuit);
             return bldr.create();
         }
         return null;
     }
 
     @Override
     protected void onPrepareDialog(int id, Dialog dialog, Bundle args) {
         super.onPrepareDialog(id, dialog, args);
         TextView message = (TextView) dialog.findViewById(android.R.id.message);
         switch (id) {
         case R.id.dialog_conn_error:
             String error = args.getString("error");
             message.setText(error);
             break;
         }
     }
 }
