 package myPack;
 
 import java.util.Vector;
 
 public abstract class Agent {
 
 	public boolean lives;
 	public String name;
 	public Coordinate position;
 	public Policy pi;
 
 	public Agent(String name, Coordinate p, Policy pi) {
 		super();
 		this.lives = true;
 		this.name = name;
 		this.position = p;
 		this.pi = pi;
 	}
 
 	public boolean isAlive() {
 		return this.lives;
 	}
 
 	public boolean isDead() {
 		return !this.lives;
 	}
 
 	public void kill() {
 		this.lives = false;
 	}
 
 	public void setPolicy(Policy pi) {
 		this.pi = pi;
 	}
 
 	public Policy getPolicy() {
 		return this.pi;
 	}
 
 	/*
 	 * public Agent doAction(Vector<Agent> worldState) { return
 	 * this.pi.getOptimalAction(this, worldState, possibleActions); }
 	 */
 	public abstract Coordinate doAction(Vector<Agent> worldState);
 
 	public abstract boolean safePosition(Coordinate c, Vector<Agent> worldState);
 
 	public abstract String print();
 
 	public Vector<Coordinate> possibleActions(Agent a, Vector<Agent> worldState) {
 
 		Vector<Coordinate> PossiblePosition = new Vector<Coordinate>();
 		PossiblePosition.add(this.position.getNorth());
 		PossiblePosition.add(this.position.getEast());
 		PossiblePosition.add(this.position.getWest());
 		PossiblePosition.add(this.position.getSouth());
 		PossiblePosition.add(this.position);
 
 		for (int i = 0; i < PossiblePosition.size(); i++) {
 			if (!this.safePosition(PossiblePosition.elementAt(i), worldState)) {
				PossiblePosition.removeElementAt(i);
 			}
 		}
		return PossiblePosition;
 	}
 
 }
