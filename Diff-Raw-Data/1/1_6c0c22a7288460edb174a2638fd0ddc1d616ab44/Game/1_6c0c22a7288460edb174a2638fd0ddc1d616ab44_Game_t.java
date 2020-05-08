 package indaprojekt;
 
 import java.awt.geom.Rectangle2D;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import org.newdawn.slick.Animation;
 import org.newdawn.slick.AppGameContainer;
 import org.newdawn.slick.BasicGame;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 
 /**
  * Describes the actual game.
  */
 public class Game extends BasicGame
 {
 	private List<Player> players;
 	private List<Projectile> projectiles;
 	private List<Obstacle> obstacles;
 	private List<Entity> entities;
 	private Input input;
 	private Image background;
 	
     public Game()
     {
         super("Awesome Game");
     }
  
     @Override
     public void init(GameContainer gc) throws SlickException 
     {
     	players = new ArrayList<Player>(2);
     	projectiles = new LinkedList<Projectile>();
     	obstacles = new ArrayList<Obstacle>();
     	entities = new LinkedList<Entity>();
     	
     	//TEMP.
     	PlayerControls player1Controls = new PlayerControls(
 											Input.KEY_W,
 											Input.KEY_A,
 											Input.KEY_S,
 											Input.KEY_D,
 											Input.KEY_LCONTROL);
     	Image player1Image = new Image("res//player.png");
     	Map<Direction, Animation> animMap1 = new HashMap<Direction, Animation>();
     	animMap1.put(Direction.UP, new Animation(new Image[]{player1Image}, 1));
     	animMap1.put(Direction.UPRIGHT, new Animation(new Image[]{player1Image}, 1));
     	animMap1.put(Direction.RIGHT, new Animation(new Image[]{player1Image}, 1));
     	animMap1.put(Direction.DOWNRIGHT, new Animation(new Image[]{player1Image}, 1));
     	animMap1.put(Direction.DOWN, new Animation(new Image[]{player1Image}, 1));
     	animMap1.put(Direction.DOWNLEFT, new Animation(new Image[]{player1Image}, 1));
     	animMap1.put(Direction.LEFT, new Animation(new Image[]{player1Image}, 1));
     	animMap1.put(Direction.UPLEFT, new Animation(new Image[]{player1Image}, 1));
     	Rectangle2D.Float player1HitBox = new Rectangle2D.Float(0f, 0f, 
     						player1Image.getWidth()/10f, player1Image.getHeight()/10f);
     	Player p1 = new Player(50, 50, player1Controls, player1HitBox, animMap1);
     	players.add(p1);
     	entities.add(p1);
     	
     	
     	//TEMP
     	PlayerControls player2Controls = new PlayerControls(
     										Input.KEY_UP,
     										Input.KEY_LEFT,
     										Input.KEY_DOWN,
     										Input.KEY_RIGHT,
     										Input.KEY_L);
     	Image player2Image = new Image("res//player2.png");
     	Map<Direction, Animation> animMap2 = new HashMap<Direction, Animation>();
     	animMap2.put(Direction.UP, new Animation(new Image[]{player2Image}, 1));
     	animMap2.put(Direction.UPRIGHT, new Animation(new Image[]{player2Image}, 1));
     	animMap2.put(Direction.RIGHT, new Animation(new Image[]{player2Image}, 1));
     	animMap2.put(Direction.DOWNRIGHT, new Animation(new Image[]{player2Image}, 1));
     	animMap2.put(Direction.DOWN, new Animation(new Image[]{player1Image}, 1));
     	animMap2.put(Direction.DOWNLEFT, new Animation(new Image[]{player2Image}, 1));
     	animMap2.put(Direction.LEFT, new Animation(new Image[]{player2Image}, 1));
     	animMap2.put(Direction.UPLEFT, new Animation(new Image[]{player2Image}, 1));
     	Rectangle2D.Float player2HitBox = new Rectangle2D.Float(0f, 0f, 
 				player2Image.getWidth()/10f, player2Image.getHeight()/10f);
     	Player p2 = new Player(150, 150, player2Controls, player2HitBox, animMap2);
     	players.add(p2);
     	entities.add(p2);
     	background = new Image("res//classroom.jpg");
     	
     	Image obstacleImage = new Image("res//bomb.png");
     	Obstacle obstacle = new Obstacle(100, 100, new Rectangle2D.Float(0, 0, obstacleImage.getWidth(), obstacleImage.getHeight()), 
     			new Animation(new Image[]{obstacleImage}, 1));
     	obstacles.add(obstacle);
     	entities.add(obstacle);
     	
     	Obstacle leftWall = new Obstacle(0, 0, new Rectangle2D.Float(0, 0, 50, 600), new Animation(new Image[]{obstacleImage}, 1));
     	Obstacle rightWall = new Obstacle(750, 0, new Rectangle2D.Float(0, 0, 50, 600), new Animation(new Image[]{obstacleImage}, 1));
     	Obstacle bottomWall = new Obstacle(0, 550, new Rectangle2D.Float(0, 0, 800, 50), new Animation(new Image[]{obstacleImage}, 1));
     	Obstacle topWall = new Obstacle(0, 0, new Rectangle2D.Float(0, 0, 800, 50), new Animation(new Image[]{obstacleImage}, 1));
     	obstacles.add(leftWall);
     	entities.add(leftWall);
     	obstacles.add(rightWall);
     	entities.add(rightWall);
     	obstacles.add(bottomWall);
     	entities.add(bottomWall);
     	obstacles.add(topWall);
     	entities.add(topWall);
     	
     	
     	input = gc.getInput();
     }
  
     @Override
     public void update(GameContainer gc, int delta) throws SlickException     
     {
     	for (Player player : players) {
     		player.doLogic(input, delta);
     	} 
     	
     	{
 	    	Iterator<Player> iterator = players.iterator();
 	    	while (iterator.hasNext()) {
 	    		Player player = iterator.next();
 	    		if (player.isDead()) {
 	    			iterator.remove();
	    			entities.remove(player);
 	    		}
 	    	}
     	}
     	
     	//in the future, only go through half of list
     	for (Entity entity : entities) {
     		for (Entity entity2 : entities) {
     			if (entity != entity2) {
     				if (entity.isCollision(entity2)) {
     					entity.handleCollision(entity2);
     					entity2.handleCollision(entity);
     				}
     			}
     		}
     	}
     	
     	for (Projectile proj : projectiles) {
     		proj.doLogic(delta);
     	}
     	
     	{
 	    	Iterator<Projectile> iterator = projectiles.iterator();
 	    	while (iterator.hasNext()) {
 	    		Projectile proj = iterator.next();
 	    		if (proj.shouldBeRemoved()) {
 	    			iterator.remove();
 	    		}
 	    	}
     	}
     	
     	//for each player, if the player have thrown a projectile,
     	//add it to the projectile list
     	for (Player player : players) {
     		Projectile proj = player.getProjectile();
     		if (proj != null) {
     			projectiles.add(proj);
     			entities.add(proj);
     		}
     	}
     }
  
     @Override
     public void render(GameContainer gc, Graphics g) throws SlickException 
     {
     	background.draw();	
     	for (Player player : players) {
     		player.draw();
     	}
     	for (Projectile proj : projectiles) {
     		proj.draw();
     	}
     	for (Obstacle obs : obstacles) {
     		obs.draw();
     	}
     }
  
     public static void main(String[] args) throws SlickException
     {
          AppGameContainer app = new AppGameContainer(new Game());
          app.setDisplayMode(800, 600, false);
          app.setVSync(true);
          app.start();
     }	
 }
