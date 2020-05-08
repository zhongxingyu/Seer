 package net.skycraftmc.SkyQuest;
 
 import java.util.ArrayList;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class SkyQuestCmd implements CommandExecutor 
 {
 	private SkyQuestPlugin plugin;
 	public SkyQuestCmd(SkyQuestPlugin plugin)
 	{
 		this.plugin = plugin;
 	}
 	private final CmdDesc[] help = {
 		new CmdDesc("quest help", "Shows this menu", null),
		new CmdDesc("quest questlog [player]", "Gives you a questlog", "skyquest.cmd.questlog"),
 		new CmdDesc("quest assign [player] <id>", "Assigns a quest to a player", "skyquest.cmd.assign"),
 		new CmdDesc("quest sassign [player] <id>", "Silently assigns a quest to a player", "skyquest.cmd.assign")
 	};
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
 	{
 		//TODO Assign command
 		if(args.length >= 1)
 		{
 			if(args[0].equalsIgnoreCase("questlog"))questlogCmd(sender, label, args);
 			else if(args[0].equalsIgnoreCase("assign"))assignCmd(sender, label, args, false);
 			else if(args[0].equalsIgnoreCase("sassign"))assignCmd(sender, label, args, true);
 			else if(args[0].equalsIgnoreCase("help"))helpCmd(sender, args, help, "SkyQuest Help");
 			else sender.sendMessage(ChatColor.GOLD + "Unrecognized command.  Type " + ChatColor.AQUA + 
 				slash(sender) + "quest help" + ChatColor.GOLD + " for help.");
 		}
 		else
 		{
 			sender.sendMessage(ChatColor.AQUA + "SkyQuest version " + plugin.getDescription().getVersion());
 			sender.sendMessage(ChatColor.GOLD + "Type " + ChatColor.AQUA + slash(sender) + "quest help" + 
 				ChatColor.GOLD + " for help.");
 		}
 		return true;
 	}
 	private boolean assignCmd(CommandSender sender, String label, String[] args, boolean silent)
 	{
 		if(noPerm(sender, "skyquest.cmd.assign"))return true;
 		Player target = null;
 		Quest q = null;
 		if(args.length == 2)
 		{
 			if(!(sender instanceof Player))return silent ? true : usage(sender, label + " assign <player> <id>");
 			target = (Player)sender;
 			q = plugin.getQuestManager().getQuest(args[1]);
 		}
 		else if(args.length == 3)
 		{
 			target = plugin.getServer().getPlayer(args[1]);
 			if(target == null)
 				return silent ? true : msg(sender, ChatColor.RED + args[1] + " cannot be found.");
 			q = plugin.getQuestManager().getQuest(args[2]);
 		}
 		if(q == null)return silent ? true : msg(sender, ChatColor.RED + (args.length == 2 ? args[1] : args[2]) + " doesn't exist!");
 		PlayerQuestLog d = plugin.getQuestManager().getQuestLog(target.getName());
 		if(d.isAssigned(q))return silent ? true : msg(sender, ChatColor.RED + (args.length == 2 ? 
 			args[1] : args[2]) + " has already been assigned to " + target.getName() + "!");
 		d.assign(q);
 		if(!silent)sender.sendMessage(ChatColor.GREEN + q.getID() + " has been assigned to " + target.getName() + ".");
 		return true;
 	}
 	private boolean questlogCmd(CommandSender sender, String label, String[] args)
 	{
 		if(noPerm(sender, "skyquest.cmd.questlog"))return true;
 		Player target = null;
 		if(args.length == 1)
 		{
 			if(!(sender instanceof Player))return usage(sender, label + " questlog <player>");
 			target = (Player)sender;
 		}
 		else if(args.length == 2)
 		{
 			target = plugin.getServer().getPlayer(args[1]);
 			if(target == null)
 				return msg(sender, ChatColor.RED + args[1] + " cannot be found.");
 		}
 		else return usage(sender, label + " questlog [player]");
 		if(target.getInventory().addItem(plugin.createQuestLogItem()).isEmpty())
 			return msg(sender, ChatColor.GREEN + (target == sender ? "You have" : args[1] + " has") 
 				+ " been given a quest log.");
 		else return msg(sender, ChatColor.RED + (target == sender ? "Your" : args[1] + "'s") 
 			+ " inventory is full.");
 	}
 	private boolean helpCmd(CommandSender sender, String[] args, CmdDesc[] help, String title)
 	{
 		int page = 1;
 		if(args.length == 2)
 		{
 			try{page = Integer.parseInt(args[1]);}catch(NumberFormatException nfe){return msg(sender, ChatColor.RED + "\"" + args[1] + "\" is not a valid number");}
 		}
 		ArrayList<String>d = new ArrayList<String>();
 		int max = 1;
 		int cmda = 0;
 		for(int i = 0; i < help.length; i ++)
 		{
 			CmdDesc c = help[i];
 			if(c.getPerm() != null)
 			{
 				if(!sender.hasPermission(c.getPerm()))continue;
 			}
 			if(d.size() < 10)
 			{
 				if(i >= (page - 1)*10 && i <= ((page - 1)*10) + 9)d.add(c.asDef());
 			}
 			if(cmda > 10 && cmda % 10 == 1)max ++;
 			cmda ++;
 		}
 		sender.sendMessage(ChatColor.GOLD + title + "(" + ChatColor.AQUA + page + ChatColor.GOLD + "/" + ChatColor.AQUA + max + ChatColor.GOLD + ")");
 		for(String s:d)sender.sendMessage(s);
 		return true;
 	}
 	private boolean noPerm(CommandSender sender, String perm)
 	{
 		if(!sender.hasPermission(perm))
 			return msg(sender, ChatColor.RED + perm);
 		return false;
 	}
 	private boolean msg(CommandSender sender, String msg)
 	{
 		sender.sendMessage(msg);
 		return true;
 	}
 	private boolean usage(CommandSender sender, String usage)
 	{
 		return msg(sender, ChatColor.RED + "Usage: " + slash(sender) + usage);
 	}
 	private String slash(CommandSender sender)
 	{
 		return sender instanceof Player ? "/" : "";
 	}
 	private class CmdDesc
 	{
 		private String cmd;
 		private String desc;
 		private String perm;
 		public CmdDesc(String cmd, String desc, String perm)
 		{
 			this.cmd = cmd;
 			this.desc = desc;
 			this.perm = perm;
 		}
 		public String asDef()
 		{
 			return ChatColor.AQUA + cmd + ChatColor.RED + " - " + ChatColor.GOLD + desc;
 		}
 		public String getPerm()
 		{
 			return perm;
 		}
 	}
 }
