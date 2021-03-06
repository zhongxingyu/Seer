 package me.desht.scrollingmenusign.parser;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Scanner;
 import java.util.Set;
 import java.util.logging.Level;
 
 import me.desht.scrollingmenusign.SMSConfig;
 import me.desht.scrollingmenusign.SMSException;
 import me.desht.scrollingmenusign.SMSMacro;
 import me.desht.scrollingmenusign.ScrollingMenuSign;
 import me.desht.scrollingmenusign.enums.ReturnStatus;
 import me.desht.util.Debugger;
 import me.desht.util.MiscUtil;
 import me.desht.util.PermissionsUtils;
 
 import org.bukkit.Bukkit;
 import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.permissions.PermissionAttachment;
 
 public class CommandParser {
 	private Set<String> macroHistory;
 
 	public CommandParser() {
 		this.macroHistory = new HashSet<String>();
 	}
 
 	private enum RunMode { CHECK_PERMS, EXECUTE };
 
 	boolean runSimpleCommandString(Player player, String command) {
 		player.chat(command);
 		return true;
 	}
 
 	/**
 	 * Parse and run a command string via the SMS command engine
 	 * 
 	 * @param player	Player who is running the command
 	 * @param command	Command to be run
 	 * @return			A return status indicating the outcome of the command
 	 * @throws SMSException	
 	 */
	public ReturnStatus runCommandString(Player player, String command) throws SMSException {
 		ParsedCommand cmd = handleCommandString(player, command, RunMode.EXECUTE);
 		
 		if (cmd == null) {
 			return ReturnStatus.CMD_OK;
 		}
 		
 		if (!cmd.isAffordable())
 			cmd.setStatus(ReturnStatus.CANT_AFFORD);
 
 		return cmd.getStatus();
 	}
 
 	public boolean verifyCreationPerms(Player player, String command) throws SMSException {
 		ParsedCommand cmd = handleCommandString(player, command, RunMode.CHECK_PERMS);
 		return cmd.getStatus() == ReturnStatus.CMD_OK;
 	}
 
 	ParsedCommand handleCommandString(Player player, String command, RunMode mode) throws SMSException {

 		// do some preprocessing ...
 		ItemStack stack =  player.getItemInHand();
 		command = command.replace("<X>", "" + player.getLocation().getBlockX());
 		command = command.replace("<Y>", "" + player.getLocation().getBlockY());
 		command = command.replace("<Z>", "" + player.getLocation().getBlockZ());
 		command = command.replace("<NAME>", player.getName());
 		command = command.replace("<N>", player.getName());
 		command = command.replace("<WORLD>", player.getWorld().getName());
 		command = command.replace("<I>", stack != null ? "" + stack.getTypeId() : "0");
 		command = command.replace("<INAME>", stack != null ? "" + stack.getType().toString() : "???");
 
 		Scanner scanner = new Scanner(command);
 
 		ParsedCommand cmd = null;
 		while (scanner.hasNext()) {
 			cmd = new ParsedCommand(player, scanner);
 
 			switch (mode) {
 			case EXECUTE:
 				if (cmd.isRestricted() || !cmd.isAffordable()) {
 					// bypassing any potential cmd.isCommandStopped() or cmd.isMacroStopped()
 					continue;
 				}
 				execute(player, cmd);
 				break;
 			case CHECK_PERMS:
 				cmd.setStatus(ReturnStatus.CMD_OK);
 				if ((cmd.isElevated() || cmd.isConsole()) && !PermissionsUtils.isAllowedTo(player, "scrollingmenusign.create.elevated")) {
 					cmd.setStatus(ReturnStatus.NO_PERMS);
 					return cmd;
 				} else if (!cmd.getCosts().isEmpty() && !PermissionsUtils.isAllowedTo(player, "scrollingmenusign.create.cost")) {
 					cmd.setStatus(ReturnStatus.NO_PERMS);
 					return cmd;
 				}
 				break;
 			default:
 				throw new IllegalArgumentException("unexpected run mode for parseCommandString()");
 			}
 			
 			if (cmd.isCommandStopped() || cmd.isMacroStopped()) {
 				break;
 			}
 		}
 
 		return cmd;
 	}
 
 	private void execute(Player player, ParsedCommand cmd) throws SMSException {
 		if (cmd.isRestricted() || !cmd.isAffordable()) 
 			return;
 
 		Cost.chargePlayer(player, cmd.getCosts());
 
 		if (cmd.getCommand() == null || cmd.getCommand().isEmpty())
 			return;
 
 		StringBuilder sb = new StringBuilder(cmd.getCommand()).append(" ");
 		for (String a : cmd.getArgs()) {
 			sb.append(a).append(" ");
 		}
 		String command = sb.toString().trim();
 
 		if (cmd.isMacro()) {
 			// run a macro
 			String macroName = cmd.getCommand();
 			if (macroHistory.contains(macroName)) {
 				MiscUtil.log(Level.WARNING, "Recursion detected and stopped in macro " + macroName);
 				cmd.setStatus(ReturnStatus.WOULD_RECURSE);
 				return;
 			} else if (SMSMacro.hasMacro(macroName)) {
 				macroHistory.add(macroName);
 				for (String c : SMSMacro.getCommands(macroName)) {
 					for (int i = 0; i < cmd.getArgs().size(); i++) {
 						c = c.replace("<" + (i + 1) + ">", cmd.arg(i));
 					}
					ParsedCommand cmd2 = handleCommandString(player, c, RunMode.EXECUTE);
 					if (cmd2.isMacroStopped())
 						break;
 				}
 				return;
 			} else {
 				cmd.setStatus(ReturnStatus.BAD_MACRO);
 				return;
 			}
 		} else if (cmd.isWhisper()) {
 			// private message to the player
 			MiscUtil.alertMessage(player, command);
 		} else if (cmd.isConsole()) {
 			// run this as a console command
 			// only works for commands that may be run via the console, but should always work
 			if (!PermissionsUtils.isAllowedTo(player, "scrollingmenusign.execute.elevated")) {
 				cmd.setStatus(ReturnStatus.NO_PERMS);
 				return;
 			}
 			Debugger.getDebugger().debug("execute (console): " + sb.toString());
 			
 			ConsoleCommandSender cs = Bukkit.getServer().getConsoleSender();
 			if (!Bukkit.getServer().dispatchCommand(cs, sb.toString())) {
 				cmd.setStatus(ReturnStatus.CMD_FAILED);
 			}
 		} else if (cmd.isElevated()) {
 			// this is a /@ command, to be run as the real player, but with temporary permissions
 			// (this now also handles the /* fake-player style, which is no longer directly supported)
 
 			if (!PermissionsUtils.isAllowedTo(player, "scrollingmenusign.execute.elevated")) {
 				cmd.setStatus(ReturnStatus.NO_PERMS);
 				return;
 			}
 
 			Debugger.getDebugger().debug("execute (elevated): " + sb.toString());
 
 			List<PermissionAttachment> attachments = new ArrayList<PermissionAttachment>();
 			boolean tempOp = false;
 			try {
 				ScrollingMenuSign plugin = ScrollingMenuSign.getInstance();
 				@SuppressWarnings("unchecked")
 				List<String> nodes = (List<String>) SMSConfig.getConfig().getList("sms.elevation.nodes");
 				for (String node : nodes) {
 					if (!node.isEmpty() && !player.hasPermission(node)) {
 //						System.out.println("add node: " + node);
 						attachments.add(player.addAttachment(plugin, node, true));
 					}
 				}
 				if (SMSConfig.getConfig().getBoolean("sms.elevation.grant_op", false) && !player.isOp()) {
 					tempOp = true;
 					player.setOp(true);
 				}
 				if (command.startsWith("/")) {
 					if (!Bukkit.getServer().dispatchCommand(player, command.substring(1))) {
 						cmd.setStatus(ReturnStatus.CMD_FAILED);
 					}
 				} else {
 					player.chat(command);
 				}
 			} finally {
 				// revoke all temporary permissions granted to the user
 				for (PermissionAttachment att : attachments) {
 //					for (Entry<String,Boolean> e : att.getPermissions().entrySet()) {
 //						System.out.println("remove attachment: " + e.getKey() + " = " + e.getValue());
 //					}
 					player.removeAttachment(att);
 				}
 				if (tempOp) {
 					player.setOp(false);
 				}
 			}
 		} else {
 			// just an ordinary command, no special privilege elevation
 			Debugger.getDebugger().debug("execute (normal): " + sb.toString());
 			if (command.startsWith("/")) {
 				if (!Bukkit.getServer().dispatchCommand(player, command.substring(1))) {
 					cmd.setStatus(ReturnStatus.CMD_FAILED);
 				}
 			} else {
 				player.chat(command);
 			}
 		}
 	}
 
 }
