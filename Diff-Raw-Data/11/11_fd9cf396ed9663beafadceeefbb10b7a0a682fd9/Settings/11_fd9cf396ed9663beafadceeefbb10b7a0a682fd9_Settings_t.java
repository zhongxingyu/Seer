 package com.gmail.andreasmartinmoerch.danandchat.utils;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.World;
 import org.bukkit.util.config.Configuration;
 import org.bukkit.util.config.ConfigurationNode;
 
 import com.gmail.andreasmartinmoerch.danandchat.DanAndChat;
 import com.gmail.andreasmartinmoerch.danandchat.channel.Channel;
 import com.gmail.andreasmartinmoerch.danandchat.commands.Commands;
 public class Settings {
 	public static final String mainDirectory = "plugins" + File.separator + "DanAndChat" + File.separator;
 	public static final String settingsDirectory = "Settings"  + File.separator;
 	
 	public Configuration config;
 		public final String configDir = "plugins" + File.separator + "DanAndChat";
 		public final String configFile = "config.yml";
 	public Configuration channelsConfig;
 		public final String channelsDir = "plugins" + File.separator + "DanAndChat";
 		public final String channelsFile = "channels.yml";
 	public Configuration prefixConfig;
 		public final String prefixConfigDir = configDir;
 		public final String prefixFile = "prefixes.yml";
 	public Configuration messageConfig;
 		public final String messageConfigDir = configDir;
 		public final String messageFile = "messages.yml";
 			
 	private DanAndChat plugin;
 	
 	public Settings(DanAndChat plugin){
 		this.plugin = plugin;
 	}
 		
 	public void initialize(){
 		if (!this.plugin.getDataFolder().exists()){
 			this.plugin.getDataFolder().mkdirs();
 		}
 		File fConfig = new File(configDir, configFile);		
 		File fChannelsConfig = new File(channelsDir, channelsFile);
 		File fPrefixConfig = new File(prefixConfigDir, prefixFile);
 		File fMessageConfig = new File(messageConfigDir, messageFile);
 		
 		try {
 			if (!fConfig.exists()){
 				fConfig.createNewFile();
 			}
 			
 			if (!fChannelsConfig.exists()){
 				fChannelsConfig.createNewFile();
 			}
 			
 			if (!fPrefixConfig.exists()){
 				fPrefixConfig.createNewFile();
 			}
 			if(!fMessageConfig.exists()){
 				fMessageConfig.createNewFile();
 			}
 		} catch (IOException e){
 			e.printStackTrace();
 		}
 		
 		config = new Configuration(fConfig);
 		config.load();
 		this.plugin.getCommandManager().initialize();
 		
 		channelsConfig = new Configuration(fChannelsConfig);
 		channelsConfig.load();
 		
 		prefixConfig = new Configuration(fPrefixConfig);
 		prefixConfig.load();
 		
 		//TODO You know what to do.
 		messageConfig = new Configuration(fMessageConfig);
 		messageConfig.load();
 		
 		if (!config.getBoolean("plugin" +"."+ "initialized", false)){
 			initializeConf();
 		}
 	}
 	
 	public MessageGetter getNewMessageGetter(){
 		String prefix = null;
 		MessageGetter msgGetter;
 		if ((prefix = this.config.getString("plugin.message-set", null)) == null){
 			msgGetter = new MessageGetter(this.plugin, this.messageConfig);
 		} else { msgGetter = new MessageGetter(this.plugin, this.messageConfig, prefix); }
		MessageGetter.writeDefaultMessagesToConfig(this.messageConfig, false);
		MessageGetter.debug = this.config.getBoolean("plugin.debug", false);
 		return msgGetter;
 	}
 	
 	public void initializeConf(){
 		config.setProperty("commands" +"."+ Commands.CH.toString().toLowerCase() +"."+ "enabled" , true);
 		config.setProperty("commands" +"."+ Commands.CHANNEL.toString().toLowerCase() +"."+ "enabled" , true);
 		config.setProperty("commands" +"."+ Commands.LEAVECHANNEL.toString().toLowerCase() +"."+ "enabled" , true);
 		config.setProperty("commands" +"."+ Commands.ME.toString().toLowerCase() +"."+ "enabled" , true);
 		config.setProperty("commands" +"."+ Commands.T.toString().toLowerCase() +"."+ "enabled" , true);
 		
 		List<String> worlds = new ArrayList<String>();
 		for (World w: this.plugin.getServer().getWorlds()){
 			worlds.add(w.getName());
 		}
 		this.channelsConfig.setProperty("channels.Global.worlds", worlds);
 		this.channelsConfig.setProperty("channels.Global.short-name", "g");
 		this.channelsConfig.setProperty("channels.Global.shortcut", "g");
 		this.channelsConfig.setProperty("channels.Global.range", -1);
 		this.channelsConfig.setProperty("channels.Global.auto-join", true);
 		this.channelsConfig.setProperty("channels.Global.auto-focus", true);
 		this.channelsConfig.setProperty("channels.Global.formatting", "&COLOUR.YELLOW[&COLOUR.GREENg&COLOUR.YELLOW]&COLOUR.WHITE<&PLAYER.PREFIX&PLAYER.DISPLAYNAME&COLOUR.WHITE>: &MESSAGE");
 		
 		this.channelsConfig.setProperty("channels.local.worlds", worlds);
 		this.channelsConfig.setProperty("channels.local.short-name", "l");
 		this.channelsConfig.setProperty("channels.local.shortcut", "l");
 		this.channelsConfig.setProperty("channels.local.range", 100);
 		this.channelsConfig.setProperty("channels.local.auto-join", true);
 		this.channelsConfig.setProperty("channels.local.auto-focus", false);
 		this.channelsConfig.setProperty("channels.local.formatting", "&PLAYER.DISPLAYNAME: &MESSAGE");
 		this.channelsConfig.save();
 		this.channelsConfig.load();
 		this.config.setProperty("plugin" + "." + "initialized", true);
 		config.save();
 		config.load();
 	}
 	
 	public List<Channel> getChannels(){
 		List<Channel> channels = new ArrayList<Channel>();
 		ConfigurationNode glNode = this.channelsConfig.getNode("channels");
 		
 		for (String s: glNode.getKeys()){
 			Channel c = new Channel(s, this.plugin);
 			c.loadFromConfig();
 			channels.add(c);
 		}
 		
 //		for (String s: channelsConfig.getKeys("local-channels")){
 //			LocalChannel l = new LocalChannel(s);
 //			l.loadFromConfig();
 //			channels.add(l);
 //		}
 		
 		refresh(channelsConfig);
 		return channels;
 	}
 	
 	public void refresh(Configuration c){
 		c.save();
 		c.load();
 	}
 }
