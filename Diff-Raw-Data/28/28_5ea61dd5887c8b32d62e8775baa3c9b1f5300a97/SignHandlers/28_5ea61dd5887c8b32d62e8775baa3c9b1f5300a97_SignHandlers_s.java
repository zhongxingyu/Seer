 package com.github.terror3659.signfacilities;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.block.SignChangeEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 
 
 
 public class SignHandlers implements Listener {
 
 	public FileConfiguration conf;
 	public MainCls instance;
 	
 	
 	public Facility f;
 	
 	public Facility recycler;
 	public Facility stockpile;
 	public Facility cistern;
 	
 	public SignHandlers(FileConfiguration config, MainCls inst){
 		conf = config;
 		instance = inst;
 		
 		f = new Facility(config, inst);
 		
 		recycler = new FacilityRecycler(config);
 		stockpile = new FacilityStockpile(config, inst);
 		cistern = new FacilityCistern(config, inst);
 	}
 	
 	//TODO: Make all this a lot less confusing.
 	
 	@EventHandler
 	public void onCreateSign(SignChangeEvent e){
 		Player plr = e.getPlayer();
 		
 		if(conf.getBoolean("SignRecycler.Enabled")){
 			if(e.getLine(0).equalsIgnoreCase(recycler.getTag())){
				if(!conf.getBoolean("SignRecycler.CreativeAllow")){
 					plr.sendMessage(ChatColor.RED+ "You cannot create recyclers in creative mode!");
 					e.setLine(0, "[DENIED]");
 				}
 				if(!plr.hasPermission("signfacilities.create.recycler")){
 					plr.sendMessage(ChatColor.RED+"You don't have permission to create a recycler.");
 					e.setLine(0, "[DENIED]");
 				}else{	
 					e.setLine(0, recycler.getTag());
 					plr.sendMessage(ChatColor.GREEN + "Recycler successfully created!");
 					plr.sendMessage(ChatColor.GRAY + "Right-Click"+ChatColor.GREEN
 							+" on the sign, while holding an item to recycle it.");
 					plr.sendMessage(ChatColor.RED+"NOTE: "+ChatColor.GREEN
 							+"Not all items are recyclable!");
 				}
 			}
 		}
 		
 		if(conf.getBoolean("SignStockpile.Enabled")){
 			if(e.getLine(0).equalsIgnoreCase(stockpile.getTag())){
				if(!conf.getBoolean("SignStockpile.CreativeAllow")){
 					plr.sendMessage(ChatColor.RED+ "You cannot create stockpiles in creative mode!");
 					e.setLine(0, "[DENIED]");
 				}
 				if(!plr.hasPermission("signfacilities.create.stockpile")){
 					plr.sendMessage(ChatColor.RED+"You don't have permission to create a stockpile.");
 					e.setLine(0, "[DENIED]");
 				}else{
 					e.setLine(0, stockpile.getTag());
 					e.setLine(1, plr.getName());
 					e.setLine(2, "");
 					e.setLine(3, "Empty");
 					plr.sendMessage(ChatColor.GREEN + "Stockpile successfully created!");
 					plr.sendMessage(ChatColor.GRAY + "Right-Click "+ ChatColor.GREEN
 							+"to add items to the stockpile. Only one type of item can be held in a stockpile.");
 					plr.sendMessage(ChatColor.GREEN + "To take items out of the stockpile, "+ChatColor.GRAY
 							+"Crouch and Right-Click "+ ChatColor.GREEN + "to take one item out, or "
 							+ChatColor.GRAY + "Crouch and Left-Click "+ ChatColor.GREEN + "to take a stack out");
 					plr.sendMessage(ChatColor.RED + "NOTE:"+ChatColor.GREEN+" If you break the sign, all the items stored will be dropped on the ground.");
 				}
 			}
 		}
 		
 		if(conf.getBoolean("SignCistern.Enabled")){
 			if(e.getLine(0).equalsIgnoreCase(cistern.getTag())){	
				if(!conf.getBoolean("SignCistern.CreativeAllow")){
 					plr.sendMessage(ChatColor.RED+ "You cannot create cisterns in creative mode!");
 					e.setLine(0, "[DENIED]");
 				}
 				if(!plr.hasPermission("signfacilities.create.cistern")){
 					plr.sendMessage(ChatColor.RED+"You don't have permission to create a cistern.");
 					e.setLine(0, "[DENIED]");
 				}else{
 					e.setLine(0, cistern.getTag());
 					e.setLine(1, plr.getName());
 					e.setLine(2, "");
 					e.setLine(3, "Empty");
 					plr.sendMessage(ChatColor.GREEN + "Cistern successfully created!");
 					plr.sendMessage(ChatColor.GRAY + "Right-Click "+ ChatColor.GREEN +
 					"with a "+ChatColor.GRAY+"water, lava or milk bucket"+ChatColor.GREEN
 					+" to store it in the cistern.");
 					plr.sendMessage(ChatColor.GRAY + "Right-Click "+ ChatColor.GREEN +
 							"with an "+ChatColor.GRAY+"empty bucket"+ChatColor.GREEN
 							+" to take the liquid out.");
 				}
 			}
 		}
 	}
 	
 	
 	@EventHandler
 	public void onSignInteract(PlayerInteractEvent e){
 		
 		
 		Block b = e.getClickedBlock();
 		if(b == null) return;
 			
 		if(b.getType() == Material.WALL_SIGN || b.getType() == Material.SIGN_POST){
 				
 			Sign s = (Sign) b.getState();
 			if(conf.getBoolean("SignRecycler.Enabled")){
 			if(s.getLine(0).equalsIgnoreCase(recycler.getTag())){
 				
 				if(e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
 				Player plr = e.getPlayer();
 					
 				if(!plr.hasPermission("signfacilities.use.recycler")){
 					plr.sendMessage(ChatColor.RED + "You don't have permission to use a recycler.");
 				}else{
 					recycler.Work(e);
 				}
 					
 			}
 				
 		}
 			
 			if(conf.getBoolean("SignStockpile.Enabled")){
 			if(s.getLine(0).equalsIgnoreCase(stockpile.getTag())){
 				
 				Player plr = e.getPlayer();
 					
 				if(e.getAction() != Action.RIGHT_CLICK_BLOCK && !plr.isSneaking()) return;
 					
 				if(!plr.hasPermission("signfacilities.use.stockpile")){
 					plr.sendMessage(ChatColor.RED + "You don't have permission to use a stockpile.");
 				}else{
 					if(s.getLine(1).equals(plr.getName()))
 					stockpile.Work(e, s);
 					else{
 						if(plr.hasPermission("signfacilities.admin.stockpile.other"))
 							stockpile.Work(e, s);
 						else
 							plr.sendMessage(ChatColor.RED+"You cannot use a stockpile that isn't yours.");
 					}
 				}
 					
 			}
 				
 		}
 			
 			
 			if(conf.getBoolean("SignCistern.Enabled")){
 			if(s.getLine(0).equalsIgnoreCase(cistern.getTag())){
 				
 				Player plr = e.getPlayer();
 					
 				if(e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
 					
 				if(!plr.hasPermission("signfacilities.use.cistern")){
 					plr.sendMessage(ChatColor.RED + "You don't have permission to use a cistern.");
 				}else{
 					if(s.getLine(1).equals(plr.getName()))
 					cistern.Work(e, s);
 					else{
 						if(plr.hasPermission("signfacilities.admin.cistern.other"))
 							cistern.Work(e, s);
 						else
 							plr.sendMessage(ChatColor.RED+"You cannot use a cistern that isn't yours.");
 					}
 				}
 					
 			}
 				
 		}
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onBlockPlace(BlockPlaceEvent e){
 		Block b = e.getBlockAgainst();
 		
 		
 		
 		if(b.getType() == Material.WALL_SIGN || b.getType() == Material.SIGN_POST){
 			
 			Sign s = (Sign) b.getState();
 			if(conf.getBoolean("SignRecycler.Enabled")){
 			if(s.getLine(0).equalsIgnoreCase(recycler.getTag())){
 				if(!e.getPlayer().isSneaking())
 					e.setCancelled(true);
 			}}
 			
 			if(conf.getBoolean("SignCistern.Enabled")){
 				if(s.getLine(0).equalsIgnoreCase(cistern.getTag())){
 					if(!e.getPlayer().isSneaking())
 						e.setCancelled(true);
 				}}
 			
 			if(conf.getBoolean("SignStockpile.Enabled")){
 				if(s.getLine(0).equalsIgnoreCase(stockpile.getTag())){
 						e.setCancelled(true);
 				}}
 			
 		}
 		
 	}
 	
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onBlockBreak(BlockBreakEvent e){
 		Block b = e.getBlock();
 		
 		if(b.getType() == Material.WALL_SIGN || b.getType() == Material.SIGN_POST){
 			
 			Sign s = (Sign) b.getState();
 			
 			if(conf.getBoolean("SignStockpile.Enabled")){
 				if(s.getLine(0).equalsIgnoreCase(stockpile.getTag())){
 				if( e.getPlayer().getName() != s.getLine(1)
 						&&!e.getPlayer().hasPermission("signfacilities.admin.stockpile.break")){
 						e.setCancelled(true);
 						e.getPlayer().sendMessage(ChatColor.RED 
 								+ "You can't break a stockpile that isn't yours!");
 						return;
 				}else{
 					stockpile.onDestroy(e, s);
 				}
 				}
 			}
 			
 			if(conf.getBoolean("SignCistern.Enabled")){
 				if(s.getLine(0).equalsIgnoreCase(cistern.getTag())){
 				if( e.getPlayer().getName() != s.getLine(1)
 						&&!e.getPlayer().hasPermission("signfacilities.admin.cistern.break")){
 						e.setCancelled(true);
 						e.getPlayer().sendMessage(ChatColor.RED 
 								+ "You can't break a cistern that isn't yours!");
 						return;
 				}else{
 					cistern.onDestroy(e, s);
 				}
 				}
 			}
 			
 		}
 		
 	}
 	
 }
