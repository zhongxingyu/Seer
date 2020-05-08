 package agent;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.concurrent.Semaphore;
 
 import DeviceGraphics.DeviceGraphics;
 import DeviceGraphics.PartGraphics;
 import GraphicsInterfaces.LaneGraphics;
 import agent.data.Part;
 import agent.interfaces.Lane;
 import factory.PartType;
 
 /**
  * Lane delivers parts to the nest
  * @author Arjun Bhargava
  */
 public class LaneAgent extends Agent implements Lane {
 
 	public List<PartType> requestList = Collections
 			.synchronizedList(new ArrayList<PartType>());
 	public List<MyPart> currentParts = Collections
 			.synchronizedList(new ArrayList<MyPart>());
 
 	public PartType currentType;
 	
 	public int topLimit = 5;
 	public int lowerThreshold = 3;
 	public int extraRequestCount = 0;
 
 	public LaneStatus state;
 
 	String name;
 
 	public Semaphore animation = new Semaphore(0, true);
 
 	public class MyPart {
 		public Part part;
 		PartStatus status;
 
 		public MyPart(Part p) {
 			part = p;
 			status = PartStatus.BEGINNING_LANE;
 		}
 	}
 
 	public enum PartStatus {
 		BEGINNING_LANE, IN_LANE, END_LANE, TOLD_NEST, NEED_TO_DELIVER, DELIVERED
 	};
 
 	public enum LaneStatus {
 		FILLING, DONE_FILLING, PURGING, WAITING, BROKEN, BROKEN_WHILE_PURGING;
 	};
 
 	FeederAgent feeder;
 	NestAgent nest;
 	LaneGraphics laneGUI;
 
 	public LaneAgent(String name) {
 		super();
 
 		this.name = name;
 		state = LaneStatus.FILLING;
 	}
 
 	@Override
 	public void msgINeedPart(PartType type) {
 		print("Received msgINeedPart");
 		requestList.add(type);
 		currentType = type;
 		stateChanged();
 	}
 
 	@Override
 	public void msgPurgeParts() {
 		print("Received msgPurgeParts");
 		state = LaneStatus.PURGING;
 		stateChanged();
 	}
 
 	@Override
 	public void msgHereIsPart(Part p) {
 		print("Received msgHereIsPart of type " + p.type.getName());
 		currentParts.add(new MyPart(p));
 		if (laneGUI != null) {
 			laneGUI.receivePart(p.partGraphics);
 		}
 
 		stateChanged();
 	}
 
 	@Override
 	public void msgGiveMePart() {
 		print("Received message give me part from nest");
 		synchronized (currentParts) {
 			for (MyPart p : currentParts) {
 				if (p.status == PartStatus.TOLD_NEST) {
 					p.status = PartStatus.NEED_TO_DELIVER;
 					break;
 				}
 			}
 		}
 		stateChanged();
 
 	}
 
 	@Override
 	public void msgReceivePartDone(PartGraphics part) {
 		print("Received msgReceivePartDone from graphics");
 		synchronized (currentParts) {
 			for (MyPart p : currentParts) {
 				if (p.status == PartStatus.BEGINNING_LANE) {
 					p.status = PartStatus.END_LANE;
 					break;
 				}
 			}
 		}
 		// animation.release();
 		stateChanged();
 	}
 
 	@Override
 	public void msgPurgeDone() {
 		print("Received msgPurgeDone");
 		animation.release();
 		stateChanged();
 	}
 
 	@Override
 	public void msgGivePartToNestDone(PartGraphics part) {
 		print("Received msgGivePartToNestDone from graphics");
 		animation.release();
 		stateChanged();
 	}
 	
 	@Override
 	public void msgChangeAmplitude() {
 		laneGUI.unjam();
 	}
 	
 	@Override
 	public void msgFixYourself() {
 		if(state == LaneStatus.BROKEN_WHILE_PURGING) {
 			state = LaneStatus.PURGING;
 		}
 		else {
 			state = LaneStatus.FILLING;
 		}
 		stateChanged();
 	}
 
 	@Override
 	public void msgBreakThis() {
 		if(state == LaneStatus.PURGING) {
 			state = LaneStatus.BROKEN_WHILE_PURGING;
 		}
 		else { 
 			state = LaneStatus.BROKEN;
 		}
 		stateChanged();
 	}
 	
 	@Override
 	public boolean pickAndExecuteAnAction() {
 		// print("In the Scheduler");
 
 		if (state == LaneStatus.PURGING) {
 			purgeSelf();
 			return true;
 		}
 		
 		if (state == LaneStatus.FILLING) {
 			synchronized (requestList) {
 				for (PartType requestedType : requestList) {
 					getParts(requestedType);
 					return true;
 				}
 			}
 		}
 
 		if (state == LaneStatus.FILLING && currentType != null) {
 			if(extraRequestCount+requestList.size()+currentParts.size() < topLimit) {
 				requestList.add(currentType);
 				extraRequestCount++;
 				return true;
 			}
 			if(extraRequestCount+requestList.size()+currentParts.size() >= topLimit && currentParts.size() != 0) {
 				state = LaneStatus.WAITING;
 				return true;
 			}
 		}
 		if (state == LaneStatus.WAITING) {
 			if(extraRequestCount+requestList.size()+currentParts.size() < lowerThreshold && currentType != null && currentParts.size() != 0) {
 				extraRequestCount = 0;
 				state = LaneStatus.FILLING;
 				return true;
 			}
 		}
 
 		
 		synchronized (currentParts) {
 			for (MyPart part : currentParts) {
 				if (part.status == PartStatus.END_LANE) {
 					tellNest(part);
 					return true;
 				}
 			}
 		}
 
 		synchronized (currentParts) {
 			for (MyPart part : currentParts) {
 				if (part.status == PartStatus.NEED_TO_DELIVER) {
 					giveToNest(part);
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	public void purgeSelf() {
 		print("Purging self");
 		requestList = Collections.synchronizedList(new ArrayList<PartType>());
 		currentParts = Collections.synchronizedList(new ArrayList<MyPart>());
 		if (laneGUI != null) {
 			laneGUI.purge();
 			try {
 				animation.acquire();
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		feeder.msgThisLanePurged(this);
 		nest.msgLanePurgeDone();
 		state = LaneStatus.FILLING;
 		stateChanged();
 	}
 
 	public void getParts(final PartType requestedType) {
 		print("Telling Feeder that it needs a part");
 		requestList.remove(requestedType);
 		feeder.msgINeedPart(requestedType, this);
 		stateChanged();
 	}
 
 	public void tellNest(MyPart p) {
 		p.status = PartStatus.TOLD_NEST;
 		nest.msgPartReady();
 		stateChanged();
 	}
 
 	public void giveToNest(MyPart mp) {
 		print("Giving part to Nest of type " + mp.part.type.getName());
 		mp.status = PartStatus.DELIVERED;
 		if (laneGUI != null) {
 			laneGUI.givePartToNest(mp.part.partGraphics);
 		}
 		try {
 			animation.acquire();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		nest.msgHereIsPart(mp.part);
 		synchronized (currentParts) {
 			for (MyPart currentPart : currentParts) {
 				if (currentPart == mp) {
 					currentParts.remove(currentPart);
 					break;
 				}
 			}
 		}
 		stateChanged();
 	}
 
 	@Override
 	public void setGraphicalRepresentation(DeviceGraphics lane) {
 		this.laneGUI = (LaneGraphics) lane;
 	}
 
 	@Override
 	public String getName() {
 		return name;
 	}
 
 	public void setFeeder(FeederAgent feeder) {
 		this.feeder = feeder;
 	}
 
 	public void setNest(NestAgent nest) {
 		this.nest = nest;
 	}
 
 	public void thisFeederAgent(FeederAgent feeder) {
 		this.feeder = feeder;
 	}
 
 }
