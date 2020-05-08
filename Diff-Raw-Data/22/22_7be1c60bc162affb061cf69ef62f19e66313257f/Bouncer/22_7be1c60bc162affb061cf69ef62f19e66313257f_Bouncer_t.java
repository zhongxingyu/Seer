 package sampleAgents;
 
 import java.awt.Color;
 
 import edu.wheaton.simulator.datastructure.ElementAlreadyContainedException;
 import edu.wheaton.simulator.entity.Prototype;
 import edu.wheaton.simulator.entity.Trigger;
 import edu.wheaton.simulator.expression.Expression;
 /**
  * Bouncer is a prototype for an agent that will begin in one direction and when it reaches
  * any kind of obstacle, it will reveres that direction.
  * 
  * @author David Emmanuel
  *
  */
 public class Bouncer extends SampleAgent{
 	
 	public Bouncer(){
 		super.sampleAgents.add(this);
 	}
 	
 	@Override
 	public Prototype initSampleAgent() {
 		Prototype bouncer = new Prototype(Color.RED, "bouncer");
 		return initBouncer(bouncer);
 	}
 	
 	/**
 	 * Adds all information to make a bouncer agent and saves the bouncer agent.
 	 * @param bouncer An empty prototype to be converted into a bouncer
 	 * @return The initialized bouncer
 	 */
 	private static Prototype initBouncer(Prototype bouncer){
 		// Add fields
 		try {
 			bouncer.addField("xNextDirection", 0 + "");
 			bouncer.addField("yNextDirection", 1 + "");
 		} catch (ElementAlreadyContainedException e) {
 			e.printStackTrace();
 		}
 		
 		// openSpot conditionals
 		Expression freeSpot = new Expression("isSlotOpen(this.x + this.xNextDirection, this.y + this.yNextDirection)");
 		Expression notFreeSpot = new Expression("!isSlotOpen(this.x + this.xNextDirection, this.y + this.yNextDirection)");
 
 		// Move behavior
 		Expression move = new Expression("move(this.x + this.xNextDirection, this.y + this.yNextDirection)");
 		
 		// bounce
 		Expression bounce = new Expression("setField('xNextDirection', -1 * this.xNextDirection) ||" +
 				"setField('yNextDirection', -1 * this.yNextDirection)");
 
 		bouncer.addTrigger(new Trigger("bounce", 1, notFreeSpot, bounce));
 		bouncer.addTrigger(new Trigger("move", 1, freeSpot, move));
 		
 		Prototype.addPrototype(bouncer);
 		// save the prototype
 		//Simulator.getInstance().savePrototypeToFile(bouncer);
 		return bouncer;
 	}
 
 }
