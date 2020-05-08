 
 package DeviceGraphicsDisplay;
 
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.Toolkit;
 import java.util.ArrayList;
 
 import javax.swing.JComponent;
 
import DeviceGraphics.DeviceGraphics;
import Networking.Client;
import Networking.Request;
import Utils.Constants;
import Utils.Location;
 import factory.data.PartType;
 
 /**
  * @author Vansh Jain
  * 
  */
 
 
 public class NestGraphicsDisplay extends DeviceGraphicsDisplay {
 	
 	private static Image nestImg;
 	
 	// max number of parts this Nest holds
 	private static final int MAX_PARTS=8;
 	// x-coordinate of the Nest
 	private static final int NEST_X=119;
 	// y-coordinate of the Nest
 	private static int NEST_Y;
 	// width and height of the nest
 	private static final int NEST_WIDTH=45; 
 	private static final int NEST_HEIGHT=80;
 	// width and height of a part
 	private static final int PART_WIDTH=20, PART_HEIGHT=21;
 	
 	// the LaneManager (client) which talks to the Server
 	private Client client;
 	// the id of this nest
 	private int nestID;
 	//boolean if the nest is full
 	private boolean isFull;
 	// dynamically stores the parts currently in the Nest
 	private ArrayList<PartGraphicsDisplay> partsInNest = new ArrayList<PartGraphicsDisplay>();
 	
 	
 	public NestGraphicsDisplay(Client x, int id) {
 		Client client = x;
 		nestID = id;
 		isFull=true;
 		nestImg = Toolkit.getDefaultToolkit().getImage("src/images/Nest.png");
 		if(nestID==0){
 			NEST_Y=100;
 		}
 		else if (nestID==1){
 			NEST_Y=175;
 		}
 		// Begin V0 requirements
 		
 		for (int i = 0; i < 8; i++) {
 			PartGraphicsDisplay temp = new PartGraphicsDisplay(PartType.A);
 			if(i<4){
 				temp.setLocation(new Location((NEST_X+i*20),(NEST_Y+1)));
 			}
 			else{
 				temp.setLocation(new Location((NEST_X+(i-4)*20),(NEST_Y+23))); 
 			}
 			partsInNest.add(temp);
 			
 		}
 		
 	}
 	
 	public void requestData(Request r) {
 		
 	}
 	
 	public void receivePart(PartGraphicsDisplay p) {
 		
 	}
 	
 //	public void givePartToPartsRobot(PartGraphicsDisplay) {		
 //	}
 	
 	public void purge() {
 		
 	}
 	
 	private void movePartsUp() {
 		
 	}
 
 	
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 
 	}
 
 
 	@Override
 	public void draw(JComponent c, Graphics2D g) {
 		// TODO Auto-generated method stub
 		
 		g.drawImage(Constants.NEST_IMAGE, NEST_X, NEST_Y, c);
 		for(int i=0; i<8; i++){
 			PartGraphicsDisplay temp = partsInNest.get(i);
 			temp.draw(c,g);
 		}
 	}
 
 	@Override
 	public void receiveData(Request req) {
 		// TODO Auto-generated method stub
 		
 	}
 
 
 	@Override
 	public void setLocation(Location newLocation) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 
 }
 
