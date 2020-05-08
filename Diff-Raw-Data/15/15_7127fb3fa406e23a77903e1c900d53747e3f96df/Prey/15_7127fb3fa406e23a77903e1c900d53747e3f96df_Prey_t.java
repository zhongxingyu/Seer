 package myPack;
 
 import java.util.Vector;
 
 public class Prey extends Agent {
 
 	public class RandomAction {
 		public double prob;
 		public Coordinate coordinate;
 
 		public RandomAction(double p, Coordinate c) {
 			this.prob = p;
 			this.coordinate = c;
 		}
 	}
 
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
 
 	@Override
 	public boolean safePosition(Coordinate c, Vector<Agent> worldState) {
 
 		for (int i = 0; i < worldState.size(); i++) {
 			Agent a = worldState.elementAt(i);
 			if (a != this && Coordinate.compareCoordinates(c, a.position)) {
 				return false;
 			}
 		}
 
 		return true;
 	}
 
 	@Override
 	public String print() {
 		return "Prey(\"" + this.name + ", <" + this.position.getX() + ", "
 				+ this.position.getY() + ">)";
 	}
 
 }
