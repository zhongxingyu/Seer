 package psy.ActivityHistory.cmd;
 
 import java.util.Date;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.FileNotFoundException;
 
 import org.bukkit.plugin.Plugin;
 import org.bukkit.entity.Player;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import psy.ActivityHistory.ActivityHistory;
 import psy.ActivityHistory.PlayerLogFile;
 import psy.util.TimeRange;
 
 public class FilePQCE extends PlayerQueryCommandExecutor{
     private ActivityHistory plugin;
     public FilePQCE(Plugin pl){
         plugin = (ActivityHistory)pl;
     }
     
     public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
         if(args.length < 1){
             return false;
         }
         
         PlayerLogFile file = null;
         try{
             file = loadLogFile(args[0]);
         }catch(FileNotFoundException e){
             sender.sendMessage("Could not find the log file.");
             return true;
         }catch(IOException e){
             sender.sendMessage("An error occurred when loading the log file.");
             return true;
         }
             
         Player player = null;
         if(sender instanceof Player)
             player = (Player) sender;
             
         String mode = cmd.getName();
         TimeRange range = CmdUtils.parseRange(sender, args);
         if(range==null) return true;
         if(mode.equalsIgnoreCase("ppercent")){
             Integer hour = CmdUtils.parseHour(sender, args);
             if(hour==null) return true;
             double percent = file.tallyActivityPercent(range, hour);
             if(percent<=0) sender.sendMessage("There is no record of that player.");
             else sender.sendMessage("" + percent + "%");
         }else if(mode.equalsIgnoreCase("ptotal")){
             sender.sendMessage(file.tallyActivityTotal(range));
         }else if(mode.equalsIgnoreCase("phours")){
             double[] data = new double[24];
            String message = "";
            //Note: This is a temporary implementation. In the future it will draw a graph.
             for(int i=0; i<24; i++){
                 data[i] = file.tallyActivityPercent(range, i);
                if(data[i]<=0){
                    sender.sendMessage("There is no record of that player.");
                    return true;
                }else{
                    message += "" + i + ":00- " + data[i] + "%   ";
                }
             }
            sender.sendMessage(message);
         }
         return true;
     }
     
     private PlayerLogFile loadLogFile(String name)throws IOException{
         String filename = plugin.accessConfig().getString("general.logFilesLocation") + "/" + name.toLowerCase() + ".log";
         PlayerLogFile file = new PlayerLogFile(filename);
         return file;
     }
 }
