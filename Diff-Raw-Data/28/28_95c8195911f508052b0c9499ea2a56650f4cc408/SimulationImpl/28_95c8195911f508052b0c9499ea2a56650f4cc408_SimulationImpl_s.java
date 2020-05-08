 package adp2.implementations;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import adp2.interfaces.*;
 
 public class SimulationImpl implements Simulation{
 
 		Graph currentGraph;
 		List<Graph> graphStates;
 		List<Ant> antList = new ArrayList<Ant>();
 		List<List<Integer>> pheromoneUpdateList = new ArrayList<List<Integer>>();		
 
 		int antQuantity;
 		int antsByStep = 0; //Anzahl der Ameisen die pro Step hinzugefuegt werden
 		double antAlpha = 0.5;
 		int startPoint = 1;
 		int pheromoneDecrease = 1;
 		int pheromoneIntensity = 10;
 		// The Simulation should not log the states of all Graphs by default
 		boolean logStates = false;
 		
 		int bestDistance = Integer.MAX_VALUE;
 		public List<Integer> bestPath = new ArrayList<Integer>();
 
 		//Ameisen werden alle direkt rein geworfen
 	    public static Simulation valueOf(Graph graph, int antsQuantity) {
 			return new SimulationImpl(graph, antsQuantity);
 		}
 	    
 	    //Ameisen werden in Wellen rein geworfen
 	    public static Simulation valueOf(Graph graph, int antsQuantity, int antsByStep) {
 			return new SimulationImpl(graph, antsQuantity, antsByStep);
 		}
 	    
 		//Ameisen werden alle direkt rein geworfen
 	    // Log States
 	    public static Simulation valueOf(Graph graph, int antsQuantity, boolean logStates) {
 			return new SimulationImpl(graph, antsQuantity, logStates);
 		}
 	    
 	    //Ameisen werden in Wellen rein geworfen
 	    // Log States
 	    public static Simulation valueOf(Graph graph, int antsQuantity, int antsByStep, boolean logStates) {
 			return new SimulationImpl(graph, antsQuantity, antsByStep, logStates);
 		}
 	    
 	    private SimulationImpl(Graph graph, int antsQuantity) {
 	    	setGraph(graph);
 	    	setAntQuantity(antsQuantity);
 	    	addAnts(antsQuantity);
 	    	graphStates = new ArrayList<Graph>();
 	    }
 	
 	    private SimulationImpl(Graph graph, int antsQuantity, int antsByStep) {
 	    	setGraph(graph);
 	    	setAntQuantity(antsQuantity);
 	    	setAntsByStep(antsByStep);
 	    	graphStates = new ArrayList<Graph>();
 	    }
 	    
 	    private SimulationImpl(Graph graph, int antsQuantity, boolean logStates) {
 	    	setGraph(graph);
 	    	setAntQuantity(antsQuantity);
 	    	addAnts(antsQuantity);
 	    	graphStates = new ArrayList<Graph>();
 	    	this.logStates = logStates;
 	    }
 	
 	    private SimulationImpl(Graph graph, int antsQuantity, int antsByStep, boolean logStates) {
 	    	setGraph(graph);
 	    	setAntQuantity(antsQuantity);
 	    	setAntsByStep(antsByStep);
 	    	graphStates = new ArrayList<Graph>();
 	    	this.logStates = logStates;
 	    }
 	    
 	    @Override
 	    public void start(){
	    	boolean antsLeft = true;
 	    	long startzeit = System.currentTimeMillis();
	    	while(System.currentTimeMillis()-startzeit < 30000){ //Abbruch nach 2Sec, weitere Bedingungen kommen
 	    		
 	    		/*
 	    		 * antList = Ameisen aktuell im Graphen
	    		 * antsByStep = Ameisen die hinzugefuegt werden pro Step wenn noetig
	    		 * antQuantity = Anzahl der Ameisen, die zu BEGINN in den Graphen gegeben werden
	    		 */
	    		
	    		
	    		if((antsByStep() != 0) && antsLeft){
	    			if( (antList().size()+antsByStep()) <= antQuantity()){ //Ameisen um antsByStep erh�hen
 	    				addAnts(antsByStep());
	    			} else if(antList().size() < antQuantity()){ // Ameisen um Rest < antsByStep erh�hen
 	    				addAnts(antQuantity()-antList().size());
	    				antsLeft = false;
 	    			}
 	    		}
	    		//System.out.println(antList().size()+" -> "+antList.size());
 	    		int i = 0;
 	    		while (i < antList().size()){
 	    			if (antList().get(i).hasFinished()) {
 	    				if (antList().get(i).weglaenge() < bestDistance()) {
 	    					setBestDistance(antList().get(i).weglaenge());
 	    					setBestPath(antList().get(i).traveledPath().waypoints());
 	    				}
 	    				removeAnt(i); 
 	    			} else {
 		    			if(antList().get(i).getWaitingTime() == 0){ 	//Befindlich auf Knoten
 		    				antList().get(i).step(); 					//Entscheidungsalgorithmus und einen Schritt gehen
 		    				addPheromoneUpdate(antList().get(i).prePosition(),antList().get(i).position(),pheromoneIntensity()); //Pheromonverteilung vorbereiten
 		    			}else{
 		    				antList().get(i).step();
 		    			}
 		    			i++;
 	    			}
 	    		}
 	    		pheromoneDecreament();
 	    		pheromoneIncrement();
 	    		if (logStates) {
 		    		graphStates.add(currentGraph);
 		    		/*
 		    		 * For every Step this adds the current Graph to the list of graphStates
 		    		 * Doing so gives us the ability to replay the Graph transformations. 
 		    		 */
 		    		currentGraph = currentGraph.deepClone();
 	    		}
 	    	}
 	    }
 		
 
 	    /*Getter and Setter*/
 	    
 	    private void setGraph(Graph graph){
 	    	this.currentGraph = graph;
 	    }
 	    
 	    private void setAntQuantity(int quantity){
 	    	antQuantity = quantity;
 	    }
 	    
 	    private int antQuantity(){
 	    	return antQuantity;
 	    }
 	    
 	    private void setAntsByStep(int quantity){
 	    	antsByStep = quantity;
 	    }
 	    
 	    private int antsByStep(){
 	    	return antsByStep;
 	    }
 	    
 	    private int bestDistance(){
 	    	return bestDistance;
 	    }
 	    
 	    private void setBestDistance(int distance){
 	    	bestDistance = distance;
 	    }
 	    
 	    private List<Integer> bestPath(){
 	    	return bestPath;
 	    }
 	    
 	    private void setBestPath(List<Integer> path){
 	    	bestPath = path;
 	    }
 	    	    
 	    private List<Ant> antList(){
 	    	return antList;
 	    }
 	    	    
 	    private double antAlpha(){
 	    	return antAlpha;
 	    }
 	    
 	    private int startPoint(){
 	    	return startPoint;
 	    }
 	    
 	    private Graph graph(){
 	    	return currentGraph;
 	    }
 	    
 	    private int pheromoneIntensity(){
 	    	return pheromoneIntensity;
 	    }
 	    
 	    private int pheromoneDecrease(){
 	    	return pheromoneDecrease;
 	    }
 	    
 	    private List<List<Integer>> pheromoneUpdateList(){
 	    	return pheromoneUpdateList;
 	    }
 	    
 	    
 	    /*Functions*/
 	    
 	    private void addAnt(){
 	    	antList().add(AntImpl.valueOf(startPoint(), antAlpha(), graph())); /////START POINT?
 	    }
 	    
 	    private void addAnts(int quantity){
 	    	for(int i=1; i <= quantity; i++){
 	    		addAnt();
 	    	}
 	    }
 	    
 	    private void removeAnt(int antIndex){
 	    	antList().remove(antIndex);
 	    }
 	    
 	    private void addPheromoneUpdate(int from,int to, int intensity){
 	    	List<Integer> pheromoneElement = new ArrayList<Integer>();
 	    	pheromoneElement.add(from);
 	    	pheromoneElement.add(to);
 	    	pheromoneElement.add(intensity);
 	    	if (from != to){
 	    		pheromoneUpdateList().add(pheromoneElement);
 	    	}
 	    }
 	    
 	    private void pheromoneIncrement(){
 	    	for(List<Integer> list: pheromoneUpdateList()){
 	    		currentGraph.incrementPheromone(list.get(0), list.get(1),list.get(2));
 	    		currentGraph.incrementPheromone(list.get(1), list.get(0),list.get(2));
 
 	    	}
 	    }
 	    
 	    private void pheromoneDecreament(){
 	    	currentGraph.decrementPheromone(pheromoneDecrease());
 	    }
 
 		@Override
 		public Map<Path, Integer> frequencyMap() {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public Path minPath() {
 			return Values.path(bestPath(), bestDistance());
 		}
 }
