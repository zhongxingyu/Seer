 package game;
 
 import org.newdawn.slick.AppGameContainer;
 import org.newdawn.slick.BasicGame;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.geom.Vector2f;
 
 import components.*;
 
 public class Game extends BasicGame {
 	
 	private Entity background = null;
 	private Entity player = null;
 
 	public Game() {
 		super("Super Game");
 	}
 
 	@Override
 	public void init(GameContainer gc) throws SlickException {
 		background = new Entity("earth");
 		background.AddComponent(new ImageRenderComponent("BackgroundRender",
 				new Image("/Sprites/background.png")));
 		
 		player = new Entity("hero");
 		player.AddComponent(new ImageRenderComponent("PlayerRender",
 				new Image("/Sprites/Character.png")));
 		player.AddComponent(new PlayerMovementComponent("PlayerMovement") );
 		player.setPosition(new Vector2f(400, 300));
 	}
 
 	@Override
 	public void update(GameContainer gc, int delta) throws SlickException {
 		player.update(gc, delta);
 		
 		// get input to exit game
 		Input input = gc.getInput();
 		if (input.isKeyDown(Input.KEY_ESCAPE))
 			System.exit(0);
 	}
 
 	public void render(GameContainer gc, Graphics g) throws SlickException {
 		background.render(gc, g);
 		player.render(gc, g);
 	}
 
 	public static void main(String[] args) throws SlickException {
 		AppGameContainer app = new AppGameContainer(new Game());
 
 		app.setDisplayMode(800, 600, false);
 		app.setVSync(false);
		app.setTargetFrameRate(60);
 		app.start();
 	}
 }
