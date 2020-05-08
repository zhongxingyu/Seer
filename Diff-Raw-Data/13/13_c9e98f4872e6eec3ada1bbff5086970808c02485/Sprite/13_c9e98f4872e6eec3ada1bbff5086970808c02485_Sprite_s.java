 package tsa2035.game.engine.scene;
 
 import static org.lwjgl.opengl.GL11.*;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import org.newdawn.slick.geom.Point;
 
 import tsa2035.game.engine.bounding.BoundingBox;
 import tsa2035.game.engine.bounding.Side;
 import tsa2035.game.engine.core.Renderer;
 import tsa2035.game.engine.texture.Texture;
 
 public class Sprite {
 	protected Texture texture = null;
 	protected float scale;
 	protected float xPos, yPos;
 	
 	protected boolean solid = false;
 	protected boolean interactable = false;
 	protected boolean hidden = false;
 	
 	protected BoundingBox boundingBox = new BoundingBox();
 	
 	protected ArrayList<CollisionCallback> collisionCallbacks = new ArrayList<CollisionCallback>();
 	protected ArrayList<InteractionCallback> interactionCallbacks = new ArrayList<InteractionCallback>();
 	
 	public Sprite(float x, float y, Texture t)
 	{
 		this(x,y,t,false);
 	}
 	
 	public Sprite(float x, float y, Texture t, boolean solid)
 	{
 		texture = t;
 		
 		this.xPos = x;
 		this.yPos = y;
 		
 		setScale(1);
 	}
 	
 	public Sprite setPosition(float x, float y)
 	{
 		xPos = x;
 		yPos = y;
 		return this;
 	}
 	
 	public Sprite setTexture(Texture t)
 	{
 		texture = t;
 		return this;
 	}
 	
 	public Point getPosition()
 	{
 		return new Point(xPos, yPos);
 	}
 	
 	public Sprite setX(float x)
 	{
 		xPos = x;
 		return this;
 	}
 	
 	public Sprite setY(float y)
 	{
 		yPos = y;
 		return this;
 	}
 	
 	public float getX()
 	{
 		return xPos;
 	}
 	
 	public float getY()
 	{
 		return yPos;
 	}
 	
 	public float getHeight()
 	{
 		return ((float)texture.getHeight()/(float)Renderer.getScreenY())*scale;
 	}
 	
 	public float getWidth()
 	{
 		return ((float)texture.getWidth()/(float)Renderer.getScreenX())*scale;
 	}
 	
 	public void render(Scene parent)
 	{
 		if ( isHidden() ) return;
 		
 		if ( texture == null )
 			return;
 
 		float x = getWidth();
 		float y = getHeight();
 		
		x= x/2;
		y= y/2;
		
 		float points[][] = {
 				{ x, y },
 				{ -x,y },
 				{ -x,-y },
 				{ x, -y }	
 		};
 		
 		for ( int i = 0; points.length > i; i++ )
 		{
 			points[i][0] += xPos;
 			points[i][1] += yPos;
 		}
 		
 		boundingBox.setPoints(points);
 		
 		Iterator<Sprite> sceneObjects = parent.iterator();
 		
 		while( sceneObjects.hasNext() )
 		{
 			Sprite obj = sceneObjects.next();
 			if ( obj.isSolid() )
 			{
 				Side sideOfHit = Side.NONE;
 				if ( !obj.equals(this) && (sideOfHit = sideOfContact(obj)) != Side.NONE )
 				{
 					doHit(obj,sideOfHit);
 					Iterator<CollisionCallback> callbackIt = collisionCallbacks.iterator();
 					while ( callbackIt.hasNext() )
 						callbackIt.next().collisionOccured(this, obj, sideOfHit);
 				}
 			}
 		}
 		
 		texture.bind();
 		glColor3f(1,1,1);
 		glBegin(GL_QUADS);
 		
 		
 		glTexCoord2f(1, 1);
 		glVertex2f(points[3][0], points[3][1]);
 
 		glTexCoord2f(0, 1);
 		glVertex2f(points[2][0], points[2][1]);
 
 		glTexCoord2f(0, 0);
 		glVertex2f(points[1][0], points[1][1]);
 
 		glTexCoord2f(1, 0);	
 		glVertex2f(points[0][0], points[0][1]);
 		
 		glEnd();
 	}
 	
 	public Sprite registerCollisionCallback(CollisionCallback callback)
 	{
 		collisionCallbacks.add(callback);
 		return this;
 	}
 	
 	public Sprite registerInteractionCallback(InteractionCallback callback)
 	{
 		interactionCallbacks.add(callback);
 		return this;
 	}
 	
 	public boolean contacting(Sprite other)
 	{
 		if ( isHidden() ) return false;
 		return boundingBox.contacting(other.getBoundingBox());
 	}
 	
 	public Side sideOfContact(Sprite other)
 	{
 		if ( isHidden() ) return Side.NONE;
 		return boundingBox.sideOfContact(other.getBoundingBox());
 	}
 	
 	public BoundingBox getBoundingBox()
 	{
 		return boundingBox;
 	}
 	
 	public boolean isSolid()
 	{
 		return solid;
 	}
 	
 	public Sprite setScale(float scale)
 	{
		this.scale = scale+1;
 		return this;
 	}
 	
 	public Sprite setSolid(boolean state)
 	{
 		solid = state;
 		return this;
 	}
 	
 	public Sprite setHidden(boolean state)
 	{
 		hidden = state;
 		return this;
 	}
 	
 	public Sprite setInteractable(boolean state)
 	{
 		interactable = state;
 		return this;
 	}
 	
 	public boolean isHidden()
 	{
 		return hidden;
 	}
 	
 	public boolean isInteractable()
 	{
 		return interactable;
 	}
 	
 	public void interact(Sprite origin)
 	{
 		if ( isHidden() || !isInteractable() || !getBoundingBox().ableToInteract(origin.getBoundingBox()) )
 			return;
 		
 		doInteract(origin);
 		
 		Iterator<InteractionCallback> callbackIt = interactionCallbacks.iterator();
 		while ( callbackIt.hasNext() )
 		{
 			callbackIt.next().interactionOccured(origin, this);
 		}
 	}
 	
 	public void doInteract(Sprite origin)
 	{
 		// Override me!
 	}
 	
 	public void doHit(Sprite origin, Side hitSide)
 	{
 		// Override me!
 	}
 }
