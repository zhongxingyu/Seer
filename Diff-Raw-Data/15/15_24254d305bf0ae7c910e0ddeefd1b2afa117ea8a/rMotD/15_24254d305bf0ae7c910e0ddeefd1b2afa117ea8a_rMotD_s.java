 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 public class rMotD extends Plugin {
 	public rPropertiesFile Messages;
 	PluginListener listener = new rMotDListener();
 	Logger log = Logger.getLogger("Minecraft");
 	Server MCServer =etc.getServer();
 	String defaultGroup;
 	String versionNumber = "1.4.5"; 
 	public iData data;
 	
 	public rMotD () {
 		Messages = new rPropertiesFile("rMotD.properties");
 	}
 	
 	public void initialize(){
 		etc.getLoader().addListener(PluginLoader.Hook.LOGIN  , listener, this, PluginListener.Priority.MEDIUM);
 		etc.getLoader().addListener(PluginLoader.Hook.COMMAND, listener, this, PluginListener.Priority.MEDIUM);
 		etc.getLoader().addListener(PluginLoader.Hook.SERVERCOMMAND, listener, this, PluginListener.Priority.MEDIUM);
 		defaultGroup = etc.getDataSource().getDefaultGroup().Name;
 		if (iData.iExist()){
 			data = new iData();
 		}
 	} 
 	public void enable(){
 		try {
 			Messages.load();
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "[rMotD]: Exception while loading properties file.", e);
 		}
 		
 		/* TODO (Efficiency): Go through each message, see if any messages actually need these listeners. */
 		// Regex: ^([A-Za-z0-9,]+):([A-Za-z0-9,]*:([A-Za-z0-9,]*disconnect([A-Za-z0-9,]*)
 		etc.getLoader().addListener(PluginLoader.Hook.DISCONNECT , listener, this, PluginListener.Priority.MEDIUM);
 		etc.getLoader().addListener(PluginLoader.Hook.BAN        , listener, this, PluginListener.Priority.MEDIUM);
 		etc.getInstance().addCommand("/grouptell", "Tell members of a group something.");
 		etc.getInstance().addCommand("/rmotd", "Displays your Message of the Day");
 		log.info("[rMotD] Loaded: Version " + versionNumber);
 	}
 	public void disable(){
 		Messages.save();
 		etc.getInstance().removeCommand("/grouptell");
 		log.info("[rMotD] Disabled!");
 	} 
 	
 	public void sendToGroup(String sendToGroup, String message) {
 		String [] arrayOfOne = new String[1];
 		arrayOfOne[0] = sendToGroup;
 		sendToGroups(arrayOfOne, message);
 		return;
 	}
 	
 	/* Looks through all of the messages,
 	 * Sends the messages triggered by groups which 'triggerMessage' is a member of,
 	 * But only if that message has the contents of 'option' as one of its options */
 	public void triggerMessagesWithOption(Player triggerMessage, String option){
 		String[] eventToReplace = new String[0];
 		String[] eventReplaceWith = new String[0];
 		triggerMessagesWithOption(triggerMessage, option, eventToReplace, eventReplaceWith);
 	}
 	
 	public void triggerMessagesWithOption(Player triggerMessage, String option, String[] eventToReplace, String[] eventReplaceWith){
 		ArrayList<String>groupArray = new ArrayList<String>();
 		/* Obtain group list */
 		if (triggerMessage.hasNoGroups()){
 			groupArray.add(defaultGroup);
 		} else {
 			groupArray.addAll(Arrays.asList(triggerMessage.getGroups()));
 		}
 		/* Obtain player list */
 		String playerList = new String();
 		List<Player> players = MCServer.getPlayerList();
 		if (players.size() == 1)
 			playerList = players.get(0).getName();
 		else {
 			for (Player getName : players){
 				playerList = getName.getName() + ", " + playerList;
 			}
 		}
 		
 		groupArray.add("<<everyone>>");
 		for (String groupName : groupArray){
 			if (Messages.keyExists(groupName)){
 				for (String sendToGroups_Message : Messages.getStrings(groupName)){
 					String [] split =  sendToGroups_Message.split(":");
 					String [] options =  split[1].split(",");
 					boolean hookValid = false;
 					if (split[1].isEmpty() && option.equalsIgnoreCase("onlogin")){
 						hookValid = true;
 					} else for (int i = 0; i <options.length && hookValid == false; i++){
 						if(options[i].equalsIgnoreCase(option)) hookValid = true;
 					}
 					
 					if (hookValid) {
 						String message = etc.combineSplit(2, split, ":");
 						
 						/* Tag replacement: First round (triggerer) go! */
 						int balance = 0;
 						if (data != null){
 							balance = data.getBalance(triggerMessage.getName());
 						}
 						String [] replace = {"@"	, "<<triggerer>>"          , "<<triggerer-ip>>"    , "<<triggerer-color>>"   , "<<triggerer-balance>>"  , "<<player-list>>"};
 						String [] with    = {"\n"	, triggerMessage.getName() , triggerMessage.getIP(),triggerMessage.getColor(), Integer.toString(balance), playerList};					
 						message = MessageParser.parseMessage(message, replace, with);
 						if (eventToReplace.length > 0)
 							message = MessageParser.parseMessage(message, eventToReplace, eventReplaceWith);
 						/* Tag replacement end! */
 						
 						sendMessage(message, triggerMessage, split[0]);
 					}
 				}
 			}
 		}
 	}
 	
 	
 	public void sendMessage(String message, Player triggerMessage, String Groups){
 		/* Default: Send to player unless groups are specified.
 		 * If so, send to those instead. */
 		if (Groups.isEmpty()) {
 			sendToPlayer(message, triggerMessage);
 		}
 		else {
 			String [] sendToGroups = Groups.split(",");
 			sendToGroups(sendToGroups, message, triggerMessage);
 		}
 	}
 
 	/* Takes care of 'psuedo-groups' like <<triggerer>>, <<server>>, and <<everyone>>,
 	 * then sends to the rest as normal */
 	public void sendToGroups (String [] sendToGroups, String message, Player triggerer) {
 		ArrayList <String> sendToGroupsFiltered = new ArrayList<String>();
 		Hashtable <Player, Player> sendToUs = new Hashtable<Player, Player>();
 		boolean everyoneTag = false;
 		for (String group : sendToGroups){
 			if (group.equalsIgnoreCase("<<triggerer>>")) {
 				sendToUs.put(triggerer, triggerer);
 			} else if (group.equalsIgnoreCase("<<everyone>>")){
 				sendToUs.clear();
 				for (Player putMe : MCServer.getPlayerList()) {
 					sendToUs.put(putMe, putMe);
 				}
 				everyoneTag = true;
 			} else if (group.equalsIgnoreCase("<<server>>")) {
 				String [] replace = {"<<recipient>>", "<<recipient-ip>>", "<<recipient-color>>", "<<recipient-balance>>"};
 				String [] with    = {"server", "", "", ""};
 				message = "[rMotD] " + MessageParser.parseMessage(message, replace, with);
 				for(String send : message.split("\n"))
 					log.info(send);
 			} else if (group.equalsIgnoreCase("<<command>>")) {
				String command = message.substring(message.indexOf('/'),message.length() -1);
 				triggerer.command(command);
 			}
 			else {
 				sendToGroupsFiltered.add(group);
 			}
 		}
 		if (!everyoneTag){
 			for (Player sendToMe : constructPlayerList(sendToGroups, sendToUs).values()){
 				sendToPlayer(message, sendToMe);
 			}
 		}
 	}
 
 	/* Sends the message string to each group named in sendToGroups */
 	public void sendToGroups (String [] sendToGroups, String message) {
 		for (Player sendToMe :  constructPlayerList(sendToGroups, new Hashtable<Player,Player>()).values()){
 			sendToPlayer(message, sendToMe);
 		}
 		return;
 	}
 	
 	public Hashtable<Player, Player> constructPlayerList(String [] inTheseGroups, Hashtable<Player,Player> List){
 		for (Player addMe: MCServer.getPlayerList()){
 			if (!List.contains(addMe)){
 				if (addMe.hasNoGroups()) {
 					search:
 					for (String isDefault : inTheseGroups) {
 						if (isDefault.equalsIgnoreCase(defaultGroup)) {
 							List.put(addMe,addMe);
 						}
 						break search;
 					}
 				} else {
 					search:
 					for(String memberGroup : addMe.getGroups()) {
 						for(String amIHere : inTheseGroups){
 							if (memberGroup.equalsIgnoreCase(amIHere)){
 								List.put(addMe, addMe);
 								break search;
 							}
 						}
 					}
 				}
 			}
 		}
 		return List;
 	}
 	
 	public void sendToPlayer(String message, Player recipient) {
 		int balance = 0;
 		if (data != null){
 			balance = data.getBalance(recipient.getName());
 		}
 		String [] replace = {"<<recipient>>"    , "<<recipient-ip>>" , "<<recipient-color>>", "<<recipient-balance>>"};
 		String [] with    = {recipient.getName(), recipient.getIP()  , recipient.getColor() , Integer.toString(balance)};
 		message = MessageParser.parseMessage(message, replace, with);
 		/* Tag replacement end. */
 		for(String send : message.split("\n"))
 			recipient.sendMessage(send);
 	}
 	
 	
 	public class rMotDListener extends PluginListener {
 		/* Checks for any messages that the player's group memberships may trigger.
 		 * Parses the message line into:
 		 *  - groups to send it to (or just the player)
 		 *  - options
 		 *  - and the message.
 		 * Sends the message on its merry way using sendToGroups.*/
 		public void onLogin(Player triggerMessage){
 			triggerMessagesWithOption(triggerMessage, "onlogin");
 			return;
 		}
 		
 		public void onDisconnect(Player triggerMessage){
 			triggerMessagesWithOption(triggerMessage, "ondisconnect");
 			return;
 		}
 		
 		public void onBan(Player mod, Player triggerMessage, java.lang.String reason) {
 			String [] replaceThese = {"<<ban-reason>>", "<<ban-setter>>", "<<ban-recipient>>"     };
 			String [] withThese =    {reason          , mod.getName()   , triggerMessage.getName()};
 			triggerMessagesWithOption(triggerMessage, "onban", replaceThese, withThese);
 		}
 		
 		public boolean onCommand(Player player, String[] split){
 			if (!player.canUseCommand(split[0]))
 	            return false;
 	        
 	        if (split[0].equalsIgnoreCase("/grouptell")){
 	        	Group iShouldExist;
 	        	if ((iShouldExist = etc.getDataSource().getGroup(split[1])) != null) {
 		        	String tag =  "<" + player.getColor() + player.getName() + Colors.White + " to " + iShouldExist.Prefix.charAt(0) + iShouldExist.Name + Colors.White + "> ";
 		        	String message = tag + etc.combineSplit(2, split, " ");
 		        	String [] functionParam = {split[1], player.getName()};
 		        	sendToGroups(functionParam, message,player);
 	        	} else {
 	        		player.sendMessage(Colors.Red + "Invalid group name!");
 	        	}
 	        	return true;
 	        }    
 	        else if (split[0].equalsIgnoreCase("/rmotd")) {
 				triggerMessagesWithOption(player, "onrmotd");
 				return true;
 			}
 			
 			return false; 
 		}
 		
 		public boolean onConsoleCommand(String[] split) {
 			if (split[0].equalsIgnoreCase("grouptell")) {
 				Group iShouldExist;
 	        	if ((iShouldExist = etc.getDataSource().getGroup(split[1])) != null) {
 		        	String tag =  "<dServer " + Colors.White + "to " + iShouldExist.Prefix.charAt(0) + iShouldExist.Name + Colors.White + "> ";
 		        	String message = tag + etc.combineSplit(2, split, " ");
 		        	sendToGroup(split[1], message);
 		        	log.info("[rMotd to " + iShouldExist.Name + "] " + etc.combineSplit(2, split, " "));
 	        	} else {
 	        		log.info("[rMotD] Invalid group name!");
 	        	}
 	        	return true;
 			}
 			return false;
 		}
 	}
 }
