 package handlers;
 
 import java.awt.Graphics2D;
 import java.util.Iterator;
 
 import handleds.Drawable;
 import handleds.Handled;
 
 /**
  * The object from this class will draw multiple drawables, calling their 
  * drawSelf-methods and removing them when necessary
  *
  * @author Mikko Hilpinen.
  *         Created 27.11.2012.
  */
 public class DrawableHandler extends Handler implements Drawable
 {	
 	// ATTRIBUTES	------------------------------------------------------
 	
 	private int depth;
 	private boolean usesDepth;
 	
 	
 	// CONSTRUCTOR	------------------------------------------------------
 	
 	/**
 	 * Creates a new drawablehandler. Drawables must be added later manually.
 	 *
 	 * @param autodeath Will the handler die if it has no living drawables to handle
 	 * @param usesDepth Will the handler draw the objects in a depth-specific order
 	 * @param depth How 'deep' the objects in this handler are drawn
 	 * @param superhandler The drawablehandler that will draw this handler (optional)
 	 * @see depthConstants
 	 */
 	public DrawableHandler(boolean autodeath, boolean usesDepth, int depth, 
 			DrawableHandler superhandler)
 	{
 		super(autodeath, superhandler);
 		
 		// Initializes attributes
 		this.depth = depth;
 		this.usesDepth = usesDepth;
 	}
 	
 	
 	// IMPLEMENTED METHODS	----------------------------------------------
 	
 	@Override
 	public void drawSelf(Graphics2D g2d)
 	{
 		// Removes any dead handleds
 		removeDeadHandleds();
 		
 		// This calls for all active actor's act method
 		for(int i = 0; i < getHandledNumber(); i++)
 		{
 			Drawable d = (Drawable) getHandled(i);
 			
 			if (d.isVisible())
 				d.drawSelf(g2d);
 		}
 	}
 
 	@Override
 	public boolean isVisible()
 	{
 		// Returns false only if all the handleds are invisible
 		for (int i = 0; i < getHandledNumber(); i++)
 		{
 			Drawable d = (Drawable) getHandled(i);
 			
 			if (d.isVisible())
 				return true;
 		}
 		
 		return false;
 	}
 
 	@Override
 	public void setVisible()
 	{
 		// tries to set all the drawables visible
 		Iterator<Handled> iterator = getIterator();
 		
 		while (iterator.hasNext())
 		{
 			Drawable d = (Drawable) iterator.next();
 			d.setVisible();
 		}
 	}
 
 	@Override
 	public void setInvisible()
 	{
 		// tries to set all the drawables invisible
 		Iterator<Handled> iterator = getIterator();
 		
 		while (iterator.hasNext())
 		{
 			Drawable d = (Drawable) iterator.next();
 			d.setInvisible();
 		}
 	}
 	
 	@Override
 	public int getDepth()
 	{
 		return this.depth;
 	}
 	
 	@Override
 	public boolean setDepth(int depth)
 	{
 		this.depth = depth;
 		return true;
 	}
 	
 	@Override
 	protected void addHandled(Handled h)
 	{
 		// Can only add drawables
 		if (!(h instanceof Drawable))
 			return;
 		
 		Drawable d = (Drawable) h;
 		
 		// If the depth sorting is on, finds the spot for the object
 		if (this.usesDepth)
 		{
 			// Prints all depths in order
 			/*
 			System.out.println("*************************************");
 			for (int i = 0; i < getHandledNumber(); i++)
 			{
 				System.out.println("Depth " + (i + 1) + "/" + getHandledNumber() + ": " + 
 						((Drawable) getHandled(i)).getDepth());
 			}
 			*/
 			
 			// Repairs any problems in the depth sorting
 			checkDepthSorting();
 			
 			int index = 0;
 			int newdepth = d.getDepth();
			Iterator<Handled> iterator = getIterator();
 			
			while (iterator.hasNext())
 			{
				Drawable other = (Drawable) iterator.next();
 				
 				//System.out.println(newdepth + " -> " + other.getDepth() + "?");
 				//System.out.println(index);
 				
 				// Checks if there's an object with a higher depth
 				if (other.getDepth() < newdepth)
 				{
 					//System.out.println("Yes");
 					insertHandled(d, index);
 					return;
 				}
 				else
 					index ++;
 			}
 			// If no object with a higher depth was found, simply adds the 
 			// drawable to the end of the list as usual
 		}
 		
 		super.addHandled(d);
 	}
 	
 	@Override
 	protected Class<?> getSupportedClass()
 	{
 		return Drawable.class;
 	}
 	
 	
 	// OTHER METHODS	---------------------------------------------------
 	
 	/**
 	 *Adds the given drawable to the handled drawables
 	 *
 	 * @param d The drawable to be added
 	 */
 	public void addDrawable(Drawable d)
 	{
 		addHandled(d);
 	}
 	
 	// Checks if there are errors in current depth sorting and tries to fix them
 	private void checkDepthSorting()
 	{
 		int lastdepth = 900000;
		Iterator<Handled> ite = getIterator();
		while(ite.hasNext())
 		{
			Drawable drawable = (Drawable) ite.next();
 			if (drawable.getDepth() > lastdepth)
 			{
 				//System.out.println("ERROR IN DEPTH SORTING");
 				tryFixingDepthSorting(drawable);
 				break;
 			}
 			lastdepth = drawable.getDepth();
 		}
 	}
 	
 	// Tries to fix the depth sorting for the given object
 	private void tryFixingDepthSorting(Drawable mistake)
 	{
 		removeHandled(mistake);
 		addHandled(mistake);
 	}
 }
