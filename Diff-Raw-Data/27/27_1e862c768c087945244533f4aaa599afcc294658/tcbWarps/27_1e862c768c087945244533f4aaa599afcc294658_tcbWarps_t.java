 package pro.homiecraft.Commands;
 
 import java.io.File;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 
 import pro.homiecraft.TheCommandBook;
 
 public class tcbWarps implements CommandExecutor {
 	public boolean onCommand (CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		if (sender.hasPermission("tcb.Warps")){
 			if(cmd.getName().equalsIgnoreCase("warps")){	
 				String path  = TheCommandBook.pluginST.getDataFolder() + "/data/warps/";
				File folder = new File(path); 
 				
				String[] fileNames = folder.list();
				String warps = fileNames.toString();
				String[] warpsS = warps.split(".");
				
				sender.sendMessage("Warps: " + warpsS);
 			}
 		}
 		return false;
	}
 }
