 package com.andoutay.clockblocker;
 
 import java.util.HashMap;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class ClockBlocker extends JavaPlugin
 {
 	private Logger log = Logger.getLogger("Minecraft");
 	public static String chPref = ChatColor.DARK_RED + "[ClockBlocker] " + ChatColor.RESET;
 	public static String logPref = "[ClockBlocker] ";
 	private boolean monitoringEnabled;
 	private PluginManager pm;
 	private CBBlockRedstoneHandler brm;
 	
 	public void onLoad()
 	{
 		new CBConfig(this);
 		brm = new CBBlockRedstoneHandler(this);
 	}
 	
 	@Override
 	public void onEnable()
 	{
 		CBConfig.onEnable();
 		pm = getServer().getPluginManager();
 		pm.registerEvents(brm, this);
 		
 		monitoringEnabled = CBConfig.monitorOnLaunch;
 		
 		log.info(logPref + "enabled successfully!");
 	}
 	
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
 	{
 		Player p = null;
 		String cmdName = cmd.getName();
 		if (sender instanceof Player)
 			p = (Player)sender;
 		
 		if (cmdName.equalsIgnoreCase("clockblocker"))
 			if (args.length == 1 && args[0].equalsIgnoreCase("start"))
 			{
 				if (p == null || p.hasPermission("clockblocker.start"))
 					if (CBConfig.allowStop)
 					{
 						startMonitor();
 						sender.sendMessage(chPref + "Monitoring enabled");
 					}
 					else
 						sender.sendMessage(chPref + ChatColor.RED + "The server admin is not allowing use of that command");
 				else
 					sender.sendMessage(ChatColor.RED + "Just what do you think you're doing?");
 				return true;
 			}
 			else if (args.length == 1 && args[0].equalsIgnoreCase("stop"))
 			{
 				if (p == null || p.hasPermission("clockblocker.stop"))
 					if (CBConfig.allowStop)
 					{
 						stopMonitor();
 						sender.sendMessage(chPref + "Monitoring disabled");
 					}
 					else
 						sender.sendMessage(chPref + ChatColor.RED + "The server admin is not allowing use of that command");
 				else
 					sender.sendMessage(ChatColor.RED + "Just what do you think you're doing?");
 				return true;
 			}
 			else if ((args.length == 1 || args.length == 2) && (args[0].equalsIgnoreCase("view") || args[0].equalsIgnoreCase("list")))
 			{
 				if (p == null || p.hasPermission("clockblocker.view"))
 				{
 					int numPages;
 					try
 					{
 						numPages = (args.length == 2) ? Integer.parseInt(args[1]) : 1;
 					}
 					catch (NumberFormatException e)
 					{
 						numPages = 1;
 					}
 					displaySuspicious(sender, numPages);
 				}
 				else
 					sender.sendMessage(ChatColor.RED + "Just what do you think you're doing?");
 				return true;
 			}
 			else if (args.length == 2 && args[0].equalsIgnoreCase("tp"))
 			{
 				if (p != null && p.hasPermission("clockblocker.tp"))
 				{
 					Object points[] = brm.getSuspicious().keySet().toArray();
 					int num;
 					Location loc;
 					try
 					{
 						num = Integer.parseInt(args[1]) - 1;
 					}
 					catch (NumberFormatException e)
 					{
 						sender.sendMessage(chPref + "Invalid argument");
 						return true;
 					}
 					
 					try
 					{
 						loc = (Location)points[num];
 					}
 					catch (IndexOutOfBoundsException e)
 					{
						sender.sendMessage(chPref + "No suspicious block with id " + num);
 						return true;
 					}
 					
 					p.teleport(loc);
 				}
 				else if (p == null)
 					sender.sendMessage(chPref + "Only players may use that command");
 				else
 					sender.sendMessage(ChatColor.RED + "Just what do you think you're doing?");
 				return true;
 			}
 			else if (args.length == 1 && args[0].equalsIgnoreCase("reload"))
 			{
 				if (p == null || p.hasPermission("clockblocker.reload"))
 				{
 					CBConfig.reload();
 					sender.sendMessage(chPref + "Config reloaded");
 				}
 				else
 					sender.sendMessage(ChatColor.RED + "Just what do you think you're doing?");
 				return true;
 			}
 			else if (args.length == 1 && args[0].equalsIgnoreCase("version"))
 			{
 				if (p == null || p.hasPermission("clockblocker.version"))
 					sender.sendMessage(chPref + "Current version: " + getDescription().getVersion());
 				else
 					sender.sendMessage(ChatColor.RED + "Just what do you think you're doing?");
 				return true;
 			}
 			else if (args.length == 1 && (args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("?")))
 			{
 				sender.sendMessage(chPref + "Help:");
 				sender.sendMessage("usage: /clockblocker <arguments>");
 				sender.sendMessage("Arguments:");
 				sender.sendMessage("start - starts monitoring redstone activity");
 				sender.sendMessage("stop - stops monitoring redstone activity");
 				sender.sendMessage("view - view suspicious activity");
 				sender.sendMessage("list - alias for view");
 				sender.sendMessage("tp <num> - teleport to a suspicious block from the list");
 				sender.sendMessage("version - see the current plugin version");
 				sender.sendMessage("help or ? - view this help message");
 				return true;
 			}
 		
 		return false;
 	}
 	
 	@Override
 	public void onDisable()
 	{
 		log.info(logPref + "disabled successfully!");
 	}
 	
 	private void startMonitor()
 	{
 		monitoringEnabled = true;
 	}
 	
 	private void stopMonitor()
 	{
 		monitoringEnabled = false;
 	}
 	
 	public boolean shouldMonitor()
 	{
 		return monitoringEnabled;
 	}
 	
 	private void displaySuspicious(CommandSender s, int pageNum)
 	{
 		HashMap<Location, Integer> suspicious = brm.getSuspicious();
 		Object points[] = suspicious.keySet().toArray();
 		Object values[] = suspicious.values().toArray();
 		int i, start, end, len = suspicious.size(), numPerPage = 9;
 		int numPages = len / numPerPage + 1;
 		if (pageNum < 1) pageNum = 1;
 		if (pageNum > numPages) pageNum = numPages;
 		start = (pageNum - 1) * numPerPage;
 		end = (len < pageNum * numPerPage) ? len : pageNum * numPerPage;
 		s.sendMessage(chPref + "Suspicious blocks  # " + ChatColor.AQUA + "Location: " + ChatColor.RED + "offenses" + ChatColor.RESET + "  (" + pageNum + "/" + numPages + ") ");
 		for (i = start; i < end; i++)
 			s.sendMessage((i + 1) +  " " + ChatColor.AQUA + "" + locToString(points[i]) + ": " + ChatColor.RED + values[i] + ChatColor.RESET);
 		if (len == 0) s.sendMessage("No suspicious blocks found");
 	}
 	
 	private String locToString(Object obj)
 	{
 		Location loc;
 		if (obj instanceof Location)
 		{
 			loc = (Location)obj;
			return "(" + loc.getX() + ", " + loc.getY() + ", " + loc.getZ() + ") in " + loc.getWorld().getName();
 		}
 		else
 			return obj.toString();
 	}
 }
