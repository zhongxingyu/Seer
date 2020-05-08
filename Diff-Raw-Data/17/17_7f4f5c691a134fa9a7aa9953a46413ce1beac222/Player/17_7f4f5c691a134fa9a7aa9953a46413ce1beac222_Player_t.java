 package rental.g3;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Random;
 import java.util.Set;
 
 import rental.sim.Drive;
 import rental.sim.Edge;
 import rental.sim.Offer;
 import rental.sim.RGid;
 import rental.sim.Ride;
 
 public class Player extends rental.sim.Player {
 	// We can keep all the information about the game into this structure
 	private Game game;
 	private DriveBuilder[] driveBuilds;
 	private List<Ride> rides;
  	
 	public String name()
 	{
 		return "Group 3";
 	}
 	
 	private Game initializeGame(int nrel, String[] carLocations,
 			String[] carDestinations, Edge[] edges, int totalTurns) {
 		// initialize the graph			
 		Graph g = new Graph(edges);
 		
 		// initialize car location
 		int ncar = carLocations.length;
 		Car[] cars = new Car[ncar];
 		for (int i = 0; i < ncar; i++)
 			cars[i] = new Car(i, g.getNodeId(carLocations[i]), g.getNodeId(carDestinations[i]));
 		
 		// create a new game
 		Game game = new Game(this.id, g, ncar, cars, nrel, totalTurns);
 		
 		// Set pick up distance
 		Graph.PICKUP_DISTANCE = (int)(0.2 * Graph.MAP_MAX_DISTANCE);
 //		Graph.PICKUP_DISTANCE = 1;
 		
 		return game;
 	}
 	
 	@Override
 	public String[] place(int nrel, String[] carLocations,
 			String[] carDestinations, Edge[] edges, int groups, int totalTurns)
 			throws Exception {
 		// initialize the game
 		game = initializeGame(nrel, carLocations, carDestinations, edges, totalTurns);
 				
 		game.doPlacement();
 				
 		return game.getStaringNodes();
 	}
 
 	@Override
 	public Offer[] offer() throws Exception {
 		game.turn++;
 		
 		Relocator rider;
 		if(rides != null) {
 			for(Ride ride : rides) {
 				rider = game.relocators[ride.rid];
 				if(!ride.executed()) {
					game.log("Offer reneged so reseting driver: " + rider.rid);
 					rider.setLocation(rider.getLastLocation());
 					rider.pickuper.updatePickupRoute(rider);
 				} else {
 					// If he managed to get to his destination himself.
 					if(rider.getLocation() == rider.car.getLocation()) {
						game.log("Driver: " + rider.rid + " arrive at pickupLoc, removing pickuper.");
 						rider.pickuper.removeDropOffRoutes(rider.rid);
 						rider.pickuper.removePickupRoutes(rider.rid);
 						rider.pickuper.removePickup(rider.rid);
 						rider.pickuper = null;
 					}
 				}
 			}
 		}
 		
 		// Calculate the moves for this turn.
 		this.driveBuilds = generateDriveRide();
 		
 		// Not for monday
 		game.offers = new LinkedList<Offer>();
 		game.offerRelocators = new LinkedList<Relocator>();
 		
 		for(Relocator r : game.relocators) {
			// Check both since we've already calculated
			// next move, a relocator may get be one
			// spot away from dropoff which would appear
			// like he's driving.
			if(r.isDriving() && r.wasDriving()) {
 				
 				// We only need to make one offer
 				// and when we accept requests we can then
 				// accept more than one.
 				int seats = 3 - r.car.passengers.size();
 				if(seats > 0) {
 					game.offers.add(new Offer(
 							game.graph.getNodeName(r.getLastLocation()),
 							game.graph.getNodeName(r.getLocation()),
 							game.turn,
 							Player.this
 							));		
 					game.offerRelocators.add(r);
 					game.log("Driver: " + r.rid + " making offer.");
 				}
 			}
 		}
 		
 		return game.offers.toArray(new Offer[0]);
 	}
 
 	@Override
 	public void request(Offer[] offers) throws Exception {
 		this.game.gameOffers = offers;
 		for(Relocator r : game.relocators) {
 			if(!r.isDriving() && r.hasCar() && r.pickuper != null) {
 				// relocator is potential candidate.
 				for(Offer offer : offers) {
 					// If offer is 
 					if( offer.group != game.gid && 
 						offer.src == game.graph.getNodeName(r.getLastLocation()) && offer.time == game.turn) {
 						int distToRelocator = game.rrdist(r.rid, r.pickuper.rid);
 						int distToCar = game.rndist(r.rid, r.car.getLocation());
 						
 						int offerToRelocator = game.rndist(r.pickuper.rid, game.graph.getNodeId(offer.dst));
 						int offerToCar = game.nndist(r.car.getLocation(), game.graph.getNodeId(offer.dst));
 						
 						if(offerToRelocator < distToRelocator) {
 							game.log("Relocator: " + r.rid + " getting closer to pickup driver.");
 							offer.request(r.rid, Player.this);
 						} else if(offerToCar < distToCar) {
 							offer.request(r.rid, Player.this);
 						}
 					}
 				}
 			}
 		}
 	}
 
 	@Override
 	public void verify() throws Exception {
 		// Not for monday
 		
 		for ( int i = 0; i < game.offers.size(); i ++)
 		{
 			Relocator r = game.offerRelocators.get(i);
 			Offer offer = game.offers.get(i);
 			RGid [] rgidArr = offer.requests();
 			
 			if(rgidArr.length <= 0) {
 				continue;
 			}
 			
 			boolean[] verifyArr = new boolean[rgidArr.length];
 			int aSeats = 3 - r.car.passengers.size();
 			for ( int j = 0; j < Math.min(aSeats, verifyArr.length); j++ )
 			{
 				Random gen = new Random();
 				int index = 0;
 				do
 				{
 					index = gen.nextInt(verifyArr.length);
 				}while(verifyArr[index]==true);
 				
 				verifyArr[index] = true;
 				for(DriveBuilder drive : this.driveBuilds) {
 					if(drive.car == r.car.cid) {
 						drive.passengerSet.add(offer.requests()[index]);
 					}
 				}
 				
 			}
 			game.offers.get(i).verify(verifyArr, Player.this);
 		}
 	}
 		
 	
 
 	@Override
 	public DriveRide action() throws Exception {
 		
 		if (game.turn >= game.nTurns) {
 			Graph.PICKUP_DISTANCE = Graph.MAP_MAX_DISTANCE;
 		}
 		
 		rides = new ArrayList<Ride>();
 		Set<Integer> relocatorsRiding = new HashSet<Integer>();
 		RGid rgid = null;
 		
 		Ride ride = null;
 		for(Offer offer: game.gameOffers) {
 			for(int i = 0; i < offer.requests().length; i++) {
 				rgid = offer.requests()[i];
 				// If we're same group and offer was accepted
 				if(
 					rgid.gid == game.gid && // Same group
 					!relocatorsRiding.contains(rgid.rid) && // We didn't already accept offer for relocator. 
 					offer.verifications()[i] // Request accepted
 							) {
 					game.log("Driver: " + rgid.rid + " making ride for accepted offer to: " + offer.dst);
 					
 					ride = new Ride(rgid.rid,offer.group,offer.dst);
 					rides.add(ride);
 					relocatorsRiding.add(rgid.rid);
 					
 					Relocator r = game.relocators[rgid.rid];
 					r.setLocation(game.graph.getNodeId(offer.dst));
 					r.pickuper.updatePickupRoute(r);
 					
 					continue;
 				}
 			}
 		}
 		
 		List<Drive> driveRides = new ArrayList<Drive>(this.driveBuilds.length);
 		for(DriveBuilder builder : driveBuilds) {
 			driveRides.add(builder.build());
 		}
 		
 		return new DriveRide(driveRides.toArray(new Drive[0]), rides.toArray(new Ride[0]));
 	}
 	
 	private DriveBuilder[] generateDriveRide() {
 		// before each turn starts, reset scheduling information
 		resetAction();
 		
 		ActionGenerator actGen = new SimpleActionGenerator(game);		
 		DriveBuilder[] db = actGen.genDriveRide();		
 		// update drive results after each run
 		return db;
 	}
 	
 	private void resetAction() {
 		for (int i = 0; i < game.nRelocator; i++)
 			game.relocators[i].reset();
 		for (int i = 0; i < game.nCar; i++)
 			game.cars[i].reset();
 	}
 
 }
