 
 package org.ss.listeners;
 
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.plugin.PluginManager;
 import org.getspout.spoutapi.SpoutManager;
 import org.getspout.spoutapi.player.SpoutPlayer;
 import org.ss.SpoutShopPermissions;
 import org.ss.SpoutShopPlugin;
 import org.ss.gui.ShopBuyPopup;
 import org.ss.gui.ShopManagerPopup;
 import org.ss.serial.Coordinate;
 import org.ss.shop.Shop;
 
 public class SSPlayerListener
 		extends PlayerListener {
 	private static SSPlayerListener instance = new SSPlayerListener();
 
 	public static SSPlayerListener getInstance() {
 		return instance;
 	}
 
 	public static void registerEvents( SpoutShopPlugin plugin ) {
 		PluginManager manager = plugin.getServer().getPluginManager();
 
 		manager.registerEvent( Type.PLAYER_INTERACT, instance, Priority.Normal, plugin );
 	}
 
 	public void onPlayerInteract( PlayerInteractEvent event ) {
 		if ( event.getAction() != Action.RIGHT_CLICK_BLOCK )
 			return;
 
 		Coordinate center_coord = new Coordinate( event.getClickedBlock() );
 		Shop shop = SSBlockListener.getInstance().getShop( center_coord );
 
 		if ( shop == null )
 			return;
 
 		SpoutPlayer player = SpoutManager.getPlayer( event.getPlayer() );
 
		if ( SpoutShopPermissions.INTERACT.hasNode( player ) ) {
 			player.sendMessage( "You do not have permission to interact with shops." );
 			event.setCancelled( true );
 			return;
 		}
 
 		if ( !player.isSpoutCraftEnabled() ) {
 			player.sendMessage( "SpoutCraft must used to interact with this." );
 			event.setCancelled( true );
 			return;
 		}
 
 		if ( shop.isManager( player ) )
 			new ShopManagerPopup( player, shop ).show();
 		else
 			new ShopBuyPopup( player, shop ).show();
 	}
 }
