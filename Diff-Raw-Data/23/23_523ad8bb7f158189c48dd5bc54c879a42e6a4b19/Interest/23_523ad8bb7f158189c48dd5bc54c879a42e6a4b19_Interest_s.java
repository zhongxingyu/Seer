 package com.seiken.interest;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import me.taylorkelly.help.Help;
 
 import org.bukkit.Location;
 import org.bukkit.Server;
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.PluginCommand;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginLoader;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Interest extends JavaPlugin {
 	
 	public static final String VERSION_1_1 = "version1.1";
 	public static final String VERSION_1_2 = "version1.2";
 	public static final String DATA_FILE = "places.txt";
 	public static final String CONFIG_FILE = "config.txt";
 
     private final InterestPlayer player = new InterestPlayer( this );
     private final InterestVehicle vehicle = new InterestVehicle( this );
     private Places places;
     private Config config;
     
     private static final Logger log = Logger.getLogger("Minecraft");
     
     private Map<String, PlaceTree> placeTrees = new HashMap<String, PlaceTree>();
     private HashMap< Player, Place > current = new HashMap< Player, Place >();
     private HashMap< Player, Long > times = new HashMap< Player, Long >();
     
     private File dataFile;
     private File configFile;
 	private static Interest instance;
 
     public void onEnable()
     {
     	if(instance == null)
     		instance = this;
     	initFiles();
     	places = new Places( this );
     	config = new Config( this );
     	
     	updatePlaces();
         getServer().getPluginManager().registerEvent( Event.Type.PLAYER_JOIN,    player,  Priority.Normal, this );
         getServer().getPluginManager().registerEvent( Event.Type.PLAYER_QUIT,    player,  Priority.Normal, this );
         getServer().getPluginManager().registerEvent( Event.Type.PLAYER_MOVE,    player,  Priority.Normal, this );
         getServer().getPluginManager().registerEvent( Event.Type.VEHICLE_MOVE,   vehicle, Priority.Normal, this );
         
         log.log(Level.INFO, this.getDescription().getFullName() + " is enabled!");
         
         registerHelp();
     }
     
     public static Interest getInstance(){
     	return instance;
     }
     
     private void registerHelp() {
 		Plugin test = this.getServer().getPluginManager().getPlugin("Help");
 		if(test != null)
 		{
 			Help help = (Help) test;
 			
 			PluginCommand[] commands = new PluginCommand[4];
 			
 			commands[0] = this.getCommand("mark");
 			commands[1] = this.getCommand("unmark");
 			commands[2] = this.getCommand("who");
 			commands[3] = this.getCommand("nearest");
 			
 			for(PluginCommand pc : commands)
 			{
 				if(pc != null)
 				{
 					help.registerCommand(pc.getName(), pc.getDescription(), this, true);
 				}
 			}
 			
 		}
 	}
 
 	public World getFirstWorld()
     {
     	return this.getServer().getWorlds().get(0);
     }
     
     public World getWorld(String worldname)
     {
     	return this.getServer().getWorld(worldname);
     }
     
     
 
 
     private void initFiles() {
 		File folder = this.getDataFolder();
 		boolean result = true;
 		if(!folder.exists()){
 			if(!folder.mkdir()){
 				result = false;
 				log.log(Level.SEVERE, "[Interest] Could not create data folder!");
 			}
 		}
 		
 		dataFile = new File(folder.getPath() + File.separator + Interest.DATA_FILE);
 		log.log(Level.INFO, "[Interest] Data file: " + dataFile.getPath());
 		configFile = new File(folder.getPath() + File.separator + Interest.CONFIG_FILE);
 		log.log(Level.INFO, "[Interest] Config file: " + configFile.getPath());
 		try {
 			if( !dataFile.exists() )
 					dataFile.createNewFile();
 			if( !configFile.exists() )
 				configFile.createNewFile();
 		
 				
 			if ( !dataFile.canWrite() ) {
 				result = false;
 			}
 			if( !configFile.canRead()) {
 				result = false;
 			}
 		
 		} catch (IOException e) {
 			log.log(Level.SEVERE, "[Interest] Error creating data files: " + e.getMessage());
 		}
 		
 		if(!result)
 			log.log(Level.INFO, "[Interest] Failed to initialize data files!");
 		
 	}
     
     public File getDataFile()
     {
     	return this.dataFile;
     }
     
     public File getConfigFile()
     {
     	return this.configFile;
     }
 
 
 	public void onDisable()
     {
     	log.log(Level.INFO, this.getDescription().getFullName() + " is disabled!");
     }
     
     @Override
 	public boolean onCommand(CommandSender sender, Command cmd,
 			String commandLabel, String[] args) {
     	
     	boolean b = false;
     	
     	String commandName = cmd.getName().toLowerCase();
     	Player p = null;
     	if(sender instanceof Player)
     		p = (Player) sender;
     	
     	if ( ( commandName.equals( "who" ) && !this.getConfig().disableWho() ) || commandName.equals( "where" ) ) {
     		if ( args.length <= 0 )
     			this.sendWho( p );
     		else
     			this.sendWho( p, args[ 0 ] );
     		b = true;
     	}
     	
     	if ( commandName.equals( "nearest" ) ) {
     		this.sendNearest( p );
     		b = true;
     	}
     	
     	if ( commandName.equals( "mark" ) ) {
     		if ( args.length <= 0 )
     			this.markPlace( p, null, -1, -1, -1, -1 );
     		else {
     			String[] split = args;
     			int r = -1;
     			int x = -1;
     			int y = -1;
     			int z = -1;
     			boolean a = true;
     			int n = 0;
     			while ( a ) {
     				a = false;
     				if ( split[ n ].length() >= 3 && split[ n ].substring( 0, 2 ).equals( "r:" ) ) {
         				try {
         					r = Integer.parseInt( split[ n ].substring( 2 ) );
         					a = true;
         					n++;
         				}
         				catch ( NumberFormatException e ) {
         				}
     				}
     				if ( split[ n ].length() >= 3 && split[ n ].substring( 0, 2 ).equals( "x:" ) ) {
         				try {
         					x = Integer.parseInt( split[ n ].substring( 2 ) );
         					a = true;
         					n++;
         				}
         				catch ( NumberFormatException e ) {
         				}
     				}
     				if ( split[ n ].length() >= 3 && split[ n ].substring( 0, 2 ).equals( "y:" ) ) {
         				try {
         					y = Integer.parseInt( split[ n ].substring( 2 ) );
         					a = true;
         					n++;
         				}
         				catch ( NumberFormatException e ) {
         				}
     				}
     				if ( split[ n ].length() >= 3 && split[ n ].substring( 0, 2 ).equals( "z:" ) ) {
         				try {
         					z = Integer.parseInt( split[ n ].substring( 2 ) );
         					a = true;
         					n++;
         				}
         				catch ( NumberFormatException e ) {
         				}
     				}
     			}
     			
     			String newArgs = args[n];
     			for(int i = n+1; i < args.length; i++)
     			{
     				newArgs += " " + args[i];
     			}
     					
     			this.markPlace( p, Interest.safeMessage( newArgs.replaceAll( "##", "§" ) ), r, x, y, z );
     		}
     		b = true;
     	}
     	
     	if ( commandName.equals( "unmark" ) ) {
     		this.unmarkPlace( p );
     		b = true;
     	}
     	
     	return b;
 	}
 
 	public Config getConfig()
     {
     	return config;
     }
     
     public static String safeMessage( String message )
     {
     	String r = message;
     	boolean b = true;
     	while ( b ) {
     		b = false;
 	    	while ( r.length() >= 2 && r.charAt( r.length() - 2 ) == '§' ) {
 	    		r = r.substring( 0, r.length() - 2 );
 	    		b = true;
 	    	}
 	    	while ( r.length() >= 1 && r.charAt( r.length() - 1 ) == '§' ) {
 	    		r = r.substring( 0, r.length() - 1 );
 	    		b = true;
 	    	}
     	}
     	return r;
     }
 
     private Place nearestPlace( Player player )
     {
     	if ( placeTrees == null )
     		return null;
     	PlaceTree pt = placeTrees.get(player.getLocation().getWorld().getName());
     	if(pt == null){
     		updatePlaces();
     		pt = placeTrees.get(player.getLocation().getWorld().getName());
     	}
     	return pt.nearest( player.getLocation() );
     }
     
     public void updateCurrent( Player player )
     {
     	if ( times.containsKey( player ) && System.currentTimeMillis() - times.get( player ) < 8000 )
     		return;
     	
     	Place place = nearestPlace( player );
     	
 		Place old = null;
 		if ( current.containsKey( player ) )
 			old = current.get( player );
 		
 		if ( ( place != old || !current.containsKey( player ) ) && !( old != null && place != null && old.getName().equals( place.getName() ) ) ) {
 			if ( place == null ) {
 				if ( old != null || !config.leavingUsesArg() )
 					player.sendMessage( config.leaving( old != null ? old.getName() : "" ) );
 			}
 			else
 				player.sendMessage( config.entering( place.getName() ) );
 			times.put( player, System.currentTimeMillis() );
 		}
 		
 		current.put( player, place );
     }
     
     public void removeCurrent( Player player )
     {
     	current.remove( player );
     	times.remove( player );
     }
     
     public void sendWho( Player player )
     {
     	Player[] players = getServer().getOnlinePlayers();
     	player.sendMessage( config.whoHead( Integer.toString( players.length ) ) );
     	for ( Player p : players ) {
 			Place place = nearestPlace( p );
 		
 			if ( place == null )
 				player.sendMessage( config.whoLineNoPlace( p.getName() ) );
 			else
 				player.sendMessage( config.whoLinePlace( p.getName(), place.getName() ) );
     	}
     }
     
     public void sendWho( Player player, String who )
     {
     	boolean b = false;
     	Player[] players = getServer().getOnlinePlayers();
     	for ( Player p : players ) {
     		if ( p.getName().equalsIgnoreCase( who ) ) {
     			Location loc = p.getLocation();
     			Place place = nearestPlace( p );
     			
     			if ( place == null )
     				player.sendMessage( config.whoLineNoPlace( p.getName() ) );
     			else
     				player.sendMessage( config.whoLinePlace( p.getName(), place.getName() ) );
     			player.sendMessage( "§f[" + Integer.toString( loc.getBlockX() ) + ", " + Integer.toString( loc.getBlockY() ) + ", " + Integer.toString( loc.getBlockZ() ) + "]" );
     			b = true;
     		}
     	}
     	if ( !b ) {
     		player.sendMessage( "§fplayer not found!" );
     	}
     }
     
     public void sendNearest( Player player )
     {
     	Place nearest = nearestPlace( player );
     	if ( nearest != null )
     		player.sendMessage( nearest.getDesc() );
     }
     
     public void markPlace( Player player, String name, int radius, int xDist, int yDist, int zDist )
     {
     	if ( config.opsOnly() && !player.isOp() ) {
     		player.sendMessage( "§fops only!" );
     		return;
     	}
     	
     	Place nearest = nearestPlace( player );
     	
     	if ( nearest != null && nearest.distance( player.getLocation() ) < 100 ) {
     		player.sendMessage( "§ftoo close to " + nearest.getDesc() + "!" );
     		return;
     	}
     	
     	if ( name == null || name.trim().equals( "" ) ) {
     		player.sendMessage( "§fsupply a name!" );
     		return;
     	}
     	
     	Place mark = null;
     	if ( radius > 0 )
     		mark = new Place( player.getLocation(), radius, name );
     	else
     		mark = new Place( player.getLocation(), xDist, yDist, zDist, name );
     	
     	places.getPlaces().add( mark );
     	updatePlaces();
     	player.sendMessage( "§fmarked " + mark.getDesc() );
     	
     	for ( Player p : getServer().getOnlinePlayers() )
     		updateCurrent( p );
     }
     
     public void unmarkPlace( Player player )
     {
     	if ( config.opsOnly() && !player.isOp() ) {
     		player.sendMessage( "§fops only!" );
     		return;
     	}
     	
     	Place nearest = nearestPlace( player );
     	
     	if ( nearest == null ) {
     		player.sendMessage( "§fnothing to unmark!" );
     		return;
     	}
     	
     	places.getPlaces().remove( nearest );
     	updatePlaces();
     	player.sendMessage( "§funmarked " + nearest.getDesc() );
     	
     	for ( Player p : getServer().getOnlinePlayers() )
     		updateCurrent( p );
     }
     
     
     private void updatePlaces()
     {
     	
     	List<Place> places = this.places.getPlaces();
    	log.info(places.toString());
     	Map<String, List<Place>> worldToList = new HashMap<String, List<Place>>();
     	
     	for(Place p : places)
     	{
     		List<Place> l = worldToList.get(p.getWorldName());
     		if(l == null){
     			l = new ArrayList<Place>();
     			worldToList.put(p.getWorldName(),l);
     		}
     		l.add(p);
     	}
     	
     	for(World w : this.getServer().getWorlds())
     	{
     		log.info("[Interest] Tracking places for world: " + w.getName());
     		placeTrees.put(w.getName(), new PlaceTree(worldToList.get(w.getName())));
     	}
     	
     	this.places.updateData();
     }
     
 }
