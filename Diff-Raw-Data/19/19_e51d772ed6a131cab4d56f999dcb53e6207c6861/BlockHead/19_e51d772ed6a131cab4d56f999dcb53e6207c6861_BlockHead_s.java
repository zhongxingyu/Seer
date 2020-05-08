 package com.zand.bukkit.blockhead;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.nijikokun.bukkit.Permissions.Permissions;
 
 /**
  * BlockHead for Bukkit
  * 
  * @author zand
  */
 public class BlockHead extends JavaPlugin {
 	public String name;
 	public String versionInfo;
 	private static Logger log = Logger.getLogger("Minecraft");
 
 	// private final HashMap<Player, Boolean> debugees = new HashMap<Player,
 	// Boolean>();
 	private final List<String> commands = new ArrayList<String>();
 
 	public Permissions Permissions = null;
 
 	@Override
 	public void onEnable() {
 		PluginDescriptionFile desc = getDescription();
 		name = desc.getName();
 		String authors = "";
 		for (String author : desc.getAuthors())
 			authors += ", " + author;
 		versionInfo = name + " version " + desc.getVersion()
 				+ (authors.isEmpty() ? "" : " by" + authors.substring(1));
 
 		commands.add(name.toLowerCase());
 		commands.add("hat");
 
 		setupOtherPlugins();
 
 		log.info(versionInfo + " is enabled!");
 	}
 
 	@Override
 	public void onDisable() {
 		if (true)
 			return;
 	}
 
 	/**
 	 * Setup dynamic support for other plugins.
 	 */
 	private void setupOtherPlugins() {
 		// Permissions
 		Plugin test = this.getServer().getPluginManager().getPlugin(
 				"Permissions");
 		if (this.Permissions == null) {
 			if (test != null) {
 				this.Permissions = (Permissions) test;
 				log.info("[" + name + "] Found Permissions plugin. Using it.");
 			}
 		}
 	}
 
 	@SuppressWarnings("static-access")
 	public List<Player> getGroupPlayers(String group) {
 		ArrayList<Player> ret = new ArrayList<Player>();
 
 		if (this.Permissions != null)
 			for (Player player : getServer().getOnlinePlayers())
 				if (this.Permissions.Security.getGroup(
 						player.getWorld().getName(), player.getName())
 						.toLowerCase().startsWith(group.toLowerCase()))
 					ret.add(player);
 
 		return ret;
 	}
 
 	@SuppressWarnings("static-access")
 	boolean checkPermission(CommandSender sender, String nodes) {
 		if (sender instanceof Player) {
 			Player player = (Player) sender;
 			if (this.Permissions == null)
 				return nodes == "blockhead.hat" || player.isOp()
 						&& !nodes.startsWith("blockhead.hat.give.");
 			return this.Permissions.Security.permission(player, nodes);
		} else if (sender instanceof ConsoleCommandSender) {
 			if (nodes.startsWith("blockhead.hat.give."))
 				return true;
 			else
 				return false;
 		}
 		return false;
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command command,
 			String commandLabel, String[] args) {
 		Player player = null;
 		if (sender instanceof Player)
 			player = (Player) sender;
 
 		if (args.length == 1) {
 			// print the current version
 			if (args[0].equals("help")) {
 				showHelp(command.getName().toLowerCase(), sender);
 			} else if (args[0].startsWith("ver"))
 				sender.sendMessage(versionInfo);
 			else { // /hat [item id]
 				if (checkPermission(sender, "blockhead.hat.items")) {
 					// Get the stack
 					ItemStack stack = stackFromString(args[0], 0);
 
 					// Check the stack
 					if (stack == null || stack.getTypeId() > 255
 							|| stack.getTypeId() < 1) {
 						sender.sendMessage(ChatColor.RED + args[0]
 								+ " is not a valid block");
 						return true;
 					}
 					placeOnHead(player, stack);
 				}
 				else
 					sender.sendMessage(ChatColor.DARK_RED
 							+ "Your not allowed to use that command");
 			}
 
 		} else if (args.length == 2) { // hat [player] [itemid]
 			if (checkPermission(sender, "blockhead.hat.give.players.items")) {
 				// Get the stack
 				ItemStack stack = stackFromString(args[1], 0);
 
 				// Check the stack
 				if (stack == null || stack.getTypeId() > 255
 						|| stack.getTypeId() < 1) {
 					sender.sendMessage(ChatColor.RED + args[1]
 							+ " is not a valid block");
 					return true;
 				}
 
 				// Look for Player
 				List<Player> players = getServer().matchPlayer(args[0]);
 
 				// Player not Found
 				if (players.size() < 1)
 					sender.sendMessage(ChatColor.RED + "Could not find player");
 
 				// More than 1 Player Found
 				else if (players.size() > 1) {
 					sender.sendMessage(ChatColor.RED
 							+ "More than one player found");
 					String msg = "";
 					for (Player other : players)
 						msg += " " + other.getName();
 					sender.sendMessage(msg.trim());
 				}
 
 				// Player Found
 				else {
 					Player other = players.get(0);
 					placeOnHead(other, stack);
 					sender.sendMessage("Putting a block on " + other.getName()
 							+ "'s head.");
 				}
 			} else
 				sender.sendMessage(ChatColor.DARK_RED
 						+ "Your not allowed to use that command");
 
 		} else if (args.length > 2 && args[0].equalsIgnoreCase("group")) { // hat
 			// group
 			// [group]
 			// [block
 			// id]
 			if (checkPermission(sender, "blockhead.hat.give.groups.items")) {
 				// Get the stack
 				ItemStack stack = stackFromString(args[2], 0);
 
 				// Check the stack
 				if (stack == null || stack.getTypeId() > 255
 						|| stack.getTypeId() < 1) {
 					sender.sendMessage(ChatColor.RED + args[2]
 							+ " is not a valid block");
 					return true;
 				}
 
 				// Look for Player
 				List<Player> players = getGroupPlayers(args[1]);
 
 				// Player not Found
 				if (players.size() < 1)
 					sender.sendMessage(ChatColor.RED
 							+ "Could not find any players in " + args[1]);
 
 				// Player Found
 				else {
 					for (Player other : players)
 						placeOnHead(other, stack);
 					sender.sendMessage("Putting blocks on players in "
 							+ args[1] + " heads.");
 				}
 			} else
 				sender.sendMessage(ChatColor.DARK_RED
 						+ "Your not allowed to use that command");
 		} else if (checkPermission(sender, "blockhead.hat")) { // hat
 			placeOnHead(player, player.getItemInHand());
 		} else
 			sender.sendMessage(ChatColor.DARK_RED
 					+ "Your not allowed to use that command");
 		return true;
 	}
 
 	/**
 	 * Shows BlockHeads help to the player
 	 * 
 	 * @param cmd
 	 *            The command to show for.
 	 * @param player
 	 *            The player to show to.
 	 */
 	private void showHelp(String cmd, CommandSender sender) {
 		ChatColor ch = ChatColor.LIGHT_PURPLE;
 		ChatColor cc = ChatColor.WHITE;
 		ChatColor cd = ChatColor.GOLD;
 		ChatColor ct = ChatColor.YELLOW;
 		sender.sendMessage(ch + versionInfo);
 		sender.sendMessage(cc + "/" + cmd + " help " + cd + "-" + ct
 				+ " Displays this");
 		sender.sendMessage(cc + "/" + cmd + " version " + cd + "-" + ct
 				+ " Displays the current version");
 		if (checkPermission(sender, "blockhead.hat"))
 			sender.sendMessage(cc + "/" + cmd + " " + cd + "-" + ct
 					+ " Puts the currently held item on your head");
 		if (checkPermission(sender, "blockhead.hat.items"))
 			sender.sendMessage(cc + "/" + cmd + " [block] " + cd + "-" + ct
 					+ " Puts a block with block id on your head");
 		if (checkPermission(sender, "blockhead.hat.give.players.items"))
 			sender.sendMessage(cc + "/" + cmd + " [player] [block] " + cd
 					+ "-" + ct + " Puts a block on another player");
 		if (checkPermission(sender, "blockhead.hat.give.groups.items"))
 			sender.sendMessage(cc + "/" + cmd + " group [group] [block] "
 					+ cd + "-" + ct
 					+ " Puts blocks on all the players in that group");
 	}
 
 	/**
 	 * Places one block from the ItemStack onto the Players Head
 	 * 
 	 * @param player
 	 *            The player to place the block on.
 	 * @param item
 	 *            The ItemStack to take from.
 	 * @return If the item was placed on the players head.
 	 */
 	private boolean placeOnHead(Player player, ItemStack item) {
 		PlayerInventory inv = player.getInventory();
 		if (item.getType() == Material.AIR) {
 			player.sendMessage(ChatColor.RED + "You have nothing in your hand");
 			return false;
 		}
 
 		int id = item.getTypeId();
 		if (id < 1 || id > 255) {
 			player.sendMessage(ChatColor.RED
 					+ "You can't put that item on your head");
 			return false;
 		}
 
 		ItemStack helmet = inv.getHelmet();
 		ItemStack hat = new ItemStack(item.getType(), (item.getAmount() < 1 ? item.getAmount() : 1), item.getDurability());
		hat.setData(item.getData());
 
 		inv.setHelmet(hat);
 		if (item.getAmount() > 1)
 			item.setAmount(item.getAmount() - 1);
 		else
 			inv.remove(item);
 
 		// put what was in the helmet spot back
 		if (helmet.getAmount() > 0) {
 			HashMap<Integer, ItemStack> leftover = inv.addItem(helmet);
 			if (!leftover.isEmpty()) {
 				player
 						.sendMessage("Was unble to put the old headhear away, droping it at your feet");
 
 				// Drop the stacks
 				for (Map.Entry<Integer, ItemStack> e : leftover.entrySet())
 					player.getWorld().dropItem(player.getLocation(),
 							e.getValue());
 			}
 		}
 
 		player.sendMessage("Enjoy your new Headgear");
 		return true;
 	}
 
 	@Override
 	public void onLoad() {
 		// TODO Auto-generated method stub
 	}
 
 	public ItemStack stackFromString(String item, int count) {
 		Material material = Material.matchMaterial(item);
 		if (material == null)
 			return null;
 		return new ItemStack(material, count);
 	}
 }
