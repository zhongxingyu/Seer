 /**
  * 
  */
 package core;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.newdawn.slick.AppGameContainer;
 import org.newdawn.slick.BasicGame;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.geom.Shape;
 
 /**
  * @author a8011484
  *
  */
 public class Game extends BasicGame {
 	Ship ship = new Ship();
 	List<Bullet> bullets = new ArrayList<Bullet>();
 
 	public Game() {
 		super("Asteroids");
 	}
 
 	@Override
 	public void render(GameContainer container, Graphics g)
 			throws SlickException {
 		
 		for (Entity entity : Entity.entities) {
 			g.draw((Shape) entity.getDrawable());
 		}
 	}
 
 	@Override
 	public void init(GameContainer container) throws SlickException {
 		
 	}
 
 	@Override
 	public void update(GameContainer container, int delta)
 			throws SlickException {
 		Input input = container.getInput();
 
 		ship.update(input.getMouseX(), input.getMouseY(), delta);
 		
 		if (input.isMouseButtonDown(Input.MOUSE_LEFT_BUTTON)) {
 			ship.thrust();
 		}
 		
 		if (input.isMousePressed(Input.MOUSE_RIGHT_BUTTON)) {
 			bullets.add(new Bullet(ship.getAngle(), ship.getX(), ship.getY()));
 		}
 		
 		Iterator<Bullet> iterator = bullets.iterator();
 		while (iterator.hasNext()) {
 			Bullet bullet = iterator.next();
 			
 			bullet.update(delta);	
 			
 			if (bullet.getX() > container.getWidth() 
 					|| bullet.getX() < 0 
 					|| bullet.getY() > container.getHeight() 
 					|| bullet.getY() < 0 ) {
 				iterator.remove();
 			}
 		}
 		
 		if (ship.getX() > container.getWidth()) {
 			ship.setX(0);
 		}
 		
 		if (ship.getX() < 0) {
 			ship.setX(container.getWidth());
 		}
 		
 		if (ship.getY() > container.getHeight()) {
 			ship.setY(0);
 		}
 		
 		if (ship.getY() < 0) {
 			ship.setY(container.getHeight());
 		}
 
 	}
 
 
 	public static void main(String[] args) {
 		try { 
 		    AppGameContainer container = new AppGameContainer(new Game()); 
 		    container.setDisplayMode(800,600,false); 
 		    container.setVSync(true);
 		    container.start();
 		} catch (SlickException e) { 
 		    e.printStackTrace(); 
 		}
 
 	}
 
 }
