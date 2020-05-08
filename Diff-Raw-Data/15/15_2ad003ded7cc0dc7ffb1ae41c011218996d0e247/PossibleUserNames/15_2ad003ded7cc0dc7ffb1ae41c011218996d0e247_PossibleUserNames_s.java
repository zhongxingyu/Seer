 package net.daboross.bukkitdev.playerdata;
 
 import net.daboross.bukkitdev.commandexecutorbase.ColorList;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 
 /**
  * This is the Command Executor for the command /gu.
  *
  * @author daboross
  */
 public class PossibleUserNames implements CommandExecutor {
 
     private PlayerData instance;
 
     /**
      * This creates a new PossibleUserNames with the main PData pd.
      *
      * @param pd The current loaded PlayerData.
      */
     protected PossibleUserNames(PlayerData pd) {
         instance = pd;
     }
 
     /**
      * This is the method to call to run the "gu" command on a sender.
      *
      * @param sender The CommandSender who ran the command.
      * @param cmd The command.
      * @param label The label of the command.
      * @param args The arguments of the command.
      */
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("gu")) {
             if (args.length > 0) {
                 String givenPlayerName = PlayerData.getCombinedString(args, 0);
                 String[] users = instance.getPDataHandler().getPossibleUsernames(givenPlayerName);
                 if (users.length > 10) {
                     sender.sendMessage(ColorList.MAIN + "First Ten Possible Auto Completes for " + ColorList.NAME + givenPlayerName + ColorList.MAIN + ":");
                 } else {
                     sender.sendMessage(ColorList.MAIN + "Possible Auto Completes for " + ColorList.NAME + givenPlayerName + ColorList.MAIN + ":");
                 }
                 for (int i = 0; i < users.length && i < 10; i++) {
                     sender.sendMessage(users[i]);
                 }
                 sender.sendMessage(ColorList.MAIN + "Total Auto Completes Found: " + ColorList.NUMBER + users.length);
             } else {
                 sender.sendMessage(ColorList.MAIN + "Please Specify A Player!");
                 sender.sendMessage(ColorList.CMD + "/" + label + ColorList.ARGS + " <PartialUsername>");
             }
             return true;
         }
         return false;
     }
 }
