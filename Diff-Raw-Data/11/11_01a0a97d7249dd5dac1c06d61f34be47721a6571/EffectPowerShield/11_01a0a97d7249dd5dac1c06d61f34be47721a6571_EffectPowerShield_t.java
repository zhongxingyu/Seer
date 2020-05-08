 package main.java.multitallented.plugins.herostronghold.effects;
 
 import multitallented.redcastlemedia.bukkit.herostronghold.HeroStronghold;
 import multitallented.redcastlemedia.bukkit.herostronghold.effect.Effect;
 import multitallented.redcastlemedia.bukkit.herostronghold.region.RegionCondition;
 import multitallented.redcastlemedia.bukkit.herostronghold.region.RegionManager;
 import multitallented.redcastlemedia.bukkit.herostronghold.region.SuperRegion;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.TNTPrimed;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityExplodeEvent;
 
 /**
  *
  * @author Multitallented
  */
 public class EffectPowerShield extends Effect {
     public EffectPowerShield(HeroStronghold plugin) {
         super(plugin);
         registerEvent(new EffectPowerShield.UpkeepListener(this));
     }
     
     @Override
     public void init(HeroStronghold plugin) {
         super.init(plugin);
         
     }
     
     public class UpkeepListener implements Listener {
         private final EffectPowerShield effect;
         private RegionManager rm;
         public UpkeepListener(EffectPowerShield effect) {
             this.effect = effect;
             rm = effect.getPlugin().getRegionManager();
         }
         
         @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
         public void onEntityExplode(EntityExplodeEvent event) {
             Entity e = event.getEntity();
             if (!e.getClass().equals(TNTPrimed.class)) {
                 return;
             }
             if (rm.shouldTakeAction(event.getLocation(), null, new RegionCondition("powershield", true, 0))) {
                 boolean powerReduced = false;
                 for (SuperRegion sr : rm.getContainingSuperRegions(event.getLocation())) {
                    if (sr.getPower() > 0 && rm.getRegionType(sr.getType()).getEffects().contains("powershield")) {
                         powerReduced = true;
                         rm.reduceRegion(sr);
                     }
                 }
                 if (!powerReduced) {
                     event.setCancelled(true);
                 }
             }
         }
     }
 }
