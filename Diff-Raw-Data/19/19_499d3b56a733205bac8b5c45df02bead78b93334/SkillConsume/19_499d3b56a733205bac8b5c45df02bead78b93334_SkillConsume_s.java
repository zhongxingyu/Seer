 package com.herocraftonline.dev.heroes.skill.skills;
 
 import java.util.List;
 
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.util.config.ConfigurationNode;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.api.HeroRegainManaEvent;
 import com.herocraftonline.dev.heroes.persistence.Hero;
 import com.herocraftonline.dev.heroes.skill.ActiveSkill;
 import com.herocraftonline.dev.heroes.util.Messaging;
 import com.herocraftonline.dev.heroes.util.Setting;
 
 public class SkillConsume extends ActiveSkill {
 
     public SkillConsume(Heroes plugin) {
         super(plugin, "Consume");
         setDescription("Consumes an item for mana");
         setUsage("/skill consume [item]");
         setArgumentRange(1, 1);
         setIdentifiers(new String[] { "skill consume" });
     }
     
     @Override
     public ConfigurationNode getDefaultConfig() {
         ConfigurationNode node = super.getDefaultConfig();
         String root = "BONE";
         node.setProperty(root + "." + Setting.LEVEL.node(), 1);
         node.setProperty(root + "." + Setting.MANA.node(), 20);
         node.setProperty(root + "." + Setting.AMOUNT.node(), 1);
         return node;
     }
 
     @Override
     public boolean use(Hero hero, String[] args) {
         Player player = hero.getPlayer();
         if (hero.getMana() == 100) {
             Messaging.send(player, "Your mana is already full!");
             return false;
         }
         
         List<String> keys = getSettingKeys(hero.getHeroClass());
         if (keys == null || keys.isEmpty()) {
             return false;
         }
         
         for (String key : keys) {
            if (key.equals(args[0].toUpperCase())) {
                 Material mat = Material.matchMaterial(key);
                 if (mat == null) {
                     throw new IllegalArgumentException("Invalid Configuration for Skill Consume: " + key + " is not a valid Material");
                 }
                 int amount = getSetting(hero.getHeroClass(), key + "." + Setting.AMOUNT.node(), 1);
                 if (amount < 1) {
                     throw new IllegalArgumentException("Invalid Configuration for Skill Consume: " + key + " has invalid amount defined");
                 }
                 int level = getSetting(hero.getHeroClass(), key + "." + Setting.LEVEL.node(), 1);
                 if (hero.getLevel() < level) {
                     Messaging.send(player, "You must be level $1 before you can consume that item", level);
                     return false;
                 }
                 ItemStack reagent = new ItemStack(mat, amount);
                 if (!hasReagentCost(player, reagent)) {
                     String reagentName = reagent.getType().name().toLowerCase().replace("_", " ");
                     Messaging.send(player, "Sorry, you need to have $1 $2 to use that skill!", new Object[] {reagent.getAmount(), reagentName});
                     return false;
                 }
                 player.getInventory().removeItem(reagent);
                 int mana = getSetting(hero.getHeroClass(), key + "." + Setting.MANA.node(), 20);
                 HeroRegainManaEvent hrmEvent = new HeroRegainManaEvent(hero, mana, this);
                 getPlugin().getServer().getPluginManager().callEvent(hrmEvent);
                 if (hrmEvent.isCancelled()) {
                     return false;
                 }
                 hero.setMana(hrmEvent.getAmount() + hero.getMana()); 
                 if (hero.isVerbose()) {
                     Messaging.send(player, Messaging.createManaBar(100));
                 } else {
                     Messaging.send(player, "You regain " + mana + " mana");
                 }
                 
                 broadcastExecuteText(hero);
                 return true;
             }
         }
         
         Messaging.send(player, "You can't consume that item!");
         return false;
     }
 }
