 package com.minecraftdimensions.bungeesuitechat;
 
 
 import net.milkbowl.vault.chat.Chat;
 
 import org.bukkit.Bukkit;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.massivecraft.factions.Factions;
 import com.minecraftdimensions.bungeesuitechat.commands.AfkCommand;
 import com.minecraftdimensions.bungeesuitechat.commands.ChatspyCommand;
 import com.minecraftdimensions.bungeesuitechat.commands.IgnoreCommand;
 import com.minecraftdimensions.bungeesuitechat.commands.IgnoresCommand;
 import com.minecraftdimensions.bungeesuitechat.commands.MeCommand;
 import com.minecraftdimensions.bungeesuitechat.commands.MessageCommand;
 import com.minecraftdimensions.bungeesuitechat.commands.MuteAllCommand;
 import com.minecraftdimensions.bungeesuitechat.commands.MuteCommand;
 import com.minecraftdimensions.bungeesuitechat.commands.NicknameCommand;
 import com.minecraftdimensions.bungeesuitechat.commands.NicknameOffCommand;
 import com.minecraftdimensions.bungeesuitechat.commands.ReloadChatCommand;
 import com.minecraftdimensions.bungeesuitechat.commands.ReplyCommand;
 import com.minecraftdimensions.bungeesuitechat.commands.TempMuteCommand;
 import com.minecraftdimensions.bungeesuitechat.commands.UnMuteAllCommand;
 import com.minecraftdimensions.bungeesuitechat.commands.UnMuteCommand;
 import com.minecraftdimensions.bungeesuitechat.commands.UnignoreCommand;
 import com.minecraftdimensions.bungeesuitechat.commands.channel.AdminCommand;
 import com.minecraftdimensions.bungeesuitechat.commands.channel.GlobalCommand;
 import com.minecraftdimensions.bungeesuitechat.commands.channel.LocalCommand;
 import com.minecraftdimensions.bungeesuitechat.commands.channel.ServerCommand;
 import com.minecraftdimensions.bungeesuitechat.commands.channel.ToggleCommand;
 import com.minecraftdimensions.bungeesuitechat.commands.factions.FactionChatAllyCommand;
 import com.minecraftdimensions.bungeesuitechat.commands.factions.FactionChatCommand;
 import com.minecraftdimensions.bungeesuitechat.commands.factions.FactionChatFactionCommand;
 import com.minecraftdimensions.bungeesuitechat.listeners.AFKListener;
 import com.minecraftdimensions.bungeesuitechat.listeners.ChatListener;
 import com.minecraftdimensions.bungeesuitechat.listeners.LoginListener;
 import com.minecraftdimensions.bungeesuitechat.listeners.LogoutListener;
 import com.minecraftdimensions.bungeesuitechat.listeners.MessageListener;
 import com.minecraftdimensions.bungeesuitechat.managers.ChannelManager;
 
 
 
 public class BungeeSuiteChat extends JavaPlugin {
 
 	public static String OUTGOING_PLUGIN_CHANNEL = "BSChat";
 	public static String INCOMING_PLUGIN_CHANNEL = "BungeeSuiteChat";
 	public static BungeeSuiteChat instance;
 	public static Chat CHAT = null;
 	public static boolean usingVault;
 	public static boolean factionChat = false;
 	public static String serverName;
 	
 	@Override
 	public void onEnable() {
 		instance = this;
 		registerCommands();
 		usingVault = setupVault();
 		registerListeners();
 		startTasks();
 	}
 
 	private void startTasks() {
 		
 	this.getServer().getScheduler().runTaskTimerAsynchronously(this, new Runnable(){
 		@Override
 		public void run() {
 			ChannelManager.cleanChannels();
 		}
 	}, 36000, 36000);
 	
 	}
 
 	private void registerListeners() {
 		this.getServer().getMessenger().registerOutgoingPluginChannel(this, OUTGOING_PLUGIN_CHANNEL);
 		Bukkit.getPluginManager().registerEvents(new ChatListener(), this);
 		Bukkit.getPluginManager().registerEvents(new LoginListener(), this);
 		Bukkit.getPluginManager().registerEvents(new LogoutListener(), this);
 		Bukkit.getPluginManager().registerEvents(new AFKListener(), this);
 		Bukkit.getMessenger().registerIncomingPluginChannel(this,
 				INCOMING_PLUGIN_CHANNEL, new MessageListener());
 	
 }
 
 	public void setupFactions() {
 		Factions factions = (Factions) Bukkit.getPluginManager().getPlugin("Factions");
 		if(factions!=null){
 			if(!factions.getDescription().getVersion().startsWith("1")){
 			factionChat = true;
 			getCommand("factionchat").setExecutor(new FactionChatCommand());
 			getCommand("factionchatally").setExecutor(new FactionChatAllyCommand());
 			getCommand("factionchatfaction").setExecutor(new FactionChatFactionCommand());
 			}
 		}
 	}
 
 	private void registerCommands() {
 		getCommand("admin").setExecutor(new AdminCommand());
 		getCommand("afk").setExecutor(new AfkCommand());
 		getCommand("chatspy").setExecutor(new ChatspyCommand());
		getCommand("channelinfo").setExecutor(new ChannelInfoCommand());
 		getCommand("global").setExecutor(new GlobalCommand());
 		getCommand("ignore").setExecutor(new IgnoreCommand());
 		getCommand("ignores").setExecutor(new IgnoresCommand());
 		getCommand("local").setExecutor(new LocalCommand());
 		getCommand("me").setExecutor(new MeCommand());
 		getCommand("message").setExecutor(new MessageCommand());
 		getCommand("mute").setExecutor(new MuteCommand());
 		getCommand("muteall").setExecutor(new MuteAllCommand());
 		getCommand("nickname").setExecutor(new NicknameCommand());
 		getCommand("nicknameoff").setExecutor(new NicknameOffCommand());
 		getCommand("reloadchat").setExecutor(new ReloadChatCommand());
 		getCommand("reply").setExecutor(new ReplyCommand());
 		getCommand("server").setExecutor(new ServerCommand());
 		getCommand("tempmute").setExecutor(new TempMuteCommand());
 		getCommand("toggle").setExecutor(new ToggleCommand());
 		getCommand("unignore").setExecutor(new UnignoreCommand());
 		getCommand("unmute").setExecutor(new UnMuteCommand());
 		getCommand("unmuteall").setExecutor(new UnMuteAllCommand());
 		
 //		getCommand("createchannel").setExecutor(new WhoisCommand(this));
 //		getCommand("deletechannel").setExecutor(new WhoisCommand(this));
 //		getCommand("leavechannel").setExecutor(new WhoisCommand(this));
 //		getCommand("joinchannel").setExecutor(new WhoisCommand(this));
 //		getCommand("kickchannel").setExecutor(new WhoisCommand(this));
 //		getCommand("formatchannel").setExecutor(new WhoisCommand(this));
 //		getCommand("channelinfo").setExecutor(new WhoisCommand(this));
 //		getCommand("setchannelowner").setExecutor(new WhoisCommand(this));
 		
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
 	        	this.getLogger().info("No Vault found");
 	            return false;
 	        }
 	    }
 }
