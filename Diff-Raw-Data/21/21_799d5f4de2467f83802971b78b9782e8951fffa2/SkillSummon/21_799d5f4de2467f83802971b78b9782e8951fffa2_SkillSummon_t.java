 package com.herocraftonline.dev.heroes.skill.skills;
 
 import java.util.Set;
 
 import org.bukkit.entity.Creature;
 import org.bukkit.entity.CreatureType;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Ghast;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Slime;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.entity.EntityListener;
 import org.bukkit.event.entity.EntityTargetEvent;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.util.config.ConfigurationNode;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.persistence.Hero;
 import com.herocraftonline.dev.heroes.skill.ActiveSkill;
 import com.herocraftonline.dev.heroes.util.Messaging;
 
 public class SkillSummon extends ActiveSkill {
 
     private SummonEntityListener eListener;
     private SummonPlayerListener pListener;
     public SkillSummon(Heroes plugin) {
         super(plugin, "Summon");
         setDescription("Summons a creature to fight by your side");
         setUsage("/skill summon <creature>");
         setArgumentRange(1, 1);
         setIdentifiers(new String[]{"skill summon"});
         
         eListener = new SummonEntityListener();
         pListener = new SummonPlayerListener();
         registerEvent(Type.ENTITY_DEATH, eListener, Priority.Monitor);
         registerEvent(Type.ENTITY_TARGET, eListener, Priority.Highest);
         registerEvent(Type.PLAYER_QUIT, pListener, Priority.Monitor);
     }
 
     @Override
     public ConfigurationNode getDefaultConfig() {
         ConfigurationNode node = super.getDefaultConfig();
         node.setProperty("max-summons", 3);
         return node;
     }
 
     @Override
     public boolean use(Hero hero, String[] args) {
         Player player = hero.getPlayer();
        CreatureType creatureType = CreatureType.fromName(toCamelCase(args[0]));
         //TODO: Why do we only allow Skeleton if this is a full-features Summon skill that says it allows all creatures-types
        if (creatureType == CreatureType.SKELETON && hero.getSummons().size() < getSetting(hero.getHeroClass(), "max-summons", 3)) {
             Entity spawnedEntity = player.getWorld().spawnCreature(player.getLocation(), creatureType);
            if (spawnedEntity instanceof Creature || spawnedEntity instanceof Ghast || spawnedEntity instanceof Slime) {
                 spawnedEntity.remove();
                 return false;
             }
             hero.getSummons().put(spawnedEntity, creatureType);
             broadcastExecuteText(hero);
             Messaging.send(player, "You have succesfully summoned a " + creatureType.toString());
             return true;
         }
         return false;
     }
 
     public class SummonEntityListener extends EntityListener {
 
         @Override
         public void onEntityDeath(EntityDeathEvent event) {
             Entity defender = event.getEntity();
             Set<Hero> heroes = getPlugin().getHeroManager().getHeroes();
             for (Hero hero : heroes) {
                 if (hero.getSummons().containsKey(defender)) {
                     hero.getSummons().remove(defender);
                 }
             }
         }
 
         @Override
         public void onEntityTarget(EntityTargetEvent event) {
             if (event.isCancelled()) return;
             if (event.getTarget() instanceof Player) {
                 Set<Hero> heroes = getPlugin().getHeroManager().getHeroes();
                 for (Hero hero : heroes) {
                     if (hero.getSummons().containsKey(event.getEntity())) {
                         if (hero.getParty() != null) {
                             //Don't target party members either
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
     }
     
     public class SummonPlayerListener extends PlayerListener {
         
         @Override
         public void onPlayerQuit(PlayerQuitEvent event) {
             //Destroy any summoned creatures when the player exits
             Hero hero = getPlugin().getHeroManager().getHero(event.getPlayer());
             if (hero.getSummons().isEmpty()) return;
             for(Entity entity : hero.getSummons().keySet()) {
                 entity.remove();
             }
             hero.getSummons().clear();
         }
     }
    
    private String toCamelCase(String string) {
        string = string.toLowerCase();
        string.replaceFirst(string.substring(0, 0), string.substring(0, 0).toUpperCase());
        return string;
    }
  }
