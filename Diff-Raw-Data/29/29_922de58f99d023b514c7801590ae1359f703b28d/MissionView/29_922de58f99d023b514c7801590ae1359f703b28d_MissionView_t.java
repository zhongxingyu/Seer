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
  * MissionView provides a view of what kind and how many monsters must
  * be killed to complete the current mission.
  *
  * @author	Bill Walker
  * @version	$Id$
  */
 public class MissionView extends Canvas implements Observer
 {
	/** * The Mission being visualized. */
 	private Mission	mModel;
 	
	/**
	 * Creates a new MissionView for the given Mission.
	 */
 	public MissionView(Mission aMission)
 	{
 		mModel = aMission;
 		
 		setBackground(Color.black);
 		setForeground(JCavernApplet.CavernOrange);
 		aMission.addObserver(this);
 	}
 	
	/**
	 * Sets the Mission to be visualized.
	 *
	 * @param	aModel		a non-null Mission
	 */
 	public void setModel(Mission aModel)
 	{
 		mModel.deleteObserver(this);
 		mModel = aModel;
 		mModel.addObserver(this);
 		repaint();
 	}
 	
	/**
	 * Reponds to update messages from the Mission being visualized by repainting.
	 *
	 * @param	a	the Mission being visualized
	 * @param	b	not in use
	 */
 	public void update(Observable a, Object b)
 	{
 		repaint();
 	}
 	
	/**
	 * Paints a visualization of a Mission.
	 * The visualization includes a thermometer-style progress indicator
	 * as well as a textual representation of the state of the Mission.
	 *
	 * @param	g 	a non-null Graphics object for painting.
	 */
 	public void paint(Graphics g)
 	{
 		int		index;
 		int		height = getSize().height;
 		int		width = getSize().width;
 		double	done = 1.0 * mModel.getKills() / mModel.getQuota();
 		
 		try
 		{
			mModel.getTarget().paint(g, 20, 20, false);
 		}
 		catch (JCavernInternalError jcie)
 		{
 			System.out.println("can't paint target in mission view " + jcie);
 		}
 
 		g.drawRect(40, 10, width - 41, 5);
 		g.fillRect(40, 10, (int) (done * (width - 41)), 5);
 		
 		g.drawString(mModel.getKills() + "/" + mModel.getQuota() + " " + mModel.getTarget().getName() + "s", 40, 30);		
 	}
 }
