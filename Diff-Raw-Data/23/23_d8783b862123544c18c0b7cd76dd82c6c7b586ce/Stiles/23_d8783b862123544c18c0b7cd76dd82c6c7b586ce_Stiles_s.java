 package org.itaio.nodes.fishing;
 
 import org.itaio.utilities.Constants;
 import org.itaio.utilities.Utilities;
 import org.itaio.utilities.interfaces.Condition;
 import org.powerbot.core.script.job.state.Node;
 import org.powerbot.game.api.methods.interactive.NPCs;
 import org.powerbot.game.api.methods.tab.Inventory;
 import org.powerbot.game.api.methods.widget.Camera;
 import org.powerbot.game.api.wrappers.interactive.NPC;
 import org.powerbot.game.api.wrappers.node.Item;
 
 public class Stiles extends Node {
 
     private NPC stiles;
 
     @Override
     public boolean activate() {
        return (stiles == null ? (stiles = NPCs.getNearest(Constants.STILES)) : stiles) != null && Inventory.isFull();
     }
 
     @Override
     public void execute() {
         if (!stiles.isOnScreen()) {
             Camera.turnTo(stiles);
         }
         final Item lobster = Inventory.getItem(Constants.LOBSTER);
         if (lobster != null && stiles != null) {
             if (Inventory.getSelectedItem() == null) {
                 if (lobster.getWidgetChild().click(true)) {
                     Utilities.waitFor(new Condition() {
                         @Override
                         public boolean activate() {
                             return Inventory.getSelectedItem() != null;
                         }
                     }, 2000);
                 }
             } else {
                if (stiles.click(true)) {
                     Utilities.waitFor(new Condition() {
                         @Override
                         public boolean activate() {
                             return Inventory.getCount(Constants.LOBSTER) == 0;
                         }
                     }, 2000);
                 }
             }
         }
     }
 }
