 package edgruberman.bukkit.consolecolor;
 
 import java.text.MessageFormat;
 import java.util.EnumMap;
 import java.util.Map;
 
 import org.bukkit.ChatColor;
 import org.fusesource.jansi.Ansi;
 import org.fusesource.jansi.Ansi.Attribute;
 
 public class AnsiColor {

     private static final Map<ChatColor, String> replacements = new EnumMap<ChatColor, String>(ChatColor.class);
 
     static {
         AnsiColor.replacements.put(ChatColor.BLACK, Ansi.ansi().fg(Ansi.Color.BLACK).boldOff().toString());
         AnsiColor.replacements.put(ChatColor.DARK_BLUE, Ansi.ansi().fg(Ansi.Color.BLUE).boldOff().toString());
         AnsiColor.replacements.put(ChatColor.DARK_GREEN, Ansi.ansi().fg(Ansi.Color.GREEN).boldOff().toString());
         AnsiColor.replacements.put(ChatColor.DARK_AQUA, Ansi.ansi().fg(Ansi.Color.CYAN).boldOff().toString());
         AnsiColor.replacements.put(ChatColor.DARK_RED, Ansi.ansi().fg(Ansi.Color.RED).boldOff().toString());
         AnsiColor.replacements.put(ChatColor.DARK_PURPLE, Ansi.ansi().fg(Ansi.Color.MAGENTA).boldOff().toString());
         AnsiColor.replacements.put(ChatColor.GOLD, Ansi.ansi().fg(Ansi.Color.YELLOW).boldOff().toString());
         AnsiColor.replacements.put(ChatColor.GRAY, Ansi.ansi().fg(Ansi.Color.WHITE).boldOff().toString());
         AnsiColor.replacements.put(ChatColor.DARK_GRAY, Ansi.ansi().fg(Ansi.Color.BLACK).bold().toString());
         AnsiColor.replacements.put(ChatColor.BLUE, Ansi.ansi().fg(Ansi.Color.BLUE).bold().toString());
         AnsiColor.replacements.put(ChatColor.GREEN, Ansi.ansi().fg(Ansi.Color.GREEN).bold().toString());
         AnsiColor.replacements.put(ChatColor.AQUA, Ansi.ansi().fg(Ansi.Color.CYAN).bold().toString());
         AnsiColor.replacements.put(ChatColor.RED, Ansi.ansi().fg(Ansi.Color.RED).bold().toString());
         AnsiColor.replacements.put(ChatColor.LIGHT_PURPLE, Ansi.ansi().fg(Ansi.Color.MAGENTA).bold().toString());
         AnsiColor.replacements.put(ChatColor.YELLOW, Ansi.ansi().fg(Ansi.Color.YELLOW).bold().toString());
         AnsiColor.replacements.put(ChatColor.WHITE, Ansi.ansi().fg(Ansi.Color.WHITE).bold().toString());
         AnsiColor.replacements.put(ChatColor.MAGIC, Ansi.ansi().a(Attribute.BLINK_SLOW).toString());
         AnsiColor.replacements.put(ChatColor.BOLD, Ansi.ansi().a(Attribute.UNDERLINE_DOUBLE).toString());
         AnsiColor.replacements.put(ChatColor.STRIKETHROUGH, Ansi.ansi().a(Attribute.STRIKETHROUGH_ON).toString());
         AnsiColor.replacements.put(ChatColor.UNDERLINE, Ansi.ansi().a(Attribute.UNDERLINE).toString());
         AnsiColor.replacements.put(ChatColor.ITALIC, Ansi.ansi().a(Attribute.ITALIC).toString());
         AnsiColor.replacements.put(ChatColor.RESET, Ansi.ansi().a(Attribute.RESET).fg(Ansi.Color.DEFAULT).toString());
     }
 
     public static String translate(final String message) {
         return AnsiColor.translate(message, "{0}");
     }
 
     public static String translate(String message, final String replace) {
         if (message == null) return null;
 
         for (final Map.Entry<ChatColor, String> replacement : AnsiColor.replacements.entrySet())
             message = message.replaceAll(MessageFormat.format("(?i)({0})", replacement.getKey()), MessageFormat.format(replace, replacement.getValue()));
 
         return message + Ansi.ansi().reset().toString();
     }
 
 }
