 package edgruberman.bukkit.messageformatter.commands;
 
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import edgruberman.bukkit.messageformatter.Main;
 import edgruberman.bukkit.messageformatter.commands.util.Action;
 import edgruberman.bukkit.messageformatter.commands.util.Context;
 import edgruberman.bukkit.messageformatter.commands.util.Parser;
 import edgruberman.bukkit.messagemanager.MessageLevel;
 
 public final class Local extends Action {
 
     public static int distance = 100;
 
     public Local(final JavaPlugin plugin) {
         super(plugin, "local");
     }
 
     @Override
     public boolean perform(final Context context) {
         if (!(context.sender instanceof Player)) {
            Main.messageManager.respond(context.sender, "Only in-game players can use the " + context.label + " command", MessageLevel.RIGHTS);
             return true;
         }
 
         String message = Parser.join(context.arguments);
         message = Main.formatColors(context.sender, message);
         if (message.length() < 1) return false;
 
         message = String.format(Main.getMessageFormat("local"), message.trim(), Main.formatSender(context.sender));
        final MessageLevel level = Main.getMessageLevel("local");
 
        final Player sender = (Player) context.sender;
        for (final Entity e : sender.getNearbyEntities(Local.distance, Local.distance, Local.distance))
             if (e instanceof Player)
                 Main.messageManager.send((Player) e, message, level);
 
        Main.messageManager.respond(context.sender, message, level);

         return true;
     }
 
 }
