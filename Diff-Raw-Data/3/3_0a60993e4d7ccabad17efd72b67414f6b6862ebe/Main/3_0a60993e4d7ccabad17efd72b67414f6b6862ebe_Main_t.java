 package edgruberman.bukkit.messageformatter;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.Reader;
 import java.io.Writer;
 import java.nio.charset.Charset;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.List;
 import java.util.logging.Handler;
 import java.util.logging.Level;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import edgruberman.bukkit.messageformatter.commands.Broadcast;
 import edgruberman.bukkit.messageformatter.commands.Local;
 import edgruberman.bukkit.messageformatter.commands.Me;
 import edgruberman.bukkit.messageformatter.commands.Reload;
 import edgruberman.bukkit.messageformatter.commands.Reply;
 import edgruberman.bukkit.messageformatter.commands.Say;
 import edgruberman.bukkit.messageformatter.commands.Send;
 import edgruberman.bukkit.messageformatter.commands.Tell;
 
 public final class Main extends JavaPlugin {
 
     private static final Version MINIMUM_CONFIGURATION = new Version("3.0.0");
 
     public static Messenger messenger;
 
     @Override
     public void onEnable() {
         this.reloadConfig();
         Main.messenger = Messenger.load(this, "messages");
 
         Bukkit.getPluginManager().registerEvents(new Formatter(this), this);
 
         this.getCommand("messageformatter:say").setExecutor(new Say());
         this.getCommand("messageformatter:me").setExecutor(new Me());
         this.getCommand("messageformatter:local").setExecutor(new Local(this));
         final Reply reply = new Reply(this);
         this.getCommand("messageformatter:reply").setExecutor(reply);
         this.getCommand("messageformatter:tell").setExecutor(new Tell(reply));
         this.getCommand("messageformatter:broadcast").setExecutor(new Broadcast(this));
         this.getCommand("messageformatter:send").setExecutor(new Send(this));
         this.getCommand("messageformatter:reload").setExecutor(new Reload(this));
     }
 
     @Override
     public void onDisable() {
        HandlerList.unregisterAll(this);
         Main.messenger = null;
     }
 
     @Override
     public void reloadConfig() {
         this.saveDefaultConfig();
         super.reloadConfig();
         this.setLogLevel(this.getConfig().getString("logLevel"));
 
         final Version version = new Version(this.getConfig().getString("version"));
         if (version.compareTo(Main.MINIMUM_CONFIGURATION) >= 0) return;
 
         this.archiveConfig("config.yml", version);
         this.saveDefaultConfig();
         this.reloadConfig();
     }
 
     @Override
     public void saveDefaultConfig() {
         this.extractConfig("config.yml", false);
     }
 
     private void archiveConfig(final String resource, final Version version) {
         final String backupName = "%1$s - Archive version %2$s - %3$tY%3$tm%3$tdT%3$tH%3$tM%3$tS.yml";
         final File backup = new File(this.getDataFolder(), String.format(backupName, resource.replaceAll("(?i)\\.yml$", ""), version, new Date()));
         final File existing = new File(this.getDataFolder(), resource);
 
         if (!existing.renameTo(backup))
             throw new IllegalStateException("Unable to archive configuration file \"" + existing.getPath() + "\" with version \"" + version + "\" to \"" + backup.getPath() + "\"");
 
         this.getLogger().warning("Archived configuration file \"" + existing.getPath() + "\" with version \"" + version + "\" to \"" + backup.getPath() + "\"");
     }
 
     private void extractConfig(final String resource, final boolean replace) {
         final Charset source = Charset.forName("UTF-8");
         final Charset target = Charset.defaultCharset();
         if (target.equals(source)) {
             super.saveResource(resource, replace);
             return;
         }
 
         final File config = new File(this.getDataFolder(), resource);
         if (config.exists()) return;
 
         final char[] cbuf = new char[1024]; int read;
         try {
             final Reader in = new BufferedReader(new InputStreamReader(this.getResource(resource), source));
             final Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(config), target));
             while((read = in.read(cbuf)) > 0) out.write(cbuf, 0, read);
             out.close(); in.close();
 
         } catch (final Exception e) {
             throw new IllegalArgumentException("Could not extract configuration file \"" + resource + "\" to " + config.getPath() + "\";" + e.getClass().getName() + ": " + e.getMessage());
         }
     }
 
     private void setLogLevel(final String name) {
         Level level;
         try { level = Level.parse(name); } catch (final Exception e) {
             level = Level.INFO;
             this.getLogger().warning("Log level defaulted to " + level.getName() + "; Unrecognized java.util.logging.Level: " + name);
         }
 
         // Only set the parent handler lower if necessary, otherwise leave it alone for other configurations that have set it
         for (final Handler h : this.getLogger().getParent().getHandlers())
             if (h.getLevel().intValue() > level.intValue()) h.setLevel(level);
 
         this.getLogger().setLevel(level);
         this.getLogger().config("Log level set to: " + this.getLogger().getLevel());
     }
 
     public static String formatSender(final CommandSender sender) {
         String formatted = null;
 
         if (sender instanceof ConsoleCommandSender)
             formatted = String.format(Main.messenger.getFormat("names.+console"), sender.getName());
 
         if (sender instanceof Player)
             formatted = String.format(Main.messenger.getFormat("names.+player"), ((Player) sender).getDisplayName());
 
         if (formatted == null)
             formatted = String.format(Main.messenger.getFormat("names.+other"), sender.getName());
 
         if (formatted != null) return formatted;
 
         return sender.getName();
     }
 
     public static String formatColors(final CommandSender sender, final String message) {
         if (!message.startsWith("&")) return message;
 
         if (!sender.hasPermission("messageformatter.colors")) return message;
 
         return ChatColor.translateAlternateColorCodes('&', message.substring(1));
     }
 
     public static String formatColors(final CommandSender sender, final String[] args) {
         return Main.formatColors(sender, Main.join(Arrays.asList(args), " "));
     }
 
     /**
      * Combine all the elements of a list together with a delimiter between each
      *
      * @param list list of elements to join
      * @param delim delimiter to place between each element
      * @return string combined with all elements and delimiters
      */
     private static String join(final List<String> list, final String delim) {
         if (list == null || list.isEmpty()) return "";
 
         final StringBuilder sb = new StringBuilder();
         for (final String s : list) sb.append(s + delim);
         sb.delete(sb.length() - delim.length(), sb.length());
 
         return sb.toString();
     }
 
 }
