 package org.phl0w.aurora.itdropper.actions;
 
 import ms.aurora.api.methods.tabs.Inventory;
 import ms.aurora.api.script.Action;
 import ms.aurora.api.wrappers.WidgetItem;
 import org.phl0w.aurora.itdropper.iTDropper;
 
 public class Drop extends Action {
 
     @Override
     public boolean activate() {
         return Inventory.getCount() > 0;
     }
 
     @Override
     public int execute() {
        iTDropper.log.info("execute");
         for (final WidgetItem i : Inventory.getAll()) {
             if (i.applyAction("Drop")) {
                 iTDropper.log.info("Successfully dropped (amount * id): " + i.getStackSize() + " * " + i.getId() + ".");
             }
         }
         return 1000;
     }
 }
