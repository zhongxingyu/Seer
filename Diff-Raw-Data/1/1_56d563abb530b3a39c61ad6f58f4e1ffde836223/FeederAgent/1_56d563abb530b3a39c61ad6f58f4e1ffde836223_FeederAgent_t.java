 package agent;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.concurrent.Semaphore;
 
 import DeviceGraphics.DeviceGraphics;
 import GraphicsInterfaces.FeederGraphics;
 import agent.data.Bin;
 import agent.data.Part;
 import agent.interfaces.Feeder;
 import factory.PartType;
 
 /**
  * Feeder receives parts from gantry and feeds the lanes
  * @author Arjun Bhargava, Michael Gendotti
  */
 public class FeederAgent extends Agent implements Feeder {
 
 	private GantryAgent gantry;
 	// Top Lane is the first lane, bottom is the second
 	public List<MyLane> lanes = Collections
 			.synchronizedList(new ArrayList<MyLane>());
 	public FeederGraphics feederGUI;
 
 	private int currentOrientation;// 0 for Top, 1 for Bottom
 
 	public Bin bin;
 
 	private FeederStatus state;
 
 	private final String name;
 
 	public Semaphore animation = new Semaphore(0, true);
 
 	public enum FeederStatus {
 		IDLE, REQUESTED_PARTS, FEEDING_PARTS, PURGING, REMOVING_BIN, RECEIVING_BIN
 	}
 
 	public enum LaneStatus {
 		DOES_NOT_NEED_PARTS, NEEDS_PARTS, GIVING_PARTS
 	};
 
 	public class MyLane {
 		public LaneAgent lane;
 		public LaneStatus state;
 		public PartType type;
 		public int numPartsNeeded;
 
 		public MyLane(LaneAgent lane, PartType type) {
 			this.lane = lane;
 			this.type = type;
 			state = LaneStatus.NEEDS_PARTS;
 			numPartsNeeded = 1;
 		}
 
 		public MyLane(LaneAgent lane) {
 			this.lane = lane;
 			this.type = null;
 			state = LaneStatus.DOES_NOT_NEED_PARTS;
 			numPartsNeeded = 0;
 		}
 	}
 
 	public FeederAgent(String name) {
 		super();
 		state = FeederStatus.IDLE;
 		this.name = name;
 		currentOrientation = 0;
 		bin = null;
 	}
 
 	@Override
 	public void msgINeedPart(PartType type, LaneAgent lane) {
 		print("Received msgINeedPart for type " + type.getName());
 		boolean found = false;
 		synchronized (lanes) {
 			for (MyLane l : lanes) {
 				if (l.lane.equals(lane)) {
 					found = true;
 					l.numPartsNeeded++;
 					l.type = type;
 					if (l.state == LaneStatus.DOES_NOT_NEED_PARTS) {
 						l.state = LaneStatus.NEEDS_PARTS;
 					}
 				}
 			}
 		}
 
 		if (!found) {
 			lanes.add(new MyLane(lane, type));
 			print("added new lane");
 		}
 		stateChanged();
 	}
 
 	@Override
 	public void msgHereAreParts(PartType type, Bin bin) {
 		print("Received msgHereAreParts " + type.toString());
 		this.bin = bin;
 		state = FeederStatus.RECEIVING_BIN;
 		synchronized (lanes) {
 			for (MyLane lane : lanes) {
 				if (lane.type != null) {
 					if (lane.type.equals(type)) {
 						print("lane type is " + lane.type.toString());
 						lane.state = LaneStatus.GIVING_PARTS;
 					}
 				}
 			}
 		}
 		stateChanged();
 	}
 
 	@Override
 	public void msgRemoveBinDone() {
 		print("Gantry has removed bin from feeder");
 		state = FeederStatus.IDLE;
 		stateChanged();
 	}
 
 	@Override
 	public void msgReceiveBinDone(Bin bin) {
 		print("Received msgReceiveBinDone from graphics");
 		animation.release();
 	}
 
 	@Override
 	public void msgPurgeBinDone(Bin bin) {
 		print("Received msgPurgeBinDone from graphics");
 		state = FeederStatus.IDLE;
 		animation.release();
 	}
 
 	@Override
 	public void msgFlipDiverterDone() {
 		print("Received msgFlipInverterDone from graphics");
 		animation.release();
 	}
 	
 	public void msgThisLanePurged(LaneAgent lane) {
 		for(MyLane currentLane : lanes) {
 			if(currentLane.lane == lane) {
 				currentLane.numPartsNeeded = 0;
 				break;
 			}
 		}
 	}
 
 	@Override
 	public boolean pickAndExecuteAnAction() {
 		// / print("In the scheduler");
 		// synchronized(lanes) {
 		if (state == FeederStatus.IDLE) {
 			synchronized (lanes) {
 				for (MyLane lane : lanes) {
 					if (lane.state == LaneStatus.NEEDS_PARTS) {
 						getParts(lane);
 						return true;
 					}
 				}
 			}
 		}
 		if (state == FeederStatus.RECEIVING_BIN) {
 			receiveBin();
 			return true;
 		}
 		if (state == FeederStatus.FEEDING_PARTS) {
 			synchronized (lanes) {
 				for (MyLane lane : lanes) {
 					if (lane.state == LaneStatus.GIVING_PARTS) {
 						print("Giving parts to lane " + lane.type.getName());
 						if (lanes.get(currentOrientation).equals(lane)) {
 							givePart(lane);
 							return true;
 						} else {
 							// print("Flipping Diverter");
 							flipDiverter();
 							return true;
 						}
 					}
 				}
 			}
 			if(!doesLaneNeedParts()) {
 				state = FeederStatus.PURGING;
 				return true;
 			}
 		}
 		// }
 		if (state == FeederStatus.PURGING) {
 			purgeBin();
 		}
 		return false;
 	}
 	
 	public boolean doesLaneNeedParts() {
 		synchronized(lanes) {
 		for(MyLane lane : lanes) {
 			if(bin != null) {
 			if(lane.type == bin.part.type) {
 				return true;
 			}
 			}
 		}
 		}
 		return false;
 	}
 
 	public void getParts(final MyLane lane) {
 		print("Telling gantry that I needs parts");
 		if (gantry != null) {
 			gantry.msgINeedParts(lane.type, this);
 		}
 		state = FeederStatus.REQUESTED_PARTS;
 		stateChanged();
 	}
 
 	public void givePart(MyLane lane) {
 		print("Giving lane a part");
 		lane.numPartsNeeded--;
		//Instead of new Part, call GUI method that will initialize the part.
 		lane.lane.msgHereIsPart(new Part(lane.type));
 		if (lane.numPartsNeeded == 0) {
 			print("shows up when lane " + lane.type.getName()
 					+ " does not need more parts");
 			state = FeederStatus.PURGING;
 			lane.state = LaneStatus.DOES_NOT_NEED_PARTS;
 		}
 		stateChanged();
 	}
 
 	public void purgeBin() {
 		print("Purging this feeder");
 		feederGUI.purgeBin(bin.binGraphics);
 		try {
 			animation.acquire();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 
 		gantry.msgRemoveBin(bin);
 		bin = null;
 		state = FeederStatus.REMOVING_BIN;
 		stateChanged();
 	}
 
 	public void flipDiverter() {
 		print("Flipping the diverter");
 		if (feederGUI != null) {
 			feederGUI.flipDiverter();
 			try {
 				animation.acquire();
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 
 		currentOrientation = currentOrientation == 0 ? 1 : 0;
 		stateChanged();
 	}
 
 	public void receiveBin() {
 		print("Telling FeederGraphics to receiveBin");
 		state = FeederStatus.FEEDING_PARTS;
 		if (feederGUI != null) {
 			feederGUI.receiveBin(bin.binGraphics);
 			try {
 				animation.acquire();
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 		stateChanged();
 	}
 
 	// GETTERS AND SETTERS
 	@Override
 	public void setGraphicalRepresentation(DeviceGraphics feeder) {
 		this.feederGUI = (FeederGraphics) feeder;
 	}
 
 	@Override
 	public String getName() {
 		return name;
 	}
 
 	@Override
 	public void setGantry(GantryAgent gantry) {
 		this.gantry = gantry;
 	}
 
 	@Override
 	public void setLane(LaneAgent lane) {
 		lanes.add(new MyLane(lane));
 	}
 
 	public void setLanes(LaneAgent lane1, LaneAgent lane2) {
 		lanes.add(new MyLane(lane1));
 		lanes.add(new MyLane(lane2));
 	}
 
 	public void thisLaneAgent(LaneAgent lane) {
 		lanes.add(new MyLane(lane));
 	}
 
 }
