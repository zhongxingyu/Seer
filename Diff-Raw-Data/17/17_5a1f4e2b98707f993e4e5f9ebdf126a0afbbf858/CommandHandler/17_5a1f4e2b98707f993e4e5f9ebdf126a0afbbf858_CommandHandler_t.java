 package de.dariusmewes.TimoliaCustom.commands;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import de.dariusmewes.TimoliaCustom.Message;
 import de.dariusmewes.TimoliaCustom.TimoliaCustom;
 
 public class CommandHandler {
 
 	private static List<TCommand> commands = new ArrayList<TCommand>();
 
 	public static void init(TimoliaCustom instance) {
 		add(new checkent("checkent"));
 		add(new google("google"));
 		add(new itp("itp"));
 		add(new protnpc("protnpc"));
 		add(new sapopvp("sapopvp"));
 		add(new sgames("sgames"));
 		add(new sgcopy("sgcopy"));
 		add(new tcustom("tcustom"));
 		add(new west("west"));
 
 		TCommand.setPluginInstance(instance);
 	}
 
 	public static void handleCommand(CommandSender sender, Command cmd, String args[]) {
 		TCommand c = getCommand(cmd);
 		if (!isvalidCommand(sender, c, args))
 			return;
 
 		c.perform(sender, args);
 	}
 
 	public static boolean isvalidCommand(CommandSender sender, TCommand cmd, String args[]) {
 		if (cmd.onlyIngame() && !(sender instanceof Player)) {
 			sender.sendMessage(Message.INGAME);
 			return false;
 		}
 
 		if (!cmd.getPermission().equalsIgnoreCase("") && !sender.hasPermission(cmd.getPermission())) {
 			sender.sendMessage(Message.NOPERM);
 			return false;
 		}
 
 		if (args.length >= cmd.getMinArgs() && (args.length <= cmd.getMaxArgs() || cmd.getMaxArgs() == -1))
 			return true;
 
 		else
			sender.sendMessage(TimoliaCustom.PREFIX + cmd.getUsage());
 		return false;
 	}
 
 	public static void add(TCommand command) {
 		commands.add(command);
 	}
 
 	public static void list() {
 		try {
 			File file = new File(System.getProperty("user.home") + File.separator + "commandscustom.txt");
 			BufferedWriter output = new BufferedWriter(new FileWriter(file));
 			for (TCommand cmd : commands) {
 				output.append("    " + cmd.getName() + ":");
 				output.newLine();
 				output.append("        usage: " + cmd.getCleanUsage());
 				output.newLine();
 				output.append("        description: ");
 				output.newLine();
 			}
 
 			output.close();
 			Message.console("DONE");
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static TCommand getCommand(Command cmd) {
 		for (TCommand c : commands) {
 			if (cmd.getName().equalsIgnoreCase(c.getName()))
 				return c;
 		}
 
 		return null;
 	}
 
 }
