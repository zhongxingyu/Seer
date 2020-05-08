 package org.scripts.combat.jobs;
 
 import org.powerbot.core.script.job.state.Node;
 import org.powerbot.game.api.methods.Calculations;
 import org.powerbot.game.api.methods.Walking;
 import org.powerbot.game.api.methods.interactive.NPCs;
 import org.powerbot.game.api.methods.interactive.Players;
 import org.powerbot.game.api.methods.widget.Camera;
 import org.powerbot.game.api.util.Filter;
 import org.powerbot.game.api.util.Timer;
 import org.powerbot.game.api.wrappers.interactive.NPC;
 import org.scripts.combat.NPCType;
 import org.scripts.combat.ScriptState;
 import org.scripts.combat.Variables;
 import org.scripts.combat.util.DynamicSleep;
 
 /**
  * A node that handles the fighting element of the script.
  * @author Thock321
  *
  */
 public class CombatNode extends Node {
 	
 	public CombatNode(Variables vars) {
 		this.vars = vars;
 	}
 	
 	private Variables vars;
 	
 	private NPC target;
 
 	@Override
 	public boolean activate() {
 		return !Players.getLocal().isInCombat() && vars.getNpcTypesToAttack().size() > 0 && 
 				Players.getLocal().isMoving() == false;
 	}
 	
 	private DynamicSleep sleeper;
 
 	@Override
 	public void execute() {
 		target = getNearestNpcToAttack();
 		if (target == null) 
 			return;
 		vars.setCurrentState(ScriptState.FIGHTING);
 		if (Calculations.distance(target, Players.getLocal()) > 10) {
 			Walking.walk(target);
 			sleeper = new DynamicSleep(new Timer(10000)) {
 
 				@Override
 				public boolean conditionMet() {
 					return Calculations.distance(target, Players.getLocal()) <= 10;
 				}
 				
 			};
 			sleeper.execute();
 		}
 		if (!target.isOnScreen()) {
 			Camera.turnTo(target);
 			sleeper = new DynamicSleep(new Timer(2000)) {
 
 				@Override
 				public boolean conditionMet() {
 					return target.isOnScreen();
 				}
 				
 			};
 			sleeper.execute();
 		}
 		target.interact("Attack", target.getName());
 		sleeper = new DynamicSleep(new Timer(20000)) {
 
 			@Override
 			public boolean conditionMet() {
 				return Players.getLocal().getInteracting() != null || Players.getLocal().isIdle();
 			}
 			
 		};
 		sleeper.execute();
 	}
 	
 	/**
 	 * Gets the closest npc that you want to attack.  Returns any npc currently attacking you regardless if you want to attack them or not (in order to be
 	 * rid of them) if any.  The RSBot method does not get the "nearest" npc all the time.
 	 * @return The closest npc you want to attack or any npc attacking you.
 	 */
 	private NPC getNearestNpcToAttack() {
 		NPC attackingPlayer = NPCs.getNearest(new Filter<NPC>() {
 
 			@Override
 			public boolean accept(NPC n) {
 				return n != null && n.getInteracting().equals(Players.getLocal());
 			}
 			
 		});
 		if (attackingPlayer != null)
 			return attackingPlayer;
 		NPC[] npcs = NPCs.getLoaded(new Filter<NPC>() {
 
 			@Override
 			public boolean accept(NPC n) {
 				for (NPCType nt : vars.getNpcTypesToAttack()) {
					if (n != null && n.getName().equalsIgnoreCase(nt.getName()) && n.getHealthPercent() > 0 && n.getInteracting() == null) {
 						return true;
 					}
 				}
 				return false;
 			}
 			
 		});
 		if (npcs.length < 1)
 			return null;
 		NPC closestNPC = npcs[0];
 		for (NPC npc : npcs) {
 			if (Calculations.distance(npc, Players.getLocal()) < Calculations.distance(closestNPC, Players.getLocal())) {
 				closestNPC = npc;
 			}
 		}
 		return closestNPC;
 	}
 
 }
