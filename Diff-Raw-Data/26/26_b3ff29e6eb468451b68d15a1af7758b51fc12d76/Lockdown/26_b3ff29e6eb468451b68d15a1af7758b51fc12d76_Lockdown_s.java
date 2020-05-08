 package io.github.matho97.lockdown;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Lockdown extends JavaPlugin implements Listener{
 
 	// Easier chat coloring during string broadcasts and such. Seeing as we do it so much in here ;)
 	public ChatColor
 	aqua = ChatColor.AQUA,
 	black = ChatColor.BLACK,
 	blue = ChatColor.BLUE,
 	bold = ChatColor.BOLD,
 	darkaqua = ChatColor.DARK_AQUA,
 	darkblue = ChatColor.DARK_BLUE,
 	darkgray = ChatColor.DARK_GRAY,
 	darkgreen = ChatColor.DARK_GREEN,
 	darkpurple = ChatColor.DARK_PURPLE,
 	darkred = ChatColor.DARK_RED,
 	gold = ChatColor.GOLD,
 	gray = ChatColor.GRAY,
 	green = ChatColor.GREEN,
 	italic = ChatColor.ITALIC,
 	magic = ChatColor.MAGIC,
 	purple = ChatColor.LIGHT_PURPLE,
 	red = ChatColor.RED,
 	reset = ChatColor.RESET,
 	strike = ChatColor.STRIKETHROUGH,
 	underline = ChatColor.UNDERLINE,
 	white = ChatColor.WHITE,
 	yellow = ChatColor.YELLOW
 	;
 	
 	public String lockdown = red + "[" + gold + "LockDown" + red + "] " + white;
 	
 	public String location1 = "Lockdown.Location 1";
 	public String location2 = "Lockdown.Location 2";
 	public String spawn1 = "Lockdown.Spawn 1";
 	public String spawn2 = "Lockdown.Spawn 2";
 	
 	@Override
 	public void onEnable(){
 		loadConfiguration();
 		//getLogger().info("Lockdown has been enabled");
 		getCommand("lockdown").setExecutor(new LockdownCommandExecutor(this));
 		this.getServer().getPluginManager().registerEvents(this, this);
 		//new PluginListener(this);
 	}
 	
 	public void loadConfiguration(){
 	     //See "Creating you're defaults"
 		getConfig().addDefault(location1, "");
 		getConfig().addDefault(location2, "");
 		//getConfig().addDefault(spawn1, "");
 		//getConfig().addDefault(spawn2, "");
 		getConfig().addDefault("Lockdown.On", "");
 		getConfig().addDefault("Lockdown.Auto.On", "");
 		getConfig().addDefault("Lockdown.Auto.Count", 0);
 	    getConfig().options().copyDefaults(true); // NOTE: You do not have to use "plugin." if the class extends the java plugin
 	    //Save the config whenever you manipulate it
 	    saveConfig();
 	}
 	
 	@EventHandler(priority = EventPriority.HIGH)
 	public void onPlayerJoinEvent(PlayerJoinEvent event){
         Player player = event.getPlayer();
     	boolean on = getConfig().getBoolean("Lockdown.On");
 		if(on == true){
 		    Double sx = getConfig().getDouble(location1 + ".X");
 		    Double sy = getConfig().getDouble(location1 + ".Y");
 		    Double sz = getConfig().getDouble(location1 + ".Z");
 		    
 		    Double spitch = getConfig().getDouble(location1 + ".Pitch");
 		    Double syaw = getConfig().getDouble(location1 + ".Yaw");
 		    
 		    /*Double spawnX = getConfig().getDouble(spawn1 + ".X");
 		    Double spawnY = getConfig().getDouble(spawn1 + ".Y");
 		    Double spawnZ = getConfig().getDouble(spawn1 + ".Z");*/
 		    
 		    Float pitch = spitch.floatValue();
 			Float yaw = syaw.floatValue();
 			
 			Location teleportloc = new Location(player.getWorld(), sx, sy, sz, yaw, pitch);
 			
			if((player.hasPermission("lockdown.immune"))){
 				//player.setBedSpawnLocation(new Location(player.getWorld(), spawnX, spawnY, spawnZ), true);
 				player.teleport(teleportloc);
 			}
 			player.sendMessage(lockdown + yellow + "The prison is now under lockdown, you will not be able to leave this area!");
 		}
 	}
 
 	@EventHandler(priority = EventPriority.HIGH)
 	public void onPlayerRespawn(PlayerRespawnEvent event){
 		Player player = event.getPlayer();
     	boolean on = getConfig().getBoolean("Lockdown.On");
     	
 		if(on == true){
 		    Double sx = getConfig().getDouble(location1 + ".X");
 		    Double sy = getConfig().getDouble(location1 + ".Y");
 		    Double sz = getConfig().getDouble(location1 + ".Z");
 		    
 		    Double spitch = getConfig().getDouble(location1 + ".Pitch");
 		    Double syaw = getConfig().getDouble(location1 + ".Yaw");
 		    
 		    Float pitch = spitch.floatValue();
 		    Float yaw = syaw.floatValue();
 			
 		    Location teleportloc = new Location(player.getWorld(), sx, sy, sz, yaw, pitch);
			if((player.hasPermission("lockdown.immune"))){
 				event.setRespawnLocation(teleportloc);
 			}
 		}
 	}
 	
 	public void reloadConfiguration(){
 		reloadConfig();
 	}
 	
 	/*public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
 		if(cmd.getName().equalsIgnoreCase("testplugin")){ // If the player typed /basic then do the following...
 			
 			sender.sendMessage("Hello World");
 			return true;
 		}
 		 //If this has happened the function will return true. 
 	        // If this hasn't happened the a value of false will be returned.
 		return false; 
 	}*/
 	
 	@Override
 	public void onDisable(){
 		getLogger().info("Lockdown has been disabled");
 	}
 }
