 package ld27jam.entities;
 
 import java.util.Set;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.geom.Vector2f;
 import org.newdawn.slick.state.StateBasedGame;
 
 import ld27jam.World;
 import ld27jam.physics.Collision;
 import ld27jam.res.ImageSheet;
 import ld27jam.spatialData.AABB;
 import ld27jam.spatialData.Region;
 
 public class Entity extends Region
 {
 	public static final float DISTANCE_TOLERANCE = 0.001f;
 	
 	public ImageSheet imageSheet;
 	public Vector2f visualDecal;
 	public boolean canBeCollided = true;
 	
	public void render(GameContainer gc, StateBasedGame sbg, Graphics g, Vector2f camera) throws SlickException
 	{
 		Vector2f pos = World.getScreenCoordinates(getPosition()).add(visualDecal).sub(camera);
 		pos.x = Math.round(pos.x);
 		pos.y = Math.round(pos.y);
 		imageSheet.render(pos);
 	}
 
 	public void update(GameContainer gc, StateBasedGame sbg, int delta, World world) throws SlickException 
 	{
 		imageSheet.update();
 	}
 	
 	public void move(Vector2f movement, World world)
 	{
 		if (getPosition().x + movement.x < 0 || getPosition().y + movement.y < 0)
 			return;
 		
 		moveForGrid(new Vector2f(movement.x, 0), world);
 		moveForGrid(new Vector2f(0, movement.y), world);
 	}
 	private void moveForGrid(Vector2f movement, World world)
 	{
 		AABB aabb = new Region(new Vector2f(getPosition().x + Math.min(0, movement.x), 
 				   				   getPosition().y + Math.min(0, movement.y)), 
 				   			   new Vector2f(getSize().x + Math.abs(movement.x),
 				   				   getSize().y + Math.abs(movement.y)));
 		
 		// Establish collision
 		Collision collision = new Collision();
 		collision.distance = movement.copy();
 		for (Entity object : world.getEntitiesInRegion(aabb))
 			if (object != this && object.canBeCollided)
 				collision = shortestCollision(collision, determineCollision(object, collision.distance));
 		for (Tile tile : world.getTilesInRegion(aabb))
 			if (tile != null && tile.type != null && !tile.type.canWalkOn)
 				collision = shortestCollision(collision, determineCollision(tile, collision.distance));
 			
 		Vector2f position = getPosition().copy().add(collision.distance);
 		if (!inCollision(world, position))
 			setPosition(position);
 	}
 
 	public boolean inCollision(World world, Vector2f position)
 	{
 		Set<? extends Entity> set = world.getEntitiesInRegion(new Region(position, getSize()));
 		set.remove(this);
 		for (Entity obj : set)
 			if (obj.canBeCollided)
 				return true;
 		return false;
 	}
 	public boolean inCollision(Entity object, Vector2f position)
 	{
 		return object.canBeCollided && xAligned(object, position) && yAligned(object, position);
 	}
 	public boolean xAligned(AABB object, Vector2f position)
 	{
 		return position.x - (object.getPosition().x + object.getSize().x) < DISTANCE_TOLERANCE &&
 			   position.x + getSize().x - object.getPosition().x > -DISTANCE_TOLERANCE;
 	}
 	public boolean yAligned(AABB object, Vector2f position)
 	{
 		return position.y - (object.getPosition().y + object.getSize().y) < DISTANCE_TOLERANCE &&
 			   position.y + getSize().y - object.getPosition().y > -DISTANCE_TOLERANCE;
 	}
 	
 	public Collision determineCollision(AABB object, Vector2f velocity)
 	{
 		float tmpX, tmpY;
 
 		Collision col = new Collision();
 		col.distance = velocity.copy();
 		
 		// Collision on the x-axis
 		if (velocity.x > 0)
 		{
 			tmpX = object.getPosition().x - getPosition().x - getSize().x;
 			tmpY = velocity.y * tmpX / velocity.x;
 			if (tmpX > -DISTANCE_TOLERANCE && tmpX < velocity.x && 
 				yAligned(object, getPosition().copy().add(new Vector2f(tmpX, tmpY))))
 			{
 				col.distance.x = tmpX;
 				col.inCollisionRight = true;
 				col.collisionX = object;
 			}
 		}
 		else if (velocity.x < 0)
 		{
 			tmpX = object.getPosition().x + object.getSize().x - getPosition().x;
 			tmpY = velocity.y * tmpX / velocity.x;
 			if (tmpX < DISTANCE_TOLERANCE && tmpX > velocity.x && 
 				yAligned(object, getPosition().copy().add(new Vector2f(tmpX, tmpY))))
 			{
 				col.distance.x = tmpX;
 				col.inCollisionLeft = true;
 				col.collisionX = object;
 			}
 		}
 		
 		// Collision on the y-axis
 		if (velocity.y > 0)
 		{
 			tmpY = object.getPosition().y - getPosition().y - getSize().y;
 			tmpX = velocity.x * tmpY / velocity.y;
 			if (tmpY > -DISTANCE_TOLERANCE && tmpY < velocity.y && 
 				xAligned(object, getPosition().copy().add(new Vector2f(tmpX, tmpY))))
 			{
 				col.distance.y = tmpY;
 				col.inCollisionBottom = true;
 				col.collisionY = object;
 			}
 		}
 		else if (velocity.y < 0)
 		{
 			tmpY = object.getPosition().y + object.getSize().y - getPosition().y;
 			tmpX = velocity.x * tmpY / velocity.y;
 			if (tmpY < DISTANCE_TOLERANCE && tmpY > velocity.y && 
 				xAligned(object, getPosition().copy().add(new Vector2f(tmpX, tmpY))))
 			{
 				col.distance.y = tmpY;
 				col.inCollisionTop = true;
 				col.collisionY = object;
 			}
 		}
 		
 		return col;
 	}	
 	public Collision shortestCollision(Collision col1, Collision col2)
 	{
 		Collision col = new Collision();
 		col.distance = new Vector2f();
 		
 		// X-axis
 		if (col1.distance.x > 0 && col2.distance.x < col1.distance.x || 
 			col1.distance.x < 0 && col2.distance.x > col1.distance.x)
 		{
 			col.distance.x = col2.distance.x;
 			col.inCollisionRight = col2.inCollisionRight;
 			col.inCollisionLeft = col2.inCollisionLeft;
 			col.collisionX = col2.collisionX;
 		}
 		else
 		{
 			col.distance.x = col1.distance.x;
 			col.inCollisionRight = col1.inCollisionRight;
 			col.inCollisionLeft = col1.inCollisionLeft;
 			col.collisionX = col1.collisionX;
 		}
 		
 		// Y-axis
 		if (col1.distance.y > 0 && col2.distance.y < col1.distance.y || 
 			col1.distance.y < 0 && col2.distance.y > col1.distance.y)
 		{
 			col.distance.y = col2.distance.y;
 			col.inCollisionBottom = col2.inCollisionBottom || col1.inCollisionBottom;
 			col.inCollisionTop = col2.inCollisionTop || col1.inCollisionTop;
 			col.collisionY = col2.collisionY != null ? col2.collisionY : col1.collisionY;
 		}
 		else
 		{
 			col.distance.y = col1.distance.y;
 			col.inCollisionBottom = col1.inCollisionBottom || col2.inCollisionBottom;
 			col.inCollisionTop = col1.inCollisionTop || col2.inCollisionTop;
 			col.collisionY = col1.collisionY != null ? col1.collisionY : col2.collisionY;
 		}
 		
 		return col;
 	}
 
 	public Entity(Vector2f position, Vector2f size, boolean canBeCollided, Vector2f visualDecal, ImageSheet imageSheet)
 	{
 		setPosition(position);
 		setSize(size);
 		this.canBeCollided = canBeCollided;
 		this.visualDecal = visualDecal;
 		this.imageSheet = imageSheet;
 	}
 }
