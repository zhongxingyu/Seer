 package hu.miracle.workers;
 
 import java.awt.Point;
 
 public class Ant extends Creature {
 
 	// Members
 	private Storage home;
 	private Storage source;
 	private boolean poisoned;
 	private int health;
 	private int cargo;
 
 	// Constructor
 	public Ant(Storage home, Storage source, boolean poisoned, int health, Scene scene) {
 		super(scene);
 		this.home = home;
 		this.source = source;
 		this.poisoned = poisoned;
 		this.health = health;
 		this.cargo = 0;
 	}
 
 	// Protected methods
 	protected void routeAndMove() {
 		System.out.println(getClass().getCanonicalName() + ".routeAndMove()");
 
 		for (Storage storage : scene.getStorages()) {
 			
 			// VAJON EZ IGY MUKODOKEPES?? mi csak storageken megyunk vegig
 			if (storage instanceof FoodStorage) {
 				
 				if (pointInRange(storage.getPosition())) {
 					cargo = storage.getItems();
 					source = storage;
 				}		
 			}
 		}
 	}
 
 	// Public interface
 	public void setPoisoned(boolean poisoned) {
 		System.out.println(getClass().getCanonicalName() + ".setPoisoned()");
 
 		this.poisoned = poisoned;
 	}
 
 	public void handleTick() {
 		System.out.println(getClass().getCanonicalName() + ".handleTick()");
 
 		if (poisoned) {
 			if (health > 0) {
 				health--;
 			}
 		}
 
 		routeAndMove();
 	}
 
 	@Override
 	public void terminate() {
 		System.out.println(getClass().getCanonicalName() + ".terminate()");
 
 		health = 0;
 	}
 
 	public void setSource(Storage storage) {
 		System.out.println(getClass().getCanonicalName() + ".setSource()");
 
 	}
 
 	public boolean isDead() {
 		if (health > 0) {
 			return false;
 		} else {
 			return true;
 		}
 	}
 
 	@Override
 	public boolean pointInRange(Point point) {
 
 		if (Point.distance(this.position.x, this.position.y, point.x, point.y) < this.radius) {
 			return true;
 		} else {
 			return false;
 		}
 
 	}
 
 }
