 package net.daboross.bukkitdev.playerdata;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import org.bukkit.Bukkit;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 
 /**
  *
  * @author daboross
  */
 public final class PlayerDataCommandExecutor extends CommandExecutorBase {
 
     private PlayerData playerDataMain;
 
     /**
      *
      */
     protected PlayerDataCommandExecutor(PlayerData playerDataMain) {
         this.playerDataMain = playerDataMain;
         initCommand("help", new String[]{"?"}, true, "playerdata.help", "This Command Views This Page");
         initCommand("viewinfo", new String[]{"getinfo", "i"}, true, "playerdata.viewinfo", (ColorList.ARGS + "<Player>" + ColorList.HELP + " Gets the Info That Player data has stored on a player"));
         initCommand("recreateall", new String[]{}, true, "playerdata.admin", ("This command deletes all player data and recreates it from bukkit!"));
         initCommand("list", new String[]{"lp", "pl", "l"}, true, "playerdata.list", "This Command Lists All Players Who have ever joined the server. In Pages.");
     }
 
     /**
      *
      * @param sender
      * @param cmd
      * @param label
      * @param args
      * @return
      */
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
         if (cmd.getName().equalsIgnoreCase("pd")) {
             String commandName = isCommandValid(sender, cmd, label, args);
             if (commandName == null) {
                 return true;
             }
             if (commandName.equalsIgnoreCase("viewinfo")) {
                 runViewInfoCommand(sender, cmd, args);
             } else if (commandName.equalsIgnoreCase("recreateall")) {
                 runReCreateAllCommand(sender, cmd, args);
             } else if (commandName.equalsIgnoreCase("list")) {
                 runListCommand(sender, cmd, args[0], getSubArray(args));
             }
             return true;
         }
         return false;
     }
 
     private void runReCreateAllCommand(CommandSender sender, Command cmd, String[] args) {
         sender.sendMessage(ColorList.MAIN + "Now Recreating All Player Data!");
         int numberLoaded = playerDataMain.getPDataHandler().createEmptyPlayerDataFilesFromBukkit();
         sender.sendMessage(ColorList.MAIN + "Player Data has loaded " + ColorList.NUMBER + numberLoaded + ColorList.MAIN + " new data files");
     }
 
     private void runViewInfoCommand(CommandSender sender, Command cmd, String[] args) {
         if (args.length < 2) {
             sender.sendMessage(ColorList.ILLEGALARGUMENT + "Must Provide A Player!");
             sender.sendMessage(getHelpMessage(args[0], cmd.getName()));
             return;
         }
         String playerName = playerDataMain.getPDataHandler().getFullUsername(args[1]);
         if (playerName == null) {
             sender.sendMessage(ColorList.ERROR + "Player: " + ColorList.ERROR_ARGS + args[1] + ColorList.ERROR + " not found!");
             return;
         }
         PData pData = playerDataMain.getPDataHandler().getPDataFromUsername(playerName);
         if (pData == null) {
             sender.sendMessage(ColorList.ERROR + "Player: " + ColorList.ERROR_ARGS + args[1] + ColorList.ERROR + " not found!");
             return;
         }
         sender.sendMessage(ColorList.MAIN + "Info Avalible For " + ColorList.NAME + pData.userName() + ColorList.MAIN + ":");
         ArrayList<String> linesToSend = new ArrayList<String>();
         linesToSend.add(ColorList.MAIN + "Display Name: " + ColorList.NAME + pData.nickName(true));
         if (pData.isOnline()) {
             linesToSend.add(ColorList.NAME + pData.userName() + ColorList.MAIN + " is online");
             Long[] logIns = pData.logIns();
             linesToSend.add(ColorList.NAME + pData.userName() + ColorList.MAIN + " has been online " + ColorList.NUMBER + PlayerData.getFormattedDDate(System.currentTimeMillis() - logIns[logIns.length - 1]));
         } else {
             linesToSend.add(ColorList.NAME + pData.userName() + ColorList.MAIN + " is not online");
             linesToSend.add(ColorList.NAME + pData.userName() + ColorList.MAIN + " was last seen " + ColorList.NUMBER + PlayerData.getFormattedDDate(System.currentTimeMillis() - pData.lastSeen()) + ColorList.MAIN + " ago");
         }
         linesToSend.add(ColorList.MAIN + "Times logged into " + ColorList.SERVERNAME + Bukkit.getServerName() + ColorList.MAIN + ": " + ColorList.NUMBER + pData.logIns().length);
         linesToSend.add(ColorList.MAIN + "Times logged out of " + ColorList.SERVERNAME + Bukkit.getServerName() + ColorList.MAIN + ": " + ColorList.NUMBER + pData.logOuts().length);
         linesToSend.add(ColorList.MAIN + "Time Played On " + ColorList.SERVERNAME + Bukkit.getServerName() + ColorList.MAIN + ": " + ColorList.NUMBER + PlayerData.getFormattedDDate(pData.timePlayed()));
         linesToSend.add(ColorList.MAIN + "First Time On " + ColorList.SERVERNAME + Bukkit.getServerName() + ColorList.MAIN + " was  " + ColorList.NUMBER + PlayerData.getFormattedDDate(System.currentTimeMillis() - pData.getFirstLogIn()) + ColorList.MAIN + " ago");
         linesToSend.add(ColorList.MAIN + "First Time On " + ColorList.SERVERNAME + Bukkit.getServerName() + ColorList.MAIN + " was  " + ColorList.NUMBER + new Date(pData.getFirstLogIn()));
         if (PlayerData.isPEX()) {
             linesToSend.add(ColorList.NAME + pData.userName() + ColorList.MAIN + " is currently " + ColorList.NUMBER + pData.getGroup());
         }
         PDataHandler pdh = playerDataMain.getPDataHandler();
         for (Data d : pData.getData()) {
             linesToSend.addAll(Arrays.asList(pdh.getDisplayData(d, false)));
         }
         sender.sendMessage(linesToSend.toArray(new String[0]));
     }
 
     private void runListCommand(CommandSender sender, Command cmd, String aliasLabel, String[] args) {
         if (args.length > 1) {
             sender.sendMessage(ColorList.MAIN + "Please Use Only 1 Number After " + ColorList.CMD + "/" + cmd.getName() + ColorList.SUBCMD + " " + aliasLabel);
         }
         int pageNumber;
         if (args.length == 0) {
             pageNumber = 1;
         } else {
             try {
                 pageNumber = Integer.valueOf(args[0]);
             } catch (Exception e) {
                 sender.sendMessage(ColorList.ERROR_ARGS + args[0] + ColorList.ERROR + " is not a number.");
                 sender.sendMessage(getHelpMessage(aliasLabel, cmd.getLabel()));
                 return;
             }
             if (pageNumber < 1) {
                 sender.sendMessage(ColorList.ERROR_ARGS + args[0] + ColorList.ERROR + " is not a non-0 positive number.");
                 return;
             }
         }
         PData[] pDataList = playerDataMain.getPDataHandler().getAllPDatas();
         ArrayList<String> messagesToSend = new ArrayList<String>();
         messagesToSend.add("");
         messagesToSend.add(ColorList.MAIN_DARK + "Player List, Page " + ColorList.NUMBER + pageNumber + ColorList.MAIN_DARK + ":");
         for (int i = ((pageNumber - 1) * 6); i < ((pageNumber - 1) * 6) + 6 & i < pDataList.length; i++) {
             PData current = pDataList[i];
            messagesToSend.add(ColorList.NAME + current.userName() + ColorList.MAIN + " was last seen " + ColorList.NUMBER + PlayerData.getFormattedDDate(current.lastSeen()) + ColorList.MAIN + " ago.");
         }
         if (pageNumber < (pDataList.length / 6.0)) {
             messagesToSend.add(ColorList.MAIN_DARK + "To View The Next Page, Type: " + ColorList.CMD + "/" + cmd.getName() + ColorList.SUBCMD + " " + aliasLabel + ColorList.ARGS + " " + (pageNumber + 1));
         }
         sender.sendMessage(messagesToSend.toArray(new String[0]));
     }
 }
