 package me.ccattell.plugins.completeeconomy.commands;
 
 import java.util.HashMap;
 import me.ccattell.plugins.completeeconomy.database.CEMainResultSet;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 /**
  *
  * @author Charlie
  */
 public class CECommands implements CommandExecutor {
 
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
         if (cmd.getName().equalsIgnoreCase("ce")) {
             // do some stuff
             sender.sendMessage("You just used the /ce command!");
             return true;
         }
         if (cmd.getName().equalsIgnoreCase("cash")) {
             String name;
             String name_type;
             Player player = null;
             if (sender instanceof Player) {
                 player = (Player) sender;
             }
             if (player != null) {
                 if (args.length < 1) {
                     name_type = "arg";
                     name = args[0];
                 }else{
                     name_type = "player";
                     name = player.getName();
                 }
             } else {
                 // command run from console so will need to supply a player name
                 if (args.length < 1) {
                     sender.sendMessage("You must supply a player name");
                     return false;
                 }
                 name_type = "arg";
                 name = args[0];
             }
             HashMap<String, Object> where = new HashMap<String, Object>();
             where.put("player_name", name);
             CEMainResultSet rsm = new CEMainResultSet(where);
             float c;
             if (rsm.resultSet()) {
                 // found a record so load data
                 c = rsm.getCash();
                 // do something with it
             } else {
                 c = 0;
             }
            if(name_type == "arg"){
                 sender.sendMessage(name + "'s cash balance: " + c);
             }else{
                 sender.sendMessage("Your cash balance: " + c);                
             }
             return true;
         }
         if (cmd.getName().equalsIgnoreCase("pay")) {
             // pay another player amount shown and save to the database
             sender.sendMessage("You just used the /pay command!");
             return true;
         }
         return false;
     }
 }
