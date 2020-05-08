 package agent;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import DeviceGraphics.DeviceGraphics;
 import Utils.Constants;
 import agent.data.Bin;
 import agent.interfaces.Camera;
 import agent.interfaces.Conveyor;
 import agent.interfaces.FCS;
 import agent.interfaces.Gantry;
 import agent.interfaces.Nest;
 import agent.interfaces.PartsRobot;
 import agent.interfaces.Stand;
 import factory.Order;
 import factory.PartType;
 
 /**
  * Controls orders that go into the kitting cell.
  * 
  * @author Daniel Paje, Michael Gendotti
  */
 public class FCSAgent extends Agent implements FCS {
 
 	private Stand stand;
 	private PartsRobot partsRobot;
 	private Gantry gantry;
 	private ArrayList<Nest> nests;
 	private Conveyor conveyor;
 	private myState state;
 	private List<Order> orders = Collections
 			.synchronizedList(new ArrayList<Order>());
 	private int numOrdersFinished = 0;
 	private Camera camera;
 
 	private factory.FCS fcs;
 
 	private final String name;
 
 	private boolean binsSet;
 	private final ArrayList<PartType> binsToAdd;
 
 	public enum myState {
 		PENDING, STARTED, LOADED
 	};
 
 	public FCSAgent(String name) {
 		super();
 		this.name = name;
 		this.nests = new ArrayList<Nest>();
 		this.orders = new ArrayList<Order>();
 		binsSet = false;
 		binsToAdd = new ArrayList<PartType>();
 		state = myState.PENDING;
 	}
 
 	public FCSAgent() {
 		super();
 		this.name = "FCS Agent";
 		binsSet = false;
 		binsToAdd = new ArrayList<PartType>();
 		state = myState.PENDING;
 	}
 
 	@Override
 	public void msgAddKitsToQueue(Order o) {
 		print("Received new order");
 		orders.add(o);
 		if (fcs != null) {
 			fcs.updateQueue((ArrayList<Order>) orders);
 		}
 		if (state == myState.PENDING) {
 			state = myState.STARTED;
 		}
 		stateChanged();
 	}
 
 	public void msgSetPartsRobotDropChance(Float c) {
 		partsRobot.msgSetDropChance(c);
 		stateChanged();
 	}
 
 	@Override
 	public void msgStopMakingKit(Order o) {
 		print("Received msgStopMakingKit");
 		synchronized (orders) {
 			for (Order order : orders) {
 				if (order.equals(o)) {
 					o.cancel = true;
 					if (fcs != null) {
 						fcs.updateQueue((ArrayList<Order>) orders);
 					}
 				}
 			}
 		}
 
 		resetCell(o);
 		stateChanged();
 	}
 
 	@Override
 	public void msgStartProduction() {
 		print("Received msgStartProduction");
 		state = myState.STARTED;
 		stateChanged();
 	}
 
 	@Override
 	public void msgAddNewPartType(PartType part) {
 		print("Received msgAddNewPartType");
 		binsToAdd.add(part);
 		stateChanged();
 	}
 
 	@Override
 	public void msgShippedKit() {
 		fcs.shippedKit();
 	}
 
 	@Override
 	public void msgOrderFinished() {
 		print("Received msgOrderFinished");
 		numOrdersFinished++;
 		System.out.println("Order " + numOrdersFinished + " Done!!!!");
 		synchronized (orders) {
 			for (Order o : orders) {
 				if (o.state == Order.orderState.ORDERED) {
 					orders.remove(o);
 					if (fcs != null) {
 						fcs.updateQueue((ArrayList<Order>) orders);
 					}
 					resetCell(o);
 					break;
 				}
 			}
 		}
 		state = myState.STARTED;
 		stateChanged();
 	}
 
 	@Override
 	public void msgBreakLane(int laneNumber) {
 		((NestAgent) nests.get(laneNumber)).lane.msgBreakThis();
 	}
 
 	@Override
 	public boolean pickAndExecuteAnAction() {
 		// print("I'm scheduling stuff");
 		if (state == myState.STARTED) {
 			if (!binsSet && gantry != null) {
 				initializeBins();
 				return true;
 			}
 			if (binsToAdd.size() > 0 && gantry != null) {
 				addBin();
 				return true;
 			}
 			if (!orders.isEmpty()) {
 				print("Checking orders");
 				synchronized (orders) {
 					for (Order o : orders) {
 						if (o.cancel) {
 							cancelOrder(o);
 							return true;
 						}
 					}
 					for (Order o : orders) {
 						if (o.state == Order.orderState.PENDING) {
 							placeOrder(o);
 							return true;
 						}
 					}
 				}
 				print("Found no orders to process");
 			}
 		}
 		return false;
 	}
 
 	public void placeOrder(Order o) {
 		print("Placing Order");
 		o.state = Order.orderState.ORDERED;
 		state = myState.LOADED;
 		if (fcs != null) {
 			fcs.updateQueue((ArrayList<Order>) orders);
 		}
 
 		int k = 0;
 		// for (PartType type : o.kitConfig.getConfig().keySet()) {
 		// for (int i = 0; i < o.kitConfig.getConfig().get(type); i++) {
 		// ((NestAgent) nests.get(k)).stopThread();
 		// k++;
 		// }
 		// }
 		camera.msgResetSelf();
 		k = 0;
 		for (PartType type : o.kitConfig.getConfig().keySet()) {
 			for (int i = 0; i < o.kitConfig.getConfig().get(type); i++) {
 				nests.get(k).msgHereIsPartType(type);
 				// ((NestAgent) nests.get(k)).startThread();
 				k++;
 			}
 		}
 		partsRobot.msgHereIsKitConfiguration(o.kitConfig);
 		conveyor.msgHereIsKitConfiguration(o.kitConfig);
 		stand.msgMakeKits(o.numKits);
 
 		/*
 		 * for(PartType type:o.kitConfig.getConfig().keySet()) {
 		 * gantry.msgHereIsBinConfig(new Bin(o.parts.get(i),i+1)); }
 		 */
 		stateChanged();
 	}
 
 	public void cancelOrder(Order o) {
 		print("Cancelling order");
 		if (o.state == Order.orderState.ORDERED) {
 			// stand.msgStopMakingTheseKits(o.parts);
 			orders.remove(o);
 		} else {
 			orders.remove(o);
 		}
 		if (fcs != null) {
 			fcs.updateQueue((ArrayList<Order>) orders);
 		}
 
 		resetCell(o);
 		stateChanged();
 	}
 
 	public void resetCell(Order o) {
 		print("Resetting cell");
 		camera.msgResetSelf();
 		// print("NEST SIZE: " + nests.size());
 		for (int i = 0; i < 8; i++) {
 			nests.get(i).msgHereIsPartType(null);
 			nests.get(i).msgPurgeSelf();
 		}
 	}
 
 	public void initializeBins() {
 		print("Messaging gantry about default bins");
 		for (int i = 0; i < Constants.DEFAULT_PARTTYPES.size(); i++) {
 			gantry.msgHereIsBin(new Bin(Constants.DEFAULT_PARTTYPES.get(i), i));
 		}
 		binsSet = true;
 		stateChanged();
 	}
 
 	public void addBin() {
 		if(binsToAdd.size()>1) {
 			for (int i = binsToAdd.size() - 1; i >= 0; i--) {
 				gantry.msgHereIsBin(new Bin(binsToAdd.get(i),
 						Constants.DEFAULT_PARTTYPES.size() - i));
 				binsToAdd.remove(i);
 			}
 		} else {
			gantry.msgHereIsBin(new Bin(binsToAdd.get(0),Constants.DEFAULT_PARTTYPES.size()));
 			binsToAdd.remove(0);
 		}
 		stateChanged();
 	}
 
 	public void setStand(Stand stand) {
 		this.stand = stand;
 	}
 
 	public void setPartsRobot(PartsRobot partsRobot) {
 		this.partsRobot = partsRobot;
 	}
 
 	public void setGantry(Gantry gantry) {
 		this.gantry = gantry;
 	}
 
 	public void setConveyor(Conveyor conveyor) {
 		this.conveyor = conveyor;
 	}
 
 	public void setNest(Nest nest) {
 		this.nests.add(nest);
 	}
 
 	public void setNests(ArrayList<Nest> nests) {
 		this.nests = nests;
 	}
 
 	public Stand getStand() {
 		return stand;
 	}
 
 	public PartsRobot getPartsRobot() {
 		return partsRobot;
 	}
 
 	public Gantry getGantry() {
 		return gantry;
 	}
 
 	public Conveyor getConveyor() {
 		return conveyor;
 	}
 
 	public ArrayList<Nest> getNests() {
 		return nests;
 	}
 
 	@Override
 	public String getName() {
 		return name;
 	}
 
 	@Override
 	public void setGraphicalRepresentation(DeviceGraphics fcs) {
 		// not used, Use setFCS instead
 	}
 
 	public DeviceGraphics getGraphicalRepresentation() {
 		// return fcsGraphics;
 		return null;
 	}
 
 	public ArrayList<Order> getOrders() {
 		return (ArrayList<Order>) orders;
 	}
 
 	public void setFCS(factory.FCS fcs) {
 		this.fcs = fcs;
 	}
 
 	public void setCamera(Camera camera) {
 		this.camera = camera;
 	}
 
 }
