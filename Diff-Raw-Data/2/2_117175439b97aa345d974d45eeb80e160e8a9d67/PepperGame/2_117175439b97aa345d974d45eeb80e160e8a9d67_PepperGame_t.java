 import org.newdawn.slick.*;
 public class PepperGame extends BasicGame {
  
     static int width = 640;
     static int height = 480;
    
     static boolean fullscreen = false;
     static boolean showFPS = true;
     static String title = "Neckbeards is not gay";
     static int fpslimit = 60;
     private Image basicImage;
     private int mouseX, mouseY;
     private int posX, posY;
     public PepperGame(String title) {
         super(title);
     }
  
     @Override
     public void init(GameContainer gc) throws SlickException {
     	basicImage = new Image("data\\image.jpg");
     	posX = 200;
     	posY = 200;
        
     }
     
     public void input(GameContainer gc, int delta) throws SlickException
     {
     	Input input = gc.getInput();
     	mouseX = input.getAbsoluteMouseX();
     	mouseY = input.getAbsoluteMouseY();
     	//posX = mouseX;
     	//posY = mouseY;
     	if(input.isKeyDown(input.KEY_W))
     	{
     		posY -= 10;
     	}
     	if(input.isKeyDown(input.KEY_S))
     	{
     		posY += 10;
     	}
     	if(input.isKeyDown(input.KEY_D))
     	{
     		posX += 10;
     	}
     	if(input.isKeyDown(input.KEY_A))
     	{
     		posX -= 10;
     	}
     }
  
     @Override
     public void update(GameContainer gc, int delta) throws SlickException {
     	input(gc, delta);
     	//System.out.println(mouseX + "," + mouseY);
        
     }
  
     @Override
     public void render(GameContainer gc, Graphics g) throws SlickException {
    	basicImage.draw(posX-(basicImage.getWidth()/2), posY-(basicImage.getHeight()/2));
     }
    
     public static void main(String[] args) throws SlickException {
         AppGameContainer app = new AppGameContainer(new PepperGame(title));
         app.setDisplayMode(width, height, fullscreen);
         app.setSmoothDeltas(true);
         app.setTargetFrameRate(fpslimit);
         app.setShowFPS(showFPS);
         app.start();
     }
    
 }
 
