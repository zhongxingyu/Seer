 package aigilas.hud;
 
 import aigilas.creatures.BaseCreature;
 import aigilas.items.Equipment;
 import aigilas.items.GenericItem;
 import aigilas.items.ItemSlot;
 import sps.graphics.Renderer;
 import sps.text.Text;
 import sps.text.TextPool;
 import sps.util.StringSquisher;
 import sps.util.StringStorage;
 
 public class DeltasHud extends BaseHud {
     private final Equipment _equipment;
 
     public DeltasHud(BaseCreature owner, Equipment equipment) {
         super(owner, Renderer.VirtualWidth / 2, Renderer.VirtualHeight / 2);
         _equipment = equipment;
     }
 
     @Override
     public void draw() {
     }
 
     private GenericItem getEquipmentIn(ItemSlot slot) {
         if (_equipment.getItems().containsKey(slot)) {
             return _equipment.getItems().get(slot);
         }
         return null;
     }
 
     private static final String delim = "|";
     private static final String positive = "+";
     private static final String title = "Deltas";
     private String display = "EMPTY";
 
     private Text heading;
     private Text deltas;
 
     public void update(GenericItem item, boolean refresh) {
         if (_isVisible) {
             if (item != null && refresh) {
                 if (getEquipmentIn(item.getItemClass().Slot) != null) {
                     StringSquisher.clear();
                     for (Integer stat : getEquipmentIn(item.getItemClass().Slot).Modifiers.getDeltas(item.Modifiers)) {
                         StringSquisher.squish(((stat > 0) ? positive : ""), StringStorage.get(stat), delim);
                     }
                     display = StringSquisher.flush();
                 }
             }
             if (heading == null || !heading.isVisible() || refresh) {
                 if (deltas != null) {
                     heading.hide();
                     deltas.hide();
                 }
                 heading = TextPool.get().write(title, getInventoryAnchor().add(30, (int) (_dimensions.Y * .2)));
                deltas = TextPool.get().write(display, getInventoryAnchor().add(30, (int) (_dimensions.Y * .1) + 20));
             }
         }
         else {
             if (heading != null) {
                 heading.hide();
                 deltas.hide();
             }
         }
     }
 }
