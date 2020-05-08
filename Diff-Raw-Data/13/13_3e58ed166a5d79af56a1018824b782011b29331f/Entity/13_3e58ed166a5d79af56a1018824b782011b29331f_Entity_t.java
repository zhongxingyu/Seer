 package ca.couchware.wezzle2d;
 
 import ca.couchware.wezzle2d.animation.Animation;
 import ca.couchware.wezzle2d.util.Util;
 import ca.couchware.wezzle2d.util.XYPosition;
 import java.awt.Rectangle;
 import java.awt.Shape;
 
 /**
  * An entity represents any element that appears in the game. The entity is
  * responsible for resolving collisions and movement based on a set of
  * properties defined either by subclass or externally.
  * 
  * Note that doubles are used for positions. This may seem strange given that
  * pixels locations are integers. However, using double means that an entity can
  * move a partial pixel. It doesn't of course mean that they will be display
  * half way through a pixel but allows us not lose accuracy as we move.
  * 
  * @author Cameron McKay (based on code by Kevin Glass)
  */
 public class Entity implements Drawable, Positionable
 {
     /** 
      * Is this visible? 
      */
     protected boolean visible;
     
     /**
      * Is it dirty (i.e. does it need to be redrawn)?
      */
     protected boolean dirty;
     
      /**
      * The rotation.
      */
     protected double theta = 0.0;
     
     /**
      * The opacity (in percent).
      */
     protected int opacity = 100;
     
     /** 
      * The current x location of this entity. 
      */
     protected double x;
 	
 	/** 
      * The current y location of this entity .
      */
 	protected double y;
     
     /**
      * The previous x location.
      */
     protected double x_;
     
     /**
      * The previous y location.
      */
     protected double y_;
     
     /** 
      * The current width of the entity.
      */
     protected int width;
     
     /**
      * The current height of the entity.
      */
     protected int height;
     
     /**
      * The previous width.
      */
     protected int width_;
     
     /**
      * The previous height.
      */
     protected int height_;
     
     /**
      * The current draw rectangle.
      */
     protected Rectangle drawRect;
     
     /**
      * The alignment of the entity.
      */
     protected int alignment;
     
     /**
      * The x-offset.
      */
     protected int offsetX;
     
     /**
      * The y-offset.
      */
     protected int offsetY;
 	
 	/** 
      * The sprite that represents this entity.
      */
 	protected Sprite sprite;
     
     /**
      * The animation attached to this entity.
      */
     protected Animation animation;
 	
 	/** 
      * The current speed of this entity horizontally (pixels/s). 
      */
 	protected double dx;
 	
 	/** 
      * The current speed of this entity vertically (pixels/s). 
      */
 	protected double dy;
 
 	/**
 	 * Construct a entity based on a sprite image and a location.
 	 * 
 	 * @param path
 	 *            The reference to the image to be displayed for this entity
 	 * @param x
 	 *            The initial x location of this entity
 	 * @param y
 	 *            The initial y location of this entity
 	 */
 	public Entity(final String path, final int x, final int y)           
 	{
         this.visible = true;
 		this.sprite = ResourceFactory.get().getSprite(path);
         
 		this.x = x;
 		this.y = y;
         
         this.x_ = x;
         this.y_ = y;
         
         this.width = sprite.getWidth();
         this.height = sprite.getHeight();
         
         this.width_ = width;
         this.height_ = height;
         
         this.alignment = TOP | LEFT;        
         
         // Set dirty so it will be drawn.
         setDirty(true);                
 	}
 
 	/**
 	 * Request that this entity move itself based on a certain amount of time
 	 * passing.
 	 * 
 	 * @param delta
 	 *            The amount of time that has passed in milliseconds.
 	 */
 	public void move(long delta)
 	{
         double newX = x + (delta * dx) / 1000;
         double newY = y + (delta * dy) / 1000;
         				           
         // Update the location of the entity based on move speeds.		
         x = newX;
         y = newY;
         
         // Set dirty so it will be drawn.
         setDirty(true);
 	}	
 	
     /**
 	 * Get the horizontal speed of this entity
 	 * 
 	 * @return The horizontal speed of this entity (pixels/s).
 	 */
 	public double getXMovement()
 	{
 		return dx;
 	}
     
 	/**
 	 * Set the horizontal speed of this entity
 	 * 
 	 * @param dx
 	 *            The horizontal speed of this entity (pixels/s).
 	 */
 	public void setXMovement(double dx)
 	{
 		this.dx = dx;
 	}
 
     /**
 	 * Get the vertical speed of this entity
 	 * 
 	 * @return The vertical speed of this entity (pixels/ms).
 	 */
 	public double getYMovement()
 	{
 		return dy;
 	}
     
 	/**
 	 * Set the vertical speed of this entity
 	 * 
 	 * @param dy
 	 *            The vertical speed of this entity (pixels/s).
 	 */
 	public void setYMovement(double dy)
 	{
 		this.dy = dy;
 	}
 
     /**
      * Sets the visibility of the entity.
      * 
      * @param visible
      */
     public void setVisible(boolean visible)
     {
         this.visible = visible;
         
         // Set dirty so it will be drawn.
         setDirty(true);
     }
 
     /**
      * Gets the visibility of the entity.
      * 
      * @return
      */
     public boolean isVisible()
     {
         return visible;
     }
         
     /**
 	 * Get the x location of this entity.
 	 * 
 	 * @return The x location of this entity.
 	 */
 	public int getX()
 	{
 		return (int) x;
 	}
     
 	/**
 	 * @param x The x to set.
 	 */
 	public void setX(final int x)
 	{                            
 		this.x = x;                
         
         // Set dirty so it will be drawn.        
         setDirty(true);
 	}
 
 	/**
 	 * Get the y location of this entity.
 	 * 
 	 * @return The y location of this entity.
 	 */
 	public int getY()
 	{               
 		return (int) y;
 	}
     
     /**
 	 * @param y The y to set.
 	 */
 	public void setY(final int y)
 	{
 		this.y = y;
         
         // Set dirty so it will be drawn.        
         setDirty(true);
 	}
     
     public XYPosition getXYPosition()
     {
         return new XYPosition((int) x, (int) y);
     }
 
     public void setXYPosition(int x, int y)
     {
         setX(x);
         setY(y);
     }
 
     public void setXYPosition(XYPosition p)
     {
         setX(p.x);
         setY(p.y);
     }
 
     public int getHeight()
     {
         return height;
     }
 
     public void setHeight(int height)
     {
         this.height = height;
         
         // Set dirty so it will be drawn.        
         setDirty(true);
     }
 
     public int getWidth()
     {
         return width;
     }
 
     public void setWidth(int width)
     {
         this.width = width;
         
         // Set dirty so it will be drawn.        
         setDirty(true);
     }
 
     /**
      * Sets the opacity of the sprite (in percent).
      * 
      * @param opacity The opacity.
      */
     public void setOpacity(final int opacity)
     {       
         if (opacity < 0)
             this.opacity = 0;
         else if (opacity > 100)
             this.opacity = 100;
         else
             this.opacity = opacity;
         
         // Set dirty so it will be drawn.        
         setDirty(true);
     }
     
     /**
      * Gets the opacity of the sprite.
      * 
      * @return The opacity.
      */
     public int getOpacity()
     {
         return opacity;
     }
     
     /**
      * Rotates the image by theta.
      */
     public void setRotation(double theta)
     {
         this.theta = theta;
         
         // Set dirty so it will be drawn.        
         setDirty(true);
     }
     
     /**
      * Gets the current theta.
      */
     public double getRotation()
     {
         return this.theta;
     }
     
     /**
 	 * Draw this entity to the graphics context unless it is not visible
      * or an animation is attached (the animation will handle the drawing).
 	 */
 	public void draw()
 	{
         this.x_ = x + offsetX;
         this.y_ = y + offsetY;
         
         this.width_ = width;
         this.height_ = height;
         
         if (isVisible() == false)
             return;
                         
         sprite.draw((int) x + offsetX, (int) y + offsetY, 
                 width, height, theta, opacity);                
 	}     
     
     public Animation getAnimation()
     {
         return animation;
     }
 
     public void setAnimation(Animation animation)
     {
         this.animation = animation;
         
         // Set dirty so it will be drawn.        
         setDirty(true);
     }        
     
     /**
      * Advances the animation depending on the amount of time that has passed.
      * 
      * @param delta The amount of time that has passed.
      */
     public void animate(long delta)
     {
         // Ignore if we have no animation.
         if (animation == null || animation.isDone() == true)
             return;
         
         // Pass through to the animation.
         animation.nextFrame(delta);  
         
         // Set dirty so it will be drawn.        
         setDirty(true);
     }   
 
     public int getAlignment()
     {
         return alignment;
     }
 
     public void setAlignment(final int alignment)
     {
         // Remember the anchor.
 		this.alignment = alignment;               
 				
 		// The Y anchors.
 		if((alignment & BOTTOM) == BOTTOM)
 		{
 			this.offsetY = -height;
 		}
 		else if((alignment & VCENTER) == VCENTER)
 		{
 			this.offsetY = -height / 2;
 		}
 		else if((alignment & TOP) == TOP)
 		{
 			this.offsetY = 0;
 		}
 		else
 		{
 			Util.handleWarning("No Y alignment set!", Thread.currentThread());
 		}
 		
 		// The X anchors. 
 		if((alignment & LEFT) == LEFT)
 		{
 			this.offsetX = 0;
 		}
 		else if((alignment & HCENTER) == HCENTER)
 		{
 			this.offsetX = width / 2;			
 		}
 		else if((alignment & RIGHT) == RIGHT)
 		{
 			this.offsetX = width;
 		}
 		else
 		{
 			Util.handleWarning("No X alignment set!", Thread.currentThread());
 		}	
         
         // Set dirty so it will be drawn.        
         setDirty(true);
     }
 
     public void setDirty(boolean dirty)
     {
 //        Util.handleMessage("I'm dirty!", null);        
         this.dirty = dirty;
     }
 
     public boolean isDirty()
     {
         return dirty;
     }
     
     public Rectangle getDrawRect()
     {
         Rectangle rect1 = new Rectangle((int) x_, (int) y_, 
                 width_ + 2, height_ + 2);
                
        Rectangle rect2 = new Rectangle((int) x, (int) y, 
                width + 2, height + 2);        
        rect2.translate(offsetX, offsetY);            
        rect1.add(rect2);
         
         return rect1;
     }
 
     public void resetDrawRect()
     {
         x_ = x;
         y_ = y;
         
         width_ = width;
         height_ = height;
     }
     
 }
