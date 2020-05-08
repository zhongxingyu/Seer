 package com.herocraftonline.dev.heroes.skill.skills;
 
 import java.util.Set;
 
 import net.minecraft.server.EntityCreature;
 
 import org.bukkit.craftbukkit.entity.CraftCreature;
 import org.bukkit.craftbukkit.entity.CraftPlayer;
 import org.bukkit.entity.Creature;
 import org.bukkit.entity.CreatureType;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 import org.bukkit.entity.Skeleton;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.event.entity.EntityCombustEvent;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.entity.EntityListener;
 import org.bukkit.event.entity.EntityTargetEvent;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.util.config.ConfigurationNode;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.effects.Beneficial;
 import com.herocraftonline.dev.heroes.effects.Dispellable;
 import com.herocraftonline.dev.heroes.effects.Effect;
 import com.herocraftonline.dev.heroes.effects.ExpirableEffect;
 import com.herocraftonline.dev.heroes.effects.PeriodicEffect;
 import com.herocraftonline.dev.heroes.persistence.Hero;
 import com.herocraftonline.dev.heroes.skill.ActiveSkill;
 import com.herocraftonline.dev.heroes.skill.Skill;
 import com.herocraftonline.dev.heroes.util.Messaging;
 import com.herocraftonline.dev.heroes.util.Setting;
 
 public class SkillSkeleton extends ActiveSkill {
 
     private String expireText;
 
     private SummonEntityListener eListener;
     private SummonPlayerListener pListener;
 
     public SkillSkeleton(Heroes plugin) {
         super(plugin, "Skeleton");
         setDescription("Summons a skeleton to fight by your side");
         setUsage("/skill skeleton");
         setArgumentRange(0, 0);
         setIdentifiers(new String[] { "skill skeleton" });
 
         eListener = new SummonEntityListener();
         pListener = new SummonPlayerListener();
         registerEvent(Type.ENTITY_DEATH, eListener, Priority.Monitor);
         registerEvent(Type.ENTITY_TARGET, eListener, Priority.Highest);
         registerEvent(Type.ENTITY_COMBUST, eListener, Priority.Highest);
         registerEvent(Type.ENTITY_DAMAGE, eListener, Priority.Monitor);
         registerEvent(Type.PLAYER_QUIT, pListener, Priority.Monitor);
     }
 
     @Override
     public ConfigurationNode getDefaultConfig() {
         ConfigurationNode node = super.getDefaultConfig();
         node.setProperty("max-summons", 3);
         node.setProperty(Setting.DURATION.node(), 60000);
         node.setProperty(Setting.EXPIRE_TEXT.node(), "The skeleton returns to it's hellish domain.");
         return node;
     }
 
     @Override
     public void init() {
         super.init();
         expireText = getSetting(null, Setting.EXPIRE_TEXT.node(), "The skeleton returns to it's hellish domain.");
     }
 
     @Override
     public boolean use(Hero hero, String[] args) {
         Player player = hero.getPlayer();
 
         if (hero.getSummons().size() < getSetting(hero.getHeroClass(), "max-summons", 3)) {
             Creature skeleton = (Creature) player.getWorld().spawnCreature(player.getLocation(), CreatureType.SKELETON);
             long duration = getSetting(hero.getHeroClass(), Setting.DURATION.node(), 60000);
             getPlugin().getHeroManager().addCreatureEffect(skeleton, new SummonEffect(this, duration, hero));
             broadcastExecuteText(hero);
             Messaging.send(player, "You have succesfully summoned a skeleton to fight for you.");
             return true;
         }
 
         Messaging.send(player, "You can't control anymore skeletons!");
         return false;
     }
 
     public class SummonEffect extends ExpirableEffect implements Dispellable, Beneficial {
 
         private Hero summoner;
 
         public SummonEffect(Skill skill, long duration, Hero summoner) {
             super(skill, "Summon", duration);
             this.summoner = summoner;
         }
 
         @Override
         public void apply(Creature creature) {
             super.apply(creature);
             summoner.getSummons().add(creature);
             FollowEffect fEffect = new FollowEffect(getSkill(), 1500, getDuration());
             summoner.addEffect(fEffect);
         }
 
         @Override
         public void remove(Creature creature) {
             super.remove(creature);
             summoner.getSummons().remove(creature);
             broadcast(creature.getLocation(), expireText);
             creature.remove();
 
             // Check if the summoner has anymore skeletons
             for (Creature c : summoner.getSummons()) {
                 if (c instanceof Skeleton) {
                     return;
                 }
             }
             // If there are no more summoned skeletons lets remove the follow effect
             summoner.removeEffect(summoner.getEffect("SkeletonFollow"));
         }
     }
 
     public class FollowEffect extends PeriodicEffect {
 
         public FollowEffect(Skill skill, long period, long duration) {
             super(skill, "SkeletonFollow", period, duration);
         }
 
         @Override
         public void apply(Hero hero) {
             super.apply(hero);
         }
 
         @Override
         public void remove(Hero hero) {
             super.remove(hero);
         }
 
         @Override
         public void tick(Hero hero) {
             super.tick(hero);
             for (Creature creature : hero.getSummons()) {
                 if (creature instanceof Skeleton && (creature.getTarget() == null || creature.getTarget().isDead())) {
                     moveSkeleton(creature, hero);
                 }
             }
         }
 
         /**
          * Moves the skeleton toward the player
          * 
          * @param creature
          * @param hero
          */
         private void moveSkeleton(Creature creature, Hero hero) {
             // Make sure the skeleton isn't in combat
            if (creature.getTarget() != null || creature.getTarget().isDead())
                 return;
             
             //If the skeleton is far away lets teleport them to the player
             if(creature.getLocation().distanceSquared(hero.getPlayer().getLocation()) > 400)
                 creature.teleport(hero.getPlayer());
             
             //Initiate pathing to the player
             EntityCreature cEntity = ((CraftCreature) creature).getHandle();
             cEntity.pathEntity = cEntity.world.findPath(cEntity, ((CraftPlayer) hero.getPlayer()).getHandle(), 16.0F);
             
         }
     }
 
     public class SummonEntityListener extends EntityListener {
 
         @Override
         public void onEntityDeath(EntityDeathEvent event) {
             if (!(event.getEntity() instanceof Creature))
                 return;
             Creature defender = (Creature) event.getEntity();
             Set<Hero> heroes = getPlugin().getHeroManager().getHeroes();
             for (Hero hero : heroes) {
                 if (hero.getSummons().contains(defender)) {
                     hero.getSummons().remove(defender);
                 }
             }
         }
 
         @Override
         public void onEntityCombust(EntityCombustEvent event) {
             if (!(event.getEntity() instanceof Skeleton) || event.isCancelled())
                 return;
             Creature creature = (Creature) event.getEntity();
             // Don't allow summoned creatures to combust
             if (getPlugin().getHeroManager().creatureHasEffect(creature, "Summon"))
                 event.setCancelled(true);
         }
 
         @Override
         public void onEntityTarget(EntityTargetEvent event) {
             if (event.isCancelled() || !(event.getEntity() instanceof Creature))
                 return;
             if (event.getTarget() instanceof Player) {
                 for (Hero hero : getPlugin().getHeroManager().getHeroes()) {
                     if (hero.getSummons().contains((Creature) event.getEntity())) {
                         if (hero.getParty() != null) {
                             // Don't target party members either
                             for (Hero pHero : hero.getParty().getMembers()) {
                                 if (pHero.getPlayer().equals(event.getTarget())) {
                                     event.setCancelled(true);
                                 }
                             }
                         } else if (hero.getPlayer().equals(event.getTarget())) {
                             event.setCancelled(true);
                         }
                     }
                 }
             }
         }
 
         @Override
         public void onEntityDamage(EntityDamageEvent event) {
             if (event.isCancelled() || !(event instanceof EntityDamageByEntityEvent))
                 return;
             if (event.getEntity() instanceof Player) {
                 Hero hero = getPlugin().getHeroManager().getHero((Player) event.getEntity());
                 // If this hero has no summons then ignore the event
                 if (hero.getSummons().isEmpty())
                     return;
 
                 EntityDamageByEntityEvent subEvent = (EntityDamageByEntityEvent) event;
                 LivingEntity damager = null;
                 // Lets get the damager
                 if (subEvent.getDamager() instanceof Projectile) {
                     damager = ((Projectile) subEvent.getDamager()).getShooter();
                 } else if (subEvent.getEntity() instanceof LivingEntity) {
                     damager = (LivingEntity) subEvent.getDamager();
                 }
                 if (damager == null)
                     return;
 
                 // Loop through the hero's summons and set the target
                 for (Creature creature : hero.getSummons()) {
                     if (!(creature instanceof Skeleton))
                         continue;
                     creature.setTarget(damager);
                 }
             } else if (event.getEntity() instanceof LivingEntity) {
                 // If a creature is being damaged, lets see if a player is dealing the damage to see if we need to make the summon aggro
                 EntityDamageByEntityEvent subEvent = (EntityDamageByEntityEvent) event;
                 Player player = null;
                 if (subEvent.getDamager() instanceof Player) {
                     player = (Player) subEvent.getDamager();
                 } else if (subEvent.getDamager() instanceof Projectile) {
                     if (((Projectile) subEvent.getDamager()).getShooter() instanceof Player) {
                         player = (Player) ((Projectile) subEvent.getDamager()).getShooter();
                     }
                 }
 
                 if (player == null)
                     return;
                 Hero hero = getPlugin().getHeroManager().getHero(player);
                 if (hero.getSummons().isEmpty())
                     return;
                 for (Creature creature : hero.getSummons()) {
                     if (!(creature instanceof Skeleton))
                         continue;
                     creature.setTarget((LivingEntity) event.getEntity());
                 }
             }
         }
     }
 
     public class SummonPlayerListener extends PlayerListener {
 
         @Override
         public void onPlayerQuit(PlayerQuitEvent event) {
             // Destroy any summoned creatures when the player exits
             Hero hero = getPlugin().getHeroManager().getHero(event.getPlayer());
             if (hero.getSummons().isEmpty())
                 return;
             for (Creature summon : hero.getSummons()) {
                 if (summon instanceof Skeleton) {
                     Effect effect = getPlugin().getHeroManager().getCreatureEffect(summon, "Summon");
                     if (effect != null) {
                         getPlugin().getHeroManager().removeCreatureEffect(summon, effect);
                     } else {
                         summon.remove();
                     }
                 }
             }
         }
     }
 }
