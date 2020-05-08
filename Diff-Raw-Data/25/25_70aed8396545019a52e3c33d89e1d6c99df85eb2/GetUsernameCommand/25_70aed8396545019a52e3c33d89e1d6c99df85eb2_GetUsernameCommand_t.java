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
import org.apache.commons.lang.StringUtils;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 
 /**
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
            String partialName = ArrayHelpers.combinedWithSeperator(args, " ");
             List<? extends PlayerData> playerDataList = playerHandler.getAllPlayerDatas();
            sender.sendMessage(ColorList.TOP_SEPERATOR + " -- " + ColorList.TOP + "AutoCompletes for " + ColorList.NAME + partialName + ColorList.TOP_SEPERATOR + " --");
            for (int numSent = 0, i = 0; i < playerDataList.size() && numSent < 10; i++) {
                PlayerData pd = playerDataList.get(i);
                 if (pd.getUsername().equals(pd.getDisplayname())) {
                    if (StringUtils.containsIgnoreCase(pd.getUsername(), partialName)) {
                         sender.sendMessage(ColorList.NAME + pd.getUsername());
                        numSent++;
                     }
                 } else {
                    if (StringUtils.containsIgnoreCase(pd.getUsername(), partialName) || StringUtils.containsIgnoreCase(ChatColor.stripColor(pd.getDisplayname()), partialName)) {
                         sender.sendMessage(ColorList.NAME + pd.getUsername() + ColorList.DIVIDER + " | " + ColorList.NAME + pd.getDisplayname());
                        numSent++;
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
