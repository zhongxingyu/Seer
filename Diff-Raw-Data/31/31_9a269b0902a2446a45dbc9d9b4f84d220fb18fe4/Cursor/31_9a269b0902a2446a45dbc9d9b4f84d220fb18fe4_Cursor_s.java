 import java.awt.Graphics;
 
 
 public class Cursor {
 	private int x;
 	private int y;
 	private int width;
 	private int height;
 	
 	private final int maxDistance = 5; 
 	
 	private Character c;
 	
 	private ImageWrapper cursor;
 	
 	public Cursor(int x, int y, int width, int height, Character c)
 	{
 		this.width = width;
 		this.height = height;
 		this.x = x;
 		this.y = y;
 		
 		this.c = c;
 		
 		cursor = new ImageWrapper("images/targetReticleAlpha.png");  //needs to be changed when initial cursor is made
 	}
 	
 	public void draw(Graphics g)
 	{
 		cursor.draw(g, x - width/2, y - height/2, width, height);
 	}
 
 	public int getX() {
 		return x;
 	}
 
 	
	public void moveCursor(int mouseX, int mouseY, int charPosX, int charPosY)
 	{
		System.out.println("MouseX: "+ mouseX + " mouseY: "+mouseY +" charPosX: "+charPosX +" charPosY:" +charPosY );
		double dist = (mouseX - charPosX)*(mouseX - charPosX) + (mouseY - charPosY)*(mouseY - charPosY);
		
		if(dist < maxDistance*maxDistance)
		{
			this.x = (mouseX - charPosX)/2;
			this.y = (mouseY - charPosY)/2;
			System.out.println("cursor x update:"+this.x+ " cursor Y update" + this.y);

		}
 	}
 
 	public int getY() {
 		return y;
 	}
 
 
 }
