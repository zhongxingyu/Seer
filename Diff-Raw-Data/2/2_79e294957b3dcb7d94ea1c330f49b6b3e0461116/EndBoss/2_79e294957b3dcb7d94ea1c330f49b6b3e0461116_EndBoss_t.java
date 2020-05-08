 package dungeonCrawler.GameElements;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.io.File;
 import java.io.IOException;
 import java.util.EnumSet;
 
 import javax.imageio.ImageIO;
 
 import dungeonCrawler.DamageType;
 import dungeonCrawler.ElementType;
 import dungeonCrawler.EventType;
 import dungeonCrawler.GameElement;
 import dungeonCrawler.GameElementImage;
 import dungeonCrawler.GameEvent;
 import dungeonCrawler.GameLogic;
 import dungeonCrawler.LevelLoader;
 import dungeonCrawler.Quest;
 import dungeonCrawler.Vector2d;
 
 /**
  * 
  * @author Dominik, Hucke
  *
  */
 public class EndBoss extends GameElement {
 	static EndBoss element;
 	GameElementImage gei = new GameElementImage();
 	private int health=1000;
 	private int lives=0;
 
 
 	/**
 	 * @param position
 	 * @param size
 	 */
 	public EndBoss(Vector2d position, Vector2d size, int id) {
 		super(position, size, id);
 		this.type = EnumSet.of(ElementType.MOVABLE);
 		gei.setSize(getSize());
 		try {
 			gei.setImage(ImageIO.read(new File("Graphics" + File.separator + "enemy.png")));
 		} catch (IOException e) {
 			gei.setImage(null);
 			e.printStackTrace();
 		}
 
 	}
 
 	@Override
 	public void draw(Graphics g) {
 		// TODO Auto-generated method stub
 		//gei.paintComponent(g);
 		g.setColor(Color.ORANGE);
 		g.fillRect(0, 0, size.getX(), size.getY());
 	}
 
 	public void setPosition(Vector2d pos) {
 		this.position = pos;
 	}
 
 
 	public void GameEventPerformed(GameEvent e) {
 
 		// TODO Auto-generated method stub
 		if(e.element instanceof Player && e.type == EventType.COLLISION){
 			System.out.println("autsch!");
 			Player elementPlayer = (Player) e.element;
 			elementPlayer.reduceHealth(25, DamageType.CONVENTIONAL, e.gameLogic);
 		}
 		if(e.type == EventType.TIMER){
			e.gameLogic.moveElement(this,(new Vector2d((int)(Math.random()*4-2),(int)(Math.random()*4-2))).mul(2));
 		}
 	}	
 
 	public void setHealth(int health) {
 		this.health = health;
 	}
 
 	public void setLives(int lives) {
 		this.lives = lives;
 	}
 
 	public void increaseHealth(int health) {
 		this.health += health;
 	}
 
 	public void reduceHealth(int health, DamageType damage, GameLogic logic) {
 		if (this.health-health > 0){
 			this.health = this.health-health;
 			System.out.println("EndBoss lost " + health + " and has now " + this.health + " Health");
 
 		}
 		else {
 			lives--;
 			if(lives<0){
 				this.health -= health;
 				this.size = new Vector2d(0,0);
 				System.out.println("EndBoss dead");
 				Quest.killedEndBoss();
 			} else {
 				this.health -= health;
 				System.out.println("EndBoss lost " + health + " and has now " + this.health + " Health");
 				this.health = 100;
 			}
 		}
 	}
 
 	public int getHealth() {
 		return this.health;
 	}
 
 	/**Creates new instance of this class.
 	 * @param param parameters of this GameElement as {@link String[]}
 	 * @param id as {@link int}
 	 * @return a {@link EndBoss} instance
 	 */
 	public static EndBoss createElement(String[] param, int id) {
 		if (param.length > 5) {
 			element = new EndBoss(new Vector2d(), new Vector2d(), Integer.parseInt(param[1]));
 		}
 		else {
 			element = new EndBoss(new Vector2d(), new Vector2d(), id);
 		}
 		element.modify(param);
 		return element;
 	}
 
 	/**Modifies parameters.
 	 * @param param as {@link String[]}
 	 */
 	public void modify(String[] param) {
 		Vector2d position = new Vector2d();
 		Vector2d size = new Vector2d();
 		try {
 			int i = (param.length > 5 ? 1 : 0);
 			position.setX(Integer.parseInt(param[i+1]));
 			position.setY(Integer.parseInt(param[i+2]));
 			size.setX(Integer.parseInt(param[i+3]));
 			size.setY(Integer.parseInt(param[i+4]));
 			element.setPosition(position);
 			element.setSize(size);
 			if (param.length > 6) {
 				element.setLives(Integer.parseInt(param[i+5]));
 				element.setHealth(Integer.parseInt(param[i+6]));
 			}
 			element.gei.setSize(size);
 		} catch (NumberFormatException e) {
 			System.out.println("Kann ENDBOSS-Parameter nicht interpretieren.");
 			element = null;
 		}
 	}
 
 	/**Gets a parameter string.
 	 * @see dungeonCrawler.GameElement#getString()
 	 */
 	@Override
 	public String getString() {
 		String sep = LevelLoader.getSplitChar();
 		return (getName() + sep + id + sep +
 				position.getX() + sep + position.getY() + sep +
 				size.getX() + sep + size.getY());
 	}
 
 
 
 	@Override
 	public String getName(){
 		return "ENDBOSS";
 	}
 
 }
