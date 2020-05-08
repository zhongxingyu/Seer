 package me.arno.blocklog;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.net.URL;
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.logging.Logger;
 
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import me.arno.blocklog.commands.*;
 import me.arno.blocklog.database.*;
 import me.arno.blocklog.listeners.*;
 import me.arno.blocklog.logs.LoggedBlock;
 import me.arno.blocklog.logs.LoggedInteraction;
 import me.arno.blocklog.schedules.Save;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 public class BlockLog extends JavaPlugin {
 	public Logger log;
 	public DatabaseSettings dbSettings;
 	public Connection conn;
 	
 	public ArrayList<String> users = new ArrayList<String>();
 	public ArrayList<LoggedBlock> blocks = new ArrayList<LoggedBlock>();
 	public ArrayList<LoggedInteraction> interactions = new ArrayList<LoggedInteraction>();
 	
 	public HashMap<String, Plugin> softDepends = new HashMap<String, Plugin>();
 	
 	public String newVersion;
 	public String currentVersion;
 	public double doubleNewVersion;
 	public double doubleCurrentVersion;
 	
 	public int autoSave = 0;
 	public boolean saving = false;
 	
 	public Config cfg;
 	
 	@Override
 	public FileConfiguration getConfig() {
 		return cfg.getConfig();
 	}
 	
 	@Override
 	public void saveConfig() {
 		cfg.saveConfig();
 	}
 	
 	@Override
 	public void reloadConfig() {
 		cfg.reloadConfig();
 	}
 	
 	public void addBlock(LoggedBlock block) {
 		blocks.add(block);
 	}
 	
 	public void addInteractions(LoggedInteraction interaction) {
 		interactions.add(interaction);
 	}
 	
 	public ArrayList<LoggedBlock> getBlocks() {
 		return blocks;
 	}
 	
 	public ArrayList<LoggedInteraction> getInteractionss() {
 		return interactions;
 	}
 	
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
 	
 	private void loadConfiguration() {
 		cfg = new Config();
 		cfg.createDefaults();
 		saveConfig();
 		
 		if(getConfig().getBoolean("blocklog.autosave.enabled")) {
 			autoSave = getConfig().getInt("blocklog.autosave.blocks");
 		}
 	}
 	
 	private void loadDatabase() {
 		String DBType = getConfig().getString("database.type");
 		String[] tables = {"blocks", "rollbacks", "undos", "interactions", "reports", "chat", "deaths", "kills"};
 		
 		try {
 	    	conn = DatabaseSettings.getConnection();
 	    	Statement stmt = conn.createStatement();
 	    	
 	    	for(String table : tables) {
 	    		if(DBType.equalsIgnoreCase("mysql")) {
 			    	stmt.executeUpdate(getResourceContent("MySQL/blocklog_" + table + ".sql"));
 				} else if(DBType.equalsIgnoreCase("sqlite")) {
 				    stmt.executeUpdate(getResourceContent("SQLite/blocklog_" + table + ".sql"));
 			    }
 	    	}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private void updateDatabase() {
 		try {
 			Statement stmt = conn.createStatement();
 			
 			Config versions = new Config("VERSIONS");
 			versions.getConfig().addDefault("database", 2);
 			versions.getConfig().options().copyDefaults(true);
 			if(versions.getConfig().getInt("database") > 2) {
 				log.info("Updating the database to version 2");
 				if(DatabaseSettings.DBType().equalsIgnoreCase("mysql")) 
 					stmt.executeUpdate("ALTER TABLE `blocklog_blocks` CHANGE `rollback_id` `rollback_id` INT(11) NOT NULL DEFAULT '0'");
 				
 				versions.getConfig().set("database", 2);
 			}
 			versions.saveConfig();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 	}
 	
 	private String loadLatestVersion() {
         String pluginUrlString = "http://dev.bukkit.org/server-mods/block-log/files.rss";
         try {
             URL url = new URL(pluginUrlString);
             Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(url.openConnection().getInputStream());
             doc.getDocumentElement().normalize();
             NodeList nodes = doc.getElementsByTagName("item");
             Node firstNode = nodes.item(0);
             if (firstNode.getNodeType() == 1) {
                 Element firstElement = (Element) firstNode;
                 NodeList firstElementTagName = firstElement.getElementsByTagName("title");
                 Element firstNameElement = (Element) firstElementTagName.item(0);
                 NodeList firstNodes = firstNameElement.getChildNodes();
                 return firstNodes.item(0).getNodeValue().replace("BlockLog", "").trim();
             }
         } catch (Exception e) {
         	// Nothing
         }
         return currentVersion;
     }
 	
 	private void loadDependencies() {
 		ArrayList<String> plugins = new ArrayList<String>();
     	plugins.add("GriefPrevention");
     	plugins.add("WorldGuard");
     	
     	for(String plugin : plugins) {
     		if(getServer().getPluginManager().isPluginEnabled(plugin)) {
    			softDepends.put(plugin, getServer().getPluginManager().getPlugin(plugin));
     		}
     	}
 	}
 	
 	public boolean reloadPlugin() {
 		if(saving)
 			return false;
 		
 		getServer().getScheduler().cancelTasks(this);
 		softDepends.clear();
 		
 		log.info("Reloading the configurations");
 		loadConfiguration();
 		    
 		log.info("Reloading the database");
 		loadDatabase();
 		   
 		log.info("Reloading the dependencies");
 		loadDependencies();
 		return true;
 	}
 	
 	private void loadPlugin() {
 		currentVersion = getDescription().getVersion();
 		log = getLogger();
 		
 		log.info("Loading the configurations");
 	    loadConfiguration();
 	    
 		log.info("Loading the database");
 	    loadDatabase();
 	    updateDatabase();
 	    
 	    log.info("Loading the dependencies");
 	    loadDependencies();
 	    
 	    if(getConfig().getBoolean("blocklog.updates")) {
 		    try {
 			    log.info("Checking for updates");
 				newVersion = loadLatestVersion();
 				
 				doubleCurrentVersion = Double.valueOf(currentVersion.replaceFirst("\\.", ""));
 				doubleNewVersion = Double.valueOf(newVersion.replaceFirst("\\.", ""));
 				
 				if(doubleNewVersion > doubleCurrentVersion) {
 					log.warning("BlockLog v" + newVersion + " is released! You're using BlockLog v" + currentVersion);
 					log.warning("Update BlockLog at http://dev.bukkit.org/server-mods/block-log/");
 				}
 		    } catch(Exception e) {
 				// Nothing
 			}
 	    }
 	    
 		log.info("Starting BlockLog");
     	new PushBlocks(this);
     	
     	getCommand("blhelp").setExecutor(new CommandHelp(this));
     	getCommand("blrollback").setExecutor(new CommandRollback(this));
     	getCommand("blrollbackradius").setExecutor(new CommandRadiusRollback(this));
     	getCommand("blrb").setExecutor(new CommandRollback(this));
     	getCommand("blconfig").setExecutor(new CommandConfig(this));
     	getCommand("blcfg").setExecutor(new CommandConfig(this));
     	getCommand("blwand").setExecutor(new CommandWand(this));
     	getCommand("blstorage").setExecutor(new CommandStorage(this));
     	getCommand("blsave").setExecutor(new CommandSave(this));
     	getCommand("blfullsave").setExecutor(new CommandSave(this));
     	getCommand("blreload").setExecutor(new CommandReload(this));
     	getCommand("blclear").setExecutor(new CommandClear(this));
     	getCommand("blundo").setExecutor(new CommandUndo(this));
     	getCommand("blautosave").setExecutor(new CommandAutoSave(this));
     	getCommand("blconvert").setExecutor(new CommandConvert(this));
     	getCommand("blreport").setExecutor(new CommandReport(this));
     	getCommand("blread").setExecutor(new CommandRead(this));
     	getCommand("blsearch").setExecutor(new CommandSearch(this));
     	getCommand("blrollbacklist").setExecutor(new CommandRollbackList(this));
     	
     	getServer().getPluginManager().registerEvents(new WandListener(this), this);
     	getServer().getPluginManager().registerEvents(new BlockListener(this), this);
     	getServer().getPluginManager().registerEvents(new InteractionListener(this), this);
     	getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
     	
     	if(getConfig().getBoolean("blocklog.updates"))
     		getServer().getPluginManager().registerEvents(new NoticeListener(this), this);
     }
 	
 	public void saveLogs(final int count) {
 		saveLogs(count, null);
 	}
 	
 	public void saveLogs(final int count, final Player player) {
 		getServer().getScheduler().scheduleAsyncDelayedTask(this, new Save(this, count, player));
 	}
 	
 	private void stopPlugin() {
 		try {
 			getServer().getScheduler().cancelTasks(this);
 			
 			log.info("Saving all the block edits!");
 			while(interactions.size() > 0) {
 	    		LoggedInteraction interaction = interactions.get(0);
 			    interaction.save();
 			    interactions.remove(0);
 	    	}
 			while(blocks.size() > 0) {
 				LoggedBlock block = blocks.get(0);
 				block.save();
 			  	blocks.remove(0);
 			}
 			log.info("Successfully saved all the block edits!");
 			
 			conn.close();
 		} catch(SQLException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	@Override
 	public void onEnable() {
 		loadPlugin();
 		PluginDescriptionFile PluginDesc = getDescription();
 		log.info("v" + PluginDesc.getVersion() + " is enabled!");
 	}
 	
 	@Override
 	public void onDisable() {
 		stopPlugin();
 		PluginDescriptionFile PluginDesc = getDescription();
 		log.info("v" + PluginDesc.getVersion() + " is disabled!");
 	}
 	
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
