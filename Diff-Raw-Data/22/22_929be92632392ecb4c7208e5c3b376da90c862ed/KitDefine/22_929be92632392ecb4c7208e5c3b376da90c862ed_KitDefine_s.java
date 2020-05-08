 package edgruberman.bukkit.inventory.sessions;
 
 import org.bukkit.entity.Player;
 
 import edgruberman.bukkit.inventory.Kit;
 import edgruberman.bukkit.inventory.repositories.KitRepository;
 
 /** automatic addition of boxes when full */
 public class KitDefine extends Session {
 
     private final KitRepository kits;
     private final Kit active;
 
     public KitDefine(final Player customer, final KitRepository kits, final Kit active) {
         super(customer, active.getContents());
         this.kits = kits;
         this.active = active;
     }
 
     @Override
    public void onExpand() {
         this.pallet.label("box-kit", this.active.getName());
     }
 
     @Override
     protected void onEnd() {
         final int viewers = this.pallet.viewers().size();
         if (viewers == 1 && this.pallet.isEmpty()) {
             this.kits.delete(this.active);
             return;
         }
 
         if (viewers == 1 && this.pallet.trim()) this.pallet.label("box-kit", this.active.getName());
         this.kits.save(this.active);
     }
 
 }
