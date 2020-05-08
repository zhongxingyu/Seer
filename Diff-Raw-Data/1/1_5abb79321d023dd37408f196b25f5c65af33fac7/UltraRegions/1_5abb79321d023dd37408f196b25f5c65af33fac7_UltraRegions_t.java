 package com.prosicraft.ultraregions;
 
 import com.prosicraft.ultraregions.util.MConfiguration;
 import com.prosicraft.ultraregions.util.MLog;
 import com.sk89q.worldedit.Vector;
 import com.sk89q.worldedit.bukkit.WorldEditPlugin;
 import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  * UltraRegions main class
  */
 public class UltraRegions extends JavaPlugin
 {
 
 	private PluginDescriptionFile pdfFile;
 	public URListener thelistener;
 	public WorldEditInterface we;
 	public List<URegion> regions			= new ArrayList<>();
 	public List<URegion> autoassign			= new ArrayList<>();
 	public List<UWorld> worlds			= new ArrayList<>();
 	public Map<String, Boolean> notifications	= new HashMap<>();
 	public int claimHeight				= 20;
 	public MConfiguration config			= null;
 	private boolean freshconfig			= false;
 
 	/**
 	 * Enable this Plugin
 	 */
 	@Override
 	public void onEnable()
 	{
 		pdfFile = getDescription();
 
 		getServer().getPluginManager().registerEvents( thelistener = new URListener( this ), this );
 
 		we = thelistener.we;
 
 		initConfig();
 		if( freshconfig ) // freshconfig will be true when config is empty
 			save();
 		else
 		{
 			load();
 			save();
 		}
 
 		// Load not inited worlds
 		MLog.i( "Load not inited worlds..." );
 		for( World itWorld : getServer().getWorlds() )
 		{
 			boolean foundWorld = false;
 			for( UWorld world : worlds )
 			{
 				if( world.getName().equalsIgnoreCase( itWorld.getName() ) )
 				{
 					foundWorld = true;
 					break;
 				}
 			}
 
 			if( !foundWorld )
 			{
 				UWorld world = new UWorld();
 				world.setName( itWorld.getName() );
 				world.setGameMode( false );
 				worlds.add( world );
 			}
 		}
 		save();
 
 		// Load notifications
 		for( int i = 0; i < getServer().getOnlinePlayers().length; i++ )
 		{
 			if( !notifications.containsKey( getServer().getOnlinePlayers()[i].getName() ) )
 			{
 				notifications.put( getServer().getOnlinePlayers()[i].getName(), Boolean.TRUE );
 			}
 		}
 
 		// Try to Hook into WorldEdit
 		tryHookWorldEdit();
 
 		// Print startup info
 		MLog.i( "Loading version " + pdfFile.getVersion() );
 		MLog.w( "THIS VERSION OF ULTRAREGIONS MIGHT BE BUGGY!" );
 	}
 
 	/**
 	 * Disable this plugin
 	 */
 	@Override
 	public void onDisable()
 	{
 		MLog.i( "Disabled UltraRegions" );
 	}
 
 	public void load()
 	{
 		int cnt = 0;
 
 		config.load();
 
 		for( String s : config.getKeys( "notifications" ) )
 		{
 			notifications.put( s, config.getBoolean( "notifications." + s, true ) );
 		}
 
 		for( String s : config.getKeys( "worlds" ) )
 		{
 			UWorld world = new UWorld();
 			world.setName( s );
 			world.setGameMode( config.getBoolean( "worlds." + s + ".gamemode", false ) );
 			world.setDefaultPlotGamemode( config.getBoolean( "worlds." + s + ".defaultPlotGamemode", true ) );
 			world.setGlobalBuild( config.getBoolean( "worlds." + s + ".enableGlobalBuild", false ) );
                         world.autoAssignCommand = config.getString( "worlds." + s + ".autoAssignCommand", "givemeaplot" );
			worlds.add( world );
 		}
 
 		for( String s : config.getKeys( "regions" ) )
 		{
 			CuboidSelection cs = new CuboidSelection( getServer().getWorld( config.getString( "regions." + s + ".world" ) ),
 				new Vector( config.getInt( "regions." + s + ".v1.x", 0 ), config.getInt( "regions." + s + ".v1.y", 0 ), config.getInt( "regions." + s + ".v1.z", 0 ) ),
 				new Vector( config.getInt( "regions." + s + ".v2.x", 0 ), config.getInt( "regions." + s + ".v2.y", 0 ), config.getInt( "regions." + s + ".v2.z", 0 ) ) );
 			URegion tr = new URegion( cs, s, config.getBoolean( "regions." + s + ".gamemode", false ), config.getString( "regions." + s + ".greeting" ), config.getString( "regions." + s + ".farewell" ) );
 			tr.owner = config.getString( "regions." + s + ".owner" );
 			tr.spawn = new Location( getServer().getWorld( config.getString( "regions." + s + ".world" ) ), config.getInt( "regions." + s + ".spawn.x", 0 ), config.getInt( "regions." + s + ".spawn.y", 0 ), config.getInt( "regions." + s + ".spawn.z", 0 ) );
 			tr.farewellOthers = config.getString( "regions." + s + ".farewellOthers", "&7Du verlaesst das Grundstueck von " + tr.owner );
 			tr.greetingOthers = config.getString( "regions." + s + ".greetingOthers", "&7Du betrittst das Grundstueck von " + tr.owner );
 			regions.add( tr );
 			cnt++;
 		}
 
 		for( String s : config.getKeys( "autoassign" ) )
 		{
 			CuboidSelection cs = new CuboidSelection( getServer().getWorld( config.getString( "autoassign." + s + ".world" ) ),
 				new Vector( config.getInt( "autoassign." + s + ".v1.x", 0 ), config.getInt( "autoassign." + s + ".v1.y", 0 ), config.getInt( "autoassign." + s + ".v1.z", 0 ) ),
 				new Vector( config.getInt( "autoassign." + s + ".v2.x", 0 ), config.getInt( "autoassign." + s + ".v2.y", 0 ), config.getInt( "autoassign." + s + ".v2.z", 0 ) ) );
 			URegion tr = new URegion( cs, s, config.getBoolean( "autoassign." + s + ".gamemode", false ), config.getString( "autoassign." + s + ".greeting" ), config.getString( "autoassign." + s + ".farewell" ) );
 			tr.owner = config.getString( "autoassign." + s + ".owner" );
 			tr.spawn = new Location( getServer().getWorld( config.getString( "autoassign." + s + ".world" ) ), config.getInt( "autoassign." + s + ".spawn.x", 0 ), config.getInt( "autoassign." + s + ".spawn.y", 0 ), config.getInt( "autoassign." + s + ".spawn.z", 0 ) );
 			tr.farewellOthers = config.getString( "autoassign." + s + ".farewellOthers", "&7Du verlaesst das Grundstueck von " + tr.owner );
 			tr.greetingOthers = config.getString( "autoassign." + s + ".greetingOthers", "&7Du betrittst das Grundstueck von " + tr.owner );
 			autoassign.add( tr );
 			cnt++;
 		}
 		MLog.i( cnt + " Plot(s) loaded" );
 	}
 
 	public void save()
 	{
 		int cnt = 0;
 		config.clear();
 
 		for( String s : notifications.keySet() )
 		{
 			config.set( "notifications." + s, notifications.get( s ) );
 		}
 
 		for( UWorld world : worlds )
 		{
 			config.set( "worlds." + world.getName() + ".gamemode", world.getGameModeBoolean() );
 			config.set( "worlds." + world.getName() + ".defaultPlotGamemode", world.getDefaultPlotGamemode() );
 			config.set( "worlds." + world.getName() + ".enableGlobalBuild", world.isGlobalBuild() );
                         config.set( "worlds." + world.getName() + ".autoAssignCommand", world.autoAssignCommand );
 		}
 
 		for( URegion r : regions )
 		{
 			String s = r.name;
 
 			if( r.sel == null )
 				continue;
 			if( r.sel.getWorld() == null )
 				continue;
 
 			config.set( "regions." + s + ".owner", r.owner );
 			config.set( "regions." + s + ".gamemode", r.gamemode );
 			config.set( "regions." + s + ".greeting", r.greet );
 			config.set( "regions." + s + ".greetingOthers", r.greetingOthers );
 			config.set( "regions." + s + ".farewell", r.farewell );
 			config.set( "regions." + s + ".farewellOthers", r.farewellOthers );
 			config.set( "regions." + s + ".v1.x", r.sel.getMinimumPoint().getBlockX() );
 			config.set( "regions." + s + ".v1.y", r.sel.getMinimumPoint().getBlockY() );
 			config.set( "regions." + s + ".v1.z", r.sel.getMinimumPoint().getBlockZ() );
 			config.set( "regions." + s + ".v2.x", r.sel.getMaximumPoint().getBlockX() );
 			config.set( "regions." + s + ".v2.y", r.sel.getMaximumPoint().getBlockY() );
 			config.set( "regions." + s + ".v2.z", r.sel.getMaximumPoint().getBlockZ() );
 			config.set( "regions." + s + ".world", r.sel.getWorld().getName() );
 			try
 			{
 				config.set( "regions." + s + ".spawn.x", r.spawn.getBlockX() );
 				config.set( "regions." + s + ".spawn.y", r.spawn.getBlockY() );
 				config.set( "regions." + s + ".spawn.z", r.spawn.getBlockZ() );
 			}
 			catch( Exception ex )
 			{
 			}
 
 			cnt++;
 		}
 		for( URegion r : autoassign )
 		{
 			String s = r.name;
 			config.set( "autoassign." + s + ".owner", r.owner );
 			config.set( "autoassign." + s + ".gamemode", r.gamemode );
 			config.set( "autoassign." + s + ".greeting", r.greet );
 			config.set( "autoassign." + s + ".greetingOthers", r.greetingOthers );
 			config.set( "autoassign." + s + ".farewell", r.farewell );
 			config.set( "autoassign." + s + ".farewellOthers", r.farewellOthers );
 			config.set( "autoassign." + s + ".v1.x", r.sel.getMinimumPoint().getBlockX() );
 			config.set( "autoassign." + s + ".v1.y", r.sel.getMinimumPoint().getBlockY() );
 			config.set( "autoassign." + s + ".v1.z", r.sel.getMinimumPoint().getBlockZ() );
 			config.set( "autoassign." + s + ".v2.x", r.sel.getMaximumPoint().getBlockX() );
 			config.set( "autoassign." + s + ".v2.y", r.sel.getMaximumPoint().getBlockY() );
 			config.set( "autoassign." + s + ".v2.z", r.sel.getMaximumPoint().getBlockZ() );
 			config.set( "autoassign." + s + ".world", r.sel.getWorld().getName() );
 			try
 			{
 				config.set( "autoassign." + s + ".spawn.x", r.spawn.getBlockX() );
 				config.set( "autoassign." + s + ".spawn.y", r.spawn.getBlockY() );
 				config.set( "autoassign." + s + ".spawn.z", r.spawn.getBlockZ() );
 			}
 			catch( Exception ex )
 			{
 			}
 
 			cnt++;
 		}
 		config.save();
 		MLog.i( "Plots saved" );
 	}
 
 	public void initConfig()
 	{
 
 		if( this.config != null )
 			return;
 
 		if( !this.getDataFolder().exists() && !getDataFolder().mkdirs() )
 			MLog.e( "Can't create missing configuration Folder for UltraRegions" );
 
 		File cf = new File( this.getDataFolder(), "config.yml" );
 
 		if( !cf.exists() )
 		{
 			try
 			{
 				MLog.w( "Configuration File doesn't exist. Trying to recreate it..." );
 				if( !cf.createNewFile() || !cf.exists() )
 				{
 					MLog.e( "Placement of Plugin might be wrong or has no Permissions to access configuration file." );
 				}
 				freshconfig = true;
 			}
 			catch( IOException iex )
 			{
 				MLog.e( "Can't create unexisting configuration file" );
 			}
 		}
 
 		config = new MConfiguration( YamlConfiguration.loadConfiguration( cf ), cf );
 
 		// Initialize DataTable
 		Map<String, MConfiguration.DataType> dt = new HashMap<>();
 
 		dt.put( "fail", MConfiguration.DataType.DATATYPE_STRING );
 
 		config.setDataTypeTable( dt );
 
 		config.load();
 
 	}
 
 	@Override
 	public boolean onCommand( CommandSender sender, Command cmd, String commandLabel, String[] args )
 	{
 
 		if( sender instanceof Player )
 		{
 
 			Player p = ( Player ) sender;
 
 			if( cmd.getLabel().equalsIgnoreCase( "ultraregions" ) )
 			{
 				if( we == null )
 				{
 					MLog.w( "WorldEdited not hooked. Try to hook now..." );
 					tryHookWorldEdit();
 				}
 
 				if( args.length == 0 )
 				{
 					sendHelp( p );
 					return true;
 				}
 				else if( args.length == 1 )
 				{
 					if( args[0].equalsIgnoreCase( "list" ) )
 					{
 						p.sendMessage( ChatColor.GRAY + "Avaiable UltraRegions:" );
 						for( URegion reg : regions )
 							p.sendMessage( ChatColor.GOLD + reg.name + ChatColor.GRAY + " with gamemode: " + ChatColor.AQUA + ( reg.gamemode ? "CREATIVE" : "SURVIVAL" ) );
 						return true;
 					}
 					else if( args[0].equalsIgnoreCase( "reload" ) )
 					{
 						if( !p.hasPermission( "ultraregions.reload" ) )
 						{
 							p.sendMessage( ChatColor.RED + "Er... what's this command?" );
 							return true;
 						}
 						load();
 						MLog.s( "Reloaded UR Configuration" );
 						p.sendMessage( ChatColor.GREEN + "Reloaded UR Configuration!" );
 						return true;
 					}
 				}
 				if( args.length == 2 && args[0].equalsIgnoreCase( "setcommand" ) )
 				{
 					if( !p.hasPermission( "ultraregions.setcommand" ) )
 					{
 						p.sendMessage( ChatColor.RED + "Er... what's this command?" );
 						return true;
 					}
 
                                         boolean foundWorld = false;
                                         String worldName = p.getWorld().getName();
                                         for( UWorld world : worlds )
                                         {
                                             if( world.name.equalsIgnoreCase( worldName ) )
                                             {
                                                 world.autoAssignCommand = args[1];
                                                 foundWorld = true;
                                                 break;
                                             }
                                         }
 
                                         if( !foundWorld )
                                         {
                                             p.sendMessage( ChatColor.RED + "Worlds havent been loaded properly!" );
                                             return true;
                                         }
                                         save();
 
 					p.sendMessage( "Set new autoAssignCommand for world " + worldName );
 					save();
 					return true;
 				}
 				else if( args.length == 2 && args[0].equalsIgnoreCase( "config") )
 				{
 					if( !p.hasPermission( "ultraregions.config" ) )
 					{
 						p.sendMessage( ChatColor.RED + "Er... what's this command?" );
 						return true;
 					}
 
 					String nodeArg = args[1];
 					p.sendMessage( ChatColor.DARK_GRAY + "Value of node '" + ChatColor.GRAY + nodeArg + ChatColor.DARK_GRAY + "': " + ChatColor.AQUA
 						+ config.getValueAsString( nodeArg ) );
 					return true;
 				}
 				if( args.length == 2 )
 				{
 					if( args[0].equalsIgnoreCase( "create" ) )
 					{
 						if( !we.isRegionSelected( p ) )
 						{
 							p.sendMessage( ChatColor.RED + "Please select a region with wand tool first." );
 							return true;
 						}
 						if( !p.hasPermission( "ultraregions.create" ) )
 						{
 							p.sendMessage( ChatColor.RED + "Er... what's this command?" );
 							return true;
 						}
 						URegion ur = new URegion( we.getRegion( p ), args[1], false, "You entered the region.", "You left the region." );
 						regions.add( ur );
 						p.sendMessage( ChatColor.GREEN + "Added region successfully." );
 						save();
 						return true;
 					}
 					else if( args[0].equalsIgnoreCase( "delete" ) )
 					{
 						if( !p.hasPermission( "ultraregions.delete" ) )
 						{
 							p.sendMessage( ChatColor.RED + "Er... what's this command?" );
 							return true;
 						}
 						for( URegion reg : regions )
 						{
 							if( reg.name.equalsIgnoreCase( args[1] ) )
 							{
 								regions.remove( reg );
 								p.sendMessage( ChatColor.GREEN + "Deleted region successfully." );
 								save();
 								return true;
 							}
 						}
 						p.sendMessage( ChatColor.RED + "There's no region called '" + args[1] + "'." );
 						return true;
 					}
 					else if( args[0].equalsIgnoreCase( "setworldmode" ) )
 					{
 						if( !p.hasPermission( "ultraregions.changeworldgamemode" ) )
 						{
 							p.sendMessage( ChatColor.RED + "Er... what's this command?" );
 							return true;
 						}
 
 						boolean gm;
 						if( args[1].equalsIgnoreCase( "survival" ) )
 							gm = false;
 						else if( args[1].equalsIgnoreCase( "creative" ) )
 							gm = true;
 						else
 						{
 							p.sendMessage( ChatColor.RED + "There is no gamemode '" + args[1] + "'!" );
 							return true;
 						}
 
 						for( UWorld world : worlds )
 						{
 							if( world.getName().equalsIgnoreCase( p.getWorld().getName() ) )
 							{
 								world.setGameMode( gm );
 								break;
 							}
 						}
 						save();
 						p.sendMessage( ChatColor.DARK_GRAY + "Set gamemode of world " + ChatColor.AQUA + p.getWorld().getName() +
 							ChatColor.DARK_GRAY + " to " + ChatColor.GREEN + args[1] );
 						return true;
 					}
 					else if( args[0].equalsIgnoreCase( "setworldglobalbuild" ) )
 					{
 						if( !p.hasPermission( "ultraregions.changeworldglobalbuild" ) )
 						{
 							p.sendMessage( ChatColor.RED + "Er... what's this command?" );
 							return true;
 						}
 
 						boolean val;
 						if( args[1].equalsIgnoreCase( "true" ) )
 							val = true;
 						else if( args[1].equalsIgnoreCase( "false" ) )
 							val = false;
 						else
 						{
 							p.sendMessage( ChatColor.RED + "Possible values: false, true" );
 							return true;
 						}
 
 						for( UWorld world : worlds )
 						{
 							if( world.getName().equalsIgnoreCase( p.getWorld().getName() ) )
 							{
 								world.setGlobalBuild( val );
 								break;
 							}
 						}
 						save();
 						p.sendMessage( ChatColor.DARK_GRAY + "Set globalBuild of world " + ChatColor.AQUA + p.getWorld().getName() +
 							ChatColor.DARK_GRAY + " to " + ChatColor.GREEN + args[1] );
 						return true;
 					}
 				}
 				else if( args.length == 3 && args[0].equalsIgnoreCase( "config" ) )
 				{
 					if( !p.hasPermission( "ultraregions.config" ) )
 					{
 						p.sendMessage( ChatColor.RED + "Er... what's this command?" );
 						return true;
 					}
 
 					String nodeArg = args[0];
 					String valueArg = args[1];
 
 					try
 					{
 						int val = Integer.parseInt( valueArg );
 						config.set( nodeArg, val );
 						config.save();
 						p.sendMessage( "Set digit value " + ChatColor.AQUA + valueArg + ChatColor.GREEN + " for node " + ChatColor.AQUA + nodeArg + ChatColor.GREEN + "." );
 					}
 					catch( NumberFormatException ex )
 					{
 						if( valueArg.equalsIgnoreCase( "false" ) )
 						{
 							config.set( nodeArg, false );
 							config.save();
 							p.sendMessage( "Set boolean value " + ChatColor.AQUA + valueArg + ChatColor.GREEN + " for node " + ChatColor.AQUA + nodeArg + ChatColor.GREEN + "." );
 						}
 						else if( valueArg.equalsIgnoreCase( "true" ) )
 						{
 							config.set( nodeArg, true );
 							config.save();
 							p.sendMessage( "Set boolean value " + ChatColor.AQUA + valueArg + ChatColor.GREEN + " for node " + ChatColor.AQUA + nodeArg + ChatColor.GREEN + "." );
 						}
 						else
 						{
 							config.set( nodeArg, valueArg );
 							config.save();
 							p.sendMessage( "Set non-digit value " + ChatColor.AQUA + valueArg + ChatColor.GREEN + " for node " + ChatColor.AQUA + nodeArg + ChatColor.GREEN + "." );
 						}
 					}
 					return true;
 				}
 				else if( args.length == 3 && args[0].equalsIgnoreCase( "gamemode" ) )
 				{
 					if( !p.hasPermission( "ultraregions.gamemode" ) )
 					{
 						p.sendMessage( ChatColor.RED + "Er... what's this command?" );
 						return true;
 					}
 					for( URegion reg : regions )
 					{
 						if( reg.name.equalsIgnoreCase( args[1] ) )
 						{
 							if( args[2].equalsIgnoreCase( "creative" ) )
 								reg.gamemode = true;
 							else if( args[2].equalsIgnoreCase( "survival" ) )
 								reg.gamemode = false;
 							else
 							{
 								p.sendMessage( ChatColor.RED + "Unknown statement: '" + args[2] + "'" );
 								return true;
 							}
 							p.sendMessage( ChatColor.GREEN + "Changed Gamemode for that region." );
 							save();
 							return true;
 						}
 					}
 					p.sendMessage( ChatColor.RED + "There's no region called '" + args[1] + "'." );
 				}
 				else if( args.length >= 3 && args[0].equalsIgnoreCase( "farewell" ) )
 				{
 					if( !p.hasPermission( "ultraregions.farewell" ) )
 					{
 						p.sendMessage( ChatColor.RED + "Er... what's this command?" );
 						return true;
 					}
 					for( URegion reg : regions )
 					{
 						if( reg.name.equalsIgnoreCase( args[1] ) )
 						{
 							String txt = "";
 							for( int i = 2; i < args.length; i++ )
 								txt += args[i] + " ";
 							reg.farewell = txt.trim();
 							save();
 							p.sendMessage( ChatColor.GREEN + "Changed farewell message for that region." );
 							return true;
 						}
 					}
 					p.sendMessage( ChatColor.RED + "There's no region called '" + args[1] + "'." );
 					return true;
 				}
 				else if( args.length >= 3 && args[0].equalsIgnoreCase( "greet" ) )
 				{
 					if( !p.hasPermission( "ultraregions.greet" ) )
 					{
 						p.sendMessage( ChatColor.RED + "Er... what's this command?" );
 						return true;
 					}
 					for( URegion reg : regions )
 					{
 						if( reg.name.equalsIgnoreCase( args[1] ) )
 						{
 							String txt = "";
 							for( int i = 2; i < args.length; i++ )
 								txt += args[i] + " ";
 							reg.greet = txt.trim();
 							save();
 							p.sendMessage( ChatColor.GREEN + "Changed greeting message for that region to '" + reg.greet + "'" );
 							return true;
 						}
 					}
 					p.sendMessage( ChatColor.RED + "There's no region called '" + args[1] + "'." );
 					return true;
 				}
 				else if( args.length == 2 && args[0].equalsIgnoreCase( "claim" ) )
 				{
 					if( !p.hasPermission( "ultraregions.claim" ) )
 					{
 						p.sendMessage( ChatColor.RED + "Er... what's this command?" );
 						return true;
 					}
 					for( URegion reg : regions )
 					{
 						if( reg.name.equalsIgnoreCase( args[1] ) )
 						{
 							if( reg.owner.equalsIgnoreCase( p.getName() ) )
 							{
 								p.sendMessage( ChatColor.RED + "You already claimed that region." );
 								return true;
 							}
 							if( !reg.owner.isEmpty() && !p.hasPermission( "ultraregion.claim.fromother" ) && !p.isOp() )
 							{
 								p.sendMessage( ChatColor.RED + "This is not your region." );
 								return true;
 							}
 							reg.owner = p.getName();
 							save();
 							p.sendMessage( ChatColor.GREEN + "Claimed region successfully." );
 							return true;
 						}
 					}
 				}
 				else if( args.length == 3 && args[0].equalsIgnoreCase( "claim" ) )
 				{
 					if( !p.hasPermission( "ultraregions.claim.forother" ) )
 					{
 						p.sendMessage( ChatColor.RED + "Er... what's this command?" );
 						return true;
 					}
 					String theU = args[2];
 					for( URegion reg : regions )
 					{
 						if( reg.name.equalsIgnoreCase( args[1] ) )
 						{
 							if( reg.owner.equalsIgnoreCase( theU ) )
 							{
 								p.sendMessage( ChatColor.RED + "You already claimed that region for player '" + theU + "'." );
 								return true;
 							}
 							if( !( reg.owner.isEmpty() || reg.owner.equalsIgnoreCase( p.getName() ) ) && !p.hasPermission( "ultraregion.claim.fromother" ) && !p.isOp() )
 							{
 								p.sendMessage( ChatColor.RED + "This is not your region or not free." );
 								return true;
 							}
 							reg.owner = theU;
 							save();
 							p.sendMessage( ChatColor.GREEN + "Claimed region successfully for player '" + theU + "'." );
 							return true;
 						}
 					}
 				}
 				else
 				{
 					sendHelp( p );
 					p.sendMessage( ChatColor.RED + "There were too few arguments" );
 				}
 				return true;
 			}
 			else if( cmd.getLabel().equalsIgnoreCase( "ultraplots" ) )
 			{
 				if( we == null )
 				{
 					tryHookWorldEdit();
 				}
 				if( we == null )
 				{
 					p.sendMessage( ChatColor.RED + "Fatal Error: WorldEdit not hooked. Reload may succeed." );
 					return true;
 				}
 				if( args.length == 7 && args[0].equalsIgnoreCase( "gen" ) )
 				{
 
 					if( !p.hasPermission( "ultraplots.gen" ) )
 					{
 						p.sendMessage( ChatColor.RED + "Er... what's this command?" );
 						return true;
 					}
 					p.sendMessage( "Started Generation of plots from where you are standing" );
 					int w = Integer.parseInt( args[1] );
 					int h = Integer.parseInt( args[2] );
 					int pw = Integer.parseInt( args[3] );
 					int ph = Integer.parseInt( args[4] );
 					int sw = Integer.parseInt( args[5] );
 					int pp = Integer.parseInt( args[6] );
 					generatePlots( p, w, h, pw, ph, sw, pp );
 
 					save();
 					p.sendMessage( "Generated all plots." );
 
 					return true;
 
 				}
 				else if( args.length == 1 && args[0].equalsIgnoreCase( "claim" ) )
 				{
 
 					if( !p.hasPermission( "ultraplots.claim" ) )
 					{
 						p.sendMessage( ChatColor.RED + "Er... what's this command?" );
 						return true;
 					}
 					for( URegion ur : autoassign )
 					{
 						if( ur.isPlayerInside( p ) )
 						{
 							if( ur.owner.equalsIgnoreCase( p.getName() ) )
 							{
 								p.sendMessage( ChatColor.RED + "Dieses Grundstück gehört bereits dir." );
 								return true;
 							}
 							else if( !ur.owner.equalsIgnoreCase( "noone" ) )
 							{
 								p.sendMessage( ChatColor.RED + "Dieses Grundstück ist bereits vergeben." );
 								return true;
 							}
 							ur.owner = p.getName();
 							ur.greetingOthers = "&7Du betrittst das Grundstueck von " + ur.owner + ".";
 							ur.farewellOthers = "&7Du verlaesst das Grundstueck von " + ur.owner + ".";
 							ur.greet = "&6Du betrittst dein eigenes Grundstueck.";
 							ur.farewell = "&6Du verlaesst dein eigenes Grundstueck.";
 							p.sendMessage( ChatColor.GREEN + "Das Grundstück gehört nun dir." );
 							save();
 							return true;
 						}
 					}
 					p.sendMessage( ChatColor.RED + "Du stehst auf keinem definierten Grundstück." );
 					return true;
 
 				}
 				else if( args.length == 2 && args[0].equalsIgnoreCase( "claim" ) )
 				{
 
 					if( !p.hasPermission( "ultraplots.claimothers" ) )
 					{
 						p.sendMessage( ChatColor.RED + "Er... what's this command?" );
 						return true;
 					}
 					for( URegion ur : autoassign )
 					{
 						if( ur.isPlayerInside( p ) )
 						{
 							if( ur.owner.equalsIgnoreCase( args[1] ) )
 							{
 								p.sendMessage( ChatColor.RED + "Diese Grundstück gehört bereits '" + args[1] + "'." );
 								return true;
 							}
 							else if( !ur.owner.equalsIgnoreCase( "noone" ) )
 							{
 								p.sendMessage( ChatColor.RED + "Dieses Grundstück ist bereits vergeben. (an " + ur.owner + ")" );
 								return true;
 							}
 							ur.owner = args[1];
 							ur.greetingOthers = "&7Du betrittst das Grundstueck von " + ur.owner + ".";
 							ur.farewellOthers = "&7Du verlaesst das Grundstueck von " + ur.owner + ".";
 							ur.greet = "&6Du betrittst dein eigenes Grundstueck.";
 							ur.farewell = "&6Du verlaesst dein eigenes Grundstueck.";
 							p.sendMessage( ChatColor.GREEN + "Das Grundstück gehört nun '" + args[1] + "'." );
 							save();
 							return true;
 						}
 					}
 					p.sendMessage( ChatColor.RED + "Du stehst auf keinem definierten Grundstück." );
 					return true;
 
 				}
 				else if( args.length == 1 && args[0].equalsIgnoreCase( "unclaim" ) )
 				{
 
 					if( !p.hasPermission( "ultraplots.unclaim" ) )
 					{
 						p.sendMessage( ChatColor.RED + "Er... what's this command?" );
 						return true;
 					}
 					for( URegion ur : autoassign )
 					{
 						if( ur.isPlayerInside( p ) )
 						{
 							if( ur.owner.equalsIgnoreCase( "noone" ) )
 							{
 								p.sendMessage( ChatColor.RED + "Dieses Grundstück hat keinen Besitzer." );
 								return true;
 							}
 							ur.owner = "noone";
 							ur.greetingOthers = "&7Du betrittst ein Grundstueck. Es gehoert nicht dir.";
 							ur.farewellOthers = "&7Du verlaesst ein Grundstueck. Es gehoert nicht dir.";
 							ur.greet = "&7Du betrittst ein Grundstueck. Es gehoert nicht dir.";
 							ur.farewell = "&7Du verlaesst ein Grundstueck. Es gehoert nicht dir.";
 							p.sendMessage( ChatColor.GREEN + "Das Grundstück hat nun keinen Besitzer mehr." );
 							save();
 							return true;
 						}
 					}
 					p.sendMessage( ChatColor.RED + "Du stehst auf keinem definierten Grundstück." );
 					return true;
 
 				}
 				else if( args.length == 1 && args[0].equalsIgnoreCase( "define" ) )
 				{
 
 					if( !p.hasPermission( "ultraplots.define" ) )
 					{
 						p.sendMessage( ChatColor.RED + "Er... what's this command?" );
 						return true;
 					}
 					if( !we.isRegionSelected( p ) )
 					{
 						p.sendMessage( ChatColor.RED + "Please select a region with wand tool first." );
 						return true;
 					}
 					if( !we.getRegion( p ).contains( p.getLocation() ) )
 					{
 						p.sendMessage( ChatColor.RED + "You have to stand inside the plot to define the spawn." );
 						return true;
 					}
 					URegion ur = new URegion( we.getRegion( p ), "acregion" + autoassign.size(), true, "&7Du betrittst ein Grundstueck. Es gehoert nicht dir.", "&7Du verlaesst ein Grundstueck." );
 					ur.spawn = p.getLocation();
 					ur.owner = "noone";
 					ur.gamemode = getWorldDefaultPlotGamemode( we.getRegion( p ).getWorld().getName() );
 					this.autoassign.add( ur );
 					p.sendMessage( ChatColor.GREEN + "Das Grundstück wurde definiert." );
 					save();
 					return true;
 
 				}
 				else if( args.length >= 1 && args[0].equalsIgnoreCase( "gamemode" ) )
 				{
 
 					if( !p.hasPermission( "ultraplots.gamemode" ) )
 					{
 						p.sendMessage( ChatColor.RED + "Er... what's this command?" );
 						return true;
 					}
 
 					if( !( args[1].equalsIgnoreCase( "creative" ) || args[1].equalsIgnoreCase( "survival" ) ) )
 					{
 						p.sendMessage( ChatColor.RED + "Valid gamemodes are: " + ChatColor.GRAY + "create, survival" );
 						return true;
 					}
 
 					URegion r = null;
 					for( URegion ur : autoassign )
 						if( ur.sel.contains( p.getLocation() ) )
 							r = ur;
 
 					if( r != null )
 					{
 						r.gamemode = args[1].equalsIgnoreCase( "creative" );
 						p.sendMessage( ChatColor.GREEN + "Gamemode gesetzt." );
 						save();
 						return true;
 					}
 					else
 					{
 						p.sendMessage( ChatColor.RED + "Du stehst auf keinem definierten Grundstück." );
 						return true;
 					}
 				}
 				else if( args.length == 1 && args[0].equalsIgnoreCase( "delete" ) )
 				{
 
 					if( !p.hasPermission( "ultraplots.delete" ) )
 					{
 						p.sendMessage( ChatColor.RED + "Er... what's this command?" );
 						return true;
 					}
 					URegion r = null;
 					for( URegion ur : autoassign )
 						if( ur.sel.contains( p.getLocation() ) )
 							r = ur;
 					if( r != null )
 					{
 						autoassign.remove( r );
 						p.sendMessage( ChatColor.GREEN + "Das Grundstück wurde entfernt." );
 						save();
 						return true;
 					}
 					else
 					{
 						p.sendMessage( ChatColor.RED + "Du stehst auf keinem definierten Grundstück." );
 						return true;
 					}
 
 				}
 				else if( args.length == 1 && args[0].equalsIgnoreCase( "save" ) )
 				{
 
 					if( !p.isOp() )
 					{
 						return false;
 					}
 					save();
 					p.sendMessage( "Saved plots." );
 					return true;
 
 				}
 				else if( args.length == 1 && args[0].equalsIgnoreCase( "notify" ) )
 				{
 					if( !p.hasPermission( "ultraplots.notify" ) )
 					{
 						p.sendMessage( ChatColor.RED + "Er... what's this command?" );
 						return true;
 					}
 					Boolean b = notifications.get( p.getName() );
 					notifications.remove( p.getName() );
 					notifications.put( p.getName(), !b );
 					p.sendMessage( "Notifications now " + ( ( notifications.get( p.getName() ) ) ? "enabled." : "disabled." ) );
 					return true;
 				}
 			}
 
 		}
 
 		return false;
 	}
 
 	private void sendHelp( Player p )
 	{
 		p.sendMessage( " === UltraRegions Commands === " );
 		p.sendMessage( "/ur create <name> -- Creates a new ultra region" );
 		p.sendMessage( "/ur delete <name> -- Deletes a URegion" );
 		p.sendMessage( "/ur gamemode <name> <creative|survival> -- Defines a region gamemode" );
 		p.sendMessage( "/ur greet <name> <text> -- Text to show on region enter" );
 		p.sendMessage( "/ur farewell <text> -- Text to show on region exit" );
 		p.sendMessage( "/ur claim <name> [user] -- claims region for a user" );
 		p.sendMessage( "/plot gen <vertnum> <horiznum> <pl-width> <pl-length> <str-width> <pl-pad> -- generates a couple of plots with streets" );
 		p.sendMessage( "/plot claim [user] -- Claims region for user or you" );
 		p.sendMessage( "/plot gamemode <survival|creative> -- sets plot-gamemode" );
 		p.sendMessage( "/plot define -- defines unclaimed plot" );
 		p.sendMessage( "/plot unclaim -- delete claim" );
 		p.sendMessage( "/plot delete -- delete plot definition" );
 	}
 
 	/**
 	 * Try to hook into WorldEdit plugin
 	 */
 	private void tryHookWorldEdit()
 	{
 		for( Plugin plug : getServer().getPluginManager().getPlugins() )
 		{
 
 			if( plug != null && plug.getDescription().getName().equalsIgnoreCase( "WorldEdit" ) )
 			{
 
 				if( plug.getDescription().getName().equalsIgnoreCase( "WorldEdit" ) )
 				{
 
 					try
 					{
 						we = new WorldEditInterface( this, ( WorldEditPlugin ) plug );
 						MLog.i( "Hooked into WorldEdit" );
 					}
 					catch( NullPointerException nex )
 					{
 						MLog.e( "Can't bind to WorldEdit!" );
 					}
 					catch( Exception ex )
 					{
 						MLog.e( "Caught Fatal Error: " + ex.getMessage() );
 					}
 
 				}
 			}
 		}
 	}
 
 	private void generatePlots( Player p, int w, int h, int pw, int ph, int swp, int pp )
 	{
 
 		final int streetsEvery = 4;
 		final int streetsX = new Double( Math.floor( w / streetsEvery ) ).intValue();
 		final int streetsY = new Double( Math.floor( h / streetsEvery ) ).intValue();
 		if( ( swp % 2 ) == 0 )
 		{
 			p.sendMessage( ChatColor.RED + "Street size (sw) can't be round, sry fo that :D" );
 			p.sendMessage( ChatColor.RED + " --> Size now: " + ( swp + 1 ) );
 		}
 		final int sw = ( ( swp % 2 ) == 0 ) ? swp + 1 : swp;
 
 		// Generate Beginnings points
 		int cx = p.getLocation().getBlockX() - new Double( ( ( streetsX * sw ) + ( w * ( pw + pp ) ) ) * 0.5 ).intValue();
 		int cy = p.getLocation().getBlockZ() - new Double( ( ( streetsY * sw ) + ( h * ( ph + pp ) ) ) * 0.5 ).intValue();
 		final int startX = cx;
 		final int startY = cy;
 
 		int[] ys = new int[ streetsY ];
 		int ysc = 0;
 		int addx = ( ( pw % 2 == 0 ) ? 1 : 0 );
 		int addy = ( ( ph % 2 == 0 ) ? 1 : 0 );
 
 		for( int x1 = 1; x1 <= w; x1++ )
 		{
 			for( int y1 = 1; y1 <= h; y1++ )
 			{
 				generatePlot( cx + new Double( Math.floor( ( w / 2 ) ) ).intValue(),
 					cy + new Double( Math.floor( ( h / 2 ) ) ).intValue(),
 					pw, ph, p );
 				cy += ph + addy + pp;
 				if( ( y1 ) % streetsEvery == 0 )
 				{
 					// Skip already existing streets
 					if( ys != null )
 					{
 						boolean found = false;
 						for( int in : ys )
 						{
 							if( ( in ) == y1 )
 							{
 								found = true;
 							}
 						}
 						if( !found && ysc < streetsY )
 						{
 							// generate street
 							generateStreet( startX + ( ( streetsX * sw ) + ( w * ( pw + addx + pp ) ) ), cy + new Double( Math.floor( sw / 2 ) ).intValue() + addy + pp,
 								startX, cy + new Double( Math.floor( sw / 2 ) ).intValue() + addy + pp, sw, p );
 							ys[ysc] = y1;
 							ysc++;
 							cy += sw + pp;
 						}
 						else
 							cy += sw + pp;
 					}
 				}
 			}
 			cx += pw + addx + pp;
 			cy = startY;
 
 			if( ( x1 ) % streetsEvery == 0 )
 			{
 				p.getWorld().getHighestBlockAt( cx, cy ).setType( Material.COAL_ORE );
 				generateStreet( cx + new Double( Math.floor( ( sw ) / 2 ) ).intValue() + addx + pp, startY,
 					cx + new Double( Math.floor( ( sw ) / 2 ) ).intValue() + addx + pp, startY + ( ( streetsY * sw ) + ( h * ( ph + addy + pp ) ) ), sw, p );
 				cx += sw + pp;
 			}
 		}
 
 	}
 
 	private void generatePlot( int x, int y, int w, int h, Player p )
 	{
 		boolean flag1 = false;
 		boolean flag2 = false;
 		boolean flag3 = false;
 		boolean flag4 = false;
 		Vector p1 = null;
 		Vector p2 = null;
 
 		for( int xx = ( x - new Double( Math.floor( w / 2 ) ).intValue() ); xx <= ( x + new Double( Math.floor( w / 2 ) ).intValue() ); xx++ )
 		{
 			for( int yy = ( y - new Double( Math.floor( h / 2 ) ).intValue() ); yy <= ( y + new Double( Math.floor( h / 2 ) ).intValue() ); yy++ )
 			{
 				Block b = p.getWorld().getHighestBlockAt( xx, yy );
 
 				p.getWorld().getBlockAt( b.getX(), b.getY() - 1, b.getZ() ).setType( Material.STONE );
 
 				if( xx == ( x - new Double( Math.floor( w / 2 ) ).intValue() ) )
 					flag1 = true;
 				if( xx == ( x + new Double( Math.floor( w / 2 ) ).intValue() ) )
 					flag2 = true;
 				if( yy == ( y - new Double( Math.floor( h / 2 ) ).intValue() ) )
 					flag3 = true;
 				if( yy == ( y + new Double( Math.floor( h / 2 ) ).intValue() ) )
 					flag4 = true;
 
 				if( flag1 || flag2 || flag3 || flag4 )
 				{
 					b.setType( Material.FENCE );
 				}
 
 				if( flag1 && flag3 )
 				{
 					p1 = new Vector( b.getX(), b.getY() - 3, b.getZ() );
 				}
 				if( flag2 && flag4 )
 				{
 					p2 = new Vector( b.getX(), b.getY() + 20, b.getZ() );
 				}
 
 				flag1 = false;
 				flag2 = false;
 				flag3 = false;
 				flag4 = false;
 			}
 		}
 		URegion ur = new URegion( new CuboidSelection( p.getWorld(), p1, p2 ), "acregion" + autoassign.size(), true, "&7Du betrittst ein Grundstueck. Es gehoert nicht dir.", "&7Du verlaesst ein Grundstueck." );
 		ur.spawn = new org.bukkit.Location( p.getWorld(), p1.getX() + new Double( Math.floor( w / 2 ) ), p1.getY() + 6, p1.getZ() + new Double( Math.floor( h / 2 ) ) );
 		ur.owner = "noone";
 		this.autoassign.add( ur );
 	}
 
 	private void generateStreet( int xp1, int yp1, int xp2, int yp2, int sw, Player p )
 	{
 		int x1, x2, y1, y2;
 		if( xp1 > xp2 )
 		{
 			x1 = xp2;
 			x2 = xp1;
 		}
 		else
 		{
 			x1 = xp1;
 			x2 = xp2;
 		}
 		if( yp1 > yp2 )
 		{
 			y1 = yp2;
 			y2 = yp1;
 		}
 		else
 		{
 			y1 = yp1;
 			y2 = yp2;
 		}
 
 		if( x1 == x2 )
 		{
 			p.sendMessage( "Generating Street horizontally" );
 			for( int yy = y1; yy < y2; yy++ )
 			{
 
 				Material m;
 				if( yy % 5 != 0 )
 				{
 					m = Material.STEP;
 				}
 				else
 					m = Material.GLOWSTONE;
 
 				for( int xx = x1 - new Double( Math.floor( sw / 2 ) ).intValue(); xx <= x1 + new Double( Math.floor( sw / 2 ) ).intValue(); xx++ )
 				{
 					Block b2 = p.getWorld().getHighestBlockAt( xx, yy );
 					p.getWorld().getBlockAt( b2.getX(), b2.getY() - 1, b2.getZ() ).setType( Material.SMOOTH_BRICK );
 				}
 
 				Block b = p.getWorld().getHighestBlockAt( x1 - new Double( Math.floor( sw / 2 ) ).intValue(), yy );
 				p.getWorld().getBlockAt( b.getX(), b.getY(), b.getZ() ).setType( m );
 
 				b = p.getWorld().getHighestBlockAt( x1 + new Double( Math.floor( sw / 2 ) ).intValue(), yy );
 				p.getWorld().getBlockAt( b.getX(), b.getY(), b.getZ() ).setType( m );
 
 			}
 		}
 		if( y1 == y2 )
 		{
 			p.sendMessage( "Generating Street vertically" );
 			for( int yy = x1; yy < x2; yy++ )
 			{
 
 				Material m;
 				if( yy % 5 != 0 )
 				{
 					m = Material.STEP;
 				}
 				else
 					m = Material.GLOWSTONE;
 
 				for( int xx = y1 - new Double( Math.floor( sw / 2 ) ).intValue(); xx <= y1 + new Double( Math.floor( sw / 2 ) ).intValue(); xx++ )
 				{
 					Block b2 = p.getWorld().getHighestBlockAt( yy, xx );
 					p.getWorld().getBlockAt( b2.getX(), b2.getY() - 1, b2.getZ() ).setType( Material.SMOOTH_BRICK );
 				}
 
 				Block b = p.getWorld().getHighestBlockAt( yy, y1 - new Double( Math.floor( sw / 2 ) ).intValue() );
 				p.getWorld().getBlockAt( b.getX(), b.getY(), b.getZ() ).setType( m );
 
 				b = p.getWorld().getHighestBlockAt( yy, y1 + new Double( Math.floor( sw / 2 ) ).intValue() );
 				p.getWorld().getBlockAt( b.getX(), b.getY(), b.getZ() ).setType( m );
 
 			}
 		}
 	}
 
 	public boolean assignPlot( Player to )
 	{
 		// Check if player already has a plot in this world.
 		for( URegion tr : autoassign )
 		{
 			if( tr.owner.equalsIgnoreCase( to.getName() ) && tr.sel.getWorld().getName().equalsIgnoreCase( to.getWorld().getName() ) )
 			{
 				to.sendMessage( ChatColor.RED + "Du hast bereits ein Grundstueck. Bitte einen Supporter anfragen wenn du ein Zweites wuenscht." );
 				return true;
 			}
 		}
 
 		// Now find empty plot and assign it
 		for( URegion ur : autoassign )
 		{
 			if( ur.owner.equalsIgnoreCase( "noone" ) && ur.sel.getWorld().getName().equalsIgnoreCase( to.getWorld().getName() ) )
 			{
 				ur.owner = to.getName();
 				to.teleport( ur.spawn );
 				to.performCommand( "/sethome" );
 				ur.greetingOthers = "&7Du betrittst das Grundstueck von " + ur.owner + ".";
 				ur.farewellOthers = "&7Du verlaesst das Grundstueck von " + ur.owner + ".";
 				ur.greet = "&6Du betrittst dein eigenes Grundstueck.";
 				ur.farewell = "&6Du verlaesst dein eigenes Grundstueck.";
 				to.sendMessage( ChatColor.GRAY + "Das ist dein Grundstück. Viel Spaß beim Bauen ;)" );
 				save();
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Switches Creative Mode for given player on or off
 	 * @param p the player
 	 * @param state the state (true = on, false = off)
 	 */
 	public void setCreativeMode( Player p, boolean state )
 	{
 		if( p != null )
 		{
 			GameMode gm = (state) ? GameMode.CREATIVE : GameMode.SURVIVAL;
 			if( p.getGameMode() != gm )
 			{
 				p.setGameMode( gm );
 			}
 		}
 	}
 
 	/**
 	 * Gets the world gamemode the player is in
 	 * @param p the player
 	 */
 	public boolean getWorldGameMode( Player p )
 	{
 		String worldName = p.getWorld().getName();
 		for( UWorld itWorld : worlds )
 		{
 			if( itWorld.getName().equalsIgnoreCase( worldName ) )
 			{
 				return itWorld.getGameModeBoolean();
 			}
 		}
 
 		// if we reach this line no world was found matching this name
 		// .. so we gonna add a new one
 		MLog.i( "World '" + worldName + "' the Player '" + p.getName() + "' will be added to memory. New GameMode: SURVIVAL" );
 		UWorld world = new UWorld();
 		world.setName( worldName );
 		world.setGameMode( false );
 		worlds.add( world );
 		save();
 
 		return world.getGameModeBoolean();
 	}
 
 	/**
 	 * Gets the default plot gamemode
 	 * @param name worldname
 	 * @return true = creative, false = survival
 	 */
 	public boolean getWorldDefaultPlotGamemode( String name )
 	{
 		for( UWorld itWorld : worlds )
 		{
 			if( itWorld.getName().equalsIgnoreCase( name ) )
 				return itWorld.getDefaultPlotGamemode();
 		}
 
 		return true;
 	}
 
 	/**
 	 * Get whether global build is enabled for this wolrd
 	 * @param name Worldname
 	 * @return true if global build is enabled, false if not
 	 */
 	public boolean getWorldGlobalBuildEnabled( String name )
 	{
 		for( UWorld itWorld : worlds )
 		{
 			if( itWorld.getName().equalsIgnoreCase( name ) )
 				return itWorld.isGlobalBuild();
 		}
 		return false;
 	}
 }
