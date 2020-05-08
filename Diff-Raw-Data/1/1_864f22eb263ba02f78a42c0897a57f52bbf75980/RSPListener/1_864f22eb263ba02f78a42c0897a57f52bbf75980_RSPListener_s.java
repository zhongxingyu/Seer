 package com.dkabot.RSPassword;
 
 import org.bukkit.ChatColor;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.Sign;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.SignChangeEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.material.Lever;
 import org.bukkit.plugin.Plugin;
 
 public class RSPListener implements Listener {
 	public RSPassword plugin;
 	public  RSPListener (RSPassword plugin) {
 	    this.plugin = plugin;
 	    plugin.getServer().getPluginManager().registerEvents(this, plugin);
 	}
 		
 		//Begin Player Interaction Listener
 	    @EventHandler(ignoreCancelled = true)
 		public void onPlayerInteract(PlayerInteractEvent event) {
 			try {
 				event.getClickedBlock().getState();
 			}
 			catch(NullPointerException e) {
 				return;
 			}
 			if(event.getClickedBlock().getState() instanceof Sign) {	
 				if(event.isCancelled()) return;
 				if(event.getAction() != Action.RIGHT_CLICK_BLOCK) {
 					return;
 				}
 				Persistance interactClass = plugin.getDatabase().find(Persistance.class).where().ieq("location", event.getClickedBlock().getLocation().toString()).findUnique();
 				if (interactClass == null) {
 					return;
 				}
 				if(plugin.password.containsKey(event.getPlayer()) || event.getPlayer().isOp() || event.getPlayer().hasPermission("rspassword.useany")) {
 					if(event.getPlayer().isOp() || event.getPlayer().hasPermission("rspassword.useany") || plugin.password.get(event.getPlayer().getName()) != null || plugin.password.get(event.getPlayer().getName()) == interactClass.getPassword()) {
 						if(plugin.password.containsKey(event.getPlayer().getName())) plugin.password.remove(event.getPlayer().getName());
 						org.bukkit.material.Sign signMaterial = (org.bukkit.material.Sign)event.getClickedBlock().getState().getData();
 						final org.bukkit.block.Sign signBlock = (org.bukkit.block.Sign)event.getClickedBlock().getState();
 						if (signBlock.getLine(3) == ChatColor.stripColor("ACCEPTED")) return;
 						BlockFace leverDirection = signMaterial.getAttachedFace();
 						if(event.getClickedBlock().getRelative(leverDirection, 2).getState().getData() instanceof Lever) {
 							final Block leverBlock = (Block) event.getClickedBlock().getRelative(leverDirection, 2);
 							final String obfuPass = plugin.obfupass(interactClass.getPassword());
 							signBlock.setLine(3, ChatColor.GREEN + "ACCEPTED");
 							signBlock.update();
 							plugin.toggleLever(leverBlock);
 							Long timer = Long.decode(Integer.toString((interactClass.getTimer())*20));
 							plugin.getServer().getScheduler().scheduleSyncDelayedTask((Plugin)plugin, new Runnable() {public void run() {plugin.toggleLever(leverBlock); signBlock.setLine(3, ChatColor.GREEN + obfuPass); signBlock.update(false);}}, timer);
 							return;
 						}
 						else {
 							event.getPlayer().sendMessage(ChatColor.RED + "No lever found!");
 						}
 					}
 					else {
 						if(plugin.password.containsKey(event.getPlayer().getName())) plugin.password.remove(event.getPlayer().getName());
 						event.getPlayer().sendMessage(ChatColor.RED + "Incorrect Password");
 						return;
 					}
 				}
 				else {
 					event.getPlayer().sendMessage(ChatColor.GREEN + "This is a RSPassword sign! Use " + ChatColor.RED + "/rsp <password>" + ChatColor.GREEN + " then click it again!");
 			}
 		}
 	}
 	   
 	//Begin Sign Change Listener
 	    @EventHandler(ignoreCancelled = true)
 		public void onSignChange(SignChangeEvent event) {
 			if(event.isCancelled()) return;
 			    if(event.getLine(0).equalsIgnoreCase("[RSPassword]") || event.getLine(0).equalsIgnoreCase("[RSP]")) {
 				    if(!event.getPlayer().isOp() && !event.getPlayer().hasPermission("rspassword.create")) {
 						event.getPlayer().sendMessage(ChatColor.RED + "You do not have permission to create RSPassword signs.");
 						return;
 				    }
 				    if(event.getLine(3) == "" || event.getLine(2).contains(" ") || event.getLine(2) == "" || event.getLine(1) == "" || plugin.isParsableToInt(event.getLine(3)) == false) {
 				    	event.getPlayer().sendMessage(ChatColor.RED + "Invalid RSP sign!");
 				    	event.setLine(2, "[Hidden Pass]");
 				    	return;
 				    }
 				    String location = event.getBlock().getLocation().toString();
 				    Persistance newClass = plugin.getDatabase().find(Persistance.class).where().ieq("location", location).findUnique();
 				    if (newClass == null) {
 				    	newClass = new Persistance(); 
 				    	newClass.setCreatorName(event.getPlayer().getName());
 				    	newClass.setLocation(event.getBlock().getLocation().toString());
 				    	newClass.setSignNick(event.getLine(1));
 				    	newClass.setPassword(event.getLine(2));
 				    	newClass.setTimer(Integer.parseInt(event.getLine(3)));
 				    	plugin.getDatabase().save(newClass);
 				    	String obfupass = plugin.obfupass(event.getLine(2));
 				    	event.setLine(0, ChatColor.LIGHT_PURPLE + event.getLine(1));
 				    	event.setLine(1, "");
 				    	event.setLine(2, ChatColor.GREEN + "Password:");
 				    	event.setLine(3, ChatColor.GREEN + obfupass);
 				    	return;
 				    }
 				    else {
 				    	event.getPlayer().sendMessage(ChatColor.RED + "Unexpected error occurred! Database still shows a previous RSP sign at this location!");
 				    	return;
 				    }
 			}
 		}
 	    
 	    //Begin Block Break Listener
 	    @EventHandler(ignoreCancelled = true)
 		public void onBlockBreak(BlockBreakEvent event) {
 			if(event.isCancelled()) return;
 			if(event.getBlock().getState() instanceof Sign){
				Sign sign = (Sign)event.getBlock().getState();
 				Persistance breakClass = plugin.getDatabase().find(Persistance.class).where().eq("location", event.getBlock().getLocation().toString()).findUnique();
 				if (breakClass == null) {
 					return;
 				}
 				else {
 					if (event.getPlayer().getName() == breakClass.getCreatorName() || event.getPlayer().isOp() || event.getPlayer().hasPermission("RSPassword.breakany")) {
 						plugin.getDatabase().delete(breakClass);
 						event.getPlayer().sendMessage(ChatColor.GREEN + "RSPassword sign destroyed!");
 						return;
 					}
 					else {
 						event.getPlayer().sendMessage(ChatColor.RED + "You do not have permission to break this RSP sign.");
 						event.setCancelled(true);
 					}
 				}
 			}
 		}
 }
