 package me.arno.blocklog.commands;
 
 
 import me.arno.blocklog.logs.DataEntry;
 import me.arno.blocklog.search.DataSearch;
 import me.arno.blocklog.util.Syntax;
 import me.arno.blocklog.util.Util;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 
 public class CommandSearch extends BlockLogCommand {
 	public CommandSearch() {
 		super("blocklog.search", true);
 		setCommandUsage("/bl search [player <value>] [data <value>] [type <value>] [world <value>] [x|y|z <value>] [since <value>] [until <value>]");
 	}
 
 	@Override
 	public boolean execute(CommandSender sender, Command cmd, String[] args) {
 		if(args.length < 3)
 			return false;
 		
 		if(!hasPermission(sender)) {
 			sender.sendMessage("You don't have permission");
 			return true;
 		}
 		
 		Syntax syn = new Syntax(args);
 		
 		int untilTime = syn.getTimeFromNow("until");
 		int sinceTime = syn.getTimeFromNow("since");
 		
 		if(untilTime != 0 && sinceTime > untilTime) {
 			sender.sendMessage(ChatColor.WHITE + "Until can't be bigger than since.");
 			return true;
 		}
 		
 		DataSearch search = new DataSearch();
 		
 		search.setPlayer(syn.getString("player"));
 		search.setData(syn.getString("data"));
		search.setLocation(new Location(Bukkit.getWorld(syn.getString("world")), syn.getInt("x"), syn.getInt("y"), syn.getInt("z")));
 		search.setType(syn.getInt("type"));
 		search.setDate(sinceTime, untilTime);
 		search.setLimit(getSettingsManager().getMaxResults());
 		
		sender.sendMessage(ChatColor.YELLOW + "Data Search " + ChatColor.DARK_GRAY + " -------------------------------");
 		sender.sendMessage(ChatColor.GRAY + Util.addSpaces("Player", 90) + Util.addSpaces("Action", 75) + "Date");
 		
 		for(DataEntry data : search.getResults()) {
			sender.sendMessage(ChatColor.DARK_BLUE + Util.addSpaces(data.getPlayer(), 90) + ChatColor.DARK_PURPLE + Util.addSpaces(data.getType().toString(), 75) + ChatColor.BLUE + Util.getDate(data.getDate()));
 			sender.sendMessage(ChatColor.DARK_RED + "Data: " + ChatColor.GOLD + data.getData());
 		}
 		return true;
 	}
 
 }
