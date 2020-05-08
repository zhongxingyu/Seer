 package com.herocraftonline.dev.heroes.skill.skills;
 
 import java.util.Map;
 
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.util.config.ConfigurationNode;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.hero.Hero;
 import com.herocraftonline.dev.heroes.skill.ActiveSkill;
 import com.herocraftonline.dev.heroes.skill.SkillType;
 import com.herocraftonline.dev.heroes.util.Messaging;
 
 public class SkillGift extends ActiveSkill{
 
     private String sendText;
 
     public SkillGift(Heroes plugin) {
         super(plugin, "Gift");
         setDescription("Teleports an item to your target");
         setUsage("/skill gift <player> [amount]");
         setArgumentRange(0, 3);
         setIdentifiers("skill gift");
         setTypes(SkillType.TELEPORT, SkillType.ITEM);
     }
 
     @Override
     public ConfigurationNode getDefaultConfig() {
         ConfigurationNode node = super.getDefaultConfig();
         node.setProperty("max-amount", 64);
         node.setProperty("send-text", "%hero% has sent you %amount% %item%");
         return node;
     }
 
     @Override
     public void init() {
         super.init();
        sendText = getSetting(null, "send-text", "%hero% has sent you %amount% %item%").replace("%hero", "$1").replace("%amount%", "$2").replace("%item%", "$3");
     }
 
     @Override
     public boolean use(Hero hero, String[] args) {
         Player player = hero.getPlayer();
         Player reciever = null;
         ItemStack item = null;
         int maxAmount = getSetting(hero.getHeroClass(), "max-amount", 64);
         int amount = 0;
         if(args.length >= 1 && plugin.getServer().getPlayer(args[0]) != null) {
             reciever = plugin.getServer().getPlayer(args[0]);
             item = player.getItemInHand().clone();
             amount = item.getAmount();
             if (amount > maxAmount) {
                 item.setAmount(maxAmount);
                 amount = maxAmount;
             }
             
             if(args.length > 2) {
                 try {
                     amount = Integer.parseInt(args[2]);
                 } catch (NumberFormatException e) {
                     Messaging.send(player, "That's not an amount!");
                     Messaging.send(player, getUsage());
                     return false;
                 }
                 if (amount > maxAmount) {
                     Messaging.send(player, "You can only send up to $1 at a time", maxAmount);
                     return false;
                 }
                 item.setAmount(amount);
             }
         } else {
             Messaging.send(player, getUsage());
             return false;
         }
         
         if(amount < item.getAmount()) {
             Messaging.send(player, "You aren't holding enough to send that amount!");
             return false;
         }
 
         player.getInventory().remove(item);
         Map<Integer, ItemStack> leftOvers = reciever.getInventory().addItem(item);
         Messaging.send(reciever, sendText, player.getName(), amount, item.getType().name().toLowerCase().replace("_", " "));
         if (!leftOvers.isEmpty()) {
             for (ItemStack leftOver : leftOvers.values()) {
                 reciever.getWorld().dropItem(reciever.getLocation(), leftOver);
             }
             Messaging.send(reciever, "Some items fall at your feet!");
         }
         broadcastExecuteText(hero);
 
         return true;
     }
 
 }
