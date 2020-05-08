 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics;
 
 
 public class Invader extends GameObject {
 	
 	static final int SIZE = 30;
 	static final int BUFFER = SIZE/2;
 	static int svelocityX = SIZE;
 	static int next_svelocityX = svelocityX;
 	static int yOffset = 20;
 	int basey;
 	String name;
	int power;
 
 	public Invader(int x, int y, int velocityX, int velocityY) {
 		super(x, y, SIZE, velocityY, SIZE, SIZE/2);
 		basey = y;
 		name = "" + randomCharacter();
 	}
 	
 	@Override
 	public void move() {
 		x += svelocityX;
 		accelerate();
 		y = basey + yOffset;
		
		
 	}
 	
 	public static void nextround() 
 	{
 		if(svelocityX != next_svelocityX)
 		{
 			yOffset += SIZE;
 		}
 		svelocityX = next_svelocityX;
 		
 	}
 
 	@Override
 	public void accelerate() {
 		if(x >= rightBound || x < 0 + SIZE )
 		{
 			next_svelocityX = -svelocityX;
 		}
 
 	}
 
 	@Override
 	public void draw(Graphics g) {
 		g.setColor(Color.blue);
		Font comicsans = new Font("Comic Sans MS", Font.PLAIN, SIZE);
 		g.setFont(comicsans);
 		g.drawString(name,x,y + SIZE);
 
 	}
 
 }
