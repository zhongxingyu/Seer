 package game;
 import gameManager.GameManager;
 
 import java.util.ArrayList;
 import java.util.Hashtable;
 
 import objectManager.GameObject;
 
 import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
 
 import utilityManager.MathBox;
 
 import brickManager.SystemBrick;
 
 public class TestWeapon extends SubSystem {
 	private double fireCount;
 	private double fireIncrement;
 
 	private double lifeDecrement;
 	
	private double maxRange;
	
 	private ArrayList<TestProjectile> projectiles = new ArrayList<TestProjectile>();
 	
 	public TestWeapon(GameObject source, SystemBrick systemBrick, Vector2D position, double orientation, double fireIncrement, double lifeDecrement) {
 		super(source, systemBrick, SubSystemType.weapon, position, orientation);
 		this.source = source;
 		this.fireIncrement = fireIncrement;
 		this.lifeDecrement = lifeDecrement;
		
		this.maxRange = 5 * (1/lifeDecrement);
 	}
 
 	private void fire() {
 		Hashtable<String, Double> projValues = new Hashtable<String, Double>();
 		
 		projValues.put("mass", 5.0);
 		projValues.put("maxVelocity", 5.0);
 		projValues.put("maxTurningVelocity", 0.01);
 		projValues.put("maxForce", 0.5);
 		projValues.put("maxTurningForce", 100.0);
 		
 		projValues.put("forceX", Math.cos(orientation));
 		projValues.put("forceY", Math.sin(orientation));
 		projValues.put("forceMagnitude", 1.0);
 		
 		projValues.put("turningForce", 0.0);
 		projValues.put("velocityX", 0.0);
 		projValues.put("velocityY", 0.0);
 		projValues.put("turningVelocity", 0.0);
 		
 		projValues.put("damage", 1.0);
 		
 		projValues.put("positionX", position.getX() + MathBox.rotatePoint(systemBrick.getPosition(), orientation).getX());
 		projValues.put("positionY", position.getY() + MathBox.rotatePoint(systemBrick.getPosition(), orientation).getY());
 		projValues.put("orientation", orientation);
 		
 		TestProjectile newProjectile = new TestProjectile(source, projValues, lifeDecrement);
 		newProjectile.addObserver(GameManager.getObjectManager()); //TODO don't really like this static call to get the objectmanager
 		projectiles.add(newProjectile);
 	}
 
 	public void update() {
 		if (!systemBrick.isAlive()) {
 			alive = false;
 			return;
 		}
 		
 		if (fireCount <= 0.0 && activated) {
 			fire();
 			fireCount = 1.0;
 		}
 		else {
 			fireCount -= fireIncrement;
 		}
 	}
 	
 	@Override
 	public void updateView() {
 	}
 	
	public boolean isInRange(Vector2D target) {
		if (position.add(systemBrick.getPosition()).distance(target) <= maxRange) {
			return true;
		}
		return false;
	}
	
 	public ArrayList<TestProjectile> getProjectiles() {
 		return projectiles;
 	}
 
 	public void clearProjectiles() {
 		projectiles.clear();
 	}
 	
 	public void setPosition(Vector2D position) {
 		this.position = position;
 	}
 
 	public void setOrientation(double orientation) {
 		this.orientation = orientation;
 	}
 
 	public void setVelocity(Vector2D velocity) {
 	}

	public double getMaxRange() {
		return maxRange;
	}
 }
