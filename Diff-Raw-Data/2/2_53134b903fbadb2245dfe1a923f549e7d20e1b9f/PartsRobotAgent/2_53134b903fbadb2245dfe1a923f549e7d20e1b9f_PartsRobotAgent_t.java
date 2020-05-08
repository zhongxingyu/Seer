 package agent;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.Semaphore;
 
 import DeviceGraphics.DeviceGraphics;
 import GraphicsInterfaces.PartsRobotGraphics;
 import agent.data.Kit;
 import agent.data.Part;
 import agent.interfaces.Nest;
 import agent.interfaces.PartsRobot;
 import agent.interfaces.Stand;
 import factory.KitConfig;
 import factory.PartType;
 
 /**
  * Parts robot picks parts from nests and places them in kits
  * @author Ross Newman, Michael Gendotti, Daniel Paje
  */
 public class PartsRobotAgent extends Agent implements PartsRobot {
 
 	public PartsRobotAgent(String name) {
 		super();
 
 		this.name = name;
 
 		// Add arms
 		for (int i = 0; i < 4; i++) {
 			this.Arms.add(new Arm());
 		}
 	}
 
 	String name;
 
 	public class MyKit {
 		public Kit kit;
 		public MyKitStatus MKS;
 
 		public MyKit(Kit k) {
 			kit = k;
 			MKS = MyKitStatus.NotDone;
 		}
 	}
 
 	public enum MyKitStatus {
 		NotDone, Done
 	};
 
 	public class Arm {
 		Part part;
 		ArmStatus AS;
 
 		public Arm() {
 			part = null;
 			AS = ArmStatus.Empty;
 		}
 	}
 
 	private enum ArmStatus {
 		Empty, Full, Emptying
 	};
 
 	private KitConfig Kitconfig;
 	private final List<MyKit> MyKits = Collections
 			.synchronizedList(new ArrayList<MyKit>());
 	public Map<Nest, List<Part>> GoodParts = new ConcurrentHashMap<Nest, List<Part>>();
 	public List<Arm> Arms = Collections.synchronizedList(new ArrayList<Arm>());
 
 	List<Kit> KitsOnStand;
 	// List<Nest> nests;
 
 	Stand stand;
 	PartsRobotGraphics partsRobotGraphics;
 
 	public Semaphore animation = new Semaphore(0, true);
 
 	// public Semaphore accessKit = new Semaphore(0, true);
 
 	/***** MESSAGES ***************************************/
 	/**
 	 * Changes the configuration for the kits From FCS
 	 */
 	@Override
 	public void msgHereIsKitConfiguration(KitConfig config) {
 		print("Received msgHereIsKitConfiguration");
 		Kitconfig = config;
 		stateChanged();
 	}
 
 	/**
 	 * From Camera
 	 */
 	@Override
 	public void msgHereAreGoodParts(Nest n, List<Part> goodParts) {
 		print("Received msgHereAreGoodParts");
 		GoodParts.put(n, goodParts);
 		stateChanged();
 	}
 
 	/**
 	 * From Stand
 	 */
 	@Override
 	public void msgUseThisKit(Kit k) {
 		print("Received msgUseThisKit");
 		MyKit mk = new MyKit(k);
 		MyKits.add(mk);
 		stateChanged();
 	}
 
 	/**
 	 * Releases animation semaphore after a part is picked up, so that a new
 	 * animation may be run by graphics. From graphics
 	 */
 	@Override
 	public void msgPickUpPartDone() {
 		print("Received msgPickUpPartDone from graphics");
 		animation.release();
 		stateChanged();
 	}
 
 	/**
 	 * Releases animation semaphore after a part is given to kit, so that a new
 	 * animation may be run by graphics. From graphics
 	 */
 	@Override
 	public void msgGivePartToKitDone() {
 		print("Received msgGivePartToKitDone from graphics");
 		animation.release();
 		stateChanged();
 	}
 
 	/**************** SCHEDULER ***********************/
 
 	@Override
 	public boolean pickAndExecuteAnAction() {
 		// print("wee");
 		// Checks if a kit is done and inspects it if it is
 		synchronized (MyKits) {
 			if (MyKits.size() > 0) {
 				for (MyKit mk : MyKits) {
 					if (mk.MKS == MyKitStatus.Done) {
 						RequestInspection(mk);
 						return true;
 					}
 				}
 			}
 		}
 		// Checks if there is an empty arm, if there is it fills it with a
 		// good part that the kit needs
 		synchronized (Arms) {
 			if (IsAnyArmEmpty()) {
 				synchronized (GoodParts) {
 					for (Nest nest : GoodParts.keySet()) {
 						// Going through all the good parts
 						for (Part part : GoodParts.get(nest)) {
 							synchronized (MyKits) {
 								for (MyKit mk : MyKits) {
 									// Checking if the good part is needed by
 									// either kit
 									// print("Kit needs: " +
 									// mk.kit.partsExpected.getConfig().toString());
 									if (mk.kit.needPart(part) > NumPartsInHand(part)) {
 										print("Found a part I need");
 
 										for (Arm arm : Arms) {
 											if (arm.AS == ArmStatus.Empty) {
 												// Find the empty arm
 												PickUpPart(arm, part, nest);
 												return true;
 											}
 										}
 
 									}
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 
 		// Checks if any arm is holding a part and places it if there is one
 		synchronized (Arms) {
 			for (Arm arm : Arms) {
 
 				if (arm.AS == ArmStatus.Full) {
 					print("Arm holding: " + arm.part.type.toString());
 					PlacePart(arm);
 					return true;
 				}
 			}
 		}
 
 		return false;
 	}
 
 	private int NumPartsInHand(Part part) {
 		synchronized (Arms) {
 			int count = 0;
 			for (Arm a : Arms) {
 				if (a.part != null) {
 					if (a.part.type == part.type) {
 						count++;
 					}
 				}
 			}
 			return count;
 		}
 	}
 
 	/********** ACTIONS **************/
 
 	private void PickUpPart(Arm arm, Part part, Nest nest) {
 
 		print("Picking up part");
 		synchronized (Arms) {
 
 			arm.AS = ArmStatus.Full;
 			arm.part = part;
 			// Tells the graphics to pickup the part
 			if (partsRobotGraphics != null) {
 				partsRobotGraphics.pickUpPart(part.partGraphics);
 				try {
 					animation.acquire();
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 			}
 
 			// Only takes 1 part from a nest at a time
 			nest.msgTakingPart(part);
 			nest.msgDoneTakingParts();
 
 			stateChanged();
 		}
 	}
 
 	private void PlacePart(Arm arm) {
 		print("Placing part");
 		synchronized (Arms) {
 			synchronized (MyKits) {
 				for (MyKit mk : MyKits) {
 					if (mk.kit.needPart(arm.part) > 0) {
 
 						if (partsRobotGraphics != null) {
 							partsRobotGraphics.givePartToKit(
 									arm.part.partGraphics, mk.kit.kitGraphics);
 							try {
 								animation.acquire();
 							} catch (InterruptedException e) {
 								e.printStackTrace();
 							}
 						}
 						// Tells the kit it has the part now
 						mk.kit.parts.add(arm.part);
 						/*
 						 * if (mk.kit.kitGraphics != null) {
 						 * System.out.println("receiving part");
 						 * mk.kit.kitGraphics
 						 * .receivePart(arm.part.partGraphics); }
 						 */
 						arm.part = null;
 						arm.AS = ArmStatus.Empty;
 
 						// Checks if the kit is done
 						CheckMyKit(mk);
 						break;
 					}
 				}
 			}
 			stateChanged();
 		}
 	}
 
 	private void CheckMyKit(MyKit mk) {
 		synchronized (MyKits) {
 			int size = 0;
 			for (PartType type : mk.kit.partsExpected.getConfig().keySet()) {
 				for (int i = 0; i < mk.kit.partsExpected.getConfig().get(type); i++) {
 					size++;
 				}
 			}
 
 			print("Need " + (size - mk.kit.parts.size())
 					+ " more part(s) to finish kit.");
			if (size - mk.kit.parts.size() == 0) {
 				mk.MKS = MyKitStatus.Done;
 			}
 		}
 		// stateChanged();
 	}
 
 	private void RequestInspection(MyKit mk) {
 		print("Requesting inspection.");
 		stand.msgKitAssembled(mk.kit);
 		MyKits.remove(mk);
 		stateChanged();
 	}
 
 	// Helper methods
 
 	// Checks if any of the arms are empty
 	private boolean IsAnyArmEmpty() {
 		synchronized (Arms) {
 			for (Arm a : Arms) {
 				if (a.AS == ArmStatus.Empty) {
 					return true;
 				}
 			}
 			return false;
 		}
 	}
 
 	@Override
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public KitConfig getKitConfig() {
 		return Kitconfig;
 	}
 
 	public void setKitConfig(KitConfig kitConfig) {
 		Kitconfig = kitConfig;
 	}
 
 	public Map<Nest, List<Part>> getGoodParts() {
 		return GoodParts;
 	}
 
 	public void setGoodParts(Map<Nest, List<Part>> goodParts) {
 		GoodParts = goodParts;
 	}
 
 	public List<Arm> getArms() {
 		return Arms;
 	}
 
 	public void setArms(List<Arm> arms) {
 		Arms = arms;
 	}
 
 	public List<Kit> getKitsOnStand() {
 		return KitsOnStand;
 	}
 
 	public void setKitsOnStand(List<Kit> kitsOnStand) {
 		KitsOnStand = kitsOnStand;
 	}
 
 	/*
 	 * public List<Nest> getNests() { return nests; } public void
 	 * setNests(List<Nest> nests) { this.nests = nests; }
 	 */
 
 	public Stand getStand() {
 		return stand;
 	}
 
 	public void setStand(Stand stand) {
 		this.stand = stand;
 	}
 
 	public PartsRobotGraphics getPartsrobotGraphics() {
 		return partsRobotGraphics;
 	}
 
 	@Override
 	public void setGraphicalRepresentation(DeviceGraphics partsrobotGraphics) {
 		this.partsRobotGraphics = (PartsRobotGraphics) partsrobotGraphics;
 	}
 
 	public Semaphore getAnimation() {
 		return animation;
 	}
 
 	public void setAnimation(Semaphore animation) {
 		this.animation = animation;
 	}
 
 	public List<MyKit> getMyKits() {
 		return MyKits;
 	}
 
 	// Initialize Arms
 	public void InitializeArms() {
 		for (int i = 0; i < 4; i++) {
 			Arms.add(new Arm());
 		}
 	}
 }
