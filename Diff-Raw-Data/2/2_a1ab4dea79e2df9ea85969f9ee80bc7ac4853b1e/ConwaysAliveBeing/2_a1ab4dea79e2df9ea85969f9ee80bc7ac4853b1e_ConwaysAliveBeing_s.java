 package sampleAgents;
 
 import java.awt.Color;
 
 import edu.wheaton.simulator.datastructure.ElementAlreadyContainedException;
 import edu.wheaton.simulator.entity.Prototype;
 import edu.wheaton.simulator.entity.Trigger;
 import edu.wheaton.simulator.expression.Expression;
 
 /**
  *  Makes an alive being from Conway's Game of Life
  *  
  * @author Elliot Penson, Emmanuel Pederson
  *
  */
 public class ConwaysAliveBeing extends SampleAgent{
 
 	public ConwaysAliveBeing(){
 		super.sampleAgents.add(this);
 	}
 	
 	@Override
 	public Prototype initSampleAgent() {
 		Prototype aliveBeing = new Prototype(new Color(93, 198, 245), "aliveBeing");
 		return initAliveBeing(aliveBeing);
 	}
 	
 	private static Prototype initAliveBeing(Prototype aliveBeing){
 		// Add fields
 				try {
 					aliveBeing.addField("alive", 1 + ""); // 0 for false, 1 for true
 					aliveBeing.addField("age", 1 + "");
 					aliveBeing.addField("neighbors", 0 + "");
 				} catch (ElementAlreadyContainedException e) {
 					e.printStackTrace();
 				}
 				
 				// Set up conditionals
 				Expression isAlive = new Expression("this.alive == 1");
 				Expression neigh1 = new Expression(
						"getFieldOfAgentAt ( this.x - 1 , this.y - 1 , 'alive' ) == 1");
 				Expression neigh2 = new Expression(
 						"getFieldOfAgentAt(this.x, this.y-1, 'alive') == 1");
 				Expression neigh3 = new Expression(
 						"getFieldOfAgentAt(this.x+1, this.y-1, 'alive') == 1");
 				Expression neigh4 = new Expression(
 						"getFieldOfAgentAt(this.x-1, this.y, 'alive') == 1");
 				Expression neigh5 = new Expression(
 						"getFieldOfAgentAt(this.x+1, this.y, 'alive') == 1");
 				Expression neigh6 = new Expression(
 						"getFieldOfAgentAt(this.x-1, this.y+1, 'alive') == 1");
 				Expression neigh7 = new Expression(
 						"getFieldOfAgentAt(this.x, this.y+1, 'alive') == 1");
 				Expression neigh8 = new Expression(
 						"getFieldOfAgentAt(this.x+1, this.y+1, 'alive') == 1");
 				Expression dieCond = new Expression(
 						"(this.alive == 1) && (this.neighbors < 2 || this.neighbors > 3)");
 
 				// Set up behaviors
 				Expression incrementAge = new Expression(
 						"setField('age', this.age+1)");
 				Expression incrementNeighbors = new Expression(
 						"setField('neighbors', this.neighbors+1)");
 				Expression die = new Expression(
 						"clonePrototype(this.x, this.y, 'deadBeing')");
 				Expression resetNeighbors = new Expression(
 						"setField('neighbors', 0)");
 
 				// Add triggers
 				aliveBeing.addTrigger(new Trigger("updateAge", 1, isAlive,
 						incrementAge));
 				aliveBeing.addTrigger(new Trigger("resetNeighbors", 2, new Expression(
 						"true"), resetNeighbors));
 				aliveBeing.addTrigger(new Trigger("checkNeigh1", 3, neigh1,
 						incrementNeighbors));
 				aliveBeing.addTrigger(new Trigger("checkNeigh2", 4, neigh2,
 						incrementNeighbors));
 				aliveBeing.addTrigger(new Trigger("checkNeigh3", 5, neigh3,
 						incrementNeighbors));
 				aliveBeing.addTrigger(new Trigger("checkNeigh4", 6, neigh4,
 						incrementNeighbors));
 				aliveBeing.addTrigger(new Trigger("checkNeigh5", 7, neigh5,
 						incrementNeighbors));
 				aliveBeing.addTrigger(new Trigger("checkNeigh6", 8, neigh6,
 						incrementNeighbors));
 				aliveBeing.addTrigger(new Trigger("checkNeigh7", 9, neigh7,
 						incrementNeighbors));
 				aliveBeing.addTrigger(new Trigger("checkNeigh8", 10, neigh8,
 						incrementNeighbors));
 				aliveBeing.addTrigger(new Trigger("die", 11, dieCond, die));
 
 				// Add the prototype to the static list of Prototypes
 				Prototype.addPrototype(aliveBeing);
 				
 				return aliveBeing;
 	}
 }
