 /**
  * 
  */
 package entities.enemies;
 
 import org.newdawn.slick.Image;
 
 import ai.Goomba;
 
 /**
  * @author Mullings
  *
  */
 public class Ent extends Enemy {
 	private static final float WIDTH = 64;
 	private static final float HEIGHT = 64;
 	private static final float GROUND = 6;
 	private static final int RUNSPEED = 2;
 	private static final int JMPSPEED = 2;
 	private static final int MAXHP = 1;
 
 	
 	
 	public Ent(float x, float y) {
 		super(x, y, WIDTH, HEIGHT, GROUND, RUNSPEED, JMPSPEED, MAXHP, new Goomba());
 		try {
			setImage(new Image("assets/images/ent1.png"));
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 }
