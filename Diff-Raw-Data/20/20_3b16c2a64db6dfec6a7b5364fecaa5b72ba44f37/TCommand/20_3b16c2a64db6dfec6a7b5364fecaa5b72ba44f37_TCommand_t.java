 /*
  *  Copyright:
  *  2013 Darius Mewes
  */
 
 package de.timolia.custom.cmds;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import de.timolia.custom.TimoliaCustom;
 
 public abstract class TCommand implements CommandExecutor {
 
 	final protected static String prefix = TimoliaCustom.PREFIX;
 	final protected static String PERMISSION_PREFIX = "tcustom.";
 	protected static TimoliaCustom instance;
 
 	private String name;
 	private String permission = "";
 	private boolean ingame = false;
 	private int minArgs = 0;
 	private int maxArgs = -1;
 	protected String usage = "";
 
 	public static final void add(String commandName, TCommand tCmd) {
 		tCmd.name = commandName;
		tCmd.prepare();
 		instance.getCommand(commandName).setExecutor(tCmd);
 	}
 
 	public static final void setPluginInstance(TimoliaCustom instance) {
 		TCommand.instance = instance;
 	}
 
 	public final boolean onCommand(final CommandSender sender, final Command cmd, final String label, String[] args) {
		if (!permission.equalsIgnoreCase("") && !sender.hasPermission(this.permission)) {
			sender.sendMessage(prefix + "Du hast keine Berechtigung für diesen Befehl!");
 			return true;
 		}
 
 		if (ingame && !(sender instanceof Player)) {
			sender.sendMessage(prefix + "Dieser Befehl kann nur ingame eingegeben werden!");
 			return false;
 		}
 
 		if (cmd.getUsage() != null)
 			this.usage = new StringBuilder(prefix).append(cmd.getUsage()).toString();
 
 		if (args.length >= minArgs && (args.length <= maxArgs || maxArgs == -1))
 			perform(sender, args);
 
 		else if (usage.equalsIgnoreCase(""))
			sender.sendMessage(prefix + "Falsche Argumenten-Zahl");
 
 		else
 			sender.sendMessage(usage);
 
 		return true;
 	}
 
 	protected abstract void prepare();
 
 	public abstract void perform(final CommandSender sender, String[] args);
 
 	protected final void permission() {
 		this.permission(this.name);
 	}
 
 	protected final void permission(String permission) {
		this.permission = PERMISSION_PREFIX + permission;
 	}
 
 	protected final void ingame() {
 		this.ingame = true;
 	}
 
 	protected final void minArgs(int minArgs) {
 		this.minArgs = minArgs;
 	}
 
 	protected final void maxArgs(int maxArgs) {
 		this.maxArgs = maxArgs;
 	}
 
 }
