 package factory;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import agent.Agent;
 import factory.interfaces.Lane;
 import factory.interfaces.Nest;
 import factory.masterControl.MasterControl;
 
 public class NestAgent extends Agent implements Nest {
 	
 	public NestAgent(MasterControl mc, Lane lane, int position) {
 		super(mc); // needed for the server 
 		this.position = position;
 		this.myLane = lane;
 	}
 
 
 	/** DATA **/
 	public ArrayList<MyPart> myParts = new ArrayList<MyPart>();
 	public Lane myLane;
 	public int numberOfParts = 0;
 	public enum NestState { NORMAL, NEEDS_TO_DUMP, HAS_STABILIZED, HAS_DESTABILIZED, PART_REMOVED, OUT_OF_PARTS}
 	public NestState nestState = NestState.NORMAL;
 	public Part currentPart;
 	public int position;
 	public enum MyPartState {  NEEDED, REQUESTED }
 	public class MyPart {
 		public Part pt;
 		public MyPartState state;
 
 		public MyPart(Part partType){
 			this.state = MyPartState.NEEDED;
 			this.pt = partType;
 		}
 	}
 	
 	
 
 	/** MESSAGES **/
 	public void msgDump() {
 		debug("received msgDump()");
 		nestState = NestState.NEEDS_TO_DUMP;
 		stateChanged();
 	}
 	
 	public void msgYouNeedPart(Part part) {
 		debug("received msgYouNeedPart("+part.name+").");
 		this.currentPart = part;
 		myParts.add(new MyPart(part));
 		stateChanged();
 	}
 	
 //	public void msgFeedingParts(int numParts) {
 //		debug("received " + numParts + " from the lane.");
 //		this.nestState = NestState.NORMAL;
 //		this.numberOfParts += numParts;
 //		stateChanged();
 //	}
 
 	
 	
 	/** 
 	 * Message from the animation notifying the NestAgent 
 	 * that its parts have stabilized after resettling.
 	 */
 	public void msgNestHasStabilized() {
 		//debug("NEST HAS STABILIZED");
 		nestState = NestState.HAS_STABILIZED;
 		stateChanged();
 	}
 	
 	/** 
 	 * Message notifying the NestAgent that its
 	 * parts have destabilized (become unstable!)
 	 */
 	public void msgNestHasDestabilized() {
 		//debug("NEST HAS BECOME UNSTABLE");
 		nestState = NestState.HAS_DESTABILIZED;
 		stateChanged();
 	}
 	
 //	/** 
 //	 * Message notifying the NestAgent that the 
 //	 * PartsRobot has grabbed one of the parts from its nest.
 //	 */
 //	public void msgPartsRobotGrabbingPartFromNest(int numPartsRemoved) {
 //		debug("PARTS ROBOT GRABS PART FROM NEST");
 //		
 //		this.numberOfParts--;
 //		
 //		if(this.numberOfParts < 0)
 //		{
 //			this.numberOfParts = 0;
 //		}
 //		
 //		nestState = NestState.PART_REMOVED;
 //
 //		if (this.numberOfParts == 0)
 //			tellMyLaneIAmOutOfParts();
 //		else
 //			stateChanged();
 //	}
 	
 
 	
 	
 	
 
 	/** SCHEDULER **/
 	public boolean pickAndExecuteAnAction() {
 		if (nestState == NestState.HAS_DESTABILIZED)
 		{
 			tellMyLaneIHaveBecomeUnstable();
 			return true;
 		}
 		else if (nestState == NestState.HAS_STABILIZED)
 		{
 			tellMyLaneIHaveBecomeStable();
 			return true;
 		}
//		else if (nestState == NestState.NEEDS_TO_DUMP)
//		{
//			//dump();
//			return true;
//		}
 		else if (nestState == NestState.PART_REMOVED)
 		{
 			tellMyLaneIHaveBecomeUnstable(); // same thing as becoming unstable as far as the lane is concerned
 			return true;
 		}
 		
 		for(MyPart p : myParts)
 		{
 
 			if (p.state == MyPartState.NEEDED)
 			{
 				askLaneToSendParts(p);
 				return true;
 			}
 		}
 		
 //		if (this.numberOfParts <= 0 && nestState != NestState.INTENTIONALLY_EMPTY_NEST && nestState != NestState.OUT_OF_PARTS)
 //		{
 //			tellMyLaneIAmOutOfParts();
 //			return true;
 //		}
 
 		
 		return false;
 	}
 
 
 	/** ACTIONS **/
 //	public void dump() {
 //		//DoDumpNest();
 //		debug("dumping nest.");
 //		this.numberOfParts = 0;
 //		nestState = NestState.NORMAL;
 //		stateChanged();
 //	}
 	
 //	private void tellMyLaneIAmOutOfParts() {
 //		debug("tell my lane I am out of parts, i have " + this.numberOfParts + " parts, my state is " + this.nestState);
 //		nestState = NestState.OUT_OF_PARTS; // we don't want to continue sending this message over and over again
 //		myLane.msgNestIsOutOfParts();
 //		stateChanged();
 //	}
 	
 	private void tellMyLaneIHaveBecomeStable() {
 		nestState = NestState.NORMAL; // we don't want to continue sending this message over and over again
 		myLane.msgNestHasStabilized();
 		stateChanged();
 	}
 	
 	private void tellMyLaneIHaveBecomeUnstable() {
 		nestState = NestState.NORMAL; // we don't want to continue sending this message over and over again
 		myLane.msgNestHasDestabilized();
 		stateChanged();
 	}
 	
 	public void askLaneToSendParts(MyPart part) { 
 		debug("asking lane to send parts of type "+part.pt.name + ".");
 		part.state = MyPartState.REQUESTED;
 		myLane.msgNestNeedsPart(part.pt);
 		stateChanged();
 	}
 	
 	
 	/** ANIMATIONS **/
 //	private void DoDumpNest() {
 //		debug("dumping.");
 //	}
 	
 	
 	/** OTHER **/
 	public void setLane(Lane la) {
 		myLane = la;
 	}
 	@Override
 	public String getNestName() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	
 	public Part getPart() {
 		return this.currentPart;
 	}
 
 	@Override
 	public int getPosition() {
 		// TODO Auto-generated method stub
 		return this.position;
 	}
 
 	
 
 	
 
 }
 
 
