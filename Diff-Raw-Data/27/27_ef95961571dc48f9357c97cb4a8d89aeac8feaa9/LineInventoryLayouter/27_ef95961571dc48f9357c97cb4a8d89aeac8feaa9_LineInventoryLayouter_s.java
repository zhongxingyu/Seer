 
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
 
            y++;
            if (y >= 9) {
                y = 0;
                x++;
             }
         }
 
         return layout;
     }
 
 }
