 package me.kitskub.shout;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import org.bukkit.Bukkit;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 
 public class ShoutCommand implements  CommandExecutor {
     private Map<String, Long> lastShout = new HashMap<String, Long>();
     
    public void onShout(Player p, String message) {
         AsyncPlayerChatEvent event = new AsyncPlayerChatEvent(true, p, message, new HashSet<Player>());
         Bukkit.getPluginManager().callEvent(event);
         String newMessage = String.format(event.getFormat(), event.getPlayer().getDisplayName(), event.getMessage());
         for (Player pl : Bukkit.getOnlinePlayers()) {
             if (pl.getName().equals(p.getName())) {
                 continue;
             }
             pl.sendMessage(newMessage);
         }
     }
 
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
         if (!(sender instanceof Player)) {
            return true;
        }
        if (sender.hasPermission("shout.bypass")) {
             return true;
         }
         if (args.length < 1) {
             return false;
         }
         double elapsed = lastShout.get(sender.getName())/(1000000000000.0d);
         double waitTime = Shout.getInstance().waitTime();
        if (lastShout.containsKey(sender.getName()) &&  elapsed < waitTime) {
             sender.sendMessage("You must wait " + (waitTime - elapsed) + " seconds more");
             return true;
         }
         lastShout.put(sender.getName(), System.nanoTime());
        onShout((Player) sender, args[0]);
         return true;
     }
 }
