 package de.darcade.minecraftlottery;
 
import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class CommandExecutorClass implements CommandExecutor {
 
 	private Main main;
 	
 	public CommandExecutorClass(Main main) {
 		this.main = main;
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String commandlabel,
 			String[] args) {
 		Player p =  (Player) sender;
 		if(cmd.getName().equalsIgnoreCase("lottery")){
 			if(args.length == 0) {
 				this.pringHelp(p);
 			}
 			
 			p.sendMessage(String.valueOf(args.length));
 			return true;
 		}
 		return false;
 	}
 
 	private void pringHelp(Player p){
 		p.sendMessage("Please enter some random ");
 	}
 }
