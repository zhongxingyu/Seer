 package com.game.entity;
 
 import org.lwjgl.util.Rectangle;
 
 import com.game.world.World;
 
 public class Entity
 {	
 	private boolean isDead;
 	protected boolean hasCollision;
 	
 	public int x;
 	public int y;
 	
 	protected int xSize;
 	protected int ySize;
 	
 	protected int xSpeed;
 	protected int ySpeed;
 	protected int xSlow;
 	protected int ySlow;
 	
 	public Entity()
 	{
 		
 	}
 	
 	public void kill()
 	{
 		isDead = true;
 	}
 	
 	public boolean isDead()
 	{
 		return isDead;
 	}
 	
 	public void render()
 	{
 		
 	}
 	
 	public void tick(World world)
 	{
 		move(world);
 	}
 	
 	public void move(World world)
 	{
 		xSlow = xSlow + xSpeed;
 		ySlow = ySlow + ySpeed;
 		
 		while(xSlow >= 100)
 		{
 			if(blockRight(world))
 			{
 				xSpeed = Math.min(0, xSpeed);
 				xSlow = 0;
 				break;
 			}
 			else
 			{
 				x++;
 				xSlow -= 100;
 			}
 		}
 		
 		while(ySlow >= 100)
 		{
 			if(blockDown(world))
 			{
 				ySpeed = Math.min(0, ySpeed);
 				ySlow = 0;
 				break;
 			}
 			else
 			{
 				y++;
 				ySlow -= 100;
 			}
 		}
 		
 		while(xSlow <= -100)
 		{
 			if(blockLeft(world))
 			{
 				xSpeed = Math.max(0, xSpeed);
 				xSlow = 0;
 				break;
 			}
 			else
 			{
 				x--;
 				xSlow += 100;
 			}
 		}
 		
 		while(ySlow <= -100)
 		{
 			if(blockUp(world))
 			{
 				ySpeed = Math.max(0, ySpeed);
 				ySlow = 0;
 				break;
 			}
 			else
 			{
 				y--;
 				ySlow += 100;
 			}
 		}
 	}
 	
 	public boolean blockUp(World world)
 	{
 		if(!hasCollision)
 		{
 			return false;
 		}
 		
 		int xMin = x / 32;
 		int xMax = (x + xSize -1) / 32;
 		int yCord = (y - 1) / 32;
 		
 		for(; xMin <= xMax; xMin++)
 		{
 			if(world.hasBlockCollision(xMin, yCord))
 			{
 				return true;
 			}
 		}
 		
 		return false;
 	}
 	
 	public boolean blockDown(World world)
 	{
 		if(!hasCollision)
 		{
 			return false;
 		}
 		
 		if(y + ySize >= 512)
 		{
 			return true;
 		}
 		
 		int xMin = x / 32;
 		int xMax = (x + xSize -1) / 32;
 		int yCord = (y + ySize + 1) / 32;
 		
 		for(; xMin <= xMax; xMin++)
 		{
 			if(world.hasBlockCollision(xMin, yCord))
 			{
 				return true;
 			}
 		}
 		
 		return false;
 	}
 	
 	public boolean blockLeft(World world)
 	{
 		if(!hasCollision)
 		{
 			return false;
 		}
 		
 		int yMin = y / 32;
 		int yMax = (y + ySize -1) / 32;
 		int xCord = (x - 1) / 32;
 		
 		for(; yMin <= yMax; yMin++)
 		{
 			if(world.hasBlockCollision(xCord, yMin))
 			{
 				return true;
 			}
 		}
 		
 		return false;
 	}
 	
 	public boolean blockRight(World world)
 	{
 		if(!hasCollision)
 		{
 			return false;
 		}
 		
 		int yMin = y / 32;
 		int yMax = (y + ySize -1) / 32;
 		int xCord = (x + xSize + 1) / 32;
 		
 		for(; yMin <= yMax; yMin++)
 		{
 			if(world.hasBlockCollision(xCord, yMin))
 			{
 				return true;
 			}
 		}
 		
 		return false;
 	}
 	
	public void onCollisionWithPlayer(World world, EntityPlayer player)
 	{
 		
 	}
 	
 	public Rectangle getCollisionBox()
 	{
 		return new Rectangle(x, y, xSize, ySize);
 	}
 }
