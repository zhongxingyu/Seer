 
 import java.awt.*;
 import java.util.concurrent.*;
 
 public class Collision
 {
 	private static Frame frame;
 	private static Content manager;
 	// Constructor
 	public Collision(Frame fIn, Content mIn)
 	{
 		frame = fIn;
 		manager = mIn;
 	}
 	private static boolean collideX(Entity eIn, Entity oIn)
 	{
 		if(Math.abs(eIn.getCenter().y - oIn.getCenter().y) < (eIn.getHeight() + oIn.getHeight()) / 2)
 		{
 			if(Math.abs(eIn.getDest().x - oIn.getCenter().x) < (eIn.getWidth() + oIn.getWidth()) / 2)
 			{
 				return true;
 			}
 			if(eIn.getCenter().x < oIn.getCenter().x && oIn.getCenter().x < eIn.getDest().x)
 			{
 				return true;
 			}
 			if(eIn.getCenter().x > oIn.getCenter().x && oIn.getCenter().x > eIn.getDest().x)
 			{
 				return true;
 			}
 		}
 		return false;
 	}
 	private static boolean collideY(Entity eIn, Entity oIn)
 	{
 		if(Math.abs(eIn.getCenter().x - oIn.getCenter().x) < (eIn.getWidth() + oIn.getWidth()) / 2)
 		{
 			if(Math.abs(eIn.getDest().y - oIn.getCenter().y) < (eIn.getHeight() + oIn.getHeight()) / 2)
 			{
 				return true;
 			}
 			if(eIn.getCenter().y < oIn.getCenter().y && oIn.getCenter().y < eIn.getDest().y)
 			{
 				return true;
 			}
 			if(eIn.getCenter().y > oIn.getCenter().y && oIn.getCenter().y > eIn.getDest().y)
 			{
 				return true;
 			}
 		}
 		return false;
 	}
 	public static Entity collisionX(Entity eIn)
 	{
 		for(String s : eIn.list)
 		{
 			Manager m = manager.getType(s);
 			if(m != null)
 			{
 				for(Object e : m.list)
 				{
 					if(collideX(eIn, (Entity)e))
 					{
 						return (Entity)e;
 					}
 				}
 			}
 		}
 		return null;
 	}
 	public static Entity collisionY(Entity eIn)
 	{
 		for(String s : eIn.list)
 		{
 			Manager m = manager.getType(s);
 			if(m != null)
 			{
 				for(Object e : m.list)
 				{
 					if(collideY(eIn, (Entity)e))
 					{
 						return (Entity)e;
 					}
 				}
 			}
 		}
 		return null;
 	}
 	public static boolean wallLeft(Entity eIn)
 	{
 		return eIn.getDest().x - eIn.getWidth() / 2 < 0;
 	}
 	public static boolean wallRight(Entity eIn)
 	{
 		return eIn.getDest().x + eIn.getWidth() / 2 > frame.getWidth() - 15; // 15 to make up for right of window frame.
 	}
 	public static boolean wallX(Entity eIn)
 	{
 		return wallLeft(eIn) || wallRight(eIn);
 	}
 	public static boolean wallUp(Entity eIn)
 	{
 		return eIn.getDest().y - eIn.getHeight() / 2 < 0;
 	}
 	public static boolean wallDown(Entity eIn)
 	{
 		return eIn.getDest().y + eIn.getHeight() / 2 > frame.getHeight() - 35; // 35 to make up for bottom of window frame.
 	}
 	public static boolean wallY(Entity eIn)
 	{
 		return wallUp(eIn) || wallDown(eIn);
 	}
 	public static Entity moveX(Entity eIn)
 	{
 		Entity o = collisionX(eIn);
 		if(o == null)
 		{
 			eIn.getCenter().x = eIn.getDest().x;
 		}
 		else if(eIn.getCenter().x < eIn.getDest().x)
 		{
 			eIn.getCenter().x = o.getCenter().x - (eIn.getWidth() + o.getWidth()) / 2;
 			eIn.dx = 0;
 		}
 		else
 		{
 			eIn.getCenter().x = o.getCenter().x + (eIn.getWidth() + o.getWidth()) / 2;
 			eIn.dx = 0;
 		}
 		return o;
 	}
 	public static Entity moveY(Entity eIn)
 	{
 		Entity o = collisionY(eIn);
 
 		if(o == null)
 		{
 			eIn.getCenter().y = eIn.getDest().y;
 		}
 		else if(eIn.getCenter().y < eIn.getDest().y)
 		{
 			eIn.getCenter().y = o.getCenter().y - (eIn.getHeight() + o.getHeight()) / 2;
 			eIn.dy = 0;
 		}
 		else
 		{
 			eIn.getCenter().y = o.getCenter().y + (eIn.getHeight() + o.getHeight()) / 2;
 			eIn.dy = 0;
 		}
 		return o;
 	}
 }
