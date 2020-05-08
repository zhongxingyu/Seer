 package com.amazar.plugin;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Level;
 
 import org.bukkit.ChatColor;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.amazar.utils.ListStore;
 
 public class ac extends JavaPlugin {
 	//Main class
 	private void copy(InputStream in, File file) {
 	    try {
 	        OutputStream out = new FileOutputStream(file);
 	        byte[] buf = new byte[1024];
 	        int len;
 	        while((len=in.read(buf))>0){
 	            out.write(buf,0,len);
 	        }
 	        out.close();
 	        in.close();
 	    } catch (Exception e) {
 	        e.printStackTrace();
 	    }
 	}
 	@SuppressWarnings("unchecked")
 	public static HashMap<String, String> loadHashMapString(String path)
 	{
 		try
 		{
 			System.out.println("Loading information!");
 	        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path));
 	        Object result = ois.readObject();
 	        ois.close();
 			return (HashMap<String, String>) result;
 		}
 		catch(Exception e)
 		{
 			System.out.println("Information failed to load error:");
 			e.printStackTrace();
 			return null;
 		}
 	}
 	public static void saveHashMap(HashMap<String, String> map, String path)
 	{
 		try
 		{
 			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path));
 			oos.writeObject(map);
 			oos.flush();
 			oos.close();
 			//Handle I/O exceptions
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 	}
 	public static Plugin bukkit;
 	public Plugin plugin;
 	public static ListStore news;
 	public static ListStore packages;
 	public static ListStore vote;
 	public static ListStore clans;
 	public static ListStore warns;
 	public static ListStore warnsplayer;
 	public static HashMap<String, String> clanMembers = new HashMap<String, String>();
 	public static HashMap<String, String> clanInvites = new HashMap<String, String>();
 	public static FileConfiguration config;
	public static PluginDescriptionFile pluginYaml;
 public void onEnable(){
 	//Now on github!
 	bukkit = this;
 	plugin = bukkit;
	pluginYaml = plugin.getDescription();
 	PluginDescriptionFile pldesc = plugin.getDescription();
     Map<String, Map<String, Object>> commands = pldesc.getCommands();
     Set<String> keys = commands.keySet();
     for(String k : keys){
     	try {
 			getCommand(k).setExecutor(new acCommandExecutor(this));
 		} catch (Exception e) {
 			getLogger().log(Level.SEVERE, "Error registering command " + k.toString());
 			e.printStackTrace();
 		}
     }
    getServer().getPluginManager().registerEvents(new AcListener(this), this);
     String pluginFolder = this.getDataFolder().getAbsolutePath();
 	(new File(pluginFolder)).mkdirs();
     File newsFile = new File(this.getDataFolder().getAbsolutePath() + File.separator + "news.txt");
     newsFile.getParentFile().mkdirs();
     //newsFile.mkdirs();
     try {
 		newsFile.createNewFile();
 	} catch (IOException e) {
 	}
     news = new ListStore(newsFile);
 	news.load();
 	File packageFile = new File(this.getDataFolder().getAbsolutePath() + File.separator + "packages.txt");
     packageFile.getParentFile().mkdirs();
     //newsFile.mkdirs();
     if(packageFile.exists() == false || packageFile.length() < 1){
     	try {
     		packageFile.createNewFile();
     	} catch (IOException e) {
     	}
     	copy(getResource("packages.txt"), packageFile);
     }
     try {
 		packageFile.createNewFile();
 	} catch (IOException e) {
 	}
     packages = new ListStore(packageFile);
 	packages.load();
 	config = getConfig();
 	try{
 		config.load(this.getDataFolder().getAbsolutePath() + File.separator + "config.yml");
 		if(!config.contains("general.loginmsg")){
 			config.set("general.loginmsg", "&c[Amazar Craft]&a %name% &6has joined &cAmazar craft&6!");
 		}
 		if(!config.contains("general.quitmsg")){
 			config.set("general.quitmsg", "&c[Amazar Craft]&a %name% &6has left &cAmazar craft&6!");
 		}
 	}
 	catch (Exception e){
 		//error
 	}
 	saveConfig();
 	File voteFile = new File(this.getDataFolder().getAbsolutePath() + File.separator + "voteInfo.txt");
     voteFile.getParentFile().mkdirs();
     //newsFile.mkdirs();
     try {
 		voteFile.createNewFile();
 	} catch (IOException e) {
 	}
     vote = new ListStore(voteFile);
 	vote.load();
 	File clansFile = new File(this.getDataFolder().getAbsolutePath() + File.separator + "clans.txt");
     clansFile.getParentFile().mkdirs();
     //newsFile.mkdirs();
     try {
 		clansFile.createNewFile();
 	} catch (IOException e) {
 	}
     clans = new ListStore(clansFile);
 	clans.load();
 	File clansMembersFile = new File(this.getDataFolder().getAbsolutePath() + File.separator + "clansMembers.bin");
     clansMembersFile.getParentFile().mkdirs();
     //newsFile.mkdirs();
     try {
 		clansMembersFile.createNewFile();
 	} catch (IOException e) {
 	}
     String path355 = this.getDataFolder().getAbsolutePath() + File.separator + "clansMembers.bin";
 	File file355 = new File(path355);
 	if(file355.exists() && file355.length() > 1){ // check if file exists before loading to avoid errors!
 		clanMembers = loadHashMapString(path355);
 	}
 	else{
 		getLogger().info("Created a new clansMembers.bin!");
 		clanMembers = new HashMap<String, String>();
 		saveHashMap(clanMembers, this.getDataFolder().getAbsolutePath() + File.separator + "clansMembers.bin");
 	}
 	File clansInvitesFile = new File(this.getDataFolder().getAbsolutePath() + File.separator + "cinvites.bin");
     clansInvitesFile.getParentFile().mkdirs();
     //newsFile.mkdirs();
     try {
 		clansInvitesFile.createNewFile();
 	} catch (IOException e) {
 	}
     String path3555 = this.getDataFolder().getAbsolutePath() + File.separator + "cinvites.bin";
 	File file3555 = new File(path3555);
 	if(file3555.exists() && file3555.length() > 1){ // check if file exists before loading to avoid errors!
 		clanInvites = loadHashMapString(path3555);
 	}
 	else{
 		getLogger().info("Created a new cinvites.bin!");
 		clanInvites = new HashMap<String, String>();
 		saveHashMap(clanInvites, this.getDataFolder().getAbsolutePath() + File.separator + "cinvites.bin");
 	}
 	pluginFolder = this.getDataFolder().getAbsolutePath();
 	(new File(pluginFolder)).mkdirs();
 	(new File(pluginFolder + File.separator + "warns")).mkdirs();
 	File warnsLogFile = new File(pluginFolder + File.separator + "warns.log");
 	if(!(warnsLogFile.exists()) || warnsLogFile.length() < 1){
 		//doesnt exist
 		try {
 			warnsLogFile.createNewFile();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 	}
 	warns = new ListStore(warnsLogFile);
 	warns.load();
 	getLogger().info("AmazarCraft plugin is enabled :)");	//Tell teh console it is enabled
 }
 
 public void onDisable(){
 	getLogger().info("AmazarCraft plugin is disabled");
 }
 }
