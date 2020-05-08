 package agentsPack;
 
 import java.util.Map;
 import java.util.Vector;
 
<<<<<<< HEAD
 //import sun.reflect.generics.reflectiveObjects.NotImplementedException;
=======
>>>>>>> Prey
 
 import actionPack.RandomAction;
 import environmentPack.Coordinate;
 
 public class Prey extends Agent {
 
 	public Prey(String name, Coordinate p, Policy pi) {
 		super(name, p, pi);
 		// TODO Auto-generated constructor stub
 	}
 
 
 
 	@Override
 	public Coordinate doAction(Vector<Agent> worldState) {
 		if (this.isDead()) {
 			return this.position;
 		}
 		// TODO Auto-generated method stub
 		Vector<RandomAction> actions = new Vector<RandomAction>();
 		actions.addElement(new RandomAction(0.05, this.position.getNorth()));
 		actions.addElement(new RandomAction(0.05, this.position.getEast()));
 		actions.addElement(new RandomAction(0.05, this.position.getWest()));
 		actions.addElement(new RandomAction(0.05, this.position.getSouth()));
 		actions.addElement(new RandomAction(0.8, this.position));
 
 		double prob = Math.random();
 		double boundary = 0.0;
 		Coordinate NewPosition = this.position;
 
 		for (int i = 0; i < actions.size(); i++) {
 			boundary += actions.get(i).prob;
 			if (prob <= boundary) {
 				if (this.safePosition(actions.get(i).coordinate, worldState)) {
 
 					NewPosition = actions.get(i).coordinate;
 					break;
 				} else {
 					this.doAction(worldState);
 					break;
 				}
 			}
 		}
 
 		return NewPosition;
 
 	}
 	
<<<<<<< HEAD
 //	@Override
 //	public Vector<Agent> typicalState(int stateIndex) {
 //		throw new NotImplementedException();
 //	}
=======
	@Override
	public Vector<Agent> typicalState(int stateIndex) {
		return null;
		}
>>>>>>> Prey
 
 	@Override
 	public boolean safePosition(Coordinate c, Vector<Agent> worldState) {
 
 		for (int i = 0; i < worldState.size(); i++) {
 			Agent a = worldState.elementAt(i);
 			if (a instanceof Predator
 					&& Coordinate.compareCoordinates(c, a.position)) {
 				return false;
 			} else if (a instanceof Prey
 					&& Coordinate.compareCoordinates(c, a.position) && a.lives) {
 				return false;
 			}
 		}
 
 		return true;
 	}
 
 	@Override
 	public String toString() {
 		return "Prey(\"" + this.name + "\", " + this.position + ">)";
 	}
 
 	@Override
 	public Integer stateIndex(Vector<Agent> worldState) {
 		// TODO Auto-generated method stub
 		Agent predator = null;
 
 		for (Agent a : worldState) {
 			if (a instanceof Predator) {
 				predator = a;
 			}
 		}
 		Coordinate c = Coordinate.difference(predator.position, this.position);
 		return (c.getX() * 11) + c.getY();
 	}
 
 	@Override
 	public Double reward(Vector<Agent> currState, Vector<Agent> nextState, Coordinate action) {
 		// TODO Auto-generated method stub
 		return 0.0;
 	}
 
 	@Override
 	public Map<Integer, Double> functionP(Vector<Agent> worldState) {
 		// TODO Auto-generated method stub
 		throw new UnsupportedOperationException("No P function for prey");
 	}
 
 
 
 	@Override
 	public Vector<Agent> typicalState(int stateIndex) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 }
