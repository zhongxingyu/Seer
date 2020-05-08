 package com.TunkDesign.MotherNature;
 
 //Java import
 import java.util.ArrayList;
 import java.util.Iterator;
 //Bukkit import
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 
 //Referenced classes of package com.TunkDesign.MotherNature:
 //          MotherNature, MotherNatureLogging, MotherNaturePermissions
 
 public class MotherNatureHelp
 {
 
   public MotherNatureHelp()
   {
   }
 
   public static void load(MotherNature parent)
   {
       MotherNature.log.debug("Loading help messages");
       helpCommands.add(new String[] {
           "mn help", "Show this message", "mothernature.command.help", "anyone"
       });
       helpCommands.add(new String[] {
           "mn version", "Show the current MotherNature version", "mothernature.command.version", "anyone"
       });
       helpCommands.add(new String[] {
           "mn reload", "Reload the MotherNature config", "mothernature.command.reload"
       });
       helpCommands.add(new String[] {
           "mn rain", "Tells the current world to rain", "mothernature.command.rain"
       });
       helpCommands.add(new String[] {
           "mn thunder", "Tells the current world to thunder", "mothernature.command.thunder"
       });
       helpCommands.add(new String[] {
           "mn strike [player]", "Strike a player with a lightning bolt!", "mothernature.command.lightning"
       });
       helpCommands.add(new String[] {
           "mn sun", "Tells the current world to be sunny", "mothernature.command.sun"
       });
     
       /* Not working properly */
 //      helpCommands.add(new String[] {
 //          "mn umbrella", "Gives you an umbrella over your head", "mothernature.command.umbrella"
 //      });
      
       helpCommands.add(new String[] {
           "mn sunrise", "Changes server time to morning", "mothernature.command.day"
       });
       helpCommands.add(new String[] {
           "mn sunset", "Changes server time to night", "mothernature.command.night"
       });
       helpCommands.add(new String[] {
           "mn lwand", "Gives you a lightning wand", "mothernature.tool.lwand"
       });
   }
 
   public static ArrayList<String> getMessages(Player player)
   {
       ArrayList<String> messages = new ArrayList<String>();
       for(Iterator<String[]> iterator = helpCommands.iterator(); iterator.hasNext();)
       {
           String command[] = iterator.next();
           if(command.length > 3)
           {
               if(MotherNaturePermissions.has(player, command[2], false))
                   messages.add((new StringBuilder()).append(ChatColor.GOLD).append("/").append(command[0]).append(ChatColor.GRAY).append(" - ").append(ChatColor.GREEN).append(command[1]).toString());
           } else
           if(MotherNaturePermissions.has(player, command[2]))
               messages.add((new StringBuilder()).append(ChatColor.GOLD).append("/").append(command[0]).append(ChatColor.GRAY).append(" - ").append(ChatColor.GREEN).append(command[1]).toString());
       }
 
       return messages;
   }
 
   private static ArrayList<String[]> helpCommands = new ArrayList<String[]>();
 
 }
