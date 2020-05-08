 package factory;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import agent.Agent;
 import factory.FeederAgent.FeederState;
 import factory.interfaces.Feeder;
 import factory.interfaces.Lane;
 import factory.interfaces.Nest;
 import factory.masterControl.MasterControl;
 
 public class LaneAgent extends Agent implements Lane {
 
 	public LaneAgent(MasterControl mc) {
 		super(mc);
 	}
 
 	/** DATA **/
 	public ArrayList<MyPart> myPartRequests = new ArrayList<MyPart>();
 	public List<Part> laneParts = Collections.synchronizedList(new ArrayList<Part>());
 	public int numberOfPartsInLane = 0;
 	
 	public Feeder myFeeder;
 	//	public enum LaneState { NORMAL, NEEDS_TO_PURGE, PURGING };  // not sure about this, need to update wiki still
 	//	public LaneState state = LaneState.NORMAL; 
 	public Nest myNest;
 	public Timer jamTimer = new Timer();
 	private static int kJAM_TIME = 4; // 4 seconds
 	public int amplitude = 5;
 	public static int kMAX_AMPLITUDE = 20;
 	public enum NestState { NORMAL, HAS_STABILIZED, HAS_DESTABILIZED,
 						NEEDS_TO_DUMP, WAITING_FOR_DUMP_CONFIRMATION,
 						NEST_WAS_DUMPED, NEEDS_TO_INCREASE_AMPLITUDE, IS_INCREASING_AMPLITUDE, AMPLITUDE_WAS_INCREASED }
 	public NestState nestState;
 
 	public enum MyPartState {  NEEDED, REQUESTED }
 
 	public class MyPart {
 		public Part pt;
 		public MyPartState state;
 
 		public MyPart(Part part){
 			this.state = MyPartState.NEEDED;
 			this.pt = part;
 		}
 	}
 
 
 	/** MESSAGES **/
 	public void msgPartAddedToLane(Part part) {
 		debug("RECEIVED: msgPartAddedToLane().");
 		laneParts.add(numberOfPartsInLane, part); // adds part to the index = numberOfPartsInNest
 		numberOfPartsInLane++;
 		stateChanged();
 	}
 	
 	public void msgPartRemovedFromLane() {
 		debug("RECEIVED: msgPartRemovedFromLane().");
 		if (laneParts.size() > 0)
 		{
 			synchronized(laneParts)
 			{
 				laneParts.remove(0); // remove the first part in the list aka the one closest to the nest
 			}
 
 			numberOfPartsInLane--;
 		}
 		stateChanged();
 	}
 	
 	public void msgIncreaseAmplitude() {
 		nestState = NestState.NEEDS_TO_INCREASE_AMPLITUDE;
 		stateChanged();
 	}
 
 	public void msgNestNeedsPart(Part part) {
 		debug("received msgNestNeedsPart("+part.name+").");
 		myPartRequests.add(new MyPart(part));
 		stateChanged();
 	}
 
 	public void msgNestHasStabilized() {
 		nestState = NestState.HAS_STABILIZED;
 		stateChanged();
 	}
 
 	public void msgNestHasDestabilized() {
 		nestState = NestState.HAS_DESTABILIZED;
 		stateChanged();
 	}
 	public void msgDumpNest() {
 		nestState = NestState.NEEDS_TO_DUMP;
 		stateChanged();
 	}
 
 	public void msgNestWasDumped() {
 		nestState = NestState.NEST_WAS_DUMPED;
 		stateChanged();
 	}
 
 	public void msgPurge() {
 		debug("received msgPurge()");
 		stateChanged();
 	}
 
 	/** SCHEDULER **/
 	public boolean pickAndExecuteAnAction() {
 		
 
 		for(MyPart p : myPartRequests)
 		{
 			if (p.state == MyPartState.NEEDED) 
 			{
 				askFeederToSendParts(p);
 				return true;
 			}
 		}
 		
 		if (nestState == NestState.HAS_DESTABILIZED)
 		{
 			tellFeederNestHasDeStabilized();
 			return true;
 		}
 		if (nestState == NestState.HAS_STABILIZED)
 		{
 			tellFeederNestHasStabilized();
 			return true;
 		}
 		if (nestState == NestState.NEEDS_TO_DUMP)
 		{
 			dumpNest();
 			return true;
 		}
 		if (nestState == NestState.NEST_WAS_DUMPED)
 		{
 			tellFeederNestWasDumped();
 			return true;
 		}
 		if (nestState == NestState.NEEDS_TO_INCREASE_AMPLITUDE)
 		{
 			increaseAmplitude();
 			return true;
 		}
 		if (nestState == NestState.AMPLITUDE_WAS_INCREASED)
 		{
 			tellFeederAmplitudeWasIncreased();
 		}
 
 
 		return false;
 	}
 
 
 
 
 	/** ACTIONS **/
 	public void tellFeederNestHasStabilized() {
 		nestState = NestState.NORMAL;
 		myFeeder.msgNestHasStabilized(this);
 		stateChanged();
 	}
 
 	private void tellFeederNestHasDeStabilized() {
 		nestState = NestState.NORMAL;
 		myFeeder.msgNestHasDeStabilized(this);
 		stateChanged();
 	}
 	
 	public void tellFeederNestWasDumped() {
 		//if (nestState != NestState.NEEDS_TO_INCREASE_AMPLITUDE) // unnecessary, will never happen
 		nestState = NestState.NORMAL;
 		myFeeder.msgNestWasDumped(this);
 		stateChanged();
 	}
 
 	public void increaseAmplitude() {
 		jamTimer.schedule(new TimerTask(){
 			public void run() {
 				nestState = NestState.AMPLITUDE_WAS_INCREASED;
 				debug("Lane amplitude timer expired.");
 				stateChanged();
 			}
 		},(long) kJAM_TIME * 1000); 
 
 		nestState = NestState.IS_INCREASING_AMPLITUDE;
 
 		stateChanged();
 	}
 	
 	public void tellFeederAmplitudeWasIncreased() 
 	{
 		myFeeder.msgLaneHasIncreasedItsAmplitude(this);
 	}
 
 	public void askFeederToSendParts(MyPart part) { 
 		debug("asking feeder to send parts of type " + part.pt.name + ".");
 		myFeeder.msgLaneNeedsPart(part.pt, this);
 		part.state = MyPartState.REQUESTED;
 		stateChanged();
 	}
 
 	public void dumpNest() { 
 		debug("telling my nest it should dump.");
 		nestState = NestState.WAITING_FOR_DUMP_CONFIRMATION;
 		stateChanged();
 	}
 
 
 
 	/** ANIMATIONS **/
 	private void DoIncreaseAmplitude(int amp) 
 	{
 		debug("amplitude increased to " + amp + ".");
 	}
 
 
 
 	/** OTHER **/
 
 	/** This method sets the nest of this lane.  
 	 * NOTE: Used for testing purposes only. 
 	 */
 	public void setNest(Nest n) {
 		myNest = n;
 	}
 
 	public Nest getNest() {
 		return myNest;
 	}
 
 	public void setFeeder(Feeder f) {
 		myFeeder = f;
 	}
 
 	@Override
 	public boolean hasMixedParts() {
 
 		if (laneParts.size() > 1)
 		{
 			String nameOfAPart = laneParts.get(0).name;
 
 			synchronized(laneParts)
 			{
 				for (Part p : laneParts)
 				{
 					if (p.name.equals(nameOfAPart) == false)
 					{
 						return true; // there is more than one type of part in the nest!
 					}
 				}
 			}
 		}
 
 		return false; // there is only ONE type of part in the nest
 	}
 	
 	
 	/** Overriding this for debugging purposes - print the Lane debug statements. */
 	protected void debug(String msg) {
 		if(true) {
 			print(msg, null);
 		}
 	}
 
 //	@Override
 //	public void msgFeedingParts(int numParts) {
 //		debug("msgFeedingParts()");
 //		myNest.msgFeedingParts(numParts);
 //		stateChanged();
 //	}
 	
 //	public void msgNestIsOutOfParts() {
 //		//debug("msgNestIsOutOfParts()");
 //		myFeeder.msgLaneIsOutOfParts(this);
 //		stateChanged();
 //	}
 
 
 
 
 }
