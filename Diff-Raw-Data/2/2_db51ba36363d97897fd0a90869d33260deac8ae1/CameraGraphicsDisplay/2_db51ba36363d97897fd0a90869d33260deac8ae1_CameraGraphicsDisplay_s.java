 package DeviceGraphicsDisplay;
 
 import java.awt.Graphics2D;
 import java.util.ArrayList;
 
 import javax.swing.JComponent;
 
 import Networking.Client;
 import Networking.Request;
 import Utils.Constants;
 import Utils.Location;
 
 /**
  * Client-side Camera object
  * 
  * @author Peter Zhang
  */
 public class CameraGraphicsDisplay extends DeviceGraphicsDisplay{
 	
 	private int flashOn = -1;
 	
 	// locations to take pictures from
	private ArrayList<Location> locs;
 	
 	public CameraGraphicsDisplay(Client c, Location loc) {
 		//TODO: No need for location
 		client = c;
 		location = loc;
 	}
 
 	@Override
 	public void draw(JComponent c, Graphics2D g) {
 		if(flashOn >= 0) {
 			for(Location loc : locs) {
 				g.drawImage(Constants.CAMERA_IMAGE, loc.getX(), loc.getY(), c);
 			}
 			flashOn--;
 		} else {
 			locs.clear();
 		}
 	}
 
 	@Override
 	public void receiveData(Request req) {
 		if (req.getCommand().equals(Constants.CAMERA_TAKE_NEST_PHOTO_COMMAND)) {
 			locs = (ArrayList<Location>) req.getData();
 			flashOn = 3;
 		} else if (req.getCommand().equals(Constants.CAMERA_TAKE_NEST_PHOTO_COMMAND)) {
 			locs.add((Location) req.getData());
 			flashOn = 3;
 		}
 	}
 
 	@Override
 	public void setLocation(Location newLocation) {
 		location = newLocation;
 	}
 
 }
