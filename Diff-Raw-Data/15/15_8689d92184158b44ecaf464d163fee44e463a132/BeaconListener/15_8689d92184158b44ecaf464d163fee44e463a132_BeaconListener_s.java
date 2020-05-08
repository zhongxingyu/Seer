 package us.twoguys.thedarkness.listeners;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.BlockState;
 import org.bukkit.block.Chest;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.SignChangeEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 
 import us.twoguys.thedarkness.TheDarkness;
 import us.twoguys.thedarkness.beacon.BeaconData;
 
 public class BeaconListener implements Listener{
 
 	TheDarkness plugin;
 	Sign s;
 	
 	public BeaconListener(TheDarkness instance){
 		this.plugin = instance;
 	}
 	
 	@EventHandler
 	public void onPlayerInteract(PlayerInteractEvent event){
 		Player player = event.getPlayer();
 		Block block = event.getClickedBlock();
 		
 		if (block == null){return;}
 		if (block.getType()==Material.AIR){return;}
 			
 		if(beaconPlace(player, block)){
 			if(plugin.beaconPlayerDataMaster.canCreateBeacon(player, true)){
 				BeaconData bd = new BeaconData(block.getLocation());
 				plugin.beaconMaster.createBeacon(player, bd);
 				plugin.beaconListenerMaster.setString(player, "null");
 				plugin.sendMessage(player, "You have created a beacon");
 			}else{
 				plugin.sendMessage(player, "You dont have enough dark essence. You need at least "+plugin.config.getBeaconCost());
 			}
 			
 		}else if(extraction(player, block)){
 			plugin.debug("Extraction");
 
 			Block chest;
 			if(block.getRelative(BlockFace.NORTH).getType()==Material.CHEST){
 				chest = block.getRelative(BlockFace.NORTH);
 			}else if(block.getRelative(BlockFace.EAST).getType()==Material.CHEST){
 				chest = block.getRelative(BlockFace.EAST);	
 			}else if(block.getRelative(BlockFace.SOUTH).getType()==Material.CHEST){
 				chest = block.getRelative(BlockFace.SOUTH);
 			}else if(block.getRelative(BlockFace.WEST).getType()== Material.CHEST){
 				chest = block.getRelative(BlockFace.WEST);
 			}else if(block.getRelative(BlockFace.DOWN).getType()==Material.CHEST){
 				chest = block.getRelative(BlockFace.DOWN);
 			}else{return;}
 			
 			Chest c = (Chest) chest.getState();
 			Inventory i = c.getInventory();
 			ItemStack[] is = i.getContents();
 		
 			if(s.getLine(1).isEmpty() || s.getLine(1).equals(player.getName())){
 				int nox = 0;
 				
 				for(int x = 0; x < is.length ; x ++){
 					if(is[x] != null && plugin.config.isWorthBeaconPoints(is[x].getType())){
 						ItemStack item = is[x];
 						int amount = plugin.config.getItemBeaconPointValue(item.getType());
 						int quantity = item.getAmount();
 						i.remove(item);
 						nox = nox + (amount * quantity);
 					}
 				}
 				plugin.beaconPlayerDataMaster.addPoints(player, nox);
 				plugin.sendMessage(player, "You have extracted "+ChatColor.GREEN+nox+ChatColor.WHITE+" Dark Essence");
 			}	
 		}
 		
 	}
 	
 	@EventHandler
 	public void onSignChange(SignChangeEvent event){
 		if(event.getLine(0).equalsIgnoreCase("Nox Extractor")){
 			event.setLine(0, ChatColor.YELLOW+"Nox Extractor");
 		}
 	}
 	private boolean extraction(Player player, Block block){
 		if(block.getType()==Material.SIGN || block.getType()==Material.SIGN_POST){
 			plugin.debug("Block is a sign");
 			s = (Sign)block.getState();
			if(s.getLine(0).equals(ChatColor.YELLOW+"Nox Extractor")){
 				if(plugin.beaconMaster.getDarknessLevel(player)==0){
 					return true;
 				}else{
 					plugin.sendMessage(player, "The darkness is too powerful here...");
 					return false;
 				}
 			}else{
 				return false;
 			}
 		}
 		return false;
 	}
 	
 	private boolean beaconPlace(Player player, Block block){
 		try{
 			return plugin.beaconListenerMaster.getPlayerString(player).equalsIgnoreCase("beaconPlace");
 		}catch(Exception e){
 			return false;
 		}	
 	}
 }
