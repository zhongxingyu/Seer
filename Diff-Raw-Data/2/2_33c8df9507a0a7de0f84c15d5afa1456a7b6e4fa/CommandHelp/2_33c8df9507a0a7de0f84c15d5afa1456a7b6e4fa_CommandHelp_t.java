 package qs.swornshop;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 /**
  * CommandHelp provides an interface for specifying and printing help for a Bukkit command. It also contains general help-printing utilities.
  */
 public class CommandHelp {
 	/**
 	 * Creates a new CommandHelp for a single command with no arguments
 	 * @param command the command's name
 	 * @param description a short description of the command
 	 * @param help additional help for this command
 	 */
 	public CommandHelp(String command, String description, String[] help) {
 		this(command, null, null, description, help);
 	}
 	/**
 	 * Creates a new CommandHelp for a single command
 	 * @param command the command's name
 	 * @param args the command's arguments
 	 * @param description a short description of the command
 	 * @param help additional help for this command
 	 */
 	public CommandHelp(String command, String args, String description, String[] help) {
 		this(command, null, args, description, help);
 	}
 	/**
 	 * Creates a new CommandHelp for a single command with an alias
 	 * @param command the command's name
 	 * @param alias an alias for the command
 	 * @param args the command's arguments
 	 * @param description a short description of the command
 	 * @param help additional help for this command
 	 */
 	public CommandHelp(String command, String alias, String args, String description, String... help) {
 		this.command = command;
 		this.args = args;
 		this.description = description;
 		this.help = Arrays.asList(help);
 		this.alias = alias;
 	}
 	/**
 	 * The command's name
 	 */
 	public String command;
 	/**
 	 * A comma-separated list of command aliases
 	 */
 	public String alias;
 	/**
 	 * A list of arguments for the command in this format:
 	 * <code>&lt;arg-1&gt; &lt;arg-2&gt; ... [optional-1] [optional-2] ...</code>
 	 */
 	public String args;
 	/**
 	 * A short (one-line) description of the command
 	 */
 	public String description;
 	/**
 	 * Additional help for the command
 	 */
 	public List<String> help;
 	
 	// for memoizing to*String() functions
 	private String indexString = null, usageString = null;
 	private String[] helpString = null;
 	
 	/**
 	 * Generates a single line string suitable for use in an index topic.
 	 * This method is memoized so the string is only created once.
 	 * @return the string
 	 */
 	public String toIndexString() {
 		if (indexString != null) 
 			return indexString;
 		StringBuilder b = new StringBuilder().
 			append("§B/").
 			append(command).
 			append("§3");
 		if (alias != null) {
 			b.append("(§B").
 				append(alias).
 				append("§3)");
 		}
 		if (args != null) {
 			b.append(' ');
 			b.append(args);
 		}
 		b.append(" §7-§F ").
 			append(description);
 		return indexString = b.toString();
 	}
 	
 	/**
 	 * Generates a single line string suitable for indicating the correct usage of this command.
 	 * This method is memoized so the string is only created once
 	 * @return the usage string
 	 */
 	public String toUsageString() {
 		if (usageString != null) 
 			return usageString;
 		StringBuilder b = new StringBuilder().
 			append("Usage: /").
 			append(command);
 		if (args != null) {
 			b.append(' ');
 			b.append(args);
 		}
 		return usageString = b.toString();
 	}
 	
 	/**
 	 * Generates a full help string suitable for use in a help page for this command.
 	 * This method is memoized so the string is only created once.
 	 * @return an array of lines
 	 */
 	public String[] toHelpString() {
 		if (helpString != null) 
 			return helpString;
 		ArrayList<String> h = new ArrayList<String>();
 		h.add(header("Help: /" + command));
 		h.add("§F" + description);
 		StringBuilder b = new StringBuilder().
 			append("§BUsage: §F").
 			append('/').
 			append(command);
 		if (args != null) {
 			b.append(' ').
 				append(args);
 		}
 		h.add(b.toString());
 		if (alias != null)
			h.add("§BAliases: §F" + alias);
 		if (help != null) {
 			h.add("");
 			h.addAll(help);
 		}
 		return helpString = h.toArray(new String[h.size()]);
 	}
 	
 	/**
 	 * Generates a chat header with the given title
 	 * @param title the text in the header
 	 * @return the chat header
 	 */
 	public static String header(String title) {
 		return "§7------------§B " + title + " §7------------";
 	}
 	
 	/**
 	 * Generates an arguments 'help' description string
 	 * @param name the name of the argument
 	 * @param description the argument's description
 	 * @return the string
 	 */
 	public static String arg(String name, String description) {
 		return "§B" + name + " §7-§F " + description;
 	}
 	
 	/**
 	 * Generates the 'help' description string for each of a list of arguments, separated by newlines
 	 * @param args... a list of (name, description) pairs
 	 * @return the string
 	 */
 	public static String[] args(String... args) {
 		String[] s = new String[args.length / 2];
 		int i = 0, 
 			len = args.length - 1;
 		while (i < len) {
 			s[i / 2] = arg(args[i++], args[i++]);
 		}
 		return s;
 	}
 }
