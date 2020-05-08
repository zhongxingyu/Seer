 package name.richardson.james.bukkit.votifieritems;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import com.vexsoftware.votifier.model.VotifierEvent;
 
 import org.bukkit.Server;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 
 import name.richardson.james.bukkit.utilities.listener.Listener;
 import name.richardson.james.bukkit.utilities.localisation.Localisation;
 import name.richardson.james.bukkit.utilities.logging.Logger;
 
 public final class PlayerListener implements Listener {
 
   /** Items held for offline players */
   private final Map<String, ItemStack[]> offlinePlayerItems = new HashMap<String, ItemStack[]>();
   
   private final Server server;
   
   private final ItemList list;
 
   private final Localisation localisation;
 
   private final Logger logger;
 
   public PlayerListener(VotifierItemsPlugin plugin, ItemList list) {
     this.list = list;
     this.server = plugin.getServer();
     this.localisation = plugin.getLocalisation();
     this.logger = plugin.getCustomLogger();
     plugin.getServer().getPluginManager().registerEvents(this, plugin);
   }
   
   @EventHandler(priority=EventPriority.NORMAL)
   public void onPlayerVote(VotifierEvent event) {
     final String playerName = event.getVote().getUsername();
     final Player player = server.getPlayerExact(playerName);
     ItemStack[] items = list.getRandomItems();
     this.server.broadcastMessage(this.localisation.getMessage(this, "vote-broadcast", this.localisation.getMessage(this, "prefix"), event.getVote().getUsername()));
     // if the player is online award them the items now
     if (player != null) {
       this.awardItems(player, items);
     // if they are offline keep the items until they login
     } else {
       this.offlinePlayerItems.put(playerName, items);
     }
   }
   
   @EventHandler(priority=EventPriority.NORMAL, ignoreCancelled=true)
   public void onPlayerJoin(PlayerJoinEvent event) {
     final String playerName = event.getPlayer().getName();
     if (!this.offlinePlayerItems.containsKey(playerName)) return;
     this.awardItems(event.getPlayer(), this.offlinePlayerItems.get(playerName));
    this.offlinePlayerItems.remove(playerName);
   }
   
   private void awardItems(Player player, ItemStack[] items) {
     if (player == null) throw new IllegalArgumentException("player can not be null");
     if (items == null) throw new IllegalArgumentException("items can not be null");
     this.logger.debug(this, "awarded-items", player.getName(), items.toString());
     final PlayerInventory inventory = player.getInventory();
     player.sendMessage(this.localisation.getMessage(this, "items-awarded"));
     Map<Integer, ItemStack> overflow;
     overflow = inventory.addItem(items);
     // drop any overflow items in front of the player
     if (!overflow.isEmpty()) {
       player.sendMessage(this.localisation.getMessage(this, "items-overflow"));
       for (ItemStack item : overflow.values()) {
         player.getWorld().dropItem(player.getEyeLocation().add(player.getLocation().getDirection()), item);
       }
     }
   }
   
 }
