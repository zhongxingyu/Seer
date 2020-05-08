 package DeviceGraphicsDisplay;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.Toolkit;
 import java.util.ArrayList;
 
 import javax.swing.JComponent;
 
 import Networking.Client;
 import Networking.Request;
 import Utils.Constants;
 import Utils.Location;
 import factory.PartType;
 
 public class PartsRobotDisplay extends DeviceGraphicsDisplay {
 	
 	private static Image partsRobotImage;
 	private static ArrayList<Image> armImage1;
 	private static ArrayList<Image> armImage2;
 	private static ArrayList<Image> armImage3;
 	private static ArrayList<Image> armImage4;
 	
 	
 	private ArrayList<PartGraphicsDisplay> partArrayGraphics;
 	
 	private Location loc, kitloc;
 	private Location initialLocation;
 	private Location currentLocation;
 	private Location armLocation;
 	
 	private boolean rotate;
 	private boolean home;
 	private boolean pickup, givekit;
 	private boolean gotpart;
 	private boolean sendmsg;
 	private boolean gavepart;
 	
 	private ArrayList<Location> partStartLoc;
 	private ArrayList<Location> armLoc;
 	private int I;
 	
 	public PartsRobotDisplay(Client prc){
		client = prc;
 		
 		initialLocation = Constants.PARTS_ROBOT_LOC; //new Location(250,450);
 		currentLocation = initialLocation;
 		armLocation = currentLocation;
 		
 		partsRobotImage = Toolkit.getDefaultToolkit().getImage("src/images/parts_robot.png");
 		armImage1 = new ArrayList<Image>();
 		armImage2 = new ArrayList<Image>();
 		armImage3 = new ArrayList<Image>();
 		armImage4 = new ArrayList<Image>();
 		int j;
 		for (j=0; j<3; j++){
 			if(j<2){
 				armImage1.add(Toolkit.getDefaultToolkit().getImage("src/images/parts_robot_arm.png"));
 				armImage2.add(Toolkit.getDefaultToolkit().getImage("src/images/parts_robot_arm.png"));
 				armImage3.add(Toolkit.getDefaultToolkit().getImage("src/images/parts_robot_arm.png"));
 				armImage4.add(Toolkit.getDefaultToolkit().getImage("src/images/parts_robot_arm.png"));
 			}
 			else{
 				armImage1.add(Toolkit.getDefaultToolkit().getImage("src/images/parts_robot_arm.png"));
 				armImage2.add(Toolkit.getDefaultToolkit().getImage("src/images/parts_robot_arm.png"));
 				armImage3.add(Toolkit.getDefaultToolkit().getImage("src/images/parts_robot_arm.png"));
 				armImage4.add(Toolkit.getDefaultToolkit().getImage("src/images/parts_robot_arm.png"));
 				
 			}
 		}
 		
 		partArrayGraphics = new ArrayList<PartGraphicsDisplay>();
 		
 		rotate = false;
 		home = true;
 		pickup = false;
 		givekit = false;
 		
 		partStartLoc = new ArrayList<Location>();
 		partStartLoc.add( new Location(currentLocation.getX(),currentLocation.getY()));
 		partStartLoc.add( new Location(currentLocation.getX()+30,currentLocation.getY()));
 		partStartLoc.add( new Location(currentLocation.getX(),currentLocation.getY()+30));
 		partStartLoc.add( new Location(currentLocation.getX()+30,currentLocation.getY()+30));
 		
 		armLoc = new ArrayList<Location>();
 		armLoc.add(new Location(armLocation.getX()+60,armLocation.getY()));
 		armLoc.add(new Location(armLocation.getX()+60,armLocation.getY()+30));
 		armLoc.add(new Location(armLocation.getX()+60,armLocation.getY()+60));
 		armLoc.add(new Location(armLocation.getX()+60,armLocation.getY()+90));
 		
 		I = 0;
 			
 		gotpart = false;
 		sendmsg = false;
 		gavepart = false;
 	}
 	
 	public void draw(JComponent c, Graphics2D g){
 		
 		if(pickup){
 			
 				if(currentLocation.getX()<loc.getX()-30){
 					currentLocation.incrementX(5);
 					
 					int k;
 					for (k=0;k<4;k++){
 						armLoc.get(k).setX(currentLocation.getX()+60);
 						partStartLoc.get(k).setX(armLoc.get(k).getX()+30);	
 					}
 					
 				
 				} else if(currentLocation.getX()>loc.getX()-30){
 					currentLocation.incrementX(-5);
 					
 					int k;
 					for (k=0;k<4;k++){
 						armLoc.get(k).setX(currentLocation.getX()+60);
 						partStartLoc.get(k).setX(armLoc.get(k).getX()+30);
 	
 					}
 					
 				}else if(currentLocation.getY()>loc.getY()){
 					currentLocation.incrementY(-5);
 					
 					int k;
 					for (k=0;k<4;k++){
 						armLoc.get(k).setY(currentLocation.getY()+30*(k));
 						partStartLoc.get(k).setY(armLoc.get(k).getY()+30*k);
 					}
 				}else if(currentLocation.getY()<loc.getY()){
 					currentLocation.incrementY(5);
 					
 					int k;
 					for (k=0;k<4;k++){
 						armLoc.get(k).setY(currentLocation.getY()+30*(k));
 						partStartLoc.get(k).setY(armLoc.get(k).getY()+30*k);
 					}
 				}
 				g.drawImage(partsRobotImage, currentLocation.getX() + client.getOffset(), currentLocation.getY(), c);
 				
 				if(currentLocation.getX() == loc.getX()-30 && currentLocation.getY() == loc.getY()){
 					System.out.println("at parts location");
 			
 					
 					if(armLoc.get(I).getX() != loc.getX()+90 && !gotpart){
 						extendArm();
 						
 						if(armLoc.get(I).getX() == loc.getX()+90){
 						pickUpPart();
 						gotpart = true;}}
 					
 					else {
 						System.out.println("got to part loc");
 						
 						if(armLoc.get(I).getX() != loc.getX()+30){
 							System.out.println("retract arm");
 							retractArm();
 							if(armLoc.get(I).getX() == loc.getX()+30){
 								pickup = false;
 								I++;
								client.sendData(new Request(
 									    Constants.PARTS_ROBOT_RECEIVE_PART_COMMAND + Constants.DONE_SUFFIX, 
 									    Constants.PARTS_ROBOT_TARGET,
 									    null));
 							}
 						}
 						
 					}
 					
 				}
 				int k;
 				for (k=0;k<3;k++){
 					g.drawImage(armImage1.get(k),armLoc.get(0).getX() + client.getOffset(), armLoc.get(0).getY(), c);
 					g.drawImage(armImage2.get(k),armLoc.get(1).getX() + client.getOffset(), armLoc.get(1).getY(), c);
 					g.drawImage(armImage3.get(k),armLoc.get(2).getX() + client.getOffset(), armLoc.get(2).getY(), c);
 					g.drawImage(armImage4.get(k),armLoc.get(3).getX() + client.getOffset(), armLoc.get(3).getY(), c);
 				}
 				
 		}else if (givekit) {
 			for (int i = 0; i < 5; i++){
 				if(currentLocation.getX()>kitloc.getX()-30){
 					currentLocation.incrementX(-1);
 					int k;
 					for (k=0;k<4;k++){
 						armLoc.get(k).setX(currentLocation.getX()+60);
 						partStartLoc.get(k).setX(armLoc.get(k).getX()+30);	
 					}
 				}
 				else if(currentLocation.getY()>kitloc.getY()){
 					currentLocation.incrementY(-1);
 					int k;
 					for (k=0;k<4;k++){
 						armLoc.get(k).setY(currentLocation.getY()+30*(k));
 						partStartLoc.get(k).setY(armLoc.get(k).getY()+30*k);
 					}
 				}else if(currentLocation.getY()<kitloc.getY()){
 					currentLocation.incrementY(1);
 					int k;
 					for (k=0;k<4;k++){
 						armLoc.get(k).setY(currentLocation.getY()+30*(k));
 						partStartLoc.get(k).setY(armLoc.get(k).getY()+30*k);
 					}
 				}
 				
 			}
 			g.drawImage(partsRobotImage, currentLocation.getX() + client.getOffset(), currentLocation.getY(), c);
 			
 			if (currentLocation.getX() == kitloc.getX()-30 && currentLocation.getY() == kitloc.getY()){
 				//Location tempLoc = armLoc.get(I-1);
 				System.out.println("got to kit location");
 				if(armLoc.get(I-1).getX() != kitloc.getX()+90 && !gavepart){
 					System.out.println("extending arm to kit");
 					extendArmToKit();
 					if(armLoc.get(I-1).getX() == kitloc.getX()+90){
 						System.out.println("giving part to kit");
 						
 						givePart();
 						gavepart = true;
 					}
 					}
 				else{
 					
 					if(armLoc.get(I-1).getX() != kitloc.getX()+30){
 						System.out.println("retract arm from kit");
 						retractArmFromKit();
 						if(armLoc.get(I-1).getX() == kitloc.getX()+30){
 							System.out.println("done giving to kit");
 							givekit = false;
 							I--;
							client.sendData(new Request(
 								    Constants.PARTS_ROBOT_GIVE_COMMAND + Constants.DONE_SUFFIX, 
 								    Constants.PARTS_ROBOT_TARGET,
 								    null));
 						}
 					}
 				}	
 			}
 			int k;
 			for (k=0;k<3;k++){
 				g.drawImage(armImage1.get(k),armLoc.get(0).getX() + client.getOffset(), armLoc.get(0).getY(), c);
 				g.drawImage(armImage2.get(k),armLoc.get(1).getX() + client.getOffset(), armLoc.get(1).getY(), c);
 				g.drawImage(armImage3.get(k),armLoc.get(2).getX() + client.getOffset(), armLoc.get(2).getY(), c);
 				g.drawImage(armImage4.get(k),armLoc.get(3).getX() + client.getOffset(), armLoc.get(3).getY(), c);
 			}
 		} else if (!givekit || !pickup) {
 			for (int i = 0; i < 5; i++){
 				if(currentLocation.getY()<450){
 					currentLocation.incrementY(1);
 					int k;
 					for (k=0;k<4;k++){
 						armLoc.get(k).setY(currentLocation.getY()+30*k);
 						partStartLoc.get(k).setY(armLoc.get(k).getY()+30*k);
 					}
 				}
 				else if(currentLocation.getX()>250){
 					currentLocation.incrementX(-1);
 					int k;
 					for (k=0;k<4;k++){
 						armLoc.get(k).setX(currentLocation.getX()+60);
 						partStartLoc.get(k).setX(armLoc.get(k).getX()+30);
 					}
 				}
 				else if(currentLocation.getX()<250){
 					currentLocation.incrementX(1);
 					int k;
 					for (k=0;k<4;k++){
 						armLoc.get(k).setX(currentLocation.getX()+60);
 						partStartLoc.get(k).setX(armLoc.get(k).getX()+30);
 					}
 				}
 				
 				
 				g.drawImage(partsRobotImage, currentLocation.getX() + client.getOffset(), currentLocation.getY(), c);
 				int k;
 				for (k=0;k<3;k++){
 					g.drawImage(armImage1.get(k),armLoc.get(0).getX() + client.getOffset(), armLoc.get(0).getY(), c);
 					g.drawImage(armImage2.get(k),armLoc.get(1).getX() + client.getOffset(), armLoc.get(1).getY(), c);
 					g.drawImage(armImage3.get(k),armLoc.get(2).getX() + client.getOffset(), armLoc.get(2).getY(), c);
 					g.drawImage(armImage4.get(k),armLoc.get(3).getX() + client.getOffset(), armLoc.get(3).getY(), c);
 				}
 			}
 		}else if(home){
 		System.out.println("arm2");
 			g.drawImage(partsRobotImage, initialLocation.getX() + client.getOffset(), initialLocation.getY(), c);
 			
 			int k;
 			for (k=0;k<3;k++){
 				g.drawImage(armImage1.get(k),armLoc.get(0).getX() + client.getOffset(), armLoc.get(0).getY(), c);
 				g.drawImage(armImage2.get(k),armLoc.get(1).getX() + client.getOffset(), armLoc.get(1).getY(), c);
 				g.drawImage(armImage3.get(k),armLoc.get(2).getX() + client.getOffset(), armLoc.get(2).getY(), c);
 				g.drawImage(armImage4.get(k),armLoc.get(3).getX() + client.getOffset(), armLoc.get(3).getY(), c);
 			}
 		}
 		
 		for (int i = 0; i < partArrayGraphics.size(); i++){
 			PartGraphicsDisplay pgd = partArrayGraphics.get(i);
 			pgd.getLocation().incrementX(client.getOffset());
 			pgd.draw(c, g);
 		}
 	
 	}
 	
 	public void pickUp(){
 		home = false;
 		pickup = true;
 		givekit = false;
 		gotpart = false;
 		gavepart = false;
 	}
 	
 	public void giveKit(){
 		home = false;
 		givekit = true;
 	}
 	public void pickUpPart(){
 		//partArrayGraphics.add(pgd);
 		PartType partType = Constants.DEFAULT_PARTTYPES.get(0);
 		PartGraphicsDisplay pgd = new PartGraphicsDisplay(partType);
 		
 		if (I<4){
 			pgd.setLocation(partStartLoc.get(I));
 			partArrayGraphics.add(pgd);
 		
 			
 			
 		}
 		else
 			System.out.println("Can't pick up more parts.");
 	}
 	
 	public void givePart(){
 		
 		if(I>0){
 			
 			partArrayGraphics.remove(I-1);
 			
 		}else
 			System.out.println("No parts to remove.");
 		
 		
 	}
 	
 	public void extendArm(){
 		
 			//if(armLocation.getX() != loc.getX()){
 		
 		if(I==0){
 			armLoc.get(0).incrementX(1);
 			partStartLoc.get(0).setX(armLoc.get(0).getX()+30);
 		}
 		else if(I==1){
 			armLoc.get(1).incrementX(1);
 			partStartLoc.get(1).setX(armLoc.get(1).getX()+30);
 		}
 		else if(I==2){
 			armLoc.get(2).incrementX(1);
 			partStartLoc.get(2).setX(armLoc.get(2).getX()+30);
 		}
 		else if(I==3){
 			armLoc.get(3).incrementX(1);
 			partStartLoc.get(3).setX(armLoc.get(3).getX()+30);
 		}
 				
 		
 	}
 	public void extendArmToKit(){
 		
 		//if(armLocation.getX() != loc.getX()){
 	
 	if(I==1){
 		armLoc.get(0).incrementX(1);
 		partStartLoc.get(0).setX(armLoc.get(0).getX()+30);
 	}
 	else if(I==2){
 		armLoc.get(1).incrementX(1);
 		partStartLoc.get(1).setX(armLoc.get(1).getX()+30);
 	}
 	else if(I==3){
 		armLoc.get(2).incrementX(1);
 		partStartLoc.get(2).setX(armLoc.get(2).getX()+30);
 	}
 	else if(I==4){
 		armLoc.get(3).incrementX(1);
 		partStartLoc.get(3).setX(armLoc.get(3).getX()+30);
 	}
 			
 	
 }
 	
 	public void retractArm(){
 		
 		//if(armLocation.getX() == loc.getX()){
 		
 		if(I==0){
 			armLoc.get(0).incrementX(-1);
 			partStartLoc.get(0).setX(armLoc.get(0).getX()+30);
 		}
 		else if(I==1){
 			armLoc.get(1).incrementX(-1);
 			partStartLoc.get(1).setX(armLoc.get(1).getX()+30);
 		}
 		else if(I==2){
 			armLoc.get(2).incrementX(-1);
 			partStartLoc.get(2).setX(armLoc.get(2).getX()+30);
 		}
 		else if(I==3){
 			armLoc.get(3).incrementX(-1);
 			partStartLoc.get(3).setX(armLoc.get(3).getX()+30);
 		}
 		
 	}
 	
 public void retractArmFromKit(){
 		
 		//if(armLocation.getX() == loc.getX()){
 	System.out.println("go to function");
 	if(I==1){
 		System.out.println("retracting");
 			armLoc.get(0).incrementX(-1);
 			System.out.println("RECTRACTING");
 			
 		}
 		else if(I==2){
 			armLoc.get(1).incrementX(-1);
 			
 		}
 		else if(I==3){
 			armLoc.get(2).incrementX(-1);
 			
 		}
 		else if(I==4){
 			armLoc.get(3).incrementX(-1);
 		
 		}
 		
 	}
 	
 	public void rotateArm(){
 		rotate = true;
 	}
 	
 	public boolean getRotate(){
 		return rotate;
 	}
 	
 	public void receiveData(Request r) {
 		if (r.getCommand().equals(Constants.PARTS_ROBOT_GIVE_COMMAND)){
 			kitloc = (Location) r.getData();
 			giveKit();
 		}else if (r.getCommand().equals(Constants.PARTS_ROBOT_PICKUP_COMMAND)){
 			loc = (Location) r.getData();
 			pickUp();
 			System.out.println("before pick up");
 		}
 	}
 
 	@Override
 	public void setLocation(Location newLocation) {
 		// TODO Auto-generated method stub
 		
 	}
 
 }
