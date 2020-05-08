 package fr.ethilvan.bukkit.accounts.listeners;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerKickEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 
 import fr.ethilvan.bukkit.api.EthilVan;
 import fr.ethilvan.bukkit.api.accounts.Account;
 import fr.ethilvan.bukkit.accounts.AccountsPlugin;
 
 public class AccountsListener implements Listener {
 
     private final AccountsPlugin plugin;
 
     public AccountsListener(AccountsPlugin plugin) {
         this.plugin = plugin;
     }
 
     @EventHandler
    public void onPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
         Account account = EthilVan.getAccounts()
                 .getByMinecraftName(event.getName());
         if (account == null)
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST, 
                     "Vous devez effectuer une postulation\n sur " +
                     "ethilvan.fr pour pouvoir accéder au serveur.");
     }
 
     @EventHandler
     public void onPlayerJoin(PlayerJoinEvent event) {
         Player player = event.getPlayer();
         String name;
         Account account = EthilVan.getAccounts().get(player);
         if (account == null) {
             plugin.getLogger().severe("Getting account for `"
                     + player.getName()
                     + "` from database failed");
             return;
         }
         name = account.getColoredName();
         player.setDisplayName(name);
         player.setPlayerListName(name);
         event.setJoinMessage(name + ChatColor.YELLOW
                 + " est entré(e) dans le monde d'Ethil Van.");
     }
 
     @EventHandler(priority=EventPriority.HIGH)
     public void onPlayerKick(PlayerKickEvent event) {
         String msg = event.getPlayer().getDisplayName();
         msg += ChatColor.YELLOW + " a dû quitter les terres d'Ethil Van.";
         event.setLeaveMessage(msg);
         plugin.updateStats(event.getPlayer());
     }
 
     @EventHandler(priority=EventPriority.HIGH)
     public void onPlayerQuit(PlayerQuitEvent event) {
         String msg = event.getPlayer().getDisplayName();
         msg += ChatColor.YELLOW + " a quitté les terres d'Ethil Van.";
         event.setQuitMessage(msg);
         plugin.updateStats(event.getPlayer());
     }
 
     @EventHandler
     public void onEntityDeath(EntityDeathEvent event) {
         if (event instanceof PlayerDeathEvent) {
             plugin.updateStats((Player)event.getEntity(), true);
         }
     }
 
     @EventHandler
     public void onPlayerInteract(PlayerInteractEvent event) {
         Block block = event.getClickedBlock(); 
         if (block != null
                 && block.getType() == Material.ENCHANTMENT_TABLE) {
             plugin.updateStats(event.getPlayer());
         }
     }
 }
