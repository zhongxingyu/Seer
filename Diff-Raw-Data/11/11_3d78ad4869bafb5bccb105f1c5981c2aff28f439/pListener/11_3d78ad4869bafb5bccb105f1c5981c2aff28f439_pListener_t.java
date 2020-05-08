 package TechGuard.Archers;
 
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event.Result;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.event.player.PlayerItemHeldEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.ItemStack;
 
 import TechGuard.Archers.Arrow.Arrow;
 import TechGuard.Archers.Arrow.ArrowHandler;
 import TechGuard.Archers.Arrow.EnumBowMaterial;
 /**
  * @author �TechGuard
  */
 public class pListener extends PlayerListener{
 
 	public void onItemHeldChange(PlayerItemHeldEvent event){
 		Player p = event.getPlayer();
 		ItemStack item = p.getInventory().getContents()[event.getNewSlot()];
 		
 		if(item == null){
 			return;
 		}
 		
 		if(item.getType() == Material.BOW){
			p.sendMessage(ChatColor.DARK_GREEN+"Bow Material: "+ChatColor.YELLOW+EnumBowMaterial.fromData(item.getDurability()).getName());
 		}
 	}
 	
 	public void onPlayerInteract(PlayerInteractEvent event){
 		if(event.getAction() == Action.LEFT_CLICK_BLOCK){
 			Player p = event.getPlayer();
 			ItemStack item = p.getItemInHand();
 			if(item.getType() == Material.BOW){
 				Material id = event.getClickedBlock().getType();
 				Material id2 = event.getClickedBlock().getRelative(0, 1, 0).getType();
 				EnumBowMaterial bm = null;
 				
 				if(id == Material.WOOD || id == Material.LOG){
 					bm = EnumBowMaterial.STANDARD;
 				}
 				if(id == Material.SNOW || id == Material.ICE){
 					bm = EnumBowMaterial.ICE;
 				}
 				if(id2 == Material.FIRE){
 					bm = EnumBowMaterial.FIRE;
 				}
 				if(id == Material.TNT){
 					bm = EnumBowMaterial.TNT;
 				}
 				if(id == Material.REDSTONE_ORE){
 					bm = EnumBowMaterial.THUNDER;
 				}
 				if(id == Material.MOB_SPAWNER){
 					bm = EnumBowMaterial.MONSTER;
 				}
 				if(id == Material.DISPENSER){
 					bm = EnumBowMaterial.THRICE;
 				}
 				if(id == Material.LAPIS_BLOCK){
 					bm = EnumBowMaterial.ZOMBIE;
 				}
 				if(bm != null){
 					if(bm.getDataValue() == EnumBowMaterial.fromData(item.getDurability()).getDataValue()){//Less spam
 						return;
 					}
 					item.setDurability(bm.getDataValue());
					p.sendMessage(ChatColor.DARK_GREEN+"Changed Bow Material to "+ChatColor.YELLOW+bm.getName()+ChatColor.DARK_GREEN+".");
 				}
 			}
 		}
 		if(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK){
 			Player p = event.getPlayer();
 			ItemStack item = p.getItemInHand();
 			if(item.getType() == Material.BOW){
 				event.setUseInteractedBlock(Result.DENY);
 				event.setCancelled(true);
 				EnumBowMaterial material = EnumBowMaterial.fromData(item.getDurability());
 				
 				if(!Archers.Permissions.has(p, "archers.bow."+material.getName().toLowerCase())){
 					return;
 				}
 				
 				int has = 0;
 				for(ItemStack stack : Properties.ArrowAmmo.get(material.getDataValue())){
 					int amount = 0;
 					for(int i = 0; i < p.getInventory().getContents().length; i++){
 						if(p.getInventory().getContents()[i] == null){
 							continue;
 						}
 						if(p.getInventory().getContents()[i].getTypeId() == stack.getTypeId()){
 							amount += p.getInventory().getContents()[i].getAmount();
 						}
 						if(amount >= stack.getAmount()){
 							++has;
 							break;
 						}
 					}
 				}
				if(has != Properties.ArrowAmmo.get(material.getDataValue()).size()){
					p.sendMessage(ChatColor.RED+"You don't have enough ammo!");
 					return;
				}
 				
 				Arrow arrow = new Arrow(p.getWorld(), p, material);
 				ArrowHandler.onArrowCreate(p, arrow);
 				
 				for(ItemStack stack : Properties.ArrowAmmo.get(material.getDataValue())){
 					CraftUpdate.removeItem(p, stack);
 				}
 			}
 		}
 	}
 }
