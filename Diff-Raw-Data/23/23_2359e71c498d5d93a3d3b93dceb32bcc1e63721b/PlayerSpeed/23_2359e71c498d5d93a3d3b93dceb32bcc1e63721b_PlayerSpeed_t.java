 package littlegruz.arpeegee.listeners;
 
 import org.bukkit.Material;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 
 public class PlayerSpeed implements Listener{
 
    @EventHandler
    public void onPlayerSprinting(PlayerMoveEvent event){
       if(event.getPlayer().isSprinting()){
         /* The exception will get thrown if player is not wear the armour piece
          * which means they are not wearing the required armour for the boost
          * anyway.*/
         try{
          if(!event.getPlayer().hasPotionEffect(PotionEffectType.SPEED)
                && event.getPlayer().getInventory().getHelmet().getType().compareTo(Material.LEATHER_HELMET) == 0
                && event.getPlayer().getInventory().getChestplate().getType().compareTo(Material.LEATHER_CHESTPLATE) == 0
                && event.getPlayer().getInventory().getLeggings().getType().compareTo(Material.LEATHER_LEGGINGS) == 0
                && event.getPlayer().getInventory().getBoots().getType().compareTo(Material.LEATHER_BOOTS) == 0)
             event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20, 1));
         }catch(NullPointerException e){}
       }
    }
 }
