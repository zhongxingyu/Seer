 package nl.taico.tekkitrestrict;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.eclipse.jdt.annotation.NonNull;
 
 public class Send {
 	public CommandSender sender;
 	
 	public void msg(@NonNull String msg){
 		sender.sendMessage(msg);
 	}
 
 	public void msg(@NonNull String command, @NonNull String explanation, ChatColor color, ChatColor color2){
 		int msglength = command.length() + 3 + explanation.length();
		if (msglength<=55)
 			sender.sendMessage(color + command + " - " + ChatColor.RESET + color2 + explanation);
 		else {
 			sender.sendMessage(color + command);
 			sender.sendMessage(color2 + " - " + explanation);
 		}
 	}
 	public void msg(@NonNull String command, @NonNull String explanation){
 		int msglength = command.length() + 3 + explanation.length();
		if (msglength<=55) sender.sendMessage(ChatColor.BLUE + command + " - " + ChatColor.GREEN + explanation);
 		else {
 			sender.sendMessage(ChatColor.BLUE + command);
 			sender.sendMessage(ChatColor.GREEN + " - " + explanation);
 		}
 	}
 	
 	public boolean noPerm(@NonNull String perm){
 		if (sender.hasPermission("tekkitrestrict." + perm)) return false;
 		sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
 		return true;
 	}
 	
 	public boolean noConsole(){
 		if (sender instanceof Player) return false;
 		sender.sendMessage(ChatColor.RED + "This command can not be run from the console!");
 		return true;
 	}
 }
