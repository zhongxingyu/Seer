 package me.Ryan6338.ColouredPlayerList;
 
 import java.util.List;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public final class ColouredPlayerList extends JavaPlugin {
 	public final Logger logger = Logger.getLogger("Minecraft");
 	public PlayerListener listener;
 	private Permissions permissions = new Permissions(this);
 
 	//Performs on Disable
 	
 	@Override
 	public void onDisable() {
 		PluginDescriptionFile pdfFile = this.getDescription();
 		logger.info(pdfFile.getName() + " has been disabled!");
 	}
 
 	//Performs on Enable if the version is correct
 	
 	@Override
 	public void onEnable() {
 		this.saveDefaultConfig();
 		RefreshAll();
 		PluginManager pm = this.getServer().getPluginManager();
 		
 		//Registers events in the PlayerListener class
 		
 		pm.registerEvents(new PlayerListener(this), this);
 	}
 	
 	//Allows Permissions to have an instance of ColouredPlayerList
 	
 	public Permissions getPermissions() {
 		return permissions;
 	}
 	
 	//Logs in the console messages from the plugin
 	
 	public void Log(String text) {
 		logger.info("[ColouredPlayerList] " + text);
 	}
 	
 	public void setName(Player p, ChatColor c) {
 		String pname = ChatColor.stripColor(p.getDisplayName());
 		
 		//Checks to see if the nickname is too long
 		
 		if (this.getConfig().getBoolean("Add Dots") == true) {
 			if (pname.length() > 12) {
 				pname = pname.substring(0, 12) + "..";
 			}
 		} else {
 			if (pname.length() > 14) {
 				pname = pname.substring(0, 14);
 			}
 		}
 		p.setPlayerListName(c + pname);
 	}
 	
 	public void Delay(final Player p, final ChatColor c) {
 		if (c != null) {
 			getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
 				@Override
 				public void run() {
 					setName(p, c);
 				}
 			}, 2L);
 		}
 	}
 	
 	public void RefreshAll() {
 		Player[] po = getServer().getOnlinePlayers();
 		for (int x = 0; x < po.length; x++) {
 			ChatColor c = getPermissions().getColour(po[x]);
 			Delay(po[x], c);
 		}
 	}
 	
 	public void CommandCheck(String cmd) {
 		List<?> nicks = this.getConfig().getList("Nick Commands");
 		for (int i = 0; i < nicks.size(); i++) {
 			String nick = nicks.get(i).toString().toLowerCase();
 			if(cmd.toLowerCase().startsWith("/" + nick)) {
 				RefreshAll();
 			}
 		}
 	}
 	
 	public boolean onCommand(CommandSender sender, Command cmd,
 			String commandLabel, String[] args){
 		Player p = (Player) sender;
 		if (commandLabel.equalsIgnoreCase("cpl")) {
 			if(args[0].equalsIgnoreCase("reload")) {
 				if(p.hasPermission("cpl.reload")) {
 					this.reloadConfig();
 					RefreshAll();
 					p.sendMessage(ChatColor.GRAY + "[Coloured Player List] " +
 							ChatColor.GREEN + "Reloaded!");
 				} else {
 					p.sendMessage(ChatColor.RED + "Insufficient Permission!");
 				}
			} else {
				p.sendMessage("/cpl reload");
 			}
 		}
 		return false;
 	}
 }
