 package com.minecraftdimensions.bungeesuitechat;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import net.milkbowl.vault.chat.Chat;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.massivecraft.factions.Factions;
 import com.minecraftdimensions.bungeesuitechat.commands.AdminCommand;
 import com.minecraftdimensions.bungeesuitechat.commands.AfkCommand;
 import com.minecraftdimensions.bungeesuitechat.commands.ChatspyCommand;
 import com.minecraftdimensions.bungeesuitechat.commands.CleanChatCommand;
 import com.minecraftdimensions.bungeesuitechat.commands.FactionChatAllyCommand;
 import com.minecraftdimensions.bungeesuitechat.commands.FactionChatCommand;
 import com.minecraftdimensions.bungeesuitechat.commands.FactionChatFactionCommand;
 import com.minecraftdimensions.bungeesuitechat.commands.FactionChatPublicCommand;
 import com.minecraftdimensions.bungeesuitechat.commands.GlobalCommand;
 import com.minecraftdimensions.bungeesuitechat.commands.IgnoreCommand;
 import com.minecraftdimensions.bungeesuitechat.commands.IgnoresCommand;
 import com.minecraftdimensions.bungeesuitechat.commands.LocalCommand;
 import com.minecraftdimensions.bungeesuitechat.commands.MessageCommand;
 import com.minecraftdimensions.bungeesuitechat.commands.MuteAllCommand;
 import com.minecraftdimensions.bungeesuitechat.commands.MuteCommand;
 import com.minecraftdimensions.bungeesuitechat.commands.NicknameCommand;
 import com.minecraftdimensions.bungeesuitechat.commands.ReloadChatCommand;
 import com.minecraftdimensions.bungeesuitechat.commands.ReplyCommand;
 import com.minecraftdimensions.bungeesuitechat.commands.ServerCommand;
 import com.minecraftdimensions.bungeesuitechat.commands.TempMuteCommand;
 import com.minecraftdimensions.bungeesuitechat.commands.ToggleCommand;
 import com.minecraftdimensions.bungeesuitechat.commands.UnMuteAllCommand;
 import com.minecraftdimensions.bungeesuitechat.commands.UnMuteCommand;
 import com.minecraftdimensions.bungeesuitechat.commands.UnignoreCommand;
 import com.minecraftdimensions.bungeesuitechat.commands.WhoisCommand;
 
 
 
 public class BungeeSuiteChat extends JavaPlugin {
 
 
 	public Utilities utils;
 
 	static String OUTGOING_PLUGIN_CHANNEL = "BungeeSuite";
 	static String INCOMING_PLUGIN_CHANNEL = "BungeeSuiteChat";
 	public double LOCAL_DISTANCE = 50;
 
 	public HashMap<Player, String> playersChannel = new HashMap<Player, String>();
 	public HashMap<Player, ArrayList<String>> playersIgnores = new HashMap<Player, ArrayList<String>>();
 	public HashMap<String, String> channelFormats = new HashMap<String, String>();
 	public ArrayList<String> mutedPlayers = new ArrayList<String>();
 	public ArrayList<Player>  cleanChat= new ArrayList<Player>();
 	
 	public Chat CHAT = null;
 	public boolean usingVault;
 	
 	public boolean createChatConfig;
 	public boolean getChannelFormats;
 	public boolean columnsCreated;
 	public boolean gotPrefixSuffix;
 
 	public boolean firstLogin = true;
 
 	public boolean muteAll;
 
 	public boolean tablesCreated;
 
 	public boolean localRadiusRetrieved;
 	
 	public String shortForm;
 	
 	boolean titles = false;
 
 	public boolean forcedChan = false;
 
 	public String forcedChannel;
 
 	public String adminColor;
 
 	public String cleanChatRegex;
 	
 	public boolean factionChat = false;
 	
 	HashMap<String,String> prefixes = new HashMap<String,String>();
 
 	public String serverName;
 
 	HashMap<String,String> suffixes = new HashMap<String,String>();
 	
	ArrayList<Player> afkPlayers = new ArrayList<Player>();
 
 	
 	@Override
 	public void onEnable() {
 		utils = new Utilities(this);
 		registerListeners();
 		registerChannels();
 		registerCommands();
 		usingVault = setupVault();
 		setupFactions();
 		if(Bukkit.getOnlinePlayers().length>0){
 			
 				if (!createChatConfig) {
 					utils.createChatConfig();
 				}
 				if (!columnsCreated) {
 					utils.addChatColumns();
 				}
 				if (!tablesCreated) {
 					utils.createBaseTables();
 				}
 				if (!usingVault && !gotPrefixSuffix) {
 					utils.getDefaultPrefixAndSuffix();
 				}
 				new FirstLoginTask(Bukkit.getOnlinePlayers()[0], this)
 						.runTaskLaterAsynchronously(this, 10);
 				firstLogin = false;
 			for(Player data: Bukkit.getOnlinePlayers()){
 				utils.getPlayersInfo(data.getName());
 			}
 		}
 	}
 
 	private void setupFactions() {
 		Factions factions = (Factions) Bukkit.getPluginManager().getPlugin("Factions");
 		if(factions!=null){
 			if(factions.getDescription().getVersion().startsWith("2")){
 			factionChat = true;
 			getCommand("factionchat").setExecutor(new FactionChatCommand(this));
 			getCommand("factionchatally").setExecutor(new FactionChatAllyCommand(this));
 			getCommand("factionchatfaction").setExecutor(new FactionChatFactionCommand(this));
 			getCommand("factionchatpublic").setExecutor(new FactionChatPublicCommand(this));
 			}
 		}
 	}
 
 	private void registerCommands() {
 		getCommand("message").setExecutor(new MessageCommand(this));
 		getCommand("reply").setExecutor(new ReplyCommand(this));
 		getCommand("mute").setExecutor(new MuteCommand(this));
 		getCommand("tempmute").setExecutor(new TempMuteCommand(this));
 		getCommand("muteall").setExecutor(new MuteAllCommand(this));
 		getCommand("unmute").setExecutor(new UnMuteCommand(this));
 		getCommand("unmuteall").setExecutor(new UnMuteAllCommand(this));
 		getCommand("local").setExecutor(new LocalCommand(this));
 		getCommand("server").setExecutor(new ServerCommand(this));
 		getCommand("global").setExecutor(new GlobalCommand(this));
 		getCommand("ignore").setExecutor(new IgnoreCommand(this));
 		getCommand("ignores").setExecutor(new IgnoresCommand(this));
 		getCommand("unignore").setExecutor(new UnignoreCommand(this));
 		getCommand("toggle").setExecutor(new ToggleCommand(this));
 		getCommand("nickname").setExecutor(new NicknameCommand(this));
 		getCommand("chatspy").setExecutor(new ChatspyCommand(this));
 		getCommand("cleanchat").setExecutor(new CleanChatCommand(this));
 		getCommand("reloadchat").setExecutor(new ReloadChatCommand(this));
 		getCommand("whois").setExecutor(new WhoisCommand(this));
 		getCommand("admin").setExecutor(new AdminCommand(this));
 		getCommand("afk").setExecutor(new AfkCommand(this));
 		
 	}
 
 	private void registerChannels() {
 		Bukkit.getMessenger().registerIncomingPluginChannel(this,
 				INCOMING_PLUGIN_CHANNEL, new ChatListener(this));
 		Bukkit.getMessenger().registerOutgoingPluginChannel(this,
 				OUTGOING_PLUGIN_CHANNEL);
 	}
 
 	private void registerListeners() {
 		getServer().getPluginManager().registerEvents(
 				new ChatListener(this), this);
 		getServer().getPluginManager().registerEvents(
 				new AFKListener(this), this);
 	}
 
 	private boolean setupVault()
     {
 		if(!packageExists("net.milkbowl.vault.chat.Chat"))return false;
         RegisteredServiceProvider<Chat> chatProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.chat.Chat.class);
         if (chatProvider != null) {
             CHAT = chatProvider.getProvider();
         } else {
         	this.getLogger().info("No Vault found");
         }
         return (CHAT != null);
     }
 	  private boolean packageExists(String...packages) {
 	        try {
 	            for (String pkg : packages) {
 	                Class.forName(pkg);
 	            }
 	            
 	            return true;
 	        } catch (Exception e) {
 	        	this.getLogger().info("No Vault found using default permission prefixes");
 	            return false;
 	        }
 	    }
 
 	public void registerAdminChannel() {
 		getCommand("admin").setExecutor(new AdminCommand(this));
 		
 	}
 }
