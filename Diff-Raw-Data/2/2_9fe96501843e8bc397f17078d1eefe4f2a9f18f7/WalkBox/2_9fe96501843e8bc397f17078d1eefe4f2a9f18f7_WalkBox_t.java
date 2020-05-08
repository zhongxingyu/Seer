 package org.injustice.rawchicken.strategies;
 
 import org.injustice.rawchicken.util.Var;
 import org.powerbot.core.script.job.state.Node;
 import org.powerbot.game.api.methods.Calculations;
 import org.powerbot.game.api.methods.Walking;
 import org.powerbot.game.api.methods.interactive.Players;
 import org.powerbot.game.api.methods.node.SceneEntities;
 import org.powerbot.game.api.methods.tab.Inventory;
 import org.powerbot.game.api.methods.widget.Camera;
 import org.powerbot.game.api.wrappers.node.SceneObject;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Injustice
  * Date: 29/03/13
  * Time: 15:13
  * To change this template use File | Settings | File Templates.
  */
 public class WalkBox extends Node {
 
     @Override
     public boolean activate() {
         return Inventory.isFull() && !Var.BANK_TILE.isOnScreen();
     }
 
     @Override
     public void execute() {
         SceneObject depositbox = SceneEntities.getNearest(Var.DEPOSIT_BOX_ID);
         if (!Walking.isRunEnabled() && Walking.getEnergy() > 20) {   // if run isn't enabled and energy is more than 20
             Var.status = "Setting run";                                  // change status
             Walking.setRun(true);                                        // set run to on
         }
         out:                                                           // end if
         if (Var.closedGate != null &&                                // if gate is closed and
                 Players.getLocal().getLocation().getX() <=                   // player's x location
                         Var.closedGate.getLocation().getX()) {                       // is less than or equal to gate's x locatino
             if (Var.closedGate.isOnScreen()) {                           // if closed gate is on screen
                 Var.status = "Opening gate";                                 // change status
                 if (Var.closedGate.interact("Open")) {
                     do sleep(500, 750); while (Players.getLocal().isMoving());
                     Walking.findPath(Var.BANK_TILE).traverse();
                     if (Calculations.distanceTo(depositbox) <= 15 && !depositbox.isOnScreen()) {
                         Camera.turnTo(depositbox);
                     }
                 }
            } else if (Var.CHICKEN_AREA.contains(Players.getLocal().getLocation())) {
                 Var.status = "Walking to gate";
                 Walking.walk(Var.GATE_TILE);
                 do sleep(500, 750); while (Players.getLocal().isMoving());
             }                                                         // else walk to closed gate
         } else {                                                     // else
             Var.status = "Walking to bank";                              // change status
             Walking.findPath(Var.BANK_TILE).traverse();                  // find a path to bank tile and walk it
             if (Calculations.distanceTo(depositbox) <= 15 && !depositbox.isOnScreen()) {
                 Camera.turnTo(depositbox);
             }
         }                                                            // end if
     }
 }
 
 /* -- Thanks to GeemanKan for helping me with some of this -- */
 
