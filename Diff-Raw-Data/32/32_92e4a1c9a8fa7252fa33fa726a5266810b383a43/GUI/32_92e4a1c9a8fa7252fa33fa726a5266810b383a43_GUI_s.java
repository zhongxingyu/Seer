 package rsmg.gui;
 
 import org.lwjgl.LWJGLException;
 import org.newdawn.slick.AppGameContainer;
 import org.newdawn.slick.BasicGame;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 
 public class GUI extends BasicGame {
 
 	Image bg = null;
 	Image img = null;
 	int x = 0;
 	int y = 0;
	
    public GUI()
    {
        super("Test");
    }
 
 	@Override
 	public void init(GameContainer g) throws SlickException {
 		img = new Image("res/art/char.png");
 		bg = new Image("res/art/conceptLevel.jpg");
 	}
 
 	@Override
 	public void update(GameContainer g, int delta) throws SlickException {
 		Input input = g.getInput();
 		if (input.isKeyDown(Input.KEY_LEFT)) {
 			x -= 1;
 		} else if (input.isKeyDown(Input.KEY_RIGHT)) {
 			x += 1;
 		}
 		if (input.isKeyDown(Input.KEY_UP)) {
 			y -= 1;
 		} else if (input.isKeyDown(Input.KEY_DOWN)) {
 			y += 1;
 		}
 	}
 
 	@Override
 	public void render(GameContainer g, Graphics gfx) throws SlickException {
 		bg.draw(0, 0, 1f);
 		img.draw(x, y);
 	}
 
    public static void main(String[] args) 
			throws SlickException, LWJGLException
    {
    	
         AppGameContainer app = 
			new AppGameContainer(new GUI());
 
         app.setDisplayMode(800, 600, false);
         app.start();
    }
 }
