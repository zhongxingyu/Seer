 package minemail;
 
 import org.bukkit.Material;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockDamageEvent;
 import org.bukkit.event.block.BlockListener;
 import org.bukkit.util.config.Configuration;
 
 
 
 public class MineMailBlockListener extends BlockListener{
 	MineMail plugin;
 	
 	public MineMailBlockListener(MineMail mineMail){
 		plugin = mineMail;
 	}
 	
 	@Override
 	public void onBlockDamage(BlockDamageEvent event){
 		if(event.getBlock().getType().equals(Material.CHEST)){
 			Configuration config = plugin.getConfiguration();
 			config.load();
 			BlockCoords coords = new BlockCoords(event.getBlock());
 			try{
 				String[] auth = config.getString(coords.getCoords()).split(",");
 				boolean damageAllowed = false;
 				for(String u : auth) {
 					if(event.getPlayer().getDisplayName().toLowerCase().equals(u.toLowerCase())){
 						damageAllowed = true;
 					}
 				}
 				if(!damageAllowed){
 					event.setCancelled(true);
 					event.getPlayer().sendMessage("This mailbox belongs to another player");
 				}
 			} catch(Exception e) {
 				//do nothing
 			}	
 
 		}
 	}
 	
 	@Override
 	public void onBlockBreak(BlockBreakEvent event) {
 		if(event.getBlock().getType().equals(Material.CHEST)) {
 			Configuration config = plugin.getConfiguration();
			MailChest chest = new MailChest(event.getBlock(),config);
 			try {
				plugin.getServer().getPlayer(chest.getOwnerName()).sendMessage("Your mailbox has been deregistered");
				chest.deregister();
 			} catch(Exception e) {
 				//Do nothing
 			}
 			config.save();
 		}
 	}
 	
 }
