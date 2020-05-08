 package edu.calpoly.csc.pulseman.gameobject;
 
 import java.util.List;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.geom.Rectangle;
 import org.newdawn.slick.geom.Vector2f;
 
 import edu.calpoly.csc.pulseman.World;
 
 public abstract class Entity extends Collidable
 {
 	public static final float GRAVITY = 0.0008f, FRICTION = 0.004f,
 			FRICTION_THRESHOLD = 0.05f;
 
 	public enum Direction
 	{
 		LEFT, RIGHT
 	};
 
 	protected static final int TOP = 0, BOTTOM = 1, LEFT = 2, RIGHT = 3;
 
 	protected Vector2f position, velocity, acceleration;
 	protected Collidable floor;
 
 	public Entity(Rectangle rect, boolean affectedByPulse)
 	{
 		super(rect, affectedByPulse);
 		position = new Vector2f(rect.getX(), rect.getY());
 		velocity = new Vector2f();
 		acceleration = new Vector2f();
 
 		acceleration.y = GRAVITY;
 
 		floor = null;
 	}
 
 	public boolean isOnGround()
 	{
 		return floor != null;
 	}
 
 	public void render(GameContainer gc, Graphics g)
 	{
 		super.render(gc, g);
 	}
 
 	@Override
 	public void update(GameContainer gc, int delta)
 	{
 		super.update(gc, delta);
 
 		updatePosition(gc, delta);
 
 		handleAllCollisions(delta);
 	}
 
 	protected void updatePosition(GameContainer gc, int delta)
 	{
 		// Update velocity
 		velocity.x += acceleration.x * delta;
 		velocity.y += acceleration.y * delta;
 
 		if(floor != null && floor instanceof MovingTile)
 		{
 			MovingTile tile = (MovingTile)floor;
 			Vector2f tileVel = tile.getPositionChange();
 			tileVel.x /= delta;
 			tileVel.y /= delta;
 
 			velocity.x = tileVel.x;
 
 			if(this instanceof Player)
 			{
 				Input input = gc.getInput();
 
 				if(input.isKeyDown(Input.KEY_A) || input.isKeyDown(Input.KEY_LEFT))
 				{
 					velocity.x -= Player.PLAYER_SPEED;
 				}
 
 				if(input.isKeyDown(Input.KEY_D) || input.isKeyDown(Input.KEY_RIGHT))
 				{
 					velocity.x += Player.PLAYER_SPEED;
 				}
 			}
 
 			if(Math.abs(tileVel.y) > Math.abs(velocity.y))
 			{
 				velocity.y = tileVel.y + 0.01f;
 			}
 		}
 
 		// Update position
 		position.x += velocity.x * delta;
 		position.y += velocity.y * delta;
 
 		if(position.x < 0.0f)
 		{
 			position.x = 0.0f;
 		}
 		else if(position.x + bounds.getWidth() > World.getWorld().getLevelWidth())
 		{
			position.x = World.getWorld().getLevelWidth() - bounds.getWidth();
 		}
 
 		if(isOnGround())
 		{
 			if(velocity.x > FRICTION_THRESHOLD)
 			{
 				velocity.x -= FRICTION * delta;
 				if(velocity.x < FRICTION_THRESHOLD)
 				{
 					velocity.x = 0;
 				}
 			}
 			else if(velocity.x < -FRICTION_THRESHOLD)
 			{
 				velocity.x += FRICTION * delta;
 				if(velocity.x > -FRICTION_THRESHOLD)
 				{
 					velocity.x = 0;
 				}
 			}
 		}
 	}
 
 	private void handleAllCollisions(int delta)
 	{
 		Rectangle oldBounds = new Rectangle(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());
 
 		bounds.setLocation(position);
 
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
 
 		boolean oldXCrossover = oldCrossover.getWidth() > 3.0f;
 		boolean oldYCrossover = oldCrossover.getHeight() > 3.0f;
 
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
 				if(velocity.y > 0.0f)
 				{
 					velocity.y = 0.0f;
 				}
 			}
 		}
 
 		bounds.setLocation(position);
 	}
 }
