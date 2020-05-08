 package trollhoehle.gamejam.magnets;
 
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.geom.Circle;
 
 /**
  * This class is a rotating ring used as boundary for the game.
  * 
  * @author Cakemix
  * 
  */
 public class Ring extends Entity {
 
     public Ring(float posX, float posY, float radius) throws SlickException {
	super(posX, posY, new Circle(posX, posY, radius - 20), new Image("res/images/testRing.png"));
     }
 
     public Obstacle[] update(float timePerFrame, float toCenterX, float toCenterY, float attract) {
 	// TODO: ROTAAAAAAATEEEEEEEEEEE! :O
 	return null;
     }
 
     @Override
     public Obstacle[] collision(Entity collider) {
 	// TODO Auto-generated method stub
 	return null;
     }
 
 }
