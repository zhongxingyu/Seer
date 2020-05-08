 package com.herocraftonline.dev.heroes.skill.skills;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.util.BlockIterator;
 import org.bukkit.util.config.ConfigurationNode;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.persistence.Hero;
 import com.herocraftonline.dev.heroes.skill.ActiveSkill;
 import com.herocraftonline.dev.heroes.util.Messaging;
 import com.herocraftonline.dev.heroes.util.Setting;
 
 public class SkillBlink extends ActiveSkill {
     
     private static Set<Material> transparentBlocks;
     static {
         transparentBlocks = new HashSet<Material>();
         transparentBlocks.add(Material.AIR);
         transparentBlocks.add(Material.SNOW);
         transparentBlocks.add(Material.REDSTONE_WIRE);
         transparentBlocks.add(Material.TORCH);
         transparentBlocks.add(Material.REDSTONE_TORCH_OFF);
         transparentBlocks.add(Material.REDSTONE_TORCH_ON);
         transparentBlocks.add(Material.RED_ROSE);
         transparentBlocks.add(Material.YELLOW_FLOWER);
         transparentBlocks.add(Material.SAPLING);
         transparentBlocks.add(Material.LADDER);
         transparentBlocks.add(Material.STONE_PLATE);
         transparentBlocks.add(Material.WOOD_PLATE);
         transparentBlocks.add(Material.CROPS);
         transparentBlocks.add(Material.LEVER);
         transparentBlocks.add(Material.WATER);
         transparentBlocks.add(Material.STATIONARY_WATER);
     }
     
     public SkillBlink(Heroes plugin) {
         super(plugin, "Blink");
         setDescription("Teleports you up to 6 blocks");
         setUsage("/skill blink");
         setArgumentRange(0, 0);
         setIdentifiers(new String[]{"skill blink"});
     }
     
     @Override
     public ConfigurationNode getDefaultConfig() {
         ConfigurationNode node = super.getDefaultConfig();
         node.setProperty(Setting.MAX_DISTANCE.node(), 6);
         return node;
     }
     
    @SuppressWarnings("null")
     @Override
     public boolean use(Hero hero, String[] args) {
         Player player = hero.getPlayer();
         int distance = getSetting(hero.getHeroClass(), Setting.MAX_DISTANCE.node(), 6);
         Block prev = null;
         Block b;
         BlockIterator iter = null;
         try {
            new BlockIterator(player, distance);
         } catch (IllegalStateException e) {
             Messaging.send(player, "There was an error getting your blink location!");
             return false;
         }
         while (iter.hasNext()) {
             b = iter.next();
             if (transparentBlocks.contains(b.getType())) {
                 prev = b;
             } else {
                 break;
             }
         }
         if (prev != null) {
             Location teleport = prev.getLocation().clone();
             //Set the blink location yaw/pitch to that of the player
             teleport.setPitch(player.getLocation().getPitch());
             teleport.setYaw(player.getLocation().getYaw());
             player.teleport(teleport);
             return true;
         } else {
             Messaging.send(player, "No location to blink to.");
             return false;
         }
     }
 }
