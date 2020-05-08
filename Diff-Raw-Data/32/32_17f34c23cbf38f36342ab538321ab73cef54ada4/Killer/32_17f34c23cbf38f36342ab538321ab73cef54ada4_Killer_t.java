 package org.awesomecraft.plugins.killer;
 
import java.util.HashSet;
import java.util.Set;
import org.bukkit.ChatColor;
 import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerInteractEntityEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Killer extends JavaPlugin implements Listener {
    
    public Set<Player> killEnabled = new HashSet<Player>(); 
     public void onDisable() {
     }
 
     public void onEnable() {
        getCommand("blazekill").setExecutor(new CommandExecutor() {
        public boolean onCommand(CommandSender cs, Command cmnd, String name, String[] args) {
        Player sender = (Player) cs;
        if(sender.hasPermission("killer.kill")) {
            if(!killEnabled.contains(sender)) {
            killEnabled.add(sender);
            sender.sendMessage(ChatColor.GREEN + "Killer enabled.");
            } else {
                killEnabled.remove(sender);
                sender.sendMessage(ChatColor.GREEN + "Killer disabled.");
            }
        }
        return true;
    }
        });
         getServer().getPluginManager().registerEvents(this, this);
     }
 
     @EventHandler
     public void onPlayerEntityInteract(PlayerInteractEntityEvent event) {
         if(event.getPlayer().getItemInHand().getType().equals(Material.BLAZE_ROD) 
                 && event.getPlayer().isSneaking()
                && killEnabled.contains(event.getPlayer())) {
             event.getRightClicked().setFallDistance(1000000000000000000F);
         }
     }
 }
 
