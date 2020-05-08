 package com.gmail.zant95;
 
 import java.io.File;
 import java.io.IOException;
 
 import net.milkbowl.vault.chat.Chat;
 import net.milkbowl.vault.permission.Permission;
 
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import org.mcstats.Metrics;
 
 import com.gmail.zant95.Listeners.ChatListener;
 import com.gmail.zant95.Listeners.CommandListener;
 import com.gmail.zant95.Listeners.JoinListener;
 
 public class LiveChat extends JavaPlugin {
 	public final ChatListener ChatListener = new ChatListener(this);
 	public final JoinListener JoinListener = new JoinListener(this);
 	public final CommandListener CommandListener = new CommandListener(this);
 
 	protected static Format Format;
 	protected static Locale Locale;
 
 	public static Permission perms = null;
 	public static Chat chat = null;
 
 	@Override
 	public void onEnable() {
 		//Setup Vault
 		if (getServer().getPluginManager().getPlugin("Vault") == null) {
 			this.getLogger().info("Vault dependency not found!");
 			getServer().getPluginManager().disablePlugin(this);
 			return;
 		}
 
 		setupPermissions();
 		setupChat();
 		
 		//Setup locale
 		Locale = new Locale(this);
 		Locale.load();
 
 		//Setup plugin folder
 		File pluginDir = getDataFolder();
 		if(!pluginDir.exists()) {
 			pluginDir.mkdirs();
 			this.getLogger().info("Creating plugin directory...");
 		}
 
 		//Setup log folder
 		File logDir = new File(getDataFolder(), "logs");
 		if(!logDir.exists()) {
 			logDir.mkdirs();
 			this.getLogger().info("Creating log directory...");
 		}
 
 		//Setup config
 		File config = new File(getDataFolder(), "config.yml");
 		if(!config.exists()) {
 			config.getParentFile().mkdirs();
 			Utils.copy(this.getResource("config.yml"), config);
 		}
 
 		//Implement listeners
 		PluginManager pm = getServer().getPluginManager();
 		pm.registerEvents(ChatListener, this);
 		pm.registerEvents(JoinListener, this);
 		pm.registerEvents(CommandListener, this);
 
 		//Implement Plugin Metrics
 		try {
 			Metrics metrics = new Metrics(this); metrics.start();
 		} catch (IOException e) { //Failed to submit the stats :-(
 			System.out.println("Error submitting stats!");
 		}
 		
 		//Implement commands
		getCommand("tell").setExecutor(new CommandHandler(this));
 		getCommand("msg").setExecutor(new CommandHandler(this));
		getCommand("pm").setExecutor(new CommandHandler(this));
 		getCommand("r").setExecutor(new CommandHandler(this));
 		getCommand("me").setExecutor(new CommandHandler(this));
 		getCommand("local").setExecutor(new CommandHandler(this));
 		getCommand("mute").setExecutor(new CommandHandler(this));
 		getCommand("block").setExecutor(new CommandHandler(this));
 		getCommand("ignore").setExecutor(new CommandHandler(this));
 		getCommand("admin").setExecutor(new CommandHandler(this));
 		getCommand("livechat").setExecutor(new CommandHandler(this));
 
 		this.getLogger().info("Hey there! I am using LiveChat");
 	}
 
 	@Override
 	public void onDisable() {
 		this.getLogger().info("Goodbye LiveChat!");
 	}
 
 	private boolean setupChat() {
 		RegisteredServiceProvider<Chat> rsp = getServer().getServicesManager().getRegistration(Chat.class);
 		chat = rsp.getProvider();
 		return chat != null;
 	}
 
 	private boolean setupPermissions() {
 		RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
 		perms = rsp.getProvider();
 		return perms != null;
 	}
 }
