 package com.herocraftonline.dev.heroes.command.skill.skills;
 
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityListener;
import org.bukkit.util.config.ConfigurationNode;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.command.skill.ActiveEffectSkill;
 import com.herocraftonline.dev.heroes.persistence.Hero;
 import com.herocraftonline.dev.heroes.persistence.HeroEffects;
 
 public class SkillManaShield extends ActiveEffectSkill {
 
     public SkillManaShield(Heroes plugin) {
         super(plugin);
         name = "ManaShield";
         description = "Uses your mana as a shield";
         usage = "/skill manashield";
         minArgs = 0;
         maxArgs = 0;
         identifiers.add("skill manashield");
 
         registerEvent(Type.ENTITY_DAMAGE, new SkillEntityListener(), Priority.Normal);
     }
 
     @Override
     public boolean use(Hero hero, String[] args) {
         Player player = hero.getPlayer();
         String playerName = player.getName();
         applyEffect(hero);
 
         notifyNearbyPlayers(player.getLocation(), useText, playerName, name);
         return true;
     }
    
    @Override
    public ConfigurationNode getDefaultConfig() {
        ConfigurationNode node = super.getDefaultConfig();
        node.setProperty("mana-amount", 20);
        return node;
    }
     public class SkillEntityListener extends EntityListener {
 
         @Override
         public void onEntityDamage(EntityDamageEvent event) {
            if (event.isCancelled()) {
                 return;
             }
 
             Entity defender = event.getEntity();
             if (defender instanceof Player) {
                 Player player = (Player) defender;
                 Hero hero = plugin.getHeroManager().getHero(player);
                 HeroEffects effects = plugin.getHeroManager().getHero(player).getEffects();
                 if (effects.hasEffect(name)) {
                    int absorbamount = getSetting(hero.getHeroClass(), "mana-amount", 20);
                     event.setDamage((int) (event.getDamage() * 0.50));
                    hero.setMana(hero.getMana() - absorbamount);
                 }
             }
         }
     }
 }
