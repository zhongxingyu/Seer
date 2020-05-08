 package me.arno.blocklog;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.logging.Logger;
 
 import me.arno.blocklog.commands.CommandAutoSave;
 import me.arno.blocklog.commands.CommandClear;
 import me.arno.blocklog.commands.CommandConfig;
 import me.arno.blocklog.commands.CommandHelp;
 import me.arno.blocklog.commands.CommandRadiusRollback;
 import me.arno.blocklog.commands.CommandReload;
 import me.arno.blocklog.commands.CommandRollback;
 import me.arno.blocklog.commands.CommandSave;
 import me.arno.blocklog.commands.CommandUndo;
 import me.arno.blocklog.commands.CommandWand;
 import me.arno.blocklog.database.DatabaseSettings;
 import me.arno.blocklog.database.PushBlocks;
 import me.arno.blocklog.listeners.LogListener;
 import me.arno.blocklog.listeners.LoginListener;
 import me.arno.blocklog.listeners.WandListener;
 import me.arno.blocklog.log.LoggedBlock;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class BlockLog extends JavaPlugin {
 	public Logger log;
 	public DatabaseSettings dbSettings;
 	public Connection conn;
 	
 	public ArrayList<String> users = new ArrayList<String>();
 	public ArrayList<LoggedBlock> blocks = new ArrayList<LoggedBlock>();
 	
 	public String NewVersion = null;
 	
 	public int autoSave = 0;
 	public boolean autoSaveMsg = false;
 	
 	public String getResourceContent(String file) {
 		try {
 			InputStream ResourceFile = getResource("resources/" + file);
 			 
 			final char[] buffer = new char[0x10000];
 			StringBuilder StrBuilder = new StringBuilder();
 			Reader InputReader = new InputStreamReader(ResourceFile, "UTF-8");
 			int read;
 			do {
 				read = InputReader.read(buffer, 0, buffer.length);
 				if (read > 0)
 					StrBuilder.append(buffer, 0, read);
 				
 			} while (read >= 0);
 			InputReader.close();
 			ResourceFile.close();
 			return StrBuilder.toString();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	public void loadConfiguration() {
 	    getConfig().addDefault("database.type", "SQLite");
 	    getConfig().addDefault("database.delay", 1);
 		getConfig().addDefault("mysql.host", "localhost");
 	    getConfig().addDefault("mysql.username", "root");
 	    getConfig().addDefault("mysql.password", "");
 	    getConfig().addDefault("mysql.database", "");
 	    getConfig().addDefault("mysql.port", 3306);
 	   	getConfig().addDefault("blocklog.wand", 369);
 	    getConfig().addDefault("blocklog.results", 5);
 	    getConfig().addDefault("blocklog.warning.blocks", 500);
 	    getConfig().addDefault("blocklog.warning.repeat", 100);
 	    getConfig().addDefault("blocklog.warning.delay", 30);
 		getConfig().options().copyDefaults(true);
 		saveConfig();
 	}
 	
 	public void loadDatabase() {
 		String DBType = getConfig().getString("database.type");
 		Statement stmt;
 		try {
 			if(DBType.equalsIgnoreCase("mysql")) {
 		    	conn = DatabaseSettings.getConnection(this);
 		    	stmt = conn.createStatement();
 				
 				stmt.executeUpdate(getResourceContent("MySQL/blocklog_blocks.sql"));
 				stmt.executeUpdate(getResourceContent("MySQL/blocklog_rollbacks.sql"));
 				stmt.executeUpdate(getResourceContent("MySQL/blocklog_interactions.sql"));
 			} else if(DBType.equalsIgnoreCase("sqlite")) {
 			    conn = DatabaseSettings.getConnection(this);
 			    stmt = conn.createStatement();
 				
 				stmt.executeUpdate(getResourceContent("SQLite/blocklog_blocks.sql"));
 				stmt.executeUpdate(getResourceContent("SQLite/blocklog_rollbacks.sql"));
 				stmt.executeUpdate(getResourceContent("SQLite/blocklog_interactions.sql"));
 		    }
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 		try { // Update database
 			if(DBType.equalsIgnoreCase("mysql")) {
 		    	conn = DatabaseSettings.getConnection(this);
 		    	stmt = conn.createStatement();
 		    	stmt.executeUpdate("ALTER TABLE `blocklog_blocks` ADD `datavalue` INT(11) NOT NULL AFTER `block_id`");
 			} else if(DBType.equalsIgnoreCase("sqlite")) {
 			    conn = DatabaseSettings.getConnection(this);
 			    stmt = conn.createStatement();
			    stmt.executeUpdate("ALTER TABLE 'blocklog_blocks' ADD 'datavalue' INTEGER NOT NULL AFTER 'block_id'");
 		    }
 			
 		} catch (SQLException e) {
 			//Prints error if table already exists
 		}
 	}
 	
 	public void getLatestVersion() {
 		try {
     		URL url = new URL("http://dl.dropbox.com/u/24494712/BlockLog/version.txt");
 		
 	        URLConnection urlConnection = url.openConnection();
 	        urlConnection.setConnectTimeout(1000);
 	        urlConnection.setReadTimeout(1000);
 	        BufferedReader breader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
 	
 	        StringBuilder stringBuilder = new StringBuilder();
 	
 	        String line;
 	        while((line = breader.readLine()) != null) {
 	                stringBuilder.append(line);
 	        }
 	
 	        int LatestVersion = Integer.parseInt(stringBuilder.toString().replace(".", ""));
 	        int ThisVersion = Integer.parseInt(getDescription().getVersion().replace(".", ""));
 	        if(LatestVersion > ThisVersion) {
 	        	log.info("There is a new version of BlockLog available (v" + stringBuilder.toString() + ")");
 	        	NewVersion = stringBuilder.toString();
 	        }
     	} catch (MalformedURLException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void loadPlugin() {
 		log = getLogger();
 		log.info("Loading the configurations");
     	loadConfiguration();
 		log.info("Loading the database");
     	loadDatabase();
 		log.info("Checking for updates");
     	getLatestVersion();
     	
     	new PushBlocks(this);
     	
     	getCommand("blhelp").setExecutor(new CommandHelp(this));
     	getCommand("blrollback").setExecutor(new CommandRollback(this));
     	getCommand("blrollbackradius").setExecutor(new CommandRadiusRollback(this));
     	getCommand("blrb").setExecutor(new CommandRollback(this));
     	getCommand("blconfig").setExecutor(new CommandConfig(this));
     	getCommand("blcfg").setExecutor(new CommandConfig(this));
     	getCommand("blwand").setExecutor(new CommandWand(this));
     	getCommand("blsave").setExecutor(new CommandSave(this));
     	getCommand("blfullsave").setExecutor(new CommandSave(this));
     	getCommand("blreload").setExecutor(new CommandReload(this));
     	getCommand("blclear").setExecutor(new CommandClear(this));
     	getCommand("blundo").setExecutor(new CommandUndo(this));
     	getCommand("blautosave").setExecutor(new CommandAutoSave(this));
     	
     	getServer().getPluginManager().registerEvents(new LogListener(this), this);
     	getServer().getPluginManager().registerEvents(new WandListener(this), this);
     	getServer().getPluginManager().registerEvents(new LoginListener(this), this);
     }
 	
 	public void saveBlocks(final int blockCount) {
 		getServer().getScheduler().scheduleAsyncDelayedTask(this, new Runnable() {
 			public void run() {
 		    	if(blocks.size() > 0) {
 		    		if(blockCount == 0) {
 		    			while(blocks.size() > 0) {
 			    			LoggedBlock block = blocks.get(0);
 					    	block.save();
 					    	blocks.remove(0);
 		    			}
 		    		} else {
 		    			for(int i=blockCount; i!=0; i--) {
 			    			LoggedBlock block = blocks.get(0);
 					    	block.save();
 					    	blocks.remove(0);
 		    			}
 		    		}
 		    	}
 		    }
 		});
 	}
 	
 	@Override
 	public void onEnable() {
 		loadPlugin();
 		PluginDescriptionFile PluginDesc = this.getDescription();
 		log.info("v" + PluginDesc.getVersion() + " is enabled!");
 	}
 	
 	@Override
 	public void onDisable() {
 		getServer().getScheduler().cancelAllTasks();
 		saveBlocks(0);
 		PluginDescriptionFile PluginDesc = this.getDescription();
 		log.info("v" + PluginDesc.getVersion() + " is disabled!");
 	}
 	
 	
 	/* Blocklog command */
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		Player player = null;
 		
 		if (sender instanceof Player)
 			player = (Player) sender;
 		
 		if(!cmd.getName().equalsIgnoreCase("blocklog"))
 			return false;
 		
 		if (player == null) {
 			sender.sendMessage("This command can only be run by a player");
 			return true;
 		}
 		
 		player.sendMessage(ChatColor.DARK_RED + "[BlockLog] " + ChatColor.GOLD + "This server is using BlockLog v" + getDescription().getVersion() + " by Anerach");
 		return true;
 	}
 }
