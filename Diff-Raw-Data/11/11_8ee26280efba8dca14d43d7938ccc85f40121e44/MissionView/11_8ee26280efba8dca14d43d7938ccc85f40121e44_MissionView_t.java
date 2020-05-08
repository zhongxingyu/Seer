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
  * MissionView provides a view of what kind and how many monsters must
  * be killed to complete the current mission.
  *
  * @author	Bill Walker
  * @version	$Id$
  */
 public class MissionView extends Canvas implements Observer
 {
 	private Mission	mModel;
 	
 	public MissionView(Mission aMission)
 	{
 		mModel = aMission;
 		
 		setBackground(Color.black);
 		setForeground(JCavernApplet.CavernOrange);
 		aMission.addObserver(this);
 	}
 	
 	public void setModel(Mission aModel)
 	{
 		mModel.deleteObserver(this);
 		mModel = aModel;
 		mModel.addObserver(this);
 		repaint();
 	}
 	
 	public void update(Observable a, Object b)
 	{
 		repaint();
 	}
 	
 	public void paint(Graphics g)
 	{
		System.out.println("MissionView.painr()");
		
		int	index;
		int	height = getSize().height;
 		
 		//g.setFont(new Font("Monospaced", Font.PLAIN, 12));
 		
 		for (index = 0; index < mModel.getQuota(); index++)
 		{
 			if (index < mModel.getKills())
 			{
				g.drawString("-", 20 + 40 * index, height / 2);
 			}
 			else
 			{
				mModel.getTarget().paint(g, 20 + 40 * index, height / 2);
 			}
 		}
 	}
 }
