 package DeviceGraphicsDisplay;
 
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.Toolkit;
 
 import javax.swing.JComponent;
 
 import Networking.Request;
 import Utils.Location;
 
 public class KitGraphicsDisplay extends DeviceGraphicsDisplay {
 	
 	Location kitLocation;
 	
 	//NEED IMAGE NAMES
 	Image kitImage = Toolkit.getDefaultToolkit().getImage("Kit.jpg");
 
	public KitGraphicsDisplay () {
		
	}
 	
 	public void setLocation (Location newLocation) {
 		kitLocation = newLocation;
 	}
 
 	public void draw(JComponent c, Graphics2D g) {
 		g.drawImage(kitImage, kitLocation.getX(), kitLocation.getY(), c);
 		
 	}
 
 	public void receiveData(Request req) {
 		
 	}
 
 	
 }
