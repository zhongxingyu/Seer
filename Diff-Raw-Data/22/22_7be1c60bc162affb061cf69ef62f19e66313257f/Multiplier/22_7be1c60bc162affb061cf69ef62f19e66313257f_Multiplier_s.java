 package sampleAgents;
 
 import java.awt.Color;
 
 import edu.wheaton.simulator.datastructure.ElementAlreadyContainedException;
 import edu.wheaton.simulator.entity.Prototype;
 import edu.wheaton.simulator.entity.Trigger;
 import edu.wheaton.simulator.expression.Expression;
import edu.wheaton.simulator.simulation.Simulator;
 
 /**
  * Multiplier is the prototype for an agent that will clone itself into any 
  * open adjacent spot and will die after 5 time steps.
  * 
  * @author Elliot Penson, Emmanuel Pederson
  *
  */
 public class Multiplier extends SampleAgent{
 
 	public Multiplier() {
 		super.sampleAgents.add(this);
 	}
 
 	@Override
 	public Prototype initSampleAgent() {
 		Prototype multiplier = new Prototype(Color.BLUE, "multiplier");
 		return initMultiplier(multiplier);
 	}
 	
 	/**
 	 * Creates a new multiplier (sample Prototype) and adds it to the static list of Prototypes.
 	 */
 	private static Prototype initMultiplier(Prototype multiplier) {
 		// Add fields
 		try {
 			multiplier.addField("age", 0 + "");
 		} catch (ElementAlreadyContainedException e) {
 			e.printStackTrace();
 		}
 
 		// Set up conditionals
 		Expression tooOld = new Expression("this.age > 5");
 		Expression emptyNeighbor1 = new Expression("isSlotOpen(this.x, this.y + 1)");
 		Expression emptyNeighbor2 = new Expression("isSlotOpen(this.x + 1, this.y + 1)");
 		Expression emptyNeighbor3 = new Expression("isSlotOpen(this.x + 1, this.y)");
 		Expression emptyNeighbor4 = new Expression("isSlotOpen(this.x + 1, this.y - 1)");
 		Expression emptyNeighbor5 = new Expression("isSlotOpen(this.x, this.y - 1)");
 		Expression emptyNeighbor6 = new Expression("isSlotOpen(this.x - 1, this.y - 1)");
 		Expression emptyNeighbor7 = new Expression("isSlotOpen(this.x - 1, this.y)");
 		Expression emptyNeighbor8 = new Expression("isSlotOpen(this.x - 1, this.y + 1)");
 
 		// Set up behaviors
 		Expression incrementAge = new Expression("setField('age', this.age + 1)");
 		Expression die = new Expression ("die()");
 		Expression cloneToEmpty1 =  new Expression("clone(this.x, this.y + 1)");
 		Expression cloneToEmpty2 =  new Expression("clone(this.x + 1, this.y + 1)");
 		Expression cloneToEmpty3 =  new Expression("clone(this.x + 1, this.y)");
 		Expression cloneToEmpty4 =  new Expression("clone(this.x + 1, this.y - 1)");
 		Expression cloneToEmpty5 =  new Expression("clone(this.x, this.y - 1)");
 		Expression cloneToEmpty6 =  new Expression("clone(this.x - 1, this.y - 1)");
 		Expression cloneToEmpty7 =  new Expression("clone(this.x - 1, this.y)");
 		Expression cloneToEmpty8 =  new Expression("clone(this.x - 1, this.y + 1)");
 
 		// Add triggers
 		multiplier.addTrigger(new Trigger("updateAge", 1, new Expression("true"), incrementAge));
 		multiplier.addTrigger(new Trigger("die", 1, tooOld, die));
 		multiplier.addTrigger(new Trigger("clone1", 1, emptyNeighbor1, cloneToEmpty1));
 		multiplier.addTrigger(new Trigger("clone2", 1, emptyNeighbor2, cloneToEmpty2));
 		multiplier.addTrigger(new Trigger("clone3", 1, emptyNeighbor3, cloneToEmpty3));
 		multiplier.addTrigger(new Trigger("clone4", 1, emptyNeighbor4, cloneToEmpty4));
 		multiplier.addTrigger(new Trigger("clone5", 1, emptyNeighbor5, cloneToEmpty5));
 		multiplier.addTrigger(new Trigger("clone6", 1, emptyNeighbor6, cloneToEmpty6));
 		multiplier.addTrigger(new Trigger("clone7", 1, emptyNeighbor7, cloneToEmpty7));
 		multiplier.addTrigger(new Trigger("clone8", 1, emptyNeighbor8, cloneToEmpty8));
 		
 		Prototype.addPrototype(multiplier);
 
 		return multiplier;
 	}
 }
