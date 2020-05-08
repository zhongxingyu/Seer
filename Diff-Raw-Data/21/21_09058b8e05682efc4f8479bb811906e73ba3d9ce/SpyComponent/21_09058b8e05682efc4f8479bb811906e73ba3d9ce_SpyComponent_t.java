 package fr.noogotte.useful_commands.component;
 
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerPickupItemEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 
 import fr.aumgn.bukkitutils.command.Commands;
 import fr.noogotte.useful_commands.UsefulCommandsPlugin;
 import fr.noogotte.useful_commands.command.SpyCommand;
 
 public class SpyComponent extends Component implements Listener {
 
     private final Set<Player> spy;
 
     public SpyComponent(UsefulCommandsPlugin plugin) {
         super(plugin);
         spy = new HashSet<Player>();
         Bukkit.getPluginManager().registerEvents(this, plugin);
     }
 
     @Override
     public String getName() {
         return "spy";
     }
 
     @Override
     public List<Commands> getCommands() {
         return Collections.<Commands>singletonList(new SpyCommand(this));
     }
 
     public boolean isSpy(Player player) {
         return spy.contains(player);
     }
 
     public HashSet<Player> getSpy() {
         return new HashSet<Player>(spy);
     }
     
     public void toggleSpyMode(Player player) {
     	if(spy.contains(player)) {
     		spy.remove(player);
     	} else {
     		spy.add(player);
     	}
     }
     
     public int getNbSpy() {
     	return spy.size();
     }
 
     @EventHandler
     public void onPlayerJoin(PlayerJoinEvent event) {
         for (Player spyPlayer : spy) {
             event.getPlayer().hidePlayer(spyPlayer);
         }
     }
 
     @EventHandler
     public void onPlayerQuit(PlayerQuitEvent event) {
         if (isSpy(event.getPlayer())) {
             event.setQuitMessage(null);
             spy.remove(event.getPlayer());
             for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                 onlinePlayer.showPlayer(event.getPlayer());
             }
         }
     }
 
     @EventHandler
     public void onPlayerPickupItem(PlayerPickupItemEvent event) {
         if (isSpy(event.getPlayer()) && !event.getPlayer().isSneaking()) {
             event.setCancelled(true);
         }
     }
 }
