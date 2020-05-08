 package tk.kirlian.SignTraderWithDucks;
 
 import java.util.Map;
 import java.util.HashMap;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockState;
 import org.bukkit.block.Chest;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.event.player.PlayerInteractEvent;
 
 import java.util.logging.Logger;
 import tk.kirlian.util.CustomLogger;
 import tk.kirlian.SignTraderWithDucks.errors.*;
 import tk.kirlian.SignTraderWithDucks.signs.*;
 import tk.kirlian.SignTraderWithDucks.trading.*;
 
 /**
  * Listens for player events -- such as clicking a sign.
  */
 public class SignTraderPlayerListener extends PlayerListener {
     private final SignTraderPlugin plugin;
     private final Logger log;
 
     public Map<Player, Boolean> playerStartedLink = new HashMap<Player, Boolean>();
     public Map<Player, TradingSign> playerLinkSign = new HashMap<Player, TradingSign>();
 
     public SignTraderPlayerListener(SignTraderPlugin plugin) {
         this.log = plugin.log;
         this.plugin = plugin;
     }
 
     @Override
     public void onPlayerInteract(PlayerInteractEvent event) {
         if(!event.isCancelled()) {
             Player player = event.getPlayer();
             Block block = event.getClickedBlock();
             BlockState state = block.getState();
             // Right click sign --> Trade
             if(event.getAction() == Action.RIGHT_CLICK_BLOCK && state instanceof Sign) {
                 event.setCancelled(useSign(player, block, (Sign)state));
             // Left clicks are used for linking signs and chests
             } else if(event.getAction() == Action.LEFT_CLICK_BLOCK) {
                 if(state instanceof Sign) {
                     markSign(player, block, (Sign)state);
                 } else if(state instanceof Chest) {
                     markChest(player, block);
                 }
             }
         }
     }
 
     private boolean useSign(Player player, Block block, Sign state) {
         TradingSign sign = null;
         try {
             sign = new TradingSign(plugin,
                                   player,
                                    block.getLocation(),
                                    state.getLines());
         } catch(InvalidSyntaxException ex) {
             // Do nothing!
         } catch(PermissionsException ex) {
             player.sendMessage("You're not allowed to use this sign for some reason.");
         }
         if(sign != null) {
             try {
                 sign.tradeWith(new PlayerInventoryAdapter(plugin, player));
             } catch(InvalidChestException ex) {
                 player.sendMessage("Invalid chest. Make sure it is connected properly.");
             } catch(CannotTradeException ex) {
                 player.sendMessage("Oh noes! Cannot trade!");
             }
             return true;
         } else {
             return false;
         }
     }
 
     private void markSign(Player player, Block block, Sign state) {
         if(playerStartedLink.containsKey(player)) {
             TradingSign sign = null;
             try {
                 sign = new TradingSign(plugin,
                                        null, // There is no placingPlayer as the sign is being marked, not placed.
                                        block.getLocation(),
                                        state.getLines());
             } catch(InvalidSyntaxException ex) {
                 player.sendMessage("That's not a valid trading sign.");
             } catch(PermissionsException ex) {
                 // See above note on placingPlayer
                 throw new RuntimeException(ex);
             }
             if(sign != null) {
                 player.sendMessage("Now left click a chest to connect it.");
                 player.sendMessage("Or left click another sign if that's not the right one.");
                 playerLinkSign.put(player, sign);
             }
         }
     }
 
     private void markChest(Player player, Block block) {
         if(playerLinkSign.containsKey(player)) {
             TradingSign sign = playerLinkSign.get(player);
             sign.setChestLocation(block.getLocation());
             player.sendMessage("Sign connected successfully! Yay!");
             cancelLink(player);
         }
     }
 
     public void cancelLink(Player player) {
         playerStartedLink.remove(player);
         playerLinkSign.remove(player);
     }
 }
