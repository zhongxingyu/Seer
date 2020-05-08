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
 		int				index;
		StringBuffer	aBuffer = new StringBuffer();;
 		
 		//g.setFont(new Font("Monospaced", Font.PLAIN, 12));
 		
 		for (index = 0; index < mModel.getQuota(); index++)
 		{
 			if (index < mModel.getKills())
 			{
				aBuffer.append ("- ");
 			}
 			else
 			{
				aBuffer.append(mModel.getTarget().getAppearance());
 			}
			
			aBuffer.append(" ");
 		}
		g.drawString(aBuffer.toString(), 10, 20);
 	}
 }
