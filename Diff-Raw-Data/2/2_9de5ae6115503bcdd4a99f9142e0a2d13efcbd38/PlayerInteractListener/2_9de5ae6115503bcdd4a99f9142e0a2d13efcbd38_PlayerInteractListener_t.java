 package no.HON95.ButtonCommands;
 
 import java.util.Set;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Effect;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.material.Button;
 import org.bukkit.material.Lever;
 
 
 final class PlayerInteractListener implements Listener {
 
 	private final BCMain PLUGIN;
 	Set<String> whiteList = null;
 	boolean enableNormal = false;
 	boolean enableConsole = false;
 	boolean enableAlias = false;
 	boolean enableBL = false;
 	boolean ignoreWhiteLists = false;
 	boolean curPerm = false;
 	boolean curNoPerm = false;
 	boolean outputInfo = false;
 	boolean rightClick = false;
 	boolean leftClick = false;
 
 	private final ChatColor GY = ChatColor.GRAY;
 	private final ChatColor GO = ChatColor.GOLD;
 	private final ChatColor BL = ChatColor.BLUE;
 	private final ChatColor AQ = ChatColor.AQUA;
 	private final ChatColor RE = ChatColor.RED;
 
 	/*
 	 * Code: 0=Fail; 1=No current; 2=Current;
 	 */
 
 	PlayerInteractListener(BCMain instance) {
 		PLUGIN = instance;
 	}
 
 	@EventHandler(priority = EventPriority.LOW)
 	public void onPlayerInteract(PlayerInteractEvent ev) {
 
 		if (ev.isCancelled())
 			return;
 
 		if (!((ev.getAction().equals(Action.LEFT_CLICK_BLOCK) && leftClick)
 		|| (ev.getAction().equals(Action.RIGHT_CLICK_BLOCK) && rightClick)))
 			return;
 
 		if (!(enableNormal || enableConsole || enableAlias))
 			return;
 
 		Block block = ev.getClickedBlock();
 		Player player = ev.getPlayer();
 
 		if (block == null)
 			return;
 		if (!block.getType().toString().toLowerCase().contains("button"))
 			return;
 
 		BlockFace bf = ((Button) block.getState().getData()).getFacing();
 		if (bf == BlockFace.NORTH) {
 			BlockFace[] testBlocks = { BlockFace.UP, BlockFace.DOWN, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST };
 			BlockFace[] testFaces = { BlockFace.NORTH, BlockFace.SOUTH };
 			Block lever = block.getRelative(BlockFace.SOUTH, 3);
 			ev.setCancelled(signFinder(player, block, testBlocks, testFaces, lever));
 		}
 		else if (bf == BlockFace.SOUTH) {
 			BlockFace[] testBlocks = { BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.WEST, BlockFace.EAST };
 			BlockFace[] testFaces = { BlockFace.SOUTH, BlockFace.NORTH };
 			Block lever = block.getRelative(BlockFace.NORTH, 3);
 			ev.setCancelled(signFinder(player, block, testBlocks, testFaces, lever));
 		}
 		else if (bf == BlockFace.EAST) {
 			BlockFace[] testBlocks = { BlockFace.UP, BlockFace.DOWN, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH };
 			BlockFace[] testFaces = { BlockFace.EAST, BlockFace.WEST };
 			Block lever = block.getRelative(BlockFace.WEST, 3);
 			ev.setCancelled(signFinder(player, block, testBlocks, testFaces, lever));
 		}
 		else if (bf == BlockFace.WEST) {
 			BlockFace[] testBlocks = { BlockFace.UP, BlockFace.DOWN, BlockFace.EAST, BlockFace.SOUTH, BlockFace.NORTH };
 			BlockFace[] testFaces = { BlockFace.WEST, BlockFace.EAST };
 			Block lever = block.getRelative(BlockFace.EAST, 3);
 			ev.setCancelled(signFinder(player, block, testBlocks, testFaces, lever));
 		}
 	}
 
 	private boolean signFinder(Player player, Block block, BlockFace[] testBlocks, BlockFace[] testFaces, final Block lever) {
 
 		Block newBlock;
 		BlockFace testFace;
 
 		for (int c = 0; c < 5; c++) {
 			if (c == 2) {
 				newBlock = block.getRelative(testBlocks[c], 2);
 				testFace = testFaces[1];
 			}
 			else {
 				newBlock = block.getRelative(testBlocks[c]);
 				testFace = testFaces[0];
 			}
 			if (!(newBlock.getState() instanceof Sign))
 				continue;
 			if (((org.bukkit.material.Sign) newBlock.getState().getData()).getFacing() != testFace)
 				continue;
 
 			int s = signConverter(player, newBlock);
 			if (s > 0) {
 				boolean n = s == 1;
 
 				if (n || !enableBL)
 					return n;
 				if (lever.getType() != Material.LEVER)
 					return n;
 				Lever l = (Lever) lever.getState().getData();
 				if (l.isPowered())
 					return n;
 				l.setPowered(true);
 				lever.setData(l.getData());
 				lever.getWorld().playEffect(lever.getLocation(), Effect.SMOKE, 4);
 				Bukkit.getScheduler().scheduleSyncDelayedTask(PLUGIN, new Runnable() {
 					@Override
 					public void run() {
 						if (lever.getType() != Material.LEVER)
 							return;
 						Lever l = (Lever) lever.getState().getData();
 						if (!l.isPowered())
 							return;
 						l.setPowered(false);
 						lever.setData(l.getData());
 						lever.getWorld().playEffect(lever.getLocation(), Effect.SMOKE, 4);
 					}
 				}, 20L);
 			}
 		}
 		return false;
 	}
 
 	private int signConverter(Player player, Block block) {
 
 		Sign sign = (Sign) block.getState();
 		String[] lines = sign.getLines();
 
 		if (!lines[1].startsWith("/"))
 			return 0;
 		lines[1] = lines[1].replaceFirst("/", "");
 
 		String cmd[] = Misc.concatCmd(lines);
 		cmd[1] = Misc.insertAll(cmd[1], player, block);
 
 		return executor(player, block, cmd, false, false, false);
 	}
 
 	private int executor(Player player, Block block, String cmd[], boolean ignorePerms, boolean ignoreWhiteList, boolean alias) {
 
 		if (cmd[1].equalsIgnoreCase("redstone") || cmd[1].equalsIgnoreCase("r")
 				|| cmd[1].equalsIgnoreCase("console") || cmd[1].equalsIgnoreCase("c")
 				|| cmd[1].equalsIgnoreCase("alias") || cmd[1].equalsIgnoreCase("a")) {
 			player.sendMessage(RE + "Invalid command sign! Need to contain a command.");
 			return 0;
 		}
 		else if ((cmd[0].equalsIgnoreCase("redstone") || cmd[0].equalsIgnoreCase("r")) && !alias) {
 			return 0;
 		}
 		else if (cmd[0].equalsIgnoreCase("console")) {
 			return consoleExecutor(player, cmd[1].replaceFirst("console ", ""), ignorePerms, ignoreWhiteList);
 		}
 		else if (cmd[0].equalsIgnoreCase("c")) {
 			return consoleExecutor(player, cmd[1].replaceFirst("c ", ""), ignorePerms, ignoreWhiteList);
 		}
 		else if (cmd[0].equalsIgnoreCase("alias") && !alias) {
 			return aliasExecutor(player, block, cmd[1].replaceFirst("alias ", ""));
 		}
 		else if (cmd[0].equalsIgnoreCase("a") && !alias) {
 			return aliasExecutor(player, block, cmd[1].replaceFirst("a ", ""));
 		}
 		else if (enableNormal) {
 			if (ignorePerms || player.hasPermission("buttoncommands.use.normal")) {
 				player.chat("/" + cmd[1]);
 				return (curPerm ? 2 : 1);
 			}
 			else {
 				player.sendMessage(RE + "You are not allowed to use command signs!");
 				return (curNoPerm ? 2 : 1);
 			}
 		}
 
 		return 0;
 	}
 
 	private int consoleExecutor(Player player, String command, boolean ignorePerms, boolean ignoreWhiteList) {
 
 		if (!enableConsole)
 			return 0;
 
 		if (!(ignorePerms || player.hasPermission("buttoncommands.use.console"))) {
 			player.sendMessage(RE + "You are not allowed to use console command signs!");
 			return (curNoPerm ? 2 : 1);
 		}
		if (ignoreWhiteLists || whiteList.contains(command.toLowerCase())) {
 			if (outputInfo)
 				PLUGIN.getLogger().info(player.getName() + " executing console command: " + command);
 			player.sendMessage(GY + "Executing console command: " + command);
 			PLUGIN.getServer().dispatchCommand(PLUGIN.getServer().getConsoleSender(), command);
 			return (curPerm ? 2 : 1);
 		}
 		else {
 			player.sendMessage(RE + "Console command is not white-listed!");
 			return 1;
 		}
 	}
 
 	private int aliasExecutor(Player player, Block block, String cmd) {
 
 		if (!enableAlias) {
 			return 0;
 		}
 
 		if (!player.hasPermission("buttoncommands.use.alias")) {
 			player.sendMessage(RE + "You are not allowed to use alias command signs!");
 			return (curNoPerm ? 2 : 1);
 		}
 
 		String alias;
 		String args;
 		if (cmd.contains(" ")) {
 			String[] tmp = cmd.split(" ", 2);
 			alias = tmp[0].toLowerCase();
 			args = tmp[1];
 		}
 		else {
 			alias = cmd.toLowerCase();
 			args = "";
 		}
 
 		if (!Alias.BCA.containsKey(alias)) {
 			player.sendMessage(GO + "Alias " + RE + alias + GO + " does not exist.");
 			return 1;
 		}
 
 		Alias a = Alias.BCA.get(alias);
 
 		if (!a.ENAB) {
 			player.sendMessage(GO + "Alias " + RE + a.NAME + GO + " is disabled.");
 			return 1;
 		}
 		if (!(a.PERM.length() == 0 || player.hasPermission(a.PERM))) {
 			player.sendMessage(GO + "Permission denied for alias" + RE + a.NAME + GO + "!");
 			return (a.C_MP ? 2 : 1);
 		}
 		if (a.LOG.containsKey(player.getName())) {
 			int d = (int) (System.currentTimeMillis() / 1000 - a.LOG.get(player.getName()) / 1000);
 			if (d < a.COOL) {
 				player.sendMessage(AQ + "Alias is cooling down. Please wait for " + BL + (a.COOL - d) + AQ + " more seconds.");
 				return 1;
 			}
 		}
 
 		for (String s : a.CMDS) {
 			s = s.trim();
 			executor(player, block, new String[] { s.split(" ", 2)[0].toLowerCase(), Misc.insertAll(s, player, block) + " " + args }, a.IGPM, a.IGWL, true);
 		}
 		a.LOG.put(player.getName(), System.currentTimeMillis());
 
 		return (a.C_HP ? 2 : 1);
 	}
 }
