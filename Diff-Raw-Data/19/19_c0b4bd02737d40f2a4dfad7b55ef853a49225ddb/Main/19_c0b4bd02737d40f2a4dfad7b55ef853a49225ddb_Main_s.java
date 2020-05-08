 package edgruberman.bukkit.messageformatter;
 
 import java.io.File;
 import java.util.Arrays;
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.HandlerList;
 
 import edgruberman.bukkit.messageformatter.commands.Broadcast;
 import edgruberman.bukkit.messageformatter.commands.Local;
 import edgruberman.bukkit.messageformatter.commands.Me;
 import edgruberman.bukkit.messageformatter.commands.Nick;
 import edgruberman.bukkit.messageformatter.commands.Reload;
 import edgruberman.bukkit.messageformatter.commands.Reply;
 import edgruberman.bukkit.messageformatter.commands.Say;
 import edgruberman.bukkit.messageformatter.commands.Send;
 import edgruberman.bukkit.messageformatter.commands.Tell;
 import edgruberman.bukkit.messageformatter.messaging.ConfigurationCourier;
 import edgruberman.bukkit.messageformatter.messaging.Courier;
 import edgruberman.bukkit.messageformatter.util.CustomPlugin;
 
 public final class Main extends CustomPlugin {
 
     public static Courier courier;
 
     @Override
    public void onLoad() { this.putConfigMinimum("config.yml", "5.2.0"); }
 
     @Override
     public void onEnable() {
         this.reloadConfig();
         Main.courier = ConfigurationCourier.Factory.create(this).setBase("messages").build();
 
         Bukkit.getPluginManager().registerEvents(new Formatter(this, this.getConfig().getLong("asyncPermissionCache") * 60 * 20), this);
 
         this.getCommand("messageformatter:say").setExecutor(new Say());
         this.getCommand("messageformatter:me").setExecutor(new Me());
         this.getCommand("messageformatter:local").setExecutor(new Local(this));
         final Reply reply = new Reply(this);
         this.getCommand("messageformatter:reply").setExecutor(reply);
         this.getCommand("messageformatter:tell").setExecutor(new Tell(reply));
         this.getCommand("messageformatter:broadcast").setExecutor(new Broadcast());
         this.getCommand("messageformatter:send").setExecutor(new Send());
         this.getCommand("messageformatter:nick").setExecutor(new Nick(this, new File(this.getDataFolder(), "nicks.yml")));
         this.getCommand("messageformatter:reload").setExecutor(new Reload(this));
     }
 
     @Override
     public void onDisable() {
         HandlerList.unregisterAll(this);
         Bukkit.getScheduler().cancelTasks(this);
         Main.courier = null;
     }
 
     // TODO ensure thread-safe config
     public static String formatSender(final CommandSender sender) {
         if (sender instanceof Player)
             return Main.courier.format("names.+player", ((Player) sender).getDisplayName());
 
         if (sender instanceof ConsoleCommandSender)
             return Main.courier.format("names.+console", sender.getName());
 
         return Main.courier.format("names.+other", sender.getName());
     }
 
     public static String translateColors(final CommandSender sender, final String[] args) {
         final String message = Main.join(Arrays.asList(args), " ");
         if (!sender.hasPermission("messageformatter.colors")) return message;
 
         return ChatColor.translateAlternateColorCodes('&', message);
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
