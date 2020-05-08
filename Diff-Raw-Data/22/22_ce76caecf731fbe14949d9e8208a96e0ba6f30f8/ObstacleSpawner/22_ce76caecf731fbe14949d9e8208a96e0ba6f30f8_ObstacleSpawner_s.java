 package trollhoehle.gamejam.magnets;
 
 import org.newdawn.slick.geom.Rectangle;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 
 /**
  * This class is used to spawn Obstacles. It is a PhysicalEntity since it move
  * in circular motions around the core.
  * 
  * @author Cakemix
  */
 public class ObstacleSpawner extends PhysicalEntity {
 
     private static final float OBSTACLE_SPEED = 0.1f;
     /**
      * ms-value.
      */
     private float timeBetweenSpawns;
     private float timeFromLastSpawn;
 
     public ObstacleSpawner(float posX, float posY, float startSpeed, float timeBetweenSpawns) throws SlickException {
 	super(posX, posY, new Rectangle(posX, posY, 10, 5), new Image("res/images/gun.png"), -1, startSpeed);
 	this.timeBetweenSpawns = timeBetweenSpawns;
     }
 
     public Obstacle[] update(float timePerFrame, float toCenterX, float toCenterY, float attract) {
 	super.update(timePerFrame, toCenterX, toCenterY, attract);
 
 	Obstacle[] spawnedObstacles = null;
 
 	this.getImg().setRotation(-(float) (180 * this.getPolarPhi() / Math.PI));
 
 	this.timeFromLastSpawn += timePerFrame;
 	if (timeFromLastSpawn >= timeBetweenSpawns) {
 	    timeFromLastSpawn = 0;
 	    spawnedObstacles = new Obstacle[1];
 	    try {
 		spawnedObstacles[0] = new Obstacle(this.getCenterX(), this.getCenterY(), 1, OBSTACLE_SPEED);
 	    } catch (SlickException e) {
 		// TODO Auto-generated catch block
 		e.printStackTrace();
 	    }
 	}
 
 	return spawnedObstacles;
     }
 
 }
