 package dungeonCrawler.GameElements;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.util.EnumSet;
 
 import dungeonCrawler.ElementType;
 import dungeonCrawler.EventType;
 import dungeonCrawler.GameElement;
 import dungeonCrawler.GameEvent;
 import dungeonCrawler.GameLogic;
 import dungeonCrawler.Vector2d;
 
 /**
  * @author Dominik
  *
  */
 public class Enemy extends GameElement {
 	private int Health=100;
 	private int lives=0;
 
 	/**
 	 * @param position
 	 * @param size
 	 */
 	public Enemy(Vector2d position, Vector2d size) {
 		super(position, size, "ENEMY", EnumSet.of(ElementType.MOVABLE));
 	}
 
 	@Override
 	public void draw(Graphics g) {
 		// TODO Auto-generated method stub
 		g.setColor(Color.PINK);
 		g.fillRect(0, 0, size.getX(), size.getY());
 	}
 	
 	public void setPosition(Vector2d pos) {
 		this.position = pos;
 	}
 
 	@Override
 	public void GameEventPerformed(GameEvent e) {
 		// TODO Auto-generated method stub
 		if(e.element instanceof Player && e.type == EventType.COLLISION){
 			System.out.println("autsch!");
 			Player elementPlayer = (Player) e.element;
 			elementPlayer.reduceHealth(10, e.gameLogic);
 		}
 		if(e.type == EventType.TIMER){
 			e.gameLogic.moveElement(this, new Vector2d((int)(Math.random()*4-2),(int)(Math.random()*4-2)));
 		}
 	}	
 	
	public void setHealt(int Health) {
 		this.Health = Health;
 	}
 	
	public void increaseHealt(int Health) {
 		this.Health += Health;
 	}
 	
 	public void reduceHealth(int Health, GameLogic logic) {
 		if (this.Health-Health > 0){
 			this.Health = this.Health-Health;
 			System.out.println("Enemy lost " + Health + " and has now " + this.Health + " Health");
 		}
 		else {
 			lives--;
 			if(lives<0){
 				this.Health -= Health;
 				this.size = new Vector2d(0,0);
 				System.out.println("Enemy dead");
 			} else {
 				this.Health -= Health;
 				System.out.println("Enemy lost " + Health + " and has now " + this.Health + " Health");
 				this.Health = 100;
 			}
 		}
 	}
 	
	public int getHealt() {
 		return this.Health;
 	}
 
 }
 
 
