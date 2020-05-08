 package littlegruz.arpeegee.listeners;
 
 import org.bukkit.Material;
 import org.bukkit.entity.Arrow;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.SmallFireball;
 import org.bukkit.entity.Snowball;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityShootBowEvent;
 import org.bukkit.event.entity.ProjectileHitEvent;
 import org.bukkit.event.entity.ProjectileLaunchEvent;
 import org.bukkit.event.player.PlayerEggThrowEvent;
 
 import littlegruz.arpeegee.ArpeegeeMain;
 
 public class PlayerProjectile implements Listener{
    private ArpeegeeMain plugin;
    
    public PlayerProjectile(ArpeegeeMain instance){
       plugin = instance;
    }
    
    @EventHandler
    public void onPlayerBowShoot(EntityShootBowEvent event){
       if(plugin.getWorldsMap().containsKey(event.getEntity().getWorld().getName())){
          if(event.getEntity() instanceof Player){
             Player playa = (Player) event.getEntity();
    
             if(plugin.getRangedPlayerMap().get(playa.getName()) != null){
                // Normal arrow
                if(playa.getInventory().getHeldItemSlot() == 0){
                  if(!plugin.getRangedPlayerMap().get(playa.getName()).isArrowReady()){
                      playa.sendMessage("Arrow is still on cooldown");
                      return;
                   }
                   
                   plugin.getProjMap().put(event.getProjectile(),
                         Integer.toString(plugin.getRangedPlayerMap().get(playa.getName()).getGearLevel()) + "|1");
                   plugin.giveCooldown(playa, "arrow", "ranged", 2);
                }
                else if(playa.getInventory().getHeldItemSlot() == 1){
                   // Blind arrow
                   if(playa.getLevel() >= 7){
                     if(!plugin.getRangedPlayerMap().get(playa.getName()).isBlindBowReady()){
                         playa.sendMessage("Blind arrow is still on cooldown");
                         return;
                      }
                      
                      plugin.getProjMap().put(event.getProjectile(),
                            Integer.toString(plugin.getRangedPlayerMap().get(playa.getName()).getGearLevel()) + "|2");
                      playa.getInventory().setItemInHand(null);
                      plugin.getRangedPlayerMap().get(playa.getName()).setBlindBowReadiness(false);
                      plugin.giveCooldown(playa, "ICANTSEE", "ranged", 3);
                   }
                   // Normal arrow
                   else{
                     if(!plugin.getRangedPlayerMap().get(playa.getName()).isArrowReady()){
                         playa.sendMessage("Arrow is still on cooldown");
                         return;
                      }
                      
                      plugin.getProjMap().put(event.getProjectile(),
                            Integer.toString(plugin.getRangedPlayerMap().get(playa.getName()).getGearLevel()) + "|1");
                      plugin.giveCooldown(playa, "arrow", "ranged", 2);
                   }
                }
                // Normal arrow
                else if(playa.getInventory().getHeldItemSlot() > 1){
                  if(!plugin.getRangedPlayerMap().get(playa.getName()).isArrowReady()){
                      playa.sendMessage("Arrow is still on cooldown");
                      return;
                   }
                   
                   plugin.getProjMap().put(event.getProjectile(),
                         Integer.toString(plugin.getRangedPlayerMap().get(playa.getName()).getGearLevel()) + "|1");
                   plugin.giveCooldown(playa, "arrow", "ranged", 2);
                }
                if(playa.getInventory().getItem(9).getAmount() < 2)
                   playa.getInventory().getItem(9).setAmount(10);
             }
          }
       }
    }
 
    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event){
       if(event.getEntity() instanceof Arrow){
          Arrow arrow = (Arrow) event.getEntity();
          if(arrow.getShooter() instanceof Player){
             Player playa = (Player) arrow.getShooter();
             if(plugin.getRangedPlayerMap().get(playa.getName()) != null){
                // Slow arrow
                if(playa.getItemInHand().getType() == Material.BOW
                      && playa.getInventory().getHeldItemSlot() == 2){
                   event.setCancelled(true);
                  if(!plugin.getRangedPlayerMap().get(playa.getName()).isSlowBowReady()){
                      playa.sendMessage("Slow arrow is still on cooldown");
                      return;
                   }
                   
                   Snowball sb = playa.launchProjectile(Snowball.class);
                   
                   sb.setVelocity(arrow.getVelocity());
                   plugin.getProjMap().put(sb,
                         Integer.toString(plugin.getRangedPlayerMap().get(playa.getName()).getGearLevel()));
                   playa.getInventory().setItemInHand(null);
                   plugin.getRangedPlayerMap().get(playa.getName()).setSlowBowReadiness(false);
                   plugin.giveCooldown(playa, "slow", "ranged", 2);
                }
                // Sheep arrow
                else if(playa.getItemInHand().getType() == Material.BOW
                      && playa.getInventory().getHeldItemSlot() == 3){
                   event.setCancelled(true);
                  if(!plugin.getRangedPlayerMap().get(playa.getName()).isSheepBowReady()){
                      playa.sendMessage("Sheep arrow is still on cooldown");
                      return;
                   }
                   
                   SmallFireball sf = playa.launchProjectile(SmallFireball.class); 
                   
                   sf.setVelocity(arrow.getVelocity());
                   plugin.getProjMap().put(sf,
                         Integer.toString(plugin.getRangedPlayerMap().get(playa.getName()).getGearLevel()));
                   playa.getInventory().setItemInHand(null);
                   plugin.getRangedPlayerMap().get(playa.getName()).setSheepBowReadiness(false);
                   plugin.giveCooldown(playa, "woof", "ranged", 2);
                }
             }
          }
       }
    }
    
    @EventHandler
    public void onEggThrow(PlayerEggThrowEvent event){
       if(plugin.getWorldsMap().containsKey(event.getPlayer().getWorld().getName())){
          event.setHatching(false);
          // Determining explosion chance by egg skill
          if(plugin.getRangedPlayerMap().get(event.getPlayer().getName()) != null
                && event.getPlayer().getLevel() >= 10){
             int egg = plugin.getRangedPlayerMap().get(event.getPlayer().getName()).getGearLevel();
             if(plugin.probabilityRoll(5 * egg)){
                event.getEgg().getLocation().getWorld().createExplosion(event.getEgg().getLocation(), 1F, false);
             }
          }
       }
    }
    
    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event){
       if(plugin.getWorldsMap().containsKey(event.getEntity().getWorld().getName())){
          // Projectile hitting something
          Entity entity = event.getEntity();
          if(plugin.getProjMap().get(entity) != null){
             plugin.getProjMap().put(entity, plugin.getProjMap().get(entity) + "grounded");
             return;
          }
       }
    }
 }
