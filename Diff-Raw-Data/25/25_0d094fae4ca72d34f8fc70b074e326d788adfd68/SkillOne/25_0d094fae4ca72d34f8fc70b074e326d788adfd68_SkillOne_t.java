 package com.herocraftonline.dev.heroes.command.skill.skills;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.event.player.PlayerMoveEvent;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.classes.HeroClass;
 import com.herocraftonline.dev.heroes.command.skill.Skill;
 import com.herocraftonline.dev.heroes.persistence.Hero;
 import com.herocraftonline.dev.heroes.util.Properties;
 
 public class SkillOne extends Skill {
 
     public SkillOne(Heroes plugin) {
         super(plugin);
        name = "One";
         description = "Skill - one";
         usage = "/one";
         minArgs = 0;
         maxArgs = 0;
        identifiers.add("one");
         configs.put("mana", "20");
         configs.put("level", "20");
 
         plugin.getServer().getPluginManager().registerEvent(Type.PLAYER_MOVE, new SkillPlayerListener(), Priority.Normal, plugin);
     }
 
     @Override
     public void use(Player player, String[] args) {
         Hero hero = plugin.getHeroManager().getHero(player);
         HeroClass heroClass = hero.getPlayerClass();
         Properties p = plugin.getConfigManager().getProperties();
 
         if (!heroClass.getSkills().contains(getName())) {
             if (!(p.getLevel(hero.getExperience()) > Integer.parseInt(p.skillInfo.get(getName() + "level")))) {
                 if (!(hero.getMana() > Integer.parseInt(p.skillInfo.get(getName() + "mana")))) {
                     return;
                 }
             }
         }
 
         hero.getEffects().put(getName(), System.currentTimeMillis() + 300000);
     }
 
     public class SkillPlayerListener extends PlayerListener {
 
        @Override
         public void onPlayerMove(PlayerMoveEvent event) {
             Player player = event.getPlayer();
             Hero hero = plugin.getHeroManager().getHero(player);
 
             if (hero.getEffects().containsKey(getName())) {
                 if (hero.getEffects().get(getName()) > System.currentTimeMillis()) {
                     player.setVelocity(player.getLocation().getDirection().multiply(1.3).setY(0));
                 }
             }
         }
 
     }
 }
