 package indaprojekt;
 
 import java.awt.geom.Rectangle2D;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import org.newdawn.slick.Color;
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
 	private UserInterface ui;
 	private Input input;
 	private Image background;
 	
     public Game()
     {
         super("Awesome Game");
     }
     
     /**
      * Sets up and adds all the entities of the map to the game
      */
     private void setupEntities(GameContainer container) throws SlickException
     {	
 		PlayerControls player1Controls = new PlayerControls(
 										Input.KEY_W,
 										Input.KEY_A,
 										Input.KEY_S,
 										Input.KEY_D,
 										Input.KEY_LCONTROL,
 										Input.KEY_LSHIFT);
 		
 		Color transp = Color.white;
 		Map<Direction, Animation> animMap1 = new HashMap<Direction, Animation>();
		animMap1.put(Direction.UP, new Animation(new Image[]{new Image("res//images//up1.png",transp),new Image("res//images//up2.png",transp) }, 1));
 		animMap1.put(Direction.UPRIGHT, new Animation(new Image[]{new Image("res//images//up1.png",transp)}, 1));
		animMap1.put(Direction.RIGHT, new Animation(new Image[]{new Image("res//images//right1.png",transp),new Image("res//images//right2.png",transp)}, 1));
 		animMap1.put(Direction.DOWNRIGHT, new Animation(new Image[]{new Image("res//images//down1.png",transp)}, 1));
		animMap1.put(Direction.DOWN, new Animation(new Image[]{new Image("res//images//down1.png",transp), new Image("res//images//down2.png",transp)}, 1));
 		animMap1.put(Direction.DOWNLEFT, new Animation(new Image[]{new Image("res//images//up1.png",transp)}, 1));
		animMap1.put(Direction.LEFT, new Animation(new Image[]{new Image("res//images//left1.png",transp), new Image("res//images//left2.png",transp)}, 1));
 		animMap1.put(Direction.UPLEFT, new Animation(new Image[]{new Image("res//images//up1.png",transp)}, 1));
 		Rectangle2D.Float player1HitBox = new Rectangle2D.Float(0f, 0f, 48, 48);
 		Player p1 = new Player(50, 50, player1Controls, player1HitBox, animMap1);
 		players.add(p1);
 		entities.add(p1);
 		
 		//TEMP
 		PlayerControls player2Controls = new PlayerControls(
 						Input.KEY_UP,
 						Input.KEY_LEFT,
 						Input.KEY_DOWN,
 						Input.KEY_RIGHT,
 						Input.KEY_L,
 						Input.KEY_K);
 		Map<Direction, Animation> animMap2 = new HashMap<Direction, Animation>();
		animMap2.put(Direction.UP, new Animation(new Image[]{new Image("res//images//up1.png",transp),new Image("res//images//up2.png",transp) }, 1));
 		animMap2.put(Direction.UPRIGHT, new Animation(new Image[]{new Image("res//images//up1.png",transp)}, 1));
		animMap2.put(Direction.RIGHT, new Animation(new Image[]{new Image("res//images//right1.png",transp),new Image("res//images//right2.png",transp)}, 1));
 		animMap2.put(Direction.DOWNRIGHT, new Animation(new Image[]{new Image("res//images//down1.png",transp)}, 1));
		animMap2.put(Direction.DOWN, new Animation(new Image[]{new Image("res//images//down1.png",transp), new Image("res//images//down2.png",transp)}, 1));
 		animMap2.put(Direction.DOWNLEFT, new Animation(new Image[]{new Image("res//images//up1.png",transp)}, 1));
		animMap2.put(Direction.LEFT, new Animation(new Image[]{new Image("res//images//left1.png",transp), new Image("res//images//left2.png",transp)}, 1));
 		animMap2.put(Direction.UPLEFT, new Animation(new Image[]{new Image("res//images//up1.png",transp)}, 1));
 		Rectangle2D.Float player2HitBox = new Rectangle2D.Float(0f, 0f, 48, 48);
 		Player p2 = new Player(150, 150, player2Controls, player2HitBox, animMap2);
 		players.add(p2);
 		entities.add(p2);
 		background = new Image("res//images//bakgrund.png");
 		
 		Image obstacleImage = new Image("res//images//isbit.png");
 		Obstacle obstacle = new Obstacle(100, 100, new Rectangle2D.Float(0, 0, obstacleImage.getWidth(), obstacleImage.getHeight()), 
 		new Animation(new Image[]{obstacleImage}, 1));
 		obstacles.add(obstacle);
 		entities.add(obstacle);
 		
 		int w = container.getWidth();
 		int h = container.getHeight();
 				
 		Image iceCube = new Image("res//images//isbit.png");
 		int cubeW = iceCube.getWidth();
 		int cubeH = iceCube.getHeight();
 		for (int i = 0; i < w; i += cubeW) {
 			Obstacle cube = new Obstacle(i, 0, new Rectangle2D.Float(0, 0, cubeW, cubeH), 
 											   new Animation(new Image[]{obstacleImage}, 1));
 			obstacles.add(cube);
 			entities.add(cube);
 		}
 		for (int i = 0; i < h; i += cubeH) {
 			Obstacle cube = new Obstacle(0, i, new Rectangle2D.Float(0, 0, cubeW, cubeH), 
 											   new Animation(new Image[]{obstacleImage}, 1));
 			obstacles.add(cube);
 			entities.add(cube);
 		}
 		for (int i = 0; i < w; i += cubeW) {
 			Obstacle cube = new Obstacle(i, h - cubeH, new Rectangle2D.Float(0, 0, cubeW, cubeH), 
 							new Animation(new Image[]{obstacleImage}, 1));
 			obstacles.add(cube);
 			entities.add(cube);
 		}
 		for (int i = 0; i < h; i += cubeW) {
 			Obstacle cube = new Obstacle(w - cubeW, i, 
 							new Rectangle2D.Float(0, 0, cubeW, cubeH), new Animation(new Image[]{obstacleImage}, 1));
 			obstacles.add(cube);
 			entities.add(cube);
 		}
 		
 		Image itemImage = new Image("res//images//bomb2.png");
 		Item item = new Item(itemImage, 250, 250, new Rectangle2D.Float(0, 0, 25, 25));
 		entities.add(item);
 		
 		Image spdUpImage = new Image("res//images//Speed.png");
 		SpeedUp spdUp = new SpeedUp(spdUpImage, 300, 300, new Rectangle2D.Float(0, 0, 25, 25), 2, 4000);
 		entities.add(spdUp);
     }
  
     @Override
     public void init(GameContainer gc) throws SlickException 
     {
     	players = new ArrayList<Player>(2);
     	projectiles = new LinkedList<Projectile>();
     	obstacles = new ArrayList<Obstacle>();
     	entities = new LinkedList<Entity>();
     	
     	Image lifeImage = new Image("res//images//BlueHeart.png");
     	Image noLifeImage = new Image("res//images//brokenHeart.png");
     	ui = new UserInterface(0, 0, gc.getWidth() - 250, gc.getHeight() - 50, lifeImage, noLifeImage, 5, 5);
     	
     	setupEntities(gc);
     	
     	input = gc.getInput();
     }
  
     @Override
     public void update(GameContainer gc, int delta) throws SlickException     
     {
     	for (Entity entity : entities) {
     		entity.doLogic(input, delta);
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
     	
     	int length = (int)Math.ceil(entities.size()/2);
     	int i = 0;
     	for (Entity entity : entities) {
     		i++;
     		if (i > length) {
     			break;
     		}
     		for (Entity entity2 : entities) {
     			if (entity != entity2) {
     				if (entity.isCollision(entity2)) {
     					entity.handleCollision(entity2);
     					entity2.handleCollision(entity);
     				}
     			}
     		}
     	}
     	
     	{
     		Entity spawn = null;
 	    	Iterator<Entity> iterator = entities.iterator();
 	    	while (iterator.hasNext()) {
 	    		Entity entity = iterator.next();
 	    		spawn = entity.getEntity();
 	    		if (entity.shouldBeRemoved()) {
 	    			iterator.remove();
 	    		}
 	    	}
 //    		if (spawn != null) {
 //    			entities.add(spawn);
 //    		}
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
     	
     	//TEMP, makes the game crash when amount of players is not 2
     	ui.setPlayer1Lives(players.get(0).getLives());
     	
     	if (players.size() >= 2) {
     		ui.setPlayer2Lives(players.get(1).getLives());
     	}
     }
  
     @Override
     public void render(GameContainer gc, Graphics g) throws SlickException 
     {
     	background.draw();
     	
     	for (Entity entity : entities) {
     		entity.draw();
     	}
     	
     	ui.draw();
     }
  
     public static void main(String[] args) throws SlickException
     {
          AppGameContainer app = new AppGameContainer(new Game());
          app.setDisplayMode(1000, 600, false);
          app.setVSync(true);
          app.setFullscreen(false);
          app.start();
     }	
 }
