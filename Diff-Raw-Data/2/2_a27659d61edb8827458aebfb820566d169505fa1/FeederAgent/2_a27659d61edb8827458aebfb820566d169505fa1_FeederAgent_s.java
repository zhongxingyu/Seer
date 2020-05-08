 package factory;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import agent.Agent;
 import factory.interfaces.Feeder;
 import factory.interfaces.Gantry;
 import factory.interfaces.Lane;
 import factory.interfaces.Vision;
 import factory.masterControl.MasterControl;
 import factory.test.mock.EventLog;
 
 
 public class FeederAgent extends Agent implements Feeder {
 
 	/** DATA **/
 
 	//agents
 	public Vision vision;
 	public Gantry gantry;
 
 	public int feederNumber;
 
 	private final static int kOK_TO_PURGE_TIME = 7; 
 	private final static int kPICTURE_DELAY_TIME = 3;
 	private int diverterSpeed = kOK_TO_PURGE_TIME; // intial speed is in-sync with the purge timer
 
 	public boolean hasASlowDiverter = false;
 	public boolean diverterSpeedWasReset = false;
 	public boolean visionShouldTakePicture = true;
 	
 	public List<MyPartRequest> requestedParts = Collections.synchronizedList(new ArrayList<MyPartRequest>());
 
 	public boolean debugMode;
 	public boolean printDebugStatements;
 	public Part currentPart;
 	Bin dispenserBin; // need to use this
 
 	//list of lanes per feeder
 	public MyLane topLane;
 	public MyLane bottomLane;
 
 	//list of timers to indicate when it's OK to feed, when the feeder is empty, and when the part is resettled
 	public Timer okayToPurgeTimer = new Timer();
 	public Timer takePictureTimer = new Timer();
 	public Timer slowDiverterTimer = new Timer();
 	public boolean feederHasABinUnderneath = false; // no bin underneath the feeder initially
 
 	//enum for the feeder state
 	public enum FeederState { EMPTY, WAITING_FOR_PARTS, CONTAINS_PARTS, OK_TO_PURGE, SHOULD_START_FEEDING, IS_FEEDING }
 	public FeederState state = FeederState.EMPTY;
 
 	//enum for the diverter state (feed top or bottom lane)
 	public enum DiverterState { FEEDING_TOP, FEEDING_BOTTOM }
 	public DiverterState diverter = DiverterState.FEEDING_BOTTOM;
 
 	//enum for the diverter's error state
 	public enum DiverterTimerState { TIMER_OFF, TIMER_EXPIRED, TIMER_RUNNING }
 	public DiverterTimerState diverterTimerState = DiverterTimerState.TIMER_OFF;
 	
 	//enum for part request state
 	public enum MyPartRequestState { NEEDED, ASKED_GANTRY, DELIVERED, PROCESSING }
 
 	//enum for picture state
 	public enum PictureState {UNSTABLE, STABLE, TOLD_VISION_TO_TAKE_PICTURE }
 
 	//enum for lane state
 	public enum MyLaneState {EMPTY, PURGING, CONTAINS_PARTS, BAD_NEST, 
 		TOLD_NEST_TO_DUMP, NEST_SUCCESSFULLY_DUMPED}
 
 	//enum for jam state
 	public enum JamState {MIGHT_BE_JAMMED, IS_JAMMED, TOLD_TO_INCREASE_AMPLITUDE, 
 		AMPLITUDE_WAS_INCREASED, NO_LONGER_JAMMED, MESSED_UP_BECAUSE_OTHER_LANE_HAS_MIXED_PARTS }
 
 	public EventLog log = new EventLog();
 
 
 	public class MyPartRequest {
 		public Part pt;
 		public MyPartRequestState state;
 		public Lane lane;
 
 		// alternative constructor #1
 		public MyPartRequest(Part part, Lane lane){
 			this.state = MyPartRequestState.NEEDED;
 			this.lane = lane;
 			this.pt = part;
 		}
 
 
 		// alternative constructor #2
 		public MyPartRequest(Part p, Lane l, MyPartRequestState s) {
 			this.pt = p;
 			this.lane = l;
 			this.state = s;
 		}
 	}
 
 
 
 	public class MyLane {
 
 		public Lane lane;
 		public MyLaneState state = MyLaneState.EMPTY;
 		public PictureState picState = PictureState.UNSTABLE; // initially it should be empty. essentially unstbale
 		public JamState jamState;
 		public int laneMightBeJammedMessageCount = 0;
 		public Part part;
 		public boolean hasMixedParts = false;
 
 		public MyLane(Lane lane){
 			this.state = MyLaneState.EMPTY;
 			this.lane = lane;
 			this.jamState = JamState.NO_LONGER_JAMMED;
 		}
 	}
 
 
 	public FeederAgent(String nameStr,int slot, Lane top, Lane bottom, Gantry g, 
 			Vision v, MasterControl mc, boolean debug, boolean printStatements) {
 		super(mc);
 		this.topLane = new MyLane(top);
 		this.bottomLane = new MyLane(bottom);
 		this.gantry = g;
 		this.vision = v;
 		this.feederNumber = slot;
 		this.debugMode = debug;
 		this.printDebugStatements = printStatements;
 	}
 
 
 	/** MESSAGES **/
 
 	/**
 	 *  The Lane sends this message to the Feeder notifying 
 	 *  him that his nest has become stable.
 	 *  @deprecated
 	 */ 
 	public void msgNestHasStabilized(Lane lane) {
 		//		myNestsHaveBeenChecked = false; // no longer used
 
 		// figure out which lane sent the message
 		if(topLane.lane == lane)
 		{
 			//			debug("My TOP lane has stabilized and so it's ready for a picture.");
 			topLane.picState = PictureState.STABLE;
 		}
 		else if(bottomLane.lane == lane)
 		{
 			//			debug("My BOTTOM lane has stabilized and so it's ready for a picture.");
 			bottomLane.picState = PictureState.STABLE;
 		}
 
 		// One of the nests has stabilized, let's check
 		// to see if the nests are ready for a picture:
 		//areMyNestsReadyForAPicture();
 
 		stateChanged();
 	}
 
 	/**
 	 *  The Lane sends this message to the Feeder notifying him that his nest has become unstable.
 	 *  @deprecated
 	 */ 
 	public void msgNestHasDeStabilized(Lane lane) {
 		//		myNestsHaveBeenChecked = false; // no longer used
 
 		// figure out which lane sent the message
 		if(topLane.lane == lane)
 		{
 			//			debug("Uhoh, my TOP lane has destabilized!");
 			topLane.picState = PictureState.UNSTABLE;
 		}
 		else if(bottomLane.lane == lane)
 		{
 			//			debug("Uhoh, my BOTTOM lane has destabilized!");
 			bottomLane.picState = PictureState.UNSTABLE;
 		}
 
 		stateChanged();
 	}
 
 	/** The GUI sends this message telling the Feeder to change its diverting algorithm 
 	 * so that the diverter is too slow in switching and parts will be fed into the wrong lane.
 	 * v.2
 	 */
 	public void msgBreakDiverterAlgorithm()
 	{
 		/**TODO: Implement this. */
 	}
 
 	/**
 	 *  The vision sends this message notifying the Feeder that its lane might be jammed.
 	 *  v.2
 	 * */
 	public void msgLaneMightBeJammed(int nestNumber) {
 		debug("received msgEmptyNest(" + nestNumber + ")");
 
 		
 		if (nestNumber == 0) 
 		{
 			topLane.laneMightBeJammedMessageCount++;
 
 			if (topLane.laneMightBeJammedMessageCount == 1) // There might be a jam -> the lane needs to increase its amplitude to dislodge the jammed part.
 			{
 				topLane.jamState = JamState.MIGHT_BE_JAMMED;
 			}
 			else if (topLane.laneMightBeJammedMessageCount > 1) // Increasing the amplitude (which breaks any lane jams) did not work.
 			{
 				// the only other possibility is that the diverter switching algorithm is incorrect
 				bottomLane.hasMixedParts = true;
 			}
 		}
 		else if (nestNumber == 1) 
 		{
 			bottomLane.laneMightBeJammedMessageCount++;
 
 			if (bottomLane.laneMightBeJammedMessageCount == 1) // There might be a jam -> the lane needs to increase its amplitude to dislodge the jammed part.
 			{
 				bottomLane.jamState = JamState.MIGHT_BE_JAMMED;
 			}
 			else if (bottomLane.laneMightBeJammedMessageCount > 1) // Increasing the amplitude (which breaks any lane jams) did not work.
 			{
 				// the only other possibility is that the diverter switching algorithm is incorrect
 				topLane.hasMixedParts = true;
 			}		
 		}
 
 
 		stateChanged();
 	}
 	
 	
 	/**
 	 *  The vision sends this message notifying the Feeder that one of its nests has heterogenous parts.
 	 *  v.2
 	 * */
 	public void msgNestHasMixedParts(int nestNumber)
 	{
 		if (nestNumber == 0)
 		{
 			topLane.hasMixedParts = true;
 		}
 		else if (nestNumber == 1)
 		{
 			bottomLane.hasMixedParts = true;
 		}
 		
 		stateChanged();
 	}
 
 //	/** 
 //	 * The lane sends this message after it has waited a sufficient amount of time
 //	 * after the lane has been told to increase its amplitude.
 //	 */
 //	public void msgLaneHasIncreasedItsAmplitude(Lane la)
 //	{
 //		if (topLane.lane == la)
 //		{
 //			topLane.jamState = JamState.AMPLITUDE_WAS_INCREASED;
 //		}
 //		else if (bottomLane.lane == la)
 //		{
 //			bottomLane.jamState = JamState.AMPLITUDE_WAS_INCREASED;
 //		}
 //
 //		stateChanged();
 //	}
 
 	/** One of the Feeder's lanes sends this message to the
 	 *  Feeder telling him that his nest was sucessfully dumped.
 	 */
 	public void msgNestWasDumped(Lane la) {
 		String laneStr = "Top Lane";
 		if (bottomLane.lane == la)
 			laneStr = "Bottom Lane";
 
 		debug("received msgNestWasDumped("+ laneStr + ")");
 		if (topLane.lane == la)
 		{
 			topLane.state = MyLaneState.NEST_SUCCESSFULLY_DUMPED;
 		}
 		else if (bottomLane.lane == la)
 		{
 			bottomLane.state = MyLaneState.NEST_SUCCESSFULLY_DUMPED;
 		}
 		stateChanged();
 	}
 
 
 	/** One of the Feeder's lanes sends this message to the Feeder when he wants a part. 
 	 *  It is the end of a chain of messages originally sent by the PartsRobot.
 	 */
 	public void msgLaneNeedsPart(Part part, Lane lane) {
 		debug("received msgLaneNeedsPart(" + part.name + ")");
 		//		MyLane targetLane = null;
 		//		
 		//		if (topLane.lane == lane)
 		//			targetLane = topLane;
 		//		else
 		//			targetLane = bottomLane;
 
 		if (lane != null && part != null)
 		{
 			//			if (lane.getNest().getPart() != null)
 			//			{
 			//			if (lane.getNest().getPart().id != part.id)
 			//				purgeLane(targetLane);
 			//			}
 
 			requestedParts.add(new MyPartRequest(part, lane)); // add this part request to the list of part requests
 		}
 		stateChanged();
 	}
 
 
 	/** The Gantry sends this message when he is 
 	 *  delivering parts to the Feeder. */
 	public void msgHereAreParts(Part part) {
 		debug("received msgHereAreParts("+part.name+")");
 
 		feederHasABinUnderneath = true; // should never be false again
 
 		synchronized(requestedParts)
 		{
 			for (MyPartRequest pr : requestedParts) {
 				if(pr.state == MyPartRequestState.ASKED_GANTRY && pr.pt.id == part.id) {
 					pr.state = MyPartRequestState.DELIVERED;
 				}
 			}
 		}
 		stateChanged();
 	}
 
 	/**
 	 *  [v2]
 	 *  The vision sends this message notifying 
 	 *  the Feeder that it had a bad nest. 
 	 * */
 	public void msgBadNest(int nestNumber) {
 
 
 		if (nestNumber == 0) // top nest 
 		{
 			debug("received msgBadNest(topNest)");
 			topLane.state = MyLaneState.BAD_NEST;
 		}
 		else if (nestNumber == 1) // bottom nest
 		{
 			debug("received msgBadNest(bottomNest)");
 			bottomLane.state = MyLaneState.BAD_NEST;
 		}
 
 //		else if (nestNumber == -1) {
 //			debug("received msgBadNest(currentNest)");
 //			if (diverter == DiverterState.FEEDING_BOTTOM)
 //				bottomLane.state = MyLaneState.BAD_NEST;
 //			else
 //				topLane.state = MyLaneState.BAD_NEST;
 //		}
 
 
 
 		stateChanged();
 	}
 
 
 	/** SCHEDULER **/
 	@Override
 	public boolean pickAndExecuteAnAction() {
 		
 		// Determine if the Nests need to be checked to see if they are ready for a picture
 		if (visionShouldTakePicture == true)
 		{
 			takePicture();
 			return true;
 		}
 		
 		if (diverterTimerState == DiverterTimerState.TIMER_EXPIRED && this.hasASlowDiverter)
 		{
 			DoSwitchLane();
 			diverterTimerState = DiverterTimerState.TIMER_OFF;
 			return true;
 		}
 		
 		if (diverterSpeedWasReset)
 		{
 		//	DoSwitchLane(); // this is to fix a problem that disabled us from correcting the slow diverter once it occured
 			diverterSpeedWasReset = false;
 			return true;
 		}
 
 		if (state == FeederState.EMPTY || state == FeederState.OK_TO_PURGE)
 		{
 			synchronized(requestedParts)
 			{
 				for (MyPartRequest p : requestedParts)
 				{
 					if (p.state == MyPartRequestState.NEEDED)
 					{
 						//askGantryForPart(p);
 						handlePartRequest(p);
 						return true;
 					}
 				}
 			}
 		}
 
 
 		if (state == FeederState.SHOULD_START_FEEDING) 
 		{ 
 			//not sure if this is needed...
 		}
 
 		synchronized(requestedParts)
 		{
 			for (MyPartRequest p : requestedParts) 
 			{
 				if (p.state == MyPartRequestState.DELIVERED)
 				{
 					//processFeederParts(p);
 					handleDeliveredPart(p);
 					return true;
 				}
 			}
 		}
 
 		if (topLane.state == MyLaneState.BAD_NEST) 
 		{
 			dumpNest(topLane);
 			return true;
 		}
 
 		if (bottomLane.state == MyLaneState.BAD_NEST)
 		{
 			dumpNest(bottomLane);
 			return true;
 		}
 
 		if (topLane.jamState == JamState.MIGHT_BE_JAMMED)
 		{
 			unjamLane(topLane);
 			return true;
 		}
 
 		if (bottomLane.jamState == JamState.MIGHT_BE_JAMMED)
 		{
 			unjamLane(bottomLane);
 			return true;
 		}
 		
 		if (topLane.hasMixedParts)
 		{
 			nestHasMixedParts(topLane);
 			return true;
 		}
 		
 		if (bottomLane.hasMixedParts)
 		{
 			nestHasMixedParts(bottomLane);
 			return true;
 		}
 		
 		/*
 		if (topLane.jamState == JamState.MESSED_UP_BECAUSE_OTHER_LANE_HAS_MIXED_PARTS)
 		{
 			laneHasMixedParts(bottomLane);
 			return true;
 		}
 		
 		if (bottomLane.jamState == JamState.MESSED_UP_BECAUSE_OTHER_LANE_HAS_MIXED_PARTS)
 		{
 			laneHasMixedParts(bottomLane);
 			return true;
 		}
 		*/
 
 //		if (topLane.jamState == JamState.AMPLITUDE_WAS_INCREASED)
 //		{
 //			laneIsNoLongerJammed(topLane);
 //			return true;
 //		}
 //
 //		if (bottomLane.jamState == JamState.AMPLITUDE_WAS_INCREASED)
 //		{
 //			laneIsNoLongerJammed(bottomLane);
 //			return true;
 //		}
 
 		if (topLane.state == MyLaneState.NEST_SUCCESSFULLY_DUMPED)
 		{
 			nestWasDumped(topLane);
 			return true;
 		}
 
 		if (bottomLane.state == MyLaneState.NEST_SUCCESSFULLY_DUMPED)
 		{
 			nestWasDumped(bottomLane);
 			return true;
 		}
 
 
 		// If nothing else has been called, then return false!
 		return false;
 	}
 
 
 
 
 
 	/** ACTIONS **/
 	private void nestHasMixedParts(MyLane la)
 	{
 		la.hasMixedParts = false;
 		
 		// purge the lane with the mixed parts
 		if (topLane == la)
 		{
 			DoPurgeTopLane();
 		}
 		else if (bottomLane == la)
 		{
 			DoPurgeBottomLane();
 		}
 	}
 	
 	private void laneHasMixedParts(MyLane la) {
 		
 		// Get more parts for the other lane because it is
 		// lacking in parts - its parts were fed to the wrong lane		
 		this.msgLaneNeedsPart(la.part, la.lane); 
 		la.jamState = JamState.NO_LONGER_JAMMED;
 		la.laneMightBeJammedMessageCount = 0; // reset the count because we have reached the end of this logic checking and handled it.
 		
 		la.hasMixedParts = false;
 		
 		// purge the lane with the mixed parts
 		if (topLane == la)
 		{
 			DoPurgeTopLane();
 		}
 		else if (bottomLane == la)
 		{
 			DoPurgeBottomLane();
 		}
 				
 	}
 	
 	private void unjamLane(MyLane la)
 	{
 		if (topLane == la)
 		{
 			debug("action Unjamming top lane.");
 		}
 		else
 		{
 			debug("action Unjamming bottom lane.");
 		}
 
 		la.jamState = JamState.NO_LONGER_JAMMED;
 		
 		DoIncreaseLaneAmplitude(la); // increase lane amplitude to dislodge the jammed object
 		DoUnjamLane(la); // if the lane was jammed, this will unjam it
 		DoResetLaneAmplitude(la); // and turn the amplitude back down to a normal amplitude
 	}
 
 //	private void laneIsNoLongerJammed(MyLane la)
 //	{
 //		debug("Lane is no longer jammed!");
 //
 //		la.jamState = JamState.NO_LONGER_JAMMED;
 //		DoUnjamLane(la);
 //		DoResetLaneAmplitude(la);
 //	}
 
 //	private void feedParts(MyPartRequest mpr) {
 //		debug("feedParts("+mpr.pt.name+")");
 //
 //		if (topLane.lane == mpr.lane) // topLane.part is set in the processFeederParts() method
 //		{
 //			debug("3a");
 //			if (topLane.state == MyLaneState.EMPTY || topLane.state == MyLaneState.CONTAINS_PARTS)
 //			{
 //				debug("3b");
 //				// check to see if the lane diverter needs to switch
 //				if (diverter == DiverterState.FEEDING_BOTTOM) {
 //					debug("3c");
 //					diverter = DiverterState.FEEDING_TOP;
 //					DoSwitchLane();   // Animation to switch lane
 //				}
 //
 //
 //				topLane.part = mpr.pt; // IMPORTANT: We must set this in order for the lane
 //				//  to know if it needs to purge in the future
 //
 //				state = FeederState.IS_FEEDING; // we don't want to call this code an infinite number of times
 //				startFeeding();
 //				//					return true;
 //			}
 //			//				return true;
 //		}
 //		//		} // note: bottomeLane.part should never be null
 //
 //		//		if (bottomLane.part != null) // bottomLane.part is set in the processFeederParts() method
 //		//		{
 //		if (bottomLane.lane == mpr.lane)
 //		{
 //			debug("4a");
 //			if (bottomLane.state == MyLaneState.EMPTY || bottomLane.state == MyLaneState.CONTAINS_PARTS)
 //			{
 //				debug("4b");
 //				// check to see if the lane diverter needs to switch
 //				if (diverter == DiverterState.FEEDING_TOP) {
 //					debug("4c");
 //					diverter = DiverterState.FEEDING_BOTTOM;
 //					DoSwitchLane();   // Animation to switch lane
 //				}
 //
 //				bottomLane.part = mpr.pt; // IMPORTANT: We must set this in order for the lane
 //				//  to know if it needs to purge in the future
 //
 //				state = FeederState.IS_FEEDING; // we don't want to call this code an infinite number of times
 //				startFeeding();
 //				//					return true;
 //			}
 //			//				return true;
 //		}
 //		//		}
 //		stateChanged();
 //		//		return true;
 //	}
 
 	/** This action checks to see if the feeder should tell the vision to take a picture. **/
 	private void takePicture() {
 		if (this.requestedParts.size() == 0)
 		{
 			debug("sending nests are ready for picture = ("+topLane.lane.getNest()+","+bottomLane.lane.getNest()+")");
 			vision.msgMyNestsReadyForPicture(topLane.lane.getNest(), bottomLane.lane.getNest(), this); // send the message to the vision
 		}
 		visionShouldTakePicture = false; // pic was taken	
 
 		
 		takePictureTimer.schedule(new TimerTask(){
 			public void run() {
 				visionShouldTakePicture = true;
 				stateChanged();
 			}
 		},(long) kPICTURE_DELAY_TIME * 1000); // we set this to a slightly different time for each feeder
 		
 	}
 
 
 	private void nestWasDumped(MyLane la) {
 		String laneStr = "Top Lane";
 		if (bottomLane == la)
 			laneStr = "Bottom Lane";
 
 		debug("has dumped nest of lane " + laneStr + ".");
 		if (this.state == FeederState.CONTAINS_PARTS)
 		{
 			// only if the feeder is actually feeding
 			la.state = MyLaneState.CONTAINS_PARTS;
 			DoContinueFeeding(currentPart);
 		}
 		else // the nestWasDumped from within a purge cycle
 		{
 			la.state = MyLaneState.PURGING;
 		}
 		stateChanged();
 	}
 
 	private void dumpNest(MyLane la) {
 		// Print the correct lane name
 		String laneStr = "Top Lane";
 		if (bottomLane == la)
 			laneStr = "Bottom Lane";
 
 		debug("Action: dump the "+ laneStr + "'s nest.");
 
 		
 		if (la == topLane)
 		{
 			DoDumpTopNest(); 
 		}
 		else if (la == bottomLane)
 		{
 			DoDumpBottomNest(); 
 		}
 		
 		la.state = MyLaneState.EMPTY;
 		
 
 	}
 
 
 	/**
 	 * This is a helper method that will switch the Feeder's diverter based on
 	 * the desired target lane and the diverter's current status/position.
 	 */
 	private void switchDiverterIfNecessary(MyLane targetLane)
 	{
 		// Set the back-end logic to know which lane it should be feeding:
 		if (targetLane == topLane)
 		{
 			if (diverter == DiverterState.FEEDING_BOTTOM) {
 				diverter = DiverterState.FEEDING_TOP;
 				if (this.hasASlowDiverter) // Switch the diverter based on a timer
 				{
 					// Switch the diverter basedon the speed of the diverter [v.2]
 					diverterTimerState = DiverterTimerState.TIMER_RUNNING;
 					slowDiverterTimer.schedule(new TimerTask(){
 						public void run() {
 							diverterTimerState = DiverterTimerState.TIMER_EXPIRED;
 							debug("Diverter slow timer has expired.");
 							stateChanged();
 						}
 					},(long) diverterSpeed * 1000); // switch the diverter after this many seconds
 				}
 				else // Switch the diverter immediately
 				{
 					DoSwitchLane();
 				}
 			}
 		}
 		else if (targetLane == bottomLane)
 		{
 			if (diverter == DiverterState.FEEDING_TOP) {
 				diverter = DiverterState.FEEDING_BOTTOM;
 				if (this.hasASlowDiverter) // Switch the diverter based on a timer
 				{
 					// Switch the diverter basedon the speed of the diverter [v.2]
 					diverterTimerState = DiverterTimerState.TIMER_RUNNING;
 					slowDiverterTimer.schedule(new TimerTask(){
 						public void run() {
 							diverterTimerState = DiverterTimerState.TIMER_EXPIRED;
 							debug("Diverter slow timer has expired.");
 							stateChanged();
 						}
 					},(long) diverterSpeed * 1000); // switch the diverter after this many seconds
 				}
 				else // Switch the diverter immediately
 				{
 					DoSwitchLane();
 				}
 			}
 		}
 
 		
 		
 	}
 
 	private void handlePartRequest(MyPartRequest partRequested) {
 
 		MyLane targetLane = targetLaneOfPartRequest(partRequested);
 
 		// The combination of these boolean values will represent the different scenarios to be handled:
 		boolean FeederIsEmpty = (currentPart == null);
 		boolean TargetLaneIsEmpty = (targetLane.part == null);
 		//		boolean OkToPurgeNow = (this.state == FeederState.OK_TO_PURGE);
 		boolean FeederHasDesiredPart;
 		boolean TargetLaneHasDesiredPart;
 
 		// Make sure these variable assignments don't cause null pointer exceptions
 		if (currentPart != null)
 			FeederHasDesiredPart = (currentPart.id == partRequested.pt.id);
 		else
 			FeederHasDesiredPart = false; // its null, so can't be the desired part
 
 		if (targetLane.part != null)
 			TargetLaneHasDesiredPart = (targetLane.part.id == partRequested.pt.id);
 		else
 			TargetLaneHasDesiredPart = false; // its null, so can't be the desired part
 
 
 		// First, switch the diverter if you need to.
 		switchDiverterIfNecessary(targetLane);
 		
 
 		/* SCENARIO #1: The very first part fed into the Feeder.
 		 * The Feeder is empty.
 		 * The target lane is empty.
 		 */
 		if (FeederIsEmpty && TargetLaneIsEmpty)
 		{
 			debug("SCENARIO #1");
 			state = FeederState.WAITING_FOR_PARTS;
 			partRequested.state = MyPartRequestState.ASKED_GANTRY;
 			gantry.msgFeederNeedsPart(partRequested.pt, this);
 		}
 
 
 		/* SCENARIO #2: We want to feed two lanes from the same Feeder without purging the feeder or the empty lane.
 		 * The Feeder has the correct part.
 		 * The target lane is empty.
 		 */
 		else if (FeederHasDesiredPart && TargetLaneIsEmpty)
 		{
 			debug("SCENARIO #2");
 			state = FeederState.IS_FEEDING;
 			partRequested.state = MyPartRequestState.DELIVERED;
 		}
 
 
 		/* SCENARIO #3: We want to feed two lanes from the same Feeder without purging the feeder, but we do want to purge the lane with the wrong parts.
 		 * The Feeder has the correct part.
 		 * The target lane has the wrong part.
 		 * The target lane is NOT empty, it is filled with the wrong part.
 		 */
 		else if (FeederHasDesiredPart && !TargetLaneHasDesiredPart && !TargetLaneIsEmpty)
 		{
 			debug("SCENARIO #3");
 			purgeLane(targetLane);
 			state = FeederState.IS_FEEDING;
 			partRequested.state = MyPartRequestState.DELIVERED;
 		}
 
 
 		/* SCENARIO #4: The PartsRobot has simply asked for more of the same type of parts to go to a nest, and the Feeder contains those parts currently.
 		 * The Feeder has the correct part.
 		 * The target lane has the right part.
 		 */
 		else if (FeederHasDesiredPart && TargetLaneHasDesiredPart)
 		{
 			debug("SCENARIO #4");
 			state = FeederState.IS_FEEDING;
 			partRequested.state = MyPartRequestState.DELIVERED;
 		}
 
 
 		/* SCENARIO #5: This case will most likely happen on a new kit configuration.
 		 * The Feeder has the wrong part.
 		 * The target lane has the wrong part.
 		 * The target lane is not empty.
 		 */
 		else if (!FeederHasDesiredPart && !TargetLaneHasDesiredPart && !TargetLaneIsEmpty)
 		{
 			debug("SCENARIO #5");
 			purgeLane(targetLane);
 			purgeFeeder();  
 			state = FeederState.WAITING_FOR_PARTS;
 			partRequested.state = MyPartRequestState.ASKED_GANTRY;
 			gantry.msgFeederNeedsPart(partRequested.pt, this);
 		}
 
 
 		/* SCENARIO #6: The Feeder has the wrong part but the target lane has the right part.
 		 * The Feeder has the wrong part.
 		 * The target lane can be empty or have the right part.
 		 * part even, but this will be handled when the Gantry delivers the parts.
 		 */
 		else if (!FeederHasDesiredPart && (TargetLaneHasDesiredPart || TargetLaneIsEmpty))
 		{
 			debug("SCENARIO #6");
 			purgeFeeder();  
 			state = FeederState.WAITING_FOR_PARTS;
 			debug("1");
 			partRequested.state = MyPartRequestState.ASKED_GANTRY;
 			debug("2");
 			gantry.msgFeederNeedsPart(partRequested.pt, this);
 			debug("3");
 		}
 
 
 
 
 
 		//		stateChanged();
 	}
 
 
 	private void handleDeliveredPart(MyPartRequest deliveredPart) {
 
 		MyLane targetLane = targetLaneOfPartRequest(deliveredPart);
 
 //		// First, switch the diverter if you need to.
 //		switchDiverterIfNecessary(targetLane);
 
 		// Remove the part request, it is no longer needed
 		synchronized(requestedParts)
 		{
 			requestedParts.remove(deliveredPart);
 		}
 		currentPart = deliveredPart.pt;  // the Feeder's current part is now the delivered part
 
 		// Set the target lane's part to be the part that is being fed to it
 		if (targetLane == topLane)
 		{
 			topLane.part = deliveredPart.pt; // IMPORTANT: We must set this in order for the lane to know if it needs to purge in the future
 		}
 		else if (targetLane == bottomLane)
 		{
 			bottomLane.part = deliveredPart.pt; // IMPORTANT: We must set this in order for the lane to know if it needs to purge in the future
 		}
 
 
 		startFeeding();
 
 
 		//stateChanged(); // there is a stateChanged() inside startFeeding()
 	}
 
 
 //	private void askGantryForPart(MyPartRequest partRequested) { 
 //		debug("asking gantry for part " + partRequested.pt.name + ".");
 //
 //		// The feeder won't need to be purged:
 //		/** TODO: Somehow we need to check the case where the top lane has part A 
 //		 *        but wants part B which is in the Feeder and is in the bottom lane. 
 //		 *        The Feeder shouldn't have to be purged, only the lane.
 //		 *        */
 //
 //		MyLane targetLane = null;
 //		if (partRequested.lane == topLane.lane)
 //			targetLane = topLane;
 //		else if (partRequested.lane == bottomLane.lane)
 //			targetLane = bottomLane;
 //		else
 //		{
 //			try {
 //				throw new Exception("ERROR: The Feeder does not have that lane.");
 //			} catch (Exception e) {
 //				e.printStackTrace();
 //			}
 //		}
 //
 //		boolean flag = false; // we only want to choose one of the following cases:
 //
 //
 //		/* ### The first time a lane is fed a part at all. ###
 //		 * We must set the targetLane.part otherwise we will see null pointer exceptions.
 //		 */
 //		if (targetLane.part == null)
 //		{
 //			if (currentPart == null)
 //			{
 //				state = FeederState.WAITING_FOR_PARTS;
 //				partRequested.state = MyPartRequestState.ASKED_GANTRY;
 //				gantry.msgFeederNeedsPart(partRequested.pt, this);
 //			}
 //			else if (currentPart != partRequested.pt)
 //			{
 //				state = FeederState.WAITING_FOR_PARTS;
 //				partRequested.state = MyPartRequestState.ASKED_GANTRY;
 //				gantry.msgFeederNeedsPart(partRequested.pt, this);
 //			}
 //			else
 //			{
 //				debug("first part into a lane - No purging needed at all.");
 //				state = FeederState.IS_FEEDING;
 //				partRequested.state = MyPartRequestState.DELIVERED;
 //				flag = true;
 //			}
 //		}
 //
 //		else if (this.currentPart != null && targetLane.part != null)
 //		{
 //			/* ### No purging at all ###
 //			 * the current part inside the feeder is the same as the requested part 
 //			 * AND the feeder is NOT empty
 //			 * AND the targetLane's part is the same as the partRequested.
 //			 */
 //			if (this.currentPart.id == partRequested.pt.id && 
 //					state != FeederState.EMPTY && 
 //					targetLane.part.id == partRequested.pt.id)
 //			{
 //				debug("No purging needed at all.");
 //				state = FeederState.IS_FEEDING;
 //				partRequested.state = MyPartRequestState.DELIVERED;
 //				flag = true;
 //			}
 //		}
 //
 //		/* ### Purge the lane only ###
 //		 * the current part inside the feeder is the same as the requested part 
 //		 * AND the feeder is NOT empty
 //		 * AND the targetLane's part is NOT the partRequested.
 //		 */
 //		/** TODO: NULL POINTER HERE UNLESS I DO TARGETLANE.PART != NULL. */
 //		if (flag == false)
 //		{
 //			if (this.currentPart != null && targetLane.part != null)
 //			{
 //				if (this.currentPart.id == partRequested.pt.id 
 //						&& state != FeederState.EMPTY 
 //						&& targetLane.part.id != partRequested.pt.id)
 //				{
 //					if (targetLane == topLane)
 //						debug("Feeder doesn't need to purge but the top lane does.");
 //					else
 //						debug("Feeder doesn't need to purge but the bottom lane does.");
 //
 //					state = FeederState.IS_FEEDING;
 //					partRequested.state = MyPartRequestState.DELIVERED;
 //					purgeLane(targetLane);
 //					flag = true;
 //				}
 //			}
 //		}
 //
 //		/* ### Purge both the Feeder and the Lane. ###
 //		 * Note: This basically says purge if you need to and/or just act as if the
 //		 * Feeder was empty and go into the waiting for parts state, msg the Gantry, etc.
 //		 */
 //		if (flag == false)
 //		{
 //			if (purgeIfNecessary(partRequested) || this.state == FeederState.EMPTY )
 //			{
 //				state = FeederState.WAITING_FOR_PARTS;
 //				partRequested.state = MyPartRequestState.ASKED_GANTRY;
 //				gantry.msgFeederNeedsPart(partRequested.pt, this);
 //			}
 //		}
 //
 //		stateChanged();
 //	}
 
 //	private boolean purgeIfNecessary(MyPartRequest partRequested) { 
 //		debug("purging if necessary.");
 //		debug("state = " + state);
 //		debug("partRequested.lane == topLane.lane is " + (topLane.lane == partRequested.lane));
 //		debug("partRequested.lane == bottomLane.lane is " + (bottomLane.lane == partRequested.lane));
 //		debug("partRequested.pt.name = " + partRequested.pt.name);
 //
 //		boolean purging = false; // assume we won't be purging
 //
 //		// Check if Feeder needs to be purged
 //		if (this.currentPart != partRequested.pt && state == FeederState.OK_TO_PURGE)
 //		{
 //			debug("0a");
 //			purging = true;
 //			purgeFeeder();	
 //		}
 //
 //
 //		// Check if lane needs to be purged
 //		if (topLane.lane == partRequested.lane && state == FeederState.OK_TO_PURGE)
 //		{
 //			debug("1a");
 //			if (topLane.part != null) // the topLane's part is initially null, which is always != to 
 //			{						  // the partRequested.pt, but we don't want to purge when the lane is empty!
 //
 //				debug("1b");
 //				if (topLane.part.id != partRequested.pt.id)
 //				{
 //					debug("1c");
 //					purging = true;
 //					purgeLane(topLane);
 //				}
 //			}
 //		}
 //
 //		if (bottomLane.lane == partRequested.lane && (state == FeederState.OK_TO_PURGE))
 //		{
 //			debug("2a");
 //			if (bottomLane.part != null) // the bottomlane's part is initially null, which is always != to
 //			{						  	 // the partRequested.pt, but we don't want to purge when the lane is empty!
 //				debug("2b");
 //				if (bottomLane.part.id != partRequested.pt.id)
 //				{
 //					debug("2c");
 //					purging = true;
 //					purgeLane(bottomLane);
 //				}
 //			}
 //		}
 //
 //		if (purging == true)
 //			debug("yep, purging.");
 //		else
 //			debug("nope, not purging.");
 //
 //		return purging;
 //	}
 
 	private void purgeFeeder(){
 
 		DoStopFeeding(); // Stop sending parts into the lane(s)
 		debug("1.1");
 		DoPurgeFeeder(); // Purge the feeder 
 		debug("1.2");
 
 		currentPart = null; // and the feeder no longer has a part in it, so its current part is null
 
 	}
 
 	private void purgeLane(MyLane myLane){
 		myLane.state = MyLaneState.PURGING; // The lane is now purging		
 
 		DoStopFeeding(); // before you can purge the lane you must stop feeding to it.
 
 		// Call the purge lane animation for the appropriate lane
 		if (myLane == topLane)
 		{
 			DoPurgeTopLane();
 		}
 		else if (myLane == bottomLane)
 		{
 			DoPurgeBottomLane();
 		}
 
 		myLane.state = MyLaneState.EMPTY; // we have received a message from the animation telling us that the lane has been purged
 
 		//myLane.lane.msgPurge(); // tell the lane to purge (v.2 if needed)
 
 	}
 
 	/** This action processes the Feeder parts after 
 	 * they have been delivered to the Feeder. */
 //	private void processFeederParts(MyPartRequest mpr){
 //		debug("processing feeder parts of type "+mpr.pt.name);
 //
 //		mpr.state = MyPartRequestState.PROCESSING; // the part request is being processed
 //
 //		// Remove the part request, it is no longer needed
 //		synchronized(requestedParts)
 //		{
 //			requestedParts.remove(mpr);
 //		}
 //
 //		currentPart = mpr.pt; // Set the current part to be the requested part
 //
 //		// Check to see if the request part is null (this would be an error)
 //		if (mpr.pt == null)
 //			debug("Mpr.pt IS NULL");
 //
 //		// Set the target lane's part to be the requested part
 //		//		if (bottomLane.lane == mpr.lane){
 //		//			bottomLane.part = mpr.pt;
 //		//		}
 //		//		else {
 //		//			topLane.part = mpr.pt;
 //		//		}
 //
 //		//feedParts(mpr);
 //		// The feeder should start feeding now
 //		//state = FeederState.SHOULD_START_FEEDING;
 //
 //		stateChanged();
 //	}
 
 
 	private void startFeeding(){
 		//debug("action start feeding.");
 
 		state = FeederState.IS_FEEDING; // we want to feed until the feeder's state changes to OK_TO_PURGE
 
 		// This timer will run so that we don't automatically purge a
 		// lane when a second request comes to the feeder (note: the 
 		// feeder almost always has multiple requests coming in at once)
 		okayToPurgeTimer.schedule(new TimerTask(){
 			public void run() {
 				state = FeederState.OK_TO_PURGE;
 				DoStopFeeding(); // for v.0/v.1
 				debug("it is now okay to purge this feeder.");
 				stateChanged();
 			}
 		},(long) kOK_TO_PURGE_TIME * 1000); // okay to purge after this many seconds
 
 
 		DoStartFeeding(currentPart); // Feed the part that is currently in the Feeder 
 		// into its appropriate lane
 
 		//stateChanged();
 	}
 
 
 	/** This method sets up the pointers to this Feeder's Lanes. **/
 	public void setUpLanes(Lane top,Lane bottom) 
 	{
 		topLane = new MyLane(top);
 		bottomLane = new MyLane(bottom);
 	}
 
 	/** This method connects the feeder to the gantry. **/
 	public void setGantry(Gantry g)
 	{
 		this.gantry = g;
 	}
 
 
 	/** This method adds a MyPartRequest to the requestedParts list. 
 	 *  It is used for testing purposes only.
 	 */
 	public void addMyPartRequest(Part p, Lane l, MyPartRequestState s) {
 		requestedParts.add(new MyPartRequest(p,l,s));
 	}
 
 
 
 
 
 
 	/** ANIMATIONS */
 
 
 	/** Animation that increases the amplitude of the lane. **/
 	private void DoIncreaseLaneAmplitude(MyLane la)
 	{
 		int laneNum;
 		
 		if (topLane == la)
 		{
 			laneNum = 0;
 		}
 		else
 		{
 			laneNum = 1;
 		}
 		
 		if (debugMode == false)
 		{
			server.command(this,"fa lm set laneamplitude " + (feederNumber+laneNum) + " " + 8); // 0-7lane# 1-8amplitude
 			debug("Feeder " + feederNumber + " set lane amplitude.");
 			try {
 				animation.acquire();
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 	} 				
 
 
 	/** Resets the amplitude of the lane to a normal amplitude. **/
 	private void DoResetLaneAmplitude(MyLane la)
 	{
 		// figure out what the lane number is
 		int laneNum0or1 = -1; 
 		if (topLane == la)
 		{
 			laneNum0or1 = 0; // top lane
 		}
 		else if (bottomLane == la)
 		{
 			laneNum0or1 = 1; // bottom lane
 		}
 		
 			
 		if (debugMode == false)
 		{
 			server.command(this,"fa lm set laneamplitude " + feederNumber+laneNum0or1 + " " + 2); // 0-7lane# 1-8amplitude
 			debug("Feeder " + feederNumber + " reset lane amplitude.");
 			try {
 				animation.acquire();
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	/** Animation that makes the Feeder START feeding parts into the lane. */
 	private void DoStartFeeding(Part part) {
 		if (debugMode == false)
 		{
 			server.command(this,"fa lm cmd startfeeding " + feederNumber);
 			debug("Feeder " + feederNumber + " started feeding.");
 			try {
 				animation.acquire();
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 
 	/** Animation that makes the Feeder STOP feeding parts into the lane. */
 	private void DoStopFeeding() { 
 		if (debugMode == false) 
 		{
 			debug("stopped feeding.");
 			server.command(this,"fa lm cmd stopfeeding " + feederNumber);
 			try {
 				animation.acquire();
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 
 	/** Animation that clears all of the parts out of the Feeder. */
 	private void DoPurgeFeeder() {
 		if (debugMode == false)
 		{
 			debug("purging feeder.");
 			server.command(this,"fa multi cmd purgefeeder " + feederNumber); /** TODO: MULTI **/
 			try {
 				animation.acquire();
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 
 	/** Animation that switches the lane that the Feeder is feeding to. */
 	private void DoSwitchLane() {
 		if (debugMode == false)
 		{
 			debug("switching lane");
 			server.command(this,"fa lm cmd switchlane " + feederNumber);
 			try {
 				animation.acquire();
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private void DoDumpTopNest()
 	{
 		if (debugMode == false)
 		{
 			debug("dumping top nest");
 			server.command(this,"fa lm cmd dumptopnest " + feederNumber);
 			try {
 				animation.acquire();
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private void DoDumpBottomNest()
 	{
 		if (debugMode == false)
 		{
 			debug("dumping top nest");
 			server.command(this,"fa lm cmd dumpbottomnest " + feederNumber);
 			try {
 				animation.acquire();
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	/** Animation that makes the Feeder continue feeding after a jam or error condition. (v.2) */
 	private void DoContinueFeeding(Part part) {
 		debug("continued feeding.");
 		if (debugMode == false)
 		{
 			// worry about this later (v.2)
 		}
 	}
 
 
 	/** Animation that clears all of the parts out of the Feeder's top lane. */
 	private void DoPurgeTopLane() {
 		if (debugMode == false)
 		{
 			debug("purging lane top");
 			server.command(this,"fa lm cmd purgetoplane " + feederNumber); /** TODO: MULTI **/
 			try {
 				animation.acquire();
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 
 	/** Animation that clears all of the parts out of the Feeder's bottom lane. */
 	private void DoPurgeBottomLane() {
 		if (debugMode == false)
 		{
 			debug("purging lane bottom");
 			server.command(this,"fa lm cmd purgebottomlane " + feederNumber); /** TODO: MULTI **/
 			try {
 				animation.acquire();
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 
 	/** Animation that simulates a jammed lane. **/
 	private void DoJamLane(int laneNumber)
 	{
 		if (debugMode == false)
 		{
 			if (laneNumber == 0)
 			{
 				debug("jamming top lane");
 				server.command(this,"fa lm cmd jamtoplane " + feederNumber); /** TODO: MULTI **/
 				try {
 					animation.acquire();
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 			}
 			else if (laneNumber == 1)
 			{
 				debug("jamming bottom lane");
 				server.command(this,"fa lm cmd jambottomlane " + feederNumber); /** TODO: MULTI **/
 				try {
 					animation.acquire();
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 
 	/** Animation that unjams a jammed lane. **/
 	private void DoUnjamLane(MyLane la)
 	{
 		if (debugMode == false)
 		{
 			if (topLane == la)
 			{
 				debug("unjamming top lane");
 				server.command(this,"fa lm cmd unjamtoplane " + feederNumber); /** TODO: MULTI **/
 				try {
 					animation.acquire();
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 			}
 			else if (bottomLane == la)
 			{
 				debug("unjamming bottom lane");
 				server.command(this,"fa lm cmd unjambottomlane " + feederNumber); /** TODO: MULTI **/
 				try {
 					animation.acquire();
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 
 
 
 	/** OTHER **/
 
 	/** Returns true if the Feeder has a bin underneath it. 
 	 *  Will be pretty much all of the time except for start-up. */
 	public boolean getFeederHasABinUnderneath() 
 	{
 		return feederHasABinUnderneath;
 	}
 
 	/** Method that returns the number of this feeder (0-3). */
 	public int getFeederNumber()
 	{
 		return this.feederNumber;
 	}
 	
 	/** Method called from the GUI setting the speed of the Diverter switching algorithm. **/
 	public void setDiverterSpeed(int num)
 	{
 		if (num == 0)
 		{
 			this.hasASlowDiverter = false;
 			diverterSpeedWasReset = true;
 		}
 		else
 		{
 			this.hasASlowDiverter = true;
 		}
 		
 		
 		this.diverterSpeed = (num + kOK_TO_PURGE_TIME); // the delay only takes effect after the ok_to_purge_time timer
 	}
 
 
 	/** Figures out what the target lane of the part request is and returns it. */	
 	public MyLane targetLaneOfPartRequest(MyPartRequest partRequested)
 	{
 		MyLane targetLane = null;
 		if (partRequested.lane == topLane.lane)
 			targetLane = topLane;
 		else if (partRequested.lane == bottomLane.lane)
 			targetLane = bottomLane;
 
 		return targetLane;
 	}
 
 	
 	protected void debug(String msg) {
 		if(true) {
 			print(msg, null);
 		}
 	}
 
 	
 
 }
 
 
 
 
 
 
 
