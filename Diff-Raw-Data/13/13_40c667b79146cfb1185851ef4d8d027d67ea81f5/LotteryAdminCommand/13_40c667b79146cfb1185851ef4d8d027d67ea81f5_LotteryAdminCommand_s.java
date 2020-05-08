 package com.imdeity.lottery.cmd;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.imdeity.lottery.Lottery;
 import com.imdeity.objects.LotteryObject;
 import com.imdeity.util.*;
 
 public class LotteryAdminCommand implements CommandExecutor {
 
     private static final List<String> output = new ArrayList<String>();
     private Lottery plugin = null;
 
     static {
         output.add(ChatTools.formatTitle("LotteryAdmin"));
         // output.add(ChatTools.formatCommand("", "/lotteryadmin", "config",
         // "Gives help on configuaration."));
         output.add(ChatTools.formatCommand("", "/lotteryadmin", "addtopot [n]",
                 "Adds [n] to current pot."));
         output.add(ChatTools.formatCommand("", "/lotteryadmin",
                 "removefrompot [n]", "Removes [n] to current pot."));
         output.add(ChatTools.formatCommand("", "/lotteryadmin", "draw",
                 "Forces a draw of t."));
     }
 
     public LotteryAdminCommand(Lottery instance) {
         this.plugin = instance;
 
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command cmd,
             String commandLabel, String args[]) {
         if (sender instanceof Player) {
             Player player = (Player) sender;
             if (Lottery.permissions.has(player, "lottery.admin")) {
                 parseCommand(player, args);
                 return true;
             }
             return false;
         } else {
             return false;
         }
     }
 
     public void parseCommand(Player player, String[] split) {
 
         if (split.length == 0) {
             for (String o : output)
                 player.sendMessage(o);
             // } else if (split[0].equalsIgnoreCase("config") ||
             // split[0].equalsIgnoreCase("c")) {
             // config(player, split);
         } else if (split[0].equalsIgnoreCase("addtopot")
                 || split[0].equalsIgnoreCase("a")) {
             addToPot(player, split);
         } else if (split[0].equalsIgnoreCase("removefrompot")
                 || split[0].equalsIgnoreCase("r")) {
             removeFromPot(player, split);
         } else if (split[0].equalsIgnoreCase("draw")
                 || split[0].equalsIgnoreCase("d")) {
             forceDraw(player, split);
         } else {
 
         }
     }
 
     public void config(Player player, String[] split) {
         if (split.length == 1) {
             player.sendMessage("HELP");
         } else if (split[1].equalsIgnoreCase("")) {
 
         }
     }
 
     public void addToPot(Player player, String[] split) {
         if (split.length == 1) {
             player.sendMessage("HELP");
         } else {
             int num = 0;
             try {
                 num = Integer.parseInt(split[1]);
             } catch (NumberFormatException ex) {
                 ex.printStackTrace();
             }
             Settings.setProperty("lottery.POT", Settings.getExtraPot() + num);
         }
     }
 
     public void removeFromPot(Player player, String[] split) {
         if (split.length == 1) {
             player.sendMessage("HELP");
         } else {
             int num = 0;
             try {
                 num = Integer.parseInt(split[1]);
             } catch (NumberFormatException ex) {
                 ex.printStackTrace();
             }
             Settings.setProperty("lottery.POT", Settings.getExtraPot() - num);
         }
     }
 
    public void forceDraw(Player player, String[] split) {
        plugin.sendGlobalMessage(LotteryObject.drawWinner());
     }
 }
