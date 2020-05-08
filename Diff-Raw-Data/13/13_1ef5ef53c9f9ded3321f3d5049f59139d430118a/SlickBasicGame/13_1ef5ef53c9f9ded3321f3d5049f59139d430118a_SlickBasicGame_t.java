 
 import org.newdawn.slick.AppGameContainer;
 import org.newdawn.slick.BasicGame;
import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
  
 public class SlickBasicGame extends BasicGame{
 	
 	Player player;
  
     Image plane = null;
     Image land = null;
     Image ring = null;
     float scale = 1;
     int width;
     int height;
  
     public SlickBasicGame(int width, int height)
     {
         super("Slick2D Path2Glory - SlickBasicGame");
         this.width = width;
         this.height = height;
     }
  
     public void init(GameContainer gc)
 			throws SlickException {
 
        plane = new Image("res/images/magnet_inactive.png");
         land = new Image("res/images/core.png");
         ring = new Image("res/images/testRing.png");
         player = new Player("foo", 200, 200, plane.getWidth() / 2, 1f);
        gc.getGraphics().setBackground(new Color(30,30,30));

     }
 
     public void update(GameContainer gc, int delta)
 			throws SlickException
     {
         Input input = gc.getInput();
         float attract = 0;
         if(input.isKeyDown(Input.KEY_A))
         {
             plane.rotate(-0.2f * delta);
         }
  
         if(input.isKeyDown(Input.KEY_D))
         {
             plane.rotate(0.2f * delta);
         }
  
         if(input.isKeyDown(Input.KEY_W))
         {
             attract = 0.3f;
             plane = new Image("res/images/magnet_active.png");
         }
         else
         	plane = new Image("res/images/magnet_inactive.png");
         if(input.isKeyDown(Input.KEY_2))
         {
             scale += (scale >= 5.0f) ? 0 : 0.1f;
             plane.setCenterOfRotation(plane.getWidth()/2.0f*scale, plane.getHeight()/2.0f*scale);
         }
         if(input.isKeyDown(Input.KEY_1))
         {
             scale -= (scale <= 1.0f) ? 0 : 0.1f;
             plane.setCenterOfRotation(plane.getWidth()/2.0f*scale, plane.getHeight()/2.0f*scale);
         }
 
         player.calculateNewPos(delta*0.2f, width/2, height/2,attract);
         ring.setCenterOfRotation(width/2,height/2);
         ring.rotate(0.05f * delta);
     }
  
     public void render(GameContainer gc, Graphics g)
 			throws SlickException
     {
         land.draw((this.width - land.getWidth()) / 2, (this.height - land.getHeight()) / 2);
  
         plane.draw(player.getMinX(), player.getMinY(), scale);
  
        ring.draw(0, 0, (float) this.height/ring.getHeight());
        
     }
  
     public static void main(String[] args)
 			throws SlickException
     {
     	int width = 600;
     	int height = 600;
          AppGameContainer app =
 			new AppGameContainer( new SlickBasicGame(width, height) );
  
          app.setDisplayMode(width, height, false);
          app.start();
         
     }
 }
