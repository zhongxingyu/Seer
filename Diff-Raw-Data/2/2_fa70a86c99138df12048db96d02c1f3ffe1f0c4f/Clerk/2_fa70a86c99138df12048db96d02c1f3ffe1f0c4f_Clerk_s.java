 package edgruberman.bukkit.delivery.sessions;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Material;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.plugin.Plugin;
 
 import edgruberman.bukkit.delivery.Ledger;
 import edgruberman.bukkit.delivery.Main;
 import edgruberman.bukkit.delivery.repositories.LedgerRepository;
 
 /** withdrawal manager */
 public class Clerk implements Listener {
 
     private final LedgerRepository ledgers;
     private final boolean record;
     private final Plugin plugin;
 
     public Clerk(final LedgerRepository ledgers, final boolean record, final Plugin plugin) {
         this.ledgers = ledgers;
         this.record = record;
         this.plugin = plugin;
     }
 
     @EventHandler(ignoreCancelled = false) // chest right clicks in air are by default cancelled since they do nothing
     public void onRequest(final PlayerInteractEvent interact) {
         if (interact.getItem() != null && interact.getItem().getTypeId() != Material.CHEST.getId()) return; // ignore when a chest item is not held
         if (interact.getAction() != Action.RIGHT_CLICK_AIR) return; // ignore if attempting to place chest
        if (!interact.getPlayer().hasPermission("parcelservice.open")) return; // ignore if not allowed
         interact.setCancelled(true);
 
         final Ledger requested = this.ledgers.load(interact.getPlayer().getName());
         if (requested == null || requested.getBalance().empty()) {
             Main.courier.send(interact.getPlayer(), "empty-balance", interact.getPlayer().getName());
             return;
         }
 
         final String provided = Main.courier.format("withdraw-reason");
         final String reason = Main.courier.format("edit-reason-format", interact.getPlayer().getName(), provided);
         final BalanceWithdraw withdraw = new BalanceWithdraw(interact.getPlayer(), this.ledgers, requested, reason, this.record);
         Bukkit.getPluginManager().registerEvents(withdraw, this.plugin);
     }
 
 }
