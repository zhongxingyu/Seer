 
 package com.quartercode.basiccommands.command;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URL;
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import com.quartercode.minecartrevolution.command.MRCommandHandler;
 import com.quartercode.minecartrevolution.conf.Conf;
 import com.quartercode.minecartrevolution.conf.URLConf;
 import com.quartercode.minecartrevolution.exception.MinecartRevolutionSilenceException;
 import com.quartercode.minecartrevolution.get.Lang;
 import com.quartercode.quarterbukkit.QuarterBukkit;
 import com.quartercode.quarterbukkit.api.command.Command;
 import com.quartercode.quarterbukkit.api.command.CommandInfo;
 
 public class InfoCommand extends MRCommandHandler {
 
     public InfoCommand() {
 
     }
 
     @Override
     public CommandInfo createInfo() {
 
         return new CommandInfo(true, null, Lang.getValue("basiccommands.info.description"), "minecartrevolution.command.info", "info");
     }
 
     @Override
     public void execute(final Command command) {
 
         final InfoThread thread = new InfoThread();
         thread.sender = command.getSender();
         thread.start();
     }
 
     class InfoThread extends Thread {
 
         public CommandSender sender;
 
         @Override
         public void run() {
 
             final int downloadCount = getDownloadCount();
 
             sender.sendMessage(ChatColor.GREEN + "========== [" + Lang.getValue("basiccommands.info.start") + "] ==========");
 
             if (downloadCount >= 0) {
                 sender.sendMessage(ChatColor.YELLOW + "Total " + Conf.NAME + " Downloads: " + ChatColor.DARK_GREEN + downloadCount);
                 sender.sendMessage("");
             }
 
             sender.sendMessage("This plugin was born at a Tuesday, on the");
             sender.sendMessage(ChatColor.DARK_PURPLE + "21th February 2012" + ChatColor.RESET + ", and filled with creativity.");
             sender.sendMessage("The first run was with only a few lines of code, and that");
             sender.sendMessage("was the beginning of a big thing which's called");
             sender.sendMessage(ChatColor.GOLD + Conf.NAME + ChatColor.RESET + ". Today it's one of the largest & most");
             sender.sendMessage("versatile Minecart plugins ever made.");
            sender.sendMessage(ChatColor.AQUA + "There is no limit with " + ChatColor.GOLD + Conf.NAME + ChatColor.RESET + "!");
         }
 
         private int getDownloadCount() {
 
             try {
                 final URL url = new URL(URLConf.BUKKIT_DEV_URL);
                 final BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
 
                 String line;
                 while ( (line = reader.readLine()) != null) {
                     if (line.contains("<dt>Downloads</dt>")) {
                         try {
                             line = reader.readLine();
                             return Integer.parseInt(line.split("\"")[1].trim());
                         }
                         catch (final NumberFormatException e) {
                             return -1;
                         }
                     }
                 }
                 reader.close();
             }
             catch (final IOException e) {
                 QuarterBukkit.exception(new MinecartRevolutionSilenceException(minecartRevolution, e, "Failed to load download count"));
             }
 
             return -1;
         }
     }
 
 }
