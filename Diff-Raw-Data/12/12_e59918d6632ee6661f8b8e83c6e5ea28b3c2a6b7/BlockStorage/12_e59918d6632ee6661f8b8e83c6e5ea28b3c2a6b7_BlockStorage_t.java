 package me.Travja.HungerArena;
 
 import java.util.List;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockBurnEvent;
 import org.bukkit.event.block.BlockFadeEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.entity.EntityExplodeEvent;
 import org.bukkit.event.player.PlayerBucketEmptyEvent;
 import org.bukkit.event.player.PlayerBucketFillEvent;
 
 public class BlockStorage implements Listener {
 	public Main plugin;
 	public BlockStorage(Main m) {
 		this.plugin = m;
 	}
 	@EventHandler(priority=EventPriority.MONITOR)
 	public void BlockBreak(BlockBreakEvent event) {
 		Block b = event.getBlock();
 		Player p = event.getPlayer();
 		String pname = p.getName();
 		boolean protall = false;
 		if (plugin.config.getString("Protected_Arena_Always").equalsIgnoreCase("True")) { 		/* Jeppa Fix/Add */
 			protall = true;
			event.setCancelled(true);
			p.sendMessage("You can't break blocks, at all, if you feel this should change, talk to the server owner.");
 		}
		if (plugin.getArena(p) != null || !protall) {
 			//int a = this.plugin.getArena(p).intValue();
 			int a = 1;											//Jeppa: define a default (may be needed if protall is true)
 			if (plugin.getArena(p) != null) a = plugin.getArena(p);
			if (!event.isCancelled() && plugin.Playing.get(a).contains(pname)){
 				if (plugin.config.getString("Protected_Arena").equalsIgnoreCase("True")) {
					event.setCancelled(true);
 					p.sendMessage(ChatColor.RED + "You can't break blocks while playing!");
 				} else if ((((plugin.canjoin.get(a))) || (protall)) && ((plugin.config.getStringList("worlds").isEmpty()) || ((!plugin.config.getStringList("worlds").isEmpty()) && (plugin.config.getStringList("worlds").contains(p.getWorld().getName()))))) { 
 					if (((plugin.management.getIntegerList("blocks.whitelist").isEmpty()) || ((!plugin.management.getIntegerList("blocks.whitelist").isEmpty()) && (!plugin.management.getIntegerList("blocks.whitelist").contains(Integer.valueOf(b.getTypeId()))))) ^ (plugin.management.getBoolean("blocks.useWhitelistAsBlacklist"))) {
 						event.setCancelled(true);
 						p.sendMessage(ChatColor.RED + "That is an illegal block!");
 					} else {
 						String w = b.getWorld().getName();
 						int x = b.getX();
 						int y = b.getY();
 						int z = b.getZ();
 						int d = b.getTypeId();
 						byte m = b.getData();
 						String coords = w + "," + x + "," + y + "," + z + "," + d + "," + m + "," + a;
 						List<String> blocks = plugin.data.getStringList("Blocks_Destroyed");
 						if (!plugin.data.getStringList("Blocks_Placed").contains(w + "," + x + "," + y + "," + z + "," + a)) {
 							blocks.add(coords);
 							plugin.data.set("Blocks_Destroyed", blocks);
 							plugin.saveData();
 						}
 					}
 				}
 			}
 		}
 	}
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void Explosion(EntityExplodeEvent event){
 		List<Block> blocksd = event.blockList();
 		Entity e = event.getEntity();
 		int i = 0;
 		if(!event.isCancelled()){
 			for(i = 1; i <= plugin.canjoin.size(); i++){
 				if(plugin.canjoin.get(i)){
 					i = plugin.canjoin.size()+1;
 					if(plugin.config.getStringList("worlds").isEmpty() || (!plugin.config.getStringList("worlds").isEmpty() && plugin.config.getStringList("worlds").contains(event.getEntity().getWorld().getName()))){
 						if(e.getType()== EntityType.PRIMED_TNT){
 							if(!plugin.data.getStringList("Blocks_Placed").contains(e.getLocation().getWorld() + "," + e.getLocation().getX() + "," + e.getLocation().getY() + "," + e.getLocation().getZ()) /*|| !plugin.data.getStringList("Blocks_Destroyed").contains(e.getLocation().getWorld() + "," + e.getLocation().getX() + "," + e.getLocation().getY() + "," + e.getLocation().getZ())*/){
 								List<String> blocks = plugin.data.getStringList("Blocks_Destroyed");
 								blocks.add(e.getLocation().getWorld().getName() + "," + e.getLocation().getX() + "," + e.getLocation().getY() + "," + e.getLocation().getZ() + ",46" + ",0");
 								plugin.data.set("Blocks_Destroyed", blocks);
 								plugin.saveData();
 							}
 						}
 						for(Block b:blocksd){
 							String w = event.getEntity().getWorld().getName();
 							int x = b.getX();
 							int y = b.getY();
 							int z = b.getZ();
 							int d = b.getTypeId();
 							byte m = b.getData();
 							String coords = w + "," + x + "," + y + "," + z + "," + d + "," + m + "," + i;
 							List<String> blocks = plugin.data.getStringList("Blocks_Destroyed");
 							if(!plugin.data.getStringList("Blocks_Placed").contains(w + "," + x + "," + y + "," + z + "," + i) || !plugin.data.getStringList("Blocks_Destroyed").contains(w + "," + x + "," + y + "," + z + "," + i)){
 								blocks.add(coords);
 								plugin.data.set("Blocks_Destroyed", blocks);
 								plugin.saveData();
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void burningBlocks(BlockBurnEvent event){
 		Block b = event.getBlock();
 		int i = 0;
 		if(!event.isCancelled()){
 			for(i = 1; i <= plugin.canjoin.size(); i++){
 				if(plugin.canjoin.get(i)){
 					i = plugin.canjoin.size()+1;
 					if(plugin.config.getStringList("worlds").isEmpty() || (!plugin.config.getStringList("worlds").isEmpty() && plugin.config.getStringList("worlds").contains(b.getWorld().getName()))){
 						String w = b.getWorld().getName();
 						int x = b.getX();
 						int y = b.getY();
 						int z = b.getZ();
 						int d = b.getTypeId();
 						byte m = b.getData();
 						String coords = w + "," + x + "," + y + "," + z + "," + d + "," + m + "," + i;
 						List<String> blocks = plugin.data.getStringList("Blocks_Destroyed");
 						if(!plugin.data.getStringList("Blocks_Placed").contains(w + "," + x + "," + y + "," + z + "," + i)){
 							blocks.add(coords);
 							plugin.data.set("Blocks_Destroyed", blocks);
 							plugin.saveData();
 						}
 					}
 				}
 			}
 		}
 	}
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void blockPlace(BlockPlaceEvent event){
 		Block b = event.getBlock();
 		Player p = event.getPlayer();
 		if(plugin.getArena(p)!= null){
 			int a = plugin.getArena(p);
 			if(!event.isCancelled()){
 				if(plugin.Playing.get(a).contains(p.getName())){
 					if(plugin.canjoin.get(a)){
 						if(plugin.config.getStringList("worlds").isEmpty() || (!plugin.config.getStringList("worlds").isEmpty() && plugin.config.getStringList("worlds").contains(b.getWorld().getName()))){
 							if((b.getType()== Material.SAND || b.getType()== Material.GRAVEL) && (b.getRelative(BlockFace.DOWN).getType()== Material.AIR || b.getRelative(BlockFace.DOWN).getType()== Material.WATER || b.getRelative(BlockFace.DOWN).getType()== Material.LAVA)){
 								int n = b.getY() -1;
 								while(b.getWorld().getBlockAt(b.getX(), n, b.getZ()).getType()== Material.AIR || b.getWorld().getBlockAt(b.getX(), n, b.getZ()).getType()== Material.WATER || b.getWorld().getBlockAt(b.getX(), n, b.getZ()).getType()== Material.LAVA){
 									n = n -1;
 									event.getPlayer().sendMessage(b.getWorld().getBlockAt(b.getX(), n, b.getZ()).getType().toString().toLowerCase());
 									if(b.getWorld().getBlockAt(b.getX(), n, b.getZ()).getType()!= Material.AIR || b.getWorld().getBlockAt(b.getX(), n, b.getZ()).getType()!= Material.WATER || b.getWorld().getBlockAt(b.getX(), n, b.getZ()).getType()!= Material.LAVA){
 										int l = n +1;
 										Block br = b.getWorld().getBlockAt(b.getX(), l, b.getZ());
 										String w = br.getWorld().getName();
 										int x = br.getX();
 										int y = br.getY();
 										int z = br.getZ();
 										String coords = w + "," + x + "," + y + "," + z + "," + a;
 										p.sendMessage(ChatColor.GREEN + "Sand/Gravel will land at " + coords);
 										List<String> blocks = plugin.data.getStringList("Blocks_Placed");
 										blocks.add(coords);
 										plugin.data.set("Blocks_Placed", blocks);
 										plugin.saveData();
 									}
 								}
 							}else{
 								if(b.getType()!= Material.SAND || b.getType()!= Material.GRAVEL){
 									String w = b.getWorld().getName();
 									int x = b.getX();
 									int y = b.getY();
 									int z = b.getZ();
 									String coords = w + "," + x + "," + y + "," + z + "," + a;
 									List<String> blocks = plugin.data.getStringList("Blocks_Placed");
 									blocks.add(coords);
 									plugin.data.set("Blocks_Placed", blocks);
 									plugin.saveData();
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void bucketEmpty(PlayerBucketEmptyEvent event){
 		if(plugin.getArena(event.getPlayer())!= null){
 			int a = plugin.getArena(event.getPlayer());
 			if(!event.isCancelled()){
 				if(plugin.canjoin.get(a)){
 					if(plugin.Playing.get(a).contains(event.getPlayer().getName())){
 						if(plugin.config.getStringList("worlds").isEmpty() || (!plugin.config.getStringList("worlds").isEmpty() && plugin.config.getStringList("worlds").contains(event.getPlayer().getWorld().getName()))){
 							Block b = event.getBlockClicked().getRelative(event.getBlockFace());
 							String w = b.getWorld().getName();
 							int x = b.getX();
 							int y = b.getY();
 							int z = b.getZ();
 							String coords = w + "," + x + "," + y + "," + z + "," + a;
 							List<String> blocks = plugin.data.getStringList("Blocks_Placed");
 							blocks.add(coords);
 							plugin.data.set("Blocks_Placed", blocks);
 							plugin.saveData();
 						}
 					}
 				}
 			}
 		}
 	}
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void bucketFill(PlayerBucketFillEvent event){
 		if(plugin.getArena(event.getPlayer())!= null){
 			int a = plugin.getArena(event.getPlayer());
 			if(!event.isCancelled()){
 				if(plugin.canjoin.get(a)){
 					if(plugin.Playing.get(a).contains(event.getPlayer().getName())){
 						if(plugin.config.getStringList("worlds").isEmpty() || (!plugin.config.getStringList("worlds").isEmpty() && plugin.config.getStringList("worlds").contains(event.getPlayer().getWorld().getName()))){
 							Block b = event.getBlockClicked().getRelative(event.getBlockFace());
 							String w = b.getWorld().getName();
 							int x = b.getX();
 							int y = b.getY();
 							int z = b.getZ();
 							int d = b.getTypeId();
 							byte m = b.getData();
 							String coords = w + "," + x + "," + y + "," + z + "," + d + "," + m + "," + a;
 							List<String> blocks = plugin.data.getStringList("Blocks_Destroyed");
 							if(!plugin.data.getStringList("Blocks_Placed").contains(w + "," + x + "," + y + "," + z + "," + a)){
 								blocks.add(coords);
 								plugin.data.set("Blocks_Destroyed", blocks);
 								plugin.saveData();
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void blockMelt(BlockFadeEvent event){
 		int i = 0;
 		if(!event.isCancelled()){
 			for(i = 1; i <= plugin.canjoin.size(); i++){
 				if(plugin.canjoin.get(i)){
 					if(plugin.config.getStringList("worlds").isEmpty() || (!plugin.config.getStringList("worlds").isEmpty() && plugin.config.getStringList("worlds").contains(event.getBlock().getWorld().getName()))){
 						i = plugin.canjoin.size()+1;
 						Block b = event.getBlock();
 						String w = b.getWorld().getName();
 						int x = b.getX();
 						int y = b.getY();
 						int z = b.getZ();
 						int d = b.getTypeId();
 						byte m = b.getData();
 						String coords = w + "," + x + "," + y + "," + z + "," + d + "," + m + "," + i;
 						List<String> blocks = plugin.data.getStringList("Blocks_Destroyed");
 						if(!plugin.data.getStringList("Blocks_Placed").contains(w + "," + x + "," + y + "," + z + "," + i)){
 							blocks.add(coords);
 							plugin.data.set("Blocks_Destroyed", blocks);
 							plugin.saveData();
 						}
 					}
 				}
 			}
 		}
 	}
 	/*@EventHandler
 	public void blockGrow(BlockGrowEvent event){
 		if(plugin.canjoin== true){
 			Block b = event.getBlock();
 			String w = b.getWorld().getName();
 			int x = b.getX();
 			int y = b.getY();
 			int z = b.getZ();
 			int d = b.getTypeId();
 			String coords = w + "," + x + "," + y + "," + z + "," + d;
 			System.out.println("Grow: " + coords);
 			List<String> blocks = plugin.data.getStringList("Blocks_Placed");
 			blocks.add(coords);
 			plugin.data.set("Blocks_Destroyed", blocks);
 			plugin.saveConfig();
 		}
 	}
 	@EventHandler
 	public void blockForm(BlockFormEvent event){
 		if(plugin.canjoin== true){
 			Block b = event.getBlock();
 			String w = b.getWorld().getName();
 			int x = b.getX();
 			int y = b.getY();
 			int z = b.getZ();
 			int d = b.getTypeId();
 			String coords = w + "," + x + "," + y + "," + z + "," + d;
 			System.out.println("Snowfall: " + coords);
 			List<String> blocks = plugin.data.getStringList("Blocks_Placed");
 			blocks.add(coords);
 			plugin.data.set("Blocks_Destroyed", blocks);
 			plugin.saveConfig();
 		}
 	}
 	@EventHandler
 	public void pistonPush(BlockPistonExtendEvent event){
 		if(plugin.canjoin== true){
 			for(Block b:event.getBlocks()){
 				String w = b.getWorld().getName();
 				int x = b.getX();
 				int y = b.getY();
 				int z = b.getZ();
 				int d = b.getTypeId();
 				String coords = w + "," + x + "," + y + "," + z + "," + d;
 				System.out.println("Piston: " + coords);
 				List<String> blocks = plugin.data.getStringList("Blocks_Destroyed");
 				blocks.add(coords);
 				plugin.data.set("Blocks_Destroyed", blocks);
 				plugin.saveConfig();
 			}
 		}
 	}
 	@EventHandler
 	public void onChange(BlockPhysicsEvent event){
 		Block block = event.getBlock();
 		Material changed = event.getChangedType();
 		if (block.getType() == Material.LAVA){
 			if (changed == Material.LAVA){
 				Block b = event.getBlock();
 				String w = b.getWorld().getName();
 				int x = b.getX();
 				int y = b.getY();
 				int z = b.getZ();
 				int d = b.getTypeId();
 				String coords = w + "," + x + "," + y + "," + z + "," + d;
 				System.out.println("Lava Change: " + coords);
 				List<String> blocks = plugin.data.getStringList("Blocks_Placed");
 				blocks.add(coords);
 				plugin.data.set("Blocks_Destroyed", blocks);
 				plugin.saveConfig();
 			}else if(changed == Material.WATER){
 				Block b = event.getBlock();
 				String w = b.getWorld().getName();
 				int x = b.getX();
 				int y = b.getY();
 				int z = b.getZ();
 				int d = b.getTypeId();
 				String coords = w + "," + x + "," + y + "," + z + "," + d;
 				System.out.println("Water Change: " + coords);
 				List<String> blocks = plugin.data.getStringList("Blocks_Placed");
 				blocks.add(coords);
 				plugin.data.set("Blocks_Destroyed", blocks);
 				plugin.saveConfig();
 			}
 		}else if (block.getType() == Material.SAND || block.getType() == Material.GRAVEL) {
 			if (changed == Material.AIR) {
 				Block b = event.getBlock();
 				String w = b.getWorld().getName();
 				int x = b.getX();
 				int y = b.getY();
 				int z = b.getZ();
 				int d = b.getTypeId();
 				String coords = w + "," + x + "," + y + "," + z + "," + d;
 				System.out.println("Sand/Gravel Fall: " + coords);
 				List<String> blocks = plugin.data.getStringList("Blocks_Placed");
 				blocks.add(coords);
 				plugin.data.set("Blocks_Destroyed", blocks);
 				plugin.saveConfig();
 			}
 		}
 	}*/
 }
