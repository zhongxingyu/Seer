 import java.util.Random;
 import java.util.Arrays;
 
 /**
  * Class to describe the behavior of the agents, or ants, in the goal
  * to find best tours over the nodes.
  *
  * The initialCity variable stores the initial city of the agent. It's necessary when the agent 
  * finish its tour and has to go back to the beginning.
  *
  * The currentCity variable stores the current city of the agent. It's used in the state transition rule.
  *
  * Each agent has to know its nextCity to go before really go. It's used basically in AntQ class.
  *
  * The citiesToVisit array store the cities that the agent didn't visit yet. The null values represents the visited nodes.
  *
  * The tour array is the path, the sequency of nodes, done by the agent.
  *
  * @author Matheus Paixao
  */
 public class Agent {
    private City initialCity;
    private City currentCity;
    private City nextCity;
    private City citiesToVisit[];
    public Edge tour[];
 
    /**
     * Method to create an agent with its initial city.
     *
     * Create the citiesToVisit array with the same size of the cities array of AntQ.
     * Create the tour array with the same size of the citiesToVisit.
     * Fill the citiesToVisit array with City objects equals to the cities array of AntQ.
     * Set the initial city the current city and remove the initial city of the cities to be visited.
     * @author Matheus Paixao
     * @param initialCity the city that will be the initial city of the agent.
     * @see loadCitiesToVisit
     * @see removeCityFromCitiesToVisit
     */
    public Agent(City initialCity){
       this.citiesToVisit = new City[AntQ.getCities().length];
       tour = new Edge[getCitiesToVisit().length];
 
       loadCitiesToVisit();
 
       this.initialCity = initialCity;
       setCurrentCity(getInitialCity());
       removeCityFromCitiesToVisit(getInitialCity());
    }
 
    public City getInitialCity(){
       return this.initialCity;
    }
 
    public City getCurrentCity(){
       return this.currentCity;
    }
 
    public void setNextCity(City city){
       this.nextCity = citiesToVisit[city.getIndex()];
    }
 
    public City getNextCity(){
       return this.nextCity;
    }
 
    public City[] getCitiesToVisit(){
       return this.citiesToVisit;
    }
 
    public void setCurrentCity(City currentCity){
       this.currentCity = citiesToVisit[currentCity.getIndex()];
    }
 
    public Edge[] getTour(){
       return this.tour;
    }
 
    /**
     * Method to fill the citiesToVisit array with City objects that 
     * are equal to the cities from the AntQ algorithm.
     *
     * @author Matheus Paixao
     */
    public void loadCitiesToVisit(){
       City cities[] = AntQ.getCities();
 
       for(int i = 0; i <= cities.length - 1; i++){
          this.citiesToVisit[i] = cities[i];
       }
    }
 
    /**
     * Method to remove a city from cities to be visited.
     *
     * Get the index of the city in the cities array in AntQ and
     * set the correspondent city in citiesToVisit to null.
     * @author Matheus Paixao
     * @param city the city to be removed from citiesToVisit.
     */
    public void removeCityFromCitiesToVisit(City city){
       citiesToVisit[city.getIndex()] = null;
    }
 
    /**
     * Method to add the initial city to the cities to be visited.
     *
     * Get the index of the initial city of the agent in the cities array in AntQ
     * and set the correspondent position of the citiesToVisit with the initial city.
     * It's used when the agent have visited all the nodes and has to go back to the first one.
     * @author Matheus Paixao
     */
    public void addInitialCityToCitiesToVisit(){
       City initialCity = getInitialCity();
       citiesToVisit[initialCity.getIndex()] = initialCity;
    }
 
    /**
     * Method to add a new city to the tour.
     *
     * Insert an edge where the city 1 is the current city and the city 2 is the city to be added.
     * @author Matheus Paixao
     * @param city city to be added to the tour.
     * @see insertEdge
     */
    public void addCityToTour(City city){
       Edge[][] edges = AntQ.getEdges();
       insertEdge(edges[getCurrentCity().getIndex()][city.getIndex()]);
    }
    
    /**
     * Method to insert an edge to the agent tour.
     *
     * Insert an edge equal to the edge from the AntQ algorihtm. The edge is inserted in the last null position of the tour.
     * @author Matheus Paixao
     * @param edge the edge from the edges matrix of AntQ algorithm to be inserted in the agent's tour.
     * @see Edge constructor
     */
    private void insertEdge(Edge edge){
       for(int i = 0; i <= tour.length - 1; i++){
          if(tour[i] == null){
             tour[i] = new Edge(edge.getCity1(), edge.getCity2(), edge.getEdgeValue());
             break;
          }
       }
    }
 
    /**
     * Method to get the last edge added to the agent tour.
     *
     * @author Matheus Paixao
     * @return the last edge added to the agent tour.
     */
    public Edge getLastTourEdge(){
       Edge lastTourEdge = null;
 
       for(int i = tour.length - 1; i >= 0; i--){
          if(tour[i] != null){
             lastTourEdge = tour[i];
             break;
          }
       }
 
       return lastTourEdge;
    }
 
    /**
     * Method to clear the agent tour.
     *
     * It's used when an agent finish a tour (visit all cities) and has to start another one.
     * @author Matheus Paixao
     */
    public void clearTour(){
       for(int i = 0; i <= tour.length - 1; i ++){
          tour[i] = null;
       }
    }
 
    /**
     * Method that implements the AntQ transition rule.
     *
     * It's generated a random number q in the interval (0,1). Then q is compared 
     * with the initialization parameter q0, this test will define if the agent will
     * choose the best possible action (exploitation) or a random action (exploration).
     * The exploration choice can be done by two methods:
     * 1) pseudo-random
     * 2) pseudo-random-proportional
     * @author Matheus Paixao
     * @return the next city of a an agent
     * @see getRandomNumber
     * @see getMaxActionChoiceCity
     * @see getPseudoRandomProportionalCity
     * @see getPseudoRandomCity
     */
    public City chooseNextCity(){
       double q = getRandomNumber();
       City nextCity = null;
 
       if(q <= AntQ.getQ0()){
          //exploitation
          nextCity = getMaxActionChoiceCity();
       }
       else{
          //exploration
          //nextCity = getPseudoRandomCity(); //method 1
         nextCity = getPseudoRandomProportionalCity(); //method 2
       }
 
       return nextCity;
    }
 
    /**
     * Method to get a random number in the (0,1) interval.
     *
     * @author Matheus Paixao
     * @return a random number in the (0,1) interval
     * @see nextDouble method in Random class
     */
    private double getRandomNumber(){
       Random random = new Random();
       return random.nextDouble();
    }
 
    /**
     * Method to get the best possible city to go.
     *
     * How 'good' is an action is measured by it's action choice.
     * @author Matheus Paixao
     * @return the best possible city to go.
     * @see getFirstCityToVisit
     * @see getActionChoice
     */
    private City getMaxActionChoiceCity(){
       City maxActionChoiceCity = getFirstCityToVisit();
       City city = null;
 
       for(int i = 0; i <= citiesToVisit.length - 1; i++){
          if(citiesToVisit[i] != null){
             city = citiesToVisit[i];
             if(AntQ.getActionChoice(getCurrentCity(), city, this) > AntQ.getActionChoice(getCurrentCity(), maxActionChoiceCity, this)){
                maxActionChoiceCity = city;
             }
          }
       }
 
       return maxActionChoiceCity;
    }
 
    /**
     * Method to get the next agent city using the pseudo-random method.
     * 
     * In this method each possible city to go receive a random probability in (0,1) interval.
     * The max probability city is choosen.
     * @author Matheus Paixao
     * @return the next agent city using pseudo-random method.
     * @see getRandomNumber
     */
    private City getPseudoRandomCity(){
       double probabilities[] = new double[citiesToVisit.length];
       double maxProbability = 0;
       City maxProbabilityCity = null;
       
       for(int i = 0; i <= probabilities.length - 1; i++){
          if(citiesToVisit[i] != null){
             probabilities[i] = getRandomNumber();
             if(probabilities[i] > maxProbability){
                maxProbability = probabilities[i];
                maxProbabilityCity = citiesToVisit[i];
             }
          }
       }
 
       return maxProbabilityCity;
    }
 
    /**
     * Method to get the next city using the pseudo-random-proportional method.
     *
     * Each possible city to go has a pseudo random proportional probability
     * calculated in the getPseudoRandomProportionalProbabilities method.
     *
     * Then a roulette selection method is runned to select the next city.
     * @author Matheus Paixao
     * @return the next city using the pseudo-random-proportional method.
     * @see getPseudoRandomProportionalProbabilities
     * @see getRouletteValue
     */
    private City getPseudoRandomProportionalCity(){
       City city = null;
       double rouletteValue = 0;
       double probabilities[] = getPseudoRandomProportionalProbabilities();
 
       rouletteValue = getRouletteValue(probabilities);
 
       for(int i = 0; i <= probabilities.length - 1; i++){
          if(rouletteValue == probabilities[i]){
             city = citiesToVisit[i];
             break;
          }
       }
 
       return city;
    }
 
    /**
     * Method to calculate the pseudo random proportional probability of all the
     * cities to be visited by the agent.
     *
     * The pseudo random proportional probability of a city is calculated by 
     * the getPseudoRandomProportionalProbability method.
     * @author Matheus Paixao
     * @return an array containing the pseudo random proportional probability of the cities to visit.
     * @see getPseudoRandomProportionalProbability
     */
    private double[] getPseudoRandomProportionalProbabilities(){
       double probabilities[] = new double[citiesToVisit.length];
       double actionChoiceSum = AntQ.getActionChoiceSum(getCurrentCity(), citiesToVisit, this);
 
       for(int i = 0; i <= probabilities.length - 1; i++){
          if(citiesToVisit[i] != null){
             probabilities[i] = AntQ.getActionChoice(getCurrentCity(), citiesToVisit[i], this) / actionChoiceSum;
          }
          else{
             probabilities[i] = 0;
          }
       }
 
       return probabilities;
    }
 
    /**
     * Method to get the value of the probability selected by the roulette.
     *
     * Higher the probability of a city, higher the chance to be choosen by the roulette.
     * For more information search for "roulette selection method".
     * @author Matheus Paixao
     * @param probabilities an array containing the probabilities for roulette selection.
     * @return the probability value choosen by the roulette.
     * @see getRandomNumber
     */
    private double getRouletteValue(double[] probabilities){
       double[] rouletteProbabilities = new double[probabilities.length];
       double neddle = 0;
       double neddleChecker = 0;
       double rouletteValue = 0;
 
       for(int i = 0; i <= rouletteProbabilities.length - 1; i++){
          rouletteProbabilities[i] = probabilities[i];
       }
 
       Arrays.sort(rouletteProbabilities);
 
       neddle = getRandomNumber();
 
       for(int i = 0; i <= rouletteProbabilities.length - 1; i++){
          neddleChecker += rouletteProbabilities[i];
          if(neddleChecker >= neddle){
             rouletteValue = rouletteProbabilities[i];
             break;
          }
       }
 
       return rouletteValue;
    }
 
    /**
     * Method to get the first possible city to be visited by the agent.
     *
     * @author Matheus Paixao
     * @return the first possible city (not null) to go in the cities to be visited array
     */
    private City getFirstCityToVisit(){
       City firstCityToVisit = null;
 
       for(int i = 0; i <= citiesToVisit.length - 1; i++){
          if(citiesToVisit[i] != null){
             firstCityToVisit = citiesToVisit[i];
             break;
          }
       }
 
       return firstCityToVisit;
    }
 }
