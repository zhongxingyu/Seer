 package agent;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.concurrent.Semaphore;
 
 import DeviceGraphics.DeviceGraphics;
 import DeviceGraphics.PartGraphics;
 import GraphicsInterfaces.NestGraphics;
 import agent.data.Part;
 import agent.interfaces.Nest;
 import factory.PartType;
 
 /**
  * Nests hold parts
  * 
  * @author Arjun Bhargava, Michael Gendotti
  */
 public class NestAgent extends Agent implements Nest {
 
 	public List<PartType> requestList = Collections.synchronizedList(new ArrayList<PartType>());
 	PartType currentPartType = null;
 	public List<MyPart> currentParts = Collections.synchronizedList(new ArrayList<MyPart>());
 	public int count = 0;
 	public int countRequest = 0;
 	int full = 8;
 	public boolean takingParts = false;
 	private boolean partReady = false;
 	private final boolean partTypeNull = false;
 
 	public NestGraphics nestGraphics;
 	private NestState state;
 
 	public Semaphore animation = new Semaphore(0, true);
 
 	String name;
 
 	LaneAgent lane;
 	LaneState laneState = LaneState.READY;
 	CameraAgent camera;
 
 	public class MyPart {
 		Part part;
 		NestStatus status;
 
 		public MyPart(Part p) {
 			part = p;
 			status = NestStatus.IN_NEST;
 		}
 	}
 
 	public enum NestStatus {
 		IN_NEST, IN_NEST_POSITION, REMOVING
 	};
 
 	public enum NestState {
 		PURGING, PRIORITY_PURGE, WAITING_FOR_LANE_PURGE, DONE_PURGING, NULL
 	};
 
 	public enum LaneState {
 		PURGING, READY
 	}
 
 	public NestAgent(String name) {
 		super();
 		state = NestState.DONE_PURGING;
 		this.name = name;
 	}
 
 	// MESSAGES
 	@Override
 	public void msgHereIsPartType(PartType type) {
 		print("Received msgHereIsPartType");
 		if (currentPartType == null && type == null) {
 			currentPartType = type;
 		}
 		else if(currentPartType == null && type != null) {
 			//if(!type.equals(currentPartType)) {
 				currentPartType = type;
 				state = NestState.PURGING;
 				laneState = LaneState.PURGING;
 				lane.msgPurgeParts();
 			//}
 		}
 		else if(currentPartType != null && type != null) {
 			if(!type.equals(currentPartType)) {
 				currentPartType = type;
 				state = NestState.PURGING;
 				laneState = LaneState.PURGING;
 				lane.msgPurgeParts();
 			}
 		} else if (currentPartType != null) {
 			currentPartType = type;
 			state = NestState.PURGING;
 			laneState = LaneState.PURGING;
 			lane.msgPurgeParts();
 		}
 
 		// camera.msgResetSelf();
 
 		stateChanged();
 	}
 
 	@Override
 	public void msgPartReady() {
 		partReady = true;
 		stateChanged();
 
 	}
 
 	@Override
 	public void msgHereIsPart(Part p) {
 		print("Received msgHereIsPart");
 		takingParts = false;
 		countRequest--;
 		count++;
 		// if (currentParts.size() <= full) {
 		currentParts.add(new MyPart(p));
 		print("Received a part of type " + p.type.getName() + " I have " + currentParts.size()
 				+ " parts and have requested " + countRequest + " takingParts: " + takingParts);
 		// }
 		stateChanged();
 	}
 
 	@Override
 	public void msgTakingPart(Part p) {
 		print("Received msgTakingPart");
 		synchronized (currentParts) {
 			for (MyPart part : currentParts) {
 				if (part.part.equals(p)) {
 					print("found part");
 					part.status = NestStatus.REMOVING;
 					// currentParts.remove(part);
 					break;
 				}
 			}
 		}
 		stateChanged();
 	}
 
 	@Override
 	public void msgLanePurgeDone() {
 		state = NestState.DONE_PURGING;
 		if (currentPartType == null) {
 			state = NestState.NULL;
 		}
 		laneState = LaneState.READY;
 		stateChanged();
 	}
 
 	@Override
 	public void msgPurgeSelf() {
 		state = NestState.PRIORITY_PURGE;
 		stateChanged();
 	}
 
 	@Override
 	public void msgDoneTakingParts() {
 		print("Received msgDoneTakingParts");
 		count--;
 		takingParts = false;
 		stateChanged();
 	}
 
 	@Override
 	public void msgReceivePartDone() {
 		print("Received msgReceivePartDone from graphics");
 		animation.release();
 		stateChanged();
 	}
 
 	@Override
 	public void msgGivePartToPartsRobotDone() {
 		print("Received msgGivePartToPartsRobotDone from graphics");
 		animation.release();
 		stateChanged();
 	}
 
 	@Override
 	public void msgPurgingDone() {
 		print("Received msgPurgingDone from graphics");
		if (currentPartType != null) {
 			state = NestState.DONE_PURGING;
 			if (currentPartType == null) {
 				state = NestState.NULL;
 			}
 			animation.release();
 		}
 		stateChanged();
 	}
 
 	@Override
 	public boolean pickAndExecuteAnAction() {
 		// print("In scheduler");
 		if (state == NestState.PURGING || state == NestState.PRIORITY_PURGE) {
 			purgeSelf();
 			return true;
 		} else if (state == NestState.DONE_PURGING && laneState == LaneState.READY && !takingParts
 				&& currentPartType != null) {
 			if (partReady && currentParts.size() < full) {
 				requestPart();
 				return true;
 			}
 			synchronized (requestList) {
 				for (PartType requestedPart : requestList) {
 					if (count + countRequest < full) {
 						getParts(requestedPart);
 						return true;
 					}
 				}
 			}
 			synchronized (currentParts) {
 				for (MyPart currentPart : currentParts) {
 					if (currentPart.status == NestStatus.REMOVING) {
 						removePart(currentPart);
 						return true;
 					}
 				}
 			}
 			synchronized (currentParts) {
 				for (MyPart currentPart : currentParts) {
 					if (currentPart.status == NestStatus.IN_NEST) {
 						moveToPosition(currentPart);
 						return true;
 					}
 				}
 			}
 			if (currentParts.size() >= full && takingParts == false) {
 				nestFull();
 				return true;
 			}
 		}
 		return false;
 	}
 
 	// ACTIONS
 	public void purgeSelf() {
 		state = NestState.WAITING_FOR_LANE_PURGE;
 		if (nestGraphics != null) {
 			nestGraphics.purge();
 			try {
 				animation.acquire();
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		takingParts = false;
 		requestList = Collections.synchronizedList(new ArrayList<PartType>());
 		currentParts = Collections.synchronizedList(new ArrayList<MyPart>());
 		countRequest = 0;
 		count = 0;
 		requestList.clear();
 		currentParts.clear();
 		requestList.add(currentPartType);
 		stateChanged();
 	}
 
 	public void requestPart() {
 		partReady = false;
 		lane.msgGiveMePart();
 		stateChanged();
 	}
 
 	public void getParts(PartType requestedType) {
 		print("Telling lane it need a part and incrementing count");
 		countRequest++;
 		lane.msgINeedPart(requestedType);
 
 		stateChanged();
 	}
 
 	public void moveToPosition(MyPart mp) {
 		print("Moving part to proper nest location");
 		// count++;
 		if (nestGraphics != null) {
 			// TODO
 			PartGraphics pg = new PartGraphics(mp.part.partGraphics);
 			nestGraphics.receivePart(mp.part.partGraphics);
 			try {
 				animation.acquire();
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		// count++;
 
 		for (MyPart currentPart : currentParts) {
 			if (currentPart == mp) {
 				currentPart.status = NestStatus.IN_NEST_POSITION;
 				break;
 			}
 		}
 		stateChanged();
 	}
 
 	public void removePart(MyPart mp) {
 		currentParts.remove(mp);
 		// countRequest--;
 		if (nestGraphics != null) {
 			PartGraphics pg = new PartGraphics(mp.part.partGraphics);
 			nestGraphics.givePartToPartsRobot(pg);
 			try {
 				animation.acquire();
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		print("count request " + countRequest);
 		stateChanged();
 	}
 
 	public void nestFull() {
 		print("Telling camera that this nest is full with " + count + " parts or " + currentParts.size());
 		camera.msgIAmFull(this);
 		takingParts = true;
 	}
 
 	@Override
 	public String getName() {
 		return name;
 	}
 
 	public void setLane(LaneAgent lane) {
 		this.lane = lane;
 	}
 
 	public void setCamera(CameraAgent camera) {
 		this.camera = camera;
 	}
 
 	@Override
 	public void setGraphicalRepresentation(DeviceGraphics nest) {
 		this.nestGraphics = (NestGraphics) nest;
 	}
 
 	public List<Part> getParts() {
 		List<Part> parts = new ArrayList<Part>();
 		synchronized (currentParts) {
 			for (MyPart p : currentParts) {
 				parts.add(p.part);
 			}
 		}
 		return parts;
 	}
 
 	public ArrayList<PartType> getTypesOfParts() {
 		ArrayList<PartType> types = new ArrayList<PartType>();
 		synchronized (currentParts) {
 			for (MyPart p : currentParts) {
 				types.add(p.part.type);
 			}
 		}
 		return types;
 	}
 
 }
