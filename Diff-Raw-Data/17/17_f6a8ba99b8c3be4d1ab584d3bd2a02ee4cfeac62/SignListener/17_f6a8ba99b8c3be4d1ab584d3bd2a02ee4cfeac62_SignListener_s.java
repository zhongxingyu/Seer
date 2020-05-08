 package me.limebyte.battlenight.core.listeners;
 
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 
 import me.limebyte.battlenight.core.BattleNight;
 import me.limebyte.battlenight.core.util.ParticleEffect;
 
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 
 public class SignListener implements Listener {
 
     public static final Set<Sign> classSigns = new HashSet<Sign>();
 
     @EventHandler(priority = EventPriority.HIGHEST)
     public void onPlayerInteract(PlayerInteractEvent event) {
 
         if (event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
             Block block = event.getClickedBlock();
             Player player = event.getPlayer();
 
             if (block.getState() instanceof Sign) {
                 Sign sign = (Sign) block.getState();
                 String name = player.getName();
                 String title = sign.getLine(0);
 
                 if (BattleNight.BattleClasses.containsKey(title) && BattleNight.getBattle().usersTeam.containsKey(name)) {
                     addSign(sign);
 
                     cleanSigns(player);
                     addName(player, sign);
 
                     BattleNight.getBattle().usersClass.put(name, title);
                     BattleNight.reset(player, true);
                     BattleNight.giveItems(player);
                     ParticleEffect.spiral(player);
 
                     if (BattleNight.getBattle().usersClass.containsKey(name)) {
                         if (!BattleNight.getBattle().usersClass.get(name).equals(title)) {
                             ParticleEffect.spiral(player);
                         }
                     }
                 }
             }
         }
     }
 
     private static void addSign(Sign sign) {
         if (!classSigns.contains(sign)) {
             classSigns.add(sign);
         }
     }
 
     private static void addName(Player player, Sign sign) {
         // Third line is not empty
         if (!sign.getLine(2).isEmpty()) {
             // Move the first name down
             sign.setLine(3, sign.getLine(2));
         }
 
         // Add the players name
         sign.setLine(2, player.getName());
 
         // Update the sign
         sign.update();
     }
 
    private static void cleanName(Player player, Sign sign) {
         //TODO Make it work for names longer than the sign.
 
         // Forth line has the players name
        if (player.getName().equals(sign.getLine(3))) {
             // Clear line four
             sign.setLine(3, "");
 
             // Update the sign
             sign.update();
         }
 
         // Third line has the players name
        if (player.getName().equals(sign.getLine(2))) {
             // Move the second name up
             sign.setLine(2, sign.getLine(3));
             sign.setLine(3, "");
 
             // Update the sign
             sign.update();
         }
     }
 
     public static void cleanSigns() {
         Iterator<Sign> it = classSigns.iterator();
         while (it.hasNext()) {
             Sign sign = it.next();
             if (sign != null) {
                 sign.setLine(2, "");
                 sign.setLine(3, "");
                 sign.update();
             } else {
                 it.remove();
             }
         }
     }
 
     public static void cleanSigns(Player player) {
         Iterator<Sign> it = classSigns.iterator();
         while (it.hasNext()) {
             Sign sign = it.next();
             if (sign != null) {
                cleanName(player, sign);
             } else {
                 it.remove();
             }
         }
     }
 }
