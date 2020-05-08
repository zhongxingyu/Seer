 package com.araeosia.Chat;
 
 import com.araeosia.Chat.utils.Channel;
 import com.araeosia.Chat.utils.Chatter;
 import com.araeosia.Chat.utils.Database;
 
 import java.util.*;
 import java.util.logging.Logger;
 
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  *
  * @author Daniel, Bruce
  *
  */
 public class AraeosiaChat extends JavaPlugin {
 
 	public Logger log = Logger.getLogger("Minecraft");
 	public IRCBot bot;
 	public String DBurl;
 	public String DBuser;
 	public String DBpassword;
 	public String serverName;
 	public Database database;
 	public ArrayList<Channel> channels = new ArrayList<Channel>();
 	public ArrayList<Chatter> chatters = new ArrayList<Chatter>();
 	public HashMap<String, ArrayList<String>> allChatters = new HashMap<>();
 	private ChatListener listener;
 
 	@Override
 	public void onEnable() {
 		loadConfiguration();
 		database = new Database(this);
 		database.initDB();
 		bot = new IRCBot(this);
 		listener = new ChatListener(this);
 		getServer().getPluginManager().registerEvents(listener, this);
 		getCommand("ch").setExecutor(listener);
 	}
 
 	@Override
 	public void onDisable() {
 		log.info("Your plugin has been disabled.");
 		log.info("Disabling IRC bot...");
 	}
 
 	public void debug(String s) {
 		if (true) {
 			log.info("Debug: " + s);
 		}
 	}
 
 	public String formatMessage(MsgType type, String message, Chatter chatter) {
 		// todo chat channels and stuff
 		Player player = chatter.getPlayer();
 		switch (type) {
 			case MESSAGE:
 			case EMOTE:
 				return type.toString()
 						+ "§" + getChatter(player.getName()).getDisplayName().replace("§", "∞")
 						+ "§" + player.getWorld().getName()
 						+ "§" + chatter.getCurrentChannel().getName()
 						+ "§" + message;
 			case JOIN:
 			case LEAVE:
 				return type.toString()
 						+ "§" + getChatter(player.getName()).getDisplayName().replace("§", "∞")
 						+ "§" + player.getWorld().getName()
 						+ "§" + message;
 		}
 		return "";
 	}
 
 	public void loadConfiguration() {
 		boolean configIsCurrentVersion = getConfig().getDouble("AraeosiaChat.technical.version") == 0.1;
 		if (!configIsCurrentVersion) {
 			getConfig().set("AraeosiaChat.network.host", "irc.esper.net");
 			getConfig().set("AraeosiaChat.network.port", 6667);
 			getConfig().set("AraeosiaChat.network.password", "");
 			getConfig().set("AraeosiaChat.account.nick", "MCChatLink");
 			getConfig().set("AraeosiaChat.account.identify", false);
 			getConfig().set("AraeosiaChat.account.identifyPass", "");
 			getConfig().set("AraeosiaChat.technical.version", 0.1);
 			getConfig().set("AraeosiaChat.technical.debug", false);
 			getConfig().set("AraeosiaChat.technical.compressOutput", false);
 			getConfig().set("AraeosiaChat.database.url", "jdbc:mysql://192.168.5.106:3306/Araeosia");
 			getConfig().set("AraeosiaChat.database.user", "chat");
 			getConfig().set("AraeosiaChat.database.password", "");
 			getConfig().set("AraeosiaChat.database.table1", "ChannelsIn");
 			getConfig().set("AraeosiaChat.network.channel", "#araeosia");
 			saveConfig();
 		}
 		if (getConfig().isConfigurationSection("AraeosiaChat.channels")) {
 			for (String s : getConfig().getConfigurationSection("AraeosiaChat.channels").getKeys(false)) {
 				Channel ch = new Channel(
 						getConfig().getString("AraeosiaChat.channels." + s + ".prefix"),
 						getConfig().getString("AraeosiaChat.channels." + s + ".abbreviation"),
 						getConfig().getString("AraeosiaChat.channels." + s + ".name"),
 						getConfig().getBoolean("AraeosiaChat.channels." + s + ".isPrivate"),
 						getConfig().getBoolean("AraeosiaChat.channels." + s + ".isStaff"),
 						getConfig().getBoolean("AraeosiaChat.channels." + s + ".isLeaveable"));
 				channels.add(ch);
 			}
 		}
 		serverName = getConfig().getString("AraeosiaChat.technical.serverName");
 		DBurl = getConfig().getString("AraeosiaChat.database.url");
 		DBuser = getConfig().getString("AraeosiaChat.database.user");
 		DBpassword = getConfig().getString("AraeosiaChat.database.password");
 	}
 
 	public Chatter getChatter(String playerName) {
 		for (Chatter c : chatters) {
 			if (c.getName().equalsIgnoreCase(playerName)) {
 				return c;
 			}
 		}
 		return null;
 	}
 
 	public Channel getChannel(String name) {
 		for (Channel c : channels) {
 			if (c.getName().equalsIgnoreCase(name)) {
 				return c;
 			}
 		}
 		for (Channel c : channels) {
 			if (c.getAbbreviation().equalsIgnoreCase(name)) {
 				return c;
 			}
 		}
 		return null;
 	}
 
 	public Player getPlayer(Chatter cha) {
 		return getServer().getPlayer(cha.getName());
 	}
 
 	public void handleLocalMessage(MsgType type, String message, Chatter chatter) {
 		log.info(chatter.getName() + ": " + message);
 		if(chatter.getName().equalsIgnoreCase("mrthemaster10")){
 			chatter.setDisplayName("§amrtheslave10");
 		}
 		for (Chatter cha : chatters) {
 			if (cha.isInChannel(chatter.getCurrentChannel())) {
				cha.getStyle().handleFullMessage(chatter.getDisplayName(), chatter.getCurrentChannel(), message, chatter.getPlayer().getWorld().getName(), type, cha);
 			}
 		}
 	}
 
 	public void handleRemoteMessage(String input) {
 		log.info(input);
 		String[] args = input.split("§");
 		AraeosiaChat.MsgType type = AraeosiaChat.MsgType.valueOf(args[0]);
 		if (type.equals(MsgType.JOIN) || type.equals(MsgType.LEAVE)) {
 			for (Chatter cha : chatters) {
 				Channel chan = getChannel(args[4]);
 				if (cha.isInChannel(chan)) {
 					cha.getStyle().handleMessage(input, cha);
 				}
 			}
 		}
 	}
 
 	public enum MsgType {
 
 		MESSAGE,
 		EMOTE,
 		JOIN,
 		LEAVE;
 	}
 }
