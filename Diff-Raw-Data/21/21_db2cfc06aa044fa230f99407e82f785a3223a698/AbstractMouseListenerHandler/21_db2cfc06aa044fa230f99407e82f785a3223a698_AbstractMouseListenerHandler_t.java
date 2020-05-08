 package handlers;
 
 import java.util.ArrayList;
 
 import handleds.Actor;
 import listeners.AdvancedMouseListener;
 
 /**
  * This class handles the informing of mouselsteners. It does not actively find 
  * any new information though which must be done in the subclasses.
  *
  * @author Mikko Hilpinen.
  *         Created 28.12.2012.
  */
 public abstract class AbstractMouseListenerHandler extends LogicalHandler 
 implements Actor
 {
 	// ATTRIBUTES	-------------------------------------------------------
 	
 	private int mouseX, mouseY;
 	private boolean ldown, rdown, lpressed, rpressed, lreleased, rreleased;
 	
 	private ArrayList<AdvancedMouseListener> entered;
 	private ArrayList<AdvancedMouseListener> over;
 	private ArrayList<AdvancedMouseListener> exited;
 	
 	
 	
 	// CONSTRUCTOR	-------------------------------------------------------
 
 	/**
 	 * Creates a new empty mouselistenerhandler
 	 *
 	 * @param autodeath Will the handler die when it runs out of living listeners
 	 * @param actorhandler The ActorHandler that will make the handler inform 
 	 * its listeners (optional)
 	 */
 	public AbstractMouseListenerHandler(boolean autodeath, ActorHandler actorhandler)
 	{
 		super(autodeath, actorhandler);
 		
 		// Initializes attributes
 		this.mouseX = 0;
 		this.mouseY = 0;
 		this.entered = new ArrayList<AdvancedMouseListener>();
 		this.over = new ArrayList<AdvancedMouseListener>();
 		this.exited = new ArrayList<AdvancedMouseListener>();
 		this.lpressed = false;
 		this.rpressed = false;
 		this.lreleased = false;
 		this.rreleased = false;
 	}
 
 	
 	// IMPLEMENTED METHODS	-----------------------------------------------
 	
 	@Override
 	public void act()
 	{
 		// Removes the dead listeners
 		removeDeadHandleds();
 		
 		// Informs the listeners about the mouse's movements and buttons
		for (int i = 0; i < getHandledNumber(); i++)
 		{
			AdvancedMouseListener l = (AdvancedMouseListener) getHandled(i);
 			
 			// Checks if informing is needed
 			if (!l.isActive())
 				continue;
 			
 			// Only if the object cares about mouse movement
 			if (l.listensMouseEnterExit())
 			{
 				// Exiting
 				if (this.exited.contains(l))
 					l.onMouseExit(getMouseX(), getMouseY());
 				// Mouseover
 				else if (this.over.contains(l))
 					l.onMouseOver(getMouseX(), getMouseY());
 				// Entering
 				else if (this.entered.contains(l))
 					l.onMouseEnter(getMouseX(), getMouseY());
 			}
 			
 			// Informs about mouse buttons
 			if (l.listensPosition(getMouseX(), getMouseY()))
 			{
 				if (leftIsDown())
 					l.onLeftDown(getMouseX(), getMouseY());
 				if (rightIsDown())
 					l.onRightDown(getMouseX(), getMouseY());
 				if (this.lpressed)
 					l.onLeftPressed(getMouseX(), getMouseY());
 				if (this.rpressed)
 					l.onRightPressed(getMouseX(), getMouseY());
 				if (this.lreleased)
 					l.onLeftReleased(getMouseX(), getMouseY());
 				if (this.rreleased)
 					l.onRightReleased(getMouseX(), getMouseY());
 			}
 		}
 		
 		// Refreshes memory
 		if (this.entered.size() > 0)
 		{
 			this.over.addAll(this.entered);
 			this.entered.clear();
 		}
 		
 		this.exited.clear();
 		this.lpressed = false;
 		this.rpressed = false;
 		this.lreleased = false;
 		this.rreleased = false;
 	}
 	
 	@Override
 	protected Class<?> getSupportedClass()
 	{
 		return AdvancedMouseListener.class;
 	}
 	
 	
 	// GETTERS & SETTERS	----------------------------------------------
 	
 	/**
 	 * @return The mouse's current x-coordinate
 	 */
 	public int getMouseX()
 	{
 		return this.mouseX;
 	}
 	
 	/**
 	 * @return The mouse's current y-coordinate
 	 */
 	public int getMouseY()
 	{
 		return this.mouseY;
 	}
 	
 	/**
 	 * @return Is the left mouse button currently down
 	 */
 	public boolean leftIsDown()
 	{
 		return this.ldown;
 	}
 	
 	/**
 	 * @return Is the right mouse button currently down
 	 */
 	public boolean rightIsDown()
 	{
 		return this.rdown;
 	}
 	
 	/**
 	 * Informs the object about the mouse's current position
 	 *
 	 * @param x The mouse's current x-coordinate
 	 * @param y The mouse's current y-coordinate
 	 */
 	public void setMousePosition(int x, int y)
 	{		
 		if (getMouseX() != x || getMouseY() != y)
 		{		
 			this.mouseX = x;
 			this.mouseY = y;
 			
 			// Removes any dead handleds
 			removeDeadHandleds();
 			
			// Goes through all the listeners and informs them	
			for(int i = 0; i < getHandledNumber(); i++)
 			{
				AdvancedMouseListener l = (AdvancedMouseListener) getHandled(i);
 				
 				// Informs about the mouse's movement (if needed)
 				if (l.isActive())
 					l.onMouseMove(x, y);
 				
 				if (l.listensMouseEnterExit())
 				{		
 					// Checks if entered
 					if (!this.over.contains(l) && !this.entered.contains(l) 
 							&& l.listensPosition(x, y))
 					{
 						this.entered.add(l);
 						continue;
 					}
 
 					// Checks if exited
 					if (this.over.contains(l) && !this.exited.contains(l) && 
 							!l.listensPosition(x, y))
 					{
 						this.over.remove(l);
 						this.exited.add(l);
 					}
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Informs the object about the mouse's left button's status
 	 *
 	 * @param leftmousedown Is the mouse's left button down
 	 */
 	public void setLeftMouseDown(boolean leftmousedown)
 	{
 		if (this.ldown != leftmousedown)
 		{
 			this.ldown = leftmousedown;
 			
 			if (leftmousedown)
 				this.lpressed = true;
 			else
 				this.lreleased = true;
 		}
 	}
 	
 	/**
 	 * Informs the object about the mouse's right button's status
 	 *
 	 * @param rightmousedown Is the mouse's right button down
 	 */
 	public void setRightMouseDown(boolean rightmousedown)
 	{
 		if (this.rdown != rightmousedown)
 		{
 			this.rdown = rightmousedown;
 			
 			if (rightmousedown)
 				this.rpressed = true;
 			else
 				this.rreleased = true;
 		}
 	}
 	
 	
 	// OTHER METHODS	---------------------------------------------------
 	
 	/**
 	 * Adds a new listener to the informed listeners
 	 *
 	 * @param m The MouseListener added
 	 */
 	public void addMouseListener(AdvancedMouseListener m)
 	{
 		addHandled(m);
 	}
 }
