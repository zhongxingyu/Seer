 /**
  * 
  */
 package org.morganm.loginlimiter;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 import java.util.Set;
 import java.util.jar.JarEntry;
 import java.util.jar.JarFile;
 import java.util.logging.Logger;
 
 import net.milkbowl.vault.permission.Permission;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.morganm.loginlimiter.bans.BanFactory;
 import org.morganm.loginlimiter.bans.BanInterface;
 
 import com.sk89q.bukkit.migration.PermissionsResolverManager;
 import com.sk89q.bukkit.migration.PermissionsResolverServerListener;
 
 /**
  * @author morganm
  *
  */
 public class LoginLimiter extends JavaPlugin {
 	public static final Logger log = Logger.getLogger(LoginLimiter.class.toString());
 	public static final String logPrefix = "[LoginLimiter] ";
 	
     private Permission vaultPermission = null;
     private PermissionsResolverManager wepifPerms = null;
     private LoginQueue loginQueue;
     private BanInterface banObject;
     private OnDuty onDuty;
 	private String version;
 	private int buildNumber = -1;
 	private boolean configLoaded = false;
 	private MyPlayerListener playerListener;
 	private BanListener banListener;
 
 	@Override
 	public void onEnable() {
 		version = getDescription().getVersion();
 		getBuildNumber();
 		
     	Debug.getInstance().init(log, logPrefix+"[DEBUG] ", false);
 		loadConfig();
 		setupPermissions();
 		
 		banObject = BanFactory.getBanObject();
 		onDuty = new OnDuty(this);
 		loginQueue = new LoginQueue(this);
 		
 		playerListener = new MyPlayerListener(this);
 		banListener = new BanListener(this);
 		
 		getServer().getPluginManager().registerEvent(Type.PLAYER_PRELOGIN, playerListener, Priority.Lowest, this);
 		getServer().getPluginManager().registerEvent(Type.PLAYER_QUIT, playerListener, Priority.Lowest, this);
 		getServer().getPluginManager().registerEvent(Type.PLAYER_LOGIN, playerListener, Priority.Monitor, this);
 		
 		getServer().getPluginManager().registerEvent(Type.PLAYER_PRELOGIN, banListener, Priority.Monitor, this);
 		getServer().getPluginManager().registerEvent(Type.PLAYER_LOGIN, banListener, Priority.Monitor, this);
 		
 		// run every 30 minutes
 		getServer().getScheduler().scheduleAsyncRepeatingTask(this, new ScheduleRunner(this, playerListener), 36000, 36000);
 		
 		log.info(logPrefix + "version "+version+", build "+buildNumber+" is enabled");
 	}
 
 	@Override
 	public void onDisable() {
 		getServer().getScheduler().cancelTasks(this);
 		log.info(logPrefix + "version "+version+", build "+buildNumber+" is disabled");
 	}
 	
 	/** We run this on a fixed schedule to do scheduled things.
 	 * 
 	 * @author morganm
 	 *
 	 */
 	private class ScheduleRunner implements Runnable {
 		private LoginLimiter plugin;
 		private MyPlayerListener listener;
 		public ScheduleRunner(LoginLimiter plugin, MyPlayerListener listener) {
 			this.plugin = plugin;
 			this.listener = listener;
 		}
 		public void run() {
 			Set<String> rejects = listener.getNoRequiredPermsRejects();
 			int numRejects = rejects.size();
 			
 			// send a tickler to every OFF duty eligible player to let them know how many people
 			// we've rejected today as a result of being OFF duty
 			List<Player> dutyEligible = plugin.getDutyEligiblePlayers();
 			if( dutyEligible.size() > 0 ) {
 				for(Player p : dutyEligible) {
 					if( onDuty.isOffDuty(p.getName()) ) {
 						p.sendMessage(ChatColor.YELLOW+"Total noPermission/duty rejections today: "+numRejects);
 					}
 				}
 			}
 			
 			// also echo to console
			log.info(logPrefix+"Total noPermission/duty rejections today: "+numRejects);
 		}
 	}
 	
 	@Override
 	public boolean onCommand(CommandSender sender, Command command,
 			String label, String[] args) {
 		String cmdName = command.getName();
 		
 		if( "ll".equals(cmdName) ) {
 			if( has(sender, command.getPermission()) ) {
 				if( args.length > 0 ) {
 					if( args[0].equals("reload") ) { 
 						loadConfig();
 						sender.sendMessage(ChatColor.YELLOW+"LoginLimiter config file reloaded");
 					}
 					else if( args[0].equals("clear") ) {
 						loginQueue.clearQueues();
 						sender.sendMessage(ChatColor.YELLOW+"Login queues cleared");
 					}
 					else if( args[0].equals("list") ) {
 						sender.sendMessage(ChatColor.YELLOW+"Players in reconnect queue: ");
 						StringBuffer sb = new StringBuffer();
 						int i = 1;
 						Set<String> players = loginQueue.getReconnectQueuedPlayers();
 						for(String p : players) {
 							if(sb.length() > 0)
 								sb.append(", ");
 							sb.append(i);
 							sb.append(":");
 							sb.append(p);
 							i++;
 						}
 						if( sb.length() == 0 )
 							sb.append("(none)");
 						sender.sendMessage(ChatColor.YELLOW+sb.toString());
 						
 						sender.sendMessage(ChatColor.YELLOW+"Players in login queue: ");
 						sb = new StringBuffer();
 						i = 1;
 						players = loginQueue.getQueuedPlayers();
 						for(String p : players) {
 							if(sb.length() > 0)
 								sb.append(", ");
 							sb.append(i);
 							sb.append(":");
 							sb.append(p);
 							i++;
 						}
 						if( sb.length() == 0 )
 							sb.append("(none)");
 						sender.sendMessage(ChatColor.YELLOW+sb.toString());
 					}
 				}
 				else {
 					sender.sendMessage(command.getUsage());
 				}
 			}
 			else
 				sender.sendMessage(ChatColor.DARK_RED + "No permission");
 			
 			return true;
 		}
 		else if( "onduty".equals(cmdName) || "offduty".equals(cmdName) || "dutylist".equals(cmdName) ) {
 			return onDuty.onCommand(sender, command, label, args);
 		}
 
 		return false;
 	}
 	
 	public void loadConfig() {
 		File file = new File(getDataFolder(), "config.yml");
 		if( !file.exists() ) {
 			copyConfigFromJar("config.yml", file);
 		}
 		
 		if( !configLoaded ) {
 			getConfig();
 			configLoaded = true;
 		}
 		else
 			reloadConfig();
 		
 		Debug.getInstance().setDebug(getConfig().getBoolean("debug", false));
 	}
 	
 	public LoginQueue getLoginQueue() { return loginQueue; }
 	public OnDuty getOnDuty() { return onDuty; }
 	public BanInterface getBanObject() { return banObject; }
 	
 	public boolean isDutyEligible(Player p) {
 		return has(p, "loginlimiter.duty");
 	}
 	public List<Player> getDutyEligiblePlayers() {
 		Player[] players = getServer().getOnlinePlayers();
 		ArrayList<Player> result = new ArrayList<Player>(players.length/2); 
 		for(int i=0; i < players.length; i++) {
 			if( isDutyEligible(players[i]) )
 				result.add(players[i]);
 		}
 		return result;
 	}
 
 	private void setupPermissions() {
 		if( !setupVaultPermissions() )
 			if( !setupWEPIFPermissions() ) {
 				log.warning(logPrefix+" No Vault or WEPIF perms found, permissions functioning in degraded mode (superperms does NOT support prelogin or offline permissions).");
 			}
 	}
 	
     private Boolean setupVaultPermissions()
     {
     	Plugin vault = getServer().getPluginManager().getPlugin("Vault");
     	if( vault != null ) {
 	        RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
 	        if (permissionProvider != null) {
 	        	Debug.getInstance().debug("Vault permissions found and enabled");
 	            vaultPermission = permissionProvider.getProvider();
 	        }
     	}
     	else
         	Debug.getInstance().debug("Vault permissions not found");
     	
         return (vaultPermission != null);
     }
     
     private boolean setupWEPIFPermissions() {
     	try {
 	    	Plugin worldEdit = getServer().getPluginManager().getPlugin("WorldEdit");
 	    	if( worldEdit != null ) {
 		    	wepifPerms = new PermissionsResolverManager(this, "LoginLimiter", log);
 		    	(new PermissionsResolverServerListener(wepifPerms, this)).register(this);
 		    	Debug.getInstance().debug("WEPIF permissions enabled");
 	    	}
     	}
     	catch(Exception e) {
     		log.info(logPrefix + " Unexpected error trying to setup WEPIF permissions hooks (this message can be ignored): "+e.getMessage());
     	}
     	
     	return wepifPerms != null;
     }
     
     public boolean isNewPlayer(String playerName) {
     	boolean newPlayerFlag = true;
     	
     	String playerDat = playerName + ".dat";
     	
     	// start with the easy, most likely check
     	File file = new File("world/players/"+playerDat);
     	if( file.exists() )
     		newPlayerFlag = false;
     	
     	/* It seems all player files go to the defaultWorld, so this part is unnecessary.
     	 * Not sure if it's possible to change the default world, should probably look
     	 * into that and adjust the above check accordingly.
     	 * 
     	// failing that, check all worlds
     	List<World> worlds = getServer().getWorlds();
     	for(World w : worlds) {
     		file = new File(w.getName()+"/players/"+playerDat);
     		if( file.exists() )
     			return false;
     	}
     	*/
     	
     	Debug.getInstance().debug("isNewPlayer() playerName=",playerName,", result=",newPlayerFlag);
     	
     	// if we didn't find any record of this player on any world, they must be new
     	return newPlayerFlag;
     }
     
     /** Check to see if player has a given permission.
      * 
      * @param p The player
      * @param permission the permission to be checked
      * @return true if the player has the permission, false if not
      */
     public boolean has(CommandSender sender, String permission) {
     	Player p = null;
     	// console always has access
     	if( sender instanceof ConsoleCommandSender )
     		return true;
     	if( sender instanceof Player )
     		p = (Player) sender;
     	
     	if( p == null )
     		return false;
     	
     	if( vaultPermission != null )
     		return vaultPermission.has(p, permission);
     	else if( wepifPerms != null )
     		return wepifPerms.hasPermission(p.getName(), permission);
     	else
     		return p.hasPermission(permission);		// fall back to superperms
     }
     
     public boolean has(String player, String permission) {
     	if( vaultPermission != null )
     		return vaultPermission.has("world", player, permission);
     	else if( wepifPerms != null )
     		return wepifPerms.hasPermission(player, permission);
     	else
     		return false;	// no options with superperms
     }
     
     /*
     public boolean isInGroup(Player p, String group) {
     	if( group == null )
     		return false;
     	
     	if( vaultPermission != null )
     		return group.equals(vaultPermission.getPrimaryGroup(p));
     	else
     		return false;
     }
     */
     
 	/** Code adapted from Puckerpluck's MultiInv plugin.
 	 * 
 	 * @param string
 	 * @return
 	 */
     private void copyConfigFromJar(String fileName, File outfile) {
         File file = new File(getDataFolder(), fileName);
         
         if (!outfile.canRead()) {
             try {
             	JarFile jar = new JarFile(getFile());
             	
                 file.getParentFile().mkdirs();
                 JarEntry entry = jar.getJarEntry(fileName);
                 InputStream is = jar.getInputStream(entry);
                 FileOutputStream os = new FileOutputStream(outfile);
                 byte[] buf = new byte[(int) entry.getSize()];
                 is.read(buf, 0, (int) entry.getSize());
                 os.write(buf);
                 os.close();
             } catch (Exception e) {
                 log.warning(logPrefix + " Could not copy config file "+fileName+" to default location");
             }
         }
     }
     
     private void getBuildNumber() {
         try {
         	JarFile jar = new JarFile(getFile());
         	
             JarEntry entry = jar.getJarEntry("build.number");
             InputStream is = jar.getInputStream(entry);
         	Properties props = new Properties();
         	props.load(is);
         	is.close();
         	Object o = props.get("build.number");
         	if( o instanceof Integer )
         		buildNumber = ((Integer) o).intValue();
         	else if( o instanceof String )
         		buildNumber = Integer.parseInt((String) o);
         } catch (Exception e) {
             log.warning(logPrefix + " Could not load build number from JAR");
         }
     }
 }
