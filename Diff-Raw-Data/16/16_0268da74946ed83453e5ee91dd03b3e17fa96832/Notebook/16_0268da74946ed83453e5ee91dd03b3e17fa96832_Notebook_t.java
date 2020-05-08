 package com.moosemanstudios.Notebook.Bukkit;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.logging.Logger;
 
 import net.gravitydevelopment.updater.Updater;
 
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.mcstats.Metrics;
 import org.mcstats.Metrics.Graph;
 
 import com.moosemanstudios.Notebook.Core.NoteManager;
 import com.moosemanstudios.Notebook.Core.NoteManager.Backend;
 
 public class Notebook  extends JavaPlugin {
 	public Logger log = Logger.getLogger("minecraft");
 	NotebookCommandExecutor noteExecutor;
 	String prefix = "[Notebook]";
 	PluginDescriptionFile pdfFile;
 	Backend backend;
 	Boolean debug = false;
 	Boolean broadcastMessage;
 	public Boolean updaterEnabled, updaterAuto, updaterNotify, updateAvailable;
 	String updateName = "";
 	Long updateSize = 0L;
 	public File pluginFile;
 	public Boolean sqlibraryFound;
 	
 	@Override
 	public void onEnable() {
 		pdfFile = this.getDescription();
 		pluginFile = this.getFile();
 		
 		// load the config - creating if not exists
 		loadConfig();
 		
 		// check updater settings
 		if (updaterEnabled) {
 			if (updaterAuto) {
 				Updater updater = new Updater(this, 35179, this.getFile(), Updater.UpdateType.DEFAULT, true);
 				if (updater.getResult() == Updater.UpdateResult.SUCCESS)
 				log.info(prefix + " Update downloaded successfully, restart server to apply update");
 			}
 			if (updaterNotify) {
 				log.info(prefix + " Notifying admins as they login if update found");
 				this.getServer().getPluginManager().registerEvents(new UpdaterPlayerListener(this), this);
 			}
 		}
 		
 		// check if SQLibrary is found before proceeding
 		if (getServer().getPluginManager().getPlugin("SQLibrary") == null) {
 			sqlibraryFound = false;
 				
 			if ((backend == Backend.SQLITE) || (backend == Backend.MYSQL)) {
 				// warn user that sqlibrary wasn't found, defaulting to flat file this time around
 				log.warning(prefix + "SQlibrary was not found and is required for mysql and sqlite database storage.");
 				log.warning(prefix + "Please download it from http://dev.bukkit.org/bukkit-plugins/sqlibrary");
 				log.warning(prefix + "Defaulting to flatfile for now");
 				backend = Backend.FLATFILE;
 			}
 		} else {
 			sqlibraryFound = true;
 		}
 		
 		// set the backend at this point
 		NoteManager.getInstance().setBackend(backend);
 		NoteManager.getInstance().init(this.getDataFolder().toString());
 		
 		// init based on backend method currently set
 		if (NoteManager.getInstance().getBackend() == Backend.FLATFILE)
 			NoteManager.getInstance().initFlatFile(getConfig().getString("storage.flatfile.filename"));
 		else if (NoteManager.getInstance().getBackend() == Backend.SQLITE)
 			NoteManager.getInstance().initSqlite(getConfig().getString("storage.sqlite.filename"), getConfig().getString("storage.sqlite.table"));
 		else if (NoteManager.getInstance().getBackend() == Backend.MYSQL)
 			NoteManager.getInstance().initMysql(getConfig().getString("storage.mysql.host"), getConfig().getInt("storage.mysql.port"), getConfig().getString("storage.mysql.username"), getConfig().getString("storage.mysql.password"), getConfig().getString("storage.mysql.database"), getConfig().getString("storage.mysql.table"));
 	
 		// register the command executor
 		noteExecutor = new NotebookCommandExecutor(this);
 		getCommand("note").setExecutor(noteExecutor);
 		
 		try {
 			Metrics metrics = new MetricsBukkit(this.getName(), this.getDescription().getVersion());
 			
 			Graph graph = metrics.createGraph("Number of note entries");
 			graph.addPlotter(new Metrics.Plotter("Notes") {
 				@Override
 				public int getValue() {
 					return NoteManager.getInstance().getNumNotes();
 				}
 			});
 			
			Graph graph2 = metrics.createGraph("Backend type");
			graph2.addPlotter(new Metrics.Plotter(NoteManager.getInstance().getBackend().toString()) {
				@Override
				public int getValue() {
					return 1;
				}
			});
			
 			metrics.start();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	
 		// everything is done, at this point let the player know its enabled.
 		log.info(prefix + " version " + pdfFile.getVersion() + " is enabled");
 	}
 	
 	@Override
 	public void onDisable() {
 		// save the hashmap to disk
 		log.info(prefix + " is disabled");
 	}
 	
 	public void loadConfig() {
 		if (!getConfig().contains("do-not-change-config-version")) {
 			getConfig().set("do-not-change-config-version", 2);
 			
 			// remove the backend property, no longer needed
 			getConfig().set("backend", null);	
 			saveConfig();
 		}
 		
 		// check config version and apply appropriately
 		switch(getConfig().getInt("do-not-change-config-version")) {
 			case(2):
 				// misc stuff				
 				if (!getConfig().contains("broadcast-message")) getConfig().set("broadcast-message", true);
 				if (!getConfig().contains("debug")) getConfig().set("debug", false);
 				
 				// updater stuff
 				if (!getConfig().contains("updater.enabled")) getConfig().set("updater.enabled", true);
 				if (!getConfig().contains("updater.auto")) getConfig().set("updater.auto", true);
 				if (!getConfig().contains("updater.notify")) getConfig().set("updater.notify", false);
 
 				// flat file stuff
 				if (!getConfig().contains("storage.flatfile.enabled")) getConfig().set("storage.flatfile.enabled", true);
 				if (!getConfig().contains("storage.flatfile.filename")) getConfig().set("storage.flatfile.filename", "notes.txt");
 
 				// sqlite stuff
 				if (!getConfig().contains("storage.sqlite.enabled")) getConfig().set("storage.sqlite.enabled", false);
 				if (!getConfig().contains("storage.sqlite.filename")) getConfig().set("storage.sqlite.filename", "notes.db");
 				if (!getConfig().contains("storage.sqlite.table")) getConfig().set("storage.sqlite.table", "notes");
 					
 				// mysql stuff
 				if (!getConfig().contains("storage.mysql.enabled")) getConfig().set("storage.mysql.enabled", false);
 				if (!getConfig().contains("storage.mysql.host")) getConfig().set("storage.mysql.host", "localhost");
 				if (!getConfig().contains("storage.mysql.port")) getConfig().set("storage.mysql.port", 3306);
 				if (!getConfig().contains("storage.mysql.username")) getConfig().set("storage.mysql.username", "root");
 				if (!getConfig().contains("storage.mysql.password")) getConfig().set("storage.mysql.password", "password");
 				if (!getConfig().contains("storage.mysql.database")) getConfig().set("storage.mysql.database", "minecraft");
 				if (!getConfig().contains("storage.mysql.table")) getConfig().set("storage.mysql.table", "notes");
 
 				saveConfig();
 				break;
 			default:
 				break;
 		}
 
 		// now read in the values
 		reloadConfig();
 		
 		debug = getConfig().getBoolean("debug");
 		if (debug) {
 			log.info(prefix + " debugging mode enabled");
 		}
 		
 		broadcastMessage = getConfig().getBoolean("broadcast-message");
 		if (debug) {
 			if (broadcastMessage) {
 				log.info(prefix + " broadcast-messages enabled");
 			}
 		}
 		
 		// updater code
 		updaterEnabled = getConfig().getBoolean("updater.enabled");
 		updaterAuto = getConfig().getBoolean("updater.auto");
 		updaterNotify = getConfig().getBoolean("updater.notify");
 		if (debug) {
 			if (updaterEnabled)
 				log.info(prefix + " Updater enabled");
 			if (updaterAuto)
 				log.info(prefix + " Auto updating enabled");
 			if (updaterNotify)
 				log.info(prefix + " Notifying on update");
 		}
 		
 		// check backend types, make sure only ones enabled
 		int i = 0;
 		if (getConfig().getBoolean("storage.flatfile.enabled")) i++;
 		if (getConfig().getBoolean("storage.sqlite.enabled")) i++;
 		if (getConfig().getBoolean("storage.mysql.enabled")) i++;
 		
 		if (i == 0) {
 			log.severe(prefix + "At least one storage method must be enabled.  Defaulting to flatfile");
 			getConfig().set("storage.flatfile.enabled", true);
			saveConfig();
 		}
 		if (i != 1) {
 			log.severe(prefix + "More than one storage method enabled, only one can be used at a time.");
 			
 			// find the first one thats enabled and enable it only
 			if (getConfig().getBoolean("storage.flatfile.enabled")) {
				getConfig().set("storage.sqlite.enabled", false);
 				getConfig().set("storage.mysql.enabled", false);
				saveConfig();
 				log.severe(prefix + "Enabling flatfile only");
 			}
 			if (getConfig().getBoolean("storage.sqlite.enabled")) {
 				getConfig().set("storage.mysql.enabled", false);
				saveConfig();
 				log.severe(prefix + "Enabling sqlite only");
 			}
 		}
 		
 		if (getConfig().getBoolean("storage.flatfile.enabled")) backend = Backend.FLATFILE;
 		if (getConfig().getBoolean("storage.sqlite.enabled")) backend = Backend.SQLITE;
 		if (getConfig().getBoolean("storage.mysql.enabled")) backend = Backend.MYSQL;
 		
 		
 		log.info(prefix + " config loaded successfully");
 	}
 	
 	public File getFileFolder() {
 		return this.getFile();
 	}
 }
