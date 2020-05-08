 package uk.co.jacekk.bukkit.grouplock.listeners;
 
 import java.util.Iterator;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.entity.EntityExplodeEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 
 import uk.co.jacekk.bukkit.baseplugin.event.BaseListener;
 import uk.co.jacekk.bukkit.grouplock.GroupLock;
 import uk.co.jacekk.bukkit.grouplock.locakble.LockableBlock;
 
 public class LockableProtectListener extends BaseListener<GroupLock> {
 	
 	public LockableProtectListener(GroupLock plugin){
 		super(plugin);
 	}
 	
 	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
 	public void onLockableOpen(PlayerInteractEvent event){
 		Block block = event.getClickedBlock();
 		Material type = block.getType();
 		String blockName = type.name().toLowerCase().replace('_', ' ');
 		Player player = event.getPlayer();
 		String playerName = player.getName();
 		
 		LockableBlock lockable = plugin.lockManager.getLockedBlock(block.getLocation());
 		
 		if (lockable != null && !lockable.canPlayerAccess(playerName)){
 			event.setCancelled(true);
 			player.sendMessage(plugin.formatMessage(ChatColor.RED + "That " + blockName + " is locked by " + lockable.getOwner()));
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
 	public void onBlockBreak(BlockBreakEvent event){
 		Player player = event.getPlayer();
 		String playerName = player.getName();
 		Block block = event.getBlock();
 		String blockName = block.getType().name().toLowerCase().replace('_', ' ');
 		
 		LockableBlock lockable = plugin.lockManager.getLockedBlock(block.getLocation());
 		
 		if (lockable != null){
 			if (!lockable.canPlayerModify(playerName)){
 				event.setCancelled(true);
 				player.sendMessage(plugin.formatMessage(ChatColor.RED + "That " + blockName + " is locked by " + lockable.getOwner()));
 			}else{
 				plugin.lockManager.removeLockedBlock(lockable.getLocation());
 			}
 		}else{
 			World world = block.getWorld();
 			int x = block.getX();
 			int y = block.getY();
 			int z = block.getZ();
 			
 			for (int dx = -1; dx <= 1; ++dx){
 				for (int dy = -2; dy <= 2; ++dy){
 					for (int dz = -1; dz <= 1; ++dz){
 						LockableBlock areaLockable = plugin.lockManager.getLockedBlock(world, x + dx, y + dy, z + dz);
 						
						if (areaLockable != null && areaLockable.canPlayerAccess(playerName)){
 							event.setCancelled(true);
 							player.sendMessage(plugin.formatMessage(ChatColor.RED + "You cannot break blocks this close to a locked " + blockName));
 						}
 					}
 				}
 			}
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
 	public void onEntityExplode(EntityExplodeEvent event){
 		Iterator<Block> blocks = event.blockList().iterator();
 		
 		while (blocks.hasNext()){
 			LockableBlock lockable = plugin.lockManager.getLockedBlock(blocks.next().getLocation());
 			
 			if (lockable != null){
 				blocks.remove();
 			}
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
 	public void onBlockPlace(BlockPlaceEvent event){
 		Player player = event.getPlayer();
 		String playerName = player.getName();
 		Block block = event.getBlock();
 		
 		World world = block.getWorld();
 		int x = block.getX();
 		int y = block.getY();
 		int z = block.getZ();
 		
 		for (int dx = -1; dx <= 1; ++dx){
 			for (int dy = -2; dy <= 2; ++dy){
 				for (int dz = -1; dz <= 1; ++dz){
 					LockableBlock lockable = plugin.lockManager.getLockedBlock(world, x + dx, y + dy, z + dz);
 					
 					if (lockable != null && !lockable.canPlayerAccess(playerName)){
 						String blockName = block.getType().name().toLowerCase().replace('_', ' ');
 						
 						event.setCancelled(true);
 						player.sendMessage(plugin.formatMessage(ChatColor.RED + "You cannot place blocks this close to a locked " + blockName));
 					}
 				}
 			}
 		}
 	}
 	
 }
