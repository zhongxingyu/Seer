 package ca.couchware.wezzle2d.ui.button;
 
 import ca.couchware.wezzle2d.*;
 import ca.couchware.wezzle2d.util.*;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.geom.RectangularShape;
 
 /**
  * A class representing a clickable button.
  * 
  * @author cdmckay
  */
 public abstract class Button extends Entity implements        
         MouseListener, 
         MouseMotionListener
 {
     // -------------------------------------------------------------------------
     // Constants
     // -------------------------------------------------------------------------        
     
     /**
      * The normal state.
      */
     final protected static int STATE_NORMAL = 0;
 
     /**
      * The active state.
      */
     final protected static int STATE_ACTIVE = 1;
     
     /**
      * The hover state.
      */
     final protected static int STATE_HOVER = 2;
     
     /**
      * The pressed state.
      */
     final protected static int STATE_PRESSED = 3;
     
     // -------------------------------------------------------------------------
     // Instance Attributes
     // -------------------------------------------------------------------------               
     
     /**
      * The window that button is in.  This is for adding and removing
      * the mouse listeners.
      */
     protected final GameWindow window;
     
     /**
      * The shape of the button.
      */
     protected final RectangularShape shape;
     
     /**
      * The button text.
      */
     protected String text;       
     
     /**
      * The current state of the button.
      */
     protected int state;
     
     /**
 	 * The current location of the mouse pointer.
 	 */
 	protected XYPosition mousePosition;
     
      /**
      * Was the button just pushed?  This flag is cleared once it
      * is read.
      */
     protected boolean clicked;
     
     // -------------------------------------------------------------------------
     // Constructors
     // -------------------------------------------------------------------------
     
     /**
      * The constructor.
      * @param shape The shape of the button.
      */
     public Button(final GameWindow window,
             final int x, final int y, 
             final int width, final int height,
             final RectangularShape shape)
     {
         // Set to visible.
         visible = true;
         window.addMouseListener(this);
         window.addMouseMotionListener(this);
      
         // Store the window reference.
         this.window = window;
         
         // Set the position.
         this.x = x;
         this.y = y;
         
         this.x_ = x;
         this.y_ = y;
         
         // Set the dimensions.
         this.width = width;
         this.height = height;
         
         // Set default anchor.
         this.alignment = TOP | LEFT;
         
         // Save shape reference.
         this.shape = shape;               
         
         // Set text to an empty string.
         this.text = "";
         
         // Set the initial state.
         this.state = STATE_NORMAL;        
         
         // Set dirty so it will be drawn.        
         setDirty(true);
     }
     
     // -------------------------------------------------------------------------
     // Instance Methods
     // -------------------------------------------------------------------------
        
     /**
      * Draws the button in its normal state.
      */
     public abstract void drawNormal();    
     
     /**
      * Draws the button in its active state.
      */
     public abstract void drawActive();
     
     /**
      * Draws the button in its hover state.
      */
     public abstract void drawHover();
     
     /**
      * Draws the button in its pressed (but not yet released) state.
      */
     public abstract void drawPressed();                 
     
     public void draw()
     {
         x_ = x;
         y_ = y;
         
         // Don't draw if not visible.
         if (isVisible() == false)
             return;
 
         // See what state we're in.
         switch (state)
         {
             case STATE_NORMAL:
                 drawNormal();
                 break;
                 
             case STATE_ACTIVE:
                 drawActive();
                 break;
                 
             case STATE_HOVER:
                 drawHover();
                 break;
                 
             case STATE_PRESSED:
                 drawPressed();
                 break;                           
                 
             default:
                 Util.handleWarning("Unrecognized or unhandled state.", 
                         Thread.currentThread());
         } // end switch
     }
     
     /**
      * Checks to see if the passed position is on top of the button.
      * @param x
      * @param y
      */
     public boolean contains(final int x, final int y)
     {
         return shape.contains(x, y);
     }
     
     public void handleReleased()
     {
         if (state == STATE_PRESSED)
             state = STATE_HOVER;
         
         // Set dirty so it will be drawn.        
         setDirty(true);
     }
     
     public void handlePressed()
     {        
         state = STATE_PRESSED;
         
         // Set dirty so it will be drawn.        
         setDirty(true);
     }            
     
     public void handleMouseOn()
     {
         if (state != STATE_PRESSED)
             state = STATE_HOVER;
         
         // Set dirty so it will be drawn.        
         setDirty(true);
     }        
     
     public void handleMouseOff()
     {        
         state = STATE_NORMAL;
         
         // Set dirty so it will be drawn.        
         setDirty(true);
     }
     
     public boolean clicked()
     {
         boolean value = clicked;
         clicked = false;
         return value;
     }    
     
     /**
      * A special version of clicked() that does not automatically reset the
      * flag if <pre>preserve</pre> is true.
      * 
      * @param preserve
      * @return
      */
     public boolean clicked(boolean preserve)
     {
         if (preserve == true)
             return clicked;
         else
             return clicked();
     }
         
     //--------------------------------------------------------------------------
     // Getters and Setters
     //--------------------------------------------------------------------------
     
 	/**
 	 * Gets the mousePosition.
 	 * @return The mousePosition.
 	 */
 	public synchronized XYPosition getMousePosition()
 	{
         if (mousePosition == null)
             Util.handleWarning("Mouse position is null!", 
                     Thread.currentThread());
         
 		return mousePosition;
 	}
 
 	/**
 	 * Sets the mousePosition.
 	 * @param mousePosition The mousePosition to set.
 	 */
 	public synchronized void setMousePosition(final int x, final int y)
 	{
 		this.mousePosition = new XYPosition(x, y);
 	}       
 
     public String getText()
     {
         return text;
     }
 
     public void setText(String text)
     {
         this.text = text;
         
         setDirty(true);
     }
     
     /**
 	 * Set the alignment of the button. 
      * The alignment is initially set to the top left. 
 	 * 
 	 * @param x The x alignment coordinate with respect 
      * to the top left corner of the button.
 	 * @param y The y alignment coordinate with respect 
      * to the top left corner of the button.
 	 */
     @Override
 	public void setAlignment(final int alignment)
 	{
         // Invoke super.
         super.setAlignment(alignment);	                
         
         // Move the shape.
         shape.setFrame(x + offsetX, y + offsetY,
                 shape.getWidth(), shape.getHeight());
 	}             
     
     //--------------------------------------------------------------------------
     // Events
     //--------------------------------------------------------------------------
     
     public void mouseClicked(MouseEvent e)
 	{
 		// Intentionally blank.
         
 	}
 
 	public void mouseEntered(MouseEvent e)
 	{
 		// Intentionally blank.
 		
 	}
 
 	public void mouseExited(MouseEvent e)
 	{
 		// Intentionally blank.
 		
 	}
 
 	public void mousePressed(MouseEvent e)
 	{
 		// Retrieve the mouse position.
         setMousePosition(e.getX(), e.getY());
         final XYPosition p = getMousePosition();                
         
         // Ignore click if we're outside the button.
         if (contains(p.x, p.y) == false)
             return;                    
             
 		// Check which button.
         switch (e.getButton())
         {
             // Left mouse clicked.
             case MouseEvent.BUTTON1:
                 handlePressed();
                 break;                        
                 
             default:
                 // Intentionally left blank.
         }
 	}
 
 	public void mouseReleased(MouseEvent e)
 	{             
         // Retrieve the mouse position.
         setMousePosition(e.getX(), e.getY());
         final XYPosition p = getMousePosition();
                     
         // Ignore click if we're outside the button.
         if (contains(p.x, p.y) == false)
             return;                    
             
 		// Check which button.
         switch (e.getButton())
         {
             // Left mouse clicked.
             case MouseEvent.BUTTON1:
                 handleReleased();
                 break;                        
                 
             default:
                 // Intentionally left blank.
         }
 	}
 
 	public void mouseDragged(MouseEvent e)
 	{
		// Set the mouse position.
 		setMousePosition(e.getX(), e.getY());
         
         // Retrieve the mouse position.
         final XYPosition p = getMousePosition();                
         
         // Ignore click if we're outside the button.
         if (contains(p.x, p.y) == false)
             handleMouseOff();
         else
             handleMouseOn();  
 	}
 
 	/**
 	 * Called automatically when the mouse is moved.
 	 */
 	public void mouseMoved(MouseEvent e)
 	{	    
 		// Set the mouse position.
 		setMousePosition(e.getX(), e.getY());
         
         // Retrieve the mouse position.
         final XYPosition p = getMousePosition();                
         
         // Ignore click if we're outside the button.
         if (contains(p.x, p.y) == false)
             handleMouseOff();
         else
             handleMouseOn();        
     }
     
     @Override
     public void setVisible(boolean visible)
     {
         // Ignore if visibility not changed.
         if (isVisible() == visible)
             return;
         
         // Invoke super.
         super.setVisible(visible);
         
         // Add or remove listener based on visibility.
         if (visible == true)
         {
             window.addMouseListener(this);
             window.addMouseMotionListener(this);
         }
         else
         {
             window.removeMouseListener(this);
             window.removeMouseMotionListener(this);
         }
         
     }
     
 }
