 package isnork.g3;
 
 import isnork.sim.GameObject.Direction;
 import isnork.sim.Observation;
 import isnork.sim.SeaLifePrototype;
 import isnork.sim.SeaLife;
 import isnork.sim.iSnorkMessage;
 
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Sets;
 
 import java.awt.geom.Point2D;
 import java.text.NumberFormat;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Random;
 import java.util.Set;
 import java.util.Queue;
 import java.util.LinkedList;
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.ImmutableSortedSet;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Ordering;
 import com.google.common.collect.Sets;
 import com.google.common.primitives.Doubles;
 
 public class WaterProofCartogram implements Cartogram {
 	private List<CreatureRecord> movingCreatures;
 	private Messenger messenger;
 	private Point2D currentLocation;
 	private Transcoder xcoder;
 	private final Pokedex dex;
 	private final Random random;
 	private final Square[][] mapStructure;
 	private final int sideLength;
 	private final int viewRadius;
 	private int ticks;
     private final Set<Integer> creaturesSeen;
     private final Map<String, Integer> speciesInViewCount;
 
 	private final static Map<Direction, Coord> DIRECTION_MAP = ImmutableMap
 			.<Direction, Coord> builder().put(Direction.E, new Coord(1, 0))
 			.put(Direction.W, new Coord(-1, 0))
 
 			.put(Direction.S, new Coord(0, 1))
 			.put(Direction.N, new Coord(0, -1))
 
 			.put(Direction.SE, new Coord(1, 1))
 			.put(Direction.SW, new Coord(-1, 1))
 
 			.put(Direction.NE, new Coord(1, -1))
 			.put(Direction.NW, new Coord(-1, -1))
 			.put(Direction.STAYPUT, new Coord(0, 0)).build();
 
 	private final static Map<Direction, Coord> orthoDirectionMap = ImmutableMap
 			.<Direction, Coord> builder().put(Direction.E, new Coord(1, 0))
 			.put(Direction.W, new Coord(-1, 0))
 
 			.put(Direction.S, new Coord(0, 1))
 			.put(Direction.N, new Coord(0, -1)).build();
 
 	private final static Map<Direction, Coord> diagDirectionMap = ImmutableMap
 			.<Direction, Coord> builder().put(Direction.SE, new Coord(1, 1))
 			.put(Direction.SW, new Coord(-1, 1))
 
 			.put(Direction.NE, new Coord(1, -1))
 			.put(Direction.NW, new Coord(-1, -1)).build();
 
 	private static final int MAX_TICKS_PER_ROUND = 60 * 8;
 
 	/*
 	 * Tunes how quickly to forget we've seen a creature. The higher this
 	 * number, the sooner a creature will be removed from the map after viewing.
 	 */
 	private static final double MAX_DECAY = 0.25;
 
 	public WaterProofCartogram(int mapWidth, int viewRadius, int numDivers,
 			Pokedex dex) {
 		this.sideLength = mapWidth;
 		this.viewRadius = viewRadius;
 		this.mapStructure = new WaterProofSquare[sideLength][sideLength];
 		for (int i = 0; i < sideLength; i++) {
 			for (int j = 0; j < sideLength; j++) {
 				this.mapStructure[i][j] = new WaterProofSquare();
 			}
 		}
 
 		this.random = new Random();
 		this.movingCreatures = Lists.newArrayList();
         this.creaturesSeen = Sets.newHashSet();
         this.speciesInViewCount = Maps.newHashMap();
 		this.dex = dex;
 		ticks = 0;
 		messenger = new WaterProofMessenger(dex, numDivers, sideLength);
 	}
 
 	@Override
 	public void update(Point2D myPosition, Set<Observation> whatYouSee,
 			Set<Observation> playerLocations,
 			Set<iSnorkMessage> incomingMessages) {
 		ticks++;
 		currentLocation = myPosition;
 
         for (int i=0; i<mapStructure.length; i++) {
             for (int j=0; j<mapStructure.length; j++) {
                 mapStructure[i][j].tick();
             }
         }
 		
         speciesInViewCount.clear();
         for (Observation obs : whatYouSee) {
             int count = speciesInViewCount.containsKey(obs.getName()) ?
                 speciesInViewCount.get(obs.getName()) + 1 : 1;
             speciesInViewCount.put(obs.getName(), count);
         }
 
 		messenger.addReceivedMessages(incomingMessages);
         // get discovered creatures based on received messages:
 		for (SeaLife creature : messenger.getDiscovered()) {
             seeCreature(
                     creature.getId(), creature.getName(),
                     creature, creature.getLocation());
 		}
 
         if (currentLocation.getX() == 0 && currentLocation.getY() == 0) {
             for (Observation obs : whatYouSee) {
                 seeCreature(obs.getId(), obs.getName(),
                         dex.get(obs.getName()), obs.getLocation());
             }
         }
 
 		if (!whatYouSee.isEmpty()) {
 			communicate(whatYouSee);
 		}
 		updateMovingCreatures();
         updateUnseenCreatures();
 
         System.out.println(toString());
 	}
 
     public void seeCreature(
             int id, String name, SeaLifePrototype seaLife, Point2D location) {
         if (name == null || seaLife == null || creaturesSeen.contains(id)) {
             return;
         }
         creaturesSeen.add(id);
         dex.personallySawCreature(name);
 
         if (seaLife.getSpeed() > 0) {
             movingCreatures.add(new CreatureRecord(location, seaLife));
         } else {
            //System.out.println(location);
            //System.out.println(squareFor(location));
            //System.out.println("l:" + (int) (location.getX() + sideLength/2) + ", " +
            //        (int) (location.getY() + sideLength/2));
             squareFor(location).addCreature(seaLife, 1.);
         }
     }
 
 	private void communicate(Set<Observation> observations) {
 		// pick highest value creature
 		Ordering<Observation> happiness = new Ordering<Observation>() {
 			public int compare(Observation left, Observation right) {
 				return Doubles.compare(left.happinessD(), right.happinessD());
 			}
 		};
 		ImmutableSortedSet<Observation> sortedObservations = ImmutableSortedSet
 				.orderedBy(happiness).addAll(observations).build();
 		Observation bestSeen = sortedObservations.first();
 
         //do not observe other divers
         if(bestSeen.getId() > 0) {
             //System.out.println("bestSeen: "+bestSeen.getName()+" "+bestSeen.getId()+" "+bestSeen.getLocation());
             messenger.addOutboundMessage(bestSeen);
         }		
 	}
 
 	private Square squareFor(Point2D location) {
 		return squareFor((int) location.getX(), (int) location.getY());
 	}
 
 	private Square squareFor(int x, int y) {
        if (! insideBounds(x, y)) return null;
        //System.out.println(x + ", " + y);
 		x += (sideLength / 2);
 		y += (sideLength / 2);
        //System.out.println(x + ", " + y);
 		return mapStructure[x][y];
 	}
 
     private boolean insideBounds(int x, int y) {
         return Math.abs(x) < sideLength / 2 && Math.abs(y) < sideLength / 2;
     }
 
 	private void updateMovingCreatures() {
 		for (Iterator<CreatureRecord> iter = movingCreatures.iterator(); iter
 				.hasNext();) {
 			CreatureRecord record = iter.next();
 
 			int r = (ticks - record.confirmedAt) / 2;
 			double certainty = 1 / (r + 1.);
 			if (certainty <= MAX_DECAY) {
 				iter.remove();
 			}
 
 			int x = (int) record.location.getX();
 			int y = (int) record.location.getY();
 
             /* Loop through squares in viewing radius */
 			for (int dx = -r; dx <= r; dx++) {
 				for (int dy = -r; dy <= r; dy++) {
 					if (insideBounds(x + dx, y + dy) &&
                             Math.sqrt(dx * dx + dy * dy) <= r) {
                         addCreatureToSquare(x + dx, y + dy, record.seaLife, certainty);
 					}
 				}
 			}
 		}
 	}
 
     private void updateUnseenCreatures() {
         double expectedHappinessInFog = 0.;
         for (SeaLifePrototype proto : dex.getAllSpecies()) {
             double expectedCount =
                 (proto.getMaxCount() + proto.getMinCount()) / 2;
             double difference;
             if (! speciesInViewCount.containsKey(proto.getName())) {
                 difference = expectedCount;
             } else {
                 difference = expectedCount - speciesInViewCount.get(proto.getName());
             }
 
             System.out.println(proto.getName() + " " + difference);
             if (difference > 0) {
                 expectedHappinessInFog += difference * proto.getHappiness();
             }
         }
 
         int squaresOutOfView =
             (int) Math.pow(sideLength, 2) - (int) Math.pow(viewRadius, 2);
         double expectedFogPerSquare = expectedHappinessInFog / squaresOutOfView;
         for (int i=0; i<sideLength; i++) {
             for (int j=0; j<sideLength; j++) {
                 double distanceToDiver =
                     Math.sqrt(Math.pow(i - (currentLocation.getX() + sideLength / 2), 2) +
                               Math.pow(j - (currentLocation.getY() + sideLength / 2), 2));
                 if (distanceToDiver > viewRadius) {
                     mapStructure[i][j]
                         .increaseExpectedHappinessBy(expectedFogPerSquare);
                 }
             }
         }
     }
 
     private int movesToSquare(int x, int y) {
         int small = x < y ? x : y;
         int large = x < y ? y : x;
 
         /* Most efficient way to travel between squares is to take diagonals
          * until you are on the same row or column, then travel the rest of the
          * way along a line. */
         return 3 * small + 2 * (large - small);
     }
 
     private double happinessProportionOfCreature(SeaLifePrototype proto) {
         double viewCount = (double) dex.getPersonalSeenCount(proto.getName());
         return viewCount > 3 ? 0. : 1. / viewCount;
     }
 
     private void addCreatureToSquare(
             int x, int y, SeaLifePrototype proto, double certainty) {
         /* TODO(haldean): this is dumb. Change how WPS works. */
         squareFor(x, y).addCreature(proto, certainty);
         squareFor(x, y).increaseExpectedHappinessBy(-proto.getHappiness());
 
         for (int dx = -viewRadius; dx <= viewRadius; dx++) {
             for (int dy = -viewRadius; dy <= viewRadius; dy++) {
                 double r = Math.sqrt(dx * dx + dy * dy);
                 if (r <= viewRadius) {
                     Square thisSquare = squareFor(x + dx, y + dy);
                     if (thisSquare != null) {
                         double modifier =
                             certainty * (1 / (1. + movesToSquare(x, y)));
                         thisSquare.increaseExpectedHappinessBy(
                                 modifier * proto.getHappiness() *
                                 happinessProportionOfCreature(proto));
                         if (r <= 1.5 && proto.isDangerous()) {
                             thisSquare.increaseExpectedDangerBy(modifier * proto.getHappiness() * 2);
                         }
                     }
                 }
             }
         }
     }
 
 	@Override
 	public String getMessage() {
 		return messenger.sendNext();
 	}
 
 	@Override
 	public Direction getNextDirection() {
 		return unOptimizedHeatmapGetNextDirection();
 	}
 
 	private Direction greedyHillClimb(double x, double y) {
 		/*
 		 * Iterate over all possible new squares you can hit next. For you to
 		 * move in a diagonal direction, you need to be 1.5* as good as ortho To
 		 * stay in the same square, you only need to be .5 * as good as ortho
 		 */
 
 		List<DirectionValue> lst = getExpectations((int) currentLocation.getX(),
 				(int) currentLocation.getY());
 
 		Direction dir = selectRandomProportionally(lst, x, y);
 		return dir;
 	}
 
 //	private Direction getMaxDirection(List<DirectionValue> lst) {
 //		DirectionValue max = lst.get(0);
 //
 //		for (DirectionValue dv : lst) {
 //			if (dv.getDub() > max.getDub()) {
 //				max = dv;
 //			}
 //		}
 //
 //		return max.getDir();
 //	}
 
 	private List<DirectionValue> getExpectations(int x, int y) {
 		List<DirectionValue> lst = Lists.newArrayListWithCapacity(8);
 
 		lst.add(new DirectionValue(Direction.STAYPUT,
 				getExpectedHappinessForCoords(x, y) * 6.0));
 
 		for (Entry<Direction, Coord> entry : orthoDirectionMap.entrySet()) {
 			lst.add(new DirectionValue(entry.getKey(),
 					getExpectedHappinessForCoords(entry.getValue().move(
 							x, y)) * 3.0));
 		}
 
 		for (Entry<Direction, Coord> entry : diagDirectionMap.entrySet()) {
 			lst.add(new DirectionValue(entry.getKey(),
 					getExpectedHappinessForCoords(entry.getValue().move(
 							x, y)) * 2.0));
 		}
 		return lst;
 	}
 	
 	private Direction selectRandomProportionally(List<DirectionValue> lst, double x, double y){
 		List<Double> intLst = Lists.newArrayListWithCapacity(8);
 		double runningSum = 0;
 		for (int i = 0; i < 8; i++) {
 			runningSum += lst.get(i).getDub();
 			intLst.add(i, runningSum);
 		}
 		
 //		Object val = random.nextInt(runningSum);
 		double myRand = random.nextDouble() * runningSum;
 		System.out.println(myRand);
 
 		Direction dir;
 		if (myRand < intLst.get(0)){
 //			System.out.println(0);
 			dir = lst.get(0).getDir();
 		}
 		else if (myRand < intLst.get(1)){
 //			System.out.println(1);
 			dir = lst.get(1).getDir();
 		}
 		else if (myRand < intLst.get(2)){
 //			System.out.println(2);
 			dir = lst.get(2).getDir();
 		}
 		else if (myRand < intLst.get(3)){
 //			System.out.println(3);
 			dir = lst.get(3).getDir();
 		}
 		else if (myRand < intLst.get(4)){
 //			System.out.println(4);
 			dir = lst.get(4).getDir();
 		}
 		else if (myRand < intLst.get(5)){
 //			System.out.println(5);
 			dir = lst.get(5).getDir();
 		}
 		else if (myRand < intLst.get(6)){
 //			System.out.println(6);
 			dir = lst.get(6).getDir();
 		}
 		else if (myRand < intLst.get(7)){
 //			System.out.println(7);
 			dir = lst.get(7).getDir();
 		}
 		else{
 			dir = returnBoat(x, y);
 		}
 		return dir;
 	}
 
 	private double getExpectedHappinessForCoords(Coord coord) {
 		return getExpectedHappinessForCoords(coord.getX(), coord.getY());
 	}
 
 	private double getExpectedHappinessForCoords(int x,
 			int y) {
 		return squareFor(x, y).getExpectedHappiness();
 	}
 
 	private final static double square(double x) {
 		return x * x;
 	}
 
 	private double getExpectedDangerForCoords(double unadjustedX,
 			double unadjustedY) {
 		if (unadjustedX == 0 && unadjustedY == 0) {
 			return 0;
 		}
 
 		if (isInvalidCoords(unadjustedX, unadjustedY)) {
 			return Double.MIN_VALUE;
 		}
 
 		double x = unadjustedX + sideLength / 2;
 		double y = unadjustedY + sideLength / 2;
 
 		int minX = (int) x - viewRadius;
 		minX = ((minX > 0) ? minX : 0);
 
 		int minY = (int) y - viewRadius;
 		minY = ((minY > 0) ? minY : 0);
 
 		int maxX = (int) x + viewRadius;
 		maxX = ((maxX < sideLength) ? maxX : sideLength);
 
 		int maxY = (int) y + viewRadius;
 		maxY = ((maxY < sideLength) ? maxY : sideLength);
 
 		double expectedDanger = 0.0;
 		for (int xCoord = minX; xCoord < maxX; xCoord++) {
 			for (int yCoord = minY; yCoord < maxY; yCoord++) {
 				double sqrt = Math.sqrt(square((xCoord - x)
 						+ square(yCoord - y)));
 
 				if (sqrt < viewRadius) {
 					expectedDanger += mapStructure[xCoord][yCoord]
 							.getExpectedDanger();
 				}
 			}
 		}
 
 		return expectedDanger;
 	}
 
 	private boolean isInvalidCoords(double x, double y) {
 		if (x < -sideLength / 2) {
 			return true;
 		} else if (x > sideLength / 2) {
 			return true;
 		} else if (y < -sideLength / 2) {
 			return true;
 		} else if (y > sideLength / 2) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	private Direction unOptimizedHeatmapGetNextDirection() {
 		int tickLeeway = MAX_TICKS_PER_ROUND - 3 * ticks;
 		double y = currentLocation.getY();
 		double x = currentLocation.getX();
 		if (Math.abs(x) < tickLeeway
 				&& Math.abs(y) < tickLeeway) {
 			return greedyHillClimb(x, y);
 		} else {
 			return returnBoat(x, y);
 		}
 	}
 
 	private Direction returnBoat(double x, double y) {
 		// Move towards boat
 		String direc = getReturnDirectionString();
 
 		return avoidDanger(genList(direc), x, y);
 	}
 
 	private String getReturnDirectionString() {
 		String direc = "";
 
 		if (currentLocation.getY() < 0)
 			direc = direc.concat("S");
 		else if (currentLocation.getY() > 0)
 			direc = direc.concat("N");
 
 		if (currentLocation.getX() < 0)
 			direc = direc.concat("E");
 		else if (currentLocation.getX() > 0)
 			direc = direc.concat("W");
 		return direc;
 	}
 
 	private Direction avoidDanger(List<DirectionValue> genList, double x, double y) {
 		for (DirectionValue dv : genList) {
 			if (getExpectedDangerForCoords(DIRECTION_MAP.get(dv.getDir()).move((int) x, (int) y)) == 0) {
 				return dv.getDir();
 			}
 		}
 		return Direction.STAYPUT;
 	}
 
 	private double getExpectedDangerForCoords(Coord coord) {
 		return getExpectedDangerForCoords(coord.getX(), coord.getY());
 	}
 
 	private static final List<DirectionValue> genList(String direc) {
 		if (direc.equals("W")) {
 			return ImmutableList.of(new DirectionValue(Direction.W, 2.0),
 					new DirectionValue(Direction.NW, 1.0), new DirectionValue(
 							Direction.SW, 1.0));
 		} else if (direc.equals("E")) {
 			return ImmutableList.of(new DirectionValue(Direction.E, 2.0),
 					new DirectionValue(Direction.NE, 1.0), new DirectionValue(
 							Direction.SE, 1.0));
 		} else if (direc.equals("N")) {
 			return ImmutableList.of(new DirectionValue(Direction.N, 2.0),
 					new DirectionValue(Direction.NE, 1.0), new DirectionValue(
 							Direction.NW, 1.0));
 		} else if (direc.equals("S")) {
 			return ImmutableList.of(new DirectionValue(Direction.S, 2.0),
 					new DirectionValue(Direction.SE, 1.0), new DirectionValue(
 							Direction.SW, 1.0));
 		} else if (direc.equals("NE")) {
 			return ImmutableList.of(new DirectionValue(Direction.NE, 2.0),
 					new DirectionValue(Direction.N, 1.0), new DirectionValue(
 							Direction.E, 1.0));
 		} else if (direc.equals("SE")) {
 			return ImmutableList.of(new DirectionValue(Direction.SE, 2.0),
 					new DirectionValue(Direction.S, 1.0), new DirectionValue(
 							Direction.E, 1.0));
 		} else if (direc.equals("NW")) {
 			return ImmutableList.of(new DirectionValue(Direction.NW, 2.0),
 					new DirectionValue(Direction.W, 1.0), new DirectionValue(
 							Direction.N, 1.0));
 		} else if (direc.equals("SW")) {
 			return ImmutableList.of(new DirectionValue(Direction.SW, 2.0),
 					new DirectionValue(Direction.S, 1.0), new DirectionValue(
 							Direction.W, 1.0));
 		} else {
 			return ImmutableList.of(new DirectionValue(Direction.STAYPUT, 1.0));
 		}
 	}
 
 	public String toString() {
 		StringBuilder output = new StringBuilder("Board at ");
 		output.append(ticks);
 		output.append("\n");
         NumberFormat numberFormat = NumberFormat.getNumberInstance();
         numberFormat.setMaximumFractionDigits(2);
         numberFormat.setMinimumFractionDigits(2);
 
 		for (int i = 0; i < mapStructure.length; i++) {
 			for (int j = 0; j < mapStructure[i].length; j++) {
 				output.append(
                         numberFormat.format(
                             mapStructure[i][j].getExpectedHappiness()));
 				output.append("\t");
 			}
 			output.append("\n");
 		}
 
 		output.append("\nWe got shit at:\n");
 		for (int i = 0; i < mapStructure.length; i++) {
 			for (int j = 0; j < mapStructure[i].length; j++) {
 				if (mapStructure[i][j].getCreatures().size() > 0) {
 					output.append(i - (sideLength / 2));
 					output.append(", ");
 					output.append(j - (sideLength / 2));
 					output.append("\n");
 				}
 			}
 		}
 		return output.toString();
 	}
 
 	private class CreatureRecord {
 		public final Point2D location;
 		public final SeaLifePrototype seaLife;
 		public final int confirmedAt;
 
 		public CreatureRecord(Point2D location, SeaLifePrototype seaLife) {
 			this.location = location;
 			this.seaLife = seaLife;
 			this.confirmedAt = ticks;
 		}
 	}
 }
