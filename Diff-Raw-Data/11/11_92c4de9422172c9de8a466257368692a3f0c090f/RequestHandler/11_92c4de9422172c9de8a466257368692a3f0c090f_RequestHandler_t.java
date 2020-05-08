 package uk.codingbadgers.bsocks.threading;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.net.Socket;
 import java.util.logging.Level;
 import org.bukkit.ChatColor;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.Server;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import org.json.simple.JSONArray;
 import org.json.simple.JSONObject;
 import org.json.simple.parser.JSONParser;
 import org.json.simple.parser.ParseException;
 
 import uk.codingbadgers.bsocks.bSocksModule;
 
 public class RequestHandler extends Thread {
 	
 	final private Socket m_sock;
 	final private String m_passHash;
 	final private JavaPlugin m_plugin;
 	final private bSocksModule m_module;
 
 	/**
 	 * Instantiates a new request handler.
 	 *
 	 * @param plugin the plugin instance
 	 * @param passHash the password as an md5 hash
 	 * @param sock the socket the connection was made on
 	 * @param module the module instance
 	 */
 	public RequestHandler(JavaPlugin plugin, String passHash, Socket sock, bSocksModule module) {
 		m_sock = sock;
 		m_plugin = plugin;
 		m_passHash = passHash;
 		m_module = module;
 	}
 
 	/**
 	 * Run the request handler thread
 	 */	
 	public void run() {
 		
 		JSONParser parser = new JSONParser();
 		try {
 			
 			BufferedReader br = new BufferedReader(new InputStreamReader(m_sock.getInputStream()));
 			
 			JSONObject json = null;
 			String line = br.readLine();
 			try {
 				json = (JSONObject)parser.parse(line);
 			} catch (ParseException pe) {
 				m_module.log(Level.WARNING, "Error Parsing: " + line);
 				m_module.log(Level.WARNING, pe.toString());
 				br.close();
 				m_sock.close();
 				return;
 			}
             
 			if (json == null) {
 				m_module.log(Level.SEVERE, "Invalid request from " + m_sock.getInetAddress().getHostAddress());
                 br.close();
                 m_sock.close();
                 return;
 			}
 
     		if (json.get("password") == null || !json.get("password").equals(m_passHash)) {
     			m_module.log(Level.SEVERE, "Wrong password from " + m_sock.getInetAddress().getHostAddress());
                 br.close();
                 m_sock.close();
                 return;
             }
     		
     		String type = (String)json.get("command");
 
 			if (type.equals("message"))
 				handleMessage(json);	
 			else if (type.equals("serverstats"))
 				handleServerStatsCommand(json, m_sock);
 			else if (type.equals("lookup"))
 				handleLookup(json, m_sock);
 			else if (type.equals("executeCommand"))
 				handleExecuteCommand(json);
 			
 			br.close();
 			m_sock.close();
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 	}	
 
 	/**
 	 * Handle lookup. Looks for a player whos name starts with a given string
 	 *
 	 * @param command the command
 	 * @param sock the sock
 	 * @throws IOException Signals that an I/O exception has occurred.
 	 */
 	@SuppressWarnings("unchecked")
 	private void handleLookup(JSONObject command, Socket sock) throws IOException {
 		JSONObject responce = new JSONObject();
 		JSONArray players = new JSONArray();
 		
		for (OfflinePlayer player : m_plugin.getServer().getOfflinePlayers()) {
 			if (player.getName().startsWith((String) command.get("player")))
 				players.add(player.getName());
 		}
 		
 		responce.put("players", players);
 		
 		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
 		bw.write(responce.toJSONString() + "\n");
 		bw.flush();
 		bw.close();
 	}
 
 	/**
 	 * Handle server stats command. Returning information about the server and its players
 	 *
 	 * @param command the command
 	 * @param sock the sock
 	 * @throws Exception the exception
 	 */
 	@SuppressWarnings("unchecked")
 	private void handleServerStatsCommand(JSONObject command, Socket sock) throws Exception {
 		
 		Server server = m_plugin.getServer();
 		Player[] onlinePlayers = server.getOnlinePlayers();
 		Runtime runtime = Runtime.getRuntime();
 		
 		JSONObject responce = new JSONObject();
 		
 		responce.put("bukkit-version", server.getBukkitVersion());
 		responce.put("server-name", server.getServerName());
 		responce.put("using-memory-bytes", (runtime.totalMemory() - runtime.freeMemory()));
 		responce.put("max-memory-bytes", runtime.maxMemory());
 		responce.put("noof-processors", runtime.availableProcessors());
 
 		responce.put("max-players", Integer.toString(server.getMaxPlayers()));
 		responce.put("online-players", Integer.toString(onlinePlayers.length));
 		
 		JSONArray players = new JSONArray();
 		
 		for (Player player : onlinePlayers) {
 			JSONObject jplayer = new JSONObject();
 			jplayer.put("player-name", player.getDisplayName());
 			
 			JSONArray groups = new JSONArray();
 			for (String group : m_module.getPermissions().getPlayerGroups(player)) {
 				JSONObject jgroup = new JSONObject();
 				jgroup.put("group", group);
 				groups.add(jgroup);
 			}
 			jplayer.put("rank-name", groups);
 			players.add(jplayer);
 		}
 		
 		responce.put("online-players", players);
 		
 		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
 		bw.write(responce.toJSONString() + "\n");
 		bw.flush();
 		bw.close();	
 	}
 	
 	/**
 	 * Handle a message request, either sending a message to a single player, the entire server
 	 * or the entire server excluding a given player.
 	 *
 	 * @param command the command
 	 */
 	private void handleMessage(JSONObject command) {
 	
 		String mode = (String)command.get("mode");
 				
 		// send a message to a single player
 		if (mode.equalsIgnoreCase("sendMessage")) {
 			String playerName = (String) command.get("playerName");
 			String message = (String) command.get("context");
 			message = formatMessage(message);
 			
 			m_module.log(Level.INFO, "Sending message '" + message + "' to " + playerName);
 			
 			Player player = m_plugin.getServer().getPlayer(playerName);
 			if (player != null) {
 				player.sendMessage(message);
 			}
 			
 		} 
 		// send a message to all players on the server
 		else if (mode.equalsIgnoreCase("sendMessageAll")) {
 			String message = (String)command.get("context");
 			message = formatMessage(message);
 			
 			m_module.log(Level.INFO, "Sending message '" + message + "' to all players");
 			
 			m_plugin.getServer().broadcastMessage(message);
 			
 		} 
 		// message all players, excluding a given player
 		else if (mode.equalsIgnoreCase("sendMessageAllEx")) {
 			Player excludePlayer = m_plugin.getServer().getPlayer((String)command.get("exludedPlayer"));
 
 			String message = (String) command.get("context");
 			message = formatMessage(message);
 			
 			m_module.log(Level.INFO, " Sending message '" + message + "' to all players apart from " + excludePlayer.getName());
 			
 			Player[] players = m_plugin.getServer().getOnlinePlayers();
 			for (int i = 0; i < players.length; ++i) {
 				Player p = players[i];
 				if (p != excludePlayer) {
 					p.sendMessage(message);
 				}				
 			}
 		}
 		else
 		{
 			// We got told to send a message, but with an unknown mode.
 			m_module.log(Level.WARNING, "Unknown message mode '" + mode + "'");
 		}
 	}
 	
 	/**
 	 * Handle execute commands.
 	 *
 	 * @param command the command to handle
 	 */
 	private void handleExecuteCommand(JSONObject json) {
 		
 		// store the name of the sender of the command
 		String senderName = (String)json.get("sender");
 		
 		CommandSender sender = null;
 		// see if its a player command or a console command
 		if (senderName.equalsIgnoreCase("Console"))
 			sender = m_plugin.getServer().getConsoleSender();
 		else 
 			sender = m_plugin.getServer().getPlayer(senderName);
 		
 		// couldn't find a sender, so don't process the command
 		if (sender == null) {
 			m_module.log(Level.SEVERE,  "Player " + senderName + " does not exist");
 			return;
 		}
 
 		// get the command 
 		String command = (String)json.get("context");		
 		m_plugin.getServer().dispatchCommand(sender, command);		
 		m_module.log(Level.INFO, "Executed command '" + command + "' as " + senderName);
 	}
 	
 	/**
 	 * Format a message.
 	 *
 	 * @param message the message to format
 	 * @return the formatted message
 	 */
 	private String formatMessage(String message) {
 
 		message = message.trim();
 		message = replaceColours(message);
 		
 		return message;
 	}
 	
 	/**
 	 * Replace &<id> colours in a message.
 	 *
 	 * @param message the original message
 	 * @return the message with colour codes properly represented
 	 */
 	private static String replaceColours(String message) {
 		
 		// Store the message in a temp buffer
 		String formattedMessage = message;
 		
 		// we'll replace the &<c> as we go, so loop through all of them
 		while(formattedMessage.indexOf("&") != -1) {
 			
 			// get the colour code first as a string, then convert the hex based char into an integer
 			String code = formattedMessage.substring(formattedMessage.indexOf("&") + 1, formattedMessage.indexOf("&") + 2);
 			
 			// get all the text up to the first '&'
 			// get the chat colour from our colour code
 			// skip the &<c> and reattach the rest of the string
 			formattedMessage = formattedMessage.substring(0, formattedMessage.indexOf("&")) +  ChatColor.getByChar(code) + formattedMessage.substring(formattedMessage.indexOf("&") + 2);
 								
 		}
 		
 		return formattedMessage;
 	}
 	
 }
