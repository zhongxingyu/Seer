 package tk.kirlian.SignTraderWithDucks;
 
 import org.bukkit.Location;
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockListener;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.block.SignChangeEvent;
 
 import java.util.logging.Logger;
 import tk.kirlian.util.CustomLogger;
 import tk.kirlian.SignTraderWithDucks.errors.*;
 import tk.kirlian.SignTraderWithDucks.signs.*;
 
 /**
  * Listens for block events -- like placing a sign.
  */
 public class SignTraderBlockListener extends BlockListener {
     private final SignTraderPlugin plugin;
     private final Logger log;
 
     public SignTraderBlockListener(SignTraderPlugin plugin) {
         this.log = plugin.log;
         this.plugin = plugin;
     }
 
     @Override
     public void onSignChange(SignChangeEvent event) {
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
             event.getPlayer().sendMessage("I'm sorry, " + event.getPlayer().getName() +". I'm afraid I can't do that.");
         }
         if(sign != null) {
             sign.updateSign(event);
             event.getPlayer().sendMessage("Created sign! Yay!");
             if(!sign.isGlobal()) {
                 event.getPlayer().sendMessage("Type \"/signtrader link\" to connect this sign with a chest.");
             }
         }
     }
 
     public void onBlockBreak(BlockBreakEvent event) {
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
                     event.getPlayer().sendMessage(ex.getMessage());
                 }
             }
         }
     }
 }
