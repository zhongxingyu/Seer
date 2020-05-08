 
 package com.quartercode.quarterbukkit.api.select;
 
 import java.util.List;
 
 public class LineInventoryLayouter implements InventoryLayouter {
 
     @Override
     public InventoryLayout getLayout(final SelectInventory selectInventory, final List<Selection> selections) {
 
         final InventoryLayout layout = new InventoryLayout();
 
         int x = 0;
         int y = 0;
         for (final Selection selection : selections) {
             layout.set(x, y, selection);
 
            x++;
            if (x >= 9) {
                x = 0;
                y++;
             }
         }
 
         return layout;
     }
 
 }
