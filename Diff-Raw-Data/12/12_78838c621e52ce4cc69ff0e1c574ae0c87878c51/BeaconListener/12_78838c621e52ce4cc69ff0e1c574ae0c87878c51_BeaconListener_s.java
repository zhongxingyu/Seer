 package us.twoguys.thedarkness.listeners;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.BlockState;
 import org.bukkit.block.Chest;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 
 import us.twoguys.thedarkness.TheDarkness;
 import us.twoguys.thedarkness.beacon.BeaconData;
 
 public class BeaconListener implements Listener{
 
 	TheDarkness plugin;
 	int c = 0; //set to 0 to disable
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
 		if(c == 1){
 			plugin.schematic.loadSchematic();
 			c = 2;
 			
 		}else if(c==2){
 			plugin.schematic.paste(player, block.getLocation());
 			c = 1;
 		}else if(beaconPlace(player, block)){
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
 			}else{return;}
 			
 			Chest c = (Chest) chest.getState();
 			Inventory i = c.getInventory();
 			ItemStack[] is = i.getContents();
 		
 			if(s.getLine(1).isEmpty() || s.getLine(1).equals(player.getName())){
 				int nox = 0;
 				
				for(ItemStack item : is){
					if(plugin.config.isWorthBeaconPoints(item.getType())){
 						int amount = plugin.config.getItemBeaconPointValue(item.getType());
 						int quantity = item.getAmount();
 						nox = nox + (amount * quantity);
 					}
 				}
 				plugin.beaconPlayerDataMaster.addPoints(player, nox);
				plugin.sendMessage(player, "You have recieved "+nox+" nox extract");
 			}	
 		}
 		
 	}
 	private boolean extraction(Player player, Block block){
 		if(block.getType()==Material.SIGN || block.getType()==Material.SIGN_POST){
 			plugin.debug("Block is a sign");
 			s = (Sign)block.getState();
 			if(s.getLine(0).equals("[Nox Extractor]")){
 				return true;
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
