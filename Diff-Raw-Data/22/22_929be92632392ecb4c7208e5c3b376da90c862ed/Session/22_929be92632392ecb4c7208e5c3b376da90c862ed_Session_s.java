 package edgruberman.bukkit.inventory.sessions;
 
 import java.util.List;
 import java.util.Observable;
 
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.HandlerList;
 import org.bukkit.event.Listener;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.event.inventory.InventoryCloseEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 
 import edgruberman.bukkit.inventory.Box;
 import edgruberman.bukkit.inventory.Pallet;
 
 /** pallet inventory interaction */
 public class Session extends Observable implements Listener {
 
     protected final Player customer;
     protected final Pallet pallet;
 
     protected int index;
 
     /** @param initial set of similar items, single instance of items excluding amount */
     public Session(final Player customer, final Pallet pallet) {
         this.customer = customer;
         this.pallet = pallet;
 
         this.onStart();
         final List<Box> boxes = pallet.getBoxes();
         final Box last = boxes.get(boxes.size() - 1);
         this.index = boxes.size() - 1;
         last.open(customer);
     }
 
     protected void onStart() {};
 
     protected void onExpand() {};
 
     public void next() {
         final List<Box> boxes = this.pallet.getBoxes();
         if (this.index == boxes.size() - 1 && boxes.get(this.index).isFull()) {
             this.pallet.addBox();
             this.onExpand();
         }
 
         final int current = this.index++;
         if (this.index > boxes.size() - 1) this.index = 0;
         if (current != this.index) boxes.get(this.index).open(this.customer);
     }
 
     public void previous() {
         final int current = this.index--;
         if (this.index < 0) this.index = this.pallet.getBoxes().size() - 1;
         if (current != this.index) this.pallet.getBoxes().get(this.index).open(this.customer);
     }
 
     @EventHandler(ignoreCancelled = true)
    protected void click(final InventoryClickEvent click) {
         if (!this.customer.equals(click.getWhoClicked())) return; // ignore when not this customer
         this.onClick(click);
         if (click.isCancelled()) return;
 
         // left or right click outside with nothing on cursor to navigate boxes forwards or backwards
         if (click.getRawSlot() == -999 && click.getCursor().getTypeId() == Material.AIR.getId()) { // TODO Fix SlotType.OUTSIDE not being properly identified; BUKKIT-2768
             if (click.isLeftClick()) {
                 this.next();
             } else {
                 this.previous();
             }
             return;
         }
     }
 
     protected void onClick(final InventoryClickEvent click) {};
 
     @EventHandler
    protected void close(final InventoryCloseEvent close) {
         if (!this.customer.equals(close.getPlayer())) return; // ignore when not this customer
         this.end();
     }
 
     @EventHandler
    protected void quit(final PlayerQuitEvent quit) {
         if (!this.customer.equals(quit.getPlayer())) return; // ignore when not this customer
         this.end();
     }
 
     protected void end() {
         HandlerList.unregisterAll(this);
         this.onEnd();
     }
 
     protected void onEnd() {};
 
     public void destroy() {
         this.end();
         this.customer.getOpenInventory().close();
     }
 
 }
