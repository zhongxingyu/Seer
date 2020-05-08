 package com.herocraftonline.dev.heroes.skill.skills;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.event.player.PlayerEggThrowEvent;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.util.config.ConfigurationNode;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.persistence.Hero;
 import com.herocraftonline.dev.heroes.persistence.HeroEffects;
 import com.herocraftonline.dev.heroes.skill.PassiveSkill;
 
 public class SkillStar extends PassiveSkill {
 
     public SkillStar(Heroes plugin) {
         super(plugin);
         name = "Star";
         description = "Throw snowballs for damage";
         minArgs = 0;
         maxArgs = 0;
 
         registerEvent(Type.PLAYER_EGG_THROW, new SkillPlayerListener(), Priority.High);
     }
 
     @Override
     public ConfigurationNode getDefaultConfig() {
         ConfigurationNode node = super.getDefaultConfig();
         node.setProperty("damage", 4);
         return node;
     }
 
     public class SkillPlayerListener extends PlayerListener {
 
        public void onPlayerEggThrow(PlayerEggThrowEvent event) {
             Player player = event.getPlayer();
             Hero hero = plugin.getHeroManager().getHero(player);
             HeroEffects effects = hero.getEffects();
             if (effects.hasEffect(name)) {
                 event.setHatching(false);
                 player.damage(getSetting(hero.getHeroClass(), "damage", 4));
             }
 
         }
     }
 
 }
