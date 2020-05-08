 package me.limebyte.battlenight.core.listeners;
 
 import java.util.HashSet;
import java.util.Iterator;
 import java.util.Set;
 
 import me.limebyte.battlenight.core.BattleNight;
 import me.limebyte.battlenight.core.util.ClassSign;
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
 
     public static Set<ClassSign> classSigns = new HashSet<ClassSign>();
 
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
 
     private static void addName(Player player, Sign sign) {
         getClassSign(sign).add(player);
     }
 
     public static void cleanSigns() {
        Iterator<ClassSign> it = classSigns.iterator();
        while (it.hasNext()) {
            it.next().clear();
         }
     }
 
     public static void cleanSigns(Player player) {
        Iterator<ClassSign> it = classSigns.iterator();
        while (it.hasNext()) {
            it.next().remove(player);
         }
     }
 
     private static ClassSign getClassSign(Sign sign) {
        Iterator<ClassSign> it = classSigns.iterator();
        while (it.hasNext()) {
            ClassSign s = it.next();
             if (s.getSign().equals(sign)) { return s; }
         }
 
         return new ClassSign(sign);
     }
 }
