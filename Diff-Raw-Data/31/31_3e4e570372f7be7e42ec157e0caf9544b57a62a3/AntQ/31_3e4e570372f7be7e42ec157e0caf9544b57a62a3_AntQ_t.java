 /**
  * Class that implements the Ant Q algorithm.
  *
  * The constants are the initialization parameters of the algorithm.
  * They are used to calculate the action choice value (delta and beta), 
  * to update the AQ Value (alfa and gamma), to make a exploration or a exploitation choice (q0) 
  * and to calculate the reinforcement learning value (w).
  *
  * The cities array stores all the cities of the instance.
  *
  * The edges matrix stores all the edges of the instance (complete graph).
  * 
  * The actionChoices matrix stores the action choices of all edges.
  *
  * The agents array stores the agents, or ants, that are acting in the algorithm.  
  *
  * @author Matheus Paixao
  */
 public class AntQ {
    private static String problem = "";
    private static double[][] times;
 
 
    //constant initialization parameters
    private static final double delta = 1;
    private static final double beta = 2;
    private static final double alfa = 0.1;
    private static final double gamma = 0.3;
    private static final double q0 = 0.9;
    private static final double w = 10.0;
 
    private static City cities[];
 
    private static Edge edges[][];
 
    public static double actionChoices[][];
 
    private static Agent agents[];
 
    /**
     * Main method, where the Ant Q algorithm is runned.
     *
     * totalIterations and iterationsCounter are used to stop the algorithm after some iterations.
     * iterationBestTour array is the best tour of all agents in a iteration.
     * globalBestTour array is the best tour of all agents in all iterations.
     *
     * @author Matheus Paixao
     * @param args[0] the number of iterations (optional)
     * @see init
     * @see chooseNextCity in Agent class
     * @see setNextCity in Agent class
     * @see getNextCity in Agent class
     * @see addCityToTour in Agent class
     * @see addInitialCityToCitiesToVisit in Agent class
     * @see getInitialCity in Agent class
     * @see getLastTourEdge in Agent class
     * @see getMaxAQValue
     * @see updateAQValue
     * @see loadCitiesToVisit in Agent class
     * @see setCurrentCity in Agent class
     * @see removeCityFromCitiesToVisit in Agent class
     * @see getIterationBestTour
     * @see clearTour in Agent class
     * @see updateReinforcementLearningValue
     * @see clearReinforcementLearningValue
     * @see calculateTourValue
     */
    public static void main(String[] args){
       //algorithm variables:
 
       int totalIterations = 0; //number of iterations to run
       int iterationsCounter = 0;
       
       //time variables
       double initialTime = 0;
       double finalTime = 0;
       double iterationTime = 0;
       double averageIterationTime = 0;
 
       Agent agent = null;
       City nextCity = null;
       double reinforcementLearningValue = 0;
 
       Edge iterationBestTour[] = null; 
       double iterationBestTourValue = 0;
       Edge globalBestTour[] = null; 
       int hereCounter = 0;
 
       if(args.length > 0){
          totalIterations = Integer.parseInt(args[0]); //the user can pass the number of iterations
 
          if(args.length > 1){
             problem = args[1];
          }
       }
       else{
          totalIterations = 200;
       }
 
       //initialization of the algorithm
       init();
 
       while(iterationsCounter <= totalIterations){
          initialTime = System.currentTimeMillis();
          if(!problem.equals("jssp")){
             //update the action choices of all edges
             updateActionChoices();
          }
          //in this step all the agents chooses the next city to move to
          //when all the agents have choosen the next city, they update the AQ value of the correspondent edge 
          for(int i = 0; i <= cities.length - 1; i++){
             //if the agent didn't visit all the cities yet
             if(i != cities.length - 1){
                for(int j = 0; j <= agents.length - 1; j++){
                   agent = agents[j];
                   nextCity = agent.chooseNextCity();
                   agent.setNextCity(nextCity);
                   agent.addCityToTour(agent.getNextCity());
 
                   //if the agent has choosen the last city to visit
                   if(i == cities.length - 2){
                      agent.addInitialCityToCitiesToVisit();
                   }
                }
             }
             //all the agents go back to their initial city
             else{
                for(int j = 0; j <= agents.length - 1; j++){
                   agent = agents[j];
                   nextCity = agent.getInitialCity();
                   agent.setNextCity(nextCity);
                   agent.addCityToTour(agent.getNextCity());
                }
             }
 
             //all the agents update the AQ value of the last edge added to their tour
             for(int j = 0; j <= agents.length - 1; j++){
                agent = agents[j];
                updateAQValue(agent.getLastTourEdge(), 0, getMaxAQValue(agent.getCitiesToVisit(), agent.getNextCity()));
 
                //if the agents has done the tour
                if(i == cities.length - 1){
                   agent.loadCitiesToVisit(); //prepare the cities to visit array for another tour
                }
 
                agent.setCurrentCity(agent.getNextCity()); //move to the next choosed city
                agent.removeCityFromCitiesToVisit(agent.getCurrentCity()); // remove the current city from the cities to visit
             }
          }
 
          iterationBestTour = getIterationBestTour();
          iterationBestTourValue = calculateTourValue(iterationBestTour);
 
          //all the agents clear their tours
          for(int i = 0; i <= agents.length - 1; i++){
             agents[i].clearTour();
          }
 
          //in this step is calculated the reinforcement learning value and is updated the AQ value only 
          //the edges belonging to the iterationBestTour
          reinforcementLearningValue = w / iterationBestTourValue;
          for(int i = 0; i <= iterationBestTour.length - 1; i++){
             updateAQValue(iterationBestTour[i], reinforcementLearningValue, 0);
          }
 
          if(iterationsCounter == 0){
             globalBestTour = iterationBestTour;
          }
          else{
             if(iterationBestTourValue < calculateTourValue(globalBestTour)){
                System.out.println("found best tour");
                hereCounter++;
                globalBestTour = iterationBestTour;
             }
          }
 
          finalTime = System.currentTimeMillis();
          iterationTime = finalTime - initialTime;
          System.out.println("time of iteration "+iterationsCounter + ": "+iterationTime);
          averageIterationTime += iterationTime;
          iterationsCounter++;
       }
       //System.out.println("here counter: " + hereCounter);
       System.out.println("Average time of iterations: " + averageIterationTime / iterationsCounter);
       System.out.println("Best tour value: " + calculateTourValue(globalBestTour));
       System.exit(0);
    }
    
    public static double getQ0(){
       return q0;
    }
 
    public static City[] getCities(){
       return cities;
    }
 
    public static Edge[][] getEdges(){
       return edges;
    }
 
    public static double getGamma(){
       return gamma;
    }
 
    /**
     * Method to initialize the algorithm.
     *
     * @author Matheus Paixao
     * @see getCitiesList in InstanceReader class
     * @see createEdges
     * @see initAQValues
     * @see getAQ0
     * @see initAgents
     */
    private static void init(){
       InstanceReader instanceReader = new InstanceReader();
 
       if(problem.equals("jssp")){
          createJSSPEdges(instanceReader.getNumberOfJobs());
          times = instanceReader.getTimesMatrix();
         initAQValues(0.01);
       }
       else{
          String instanceType = instanceReader.getInstanceType();
          //System.out.println("memory before edges");
          //printUsedMemory();
          if(instanceType == "coordinates"){
             createCartesianCoordinatesEdges(instanceReader.getCitiesList());
          }
          else if(instanceType == "matrix"){
             createMatrixEdges(instanceReader.getEdgesValuesMatrix());
          }
          //System.out.println("memory after edges");
          //printUsedMemory();
 
          actionChoices = new double[edges.length][edges.length];
          initAQValues(getAQ0());
       }
 
       initAgents();
    }
 
    private static void createJSSPEdges(int numberOfJobs){
       cities = new City[numberOfJobs];
 
       for(int i = 0; i <= cities.length - 1; i++){
          cities[i] = new City(i);
       }
 
       edges = new Edge[cities.length][cities.length];
       for(int i = 0; i <= cities.length - 1; i++){
          for(int j = 0; j <= cities.length - 1; j++){
             edges[i][j] = new Edge(cities[i], cities[j]);
          }
       }
    }
 
    /**
     * Method to create the edges from each city to each city.
     *
     * It's a complete graph.
     * @author Matheus Paixao
     */
    private static void createCartesianCoordinatesEdges(City citiesList[]){
       cities = citiesList;
       edges = new Edge[cities.length][cities.length];
 
       for(int i = 0; i <= cities.length - 1; i++){
          System.out.println("line "+i);
          for(int j = 0; j <= cities.length - 1; j++){
             edges[i][j] = new Edge(cities[i], cities[j]);
          }
       }
    }
 
    private static void createMatrixEdges(double edgesValuesMatrix[][]){
       cities = new City[edgesValuesMatrix.length];
 
       for(int i = 0; i <= edgesValuesMatrix.length - 1; i++){
          cities[i] = new City(i);
       }
 
       edges = new Edge[cities.length][cities.length];
       for(int i = 0; i <= cities.length - 1; i++){
          for(int j = 0; j <= cities.length - 1; j++){
             edges[i][j] = new Edge(cities[i], cities[j], edgesValuesMatrix[i][j]);
          }
       }
    }
 
    /**
     * Method to get the initial AQ value for all edges.
     *
     * The initial AQ value is composed by the average value of the edges and the number of cities.
     * @author Matheus Paixao
     * @return the initial AQ value for all edges.
     * @see getEdgeValue in Edge class
     * @see getNumberOfEdges
     */
    private static double getAQ0(){
       double sumOfEdges = 0;
       double averageValueOfEdges = 0;
 
       for(int i = 0; i <= edges.length - 1; i++){
          for(int j = 0; j <= edges.length - 1; j++){
             sumOfEdges += edges[i][j].getEdgeValue();
          }
       }
       averageValueOfEdges = sumOfEdges / getNumberOfEdges();
       
       return 1 / (averageValueOfEdges * cities.length);
    }
 
    /**
     * Method to get the number of existing edges.
     *
     * When 'i' is equal to 'j' there is no edge.
     * @author Matheus Paixao
     * @return the number of existing edges.
     */
    private static int getNumberOfEdges(){
       int numberOfEdges = 0;
 
       for(int i = 0; i <= cities.length - 1; i++){
          for(int j = 0; j <= cities.length - 1; j++){
             if(i != j){
                numberOfEdges++;
             }
          }
       }
 
       return numberOfEdges;
    }
 
    /**
     * Method to set the initial AQ value for each edge.
     *
     * When 'i' is equal to 'j' there is no edge, so the initial AQ value is 0.
     * @author Matheus Paixao
     * @param AQ0 the initial AQ value for all edges.
     * @see setAQValue in Edge class.
     */
    private static void initAQValues(double AQ0){
       for(int i = 0; i <= edges.length - 1; i++){
          for(int j = 0; j <= edges.length - 1; j++){
             if(i == j){
                edges[i][j].setAQValue(0);
             }
             else{
                edges[i][j].setAQValue(AQ0);
             }
          }
       }
    }
 
    /**
     * Method to init the agents, or the ants.
     *
     * One agent is put in each city of the instance.
     * @author Matheus Paixao
     * @see Agent constructor in Agent class.
     */
    private static void initAgents(){
       agents = new Agent[cities.length]; 
       //agents = new Agent[1]; 
 
       //System.out.println("memory before agents");
       //printUsedMemory();
       for(int i = 0; i <= agents.length - 1; i++){
          agents[i] = new Agent(cities[i]);
       }
       //System.out.println("memory after agents");
       //printUsedMemory();
    }
 
    /**
     * Method to update the action choices of all edges.
     *
     * Uses the edges of the edges matrix.
     * When the action choice value is too low (Not a Number or Infinity) is considered equal to 0.
     * @author Matheus Paixao
     * @see Math.pow
     */
    private static void updateActionChoices(){
       Edge edge =  null;
       double actionChoice = 0;
 
       for(int i = 0; i <= actionChoices.length - 1; i++){
          for(int j = 0; j <= actionChoices.length - 1; j++){
             edge = edges[i][j];
             if(i != j){
                actionChoice =  Math.pow(edge.getAQValue(), delta) * Math.pow(edge.getEdgeHeuristicValue(), beta);
 
                if((Double.isNaN(actionChoice)) || (Double.POSITIVE_INFINITY == actionChoice) || (Double.NEGATIVE_INFINITY == actionChoice)){
                   actionChoice = 0;
                }
 
                actionChoices[i][j] = actionChoice;
             }
          }
       }
    }
 
    /**
     * Method to get the action choice of an edge.
     *
     * @author Matheus Paixao
     * @param city1 the first city of the edge 
     * @param city2 the second city of the edge 
     * @return the action choice of the edge
     */
    public static double getActionChoice(City city1, City city2, Agent agent){
       double actionChoice = 0;
 
       if(problem.equals("jssp")){
          int[] nodes = getNodes(city1, city2, agent);
         double heuristicValue = 1 / getMakespan(nodes);

         Edge edge = edges[city1.getIndex()][city2.getIndex()];
         actionChoice =  Math.pow(edge.getAQValue(), delta) * Math.pow(heuristicValue, beta);
 
          if(Double.isNaN(actionChoice) || actionChoice == Double.POSITIVE_INFINITY || actionChoice == Double.NEGATIVE_INFINITY){
             actionChoice = 0;
          }
       }
       else{
          actionChoice = actionChoices[city1.getIndex()][city2.getIndex()];
       }
 
       return actionChoice;
    }
 
    private static int getNumberOfNodes(Edge[] tour){
       int nodesCounter = 0;
 
       for(int i = 0; i <= tour.length - 1; i++){
          if(tour[i] != null){
             nodesCounter++;
          }
       }
 
       return nodesCounter + 2;
    }
 
    private static int[] getNodes(City city1, City city2, Agent agent){
       int[] nodes;
 
       Edge[] tour = agent.getTour();
       nodes = new int[getNumberOfNodes(tour)];
 
       if(tour[0] == null){
          nodes[0] = city1.getIndex();
          nodes[1] = city2.getIndex();
       }
       else{
          for(int i = 0; i <= tour.length - 1; i++){
             if(tour[i] != null){
                nodes[i] = tour[i].getCity1().getIndex();
                nodes[i+1] = tour[i].getCity2().getIndex();
             }
          }
          nodes[nodes.length - 1] = city2.getIndex();
       }
 
       return nodes;
    }
 
    private static double getMakespan(int[] nodes){
       double[] makespan = new double[times[0].length];
       int job = 0;
 
       for(int i = 0; i <= nodes.length - 1; i++){
          job = nodes[i];
          makespan[0] = makespan[0] + times[job][0];
          for(int j = 1; j <= times[0].length - 1; j++){
             if(makespan[j] > makespan[j - 1]){
                makespan[j] = makespan[j] + times[job][j];
             }
             else{
                makespan[j] = makespan[j - 1] + times[job][j];
             }
          }
       }
 
       return makespan[times[0].length - 1];
    }
 
    /**
     * Method to get the sum of action choices of all remaining cities to visit of an agent.
     *
     * @author Matheus Paixao
     * @return the sum of action choices of all remaining cities to visit of an agent.
     * @see getActionChoice
     */
    public static double getActionChoiceSum(City currentCity, City citiesToVisit[], Agent agent){
       double actionChoiceSum = 0;
 
       for(int i = 0; i <= citiesToVisit.length - 1; i++){
          if(citiesToVisit[i] != null){
             actionChoiceSum += getActionChoice(currentCity, citiesToVisit[i], agent);
          }
       }
 
       return actionChoiceSum;
    }
 
    /**
     * Method to get the max AQ value of the next choosed city.
     *
     * The method evaluates the AQ values of all the edges from the next choosed city
     * to all the cities that the agent didn't visit yet.
     * @author Matheus Paixao
     * @param citiesToVisit array of the cities to be visited by the agent
     * @param nextCity the next choosed city
     * @return the max AQ value of the next choosed city.
     * @see equals method in City class.
     * @see getAQValue method in Edge class.
     */
    public static double getMaxAQValue(City citiesToVisit[], City nextCity){
       double maxAQValue = 0;
       double edgeAQValue = 0;
       int nextCityIndex = nextCity.getIndex();
 
       for(int i = 0; i <= citiesToVisit.length - 1; i++){
          //only evaluate the city if the agent didn't visit it yet and it is different of the next choosed city
          if((citiesToVisit[i] != null) && (!nextCity.equals(citiesToVisit[i]))){
             edgeAQValue = edges[nextCityIndex][citiesToVisit[i].getIndex()].getAQValue();
             if(edgeAQValue > maxAQValue){
                maxAQValue = edgeAQValue;
             }
          }
       }
 
       return maxAQValue;
    }
 
    /**
     * Method to update the AQ value of the passed edge.
     *
     * To update the AQ value of an edge, it's used the reinforcement learning value of the edge
     * and the max AQ value of the next choosed city.
     * @author Matheus Paixao
     * @param edge the edge to update.
     * @param reinforcementLearningValue the reinforcement learning value of the edge.
     * @param maxAQValue the max AQ value of the next choosed city.
     * @see getAQValue in Edge class.
     * @see getReinforcementLearningValue in Edge class.
     * @see setAQValue in Edge class.
     */
    private static void updateAQValue(Edge edge, double reinforcementLearningValue, double maxAQValue){
       int city1Index = edge.getCity1().getIndex();
       int city2Index = edge.getCity2().getIndex();
       Edge edgeToUpdate = edges[city1Index][city2Index];
 
       edgeToUpdate.setAQValue((1 - alfa) * edgeToUpdate.getAQValue() + alfa * (reinforcementLearningValue + gamma * maxAQValue));
    }
 
    /**
     * Method to get the best tour, of all agents, of an iteration.
     *
     * It's used an auxiliary array to create a new array with the same elements. 
     * @author Matheus Paixao
     * @return the iteration best tour
     * @see getTour in Agent class
     * @see calculateTourValue
     */
    private static Edge[] getIterationBestTour(){
       Edge iterationBestTourTemp[] = agents[0].getTour();
       Edge iterationBestTour[] = new Edge[iterationBestTourTemp.length];
       Edge tour[] = null;
       double iterationBestTourValue = calculateTourValue(iterationBestTourTemp);
       double tourValue = 0;
 
       for(int i = 0; i <= agents.length - 1; i++){
          tour = agents[i].getTour();
          tourValue = calculateTourValue(tour);
          if(calculateTourValue(tour) < iterationBestTourValue){
             iterationBestTourValue = tourValue;
             iterationBestTourTemp = tour;
          }
       }
 
       for(int i = 0; i <= iterationBestTourTemp.length - 1; i++){
          iterationBestTour[i] = iterationBestTourTemp[i];
       }
 
       return iterationBestTour;
    }
 
    /**
     * Method to calculate the value of a tour.
     *
     * @author Matheus Paixao
     * @param tour the tour to calculate the value
     * @return the value of the tour
     * @see getEdgeValue in Edge class
     */
    private static double calculateTourValue(Edge[] tour){
       double tourValue = 0;
 
      if(problem.equals("jssp")){
         int[] nodes = new int[tour.length];

         for(int i = 0; i <= nodes.length - 1; i++){
            nodes[i] = tour[i].getCity1().getIndex();
         }

         tourValue = getMakespan(nodes);
      }
      else{
         for(int i = 0; i <= tour.length - 1; i++){
            tourValue += tour[i].getEdgeValue();
         }
       }
 
       return tourValue;
    }
 
    private static void printUsedMemory(){
       double mb = 1024 * 1024;
       Runtime runtime = Runtime.getRuntime();
 
       System.out.println("used memory: "+(runtime.totalMemory() - runtime.freeMemory()) / mb + " MegaBytes");
       System.out.println("==========================================================");
    }
 }
