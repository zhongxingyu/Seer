 /**
  * 
  */
 package model.simulation;
 
 import java.util.ArrayDeque;
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
 	
 	public SolverMapGraph(SimulationEditorModel simulationEditorModel){
 		
 		this.simulationEditorModel = simulationEditorModel;
 		
 	}
 	
 	private void startSimulation() throws InterruptedException {
 			
 		
 			SimpleDirectedWeightedGraph<Node, DefaultWeightedEdge> swg = createMapGraph();
 			
 			Queue<Node> pathForVehicle = getPathForVehicle(vehicle, swg);
 			
 			if(pathForVehicle.isEmpty()) {
 				throw new SimulationEditorModelException(String.format(Constants.MSG_NO_PATH, vehicle.getName()));
 			}
 			
 			pathForVehicle.poll();
 			
 			//Statistics
 			if(vehicle.getPath() == null){
 				
 				vehicle.setPath(pathForVehicle);
 			}//TODO:falls path neu berechnet wurde, das neue teilst�ck hinzuf�gen.
 			
 			
 			System.out.println("path for Vehicle " + pathForVehicle);
 			
 			//statistics
 			start = System.currentTimeMillis();
 			
 			while(!vehicle.getCurrentKnot().equals(vehicle.getFinishKnot())){
 				
 							
 				//getting the nextKnot from Dijkstra
 				vehicle.setNextKnot(pathForVehicle.poll());
 				
 				
 				Street currentStreet = null;
 				
 				for(int i = 0; i<simulationEditorModel.getMapEditorModel().getStreets().size(); i++) {
 					Street street = simulationEditorModel.getMapEditorModel().getStreets().get(i);
 					if(street.getStart().equals(vehicle.getCurrentKnot()) && street.getEnd().equals(vehicle.getNextKnot()) ||
 							   street.getStart().equals(vehicle.getNextKnot()) && street.getEnd().equals(vehicle.getCurrentKnot())){
 								currentStreet = street;
 								break;
 							}
 					
 				}
 				
 				currentStreet.getVehicles().add(vehicle);
 				
 				// remove unused temporary streets during simulation
 				for(int i = 0; i<simulationEditorModel.getMapEditorModel().getStreets().size(); i++) {
 					Street street = simulationEditorModel.getMapEditorModel().getStreets().get(i);
 					if(street instanceof TemporaryStreet) {
 						TemporaryStreet tempStreet = (TemporaryStreet)street;
 						System.out.println(tempStreet.getVehicles());
 						if(tempStreet.getVehicles().isEmpty()) {
 							System.out.println("remove " + tempStreet);
 							simulationEditorModel.getMapEditorModel().removeStreet(tempStreet);
 						}
 					}
 					
 				}
 				
 				
 				driveFromTo(vehicle.getCurrentKnot(), vehicle.getNextKnot(), currentStreet);
 				
 				vehicle.setCurrentKnot(vehicle.getNextKnot());
 				currentStreet.getVehicles().remove(vehicle);
 					
 			}
 						
 			//reinitialize the currentKnot so a new simulation can be performed
 			vehicle.setCurrentKnot(vehicle.getStartKnot());
 
 		
 	}
 	
 	private int calculateSpeed(Street currentStreet) throws InterruptedException {
 		
 		int speedLimit = 0;
 		
 		// Limit by street type
 		if(!SimulationOption.IGNORE_SPEEDLIMIT.equals(vehicle.getSimulationOption())) {
 			
 			speedLimit = Math.min(currentStreet.getStreetType().getSpeedLimit(), vehicle.getMaxSpeed());
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
 		
 		
 		for(int i=1; i<=ticks; i++) {
 
 			Node currentPosition = new Node();
 			currentPosition.setX((int) (from.getX() + (to.getX() - from.getX())*(i*(1/ticks))));
 			currentPosition.setY((int) (from.getY() + (to.getY() - from.getY())*(i*(1/ticks))));
 			
 			// TODO current position height
 			vehicle.setCurrentPosition(currentPosition);
 
 			//statistics
 			end = System.currentTimeMillis();
 			vehicle.setActualTime(vehicle.getActualTimeTemp() + (end-start)/1000.0);
 			
 			int speedLimit = calculateSpeed(currentStreet);
 
 			Thread.sleep((2000/speedLimit));
 
 			simulationEditorModel.changed(vehicle);
 		}
 	}
 	
 	
 	protected SimpleDirectedWeightedGraph<Node, DefaultWeightedEdge> createMapGraph(){
 			
 		SimpleDirectedWeightedGraph<Node, DefaultWeightedEdge> swg = new SimpleDirectedWeightedGraph<Node, DefaultWeightedEdge>(DefaultWeightedEdge.class);
 		
 		List<Street> streets = simulationEditorModel.getMapEditorModel().getStreets();
 		
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
 			int weight = 0;
 			switch (vehicle.getSimulationOption()) {
 
 			case FASTEST_PATH:
 
 				if(vehicle.getMaxSpeed() < s.getStreetType().getSpeedLimit()){
 					weight = s.getLenth() / vehicle.getMaxSpeed();
 				} else {
 					weight =  s.getLenth() / s.getStreetType().getSpeedLimit();
 				}
 
 				break;
 				
 			case IGNORE_SPEEDLIMIT:
 				weight = s.getLenth();
 				break;
 				
 			case LOWEST_GAS_CONSUMPTION:
 				
 				Car car = (Car)vehicle;
 				
 				switch (s.getStreetType()) {
 				
 				case QUARTIER:						
 					weight = (int) (car.getGasConsumptionLow()/100 *s.getLenth());
 					break;
 				case INNERORTS:						
 					weight = (int) (car.getGasConsumptionLow()/100 *s.getLenth());
 					break;
 				case AUSSERORTS:						
 					weight = (int) (car.getGasConsumptionMedium()/100 *s.getLenth());
 					break;
 				case AUTOSTRASSE:						
 					weight = (int) (car.getGasConsumptionMedium()/100 *s.getLenth());
 					break;
 				case AUTOBAHN:						
 					weight = (int) (car.getGasConsumptionHigh()/100 *s.getLenth());
 					break;
 				}
 				
 				break;
 			case SHORTEST_PATH:
 				weight = s.getLenth();
 				break;
 				
 			default:
 				break;
 
 			}
 			
 			DefaultWeightedEdge edgeOne = swg.addEdge(s.getStart(), s.getEnd());
 			swg.setEdgeWeight(edgeOne, weight);
 			
 			if(!s.isOneWay()) {
 				DefaultWeightedEdge edgeTwo = swg.addEdge(s.getEnd(), s.getStart());
 				swg.setEdgeWeight(edgeTwo, weight);
 			}
 
 		}
 		return swg;
 	}
 
 	protected Queue<Node> getPathForVehicle(Vehicle vehicle, SimpleDirectedWeightedGraph<Node, DefaultWeightedEdge> swg) {
 		
 		if(!swg.containsVertex(vehicle.getCurrentKnot()) || !swg.containsVertex(vehicle.getFinishKnot())) {
 			throw new SimulationEditorModelException(String.format(Constants.MSG_NO_PATH, vehicle.getName()));
 		}
 		
 		DijkstraShortestPath<Node, DefaultWeightedEdge> dsp = new DijkstraShortestPath<Node, DefaultWeightedEdge>(swg, vehicle.getCurrentKnot(), vehicle.getFinishKnot());			
 		
 		//statistics
 		vehicle.setPathLength(dsp.getPathLength());
 		
 		
 		Queue<Node> nodes = new ArrayDeque<Node>();
 
 		if(dsp.getPathEdgeList() == null) {
 			// no path exists, return empty queue
 			return nodes;
 		}
 		
 		nodes.add(vehicle.getCurrentKnot());
 		
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
 		
 		//statistics
 		vehicle.setPathLength(dsp.getPathLength());
 
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
 		
 		// Clear temporary Streets from model
 		for(int i = 0; i<simulationEditorModel.getMapEditorModel().getStreets().size(); i++) {
 			Street street = simulationEditorModel.getMapEditorModel().getStreets().get(i);
 			if(street instanceof TemporaryStreet) {
 				simulationEditorModel.getMapEditorModel().removeStreet(street);
 			}
 		}
 		
 		this.vehicle.getThread().interrupt();
 		end = System.currentTimeMillis();
 		vehicle.setActualTimeTemp(vehicle.getActualTimeTemp() + ((end-start)/1000.0));
 		
 		Node temp = vehicle.getCurrentPosition();
 		//TODO: height of temp
 		
 		TemporaryStreet s1 = new TemporaryStreet(temp, vehicle.getCurrentKnot());
 		s1.setAllowedVehicle(vehicle);
 		TemporaryStreet s2 = new TemporaryStreet(temp, vehicle.getNextKnot());
 		s2.setAllowedVehicle(vehicle);
 		
 		for(Street s : simulationEditorModel.getMapEditorModel().getStreets()) {
 			if(s.isPointOnStreet(temp.getX(), temp.getY())) {
 				s1.setStreetType(s.getStreetType());
 				s2.setStreetType(s.getStreetType());
 			}
 		}
 		
 		simulationEditorModel.getMapEditorModel().addStreet(s1);
 		simulationEditorModel.getMapEditorModel().addStreet(s2);
 		
 		this.vehicle.setCurrentKnot(this.vehicle.getCurrentPosition());
 		
 		SolverMapGraph smg = new SolverMapGraph(simulationEditorModel);
 		smg.setVehicle(vehicle);
 		
 		this.vehicle.setThread(new Thread(smg));
 		this.getVehicle().getThread().start();
 		
 		
 	}
 
 	public void update(Observable o, Object arg) {
 		if(arg instanceof UserDisruption) {
 			System.out.println("recalculate route");
 			this.simulationEditorModel = (SimulationEditorModel) o;
 			recalculate();
 		}
 	}
 
 	public void run() {
 		try {
 			Thread.sleep(vehicle.getDelay()*1000);
 			
 //			start = System.currentTimeMillis();
 			startSimulation();
 			
 		} catch (InterruptedException e) {
 			// Nothinig to do here
 		} finally {
 			SimulationEditorModel.decRunningSimulations();
 		}
 	}
 
 
 
 }
