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
                //ArrayList<String> targetNames = new ArrayList<String>();
                
                StringBuilder sb = new StringBuilder();
				for(int i = 0; i < fileNames.length; i++)
				{
				    sb.append(fileNames[i]).append(" ");
				}
				String msg = sb.toString().trim();
				sender.sendMessage("Warps: " + msg.replace(".yml", ""));
 				
//                for (int i = 0; i < fileNames.length; i++) {
//                  String[] tnt = fileNames[i].split(".");
//                  
//                  if(tnt.length == 1){
//                	targetNames.add(tnt[0]);
//                    sender.sendMessage("Warps: " + tnt[0]);
//                    return true;
//                  }                  
//                }
                return true;
 			}
 		}
 		return false;
 	}
 }
