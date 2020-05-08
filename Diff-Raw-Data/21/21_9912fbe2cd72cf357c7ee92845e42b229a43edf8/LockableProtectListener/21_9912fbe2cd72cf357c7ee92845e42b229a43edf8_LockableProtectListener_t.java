 package uk.co.jacekk.bukkit.grouplock.listeners;
 
 import net.minecraft.server.TileEntity;
 import net.minecraft.server.World;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.craftbukkit.CraftWorld;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.entity.EntityExplodeEvent;
 
 import uk.co.jacekk.bukkit.baseplugin.v5.event.BaseListener;
 import uk.co.jacekk.bukkit.grouplock.GroupLock;
 import uk.co.jacekk.bukkit.grouplock.event.LockableOpenEvent;
 import uk.co.jacekk.bukkit.grouplock.nms.TileEntityLockable;
 
 public class LockableProtectListener extends BaseListener<GroupLock> {
 	
 	public LockableProtectListener(GroupLock plugin){
 		super(plugin);
 	}
 	
 	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
 	public void onLockableOpen(LockableOpenEvent event){
 		Block block = event.getBlock();
 		Material type = block.getType();
 		String blockName = type.name().toLowerCase().replace('_', ' ');
 		Player player = event.getPlayer();
 		String playerName = player.getName();
 		TileEntityLockable lockable = event.getlockable();
 		
 		if (!lockable.canAccess(playerName)){
 			event.setCancelled(true);
 			player.sendMessage(plugin.formatMessage(ChatColor.RED + "That " + blockName + " is locked by " + lockable.getOwnerName()));
 		}
		
		player.sendMessage(plugin.formatMessage(ChatColor.AQUA + "That " + blockName + " is locked by " + lockable.getOwnerName()));
 	}
 	
 	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
 	public void onBlockBreak(BlockBreakEvent event){
 		Player player = event.getPlayer();
 		String playerName = player.getName();
 		Block block = event.getBlock();
 		String blockName = block.getType().name().toLowerCase().replace('_', ' ');
 		World world = ((CraftWorld) block.getWorld()).getHandle();
 		TileEntity tileEntity = world.getTileEntity(block.getX(), block.getY(), block.getZ());
 		
 		if (tileEntity != null && tileEntity instanceof TileEntityLockable){
 			TileEntityLockable lockable = (TileEntityLockable) tileEntity;
 			
			if (!lockable.canModify(playerName)){
 				event.setCancelled(true);
 				player.sendMessage(plugin.formatMessage(ChatColor.RED + "That " + blockName + " is locked by " + lockable.getOwnerName()));
 			}
 		}else{
 			int x = block.getX();
 			int y = block.getY();
 			int z = block.getZ();
 			
 			for (int dx = -1; dx <= 1; ++dx){
 				for (int dy = -2; dy <= 2; ++dy){
 					for (int dz = -1; dz <= 1; ++dz){
 						tileEntity = world.getTileEntity(x + dx, y + dy, z + dz);
 						
 						if (tileEntity != null && tileEntity instanceof TileEntityLockable){
 							TileEntityLockable lockable = (TileEntityLockable) tileEntity;
 							
 							if (lockable.canAccess(playerName)){
 								event.setCancelled(true);
 								player.sendMessage(plugin.formatMessage(ChatColor.RED + "You cannot break blocks this close to a locked " + blockName));
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
 	public void onEntityExplode(EntityExplodeEvent event){
 		World world = ((CraftWorld) event.getLocation().getWorld()).getHandle();
 		
 		for (Block block : event.blockList()){
 			TileEntity tileEntity = world.getTileEntity(block.getX(), block.getY(), block.getZ());
 			
 			if (tileEntity != null && tileEntity instanceof TileEntityLockable){
 				if (((TileEntityLockable) tileEntity).hasOwnerName()){
 					event.setCancelled(true);
 					return;
 				}
 			}
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
 	public void onBlockPlace(BlockPlaceEvent event){
 		Player player = event.getPlayer();
 		String playerName = player.getName();
 		Block block = event.getBlock();
 		String blockName = block.getType().name().toLowerCase().replace('_', ' ');
 		World world = ((CraftWorld) block.getWorld()).getHandle();
 		
 		int x = block.getX();
 		int y = block.getY();
 		int z = block.getZ();
 		
 		for (int dx = -1; dx <= 1; ++dx){
 			for (int dy = -2; dy <= 2; ++dy){
 				for (int dz = -1; dz <= 1; ++dz){
 					TileEntity tileEntity = world.getTileEntity(x + dx, y + dy, z + dz);
 					
 					if (tileEntity != null && tileEntity instanceof TileEntityLockable){
 						TileEntityLockable lockable = (TileEntityLockable) tileEntity;
 						
						if (!lockable.canAccess(playerName)){
 							event.setCancelled(true);
 							player.sendMessage(plugin.formatMessage(ChatColor.RED + "You cannot place blocks this close to a locked " + blockName));
 						}
 					}
 				}
 			}
 		}
 	}
 	
 }
