 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Rectangle;
 
 import javax.sound.sampled.Line;
 
 public class hero implements constants
 {
 
 	private int heroWidth = 24;
 	private int heroHeight = 30;
 	public int dx = 2;
 	public int dy = 2;
 	private int x = 0,jc=0,dc=0;
 	private int y = constants.HEIGHT-50-heroHeight;
 	public boolean up, isJumping = false;
 	public Rectangle heroBorder, r1, r2, r3, r4, r5;
 	public Line left, right, top, down;
 	public hero()
 	{
 		System.out.println("hero created");
 	}
 	
 	public void setX(int x)
 	{
 		this.x = x;
 	}
 	
 	public void setY(int y)
 	{
 		this.y = y;
 	}
 	
 	public int getX()
 	{
 		return x;
 	}
 	
 	public int getY()
 	{
 		return y;
 	}
 	
 	public void moveX(int b)
 	{
 		x += b;
 	}
 	
 	public void moveY(int b)
 	{
 		if(y <= constants.HEIGHT-heroHeight-50)
 			y += b;
 		if(y > constants.HEIGHT-heroHeight-50)
 		{
 			y -= 1;
 		}
 	}
 	public void jump() {
		double offset = this.heroHeight * 0.3;
 		System.out.println(offset);
 		int o = (int)offset;
 		jc++;
 		if(this.up)
 		this.moveY(o*-1);
 
 		if(!this.up)
 		{
 			this.moveY(o);
 			dc++;
 			if(dc==10)
 			{
 			 this.isJumping = false;
 			  dc = 0;
 			}
 		}
 		if(jc==10)
 		{
 			this.up = false;
 			jc = 0;
 		}
 	}
 	
 	public void paint(Graphics g)
 	{
 		Graphics2D g2d = ( Graphics2D ) g;
 		g2d.setColor(Color.WHITE);
 		g2d.fillRect(0, constants.HEIGHT-50, 500, 2);
 		g2d.fillRect(300,430,50,50);
 		r1 = new Rectangle(300, 430, 50, 50);
 
 		g2d.setColor(Color.BLACK);
 		g2d.fillRect(x, y, heroWidth, heroHeight);
 		heroBorder = new Rectangle(x, y, heroWidth, heroHeight);
 		
 		
 		
 		collisionLogic(r1);
 	}
 	
 	public void collisionLogic(Rectangle temp)
 	{
 		//System.out.println("("+heroBorder.x+","+temp.x+")");
 		
 		if((heroBorder.x) == (temp.x - heroWidth))
 		{
 			moveX(-2);
 		}
 		
 		
 		else
 		{
 			dx = 2;
 			dy = 2;
 		}
 	}
 }
