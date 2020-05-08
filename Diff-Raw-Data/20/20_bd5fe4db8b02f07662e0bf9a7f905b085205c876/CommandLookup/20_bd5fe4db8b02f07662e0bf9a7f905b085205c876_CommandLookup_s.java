 package me.arno.blocklog.commands;
 
 import me.arno.blocklog.logs.BlockEntry;
 import me.arno.blocklog.logs.LogType;
 import me.arno.blocklog.search.BlockSearch;
 import me.arno.blocklog.util.Syntax;
 import me.arno.blocklog.util.Util;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class CommandLookup extends BlockLogCommand {
 	public CommandLookup() {
 		super("blocklog.lookup");
		setCommandUsage("/bl lookup [player <value>] [entity <value>] [since <value>] [until <value>] [area <value>]");
 	}
 
 	@Override
 	public boolean execute(CommandSender sender, Command cmd, String[] args) {
 		if(args.length < 2)
 			return false;
 		
 		if(args.length % 2 != 0) {
 			sender.sendMessage("Invalid amount of args");
 			return true;
 		}
 		
 		if(!hasPermission(sender)) {
 			sender.sendMessage("You don't have permission");
 			return true;
 		}
 		
 		Player player = (Player) sender;
 		
 		Syntax syn = new Syntax(args);
 		
 		int untilTime = syn.getTimeFromNow("until");
 		int sinceTime = syn.getTimeFromNow("since");
 		
 		if(untilTime != 0 && sinceTime > untilTime) {
 			player.sendMessage("Until can't be bigger than since.");
 			return true;
 		}
 		
 		BlockSearch search = new BlockSearch();
 		
 		search.setPlayer(syn.getString("player"));
 		search.setEntity(syn.getString("entity"));
 		search.setArea(syn.getInt("area"));
		search.setLocation(new Location(Bukkit.getWorld(syn.getString("world")), syn.getInt("x"), syn.getInt("y"), syn.getInt("z")));
 		search.setRollback(syn.getInt("rollback"));
 		search.setDate(sinceTime, untilTime);
 		search.setLimit(getSettingsManager().getMaxResults());
 		
 		player.sendMessage(ChatColor.YELLOW + "Edit Search" + ChatColor.DARK_GRAY + " -------------------------------");
 		player.sendMessage(ChatColor.GRAY + Util.addSpaces("Name", 90) + Util.addSpaces("Action", 75) + "Details");
 		
         for(BlockEntry block : search.getResults()) {
         	String name = Material.getMaterial(block.getBlock()).toString();
 			LogType type = block.getType();
 			
 			player.sendMessage(Util.addSpaces(ChatColor.GOLD + block.getPlayer(), 99) + Util.addSpaces(ChatColor.DARK_RED + type.name(), 81) + ChatColor.GREEN + name + ChatColor.AQUA + " [" + Util.getDate(block.getDate()) + "]");
 		}
 		return true;
 	}
 
 }
