 /**
  * 
  */
 package isnork.g4;
 
 import isnork.g4.util.ClusteringStrategy;
 import isnork.g4.util.EndGameStrategy;
 import isnork.g4.util.MarkovSimulator;
 import isnork.g4.util.MessageMap;
 import isnork.g4.util.HeatMap;
 import isnork.g4.util.ObjectPool;
 import isnork.g4.util.Strategy;
 import isnork.sim.GameObject.Direction;
 import isnork.sim.Observation;
 import isnork.sim.Player;
 import isnork.sim.SeaLifePrototype;
 import isnork.sim.iSnorkMessage;
 
 import java.awt.Point;
 import java.awt.geom.Point2D;
 import java.util.Collection;
 import java.util.Comparator;
 import java.util.Enumeration;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.Set;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 /**
  * @author Lenovo
  *
  */
 public class G4Diver extends Player {
 	
 	Strategy strategy = null;
 	HeatMap seaBoard = null;
 	
 	/* (non-Javadoc)
 	 * @see isnork.sim.Player#getName()
 	 */
 	@Override
 	public String getName() {
 		return "G4 Diver";
 	}
 
 	/** Mapping form species to communication code. */
 	private MessageMap messageMap;
 	/** Mapping from name to SeaLifePrototype. This field is meant to be read-only... */
 	private static Hashtable<String, SeaLifePrototype> species = null;
 	/** Set of IDs of stationary objects that were already reported. */
 	private Set<Integer> reportedStationaries;
 	/** Map from id to observation of the last observations of the last tick. */
 	private Hashtable<Integer, Observation> lastCreatureObservations = null;
 	/** The radius of visibility. */
 	private DiverHeatmap heatmap;
 	/** The current position. */
 	Point2D position;
 	/** The dimension of the board. */
 	int dimension;
 	ObjectPool<Point2D.Double> pointPool;
 	/** EndGameStrategy object */
 	private EndGameStrategy endGameStrategy = new EndGameStrategy();
 	/** Current and Max Rounds */
 	private int currentRound = 0, maxRounds = 480;
 	private double dangerDensity = 0;
 	
 	public static Hashtable<Point2D, Direction> neighbors;
 	
 	{
 		neighbors = new Hashtable<Point2D, Direction>(8);
 		
 		neighbors.put(new Point(-1, 1), Direction.SW);
 		neighbors.put(new Point(0, 1), Direction.S);
 		
 		neighbors.put(new Point(1, 1), Direction.SE);
 		neighbors.put(new Point(-1, 0), Direction.W);
 		neighbors.put(new Point(1, 0), Direction.E);
 		
 		neighbors.put(new Point(-1, -1), Direction.NW);
 		neighbors.put(new Point(0, -1), Direction.N);
 		neighbors.put(new Point(1, -1), Direction.NE);
 	}
 	
 	/**
 	 * Build the set of name to prototype mappings
 	 * @param possibilities the possible species on the board
 	 */
 	private static void constructPossibilitiesMap(Collection<SeaLifePrototype> possibilities) {
 		species = new Hashtable<String, SeaLifePrototype>();
 		for (SeaLifePrototype p: possibilities) {
 			species.put(p.getName(), p);
 		}
 	}
 	
 	/* (non-Javadoc)
 	 * @see isnork.sim.Player#newGame(java.util.Set, int, int, int, int)
 	 */
 	@Override
 	public void newGame(Set<SeaLifePrototype> seaLifePossibilites, int penalty,
 			int d, int r, int n) {
 
 		dangerDensity = calculateDangerDensity(seaLifePossibilites);
 		
 		if (species == null) {
 			constructPossibilitiesMap(seaLifePossibilites);
 		}
 		reportedStationaries = new HashSet<Integer>();
 		
 //		seaBoard = new HeatMap(seaLifePossibilites, d);
 //		strategy = new Strategy(seaLifePossibilites, penalty, d, r, n, random);
 		
 		messageMap = new MessageMap(seaLifePossibilites);
 		heatmap = new DiverHeatmap(d);
 		dimension = d;
 		pointPool = new ObjectPool<Point2D.Double>(Point2D.Double.class);
 
 		ClusteringStrategy.getInstance().initialize(d, n, getId());
 	}
 	
 	
 	/**
 	 * returns the danger density of the board as follows 
 	 * <br><b>density = (totalScore - expectedDangerousScore) / numTotal</b>
 	 * 
 	 * @param seaLifePossibilites
 	 * @return dangerDensity
 	 */
 	public double calculateDangerDensity(Set<SeaLifePrototype> seaLifePossibilites) {
 		double density = 0;
 		int numStaticDangerous = 0, numMovingDangerous = 0, numSafe = 0, numTotal = 0;
 		double totalScore = 0, expectedDangerousScore = 0, individualHappyScore = 0, individualDangerScore = 0, maxHappyCreatureScore = -9999999, maxDangerCreatureScore = -9999999;
 		String maxHappyCreatureName = null, maxDangerCreatureScoreName = null;
 		for(SeaLifePrototype s : seaLifePossibilites){
 			individualHappyScore = 0; individualDangerScore = 0;
 			if(s.getMinCount() > 0){
 				if(s.getMaxCount()<2){
 					individualHappyScore = individualHappyScore + s.getHappiness();
 //					totalScore = totalScore + s.getHappiness();
 				}
 				if(s.getMaxCount()<3){
 					individualHappyScore = individualHappyScore + 0.5*s.getHappiness();
 //					totalScore = totalScore + 0.5*s.getHappiness();
 				}
 				if(s.getMaxCount() >= 3){
 					individualHappyScore = individualHappyScore + 0.25*s.getHappiness();
 //					totalScore = totalScore + 0.25*s.getHappiness();
 				}
 			}
 			totalScore = totalScore + individualHappyScore;
 			
 			if(s.isDangerous()){
 				individualDangerScore = ((s.getMaxCount()+s.getMinCount())/2.0)*(2*s.getHappiness());
 				expectedDangerousScore = expectedDangerousScore + individualDangerScore;
 				if(s.getSpeed() > 0){
 					numMovingDangerous = numMovingDangerous + (int)Math.ceil((s.getMaxCount()+s.getMinCount())/2.0);
 				} else {
 					numStaticDangerous = numStaticDangerous + (int)Math.ceil((s.getMaxCount()+s.getMinCount())/2.0);
 				}
 				
 				if(individualDangerScore > maxDangerCreatureScore){
 					maxDangerCreatureScore = individualDangerScore;
 					maxDangerCreatureScoreName = s.getName();
 				}
 			} else {
 				numSafe = numSafe + (int)Math.ceil((s.getMaxCount()+s.getMinCount())/2.0);
 				if(individualHappyScore > maxHappyCreatureScore){
 					maxHappyCreatureScore = individualHappyScore;
 					maxHappyCreatureName = s.getName();
 				}
 			}
 		}
 		numTotal = numSafe + (numStaticDangerous + numMovingDangerous);
 		density = (totalScore - expectedDangerousScore) / numTotal;
 		System.out.println(numTotal + "," + numSafe + "," + numStaticDangerous + "," + numMovingDangerous);
 		System.out.println(totalScore + ", " + expectedDangerousScore);
 		System.out.println(maxHappyCreatureName + "," + maxHappyCreatureScore + " -- " + maxDangerCreatureScoreName + "," + maxDangerCreatureScore);
 		System.out.println("Density = " + density);
 		return density;
 	}
 	
 	/**
 	 * Convert a string name to a prototype
 	 * @param name the name
 	 * @return the prototype, or null if it wasn't found
 	 */
 	public static SeaLifePrototype getProtoFromName(String name) {
 		assert(species != null);
 		try {
 			return species.get(name);
 		} catch (NullPointerException e) {
 			return null;
 		}
 	}
 	
 	/**
 	 * Get a set of observations and filter out the players, leaving just the 
 	 * creatures. 
 	 * @param observations the set of observations
 	 * @return the set of observations without the players
 	 */
 	private Set<Observation> creaturesFilter(Set<Observation> observations) {
 		HashSet<Observation> ret = new HashSet<Observation>();
 		
 		for (Observation o: observations) {
 			if (o.getId() > 0) {
 				ret.add(o);
 				System.out.println("added " + o.getId());
 //				if (o.isDangerous()) {
 //					System.out.println("Jesus Christ! Is that a "+o.getName()+"!??!");
 //				}
 			}
 		}
 		
 		return ret;
 	}
 	
 	private Observation moreValuableObservation(Observation o1, Observation o2) {
 		if (messageMap.moreValuable(o1.getName(), o2.getName()) == o1.getName()) {
 			return o1;
 		} else {
 			return o2;
 		}
 	}
 	
 	private Observation getMostValuable(Collection<Observation> observations) {
 		Observation mostValuable = null;
 		
 		for (Observation s: observations) {
 			if (mostValuable == null) {
 				mostValuable = s;
 				continue;
 			}
 			mostValuable = moreValuableObservation(mostValuable, s);
 		}
 		return mostValuable;
 	}
 	
 	/**
 	 * Return the name of the most valuable species. This is where the bulk of 
 	 * the communication dispatching logic will want to live.
 	 * TODO Put it here. 
 	 * @param species the set of species
 	 * @return the name of the most valuable species, or null if there is 
 	 * nothing to report
 	 */
 	private String chooseSpeciesToReport(Set<Observation> observations) {
 		HashSet<Observation> stationaries = new HashSet<Observation>();
 		HashSet<Observation> mobiles = new HashSet<Observation>();
 		
 		// Partition the list into stationary and nonstationary lists. Also 
 		// discard observations of stationaries we've already seen. 
 		for (Observation s: observations) {
 			if (reportedStationaries.contains(s.getId())) {
 				continue;
 			}
 			if (G4Diver.getProtoFromName(s.getName()).getSpeed() == 0) {
 				stationaries.add(s);
 			} else {
 				mobiles.add(s);
 			}
 		}
 		
 		Observation mostValuable = getMostValuable(stationaries);
 		if (mostValuable == null) {
 			mostValuable = getMostValuable(mobiles);
 		}
 		
 		if (mostValuable == null) {
 			return null;
 		} else {
 			return mostValuable.getName();
 		}
 	}
 	
 	/**
 	 * Convenience function that takes a collection of observations and returns a set of 
 	 * species that those observations correspond to. 
 	 * @param set the collection of observations
 	 * @return the set of prototype objects for that collection
 	 */
 	private Set<SeaLifePrototype> getSpeciesFromObservations(Collection<Observation> set) {
 		HashSet<SeaLifePrototype> ret = new HashSet<SeaLifePrototype>();
 		for (Observation o: set) {
 			SeaLifePrototype proto = getProtoFromName(o.getName());
 			// There must be a prototype for that 
 			assert(proto == null && o.getId() > 0);
 			if (proto != null) {
 				ret.add(proto);
 			}
 		}
 		return ret;
 	}
 	
 	private Hashtable<Integer, Observation> archiveObservations(Set<Observation> observations) {
 		Hashtable<Integer, Observation> ret = new Hashtable<Integer, Observation>();
 		for (Observation o: observations) {
 			ret.put(o.getId(), o);
 		}
 		return ret;
 	}
 	
 	private void registerWithHeatmap(Collection<Observation> observations) {
 		for (Observation o: observations) {
 			if (getProtoFromName(o.getName()).getSpeed() == 0) {
 				heatmap.registerStationary(o);
 			} else {
 				heatmap.registerMoving(o);
 			}
 		}
 	}
 	
 	/* (non-Javadoc)
 	 * @see isnork.sim.Player#tick(java.awt.geom.Point2D, java.util.Set, java.util.Set, java.util.Set)
 	 */
 	@Override
 	public String tick(Point2D myPosition, Set<Observation> whatYouSee,
 			Set<iSnorkMessage> incomingMessages,
 			Set<Observation> playerLocations) {
 		currentRound++;
 		
 //		strategy.updateAfterEachTick(myPosition, whatYouSee, incomingMessages, playerLocations, getId());
 		position = myPosition;
 		
 		for(Observation o : whatYouSee){
 			System.out.println(" u see - " + o.getId());
 		}
 		
 		Set<Observation> justCreatures = creaturesFilter(whatYouSee);
 		
 		// Notify the heatmap of stationary creatures
 		registerWithHeatmap(justCreatures);
 		// Select which species to dispatch a report on
 		String speciesToReport = chooseSpeciesToReport(justCreatures);
 		// Map the species to a string message
 		String message = messageMap.get(speciesToReport);
 		
 		// Store the last creature observations in the hashtable. 
 		lastCreatureObservations = archiveObservations(whatYouSee);
 		
 		return message;
 	}
 	
 	private Direction getNeighbor(Point2D current, Point2D neighbor) {
 		Point2D scr = (Point2D) current.clone();
 		scr.setLocation(neighbor.getX() - current.getX(), neighbor.getY() - current.getY());
 		return neighbors.get(scr);
 	}
 	
 	private Point2D getLargest(Hashtable<Point2D, Double> m) {
 		Enumeration<Point2D> e = m.keys();
 		double index = 0.0;
 		Point2D best = null;
 		
 		while (e.hasMoreElements()) {
 			Point2D candidate = e.nextElement();
 			double potentialIndex = m.get(candidate);
 			
 			if (best == null || potentialIndex > index) {
 				index = potentialIndex;
 				best = candidate;
 			}
 		}
 		
 		return best;
 	}
 	
 	private Direction getSafest() {
 		double safestDanger = -9999999;
 		Point2D safest = null;
 		System.out.println(" >> myPos = " + position);
 		for (Point2D p: neighbors.keySet()) {
 			Point2D.Double scr = pointPool.get();
 			scr.setLocation(p.getX() + position.getX(), p.getY() + position.getY());
 			
 			double potentialDanger;
 			try {
 				potentialDanger = heatmap.dangerGet((int) scr.x, (int) scr.y) + Math.random() / 100;
 				System.out.println(" Potential Danger at " + scr + "" + potentialDanger);
 			} catch (ArrayIndexOutOfBoundsException e) {
 				continue; 
 			}
 			
 			if (safest == null || potentialDanger > safestDanger) {
 				safest = scr;
 				safestDanger = potentialDanger;
 			}
 		}
 		pointPool.reset();
 		System.out.println("safets = " + safest);
 		return getNeighbor(position, safest);
 	}
 	
 	private Direction getMoveDijkstra(Point2D dest) {
 		HashSet<Point2D> visited = new HashSet<Point2D>();
 		Hashtable<Point2D, Double> indices = new Hashtable<Point2D, Double>();
 		Hashtable<Point2D, Point2D> predecessors = new Hashtable<Point2D, Point2D>();
 		
 		indices.put(position, heatmap.dangerGet((int) position.getX(), (int) position.getY()));
 		
 		if (dest.equals(position)) {
 			return Direction.STAYPUT;
 		}
 		
 		while (indices.size() != 0) {
 			Point2D current = getLargest(indices);
 			double index = indices.get(current);
 			if (current.equals(dest)) {
 				break;
 			}
 			indices.remove(current);
 			visited.add(current);
 			
 			for (Point2D p: neighbors.keySet()) {
 				Point2D.Double scr = pointPool.get();
 				scr.setLocation(p.getX() + current.getX(), p.getY() + current.getY());
 				
 				if (visited.contains(scr)) {
 					continue;
 				}
 				
 				boolean put = false;
 				
 				double thisIndex;
 				try {
 					thisIndex = index + heatmap.dangerGet((int) scr.getX(), (int) scr.getY());
 				} catch (ArrayIndexOutOfBoundsException e) {
 					continue;
 				}
 				
 				try {
 					double oldIndex = indices.get(scr);
 					if (oldIndex > thisIndex) {
 						put = true;
 					}
 				} catch (NullPointerException e) {
 					put = true;
 				}
 				
 				if (put) {
 					indices.put(scr, thisIndex);
 					predecessors.put(scr, current);
 				}
 			}
 		}
 		
 		if (indices.size() == 0) {
 			return null;
 		}
 		
 		pointPool.reset();
 		
 		Point2D move = dest;
 		while (true) {
 			Point2D next = predecessors.get(move);
 			if (next.equals(position)) {
 				break;
 			} else {
 				move = next;
 			}
 		}
 		
 		return getNeighbor(position, move);
 	}
 	
 	private Direction goToBoat() {
 		Point2D dest = new Point2D.Double(0, 0);
 
 		if (dest.equals(position)) {
 			return Direction.STAYPUT;
 		}
 
 		double x = position.getX();
 		double y = position.getY();
 		double new_x1 = 0, new_y1 = 0, new_x2 = 0, new_y2 = 0, new_x3 = 0, new_y3 = 0;
 		double danger1, danger2, danger3;
 		
 		// first move option, decrease abs of x (or if x == 0, do nothing)
 		if ( Math.abs(x) != 0 ) {
 			new_x1 = (x)/(Math.abs(x))*(Math.abs(x)-1);
 			new_y1 = y;
 			danger1 = heatmap.dangerGet((int)new_x1, (int)new_y1);
 		}
 		else {
 			danger1 = 0;
 		}
 	
 		// second move option, decrease abs of y (or if y == 0, increase abs of y and decrease abs of x)
 		if ( Math.abs(x) != 0 ) {
 			new_x2 = x;
 			new_y2 = (y)/(Math.abs(y))*(Math.abs(y)-1);
 			danger2 = heatmap.dangerGet((int)new_x2, (int)new_y2);
 		}
 		else {
 			danger2 = 0;
 		}
 		
 		// third option (if not on axis) decrease abs of both x and y
 		if ( Math.abs(x) != 0 && Math.abs(y) != 0) {
 			new_x3 = (x)/(Math.abs(x))*(Math.abs(x)-1);
 			new_y3 = (y)/(Math.abs(y))*(Math.abs(y)-1);
 			danger3 = heatmap.dangerGet((int)new_x3, (int)new_y3);
 		}
 		else danger3 = 0;
 		
 		// get the dangers of each step, then find the min of these three
 		
 	    if (Math.min(danger1, danger3) > danger2) return findDirection(position, new Point2D.Double(new_x2, new_y2));
 	    if (Math.min(danger2, danger3) > danger1) return findDirection(position, new Point2D.Double(new_x1, new_y1));
 	    return findDirection(position, new Point2D.Double(new_x3, new_y3));
 	    
 	}
 	
 	/* (non-Javadoc)
 	 * @see isnork.sim.Player#getMove()
 	 */
 	@Override
 	public Direction getMove() {
 //		System.out.println(ClusteringStrategy.getInstance().toString());
 //		System.out.println(" ------------------------- " + getId());
 //		Direction d = strategy.getMove(getId());
 //		System.out.println(" ------------------------- getMove - " + getId() + " -DIR- " + d);
 		
 		MarkovSimulator.simulate(6, new Point(0, 0), new Point(1, 1));
 		try {
 			if(endGameStrategy.allowedReturnTimeRadius((double)30, this) <= endGameStrategy.fastestReturnTime(position)) {
 				return goToBoat();
 				//return findDirection(position, new Point2D.Double(0, 0));
 			}
 			return getSafest();
 		} catch (Exception e) {
 			e.printStackTrace();
 			System.exit(-1);
 		}
 		return null;
 	}
 
 	public int getCurrentRound() {
 		return currentRound;
 	}
 
 	public int getMaxRounds() {
 		return maxRounds;
 	}
 	
 	public static Direction findDirection(Point2D currentPos, Point2D newPos){
 		double currX = currentPos.getX();
 		double currY = currentPos.getY();
 		double newX = newPos.getX();
 		double newY = newPos.getY();
 		
 		// in a quadrant
 		if (currX > newX && currY > newY) {
 			return Direction.NW;
 		}
 		if (currX < newX && currY < newY) {
 			return Direction.SE;
 		}
 		if (currX < newX && currY > newY) {
 			return Direction.NE;
 		}
 		if (currX > newX && currY < newY) {
 			return Direction.SW;
 		}
 
 		// on a line
 		if (currX < newX && currY > newY || currX < newX
 				&& currY == newY) {
 			return Direction.E;
 		}
 		if (currX > newX && currY < newY || currX == newX
 				&& currY < newY) {
 			return Direction.S;
 		}
 
 		if (currX == newX && currY > newY) {
 			return Direction.N;
 		}
 		if (currX > newX && currY == newY) {
 			return Direction.W;
 		}
 		
 		if (currX == newX && currY == newY) return Direction.STAYPUT;
 		
		return Direction.STAYPUT;
 		
 	}
 }
