 package tk.kirlian.SignTraderWithDucks.signs;
 
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 import org.bukkit.block.Sign;
 import org.bukkit.event.block.SignChangeEvent;
 
 import java.util.logging.Logger;
 import tk.kirlian.util.CustomLogger;
 import tk.kirlian.util.Misc;
 import tk.kirlian.SignTraderWithDucks.*;
 import tk.kirlian.SignTraderWithDucks.errors.*;
 import tk.kirlian.SignTraderWithDucks.permissions.*;
 import tk.kirlian.SignTraderWithDucks.trading.*;
 
 /**
  * Represents a sign that can be used as a shop.
  */
 public class TradingSign {
     private SignTraderPlugin plugin;
     private Logger log;
     private Player placingPlayer;
     private Player owner;
     private Location signLocation;
     private boolean global;
     private SignLine buyerToSeller, sellerToBuyer;
 
     /**
      * Create a new TradingSign instance. The placing player may be null
      * if unknown.
      *
      * @throws InvalidSyntaxException if the lines cannot be parsed.
      * @throws PermissionsException if the placing player does not have
      *         permission to place trading signs.
      */
     public TradingSign(SignTraderPlugin plugin, Player placingPlayer, Location signLocation, String[] lines)
       throws InvalidSyntaxException, PermissionsException {
         this.plugin = plugin;
         this.log = plugin.log;
         this.placingPlayer = placingPlayer;
         this.signLocation = signLocation;
 
         for(int i = 0; i < lines.length; ++i) {
             lines[i] = lines[i].trim();
         }
 
         this.global = lines[0].equalsIgnoreCase("[Global]");
         if(!global) {
             owner = plugin.getServer().getPlayer(lines[0]);
             if(owner == null) {
                 throw new InvalidSyntaxException();
             }
         }
 
        PermissionsProvider permissions = PermissionsProvider.getBest();
        String permissionsNode = "SignTrader.place." + (global ? "global" : "personal");
        permissions.throwIfCannot(placingPlayer, permissionsNode);
 
         // Parse the two middle lines
         SignLine line1 = SignLine.fromString(lines[1]);
         SignLine line2 = SignLine.fromString(lines[2]);
         if(line1.getVerb().equals(SignVerb.Give) &&
            line2.getVerb().equals(SignVerb.Take)) {
             buyerToSeller = line1;
             sellerToBuyer = line2;
         } else if(line1.getVerb().equals(SignVerb.Take) &&
                   line2.getVerb().equals(SignVerb.Give)) {
             buyerToSeller = line2;
             sellerToBuyer = line1;
         } else {
             throw new InvalidSyntaxException();
         }
 
         //this.chestLocation = Misc.parseLocation(plugin.getServer(), signLocation.getWorld(), lines[3]);
     }
 
     public String[] writeToStringArray(String[] lines) {
         if(lines.length != 4) {
             throw new IllegalArgumentException("String array must be of length 4");
         }
         if(global) {
             lines[0] = "[Global]";
         } else {
             lines[0] = owner.getName();
         }
         lines[1] = buyerToSeller.toString();
         lines[2] = sellerToBuyer.toString();
         lines[3] = "";
         return lines;
     }
 
     /**
      * Update a sign through a {@link SignChangeEvent}.
      */
     public void updateSign(SignChangeEvent event) {
         writeToStringArray(event.getLines());
     }
 
     /**
      * Return a TradeAdapter corresponding to this sign.
      *
      * @see TradeAdapter
      */
     public TradeAdapter getAdapter() throws InvalidChestException {
         if(global) {
             return new GlobalSignAdapter(plugin);
         } else {
             Location chestLocation = getChestLocation();
             if(chestLocation != null) {
                 return new ChestInventoryAdapter(plugin, owner, chestLocation);
             } else {
                 throw new InvalidChestException();
             }
         }
     }
 
     /**
      * Return whether this sign is <i>global</i>.
      *
      * A sign is global if it is not associated with a player.
      */
      public boolean isGlobal() {
          return global;
      }
 
     /**
      * Attempt to trade with another TradeAdapter.
      */
     public void tradeWith(final TradeAdapter buyerAdapter)
       throws InvalidChestException, CannotTradeException {
         final TradeAdapter sellerAdapter = getAdapter();
         // ARGH!!!
         if(sellerAdapter.canAddSignItem(buyerToSeller.getItem()) &&
            sellerAdapter.canSubtractSignItem(sellerToBuyer.getItem()) &&
            buyerAdapter.canAddSignItem(sellerToBuyer.getItem()) &&
            buyerAdapter.canSubtractSignItem(buyerToSeller.getItem())) {
             sellerAdapter.addSignItem(buyerToSeller.getItem());
             sellerAdapter.subtractSignItem(sellerToBuyer.getItem());
             buyerAdapter.addSignItem(sellerToBuyer.getItem());
             buyerAdapter.subtractSignItem(buyerToSeller.getItem());
         } else {
             throw new CannotTradeException();
         }
     }
 
     /**
      * Create a human-readable representation of this TradingSign.
      *
      * Do not use this for updating the actual sign; to do this, use
      * updateSign() and writeToStringArray().
      */
     public String toString() {
         Location chestLocation = getChestLocation();
         return "TradingSign: Global=" + global + "; " + buyerToSeller + "; " + sellerToBuyer + "; Chest=" + (chestLocation != null ? Misc.locationToString(chestLocation) : "<null>");
     }
 
     public Location getSignLocation() {
         return signLocation;
     }
 
     public Location getChestLocation() {
         return SignManager.getInstance(plugin).getChestLocation(signLocation);
     }
 
     public void setChestLocation(Location chestLocation) {
         SignManager.getInstance(plugin).setChestLocation(signLocation, chestLocation);
         log.info("Set chest location to " + chestLocation);
     }
 
     /**
      * Called when the sign is destroyed.
      *
      * @throws PermissionsException if player is not allowed to break
      *         another player's sign.
      */
     public void destroy(Player breakingPlayer) throws PermissionsException {
         // Players must have special permissions to break other player's signs
        if(!breakingPlayer.getName().equals(owner.getName())) {
            PermissionsProvider.getBest().throwIfCannot(breakingPlayer, "SignTrader.break." + (global ? "global" : "personal"));
         }
         SignManager.getInstance(plugin).removeChestLocation(signLocation);
     }
 }
