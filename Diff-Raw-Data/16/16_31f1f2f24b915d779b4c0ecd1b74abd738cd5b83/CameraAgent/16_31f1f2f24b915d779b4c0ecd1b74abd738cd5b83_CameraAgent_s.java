 package agent;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import agent.NestAgent.MyPart;
 import agent.data.Kit;
 import agent.data.Part;
 import agent.interfaces.Camera;
 import agent.interfaces.Nest;
 
 import DeviceGraphics.DeviceGraphics;
 import DeviceGraphics.KitGraphics;
 import GraphicsInterfaces.CameraGraphics;
 import GraphicsInterfaces.NestGraphics;
 
 /**
  * Camera is responsible for inspecting full nests and assembled kits.
  * @author Ross Newman, Michael Gendotti
  */
 public class CameraAgent extends Agent implements Camera {
 
 	String name;
 
 	public CameraAgent(String name) {
 		super();
 		this.name = name;
 	}
 
 	public List<MyNest> nests = Collections
 			.synchronizedList(new ArrayList<MyNest>());
 	private final List<MyKit> kits = Collections
 			.synchronizedList(new ArrayList<MyKit>());
 	public CameraGraphics guiCamera;
 
 	public KitRobotAgent kitRobot;
 	public PartsRobotAgent partRobot;
 
 	public enum NestStatus {
 		NOT_READY, READY, PHOTOGRAPHING, PHOTOGRAPHED
 	};
 
 	public enum KitStatus {
 		NOT_READY, DONE, MESSAGED, PICTURE_BEING_TAKEN
 	};
 
 	public class MyKit {
 		public Kit kit;
 		public KitStatus ks;
 		public boolean kitDone;
 
 		public MyKit(Kit k) {
 			kit = k;
 			ks = KitStatus.NOT_READY;
 			kitDone = false;
 		}
 	}
 
 	public class MyNest {
 		public NestAgent nest;
 	//	public List<Part> Parts;
 		public NestStatus state;
 
 		public MyNest(NestAgent nest) {
 			this.nest = nest;
 			this.state = NestStatus.NOT_READY;
 		}
 	}
 
 	/********** MESSAGES ************/
 	/**
 	 * 
 	 */
 	@Override
 	public void msgInspectKit(Kit kit) {
 		kits.add(new MyKit(kit));
 		stateChanged();
 	}
 
 	@Override
 	public void msgIAmFull(Nest nest) {
 		for (MyNest n : nests) {
 			if (n.nest == nest) {
 				n.state = NestStatus.READY;
 			}
 		}
 		stateChanged();
 	}
 
 	@Override
 	public void msgTakePictureNestDone(NestGraphics nest, boolean done, NestGraphics nest2,boolean done2) {
 		print("CameraGraphics finished animating nest photograph");
 		boolean found1 = false;
 		boolean found2 = false;
 		synchronized (nests) {
 			for (MyNest n : nests) {
 				if (n.nest.guiNest == nest) 
 				{
 					// In v0 all parts are good parts
 					n.state = NestStatus.PHOTOGRAPHED;
 					if(found2){
 						break;
 					}
 					found1 = true;
 				}
 				else if (n.nest.guiNest == nest2) 
 				{
 					// In v0 all parts are good parts
 					n.state = NestStatus.PHOTOGRAPHED;
 					if(found1){
 						break;
 					}
 					found2 = true;
 				}
 			}
 		}
 		stateChanged();
 	}
 
 	@Override
 	public void msgTakePictureKitDone(KitGraphics k, boolean done) {
 		print("CameraGraphics finished animating kit photograph. \nKit passed inspection.");
 		for (MyKit mk : kits) {
 			if (mk.kit.kitGraphics == k) {
 				mk.kitDone = done;
 				mk.ks = KitStatus.DONE;
 			}
 		}
 		stateChanged();
 	}
 
 	/*********** SCHEDULER **************/
 	@Override
 	public boolean pickAndExecuteAnAction() {
 		synchronized (nests) {
 			for (int i = 0; i < nests.size(); i += 2) {
 				if (nests.size() > i + 1) { // Quick check to make sure there
 											// is a nest paired with this
 											// one
 					if (nests.get(i).state == NestStatus.READY
 							&& nests.get(i + 1).state == NestStatus.READY) {
 						print("Taking photos of nests");
						takePictureOfNest(nests.get(i));
						takePictureOfNest(nests.get(i + 1));
 						return true;
 					}
				} else {
 					if (nests.get(i).state == NestStatus.READY) {
 						takePictureOfNest(nests.get(i));
 						return true;
 					}
				}
 			}
 		}
 
 		synchronized (nests) {
 			for (MyNest n : nests) {
 				if (n.state == NestStatus.PHOTOGRAPHED) {
 					tellPartsRobot(n);
 					return true;
 				}
 			}
 		}
 
 		synchronized (kits) {
 			for (MyKit k : kits) {
 				if (k != null && k.ks == KitStatus.NOT_READY) {
 					takePictureOfKit(k);
 					return true;
 				}
 			}
 			for (MyKit k : kits) {
 				if (k != null && k.ks == KitStatus.DONE) {
 					tellKitRobot(k);
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	/*********** ACTIONS *******************/
 	private void tellKitRobot(MyKit k) {
 		k.ks = KitStatus.MESSAGED;
 		kitRobot.msgKitPassedInspection();
 		stateChanged();
 	}
 
 	private void takePictureOfKit(MyKit kit) {
 		kit.ks = KitStatus.PICTURE_BEING_TAKEN;
 		if (guiCamera != null) {
 			guiCamera.takeKitPhoto(kit.kit.kitGraphics);
 		}
 		stateChanged();
 	}
 
 	private void tellPartsRobot(MyNest n) {
 		List<Part> goodParts = new ArrayList<Part>();
 		for (MyPart part : n.nest.currentParts) {
 			if (part.part.isGood) {
 				goodParts.add(part.part);
 			}
 		}
 		print("good parts count: " + goodParts.size());
 		partRobot.msgHereAreGoodParts(n.nest, goodParts);
 		n.state = NestStatus.NOT_READY;
 		stateChanged();
 
 	}
 
	private void takePictureOfNest(MyNest n) {
 		
		 if (guiCamera != null) { guiCamera.takeNestPhoto(n.nest.guiNest); }
 		
 		n.state = NestStatus.PHOTOGRAPHING;
 		stateChanged();
 	}
 
 	public void setNest(NestAgent nest) {
 		nests.add(new MyNest(nest));
 	}
 
 	public void setPartsRobot(PartsRobotAgent parts) {
 		this.partRobot = parts;
 
 	}
 
 	public void setNests(ArrayList<NestAgent> nests) {
 		for (NestAgent nest : nests) {
 			this.nests.add(new MyNest(nest));
 		}
 	}
 
 	@Override
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public List<MyNest> getNests() {
 		return nests;
 	}
 
 	public void setNests(List<MyNest> nests) {
 		this.nests = nests;
 	}
 
 	public CameraGraphics getGuiCamera() {
 		return guiCamera;
 	}
 
 	public void setGraphicalRepresentation(DeviceGraphics guiCamera) {
 		this.guiCamera = (CameraGraphics) guiCamera;
 	}
 
 	public KitRobotAgent getKitRobot() {
 		return kitRobot;
 	}
 
 	public void setKitRobot(KitRobotAgent kitRobot) {
 		this.kitRobot = kitRobot;
 	}
 
 	public PartsRobotAgent getPartRobot() {
 		return partRobot;
 	}
 
 	public void setPartRobot(PartsRobotAgent partRobot) {
 		this.partRobot = partRobot;
 	}
 
 	public List<MyKit> getKits() {
 		return kits;
 	}
 
 }
