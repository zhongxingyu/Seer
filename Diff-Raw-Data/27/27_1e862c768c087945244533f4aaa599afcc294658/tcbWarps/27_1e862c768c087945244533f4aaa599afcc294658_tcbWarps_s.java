 package pro.homiecraft.Commands;
 
 import java.io.File;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 
 import pro.homiecraft.TheCommandBook;
 
 public class tcbWarps implements CommandExecutor {
	@SuppressWarnings("unused")
 	public boolean onCommand (CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		if (sender.hasPermission("tcb.Warps")){
 			if(cmd.getName().equalsIgnoreCase("warps")){	
 				String path  = TheCommandBook.pluginST.getDataFolder() + "/data/warps/";
 				
				  String files;
				  File folder = new File(path);
				  File[] listOfFiles = folder.listFiles(); 
				 
				  for (int i = 0; i < listOfFiles.length; i++) 
				  {
				 
				   if (listOfFiles[i].isFile()) 
				   {
				   files = listOfFiles[i].getName();
				   sender.sendMessage(files);
				   }
				return true;
 			}
 		}
	}
 		return false;
}
 }
