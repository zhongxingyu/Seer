 package de.codeinfection.quickwango.Announcer;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import org.bukkit.Server;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 /**
  *
  * @author CodeInfection
  */
 public class AnnounceCommand implements CommandExecutor
 {
     protected Server server;
     protected File messageDir;
 
     public AnnounceCommand(Server server, File dataFolder)
     {
         this.server = server;
         this.messageDir = new File(dataFolder, "manualMessages");
         this.messageDir.mkdirs();
     }
 
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
     {
         if (sender instanceof Player)
         {
             if (!Announcer.has((Player)sender, "Announcer.announce"))
             {
                sender.sendMessage("Permission denied!");
                 return true;
             }
         }
         if (args.length > 0)
         {
             if (!args[0].matches("[\\.\\\\/\\*\\?:\\|<>\"]"))
             {
                 File messageFile = new File(this.messageDir, args[0] + ".txt");
                 if (messageFile.exists())
                 {
                     try
                     {
                         BufferedReader reader = new BufferedReader(new FileReader(messageFile));
                         String line = "";
                         while ((line = reader.readLine()) != null)
                         {
                             this.server.broadcastMessage(line.trim().replaceAll("&([a-f0-9])", "\u00A7$1"));
                         }
                         reader.close();
                     }
                     catch (IOException e)
                     {
                         sender.sendMessage("Failed to load the announcement!");
                         Announcer.error("Failed to load the message file " + messageFile.getName() + "!", e);
                     }
                 }
                 else
                 {
                     sender.sendMessage("The given requested announcement is not available!");
                 }
             }
             else
             {
                 sender.sendMessage("The given name is invalid!");
             }
         }
         else
         {
             sender.sendMessage("No file name given!");
             return false;
         }
         return true;
     }
 }
