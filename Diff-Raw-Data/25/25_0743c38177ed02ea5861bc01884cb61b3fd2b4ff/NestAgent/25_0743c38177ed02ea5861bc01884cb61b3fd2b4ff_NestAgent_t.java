 package factory;
 
 import java.util.ArrayList;
 import java.util.Collections;
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
 	public List<Part> nestParts = Collections.synchronizedList(new ArrayList<Part>());
 	public int numberOfPartsInNest = 0;
 	public boolean beingUsed = false;
 	public Lane myLane;
 	public NestState nestState = NestState.STABLE;
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
 	
 	public void msgYouNeedPart(Part part) {
 		debug("received msgYouNeedPart("+part.name+").");
 		this.currentPart = part;
 		myParts.add(new MyPart(part));
 		stateChanged();
 	}
 	
 	public void msgPartAddedToNest(Part part) {
 		debug("RECEIVED: msgPartAddedToNest(" + part.name + ").");
 		nestParts.add(numberOfPartsInNest, part); // adds part to the index = numberOfPartsInNest
 		numberOfPartsInNest++;
 		stateChanged();
 	}
 	
 	public void msgPartRemovedFromNest(String partName) {
 		debug("RECEIVED: msgPartRemovedFromNest(" + partName + ").");
 		
 		synchronized(nestParts)
 		{
 			for (Part p : nestParts)
 			{
 				if (p.name.equals(partName))
 				{
 					nestParts.remove(p);
 				}
 			}
 		}
 		
 		numberOfPartsInNest--;
 		stateChanged();
 	}
 	
 
 	
 	/** 
 	 * Message from the animation notifying the NestAgent 
 	 * that its parts have stabilized after resettling.
 	 */
 	public void msgNestHasStabilized() {
 		debug("RECEIVED: msgNestHasStabilized().");
 		nestState = NestState.STABLE;
 		stateChanged();
 	}
 	
 	/** 
 	 * Message notifying the NestAgent that its
 	 * parts have destabilized (become unstable!)
 	 */
 	public void msgNestHasDestabilized() {
 		debug("RECEIVED: msgNestHasDestabilized().");
 		nestState = NestState.UNSTABLE;
 		stateChanged();
 	}
 	
 
 
 	
 	
 	
 
 	/** SCHEDULER **/
 	public boolean pickAndExecuteAnAction() {
 
 	
 		
 		for(MyPart p : myParts)
 		{
 
 			if (p.state == MyPartState.NEEDED)
 			{
 				askLaneToSendParts(p);
 				return true;
 			}
 		}
 		
 
 		
 		return false;
 	}
 
 
 	/** ACTIONS **/
 
 
 	
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
 		return this.position;
 	}
 
 	@Override
 	public boolean isBeingUsed() {
 		return this.beingUsed;
 	}
 
 	@Override
 	public List<Part> getParts() {
 		return this.nestParts;
 	}
 
 	@Override
 	public NestState getState() {
 		return this.nestState;
 	}
 
 	@Override
 	public Lane getLane() {
 		return this.myLane;
 	}
 	
 	public void setBeingUsed(boolean val){
 		this.beingUsed = val;
 	}
 	
 
 }
 
 
