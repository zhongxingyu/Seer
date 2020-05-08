 package isnork.g6;
 
 import isnork.sim.GameObject.Direction;
 import isnork.sim.Observation;
 import isnork.sim.Player;
 import isnork.sim.SeaLifePrototype;
 import isnork.sim.iSnorkMessage;
 
 import java.awt.geom.Point2D;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Set;
 
 
 public class NewPlayer extends Player {
 	
 	private LinkedList<Node> path;
 	public Strategy myStrategy;
 	private LinkedList<CreatureTracker> myTracker;
 	public iSnorkDecode decoder;
 	public LinkedList<Destination> possibleDestinations; //array of each players priority queue
 	public ArrayList<LinkedList<Destination>> priorityQueue;
 	public static final int turnAroundTimeAllowance = 7;
 	public static int dangerAvoidTravelTime = 0; // default for really happy maps
 //TODO: initialize dangerAvoidTravelTime when determining how dangerous the board is
 	public static final boolean DANGER = true;
 	public static final boolean HAPPY = false;
 	public enum Level {
 		NONE (0.0), LOW (1.0), MEDIUM (5.0), MAYBE (0.2), HIGH (10.0), OMG (20.0);
 		private final double factor;
 		Level(double factor){
 			this.factor = factor;
 		}
 		public double getFactor(){
 			return factor;
 		}
 	}
 	public enum Risk {
 		VERYBAD (2.0), BAD (0.5), EVEN (0.2), GOOD (0.5), VERYGOOD (2.0), NONE(0);
 		private final double factor;
 		Risk(double factor){
 			this.factor = factor;
 		}
 		public double getFactor(){
 			return factor;
 		}
 	}
 	static int d;
 	static int r;
 	static int n;
 	static Set<SeaLifePrototype> seaLifePossibilites;
 	static int penality;
 	int minutesLeft;
 	Point2D currentPosition;
 	Point2D destination;
 	Set<Observation> whatISee;
 	LinkedList<Node> currentPath;
 	public static int averageDanger;
 	
 	
 
 	@Override
 	public String getName() {
 		return "G6: SELECT from team_names WHERE attribute = 'clever'";
 	}
 	
 	@Override
 	public void newGame(Set<SeaLifePrototype> seaLifePossibilites, int penalty,
 			int d, int r, int n) {
 		NewPlayer.seaLifePossibilites = seaLifePossibilites;
 		NewPlayer.penality = penalty;
 		NewPlayer.d = d;
 		NewPlayer.r = r;
 		NewPlayer.n = n;
 		
 		/* Added average danger for Danger Avoidance -Eli */
 		int tiles = NewPlayer.d * NewPlayer.d + 1;
 		int totalMaxDanger = 0;
 		int totalMinDanger = 0;
 		for (SeaLifePrototype s: seaLifePossibilites){
 			if (s.isDangerous()){
 				totalMaxDanger += s.getHappiness()*s.getMaxCount();
 				totalMinDanger += s.getHappiness()*s.getMinCount();
 			}
 		}
 		averageDanger = (totalMaxDanger + totalMinDanger) / 2;
 		
 		minutesLeft = 8 * 60;
 		assignTravelTimeAllowance();
 		this.currentPath = new LinkedList<Node>();
 		destination = null;
 		myStrategy = new BalancedStrategy(seaLifePossibilites, penalty, d, r, n, this);
 		initializeTracker();
 		initializePriorityQueue();
 		decoder = new iSnorkDecode(seaLifePossibilites);
 		if (getId() == 0)
 		{
 			initializeCoordinates();
 			//print out the dictionary
 			decoder.printDecodedList();
 		}
 		//int dangerIndex = getBoardDanger();
 		//myStrategy = decideStrategy(seaLifePossibilites, penality, d, r, n);
 	}
 	
 	/* changed so that the tracker representing the current player has the player object set to this */
 	/* this change enables not counting creatures as seen when they are seen from the boat */
 	private void initializeTracker() {
 		myTracker = new LinkedList<CreatureTracker>();
 		for(int i = 0; i < n; i++)
 		{
 			CreatureTracker newCT = new CreatureTracker(i);
 			if (i == this.getId() * -1)
 			{
 				newCT.setPlayer(this);
 			}
 			myTracker.add(newCT);
 		}
 
 	}
 	
 	private void initializePriorityQueue() {
 //		for (int i = 0; i < n; i ++)
 			 //each player gets their own priority queue
 	//		possibleDestinations[i] = (LinkedList<Destination>)new LinkedList<Destination>();
 		priorityQueue = new ArrayList<LinkedList<Destination>>();
 		for (int i = 0; i < n; i ++) {
 			possibleDestinations = new LinkedList<Destination>();	
 			priorityQueue.add(possibleDestinations);
 		}
 	}
 	
 	private void initializeCoordinates()
 	{
 		CoordinateCalculator.initCoordinateCalculator(d, r, n);
 		CoordinateCalculator.updateCoordMap();
 //		CoordinateCalculator.printCoords();
 //		CoordinateCalculator.updateCoordMap(CoordinateCalculator.nextIteration);
 		//CoordinateCalculator.printCoords();
 	}
 
 	public CreatureTracker getWhatPlayerSaw(int id) {
 		//make id positive
 		if(id < 0) id *= -1;
 		for(int i = 0; i < myTracker.size();  i++) {
 			CreatureTracker creatureTracker = myTracker.get(i);
 			if(creatureTracker.getPlayerID() == id)
 				return creatureTracker;
 		}
 		
 		System.out.print("ERROR: Diver's tracker could not be found");
 		return null;
 	}
 	 
 	private void updatePlayerTracker() {
 		
 		//**uncomment lines for debuggin and printing tracker list to the console
 		CreatureTracker creatureTracker = getWhatPlayerSaw(getId());
 		int id = getId(); //make positive
 		//creatureTracker.printListOfSeenCreatures();
 		for(Object obj : whatISee) {
 			Observation creature = (Observation)obj;
 			if(!creatureTracker.didSeeCreature(creature.getName())) {
 				creatureTracker.addToTracker(creature);
 				//System.out.print("Player " + id + " First time seeing: " + creature.getName() + "\n");
 			} else {
 				//add id to the list of seen creature
 				creatureTracker.seeCreature(creature);
 				//System.out.print("Player " + id + " Already saw: " + creature.getName()+ "\n");
 			}
 		}
 		
 	}
 	
 	
 	
 	private boolean shouldSendMessage(Observation creature) {
 		//check if what player sees if worth sending a message
 		//only send message if its high priority
 		if(decoder.isCreatureInDecoder(creature.getName())) {
 			//send message
 			//System.out.print("Player " + getId() + " sent message for seeing " + creature.getName() + "\n");
 			return true;
 		}
 		return false;
 	}
 	
 	private boolean isDestinationInQueue(Destination dest, int id) {
 		id *= -1;
 		for(int i = 0; i < priorityQueue.get(id).size(); i++) {
 			if(priorityQueue.get(id).get(i)== dest) {
 				return true;
 			}			
 		}
 		return false;
 	}
 	
 	public int assignPriority(String creature, boolean isDangerous) {
 		//if its a high valued creature add more, otherwise its moderate
 		//high valued creatures are added in the iSnorkDecoder so test against that
 		if(decoder.isCreatureInDecoder(creature))
 			return 5; //highest priority
 		if(isDangerous)
 			return 1;
 		//if here, then its of moderate priority
 		return 3;
 	}
 	
 	public boolean isPlayerOnBoat()
 	{
 		if (currentPosition.distance(new Point2D.Double(0,0)) == 0)
 		{
 			return true;
 		}
 		else
 		{
 			return false;
 		}
 	}
 	
 	@Override
 	public String tick(Point2D myPosition, Set<Observation> whatYouSee,
 			Set<iSnorkMessage> incomingMessages,
 			Set<Observation> playerLocations) {
 		minutesLeft -= 1;
 		currentPosition = myPosition;
 		whatISee = whatYouSee;
 		int positiveID = -1 * getId();
 		String message = null;
 		
		if (true)
			return null;
		
 		updatePlayerTracker();
 		
 		//read messages
 		iSnorkMessage temp = null;
 		String mess = null;
 		SeaLifePrototype obs = null;
 		//what is the message
 		Iterator<iSnorkMessage> it = incomingMessages.iterator();
 		while(it.hasNext())
 		{	
 			temp = it.next();
 			mess= temp.getMsg();
//			obs = MessageTranslator.hm.get(mess);
 			if(obs == null)
 				continue;
 		}
 		
 		
 		//see if its worth sending a message
 		for(Object obj : whatISee) {
 			Observation creature = (Observation)obj;
 			if(shouldSendMessage(creature)) {
 				//check if creature is static
 				boolean isStatic = false;
 				for(int i = 0; i < decoder.creatureList.size(); i++) {
 					if(creature.getName() == decoder.creatureList.get(i).getName())
 						if(decoder.creatureList.get(i).isStatic)
 							isStatic = true;
 				}
 				Destination dest = new Destination(myPosition, assignPriority(creature.getName(), creature.isDangerous()), 1, isStatic);
 				//check if destination is already in the list
 				if(priorityQueue.get(positiveID).size() == 0)
 					priorityQueue.get(positiveID).add(dest);
 				if(!isDestinationInQueue(dest, getId())) {
 					//check if the player has seen the creature already, if it has don't add to the queue
 					CreatureTracker tracker = myTracker.get(positiveID);
 					if(!tracker.didSeeCreature(creature.getName())) {
 						//add this destination to the priority Queue
 						priorityQueue.get(positiveID).add(dest);
 					}
 				}
 			}
 		}
 		return null;
 	}
 	
 
 	@Override
 	public Direction getMove() {
 		return myStrategy.nextMove();
 	}
 	
 	public Strategy decideStrategy(){
 		return null;
 	}
 	
 	public void assignTravelTimeAllowance()
 	{
 		int minPerMove = 3;
 		switch(getLevel(DANGER))
 		{
 		case NONE:
 			dangerAvoidTravelTime = 0;
 			break;
 		case LOW:
 			dangerAvoidTravelTime = d * minPerMove;
 			break;
 		case MEDIUM:
 			dangerAvoidTravelTime = d * 2 * minPerMove;
 			break;
 		case MAYBE:
 			dangerAvoidTravelTime = d * minPerMove;
 			break;
 		case HIGH:
 			dangerAvoidTravelTime = d * 3 * minPerMove;
 			break;
 		case OMG:
 			dangerAvoidTravelTime = d * 4 * minPerMove;
 			break;
 		}
 	}
 	
 	public Risk getRisk(){
 		Level happy = getLevel(HAPPY);
 		Level danger = getLevel(DANGER);
 		if (Risk.NONE.factor*happy.factor == danger.factor){
 			return Risk.NONE;
 		}
 		else if(Risk.VERYBAD.factor*danger.factor > happy.factor){
 			return Risk.VERYBAD;
 		}
 		else if(Risk.BAD.factor*danger.factor > happy.factor){
 			return Risk.BAD;
 		}
 		else if(Risk.VERYGOOD.factor*happy.factor > danger.factor){
 			return Risk.VERYGOOD;
 		}
 		else if(Risk.GOOD.factor*happy.factor > danger.factor){
 			return Risk.GOOD;
 		}
 		else 
 			return Risk.EVEN;
 	}
 	
 	
 	public Level getLevel(boolean isDangerous){
 		int tiles = getNumTiles();
 		int totalMaxDanger = 0;
 		int totalMinDanger = 0;
 		for (SeaLifePrototype s: seaLifePossibilites){
 			if (s.isDangerous() && isDangerous){
 				totalMaxDanger += s.getHappiness()*s.getMaxCount();
 				totalMinDanger += s.getHappiness()*s.getMinCount();
 			}
 		}
 		int averageDanger = (totalMaxDanger + totalMinDanger) / 2;
 		int varianceNumerator = (totalMaxDanger - averageDanger)*(totalMaxDanger - averageDanger) + (totalMinDanger - averageDanger)*(totalMinDanger - averageDanger);
 		int variance = varianceNumerator / 2;
 		if (variance > averageDanger * Level.MAYBE.getFactor()){	
 			return Level.MAYBE;
 		}
 		else if ((averageDanger/tiles) <=  Level.NONE.getFactor()){
 			return Level.NONE;
 		}
 		else if ((averageDanger/tiles) <=  Level.LOW.getFactor()){
 			return Level.LOW;
 		}
 		else if ((averageDanger/tiles) <=  Level.MEDIUM.getFactor()){
 			return Level.MEDIUM;
 		}
 		else if ((averageDanger/tiles) <=  Level.HIGH.getFactor()){
 			return Level.HIGH;
 		}
 		else
 			return Level.OMG;
 	}
 		
 	public String composeMessage(){
 		return null;
 	}	
 	
 	public int getNumTiles(){
 		return (d + d + 1)*(d + d + 1);
 	}
 }
