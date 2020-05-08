 /*
  * UltraRegions Security Listener
  */
 package com.prosicraft.ultraregions;
 
 import com.prosicraft.ultraregions.util.MLog;
 import com.sk89q.worldedit.bukkit.WorldEditPlugin;
 import java.util.HashMap;
 import java.util.Map;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.hanging.HangingBreakByEntityEvent;
 import org.bukkit.event.hanging.HangingPlaceEvent;
 import org.bukkit.event.player.PlayerBucketEmptyEvent;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 import org.bukkit.event.player.PlayerInteractEntityEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.server.PluginEnableEvent;
 import org.bukkit.plugin.Plugin;
 
 /**
  *
  * @author prosicraft
  */
 public class URListener implements Listener
 {
 
 	public UltraRegions ur = null;
 	public WorldEditInterface we = null;
 	public Map<Player, GameMode> gmbackup = new HashMap<>();
 	public Map<Player, GameMode> gmbackupa = new HashMap<>();
 	public int savecount = 0;
 
 	public URListener( UltraRegions prnt )
 	{
 		this.ur = prnt;
 	}
 
 	@EventHandler( priority = EventPriority.NORMAL )
 	public void onJoin( PlayerJoinEvent event )
 	{
 		if( !ur.notifications.containsKey( event.getPlayer().getName() ) )
 			ur.notifications.put( event.getPlayer().getName(), Boolean.TRUE );
 		if( savecount > 3 )
 		{
 			ur.save();
 		}
 		savecount++;
 	}
 
 	/**
 	 * Try to hook into WorldEdit here
 	 * @param event
 	 */
 	@EventHandler( priority = EventPriority.NORMAL )
 	public void onPluginEnable( PluginEnableEvent event )
 	{
 		Plugin plug = event.getPlugin();
 
 		if( plug != null && plug.getDescription().getName().equalsIgnoreCase( "WorldEdit" ) )
 		{
 
 			if( plug.getDescription().getName().equalsIgnoreCase( "WorldEdit" ) )
 			{
 
 				try
 				{
 					we = new WorldEditInterface( ur, ( WorldEditPlugin ) plug );
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
 
 	/**
 	 * Check if event is permitted
 	 * @param p The Player invoking this event
 	 * @param l The location of modified entity
 	 * @return true if player is permitted to do so
 	 */
 	public boolean isPermitted( Player p, Location l )
 	{
 		boolean blockInRegion = false;
 		for( URegion reg : ur.regions )
 		{
 			if( !reg.sel.contains( l ) )
 				continue;
 			blockInRegion = true;
 			if( !reg.owner.equalsIgnoreCase( p.getName() ) && !reg.owner.isEmpty() )
 			{
 				if( !p.hasPermission( "ultraregions.build.others" ) )
 					return false;
 			}
 		}
 		for( URegion reg : ur.autoassign )
 		{
 			if( !reg.sel.contains( l ) )
 				continue;
 			blockInRegion = true;
 			if( !reg.owner.equalsIgnoreCase( p.getName() ) && !reg.owner.isEmpty() )
 			{
 				if( !p.hasPermission( "ultraregions.build.others" ) )
 					return false;
 			}
 		}
 		if( !blockInRegion )
 		{
 			if( !p.hasPermission( "ultraregions.build.everywhere" ) )
 				return false;
 		}
 
 		return true;
 	}
 
 	/**
 	 * Handle Block Placing Event
 	 * @param e the event
 	 */
 	@EventHandler( priority = EventPriority.LOWEST )
 	public void onBlockPlace( BlockPlaceEvent e )
 	{
 		if( !isPermitted( e.getPlayer(), e.getBlock().getLocation() ) )
 			e.setCancelled( true );
 	}
 
 	/**
 	 * Handle Block Breaking event
 	 * @param e
 	 */
 	@EventHandler( priority = EventPriority.LOWEST )
 	public void onBlockBreak( BlockBreakEvent e )
 	{
 		if( !isPermitted( e.getPlayer(), e.getBlock().getLocation() ) )
 			e.setCancelled( true );
 	}
 
 	/**
 	 * Handle Hanging (Painting and stuff) placment event
 	 * @param e
 	 */
 	@EventHandler( priority = EventPriority.LOWEST )
 	public void onHangingPlace( HangingPlaceEvent e )
 	{
 		if( e.getPlayer().getType() == EntityType.PLAYER )
 		{
 			if( !isPermitted( e.getPlayer(), e.getBlock().getLocation() ) )
 				e.setCancelled( true );
 		}
 	}
 
 	/**
 	 * Handle Hanging (Painting and stuff) breaking event
 	 * @param e
 	 */
 	@EventHandler( priority = EventPriority.LOWEST )
 	public void onHangingBreak( HangingBreakByEntityEvent e )
 	{
 		if( e.getRemover().getType() == EntityType.PLAYER )
 		{
 			if( !isPermitted( (Player)e.getRemover(), e.getEntity().getLocation() ) )
 				e.setCancelled( true );
 		}
 	}
 
 	/**
 	 * Handle bucket empty event
 	 */
 	@EventHandler( priority = EventPriority.LOWEST )
 	public void onPlayerBucketEmpty( PlayerBucketEmptyEvent e )
 	{
 		if( !isPermitted( e.getPlayer(), e.getBlockClicked().getLocation() ) )
 			e.setCancelled( true );
 	}
 
 	/**
 	 * Handle change of item-frames
 	 * @param e
 	 */
 	@EventHandler( priority = EventPriority.LOWEST )
 	public void onPlayerInteraction( PlayerInteractEntityEvent e )
 	{
 		if( e.getRightClicked().getType() == EntityType.ITEM_FRAME )
 		{
 			if( !isPermitted( (Player)e.getPlayer(), e.getRightClicked().getLocation() ) )
 				e.setCancelled( true );
 		}
 	}
 
 	@EventHandler( priority = EventPriority.LOWEST )
 	public void onPlayerMove( PlayerMoveEvent event )
 	{
 		for( URegion reg : ur.regions )
 		{
 			if( reg.sel.contains( event.getTo() ) && !reg.sel.contains( event.getFrom() ) )
 			{
 				if( reg.showMessages && ur.notifications.get( event.getPlayer().getName() ) )
 					event.getPlayer().sendMessage( MLog.real( reg.greet ) );
 				gmbackup.put( event.getPlayer(), event.getPlayer().getGameMode() );
 				if( reg.gamemode )
 					event.getPlayer().setGameMode( GameMode.CREATIVE );
 				else
 					event.getPlayer().setGameMode( GameMode.SURVIVAL );
 			}
 			else if( !reg.sel.contains( event.getTo() ) && reg.sel.contains( event.getFrom() ) )
 			{
 				if( reg.showMessages && ur.notifications.get( event.getPlayer().getName() ) )
 					event.getPlayer().sendMessage( MLog.real( reg.farewell ) );
 				event.getPlayer().setGameMode( gmbackup.get( event.getPlayer() ) );
 				gmbackup.remove( event.getPlayer() );
 			}
 			else if( reg.sel.contains( event.getTo() ) && reg.sel.contains( event.getFrom() ) )
 			{
 				if( gmbackup.get( event.getPlayer() ) == null )
 				{
 					if( reg.gamemode )
 						event.getPlayer().setGameMode( GameMode.CREATIVE );
 					else
 						event.getPlayer().setGameMode( GameMode.SURVIVAL );
 				}
 			}
 			/*else if ( !reg.sel.contains(event.getTo()) && !reg.sel.contains(event.getFrom()) ) {      // Nothing in there
 			 if (gmbackup.containsKey(event.getPlayer())) {
 			 event.getPlayer().setGameMode(gmbackup.get(event.getPlayer()));
 			 gmbackup.remove(event.getPlayer());
 			 } else {
 			 if (parent.global.gamemode)
 			 event.getPlayer().setGameMode(GameMode.CREATIVE);
 			 else
 			 event.getPlayer().setGameMode(GameMode.SURVIVAL);
 			 }
 			 }*/
 		}
 		boolean inRegion = false;
 		for( URegion reg : ur.autoassign )
 		{
 
 			if( reg.sel.contains( event.getTo() ) && !reg.sel.contains( event.getFrom() ) ) // Enter
 			{
 				inRegion = true;
 				if( reg.owner.equalsIgnoreCase( event.getPlayer().getName() ) || reg.owner.equalsIgnoreCase( "noone" ) )
 				{
 					if( reg.showMessages && ur.notifications.get( event.getPlayer().getName() ) )
 						event.getPlayer().sendMessage( MLog.real( reg.greet ) );
 				}
 				else
 				{
 					if( reg.showMessages && ur.notifications.get( event.getPlayer().getName() ) )
 						event.getPlayer().sendMessage( MLog.real( reg.greetingOthers ) );
 				}
 				gmbackupa.put( event.getPlayer(), event.getPlayer().getGameMode() );
 				if( reg.gamemode )
 					event.getPlayer().setGameMode( GameMode.CREATIVE );
 				else
 					event.getPlayer().setGameMode( GameMode.SURVIVAL );
 
 			}
 			else if( !reg.sel.contains( event.getTo() ) && reg.sel.contains( event.getFrom() ) )
 			{
 				inRegion = true;
 				if( reg.owner.equalsIgnoreCase( event.getPlayer().getName() ) || reg.owner.equalsIgnoreCase( "noone" ) )
 				{
 					if( reg.showMessages && ur.notifications.get( event.getPlayer().getName() ) )
 						event.getPlayer().sendMessage( MLog.real( reg.farewell ) );
 				}
 				else
 				{
 					if( reg.showMessages && ur.notifications.get( event.getPlayer().getName() ) )
 						event.getPlayer().sendMessage( MLog.real( reg.farewellOthers ) );
 				}
 				if( gmbackupa.get( event.getPlayer() ) != null )
 				{
 					event.getPlayer().setGameMode( gmbackupa.get( event.getPlayer() ) );
 					gmbackupa.remove( event.getPlayer() );
 				}
 			}
 			else if( reg.sel.contains( event.getTo() ) && reg.sel.contains( event.getFrom() ) )
 			{
 				inRegion = true;
 			}
 
 		}
 		if( !inRegion && !event.getPlayer().hasPermission( "ultraplots.keepgamemode" ) )
 		{
 			if( ur != null && ur.global != null && ur.global.gamemode && event.getPlayer().getGameMode() != GameMode.CREATIVE )
 			{
 				event.getPlayer().setGameMode( GameMode.CREATIVE );
 			}
 			else if( !ur.global.gamemode && event.getPlayer().getGameMode() == GameMode.CREATIVE )
 			{
 				event.getPlayer().setGameMode( GameMode.SURVIVAL );
 			}
 		}
 	}
 
 	@EventHandler( priority = EventPriority.NORMAL )
 	public void onPlayerQuit( PlayerQuitEvent event )
 	{
 		if( gmbackup.containsKey( event.getPlayer() ) )
 			event.getPlayer().setGameMode( gmbackup.get( event.getPlayer() ) );
 		if( gmbackupa.containsKey( event.getPlayer() ) )
 			event.getPlayer().setGameMode( gmbackupa.get( event.getPlayer() ) );
 	}
 
	@EventHandler( priority = EventPriority.HIGHEST )
 	public void onPlayerCommand( PlayerCommandPreprocessEvent e )
 	{
 		if( e.isCancelled() )
 			return;
 		if( e.getMessage().substring( 1 ).equalsIgnoreCase( this.ur.autoAssignCommand ) )
 		{
 			MLog.i( "Auto assigning Plot to player '" + e.getPlayer().getName() + "'" );
 			ur.assignPlot( e.getPlayer() );
 			// catch
 			e.setCancelled( true );
 		}
 	}
 }
