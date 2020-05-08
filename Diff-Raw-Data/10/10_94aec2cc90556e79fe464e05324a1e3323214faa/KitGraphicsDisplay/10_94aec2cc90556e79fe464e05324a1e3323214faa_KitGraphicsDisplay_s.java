 package DeviceGraphicsDisplay;
 
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.geom.AffineTransform;
 import java.util.ArrayList;
 
 import javax.swing.JComponent;
 
 import Networking.Client;
 import Networking.Request;
 import Utils.Constants;
 import Utils.Location;
 import factory.PartType;
 
 public class KitGraphicsDisplay extends DeviceGraphicsDisplay {
 	private static final int MAX_PARTS = 8;
 
 	private Location kitLocation;
 	
 	// RotationPart
 
 	private int position;
 
 	private boolean rotating;
 
 	private ArrayList<PartGraphicsDisplay> parts = new ArrayList<PartGraphicsDisplay>();
 	
 	Image kitImage;
 	
 	private Client kitClient;
 	
 	public Image getKitImage() {
 		return kitImage;
 	}
 
 	public void setKitImage(Image kitImage) {
 		this.kitImage = kitImage;
 	}
 
 	public KitGraphicsDisplay(Client kitClient){
 		kitLocation = Constants.KIT_LOC;
 		position = 0;
 		kitImage = Constants.KIT_IMAGE;
 		this.kitClient = kitClient;
		
 	}
 	public KitGraphicsDisplay() {

 		kitLocation = Constants.KIT_LOC;
 		position = 0;
 		kitImage = Constants.KIT_IMAGE;
 	}
 
 	public int getPosition() {
 		return position;
 	}
 
 	public void setPosition(int position) {
 		this.position = position;
 	}
 
 	public void setLocation(Location newLocation) {
 		kitLocation = newLocation;
 	}
 
 	public Location getLocation() {
 		return kitLocation;
 	}
 	
 	public void draw(JComponent c, Graphics2D g) {
 		drawWithOffset(c, g, 0);
 	}
 
 	public void drawWithOffset(JComponent c, Graphics2D g, int offset) {
 		g.drawImage(kitImage, kitLocation.getX() + offset,
 				kitLocation.getY(), c);
 
 		//TODO fix so that it draws the actual parts
 		for (PartGraphicsDisplay part : parts) {
 			g.drawImage(part.getPartType().getImage(), part.getLocation().getX() + offset, part
 					.getLocation().getY(), c);
 		}
 
 	}
 
 	public void receiveData(Request req) {
 		if (req.getCommand().equals(Constants.KIT_UPDATE_PARTS_LIST_COMMAND)) {
 			PartType type = (PartType) req.getData();
 			receivePart(new PartGraphicsDisplay(type));
 		}
 	}
 
 
 	public void receivePart(PartGraphicsDisplay pgd) {
 		parts.add(pgd);
 
 		// set location of the part
 		if ((parts.size() % 2) == 1) {
 			pgd.setLocation(new Location(kitLocation.getX() + 5, kitLocation
 					.getY() + (20 * (parts.size() - 1) / 2)));
 
 		} else {
 			pgd.setLocation(new Location(kitLocation.getX() + 34, kitLocation
 					.getY() + (20 * (parts.size() - 2) / 2)));
 		}
 
 		if (parts.size() == MAX_PARTS) {
 			parts.clear();
 		}
 
 	}
 
 }
