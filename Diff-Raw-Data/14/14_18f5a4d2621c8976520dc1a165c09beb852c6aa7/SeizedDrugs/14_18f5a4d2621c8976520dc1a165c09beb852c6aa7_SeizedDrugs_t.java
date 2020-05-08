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
 
 package net.dragon_weed.plugins.seizeddrugs;
 
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
 import com.sk89q.worldguard.protection.ApplicableRegionSet;
 import com.sk89q.worldguard.protection.managers.RegionManager;
 import com.sk89q.worldguard.protection.regions.ProtectedRegion;
 /**
  *
  * @author tux
  */
 public class SeizedDrugs extends JavaPlugin implements Listener {
 	private enum BeatdownResult {
 		HIT,
 		MISS,
 		BEAT
 	}
 	
 	private enum Mode {
 		DRUG_SEIZE,
 		BEATDOWN
 	}
 	
     private Map<String, Integer> copInfo = new HashMap<String, Integer>();
     private Map<String, Integer> beatdownInfo = new HashMap<String, Integer>();
     private Map<String, Mode> copModes = new HashMap<String, Mode>();
     private Random rnd = new Random();
     private WorldGuardPlugin wgplugin = null;
     private Logger log = Logger.getLogger("Minecraft");
     private String filePath;
     
     /**
      * Given a player name, return the current health value of the player in beatdown mode.
      * 
      * @param player A player (as a string, not a Player)
      * @return health value as a Integer
      */
 
     public Integer getBeatdownHealth(String player) {
         if(beatdownInfo.containsKey(player)) {
             return beatdownInfo.get(player);
         } else {
             return getConfig().getInt("beatdown-health", 20);
         }
     }
    
     /**
      * Set a player's beatdown health. This function can be used to give bluffs, for example.
      * This is not affected by the max beatdown health value.
      * 
      * @param player (as a String, not a Player)
      * @param health an Integer
      */
     public void setBeatdownHealth(String player, Integer health) {
         if(beatdownInfo.containsKey(player)) {
             beatdownInfo.remove(player);
             beatdownInfo.put(player, health);
         } else {
         	beatdownInfo.put(player, health);
         }
     }
     
     private boolean canBeatHere(Player c) {
     	List<String> regions = getConfig().getStringList("ban-arrest-regions");
 		if(wgplugin != null) {
 			RegionManager regionManager = wgplugin.getRegionManager(c.getWorld());
 			if(regionManager != null) {
     			ApplicableRegionSet set = regionManager.getApplicableRegions(c.getLocation());
     			for (ProtectedRegion region : set) {
     				if(regions.contains(region.getId())) {
     					return false;
     				}
     			}
 			}
 		}
 		return true;
     }
     
     private BeatdownResult beatdown(Player caught) {
         if(!beatdownInfo.containsKey(caught.getName())) {
             beatdownInfo.put(caught.getName(), getConfig().getInt("beatdown-health", 20));
         }
         if(rnd.nextInt(100) <= getConfig().getInt("per-beat-miss", 1)) {
             return BeatdownResult.MISS;
         } else {
             Integer s = beatdownInfo.get(caught.getName()) - getConfig().getInt("per-beat-health");
             beatdownInfo.remove(caught.getName());
             if(s<=0) {
                 return BeatdownResult.BEAT;
             } else {
             	beatdownInfo.put(caught.getName(), s);
                 return BeatdownResult.HIT;
             }
         }
     }
     
     private boolean badCopHandler(Player policeman, boolean good) {
     	// Bad donut eater, bad!
         if(!good) {
             if(!copInfo.containsKey(policeman.getName())) {
                 copInfo.put(policeman.getName(), 1);
             } else {
                 Integer k = copInfo.get(policeman.getName()) + 1;
                 copInfo.remove(policeman.getName());
                 copInfo.put(policeman.getName(), k);
             }
             return false;
         } else {
             if(copInfo.containsKey(policeman.getName())) {
                 copInfo.remove(policeman.getName());
             }
         }
         return true;
     }
     
     /**
      * Seize a player's drugs.
      * 
      * @return true if drugs were found and false if not, increasing/resetting cop offense
      */
     @SuppressWarnings("deprecation")
     private boolean seize(Player policeman, Player p) {
    	ArrayList<ItemStack> drugs = new ArrayList<ItemStack>();
         ItemStack[] i = p.getInventory().getContents();
         // Do a simple scan first to count item stacks.
         // We'll cache the results to speed up removal as well:
         for (ItemStack item : i) {
             if (item != null) {
                 if(isDrug(item.getTypeId(), item.getDurability())) {
                	drugs.add(item);
                 }
             }
         }
         
         // Check if we seized drugs
        if(drugs.size() < getConfig().getInt("num-stacks-required-to-arrest", 1)) {
         	return badCopHandler(policeman, false);
         }
         
         // Now do the actual removal.
         // We filtered the list earlier
        for (ItemStack item : drugs.toArray(i)) {
             p.getInventory().remove(item);
             if(!getConfig().getBoolean("destroy-items")) {
                 if(policeman.getInventory().firstEmpty() == -1) {
                     if(!getConfig().getBoolean("destroy-items-if-inv-full")) {
                         p.getWorld().dropItemNaturally(policeman.getLocation(), item);
                     }
                 } else {
                     policeman.getInventory().addItem(item);
                 }
             }
         }
         p.updateInventory();
         policeman.updateInventory();
 
         return badCopHandler(policeman, true);
     }
     
     /**
      * Given a cop's name, return how many incorrectly-performed seizures they have performed.
      * This function could be used to inflict other punishments that are more than the vanilla jailing.
      * 
      * @param co The cop's name (as a String, not a Player)
      * @return the times they have incorrectly caught people
      */
     public Integer getCopIncorrectSeizure(String co) {
         if(copInfo.containsKey(co)) {
             return copInfo.get(co);
         } else {
             return 0;
         }
     }
     
     @SuppressWarnings("unchecked")
 	@Override
     public void onEnable() {
     	filePath = this.getDataFolder().getAbsolutePath() + "/badCops.dat";
         this.getDataFolder().mkdirs();
         getConfig().options().copyDefaults(true);
         if(!getConfig().contains("drugs")) {
         	log.info("Adding default drug configuration.");
         	getConfig().set("drugs.353", true);
         	getConfig().set("drugs.339", true);
         	getConfig().set("drugs.372", true);
         	getConfig().set("drugs.296", true);
         	getConfig().set("drugs.351:2", true);
         	getConfig().set("drugs.351:1", true);
         	getConfig().set("drugs.40", true);
         	getConfig().set("drugs.39", true);
         	getConfig().set("drugs.351:15", true);
         	getConfig().set("drugs.351:3", true);
         }
         this.saveConfig();
         getServer().getPluginManager().registerEvents(this, this);
         try {
             ObjectInputStream s = new ObjectInputStream(new FileInputStream(filePath));
             try {
                 copInfo = (HashMap<String,Integer>)s.readObject();
             } catch (ClassNotFoundException ex) {
             }
             s.close();
         } catch (IOException ex) {
         	log.info("Error while loading cop data, starting from scratch...");
             copInfo = new HashMap<String, Integer>();
             log.info("More info: "+ex.getMessage());
         }
     	Plugin wgTmpPlugin = getServer().getPluginManager().getPlugin("WorldGuard");
     	if(wgTmpPlugin == null) {
     		log.info("WorldGuard not found. Region-specific features are disabled.");
     	} else {
     		wgplugin = (WorldGuardPlugin)wgTmpPlugin;
     	}
         log.info("SeizedDrugs plugin enabled");
     }
     
     @Override
     public void onDisable() {
         try {
             ObjectOutputStream s = new ObjectOutputStream(new FileOutputStream(filePath));
             s.writeObject(copInfo);
             s.close();
         } catch (IOException ex) {
         	log.info("Error while saving cop data!");
             log.info("More info: "+ex.getMessage());
         }
         this.saveConfig();
         wgplugin = null;
         copInfo = null;
         copModes = null;
         beatdownInfo = null;
         rnd = null;
         log = null;
     }
     
     private boolean isDrug(int it, int id) {
         String b = "";
         if(id==0) {
             b = b + it;
         } else {
             b = b + it+":"+id;
         }
         
         if(getConfig().getBoolean("drugs."+b, false)) {
         	return true;
         } else {
         	return getConfig().getBoolean("drugs."+it+":*", false);
         }
     }
     
     private boolean canUseMode(String user, Mode m) {
     	// Configuration
     	if(getConfig().getBoolean("beatdown-only", false) && m == Mode.DRUG_SEIZE) {
     		return false;
     	}
     	if(getConfig().getBoolean("seize-only", false) && m == Mode.BEATDOWN) {
     		return false;
     	}
     	// Permissions
     	if(m == Mode.DRUG_SEIZE && !getServer().getPlayerExact(user).hasPermission("seizeddrugs.use.seize")) {
     		return false;
     	}
     	if(m == Mode.BEATDOWN && !getServer().getPlayerExact(user).hasPermission("seizeddrugs.use.beatdown")) {
     		return false;
     	}
     	return true;
     }
     
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
     	// Case 1: Police and rogue admins
     	if(!sender.hasPermission("seizeddrugs.use")) {
             sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
             return true;
         }
         if (args.length < 1) {
            sender.sendMessage("No arguments provided!");
            if(sender.hasPermission("seizeddrugs.admin")) {
 	           sender.sendMessage("/police check <name>: Check player status");
 	           sender.sendMessage("/police reload: Reload plugin");
 	           sender.sendMessage("/police reset: Reset cop data");
 	           sender.sendMessage("/police beatreset: Reset beatdown health for all players");
            }
            if(sender instanceof Player) {
         	   sender.sendMessage("/police mode: Switch from beatdown mode to drug seize mode and vice versa");
            }
            return true;
         }
         
         if("mode".equals(args[0]) && sender instanceof Player) {
         	Mode s = copModes.get(sender.getName());
         	switch(s) {
 			case BEATDOWN:
 				if(canUseMode(sender.getName(), Mode.DRUG_SEIZE)) {
 					copModes.remove(sender.getName());
 					copModes.put(sender.getName(), Mode.DRUG_SEIZE);
 					sender.sendMessage(ChatColor.GOLD + "Changed to drug seizing mode.");
 				} else {
 					sender.sendMessage(ChatColor.RED + "You are restricted from changing to another mode.");
 				}
 				break;
 			case DRUG_SEIZE:
 				if(canUseMode(sender.getName(), Mode.BEATDOWN)) {
 					copModes.remove(sender.getName());
 					copModes.put(sender.getName(), Mode.BEATDOWN);
 					sender.sendMessage(ChatColor.GOLD + "Changed to beatdown mode.");
 				} else {
 					sender.sendMessage(ChatColor.RED + "You are restricted from changing to another mode.");
 				}
 				break;
 			default:
 				break;
         	}
         }
         // Case 2: Admin
     	if(!sender.hasPermission("seizeddrugs.admin")) {
             sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
             return true;
         }
         if("reload".equals(args[0])) {
             this.reloadConfig();
             sender.sendMessage("Plugin configuration reloaded!");
         }
         if("check".equals(args[0])) {
             if(args.length < 2) {
                 sender.sendMessage("You must specify the player to check.");
                 return true;
             }
             sender.sendMessage("Incorrect seizures for "+args[1]+": "+this.getCopIncorrectSeizure(args[1]));
         }
         if("reset".equals(args[0])) {
             copInfo.clear();
             sender.sendMessage("All cop statuses cleared!");
         }
         if("beatreset".equals(args[0])) {
             this.beatdownInfo.clear();
             sender.sendMessage("Beatdown health restored for all players.");
         }
         return true;
     }
     
     @EventHandler
     public void onPlayerJoin(PlayerJoinEvent evt) {
     	if(evt.getPlayer().hasPermission("seizeddrugs.use")) {
     		if(!copModes.containsKey(evt.getPlayer().getName())) {
     			if(getConfig().getBoolean("beatdown-only", false)) {
     				copModes.put(evt.getPlayer().getName(), Mode.BEATDOWN);
     			} else {
     				copModes.put(evt.getPlayer().getName(), Mode.DRUG_SEIZE);
     			}
     		}
     	}
     }
     
     @EventHandler
     public void onEntityDamageByEntity(EntityDamageByEntityEvent evt) {
         if(evt.isCancelled() && !getConfig().getBoolean("ignore-pvp-restrictions")) {
             return;
         }
         if(evt.getEntity() instanceof Player && evt.getDamager() instanceof Player && evt.getCause() == DamageCause.ENTITY_ATTACK) {
             Player cop = (Player)evt.getDamager();
             Player caught = (Player)evt.getEntity();
             if(cop.getItemInHand().getTypeId() == getConfig().getInt("police-item-id") && cop.hasPermission("seizeddrugs.use") && !caught.hasPermission("seizeddrugs.exempt") && canBeatHere(caught)) {
             	// We are going to be mode-dependent here.
                 switch(copModes.get(cop.getName())) {
                 case BEATDOWN:
                 	performBeatdown(cop, caught);
                 	break;
                 case DRUG_SEIZE:
                     performSeize(cop, caught);
                     break;
                 default:
                 	log.severe("Improper copMode! This is a bug, please report.");
                 }
                 evt.setCancelled(true);
             }
         }
     }
     
     private void executeJailServerCommand(String executor, String username, String duration) {
         String command = getConfig().getString("jail-command");
         command = command.replaceAll("%username%", username);
         command = command.replaceAll("%duration%", duration);
         command = command.replaceAll("%cop%", executor);
         // CommandSender may vary.
         // config.yml will dictate if the command is to be executed as the cop or the console
         // The default is the console.
         CommandSender s = getServer().getConsoleSender();
         if(getConfig().getBoolean("run-command-as-cop")) {
         	s = (CommandSender)getServer().getPlayer(executor);
         }
         getServer().dispatchCommand(s, command);
     }
     
     private void performSeize(Player cop, Player caught) {
         if(seize(cop, caught)) {
             executeJailServerCommand(cop.getName(), caught.getName(), getConfig().getString("jail-duration-for-player"));
             caught.sendMessage(formatMessage(getConfig().getString("caught-player"), caught, cop));
             cop.sendMessage(formatMessage(getConfig().getString("cop-congratulation"), caught, cop));
         } else {
         	int threshold = getConfig().getInt("cop-threshold");
             if(threshold > 0) {
                 // Fuck the cop.
                 Integer co = getCopIncorrectSeizure(cop.getName());
                 cop.sendMessage(formatMessage(getConfig().getString("cop-warning"), caught, cop));
                 if(co > threshold) {
                     executeJailServerCommand(cop.getName(), cop.getName(), getConfig().getString("jail-duration-for-cop"));
                     cop.sendMessage(formatMessage(getConfig().getString("cop-jailed"), cop, cop));
                 }
             }
         }
     }
 
     private void performBeatdown(Player cop, Player caught) {
         switch(beatdown(caught)) {
             case HIT:
                 cop.sendMessage(formatMessage(getConfig().getString("beatdown-hit"), caught, cop));
                 caught.sendMessage(formatMessage(getConfig().getString("beatdown-player-hit"), caught, cop));
                 break;
             case MISS:
                 cop.sendMessage(formatMessage(getConfig().getString("beatdown-miss"), caught, cop));
                 break;
             case BEAT:
                 cop.sendMessage(formatMessage(getConfig().getString("beatdown-beat"), caught, cop));
                 caught.sendMessage(getConfig().getString("beatdown-player"));
                 executeJailServerCommand(cop.getName(), caught.getName(), getConfig().getString("jail-duration-for-player"));
                 break;
         }
     }
     
     // General message formatting function(s).
     private String formatMessage(String msg, Player player, Player cop) {
         String m = msg.replace("%health%", Integer.toString(getBeatdownHealth(player.getName())));
         m = m.replace("%max%", Integer.toString(getConfig().getInt("beatdown-health", 20)));
         m = m.replace("%player%", player.getName());
         m = m.replace("%cop%", cop.getName());
         m = m.replace("%times%", getCopIncorrectSeizure(cop.getName()).toString());
         return ChatColor.translateAlternateColorCodes('&', m);
     }
 }
