 package agent;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.concurrent.Semaphore;
 
 import DeviceGraphics.DeviceGraphics;
 import GraphicsInterfaces.ConveyorGraphics;
 import agent.data.Kit;
 import agent.interfaces.Conveyor;
 import agent.interfaces.KitRobot;
 import agent.test.mock.MockGraphics;
 import factory.KitConfig;
 
 /**
  * Conveyor brings empty kits into and takes completed (i.e. assembled and
  * inspected) kits out of the kitting cell. Interacts with the Factory Control
  * System (FCS) and the Kit Robot.
  * @author Daniel Paje
  */
 public class ConveyorAgent extends Agent implements Conveyor {
 
 	private final List<MyKit> kitsOnConveyor = Collections
 			.synchronizedList(new ArrayList<MyKit>());
 
	private final KitConfig kitConfig;
 
 	private MyKit incomingKit;
 	private MyKit outgoingKit;
 	private int numKitsToDeliver;
 
 	// Used to prevent animations from overlapping
 	Semaphore animation = new Semaphore(0, true);
 
 	// References to other agents
 	private KitRobot kitrobot;
 	private FCSAgent fcs;
 	private ConveyorGraphics conveyorGraphics;
 	private MockGraphics mockgraphics;
 
 	// Name of the conveyor
 	private final String name;
 
 	/**
 	 * Inner class encapsulates kit and adds states relevant to the conveyor
 	 * @author dpaje
 	 */
 	private class MyKit {
 		public Kit kit;
 		public KitStatus KS;
 
 		public MyKit(Kit k) {
 			this.kit = k;
 			this.KS = KitStatus.MovingIn;
 		}
 	}
 
 	public enum KitStatus {
 		MovingIn, ArrivedAtPickupLocation, AwaitingPickup, PickupRequested, PickedUp, AwaitingDelivery, MovingOut, Delivered
 	};
 
 	/**
 	 * Constructor for ConveyorAgent class
 	 * @param name name of the conveyor
 	 */
 	public ConveyorAgent(String name) {
 		super();
 
 		this.name = name;
 		this.numKitsToDeliver = 0;
 		kitConfig = null;
 	}
 
 	/*
 	 * Messages.
 	 */
 
 	@Override
 	public void msgNeedKit() {
 		print("Received msgNeedKit");
 		numKitsToDeliver++;
 		stateChanged();
 	}
 
 	@Override
 	public void msgGiveMeKit() {
 		print("Received msgGiveMeKit");
 		if (kitsOnConveyor.size() > 0) {
 			kitsOnConveyor.get(0).KS = KitStatus.PickupRequested;
 			print(kitsOnConveyor.get(0).toString() + " will be sent");
 		}
 		stateChanged();
 	}
 
 	@Override
 	public void msgTakeKitAway(Kit k) {
 		print("Received msgTakeKitAway");
 		MyKit mk = new MyKit(k);
 		outgoingKit = mk;
 		mk.KS = KitStatus.AwaitingDelivery;
 		kitsOnConveyor.add(mk);
 		stateChanged();
 	}
 
 	@Override
 	public void msgBringEmptyKitDone() {
 		print("Received msgBringEmptyKitDone from graphics");
 		animation.release();
 		incomingKit.KS = KitStatus.ArrivedAtPickupLocation;
 		stateChanged();
 	}
 
 	@Override
 	public void msgGiveKitToKitRobotDone() {
 		print("Received msgGiveKitToKitRobotDone from graphics");
 		animation.release();
 		stateChanged();
 	}
 
 	@Override
 	public void msgReceiveKitDone() {
 		print("Received msgReceiveKitDone from graphics");
 		kitsOnConveyor.remove(outgoingKit);
 		// animation.release();
 		stateChanged();
 	}
 
 	/*
 	 * Scheduler
 	 * @see agent.Agent#pickAndExecuteAnAction()
 	 */
 
 	@Override
 	public boolean pickAndExecuteAnAction() {
 
 		synchronized (kitsOnConveyor) {
 			// Send the kit if it has reached the "stop" position on the
 			// conveyor where the kit robot can pick it up and the kit robot can
 			// pick it up.
 			for (MyKit mk : kitsOnConveyor) {
 				if (mk.KS == KitStatus.PickupRequested) {
 					print("About to send kit");
 					sendKit(mk);
 					return true;
 				}
 			}
 
 			// Send the kit if it has reached the "stop" position on the
 			// conveyor where the kit robot can pick it up.
 			for (MyKit mk : kitsOnConveyor) {
 				if (mk.KS == KitStatus.ArrivedAtPickupLocation) {
 					mk.KS = KitStatus.AwaitingPickup;
 					kitrobot.msgKitReadyForPickup();
 					return true;
 				}
 			}
 
 			// Place kit onto conveyor and start moving it out of the cell if
 			// the kit robot has dropped off a completed kit
 			for (MyKit mk : kitsOnConveyor) {
 				if (mk.KS == KitStatus.AwaitingDelivery) {
 					deliverKit(mk);
 					return true;
 				}
 			}
 
 			// Place kit onto conveyor and start moving it into the cell if a
 			// new kit was requested by the kit robot
 			if (numKitsToDeliver > 0) {
 				prepareKit();
 				return true;
 			}
 
 		}
 
 		if (kitsOnConveyor.size() == 0) {
 			kitrobot.msgNoKitsLeftOnConveyor();
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
 	 * Generate a new kit to move into the kitting cell.
 	 */
 	private void prepareKit() {
 		print("Requesting new kit");
 		Kit k = new Kit(kitConfig);
 		incomingKit = new MyKit(k);
 		// print("Got a permit");
 		if (mockgraphics != null) {
 			mockgraphics.msgBringEmptyKit(k.kitGraphics);
 		}
 		if (conveyorGraphics != null) {
 			print("Asking conveyor graphics to animate a new kit");
 			if (k.kitGraphics == null) {
 				print("kitGraphics null");
 			}
 			conveyorGraphics.msgBringEmptyKit(k.kitGraphics);
 		}
 		try {
 			animation.acquire();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		print("Got permit");
 
 		kitsOnConveyor.add(incomingKit);
 		numKitsToDeliver--;
 		stateChanged();
 	}
 
 	/**
 	 * Send an empty kit to the kitrobot
 	 * @param k the kit being sent.
 	 */
 	private void sendKit(MyKit mk) {
 		print("Sending kit to kit robot");
 		mk.KS = KitStatus.PickedUp;
 		if (mockgraphics != null) {
 			mockgraphics.msgGiveKitToKitRobot(mk.kit.kitGraphics);
 		}
 		if (conveyorGraphics != null) {
 			conveyorGraphics.msgGiveKitToKitRobot(mk.kit.kitGraphics);
 		}
 		try {
 			animation.acquire();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		kitrobot.msgHereIsKit(mk.kit);
 
 		kitsOnConveyor.remove(mk);
 
 		stateChanged();
 	}
 
 	/**
 	 * Send a finished kit out of the cell.
 	 * @param k the kit being delivered.
 	 */
 	private void deliverKit(MyKit mk) {
 		/*
 		 * try { animation.acquire(); } catch (InterruptedException e) { // TODO
 		 * Auto-generated catch block e.printStackTrace(); }
 		 */
 		mk.KS = KitStatus.MovingOut;
 		if (mockgraphics != null) {
 			mockgraphics.msgReceiveKit(mk.kit.kitGraphics);
 		}
 		if (conveyorGraphics != null) {
 			conveyorGraphics.msgReceiveKit(mk.kit.kitGraphics);
 		}
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
 	 * @param fcs the FCS
 	 */
 	public void setFCS(FCSAgent fcs) {
 		this.fcs = fcs;
 		stateChanged();
 	}
 
 	/**
 	 * GUI Hack to set the reference to this class' gui component
 	 * @param gc the gui representation of conveyor
 	 */
 	@Override
 	public void setGraphicalRepresentation(DeviceGraphics gc) {
 		this.conveyorGraphics = (ConveyorGraphics) gc;
 		stateChanged();
 	}
 
 	public MockGraphics getMockgraphics() {
 		return mockgraphics;
 	}
 
 	public void setMockgraphics(MockGraphics mockgraphics) {
 		this.mockgraphics = mockgraphics;
 	}
 
 	@Override
 	public String getName() {
 		return name;
 	}
 
 	public MyKit getIncomingKit() {
 		return incomingKit;
 	}
 
 	public void setIncomingKit(MyKit incomingKit) {
 		this.incomingKit = incomingKit;
 	}
 
 	public int getNumKitsToDeliver() {
 		return numKitsToDeliver;
 	}
 
 	public void setNumKitsToDeliver(int numKitsToDeliver) {
 		this.numKitsToDeliver = numKitsToDeliver;
 	}
 
 	public Semaphore getAnimation() {
 		return animation;
 	}
 
 	public void setAnimation(Semaphore animation) {
 		this.animation = animation;
 	}
 
 	public KitRobot getKitrobot() {
 		return kitrobot;
 	}
 
 	public void setKitrobot(KitRobot kitrobot) {
 		this.kitrobot = kitrobot;
 	}
 
 	public FCSAgent getFcs() {
 		return fcs;
 	}
 
 	public void setFcs(FCSAgent fcs) {
 		this.fcs = fcs;
 	}
 
 	public ConveyorGraphics getConveyorGraphics() {
 		return conveyorGraphics;
 	}
 
 	public List<MyKit> getKitsOnConveyor() {
 		return kitsOnConveyor;
 	}
 
 	@Override
 	public void msgHereIsKitConfiguration(KitConfig config) {
 	}
 
 }
