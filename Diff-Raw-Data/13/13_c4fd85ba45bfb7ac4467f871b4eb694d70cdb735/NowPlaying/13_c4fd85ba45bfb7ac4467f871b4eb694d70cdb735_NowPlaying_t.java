 package com.thegleek.bukkit.NowPlaying;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.logging.Logger;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 
 //import bsh.Interpreter;//TODO: REMOVE BEFORE DISTRIBUTION!
 
 /**
  * Last.FM plugin for Bukkit
  * 
  * @author thegleek
  */
 public class NowPlaying extends JavaPlugin {
 	// public Interpreter beanshell;// TODO: REMOVE BEFORE DISTRIBUTION!
 	private final static String PLUGIN_NAME = "NowPlaying";
 	public static final Logger log = Logger.getLogger("Minecraft");
 	private static final String CODE_NAME = "Diorama";
 	private String pluginSettingsFile = PLUGIN_NAME + ".settings";
 	private String sPrevDir = PLUGIN_NAME + "\\";
 	private File prevdir;
 	public static HashMap<String, String> players;
 	public static cControl Watch = new cControl();
 	public static cProperty Settings;
 	private cProperty PrevSettings;
 	private Boolean bLegacyPath = false;
 	public static String[] watching = { "apikey" };
 	public static String[] defaults = { "YOUR_LASTFM_API_KEY_GOES_HERE" };
 	private String sVersion;
 
 	/**
 	 * 
 	 */
 	public void onPluginLoad() {
 		this.prevdir = new File(sPrevDir);
 		this.bLegacyPath = this.prevdir.exists();
 		if (this.bLegacyPath) {
 			this.PrevSettings = new cProperty(sPrevDir + "\\"
 					+ pluginSettingsFile);
 			this.PrevSettings.load();
 		}
 
		File folder = this.getDataFolder();
		
 		if (!folder.exists()) {
 			folder.mkdirs();
 		}
 
		if (!new File(this.getDataFolder(), pluginSettingsFile).exists()) {
 			try {
				new File(this.getDataFolder(), pluginSettingsFile).createNewFile();
 			} catch (IOException ex) {
 				log.warning("[NowPlaying] " + ex.toString());
 			}
 		}
 
 		this.setVersion("loaded");
 		log.info(this.getVersion());
 	}
 
 	// TODO: REMOVE BEFORE DISTRIBUTION!
 	// public void EnableDebug() {
 	// enables the beanshell remote interpreter
 	// beanshell = new Interpreter();
 
 	// try {
 	// beanshell.set("plugin", this); // Provide a reference to your app
 	// beanshell.set("portnum", 1234);
 	// beanshell.eval("setAccessibility(true)"); // turn off access
 	// restrictions
 	// beanshell.eval("server(portnum)");
 	// } catch (Exception e) {
 	// log.severe("Beanshell initialization failure");
 	// }
 	// }
 
 	/**
 	 * @return
 	 */
 	public String getVersion() {
 		return this.sVersion;
 	}
 
 	/**
 	 * @param sVersion
 	 */
 	public void setVersion(String sVersion) {
 		this.sVersion = this.sVersionMessage(sVersion);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.bukkit.plugin.Plugin#onEnable()
 	 */
 	public void onEnable() {
 		onPluginLoad();
 
 		getCommand("nphelp").setExecutor(new cPlayerListener(this));
 		getCommand("npver").setExecutor(new cPlayerListener(this));
 		getCommand("npsize").setExecutor(new cPlayerListener(this));
 		getCommand("npadd").setExecutor(new cPlayerListener(this));
 		getCommand("npdel").setExecutor(new cPlayerListener(this));
 		getCommand("npcheck").setExecutor(new cPlayerListener(this));
 		getCommand("npb").setExecutor(new cPlayerListener(this));
 		getCommand("np").setExecutor(new cPlayerListener(this));
 
 		cPlayerListener.setAliasList(new AliasList(getServer()));
 
 		String sPluginDir = this.getDataFolder().getPath();
 		Settings = new cProperty(sPluginDir + "\\" + pluginSettingsFile);
 
 		if (this.bLegacyPath) {
 			Map<String, String> map = new HashMap<String, String>();
 			map = this.PrevSettings.returnProperties();
 
 			for (Entry<String, String> sKVP : map.entrySet()) {
 				cControl.add(sKVP.getKey(),
 						Settings.getString(sKVP.getKey(), sKVP.getValue()));
 			}
 
 			Settings.save();
 
 			System.out
 					.println("[NowPlaying]: Previous settings have been copied over to the new location.");
 			System.out
 					.println("[NowPlaying]: Previous settings folder (you can delete this folder): "
 							+ this.prevdir.getAbsolutePath() + "\\");
 			System.out.println("[NowPlaying]: Current settings folder: "
					+ this.getDataFolder().getAbsolutePath() + "\\");
 		} else {
 			for (int x = 0; x < watching.length; x++)
 				cControl.add(watching[x],
 						Settings.getString(watching[x], defaults[x]));
 		}
 
 		// EnableDebug();// TODO: REMOVE BEFORE DISTRIBUTION!
 
 		this.setVersion("enabled");
 		log.info(this.getVersion());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.bukkit.plugin.Plugin#onDisable()
 	 */
 	public void onDisable() {
 		this.setVersion("disabled");
 		log.info(this.getVersion());
 	}
 
 	/**
 	 * @param msg
 	 * @return
 	 */
 	private String sVersionMessage(String msg) {
 		PluginDescriptionFile pdfFile = this.getDescription();
 
 		sVersion = "[" + pdfFile.getName() + "]: v" + pdfFile.getVersion()
 				+ " (" + CODE_NAME + ")";
 
 		if (!msg.isEmpty()) {
 			sVersion += " " + msg;
 		}
 
 		sVersion += ".";
 
 		return sVersion;
 	}
 }
