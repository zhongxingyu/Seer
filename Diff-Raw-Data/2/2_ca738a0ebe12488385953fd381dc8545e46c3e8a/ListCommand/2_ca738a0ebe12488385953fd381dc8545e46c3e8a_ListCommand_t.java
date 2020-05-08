 package net.robbytu.banjoserver.bungee.list;
 
 import net.md_5.bungee.api.ChatColor;
 import net.md_5.bungee.api.CommandSender;
 import net.md_5.bungee.api.config.ListenerInfo;
 import net.md_5.bungee.api.config.ServerInfo;
 import net.md_5.bungee.api.plugin.Command;
 import net.robbytu.banjoserver.bungee.Main;
 
 import java.util.Map;
 
 public class ListCommand extends Command {
     public ListCommand() {
         super("list", null, "online");
     }
 
     @Override
     public void execute(CommandSender sender, String[] args) {
         sender.sendMessage(" ");
        sender.sendMessage(ChatColor.GREEN + "Er zijn " + ChatColor.BOLD + Main.instance.getProxy().getOnlineCount() + ChatColor.RESET + "" + ChatColor.GREEN + "/" + ((ListenerInfo) Main.instance.getProxy().getConfigurationAdapter().getListeners().toArray()[0]).getMaxPlayers() + " spelers online.");
 
         for(ServerInfo info : Main.instance.getProxy().getServers().values()) {
             sender.sendMessage(ChatColor.GRAY + info.getName() + ": " + info.getPlayers().size() + " online");
         }
 
         sender.sendMessage(" ");
     }
 }
