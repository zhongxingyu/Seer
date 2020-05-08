 package main.java.multitallented.plugins.herostronghold.effects;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.hero.Hero;
 import main.java.multitallented.plugins.herostronghold.Effect;
 import main.java.multitallented.plugins.herostronghold.HeroStronghold;
 import main.java.multitallented.plugins.herostronghold.PlayerInRegionEvent;
 import main.java.multitallented.plugins.herostronghold.Region;
 import main.java.multitallented.plugins.herostronghold.RegionManager;
 import main.java.multitallented.plugins.herostronghold.RegionType;
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 import org.bukkit.event.CustomEventListener;
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 
 /**
  *
  * @author Multitallented
  */
 public class EffectGainHealth extends Effect {
     public final HeroStronghold aPlugin;
     public EffectGainHealth(HeroStronghold plugin) {
         super(plugin);
         this.aPlugin = plugin;
         registerEvent(Type.CUSTOM_EVENT, new IntruderListener(this), Priority.Highest);
     }
     
     @Override
     public void init(HeroStronghold plugin) {
         super.init(plugin);
     }
     
     public class IntruderListener extends CustomEventListener {
         private final EffectGainHealth effect;
         public IntruderListener(EffectGainHealth effect) {
             this.effect = effect;
         }
         
         @Override
         public void onCustomEvent(Event event) {
             if (!(event instanceof PlayerInRegionEvent))
                 return;
             PlayerInRegionEvent pIREvent = (PlayerInRegionEvent) event;
             Player player = pIREvent.getPlayer();
             Hero hero = null;
             Heroes heroes = effect.aPlugin.getHeroes();
             if (heroes != null)
                 hero = heroes.getHeroManager().getHero(player);
             if (hero == null) {
                 if (player.getHealth() == 20)
                     return;
             } else if (hero.getHealth() == hero.getMaxHealth())
                 return;
             //Check if the region has the shoot arrow effect and return arrow velocity
             int addHealth = effect.regionHasEffect(pIREvent.getEffects(), "gainhealth");
             if (addHealth == 0)
                 return;
             
             Location l = pIREvent.getRegionLocation();
             RegionManager rm = effect.getPlugin().getRegionManager();
             Region r = rm.getRegion(l);
             RegionType rt = rm.getRegionType(r.getType());
             
             //Check if the player owns or is a member of the region
             if (!effect.isOwnerOfRegion(player, l) && !effect.isMemberOfRegion(player, l) && hero == null) {
                 return;
            } else if (!rt.containsFriendlyClass(hero.getHeroClass().getName())) {
                 return;
             }
             
             //Check to see if the HeroStronghold has enough reagents
             if (!effect.hasReagents(l))
                 return;
             
             //Run upkeep but don't need to know if upkeep occured
             effect.forceUpkeep(l);
             
             //grant the player food
             if (hero == null) {
                 if (player.getHealth() + addHealth <= 20) {
                     player.setHealth(player.getHealth() + addHealth);
                 } else {
                     player.setHealth(20);
                 }
             } else if (hero.getHealth() + addHealth <= hero.getMaxHealth()) {
                 hero.setHealth(hero.getHealth() + addHealth);
             } else {
                 hero.setHealth(hero.getMaxHealth());
             }
         }
     }
     
 }
