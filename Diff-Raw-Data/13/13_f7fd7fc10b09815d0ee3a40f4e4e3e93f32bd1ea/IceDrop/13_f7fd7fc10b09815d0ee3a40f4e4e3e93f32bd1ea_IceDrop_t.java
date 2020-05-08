 package com.github.computerdude5000.terracrafttools.modules;
 
 import com.github.computerdude5000.terracrafttools.TerraCraftTools;
 import org.bukkit.GameMode;
 import org.bukkit.Material;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.HandlerList;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.entity.ItemSpawnEvent;
 import org.bukkit.inventory.ItemStack;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Administrator
  * Date: 4/14/13
  * Time: 12:03 AM
  * To change this template use File | Settings | File Templates.
  */
 public class IceDrop implements SuperSoapToolsModule, Listener
 {
     TerraCraftTools plugin;
 
     public void initModule(TerraCraftTools sst)
     {
         plugin = sst;
         plugin.getServer().getPluginManager().registerEvents(this, plugin);
         plugin.logger.info("[TerraCraftTools][IceDrops] Module Enabled!");
 
     }
 
     public void deinitModule()
     {
         HandlerList.unregisterAll(this);
         plugin.logger.info("[TerraCraftTools][IceDrops] Module Disabled!");
         plugin = null;
     }
 
     public boolean callCommand(CommandSender sender, Command command, String label, String[] args)
     {
        return false;
     }
 
     @EventHandler(priority = org.bukkit.event.EventPriority.MONITOR)
     public void onBlockBreak(BlockBreakEvent event)
     {
         if (!event.isCancelled() && event.getBlock().getType() == Material.ICE && event.getPlayer().getGameMode() != GameMode.CREATIVE && plugin.getPermissionsApi().has(event.getPlayer(), "infiniumcrafttools.donor.ice"))
         {
            event.setCancelled(true);
             event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(Material.ICE, 1));
            event.getBlock().setTypeId(0);
             return;
         }
     }
 }
