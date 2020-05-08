 package com.github.computerdude5000.terracrafttools.modules;
 
 import com.github.computerdude5000.terracrafttools.TerraCraftTools;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.TNTPrimed;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.HandlerList;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.ProjectileLaunchEvent;
 import org.bukkit.event.player.PlayerEggThrowEvent;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Administrator
  * Date: 4/13/13
  * Time: 9:34 PM
  * To change this template use File | Settings | File Templates.
  */
 public class EggXplosion implements SuperSoapToolsModule, Listener
 {
     TerraCraftTools plugin;
 
     public void initModule(TerraCraftTools sst)
     {
         plugin = sst;
         plugin.getServer().getPluginManager().registerEvents(this, plugin);
         plugin.logger.info("[TerraCraftTools][EggXplosion] Module Enabled!");
 
     }
 
     public void deinitModule()
     {
         HandlerList.unregisterAll(this);
         plugin.logger.info("[TerraCraftTools][EggXplosion] Module Disabled!!");
         plugin = null;
     }
 
     public boolean callCommand(CommandSender sender, Command command, String label, String[] args)
     {
         return false;
     }
 
     @EventHandler(priority = EventPriority.MONITOR)
     public void onPlayerEggThrow(PlayerEggThrowEvent event)
     {
         Player p = event.getPlayer();
         Location L = event.getEgg().getLocation();
         World W = event.getPlayer().getWorld();
 
         if (plugin.getPermissionsApi().has(p, "infiniumcrafttools.eggs"))
         {
             event.setHatching(false);
            W.createExplosion(L, 1f);
 
 
         }
 
     }
 }
