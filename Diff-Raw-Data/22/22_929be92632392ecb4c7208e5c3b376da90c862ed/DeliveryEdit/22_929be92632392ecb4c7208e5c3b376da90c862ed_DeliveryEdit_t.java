 package edgruberman.bukkit.inventory.sessions;
 
 import org.bukkit.entity.Player;
 
 import edgruberman.bukkit.inventory.Delivery;
 import edgruberman.bukkit.inventory.repositories.DeliveryRepository;
 
 public class DeliveryEdit extends Session {
 
     protected final DeliveryRepository deliveries;
     protected final Delivery active;
 
     public DeliveryEdit(final Player customer, final DeliveryRepository deliveries, final Delivery active) {
         super(customer, active.getBalance());
         this.deliveries = deliveries;
         this.active = active;
     }
 
     @Override
    protected void onExpand() {
         this.pallet.label("box-delivery", this.active.getPlayer());
     }
 
     @Override
     protected void onEnd() {
         final int viewers = this.pallet.viewers().size();
         if (viewers == 1 && this.pallet.isEmpty()) {
             this.deliveries.delete(this.active);
             return;
         }
 
         if (viewers == 1 && this.pallet.trim()) this.pallet.label("box-delivery", this.active.getPlayer());
         this.deliveries.save(this.active);
     }
 
 }
