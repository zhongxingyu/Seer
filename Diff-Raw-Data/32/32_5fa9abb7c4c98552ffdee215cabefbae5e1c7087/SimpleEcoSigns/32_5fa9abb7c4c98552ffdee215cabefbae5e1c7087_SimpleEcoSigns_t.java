 /*
  * Copyright (c) 2012, tuxed
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * 
  * 	* Redistributions of source code must retain the above copyright
  * 	notice, this list of conditions and the following disclaimer.
  * 	* Redistributions in binary form must reproduce the above copyright
  * 	notice, this list of conditions and the following disclaimer in the
  * 	documentation and/or other materials provided with the distribution.
  * 	* Neither the name of tuxed nor the
  *	names of its contributors may be used to endorse or promote products
  * 	derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL TUXED BE LIABLE FOR ANY
  * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
/*
 * Some edits added by aabmass! 
 */

 package com.tropicalwikis.tuxcraft.plugins.simpleecosigns;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
import java.util.Scanner;
 
 import net.milkbowl.vault.economy.Economy;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.block.SignChangeEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class SimpleEcoSigns extends JavaPlugin implements Listener {
 	private Economy econ;
 	private HashMap<String, String> namesToIds = new HashMap<String, String>();
 	@Override
 	public void onEnable() {
 		if (getServer().getPluginManager().getPlugin("Vault") != null) {
 			RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
 			if (rsp != null) {
 				econ = rsp.getProvider();
 			}
 		} else {
 			getLogger().severe("Vault not found! Disabling plugin.");
 			getServer().getPluginManager().disablePlugin(this);
 			return;
 		}
 		getServer().getPluginManager().registerEvents(this, this);
 		try {
			File pluginFolder = this.getDataFolder();
			if (!pluginFolder.exists()) {
				pluginFolder.mkdir();
			}
			
			Scanner scan = new Scanner(new File(pluginFolder, "items.csv"));
			while (scan.hasNextLine()) {
				String line = scan.nextLine();
				String[] values = line.split(",");
				if (!line.startsWith("#") && values.length == 3) {
					namesToIds.put(values[0].toLowerCase(), values[1] + ":" + values[2]);
 				}
 			}
			getLogger().info("Successfully loaded items.csv!");
			scan.close();
 		} catch (IOException e) {
 			getLogger().info("Could not load items.csv; this is okay. We will use Bukkit names for materials.");
 		}
 	}
 	
     @EventHandler
     public void signChange(SignChangeEvent event) {
         if (event.getLine(0).equalsIgnoreCase("[Buy]") || event.getLine(0).equalsIgnoreCase("[Sell]")) {
             // Verify the sign:
         	// [Buy]
         	// 1
         	// Diamond
         	// $5000
         	try {
         		Integer.valueOf(event.getLine(1));
         		String s2 = event.getLine(2);
         		String s3 = s2;
         		if(s2.contains(":")) {
         			s3 = s2.split(":")[0];
         		}
         		try {
         			Integer.valueOf(s3);
         		} catch (NumberFormatException e) {
         			if(!namesToIds.containsKey(s3.toLowerCase()))
         				if(Material.matchMaterial(s3) == null)
         					throw new Exception();
         		}
         		Double.valueOf(event.getLine(3).replace("$", ""));
         	} catch (Exception e) {
         		event.setLine(0, ChatColor.DARK_RED + event.getLine(0));
         		event.getPlayer().sendMessage(ChatColor.RED + "Sign format invalid");
         		return;
         	}
             if (event.getPlayer().hasPermission("simpleecosigns.create")) {
                 event.setLine(0, ChatColor.DARK_BLUE + event.getLine(0));
             } else {
                 event.setLine(0, ChatColor.DARK_RED + event.getLine(0));
             }
         }
     }
     
     @SuppressWarnings("deprecation")
 	@EventHandler
     public void onPlayerInteract(PlayerInteractEvent event) {
         Player p = event.getPlayer();
         if (event.hasBlock() && event.getClickedBlock().getState() instanceof Sign && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
             Sign s = (Sign) event.getClickedBlock().getState();
             if(this.blockIsValidEcoSign(s)) {
             	if(s.getLine(0).matches(ChatColor.DARK_BLUE + "(?i)\\[Buy\\]")) {
             		if(econ.getBalance(p.getName()) >= Double.valueOf(s.getLine(3).replace("$", ""))) {
             			econ.withdrawPlayer(p.getName(), Double.valueOf(s.getLine(3).replace("$", "")));
             			if(p.getInventory().firstEmpty() == -1) {
             				p.sendMessage(ChatColor.RED + "You do not have enough inventory space. The items will be dropped on the ground.");
             				p.getWorld().dropItemNaturally(p.getLocation(), nameToItemStack(s.getLine(2), Integer.valueOf(s.getLine(1))));
             			} else {
             				p.getInventory().addItem(nameToItemStack(s.getLine(2), Integer.valueOf(s.getLine(1))));
             			}
             			p.sendMessage(ChatColor.GREEN + "Item purchased.");
             		} else {
             			p.sendMessage(ChatColor.RED + "You do not have enough funds.");
             		}
             		p.updateInventory();
             	}
             	if(s.getLine(0).matches(ChatColor.DARK_BLUE + "(?i)\\[Sell\\]")) {
             		// I have to do this backwards-ass way because Bukkit sucks.
             		Integer amt = Integer.valueOf(s.getLine(1));
             		Integer s2 = 0;
             		ItemStack i = nameToItemStack(s.getLine(2), amt);
             		if(!p.getInventory().containsAtLeast(i, amt)) {
             			p.sendMessage(ChatColor.RED + "You don't have enough items in your inventory.");
             			return;
             		}
             		for(ItemStack item : p.getInventory().getContents()) {
             			if(item != null && item.isSimilar(i) && s2 < amt) {
             				p.getInventory().removeItem(item);
             				s2 = s2 + item.getAmount();
             			}
             		}
             		p.updateInventory();
             		if(s2 > amt) {
             			s2 = s2 - amt;
             			p.getInventory().addItem(new ItemStack(i.getTypeId(), s2, i.getDurability()));
             			p.updateInventory();
             		}
         			econ.depositPlayer(p.getName(), Double.valueOf(s.getLine(3).replace("$", "")));
         			p.sendMessage(ChatColor.GREEN + "Item sold.");
         			p.updateInventory();
         			return;
             	}
             }
         }
     }
     
     private ItemStack nameToItemStack(String name, Integer amt) {
     	// Let's make life a bit easier on us:
     	name = name.toLowerCase();
     	if(name.contains(":")) {
     		try {
     			ItemStack tmp = new ItemStack(Integer.valueOf(name.split(":")[0]), amt, Short.valueOf(name.split(":")[1]));
     			return tmp;
     		} catch (NumberFormatException e) {
 	    		if(!namesToIds.containsKey(name.split(":")[0]))
 	    			return new ItemStack(Material.matchMaterial(name.split(":")[0]), amt, Short.valueOf(name.split(":")[1]));
 	    		else {
 	    			String s2 = name.split(":")[0];
 	    			String s3 = name.split(":")[1];
 	    			return new ItemStack(Integer.valueOf(namesToIds.get(s2).split(":")[0]), amt, Short.valueOf(s3));
 	    		}
     		}
     	} else {
     		try {
     			ItemStack tmp = new ItemStack(Integer.valueOf(name), amt);
     			return tmp;
     		} catch (NumberFormatException e) {
     			if(!namesToIds.containsKey(name.toLowerCase()))
     				return new ItemStack(Material.matchMaterial(name), amt);
     			else
     				return new ItemStack(Integer.valueOf(namesToIds.get(name).split(":")[0]), amt, Short.valueOf(namesToIds.get(name).split(":")[1]));
     		}
     	}
     }
 
     @EventHandler
     public void blockPlace(BlockPlaceEvent event) {
         if (event.getBlock() instanceof Sign) {
             Sign sign = (Sign) event.getBlock().getState();
             // Don't let the user make this an auth'd sign
             if (sign.getLine(0).matches(ChatColor.DARK_BLUE + "(?i)\\[Buy\\]") || sign.getLine(0).matches(ChatColor.DARK_BLUE + "(?i)\\[Sell\\]")) {
                 event.setCancelled(true);
                 return;
             }
         }
         if (event.getBlockAgainst().getState() instanceof Sign && blockIsValidEcoSign((Sign)event.getBlockAgainst().getState())) {
         	event.setCancelled(true);
         }
     }
 
     @EventHandler
     public void blockBreak(BlockBreakEvent event) {
         if (!(event.getBlock().getState() instanceof Sign)) {
             return;
         }
         Sign s = (Sign) event.getBlock().getState();
         if (blockIsValidEcoSign(s)) {
             if (!event.getPlayer().hasPermission("simpleecosigns.destroy")) {
                 event.setCancelled(true);
             }
         }
     }
     
     private boolean blockIsValidEcoSign(Sign s) {
     	try {
     		Integer.valueOf(s.getLine(1));
     		String s2 = s.getLine(2);
     		String s3 = s2;
     		if(s2.contains(":")) {
     			s3 = s2.split(":")[0];
     		}
     		try {
     			Integer.valueOf(s3);
     		} catch (NumberFormatException e) {
     			if(!namesToIds.containsKey(s3.toLowerCase()))
     				if(Material.matchMaterial(s3) == null)
     					return false;
     		}
     		Double.valueOf(s.getLine(3).replace("$", ""));
     	} catch (Exception e) {
     		return false;
     	}
     	return s.getLine(0).matches(ChatColor.DARK_BLUE + "(?i)\\[Buy\\]") || s.getLine(0).matches(ChatColor.DARK_BLUE + "(?i)\\[Sell\\]");
     }
 }
