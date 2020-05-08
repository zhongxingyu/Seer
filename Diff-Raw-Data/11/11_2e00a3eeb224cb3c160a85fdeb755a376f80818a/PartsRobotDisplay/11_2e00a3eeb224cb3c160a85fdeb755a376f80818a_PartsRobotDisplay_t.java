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
 import Utils.PartData;
 import factory.PartType;
 
 public class PartsRobotDisplay extends DeviceGraphicsDisplay {
 
 	private static Image partsRobotImage;
 	private static ArrayList<Image> armImage;
 
 	private final ArrayList<PartGraphicsDisplay> partArrayGraphics;
 
 	private Location loc, kitloc;
 	private final Location initialLocation;
 	private final Location currentLocation;
 	private final Location armLocation;
 
 	private PartType pt;
 
 	private boolean rotate;
 	private boolean home;
 	private boolean pickup, givekit;
 	private boolean gotpart;
 	private final boolean sendmsg;
 	private boolean gavepart;
 
 	private final ArrayList<Location> partStartLoc;
 	private final ArrayList<Location> armLoc;
 	private int I;
 
 	public PartsRobotDisplay(Client prc) {
 		client = prc;
 
 		initialLocation = Constants.PARTS_ROBOT_LOC;
 		currentLocation = initialLocation;
 		armLocation = currentLocation;
 
 		partsRobotImage = Toolkit.getDefaultToolkit().getImage("src/images/parts_robot.png");
 		armImage = new ArrayList<Image>();
 		
 		for (int j = 0; j < 4; j++) {
 			armImage.add(Toolkit.getDefaultToolkit().getImage("src/images/parts_robot_arm.png"));
 		}
 
 		partArrayGraphics = new ArrayList<PartGraphicsDisplay>();
 
 		rotate = false;
 		home = true;
 		pickup = false;
 		givekit = false;
 
 		partStartLoc = new ArrayList<Location>();
 		partStartLoc.add(new Location(currentLocation.getX(), currentLocation.getY()));
 		partStartLoc.add(new Location(currentLocation.getX() + 30, currentLocation.getY()));
 		partStartLoc.add(new Location(currentLocation.getX(), currentLocation.getY() + 30));
 		partStartLoc.add(new Location(currentLocation.getX() + 30, currentLocation.getY() + 30));
 
 		armLoc = new ArrayList<Location>();
 		armLoc.add(new Location(armLocation.getX(), armLocation.getY()));
 		armLoc.add(new Location(armLocation.getX(), armLocation.getY() + 30));
 		armLoc.add(new Location(armLocation.getX(), armLocation.getY() + 60));
 		armLoc.add(new Location(armLocation.getX(), armLocation.getY() + 90));
 		
 
 		I = 0;
 
 		gotpart = false;
 		sendmsg = false;
 		gavepart = false;
 	}
 
 	@Override
 	public void draw(JComponent c, Graphics2D g) {
 
 		if (pickup) {
 			for (int i = 0; i < 5; i++) {
 				if (currentLocation.getX() < loc.getX() - 60) {
 					currentLocation.incrementX(1);
 					updateArmLoc(currentLocation);
 					updatePartLoc(armLoc);
 				} else if (currentLocation.getX() > loc.getX() - 60) {
 					currentLocation.incrementX(-1);
 					updateArmLoc(currentLocation);
 					updatePartLoc(armLoc);
 				} else if (currentLocation.getY() > loc.getY()) {
 					currentLocation.incrementY(-1);
 					updateArmLoc(currentLocation);
 					updatePartLoc(armLoc);
 				} else if (currentLocation.getY() < loc.getY()) {
 					currentLocation.incrementY(1);
 					updateArmLoc(currentLocation);
 					updatePartLoc(armLoc);
 				}
 			}
 
 			if (currentLocation.getX() == loc.getX() - 60 && currentLocation.getY() == loc.getY()) {
 				// System.out.println("at parts location");
 
 				if (armLoc.get(I).getX() != loc.getX() && !gotpart) {
 					for (int i = 0; i < 5; i++) {
 						extendArm();
 					}
 
 					if (armLoc.get(I).getX() == loc.getX()) {
 						pickUpPart();
 						// System.out.println("Array Size after Pickup: " +partArrayGraphics.size());
 						gotpart = true;
 					}
 				}
 
 				else {
 					// System.out.println("got to part loc");
 
					if (armLoc.get(I).getX() != loc.getX() -60) {
 						// System.out.println("retract arm");
 						for (int i = 0; i < 5; i++) {
 							retractArm();
 						}
						if (armLoc.get(I).getX() == loc.getX() - 60) {
 							pickup = false;
 							I++;
 							client.sendData(new Request(Constants.PARTS_ROBOT_RECEIVE_PART_COMMAND
 									+ Constants.DONE_SUFFIX, Constants.PARTS_ROBOT_TARGET, null));
 						}
 					}
 
 				}
 
 			}
 			
 			for (int k = 0; k < 4; k++) {
 				g.drawImage(armImage.get(k), armLoc.get(k).getX() + client.getOffset(), armLoc.get(k).getY(), c);
 			}
 
 			g.drawImage(partsRobotImage, currentLocation.getX() + client.getOffset(), currentLocation.getY(), c);
 
 		} else if (givekit) {
 			// System.out.println("Array Size: " +partArrayGraphics.size());
 			for (int i = 0; i < 5; i++) {
 				if (currentLocation.getX() > kitloc.getX() - 60) {
 					currentLocation.incrementX(-1);
 					updateArmLoc(currentLocation);
 					updatePartLoc(armLoc);
 				} else if (currentLocation.getY() > kitloc.getY()) {
 					currentLocation.incrementY(-1);
 					updateArmLoc(currentLocation);
 					updatePartLoc(armLoc);
 				} else if (currentLocation.getY() < kitloc.getY()) {
 					currentLocation.incrementY(1);
 					updateArmLoc(currentLocation);
 					updatePartLoc(armLoc);
 				}
 			}
 
 			if (currentLocation.getX() == kitloc.getX() - 60 && currentLocation.getY() == kitloc.getY()) {
 				// System.out.println("got to kit location");
 				if (partArrayGraphics.isEmpty()) {
 					givekit = false;
 					gavepart = true;
 					I = 0;
 				} else if (armLoc.get(I - 1).getX() != kitloc.getX() && !gavepart) {
 					// System.out.println("extending arm to kit");
 					for (int i = 0; i < 5; i++) {
 						extendArmToKit();
 					}
 					if (armLoc.get(I - 1).getX() == kitloc.getX()) {
 						// System.out.println("giving part to kit");
 						PartType tempPartType = givePart();
 						gavepart = true;
 						client.sendData(new Request(Constants.PARTS_ROBOT_GIVE_COMMAND + Constants.DONE_SUFFIX,
 								Constants.PARTS_ROBOT_TARGET, tempPartType));
 					}
 				} else {
					if (armLoc.get(I - 1).getX() != kitloc.getX() - 60) {
 						// System.out.println("retract arm from kit");
 						for (int i = 0; i < 5; i++) {
 							retractArmFromKit();
 						}
 						// System.out.println("value of I: " + I);
						if (armLoc.get(I - 1).getX() == kitloc.getX() - 60) {
 							// System.out.println("done giving to kit");
 							I--;
 							gavepart = false;
 						}
 					}
 				}
 
 			}
 			
 			for (int k = 0; k < 4; k++) {
 				g.drawImage(armImage.get(k), armLoc.get(k).getX() + client.getOffset(), armLoc.get(k).getY(), c);
 			}
 			g.drawImage(partsRobotImage, currentLocation.getX() + client.getOffset(), currentLocation.getY(), c);
 
 		} else if (!givekit && !pickup) {
 			for (int i = 0; i < 5; i++) {
 				if (currentLocation.getY() < 450) {
 					currentLocation.incrementY(1);
 					updateArmLoc(currentLocation);
 					updatePartLoc(armLoc);
 				} else if (currentLocation.getX() > 250) {
 					currentLocation.incrementX(-1);
 					updateArmLoc(currentLocation);
 					updatePartLoc(armLoc);
 				} else if (currentLocation.getX() < 250) {
 					currentLocation.incrementX(1);
 					updateArmLoc(currentLocation);
 					updatePartLoc(armLoc);
 				}
 
 				for (int k = 0; k < 4; k++) {
 					g.drawImage(armImage.get(k), armLoc.get(k).getX() + client.getOffset(), armLoc.get(k).getY(), c);
 				}
 
 				g.drawImage(partsRobotImage, currentLocation.getX() + client.getOffset(), currentLocation.getY(), c);
 
 			}
 		} else if (home) {
 			
 			g.drawImage(partsRobotImage, initialLocation.getX() + client.getOffset(), initialLocation.getY(), c);
 
 			for (int k = 0; k < 4; k++) {
 				g.drawImage(armImage.get(k), armLoc.get(k).getX() + client.getOffset(), armLoc.get(k).getY(), c);
 			}
 		}
 
 		for (int i = 0; i < partArrayGraphics.size(); i++) {
 			PartGraphicsDisplay pgd = partArrayGraphics.get(i);
 			pgd.drawWithOffset(c, g, client.getOffset());
 		}
 
 	}
 
 	public void pickUp() {
 		home = false;
 		pickup = true;
 		givekit = false;
 		gotpart = false;
 		gavepart = false;
 	}
 
 	public void giveKit() {
 		home = false;
 		givekit = true;
 	}
 	
 	public void updateArmLoc(Location loc){
 		
 		for (int k = 0; k < 4; k++) {
 			armLoc.get(k).setX(loc.getX());
 			armLoc.get(k).setY(currentLocation.getY() + 30 * k);
 		}
 		
 	}
 	
 	public void updatePartLoc(ArrayList<Location> loc){
 		
 		for (int j = 0; j < partArrayGraphics.size(); j++) {
 			partStartLoc.get(j).setX(loc.get(j).getX() + 30);
 			partStartLoc.get(j).setY(loc.get(j).getY());
 			partArrayGraphics.get(j).setLocation(partStartLoc.get(j));
 		}
 		
 	}
 
 	public void pickUpPart() {
 
 		PartType partType = pt;
 		PartGraphicsDisplay pgd = new PartGraphicsDisplay(partType);
 
 		if (I < 4) {
 			pgd.setLocation(partStartLoc.get(I));
 			partArrayGraphics.add(pgd);
 		}
 		
 	}
 
 	public PartType givePart() {
 
 		if (I > 0) {
 			return partArrayGraphics.remove(I - 1).getPartType();
 			//Reminder for Adrian: Add sendData to Stand
 		}
 		return null;
 	}
 
 	public void extendArm() {
 
 		if (I == 0) {
 			armLoc.get(0).incrementX(1);
 			partStartLoc.get(0).setX(armLoc.get(0).getX() + 30);
 			partStartLoc.get(0).setY(armLoc.get(0).getY());
 		} else if (I == 1) {
 			armLoc.get(1).incrementX(1);
 			partStartLoc.get(1).setX(armLoc.get(1).getX() + 30);
 			partStartLoc.get(1).setY(armLoc.get(1).getY());
 		} else if (I == 2) {
 			armLoc.get(2).incrementX(1);
 			partStartLoc.get(2).setX(armLoc.get(2).getX() + 30);
 			partStartLoc.get(2).setY(armLoc.get(2).getY());
 		} else if (I == 3) {
 			armLoc.get(3).incrementX(1);
 			partStartLoc.get(3).setX(armLoc.get(3).getX() + 30);
 			partStartLoc.get(3).setY(armLoc.get(3).getY());
 		}
 
 	}
 
 	public void extendArmToKit() {
 
 		if (I == 4) {
 			armLoc.get(3).incrementX(1);
 			partStartLoc.get(3).setX(armLoc.get(3).getX() + 30);
 			partArrayGraphics.get(3).setLocation(partStartLoc.get(3));
 		} else if (I == 3) {
 			armLoc.get(2).incrementX(1);
 			partStartLoc.get(2).setX(armLoc.get(2).getX() + 30);
 			partArrayGraphics.get(2).setLocation(partStartLoc.get(2));
 		} else if (I == 2) {
 			armLoc.get(1).incrementX(1);
 			partStartLoc.get(1).setX(armLoc.get(1).getX() + 30);
 			partArrayGraphics.get(1).setLocation(partStartLoc.get(1));
 		} else if (I == 1) {
 			armLoc.get(0).incrementX(1);
 			partStartLoc.get(0).setX(armLoc.get(0).getX() + 30);
 			partArrayGraphics.get(0).setLocation(partStartLoc.get(0));
 		}
 
 	}
 
 	public void retractArm() {
 
 		if (I == 0) {
 			armLoc.get(0).incrementX(-1);
 			partStartLoc.get(0).setX(armLoc.get(0).getX() + 30);
 			partArrayGraphics.get(0).setLocation(partStartLoc.get(0));
 		} else if (I == 1) {
 			armLoc.get(1).incrementX(-1);
 			partStartLoc.get(1).setX(armLoc.get(1).getX() + 30);
 			partArrayGraphics.get(1).setLocation(partStartLoc.get(1));
 		} else if (I == 2) {
 			armLoc.get(2).incrementX(-1);
 			partStartLoc.get(2).setX(armLoc.get(2).getX() + 30);
 			partArrayGraphics.get(2).setLocation(partStartLoc.get(2));
 		} else if (I == 3) {
 			armLoc.get(3).incrementX(-1);
 			partStartLoc.get(3).setX(armLoc.get(3).getX() + 30);
 			partArrayGraphics.get(3).setLocation(partStartLoc.get(3));
 		}
 
 	}
 
 	public void retractArmFromKit() {
 
 		if (I == 4) {
 			armLoc.get(3).incrementX(-1);
 			partStartLoc.get(3).setX(armLoc.get(3).getX() + 30);
 		} else if (I == 3) {
 			armLoc.get(2).incrementX(-1);
 			partStartLoc.get(2).setX(armLoc.get(2).getX() + 30);
 		} else if (I == 2) {
 			armLoc.get(1).incrementX(-1);
 			partStartLoc.get(1).setX(armLoc.get(1).getX() + 30);
 		} else if (I == 1) {
 			armLoc.get(0).incrementX(-1);
 			partStartLoc.get(0).setX(armLoc.get(0).getX() + 30);
 		}
 
 	}
 
 	public void rotateArm() {
 		rotate = true;
 	}
 
 	public boolean getRotate() {
 		return rotate;
 	}
 
 	@Override
 	public void receiveData(Request r) {
 		if (r.getCommand().equals(Constants.PARTS_ROBOT_GIVE_COMMAND)) {
 			kitloc = ((PartData) r.getData()).getKitLocation();
 			giveKit();
 		} else if (r.getCommand().equals(Constants.PARTS_ROBOT_PICKUP_COMMAND)) {
 			loc = ((PartData) r.getData()).getLocation();
 			pt = ((PartData) r.getData()).getPartType();
 			pickUp();
 			// System.out.println("before pick up");
 		}
 	}
 	@Override
 	public void setLocation(Location newLocation) {
 		// TODO Auto-generated method stub
 
 	}
 
 }
