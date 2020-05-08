 package net.worldoftomorrow.nala.ni;
 
import java.util.HashMap;
import java.util.Map;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 
 public class ToolListener implements Listener{
 	
 	public ToolListener(){
 		
 	}
 	
 	private Log log = new Log();
 
 	private boolean notifyAdmin = Configuration.notifyAdmins();
 	
	private Map<Integer, Tools> blockTools = new HashMap<Integer, Tools>();
 	/*
 	 * I could do it based on permissions, or based off a list...maybe both?
 	 * i.e. noitem.nouse.diamondpick.5 or
 	 * BlockToolDefinitions:
 	 *     - diamondpick:stone
 	 *     - woodaxe:wood:1
 	 */
 	
 	@EventHandler
 	public void onBlockBreak(BlockBreakEvent event){
 		
 		Player p = event.getPlayer();
 		int id = p.getItemInHand().getTypeId();
 		
 		if(Configuration.debugging()){
 			log.log("Block Break event fired. \nUsed: " + id);
 		}
 		if(VaultPerms.Permissions.NOUSE.has(p, id) && !p.isOp() && !VaultPerms.Permissions.ALLITEMS.has(p)){
 			event.setCancelled(true);
 			if (Configuration.notifyNoUse()) {
 				notifyPlayer(p, id);
 			}
 			if (notifyAdmin) {
 				notifyAdmin(p, id);
 			}
 		}
 	}
 	
 	@EventHandler
 	public void onLandFarm(PlayerInteractEvent event){
 		Block block = event.getClickedBlock();
 		if(event.getAction() == Action.RIGHT_CLICK_BLOCK){
 			if(block.getType() == Material.DIRT || block.getType() == Material.GRASS){
 				int inHand = event.getPlayer().getItemInHand().getTypeId();
 				Player p = event.getPlayer();
 				if(inHand >= 290 && inHand <= 294){
 					if(VaultPerms.Permissions.NOUSE.has(p, inHand) && !p.isOp() && !VaultPerms.Permissions.ALLITEMS.has(p)){
 						event.setCancelled(true);
 						if (Configuration.notifyNoUse()) {
 							notifyPlayer(p, inHand);
 						}
 						if (notifyAdmin) {
 							notifyAdmin(p, inHand);
 						}
 					}
 				}
 			}
 		}
 	}
 	
 	private void notifyPlayer(Player p, int iid) {
 		String dn = p.getDisplayName();
 		String w = p.getWorld().getName();
 
 		int x = p.getLocation().getBlockX();
 		int y = p.getLocation().getBlockY();
 		int z = p.getLocation().getBlockZ();
 
 		p.sendMessage(ChatColor.RED + "[NI] " + ChatColor.BLUE
				+ StringHelper.replaceVars(Configuration.playerMessage(), dn, w, x, y, z, iid));
 	}
 
 	private void notifyAdmin(Player p, int iid) {
 		String dn = p.getDisplayName();
 		String w = p.getWorld().getName();
 
 		int x = p.getLocation().getBlockX();
 		int y = p.getLocation().getBlockY();
 		int z = p.getLocation().getBlockZ();
 		String formatedMessage = StringHelper.replaceVars(Configuration.adminMessage(), dn, w, x, y,
 				z, iid);
 
 		log.log(formatedMessage);
 		Player[] players = Bukkit.getOnlinePlayers();
 		for (Player player : players)
 			if ((player.isOp()) || (VaultPerms.Permissions.ADMIN.has(player)))
 				player.sendMessage(ChatColor.RED + "[NI] " + ChatColor.BLUE
 						+ formatedMessage);
 	}
 }
