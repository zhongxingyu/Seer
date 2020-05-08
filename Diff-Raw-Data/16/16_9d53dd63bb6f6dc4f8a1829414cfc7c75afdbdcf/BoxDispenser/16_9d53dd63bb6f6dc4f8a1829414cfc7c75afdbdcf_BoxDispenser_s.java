 package fi.ringofsnake.util;
 
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.SlickException;
import org.newdawn.slick.util.Log;
 
 import fi.ringofsnake.entities.Box;
import fi.ringofsnake.entities.SnakeMap;
 
 /**
  * Creates boxes for the player to try to bypass
  * @author Yaamboo
  *
  */
 public class BoxDispenser {
 	
 	private static final int[] BOXLEVELS = {200, 300, 400};
 	
 	private List<Box> boxes = new LinkedList<Box>();
 	
	private SnakeMap map;
	
	public BoxDispenser(SnakeMap map) {
		this.map = map;
 	}
 	
 	private void createBox() {
 		// randomize and stuff
 		// let's forget about y-velocity right now
 		Box box = new Box(1280 + (int)(Math.random() * 1000), BOXLEVELS[(int)(Math.random()* 3)], (float)(Math.random() * -10) - 5, 0);
 		//Log.debug("Creating new box at " + box.position.x + "," + box.position.y);
 		boxes.add(box);
 	}
 	
 	public void update(GameContainer cont, int delta) throws SlickException {
 		
 		if (Math.random() > 0.99)
 			createBox();
 		
 		for (Box box : boxes)
 			box.update(cont, delta);
 	}
 	
 	public void render(GameContainer cont, Graphics g) throws SlickException {
 		HashSet<Box> toRemove = new HashSet<Box>();
 		for (Iterator<Box> i = boxes.iterator(); i.hasNext();) {
 			Box box = i.next();
 			if ( box.position.x < -100)
 			{
 				//Log.debug("Destroying box at " + box.position.x);
 				toRemove.add(box);
 			}
 			else
 				box.render(cont, g);
 		}
 		boxes.removeAll(toRemove);
 	}
 	
 }
