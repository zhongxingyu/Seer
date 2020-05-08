 package iggy.Regions;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockBurnEvent;
 import org.bukkit.event.block.BlockIgniteEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
 
 public class BlockMonitor implements Listener{
 	Regions plugin;
 	BlockMonitor (Regions state) {
 		plugin = state;
 		Bukkit.getPluginManager().registerEvents(this, plugin);
 	}
 	//Called when a block is broken by a player. 
 	@EventHandler (priority = EventPriority.HIGHEST)
 	public void stopBreak (BlockBreakEvent event){
 		event.setCancelled(shouldCancel(event.getBlock().getLocation(),event.getPlayer()));
 	}
 	//Called when a block is destroyed as a result of being burnt by fire. 
 	@EventHandler (priority = EventPriority.HIGHEST)
 	public void stopBurn (BlockBurnEvent event){
 		event.setCancelled(shouldCancel(event.getBlock().getLocation(),null));
 	}
 	//Called when a block is ignited
 	@EventHandler (priority = EventPriority.HIGHEST)
 	public void stopFire (BlockIgniteEvent event){
 		event.setCancelled(shouldCancel(event.getBlock().getLocation(),event.getPlayer()));
 	}
  	//Called when a block is placed by a player. 
  	@EventHandler (priority = EventPriority.HIGHEST)
  	public void stopBuild (BlockPlaceEvent event) {
  		event.setCancelled(shouldCancel(event.getBlock().getLocation(),event.getPlayer()));
 	}
  	
  	
  	//Called when a bucket is placed
  	@EventHandler (priority = EventPriority.HIGHEST)
  	public void stopEmptyBuckets (PlayerBucketEmptyEvent event){
  		event.setCancelled(shouldCancel(event.getBlockClicked().getLocation(),event.getPlayer()));
  	}
  	
  	@EventHandler (priority = EventPriority.HIGHEST)
  	public void stopFillBuckets (PlayerBucketFillEvent event){
  		event.setCancelled(shouldCancel(event.getBlockClicked().getLocation(),event.getPlayer()));
  	} 
  	
  	/******************************** SHOULD CANCEL *******************************\
  	| this function reads in the situation and desides if the even should be canceld
  	| or not. It should be canceled if the event is happening inside a plot and the
  	| player causing the event is not an owner.
  	\******************************************************************************/
 
  	public boolean shouldCancel(Location location, Player player){
  		Position chunk = new Position(location);
 		String chunkName = plugin.chunkNames.get(chunk);
 		// if the plot does not exist it does not get canceled
 		if (chunkName == null){return false;}
 		// if code reaches here then the block is in an existing chunk
 		if (player == null) {
 			// if there is no player breaking the block in a plot then they cant be the owners of it
 			plugin.info("Block break event by non player in "+chunkName+" canceled");
 			return true;
 		}
 		Owners owners = plugin.chunkOwners.get(chunkName);
 		if (owners == null) {
 			plugin.severe("Chunk found but owners not! Check config file");
 			player.sendMessage("[ERROR LOADING CHUNK OWNERS] tell admin");
 			return true;
 		}
 		if (!owners.hasOwner(player.getName())){
 			player.sendMessage("Don't break other people's stuff!");
 			return true;
 		} 
 		return false;
  	}
 }
