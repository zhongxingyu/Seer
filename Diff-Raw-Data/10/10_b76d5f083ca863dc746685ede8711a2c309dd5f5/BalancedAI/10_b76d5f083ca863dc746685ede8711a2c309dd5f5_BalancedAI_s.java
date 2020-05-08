 package net.carmgate.morph.model.ai;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import net.carmgate.morph.model.Model;
 import net.carmgate.morph.model.behaviors.common.Movement;
 import net.carmgate.morph.model.behaviors.steering.Break;
 import net.carmgate.morph.model.behaviors.steering.Flee;
 import net.carmgate.morph.model.entities.Ship;
 import net.carmgate.morph.model.entities.common.Entity;
 import net.carmgate.morph.model.events.Event;
 import net.carmgate.morph.model.events.TakeDamage;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class BalancedAI {
 
 	private class DamageTaken {
 		protected float takingDamageAmount;
 		protected long takingDamageTsForDps = 0;
 		protected float takingDamageDps;
 	}
 
 	private final static Logger LOGGER = LoggerFactory.getLogger(BalancedAI.class);
 
 	private Map<Entity, DamageTaken> damage = new HashMap<>();
 	private Entity worstEnemy;
 	private Ship ship;
 	private float runTotalDpsTaken;
 	private long lastTsOfDamageTaken;
 
 	private boolean fleeing;
 
 	public BalancedAI(Ship ship) {
 		this.ship = ship;
 	}
 
 	public BalancedAI cloneForShip(Ship ship) {
 		BalancedAI balancedAI = new BalancedAI(ship);
 		return balancedAI;
 	}
 
 	public void handleEvent(Event event) {
 		if (event instanceof TakeDamage) {
 			LOGGER.debug("Caught event : " + ((TakeDamage) event).getDamageAmount());
 
 			Entity entity = ((TakeDamage) event).getSourceOfDamage();
 			DamageTaken dt = damage.get(entity);
 			if (dt == null) {
 				dt = new DamageTaken();
 				damage.put(entity, dt);
 			}
 
 			dt.takingDamageAmount += ((TakeDamage) event).getDamageAmount();
 
 			lastTsOfDamageTaken = Model.getModel().getCurrentTS();
 		}
 	}
 
 	private void preRun() {
 		float maxDps = 0;
 		worstEnemy = null;
 		for (Entry<Entity, DamageTaken> entry : damage.entrySet()) {
 			// Compute dps taken by Ship as a whole
 			if (Model.getModel().getCurrentTS() - entry.getValue().takingDamageTsForDps > 1000
 					&& entry.getValue().takingDamageAmount > 0) {
 				entry.getValue().takingDamageDps = entry.getValue().takingDamageAmount;
 				LOGGER.debug("taking dps: " + entry.getValue().takingDamageDps);
 				entry.getValue().takingDamageAmount = 0;
 				entry.getValue().takingDamageTsForDps = Model.getModel().getCurrentTS();
 			}
 
 			if (Model.getModel().getCurrentTS() - entry.getValue().takingDamageTsForDps > 2000) {
 				LOGGER.debug("No damage for last 2s");
 				entry.getValue().takingDamageAmount = 0;
 				entry.getValue().takingDamageDps = 0;
 			}
 
 			runTotalDpsTaken += entry.getValue().takingDamageDps;
 			if (entry.getValue().takingDamageDps > maxDps) {
 				worstEnemy = entry.getKey();
 			}
 		}
 	}
 
 	private void reset() {
 		// TODO we should get back to original plan instead of just stopping everything
 		damage.clear();
 		runTotalDpsTaken = 0;
 		worstEnemy = null;
 	}
 
 	public void run() {
 		preRun();
 
 		// If dps is too high to win the battle : flee
 		if (runTotalDpsTaken > ship.getMaxDpsInflictable()) {
 			LOGGER.debug("If I do not flee, the battle will end in a loss or a draw (" + runTotalDpsTaken + "/"
 					+ ship.getMaxDpsInflictable() + ")");
 			if (!fleeing) {
 				LOGGER.debug("Run, Forest, run !!");
 				ship.removeBehaviorsByClass(Movement.class);
 				ship.addBehavior(new Flee(ship, worstEnemy));
 				fleeing = true;
 			}
 		}
 
 		// if we are fleeing but have not taken any damage for more than 2s, the break and reset AI
 		if (fleeing && Model.getModel().getCurrentTS() - lastTsOfDamageTaken > 3000) {
 			LOGGER.debug("Now, we're safe ... no damage for more than 3s");
 			ship.removeBehaviorsByClass(Flee.class);
 			ship.addBehavior(new Break(ship));
 			fleeing = false;
 			reset();
 		} else {
 			LOGGER.debug(fleeing + " - " + (Model.getModel().getCurrentTS() - lastTsOfDamageTaken) + " - "
 					+ ship.hasBehaviorByClass(Flee.class) + " - " + ship.hasBehaviorByClass(Break.class));
 		}
 
 	}
 }
