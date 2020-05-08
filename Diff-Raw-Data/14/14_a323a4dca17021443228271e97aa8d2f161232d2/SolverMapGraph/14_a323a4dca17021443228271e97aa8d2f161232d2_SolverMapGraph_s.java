 /**
  * 
  */
 package model.simulation;
 
 import java.util.ArrayDeque;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Observable;
 import java.util.Observer;
 import java.util.Queue;
 
 import model.SimulationEditorModelException;
 import model.UserDisruption;
 import model.config.SimulationOption;
 import model.entity.Car;
 import model.entity.Node;
 import model.entity.SimulationEditorModel;
 import model.entity.Street;
 import model.entity.TemporaryStreet;
 import model.entity.Vehicle;
 
 import org.jgrapht.alg.DijkstraShortestPath;
 import org.jgrapht.graph.DefaultWeightedEdge;
 import org.jgrapht.graph.SimpleDirectedWeightedGraph;
 
 import common.Constants;
 
 /**
  * @author dimitri.haemmerli
  *
  */
 public class SolverMapGraph implements Runnable, Observer{
 	
 	private SimulationEditorModel simulationEditorModel;
 	private Vehicle vehicle;
 	private long start;
 	private long end;
 	private boolean statistics;
 	
 	public SolverMapGraph(SimulationEditorModel simulationEditorModel){
 		
 		this.simulationEditorModel = simulationEditorModel;
 		this.statistics = false;
 	}
 	
 	private void startSimulation() throws InterruptedException {
 			
 		
 			SimpleDirectedWeightedGraph<Node, DefaultWeightedEdge> swg = createMapGraph();
 			
 			Queue<Node> pathForVehicle = getPathForVehicle(vehicle, swg, null);
 			
 			if(pathForVehicle.isEmpty()) {
 				throw new SimulationEditorModelException(String.format(Constants.MSG_NO_PATH, vehicle.getName()));
 			}
 			
 			Node n = pathForVehicle.poll();
 			
 			//statistics
 			if(n!=null){
 				
 				vehicle.addNode(n);
 
 			}
 						
 			System.out.println("path for Vehicle " + pathForVehicle);
 						
 			while(!vehicle.getCurrentNode().equals(vehicle.getFinishNode())){
 				
 				vehicle.setNextNode(pathForVehicle.poll());								
 				
 				Street currentStreet = null;
 				
 				for(int i = 0; i<simulationEditorModel.getMapEditorModel().getStreets().size(); i++) {
 					Street street = simulationEditorModel.getMapEditorModel().getStreets().get(i);
 					if(street.getStart().equals(vehicle.getCurrentNode()) && street.getEnd().equals(vehicle.getNextNode()) ||
 							   street.getStart().equals(vehicle.getNextNode()) && street.getEnd().equals(vehicle.getCurrentNode())){
 								currentStreet = street;
 								break;
 							}
 					
 				}
 				
 				currentStreet.getVehicles().add(vehicle);
 				
 				driveFromTo(vehicle.getCurrentNode(), vehicle.getNextNode(), currentStreet);
 				
 				//statistics
 				vehicle.addNode(vehicle.getNextNode());
 
 				vehicle.setCurrentNode(vehicle.getNextNode());
 				currentStreet.getVehicles().remove(vehicle);
 					
 			
 			
 			
 			}			
 
 			//reinitialize the current Node so a new simulation can be performed
 			vehicle.setCurrentNode(vehicle.getStartNode());
 			
 			
 			//statistics
 			vehicle.addNode(vehicle.getFinishNode());
 			System.out.println(vehicle.getPath());
 			updateStatistics();
 			removeTemporaryStreets();
 	}
 	
 	private void removeTemporaryStreets() {
 
 		// Clear temporary Streets from model
 		for(int i = 0; i<simulationEditorModel.getMapEditorModel().getStreets().size(); i++) {
 			Street street = simulationEditorModel.getMapEditorModel().getStreets().get(i);
 			if(street instanceof TemporaryStreet) {
				simulationEditorModel.getMapEditorModel().removeStreet(street);
 			}
 		}
 
 	}
 
 	private void updateStatistics() {
 
 		statistics = true;
 		SimulationOption simulationOption = vehicle.getSimulationOption();
 		
 		SimpleDirectedWeightedGraph<Node, DefaultWeightedEdge> swg;
 		//TODO: falls mglich anders behandeln
 		if(vehicle.getPath().size() > 0){
 			
 			vehicle.setSimulationOption(SimulationOption.SHORTEST_PATH);
 			swg = createMapGraph();
 			getPathForVehicle(vehicle, swg, SimulationOption.SHORTEST_PATH);
 			
 			vehicle.setSimulationOption(SimulationOption.FASTEST_PATH);
 			swg = createMapGraph();
 			getPathForVehicle(vehicle, swg, SimulationOption.FASTEST_PATH);
 			
 			if(vehicle instanceof Car){
 				
 				vehicle.setSimulationOption(SimulationOption.LOWEST_GAS_CONSUMPTION);
 				swg = createMapGraph();
 				getPathForVehicle(vehicle, swg, SimulationOption.LOWEST_GAS_CONSUMPTION);
 
 			}
 		}
 		
 		statistics = false;
 		vehicle.setSimulationOption(simulationOption);
 	}
 
 	private int calculateSpeed(Street currentStreet) throws InterruptedException {
 		
 		int speedLimit = 0;
 		
 		// Limit by street type
 		if(!SimulationOption.IGNORE_SPEEDLIMIT.equals(vehicle.getSimulationOption())) {
 			
 			speedLimit = Math.min(currentStreet.getStreetType().getSpeedLimit(), vehicle.getMaxSpeed());
 		}else{
 			
 			speedLimit = vehicle.getMaxSpeed();
 		}
 		
 		// Limitation for no-passing streets	
 		if(currentStreet.isNoPassing()) {
 			for(Vehicle other : currentStreet.getVehicles()) {
 				
 				int abstand =  this.vehicle.getDistToNext() - other.getDistToNext();
 				if(other == this.vehicle || abstand < 0) {
 					// Don't check myself and only look forward
 					continue;
 				}
 				
 				if(abstand == Constants.DISTANCE_NO_PASSING) {
 					// speed of other vehicle
 					speedLimit = Math.min(currentStreet.getStreetType().getSpeedLimit(), other.getMaxSpeed());
 				} else if(abstand < Constants.DISTANCE_NO_PASSING) {
 					// slow a bit down
 					speedLimit = Math.min(currentStreet.getStreetType().getSpeedLimit(), other.getMaxSpeed()) - 10;
 				}
 			}
 		}
 		
 		return speedLimit;
 	}
 	
 	private void driveFromTo(Node from, Node to, Street currentStreet) throws InterruptedException {
 		
 		
 		System.out.println("Drive from " + from + " to " + to);
 		
 		float ticks = new Street(from, to).getLenth();
 		
 		//statistics
 		start = System.currentTimeMillis();
 
 		for(int i=1; i<=ticks; i++) {
 
 			Node currentPosition = new Node();
 			currentPosition.setX((int) (from.getX() + (to.getX() - from.getX())*(i*(1/ticks))));
 			currentPosition.setY((int) (from.getY() + (to.getY() - from.getY())*(i*(1/ticks))));
 			
 			// TODO current position height
 			vehicle.setCurrentPosition(currentPosition);
 
 			int speedLimit = calculateSpeed(currentStreet);
 
 			Thread.sleep((2000/speedLimit));
 
 			simulationEditorModel.changed(vehicle);
 		}
 		
 		//statistics
 		end = System.currentTimeMillis();
 		vehicle.setActualTime(vehicle.getActualTimeTemp() + (end-start)/1000.0);
 
 	}
 	
 	
 	protected SimpleDirectedWeightedGraph<Node, DefaultWeightedEdge> createMapGraph(){
 			
 		SimpleDirectedWeightedGraph<Node, DefaultWeightedEdge> swg = new SimpleDirectedWeightedGraph<Node, DefaultWeightedEdge>(DefaultWeightedEdge.class);
 		
 		List<Street> streets = simulationEditorModel.getMapEditorModel().getStreets();
 		
 		//statistics
 		if(statistics){
 			
 			Iterator<Node> pathIterator = vehicle.getPath().iterator();
 			Node n1 = pathIterator.next();
 			
 			List<Street> statisticStreets = new ArrayList<Street>();
 			
 			while (pathIterator.hasNext()){
 				
 				Node n2 = pathIterator.next();
 				
 				for(int y = 0; y < streets.size(); y++){
 					if(streets.get(y).getStart().equals(n1) && streets.get(y).getEnd().equals(n2) ||
 							streets.get(y).getEnd().equals(n1) && streets.get(y).getStart().equals(n2)){
 						
 						statisticStreets.add(streets.get(y));
 					}
 
 				}
 				
 				n1 = n2;
 			}
 		
 			streets = statisticStreets;
 		}
 		
 		
 		for(int i = 0; i<streets.size(); i++) {
 			Street s = streets.get(i);
 			
 			if(s.isClosed()) {
 				continue;
 			}
 			
 			// Temp streets can only be used for corresponding vehicle
 			if(s instanceof TemporaryStreet) {
 				TemporaryStreet tempStreet = (TemporaryStreet)s;
 				if(tempStreet.getAllowedVehicle() != vehicle) {
 					continue;
 				}
 			}
 
 
 			// add the vertices
 			swg.addVertex(s.getStart());
 			swg.addVertex(s.getEnd());
 
 			// add edges to create linking structure
 			double weightAB = 0.0;
 			double weightBA = 0.0;
 			switch (vehicle.getSimulationOption()) {
 
 			case FASTEST_PATH:
 
 				if(vehicle.getMaxSpeed() < s.getStreetType().getSpeedLimit()){
 					weightAB = s.getLenth() * 60.0 / vehicle.getMaxSpeed();
 					weightBA = weightAB;
 				} else {
 					weightAB =  s.getLenth() *60.0 / s.getStreetType().getSpeedLimit();
 					weightBA = weightAB;
 				}
 
 				break;
 				
 			case IGNORE_SPEEDLIMIT:
 				weightAB = s.getLenth()*1.0;
 				weightBA = weightAB;
 				break;
 				
 			case LOWEST_GAS_CONSUMPTION:
 				
 				Car car = (Car)vehicle;
 				System.out.println(car.getVehicleTypes().getUrlVehicle() +" incline " + s.getIncline());
 
 				switch (s.getStreetType()) {
 				
 				case DISTRICT_ROAD:
 					weightAB = car.getGasConsumptionLow()/100.0 *s.getLenth() * getInclineFactor(s);
 					weightBA = car.getGasConsumptionLow()/100.0 *s.getLenth() * (1/getInclineFactor(s));
 					break;
 				case IN_TOWN:
 					weightAB = car.getGasConsumptionLow()/100 *s.getLenth() * getInclineFactor(s);
 					weightBA = car.getGasConsumptionLow()/100 *s.getLenth() * (1/getInclineFactor(s));
 					break;
 				case OUT_OF_TOWN:
 					weightAB = car.getGasConsumptionMedium()/100 *s.getLenth() * getInclineFactor(s);
 					weightBA = car.getGasConsumptionMedium()/100 *s.getLenth() * (1/getInclineFactor(s));
 					break;
 				case MOTORWAY:
 					weightAB = car.getGasConsumptionHigh()/100 *s.getLenth() * getInclineFactor(s);
 					weightBA = car.getGasConsumptionHigh()/100 *s.getLenth() * (1/getInclineFactor(s));
 					break;
 				case FREEWAY:
 					weightAB = car.getGasConsumptionHigh()/100 *s.getLenth() * getInclineFactor(s);
 					weightBA = car.getGasConsumptionHigh()/100 *s.getLenth() * (1/getInclineFactor(s));
 					break;
 				}
 				
 				break;
 			case SHORTEST_PATH:
 				weightAB = s.getLenth()*1.0;
 				weightBA = weightAB;
 				break;
 				
 			default:
 				break;
 
 			}
 
 			//TODO: Loops not allowed / nullpointer bei strassen sperren
 			DefaultWeightedEdge edgeOne = swg.addEdge(s.getStart(), s.getEnd());
 			if (edgeOne != null){
 				swg.setEdgeWeight(edgeOne, weightAB);
 			}
 			
 			if(!s.isOneWay()) {
 				DefaultWeightedEdge edgeTwo = swg.addEdge(s.getEnd(), s.getStart());
 				
 				if(edgeTwo != null){
 						swg.setEdgeWeight(edgeTwo, weightBA);
 						
 				}
 			}
 
 		}
 		return swg;
 	}
 
 	// TODO move to Street
 	private double getInclineFactor(Street s) {
 		
 		double incline = s.getIncline();
 		
 		if(incline < 0){
 			incline = 1/(-1 * incline +1);
 		}else {
 			incline+=1;
 		}
 		
 		System.out.println("incline " +incline);
 
 		return incline;
 	}
 
 	protected Queue<Node> getPathForVehicle(Vehicle vehicle, SimpleDirectedWeightedGraph<Node, DefaultWeightedEdge> swg, SimulationOption simulationStatistic) {
 		
 		if(!swg.containsVertex(vehicle.getCurrentNode()) || !swg.containsVertex(vehicle.getFinishNode())) {
 			throw new SimulationEditorModelException(String.format(Constants.MSG_NO_PATH, vehicle.getName()));
 		}
 		
 		DijkstraShortestPath<Node, DefaultWeightedEdge> dsp = new DijkstraShortestPath<Node, DefaultWeightedEdge>(swg, vehicle.getCurrentNode(), vehicle.getFinishNode());			
 		
 		if (statistics) {
 
 			switch (simulationStatistic) {
 
 			case SHORTEST_PATH:
 
 				vehicle.setPathLength(dsp.getPathLength());
 			
 			case FASTEST_PATH:
 
 				vehicle.setExpectedTime(dsp.getPathLength()/60.0);
 
 			case LOWEST_GAS_CONSUMPTION:
 
 				Car car = (Car) vehicle;
 				car.setGasUsage(dsp.getPathLength());
 				
 			default:
 				break;
 
 			}
 		}
 		
 		Queue<Node> nodes = new ArrayDeque<Node>();
 
 		if(dsp.getPathEdgeList() == null) {
 			// no path exists, return empty queue
 			return nodes;
 		}
 		
 		nodes.add(vehicle.getCurrentNode());
 		
 		for(DefaultWeightedEdge egde : dsp.getPathEdgeList()) {
 
 			Node source = swg.getEdgeSource(egde);
 			if(!nodes.contains(source)) {
 				nodes.add(source);
 			}
 
 			Node target = swg.getEdgeTarget(egde);
 			if(!nodes.contains(target)) {
 				nodes.add(target);
 			}
 		}
 		
 		return nodes;
 	}
 
 
 	/**
 	 * @return the vehicle
 	 */
 	public Vehicle getVehicle() {
 		return vehicle;
 	}
 
 	/**
 	 * @param vehicle the vehicle to set
 	 */
 	public void setVehicle(Vehicle vehicle) {
 		this.vehicle = vehicle;
 	}
 
 	private void recalculate() {
 		SimulationEditorModel.incRunningSimulations();
 		System.out.println("vehicle in recalc " + vehicle.getVehicleTypes());		
 		this.vehicle.getThread().interrupt();
 		this.vehicle.setSimulationDelay(0);
 		end = System.currentTimeMillis();
 		vehicle.setActualTimeTemp(vehicle.getActualTimeTemp() + ((end-start)/1000.0));
 		
 		Node temp = vehicle.getCurrentPosition();
 		//TODO: height of temp
 		
 		TemporaryStreet s1 = new TemporaryStreet(vehicle.getCurrentNode(), temp);
 		s1.setAllowedVehicle(vehicle);
 		TemporaryStreet s2 = new TemporaryStreet(temp, vehicle.getNextNode());
 		s2.setAllowedVehicle(vehicle);
 		
 		for(Street s : simulationEditorModel.getMapEditorModel().getStreets()) {
 			if(s.isPointOnStreet(temp.getX(), temp.getY())) {
 
 				s1.setStreetType(s.getStreetType());
 				s2.setStreetType(s.getStreetType());
 				
 				s1.setNoPassing(s.isNoPassing());
 				s2.setNoPassing(s.isNoPassing());
 				
 				s1.setOneWay(s.isOneWay());
 				s2.setOneWay(s.isOneWay());
 				
 				break;
 			}
 		}
 		
 		simulationEditorModel.getMapEditorModel().addStreet(s1);
 		simulationEditorModel.getMapEditorModel().addStreet(s2);
 		
 		this.vehicle.setCurrentNode(this.vehicle.getCurrentPosition());
 				
 		SolverMapGraph smg = new SolverMapGraph(simulationEditorModel);
 		smg.setVehicle(vehicle);
 		
 		this.vehicle.setThread(new Thread(smg));
 		this.getVehicle().getThread().start();
 		
 		
 	}
 
 	public void update(Observable o, Object arg) {
 		if(arg instanceof UserDisruption && this.vehicle.getThread().isAlive()) {
 			System.out.println("recalculate route");
 			this.simulationEditorModel = (SimulationEditorModel) o;
 			recalculate();
 		}
 	}
 
 	public void run() {
 		try {
 			Thread.sleep(vehicle.getSimulationDelay()*1000);
 			
 			startSimulation();
 			
 		} catch (InterruptedException e) {
 			// Nothinig to do here
 		} finally {
 			SimulationEditorModel.decRunningSimulations();
 		}
 	}
 
 
 
 }
