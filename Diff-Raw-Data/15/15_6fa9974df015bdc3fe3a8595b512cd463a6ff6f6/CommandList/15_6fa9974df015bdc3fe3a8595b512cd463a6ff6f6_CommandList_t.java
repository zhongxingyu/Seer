 package to.joe.bungee.commands;
 
 import java.util.Collection;
 
 import net.md_5.bungee.api.ChatColor;
 import net.md_5.bungee.api.CommandSender;
 import net.md_5.bungee.api.ProxyServer;
 import net.md_5.bungee.api.connection.ProxiedPlayer;
 import net.md_5.bungee.api.plugin.Command;
 
 /**
  * Command to list all players connected to the proxy.
  */
 public class CommandList extends Command {
 
     public CommandList() {
         super("glist", "j2.default");
     }
 
     @Override
     public void execute(CommandSender sender, String[] args) {
         final StringBuilder users = new StringBuilder();
         final Collection<ProxiedPlayer> connections = ProxyServer.getInstance().getPlayers();
        if (connections.size() > 0) {
            for (final ProxiedPlayer con : connections) {
                users.append(con.getName());
                users.append(", ");
            }
            users.setLength(users.length() - 2);
            sender.sendMessage(ChatColor.BLUE + "Currently online across all servers (" + connections.size() + "): " + ChatColor.RESET + users);
        } else {
            sender.sendMessage(ChatColor.RED + "Nobody home.");
         }
     }
 }
