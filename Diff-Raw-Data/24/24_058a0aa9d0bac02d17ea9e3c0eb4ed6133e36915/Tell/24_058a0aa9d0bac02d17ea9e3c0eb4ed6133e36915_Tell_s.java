 package edgruberman.bukkit.messageformatter.commands;
 
 import org.bukkit.OfflinePlayer;
 import org.bukkit.command.CommandSender;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import edgruberman.bukkit.messageformatter.Main;
 import edgruberman.bukkit.messageformatter.commands.util.Action;
 import edgruberman.bukkit.messageformatter.commands.util.Context;
 import edgruberman.bukkit.messageformatter.commands.util.Parser;
 import edgruberman.bukkit.messagemanager.MessageLevel;
 
 public final class Tell extends Action {
 
     public Tell(final JavaPlugin plugin) {
         super(plugin, "tell");
     }
 
     @Override
     public boolean perform(final Context context) {
         // Must supply at least two arguments: /<command> <Player> <Message>
         if (context.arguments.size() < 2) return false;
 
         final OfflinePlayer recipient = Parser.parsePlayer(context, 0);
         if (recipient == null || recipient.getPlayer() == null || !recipient.getPlayer().isOnline()) {
             Main.messageManager.respond(context.sender, "There is no online player matching the name of: " + context.arguments.get(0), MessageLevel.WARNING, false);
             return true;
         }
 
         String message = Parser.join(context.arguments.subList(1, context.arguments.size())).trim();
         message = Main.formatColors(context.sender, message);
         Tell.send(recipient.getPlayer(), context.sender, message);
         return true;
     }
 
     static void send(final CommandSender recipient, final CommandSender sender, final String message) {
         // Sender
         Main.messageManager.respond(
                 sender
                 , String.format(Main.getMessageFormat("tell.sender"), message, Main.formatSender(sender), Main.formatSender(recipient))
                 , Main.getMessageLevel("tell")
         );
 
         // Recipient
         Main.messageManager.respond(
                 recipient
                 , String.format(Main.getMessageFormat("tell.recipient"), message, Main.formatSender(sender), Main.formatSender(recipient))
                 , Main.getMessageLevel("tell")
         );
 
        Reply.lastTellFrom.put(recipient, sender);
     }
 
}
