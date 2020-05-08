<<<<<<< HEAD
import org.newdawn.slick.AppGameContainer;
=======
 import org.newdawn.slick.AppGameContainer;
>>>>>>> 4259424bc0d65f4267535f019a9d511148c5e337
 import org.newdawn.slick.BasicGame;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
  
 public class SlickBasicGame extends BasicGame{
 	
 	Player player;
  
     Image plane = null;
     Image land = null;
     float x = 400;
     float y = 300;
     float scale = 1;
  
     public SlickBasicGame()
     {
         super("Slick2D Path2Glory - SlickBasicGame");
     }
  
    @Override
     public void init(GameContainer gc)
 			throws SlickException {
 		player = new Player("foo", 100, 60, (float)Math.PI/2, 0.01f);
 
         plane = new Image("res/images/magnet_inactive.png");
         land = new Image("res/images/core.png");
     }
 
    @Override
     public void update(GameContainer gc, int delta)
 			throws SlickException
     {
         Input input = gc.getInput();
  
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
             float hip = 0.4f * delta;
  
             float rotation = plane.getRotation();
  
             x+= hip * Math.sin(Math.toRadians(rotation));
             y-= hip * Math.cos(Math.toRadians(rotation));
         }
  
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
         
         player.calculateNewPos(delta*0.01f, x/2, y/2);
     }
  
     public void render(GameContainer gc, Graphics g)
 			throws SlickException
     {
         land.draw(0, 0);
  
         plane.draw(player.getCenterX(), player.getCenterY(), scale);
  
     }
  
     public static void main(String[] args)
 			throws SlickException
     {
          AppGameContainer app =
 			new AppGameContainer( new SlickBasicGame() );
  
          app.setDisplayMode(800, 600, false);
         app.start();
<<<<<<< HEAD
     }
 }
=======
    }}
>>>>>>> 4259424bc0d65f4267535f019a9d511148c5e337
