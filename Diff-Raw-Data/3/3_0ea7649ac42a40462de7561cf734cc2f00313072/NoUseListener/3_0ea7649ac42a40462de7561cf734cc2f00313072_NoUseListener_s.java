 package net.worldoftomorrow.nala.ni;
 
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.player.PlayerBucketFillEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.event.player.PlayerBucketEmptyEvent;
 import org.bukkit.event.player.PlayerShearEntityEvent;
 //import org.bukkit.event.player.PlayerFishEvent;
 
 public class NoUseListener implements Listener{
 	
 	public NoUseListener(){
 		
 	}
 	
 	private Log log = new Log();
 	
 	@EventHandler
 	public void onBlockBreak(BlockBreakEvent event){
 		
 		Player p = event.getPlayer();
 		int id = p.getItemInHand().getTypeId();
 		//Create a new item stack because it likes to reset the item otherwise.
 		ItemStack item = new ItemStack(p.getItemInHand());
 		//Do this to prevent accidentally using armor or tool damage values
 		if(Tools.isTool(id) || Armour.armours.containsKey(id)){
 			item.setDurability((short) 0);
 		}
 		
 		log.debug("BlockBreakEvent fired. ".concat(Integer.toString(id)));
 		
 		if(Perms.NOUSE.has(p, item)){
 			event.setCancelled(true);
 			StringHelper.notifyPlayer(p, EventTypes.USE, id);
 			StringHelper.notifyAdmin(p, EventTypes.USE, p.getItemInHand());
 		}
 	}
 	
 	@EventHandler
 	public void onBlockPlace(BlockPlaceEvent event){
 		Player p = event.getPlayer();
 		ItemStack block = new ItemStack(event.getBlockPlaced().getType());
 		if(Perms.NOUSE.has(p, block)){
 			event.setCancelled(true);
 			StringHelper.notifyPlayer(p, EventTypes.USE, block.getTypeId());
			StringHelper.notifyAdmin(p, EventTypes.USE, p.getItemInHand());
			//TODO: fix this nonsense
 		}
 	}
 	
 	@EventHandler
 	public void onPlayerInteract(PlayerInteractEvent event){
 		Action action = event.getAction();
 		if(action.equals(Action.LEFT_CLICK_BLOCK)){
 			this.handleBlockLeftClick(event);
 		}
 		if(action.equals(Action.RIGHT_CLICK_BLOCK)){
 			this.handleBlockRightClick(event);
 		}
 		//I need to handle right and left click air
 	}
 	
 	private void handleBlockRightClick(PlayerInteractEvent event){
 		Player p = event.getPlayer();
 		ItemStack stack = new ItemStack(p.getItemInHand());
 		Material type = stack.getType();
 		stack.setDurability((short) 0);
 		if(type.equals(Material.FLINT_AND_STEEL)){
 			if(Perms.NOUSE.has(p, stack)){
 				event.setCancelled(true);
 				StringHelper.notifyPlayer(p, EventTypes.USE, stack.getTypeId());
 				StringHelper.notifyAdmin(p, EventTypes.USE, stack);
 			}
 		}
 		if(type.equals(Material.DIRT) || type.equals(Material.GRASS)){
 			//If it is a hoe (gotta handle them hoes!)
 			if(stack.getTypeId() >= 290 && stack.getTypeId() <= 294){
 				if(Perms.NOUSE.has(p, stack)){
 					event.setCancelled(true);
 					StringHelper.notifyPlayer(p, EventTypes.USE, stack.getTypeId());
 					StringHelper.notifyAdmin(p, EventTypes.USE, stack);
 				}
 			}
 		}
 	}
 	
 	private void handleBlockLeftClick(PlayerInteractEvent event){
 		Player p = event.getPlayer();
 		Block b = event.getClickedBlock();
 		ItemStack stack = new ItemStack(p.getItemInHand());
 		Material type = stack.getType();
 		if(type.equals(Material.FLINT_AND_STEEL) && b.getType().equals(Material.TNT)){
 			if(Perms.NOUSE.has(p, stack)){
 				event.setCancelled(true);
 				StringHelper.notifyPlayer(p, EventTypes.USE, stack.getTypeId());
 				StringHelper.notifyAdmin(p, EventTypes.USE, stack);
 			}
 		}
 	}
 	
 	@EventHandler
 	public void onBucketEmpty(PlayerBucketEmptyEvent event){
 		Player p = event.getPlayer();
 		int bucketID = event.getBucket().getId();
 		if(Perms.NOUSE.has(p, bucketID)){
 			event.setCancelled(true);
 			StringHelper.notifyPlayer(p, EventTypes.USE, bucketID);
 			StringHelper.notifyAdmin(p, EventTypes.USE, event.getItemStack());
 		}
 	}
 	
 	@EventHandler
 	public void onBucketFill(PlayerBucketFillEvent event){
 		Player p = event.getPlayer();
 		int bucketID = event.getBucket().getId();
 		if(Perms.NOUSE.has(p, bucketID)){
 			event.setCancelled(true);
 			StringHelper.notifyPlayer(p, EventTypes.USE, bucketID);
 			StringHelper.notifyAdmin(p, EventTypes.USE, event.getItemStack());
 		}
 	}
 	
 	@EventHandler
 	public void onPlayerShear(PlayerShearEntityEvent event){
 		Player p = event.getPlayer();
 		ItemStack shears = new ItemStack(Material.SHEARS);
 		shears.setDurability((short) 0);
 		if(Perms.NOUSE.has(p, shears)){
 			event.setCancelled(true);
 			StringHelper.notifyPlayer(p, EventTypes.USE, shears.getTypeId());
 			StringHelper.notifyAdmin(p, EventTypes.USE, shears);
 		}
 	}
 	
 	@EventHandler
 	public void onSwordSwing(EntityDamageByEntityEvent event){
 		Entity damager = event.getDamager();
 		if(damager instanceof Player){
 			Player p = (Player) damager;
 			ItemStack stack = new ItemStack(p.getItemInHand());
 			stack.setDurability((short) 0);
 			Material type = stack.getType();
 			if(Tools.isTool(type) && Perms.NOUSE.has(p, stack)){
 				event.setCancelled(true);
 				StringHelper.notifyPlayer(p, EventTypes.USE, stack.getTypeId());
 				StringHelper.notifyAdmin(p, EventTypes.USE, stack);
 			}
 		}
 	}
 	//TODO: Add bow support
 	//TODO: Change notifyPlayer and notifyAdmin methods to do the
 }
