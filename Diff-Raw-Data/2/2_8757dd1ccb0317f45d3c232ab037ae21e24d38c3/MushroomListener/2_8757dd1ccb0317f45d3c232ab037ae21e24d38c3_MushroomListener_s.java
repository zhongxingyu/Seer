 package org.hopto.seed419.Listeners;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 import org.hopto.seed419.MagicMushrooms;
 
 import java.util.logging.Logger;
 
 /**
  * Created with IntelliJ IDEA.
  * User: seed419
  * Date: 4/27/12
  * Time: 7:11 PM
  * To change this template use File | Settings | File Templates.
  */
 public class MushroomListener implements Listener {
 
 
     private MagicMushrooms mm;
     private final Logger log = Logger.getLogger("MagicMushrooms");
 
 
     public MushroomListener(MagicMushrooms instance) {
         this.mm = instance;
     }
 
     @EventHandler
     public void onBlockBreak(BlockBreakEvent event) {
 
         Material mat = event.getBlock().getType();
         Player player = event.getPlayer();
 
         if (mat == Material.RED_MUSHROOM || mat == Material.BROWN_MUSHROOM) {
             int randomInt = (int) (Math.random()*100);
             if (randomInt <= 3) {
                 for (Player x : mm.getServer().getOnlinePlayers()) {
                     if (x != player) {
                         x.sendMessage(player.getDisplayName() + ChatColor.DARK_GREEN + " had a few too many shrooms.");
                     }
                 }
                 log.info(player.getName() + " had a few too many shrooms.");
                 int randomSpeed = (int) (Math.random()*50);
                 int randomJump = (int) (Math.random()*10);
                 int randomConfusion = (int) (Math.random()*50);
                 int randomBlindness = (int) (Math.random()*50);
                 player.sendMessage(ChatColor.DARK_GREEN + "That mushroom appears to cause psychoactive effects.");
                 player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 1000, randomConfusion));
                 player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 1000, randomSpeed));
                 player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 1000, randomJump));
                if (randomInt == 5) {
                     player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 1000, randomBlindness));
                 }
             }
         }
     }
 }
