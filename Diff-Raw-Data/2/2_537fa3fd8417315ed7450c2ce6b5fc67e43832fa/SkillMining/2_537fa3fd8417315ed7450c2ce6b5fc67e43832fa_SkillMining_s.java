 package com.herocraftonline.dev.heroes.skill.skills;
 
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockListener;
 import org.bukkit.inventory.ItemStack;
 
 import com.herocraftonline.dev.heroes.HBlockListener;
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.effects.EffectType;
 import com.herocraftonline.dev.heroes.hero.Hero;
 import com.herocraftonline.dev.heroes.skill.PassiveSkill;
 import com.herocraftonline.dev.heroes.skill.Skill;
 import com.herocraftonline.dev.heroes.skill.SkillConfigManager;
 import com.herocraftonline.dev.heroes.skill.SkillType;
 import com.herocraftonline.dev.heroes.util.Setting;
 import com.herocraftonline.dev.heroes.util.Util;
 
 public class SkillMining extends PassiveSkill {
 
     public SkillMining(Heroes plugin) {
         super(plugin, "Mining");
         setDescription("You understand mining and ores!");
         setEffectTypes(EffectType.BENEFICIAL);
         setTypes(SkillType.KNOWLEDGE, SkillType.EARTH, SkillType.BUFF);
         
         registerEvent(Type.BLOCK_BREAK, new SkillBlockListener(this), Priority.Monitor);
     }
 
     @Override
     public ConfigurationSection getDefaultConfig() {
         ConfigurationSection node = super.getDefaultConfig();
         node.set(Setting.CHANCE_LEVEL.node(), .001);
         node.set("chance-from-stone", .0005);
         return node;
     }
 
     public class SkillBlockListener extends BlockListener {
 
         private Skill skill;
         
         SkillBlockListener(Skill skill) {
             this.skill = skill;
         }
         
         @Override
         public void onBlockBreak(BlockBreakEvent event) {
             Heroes.debug.startTask("HeroesSkillListener");
             if (event.isCancelled()) {
                 Heroes.debug.stopTask("HeroesSkillListener");
                 return;
             }
             
             Hero hero = plugin.getHeroManager().getHero(event.getPlayer());
             if (!hero.hasEffect("Mining")) {
                 Heroes.debug.stopTask("HeroesSkillListener");
                 return;
             }
             Block block = event.getBlock();
             if (HBlockListener.placedBlocks.containsKey(block.getLocation())) {
                 Heroes.debug.stopTask("HeroesSkillListener");
                 return;
             }
 
             Material dropMaterial = null;
             boolean isStone = false;
             switch (block.getType()) {
                 case IRON_ORE:
                 case GOLD_ORE:
                     dropMaterial = block.getType();
                     break;
                 case DIAMOND_ORE:
                     dropMaterial = Material.DIAMOND;
                     break;
                 case COAL_ORE:
                     dropMaterial = Material.COAL;
                     break;
                 case REDSTONE_ORE:
                     dropMaterial = Material.REDSTONE;
                     break;
                 case LAPIS_BLOCK:
                     dropMaterial = Material.INK_SACK;
                     break;
                 case STONE:
                     isStone = true;
                     break;
                 default:
                     Heroes.debug.stopTask("HeroesSkillListener");
                     return;
             }
 
             double chance = Util.rand.nextDouble();
             
            if (isStone && chance <= SkillConfigManager.getUseSetting(hero, skill, Setting.CHANCE_LEVEL, .0005, false) * hero.getSkillLevel(skill)) {
                 block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(getMatFromHeight(block), 1));
                 Heroes.debug.stopTask("HeroesSkillListener");
                 return;
             } else if (isStone) {
                 Heroes.debug.stopTask("HeroesSkillListener");
                 return;
             }
             
             if (chance >= SkillConfigManager.getUseSetting(hero, skill, Setting.CHANCE_LEVEL, .001, false) * hero.getSkillLevel(skill)) {
                 Heroes.debug.stopTask("HeroesSkillListener");
                 return;
             }
             if (dropMaterial == Material.INK_SACK) {
                 block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(dropMaterial, 1, (short) 0, (byte) 4));
             } else {
                 block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(dropMaterial, 1));
             }
             Heroes.debug.stopTask("HeroesSkillListener");
         }
 
         private Material getMatFromHeight(Block block) {
             int y = block.getY();
 
             if (y < 20)
                 return Material.DIAMOND;
             else if (y < 40)
                 return Material.GOLD_ORE;
             else if (y < 60)
                 return Material.IRON_ORE;
             else
                 return Material.COAL_ORE;
         }
     }
 }
