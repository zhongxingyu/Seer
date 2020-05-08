 package rental.g3;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Stack;
 
 import rental.g3.Relocator;
 import rental.g3.DriveBuilder;
 import rental.sim.RGid;
 
 public class SimpleActionGenerator extends ActionGenerator {
 	public SimpleActionGenerator(Game game) { super(game); }
 	
 	private Stack<Relocator> drivingRelocators = new Stack<Relocator>();
 
 	@Override
 	public DriveBuilder[] genDriveRide() {
 		List<DriveBuilder> drives = new ArrayList<DriveBuilder>();
 		DriveBuilder drive;
 		
 		// Calculate pickups for all relocators.
 		assignPickups(false);
 			
 		// Three Passes
 		// Pass 1: first schedule those relocators that are going to drive in this round
 		Relocator r;
 		for (int i = 0; i < game.nRelocator;  i++) {
 			// skip scheduled relocators and those have don't have cars
 			r = game.relocators[i];
 			if (r.isScheduled() || !r.isDriving() || r.pickuper != null) 
 				continue;	
 			
 			drivingRelocators.push(r);
 		}
 		
 		while(drivingRelocators.size() > 0) {
 			r = drivingRelocators.pop();
 			RGid rgid = new RGid(r.rid, game.gid);
 			for(Car car : game.cars) {
 				if( car.passengers.contains(rgid)) {
 					game.log("Passenger: " + r.rid + " is now driving...");
 					car.passengers.remove(rgid);
 					
 					if(r.pickuper != null) {
 						r.pickuper.removeDropOffRoutes(r.rid);
 						r.pickuper.removePickup(r.rid);
 						r.pickuper = null;
 						game.log("... Removing as passenger from: " + car.driver.rid);
 					}
 				}
 			}
 			
 			drive = genDriverDrive(r.rid);	
 			
 			if(drive == null) {
 				game.log("Returned null drive.");
 			} else {
 				drives.add(drive);
 			}
 		}
 		
 		
 		
 		// Pass 2: schedule passengers
 		for (int i = 0; i < game.nRelocator; i++) {
 			if (game.relocators[i].isScheduled() || !game.relocators[i].hasCar())
 				continue;
 			drive = genPassengerDrive(i);
 			if (drive!=null)
 				drives.add(drive);
 		}
 			
 		// Pass 3: schedule other relocators
 		for (int i = 0; i < game.nRelocator; i++) {
 			if (game.relocators[i].isScheduled())
 				continue;		
 			drive = genOtherDrive(i);
 			if (drive != null)
 				drives.add(drive);
 		}
 		if(drives.size() == 0) {
 			throw new RuntimeException("No drive objects but game is not over.");
 		}
 		
 		return drives.toArray(new DriveBuilder[0]);
 	}
 
 	// a simple version: deposit at once
 	private boolean depositOrNot(int nextLoc, Stack<Route> routes) {
 		if (routes.size() > 1)
 			return false;
 			
 		game.log("last dst: " + routes.peek().dst);
 		// if there is empty car there, directly deposit
 		if (nextLoc== routes.peek().dst /* && game.getEmptyCars(dstLoc).size() > 0 */)
 			return true;
 		return false;
 	}
 	
 	private DriveBuilder genDriverDrive(int rid) {	
 		Relocator r = game.relocators[rid];	
 		assert(r.hasCar());
 		int nextLoc;
 		boolean toDeposit;
 		
 		List<RGid> passengers = r.car.passengers;
 		// drop off
 		List<RGid> dropoffs = new ArrayList<RGid>();
 		for (RGid passenger : passengers) {
 			if (passenger.gid == game.gid) {
 				Relocator otherR = game.relocators[passenger.rid];
 				if (otherR.isDriving()) { // if reach its destination
 					game.log("Driver: " + r.rid + " dropped off passenger: " + passenger.rid);
 
 					dropoffs.add(passenger);
 					game.relocators[passenger.rid].pickuper = null; // reset pickuper
 					r.removeDropOffRoutes(otherR.rid);
 					r.removePickup(otherR.rid);
 					game.log("Next destination: " + game.graph.getNodeName(r.firstRoute().dst));
 				}
 			}			
 		}
 		passengers.removeAll(dropoffs);		
 		
 		// pick up
 		// This goes after because sometimes we pickup in our immediate spot.
 		for (Relocator or : game.relocators) {
 			// If we arrive at same location as or and we're marked as pick up and
 			// or is not already our passenger.  pick his ass up.
 			if (	or.pickuper == r && 
 					or.getLocation() == r.getLocation() && 
 					!passengers.contains(new RGid(or.rid, game.gid))
 					) {
 				if(or.isDriving()) {
 					// this was a coincidence, the driver hasn't been scheduled yet but we still think
 					// he's a passenger since our locations and pickups match.
 					or.pickuper = null;
 					continue;
 				}
 
 				passengers.add(new RGid(or.rid, game.gid));
 				game.log("Driver: " + r.rid + " We've arrived to pickup:" + or.rid);
 				r.removePickupRoutes(or.rid);
 				r.removePickup(r.rid);
 				game.log("Next destination: " + game.graph.getNodeName(r.firstRoute().dst));
 			}
 		}
 
 		nextLoc = game.graph.nextMove(r.getLocation(), r.firstRoute().dst);
 		r.move(nextLoc);
 		r.car.move(nextLoc);
 
 		if(r.getLastLocation() == r.getLocation()) {		
 			game.log("Driver: " + r.rid + " was schedule for illegal move.");
 			game.log("Illegal moving car " + r.car.cid);
 			game.log("Driver: " + r.rid + " assigned chain car: " +r.getChainCar());
 		}
 
 		game.log("Driver: "+ r.rid + " going to " + game.graph.getNodeName(nextLoc));
 
 		toDeposit = depositOrNot(nextLoc, r.getRoutes());
 
 
 		if(toDeposit) {
 			// Determine if we're the last available car.
 			// This can happen if all cars are deposited at the same time.
 			boolean moreRelocators = false;
 			for(Relocator otherR : game.relocators) {
 				if(otherR != r && otherR.hasCar()) {
 					moreRelocators = true;
 					break;
 				}
 			}
 
 			List<Car> availCars = new ArrayList<Car>(Arrays.asList(game.cars));
 			for(int i = 0; i < availCars.size();) {
 				if(availCars.get(i).isDeposit()) {
 					availCars.remove(i);
 				} else {
 					i++;
 				}
 			}
 			
 			// Bug fixed by Jiacheng
 			// If there is another car at the depost location
 			// which is the chain car of this relocator
 			// it should deposit the car at once
 			if(!moreRelocators && !(availCars.size() <= 1) && r.hasChainCar() == false) {								
 				// This is the last relocator with a car so don't deposit.
 				game.log("Driver: " + r.rid + " is last driver so refusing to deposit.");
 				toDeposit = false;
 				
 				// Find out if we have a passenger:
 				if(r.car.passengers.size() < 1) {
 					// We have no passengers but there are still cars out.
 					game.log("Driver: " + r.rid + " expanding search radius.");
 					
 					// Assign pickups ignoring map limit.
 					assignPickups(true);
 					drivingRelocators.add(r);
 					r.setLocation(r.getLastLocation());
 					return null;
 				}
 			}
 
 
 			game.log("Driver: " + r.rid + " will deposit car.");
 			r.pickups.clear();
 			if(passengers.size() > 0) {
 				// this car gets deposited so our passenger sits in it
 				// and is no longer updated since we use the pickup relocator
 				// to see if he's a passenger (which by now has a new car).
 				// so we update him here.
 				for(RGid rgid : passengers) {
 					if(rgid.gid == game.gid) {
 						game.relocators[rgid.rid].pickuper = null;
 						game.relocators[rgid.rid].move(nextLoc);
 					}
 				}
 			}
 		}
 		
 		DriveBuilder drive = new DriveBuilder(rid, r.car.cid, toDeposit, passengers.toArray(new RGid[0]), game.graph.getNodeName(nextLoc));
 		
 		// if the car is deposited, consider assigning him a new car
 		if (toDeposit) {
 			Relocator carDriver = r.car.driver;
 			game.log("the car is driven by " + r.car.driver.rid);
 			game.log("i am " + r.rid);
 			assert(carDriver == r);
 			r.car.deposit();
 						
 			Car emptyCar = null;
 			if(r.hasChainCar()) { // has a car already reserved at location.
 				emptyCar = game.cars[r.getChainCar()];
 				game.log("Driver: " + r.rid + " taking already chained car: " + emptyCar.cid);
 				
 			} else {
 				List<Car> emptyCars = game.getEmptyCars(nextLoc);
 				if (emptyCars.size() > 0) { // if there are empty cars
 					emptyCar = pickCar(emptyCars);
 					game.log("Driver: " + r.rid + " found car " + emptyCar.cid + " at " + game.graph.getNodeName(nextLoc) + " and will take it.");
 				}
 			}
 			
 			if(emptyCar != null) {
 				r.assignCar(emptyCar);
 				emptyCar.assignDriver(r);
 			}
 			if (r.hasCar()) {
				game.log("Driver: " + r.rid +  " has car " + r.car.cid + " after deposit.");
 				if (emptyCar != null)
 					game.log("empty car is " + emptyCar.cid);
 				else
 					game.log("no empty car.");
 			}
 			else {
 				game.log("Driver " + r.rid + " doesn't have car after depost");
 			}
 		}		
 		return drive;		
 	}
 	
 	// pick a car when we have more than one available cars at the location
 	private Car pickCar(List<Car> emptyCars) {
 		int mindist = Integer.MAX_VALUE;
 		Car topick = null;
 		int dist;
 		for (Car car : emptyCars) {
 			if ((dist = game.nndist(car.source, car.destination)) < mindist) {
 				mindist = dist;
 				topick = car;
 			}
 		}
 		return topick;
 	}
 	
 	private void assignPickups(boolean ignoreLimit) {
 		List<PickupWrapper> pickupWrappers = findPickups(ignoreLimit);
 		Collections.sort(pickupWrappers);
 		
 		// Pick best pickup combinations.
 		Pickup pickup;
 		Relocator driver;
 		Relocator passenger;
 		Car driverCar;
 		Car pickupCar;
 		for(PickupWrapper pWrap : pickupWrappers) {
 			pickup = pWrap.pickup;
 			passenger = game.relocators[pickup.passengerRid];
 			driver = game.relocators[pickup.driverRid];
 			driverCar = driver.car;
 			pickupCar = game.cars[pickup.pickupingUpCid];
 			
 			int passengers = driver.pickups.size() + driverCar.passengers.size();
 			
 			// Ensure driver and passenger weren't already scheduled.
 			if(passengers < Game.MAX_PASSENGERS && passenger.pickuper == null) {
 				
 				// bug fixed by jiacheng
 				// the car may already be assigned as a chain car first						
 				if (pickupCar.isInuse()) { 
 					continue;
 				}
 
 				if(driver.baseDestination == pickup.dropLoc && !driver.hasChainCar()) {
 					// If the destination of this car happens to be our current destination...
 					// chain it to this car.
 					game.log("Driver: " + driver.rid + " is claming car : " + pickupCar.cid + " for his chain.");
 					pickupCar.assignDriver(driver);
 					driver.setChainCar(pickupCar.cid);
 					continue;
 				} else {
 					passenger = game.relocators[pickup.passengerRid];
 					pickupCar.assignDriver(passenger);
 					passenger.assignCar(pickupCar);
 					passenger.pickuper = driver;
 
 					game.log("Adding pickup for " + driver.rid + ": " + pickup);
 
 					if(driver.firstRoute().dst != pickup.dropLoc) {
 						driver.pushRoute(new Route(driver.car.cid, pickup.dropLoc, passenger.rid, Route.DROPOFF));
 						driver.pushRoute(new Route(driver.car.cid, pickup.pickLoc, passenger.rid, Route.PICKUP));
 					} else {
 						int nextLoc = game.graph.nextMove(driver.getLocation(), driver.firstRoute().dst);
 						// If the pickup location is our destination or our next move. then don't push.
 						if(driver.baseDestination != pickup.pickLoc && nextLoc != pickup.pickLoc) {
 							game.log("Driver: " + driver.rid + " Pushed pickup route to: " + game.graph.getNodeName(pickup.pickLoc));
 							driver.pushRoute(new Route(driver.car.cid, pickup.pickLoc, passenger.rid, Route.PICKUP));
 						}
 					}
 					
 					// Always add pickup so we know if this relocator is already scheduled.
 					driver.pickups.add(pickup);
 				}
 			}
 		}
 	}
 	
 	private List<PickupWrapper> findPickups(boolean ignoreLimit) {
 		
 		// if there is a waiting relocator (!hasCar) within distance d
 		// && is not scheduled a pickup
 		// && there is an available car within distance d from the pickup location
 		
 		List<PickupWrapper> pickups = new LinkedList<PickupWrapper>();
 		
 		// This happens before the drives are calculated so use currrent position.
 		Pickup pickup;
 		PickupDistance pickupDist;
 		for(Relocator driver : game.relocators) {
 			
 			
 			
 			if(		driver.isDriving() &&
 					driver.pickups.size() + driver.car.passengers.size() < Game.MAX_PASSENGERS) {
 				for(Relocator otherR : game.relocators) {
 					
 					// TODO: Optimize pickups until turn limit then relax perimeter to entire map.
 					int maxDistance = (ignoreLimit) ? Graph.MAP_MAX_DISTANCE : Graph.PICKUP_DISTANCE;
 					
 					if( 	!otherR.hasCar() && 
 							!otherR.hasChainCar() &&
 							!otherR.isScheduled() && // Driver may be dropping off and this can be ran after gendrive.
 							game.rrdist(otherR.rid, driver.rid) <= maxDistance)  {
 						assert(!otherR.isScheduled());
 						
 						for(Car car : game.cars) {
 							if(		!car.isInuse() && 
 									!car.isDeposit ) {
 								
 								// Gen pickup
 								pickup = new Pickup(otherR.rid, car.cid, otherR.getLocation(), car.getLocation(), driver.rid);
 								
 								// Gen cost of pickup.
 								pickupDist = new PickupDistance(
 										pickups.indexOf(pickup), 
 										
 										// 
 										game.rndist(driver.rid, pickup.pickLoc) + 
 										game.nndist(pickup.pickLoc, pickup.dropLoc) +
 										game.nndist(pickup.dropLoc, driver.baseDestination)
 										);
 								
 								pickups.add(new PickupWrapper(pickup, pickupDist));
 							}
 						}
 					}
 				}
 			}
 		}
 		
 		return pickups;
 	}
 	
 
 	private DriveBuilder genOtherDrive(int i) {
 		Relocator r = game.relocators[i];			
 		assert(r.pickuper == null);
 		r.move(r.getLocation());
 		return null;
 	}
 	
 	// is assigned a car, but not at the car
 	private DriveBuilder genPassengerDrive(int rid) {			
 		Relocator r = game.relocators[rid];	
 		
 		// TODO: Choose pickups optimized from Waiting perspective.
 		
 		if(r.pickuper == null || r.pickuper.car == null) {
 			// We were kicked out of car in previous step, so drive!
 			return null;
 		}
 		// follows the driver
 		if (r.pickuper.car.passengers.contains(new RGid(rid, game.gid))) {
 			game.log("Driver: " + rid + " riding with " + r.pickuper.rid);
 			r.move(r.pickuper.getLocation());		
 		} else {  // pickuper not here yet
 			game.log("Driver: " + rid + " waiting for " + r.pickuper.rid);
 			r.move(r.getLocation());
 		}
 		return null;
 	}
 }
