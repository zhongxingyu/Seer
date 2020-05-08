 package org.scripts.combat;
 
 import org.powerbot.core.script.job.LoopTask;
 import org.powerbot.game.api.methods.interactive.Players;
 import org.powerbot.game.api.util.Random;
 
 import sk.action.ActionBar;
 
 public class AbilityLoopTask extends LoopTask {
 
 	@Override
 	public int loop() {
 		if (Players.getLocal().isInCombat() && Players.getLocal().getInteracting() != null) {
 	        if (!ActionBar.isExpanded()) {
 	            ActionBar.expand(true);
 	        }
	        
 	        for (int i = 0; i < 9; i++) {
 	            if (ActionBar.getNode(i).canUse()) {
 	            	if (Random.nextInt(1, 30) == 1) {
 	            		ActionBar.getNode(i).spam();
 	            	} else {
 	            		ActionBar.getNode(i).send();
 	            	}
 	                break;
 	            }
 	
 	        }
 		}
 		return 1000;
 	}
 
 }
