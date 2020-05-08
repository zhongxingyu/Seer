 package org.josue.dragonfiend.nodes;
 
 import org.josue.dragonfiend.method.Methods;
 import org.powerbot.core.script.job.Task;
 import org.powerbot.core.script.job.state.Node;
 import org.powerbot.game.api.methods.interactive.Players;
 import org.powerbot.game.api.methods.tab.Prayer;
 import org.powerbot.game.api.wrappers.interactive.NPC;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Josue
  * Date: 5/11/13
  * Time: 5:49 PM
  * To change this template use File | Settings | File Templates.
  */
 public class QuickPrayers extends Node {
 
 
     @Override
     public boolean activate() {
         return Players.getLocal().isInCombat() && Players.getLocal().getInteracting() != null && Prayer.isQuickOn() == false && Prayer.getPoints() > 0;
 
     }
 
     @Override
     public void execute() {
         final NPC AGGRESSIVE = Methods.getNearest();
         if (AGGRESSIVE != null) {
             System.out.println("Quickprayer Node");
             Prayer.toggleQuick(true);
             Task.sleep(1000);
         }
     }
 }
 
