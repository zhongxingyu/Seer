 package me.limebyte.battlenight.core.listeners;
 
 import me.limebyte.battlenight.api.BattleNightAPI;
 import me.limebyte.battlenight.api.battle.Battle;
 import me.limebyte.battlenight.core.util.Messenger;
 import me.limebyte.battlenight.core.util.Metadata;
 import me.limebyte.battlenight.core.util.config.ConfigManager;
 import me.limebyte.battlenight.core.util.config.ConfigManager.Config;
 
 import org.bukkit.Bukkit;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.ItemStack;
 
 public class InteractListener implements Listener {
 
     private BattleNightAPI api;
 
     public InteractListener(BattleNightAPI api) {
         this.api = api;
     }
 
     @EventHandler(priority = EventPriority.NORMAL)
     public void onPlayerInteract(PlayerInteractEvent event) {
         Action action = event.getAction();
         Player player = event.getPlayer();
         Battle battle = api.getBattle();
 
         if (action.equals(Action.LEFT_CLICK_BLOCK)) {
             Block block = event.getClickedBlock();
 
             if (block.getTypeId() == ConfigManager.get(Config.MAIN).getInt("ReadyBlock", 42)) {
                 if (battle.containsPlayer(player)) {
                     if (api.getPlayerClass(player) != null) {
                        if (!Metadata.getBoolean(player, "ready")) {
                            Metadata.set(player, "ready", true);
                            Messenger.tellEveryone(player.getDisplayName() + " is ready!", true);
                         }
 
                        if (battle.getPlayers().size() < 2) return;
                         boolean allReady = true;
                         for (String n : battle.getPlayers()) {
                             Player p = Bukkit.getPlayerExact(n);
                             if (p == null) continue;
                             if (!Metadata.getBoolean(p, "ready")) {
                                 allReady = false;
                                 break;
                             }
                         }
                         if (allReady) battle.start();
                     } else {
                         Messenger.tell(player, "You have not picked a class.");
                     }
                 }
             }
         }
 
         if (action.equals(Action.LEFT_CLICK_AIR) || action.equals(Action.LEFT_CLICK_BLOCK)) {
             if (battle.containsSpectator(player)) {
                 ItemStack stack = player.getItemInHand();
                 if (stack != null && stack.getItemMeta() != null) {
                     String itemName = stack.getItemMeta().getDisplayName();
                     if (itemName.contains("Previous Player")) {
                         Messenger.tell(player, "Teleporting to previous player.");
                     } else if (itemName.contains("Next Player")) {
                         Messenger.tell(player, "Teleporting to next player.");
                     }
                 }
             }
         }
     }
 }
