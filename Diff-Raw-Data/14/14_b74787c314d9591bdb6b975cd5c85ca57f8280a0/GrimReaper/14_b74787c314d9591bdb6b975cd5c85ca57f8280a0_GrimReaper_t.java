 package me.Pluto1099.GrimReaper;
 
 import java.util.logging.Logger;
 
import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class GrimReaper extends JavaPlugin {
 	public final Logger logger = Logger.getLogger("Minecraft");
 	public static GrimReaper plugin;
 	
 	@Override
 	public void onDisable() {
 		PluginDescriptionFile pdfFile = this.getDescription();
 		this.logger.info(pdfFile.getName() + "I will rise again!");
 		this.saveDefaultConfig();
 	}
 	
 	@Override
 	public void onEnable () {
 		PluginDescriptionFile pdfFile = this.getDescription();
 		this.logger.info(pdfFile.getName() + "I have risen!");
 	}
 	
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
 		Player player = (Player) sender;
		if(commandLabel.equalsIgnoreCase("death")){
 			player.sendMessage(ChatColor.YELLOW + this.getConfig().getString("message"));
 		    player.setHealth(0);
		    Bukkit.broadcastMessage(ChatColor.YELLOW + "Reaper got Revenge on " + ChatColor.WHITE + player.getDisplayName());
		} return true;
 	}
 }
