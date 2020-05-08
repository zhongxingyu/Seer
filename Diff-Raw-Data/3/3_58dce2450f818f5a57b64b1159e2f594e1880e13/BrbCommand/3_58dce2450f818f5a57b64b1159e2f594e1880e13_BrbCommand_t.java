 package coolawesomeme.basics_plugin.commands;
 
 import org.bukkit.Bukkit;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import coolawesomeme.basics_plugin.Basics;
 import coolawesomeme.basics_plugin.MinecraftColors;
 
 public class BrbCommand implements CommandExecutor{
 	
 	private Basics basics;
 	private String[] owners;
 	public static boolean isOwnerBRBing = false;
 	
 	public BrbCommand(Basics instance){
 		basics = instance;
 		owners = basics.owners;
 	}
 
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		if (!(sender instanceof Player)) {
 			brbCommand(sender, cmd, label, args);
 			return true;
 		} else {
 			boolean flag = false;
 			for(int i=0;i<owners.length;i++){
 				if(sender.getName().equals(owners[i]) || sender.getName() == owners[i]){
 					flag=true;
 				}
 			}
 			if(flag){
 				brbCommand(sender, cmd, label, args);
 				return true;
 			}else{
				sender.sendMessage("You must be the owner to do that!");
				return true;
 			}
 		}
 	}
 	
 	private void brbCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		if(args == null || args.equals(null) || args.length == 0){
 			if(isOwnerBRBing){
 				isOwnerBRBing = false;
 				Bukkit.broadcastMessage(MinecraftColors.lightPink + "[Basics] The server owner is no longer away!");
 			}else{
 				isOwnerBRBing = true;
 				Bukkit.broadcastMessage(MinecraftColors.lightPink + "[Basics] The server owner is away!");
 			}
 		}else{
 			if(args.length > 1){
 				sender.sendMessage("This command has 1 arguments!");
 			}
 			else{
 				boolean oldBRB = isOwnerBRBing;
 				isOwnerBRBing = Boolean.parseBoolean(args[0]);
 				if(oldBRB != isOwnerBRBing)
 					Bukkit.broadcastMessage(isOwnerBRBing ? (MinecraftColors.lightPink + "[Basics] The server owner is away!") : (MinecraftColors.lightPink + "[Basics] The server ownder is no longer away!"));
 			}
 		}
 	}
 
 }
