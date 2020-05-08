 package engine;
 
 import java.awt.geom.Point2D;
 
 import playable.Player;
 import dispatcher.Dispatcher;
 
 /**
  * This class represents a unit (a cluster of agents) for the engine (no display for this class).
  * @author Yuki
  *
  */
 public class Unit extends Element {
 	private Element goal;
 	private Element start;
 	private int moveSpeed = 2;
 	private Player owner;
 	
 	private Point2D.Float direction;
 	
 	public Unit(double nbAgents, Base start, Element goal){
 		super((int)start.getCenter().x, (int)start.getCenter().y, nbAgents);
 		
 		this.owner = start.getOwner();
 		this.start = start;
 		this.goal = goal;
 
 		Point2D.Float startingPosition = start.getCenter();
 		Point2D.Float endingPosition = goal.getCenter();
		this.direction = new Point2D.Float(endingPosition.x - startingPosition.x, endingPosition.y - startingPosition.y);
		float normDirection = (float) this.direction.distance(0, 0);
		this.direction.x /= normDirection;
		this.direction.y /= normDirection;
 	}
 	
 	/**
 	 * Move the unit along his direction vector.
 	 */
 	public void move(){
 		this.position.setLocation(this.position.x + (this.direction.x * this.moveSpeed), 
 				this.position.y + (this.direction.y * this.moveSpeed));
 	}
 	
 	/**
 	 * Called when the unit is at his destination. Resolves the situation (depend on the owner of the base).
 	 * @return Boolean - true if the unit have to be delete, else false
 	 */
 	public boolean resolveAttack(){
 		//if the destination is a base
 		if(this.goal.getClass() == Base.class){
 			Base tmpGoal = (Base) this.goal;
 			if(tmpGoal.getOwner() != this.owner){
 				if(this.nbAgents <= this.goal.getNbAgents()){
 					tmpGoal.reduceNbAgents(this.nbAgents);
 				}
 				else if(this.nbAgents == this.goal.getNbAgents()){
 					tmpGoal.reduceNbAgents(this.nbAgents);
 					tmpGoal.setOwner(Dispatcher.getPlayers().get("Neutral"));
 				}
 				else{
 					tmpGoal.reduceNbAgents(this.nbAgents);
 					tmpGoal.setOwner(this.owner);
 					tmpGoal.makeTheChangeOfCamp();
 					for(Tower t:Dispatcher.getEngine().getTowerAround(tmpGoal))
 						t.destroyTower();
 				}
 			}
 			else{
 				tmpGoal.increaseNbAgents(this.nbAgents);
 			}
 		}
 		//if the destination is a tower
 		else if(this.goal.getClass() == Tower.class){
 			Tower tmpGoal = (Tower) this.goal;
 			//Send back a unit if the target tower is full
 			int sendBackAgents = tmpGoal.addNbAgents(this.nbAgents);
 			if(sendBackAgents != -1){
 				System.out.println(sendBackAgents);
 				this.nbAgents = sendBackAgents;
 				Element tmpElt = this.start;
 				this.start = this.goal;
 				this.goal = tmpElt;
 				this.direction.x *= -1;
 				this.direction.y *= -1;
 				return false;
 			}
 		}
 		
 		return true;
 	}
 	
 	/**
 	 * Return if the unit is at his destination (a base or a tower) or not.
 	 */
 	public boolean atDestination(){
 		if(this.position.distance(this.goal.getCenter()) < 10)
 			return true;
 		return false;
 	}
 
 	// GETTERS & SETTERS
 	
 	public Element getGoal() {
 		return goal;
 	}
 	
 	public Player getOwner() {
 		return owner;
 	}
 
 	public Point2D.Float getDirection() {
 		return direction;
 	}
 
 	@Override
 	public Point2D.Float getCenter() {
 		return null;
 	}
 }
