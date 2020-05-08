 package org.concord.sensor.applet;
 
 import java.applet.Applet;
 import java.awt.Graphics;
 
 public class DetectionApplet extends Applet {
 	private static final long serialVersionUID = 1L;
 	
 	@Override
 	public void paint(Graphics g) {
 		g.clearRect(0, 0, getWidth(), getHeight());
 		
 		g.drawString("Loading...", (getWidth()/2)-29, (getHeight()/2)+7);
 	}
 	
 	public boolean areYouLoaded() {
 		return true;
 	}
 
 }
