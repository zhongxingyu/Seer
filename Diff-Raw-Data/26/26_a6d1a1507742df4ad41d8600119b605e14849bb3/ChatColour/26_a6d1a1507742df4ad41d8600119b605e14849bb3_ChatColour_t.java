 package no.runsafe.framework.output;
 
 import org.bukkit.ChatColor;
 
 import java.util.regex.Pattern;
 
 public enum ChatColour
 {
 	BLACK('0', ChatColor.BLACK, ConsoleColors.BLACK),
 	DARK_BLUE('1', ChatColor.DARK_BLUE, ConsoleColors.DARK_BLUE),
 	DARK_GREEN('2', ChatColor.DARK_GREEN, ConsoleColors.DARK_GREEN),
 	DARK_AQUA('3', ChatColor.DARK_AQUA, ConsoleColors.DARK_AQUA),
 	DARK_RED('4', ChatColor.DARK_RED, ConsoleColors.DARK_RED),
 	DARK_PURPLE('5', ChatColor.DARK_PURPLE, ConsoleColors.DARK_PURPLE),
 	GOLD('6', ChatColor.GOLD, ConsoleColors.GOLD),
 	GRAY('7', ChatColor.GRAY, ConsoleColors.GRAY),
 	DARK_GRAY('8', ChatColor.DARK_GRAY, ConsoleColors.DARK_GRAY),
 	BLUE('9', ChatColor.BLUE, ConsoleColors.BLUE),
 	GREEN('a', ChatColor.GREEN, ConsoleColors.GREEN),
 	AQUA('b', ChatColor.AQUA, ConsoleColors.AQUA),
 	RED('c', ChatColor.RED, ConsoleColors.RED),
 	LIGHT_PURPLE('d', ChatColor.LIGHT_PURPLE, ConsoleColors.LIGHT_PURPLE),
 	YELLOW('e', ChatColor.YELLOW, ConsoleColors.YELLOW),
 	WHITE('f', ChatColor.WHITE, ConsoleColors.WHITE),
 	MAGIC('k', ChatColor.MAGIC, ConsoleColors.MAGIC),
 	BOLD('l', ChatColor.BOLD, ConsoleColors.BOLD),
 	STRIKETHROUGH('m', ChatColor.STRIKETHROUGH, ConsoleColors.STRIKETHROUGH),
 	UNDERLINE('n', ChatColor.UNDERLINE, ConsoleColors.UNDERLINE),
 	ITALIC('o', ChatColor.ITALIC, ConsoleColors.ITALIC),
 	RESET('r', ChatColor.RESET, ConsoleColors.RESET);
 
 	ChatColour(char colourCode, ChatColor bukkitColour, String ansiCode)
 	{
 		code = String.format("&%s", colourCode);
 		bukkitCode = bukkitColour.toString();
 		consoleCode = ansiCode;
 	}
 
 	@Override
 	public String toString()
 	{
 		return code;
 	}
 
 	public String toConsole()
 	{
 		return consoleCode;
 	}
 
 	public String toBukkit()
 	{
 		return bukkitCode;
 	}
 
 	public static String ToMinecraft(String message)
 	{
 		message = CODE_DARK_GREEN.matcher(message).replaceAll(DARK_GREEN.toBukkit());
 		message = CODE_DARK_BLUE.matcher(message).replaceAll(DARK_BLUE.toBukkit());
 		message = CODE_BLACK.matcher(message).replaceAll(BLACK.toBukkit());
 		message = CODE_DARK_AQUA.matcher(message).replaceAll(DARK_AQUA.toBukkit());
 		message = CODE_DARK_RED.matcher(message).replaceAll(DARK_RED.toBukkit());
 		message = CODE_DARK_PURPLE.matcher(message).replaceAll(DARK_PURPLE.toBukkit());
 		message = CODE_GOLD.matcher(message).replaceAll(GOLD.toBukkit());
 		message = CODE_GRAY.matcher(message).replaceAll(GRAY.toBukkit());
 		message = CODE_DARK_GRAY.matcher(message).replaceAll(DARK_GRAY.toBukkit());
 		message = CODE_BLUE.matcher(message).replaceAll(BLUE.toBukkit());
 		message = CODE_GREEN.matcher(message).replaceAll(GREEN.toBukkit());
 		message = CODE_AQUA.matcher(message).replaceAll(AQUA.toBukkit());
 		message = CODE_RED.matcher(message).replaceAll(RED.toBukkit());
 		message = CODE_LIGHT_PURPLE.matcher(message).replaceAll(LIGHT_PURPLE.toBukkit());
 		message = CODE_YELLOW.matcher(message).replaceAll(YELLOW.toBukkit());
 		message = CODE_WHITE.matcher(message).replaceAll(WHITE.toBukkit());
 		message = CODE_MAGIC.matcher(message).replaceAll(MAGIC.toBukkit());
 		message = CODE_BOLD.matcher(message).replaceAll(BOLD.toBukkit());
 		message = CODE_STRIKETHROUGH.matcher(message).replaceAll(STRIKETHROUGH.toBukkit());
 		message = CODE_UNDERLINE.matcher(message).replaceAll(UNDERLINE.toBukkit());
 		message = CODE_ITALIC.matcher(message).replaceAll(ITALIC.toBukkit());
 		message = CODE_RESET.matcher(message).replaceAll(RESET.toBukkit());
 		return message;
 	}
 
 	public static String ToConsole(String message)
 	{
 		message = CODE_DARK_GREEN.matcher(message).replaceAll(DARK_GREEN.toConsole());
 		message = CODE_DARK_BLUE.matcher(message).replaceAll(DARK_BLUE.toConsole());
 		message = CODE_BLACK.matcher(message).replaceAll(BLACK.toConsole());
 		message = CODE_DARK_AQUA.matcher(message).replaceAll(DARK_AQUA.toConsole());
 		message = CODE_DARK_RED.matcher(message).replaceAll(DARK_RED.toConsole());
 		message = CODE_DARK_PURPLE.matcher(message).replaceAll(DARK_PURPLE.toConsole());
 		message = CODE_GOLD.matcher(message).replaceAll(GOLD.toConsole());
 		message = CODE_GRAY.matcher(message).replaceAll(GRAY.toConsole());
 		message = CODE_DARK_GRAY.matcher(message).replaceAll(DARK_GRAY.toConsole());
 		message = CODE_BLUE.matcher(message).replaceAll(BLUE.toConsole());
 		message = CODE_GREEN.matcher(message).replaceAll(GREEN.toConsole());
 		message = CODE_AQUA.matcher(message).replaceAll(AQUA.toConsole());
 		message = CODE_RED.matcher(message).replaceAll(RED.toConsole());
 		message = CODE_LIGHT_PURPLE.matcher(message).replaceAll(LIGHT_PURPLE.toConsole());
 		message = CODE_YELLOW.matcher(message).replaceAll(YELLOW.toConsole());
 		message = CODE_WHITE.matcher(message).replaceAll(WHITE.toConsole());
 		message = CODE_MAGIC.matcher(message).replaceAll(MAGIC.toConsole());
 		message = CODE_BOLD.matcher(message).replaceAll(BOLD.toConsole());
 		message = CODE_STRIKETHROUGH.matcher(message).replaceAll(STRIKETHROUGH.toConsole());
 		message = CODE_UNDERLINE.matcher(message).replaceAll(UNDERLINE.toConsole());
 		message = CODE_ITALIC.matcher(message).replaceAll(ITALIC.toConsole());
 		message = CODE_RESET.matcher(message).replaceAll(RESET.toConsole());
 		return message + RESET.toConsole();
 	}
 
 	public static String Strip(String message)
 	{
		while(CODE_ANY.matcher(message).matches())
			message = CODE_ANY.matcher(message).replaceAll("");
 		return message;
 	}
 
 	private final String code;
 	private final String bukkitCode;
 	private final String consoleCode;
 
	private static final Pattern CODE_ANY = Pattern.compile("&[0-9a-fk-r]");
 	private static final Pattern CODE_BLACK = Pattern.compile(ChatColour.BLACK.toString());
 	private static final Pattern CODE_DARK_GREEN = Pattern.compile(ChatColour.DARK_GREEN.toString());
 	private static final Pattern CODE_DARK_BLUE = Pattern.compile(ChatColour.DARK_BLUE.toString());
 	private static final Pattern CODE_DARK_AQUA = Pattern.compile(ChatColour.DARK_AQUA.toString());
 	private static final Pattern CODE_DARK_RED = Pattern.compile(ChatColour.DARK_RED.toString());
 	private static final Pattern CODE_DARK_PURPLE = Pattern.compile(ChatColour.DARK_PURPLE.toString());
 	private static final Pattern CODE_GOLD = Pattern.compile(ChatColour.GOLD.toString());
 	private static final Pattern CODE_GRAY = Pattern.compile(ChatColour.GRAY.toString());
 	private static final Pattern CODE_DARK_GRAY = Pattern.compile(ChatColour.DARK_GRAY.toString());
 	private static final Pattern CODE_BLUE = Pattern.compile(ChatColour.BLUE.toString());
 	private static final Pattern CODE_GREEN = Pattern.compile(ChatColour.GREEN.toString());
 	private static final Pattern CODE_AQUA = Pattern.compile(ChatColour.AQUA.toString());
 	private static final Pattern CODE_RED = Pattern.compile(ChatColour.RED.toString());
 	private static final Pattern CODE_LIGHT_PURPLE = Pattern.compile(ChatColour.LIGHT_PURPLE.toString());
 	private static final Pattern CODE_YELLOW = Pattern.compile(ChatColour.YELLOW.toString());
 	private static final Pattern CODE_WHITE = Pattern.compile(ChatColour.WHITE.toString());
 	private static final Pattern CODE_MAGIC = Pattern.compile(ChatColour.MAGIC.toString());
 	private static final Pattern CODE_BOLD = Pattern.compile(ChatColour.BOLD.toString());
 	private static final Pattern CODE_STRIKETHROUGH = Pattern.compile(ChatColour.STRIKETHROUGH.toString());
 	private static final Pattern CODE_UNDERLINE = Pattern.compile(ChatColour.UNDERLINE.toString());
 	private static final Pattern CODE_ITALIC = Pattern.compile(ChatColour.ITALIC.toString());
 	private static final Pattern CODE_RESET = Pattern.compile(ChatColour.RESET.toString());
 }
