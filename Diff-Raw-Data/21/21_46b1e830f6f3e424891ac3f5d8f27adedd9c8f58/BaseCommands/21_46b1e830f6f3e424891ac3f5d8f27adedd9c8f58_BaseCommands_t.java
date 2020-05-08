 package com.github.catchaser;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.Writer;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.logging.Logger;
 
 import net.milkbowl.vault.permission.Permission;
 
 import org.bukkit.event.Listener;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.github.catchaser.banning.BanExecutor;
 import com.github.catchaser.commands.BCC1;
 import com.github.catchaser.commands.BCC2;
 import com.github.catchaser.commands.BCC3;
 import com.github.catchaser.commands.ginfo;
 import com.github.catchaser.commands.misc.misc;
 import com.github.catchaser.commands.passwd.passwd;
 import com.github.catchaser.events.BanLogging;
 import com.github.catchaser.events.joining;
 import com.github.catchaser.events.mutedListener;
 import com.github.catchaser.home.home;
 import com.github.catchaser.listeners.BanStore;
 import com.github.catchaser.listeners.freezeListener;
 import com.github.catchaser.listeners.namestore;
 import com.github.catchaser.spawn.BCListener;
 import com.github.catchaser.warp.warp;
 
 import couk.Adamki11s.Extras.Extras.Extras;
 
 @SuppressWarnings("unused")
 public class BaseCommands extends JavaPlugin implements Listener{
 	public static Logger logger = Logger.getLogger("Minecraft");
 	public static BaseCommands plugin;
 	private BCC1 BCC1e;
 	private BCC3 BCC3e;
 	private ginfo ginfoe;
 	private BanExecutor ban;
 	private BCListener spawn;
 	private BCC2 BCC2e;
 	private home homee;
 	private warp warpe;
 	private misc mis;
 	private passwd pas;
 	public static final String PREFIX ="[BaseCommands]";
 	public BanStore bannedPlayers;
 	public namestore nick;
 	public static Permission permission = null;
 	public final freezeListener fl = new freezeListener(this);
 	public final mutedListener ml = new mutedListener(this);
 	public boolean freeze = false;
 	public boolean mute = false;
 	public boolean passwod = false;
 	
 	@SuppressWarnings("static-access")
 	@Override
 	public void onEnable() { // Enables the plugin
 		PluginDescriptionFile pdfFile = this.getDescription();
 		this.logger.info(PREFIX + " " + pdfFile.getName() + " Version: " + pdfFile.getVersion() + " has been enabled!");
 		Extras ex = new Extras("BaseCommands");
 		setupPermissions();
 
         this.getServer().getPluginManager().registerEvents(new joining(this), this); // registers the MOTD on join event
 		this.getServer().getPluginManager().registerEvents(new BanLogging(this), this); // registers the banned player on login event
 		this.getServer().getPluginManager().registerEvents(fl, this); //registers the freeze event
 		this.getServer().getPluginManager().registerEvents(ml, this);//registers the mute
 		
 		PDIR();
 		config();
 		checkConfig();
 		banned();
         LoadCommands();
     	HDIR();	
     	WDIR();
     	passwds();
 	}
 
 	@Override
 	public void onDisable() { //Disables the plugin
 		PluginDescriptionFile pdfFile = this.getDescription();
 		this.bannedPlayers.save();
 		logger.info("[BaseCommands] Saving Banned players"); //saves the BC-Banned-Player.txt file
 		BaseCommands.logger.info(pdfFile.getName() + " has been Disabled!");
 	}
 	
 	public void HDIR() { //Home folder
 		boolean success = (new File("plugins/BaseCommands/homes/")).mkdir();
 		if(success){//Creates the home folder if it doesn't exist
 			logger.info("[BaseCommands] Successfully created homes directory!");
 		}else{//loads the home foler if it exists
 			logger.info("[BaseCommands] Successfully loaded homes directory!");
 		}
 	}
 	
 	public void WDIR() {//warp folder
 		boolean success2 = (new File("plugins/BaseCommands/warps/")).mkdir();
 		if(success2) {//creates warp folder if it doesn't exist
 		logger.info("[BaseCommands] Successfully created warps directory!");	
 		}else{//loads the warp folder if it exists
 			logger.info("[BaseCommands] Successfully loaded warps directory!");
 		}
 	}
 	
 	public void PDIR() {
 		PluginDescriptionFile pdfFile = this.getDescription();
 		boolean success1 = (new File("plugins/BaseCommands/")).mkdir();
 		if(success1){ //creates the BaseCommands directory if it doesn't exist
 			logger.info("[BaseCommands] Successfully created BaseCommands directory!");
 		}else//loads BaseCommands directory if it exists
 			logger.info("[BaseCommands] Successfully Loaded Plugin directory!");
 		try //spawn file
 		{
 			boolean success11 = (new File("plugins/BaseCommands/spawn")).createNewFile();
 			if(success11)
 			{
 				logger.info(pdfFile.getName() + " Successfully created spawn file!");
 				Writer output = new FileWriter("plugins/BaseCommands/spawn", true);
 				output.write("0,0,0,0,0,0");
 				output.close();
 				logger.info("[BaseCommands] Set spawn to default spawn!");
 				logger.severe(PREFIX +"If this is the first time using this plugin please reload the server for the other directories to be Loaded");
 				logger.severe(PREFIX + "if this is not the first time then you can just ignore this message");
 			}else{
 				logger.info("[BaseCommands] Successfully loaded spawn File!");
 			}
 		}
 		catch(IOException x)
 		{
 			System.out.println(x.toString());
 		}	
 	}	
 	
 	public void LoadCommands() { //loads all the commands for BaseCommands
 		BCC3e = new BCC3(this);
 		BCC1e = new BCC1(this);
 		ginfoe = new ginfo(this);
 		spawn = new BCListener(this);
 		homee = new home(this);
 		warpe = new warp(this);
 		BCC2e = new BCC2(this);
 		mis = new misc(this);
 		ban = new BanExecutor(this);
 		pas = new passwd(this);
 		
     	getCommand("heal").setExecutor(BCC1e);
     	getCommand("tp").setExecutor(BCC1e);
     	getCommand("rules").setExecutor(BCC1e);
     	getCommand("tphere").setExecutor(BCC1e);
     	getCommand("whoiso").setExecutor(BCC1e);
     	getCommand("ginfo").setExecutor(ginfoe);
     	getCommand("ginfo2").setExecutor(ginfoe);
     	getCommand("fly").setExecutor(BCC1e);
     	getCommand("dfly").setExecutor(BCC1e);
     	getCommand("spawn").setExecutor(spawn);
 		getCommand("setspawn").setExecutor(spawn);
 		getCommand("home").setExecutor(homee);
 		getCommand("sethome").setExecutor(homee);
 		getCommand("warp").setExecutor(warpe);
 		getCommand("setwarp").setExecutor(warpe);
 		getCommand("delwarp").setExecutor(warpe);
 		getCommand("warplist").setExecutor(warpe);
 		getCommand("time").setExecutor(BCC2e);
 		getCommand("weather").setExecutor(BCC2e);
 		getCommand("kill").setExecutor(BCC2e);
 		getCommand("kick").setExecutor(BCC2e);
 		getCommand("ban").setExecutor(ban);
 		getCommand("unban").setExecutor(ban);
 		getCommand("nickname").setExecutor(mis);
 		getCommand("unnick").setExecutor(mis);
 		getCommand("bcversion").setExecutor(mis);
 		getCommand("feed").setExecutor(mis);
 		getCommand("msg").setExecutor(BCC3e);
 		getCommand("freeze").setExecutor(BCC3e);
 		getCommand("unfreeze").setExecutor(BCC3e);
 		getCommand("mute").setExecutor(BCC3e);
 		getCommand("unmute").setExecutor(BCC3e);
 		getCommand("passwd").setExecutor(pas);
 		getCommand("setpasswd").setExecutor(pas);
 		getCommand("resetpasswd").setExecutor(pas);
 	}
 	
 	public boolean setupPermissions(){ //loads the permissions using vualt
 		PluginDescriptionFile pdfFile = this.getDescription();
 		if(new File("plugins/Vault.jar").exists()) {
 		RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
 		if(permissionProvider != null){
 			permission = permissionProvider.getProvider();
 			logger.info(PREFIX + " Found and hooked into " + permissionProvider.getProvider().getName() + " with Vault!");
 		}
 		return (permission != null);
 		}else {
 			logger.severe(PREFIX + " Vault not found Defaulting to OP/Bukkit Permissions!");
 		}
 		return false;
 	}	
 	
 	public void config() {
 		PluginDescriptionFile pdfFile = this.getDescription();
		String ver = this.getConfig().getString("version#");
 		 if(new File("plugins/BaseCommands/config.yml").exists()) { //checks if config.yml already exsits
 				logger.info("[BaseCommands] Config Loaded"); //loads the config.yml
 			}else{
 			    logger.info("[BaseCommands] Config Created");
 			}
		 if(pdfFile.getVersion().equals(ver)) {
			 logger.info(PREFIX + " Config is up to date!");
		 }else if(!(pdfFile.getVersion().equals(ver))) {
			 logger.severe(PREFIX + " Config Outdated/Corrupted!!");
			 logger.warning(PREFIX + " THIS WILL RESET THE ENTIRE CONFIG!");
			 logger.warning(PREFIX + " ANY MODIFICATIONS TO THE CONFIG WILL BE ERASED SO YOU WILL HAVE TO SET THEM AGAIN!");
			 logger.info(PREFIX + " Replacing old/corrupted config!");
 			 File configFile = new File("plugins/BaseCommands/config.yml");
 			 configFile.delete();
 		 }
 	        if(this.getConfig().getString("passwd").equals("true")) {
 	        	logger.info(PREFIX + " Password enabled!");
 	        	passwod = true;
 	        }else if(this.getConfig().getString("passwd").equals("false")) {
 	        	logger.info(PREFIX + " Password disabled!");
 	        	passwod = false;
 	        }
 	}
 	public void banned() {
 		try {
         	if(new File("BC-Banned-Players.txt").exists()) { //checks if BC-Banned-Players.txt exsits
             	this.bannedPlayers = new BanStore(new File("BC-banned-players.txt"));
                 this.bannedPlayers.load(); //loads BC-Banned-Players.txt
                 logger.info(PREFIX + " Loaded the BC-banned-players file");
         	}else{
 			new File("BC-banned-players.txt").createNewFile(); //creates file if it doesnt exsit
         	logger.info(PREFIX + " Loaded BC-banned-players file");
         	this.bannedPlayers = new BanStore(new File("BC-banned-players.txt"));
             this.bannedPlayers.load(); 
         	}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	public void passwds() {
 		if(this.passwod) {
 			boolean success2 = (new File("plugins/BaseCommands/passwds/")).mkdir();
 			if(success2) {//creates passwords folder if it doesn't exist
 			logger.info("[BaseCommands] Successfully created Password directory!");	
 			}else{//loads the Password folder if it exists
 				logger.info("[BaseCommands] Successfully loaded Password directory!");
 			}
 		}else{
 			
 		}
 	}
 	
 	public void checkConfig() {
 		  if(!checkFile("config.yml", this.getDataFolder())) {
 		    getLogger().warning("Error creating config!");
 		  }
 		}
 		public boolean checkFile(String filename, File directory) {
 		  if(!(new File(directory, filename)).exists()) {
 		    if(!extractFile(filename, directory)) {
 		      return false;
 		    }
 		  }
 		  return true;
 		}
 		public boolean extractFile(String filename, File destination) {
 		  File outputFile = new File(destination, filename);
 		  DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
 		  Date date = new Date();
 		  String theDate = dateFormat.format(date);
 		  File backup = new File(destination, theDate + filename);
 		  try {
 		    if(outputFile.exists()) {
 		      outputFile.renameTo(backup);
 		    }
 		    destination.mkdir();
 		    if(getClass().getResourceAsStream("/" + filename) == null) {
 		      if(backup.exists()) {
 		        backup.renameTo(outputFile);
 		      }
 		      System.out.println("File not found in jar: " + filename);
 		      return false;
 		    }
 		    outputFile.createNewFile();
 		    InputStream is = getClass().getResourceAsStream("/" + filename);
 		    FileOutputStream fos = new FileOutputStream(outputFile);
 		    byte[] buffer = new byte[1024];
 		    int bytesRead;
 		    while((bytesRead = is.read(buffer)) > 0) {
 		      fos.write(buffer, 0, bytesRead);
 		    }
 		    fos.flush();
 		    fos.close();
 		    is.close();
 		  }
 		  catch (IOException e) {
 		    e.printStackTrace();
 		    System.out.println("Error extracting file: " + filename);
 		  }
 		  return true;
 		}
 }
