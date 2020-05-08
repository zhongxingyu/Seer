 package me.PerwinCZ.DigitalClock;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class Commands implements CommandExecutor {
 	private Main plugin;
 	
 	protected Commands(Main instance) {
 		this.plugin = instance;
 	}
 	
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
 		if(command.getName().equalsIgnoreCase("digitalclock") && args.length > 0 && sender instanceof Player) {
 			Player player = (Player) sender;
 			if(args[0].equals("create")) {
 				if(args.length != 2) {
 					player.sendMessage(ChatColor.RED + "[DigitalClock] Correct usage: '/digitalclock create <name>'");
 				} else if(!player.hasPermission("digitalclock.create") && !player.isOp()) {
 					player.sendMessage(ChatColor.RED + "[DigitalClock] You aren't allowed to use this command!");
 				} else if(plugin.enableBuildUsers.containsKey(player)) {
 					player.sendMessage(ChatColor.RED + "[DigitalClock] You are just creating another clock. You can't create more clocks in the same time!");
 				} else if(plugin.getConfig().getKeys(false).contains(args[1])) {
 					player.sendMessage(ChatColor.RED + "[DigitalClock] Clock with this name already exists!");
 				} else {
 					int count = 0;
 					if(this.plugin.usersClock.get(player.getName()) != null) {
 						count = this.plugin.usersClock.get(player.getName());
 					}
 					boolean limitReached = false;
					if(player.hasPermission("digitalclock.limit." + count)) {
 						limitReached = true;
 					}
 					if(limitReached == false) {
 				    	plugin.enableBuildUsers.put(player, args[1]);
 				      	player.sendMessage(ChatColor.GREEN + "[DigitalClock] Now you can create your " + (count+1) + ". clock. Click with any block anywhere to set start block.");
 					} else {
 				      	player.sendMessage(ChatColor.RED + "[DigitalClock] You can't create next clock. You have reached the limit of " + count + " clocks.");
 					}
 				}
 			} else if(args[0].equals("remove") || args[0].equals("delete")) {
 				if(args.length != 2) {
 					player.sendMessage(ChatColor.RED + "[DigitalClock] Correct usage: '/digitalclock remove <name>'");
 				} else if(!player.hasPermission("digitalclock.remove") && !player.isOp()) {
 					player.sendMessage(ChatColor.RED + "[DigitalClock] You aren't allowed to use this command!");
 				} else if(!plugin.getConfig().getKeys(false).contains(args[1])) {
 					player.sendMessage(ChatColor.RED + "[DigitalClock] Clock '" + args[1] + "' not found!");
 				} else {		
 					Clock clock = Clock.loadClockByClockName(args[1]);
 					Clock.remove(clock);
 					plugin.getClocks();
 					player.sendMessage(ChatColor.GREEN + "[DigitalClock] Your clock '" + args[1] + "' has been successfully removed.");
 				}
 			} else if(args[0].equals("rotate")) {
 				if(args.length != 3) {
 					player.sendMessage(ChatColor.RED + "[DigitalClock] Correct usage: '/digitalclock rotate <name> <direction>'");
 				} else if(!player.hasPermission("digitalclock.rotate") && !player.isOp()) {
 					player.sendMessage(ChatColor.RED + "[DigitalClock] You aren't allowed to use this command!");
 				} else if(!plugin.getConfig().getKeys(false).contains(args[1])) {
 					player.sendMessage(ChatColor.RED + "[DigitalClock] Clock '" + args[1] + "' not found!");
 				} else if(!args[2].equals("north") && !args[2].equals("south") && !args[2].equals("east") && !args[2].equals("west")) {
 					player.sendMessage(ChatColor.RED + "[DigitalClock] Direction can be only north, south, east or west!");
 				} else {	
 					Clock clock = Clock.loadClockByClockName(args[1]);
 					player.sendMessage(ChatColor.GREEN + "[DigitalClock] Your clock '" + args[1] + "' rotated successfully from " + clock.getDirection().name().toLowerCase() + " to " + clock.rotate(args[2]).name().toLowerCase());
 				}
 			} else if(args[0].equals("material")) {
 				if(args.length != 3) {
 					player.sendMessage(ChatColor.RED + "[DigitalClock] Correct usage: '/digitalclock material <name> <material id>'");
 				} else if(!player.hasPermission("digitalclock.material") && !player.isOp()) {
 					player.sendMessage(ChatColor.RED + "[DigitalClock] You aren't allowed to use this command!");
 				} else if(!plugin.getConfig().getKeys(false).contains(args[1])) {
 					player.sendMessage(ChatColor.RED + "[DigitalClock] Clock '" + args[1] + "' not found!");
 				} else {	
 					Clock clock = Clock.loadClockByClockName(args[1]);
 					player.sendMessage(ChatColor.GREEN + "[DigitalClock] Your clock '" + args[1] + "' changed material from " + clock.getMaterial().name() + " to "+ clock.changeMaterial(Integer.parseInt(args[2])).name());
 				}
 			} else if(args[0].equals("fill")) {
 				if(args.length != 3) {
 					player.sendMessage(ChatColor.RED + "[DigitalClock] Correct usage: '/digitalclock fill <name> <material id>'");
 				} else if(!player.hasPermission("digitalclock.fill") && !player.isOp()) {
 					player.sendMessage(ChatColor.RED + "[DigitalClock] You aren't allowed to use this command!");
 				} else if(!plugin.getConfig().getKeys(false).contains(args[1])) {
 					player.sendMessage(ChatColor.RED + "[DigitalClock] Clock '" + args[1] + "' not found!");
 				} else {	
 					Clock clock = Clock.loadClockByClockName(args[1]);
 					player.sendMessage(ChatColor.GREEN + "[DigitalClock] Your clock '" + args[1] + "' changed filling material from " + clock.getFillingMaterial().name() + " to "+ clock.setFillingMaterial(Integer.parseInt(args[2])).name());
 				}
 			} else if(args[0].equals("move")) {
 				if(args.length != 2) {
 					player.sendMessage(ChatColor.RED + "[DigitalClock] Correct usage: '/digitalclock move <name>'");
 				} else if(!player.hasPermission("digitalclock.move") && !player.isOp()) {
 					player.sendMessage(ChatColor.RED + "[DigitalClock] You aren't allowed to use this command!");
 				} else if(!plugin.getConfig().getKeys(false).contains(args[1])) {
 					player.sendMessage(ChatColor.RED + "[DigitalClock] Clock '" + args[1] + "' not found!");
 				} else if(plugin.enableMoveUsers.containsKey(player)) {
 					player.sendMessage(ChatColor.GREEN + "[DigitalClock] Moving clock '" + args[1] + "' has been rejected!");
 					plugin.enableMoveUsers.remove(player);
 				} else {	
 					plugin.enableMoveUsers.put(player, args[1]);
 					player.sendMessage(ChatColor.GREEN + "[DigitalClock] Moving clock '" + args[1] + "' has been enabled. Now just right click to some place to move your clock there.");
 				}
 			} else if(args[0].equals("tp")) {
 				if(args.length != 2) {
 					player.sendMessage(ChatColor.RED + "[DigitalClock] Correct usage: '/digitalclock tp <name>'");
 				} else if(!player.hasPermission("digitalclock.tp") && !player.isOp()) {
 					player.sendMessage(ChatColor.RED + "[DigitalClock] You aren't allowed to use this command!");
 				} else if(!plugin.getConfig().getKeys(false).contains(args[1])) {
 					player.sendMessage(ChatColor.RED + "[DigitalClock] Clock '" + args[1] + "' not found!");
 				} else {	
 					Clock clock = Clock.loadClockByClockName(args[1]);
 					clock.teleportToClock(player);
 					player.sendMessage(ChatColor.GREEN + "[DigitalClock] You have been successfully teleported to your clock '" + args[1] + "'.");
 				}
 			} else if(args[0].equals("list")) {
 				if(args.length != 1) {
 					player.sendMessage(ChatColor.RED + "[DigitalClock] Correct usage: '/digitalclock list'");
 				} else if(!player.hasPermission("digitalclock.list") && !player.isOp()) {
 					player.sendMessage(ChatColor.RED + "[DigitalClock] You aren't allowed to use this command!");
 				} else {	
 					player.sendMessage(ChatColor.GREEN + "[DigitalClock] List of all existing clocks:");
 					String list = "";
 					int i = 0;
 					if(plugin.clocks.size() != 0) {
 						for(String name : plugin.clocks) {
 							Clock clock = Clock.loadClockByClockName(name);
 							list += clock.getName();
 							if(i != plugin.clocks.size()-1) {
 								list += ", ";
 							}
 							i++;
 						}
 					} else {
 						list = ChatColor.ITALIC + "No clocks found!";
 					}
 					player.sendMessage(ChatColor.GREEN + list);
 				}
 			} else if(args[0].equals("reload")) {
 				if(args.length != 1) {
 					player.sendMessage(ChatColor.RED + "[DigitalClock] Correct usage: '/digitalclock reload'");
 				} else if(!player.hasPermission("digitalclock.reload") && !player.isOp()) {
 					player.sendMessage(ChatColor.RED + "[DigitalClock] You aren't allowed to use this command!");
 				} else {	
 					plugin.reloadSettings();
 					player.sendMessage(ChatColor.GREEN + "[DigitalClock] File settings.yml has been reloaded!");
 				}
 			} else if(args[0].equals("help") || args[0].equals("?")) {
 				if(args.length != 1) {
 					player.sendMessage(ChatColor.RED + "[DigitalClock] Correct usage: '/digitalclock help'");
 				} else if(!player.hasPermission("digitalclock.help") && !player.isOp()) {
 					player.sendMessage(ChatColor.RED + "[DigitalClock] You aren't allowed to use this command!");
 				} else {	
 					player.sendMessage(ChatColor.GREEN + "[DigitalClock] List of all commands:");
 					player.sendMessage(ChatColor.DARK_GREEN + "/digitalclock create <name>" + ChatColor.GREEN + " - enables creating a new clock for you");
 					player.sendMessage(ChatColor.DARK_GREEN + "/digitalclock remove/delete <name>" + ChatColor.GREEN + " - removes the clock");
 					player.sendMessage(ChatColor.DARK_GREEN + "/digitalclock rotate <name> <direction>" + ChatColor.GREEN + " - rotates the clock to north/east/south/west");
 					player.sendMessage(ChatColor.DARK_GREEN + "/digitalclock material <name> <id>" + ChatColor.GREEN + " - changes the material of clock");
 					player.sendMessage(ChatColor.DARK_GREEN + "/digitalclock fill <name> <id>" + ChatColor.GREEN + " - changes the filling material (air) between blocks of clock");
 					player.sendMessage(ChatColor.DARK_GREEN + "/digitalclock move <name>" + ChatColor.GREEN + " - enables moving with the clock for you");
 					player.sendMessage(ChatColor.DARK_GREEN + "/digitalclock tp <name>" + ChatColor.GREEN + " - teleports you to the clock");
 					player.sendMessage(ChatColor.DARK_GREEN + "/digitalclock list" + ChatColor.GREEN + " - lists all existsing clocks");
 					player.sendMessage(ChatColor.DARK_GREEN + "/digitalclock reload" + ChatColor.GREEN + " - reloads data from the settings.yml file");
 					player.sendMessage(ChatColor.DARK_GREEN + "/digitalclock help/?" + ChatColor.GREEN + " - opens this help window");
 				}
 			} else {
 				player.sendMessage(ChatColor.RED + "[DigitalClock] This argument doesn't exist. Show '/digitalclock help' for more info.");
 			}
 			return true;
 		} else {
 			sender.sendMessage(ChatColor.RED + "[DigitalClock] This command can be executed only from game and has to have minimally 1 argument!");
 		}
 		return false;
 	}
 
 }
