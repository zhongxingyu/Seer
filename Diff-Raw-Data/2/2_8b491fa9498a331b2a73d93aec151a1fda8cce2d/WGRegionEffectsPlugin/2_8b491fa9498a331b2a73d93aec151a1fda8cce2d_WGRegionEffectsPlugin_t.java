 /*
  * Copyright (C) 2012 mewin <mewin001@hotmail.de>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.mewin.WGRegionEffects;
 
 import com.mewin.WGCustomFlags.WGCustomFlagsPlugin;
 import com.mewin.WGCustomFlags.flags.CustomSetFlag;
 import com.mewin.WGRegionEffects.flags.PotionEffectDesc;
 import com.mewin.WGRegionEffects.flags.PotionEffectFlag;
 import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
 import com.sk89q.worldguard.protection.flags.RegionGroup;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 
 /**
  *
  * @author mewin <mewin001@hotmail.de>
  */
 public class WGRegionEffectsPlugin extends JavaPlugin {
     public static final CustomSetFlag EFFECT_FLAG = new CustomSetFlag("effects", new PotionEffectFlag("effect", RegionGroup.ALL));
     
     private WGCustomFlagsPlugin custPlugin;
     private WorldGuardPlugin wgPlugin;
     private WGRegionEffectsListener listener;
     private File confFile;
     private int tickDelay = 20;
     
     public static final Map<Player, List<PotionEffectDesc>> playerEffects = new HashMap<Player, List<PotionEffectDesc>>();
     public static List<Player> ignoredPlayers = new ArrayList<Player>();
     
     @Override
     public void onEnable()
     {
         Plugin plug = getServer().getPluginManager().getPlugin("WGCustomFlags");
      
         confFile = new File(this.getDataFolder(), "config.yml");
         
         if (plug == null || !(plug instanceof WGCustomFlagsPlugin) || !plug.isEnabled())
         {
             getLogger().warning("Could not load WorldGuard Custom Flags Plugin, disabling");
             getServer().getPluginManager().disablePlugin(this);
             return;
         }
         else
         {
             custPlugin = (WGCustomFlagsPlugin) plug;
         }
         
         plug = getServer().getPluginManager().getPlugin("WorldGuard");
         
         if (plug == null || !(plug instanceof WorldGuardPlugin) || !plug.isEnabled())
         {
             getLogger().warning("Could not load WorldGuard Plugin, disabling");
             getServer().getPluginManager().disablePlugin(this);
             return;
         }
         else
         {
             wgPlugin = (WorldGuardPlugin) plug;
         }
         loadConfig();
         
         listener = new WGRegionEffectsListener(wgPlugin, this);
         
         getServer().getPluginManager().registerEvents(listener, plug);
         
         custPlugin.addCustomFlag(EFFECT_FLAG);
         
         scheduleTask();
     }
     
     private void loadConfig()
     {
         confFile.getParentFile().mkdirs();
         getConfig().set("effect-duration", 2000);
         getConfig().set("effect-tick-delay", 1000);
         if (!confFile.exists())
         {
             try
             {
                 if (!confFile.createNewFile())
                 {
                     throw new IOException("Could not create configuration file.");
                 }
                 getLogger().log(Level.INFO, "Configuration does not exist. Creating default config.yml.");
                 getConfig().save(confFile);
             }
             catch(IOException ex)
             {
                 getLogger().log(Level.WARNING, "Could not write default configuration: ", ex);
             }
         }
         else
         {
             try
             {
                 getConfig().load(confFile);
             }
             catch(Exception ex)
             {
                 getLogger().log(Level.WARNING, "Could not load configuration:", ex);
             }
         }
         
         PotionEffectDesc.defaultLength = getConfig().getInt("effect-duration", 2000) / 50;
         tickDelay = getConfig().getInt("effect-tick-delay", 1000) / 50;
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
     {
         if (cmd.getName().equalsIgnoreCase("toggleeffects")
                 || cmd.getName().equalsIgnoreCase("te"))
         {
             if (sender instanceof Player)
             {
                 Player player = (Player) sender;
 
                 if (!player.hasPermission("effects.toggle"))
                 {
                     player.sendMessage(ChatColor.RED + "You don't have permission for that.");
                 }
                 else if (WGRegionEffectsPlugin.ignoredPlayers.contains(player))
                 {
                     WGRegionEffectsPlugin.ignoredPlayers.remove(player);
                     player.sendMessage(ChatColor.GOLD + "Region effects toggled on.");
                 }
                 else
                 {
                     WGRegionEffectsPlugin.ignoredPlayers.add(player);
                     player.sendMessage(ChatColor.GOLD + "Region effects toggled off.");
                 }
             }
             else
             {
                 sender.sendMessage("How could a console be affected by effects?");
             }
             return true;
         }
         return false;
     }
 
     private void scheduleTask()
     {
         getServer().getScheduler().scheduleSyncRepeatingTask(wgPlugin, new Runnable()
         {
 
             @Override
             public synchronized void run() {
                 for(Player p : getServer().getOnlinePlayers())
                 {
                     if (ignoredPlayers.contains(p))
                     {
                         continue;
                     }
                     List<PotionEffectDesc> effects;
                     try
                     {
                         effects = new ArrayList<PotionEffectDesc>(playerEffects.get(p));
                     }
                     catch(NullPointerException ex)
                     {
                         continue;
                     }
                     if (effects == null) {
                         continue;
                     }
                     Iterator<PotionEffectDesc> itr = effects.iterator();
                     {
                         CUR_POTION:
                         while(itr.hasNext())
                         {
                             PotionEffect effect = itr.next().createEffect();
                             Iterator<PotionEffect> itr2 = p.getActivePotionEffects().iterator();
 
                             while(itr2.hasNext())
                             {
                                 PotionEffect pe = itr2.next();
                                 
                                 if ((pe.getType().equals(PotionEffectType.POISON) || 
                                         pe.getType().equals(PotionEffectType.WITHER)) &&
                                         effect.getType().equals(pe.getType()) &&
                                         pe.getDuration() > 40)
                                 {
                                     continue CUR_POTION;
                                 }
                                 
                                 if (pe.getType() == effect.getType() && pe.getDuration() > effect.getDuration())
                                 {
                                     continue CUR_POTION;
                                 }
                             }
                             if (effect.getAmplifier() != -1)
                             {
                                 p.addPotionEffect(effect, true);
                             }
                             else
                             {
                                 p.removePotionEffect(effect.getType());
                             }
                         }
                     }
                 }
             }
             
        }, 5L, tickDelay);
     }
 }
