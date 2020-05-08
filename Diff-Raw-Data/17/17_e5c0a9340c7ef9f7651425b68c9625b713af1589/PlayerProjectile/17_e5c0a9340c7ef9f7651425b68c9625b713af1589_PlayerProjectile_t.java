 package littlegruz.arpeegee.listeners;
 
 import org.bukkit.Material;
 import org.bukkit.entity.Arrow;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.SmallFireball;
 import org.bukkit.entity.Snowball;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.ProjectileHitEvent;
 import org.bukkit.event.entity.ProjectileLaunchEvent;
 import org.bukkit.event.player.PlayerEggThrowEvent;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 
 import littlegruz.arpeegee.ArpeegeeMain;
 import littlegruz.arpeegee.entities.RPGRangedPlayer;
 
 public class PlayerProjectile implements Listener{
    private ArpeegeeMain plugin;
    
    public PlayerProjectile(ArpeegeeMain instance){
       plugin = instance;
    }
 
    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event){
       if(plugin.getWorldsMap().containsKey(event.getEntity().getWorld().getName())){
          if(plugin.getConfMap().get(event.getEntity().getShooter().getUniqueId()) != null){
             // Apply damage and set the damage cause to the mage who cast this confusion
             event.getEntity().getShooter().damage(2, plugin.getServer().getPlayer(plugin.getConfMap().get(event.getEntity().getShooter().getUniqueId())));
          }
          
          if(event.getEntity() instanceof Arrow){
             Arrow arrow = (Arrow) event.getEntity();
             if(arrow.getShooter() instanceof Player){
                Player playa = (Player) arrow.getShooter();
                if(plugin.getRangedPlayerMap().get(playa.getName()) != null
                      && playa.getItemInHand().getType() == Material.BOW){
                   RPGRangedPlayer rpgr = plugin.getRangedPlayerMap().get(playa.getName());
                   
                   // Slow arrow
                   if(rpgr.getOnHit() == 1){
                      event.setCancelled(true);
                     if(!rpgr.isSlowReady()){
                         playa.sendMessage("Slow arrow is still on cooldown");
                         return;
                      }
                      
                      Snowball sb = playa.launchProjectile(Snowball.class);
                      
                      sb.setVelocity(arrow.getVelocity());
                      plugin.getProjMap().put(sb,
                            Float.toString(rpgr.getAttack()) + "|" + rpgr.getParty());
                     rpgr.setSlowReadiness(false);
                      rpgr.setOnHit(0);
                      plugin.giveCooldown(playa, "slow", "ranged", 5);
                   }
                   // Blind arrow
                   else if(rpgr.getOnHit() == 2){
                     if(!rpgr.isBlindReady()){
                         playa.sendMessage("Blind arrow is still on cooldown");
                         event.setCancelled(true);
                         return;
                      }
                      
                      plugin.getProjMap().put(arrow,
                            Float.toString(rpgr.getAttack()) + "|2|" + rpgr.getParty());
                     rpgr.setBlindReadiness(false);
                      rpgr.setOnHit(0);
                      plugin.giveCooldown(playa, "bow", "ranged", 7);
                   }
                   // Sheep arrow
                   else if(rpgr.getOnHit() == 3){
                      event.setCancelled(true);
                     if(!rpgr.isSheepReady()){
                         playa.sendMessage("Killer sheep is still on cooldown");
                         return;
                      }
                      
                      SmallFireball sf = playa.launchProjectile(SmallFireball.class); 
                      
                      sf.setVelocity(arrow.getVelocity());
                      plugin.getProjMap().put(sf,
                            Float.toString(rpgr.getAttack()) + "|" + rpgr.getParty());
                     rpgr.setSheepReadiness(false);
                      rpgr.setOnHit(0);
                      plugin.giveCooldown(playa, "bow", "ranged", 13);
                   }
                   // Normal arrow
                   else{
                      if(!rpgr.isBowReady()){
                         playa.sendMessage("Bow is still on cooldown");
                         event.setCancelled(true);
                         return;
                      }
                      
                      plugin.getProjMap().put(arrow,
                            Float.toString(rpgr.getAttack()) + "|1|" + rpgr.getParty());
                      rpgr.setBowReadiness(false);
                      plugin.giveCooldown(playa, "bow", "ranged", 1);
                   }
 
                   /* Give player some arrows if running low*/
                   if(playa.getInventory().getItem(9).getAmount() < 2)
                      playa.getInventory().getItem(9).setAmount(10);
                }
             }
          }
       }
    }
    
    @EventHandler
    public void onEggThrow(PlayerEggThrowEvent event){
       if(plugin.getWorldsMap().containsKey(event.getPlayer().getWorld().getName())){
          if(plugin.getConfMap().get(event.getPlayer().getUniqueId()) != null){
             // Apply damage and set the damage cause to the mage who cast this confusion
             event.getPlayer().damage(2, plugin.getServer().getPlayer(plugin.getConfMap().get(event.getPlayer().getUniqueId())));
          }
          
          event.setHatching(false);
          // Determining explosion chance by egg skill
          if(plugin.getRangedPlayerMap().get(event.getPlayer().getName()) != null
                && event.getPlayer().getLevel() >= 16){
             float egg = plugin.getRangedPlayerMap().get(event.getPlayer().getName()).getAttack();
             
             // Max will be a 96% chance of exploding
             if(plugin.probabilityRoll((int)(12 * egg))){
                int range = (int)(egg * 0.5);
                event.getEgg().getLocation().getWorld().createExplosion(event.getEgg().getLocation(), 1F, false);
                
                for(Entity victims : event.getEgg().getNearbyEntities(range, range, range)){
                   if (victims instanceof LivingEntity
                         && plugin.isEnemy(victims, plugin.getRangedPlayerMap().get(event.getPlayer().getName()).getParty())) {
                      ((LivingEntity) victims).addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, (int) egg, 2), true);
                   }
                }
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
