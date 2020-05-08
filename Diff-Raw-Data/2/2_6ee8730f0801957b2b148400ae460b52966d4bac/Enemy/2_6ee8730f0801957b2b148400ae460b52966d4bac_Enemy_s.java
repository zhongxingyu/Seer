 package enemies;
 
 import java.awt.*;
 
 public abstract class Enemy 
 {
 	protected float x, y;
 	
 	public Enemy(float nx, float ny) 
 	{
             x = nx; 
             y = ny;
 	}
 	
 	public float getX() 
 	{
             return x;
 	}
 	
 	public float getY() 
 	{
             return y;
 	}
 	
 	public Color getColor() 
 	{
             return Color.WHITE;
 	}
 	
	public abstract void move(int mx, int my);
 	
 	protected float distanceFrom(float mx, float my) 
         {
             float p1 = (x+5)-mx;
             float p2 = (y+5)-my;
             return (float)Math.pow(p2, 2) + (float)Math.pow(p1, 2);
 	}
 	
 	public boolean collidesWith(int mx, int my) 
 	{
             return distanceFrom(mx, my) < 25;
 	}
 	
 	public void paint (Graphics g) 
 	{
             Color old = g.getColor();
             g.setColor(this.getColor());
             g.fillOval((int)x, (int)y, 10, 10);
 
             g.setColor(old);
 	}
 	
 	public boolean isMortal() 
 	{
             return false;
 	}
 }
