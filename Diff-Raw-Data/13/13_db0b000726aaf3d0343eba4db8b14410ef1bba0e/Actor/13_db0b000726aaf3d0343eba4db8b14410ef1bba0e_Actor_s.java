 package petri.api;
 
 import java.awt.Graphics;
 import java.awt.Point;
 import java.awt.Polygon;
 import java.awt.geom.Point2D;
 import java.awt.geom.Point2D.Float;
 
 /**
  * The base class for all things that both move and paint. Offers variables for
  * position, size, velocity, health, direction facing, and max health. Offers
  * methods for healing, damage, drawing, moving, collision checking, and death.
  * 
  * @author Cody Swendrowski
  * 
  */
 public abstract class Actor {
 
 	protected boolean death, collision;
 	protected Point2D.Float center, size, vectVel; // speed in pixels/s
 	protected Polygon basePoly;
 	protected GameEngine engine;
 	protected int health, dir, maxHealth;
 
 	/**
 	 * Creates a new Actor.
 	 * 
 	 * @param e
 	 *            GameEngine to utilize
 	 */
 	public Actor(GameEngine e) {
 		vectVel = new Point2D.Float(0, 0);
 		center = new Point2D.Float(0, 0);
 		size = new Point2D.Float(0, 0);
 		death = false;
 		collision = false;
 		engine = e;
 		basePoly = new Polygon();
 		health = 0;
 		dir = 0;
 		maxHealth = 0;
 	}
 
 	/**
 	 * Returns center of Actor in a Point2D.Float format.
 	 * 
 	 * @return center
 	 */
 	public Point2D.Float getCenter() {
 		return center;
 	}
 
 	/**
 	 * Returns velocity of Actor in a Point2D.Float format.
 	 * 
 	 * @return vectVel
 	 */
 	public Point2D.Float getVelocity() {
 		return vectVel;
 	}
 	
 	public Point2D.Float getSize() {
 		return size;
 	}
 
 	/**
 	 * Draws the Actor.
 	 * 
 	 * @param g
 	 *            Graphics to draw with
 	 */
 	public abstract void draw(Graphics g);
 
 	/**
 	 * Moves the Actor.
 	 * 
 	 * @param ms
 	 *            Time since last call in Milliseconds
 	 */
 	public void move(int ms) {
 		setCenter(center.x + (ms / 100F) * vectVel.x, center.y + (ms / 100F)
 				* vectVel.y);
 	}
 
 	/**
 	 * Returns true if Actor is dead; else, returns false.
 	 * 
 	 * @return death
 	 */
 	public boolean isDead() {
 		return death;
 	}
 
 	/**
 	 * Returns the amount of health an Actor has, in int form
 	 * 
 	 * @return health
 	 */
 	public int getHealth() {
 		return health;
 	}
 
 	/**
 	 * Deals damage to the Actor. If Actor health is less than or equal to 0,
 	 * sets Actor to dead.
 	 * 
 	 * @param damage
 	 *            Amount of damage to deal to Actor.
 	 */
 	public void dealDamage(int damage) {
 		health -= damage;
 		if (health <= 0)
 			death = true;
 	}
 
 	/**
 	 * Heals the Actor up to max health amount.
 	 * 
 	 * @param healAmt
 	 *            Max amount to heal by
 	 */
 	public void heal(int healAmt) {
 		health += healAmt;
 		if (health >= maxHealth)
 			health = maxHealth;
 	}
 	
 	/**
 	 * Sets the basePoly, which is the default shape of the Actor, to a new
 	 * Polygon.
 	 * 
 	 * @param poly
 	 *            Polygon to set to
 	 */
 	public void setBasePoly(Polygon poly) {
 		basePoly = poly;
 	}
 
 	/**
 	 * Checks to see if Actor is colliding with another Actor. This method
 	 * checks to see if either of the Polygons that contain collision points are
 	 * intersecting, and if they are, sets boolean collision to true.
 	 * 
 	 * @param a
 	 *            Actor to check collision against
 	 */
 	public void checkCollision(Actor a) {
 		if (a.equals(this))
 			return;
 		
 		Polygon otherPoly = a.basePoly;
 		int distance = 1;
 		
 		if (otherPoly != null && basePoly != null) {
			
 			//Calculate distance using the formula x^2 + y^2 = z^2
 			int x = otherPoly.getBounds().x - basePoly
 					.getBounds().x;
 			int y = otherPoly.getBounds().y - basePoly.getBounds().y;
 			distance = (int) Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
 		}
 		if (distance > 150 || a instanceof Particle)
 			return;

 		for (int i = 0; i < basePoly.npoints; i++) {
 			if (otherPoly.contains(new Point(basePoly.xpoints[i],
 					basePoly.ypoints[i]))) {
 				collision = true;
 				a.collision = true;
 			}
 		}
 	}
 
 	/**
 	 * Sets the center of the Actor.
 	 * 
 	 * @param x
 	 *            X co-ordinate of center
 	 * @param y
 	 *            Y co-ordinate of center
 	 */
 	public void setCenter(float x, float y) {
 		basePoly.translate((int) -center.x, (int) -center.y);
 		center = new Point2D.Float(x, y);
 		basePoly.translate((int) center.x, (int) center.y);
 	}
 
 	/**
 	 * Sets the death boolean of Actor.
 	 * 
 	 * @param d
 	 *            boolean to set to
 	 */
 	public void setDeath(boolean d) {
 		death = d;
 	}
 
 	/**
 	 * Returns true IFF checkCollision has detected a collision.
 	 * 
 	 * @return collision
 	 */
 	public boolean isColliding() {
 		return collision;
 	}
 
 	/**
 	 * Sets boolean collision to false. Used to allow Actor to check for a new
 	 * collision.
 	 */
 	public void clearCollision() {
 		collision = false;
 	}
 
 	/**
 	 * Allows System to print name of object. Returns the name of the Actor
 	 * 
 	 * @return "Actor"
 	 */
 	public String toString() {
 		return "Actor";
 	}
 
 }
