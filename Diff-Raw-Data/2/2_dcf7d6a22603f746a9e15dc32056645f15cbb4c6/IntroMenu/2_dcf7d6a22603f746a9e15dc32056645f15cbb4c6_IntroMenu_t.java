 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics2D;
 import java.awt.event.KeyEvent;
 
 import com.golden.gamedev.GameEngine;
 import com.golden.gamedev.GameObject;
 import com.golden.gamedev.object.Background;
 import com.golden.gamedev.object.GameFont;
 import com.golden.gamedev.object.PlayField;
 import com.golden.gamedev.object.Sprite;
 import com.golden.gamedev.object.background.ColorBackground;
 
 
 public class IntroMenu extends GameObject
 {
 	private GameFont f;
 	private Background background;
 	private PlayField field;
 	private Sprite left;
 	private Sprite right;
 	private Sprite left2;
 	private Sprite right2;
 
 	public IntroMenu(GameEngine parent) 
 	{
 		super(parent);
 	}
 
 	public void initResources()
 	{
 		left = new Sprite(getImage("resources/CharmanderRight.png"));
 		right = new Sprite(getImage("resources/Charmander.png"));
 		left2 = new Sprite(getImage("resources/CharmanderRight.png"));
 		right2 = new Sprite(getImage("resources/Charmander.png"));
 		
 		left.setLocation(0,0);
 		left2.setLocation(0, 682);
 		right.setLocation(1094, 682);
 		right2.setLocation(1094, 0);
 		
 		background = new ColorBackground(Color.YELLOW, 1280, 800);
 		Font font = new Font( "Monospaced", Font.BOLD, 24 );
 		f = fontManager.getFont(font);
 		
 		field = new PlayField();
 		
 		field.setBackground(background);
 		field.add(left);
 		field.add(right);
 		field.add(left2);
 		field.add(right2);
 	}
 
 	public void render(Graphics2D pen) 
 	{
 		field.render(pen);
 		pen.setColor(Color.BLUE);
 		f.drawString(pen, "Welcome to Charmander attack," +  " \npress 1 for score mode and 2 for survival mode" , 
 				GameFont.CENTER, 600, 300, 20);
 		f.drawString(pen, "Press escape to exit the game", GameFont.CENTER, 600, 400, 20);
 		f.drawString(pen, "In score mode, kill as many enemies as possible in 30 seconds", GameFont.CENTER, 600, 500, 20);
 		f.drawString(pen, "In surivival mode, avoid death for 30 seconds, you cannot attack", GameFont.CENTER, 600, 600, 20);
		f.drawString(pen,  "however, you have 50% more health", GameFont.CENTER, 600, 650, 20);
 	}
 
 
 	public void update(long elapsedTime) 
 	{
 	       if (keyDown(KeyEvent.VK_1)) 
 	        {
 	        	parent.nextGameID = 1;
 	 	       finish();
 	        }
 	       if (keyDown(KeyEvent.VK_ESCAPE)) 
 	        {
 	 	       finish();
 	        }
 	       if (keyDown(KeyEvent.VK_2)) 
 	        {
 	        	parent.nextGameID = 4;
 	 	       finish();
 	        }
 	}
 
 }
