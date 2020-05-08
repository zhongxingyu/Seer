 /**
  * 
  */
 package model;
 
 import java.util.ArrayDeque;
 import java.util.List;
 import java.util.Observable;
 import java.util.Observer;
 import java.util.Queue;
 
 import org.jgrapht.alg.DijkstraShortestPath;
 import org.jgrapht.graph.DefaultWeightedEdge;
 import org.jgrapht.graph.SimpleWeightedGraph;
 
 /**
  * @author dimitri.haemmerli
  *
  */
 public class SolverMapGraph implements Runnable, Observer{
 	
 	private SimulationEditorModel simulationEditorModel;
 	private Vehicle vehicle;
 	
 	public SolverMapGraph(SimulationEditorModel simulationEditorModel){
 		
 		this.simulationEditorModel = simulationEditorModel;
 		
 	}
 	
 	private void startSimulation() {
 			
 			//TODO: abfangen falls es keinen k�zesten pfad gibt
 		
 			SimpleWeightedGraph<Node, DefaultWeightedEdge> swg = createMapGraph();
 			int speedLimit = 0;
 			
 			Queue<Node> pathForVehicle = getPathForVehicle(vehicle, swg);
 			
 			System.out.println(pathForVehicle);
 			
 			while(!vehicle.getCurrentKnot().equals(vehicle.getFinishKnot())){
 				
 							
 				//getting the nextKnot from Dijkstra
 				vehicle.setNextKnot(pathForVehicle.poll());
 				
 				for(Street s : simulationEditorModel.getMapEditorModel().getStreets() ){
 					if(s.getStart().equals(vehicle.getCurrentKnot()) && s.getEnd().equals(vehicle.getNextKnot()) ||
 							s.getStart().equals(vehicle.getNextKnot()) && s.getEnd().equals(vehicle.getCurrentKnot())){
 						speedLimit = s.getStreetType().getSpeedLimit();
 					}
 				}
 				
 					try {
 						driveFromTo(vehicle.getCurrentKnot(), vehicle.getNextKnot(), speedLimit);
 					} catch (InterruptedException e) {
 						return;
 					}
 					vehicle.setCurrentKnot(vehicle.getNextKnot());
 					
 			}
 			
 			//reinitialize the currentKnot so a new simulation can be performed
 			vehicle.setCurrentKnot(vehicle.getStartKnot());
 
 		
 	}
 	
 	private void driveFromTo(Node from, Node to, int speedLimit) throws InterruptedException {
 		
 		
 		// TODO: get speed from car
 		int speed = 1; 
 		float ticks = new Street(from, to).getLenth() / speed;
 		
 		
 		for(int i=1; i<=ticks; i++){
 			
 			Node currentPosition = new Node();
 			currentPosition.setX((int) (from.getX() + (to.getX() - from.getX())*(i*(1/ticks))));
 			currentPosition.setY((int) (from.getY() + (to.getY() - from.getY())*(i*(1/ticks))));
 			vehicle.setCurrentPosition(currentPosition);
 			
 			try {
				vehicle.getThread().sleep(5 * (140-speedLimit)/10);
 			} catch (InterruptedException e) {
 				// Re-throw
 				throw e;
 			}
 			updateModelSync();
 
 		}
 	}
 	
 	
 	protected SimpleWeightedGraph<Node, DefaultWeightedEdge> createMapGraph(){
 			
 		SimpleWeightedGraph<Node, DefaultWeightedEdge> swg = new SimpleWeightedGraph<Node, DefaultWeightedEdge>(
 				DefaultWeightedEdge.class);
 		
 		List<Street> streets = simulationEditorModel.getMapEditorModel().getStreets();
 
 		switch (vehicle.getSimulationOption()) {
 		
 		case SHORTEST_PATH: 
 
 			for (Street s : streets) {
 
 				if(s.isClosed()) {
 					continue;
 				}
 
 
 				// add the vertices
 				swg.addVertex(s.getStart());
 				swg.addVertex(s.getEnd());
 
 				// add edges to create linking structure
 				DefaultWeightedEdge dwg = swg.addEdge(s.getStart(),	s.getEnd());
 				swg.setEdgeWeight(dwg, s.getLenth());
 
 			}
 			
 			break;
 			
 		case FASTEST_PATH:
 			
 			for (Street s : streets) {
 				
 				if(s.isClosed()) {
 					continue;
 				}
 
 				// add the vertices
 				if (!swg.containsVertex(s.getStart())) {
 					swg.addVertex(s.getStart());
 				}
 				if (!swg.containsVertex(s.getEnd())) {
 					swg.addVertex(s.getEnd());
 				}
 
 				// add edges to create linking structure
 				if (!swg.containsEdge(s.getStart(), s.getEnd())) {
 					DefaultWeightedEdge dwg = swg.addEdge(s.getStart(),	s.getEnd());
					swg.setEdgeWeight(dwg, s.getLenth()/s.getStreetType().getSpeedLimit());
 				}
 				
 			}
 			
 			break;
 		
 		case LOWEST_GAS_CONSUMPTION:
 			
 			for (Street s : streets) {
 				
 				if(s.isClosed()) {
 					continue;
 				}
 
 				// add the vertices
 				if (!swg.containsVertex(s.getStart())) {
 					swg.addVertex(s.getStart());
 				}
 				if (!swg.containsVertex(s.getEnd())) {
 					swg.addVertex(s.getEnd());
 				}
 
 				//TODO: if car und andere fahrzeugtypen unterscheiden
 				Car c = (Car) vehicle;
 				// add edges to create linking structure
 				if (!swg.containsEdge(s.getStart(), s.getEnd())) {
 					DefaultWeightedEdge dwg = swg.addEdge(s.getStart(),	s.getEnd());
 					
 					switch (s.getStreetType()){
 					
 					case QUARTIER:						
 						swg.setEdgeWeight(dwg, c.getGasConsumptionLow()/100 *s.getLenth());
 						break;
 					case INNERORTS:						
 						swg.setEdgeWeight(dwg, c.getGasConsumptionLow()/100 *s.getLenth());
 						break;
 					case AUSSERORTS:						
 						swg.setEdgeWeight(dwg, c.getGasConsumptionMedium()/100 *s.getLenth());
 						break;
 					case AUTOSTRASSE:						
 						swg.setEdgeWeight(dwg, c.getGasConsumptionMedium()/100 *s.getLenth());
 						break;
 					case AUTOBAHN:						
 						swg.setEdgeWeight(dwg, c.getGasConsumptionHigh()/100 *s.getLenth());
 						break;
 					
 					}
 				}
 				
 			}
 			
 			break;
 			
 		default:
 			break;
 			
 		}
 		
 		return swg;
 	}
 
 	private Node shortestPathDijkstra(Vehicle v, SimpleWeightedGraph<Node, DefaultWeightedEdge> swg, Node currentPosition, Node endPosition){
 		
 //		System.out.println("test");
 
 		DijkstraShortestPath<Node, DefaultWeightedEdge> dsp = new DijkstraShortestPath<Node, DefaultWeightedEdge>(swg, currentPosition, endPosition);
 		//BellmanFordShortestPath<Knot, DefaultWeightedEdge> bfsp = new BellmanFordShortestPath<Knot, DefaultWeightedEdge>(swg, startPosition);
 		//System.out.println(bfsp.getCost(endPosition));
 		System.out.println(dsp.getPathEdgeList());
 
 		List<DefaultWeightedEdge> shortestPath = dsp.getPathEdgeList();
 		System.out.println("pathlength " + dsp.getPathLength());
 
 		
 		if (shortestPath != null) {
 			
 			for (DefaultWeightedEdge d1 : shortestPath) {
 
 				// return the nextKnot (could be the source or the target of the
 				// edge )
 				if (swg.getEdgeSource(d1).equals(v.getCurrentKnot())) {
 
 					return swg.getEdgeTarget(d1);
 				}
 				if (swg.getEdgeTarget(d1).equals(v.getCurrentKnot())) {
 
 					return swg.getEdgeSource(d1);
 				}
 
 			}
 		}
 		
 		return null;
 	}
 	
 	protected Queue<Node> getPathForVehicle(Vehicle vehicle, SimpleWeightedGraph<Node, DefaultWeightedEdge> swg) {
 		
 		DijkstraShortestPath<Node, DefaultWeightedEdge> dsp = new DijkstraShortestPath<Node, DefaultWeightedEdge>(swg, vehicle.getCurrentKnot(), vehicle.getFinishKnot());
 		
 		
 		
 		Queue<Node> nodes = new ArrayDeque<Node>();
 
 		if(dsp.getPathEdgeList() == null) {
 			//TODO: no path exists
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
 
 	@Override
 	public void run() {
 
 		try {
 			Thread.sleep(vehicle.getDelay()*1000);
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		startSimulation();
 		
 	}
     
 	private void updateModelSync() {
 		
 		simulationEditorModel.changed(vehicle);
 				
 	}
 	
 	private void recalculate() {
 		this.vehicle.getThread().interrupt();
 		
 		Node temp = vehicle.getCurrentPosition();
 		Street s1 = new Street(temp, vehicle.getCurrentKnot());
 		s1.setTemporary(true);
 		Street s2 = new Street(temp, vehicle.getNextKnot());
 		s2.setTemporary(true);
 		
 		for(Street s : simulationEditorModel.getMapEditorModel().getStreets()) {
 			if(s.isPointOnStreet(temp.getX(), temp.getY())) {
 				s1.setStreetType(s.getStreetType());
 				s2.setStreetType(s.getStreetType());
 			}
 		}
 		
 		simulationEditorModel.getMapEditorModel().addStreet(s1);
 		simulationEditorModel.getMapEditorModel().addStreet(s2);
 		
 		//TODO: height etc
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
 
 
 }
