 package com.bukkit.furt.CraftEssence;
 
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import net.minecraft.server.WorldServer;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Server;
import org.bukkit.World;
 import org.bukkit.craftbukkit.CraftWorld;
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.PlayerChatEvent;
 import org.bukkit.event.player.PlayerEvent;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.inventory.ItemStack;
 import com.nijikokun.bukkit.Permissions.Permissions;
 
 public class cePlayerListener extends PlayerListener {
 	private final CraftEssence plugin;
 	private final Server srv;
 	public int y;
 	public int rotation;
 
 	public cePlayerListener(CraftEssence instance) {
 		this.plugin = instance;
 		this.srv = plugin.getServer();
 
 	}
 
 	public void onPlayerChat(PlayerChatEvent event) {
 		event.setCancelled(false);
 
 		Player player = event.getPlayer();
 
 		if (player == null) {
 			return;
 		}
 		String group = Permissions.Security.getGroup(player.getName());
 
 		if (group == null) {
 			return;
 		}
 		String prefix = Permissions.Security.getGroupPrefix(group);
 
 		if ((prefix == null) || (prefix.trim().length() != 1)) {
 			return;
 		}
 		String dn = player.getDisplayName();
 		String out = dn.replaceAll(".", "");
 
 		player.setDisplayName(out);
 
 		event.setFormat(event.getFormat().replace(
 				"%1$s",
 				ChatColor.getByCode(Integer.parseInt(prefix, 16)) + "%1$s"
 						+ ChatColor.WHITE));
 	}
 
 	public void onPlayerJoin(PlayerEvent event) {
 		Player player = event.getPlayer();
 		String[] motd = plugin.getMotd();
 		if (motd == null || motd.length < 1) {
 			return;
 		}
 		String location = (int) player.getLocation().getX() + "x, "
 				+ (int) player.getLocation().getY() + "y, "
 				+ (int) player.getLocation().getZ() + "z";
 		String ip = player.getAddress().getAddress().getHostAddress();
 
 		for (String line : motd) {
 			player.sendMessage(plugin.argument(line, new String[] {
 					"+dname,+d", "+name,+n", "+location,+l", "+ip" },
 					new String[] { player.getDisplayName(), player.getName(),
 							location, ip }));
 		}
 		// TODO show mail on login
 
 		/*
 		 * List<String> mail = plugin.readMail(player); if (mail.isEmpty())
 		 * player.sendMessage(ChatColor.GRAY + "You have no new mail."); else
 		 * player.sendMessage("You have " + mail.size() +
 		 * " messages! Type /mail read to view your mail.");
 		 */
 
 		super.onPlayerJoin(event);
 	}
 
 	public void onPlayerCommand(PlayerChatEvent event) {
 		if (event.isCancelled())
 			return;
 
 		Player player = event.getPlayer();
 		String[] sects = event.getMessage().split(" +", 2);
 		String[] args = (sects.length > 1 ? sects[1].split(" +")
 				: new String[0]);
 
 		String prefix = plugin.getConfiguration().getString("command-prefix",
 				"");
 		if (!sects[0].toLowerCase().startsWith("/" + prefix.toLowerCase()))
 			return;
 		String msg = event.getMessage();
 
 		Commands cmd;
 		try {
 			cmd = Commands.valueOf(sects[0].substring(1 + prefix.length())
 					.toUpperCase());
 		} catch (IllegalArgumentException ex) {
 			return;
 		}
 
 		if (!plugin.check(player, cmd)) {
 			player.sendMessage(CraftEssence.premessage
 					+ "You do not have permission to use that command.");
 			return;
 		}
 
 		try {
 			switch (cmd) {
 			case ALERT:
 				alert(player, msg);
 				break;
 
 			case ME:
 				me(player, msg);
 				break;
 
 			case COMPASS:
 				compass(player);
 				break;
 
 			case TELL:
 				break;
 
 			case TP:
 				tp(player, args);
 				break;
 
 			case TPHERE:
 			case TPH:
 				tphere(player, args);
 				break;
 
 			case ITEM:
 			case I:
 				item(player, sects, args);
 				break;
 
 			case GIVE:
 			case G:
 				give(player, sects, args);
 				break;
 
 			case LIST:
 			case PLAYERLIST:
 			case WHO:
 			case ONLINE:
 				list(player, sects, args);
 				break;
 
 			case SETHOME:
 				sethome(player, sects, args);
 				break;
 
 			case HOME:
 				home(player, sects, args);
 				break;
 
 			case COORDS:
 			case GETPOS:
 				getpos(player, sects, args);
 				break;
 
 			case SPAWN:
 				spawn(player, sects, args);
 				break;
 
 			case SETSPAWN:
 				setspawn(player, sects, args);
 				break;
 
 			case TOP:
 				top(player, sects, args);
 				break;
 
 			case SUPPORT:
 				support(player, sects, args);
 				break;
 
 			case TIME:
 				time(player, sects, args);
 				break;
 
 			case CLEARINVENTORY:
 				clearinventory(player, sects, args);
 				break;
 
 			case MAIL:
 				mail(player, sects, args);
 				break;
 
 			case KIT:
 				kit(player, sects, args);
 				break;
 
 			case HELP:
 				help(player, sects, args);
 				break;
 
 			case HEAL:
 				heal(player, sects, args);
 				break;
 
 			case MOTD:
 				motd(player, sects, args);
 				break;
 
 			case WARP:
 				warp(player, args);
 				break;
 
 			case SETWARP:
 				setwarp(player, args);
 				break;
 			}
 
 			Logger.getLogger("Minecraft").log(
 					Level.INFO,
 					String.format("[CraftEssence] %1$s issued command: %2$s",
 							player.getName(), event.getMessage()));
 			event.setCancelled(true);
 		} catch (NoSuchMethodError ex) {
 			player.sendMessage(CraftEssence.premessage
 					+ "The server is not recent enough to support "
 					+ sects[0].toLowerCase() + ".");
 		} catch (Exception ex) {
 			player.sendMessage(ex.getMessage());
 			event.setCancelled(true);
 		}
 	}
 
 	private void alert(Player player, String msg) {
 		for (Player localplayer : srv.getOnlinePlayers()) {
 			msg = msg.replace("/alert", "");
 			localplayer.sendMessage(ChatColor.RED + "[Attention]"
 					+ ChatColor.YELLOW + msg);
 		}
 
 	}
 
 	private void setwarp(Player player, String[] args) {
 		plugin.setWarp(player, player.getLocation(), args);
 
 	}
 
 	private void warp(Player player, String[] args) {
 		player.teleportTo(plugin.getWarp(player, args));
 		player.sendMessage(CraftEssence.premessage + "Warping...");
 
 	}
 
 	private void me(Player player, String msg) {
 		for (Player localplayer : srv.getOnlinePlayers()) {
 			msg = msg.replace("/me", "");
 			localplayer.sendMessage(ChatColor.GRAY + "*" + player.getName()
 					+ msg + "*");
 		}
 	}
 
 	private void compass(Player player) {
 		double degreeRotation = ((player.getLocation().getYaw() - 90) % 360);
 
 		if (degreeRotation < 0) {
 			degreeRotation += 360.0;
 		}
 
 		player.sendMessage(ChatColor.RED + "Compass: "
 				+ getDirection(degreeRotation));
 
 	}
 
 	private void help(Player player, String[] sects, String[] args) {
 		int page;
 		try {
 			page = args.length > 0 ? Integer.parseInt(args[0]) : 1;
 		} catch (Exception ex) {
 			page = -1;
 		}
 
 		player.sendMessage(ChatColor.RED + "Type /help # to change pages.");
 		player.sendMessage(ChatColor.RED + "Page: " + page);
 
 		switch (page) {
 		case 1:
 			if (plugin.check(player, Commands.TP))
 				player.sendMessage(ChatColor.RED + "/tp" + ChatColor.YELLOW
 						+ " [player]: Teleport to player");
 
 			if (plugin.check(player, Commands.TPHERE))
 				player.sendMessage(ChatColor.RED + "/tphere" + ChatColor.YELLOW
 						+ " [player]: Teleport player to you");
 
 			if (plugin.check(player, Commands.ITEM))
 				player.sendMessage(ChatColor.RED + "/item" + ChatColor.YELLOW
 						+ " [item|numeric] <amount>: Spawn items");
 
 			if (plugin.check(player, Commands.GIVE))
 				player.sendMessage(ChatColor.RED
 						+ "/give"
 						+ ChatColor.YELLOW
 						+ " [player] [item|numeric] <amount>: Give player items");
 
 			if (plugin.check(player, Commands.PLAYERLIST))
 				player.sendMessage(ChatColor.RED + "/playerlist"
 						+ ChatColor.YELLOW + ": List all online players");
 
 			if (plugin.check(player, Commands.HOME))
 				player.sendMessage(ChatColor.RED + "/home" + ChatColor.YELLOW
 						+ ": Teleport to your home");
 
 			if (plugin.check(player, Commands.SETHOME))
 				player.sendMessage(ChatColor.RED + "/sethome"
 						+ ChatColor.YELLOW
 						+ ": Set your home to your current location");
 			break;
 
 		case 2:
 			if (plugin.check(player, Commands.CLEARINVENTORY))
 				player.sendMessage(ChatColor.RED + "/clearinventory"
 						+ ChatColor.YELLOW + ": Clear your inventory");
 			if (plugin.check(player, Commands.GETPOS))
 				player.sendMessage(ChatColor.RED + "/getpos" + ChatColor.YELLOW
 						+ ": Get your current coordinates");
 			if (plugin.check(player, Commands.SPAWN))
 				player.sendMessage(ChatColor.RED + "/spawn" + ChatColor.YELLOW
 						+ ": Teleport to the spawn");
 			if (plugin.check(player, Commands.SETSPAWN))
 				player.sendMessage(ChatColor.RED + "/setspawn"
 						+ ChatColor.YELLOW
 						+ ": Change the spawn to your current location");
 			if (plugin.check(player, Commands.TIME))
 				player.sendMessage(ChatColor.RED + "/time" + ChatColor.YELLOW
 						+ " [day|night]: Change server time to day or night");
 			if (plugin.check(player, Commands.KIT))
 				player.sendMessage(ChatColor.RED
 						+ "/kit"
 						+ ChatColor.YELLOW
 						+ " <name>: Spawn the items in a kit, or lists all kits");
 			player.sendMessage(ChatColor.RED + "/version <plugin>"
 					+ ChatColor.YELLOW
 					+ ": Information about bukkit or a plugin");
 			if (isAdmin(player))
 				player.sendMessage(ChatColor.RED + "/reload" + ChatColor.YELLOW
 						+ ": Reload server.properties");
 			break;
 
 		case 3:
 			if (isAdmin(player))
 				player.sendMessage(ChatColor.RED
 						+ "/#XXX"
 						+ ChatColor.YELLOW
 						+ ": Runs XXX as if from the console; /#help for more info");
 			if (isAdmin(player))
 				player.sendMessage(ChatColor.RED + "/?" + ChatColor.YELLOW
 						+ ": Alias for /#help");
 			if (plugin.check(player, Commands.HEAL))
 				player.sendMessage(ChatColor.RED + "/heal" + ChatColor.YELLOW
 						+ ": Restores your health");
 			if (plugin.check(player, Commands.MOTD))
 				player.sendMessage(ChatColor.RED + "/motd" + ChatColor.YELLOW
 						+ ": View the Message Of The Day");
 			if (plugin.check(player, Commands.SUPPORT))
 				player.sendMessage(ChatColor.RED + "/support" + ChatColor.YELLOW
 						+ " [message]: Send a message to all online staff members");
 			if (plugin.check(player, Commands.TOP))
 				player.sendMessage(ChatColor.RED + "/top" + ChatColor.YELLOW
 						+ ": Teleport to the highest block at your position");
 			if (plugin.check(player, Commands.MAIL))
 				player.sendMessage(ChatColor.RED
 						+ "/mail"
 						+ ChatColor.YELLOW
 						+ " ...: Read/send/clear mail; type /mail for more info");
 			player.sendMessage(ChatColor.RED + "-- Last Page --");
 			// player.sendMessage(ChatColor.RED + "/command" + ChatColor.YELLOW
 			// + ": reference");
 			break;
 		}
 	}
 
 	private Player getPlayer(String[] args, int pos)
 			throws IndexOutOfBoundsException, NoSuchFieldException {
 		if (args.length <= pos)
 			throw new IndexOutOfBoundsException(CraftEssence.premessage
 					+ "Invalid command syntax.");
 		List<Player> matches = srv.matchPlayer(args[0]);
 		if (matches.size() < 1)
 			throw new NoSuchFieldException(CraftEssence.premessage
 					+ "Player is offline or not found.");
 		return matches.get(0);
 	}
 
 	private String getDirection(double degrees) {
 		if (0 <= degrees && degrees < 22.5) {
 			return "N";
 		} else if (22.5 <= degrees && degrees < 67.5) {
 			return "NE";
 		} else if (67.5 <= degrees && degrees < 112.5) {
 			return "E";
 		} else if (112.5 <= degrees && degrees < 157.5) {
 			return "SE";
 		} else if (157.5 <= degrees && degrees < 202.5) {
 			return "S";
 		} else if (202.5 <= degrees && degrees < 247.5) {
 			return "SW";
 		} else if (247.5 <= degrees && degrees < 292.5) {
 			return "W";
 		} else if (292.5 <= degrees && degrees < 337.5) {
 			return "NW";
 		} else if (337.5 <= degrees && degrees < 360.0) {
 			return "N";
 		} else {
 			return "ERROR";
 		}
 	}
 
 	public static boolean isInteger(String input) {
 		try {
 			Integer.parseInt(input);
 			return true;
 		} catch (Exception ex) {
 		}
 		return false;
 	}
 
 	private void tp(Player player, String[] args)
 			throws IndexOutOfBoundsException, NoSuchFieldException {
 		player.teleportTo(getPlayer(args, 0));
 		Player tpme = getPlayer(args, 0);
 		player.sendMessage(CraftEssence.premessage + "Teleporting to "
 				+ tpme.getDisplayName() + ".");
 	}
 
 	private void tphere(Player player, String[] args)
 			throws IndexOutOfBoundsException, NoSuchFieldException {
 		getPlayer(args, 0).teleportTo(player);
 		Player tphp = getPlayer(args, 0);
 		player.sendMessage(CraftEssence.premessage + "Teleporting "
 				+ tphp.getDisplayName() + " to " + player.getName() + ".");
 	}
 
 	private void item(Player player, String[] sects, String[] args) {
 		if (args.length < 1) {
 			player.sendMessage(CraftEssence.premessage + "Usage: " + sects[0]
 					+ " [item] <amount>");
 			return;
 		}
 		int itemId = args[0].matches("[0-9]+") ? Integer.parseInt(args[0])
 				: ceItem.getItem(args[0]).id;
 		int itemAmount = args.length < 2 ? 64 : Integer.parseInt(args[1]);
 
 		int slot = player.getInventory().firstEmpty();
 		if (slot < 0) {
 			player.getWorld().dropItem(player.getLocation(),
 					new ItemStack(itemId, itemAmount));
 		} else {
 			player.getInventory().addItem(new ItemStack(itemId, itemAmount));
 		}
 		player.sendMessage(CraftEssence.premessage + "Giving " + itemAmount
 				+ " of item #" + itemId + ".");
 	}
 
 	private void give(Player player, String[] sects, String[] args)
 			throws IndexOutOfBoundsException, NoSuchFieldException {
 		if (args.length < 2) {
 			player.sendMessage(CraftEssence.premessage + "Usage: " + sects[0]
 					+ " [player] [item] <amount>");
 			return;
 		}
 		int itemId = args[1].matches("[0-9]+") ? Integer.parseInt(args[1])
 				: ceItem.getItem(args[1]).id;
 		int itemAmount = args.length < 3 ? 64 : Integer.parseInt(args[2]);
 		Player giveTo = getPlayer(args, 0);
 
 		int slot = giveTo.getInventory().firstEmpty();
 		if (slot < 0) {
 			giveTo.getWorld().dropItem(giveTo.getLocation(),
 					new ItemStack(itemId, itemAmount));
 		} else {
 			giveTo.getInventory().addItem(new ItemStack(itemId, itemAmount));
 		}
 		player.sendMessage(CraftEssence.premessage + "Giving " + itemAmount
 				+ " of item #" + itemId + " to " + giveTo.getDisplayName()
 				+ ".");
 	}
 
 	private void list(Player player, String[] sects, String[] args) {
 		StringBuilder online = new StringBuilder();
 		for (Player p : srv.getOnlinePlayers()) {
 			online.append(online.length() == 0 ? ChatColor.YELLOW
 					+ "Connected players: " + ChatColor.WHITE : ", ");
 			online.append(p.getDisplayName());
 		}
 		player.sendMessage(online.toString());
 	}
 
 	private void home(Player player, String[] sects, String[] args)
 			throws Exception {
 		player.teleportTo(plugin.getHome(player));
 		player.sendMessage(CraftEssence.premessage + "Teleporting home...");
 	}
 
 	private void getpos(Player player, String[] sects, String[] args) {
 		Location coords = player.getLocation();
 		player.sendMessage(CraftEssence.premessage + "Coordinates: "
 				+ ChatColor.WHITE + "(" + coords.getBlockX() + ", "
 				+ coords.getBlockY() + ", " + coords.getBlockZ() + ")");
 	}
 
 	private void top(Player player, String[] sects, String[] args) {
 		int topX = player.getLocation().getBlockX();
 		int topZ = player.getLocation().getBlockZ();
 		int topY = player.getWorld().getHighestBlockYAt(topX, topZ);
 		player.teleportTo(new Location(player.getWorld(), player.getLocation()
 				.getX(), topY + 1, player.getLocation().getZ()));
 		player.sendMessage(CraftEssence.premessage + "Teleported up.");
 	}
 
 	private void support(Player player, String[] sects, String[] args) {
 		if (args.length < 1) {
 			isAdmin(player);
 			player.sendMessage(CraftEssence.premessage
 					+ "To request help from the staff" + ChatColor.WHITE
 					+ ", Type " + sects[0].toLowerCase()
 					+ ", followed by your question.");
 			return;
 		}
 		for (Player p : srv.getOnlinePlayers()) {
 			if (!isAdmin(p))
 				continue;
 			p.sendMessage(ChatColor.RED + "[Support]" + ChatColor.GRAY
 					+ player.getDisplayName() + ":" + ChatColor.WHITE
 					+ sects[1]);
 		}
 	}
 
 	private boolean isAdmin(Player player) {
 		if (!Permissions.Security.permission(player, "*")) {
 			return false;
 		} else {
 			return true;
 		}
 	}
 
 	private void time(Player player, String[] sects, String[] args) {
		World world = player.getWorld();
		long time = world.getTime();
 		time = time - time % 24000;
 		if (args.length < 1) {
 			player.sendMessage(CraftEssence.premessage
 					+ "Usage: /time [day|night]");
 		} else if ("day".equalsIgnoreCase(args[0])) {
			world.setTime(time + 24000);
 			player.sendMessage(CraftEssence.premessage + "Time set to day.");
 		} else if ("night".equalsIgnoreCase(args[0])) {
			world.setTime(time + 37700);
 			player.sendMessage(CraftEssence.premessage + "Time set to night.");
 		} else {
 			player.sendMessage(CraftEssence.premessage
 					+ "/time only supports day/night.");
 		}
 	}
 
 	private void sethome(Player player, String[] sects, String[] args) {
 		plugin.setHome(player, player.getLocation());
 	}
 
 	private void mail(Player player, String[] sects, String[] args)
 			throws Exception {
 		// TODO mail
 		if (args.length == 1 && "read".equalsIgnoreCase(args[0])) {
 			List<String> mail = plugin.readMail(player);
 			if (mail.isEmpty())
 				player.sendMessage(CraftEssence.premessage
 						+ "You do not have any mail!");
 			else
 				for (String s : mail)
 					player.sendMessage(s);
 			return;
 		}
 		if (args.length >= 3 && "send".equalsIgnoreCase(args[0])) {
 			plugin.sendMail(player, args[1], sects[1].split(" +", 3)[2]);
 			return;
 		}
 		if (args.length >= 1 && "clear".equalsIgnoreCase(args[0])) {
 			plugin.clearMail(player);
 			return;
 		}
 		player.sendMessage(CraftEssence.premessage
 				+ "Usage: /mail [read|clear|send [to] [message]]");
 	}
 
 	private void clearinventory(Player player, String[] sects, String[] args) {
 		player.getInventory().clear();
 		player.sendMessage(CraftEssence.premessage + "Inventory cleared.");
 	}
 
 	private void spawn(Player player, String[] sects, String[] args) {
 		player.teleportTo(player.getWorld().getSpawnLocation());
 		player.sendMessage(CraftEssence.premessage + "Returned to spawn.");
 	}
 
 	private void setspawn(Player player, String[] sects, String[] args) {
 		WorldServer ws = ((CraftWorld) player.getWorld()).getHandle();
 		ws.spawnX = player.getLocation().getBlockX();
 		ws.spawnY = player.getLocation().getBlockY();
 		ws.spawnZ = player.getLocation().getBlockZ();
 		// ceProperties cep = new ceProperties(new File(file ,
 		// "craftessence.properties"));
 		// y = cep.getInt("y", player.getLocation().getBlockY(),
 		// "y axis for spawn");
 		// rotation = cep.getInt("rotation", (int)
 		// player.getLocation().getYaw(), "Rotation for spawn");
 		// cep.save();
 		player.sendMessage(CraftEssence.premessage + "Spawn position modified.");
 	}
 
 	private void kit(Player player, String[] sects, String[] args) {
 		// TODO kit command
 	}
 
 	private void heal(Player player, String[] sects, String[] args) {
 		if (args.length < 1) {
 			player.setHealth(20);
 			player.sendMessage(CraftEssence.premessage
 					+ "You are fully healed.");
 		} else {
 			int heal = Integer.parseInt(args[0]);
 			int hearts = heal / 2;
 			player.setHealth(heal);
 			player.sendMessage(ChatColor.RED + "[CraftEssence] "
 					+ ChatColor.YELLOW + "You now have " + hearts + " hearts");
 		}
 	}
 
 	private void motd(Player player, String[] sects, String[] args) {
 		String[] motd = plugin.getMotd();
 		if (motd == null || motd.length < 1) {
 			return;
 		}
 		String location = (int) player.getLocation().getX() + "x, "
 				+ (int) player.getLocation().getY() + "y, "
 				+ (int) player.getLocation().getZ() + "z";
 		String ip = player.getAddress().getAddress().getHostAddress();
 
 		for (String line : motd) {
 			player.sendMessage(plugin.argument(line, new String[] {
 					"+dname,+d", "+name,+n", "+location,+l", "+ip" },
 					new String[] { player.getDisplayName(), player.getName(),
 							location, ip }));
 		}
 	}
 
 	public enum Commands {
 		TP("tp"), TPHERE("tphere"), TPH("tphere"), ITEM("item"), I("item"), GIVE(
 				"give"), G("give"), LIST("list"), PLAYERLIST("list"), WHO(
 				"list"), ONLINE("list"), HOME("home"), SETHOME("sethome"), MAIL(
 				"mail"), SUPPORT("support"), CLEARINVENTORY("clearinventory"), GETPOS(
 				"getpos"), COORDS("getpos"), SPAWN("spawn"), SETSPAWN(
 				"setspawn"), TOP("top"), TIME("time"), KIT("kit"), HELP("help"), HEAL("heal"), MOTD("motd"), COMPASS(
 				"compass"), ME("me"), WARP("warp"), SETWARP("setwarp"), TELL(
 				"tell"), ALERT("alert");
 		public final String permNode;
 
 		private Commands(String permNode) {
 			this.permNode = "craftessence." + permNode;
 		}
 	}
 }
