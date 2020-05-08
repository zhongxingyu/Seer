 package com.herocraftonline.dev.heroes.command.skill.skills;
 
 import java.util.List;
 
 import org.bukkit.block.Block;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.command.skill.ActiveSkill;
 import com.herocraftonline.dev.heroes.persistence.Hero;
 
 public class SkillBlink extends ActiveSkill{
 
     public SkillBlink(Heroes plugin) {
         super(plugin);
         name = "Blink";
         description = "Teleports you 4-5 blocks";
         usage = "/skill blink";
         minArgs = 0;
         maxArgs = 0;
         identifiers.add("skill blink");
     }
 
     @Override
     public boolean use(Hero hero, String[] args) {
         List<Block> blocks = hero.getPlayer().getLineOfSight(null, 6);
        hero.getPlayer().teleport(blocks.get(blocks.size() - 1).getLocation());
         return true;
     }
 
 }
