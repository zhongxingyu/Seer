 package trollhoehle.gamejam.magnets;
 
 import org.newdawn.slick.Image;
 import org.newdawn.slick.geom.Shape;
 
 /**
  * Still pretty basic class, extending the {@link Entity}. New:
  * {@link PhysicalEntity}s have a specified amount of hitpoints and a
  * speed-value with which they can move through the game.
  * 
  * @author Cakemix
  * 
  */
 public abstract class PhysicalEntity extends Entity {
 
     private int hp;
     private float speed;
 
     public PhysicalEntity(float posX, float posY, Shape shape, Image img, int hp, float startSpeed) {
 	super(posX, posY, shape, img);
 	this.hp = hp;
 	this.speed = startSpeed;
     }
 
     /**
      * Returns the amount of health this PhysicalEntity has.
      * 
      * @return
      */
     public int getHp() {
 	return this.hp;
     }
 
     public float getSpeed() {
 	return this.speed;
     }
     
     public float getPolarPhi() {
     	float transX = this.getCenterX() - 360;	// HARDCODE
     	float transY = 360 - this.getCenterY();	// HARDCODE
     	return (float) Math.atan2(transY,transX);
     }
     public float getPolarRadius() {
     	float transX = this.getCenterX() - 360;	// HARDCODE
     	float transY = 360 - this.getCenterY();	// HARDCODE
     	return (float) Math.sqrt(Math.pow(transX, 2) + Math.pow(transY, 2));
     }
 
     /**
      * Sets the health of this PhysicalEntity to a given value.
      * 
      * @param hp
      */
     public void setHp(int hp) {
 	this.hp = hp;
     }
 
     public void setSpeed(float speed) {
 	this.speed = speed;
     }
 
     public Obstacle[] update(float timePerFrame, float toCenterX, float toCenterY, float attract) {
 	// calculate new position
 	this.calculateCircularMovement(timePerFrame, toCenterX, toCenterY, attract);
 
 	return null;
     }
 
     /**
      * Updates the this PhysicalEntitie's position assuming a circular movement.
      * 
      * 
      * @param timePerFrame
      *            The time between the next and the last frame.
      * @param toCenterX
      *            The absolute distance to the center of the screen on the
      *            x-axis.
      * @param toCenterY
      *            The absolute distance to the center of the screen on the
      *            y-axis.
      * @param attract
      *            The strength with which the core attracts objects.
      */
    protected void calculateCircularMovement(float timePerFrame, float toCenterX, float toCenterY, float attract) {
 	// from Cartesian to Radial
 	float phi = this.getPolarPhi();
 	float radius = this.getPolarRadius();
 
 	// calculate new values
 	phi += speed * timePerFrame / radius;
 
 	// constantly increase radius, except attract != 0
 	radius += (0.08 - attract) * timePerFrame;
 
 	// back from Radial to Cartesian and save
 
 	this.setCenterX((float) (toCenterX + Math.cos(phi) * radius));
 	this.setCenterY((float) (toCenterY - Math.sin(phi) * radius));
     }
 
     /**
      * Every PhysicalEntity will lose 1 hp when colliding with another Entity.
      */
     public Obstacle[] collision(Entity collider) {
 	this.setHp(this.getHp() - 1);
 
 	return null;
     }
 }
