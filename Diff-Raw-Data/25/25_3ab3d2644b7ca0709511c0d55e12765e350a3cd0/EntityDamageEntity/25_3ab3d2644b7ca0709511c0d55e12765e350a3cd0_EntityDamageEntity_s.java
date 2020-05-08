 package littlegruz.arpeegee.listeners;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import littlegruz.arpeegee.ArpeegeeMain;
 
 import org.bukkit.Effect;
 import org.bukkit.EntityEffect;
 import org.bukkit.Material;
 import org.bukkit.entity.Arrow;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Zombie;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.inventory.ItemStack;
 
 public class EntityDamageEntity implements Listener {
    private ArpeegeeMain plugin;
    
    public EntityDamageEntity(ArpeegeeMain instance){
       plugin = instance;
    }
 
    @EventHandler
    public void onEntityDamageEntity(EntityDamageByEntityEvent event){
       if(plugin.getWorldsMap().containsKey(event.getEntity().getWorld().getUID().toString())){
         if(event.getDamager() instanceof Player
                && event.getEntity() instanceof LivingEntity){
             Player playa = (Player) event.getDamager();
             LivingEntity victim = (LivingEntity) event.getEntity();
             
             event.setDamage(0);
             
            if(playa.getItemInHand().getTypeId() == 0)
               playa.sendMessage("Fist bump");
            
             // Heal spell
             if(playa.getItemInHand().getData().toString().contains("WHITE DYE")
                   && plugin.getMagicPlayerMap().get(playa.getName()) != null
                   && playa.getLevel() >= 3){
                if(!plugin.getMagicPlayerMap().get(playa.getName()).isTeleportReady()){
                   playa.sendMessage("Heal is still on cooldown");
                   return;
                }
                else{
                   ItemStack is = new ItemStack(351,1);
                   is.setDurability((short)15);
                   playa.getInventory().remove(is);
                   plugin.giveCooldown(playa, "heal", 7);
                   plugin.getMagicPlayerMap().get(playa.getName()).setHealReadiness(false);
                }
                healSpell(playa, victim, 1);
             }
             // Advanced heal spell
             else if(playa.getItemInHand().getData().toString().contains("BONE")
                   && plugin.getMagicPlayerMap().get(playa.getName()) != null
                   && playa.getLevel() >= 11){
                if(!plugin.getMagicPlayerMap().get(playa.getName()).isTeleportReady()){
                   playa.sendMessage("Advanced heal is still on cooldown");
                   return;
                }
                else{
                   playa.getInventory().remove(Material.BONE);
                   plugin.giveCooldown(playa, "advHeal", 11);
                   plugin.getMagicPlayerMap().get(playa.getName()).setAdvHealReadiness(false);
                }
                healSpell(playa, victim, 2);
             }
             // Damage by a diamond sword (crit sword)
             else if(playa.getItemInHand().getType().compareTo(Material.DIAMOND_SWORD) == 0
                   && plugin.getMeleePlayerMap().get(playa.getName()) != null
                   && playa.getLevel() >= 5){
                int blade, crit, level;
                
                // Check if the player can swing yet
                if(plugin.getMeleePlayerMap().get(playa.getName()).isAttackReady()){
                   plugin.giveCooldown(plugin.getMeleePlayerMap().get(playa.getName()), 1);
                   plugin.getMeleePlayerMap().get(playa.getName()).setAttackReadiness(false);
                }
                else
                   return;
                
                blade = (int) plugin.getMeleePlayerMap().get(playa.getName()).getSubClassObject().getBlade();
                level = (int) plugin.getMeleePlayerMap().get(playa.getName()).getLevel();
                
                /* If player is in Berserk mode, attack has an increased chance of
                 * crit (double damage) otherwise the crit is (5 * blade skill)%*/
                if(plugin.getBerserkMap().get(playa.getName()) != null){
                   if(plugin.probabilityRoll(5 * blade + level))
                      crit = 2;
                   else
                      crit = 1;
                   event.setDamage((blade + (level / 2)) * crit);
                   
                }
                else{
                   if(plugin.probabilityRoll(5 * blade))
                      crit = 2;
                   else
                      crit = 1;
                   event.setDamage(blade * crit);
                   plugin.getMeleePlayerMap().get(playa.getName()).addRage(5);
                }
                if(crit == 2)
                   playa.sendMessage("*crit*");
                
                //playa.getItemInHand().setDurability((short) 0);
             }
             // Damage by an iron sword (block sword)
             else if(playa.getItemInHand().getType().compareTo(Material.IRON_SWORD) == 0
                   && plugin.getMeleePlayerMap().get(playa.getName()) != null){
                int blade;
    
                // Check if the player can swing yet
                if(plugin.getMeleePlayerMap().get(playa.getName()).isAttackReady()){
                   plugin.giveCooldown(plugin.getMeleePlayerMap().get(playa.getName()), 1);
                   plugin.getMeleePlayerMap().get(playa.getName()).setAttackReadiness(false);
                }
                else
                   return;
                
                blade = (int) plugin.getMeleePlayerMap().get(playa.getName()).getSubClassObject().getBlade();
    
                event.setDamage(blade);
    
                if(plugin.getBerserkMap().get(playa.getName()) == null)
                   plugin.getMeleePlayerMap().get(playa.getName()).addRage(5);
                
                //playa.getItemInHand().setDurability((short) 0);
                
             }
          }
          // Melee player taking (reduced) damage and possibly blocking an attack
          else if(event.getEntity() instanceof Player){
             Player playa = (Player) event.getEntity();
             if(plugin.getMeleePlayerMap().get(playa.getName()) != null){
                int dmg;
                
                /* The amount that is decreased from the damage to be taken depends
                 * on the players level */
                if(playa.getLevel() < 5)
                   dmg = event.getDamage();
                else if(playa.getLevel() < 10)
                   dmg = (int) (event.getDamage() * (event.getDamage() * 0.67) / event.getDamage());
                else
                   dmg = (int) (event.getDamage() * (event.getDamage() * 0.5) / event.getDamage());
                event.setDamage(dmg);
                
                // Damage block check
                if(playa.getItemInHand().getType().compareTo(Material.IRON_SWORD) == 0){
                   int block;
                   
                   block = (int) plugin.getMeleePlayerMap().get(playa.getName()).getSubClassObject().getBlock();
                   if(plugin.probabilityRoll(5 * block)){
                      event.setCancelled(true);
                      playa.sendMessage("*blocked*");
                      return;
                   }
                }
             }
          }
          // Player arrow hit
          else if(event.getDamager() instanceof Arrow){
             // Check that it came from a player
             if(plugin.getProjMap().get(event.getDamager()) != null){
                ArrayList<Entity> ent = new ArrayList<Entity>();
                int arch, i;
                
                arch = (int) Double.parseDouble(plugin.getProjMap().get(event.getDamager()).replace("grounded", ""));
                
                // If crit, do double damage
                if(plugin.probabilityRoll(5 * arch)){
                   event.getEntity().getWorld().strikeLightningEffect(event.getEntity().getLocation());
                   event.setDamage(1 * arch * 2);
                }
                else
                   event.setDamage(1 * arch);
                
                // Remove all arrows that have hit the ground from hashmap
                // The removal is separate to stop concurrency issues
                Iterator<Map.Entry<Entity, String>> it = plugin.getProjMap().entrySet().iterator();
                while(it.hasNext()){
                   Entry<Entity, String> arrow = it.next();
                   if(arrow.getValue().contains("grounded"))
                      ent.add(arrow.getKey());
                }
                for(i = ent.size() - 1; ent.size() > 0; i--){
                   plugin.getProjMap().remove(ent.get(i));
                   ent.remove(i);
                }
             }
          }
         // Fist bump!
         else if(event.getEntity() instanceof Player
               && event.getDamager() instanceof Player){
            Player playa = (Player) event.getEntity();
            if(playa.getItemInHand().getTypeId() == 0){
               ((Player) event.getEntity()).sendMessage("*fist bumped by " + playa.getName() + "*");
               playa.sendMessage("*fist bump*");
            }
         }
       }
    }
    
    private void healSpell(Player playa, LivingEntity fortunate, int adv){
       int spell = (int) plugin.getMagicPlayerMap().get(playa.getName()).getSubClassObject().getSpell();
       if(fortunate instanceof Player){ //Change on release
          playa.playEffect(fortunate.getLocation(), Effect.SMOKE, 1);
          if(fortunate.getHealth() + (spell * adv) > fortunate.getMaxHealth())
             fortunate.setHealth(fortunate.getMaxHealth());
          else
             fortunate.setHealth(fortunate.getHealth() + (spell * adv));
          playa.sendMessage("Healed");
       }
       // If player heals a zombie, then deal damage instead
       else if(fortunate instanceof Zombie){
          fortunate.playEffect(EntityEffect.HURT);
          fortunate.damage(spell * adv);
          playa.sendMessage("Undead damage!");
       }
    }
 }
