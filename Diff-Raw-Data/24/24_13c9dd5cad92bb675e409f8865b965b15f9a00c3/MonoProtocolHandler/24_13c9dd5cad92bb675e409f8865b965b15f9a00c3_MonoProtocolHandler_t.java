 package edu.rochester.nbook.monopdroid;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.StringReader;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import org.xmlpull.v1.XmlPullParser;
 import org.xmlpull.v1.XmlPullParserException;
 import org.xmlpull.v1.XmlPullParserFactory;
 
 import android.util.Log;
 
 public class MonoProtocolHandler {
 	/**
 	 * Protocol type, meta server or monopd game.
 	 * @author Nate
 	 */
 	private enum MonoProtocolType {
 		META, GAME
 	}
 	
 	/**
 	 * Node type used for parsing of XML.
 	 * @author Nate
 	 */
 	private enum XmlNodeType {
 		NONE,
 		META_ATLANTIC, METASERVER, SERVERGAMELIST, GAME,
 		MONOPD, SERVER, CLIENT, PLAYERUPDATE, MSG, GAMEUPDATE,
 		UPDATEPLAYERLIST, PLAYER, CONFIGUPDATE, OPTION, ESTATEUPDATE,
 		ESTATEGROUPUPDATE, DISPLAY, UPDATEGAMELIST, DELETEPLAYER
 	}
 	
 	/**
 	 * Static map between node names and enum above.
 	 */
 	public static final HashMap<String, XmlNodeType> nodeByName = new HashMap<String, XmlNodeType>() {
 		private static final long serialVersionUID = 6215377626338659579L;
 
 		{
 			put("meta_atlantic", XmlNodeType.META_ATLANTIC);
 			put("metaserver", XmlNodeType.METASERVER);
 			put("servergamelist", XmlNodeType.SERVERGAMELIST);
 			put("game", XmlNodeType.GAME);
 			
 			put("monopd", XmlNodeType.MONOPD);
 			put("server", XmlNodeType.SERVER);
 			put("client", XmlNodeType.CLIENT);
 			put("playerupdate", XmlNodeType.PLAYERUPDATE);
 			put("msg", XmlNodeType.MSG);
 			put("gameupdate", XmlNodeType.GAMEUPDATE);
 			put("updateplayerlist", XmlNodeType.UPDATEPLAYERLIST);
 			put("player", XmlNodeType.PLAYER);
 			put("configupdate", XmlNodeType.CONFIGUPDATE);
 			put("option", XmlNodeType.OPTION);
 			put("estateupdate", XmlNodeType.ESTATEUPDATE);
 			put("estategroupupdate", XmlNodeType.ESTATEGROUPUPDATE);
 			put("display", XmlNodeType.DISPLAY);
 			put("updategamelist", XmlNodeType.UPDATEGAMELIST);
 			put("deleteplayer", XmlNodeType.DELETEPLAYER);
 		}
 	};
 	
 	private Socket socket = null;
 	private boolean socketClosed = false;
 	private BufferedWriter wr = null;
 	private BufferedReader rd = null;
 	private XmlPullParser parser = null;
 	private MonoProtocolListener listener = null;
 	private MonoProtocolType type = MonoProtocolType.GAME;
 	private String client = null;
 	private String version = null;
 	
 	private void initXml() {
 		XmlPullParserFactory factory;
 		try {
 			factory = XmlPullParserFactory.newInstance();
 			parser = factory.newPullParser();
 		} catch (XmlPullParserException ex) {
 			listener.onException("XML parser initialization failure", ex);
 		}
 	}
 
 	private void initSocket(String host, int port) {
 		try {
 			socket = new Socket(host, port);
 		} catch (UnknownHostException ex) {
 			listener.onException("Connection failure", ex);
 		} catch (IOException ex) {
 			listener.onException("Connection failure", ex);
 		}
 		try {
 			wr = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
 		} catch (IOException e) {
 			listener.onException("Socket cannot be written to", e);
 		}
 		try {
 			rd = new BufferedReader(new InputStreamReader(socket.getInputStream()));
 		} catch (IOException e) {
 			listener.onException("Socket cannot be written to", e);
 		}
 	}
 	
 	/**
 	 * Initialize and connect to a meta server.
 	 * @param meta The listener to handle meta server events.
 	 * @param host The meta server host name.
 	 * @param port The meta server port.
 	 * @param client The game client name to send.
 	 * #param version The game client version to send.
 	 */
 	public MonoProtocolHandler(MonoProtocolMetaListener meta, String host, int port, String client, String version) {
 		this.listener = meta;
 		this.type = MonoProtocolType.META;
 		this.client = client;
 		this.version = version;
 		initXml();
 		initSocket(host, port);
 	}
 
 	/**
 	 * Initialize and connect to a game server.
 	 * @param game The listener to handle game server events.
 	 * @param host The game server host name.
 	 * @param port The game server port.
 	 * @param client The game client name to send.
 	 * #param version The game client version to send.
 	 */
 	public MonoProtocolHandler(MonoProtocolGameListener game, String host, int port, String client, String version) {
 		this.listener = game;
 		this.type =  MonoProtocolType.GAME;
 		this.client = client;
 		this.version = version;
 		initXml();
 		initSocket(host, port);
 	}
 	
 	/**
 	 * Send meta game list request.
 	 */
 	public void sendMetaListGames() {
 		if (type == MonoProtocolType.META) {
 			sendCommand("CHECKCLIENT" + client + version, false);
 			sendCommand("GAMELIST");
 		} else {
 			listener.onException("Incorrect monopd handler type", new Exception());
 		}
 	}
 	
 	/**
 	 * Send meta server list request.
 	 */
 	public void sendMetaListServers() {
 		if (type == MonoProtocolType.META) {
 			sendCommand("CHECKCLIENT" + client + version, false);
 			sendCommand("SERVERLIST");
 		} else {
 			listener.onException("Incorrect monopd handler type", new Exception());
 		}
 	}
 	
 	/**
 	 * Send client hello.
 	 * This message is buffered until another message is sent.
 	 */
 	public void sendClientHello() {
 		sendCommand("client name=\"" + client + "\" version=\"" + version + "\" protocol=\"monopd\"", false);
 	}
 
 	/**
 	 * Change nick name and explicitly choose to flush buffer.
 	 * @param nick The nick name to use.
 	 * @param doFlush Whether to send this now or buffer it.
 	 */
 	public void changeNick(String nick, boolean doFlush) {
 		sendCommand(".n" + nick, doFlush);
 	}
 
 	/**
 	 * Change nick name.
 	 * @param nick The nick name to use.
 	 */
 	public void changeNick(String nick) {
 		changeNick(nick, true);
 	}
 
 	/**
 	 * Set game type.
 	 * @param type
 	 */
 	public void sendGameType(String type) {
 		sendCommand(".gn" + type);
 	}
 	
 	/**
 	 * Reconnect command.
 	 * @param cookie The cookie sent last connect.
 	 */
 	public void sendReconnect(String cookie) {
 		sendCommand(".R" + cookie);
 	}
 	
 	/**
 	 * Gets whether the socket has closed.
 	 * @return Returns true if the socket is closed.
 	 */
 	public boolean isClosed() {
 		try {
 			return (socketClosed && socket.getInputStream().available() == 0);
 		} catch (IOException e) {
 			return socketClosed;
 		}
 	}
 	
 	/**
 	 * Sends command to server.
 	 * @param command The comamnd.
 	 * @param doFlush Pass true to send right away, false to wait for more input. 
 	 */
 	private void sendCommand(String command, boolean doFlush) {
 		try {
 			wr.write(command + "\n");
 			if (doFlush) {
 				wr.flush();
 			}
 		} catch (IOException e) {
 			listener.onException("Socket write error", e);
 		}
 	}
 	
 	/**
 	 * Sends command to server, flushing the write buffer after send.
 	 * @param command
 	 */
 	public void sendCommand(String command) {
 		sendCommand(command, true);
 	}
 	
 	/**
 	 * Receives a line of XML from the server and parses it.
 	 * Calls the listener's methods when handled.
 	 * Will block until a whole line is received, so use a background thread!
 	 */
 	public void doReceive() {
 		try {
 			Log.v("monopd", "monopd: Do receive");
 			while (rd.ready()) {
 				String line = rd.readLine();
 				if (line == null) {
 					socketClosed = true;
 					listener.onClose();
 					return;
 				} else {
 					pullParse(line);
 				}
 			}
 		} catch (IOException e) {
 			listener.onException("Socket read error", e);
 			socketClosed = true;
 			listener.onClose();
 		}
 	}
 	
 	/**
 	 * Handles a line of XML. Must be a single complete tag.
 	 * @param line The line to parse.
 	 */
 	private void pullParse(String line) {
 		int eventType;
 		XmlNodeType nodeType = XmlNodeType.NONE;
 		XmlNodeType prevReadNodeType = XmlNodeType.NONE;
 		String attr, val;
 		HashMap<String, String> data = new HashMap<String, String>();
 		List<Object> l = new ArrayList<Object>();
 		
 		try {
 			eventType = parser.getEventType();
 		} catch (XmlPullParserException e) {
 			listener.onException("XML parser error", e);
 			return;
 		}
 		
 		try {
 			parser.setInput(new StringReader(line));
 		} catch (XmlPullParserException e) {
 			listener.onException("XML read error", e);
 			return;
 		}
 		
 		while (eventType != XmlPullParser.END_DOCUMENT) {
 			if (eventType == XmlPullParser.START_DOCUMENT) {
 				Log.v("monopd", "xml: START DOCUMENT");
 			} else if (eventType == XmlPullParser.START_TAG) {
 				Log.v("monopd", "xml: START TAG " + parser.getName());
 				nodeType = nodeByName.get(parser.getName());
 				for (int index = 0; index < parser.getAttributeCount(); index++) {
 					attr = parser.getAttributeName(index);
 					val = parser.getAttributeValue(index);
 					data.put(attr, val);
 				}
 			} else if (eventType == XmlPullParser.END_TAG) {
 				Log.v("monopd", "xml: END TAG " + parser.getName());
 				nodeType = nodeByName.get(parser.getName());
 				if (nodeType == null) {
 					Log.w("monopd", "Unhandled tag type: " + parser.getName());
 				} else {
 					handleNode(nodeType, prevReadNodeType, data, l);
 				}
 				prevReadNodeType = nodeType;
 			} else if (eventType == XmlPullParser.TEXT) {
 				Log.v("monopd", "xml: TEXT TAG " + parser.getText());
 			}
 			try {
 				eventType = parser.next();
 			} catch (XmlPullParserException e) {
 				listener.onException("XML read/format error", e);
 				return;
 			} catch (IOException e) {
 				listener.onException("XML read I/O error", e);
 				return;
 			}
 		}
 		Log.v("monopd", "xml: END DOCUMENT");
 
 		try {
 			parser.setInput(null);
 		} catch (XmlPullParserException e) {
 			listener.onException("XML reset error", e);
 			return;
 		}
 	}
 	
 	/**
 	 * Handles a node in a parse operation. Called when the end tag of any node is reached, with
 	 * a map containing the data in the node's attributes.
 	 * @param nodeType The type of node.
 	 * @param prevReadNodeType The type of node that was read last before this one. Used in some cases for context. 
 	 * @param data The data map. The keys are the attributes. The special key "#text", if present,
 	 * is the text contained in the current node. The methods called by this may manipulate the map to
 	 * aggregate lists and other things I need to do. The methods shuld clear the data once an event
 	 * is dispatched to the listener and the data is no longer needed.
 	 */
 	private void handleNode(XmlNodeType nodeType, XmlNodeType prevReadNodeType, HashMap<String, String> data, List<Object> currentList) {
 		MonoProtocolGameListener glistener = null;
 		MonoProtocolMetaListener mlistener = null;
 		if (this.type == MonoProtocolType.META) {
 			mlistener = (MonoProtocolMetaListener) this.listener;
 		} else {
 			glistener = (MonoProtocolGameListener) this.listener;
 		}
 		switch (nodeType) {
 		case META_ATLANTIC: handleNodeMetaAtlantic(nodeType, prevReadNodeType, data, mlistener, currentList); break;
 		case METASERVER: handleNodeMetaServer(nodeType, data, mlistener); break;
 		case SERVERGAMELIST: handleNodeServerGameList(nodeType, data, mlistener, currentList); break;
 		case GAME:
 			if (this.type == MonoProtocolType.META) {
 				// meta game list item
 				handleNodeGameItem(nodeType, data, mlistener, currentList);
 			} else {
 				// in-game game update notification
 				//NYI: handleNodeGame(nodeType, data, glistener);
 			}
 			break;
 		case MONOPD:
 			// <monopd>[varied]</monopd>
 			break;
 		case SERVER:
 			if (this.type == MonoProtocolType.META) {
 				// meta server list item
 				handleNodeServerItem(nodeType, data, mlistener, currentList);
 			} else {
 				// game server identification
 				handleNodeServer(nodeType, data, glistener);
 			}
 			break;
 		case CLIENT: handleNodeClient(nodeType, data, glistener); break;
 		case PLAYERUPDATE: handleNodePlayerUpdate(nodeType, data, glistener); break;
 		case GAMEUPDATE: handleNodeGameUpdate(nodeType, data, glistener); break;
 		case CONFIGUPDATE: handleNodeConfigUpdate(nodeType, data, glistener, currentList); break;
 		case OPTION: handleNodeOption(nodeType, data, glistener, currentList); break;
 		}
 	}
 
 	/**
 	 * Gets an attribute from a map (constructed in pullParse) as an integer.
 	 * Passes an exception to the listener if integer not valid.
 	 * @param data The map.
 	 * @param attr The attribute name.
 	 * @return The integer value of the attribute.
 	 */
 	private int getAttributeAsInt(HashMap<String, String> data, String attr) {
 		try {
 			if (data.containsKey(attr)) {
 				return Integer.parseInt(data.get(attr).toString());
 			} else {
 				return Integer.MIN_VALUE;
 			}
 		} catch (NumberFormatException nfex) {
 			listener.onException("Number format error", nfex);
 			return Integer.MIN_VALUE;
 		}
 	}
 
 	/**
 	 * Gets an attribute from a map (constructed in pullParse) as a String.
 	 * @param data The map.
 	 * @param attr The attribute name.
 	 * @return The String value of the attribute.
 	 */
 	private String getAttributeAsString(HashMap<String, String> data, String attr) {
 		if (data.containsKey(attr)) {
 			return data.get(attr).toString();
 		} else {
 			return "";
 		}
 	}
 
 	/**
 	 * Gets an attribute from a map (constructed in pullParse) as a boolean.
 	 * Returns false if the value is "0". Returns true in all other cases.
 	 * Passes an exception to the listener if not a valid integer.
 	 * @param map The map.
 	 * @param attr The attribute name.
 	 * @return The boolean value of the attribute.
 	 */
 	private boolean getAttributeAsBoolean(HashMap<String, String> map, String attr) {
 		return getAttributeAsInt(map, attr) != 0;
 	}
 	
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	private void handleNodeMetaAtlantic(XmlNodeType nodeType,
 			XmlNodeType prevReadNodeType, HashMap<String, String> data,
 			MonoProtocolMetaListener mlistener, List list) {
 		if (prevReadNodeType == XmlNodeType.SERVERGAMELIST) {
 			mlistener.onServerGameListEnd();
 		} else if (prevReadNodeType == XmlNodeType.SERVERGAMELIST) {
 			mlistener.onServerList((List<ServerItem>) list);
 			data.clear();
 		} else {
 			// was server identification, do not close
 			return;
 		}
 		
 		listener.onClose();
 		try {
 			socket.close();
 		} catch (IOException e) { }
 	}
 
 	private void handleNodeMetaServer(XmlNodeType nodeType, HashMap<String, String> data,
 			MonoProtocolMetaListener mlistener) {
 		// <metaserver version="#.#.#"></metaserver>
 		mlistener.onMetaServer(getAttributeAsString(data, "version"));
 		data.clear();
 	}
 
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	private void handleNodeServerGameList(XmlNodeType nodeType, HashMap<String, String> data,
 			MonoProtocolMetaListener mlistener, List list) {
 		// <servergamelist host="monopd.gradator.net" port="#" version="#.#.#"><game>[...]</servergamelist>
 		mlistener.onServerGameList(
 				getAttributeAsString(data, "host"),
 				getAttributeAsInt(data, "port"),
 				getAttributeAsString(data, "version"),
 				(List<GameItem>) list);
 		data.clear();
 	}
 
	@SuppressWarnings({ "unchecked", "rawtypes" })
 	private void handleNodeGameItem(XmlNodeType nodeType, HashMap<String, String> data,
 			MonoProtocolMetaListener mlistener, List list) {
 		// <game id="162" players="1" gametype="city" name="Monopoly" description="Ribose2's game" canbejoined="1"/>
 		if (!data.containsKey("players")) {
 			data.put("players", "0");
 		}
 		if (!data.containsKey("canbejoined")) {
 			data.put("canbejoined", "1");
 		}
 		list.add(new GameItem(
 				getAttributeAsInt(data, "id"),
 				getAttributeAsString(data, "host"), 
 				getAttributeAsInt(data, "port"),
 				getAttributeAsString(data, "version"),
 				getAttributeAsString(data, "gametype"),
 				getAttributeAsString(data, "name"),
 				getAttributeAsString(data, "description"),
 				getAttributeAsInt(data, "players"),
 				getAttributeAsBoolean(data, "canbejoined")));
 		data.remove("id");
 		data.remove("gametype");
 		data.remove("name");
 		data.remove("description");
 		data.remove("players");
 		data.remove("canbejoined");
 	}
 
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	private void handleNodeServerItem(XmlNodeType nodeType,
 			HashMap<String, String> data, MonoProtocolMetaListener mlistener, List list) {
 		// <meta_server><server>[]</meta_server>
 		// <server host="monopd.gradator.net" port="1230" version="0.8.3" users="0" />	
 		String host = getAttributeAsString(data, "host");
 		int port = getAttributeAsInt(data, "port");
 		String version = getAttributeAsString(data, "version");
 		int users = getAttributeAsInt(data, "users");
 
 		list.add(new ServerItem(host, port, version, users));
 		
 		data.remove("host");
 		data.remove("port");
 		data.remove("version");
 		data.remove("users");
 	}
 
 	private void handleNodeServer(XmlNodeType nodeType,
 			HashMap<String, String> data, MonoProtocolGameListener glistener) {
 		// <server version="0.8.3" />
 		String version = getAttributeAsString(data, "version");
 		glistener.onServer(version);
 		data.clear();
 	}
 	
 	private void handleNodeClient(XmlNodeType nodeType,
 			HashMap<String, String> data, MonoProtocolGameListener glistener) {
 		// <client playerid="422" cookie="422/1949477926"/>
 		int playerId = getAttributeAsInt(data, "playerid");
 		String cookie = getAttributeAsString(data, "cookie");
 		glistener.onClient(playerId, cookie);
 		data.clear();
 	}
 
 	private void handleNodePlayerUpdate(XmlNodeType nodeType,
 			HashMap<String, String> data, MonoProtocolGameListener glistener) {
 		int playerId = getAttributeAsInt(data, "playerid");
 		for (String key : data.keySet()) {
 			if (!key.equals("playerid")) {
 				glistener.onPlayerUpdate(playerId, key, data.get(key));
 			}
 		}
 		data.clear();
 	}
 
 	private void handleNodeGameUpdate(XmlNodeType nodeType,
 			HashMap<String, String> data, MonoProtocolGameListener glistener) {
 		int gameId = getAttributeAsInt(data, "gameid");
 		String status = getAttributeAsString(data, "status");
 		glistener.onGameUpdate(gameId, status);
 		data.clear();
 	}
 	
 	private void handleNodeOption(XmlNodeType nodeType,
 			HashMap<String, String> data, MonoProtocolGameListener glistener, List<Object> list) {
 		String title = getAttributeAsString(data, "title");
 		String type = getAttributeAsString(data, "type");
 		String command = getAttributeAsString(data, "command");
 		String value = getAttributeAsString(data, "value");
 		boolean editable = getAttributeAsBoolean(data, "editable");
 		
 		list.add(new Configurable(title, type, command, value, editable));
 		
 		data.remove("title");
 		data.remove("type");
 		data.remove("command");
 		data.remove("value");
 		data.remove("editable");
 	}
 
 	@SuppressWarnings("unchecked")
 	private void handleNodeConfigUpdate(XmlNodeType nodeType,
 			HashMap<String, String> data, MonoProtocolGameListener glistener, @SuppressWarnings("rawtypes") List list) {
 		glistener.onConfigUpdate((List<Configurable>) list);
 		data.clear();
 	}
 
 	public void disconnect() {
 		try {
 			socket.close();
 		} catch (IOException e) { }
 	}
 }
