 package agent;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Timer;
 import java.util.TreeMap;
 
 import DeviceGraphics.DeviceGraphics;
 import DeviceGraphics.KitGraphics;
 import agent.data.Kit;
 import agent.interfaces.FCS;
 import agent.interfaces.KitRobot;
 import agent.interfaces.PartsRobot;
 import agent.interfaces.Stand;
 
 /**
  * Stand manages the kits that are placed on the kitting stand. Interacts with
  * the Factory Control System (FCS), the Parts Robot and the Kit Robot.
  * @author Daniel Paje
  */
 public class StandAgent extends Agent implements Stand {
 
 	// References to other agents
 	private KitRobot kitrobot;
 	private PartsRobot partsrobot;
 	private FCS fcs;
 
 	private final String name;
 
 	// Kits received but not yet placed. Should only have <= 2 keys at any given
 	// time.
 	private final Map<MyKit, Integer> myKits = Collections
 			.synchronizedMap(new HashMap<MyKit, Integer>());
 
 	private int numKitsToMake;
 	private int numKitsMade;
 	private boolean start;
 	private final Timer timer;
 	private StandStatus status;
 
 	private enum StandStatus {
 		IDLE, NEED_TO_INITIALIZE, KIT_REQUESTED, DONE
 	};
 
 	// Tracks stand positions and whether or not they are open (false indicates
 	// 'not occupied')
 	// Note that this is the inverse of what the KitRobot uses in its
 	// standPositions
 	private final Map<Integer, Boolean> standPositions = Collections
 			.synchronizedMap(new TreeMap<Integer, Boolean>());
 	private final List<Kit> kitsOnStand = Collections
 			.synchronizedList(new ArrayList<Kit>());
 
 	/**
 	 * Inner class encapsulates kit and adds states relevant to the stand
 	 * @author dpaje
 	 */
 	private class MyKit {
 		public Kit kit;
 		public KitStatus KS;
 
 		public MyKit(Kit k) {
 			this.kit = k;
 			this.KS = KitStatus.RECEIVED;
 		}
 	}
 
 	public enum KitStatus {
 		RECEIVED, PLACED_ON_STAND, ASSEMBLED, MARKED_FOR_INSPECTION, AWAITING_INSPECTION, INSPECTED, SHIPPED;
 	};
 
 	/**
 	 * Constructor for StandAgent class
 	 * @param name name of the stand
 	 */
 	public StandAgent(String name) {
 		super();
 
 		this.name = name;
 		numKitsToMake = 0;
 		numKitsMade = 0;
 		start = false;
 		status = StandStatus.IDLE;
 
 		kitsOnStand.add(null);
 		kitsOnStand.add(null);
 		kitsOnStand.add(null);
 
 		standPositions.put(0, false);
 		standPositions.put(1, false);
 		standPositions.put(2, false);
 		timer = new Timer();
 	}
 
 	/*
 	 * Messages
 	 */
 
 	@Override
 	public void msgMakeKits(int numKits) {
 		print("Received msgMakeKits");
 		numKitsToMake = numKits;
 		numKitsMade = 0;
 		status = StandStatus.NEED_TO_INITIALIZE;
 		start = true;
 		stateChanged();
 	}
 
 	@Override
 	public void msgHereIsKit(Kit k, int destination) {
 		print("Received msgHereIsKit with destination " + destination);
 		status = StandStatus.IDLE;
 		myKits.put(new MyKit(k), destination);
 		stateChanged();
 	}
 
 	@Override
 	public void msgKitAssembled(Kit k) {
 		print("Received msgKitAssembled");
 		for (MyKit mk : myKits.keySet()) {
 			if (mk.kit == k) {
 				mk.KS = KitStatus.ASSEMBLED;
 				break;
 			}
 		}
 		stateChanged();
 	}
 
 	@Override
 	public void msgMovedToInspectionArea(Kit k, int oldLocation) {
 		print("Received msgMovedToInspectionArea");
 		standPositions.put(oldLocation, false);
 		kitsOnStand.set(0, k);
 		stateChanged();
 	}
 
 	@Override
 	public void msgShippedKit() {
 		print("Received msgShippedKit");
 		for (MyKit mk : myKits.keySet()) {
 			if (mk.kit == kitsOnStand.get(0)) {
 				mk.KS = KitStatus.SHIPPED;
 				numKitsMade++;
 				print(numKitsToMake - numKitsMade + " kits left to make");
 				break;
 			}
 		}
 		stateChanged();
 	}
 
 	/*
 	 * Scheduler
 	 * @see agent.Agent#pickAndExecuteAnAction()
 	 */
 	@Override
 	public boolean pickAndExecuteAnAction() {
 
 		if (status == StandStatus.NEED_TO_INITIALIZE) {
 			initializeKitRobot();
 			return true;
 		}
 
 		if (start) {
 			if (numKitsMade == numKitsToMake) {
 				finalizeOrder();
 				return true;
 			}
 
 			// print("NumKitsToMake greater than 0");
 			synchronized (myKits) {
 
 				for (MyKit mk : myKits.keySet()) {
 					// Received a kit from kit robot
 					if (mk.KS == KitStatus.RECEIVED) {
 						mk.KS = KitStatus.PLACED_ON_STAND;
 						placeKit(mk);
 						return true;
 					}
 					// Kit robot shipped a kit
 					else if (mk.KS == KitStatus.SHIPPED) {
 						kitsOnStand.set(0, null);
 						print("Removing " + mk.kit.toString() + " (shipped)");
 						myKits.remove(mk);
 						return true;
 					}
 
 					// Kit needs to be inspected
 					else if (mk.KS == KitStatus.ASSEMBLED) {
 						mk.KS = KitStatus.AWAITING_INSPECTION;
 						requestInspection(mk);
 						return true;
 					}
 				}
 			}
 
 			// Attempt to request a new kit if necessary
 			int loc = 0;
 			int count = 0;
 			// if (!kitRequested) {
			for (int i = 1; i < 3; i++) {
 				if (!standPositions.get(i)) {
 					count++;
 				}
 			}
 			if (numKitsToMake > 0 && numKitsToMake > numKitsMade && count > 0) {
 				print("NumKits to make greater than numKitsMade. Stand positions empty count: "
 						+ count);
 				if (!standPositions.get(1) && !standPositions.get(2)) {
 					print("Neither position full");
 					status = StandStatus.KIT_REQUESTED;
 					requestKit(loc = 1);
 					print("I'm requesting a new kit at position 1");
 					return true;
				} else {
 					print("One position full, but need to make more than 1 kit.");
 					status = StandStatus.KIT_REQUESTED;
 					requestKit(loc = standPositions.get(1) == false ? 1 : 2);
 					print("I'm requesting a new kit at position " + loc);
 					return true;
 				}
 			}
 		}
 
 		/*
 		 * Tried all rules and found no actions to fire. Return false to the
 		 * main loop of abstract base class Agent and wait.
 		 */
 		return false;
 	}
 
 	/*
 	 * Actions
 	 */
 
 	/**
 	 * Tells the kitrobot how many kits it should expect to make.
 	 */
 	private void initializeKitRobot() {
 		print("Initializing KitRobot.");
 		status = StandStatus.IDLE;
 		kitrobot.msgNeedThisManyKits(numKitsToMake);
 		stateChanged();
 	}
 
 	/**
 	 * Requests a kit from kit robot at the specified location.
 	 * @param index the empty location on the stand where the new kit will be
 	 * placed
 	 */
 	private void requestKit(int index) {
 		status = StandStatus.IDLE;
 		standPositions.put(index, true);
 		kitrobot.msgNeedKit(index);
 		stateChanged();
 	}
 
 	/**
 	 * Places a kit into the list of kits on the stand
 	 * @param k the kit being placed
 	 */
 	private void placeKit(MyKit mk) {
 		synchronized (myKits) {
 			int spot = 5;
 			// kitRequesteds--;
 			spot = myKits.get(mk);
 			print("Found a spot at " + spot);
 
 			kitsOnStand.set(spot, mk.kit);
 			print("Kit ID is " + mk.kit.toString());
 			// print(kitsOnStand.size() + " kits on stand");
 			partsrobot.msgUseThisKit(mk.kit); // THIS DOESN'T WORK YET
 			/*
 			 * // For testing, assume parts robot finishes after 1s
 			 * timer.schedule(new TimerTask() {
 			 * @Override public void run() {
 			 * print("Faking partsrobot finishing kit assembly");
 			 * msgKitAssembled(k); } }, 100);
 			 */
 			stateChanged();
 		}
 	}
 
 	/**
 	 * Requests inspection of an assembled kit.
 	 * @param k the kit to be inspected.
 	 */
 	private void requestInspection(MyKit mk) {
 		kitrobot.msgMoveKitToInspectionArea(mk.kit);
 		stateChanged();
 	}
 
 	/**
 	 * Updates the FCS when a batch of kits has been completed.
 	 */
 	private void finalizeOrder() {
 		start = false;
 		status = StandStatus.DONE;
 		fcs.msgOrderFinished();
 		System.out.println("====================");
 		print("I FINISHED HURRAY");
 		System.out.println("====================");
 		// No need to call stateChanged() here as presumably the kitting cell is
 		// idle (i.e., no queued orders)
 	}
 
 	/**
 	 * GUI Hack to set the reference to the partsrobot.
 	 * @param pr the partsrobot
 	 */
 	public void setPartsRobot(PartsRobot pr) {
 		this.partsrobot = pr;
 	}
 
 	/**
 	 * GUI Hack to set the reference to the kitrobot.
 	 * @param kr the kitrobot
 	 */
 	public void setKitRobot(KitRobot kr) {
 		this.kitrobot = kr;
 	}
 
 	/**
 	 * GUI Hack to set the reference to the FCS.
 	 * @param fcs the fcs
 	 */
 	public void setFCS(FCS fcs) {
 		this.fcs = fcs;
 	}
 
 	@Override
 	public String getName() {
 		return name;
 	}
 
 	public KitRobot getKitrobot() {
 		return kitrobot;
 	}
 
 	public void setKitrobot(KitRobot kitrobot) {
 		this.kitrobot = kitrobot;
 	}
 
 	public PartsRobot getPartsrobot() {
 		return partsrobot;
 	}
 
 	public FCS getFcs() {
 		return fcs;
 	}
 
 	public void setFcs(FCS fcs) {
 		this.fcs = fcs;
 	}
 
 	public int getNumKitsToMake() {
 		return numKitsToMake;
 	}
 
 	public void setNumKitsToMake(int numKitsToMake) {
 		this.numKitsToMake = numKitsToMake;
 	}
 
 	public boolean isStart() {
 		return start;
 	}
 
 	public void setStart(boolean start) {
 		this.start = start;
 	}
 
 	public Map<MyKit, Integer> getMyKits() {
 		return myKits;
 	}
 
 	public Timer getTimer() {
 		return timer;
 	}
 
 	public Map<Integer, Boolean> getStandPositions() {
 		return standPositions;
 	}
 
 	public List<Kit> getKitsOnStand() {
 		return kitsOnStand;
 	}
 
 	// Faking kit completion, used by KitRobotManager
 	public void fakeKitCompletion(KitGraphics kg) {
 		synchronized (myKits) {
 			for (MyKit mk : myKits.keySet()) {
 				if (mk.kit.kitGraphics == kg) {
 					print("Faking kit completion for kit " + mk.kit.toString());
 					msgKitAssembled(mk.kit);
 				}
 			}
 		}
 	}
 
 	@Override
 	public void setGraphicalRepresentation(DeviceGraphics dg) {
 		// Currently has no graphical representation (changing in v1)
 	}
 }
