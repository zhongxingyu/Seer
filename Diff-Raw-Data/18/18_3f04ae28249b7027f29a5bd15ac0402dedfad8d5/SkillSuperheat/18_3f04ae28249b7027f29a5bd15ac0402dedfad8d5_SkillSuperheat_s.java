 package com.herocraftonline.dev.heroes.skill.skills;
 
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockListener;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.util.config.ConfigurationNode;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.effects.Beneficial;
 import com.herocraftonline.dev.heroes.effects.Dispellable;
 import com.herocraftonline.dev.heroes.effects.ExpirableEffect;
 import com.herocraftonline.dev.heroes.persistence.Hero;
 import com.herocraftonline.dev.heroes.skill.ActiveSkill;
 import com.herocraftonline.dev.heroes.skill.Skill;
 import com.herocraftonline.dev.heroes.util.Setting;
 
 public class SkillSuperheat extends ActiveSkill {
 
     private BlockListener playerListener = new SkillPlayerListener();
     private String applyText;
     private String expireText;
 
     public SkillSuperheat(Heroes plugin) {
         super(plugin, "Superheat");
         setDescription("Your pickaxe becomes superheated");
         setUsage("/skill superheat");
         setArgumentRange(0, 0);
         setIdentifiers(new String[] { "skill superheat" });
 
         registerEvent(Type.BLOCK_BREAK, playerListener, Priority.Normal);
     }
 
     @Override
     public ConfigurationNode getDefaultConfig() {
         ConfigurationNode node = super.getDefaultConfig();
         node.setProperty(Setting.DURATION.node(), 20000);
         node.setProperty(Setting.APPLY_TEXT.node(), "%hero%'s pick has become superheated!");
         node.setProperty(Setting.EXPIRE_TEXT.node(), "%hero%'s pick has cooled down!");
         return node;
     }
 
     @Override
     public void init() {
         super.init();
         applyText = getSetting(null, Setting.APPLY_TEXT.node(), "%hero%'s pick has become superheated!").replace("%hero%", "$1");
         expireText = getSetting(null, Setting.EXPIRE_TEXT.node(), "%hero%'s pick has cooled down!").replace("%hero%", "$1");
     }
 
     @Override
     public boolean use(Hero hero, String[] args) {
         broadcastExecuteText(hero);
 
         int duration = getSetting(hero.getHeroClass(), Setting.DURATION.node(), 20000);
         hero.addEffect(new SuperheatEffect(this, duration));
 
         return true;
     }
 
     public class SkillPlayerListener extends BlockListener {
 
         @Override
         public void onBlockBreak(BlockBreakEvent event) {
             Block block = event.getBlock();
             Player player = event.getPlayer();
             Hero hero = getPlugin().getHeroManager().getHero(player);
             if (hero.hasEffect("Superheat")) {
                 switch (block.getType()) {
                     case IRON_ORE:
                         event.setCancelled(true);
                         block.setType(Material.AIR);
                        block.getWorld().dropItem(block.getLocation(), new ItemStack(Material.IRON_INGOT));
                         break;
                     case GOLD_ORE:
                         event.setCancelled(true);
                         block.setType(Material.AIR);
                        block.getWorld().dropItem(block.getLocation(), new ItemStack(Material.GOLD_INGOT));
                         break;
                     case SAND:
                         event.setCancelled(true);
                         block.setType(Material.AIR);
                        block.getWorld().dropItem(block.getLocation(), new ItemStack(Material.GLASS));
                         break;
                     case COBBLESTONE:
                         event.setCancelled(true);
                         block.setType(Material.AIR);
                        block.getWorld().dropItem(block.getLocation(), new ItemStack(Material.STONE));
                         break;
                 }
             }
         }
     }
 
     public class SuperheatEffect extends ExpirableEffect implements Dispellable, Beneficial {
 
         public SuperheatEffect(Skill skill, long duration) {
             super(skill, "Superheat", duration);
         }
 
         @Override
         public void apply(Hero hero) {
             super.apply(hero);
             Player player = hero.getPlayer();
             broadcast(player.getLocation(), applyText, player.getDisplayName());
         }
 
         @Override
         public void remove(Hero hero) {
             super.remove(hero);
             Player player = hero.getPlayer();
             broadcast(player.getLocation(), expireText, player.getDisplayName());
         }
 
     }
 
 }
