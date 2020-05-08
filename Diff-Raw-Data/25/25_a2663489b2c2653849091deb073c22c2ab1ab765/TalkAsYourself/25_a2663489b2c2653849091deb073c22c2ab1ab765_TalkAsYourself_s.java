 package me.blha303;
 
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Listener;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class TalkAsYourself extends JavaPlugin implements Listener {
 
 	public Logger log = Logger.getLogger("Minecraft");
 
 	public void info(String str) {
 		log.info("[TalkToYourself] " + str);
 	}
 
 	public void onEnable() {
 		getConfig().addDefault("string", "&f<%name%&f>");
 		getConfig().addDefault("name", "&4Console");
 		getConfig().options().copyDefaults(true);
 		saveConfig();
 		info("Enabled");
 	}
 
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
 		String msg = "";
 
 		if (sender instanceof Player) {
 			sender.sendMessage(ChatColor.RED + "You can't use this command! ");
			sender.sendMessage(ChatColor.GRAY
					+ "(If you wanted /tell, try /m instead)");
 			return true;
 		}
 
		if (args.length == 0) {
			return false;
		}
 
 		if (command.getName().equalsIgnoreCase("talk")) {
 			for (int i = 0; i < args.length; i++) {
 				msg += args[i] + " ";
 			}
			String mesg = ChatColor.translateAlternateColorCodes('&',
					getConfig().getString("string").replace("%name%",
							getConfig().getString("name"))
							+ " " + msg);
 			getServer().broadcastMessage(mesg);
 			return true;
 		}
 
 		if (command.getName().equalsIgnoreCase("talkas")) {
 			for (int i = 1; i < args.length; i++) {
 				msg += args[i] + " ";
 			}
 			String talkas = args[0];
			String mesg = ChatColor.translateAlternateColorCodes('&', getConfig()
					.getString("string").replace("%name%", talkas)
					+ " " + msg);
 			getServer().broadcastMessage(mesg);
 			return true;
 		}
 		return false;
 
 	}
 
 }
