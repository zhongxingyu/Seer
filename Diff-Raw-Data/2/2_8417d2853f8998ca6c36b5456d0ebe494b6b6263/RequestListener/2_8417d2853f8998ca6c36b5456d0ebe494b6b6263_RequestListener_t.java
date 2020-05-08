 package me.Josvth.Trade.Listeners;
 
 import java.util.HashMap;
 import me.Josvth.Trade.Handlers.LanguageHandler;
 import me.Josvth.Trade.Trade;
 import org.bukkit.Server;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerInteractEntityEvent;
 import org.bukkit.plugin.PluginManager;
 
 public class RequestListener
   implements Listener
 {
   Trade plugin;
 
   public RequestListener(Trade instance)
   {
     plugin = instance;
     plugin.getServer().getPluginManager().registerEvents(this, plugin);
   }
 
   @EventHandler
   public void onRightClickPlayer(PlayerInteractEntityEvent event) {
     if (!(event.getRightClicked() instanceof Player)) return;
 
     Player requester = event.getPlayer();
     Player requested = (Player)event.getRightClicked();
 
         if (plugin.yamlHandler.configYaml.contains("right_click_item") && plugin.yamlHandler.configYaml.getInt("right_click_item") != requester.getItemInHand().getTypeId())
             return;
 
        if (!plugin.yamlHandler.configYaml.getBoolean("shift_right_click", false) && requester.isSneaking())
             return;
 
         if (!plugin.yamlHandler.configYaml.getBoolean("mob_arena_trade", true)) {
             if (plugin.mobArenaHandler != null && plugin.mobArenaHandler.isPlayerInArena(requester.getName()))
                 return;
         }
 
         if (!plugin.yamlHandler.configYaml.getBoolean("heroes_combat_trade", true)) {
             if (plugin.heroesPlugin != null && plugin.heroesPlugin.getCharacterManager().getHero(requester).isInCombat())
                 return;
         }
 
     if (plugin.pendingRequests.containsKey(requested)) {
       if (((!requester.isSneaking()) || (!requester.hasPermission("trade.accept-request.shift-right-click"))) && (!requester.hasPermission("trade.accept-request.right-click"))) {
         plugin.getLanguageHandler().sendMessage(requester, "command.no-permission");
         return;
       }
       plugin.acceptRequest(requester, requested);
     } else {
       if (((!requester.isSneaking()) || (!requester.hasPermission("trade.request.shift-right-click"))) && (!requester.hasPermission("trade.request.right-click"))) return;
       plugin.requestPlayer(requester, requested);
     }
   }
 }
