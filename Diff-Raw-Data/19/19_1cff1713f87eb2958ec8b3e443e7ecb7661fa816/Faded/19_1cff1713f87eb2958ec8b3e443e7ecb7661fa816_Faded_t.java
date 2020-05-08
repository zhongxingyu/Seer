 package me.lilfade.faded;
 
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Faded extends JavaPlugin{

 	@Override
 	public void onEnable() {
 	//	PluginDescriptionFile pdfFile = this.getDescription();
 	//	this.logger.info(pdfFile.getName() + " Version " + pdfFile.getVersion() + " Has been Enabled!");		
 		PluginManager manager = this.getServer().getPluginManager();
 		manager.registerEvents(new FadedPlayerListener(),  this);
 	}
 	
 	@Override
 	public void onDisable() {
 	//	PluginDescriptionFile pdfFile = this.getDescription();
 	//	this.logger.info(pdfFile.getName() + " Has Been Disabled");
 	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		Player player = (Player) sender;
		if(commandLabel.equalsIgnoreCase("bug")) {
			//we got a bug here
			player.sendMessage("bug sent");
		}else if(commandLabel.equalsIgnoreCase("suggestion")){
			//we got a suggestion
			player.sendMessage("suggestion sent");
		}
		return false;
	}
 }
