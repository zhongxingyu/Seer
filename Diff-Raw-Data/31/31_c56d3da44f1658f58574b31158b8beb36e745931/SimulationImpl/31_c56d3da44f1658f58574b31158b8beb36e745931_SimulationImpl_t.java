 package adp2.implementations;
 
 import adp2.app.Main;
 import java.util.*;
 
 import static adp2.implementations.Values.*;
 import adp2.interfaces.*;
 
 public class SimulationImpl implements Simulation {
     
     boolean antsSpecifiedNodes = false;
     boolean antsRandomNodes = false;
     List<Integer> antsSpecifiedNodesToAdd = new ArrayList<Integer>();
     
     private Graph currentGraph;
     private List<Graph> graphStates;
     private List<Ant> antList = new ArrayList<Ant>();
     private List<PheromoneElement> pheromoneUpdateList = new ArrayList<PheromoneElement>();
 	
     private int antQuantity;
     private int antsLaunched = 0;
     private int antsPerStep = 0;             //Number of ants added per step
 
     private double pheromoneDecrease = 1.0; // was 1.0 in code from group 2
     private double pheromoneIntensity = 10.0; // was 10.0 in code from group 2
     // The Simulation should not log the states of all Graphs by default
     final boolean logStates;
 	
     private double bestDistance = Double.MAX_VALUE;
     public List<Integer> bestPath = new ArrayList<Integer>();
 	
     private int DELAYEDUPDATE = 1;
     private int STEPBYSTEPUPDATE = 2;
 	private double antAlpha = 0.6;
 
     //Start all ants at once
     protected static Simulation create(Graph graph, int antsQuantity) {
         return new SimulationImpl(graph, antsQuantity);
     }
 
     //Start ants in waves of specified size
     protected static Simulation create(Graph graph, int antsQuantity, int antsPerStep) {
         return new SimulationImpl(graph, antsQuantity, antsPerStep);
     }
 
     //Start all ants at once
     // Log States
     protected static Simulation create(Graph graph, int antsQuantity, boolean logStates) {
         return new SimulationImpl(graph, antsQuantity, logStates);
     }
 
     //Start ants in waves of specified size
     // Log States
     protected static Simulation create(Graph graph, int antsQuantity, int antsPerStep, boolean logStates) {
         return new SimulationImpl(graph, antsQuantity, antsPerStep, logStates);
     }
     
     //Start ants in waves of specified size, ants with random startNodes
     //Log States
     protected static Simulation create(Graph graph, int antQuantity, int antsPerStep, boolean addAntsRandom, boolean logStates) {
         return new SimulationImpl(graph, antQuantity, antsPerStep, addAntsRandom, logStates);
     }
     
     //Start ants in waves of specified size, ants with specified startNodes
     //Log States
     protected static Simulation create(Graph graph, int antQuantity, int antsPerStep, boolean addAntsSpecifiedNodes, List<Integer> specifiedNodesToAdd, boolean logStates) {
         return new SimulationImpl(graph, antQuantity, antsPerStep, addAntsSpecifiedNodes, specifiedNodesToAdd, logStates);
     }
 	
         
     private SimulationImpl(Graph graph, int antsQuantity) {
         setGraph(graph);
         setAntQuantity(antsQuantity);
         addAnts(antsQuantity);
         graphStates = new ArrayList<Graph>();
         logStates = false;
     }
     
     private SimulationImpl(Graph graph, int antQuantity, int antsPerStep, boolean addAntsSpecifiedNodes, List<Integer> specifiedNodesToAdd, boolean logStates) {
         antsSpecifiedNodes = addAntsSpecifiedNodes;
         antsSpecifiedNodesToAdd = specifiedNodesToAdd;
         setGraph(graph);
         setAntQuantity(antQuantity);
         setAntsPerStep(antsPerStep);
         graphStates = new ArrayList<Graph>();
         this.logStates = logStates;
     }
     private SimulationImpl(Graph graph, int antQuantity, int antsPerStep, boolean addAntsRandom, boolean logStates) {
         antsRandomNodes = addAntsRandom;
         setGraph(graph);
         setAntQuantity(antQuantity);
         setAntsPerStep(antsPerStep);
         graphStates = new ArrayList<Graph>();
         this.logStates = logStates;
     }
 
 
     private SimulationImpl(Graph graph, int antQuantity, int antsPerStep) {
         setGraph(graph);
         setAntQuantity(antQuantity);
         setAntsPerStep(antsPerStep);
         graphStates = new ArrayList<Graph>();
         logStates = false;
     }
 
     private SimulationImpl(Graph graph, int antQuantity, boolean logStates) {
         setGraph(graph);
         setAntQuantity(antQuantity);
         addAnts(antQuantity);
         graphStates = new ArrayList<Graph>();
         this.logStates = logStates;
     }
 
     private SimulationImpl(Graph graph, int antQuantity, int antsPerStep, boolean logStates) {
         setGraph(graph);
         setAntQuantity(antQuantity);
         setAntsPerStep(antsPerStep);
         graphStates = new ArrayList<Graph>();
         this.logStates = logStates;
     }
 	
 	private SimulationImpl(Graph graph, int antQuantity, int antsPerStep, double antAlpha) {
         setGraph(graph);
         setAntQuantity(antQuantity);
         setAntsPerStep(antsPerStep);
         graphStates = new ArrayList<Graph>();
 		this.antAlpha = antAlpha;
         this.logStates = false;
     }
 
     public void run() {
         while (simulate(DELAYEDUPDATE)) {
 			Main.logger.info("Best path: " + this.bestPath() + " Shortest way: " + this.bestDistance());
         }
     }
 
     public void runForSteps(int simulationSteps) {
         int loops = 0;
         while (simulate(STEPBYSTEPUPDATE) && loops < simulationSteps) {
 			Main.logger.info("Best path: " + this.bestPath() + " Shortest way: " + this.bestDistance());
             loops++;
         }
     }
 
     public void runForSeconds(int runtimeInS) {
         long timeStart = System.currentTimeMillis();
         long timeStop = runtimeInS * 1000;
         while (simulate(DELAYEDUPDATE) && System.currentTimeMillis() - timeStart < timeStop) {
 			Main.logger.info("Best path: " + this.bestPath() + " Shortest way: " + this.bestDistance());
         }
     }
 
     private boolean simulate(int method) {
 		Main.logger.info("Ant count " + antList.size());
         /*
          * antList = List of ants currently traversing the graph
          * antsPerStep = Number of ants starting each step (if given)
          * antQuantity = total ants to traverse the graph
          */
 
     	/*
     	// pseudocode aus der aufgabe
 		repeat 
 		    if antCount < maxAnts then
 		        create a new ant
 		        set initial state
 		    end if 
 		    for all ants do
 		       determine all feasible neighbor states  
 		       if solution found or no feasible neighbor state then
 		          kill ant 
 		          if we use delayed pheromone update then
 		             evaluate solution
 		             deposit pheromone on all used edges according to a a strategy
 		          end if 
 		      else
 		          stochastically select a feasible neighbor state  
 		          if we use step-by-step pheromone update then
 		             deposit pheromone on the used edge
 		          end if 
 		      end if
 		   end for
 		   evaporate pheromone 
 		until termination criterion satisfied
     	 */
 
     	//PC: if antCount < maxAnts then
         if ((antsPerStep() != 0) && (antsLaunched < antQuantity())) {
             if ((antsLaunched + antsPerStep()) <= antQuantity()) {
                 // Increase ants by antsPerStep
                 antsLaunched += antsPerStep();
                 //PC: create a new ant
                 //PC: set initial state
                 addAnts(antsPerStep());
 				Main.logger.fine(antsPerStep() + " ants created");
             } else if (antsLaunched < antQuantity()) {
                 // Increases the number of ants to the maximum if the current step would go beyond the limit
                 antsLaunched += antQuantity() - antList().size();
                 //PC: create a new ant
                 //PC: set initial state
                 addAnts(antQuantity() - antList().size());
 				Main.logger.fine(antQuantity() - antList().size() + " ants created, maximum reached");
             }
         }
         //PC: for all ants do
         int i = 0;
         while (i < antList().size()) {
             Ant ant = antList().get(i);
             //PC: if solution found or no feasible neighbor state then
             if (ant.hasFinished()) {
                 //PC: kill ant 
 				Main.logger.fine(antList.get(i).toString() +" removed");
                 removeAnt(i);
                 //PC: evaluate solution
                 // algorithm gets here for every finished ant, no matter if delayed or step by step pheromone update!
                 if (ant.traveledPath().waypoints().size() == (currentGraph.allNodes().size() + 1) && ant.pathLength() < bestDistance()) {
 					Main.logger.info(ant.toString() + " update distance from " + this.bestDistance() + " to " + ant.pathLength() + " and path from " + this.bestPath() + " to " + ant.traveledPath().waypoints());
                     setBestDistance(ant.pathLength());
                     setBestPath(ant.traveledPath().waypoints());
 	                //PC: if we use delayed pheromone update then
 	                if (method == DELAYEDUPDATE) {
 	                	//PC: deposit pheromone on all used edges according to a a strategy
 	                    // strategy is: "if better way was found". given due to solution evaluation above.
 	                    List<Integer> waypoints = ant.traveledPath().waypoints();
 	                    for (int j = 1; j < waypoints.size(); j++) {
 	                    	addPheromoneUpdate(waypoints.get(j-1), waypoints.get(j), pheromoneIntensity()); // Prepare pheromone updates
 	                    }
 	                    // TODO: strategy part 2: optional (?) pheromone boost according to path quality (means: not just better then before, but differentiate: bit better -> boost 1, lots better -> boost 2, ...)
 	                    pheromoneIncrement();
 	                }
                 }
             } else {
             	//PC: stochastically select a feasible neighbor state 
                 stochasticNeighborSelection(ant); // Choose next node & move there
 //            	ant.step(); // Choose next node & move there
                 //PC: if we use step-by-step pheromone update then
                 if (method == STEPBYSTEPUPDATE) {
                 	//PC: deposit pheromone on the used edge
                 	addPheromoneUpdate(ant.prevPosition(), ant.position(), pheromoneIntensity()); // Prepare pheromone updates
                 	pheromoneIncrement();
                 	// fehlt theoretisch: finished? dann: ant routing table updaten -> setbestpath + setbestdistance
                 }
                 i++;
             }
         }
 
         //PC: evaporate pheromone 
         pheromoneDecrement();
         if (logStates) {
             graphStates.add(currentGraph);
             /*
              * For every Step this adds the current Graph to the list of
              * graphStates. Doing so gives us the ability to replay the Graph
              * transformations.
              */
             currentGraph = currentGraph.deepClone();
         }
         // Ends if all ants have traversed the graph
         return !(antList().isEmpty() && (antsLaunched == antQuantity()));
     }
 
     private void stochasticNeighborSelection(Ant ant) {
         if (ant.hasFinished()) {
             Main.logger.warning("Ant dead!");
         } else if (!ant.hasFinished()) {
             if (ant.getUnvisitedNodes().isEmpty()) {
             	ant.getUnvisitedNodes().add(ant.getPath().get(0));
             }
 
             // calculation of balances
             Map<Integer, Double> probabilities = ant.balances();
 
             if (probabilities.isEmpty()) {
                 ant.finish();
                 Main.logger.info(this.toString() + " fertig");
             } else {
 
                 /*
                  * Adds up the balance-values; the sum and single values
                  * are needed to calculate the probabilities in the next step
                  */
 //                double sum = sumOfValues(probabilities);
 
                 /*
                  * maps the neighbors to a value between 0 and 1
                  * Value = own probability + pre-calculated value
                  * Ex.:
                  * 
                  * Node                 Probability             Value
                  * 1					0.3                     0.3
                  * 2					0.1                     0.4
                  * 3					0.5						0.9
                  * 4					0.1						1.0
                  */
 //                 comment from group 3: why not use the probability? why does the highest node get the highest value?
 //                double predecessor = 0;
 //
 //                for (Map.Entry<Integer, Double> entry : probabilities.entrySet()) {
 //                    double probability = entry.getValue() / sum + predecessor;
 //                    if (probability > 1.0) {
 //                        probability = 1;
 //                    }
 //                    probabilities.put(entry.getKey(), probability);
 //                    predecessor = probability;
 //                }
 
                 /*
                  * Determines the random choice of a path by the ant using a random value (0.0~1.0)
                  * and the calculated probabilities
                  * ==> Chooses the ant's next node
                  * 
                  * You may want to set minNode to -1 for debugging purposes
                  */ 
                 int minNode = 1;
                 double minValue = 0;
 
                double ran = 0;
                 ran = Math.random(); 
                 for (Map.Entry<Integer, Double> e : probabilities.entrySet()) {
                     if (e.getValue() >= minValue && e.getValue() <= ran) {
                         minNode = e.getKey();
                         minValue = e.getValue();
                     }
                 }
                 // if random choice failed, go best way
                 if (minNode == 0) {
                     for (Map.Entry<Integer, Double> e : probabilities.entrySet()) {
                         if (e.getValue() >= minValue) {
                             minNode = e.getKey();
                             minValue = e.getValue();
                         }
                     }
                 }
 
                 Main.logger.fine("Random: " + ran + "; minValue: " + minValue + "; minNode: " + minNode);
 
                 // pathlength += graph.distance(position(), minNode);
                 ant.updatePathLength(minNode);
 
 				Main.logger.fine(ant.toString() + " moves from " + ant.getPath().get(ant.getPath().size()-1) + " to " + minNode);
                 ant.getPath().add(minNode);
                 ant.getUnvisitedNodes().remove(minNode);
 
                 if (ant.getUnvisitedNodes().isEmpty() && ant.getPath().get(ant.getPath().size() - 1) == ant.getPath().get(0)) {
                     ant.finish();
                     Main.logger.info(ant.toString() + " finished");
                 }
             }
         }
     }
 
     /*Getter and Setter*/
     private void setGraph(Graph graph) {
         this.currentGraph = graph;
     }
 
     private void setAntQuantity(int quantity) {
         antQuantity = quantity;
     }
 
     private int antQuantity() {
         return antQuantity;
     }
 
     private void setAntsPerStep(int quantity) {
         antsPerStep = quantity;
     }
 
     private int antsPerStep() {
         return antsPerStep;
     }
 
     private double bestDistance() {
         return bestDistance;
     }
 
     private void setBestDistance(double distance) {
         bestDistance = distance;
     }
 
     private List<Integer> bestPath() {
         return bestPath;
     }
 
     private void setBestPath(List<Integer> path) {
         bestPath = path;
     }
 
     private List<Ant> antList() {
         return antList;
     }
 
     private double antAlpha() {
         return antAlpha;
     }
 	
     private Graph graph() {
         return currentGraph;
     }
 
     private double pheromoneIntensity() {
         return pheromoneIntensity;
     }
 
     private double pheromoneDecrease() {
         return pheromoneDecrease;
     }
 
     private List<PheromoneElement> pheromoneUpdateList() {
         return pheromoneUpdateList;
     }
 
     private void addAnts(int quantity) {
         if(antsRandomNodes == false && antsSpecifiedNodes == false){
             for (int i = 1; i <= quantity; i++) {
                     antList().add(AntImpl.create(antAlpha(), graph()));
                     System.out.println(" - - - Ant -" + i + ": startet bei - " + antList().get(i-1).getPath());
             }
         }
         else if(antsSpecifiedNodes == true && this.antsSpecifiedNodesToAdd.size() >0){
             addAntsSpecified(quantity);
             
         }
         else if(antsRandomNodes==true){
             addAntsRandom(quantity);
         }
         else{
             antsSpecifiedNodes = false;
             addAnts(quantity);
         }
         
     }
     private void addAntsRandom(int quantity) {
         int nodeSize = this.currentGraph.allNodes().size();
         for (int i = 1; i <= quantity; i++) {
         	antList().add(AntImpl.create((int)(Math.random()*nodeSize)+1, antAlpha, currentGraph));
                 System.out.println(" - - - Ant -" + i + ": startet bei - " + antList().get(i-1).getPath());
         }
     }
     private void addAntsSpecified(int quantity){
         int startNode = 1;
         for (int i = 1; i <= quantity; i++) {
             startNode = this.antsSpecifiedNodesToAdd.get((i-1) % this.antsSpecifiedNodesToAdd.size());
             antList().add(AntImpl.create(startNode, antAlpha, currentGraph));
             System.out.println(" - - - Ant -" + i + ": startet bei - " + antList().get(i-1).getPath());
         }
     }
 
     private void removeAnt(int antIndex) {
         antList().remove(antIndex);
     }
 
     private void addPheromoneUpdate(int from, int to, double d) {
         PheromoneElement pheromoneElem = pheromoneElement(from, to, d);
         if (from != to) {
             pheromoneUpdateList().add(pheromoneElem);
         }
     }
 
     private void pheromoneIncrement() {
         for (PheromoneElement elem : pheromoneUpdateList()) {
 			Main.logger.finer("Edge " + elem.from() + "_" + elem.to() + " " + elem.pheromone());
             currentGraph.incrementPheromones(elem.from(), elem.to(), elem.pheromone());
         }
         pheromoneUpdateList().clear();
     }
 
     private void pheromoneDecrement() {
         currentGraph.decrementPheromones(pheromoneDecrease());
     }
 
     public Path minPath() {
         return Values.path(bestPath(), bestDistance());
     }
 	
     private double sumOfValues(Map<?, Double> m) {
         double sum = 0;
         for (Double elem : m.values()) {
             sum += elem;
         }
         return sum;
     }
 }
