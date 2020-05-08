 package tetrix.core;
 
 import org.newdawn.slick.AppGameContainer;
 import org.newdawn.slick.BasicGame;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 
 public class CannonMoveTest extends BasicGame {
 
 	private Cannon cannon;
 	private Image background;
 	public final static int WINDOW_WIDTH = 500;
 	public final static int WINDOW_HEIGHT = 600;
 	private int value;
 	
 	public CannonMoveTest() {
 		super("Cannon Move Test");
 	}
 
 	@Override
 	public void render(GameContainer arg0, Graphics arg1) throws SlickException {
 		background.draw(0,0);
 		
 		// +25 to get the middle
        cannon.draw(cannon.getX(), cannon.getY());
 	}
 
 	@Override
 	public void init(GameContainer gc) throws SlickException {
 		background= new Image("img/game-background.png");
 		cannon = new Cannon(250,550, new Image("img/cannon2.png"));	
 		value = 250;
 	}
 
 	@Override
 	public void update(GameContainer gc, int arg) throws SlickException {
 		Input input = gc.getInput();
 		
 		if(input.isKeyDown(Input.KEY_RIGHT)) {
			cannon.move(1,0);
 		}
		
		if(input.isKeyDown(Input.KEY_LEFT)) {
			cannon.move(-1,0);
 		}
 	}
 	
 	public static void main(String[] args) 
 			throws SlickException
     {
          AppGameContainer app = 
 			new AppGameContainer(new CannonMoveTest());
  
          app.setDisplayMode(WINDOW_WIDTH, WINDOW_HEIGHT, false);
          app.start();
     }
 
 }
