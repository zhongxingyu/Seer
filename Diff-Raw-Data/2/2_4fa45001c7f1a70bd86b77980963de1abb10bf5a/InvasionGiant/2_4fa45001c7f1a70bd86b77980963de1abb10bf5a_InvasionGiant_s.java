 package net.kingdomsofarden.andrew2060.invasion.monsters;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Random;
 
 import net.kingdomsofarden.andrew2060.invasion.InvasionPlugin;
 import net.kingdomsofarden.andrew2060.invasion.MobManager;
 import net.kingdomsofarden.andrew2060.invasion.util.TargettingUtil;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Effect;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Giant;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Monster;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Skeleton;
 import org.bukkit.entity.Villager;
 import org.bukkit.entity.WitherSkull;
 import org.bukkit.entity.Wolf;
 import org.bukkit.entity.Zombie;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 import org.bukkit.util.Vector;
 /**
  * Represents a giant spawned by this plugin.
  */
 public class InvasionGiant {
 
     private Random rand;
     private Giant giant;
     private ArrayList<Monster> minions;
     private int tier;
     private String type;
     LinkedList<PotionEffect> effects;
     public InvasionGiant(Location spawnLoc) {
         this.rand = new Random();
         this.minions = new ArrayList<Monster>();
         this.tier = rand.nextInt(5);
         this.type = "";
         this.effects = new LinkedList<PotionEffect>();
         switch(tier) {
         case 0: {
             this.type = " Legionnaire";
             this.effects.add(PotionEffectType.SPEED.createEffect(100, 1));
             break;
         }
         case 1: {
             this.type = " Decanus";
             this.effects.add(PotionEffectType.SPEED.createEffect(100, 2));
             this.effects.add(PotionEffectType.INCREASE_DAMAGE.createEffect(100, 1));
             break;
         }
         case 2: {
             this.type = " Optio";
             this.effects.add(PotionEffectType.SPEED.createEffect(100, 2));
             this.effects.add(PotionEffectType.INCREASE_DAMAGE.createEffect(100, 2));
             this.effects.add(PotionEffectType.DAMAGE_RESISTANCE.createEffect(100, 1));
             this.effects.add(PotionEffectType.FIRE_RESISTANCE.createEffect(100, 1));
             break;
         }
         case 3: {
             this.type = " Centurion";
             this.effects.add(PotionEffectType.SPEED.createEffect(100, 3));
             this.effects.add(PotionEffectType.INCREASE_DAMAGE.createEffect(100, 2));
             this.effects.add(PotionEffectType.DAMAGE_RESISTANCE.createEffect(100, 2));
             this.effects.add(PotionEffectType.FIRE_RESISTANCE.createEffect(100, 2));
             this.effects.add(PotionEffectType.HARM.createEffect(100, 1));
             break;
         }
         case 4: {
             this.type = " Praetorian";
             this.effects.add(PotionEffectType.SPEED.createEffect(100, 4));
             this.effects.add(PotionEffectType.INCREASE_DAMAGE.createEffect(100, 2));
             this.effects.add(PotionEffectType.DAMAGE_RESISTANCE.createEffect(100, 2));
             this.effects.add(PotionEffectType.FIRE_RESISTANCE.createEffect(100, 2));
             this.effects.add(PotionEffectType.HARM.createEffect(100, 2));
             break;
         }
         }
         for(int i = 0; i < rand.nextInt(1)+1;i++) {
             Giant g = (Giant) spawnLoc.getWorld().spawnEntity(spawnLoc, EntityType.GIANT);
             g.setCustomName("Undead Juggernaut");
             g.setCustomNameVisible(true);
             g.setRemoveWhenFarAway(false);
             this.giant = g;
         }
         for(PotionEffect effect: effects) {
             giant.addPotionEffect(effect,true);
         }
         spawnMinions(10,5,0);
     }
     private void spawnMinions(int zombies, int skeletons, int ghasts) {
         Location loc = giant.getLocation();
         //Get Items
         ItemStack helmet = null;
         ItemStack chest = null;
         ItemStack leggings = null;
         ItemStack boots = null;
         ItemStack sword = null;
         ItemStack bow = new ItemStack(Material.BOW);
         switch(tier) {
         case 0: {
             helmet = new ItemStack(Material.GOLD_HELMET); 
             chest = new ItemStack(Material.GOLD_CHESTPLATE);
             leggings = new ItemStack(Material.GOLD_LEGGINGS);
             boots = new ItemStack(Material.GOLD_BOOTS);
             sword = new ItemStack(Material.STONE_SWORD);
             boots.addUnsafeEnchantment(Enchantment.PROTECTION_FALL, 1);
             sword.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 1);
             bow.addUnsafeEnchantment(Enchantment.ARROW_DAMAGE, 1);
             break;
         }
         case 1: {
             helmet = new ItemStack(Material.CHAINMAIL_HELMET);
             chest = new ItemStack(Material.CHAINMAIL_CHESTPLATE);
             leggings = new ItemStack(Material.CHAINMAIL_LEGGINGS);
             boots = new ItemStack(Material.CHAINMAIL_BOOTS);
             sword = new ItemStack(Material.GOLD_SWORD);
             chest.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
             boots.addUnsafeEnchantment(Enchantment.PROTECTION_FALL, 2);
             sword.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 2);
             sword.addUnsafeEnchantment(Enchantment.FIRE_ASPECT, 1);
             bow.addUnsafeEnchantment(Enchantment.ARROW_DAMAGE, 2);
             break;
         }
         case 2: {
             helmet = new ItemStack(Material.IRON_HELMET);
             chest = new ItemStack(Material.IRON_CHESTPLATE);
             leggings = new ItemStack(Material.IRON_LEGGINGS);
             boots = new ItemStack(Material.IRON_BOOTS);
             sword = new ItemStack(Material.IRON_SWORD);
             chest.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
             chest.addUnsafeEnchantment(Enchantment.PROTECTION_PROJECTILE, 1);
             boots.addUnsafeEnchantment(Enchantment.PROTECTION_FALL, 3);
             sword.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 3);
             sword.addUnsafeEnchantment(Enchantment.FIRE_ASPECT, 2);
             sword.addUnsafeEnchantment(Enchantment.KNOCKBACK, 1);
             bow.addUnsafeEnchantment(Enchantment.ARROW_DAMAGE, 3);
             break;
         }
         case 3: {
             helmet = new ItemStack(Material.DIAMOND_HELMET);
             chest = new ItemStack(Material.DIAMOND_CHESTPLATE);
             leggings = new ItemStack(Material.DIAMOND_LEGGINGS);
             boots = new ItemStack(Material.DIAMOND_BOOTS);
             sword = new ItemStack(Material.DIAMOND_SWORD);
             chest.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 3);
             chest.addUnsafeEnchantment(Enchantment.PROTECTION_PROJECTILE, 2);
             chest.addUnsafeEnchantment(Enchantment.THORNS, 1);
             boots.addUnsafeEnchantment(Enchantment.PROTECTION_FALL, 4);
             sword.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 4);
             sword.addUnsafeEnchantment(Enchantment.FIRE_ASPECT, 3);
             sword.addUnsafeEnchantment(Enchantment.KNOCKBACK, 2);
             bow.addUnsafeEnchantment(Enchantment.ARROW_DAMAGE, 4);
             break;
         }
         case 4: {
             helmet = new ItemStack(Material.DIAMOND_HELMET);
             chest = new ItemStack(Material.DIAMOND_CHESTPLATE);
             leggings = new ItemStack(Material.DIAMOND_LEGGINGS);
             boots = new ItemStack(Material.DIAMOND_BOOTS);
             sword = new ItemStack(Material.DIAMOND_SWORD);
             chest.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 5);
             chest.addUnsafeEnchantment(Enchantment.PROTECTION_PROJECTILE, 5);
             chest.addUnsafeEnchantment(Enchantment.THORNS, 2);
             boots.addUnsafeEnchantment(Enchantment.PROTECTION_FALL, 4);
             sword.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 5);
             sword.addUnsafeEnchantment(Enchantment.FIRE_ASPECT, 4);
             sword.addUnsafeEnchantment(Enchantment.KNOCKBACK, 2);
             bow.addUnsafeEnchantment(Enchantment.ARROW_DAMAGE, 5);
             break;
         }
         }
         
         //Spawn Accompaniment
         
         for(int i = 0; i < zombies; i++) {
             Zombie zomb  = (Zombie) loc.getWorld().spawnEntity(loc, EntityType.ZOMBIE);
             if(zomb == null) {
                 continue;
             }
             zomb.getEquipment().setArmorContents(new ItemStack[] {
                     helmet,
                     chest,
                     leggings,
                     boots
             });
             zomb.getEquipment().setItemInHand(sword);
             zomb.setCustomName("Undead" + type);
             zomb.setCustomNameVisible(true);
             zomb.setRemoveWhenFarAway(false);
             minions.add(zomb);
         }
         for(int i = 0; i < skeletons ; i++) {
             Skeleton skele = (Skeleton) loc.getWorld().spawnEntity(loc, EntityType.SKELETON);
             if(skele == null) {
                 continue;
             }
             skele.getEquipment().setArmorContents(new ItemStack[] {
                     helmet,
                     chest,
                     leggings,
                     boots
             });
             skele.getEquipment().setItemInHand(bow);
             skele.setCustomName("Undead" + type + " Archer");
             skele.setCustomNameVisible(true);
             skele.setRemoveWhenFarAway(false);
             minions.add(skele);
         }
     }
     /**
      * @return The giant.
      */
     public Giant getGiant() {
         return giant;
     }
     /**
      * @return The minions spawned by this Giant.
      */
     public ArrayList<Monster> getMinions() {
         return minions;
     }
     /**
      * Calls on giant to choose an execute an attack option through a randomly generated list of actions
      * @param mobManager 
      */
     public void tick(MobManager mobManager) {
         if(!giant.isValid()) {
             mobManager.removeGiant(this);
             for(final Monster m : minions) {
                 Bukkit.getScheduler().runTask(InvasionPlugin.instance, new Runnable() {
 
                     @Override
                     public void run() {
                         minions.remove(m);
                         
                     }
                     
                 });
                 continue;
             }
             return;
         }
         for(final Monster m : minions) {
             if(!m.isValid()) {
                 Bukkit.getScheduler().runTask(InvasionPlugin.instance, new Runnable() {
 
                     @Override
                     public void run() {
                         minions.remove(m);
                         
                     }
                     
                 });
                 continue;
             }
             for(PotionEffect effect: effects) {
                 m.addPotionEffect(effect,true);
             }
         }
         for(PotionEffect effect: effects) {
             giant.addPotionEffect(effect,true);
         }
         List<Entity> near = giant.getNearbyEntities(32, 16, 32);
         List<LivingEntity> validTargets = new LinkedList<LivingEntity>();
         for(Entity e : near) {
             if(e instanceof Player || e instanceof Villager || e instanceof Wolf) {
                 validTargets.add((LivingEntity) e);
             }
         }
         //Get the closest Player
         LivingEntity target = null;
         double distance = 0;
         for(LivingEntity lE : validTargets) {
             if(target == null) {
                 target = lE;
                 distance = lE.getLocation().distanceSquared(giant.getEyeLocation());
                 continue;
             }
             double testDistance = lE.getLocation().distanceSquared(giant.getEyeLocation());
             if(testDistance < distance) {
                 distance = testDistance;
                 target = lE;
             }
         }
         //Switch Target to closest Valid Target
         giant.setTarget(target);
         if(!(target == null)) {
             for(Monster mon : minions) {
                 if(mon.getLocation().distanceSquared(giant.getLocation()) > 1024) {
                     mon.teleport(giant);
                 }
                 mon.setTarget(target);
             }
         }
         switch(rand.nextInt(10)) {
         case 0: {   //Shockwave
             if(rand.nextInt(10) < 7) {
                 return;
             }
             for(LivingEntity lE : validTargets) {
                 if(lE.getLocation().distanceSquared(giant.getLocation()) <= 64) {
                     Vector v =lE.getLocation().add(0, 2, 0).toVector().subtract(giant.getLocation().toVector()).normalize();
                     v = v.multiply(4);
                     v = v.setY(0);
                     lE.setVelocity(v);
                     lE.getWorld().playEffect(lE.getLocation(), Effect.ZOMBIE_CHEW_WOODEN_DOOR, 1);
                 }
             }
             return;
         }
         case 1:
         case 2: {   //Earthquake
             for(LivingEntity lE : validTargets) {
                 Vector original = lE.getLocation().toVector();
                 Vector to = lE.getLocation().add(0, 1, 0).toVector();
                 Vector applied = to.subtract(original).normalize();
                 if(lE instanceof Player) {       
                     int roll = rand.nextInt(100);
                     if(roll < 30) {
                         ((Player)lE).addPotionEffect(PotionEffectType.CONFUSION.createEffect(400,10));
                     }
                 }
                 lE.setVelocity(applied);
                 lE.getWorld().createExplosion(lE.getLocation().subtract(0, 6, 0), 0F);
                 lE.damage(10,giant);
                 lE.addPotionEffect(PotionEffectType.SLOW.createEffect(30, 1),true);
                 lE.getWorld().playEffect(lE.getLocation(), Effect.ZOMBIE_CHEW_WOODEN_DOOR, 1);
             }
             return;
         }
         case 3: 
         case 4: {   //Spawn new minions
             if(minions.size() > 50) {   //Limit size to 50
                 return;
             } else {
                 spawnMinions(rand.nextInt(5)+1, rand.nextInt(5)+1, rand.nextInt(2)+1);
                 return;
             }
         }
         case 5: {   //Jump
             final LivingEntity to = target;
             if(to == null) {
                 return;
             }
             giant.setVelocity(giant.getLocation().add(0,5,0).subtract(giant.getLocation()).toVector());
            Bukkit.getScheduler().scheduleSyncDelayedTask(Bukkit.getPluginManager().getPlugin("Swagserv-Invasion"), new Runnable() {
                 @Override
                 public void run() {
                    giant.teleport(to.getLocation().add(0,4,0));
                    giant.setFallDistance(0);
                    giant.setVelocity(new Vector());
                    to.damage(10,giant);
                 }
                 
             }, 18L);
             return;
         }
         case 6:
         case 7:
         case 8:
         case 9: {   //Wither Skull Attack Option
             if(!(target == null)) {
                 for(int i = 0; i < tier*2; i++) {
                     TargettingUtil.faceEntity(giant, target);
                     WitherSkull WitherSkull = giant.launchProjectile(WitherSkull.class);
                     WitherSkull.setShooter(giant);
                     WitherSkull.setVelocity(target.getLocation().add(rand.nextInt(5)-1,rand.nextInt(5)-7,rand.nextInt(5)-1).subtract(giant.getEyeLocation()).toVector().normalize().multiply(1));
                 }
             }
             return;
         }
         }
     }
     public void kill() {
         giant.remove();
         for(Monster m :minions) {
             m.remove();
         }
     }
 }
