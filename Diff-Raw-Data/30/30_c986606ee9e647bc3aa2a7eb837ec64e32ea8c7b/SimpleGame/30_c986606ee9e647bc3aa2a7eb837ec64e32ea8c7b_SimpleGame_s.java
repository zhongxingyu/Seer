 import org.newdawn.slick.AppGameContainer;
 import org.newdawn.slick.BasicGame;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
  
 public class SimpleGame extends BasicGame
 {
 	@SuppressWarnings("unused")
 	private GameContainer game = null;
 	private Menu menu = null;
 	private Gameover gameover = null;
 	private Background background = null;
 	private Clouds clouds = null;
 	private Sun sun = null;
 	private Eggs eggs = null;
 	private Impact impact = null;
 	private Bird bird;
 	private Health birdHealth;
 	private EntityManager entityManager;
 	private EntityQueueHandler entityQueueHandler;
 	private Score score;
 	private boolean inMenu = true;
 	private boolean exitGame;
 	
     public SimpleGame()
     {
         super("Bird Poop!");
     }
 
     @Override
     public void init(GameContainer gc) 
 			throws SlickException 
 	{
    	game = gc.setShowFPS(false);
     	
     	if(inMenu){
     		score = new Score();
     		menu = new Menu(gc);
     		inMenu = false;
     		gameover = new Gameover(gc, score);
     		
     	} else {
     	this.exitGame = false;
     	impact = new Impact(IMAGES.getImpacts());
     	birdHealth = new Health(impact);
     	background = new Background(IMAGES.BACKGROUND, IMAGES.BACKGROUND);
     	clouds = new Clouds(IMAGES.CLOUDS, IMAGES.CLOUDS);
     	sun = new Sun(IMAGES.SUN.getScaledCopy(GLOBAL.SUN_SCALE));
     	eggs = new Eggs(IMAGES.getNewEggs(), birdHealth);
     	bird = new Bird(IMAGES.getNewBird());
     	entityManager = new EntityManager(bird, birdHealth, score);
     	entityQueueHandler = new EntityQueueHandler(entityManager);
     	
     	System.out.println("RESET");
     	}
     }
  
     @Override
     public void update(GameContainer gc, int delta) 
 			throws SlickException     
     {
     	// Button listeners:
     	Input input = gc.getInput();
     	
     	if(menu.getStartgame()){
     		menu.setActive(false);
     		inMenu = false;
     		this.init(gc);
     		menu.setStartgame(false);
     	}else if(gameover.getGotomenu()){
     		score.reset();
     		menu.setActive(true);
     		inMenu = true;
     		this.init(gc);
     	}else if(gameover.getStartgame()){
     		score.reset();
     		this.exitGame = false;
     		gameover.setStartgame(false);
     		this.init(gc);
     	}
     	if(menu.isActive()){
     		menu.update(input, delta);
     	}else if(this.exitGame){
     		gameover.update(input, delta);
     	}else{
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
 	    	if(input.isKeyPressed(Input.KEY_I)){
 	    		birdHealth.increaseHealth();
 	    		System.out.println("Increased health: " + birdHealth.getHealth());
 	    	}
 	    	if(input.isKeyPressed(Input.KEY_U)){
 	    		birdHealth.decreaseHealth();
 	    		System.out.println("Decreased health: " + birdHealth.getHealth());
 	    	}
 	    	
 	    	// Updates
 	    	background.update(delta);
 	    	clouds.update(delta);
 	    	eggs.update();
 	    	score.update();
 	    	entityQueueHandler.update();
 	    	this.exitGame = entityManager.update(input, delta);
 	    	
 	    	if (this.exitGame){
 	    		System.out.println("GAME OVER!");
 	    	}
     	}
     }
     
     @Override
     public void render(GameContainer gc, Graphics g) 
 			throws SlickException 
     {
     	if(menu.isActive()){
     		menu.draw();
     	}else if(this.exitGame){
     		gameover.draw();
     	}else{
     	
     	background.draw();
     	sun.draw();
     	clouds.draw();
     	eggs.draw();
     	entityManager.draw();
     	impact.draw();
     	
     	gc.getGraphics().drawString(score.getScorePrintable(), GLOBAL.SCREEN_WIDTH - 375, 5);
     	}
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
