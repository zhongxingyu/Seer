 package com.github.Holyvirus.Blacksmith.Listeners;
 
 import com.github.Holyvirus.Blacksmith.BlackSmith;
 //import com.github.Holyvirus.Blacksmith.core.Eco.iEco;
 import com.github.Holyvirus.Blacksmith.core.config;
 import com.github.Holyvirus.Blacksmith.core.perms.Permission;
 import com.github.Holyvirus.Blacksmith.core.Misc.Misc;
 import com.github.Holyvirus.Blacksmith.core.Tools.Cost.Cost;
 import com.github.Holyvirus.Blacksmith.core.Tools.Cost.Repair;
 import com.github.Holyvirus.Blacksmith.core.Tools.Dismantling.Dismantling;
 import com.github.Holyvirus.Blacksmith.core.Tools.Enchanter.Enchanter;
 import com.github.Holyvirus.Blacksmith.core.Tools.Sign.*;
 
 import java.util.List;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Listener;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 
 public class PlayerListener implements Listener {
 
 	private BlackSmith plugin;
 	private Permission pH;
 	
 	config conf = config.Obtain();
 	
 	public PlayerListener(BlackSmith plugin) {
 		this.plugin = plugin;
 		this.pH = plugin.getPermHandler().getEngine();
 	}
 	
 	@EventHandler
 	public void onPlayerInteract(PlayerInteractEvent event) {
 		if(event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
 			if(event.getClickedBlock().getState() instanceof Sign) {
 				SignType st = SignValidator.getType((Sign) event.getClickedBlock().getState());
 				Player p = event.getPlayer();
 				switch(st) {
 					case VALUE:
 						List<String> rCost = Cost.calcCost(event.getItem(), true);
 						int dM = (conf.getInt("BlackSmith.Settings.DismantleDivider") > 0) ? conf.getInt("BlackSmith.Settings.DismantleDivider") : 1;
 						String dCost = String.valueOf(Misc.Round(Cost.calcCost(event.getItem()) / dM, 2));
 						if(pH.has(p, "blacksmith.use.value")) {
 							Material m = Misc.getMatType(event.getItem());
 							if(m != null) {
 								String name = Misc.getItemName(event.getItem());
 								if(event.getItem().getDurability() > 0) {
 									if(rCost.size() == 1) {
 										p.sendMessage(ChatColor.BLUE + "It will cost you " + rCost.get(0) + " to repair your " + name + " or " + dCost + " to dismantle!");
 									}else{
 										p.sendMessage(ChatColor.BLUE + "To repair your " + name+ " it will cost you");
 										for(String s : rCost) {
 											p.sendMessage(ChatColor.BLUE + " - " + s);
 										}
 										if(!conf.getBoolean("BlackSmith.Settings.DismantleOnlyOnFull"))
 											p.sendMessage(ChatColor.BLUE + "or " + dCost + " to dismantle!");
 									}
 								}else{
 									p.sendMessage(ChatColor.BLUE + "It will cost you " + dCost + " to dismantle your " + name + "!");
 								}
 							}else{
 								p.sendMessage(ChatColor.DARK_RED + "Item not a tool!");
 							}
 						}else{
 							p.sendMessage(ChatColor.DARK_RED + "You do not have permission to use that sign!");
 						}
 						break;
 					case REPAIR:
 						if(pH.has(p, "blacksmith.use.repair")) {
 							Material m = Misc.getMatType(event.getItem());
 							if(m != null) {
 								if(event.getItem().getDurability() > 0) {
 									String s = Repair.doRepair(p, event.getItem());
 									if(s == null) {
 										p.sendMessage(ChatColor.GREEN + "Tool repaired!");
 									}else{
 										p.sendMessage(ChatColor.DARK_RED + "Failed to repair tool because " + s); 
 									}
 								}else{
 									p.sendMessage(ChatColor.BLUE + "You tool is not even broken!");
 								}
 							}else{
 								p.sendMessage(ChatColor.DARK_RED + "Item not a tool!");
 							}
 						}else{
 							p.sendMessage(ChatColor.DARK_RED + "You do not have permission to use that sign!");
 						}
 						break;
 					case FREE:
 						if(pH.has(p, "blacksmith.use.free")) {
 							Material m = Misc.getMatType(event.getItem());
 							if(m != null) {
 								event.getItem().setDurability((short) 0);
 								p.sendMessage(ChatColor.GREEN + "Tool repaired!");
 							}else{
 								p.sendMessage(ChatColor.DARK_RED + "Item not a tool!");
 							}
 						}else{
 							p.sendMessage(ChatColor.DARK_RED + "You do not have permission to use that sign!");
 						}
 						break;
 					case KILL:
 						config c = config.Obtain();
 						if(c.getBoolean("BlackSmith.global.debug")) {
 							if(pH.has(p, "blacksmith.use.kill")) {
 								Material m = Misc.getMatType(event.getItem());
 								if(m != null) {
 									event.getItem().setDurability((short) (event.getItem().getType().getMaxDurability() - 1));
 									p.sendMessage(ChatColor.GREEN + "Tool killed!");
 								}else{
 									p.sendMessage(ChatColor.DARK_RED + "Item not a tool!");
 								}
 							}else{
 								p.sendMessage(ChatColor.DARK_RED + "You do not have permission to use that sign!");
 							}
 						}else{
 							if(pH.has(p, "blacksmith.use.kill")) {
 								p.sendMessage(ChatColor.DARK_RED + "Debug mode is not enabled!");
 							}
 						}
 						break;
 					case DISMANTLE:
 						if(pH.has(p, "blacksmith.use.dismantle")) {
 							Material m = Misc.getMatType(event.getItem());
 							if(m != null) {
 								String d = Dismantling.dismantle(p, event.getItem());
 								if(d == null) {
 									p.sendMessage(ChatColor.GREEN + "Tool dismantled!");
 								}else{
 									p.sendMessage(ChatColor.DARK_RED + "Failed to repair tool because " + d); 
 								}
 							}else{
 								p.sendMessage(ChatColor.DARK_RED + "Item not a tool!");
 							}
 						}else{
 							p.sendMessage(ChatColor.DARK_RED + "You do not have permission to use that sign!");
 						}
 						break;
 					case ENCHANT:
 						if(pH.has(p, "blacksmith.use.enchant")) {
 							Material m = Misc.getMatType(event.getItem());
 							if(m != null) {
								if(Enchanter.add(p, event.getItem())) {
									ChatListener.add(p, 1);
									p.sendMessage(ChatColor.GREEN + "Welcome to the BlackSmith enchanting service! Please type \"help\" for more info, or type the enchant and the level that you want on your " + event.getItem().getType().toString().toLowerCase().replace("_", " ") + " !");
								}
 							}else{
 								p.sendMessage(ChatColor.DARK_RED + "Item not a tool!");
 							}
 						}else{
 							p.sendMessage(ChatColor.DARK_RED + "You do not have permission to use that sign!");
 						}
 						break;
 					default:
 						return;
 				}
 			}
 		}
 	}
 	
 }
