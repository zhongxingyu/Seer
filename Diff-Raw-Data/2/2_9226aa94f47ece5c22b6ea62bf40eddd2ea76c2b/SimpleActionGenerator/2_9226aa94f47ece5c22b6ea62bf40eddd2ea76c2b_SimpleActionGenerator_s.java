 package rental.g3;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Stack;
 
 import rental.g3.Relocator;
 import rental.g3.Relocator.RelocatorStatus;
 import rental.sim.Drive;
 import rental.sim.Ride;
 import rental.sim.RGid;
 import rental.sim.Player.DriveRide;
 
 public class SimpleActionGenerator extends ActionGenerator {
 	public SimpleActionGenerator(Game game) { super(game); }
 
 	@Override
 	public DriveRide genDriveRide() {
 		List<Drive> drives = new ArrayList<Drive>();
 		List<Ride> rides = new ArrayList<Ride>();		
 		Drive drive;
 		Ride ride;	
 		for (int i = 0; i < game.nRelocator;  i++) {
 			switch (game.relocators[i].status) {
 			case ENROUTE:
 				drive = genEnrouteDrive(i);
 				ride = genEnrouteRide(i);
 				break;
 			case IDLING:
 				drive = genIdlingDrive(i);
 				ride = genIdlingRide(i);
 				break;
 			case WAITING:
 				drive = genWaitingDrive(i);
 				ride = genWaitingRide(i);
 				break;
 			case PASSENGER:
 				drive = genPassengerDrive(i);
 				ride = genPassengerRide(i);
 				break;
 			case PICKUP:
 				drive = genPickupDrive(i);
 				ride = genPickupRide(i);
 				break;
 			case DROPOFF:
 				drive = genDropoffDrive(i);
 				ride = genDropoffRide(i);
 				break;
 			default:
 				throw new RuntimeException("Unexpected relocator status:"  + i + ": " + game.relocators[i].status);
 			}
 			if (drive != null) 
 				drives.add(drive);
 			if (ride != null)
 				rides.add(ride);
 		}
 				
 		return new DriveRide(drives.toArray(new Drive[0]), rides.toArray(new Ride[0]));
 	}
 
 
 	// a simple version: deposit at once
 	private boolean depositOrNot(int nextLoc, Stack<Route> routes) {
 		if (routes.size() > 1)
 			return false;
 		
 		// if there is empty car there, directly deposit
 		if (nextLoc== routes.peek().dst /* && game.getEmptyCars(dstLoc).size() > 0 */)
 			return true;
 		return false;
 	}
 	
 	private Drive genEnrouteDrive(int rid) {	
 		Relocator r = game.relocators[rid];			
 		
 		// when we arrive at an destination: deposit car, pick up or drop off passengers
 		if (r.location == r.firstRoute().dst) {
 			r.popRoute();
 			// check passengers to drop-off
 			List<RGid> passengers = game.cars[r.cid].passengers;
 			List<RGid> toRemove = new ArrayList<RGid>();
 			for (RGid rgid : passengers) {
 				// FOR MONDAY, SKIP OTHER GROUP
 				if (rgid.gid != game.gid)
 					continue;
 				// if arrive at one of the dropoff nodes
 				if (r.location == game.cars[game.relocators[rgid.rid].firstRoute().cid].source) {
 					// passengers.remove(rgid);
 					toRemove.add(rgid);
 
 				}
 			}
 			passengers.removeAll(toRemove);
 			
 			// check passengers to pick up
 			for (int i = 0; i < game.nRelocator; i++) {
 				if (game.relocators[i].pickuper == rid && game.relocators[i].location == r.location) {
 					passengers.add(new RGid(i, game.gid));
 				}
 			}	
 			// if it is the final destination
 			// continue to relocate another car or stay waiting
 			if (r.getRoutes().size() == 0) {				
 				List<Car> emptyCars = game.getEmptyCars(r.location);
 				if (emptyCars.size() > 0) { // if there are empty cars
 					int carId = pickCar(emptyCars);
 					r.pushRoute(new Route(carId, game.cars[carId].destination));
 					r.cid = carId;
 					game.cars[carId].inuse = true;
 					game.cars[carId].driver = rid;
 				}				
 				else { // there is no available cars, start waiting
 					r.setNext(RelocatorStatus.WAITING, r.location);
 					return null;
 				}
 			}
 		}	
 				
 		// the car is still in use
 		// option 1: continue relocate this car
 		// option 2: reroute and pick up other relocators	
 		if (r.firstRoute().dst != r.location) {
 			// if the car is not full, try pick up					
 			if (game.cars[r.cid].getPassengers().size() < 3) { 
 				List<Pickup> picks = findPickups(rid);
 				int pickCount = Math.min(3- game.cars[r.cid].getPassengers().size(), picks.size());
 				for (int i = 0; i < pickCount; i++) {
 					if (picks.get(i).dropLoc != r.firstRoute().dst)
 						r.pushRoute(new Route(r.cid, picks.get(i).dropLoc));
 					if (r.location != picks.get(i).pickLoc)
 						r.pushRoute(new Route(r.cid, picks.get(i).pickLoc));
 					game.relocators[picks.get(i).rid].pushRoute(
 							new Route(picks.get(i).cid, game.cars[picks.get(i).cid].destination));
 					game.relocators[picks.get(i).rid].pickuper = rid;
 					game.relocators[picks.get(i).rid].cid = picks.get(i).cid;
 					game.cars[picks.get(i).cid].inuse = true;
 					game.cars[picks.get(i).cid].driver = picks.get(i).rid;
 				}
 			}
 			else { // if the car is full, do not reroute, just follow the route 	
 				// do nothing
 			}
 		}
 		
 		// the car is delivered but not deposited
 		// NOT FOR MONDAY
 		
 		// update information
 		int nextLoc = game.graph.nextMove(r.location, r.firstRoute().dst);
 		boolean toDeposit = depositOrNot(nextLoc, r.getRoutes());
 		r.setNext(RelocatorStatus.ENROUTE, nextLoc);
 		game.cars[r.cid].setNext(nextLoc, toDeposit);
 
 		// update information for passengers
 		List<RGid> passengers = game.cars[r.cid].passengers;
 		for (RGid rgid : passengers) {
 			// FOR MONDAY, SKIP OTHER GROUP
 			if (rgid.gid != game.gid)
 				continue;
 			game.relocators[rgid.rid].setNext(RelocatorStatus.PASSENGER, nextLoc);			
 		}
 			
 		// generate a drive
 		Drive drive = new Drive(rid, r.cid, toDeposit, game.cars[r.cid].passengers.toArray(new RGid[0]), 
 				game.graph.getNodeName(nextLoc));
 		return drive;
 	}
 	
 	private int pickCar(List<Car> emptyCars) {
 		int mindist = Integer.MAX_VALUE;
 		int topick = -1;
 		int dist;
 		for (Car car : emptyCars) {
 			if ((dist = game.nndist(car.source, car.destination)) < mindist) {
 				mindist = dist;
 				topick = car.getCid();
 			}
 		}
 		return topick;
 	}
 	
 	private List<Pickup> findPickups(int rid) {
 		// if there is a waiting relocator within distance d
 		// && is not scheduled a pickup
 		// && there is an available car within distance d from the pickup location
 		
 		List<Pickup> pickups = new ArrayList<Pickup>();
 		for (int oid = 0; oid < game.nRelocator; oid++) {
 			// skip myself, skip relocator who is scheduled for a pick up
 			if (oid == rid || game.relocators[oid].pickuper > 0) 
 				continue;
 			// skip non-waiting relocator
 			// NOTE: Need Revision
 			if (game.relocators[oid].nextStatus != Relocator.RelocatorStatus.WAITING)
 				continue;
 			// skip too-far relocator
 			if (game.rrdist(rid, oid) > Pickup.MaxPickupDist)
 				continue;
 			// find nearby empty cars
 			for (int cid = 0; cid < game.nCar; cid++) {
 				if (game.cars[cid].inuse == true || game.cars[cid].isDeposit || 
 					game.rndist(rid, game.cars[cid].location) > Pickup.MaxPickupDist)
 					continue;
 				pickups.add(new Pickup(oid, cid, game.relocators[oid].location, game.cars[cid].source));				
 			}
 		}				
 		return pickups;
 	}	
 	private Drive genIdlingDrive(int i) {		
 		return null;
 	}
 	private Drive genPickupDrive(int rid) {
 		return null;
 	}
 	private Drive genDropoffDrive(int rid) {	
 		return null;
 	}
 	
 	// A waiting relocator will not generate new drive
 	private Drive genWaitingDrive(int rid) {
 		Relocator r = game.relocators[rid];
 		if (r.pickuper < 0) { // there is no scheduled pick up, do nothing
 			r.setNext(RelocatorStatus.WAITING, r.location);
 		}
 		else if (r.location != game.relocators[r.pickuper].location){			
 			r.setNext(RelocatorStatus.WAITING, r.location);
 		}
 		else {
 			r.setNext(RelocatorStatus.PASSENGER, game.relocators[r.pickuper].nextLoc);
 			// PROBLEMATIC, the next location may not be set yet
 		}
 		return null;
 	}
 
 	// if a passenger is dropped off
 	// generate a move
 	private Drive genPassengerDrive(int rid) {
 		Relocator r = game.relocators[rid];
 		// if arrive at dropped off node
 		// generate a drive
 		
 		int dropOffNode = game.cars[r.firstRoute().cid].source;
 		if (r.location == dropOffNode) {
 			// update information
 			int nextLoc = game.graph.nextMove(r.location, r.firstRoute().dst);
 			boolean toDeposit = depositOrNot(nextLoc, r.getRoutes());
 			r.setNext(RelocatorStatus.ENROUTE, nextLoc);
 			game.cars[r.cid].setNext(nextLoc, toDeposit);
 			r.pickuper = -1; // reset pickuper
 			
 			// generate a drive
 			
 			if (r.location == nextLoc) {
 				System.out.println("BUG FROM PASSENGER!");
 			}
 			
 			Drive drive = new Drive(rid, r.cid, toDeposit, new RGid[0], 
 					game.graph.getNodeName(nextLoc));
 			return drive;
 		}
 		else {		
 			// otherwise do nothing
 			// the driver will set the status for passengers
 			r.setNext(RelocatorStatus.PASSENGER, game.relocators[r.pickuper].nextLoc);
 			return null;
 		}
 	}
 	
 	
 	
 	
 	// For monday, we do not generate rides
 	private Ride genEnrouteRide(int rid) {
 		return null;
 	}
 	private Ride genPassengerRide(int rid) {
 		return null;
 	}
 	private Ride genDropoffRide(int rid) {
 		return null;
 	}
 	private Ride genPickupRide(int rid) {
 		return null;
 	}
 	private Ride genWaitingRide(int rid) {
 		return null;
 	}
 	private Ride genIdlingRide(int rid) {
 		return null;
 	}
 }
