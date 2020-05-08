 package com.herocraftonline.dev.heroes.skill.skills;
 
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.hero.Hero;
 import com.herocraftonline.dev.heroes.skill.ActiveSkill;
 import com.herocraftonline.dev.heroes.skill.SkillType;
 import com.herocraftonline.dev.heroes.util.Messaging;
 
 public class SkillEntityPort extends ActiveSkill{
 
     public SkillEntityPort(Heroes plugin, String name) {
         super(plugin, name);
         setDescription("Teleports an item to your target");
         setUsage("/skill entityport <player> [item] [amount]");
         setArgumentRange(0, 3);
         setIdentifiers("skill entityport");
         setTypes(SkillType.TELEPORT, SkillType.ITEM);
     }
 
     @Override
     public boolean use(Hero hero, String[] args) {
         Player player = hero.getPlayer();
         Player reciever = null;
         ItemStack item = null;
         if(args.length >= 1 && plugin.getServer().getPlayer(args[0]) != null) {
             reciever = plugin.getServer().getPlayer(args[0]);
             item = player.getItemInHand();
             if(args.length >= 2) {
                 item = new ItemStack(Material.matchMaterial(args[1]));
                 if(args.length == 3) {
                     item.setAmount(Integer.parseInt(args[2]));
                 }
             }
         }else {
             Messaging.send(player, "Sorry, this skill requires a player to be stated!");
             return false;
         }
         
         player.getInventory().remove(item);
        reciever.getInventory().addItem(item);
         broadcastExecuteText(hero);
 
         return true;
     }
 
 }
