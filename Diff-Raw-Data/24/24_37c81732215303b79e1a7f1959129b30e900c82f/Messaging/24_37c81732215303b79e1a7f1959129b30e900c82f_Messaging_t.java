 package com.herocraftonline.dev.heroes.util;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
import org.bukkit.entity.CaveSpider;
 import org.bukkit.entity.Chicken;
 import org.bukkit.entity.Cow;
 import org.bukkit.entity.Creature;
 import org.bukkit.entity.Creeper;
import org.bukkit.entity.Enderman;
 import org.bukkit.entity.Ghast;
 import org.bukkit.entity.Giant;
 import org.bukkit.entity.Pig;
 import org.bukkit.entity.PigZombie;
 import org.bukkit.entity.Sheep;
import org.bukkit.entity.Silverfish;
 import org.bukkit.entity.Skeleton;
 import org.bukkit.entity.Slime;
 import org.bukkit.entity.Spider;
 import org.bukkit.entity.Squid;
 import org.bukkit.entity.Wolf;
 import org.bukkit.entity.Zombie;
 
 import com.herocraftonline.dev.heroes.Heroes;
 
 public final class Messaging {
 
     public static void broadcast(Heroes plugin, String msg, Object... params) {
         plugin.getServer().broadcastMessage(parameterizeMessage(msg, params));
     }
 
     public static String createFullHealthBar(double health, double maxHealth) {
         return "§aHP: §f" + (int) Math.ceil(health) + "/" + (int) Math.ceil(maxHealth) + " " + createHealthBar(health, maxHealth);
     }
 
     public static String createHealthBar(double health, double maxHealth) {
         String healthBar = ChatColor.RED + "[" + ChatColor.GREEN;
         int progress = (int) (health / maxHealth * 50.0);
         for (int i = 0; i < progress; i++) {
             healthBar += "|";
         }
         healthBar += ChatColor.DARK_RED;
         for (int i = 0; i < 50 - progress; i++) {
             healthBar += "|";
         }
         healthBar += ChatColor.RED + "]";
         return healthBar + " - " + ChatColor.GREEN + (int) (health / maxHealth * 100.0) + "%";
     }
 
     public static String createManaBar(int mana) {
         String manaBar = ChatColor.RED + "[" + ChatColor.BLUE;
         int progress = (int) (mana / 100.0 * 50);
         for (int i = 0; i < progress; i++) {
             manaBar += "|";
         }
         manaBar += ChatColor.DARK_RED;
         for (int i = 0; i < 50 - progress; i++) {
             manaBar += "|";
         }
         manaBar += ChatColor.RED + "]";
         return manaBar + " - " + ChatColor.BLUE + mana + "%";
     }
 
     public static String getCreatureName(Creature creature) {
        if (creature instanceof CaveSpider)
            return "Cave Spider";
        else if (creature instanceof Cow)
             return "Cow";
         else if (creature instanceof Chicken)
             return "Chicken";
         else if (creature instanceof Creeper)
             return "Creeper";
        else if (creature instanceof Enderman)
            return "Enderman";
         else if (creature instanceof Ghast)
             return "Ghast";
         else if (creature instanceof Giant)
             return "Giant";
         else if (creature instanceof Pig)
             return "Pig";
         else if (creature instanceof PigZombie)
             return "Pig Zombie";
         else if (creature instanceof Sheep)
             return "Sheep";
         else if (creature instanceof Skeleton)
             return "Skeleton";
        else if (creature instanceof Silverfish)
            return "Silverfish";
         else if (creature instanceof Slime)
             return "Slime";
         else if (creature instanceof Spider)
             return "Spider";
         else if (creature instanceof Squid)
             return "Squid";
         else if (creature instanceof Wolf)
             return "Wolf";
         else if (creature instanceof Zombie)
             return "Zombie";
         else
             return null;
     }
 
     public static void send(CommandSender player, String msg, Object... params) {
         player.sendMessage(parameterizeMessage(msg, params));
     }
 
     private static String parameterizeMessage(String msg, Object... params) {
         msg = ChatColor.RED + "Heroes: " + ChatColor.GRAY + msg;
         if (params != null) {
             for (int i = 0; i < params.length; i++) {
                 msg = msg.replace("$" + (i + 1), ChatColor.WHITE + params[i].toString() + ChatColor.GRAY);
             }
         }
         return msg;
     }
 }
