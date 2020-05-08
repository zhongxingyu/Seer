 import org.newdawn.slick.AppGameContainer;
 import org.newdawn.slick.BasicGame;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
  
 public class SimpleGame extends BasicGame
 {	
 	private Background background = null;
 	private Clouds clouds = null;
 	private Bird bird;
 	//private Enemy enemy;
 	private EntityManager entityManager;
 	private EntityQueueHandler entityQueueHandler;
 	private Score score;
 	
     public SimpleGame()
     {
         super("Bird Poop!");
     }
  
     @Override
     public void init(GameContainer gc) 
 			throws SlickException 
 	{
     	score = new Score();
     	background = new Background(IMAGES.BACKGROUND, IMAGES.BACKGROUND);
     	
     	Image[] ibird = {IMAGES.BIRD_WHITE_1, IMAGES.BIRD_WHITE_2, IMAGES.BIRD_WHITE_3,
     			IMAGES.BIRD_WHITE_4, IMAGES.BIRD_WHITE_5 };
     	bird = new Bird(ibird);
     	
     	entityManager = new EntityManager(bird);
     	entityQueueHandler = new EntityQueueHandler(entityManager);
     }
  
     @Override
     public void update(GameContainer gc, int delta) 
 			throws SlickException     
     {
     	Input input = gc.getInput();
     	
     	if(input.isKeyPressed(Input.KEY_H)){
     		entityManager.addEntity(Human.getRandomlyPlacedHuman());
     	}
     	if(input.isKeyPressed(Input.KEY_E)){
     		entityManager.addEntity(Enemy.getRandomlyPlacedEnemy(bird));
     	}
     	if(input.isKeyPressed(Input.KEY_C)){
     		entityManager.addEntity(Car.getRandomlyPlacedCar());
     	}
     	if(input.isKeyPressed(Input.KEY_T)){
     		entityManager.addEntity(Tree.getRandomlyPlacedTree());
     	}
     	if(input.isKeyPressed(Input.KEY_P)){
     		entityQueueHandler.increaseDelay();
     		System.out.println("Increased Queue delay: " + entityQueueHandler.getDelay());
     	}
     	if(input.isKeyPressed(Input.KEY_O)){
     		entityQueueHandler.decreaseDelay();
     		System.out.println("Decreased Queue delay: " + entityQueueHandler.getDelay());
     	}
     	
     	background.update(delta);
     	clouds.update(delta);
     	
     	entityQueueHandler.update();
     	entityManager.update(input, delta);
     	score.update();
     }
     
     @Override
     public void render(GameContainer gc, Graphics g) 
 			throws SlickException 
     {
     	background.draw();
     	clouds.draw();
     	entityManager.draw();
     	
     	gc.getGraphics().drawString(score.getScorePrintable(), GLOBAL.SCREEN_WIDTH - 75, 0);
     }
  
     /**
      * Entry point
      * @param args
      * @throws SlickException
      */
     public static void main(String[] args) 
 			throws SlickException
     {    	
          AppGameContainer app = new AppGameContainer(new SimpleGame());
  
          app.setDisplayMode(GLOBAL.SCREEN_WIDTH, GLOBAL.SCREEN_HEIGHT, false);
          app.setTargetFrameRate(60);
          app.start();
     }
 }
