 package rental.g3;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 
 import rental.sim.Drive;
 import rental.sim.Edge;
 import rental.sim.Offer;
 import rental.sim.RGid;
 import rental.sim.Ride;
 
 public class Player extends rental.sim.Player {
 	// We can keep all the information about the game into this structure
 	private Game game;	
  	
 	public static void main(String[] args) {
 		/* Testing with dot:
 graph {
 Berlin -- Warsaw
 Berlin -- Munich
 Munich -- Warsaw
 Berlin -- Zurich
 Zurich -- Zagreb
 Zagreb -- Budapest
 Zurich -- Vienna
 Vienna -- Berlin
 Vienna -- Budapest
 Budapest -- Bucharest
 Budapest -- Moscow
 Zagreb -- Vienna
 Moscow -- Warsaw
 Warsaw -- Copenhagen
 Munich -- Copenhagen
 }
 		*/
 		Edge[] edges = new Edge[] {
 			new Edge("Berlin", "Warsaw"),
 			new Edge("Berlin", "Munich"),
 			new Edge("Munich", "Warsaw"),
 			new Edge("Berlin", "Zurich"),
 			new Edge("Zurich", "Zagreb"),
 			new Edge("Zagreb", "Budapest"),
 			new Edge("Zurich", "Vienna"),
 			new Edge("Vienna", "Berlin"),
 			new Edge("Vienna", "Budapest"),
 			new Edge("Budapest", "Bucharest"),
 			new Edge("Budapest", "Moscow"),
 			new Edge("Zagreb", "Vienna"),
 			new Edge("Moscow", "Warsaw"),
 			new Edge("Warsaw", "Copenhagen"),
 			new Edge("Munich", "Copenhagen")
 		};
 		
 		String[] carL = new String[] {
 			"Berlin",
 			"Vienna",
 			"Warsaw"
 		};
 		
 		String[] carD = new String[] {
 			"Zagreb",
 			"Budapest",
 			"Vienna"
 		};
 		
 		Player p = new Player();
 		try {
 			String[] starts = p.place(1, carL, carD, edges, 0);
 			System.out.println("Starting locations:");
 			System.out.println(Arrays.toString(starts));
 			System.out.println();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		for(int i = 0; i < p.game.graph.nodeCount(); i++) {
 			System.out.print(i + ": " + p.game.graph.getNodeName(i) + " ");
 			System.out.println(Arrays.toString(p.game.graph.getPaths()[i]));
 		}
 	}
 	
 	private Game initializeGame(int nrel, String[] carLocations,
 			String[] carDestinations, Edge[] edges) {
 		// initialize the graph			
 		Graph g = new Graph(edges);
 		
 		// initialize car location
 		int ncar = carLocations.length;
 		Car[] cars = new Car[ncar];
 		for (int i = 0; i < ncar; i++)
 			cars[i] = new Car(i, g.getNodeId(carLocations[i]), g.getNodeId(carDestinations[i]));
 		
 		// create a new game
 		Game game = new Game(this.id, g, ncar, cars, nrel, 10);
 		return game;
 	}
 	
 	@Override
 	public String[] place(int nrel, String[] carLocations,
 			String[] carDestinations, Edge[] edges, int groups)
 			throws Exception {
 		// initialize the game
 		game = initializeGame(nrel, carLocations, carDestinations, edges);
 		
 		
 		// place relocators
 		// If we have enough relocators for every car
 		// just return those locations
 		
 		Relocator[] relocators = new Relocator[nrel];
 		String[] startingNodes = new String[nrel];
 		if(nrel >= carLocations.length) {
 			// Note: Some locations may have more than one relocator
 			// or extra relocators are distributed to location 0
 
 			for (int i = 0; i < game.nCar; i++) {
 				relocators[i] = new Relocator(i, Relocator.RelocatorStatus.ENROUTE, game.graph.getNodeId(carLocations[i]));
 				relocators[i].pushRoute(new Route(i, game.cars[i].getDestination()));
 				startingNodes[i] = carLocations[i];
 				game.cars[i].setInuse(true);
 			}								
 			for (int i = game.nCar; i < nrel; i++) {
 				relocators[i] = new Relocator(i, Relocator.RelocatorStatus.WAITING, 0);
 				startingNodes[i] = game.graph.getNodeName(0);
 			}			
 		}
 		else {
 			// traverse car locations & desinations and
 			List<Graph.Distance> distances = new ArrayList<Graph.Distance>(game.nCar);
 			Graph.Distance distance;
 			int dist;
 			
 			// Find all the distances from S -> D
 			for(int i = 0; i < game.nCar; i++) {
 				int sourceId = game.graph.getNodeId(carLocations[i]);
 				int destId = game.graph.getNodeId(carDestinations[i]);				
 				dist = game.graph.getPaths()[sourceId][destId].dist;				
 				distance = game.graph.new Distance(i, dist);
 				distances.add(distance);
 			}
 			
 			// Sort them by distance
 			Collections.sort(distances);
 			
 			// Pick the shortest locations.						
 			for (int i = 0; i < game.nRelocator; i++) {
 				int carId = distances.get(i).carId;
 				int dst = game.cars[carId].destination;
 				startingNodes[i] = carLocations[carId];
 				relocators[i] = new Relocator(i, Relocator.RelocatorStatus.ENROUTE, game.graph.getNodeId(startingNodes[i]));
 				relocators[i].pushRoute(new Route(carId, dst));
 				relocators[i].cid = carId;
 				game.cars[carId].inuse = true;
 				game.cars[carId].driver = i;				
 			}
 		}
 		
 		// Set relocators in game
 		game.relocators = relocators;
 		return startingNodes;
 	}
 
 	@Override
 	public Offer[] offer() throws Exception {
 		// Not for monday
 		return new Offer[0];
 	}
 
 	@Override
 	public void request(Offer[] offers) throws Exception {
 		// Not for monday
 		
 	}
 
 	@Override
 	public void verify() throws Exception {
 		// Not for monday
 		
 	}
 
 	@Override
 	public DriveRide action() throws Exception {				
 		ActionGenerator actGen = new SimpleActionGenerator(game);
 		DriveRide dr = actGen.genDriveRide();		
 		// update drive results after each run
 		updateDrive();
 		return dr;
 	}
 
 	// update drives after each turn of action
 	private void updateDrive() {
 		for (Car car : game.cars)
 			car.move();	
 		for (Relocator r : game.relocators)
 			r.move();		
 	}
 	
 	// update rides before each turn of offer
 	// since all ride information is only available at that time 
 	private void updateRide(Ride[] rides) {		
 	}
 	
 }
