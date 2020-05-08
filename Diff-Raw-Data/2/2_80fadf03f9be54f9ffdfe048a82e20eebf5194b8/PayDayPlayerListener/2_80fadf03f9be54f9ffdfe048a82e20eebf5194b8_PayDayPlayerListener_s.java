 package tzer0.PayDay;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 import org.bukkit.event.player.PlayerListener;
 
 public class PayDayPlayerListener extends PlayerListener{
     PayDay plugin;
     public PayDayPlayerListener(PayDay plugin) {
         this.plugin = plugin;
     }
     public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
         Player pl = event.getPlayer();
         String[] command = event.getMessage().split(" ");
         if (command[0].equalsIgnoreCase("/mp")) {
            if (plugin.permissions == null || !plugin.permissions.has(pl, "payday.admin")) {
                 pl.sendMessage(ChatColor.RED+"You do not have access to this command!");
                 return;
             }            String []args = new String[command.length-1];
             for (int i = 0; i < args.length; i++) {
                 args[i] = command[i+1];
             }
             String commandLabel = command[0];
             plugin.onCommand((CommandSender)pl, null, commandLabel, args);
             event.setCancelled(true);
         }
     }
 }
