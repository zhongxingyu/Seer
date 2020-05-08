 /*
  * Author: Dabo Ross
  * Website: www.daboross.net
  * Email: daboross@daboross.net
  */
 package net.daboross.bukkitdev.playerdata;
 
 import java.util.List;
 import net.daboross.bukkitdev.playerdata.api.PlayerData;
 import net.daboross.bukkitdev.playerdata.api.PlayerHandler;
 import net.daboross.bukkitdev.playerdata.libraries.commandexecutorbase.ArrayHelpers;
 import net.daboross.bukkitdev.playerdata.libraries.commandexecutorbase.ColorList;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 
 /**
 * This is the Command Executor for the command /gu.
  *
  * @author daboross
  */
 public class GetUsernameCommand implements CommandExecutor {
 
     private final PlayerHandler playerHandler;
 
     protected GetUsernameCommand(PlayerHandler playerHandler) {
         this.playerHandler = playerHandler;
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
         if (args.length > 0) {
            String givenName = ArrayHelpers.combinedWithSeperator(args, " ").toLowerCase();
             List<? extends PlayerData> playerDataList = playerHandler.getAllPlayerDatas();
            sender.sendMessage(ColorList.TOP_SEPERATOR + " -- " + ColorList.TOP + "AutoCompletes for " + ColorList.NAME + givenName + ColorList.TOP_SEPERATOR + " --");
            for (int i = 0; i < playerDataList.size(); i++) {
                final PlayerData pd = playerDataList.get(i);
                final String checkUsername = pd.getUsername().toLowerCase();
                final String checkDisplayname = ChatColor.stripColor(pd.getDisplayname()).toLowerCase();
                 if (pd.getUsername().equals(pd.getDisplayname())) {
                    if (checkUsername.contains(givenName)) {
                         sender.sendMessage(ColorList.NAME + pd.getUsername());
                     }
                 } else {
                    if (checkUsername.contains(givenName) || checkDisplayname.contains(givenName)) {
                         sender.sendMessage(ColorList.NAME + pd.getUsername() + ColorList.DIVIDER + " | " + ColorList.NAME + pd.getDisplayname());
                     }
                 }
             }
         } else {
             sender.sendMessage(ColorList.ERR + "Please specify a player");
             sender.sendMessage(ColorList.CMD + "/" + label + ColorList.ARGS + " <Partial Name>");
         }
         return true;
     }
 }
