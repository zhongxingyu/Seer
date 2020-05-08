 package org.josue.dragonfiend.nodes;
 
 import org.josue.dragonfiend.method.Methods;
 import org.josue.dragonfiend.util.Constants;
 import org.powerbot.core.script.job.Task;
 import org.powerbot.core.script.job.state.Node;
 import org.powerbot.game.api.methods.Walking;
 import org.powerbot.game.api.methods.interactive.NPCs;
 import org.powerbot.game.api.methods.interactive.Players;
 import org.powerbot.game.api.methods.widget.Camera;
 import org.powerbot.game.api.wrappers.interactive.NPC;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Josue
  * Date: 5/11/13
  * Time: 4:37 PM
  * To change this template use File | Settings | File Templates.
  */
 
 public class Attack extends Node {
 
 
     @Override
     public boolean activate() {
         final NPC DRAGON = NPCs.getNearest(Constants.BLACK_DRAGONS);
         return DRAGON != null && Players.getLocal().getInteracting() == null;
     }
 
     @Override
     public void execute() {
         System.out.println("Attacking Node");
         final NPC AGGRESSIVE = Methods.getNearest();
         final NPC DRAGON = NPCs.getNearest(Constants.BLACK_DRAGONS);
         if (AGGRESSIVE != null) {
             System.out.println("Aggressive Spotted");
             if (Methods.isOnScreen(AGGRESSIVE)) {
                 AGGRESSIVE.interact("Attack");
                 Task.sleep(700, 1500);
             } else {
                 Walking.walk(AGGRESSIVE);
                 Camera.turnTo(AGGRESSIVE);
                 Task.sleep(700, 1500);
             }
        } else if (AGGRESSIVE == null && DRAGON != null && !DRAGON.isInCombat() && DRAGON.getInteracting() == null) {
             if (Methods.isOnScreen(DRAGON)) {
                 System.out.println("Attacking idle DRAGON");
                 DRAGON.interact("Attack");
                 Camera.turnTo(DRAGON);
                 Task.sleep(1500);
             } else {
                 Walking.walk(DRAGON);
                 Camera.turnTo(DRAGON);
             }
         }
 
     }
 }
 
