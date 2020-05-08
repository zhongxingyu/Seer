 package tk.allele.duckshop;
 
 import org.bukkit.block.Block;
 import org.bukkit.block.Chest;
 import org.bukkit.block.Sign;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockListener;
 import org.bukkit.event.block.SignChangeEvent;
 import tk.allele.duckshop.errors.InvalidSyntaxException;
 import tk.allele.duckshop.signs.SignManager;
 import tk.allele.duckshop.signs.TradingSign;
 import tk.allele.permissions.PermissionsException;
 
 import java.util.logging.Logger;
 
 /**
  * Listens for block events -- like placing a sign.
  */
 public class DuckShopBlockListener extends BlockListener {
     private final DuckShop plugin;
     private final Logger log;
 
     public DuckShopBlockListener(DuckShop plugin) {
         this.log = plugin.log;
         this.plugin = plugin;
     }
 
     @Override
     public void onSignChange(SignChangeEvent event) {
         if(event.isCancelled()) return;
         TradingSign sign = null;
         try {
             sign = new TradingSign(plugin,
                                    event.getPlayer(),
                                    event.getBlock().getLocation(),
                                    event.getLines());
         } catch(InvalidSyntaxException ex) {
             // Do nothing
         } catch(PermissionsException ex) {
             // Science fiction allusions FTW
             event.setCancelled(true);
             event.getPlayer().sendMessage("I'm sorry, " + event.getPlayer().getName() +". I'm afraid I can't do that.");
         }
         if(sign != null) {
             sign.writeToStringArray(event.getLines());
             event.getPlayer().sendMessage("Created sign successfully.");
             if(!sign.isGlobal()) {
                 event.getPlayer().sendMessage("Type \"/duckshop link\" to connect this sign with a chest.");
             }
         }
     }
 
     public void onBlockBreak(BlockBreakEvent event) {
         if(event.isCancelled()) return;
         Block block = event.getBlock();
         if(block.getState() instanceof Sign) {
             TradingSign sign = null;
             try {
                 sign = new TradingSign(plugin,
                                        null,
                                        block.getLocation(),
                                        ((Sign)block.getState()).getLines());
             } catch(InvalidSyntaxException ex) {
                 // Do nothing!
             } catch(PermissionsException ex) {
                 // This shouldn't happen, as there shouldn't be a
                 // PermissionsException until sign.destroy() below.
                 throw new RuntimeException(ex);
             }
             if(sign != null) {
                 try {
                     sign.destroy(event.getPlayer());
                 } catch(PermissionsException ex) {
                     event.setCancelled(true);
                     event.getPlayer().sendMessage("You can't break this!");
                 }
             }
         } else if(block.getState() instanceof Chest) {
             if(SignManager.getInstance(plugin).isChestConnected(block.getLocation())) {
                 event.getPlayer().sendMessage("Warning: This chest is used by a DuckShop sign. The sign will no longer work unless the chest is replaced.");
             }
         }
     }
 }
