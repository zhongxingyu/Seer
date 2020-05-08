 package Game;
 
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.event.KeyEvent;
 import java.awt.geom.AffineTransform;
 import java.awt.image.BufferedImage;
 
 public class Player
 {
 	private float xpos;
 	private float ypos;
 	private float xspeed;
 	private float yspeed;
 	private int width = 30;
 	private int height = 30;
 	private double degrees = 0;
 	private BufferedImage look;
 	private final int PLAYERSPEED = 200;
 	
 	public Player(int xpos, int ypos)
 	{
 		this.xpos = xpos;
 		this.ypos = ypos;
 		
 		look = Imageloader.loadImage("Spieler");
 
 	}
 	
 
 	
 	public void draw(Graphics g)
 	{
 		g.drawImage(rotate(degrees + 90), (int) xpos, (int) ypos, null);
 	}
 	public BufferedImage rotate(double degreese)
 	{
 		AffineTransform at = AffineTransform.getRotateInstance(Math.toRadians(degreese), look.getWidth()/2, look.getHeight()/2);
 		BufferedImage rotatedImage = new BufferedImage(look.getWidth(), look.getHeight(), look.getType());
 		Graphics2D g = (Graphics2D) rotatedImage.getGraphics();
 		g.setTransform(at);
 		g.drawImage(look, 0, 0, null);
 		return rotatedImage;
 		
 	}
 	public double getRotation()
 	{
 		int absx = (int) (Keyboard.getMouseX() + 8 - (xpos + 15));
 		int absy = (int) (Keyboard.getMouseY() + 8 - (ypos + 15));
 		return Math.atan2(absy, absx);
 		
 	}
	public boolean inMouse()
	{
		float absx = (Keyboard.getMouseX() + 8) -(xpos + 15);
		float absy = (Keyboard.getMouseY() + 8) -(ypos + 15);
		float abs = absx * absx + absy * absy;
		if(abs <= 15 * 15)
		{
			return true;
		}
		return false;
	}
 	public void update(float tslf)
 	{
 		xspeed = 0;
 		yspeed = 0;
 		
		boolean inmouse = inMouse();
		
 		degrees = (int) Math.toDegrees((getRotation()));
 		
		if(Keyboard.isKeyPressed(KeyEvent.VK_W) && !inmouse)
 		{
 			xspeed = (float) Math.cos(getRadianDegrees()) * PLAYERSPEED;
 			yspeed = (float) Math.sin(getRadianDegrees()) * PLAYERSPEED;
 		}
		if(Keyboard.isKeyPressed(KeyEvent.VK_S) && !inmouse)
 		{
 			xspeed = (float) Math.cos(getRadianDegrees() + Math.PI) * PLAYERSPEED;
 			yspeed = (float) Math.sin(getRadianDegrees() + Math.PI) * PLAYERSPEED;
 		}
 		if(Keyboard.isKeyPressed(KeyEvent.VK_D))
 		{
 			xspeed = (float) Math.cos(getRadianDegrees() + Math.PI/2) * PLAYERSPEED;
 			yspeed = (float) Math.sin(getRadianDegrees() + Math.PI/2) * PLAYERSPEED;
 		}
 		if(Keyboard.isKeyPressed(KeyEvent.VK_A))
 		{
 			xspeed = (float) Math.cos(getRadianDegrees() - Math.PI/2) * PLAYERSPEED;
 			yspeed = (float) Math.sin(getRadianDegrees() - Math.PI/2) * PLAYERSPEED;
 		}
 
 
 		xpos += xspeed * tslf;
 		ypos += yspeed * tslf;
 		
 		if(xpos < 0) xpos = 0;
 		if(xpos + width > Main.width) xpos = 800 - width;
 		if(ypos < 0) ypos = 0;
 		if(ypos + height > Main.height) ypos = 600 - height;
 	}
 	public double getRadianDegrees()
 	{
 		return Math.toRadians(degrees);
 	}
 	
 }
