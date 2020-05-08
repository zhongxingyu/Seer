 package no.runsafe.framework.output;
 
 import no.runsafe.framework.ANSI.Colour;
 import no.runsafe.framework.ANSI.Rendition;
 import org.bukkit.ChatColor;
 
 import java.util.regex.Pattern;
 
 public class ConsoleColors
 {
 	public static String FromMinecraft(String message)
 	{
 		message = BUKKIT_DARK_GREEN.matcher(message).replaceAll(DARK_GREEN);
 		message = BUKKIT_DARK_BLUE.matcher(message).replaceAll(DARK_BLUE);
 		message = BUKKIT_BLACK.matcher(message).replaceAll(BLACK);
 		message = BUKKIT_DARK_AQUA.matcher(message).replaceAll(DARK_AQUA);
 		message = BUKKIT_DARK_RED.matcher(message).replaceAll(DARK_RED);
 		message = BUKKIT_DARK_PURPLE.matcher(message).replaceAll(DARK_PURPLE);
 		message = BUKKIT_GOLD.matcher(message).replaceAll(GOLD);
 		message = BUKKIT_GRAY.matcher(message).replaceAll(GRAY);
 		message = BUKKIT_DARK_GRAY.matcher(message).replaceAll(DARK_GRAY);
 		message = BUKKIT_BLUE.matcher(message).replaceAll(BLUE);
 		message = BUKKIT_GREEN.matcher(message).replaceAll(GREEN);
 		message = BUKKIT_AQUA.matcher(message).replaceAll(AQUA);
 		message = BUKKIT_RED.matcher(message).replaceAll(RED);
 		message = BUKKIT_LIGHT_PURPLE.matcher(message).replaceAll(LIGHT_PURPLE);
 		message = BUKKIT_YELLOW.matcher(message).replaceAll(YELLOW);
 		message = BUKKIT_WHITE.matcher(message).replaceAll(WHITE);
 		message = BUKKIT_MAGIC.matcher(message).replaceAll(MAGIC);
 		message = BUKKIT_BOLD.matcher(message).replaceAll(BOLD);
 		message = BUKKIT_STRIKETHROUGH.matcher(message).replaceAll(STRIKETHROUGH);
 		message = BUKKIT_UNDERLINE.matcher(message).replaceAll(UNDERLINE);
 		message = BUKKIT_ITALIC.matcher(message).replaceAll(ITALIC);
 		message = BUKKIT_RESET.matcher(message).replaceAll(RESET);
 		return message + RESET;
 	}
 
 	private static final Pattern BUKKIT_DARK_GREEN = Pattern.compile(ChatColor.DARK_GREEN.toString());
 	private static final Pattern BUKKIT_DARK_BLUE = Pattern.compile(ChatColor.DARK_BLUE.toString());
 	private static final Pattern BUKKIT_BLACK = Pattern.compile(ChatColor.BLACK.toString());
 	private static final Pattern BUKKIT_DARK_AQUA = Pattern.compile(ChatColor.DARK_AQUA.toString());
 	private static final Pattern BUKKIT_DARK_RED = Pattern.compile(ChatColor.DARK_RED.toString());
 	private static final Pattern BUKKIT_DARK_PURPLE = Pattern.compile(ChatColor.DARK_PURPLE.toString());
 	private static final Pattern BUKKIT_GOLD = Pattern.compile(ChatColor.GOLD.toString());
 	private static final Pattern BUKKIT_GRAY = Pattern.compile(ChatColor.GRAY.toString());
 	private static final Pattern BUKKIT_DARK_GRAY = Pattern.compile(ChatColor.DARK_GRAY.toString());
 	private static final Pattern BUKKIT_BLUE = Pattern.compile(ChatColor.BLUE.toString());
 	private static final Pattern BUKKIT_GREEN = Pattern.compile(ChatColor.GREEN.toString());
 	private static final Pattern BUKKIT_AQUA = Pattern.compile(ChatColor.AQUA.toString());
 	private static final Pattern BUKKIT_RED = Pattern.compile(ChatColor.RED.toString());
 	private static final Pattern BUKKIT_LIGHT_PURPLE = Pattern.compile(ChatColor.LIGHT_PURPLE.toString());
 	private static final Pattern BUKKIT_YELLOW = Pattern.compile(ChatColor.YELLOW.toString());
 	private static final Pattern BUKKIT_WHITE = Pattern.compile(ChatColor.WHITE.toString());
 	private static final Pattern BUKKIT_MAGIC = Pattern.compile(ChatColor.MAGIC.toString());
 	private static final Pattern BUKKIT_BOLD = Pattern.compile(ChatColor.BOLD.toString());
 	private static final Pattern BUKKIT_STRIKETHROUGH = Pattern.compile(ChatColor.STRIKETHROUGH.toString());
 	private static final Pattern BUKKIT_UNDERLINE = Pattern.compile(ChatColor.UNDERLINE.toString());
 	private static final Pattern BUKKIT_ITALIC = Pattern.compile(ChatColor.ITALIC.toString());
 	private static final Pattern BUKKIT_RESET = Pattern.compile(ChatColor.RESET.toString());
 	public static final String DARK_GREEN = Rendition.Clear().Faint().Foreground(Colour.Green).toString();
 	public static final String DARK_BLUE = Rendition.Clear().Faint().Foreground(Colour.Blue).toString();
 	public static final String BLACK = Rendition.Clear().Foreground(Colour.Black).toString();
 	public static final String DARK_AQUA = Rendition.Clear().Faint().Foreground(Colour.Cyan).toString();
 	public static final String DARK_RED = Rendition.Clear().Faint().Foreground(Colour.Red).toString();
 	public static final String DARK_PURPLE = Rendition.Clear().Faint().Foreground(Colour.Magenta).toString();
 	public static final String GOLD = Rendition.Clear().Faint().Foreground(Colour.Yellow).toString();
 	public static final String GRAY = Rendition.Clear().Foreground(Colour.White).toString();
 	public static final String DARK_GRAY = Rendition.Clear().Faint().Foreground(Colour.White).Faint().toString();
 	public static final String BLUE = Rendition.Clear().Foreground(Colour.Blue).Bright().toString();
 	public static final String GREEN = Rendition.Clear().Foreground(Colour.Green).Bright().toString();
 	public static final String AQUA = Rendition.Clear().Foreground(Colour.Cyan).Bright().toString();
 	public static final String RED = Rendition.Clear().Foreground(Colour.Red).Bright().toString();
 	public static final String LIGHT_PURPLE = Rendition.Clear().Foreground(Colour.Magenta).Bright().toString();
 	public static final String YELLOW = Rendition.Clear().Foreground(Colour.Yellow).Bright().toString();
 	public static final String WHITE = Rendition.Clear().Bright().Foreground(Colour.White).toString();
	public static final String MAGIC = Rendition.Clear().Foreground(Colour.Black).Background(Colour.White).Bright().Reverse().toString();
 	public static final String BOLD = Rendition.Clear().Bright().toString();
 	public static final String STRIKETHROUGH = Rendition.Clear().CrossedOut().toString();
 	public static final String UNDERLINE = Rendition.Clear().Underline().toString();
 	public static final String ITALIC = Rendition.Clear().Italic().toString();
 	public static final String RESET = Rendition.Clear().toString();
 }
