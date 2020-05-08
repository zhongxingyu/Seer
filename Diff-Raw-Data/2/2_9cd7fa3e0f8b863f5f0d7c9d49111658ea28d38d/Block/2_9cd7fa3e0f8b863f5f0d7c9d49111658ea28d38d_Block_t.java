 package games;
 
 import java.util.Random;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 public class Block extends Image{
 	Image sprite = null;
 	Random r = new Random();
 	int xVel;
 	int yVel;
 	int x;
 	int y;
 	
 	Block() throws SlickException{
		sprite = new Image("resources/plane.png");
 		xVel = r.nextInt(10);
 		yVel = r.nextInt(10);
 		
 		x = r.nextInt(640);
 		y = r.nextInt(480);
 	}
 
 }
