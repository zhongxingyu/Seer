 package edu.calpoly.csc.pulseman.gameobject;
 
 import java.util.List;
 
 import org.newdawn.slick.geom.Rectangle;
 import org.newdawn.slick.geom.Vector2f;
 
 import edu.calpoly.csc.pulseman.World;
 
 public abstract class Entity extends Collidable
 {
 	protected static final int TOP = 0, BOTTOM = 1, LEFT = 2, RIGHT = 3;
 
 	protected Vector2f position, velocity, acceleration;
 	protected Collidable floor;
 
 	public Entity(Rectangle rect)
 	{
 		super(rect);
 		position = new Vector2f(rect.getX(), rect.getY());
 		velocity = new Vector2f();
 		acceleration = new Vector2f();
 
 		floor = null;
 	}
 
 	public void update(int delta)
 	{
 		// Update velocity
 		velocity.x += acceleration.x * delta;
 		velocity.y += acceleration.y * delta;
 
 		// Update position
 		if(Math.abs(velocity.x * delta) > 20)
 		{
 			System.out.println("big diff");
 		}
 		position.x += velocity.x * delta;
 		position.y += velocity.y * delta;
 
 		handleAllCollisions(delta);
 	}
 
 	private void handleAllCollisions(int delta)
 	{
 		Rectangle oldBounds = new Rectangle(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());
 
 		bounds.setLocation(position);
 
 		if(floor != null && floor instanceof MovingTile)
 		{
 			MovingTile tile = (MovingTile)floor;
 			tile.updateOther(bounds);
 
 			position.x = bounds.getX();
 			position.y = bounds.getY();
 		}
 
 		floor = null;
 		List<Collidable> collidables = World.getWorld().getCollidables();
 
 		for(int i = 0; i < collidables.size(); ++i)
 		{
 			if(bounds.intersects(collidables.get(i).bounds))
 			{
 				handleCollision(collidables.get(i), oldBounds);
 			}
 		}
 	}
 
 	protected void handleCollision(Collidable collidable, Rectangle oldBounds)
 	{
 		Rectangle crossover = getCollision(bounds, collidable.bounds);
 		Rectangle oldCrossover = getCollision(oldBounds, collidable.bounds);
 
 		if(crossover.getWidth() <= 0 || crossover.getHeight() <= 0)
 		{
 			return;
 		}
 
 		boolean oldXCrossover = oldCrossover.getWidth() > 0.1f;
 		boolean oldYCrossover = oldCrossover.getHeight() > 0.1f;
 
 		float diffx = bounds.getCenterX() - collidable.bounds.getCenterX();
 		float diffy = bounds.getCenterY() - collidable.bounds.getCenterY();
 
 		diffx *= ((bounds.getHeight() + collidable.bounds.getHeight()) / (bounds.getWidth() + collidable.bounds.getWidth()));
 		diffy *= (bounds.getWidth() + collidable.bounds.getWidth()) / ((bounds.getHeight() + collidable.bounds.getHeight()));
 
 		// Horizontal
 		if(oldYCrossover && !oldXCrossover)
 		{
 			if(diffx >= 0)
 			{
 				if(velocity.x > collidable.bounds.getWidth() / 2)
 				{
 					position.x -= crossover.getWidth();
 				}
 				else
 				{
 					position.x += crossover.getWidth();
 				}
 			}
 			else
 			{
 				if(velocity.x < -collidable.bounds.getWidth() / 2)
 				{
 					position.x += crossover.getWidth();
 				}
 				else
 				{
 					position.x -= crossover.getWidth();
 				}
 			}
 
 			velocity.x = 0.0f;
 		}
 
 		// Vertical
 		if(oldXCrossover)
 		{
 			// Below
 			if(diffy >= 0)
 			{
 				if(velocity.y > collidable.bounds.getHeight() / 2)
 				{
 					position.y -= crossover.getHeight();
 				}
 				else
 				{
 					position.y += crossover.getHeight();
 				}
 
 				if(velocity.y < 0.0f)
 				{
 					velocity.y = 0.0f;
 				}
 			}
 			else
 			// Above
 			{
 				if(velocity.y < -collidable.bounds.getHeight() / 2)
 				{
 					position.y += crossover.getHeight();
 				}
 				else
 				{
 					position.y -= crossover.getHeight();
 				}

 				floor = collidable;
 				velocity.y = 0.0f;
 			}
 		}
 
 		bounds.setLocation(position);
 	}
 }
