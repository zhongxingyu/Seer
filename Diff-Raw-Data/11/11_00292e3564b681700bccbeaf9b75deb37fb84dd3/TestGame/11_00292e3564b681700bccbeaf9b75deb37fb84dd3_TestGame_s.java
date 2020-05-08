 package rogueshadow.XionEngine.test;
 
 import org.newdawn.slick.tiled.*;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.BasicGame;
 import org.newdawn.slick.AppGameContainer;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 
 import rogueshadow.XionEngine.Player;
 
 //TODO add networking support
 
 /**
  *
  * @author Adam
  */
 public class TestGame extends BasicGame {
 
     public TiledMap map;
     public Input input;
     public GameContainer container;
     public Image background;
     public Player player;
     public static  Integer width = 800;
     public static  Integer height = 600;
     public Image tiledBackground;
     public Integer offsetx;
     public Integer offsety;
     public Integer maxX;
     public Integer maxY;
 
     public static void main(String[] argv) throws SlickException {
 	AppGameContainer container = new AppGameContainer(new TestGame(), width, height, false);
 	container.start();
     }
     
     public TestGame(){
         super("Test Game");
     }
 
     @Override
     public void init(GameContainer container) throws SlickException {
         input = container.getInput();
         container.setTargetFrameRate(60);
         player = new Player("adam","Rogue Shadow", 50, 50, new Image("res/background.png"));
         map = new TiledMap("res/desert.tmx");
         offsetx = offsety = 0;
         container.setVSync(true);
         //container.setFullscreen(true);
         maxX = map.getWidth()*map.getTileWidth()-container.getWidth();
         maxY = map.getHeight()*map.getTileHeight()-container.getHeight();
     }
 
  
     @Override
 	public void update(GameContainer container, int delta) throws SlickException {
         Integer speed = 10;
         Integer buffer = 10;
         input = container.getInput();
         if (input.isKeyDown(Input.KEY_UP))setOffset(0,-speed);
         if (input.isKeyDown(Input.KEY_DOWN))setOffset(0, speed);
         if (input.isKeyDown(Input.KEY_LEFT))setOffset(-speed, 0);
         if (input.isKeyDown(Input.KEY_RIGHT))setOffset(speed, 0);
         if (input.isKeyDown(Input.KEY_ESCAPE))container.exit();
         if (input.getMouseY() < buffer)setOffset(0, -speed);
         if (input.getMouseX() < buffer)setOffset(-speed, 0);
         if (input.getMouseY() > container.getHeight() - buffer)setOffset(0, speed);
         if (input.getMouseX() > container.getWidth() - buffer)setOffset(speed, 0);
         if (input.isKeyDown(Input.KEY_W))player.movePlayer(0,-speed);
         if (input.isKeyDown(Input.KEY_S))player.movePlayer(0, speed);
         if (input.isKeyDown(Input.KEY_A))player.movePlayer(-speed, 0);
         if (input.isKeyDown(Input.KEY_D))player.movePlayer(speed, 0);
     }
 
     public void setOffset(Integer deltaX, Integer deltaY) {
         offsetx += deltaX;
         offsety += deltaY;
         if (offsetx < 0)offsetx = 0;
         if (offsetx > maxX)offsetx = maxX;
         if (offsety < 0)offsety = 0;
         if (offsety > maxY)offsety = maxY;
     }
     @Override
 	public void render(GameContainer container, Graphics g) throws SlickException {;
         String mousex = Integer.toString(input.getMouseX());
         String mousey = Integer.toString(input.getMouseY());
         map.render(-offsetx,-offsety);
        player.draw();
         g.drawString("Hello World", 40 , 40);
         g.drawString(mousex + "/" + Integer.toString(maxX), 40, 60);
         g.drawString(mousey + "/" + Integer.toString(maxY), 40, 80);
 
     }
 }
