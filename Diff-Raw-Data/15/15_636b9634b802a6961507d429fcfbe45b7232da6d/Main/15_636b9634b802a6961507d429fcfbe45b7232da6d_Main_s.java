 package de.bdh.kb2;
 
 import java.io.File;
 
 import net.milkbowl.vault.economy.Economy;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Server;
 import org.bukkit.configuration.Configuration;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import de.bdh.kb2.Commander;
 import de.bdh.kb2.KBPlayerListener;
 import de.bdh.kb.util.Database;
 import de.bdh.kb.util.Downloader;
 import de.bdh.kb.util.configManager;
 
 public class Main extends JavaPlugin
 {
 	private static Server Server = null;
 	public static Database Database = null;
 	public KBPlayerListener playerListener = null;
 	public Economy econ = null;
 	public KBHelper helper = null;
 
  	public Main()
     {
  		
     }
 
     public void onDisable()
     {
         getServer().getScheduler().cancelTasks(this);
         System.out.println((new StringBuilder(String.valueOf(cmdName))).append("by ").append(author).append(" version ").append(version).append(" disabled.").toString());
     }
 
     public void onEnable()
     {	
     	Server = getServer();
     	
     	//Lade Config Datei
     	new configManager(this);
     	
     	//Lade mySQL Lib - wenn nicht existent
     	if (!(new File("lib/", "mysql-connector-java-bin.jar")).exists()) Downloader.install(configManager.MySQL_Jar_Location, "mysql-connector-java-bin.jar");
     	
     	//Erstelle Tabellen
     	try
     	{
     		Database = new Database();
     		Database.setupTable();
     		Database.setupTableMutex();
     	} catch(Exception e)
     	{
     		System.out.println((new StringBuilder()).append("[KB] Database initialization failed: ").append(e).toString());
             Server.getPluginManager().disablePlugin(this);
             return;
     	}
     	
         pdf = getDescription();
         name = pdf.getName();
         cmdName = (new StringBuilder("[")).append(name).append("] ").toString();
         version = pdf.getVersion();
         author = "Krim";
         
         System.out.println((new StringBuilder(String.valueOf(cmdName))).append("by ").append(author).append(" version ").append(version).append(" enabled.").toString());
        
         RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
         if (rsp == null) 
         {
        	System.out.println((new StringBuilder()).append("[KB] unable to hook iconomy").toString()); 
         } else
         	econ = rsp.getProvider();
         
         this.helper = new KBHelper(this);
         this.helper.Tick();
         playerListener = new KBPlayerListener(this);
         
         KBTimer k = new KBTimer(this);
         Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, k, 1, 1);
         
         Bukkit.getServer().getPluginManager().registerEvents(playerListener, this);
         Commander c = new Commander(this);
         getCommand("makesell").setExecutor(c); 
         getCommand("buyGS").setExecutor(c);
         getCommand("upgradeGS").setExecutor(c);
         getCommand("passwortGS").setExecutor(c);
         getCommand("useGS").setExecutor(c);
         getCommand("giveGS").setExecutor(c);
         getCommand("listGS").setExecutor(c);
         getCommand("delGS").setExecutor(c);
         getCommand("mineGS").setExecutor(c);
         getCommand("passGS").setExecutor(c);
         getCommand("sellGS").setExecutor(c); 
         getCommand("kbruleset").setExecutor(c); 
         getCommand("nolooseGS").setExecutor(c);
         getCommand("neverlooseGS").setExecutor(c);
         getCommand("kbupdate").setExecutor(c); 
         getCommand("tpGS").setExecutor(c); 
         getCommand("nextGS").setExecutor(c); 
         
 
     	if(configManager.BrauTec.equals("1"))
     	{
     		System.out.println((new StringBuilder()).append("[KB] BrauTec Adaption loaded").toString());
     	} else
     	{
    		System.out.println((new StringBuilder()).append("[KB] BdH Creative Mode").toString());
     	}
     }
     
     public static PluginDescriptionFile pdf;
     public static String name;
     public static String cmdName;
     public static String version;
     public static String author;
     public static Configuration config;
 }
