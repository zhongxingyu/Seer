 package com.minecarts.dbconnector;
 
 import java.util.logging.Logger;
 import java.util.List;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.util.config.Configuration;
 
 import com.minecarts.dbconnector.providers.*;
 import com.minecarts.dbconnector.command.DBCommand;
 
 import java.sql.Connection;
 
 public class DBConnector extends org.bukkit.plugin.java.JavaPlugin{
 	public final Logger log = Logger.getLogger("com.minecarts.dbconnector"); 
 	public MySQLPool minecarts;
 	
 	private HashMap<String,Provider> providers = new HashMap<String, Provider>();
 	
     public void onEnable() {
         PluginDescriptionFile pdf = getDescription();
         Configuration config = getConfiguration();
        
         List<String> providersActive = config.getStringList("providersActive", null);
         
         String pkf = "providers.%s.%s"; //Pool Key Format string 
         for(String provider : providersActive){
             if(config.getString(String.format(pkf,provider,"type")).equalsIgnoreCase("mysqlpool")){
                 MySQLPool msqlp = new MySQLPool();
                 msqlp.connect(
                         config.getString(String.format(pkf,provider,"url"), "jdbc:mysql://localhost:3306/database"),
                         config.getString(String.format(pkf,provider,"username"), "username"),
                         config.getString(String.format(pkf,provider,"password"), "password"),
                         config.getInt(String.format(pkf,provider,"min_conn"), 3),
                         config.getInt(String.format(pkf,provider,"max_conn"), 5),
                         config.getInt(String.format(pkf,provider,"max_create"), 7),
                         config.getInt(String.format(pkf,provider,"conn_timeout"), 60*60)
                        );
                 providers.put(provider, msqlp);
             }
         }
         
         //Register commands
         getCommand("db").setExecutor(new DBCommand(this));
         
         log.info("[" + pdf.getName() + "] version " + pdf.getVersion() + " enabled.");
     }
     
     public void onDisable(){
         for (Object value : providers.values()) {
             if(value instanceof MySQLPool){ //Release all MySQLPools
                 ((MySQLPool)value).connected = false;
                ((MySQLPool)value).pool.release();
                 ((MySQLPool)value).pool.unregisterMBean();
                 System.out.println("Released pool");
             }
         }
     }
     
     public Connection getConnection(String providerName){
         if(this.providers.containsKey(providerName)){
             return this.providers.get(providerName).getConnection();
         } else {
             log.severe("Invalid provider name provided to DBConnector: " + providerName);
             return null;
         }
     }
 }
