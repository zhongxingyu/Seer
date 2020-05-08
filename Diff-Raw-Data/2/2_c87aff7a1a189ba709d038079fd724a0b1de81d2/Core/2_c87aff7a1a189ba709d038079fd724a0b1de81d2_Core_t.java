 package trollhoehle.gamejam.magnets;
 
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.geom.Circle;
 
 /**
  * The core is the .. core! .. in the middle of the game. This class has extra
  * private methods for awesome special effects like a pulsing-effect or the
  * spawning of Obstacles.
  * 
  * @author Cakemix
  * 
  */
 public class Core extends Entity {
 
     public Core(float screenWidth, float screenHeight) throws SlickException {
	super(screenWidth / 2 - 25, screenHeight / 2 - 25, new Circle(screenWidth / 2 - 25, screenHeight / 2 - 25,
 		50), new Image("res/images/core.png"));
     }
 
     @Override
     public Obstacle[] update(float timePerFrame, float toCenterX, float toCenterY, float attract) {
 	Obstacle spawnedObstacles[];
 
 	this.pulse(timePerFrame);
 
 	spawnedObstacles = this.spawn(timePerFrame);
 
 	return spawnedObstacles;
     }
 
     private Obstacle[] spawn(float timePerFrame) {
 	Obstacle spawnedObstacles[] = new Obstacle[0];
 	// TODO maaaaaake 'em spaaaaaawn!
 	return spawnedObstacles;
     }
 
     private void pulse(float timePerFrame) {
 	// TODO maaaaake iiiiit puuuuuuuulse! :O
 
     }
 
 }
