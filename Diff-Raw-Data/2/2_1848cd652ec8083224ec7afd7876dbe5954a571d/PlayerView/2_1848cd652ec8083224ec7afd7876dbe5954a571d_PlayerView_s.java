 /* 
 	JCavernApplet.java
 
 	Title:			JCavern And Glen
 	Author:			Bill Walker
 	Description:	
 */
 
 package jcavern.ui;
 
 import jcavern.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.util.*;
 
 /**
  * PlayerView displays the player's current statistics.
  *
  * @author	Bill Walker
  * @version	$Id$
  */
 public class PlayerView extends Canvas implements Observer
 {
 	private Player	mModel;
 	
 	public PlayerView(Player aPlayer)
 	{
 		mModel = aPlayer;
 		
 		setBackground(Color.black);
 		setForeground(JCavernApplet.CavernOrange);
 		
 		aPlayer.addObserver(this);
 	}
 
 	public void update(Observable a, Object b)
 	{
 		repaint();
 	}
 	
 	public void paint(Graphics g)
 	{
 		setBackground(Color.black);
 		setForeground(JCavernApplet.CavernOrange);
 
 		int y = g.getFontMetrics().getHeight();
 		int lineHeight = g.getFontMetrics().getHeight();
 
		g.drawString(mModel.getName(), 1, y); y += lineHeight;
 		
 		g.drawString("Points: " + mModel.getPoints() + "/" + mModel.getMaximumPoints(), 1, y); y += lineHeight;
 		g.drawString("Gold: " + mModel.getGold(), 1, y); y += lineHeight;
 		g.drawString("Arrows: " + mModel.getArrows(), 1, y); y += lineHeight;
 		g.drawString("Moves: " + mModel.getMoveCount(), 1, y); y += lineHeight; y += lineHeight;
 		
 		g.drawString("In Use:", 1, y); y += lineHeight; 
 			
 		Vector items = mModel.getInUseItems();
 				
 		for (int index = 0; index < items.size(); index++)
 		{
 			g.drawString(index + " " + ((Treasure) items.elementAt(index)).toString(), 1, y); y += lineHeight;
 		}
 		
 		y += lineHeight;
 		g.drawString("Unused:", 1, y); y += lineHeight;
 			
 		Vector items2 = mModel.getUnusedItems();
 		
 		for (int index = 0; index < items2.size(); index++)
 		{
 			g.drawString(index + " " + ((Treasure) items2.elementAt(index)).toString(), 1, y); y += lineHeight;
 		}
 	}
 }
