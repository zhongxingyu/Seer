 package com.herocraftonline.dev.heroes.skill.skills;
 
 import java.util.List;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.util.config.ConfigurationNode;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.persistence.Hero;
 import com.herocraftonline.dev.heroes.skill.ActiveSkill;
 import com.herocraftonline.dev.heroes.util.Messaging;
 
 public class SkillPort extends ActiveSkill {
 
     public SkillPort(Heroes plugin) {
         super(plugin, "Port");
         setDescription("Teleports you and your nearby party to the set location!");
         setUsage("/skill port <location>");
         setArgumentRange(1, 1);
         setIdentifiers(new String[]{"skill port"});
     }
 
     @Override
     public ConfigurationNode getDefaultConfig() {
         ConfigurationNode node = super.getDefaultConfig();
         node.setProperty("item-cost", "redstone");
         return node;
     }
 
     @Override
     public boolean use(Hero hero, String[] args) {
         Player player = hero.getPlayer();
         
         if(args[0].equalsIgnoreCase("list")) {
             for(String n : getConfig().getKeys()) {
                 String retrievedNode = getSetting(hero.getHeroClass(), n, null);
                if(retrievedNode.split(":").length == 4) {
                     Messaging.send(player, "$1 - $2", n, retrievedNode);
                 }
             }
             return false;
         }
         if (getSetting(hero.getHeroClass(), args[0].toLowerCase(), null) != null) {
             String[] splitArg = getSetting(hero.getHeroClass(), args[0].toLowerCase(), null).split(":");
             int levelRequirement = Integer.parseInt(splitArg[3]);
 
             if (hero.getLevel() < levelRequirement) {
                 Messaging.send(player, "Sorry, you need to be level $1 to use that!", levelRequirement);
                 return false;
             }
 
             ItemStack itemStack = null;
             if (Material.matchMaterial(getSetting(hero.getHeroClass(), "itemcost", "redstone")) != null) {
                 itemStack = new ItemStack(Material.matchMaterial(getSetting(hero.getHeroClass(), "itemcost", "redstone")), 1);
             }
 
             if (!(itemStack == null)) {
                 if (player.getInventory().contains(itemStack.getType())) {
                     player.getInventory().remove(itemStack);
                 } else {
                     Messaging.send(player, "Sorry, you need to have $1 to use that!", itemStack.getType().toString());
                     return false;
                 }
             }
 
             List<Entity> surrounding = player.getNearbyEntities(10, 10, 10);
             for (Entity n : surrounding) {
                 if (n instanceof Player) {
                     Player playerN = (Player) n;
                     if (getPlugin().getHeroManager().getHero(playerN).getParty() != null) {
                         if (getPlugin().getHeroManager().getHero(playerN).getParty().isPartyMember(hero)) {
                             playerN.teleport(new Location(hero.getPlayer().getWorld(), Double.parseDouble(splitArg[0]), Double.parseDouble(splitArg[1]), Double.parseDouble(splitArg[2])));
                         }
                     }
                 }
             }
             player.teleport(new Location(hero.getPlayer().getWorld(), Double.parseDouble(splitArg[0]), Double.parseDouble(splitArg[1]), Double.parseDouble(splitArg[2])));
             broadcastExecuteText(hero);
             return true;
         } else {
             return false;
         }
     }
 }
