 package agent;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.TreeMap;
 
 import DeviceGraphics.DeviceGraphics;
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
 	private boolean start;
 	private boolean incomingKit;
 	private final Timer timer;
 
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
 			this.KS = KitStatus.Received;
 		}
 	}
 
 	public enum KitStatus {
 		Received, PlacedOnStand, Assembled, MarkedForInspection, AwaitingInspection, Inspected, Shipped;
 	};
 
 	/**
 	 * Constructor for StandAgent class
 	 * @param name name of the stand
 	 */
 	public StandAgent(String name) {
 		super();
 
 		this.name = name;
 		numKitsToMake = 0;
 		start = false;
 		incomingKit = false;
 		kitsOnStand.add(null);
 		kitsOnStand.add(null);
 		kitsOnStand.add(null);
 
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
 		start = true;
 		stateChanged();
 	}
 
 	@Override
 	public void msgHereIsKit(Kit k, int destination) {
 		print("Received msgHereIsKit with destination " + destination);
 		incomingKit = false;
 		myKits.put(new MyKit(k), destination);
 		stateChanged();
 	}
 
 	@Override
 	public void msgKitAssembled(Kit k) {
 		print("Received msgKitAssembled");
 		for (MyKit mk : myKits.keySet()) {
 			if (mk.kit == k) {
 				mk.KS = KitStatus.Assembled;
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
 				mk.KS = KitStatus.Shipped;
 				numKitsToMake--;
 				print(numKitsToMake + " kits left to make");
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
 
 		if (numKitsToMake > 0) {
 			// print("NumKitsToMake greater than 0");
 			synchronized (myKits) {
 				// Received a kit from kit robot
 				for (MyKit mk : myKits.keySet()) {
 					if (mk.KS == KitStatus.Received) {
 						placeKit(mk.kit);
 						return true;
 					}
 				}
 
 				// Kit robot shipped a kit
 				for (MyKit mk : myKits.keySet()) {
 					if (mk.KS == KitStatus.Shipped) {
 						kitsOnStand.set(0, null);
 						myKits.remove(mk);
 						return true;
 					}
 				}
 			}
 
 			synchronized (kitsOnStand) {
 				// Stand has an empty position (does not check the
 				// inspection
 				// area of the stand)
				int loc;
				// if (numKitsToMake == 1 && !incomingKit) { // Special case
 				if (standPositions.get(1) == false
 						&& standPositions.get(2) == false) {
 					print("Neither position full");
					requestKit(loc = 1);
					print("I'm requesting a new kit at position 1");
					incomingKit = true;
					return true;
 				} else if (standPositions.get(1) == false
						|| standPositions.get(2) == false && numKitsToMake > 1) {
 					print("One position full");
					requestKit(loc = standPositions.get(1) == false ? 1 : 2);
					print("I'm requesting a new kit at position " + loc);
					incomingKit = true;
					return true;
 				}
				// }
 
 			}
 
 			synchronized (myKits) {
 				// Kit needs to be inspected
 				for (MyKit mk : myKits.keySet()) {
 					if (mk.KS == KitStatus.Assembled) {
 						requestInspection(mk);
 						return true;
 					}
 				}
 			}
 		} else if (start) {
 			finalizeOrder();
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
 	 * Requests a kit from kit robot at the specified location.
 	 * @param index the empty location on the stand where the new kit will be
 	 * placed
 	 */
 	private void requestKit(int index) {
 		kitrobot.msgNeedKit(index);
 		standPositions.put(index, true);
 		stateChanged();
 	}
 
 	/**
 	 * Places a kit into the list of kits on the stand
 	 * @param k the kit being placed
 	 */
 	private void placeKit(final Kit k) {
 		int spot = 5;
 		// incomingKits--;
 
 		for (MyKit mk : myKits.keySet()) {
 			if (mk.kit == k) {
 				mk.KS = KitStatus.PlacedOnStand;
 				spot = myKits.get(mk);
 				print("Found a spot at " + spot);
 				break;
 			}
 		}
 
 		kitsOnStand.set(spot, k);
 		// partsrobot.msgUseThisKit(k); // THIS DOESN'T WORK YET
 
 		// For testing, assume parts robot finishes after 1s
 		timer.schedule(new TimerTask() {
 
 			@Override
 			public void run() {
 				print("Faking partsrobot finishing kit assembly");
 				msgKitAssembled(k);
 			}
 		}, 1000);
 		stateChanged();
 	}
 
 	/**
 	 * Requests inspection of an assembled kit.
 	 * @param k the kit to be inspected.
 	 */
 	private void requestInspection(MyKit mk) {
 		mk.KS = KitStatus.AwaitingInspection;
 		kitrobot.msgMoveKitToInspectionArea(mk.kit);
 		stateChanged();
 	}
 
 	/**
 	 * Updates the FCS when a batch of kits has been completed.
 	 */
 	private void finalizeOrder() {
 		// fcs.msgOrderFinished();
 		start = false;
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
 		stateChanged();
 	}
 
 	/**
 	 * GUI Hack to set the reference to the kitrobot.
 	 * @param kr the kitrobot
 	 */
 	public void setKitRobot(KitRobot kr) {
 		this.kitrobot = kr;
 		stateChanged();
 	}
 
 	/**
 	 * GUI Hack to set the reference to the FCS.
 	 * @param fcs the fcs
 	 */
 	public void setFCS(FCS fcs) {
 		this.fcs = fcs;
 		stateChanged();
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
 
 	public void setPartsrobot(PartsRobot partsrobot) {
 		this.partsrobot = partsrobot;
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
 
 	public boolean getIncomingKits() {
 		return incomingKit;
 	}
 
 	public void setIncomingKit(boolean incomingKit) {
 		this.incomingKit = incomingKit;
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
 
 	@Override
 	public void setGraphicalRepresentation(DeviceGraphics dg) {
 		// Currently has no graphical representation (changing in v1)
 	}
 }
