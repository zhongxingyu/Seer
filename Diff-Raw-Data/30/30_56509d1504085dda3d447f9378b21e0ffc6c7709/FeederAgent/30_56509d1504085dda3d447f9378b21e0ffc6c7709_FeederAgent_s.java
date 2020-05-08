 package factory;
 
 import java.util.ArrayList;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.Semaphore;
 
 import agent.Agent;
 import factory.graphics.GraphicLaneManagerClient;
 import factory.graphics.GraphicLaneMenuPanel;
 import factory.interfaces.Feeder;
 import factory.interfaces.Gantry;
 import factory.interfaces.Lane;
 import factory.interfaces.Nest;
 import factory.interfaces.Vision;
 import factory.masterControl.MasterControl;
 import factory.test.mock.EventLog;
 import factory.test.mock.LoggedEvent;
 
 
 
 public class FeederAgent extends Agent implements Feeder {
 
 	/** DATA **/
 	private static int kOK_TO_PURGE_TIME = 10;
 	public GraphicLaneMenuPanel glmp;
 	private String name;
 	public int feederSlot;
 	public ArrayList<MyPartRequest> requestedParts = new ArrayList<MyPartRequest>();   
 	public MyLane topLane;
 	public MyLane bottomLane;
 	public Vision vision;
 	public DiverterState diverter = DiverterState.FEEDING_BOTTOM;
 	public Gantry gantry;
 	public Part currentPart;
 	Bin dispenserBin; // need to use this
 	public FeederState state = FeederState.EMPTY;
 	public Timer okayToPurgeTimer = new Timer();
 	public Timer feederEmptyTimer = new Timer();
 	public Timer partResettleTimer = new Timer();
 	public boolean feederHasABinUnderneath = false; // no bin underneath the feeder initially
 	
 	public enum FeederState { EMPTY, WAITING_FOR_PARTS, CONTAINS_PARTS, OK_TO_PURGE, SHOULD_START_FEEDING, IS_FEEDING }
 	public enum DiverterState { FEEDING_TOP, FEEDING_BOTTOM }
 
 	public enum MyPartRequestState { NEEDED, ASKED_GANTRY, DELIVERED, PROCESSING }
 
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
 
 
 	public enum MyLaneState {EMPTY, PURGING, CONTAINS_PARTS, BAD_NEST, 
 		TOLD_NEST_TO_DUMP, NEST_SUCCESSFULLY_DUMPED}
 	public enum JamState {MIGHT_BE_JAMMED, TOLD_TO_INCREASE_AMPLITUDE, 
 		AMPLITUDE_WAS_INCREASED, NOT_JAMMED }
 
 	public class MyLane {
 
 		public Lane lane;
 		public MyLaneState state = MyLaneState.EMPTY;
 		public JamState jamState;
 		public Part part;
 		public boolean readyForPicture = false;
 
 		public MyLane(Lane lane){
 			this.state = MyLaneState.EMPTY;
 			this.lane = lane;
 			this.jamState = JamState.NOT_JAMMED;
 		}
 	}
 
 
 	public FeederAgent(String nameStr,int slot, Lane top, Lane bottom, Gantry g, MasterControl mc) {
 		super(mc);
 		this.topLane = new MyLane(top);
 		this.bottomLane = new MyLane(bottom);
 		this.gantry = g;
 		this.name = nameStr+slot;
 		this.feederSlot = slot;
 	}
 
 
 	/** MESSAGES **/
 
 	/**
 	 * Message from the server when the animation is done
 	 */
 	public void msgAnimationDone(){
 		debug("Received msgAnimationDone() from server");
 		animation.release();
 	}
 
 
 	public void msgEmptyNest(Nest n) {
 		debug("received msgEmptyNest()");
 		if (topLane.lane.getNest() == n) 
 		{
 			topLane.lane.msgIncreaseAmplitude();
 			topLane.jamState = JamState.MIGHT_BE_JAMMED;
 		}
 		else if (bottomLane.lane.getNest() == n) 
 		{
 			bottomLane.lane.msgIncreaseAmplitude();
 			bottomLane.jamState = JamState.MIGHT_BE_JAMMED;
 		}
 		stateChanged();
 	}
 
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
 
 	public void msgLaneNeedsPart(Part part, Lane lane) {
 		String laneStr = "Top Lane";
 		if (bottomLane.lane == lane)
 			laneStr = "Bottom Lane";
 
 		debug("received msgLaneNeedsPart("+part.name + "," + laneStr + ")");
 		requestedParts.add(new MyPartRequest(part, lane));
 		stateChanged();
 	}
 
 	public void msgHereAreParts(Part part) {
 		debug("received msgHereAreParts("+part.name+")");
 		
 		feederHasABinUnderneath = true; // should never be false again
 		
 		for (MyPartRequest pr : requestedParts) {
 			if(pr.state == MyPartRequestState.ASKED_GANTRY && pr.pt == part) {
 				pr.state = MyPartRequestState.DELIVERED;
 			}
 		}
 		stateChanged();
 	}
 
 	public void msgBadNest(Nest n) {
 		debug("received msgBadNest()");
 		if (topLane.lane.getNest() == n) 
 			topLane.state = MyLaneState.BAD_NEST;
 		else if (bottomLane.lane.getNest() == n)
 			bottomLane.state = MyLaneState.BAD_NEST;
 		stateChanged();
 	}
 
 
 	/** SCHEDULER **/
 	@Override
 	public boolean pickAndExecuteAnAction() {
 
 		if (state == FeederState.EMPTY || state == FeederState.OK_TO_PURGE)
 		{
 			for (MyPartRequest p : requestedParts)
 			{
 				if (p.state == MyPartRequestState.NEEDED)
 				{
 					askGantryForPart(p);
 					return true;
 				}
 			}
 		}
 
		for (MyPartRequest p : requestedParts) 
		{
			if (p.state == MyPartRequestState.DELIVERED)
			{
				processFeederParts(p);
				return true;
			}
		}

 		if (state == FeederState.SHOULD_START_FEEDING) 
 		{ 
 			//			log.add(new LoggedEvent(
 			//					"Trying to decide which lane to feed parts to."));
 
 			// Which lane should it feed to?
 			if (topLane.part == currentPart)
 			{
 				if (topLane.state == MyLaneState.EMPTY || topLane.state == MyLaneState.CONTAINS_PARTS)
 				{
 					state = FeederState.IS_FEEDING; // we don't want to call this code an infinite number of times
 					StartFeeding();
 					return true;
 				}
 			}
 			else if (bottomLane.part == currentPart)
 			{
 				if (bottomLane.state == MyLaneState.EMPTY || bottomLane.state == MyLaneState.CONTAINS_PARTS)
 				{
 					state = FeederState.IS_FEEDING; // we don't want to call this code an infinite number of times
 					StartFeeding();
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
 		String laneStr = "Top Lane";
 		if (bottomLane == la)
 			laneStr = "Bottom Lane";
 
 		debug("Go dump nest of lane "+ laneStr + ".");
 		DoStopFeeding();
 		la.state = MyLaneState.TOLD_NEST_TO_DUMP;
 		la.lane.msgDumpNest();
 		stateChanged();
 
 	}
 
 	private void askGantryForPart(MyPartRequest partRequested) { 
 		debug("asking gantry for part " + partRequested.pt.name + ".");
 		if (this.currentPart == partRequested.pt && state != FeederState.EMPTY)
 		{
 			state = FeederState.IS_FEEDING;
 			partRequested.state = MyPartRequestState.DELIVERED;
 		}
 		if (purgeIfNecessary(partRequested) || this.state == FeederState.EMPTY )
 		{
 			state = FeederState.WAITING_FOR_PARTS;
 			partRequested.state = MyPartRequestState.ASKED_GANTRY;
 			gantry.msgFeederNeedsPart(partRequested.pt, this);
 		}
 
 		stateChanged();
 	}
 
 	private boolean purgeIfNecessary(MyPartRequest partRequested) { 
 		debug("purging if necessary.");
 		log.add(new LoggedEvent(
 				"Action purgeIfNecessary()"));
 
 		boolean purging = false;
 
 		// Check if Feeder needs to be purged
 		if (this.currentPart != partRequested.pt && state == FeederState.OK_TO_PURGE)
 		{
 			purging = true;
 			purgeFeeder();	
 			//currentPart = partRequested.pt; // We have purged the feeder, and for all intensive purposes, 
 			//						  	      // the feeder's current part is (about to be) the requested part
 		}
 
 		// Check if lane needs to be purged
 		// A BUG IS HERE
 		if (topLane.lane == partRequested.lane && state == FeederState.OK_TO_PURGE)
 		{
 			if (topLane.part != partRequested.pt)
 			{
 				purging = true;
 				purgeLane(topLane);
 			}
 		}
 
 		if (bottomLane.lane == partRequested.lane && state == FeederState.OK_TO_PURGE)
 		{
 			if (bottomLane.part != partRequested.pt)
 			{
 				purging = true;
 				purgeLane(bottomLane);
 			}
 		}
 
 
 		return purging;
 	}
 
 	private void purgeFeeder(){
 
 		DoStopFeeding();
 		DoPurgeFeeder();
 
 		currentPart = null;
 
 	}
 
 	private void purgeLane(MyLane myLane){
 
 		if (myLane == topLane)
 		{
 			DoPurgeTopLane();
 
 		}
 		else if (myLane == bottomLane)
 		{
 			DoPurgeBottomLane();
 		}
 
 		myLane.state = MyLaneState.PURGING; 
 		//myLane.state = MyLaneState.EMPTY; // for v.0, we will simply purge it "instantly"
 
 		myLane.lane.msgPurge();
 	}
 
 	// Old way was using a DELETED state, and was looking at all the requestedParts
 	/*private void processFeederParts(){
 		MyPartRequest rq = null;
 		for (MyPartRequest p : requestedParts) {
 			if( p.state == MyPartRequestState.DELETED){ 
 				rq = p;
 				break;
 			}
 		}
 
 		requestedParts.remove(rq);
 		currentPart = rq.pt;
 
 		if (bottomLane.lane == rq.lane){
 			bottomLane.part = rq.pt;
 		}
 		else {
 			topLane.part = rq.pt;
 		}
 		state = FeederState.SHOULD_START_FEEDING;
 	}
 	 */
 
 
 	private void processFeederParts(MyPartRequest mpr){
 		debug("processing feeder parts of type "+mpr.pt.name);
 		mpr.state = MyPartRequestState.PROCESSING;
 		log.add(new LoggedEvent("Action ProcessFeederParts()"));
 		requestedParts.remove(mpr);
 		currentPart = mpr.pt;
 
 		if (bottomLane.lane == mpr.lane){
 			bottomLane.part = mpr.pt;
 		}
 		else {
 			topLane.part = mpr.pt;
 		}
 
 		state = FeederState.SHOULD_START_FEEDING;
 
 		stateChanged();
 	}
 
 	private void StartFeeding(){
 		debug("action start feeding.");
 		final MyLane currentLane;
 		if(currentPart == topLane.part)
 			currentLane = topLane;
 		else 
 			currentLane = bottomLane;
 		// Switch Diverter
 		if (diverter == DiverterState.FEEDING_BOTTOM) {
 			if (currentLane == topLane)
 			{
 				diverter = DiverterState.FEEDING_TOP;
 				DoSwitchLane();   // Animation to switch lane
 			}
 		}
 		else if (currentLane == bottomLane && diverter == DiverterState.FEEDING_TOP) {
 			diverter = DiverterState.FEEDING_BOTTOM;
 			DoSwitchLane();   // Animation to switch lane
 		}
 
 
 		okayToPurgeTimer.schedule(new TimerTask(){
 			public void run() {
 				state = FeederState.OK_TO_PURGE;
 				DoStopFeeding(); // for v.0/v.1
 				debug("it is now okay to purge this feeder.");
 				stateChanged();
 			}
 		},(long) kOK_TO_PURGE_TIME * 1000); // okay to purge after this many seconds
 
 		feederEmptyTimer.schedule(new TimerTask(){
 			public void run(){		    
 				partResettleTimer.schedule(new TimerTask() {
 					public void run() {
 						debug("A lane is ready for a picture.");
 						currentLane.readyForPicture = true;
 
 						if (bottomLane.readyForPicture == true && topLane.readyForPicture == true)
 						{
 							debug("both of my nests are ready for a picture.");
 							vision.msgMyNestsReadyForPicture(topLane.lane.getNest(), bottomLane.lane.getNest(), this);
 							topLane.readyForPicture = false;
 							bottomLane.readyForPicture = false;
 						}
 						stateChanged();
 					}
 				}, 3000); // 3 seconds to resettle in the nest
 			}
 		}, (long) kOK_TO_PURGE_TIME * 1000); // time it takes the part to move down the lane and fill a nest 
 
 		//	Timer.new(30000, { state = FeederState.OK_TO_PURGE; });
 		//		Timer.new(currentPart.averageDelayTime,{vision.msgMyNestsReadyForPicture(topLane.lane.getNest(), bottomLane.lane.getNest(), this) });
 		// need a timer so that we don't immediately purge the new ones
 
 		DoStartFeeding(currentPart);
 		stateChanged();
 	}
 
 	/** This method returns the name of the FeederAgent **/
 	public String getName() {
 		return name;
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
 
 	/** ANIMATIONS 
 	 * @throws InterruptedException **/
 	private void DoStartFeeding(Part part) {
 //		server.command("fa fpm cmd startFeeding " + feederSlot + " endcmd");
 		debug("Feeder " + feederSlot + " started feeding.");
 		//		try {
 		//			animation.acquire();
 		//		} catch (InterruptedException e) {
 		//			// TODO Auto-generated catch block
 		//			e.printStackTrace();
 		//		}
 	}
 
 	private void DoStopFeeding() { 
 //		server.command("fa fpm cmd stopFeeding " + feederSlot + " endcmd");
 		debug("stopped feeding.");
 		//		try {
 		//		animation.acquire();
 		//	} catch (InterruptedException e) {
 		//		// TODO Auto-generated catch block
 		//		e.printStackTrace();
 		//	}
 	}
 
 	private void DoPurgeFeeder() {
 		log.add(new LoggedEvent(
 				"Animation DoPurgeFeeder()"));
 
 //		server.command("fa fpm cmd purgeFeeder " + feederSlot + " endcmd");
 		debug("purging feeder.");
 		//		try {
 		//		animation.acquire();
 		//	} catch (InterruptedException e) {
 		//		// TODO Auto-generated catch block
 		//		e.printStackTrace();
 		//	}
 	}
 
 	private void DoSwitchLane() {
 		log.add(new LoggedEvent(
 				"Animation DoSwitchLane()"));
 
 //		server.command("fa fpm cmd switchLane " + feederSlot + " endcmd");
 		debug("switching lane");
 		//		try {
 		//		animation.acquire();
 		//	} catch (InterruptedException e) {
 		//		// TODO Auto-generated catch block
 		//		e.printStackTrace();
 		//	}
 	}
 
 	private void DoContinueFeeding(Part part) {
 		debug("continued feeding.");
 		// worry about this later (v.2)
 	}
 
 	private void DoPurgeTopLane() {
 		log.add(new LoggedEvent(
 				"Animation DoPurgeTopLane()"));
 
 //		server.command("fa fpm cmd purgeTopLane " + feederSlot + " endcmd");
 		debug("purging top lane");
 		//		try {
 		//		animation.acquire();
 		//	} catch (InterruptedException e) {
 		//		// TODO Auto-generated catch block
 		//		e.printStackTrace();
 		//	}
 	}
 
 	private void DoPurgeBottomLane() {
 		log.add(new LoggedEvent(
 				"Animation DoPurgeBottomLane()"));
 
 //		server.command("fa fpm cmd purgeBottomLane " + feederSlot + " endcmd");
 		debug("purging bottom lane");
 		//		try {
 		//		animation.acquire();
 		//	} catch (InterruptedException e) {
 		//		// TODO Auto-generated catch block
 		//		e.printStackTrace();
 		//	}
 	}
 
 	
 	/** OTHER **/
 	public boolean getFeederHasABinUnderneath() {
 		return feederHasABinUnderneath;
 	}
 
 }
 
 
 
 
 
 
 
