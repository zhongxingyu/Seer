 package agent;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.Semaphore;
 
 import agent.data.Bin;
 import agent.data.Part;
 import agent.data.PartType;
 import agent.interfaces.Feeder;
 import agent.interfaces.Nest;
 
 import DeviceGraphics.DeviceGraphics;
 import GraphicsInterfaces.FeederGraphics;
 import GraphicsInterfaces.LaneGraphics;
 
 /**
  * Feeder receives parts from gantry and feeds the lanes
  * @author Arjun Bhargava
  */
 public class FeederAgent extends Agent implements Feeder {
 	
 	private GantryAgent gantry;
 	public List<MyLane> lanes= new ArrayList<MyLane>(); //Top Lane is the first lane, bottom is the second
 	private FeederGraphics feederGUI;
 	
 	private int currentOrientation;//0 for Top, 1 for Bottom
 	
 	public Bin bin;
 	
 	private FeederStatus state;
 
 	String name;
 	
 	public Semaphore animation = new Semaphore(0, true);
 	
 	public enum FeederStatus {
 		IDLE,REQUESTED_PARTS,FEEDING_PARTS,PURGING
 	}
 	
 	public enum LaneStatus {
 		DOES_NOT_NEED_PARTS,NEEDS_PARTS,GIVING_PARTS
 	};
 	
 	public class MyLane {
 		public LaneAgent lane;
 		public LaneStatus state;
 		public PartType type;
 		public int numPartsNeeded;
 		
 		public MyLane(LaneAgent lane,PartType type){
 			this.lane=lane;
 			this.type=type;
 			state=LaneStatus.NEEDS_PARTS;
 			numPartsNeeded=1;
 		}
 		public MyLane(LaneAgent lane){
 			this.lane=lane;
 			this.type=null;
 			state=LaneStatus.DOES_NOT_NEED_PARTS;
 			numPartsNeeded=0;
 		}
 	}
 
 	public FeederAgent(String name) {
 		super();
 		state=FeederStatus.IDLE;
 		this.name = name;
 		currentOrientation=0;
 		bin=null;
 	}
 
 	@Override
 	public void msgINeedPart(PartType type, LaneAgent lane) {
 		boolean found=false;
 		for(MyLane l:lanes){
 			if(l.lane.equals(lane)){
 				found=true;
 				l.numPartsNeeded++;
 				l.type=type;
 				if(l.state==LaneStatus.DOES_NOT_NEED_PARTS){
 					l.state=LaneStatus.NEEDS_PARTS;
 				}
 			}
 		}
 		if(!found){
 			lanes.add(new MyLane(lane,type));
 			print("added new lane");
 		}
 		stateChanged();
 	}
 
 	@Override
 	public void msgHereAreParts(PartType type, Bin bin) {
 		print("Recieved bin from gantry");
 		this.bin=bin;
 		for(MyLane lane:lanes){
 			if(lane.type==type){
 				lane.state=LaneStatus.GIVING_PARTS;
 				state=FeederStatus.FEEDING_PARTS;
 			}
 		}
 		stateChanged();
 	}
 	
 	@Override
 	public void msgRecieveBinDone(Bin bin) {
 		// TODO Auto-generated method stub
 		animation.release();
 	}
 
 	@Override
 	public void msgPurgeBinDone(Bin bin) {
 		// TODO Auto-generated method stub
 		animation.release();
 	}
 
 	@Override
 	public boolean pickAndExecuteAnAction() {
 		if(state==FeederStatus.IDLE){
 			for(MyLane lane:lanes){
 				if(lane.state==LaneStatus.NEEDS_PARTS){
 					getParts(lane);
 					return true;
 				}
 			}
 		}
 		if(state==FeederStatus.FEEDING_PARTS){
 			for(MyLane lane:lanes){
 				if(lane.state==LaneStatus.GIVING_PARTS){
 					if(lanes.get(currentOrientation).equals(lane)){
 						givePart(lane);
 						return true;
 					}
 					else {
 						flipDiverter();
 						return true;
 					}
 				}
 			}
 		}
 		if(state==FeederStatus.PURGING){
 			purgeBin();
 		}
 		return false;
 	}
 
 	public void getParts(MyLane lane) {
 		print("Telling gantry that I needs parts");
		//gantry.msgINeedParts(lane.type);
 		state=FeederStatus.REQUESTED_PARTS;
 		stateChanged();
 	}
 	
 	public void givePart(MyLane lane) {
 		print("Giving lane a part");
 		lane.numPartsNeeded--;
 		lane.lane.msgHereIsPart(new Part(lane.type));
 		if(lane.numPartsNeeded==0){
 			state=FeederStatus.PURGING;
 			lane.state=LaneStatus.DOES_NOT_NEED_PARTS;
 		}
 		stateChanged();
 	}
 	
 	public void purgeBin() {
 		print("Purging this feeder");
 		//feederGUI.purgeBin(bin.binGraphics);
 		try {
 			animation.acquire();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		gantry.msgremoveBinDone(bin);
 		bin=null;
 		state=FeederStatus.IDLE;
 		stateChanged();
 	}
 	
 	public void flipDiverter() {
 		print("Flipping the diverter");
 		//feederGUI.flipDiverter();
 		try {
 			animation.acquire();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		if(currentOrientation==0) {
 			currentOrientation=1;
 		}
 		else {
 			currentOrientation=0;
 		}
 	}
 
 	// GETTERS AND SETTERS
 	public void setGraphicalRepresentation(DeviceGraphics feeder) {
 		this.feederGUI = (FeederGraphics)feeder;
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
 	
 	public void setLanes(LaneAgent lane1,LaneAgent lane2) {
 		lanes.add(new MyLane(lane1));
 		lanes.add(new MyLane(lane2));
 	}
 	
 	public void thisLaneAgent(LaneAgent lane) {
 		lanes.add(new MyLane(lane));
 	}
 	
 
 }
