 package dungeonCrawler.GameElements;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.util.EnumSet;
 
 import dungeonCrawler.ElementType;
 import dungeonCrawler.EventType;
 import dungeonCrawler.GameElement;
 import dungeonCrawler.GameEvent;
 import dungeonCrawler.Vector2d;
 
 /**
  * @author Dominik
  *
  */
 public class Healthpot extends GameElement {
 
 	/**
 	 * @param position
 	 * @param size
 	 */
 	public Healthpot(Vector2d position, Vector2d size) {
 		super(position, size, "HEALTHPOT", EnumSet.of(ElementType.IMMOVABLE, ElementType.WALKABLE));
 	}
 
 	@Override
 	public void draw(Graphics g) {
 		// TODO Auto-generated method stub
 		g.setColor(Color.MAGENTA);
 		g.fillRect(0, 0, size.getX(), size.getY());
 	}
 
 	@Override
 	public void GameEventPerformed(GameEvent e) {
 		// TODO Auto-generated method stub
 		if(e.element instanceof Player && e.type == EventType.COLLISION){
 			System.out.println("Heil mich");
 			Player elementPlayer = (Player) e.element;
			elementPlayer.increaseHealt(100);
 			this.size.setX(0);this.size.setY(0);
 		}
 	}
 
 }
