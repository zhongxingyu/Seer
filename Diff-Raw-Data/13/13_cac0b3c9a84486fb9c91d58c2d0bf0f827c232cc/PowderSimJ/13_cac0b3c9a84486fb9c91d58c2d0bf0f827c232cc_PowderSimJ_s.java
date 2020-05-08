 package net.psj;
 
 import net.psj.Simulation.Air;
 
 import org.newdawn.slick.AppGameContainer;
 import org.newdawn.slick.BasicGame;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 
 public class PowderSimJ extends BasicGame{
 	
 	public static final int width = 800;
<<<<<<< HEAD
 	public static final int height = 400;
 	public static final int cell = 4;
=======
	public static final int height = 600;
	public static final int cell = 16;
>>>>>>> 510fc352ce3229e2d32fb1646529039d6e6ce56d
 	
 	public static final int MAX_TEMP = 9000;
 	public static final int MIN_TEMP = 0;
 	
 	int mouseX;
 	int mouseY;
 	
 	public Air air = new Air();
 	
 	public PowderSimJ()
     {
         super("PowderSimJ");
     }
  
     @Override
     public void init(GameContainer gc) throws SlickException {
     }
  
     public static void main(String[] args) throws SlickException
     {
          AppGameContainer app = new AppGameContainer(new PowderSimJ());
          app.setDisplayMode(width, height, false);
          app.setVSync(true);
          app.start();
     }
 
 	@Override
 	public void render(GameContainer arg0, Graphics arg1) throws SlickException {
 		air.drawAir();
 	}
 
 	@Override
 	public void update(GameContainer arg0, int arg1) throws SlickException {
 		
 		air.update_air();
 		air.make_kernel();
 		air.update_airh();
 		
 		Input input = arg0.getInput();
 		onKeypress(input);
 		mouseX = input.getMouseX();
 		mouseY = input.getMouseY();
 		if(input.isMouseButtonDown(Input.MOUSE_LEFT_BUTTON))
 			onMouseClick();
 		if(input.isMouseButtonDown(Input.MOUSE_RIGHT_BUTTON))
 			onMouseRightClick();
 
 	}
 	
 	public void onKeypress(Input keys)
 	{
 		if(keys.isKeyDown(Input.KEY_EQUALS))
 			for (int y=0; y<height/cell; y++)
 					for (int x=0; x<width/cell; x++)
 					{
 						air.pv[y][x] = 0f;
 						air.vy[y][x] = 0f;
 						air.vx[y][x] = 0f;
 					}
 	}
 	
 	public void onMouseClick()
 	{
 		air.pv[mouseY/cell][mouseX/cell] -= 500.0f;
 	}
 	
 	public void onMouseRightClick()
 	{
 		air.pv[mouseY/cell][mouseX/cell] += 500.0f;
 	}
 }
