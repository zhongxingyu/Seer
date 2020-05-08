 package net.infinitycoding.carsim.modules;
 
 import java.awt.Rectangle;
 
 import javax.swing.ImageIcon;
 
 public class Street
 {
 	Rectangle stopLine;
 	public int startX, startY;
 	TrafficLight trafficLight;
 	ImageIcon streetPic;
 	
 	public Street(Rectangle stopLine, int startX, int startY, TrafficLight trafficLight)
 	{
 		this.stopLine = stopLine;
 		this.startX = startX;
 		this.startY = startY;
 		this.trafficLight = trafficLight;
		
 	}
 }
