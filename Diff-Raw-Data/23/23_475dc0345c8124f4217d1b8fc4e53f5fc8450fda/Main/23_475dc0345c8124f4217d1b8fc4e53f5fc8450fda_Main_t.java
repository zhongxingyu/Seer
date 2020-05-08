 package me.docredstone.gc;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Main extends JavaPlugin {
 
	asdjkeInjectGUI injectGUI = new asdjkeInjectGUI();
	
 	public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String args[]) {
		if (cmdLabel.equalsIgnoreCase("joinchat")) {
			Player speaker = (Player) sender;
			injectGUI.visibleChat.add(speaker);
 		}
 		return false;
 	}
 	
 }
