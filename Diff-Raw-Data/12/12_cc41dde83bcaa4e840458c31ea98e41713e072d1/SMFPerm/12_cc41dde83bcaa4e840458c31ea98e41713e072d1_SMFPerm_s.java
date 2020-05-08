 package net.teamio.smfperm;
 
 /*
  * TODO: add groups.yml configuration file with format:
  * 
  * groups:
  *     10:
  *     - Members
  *     - Admins
  *     - Default
  *     2:
  *     - Members
  *     - Default
  * 
  * TODO: create Async task to handle permissions. We don't want this
  * holding down the whole server.
  * 
  * FUTURE: Integrate xAuth capability with this. But for now, no.
  * OKB3 had a secure mode we could also implement.
  * (We cannot take from AuthDB; it is not open-source.)
  */
 
 import lib.PatPeter.SQLibrary.MySQL;
 import net.milkbowl.vault.chat.Chat;
 import net.milkbowl.vault.permission.Permission;
 import net.teamio.ThreadHelper;
 import net.teamio.smfperm.SMFPermPlayerListener;
 
 import org.bukkit.event.Event;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.util.config.ConfigurationException;
 
 public class SMFPerm extends JavaPlugin{
 	
 	protected final ThreadHelper th = new ThreadHelper("SMFCon");
 	protected MySQL connection;
 	protected static Permission permission = null;
 	protected static Chat chat = null;
 	private final SMFPermPlayerListener playerListener = new SMFPermPlayerListener(this);
	private PluginManager pm = this.getServer().getPluginManager();
 
 	@Override
 	public void onDisable() {
		// TODO Auto-generated method stub
 		th.print("SMFPerm shutdown call caught, disabling...",1);
 		th.print("Closing connection to MySQL database...",0);
 		connection.close();
 		th.print("Closed SMFPerm version " + this.getDescription().getVersion(),1);
 	}
 
 	@Override
 	public void onEnable() {
		// TODO Auto-generated method stub
 		th.print("Starting up SMFPerm version " + this.getDescription().getVersion() + "...",0);
 		if (!setupPermission()){
 			th.print("Vault failed to setup permissions, disabling.",-1);
 			onDisable();
 		}
 		else {
 			th.print("Loaded Permissions.", 0);
 			try {
 				if (setupDefaults()){
 					th.print("New configuration has been written, disabling to avoid errors.",1);
 					th.print("Modify the new configuration and restart the server to load.",1);
 					onDisable();
 				}
 				else{
 					th.print("Loaded configuration.", 0);
 					if(!setupMySQL()){
 						th.print("Failed to establish a connection to MySQL, disabling.",-1);
 						onDisable();
 					}
					else if (!setupListener()){
 						th.print("Failed to register listeners, disabling.",-1);
 						onDisable();
 					}
 					else
 						th.print("Loaded!", 0);
 				}
 			} catch (ConfigurationException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 				th.print("Could not write/load configuration! Do you have permissions?", -1);
 				th.print("Or maybe an invalid configuration? (Use Notepad++! It can spot invisible characters.)",-1);
 				onDisable();
 			}
 		}
 		
 	}
 	
 	
	private boolean setupListener() {
 		pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Event.Priority.Normal, this);
 		pm.registerEvent(Event.Type.PLAYER_CHANGED_WORLD, playerListener, Event.Priority.Normal, this);
 		return true;
 	}
 
 	private boolean setupPermission(){
 		RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
 		if (permissionProvider != null) {
 			permission = permissionProvider.getProvider();
 		}
 
 		return (permission != null);
 	}
 	
 	private boolean setupDefaults() throws ConfigurationException{
 
 		boolean setup = false;
 		
 		/* mysql configuration */
 		try{
 			th.actMySQLFile(false);
 			if (!th.mysqlconfig.contains("prefix")){
 				th.mysqlconfig.set("prefix", "smf_");
 				setup = true;
 			}
 			if (!th.mysqlconfig.contains("hostname")){
 				th.mysqlconfig.set("hostname", "localhost");
 				setup = true;
 			}
 			if (!th.mysqlconfig.contains("port")){
 				th.mysqlconfig.set("port", "3306");
 				setup = true;
 			}
 			if (!th.mysqlconfig.contains("database")){
 				th.mysqlconfig.set("database", "forum");
 				setup = true;
 			}
 			if (!th.mysqlconfig.contains("username")){
 				th.mysqlconfig.set("username", "root");
 				setup = true;
 			}
 			if (!th.mysqlconfig.contains("password")){
 				th.mysqlconfig.set("password", "toor");
 				setup = true;
 			}
 			th.actMySQLFile(true);
 		}catch(Exception e1){
 			e1.printStackTrace();
 			throw new ConfigurationException("Could not set defaults!");
 		}
 		
 		/* defaults configuration */
 		try{
 			th.actDefaultsFile(false);
 			if (!th.defaults.contains("tabletocheck")){
 				th.defaults.set("tabletocheck","members");
 				setup = true;
 			}
 			if (!th.defaults.contains("username_field")){
 				th.defaults.set("username_field","member_name");
 				setup = true;
 			}
 			if (!th.defaults.contains("displayname_field")){
 				th.defaults.set("displayname_field", "real_name");
 				setup = true;
 			}
 			if (!th.defaults.contains("rank_field")){
 				th.defaults.set("rank_field","id_group");
 				setup = true;
 			}
 			th.actDefaultsFile(true);
 		}catch(Exception e1){
 			e1.printStackTrace();
 			throw new ConfigurationException("Could not set defaults!");
 		}
 		
 		/* groups configuration */
 		
 
 		return setup;
 	}
 	
 	private boolean setupMySQL() {
 		connection = new MySQL(th.getLog(), th.mysqlconfig.getString("prefix"),
 				th.mysqlconfig.getString("hostname"),
 				th.mysqlconfig.getString("port"),
 				th.mysqlconfig.getString("database"),
 				th.mysqlconfig.getString("username"),
 				th.mysqlconfig.getString("password"));
 		return connection.checkConnection();
 	}
 	
 	
 
 }
