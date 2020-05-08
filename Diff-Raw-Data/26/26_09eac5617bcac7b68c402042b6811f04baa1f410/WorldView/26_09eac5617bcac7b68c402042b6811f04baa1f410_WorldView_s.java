 /* 
 	JCavernApplet.java
 
 	Title:			JCavern And Glen
 	Author:			Bill Walker
 	Description:	
 */
 
 package jcavern;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.util.*;
 
 /**
  * WorldView displays a view of a world centered around a player's location.
  *
  * @author	Bill Walker
  * @version	$Id$
  */
 public class WorldView extends Canvas implements Observer
 {
 	/** * Number of pixels between board pictures. */
 	private static final int	kSpacing = 40;
 	
 	/** * The World being viewed by this View. */
 	private World				mModel;
 	
 	/**
 	 * Creates a new WorldView for the given World.
 	 *
 	 * @param	aWorld	a non-null World being viewed.
 	 */
 	public WorldView(World aWorld)
 	{
 		System.out.println("Create WorldView");
 		
 		mModel = aWorld;
 		
 		setBackground(Color.black);
 		setForeground(JCavernApplet.CavernOrange);
 	}
 	
 	/**
 	 * Receives update notification that the World being viewed has changed.
 	 */
 	public void update(Observable a, Object b)
 	{
 		repaint();
 	}
 	
 	/**
 	 * Paints a view of the world, centered around the player's current location.
 	 */
 	public void paint(Graphics g)
 	{
 		Player		thePlayer = mModel.getPlayer();
 
 
 		if (thePlayer == null)
 		{
 			return;
 		}
 		
 		try
 		{
			Location	theLocation = mModel.getLocation(thePlayer).enforceMinimumInset(mModel.getBounds(), 3);
 		
 			for (int yIndex = -3; yIndex <= 3; yIndex++)
 			{
 				for (int xIndex = -3; xIndex <= 3; xIndex++)
 				{
 					Location	aLocation = new Location(xIndex + theLocation.getX(), yIndex + theLocation.getY());
					int			plotX = kSpacing + kSpacing * (xIndex + 3);
					int			plotY = kSpacing + kSpacing * (yIndex + 3);
 					
					if (aLocation.inBounds(mModel.getBounds()))
 					{
 						if (! mModel.isEmpty(aLocation))
 						{
 							Thing theThing = mModel.getThing(aLocation);
 					
 							theThing.paint(g, plotX, plotY);
 						}
 						else
 						{
 							g.drawString(".", plotX, plotY);
 						}
 					}
					else
					{
						//g.fillRect(plotX - kSpacing / 2, plotY - kSpacing / 2, kSpacing, kSpacing);
					}
 				}
 			}
 		}
 		catch (JCavernInternalError jcie)
 		{
 			System.out.println("Can't paint world view, " + jcie);
 		}
 		catch (EmptyLocationException ele)
 		{
 			System.out.println("Can't paint world view, internal error " + ele);
 		}
 		catch (IllegalLocationException ile)
 		{
 			System.out.println("Illegal location exception, " + ile);
 		}
 	}
 }
