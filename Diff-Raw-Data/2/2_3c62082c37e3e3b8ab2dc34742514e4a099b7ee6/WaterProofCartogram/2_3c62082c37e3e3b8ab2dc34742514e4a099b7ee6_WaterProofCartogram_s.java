 package isnork.g3;
 
 import isnork.sim.GameObject.Direction;
 import isnork.sim.Observation;
 import isnork.sim.SeaLifePrototype;
 import isnork.sim.SeaLife;
 import isnork.sim.iSnorkMessage;
 
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
 
 import com.google.common.annotations.VisibleForTesting;
 import com.google.common.base.Predicate;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.ImmutableSortedSet;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Sets;
 import com.google.common.collect.Ordering;
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
     private final Set<Integer> creaturesOnMap;
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
         this.creaturesOnMap = Sets.newHashSet();
         this.speciesInViewCount = Maps.newHashMap();
 		this.dex = dex;
 		ticks = 0;
 		messenger = new WaterProofMessenger(dex, numDivers, sideLength);
 	}
 
 	@Override
 	public void update(final Point2D myPosition, Set<Observation> whatYouSee,
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
 
         Predicate<iSnorkMessage> isNotFromMyLocation = new Predicate<iSnorkMessage>() {
         	@Override
         	public boolean apply(iSnorkMessage msg) {
         		return !myPosition.equals(msg.getLocation());
         	}
         };
         
 		messenger.addReceivedMessages(Sets.filter(incomingMessages, isNotFromMyLocation));
 
         for (Observation obs : whatYouSee) {
             /* This is a diver. */
             if (obs.getId() <= 0) continue;
 
             if (currentLocation.getX() != 0 || currentLocation.getY() != 0) {
                 dex.personallySawCreature(obs.getName());
             }
 
             seeCreature(obs.getId(), obs.getName(),
                     dex.get(obs.getName()), obs.getLocation());
         }
 
         // get discovered creatures based on received messages:
 		for (SeaLife creature : messenger.getDiscovered()) {
             seeCreature(
                     creature.getId(), creature.getName(),
                     creature, creature.getLocation());
 		}
 
 		if (!whatYouSee.isEmpty()) {
 			communicate(whatYouSee);
 		}
 		updateMovingCreatures();
         updateUnseenCreatures();
         updateEdgeAtStart();
         squareFor(0, 0).setExpectedHappiness(0);
 
         //System.out.println(toString());
 	}
 
     public void seeCreature(
             int id, String name, SeaLifePrototype seaLife, Point2D location) {
         if (name == null || seaLife == null) {
             return;
         }
 
         SeaLifePrototype seaLifeProto = new SeaLifePrototypeBuilder().name(name).
         	happiness(dex.getHappiness(name)).dangerous(seaLife.isDangerous()).
         	moving(seaLife.getSpeed() > 0 ? true : false).create();
 
         if (seaLife.getSpeed() > 0) {
             movingCreatures.add(new CreatureRecord(id, location, seaLifeProto));
         } else {
             squareFor(location).addCreature(seaLifeProto, 1.);
         }
 
         if (currentLocation.getX() != 0 || currentLocation.getY() != 0) {
             creaturesSeen.add(id);
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
             messenger.addOutboundMessage(bestSeen);
         }		
 	}
 
 	@VisibleForTesting Square squareFor(Point2D location) {
 		return squareFor((int) location.getX(), (int) location.getY());
 	}
 
 	@VisibleForTesting Square squareFor(int x, int y) {
         if (! insideBounds(x, y)) return null;
 		x += (sideLength / 2);
 		y += (sideLength / 2);
 		return mapStructure[x][y];
 	}
 
     private boolean insideBounds(int x, int y) {
         return Math.abs(x) <= sideLength / 2 && Math.abs(y) <= sideLength / 2;
     }
 
     private void updateEdgeAtStart() {
         for (int i=0; i<sideLength; i++) {
             mapStructure[i][0].increaseExpectedHappinessBy(100. * (1. / ticks));
             mapStructure[i][sideLength-1]
                 .increaseExpectedHappinessBy(100. * (1. / ticks));
         }
         for (int j=1; j<sideLength-1; j++) {
             mapStructure[0][j].increaseExpectedHappinessBy(100. * (1. / ticks));
             mapStructure[sideLength-1][j]
                 .increaseExpectedHappinessBy(100. * (1. / ticks));
         }
     }
 
 	private void updateMovingCreatures() {
         creaturesOnMap.clear();
 		for (Iterator<CreatureRecord> iter = movingCreatures.iterator(); iter
 				.hasNext();) {
 			CreatureRecord record = iter.next();
             if (creaturesOnMap.contains(record.id)) {
                 //System.out.println("Duplicate record");
                 continue;
             }
             creaturesOnMap.add(record.id);
 
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
                         addCreatureToSquare(record.id, x + dx, y + dy,
                                 record.seaLife, certainty);
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
 
     private int movesToSquare(double r, int x, int y) {
         double r_delta = (r - viewRadius) / r;
         if (r_delta < 0) return 0;
 
         int small = x < y ? x : y;
         int large = x < y ? y : x;
 
         /* Most efficient way to travel between squares is to take diagonals
          * until you are on the same row or column, then travel the rest of the
          * way along a line. */
         return (int) (r_delta * (3 * small + 2 * (large - small)));
     }
 
     private double happinessProportionOfCreature(SeaLifePrototype proto) {
         double viewCount = (double) dex.getPersonalSeenCount(proto.getName());
         return viewCount > 3 ? 0. : 1. / (1. + viewCount);
     }
 
     private void addCreatureToSquare(
             int id, int x, int y, SeaLifePrototype proto, double certainty) {
         for (int dx = -viewRadius; dx <= viewRadius; dx++) {
             for (int dy = -viewRadius; dy <= viewRadius; dy++) {
                 double r = Math.sqrt(dx * dx + dy * dy);
                 if (r <= viewRadius) {
                     Square thisSquare = squareFor(x + dx, y + dy);
                     if (thisSquare != null) {
                         double modifier =
                             certainty * (1 / (1. + movesToSquare(r, x, y)));
 
                         double addHappiness = creaturesSeen.contains(id) ? 0 :
                             modifier * proto.getHappiness() *
                             happinessProportionOfCreature(proto);
 
                         double addDanger = ! proto.isDangerous() || r > 1.5 ? 
                             0 : modifier * proto.getHappiness() * 2;
 
                         //System.out.println(x+dx + ", " + (y+dy) + ", " + 
                         //        proto.getName() + ": mod=" +
                         //        modifier + ", hap=" + addHappiness + ", dan=" +
                         //        addDanger);
 
                         thisSquare.increaseExpectedHappinessBy(addHappiness);
                         thisSquare.increaseExpectedDangerBy(addDanger);
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
 		Direction nextDir = unOptimizedHeatmapGetNextDirection();
 		//double danger = getExpectedDangerForCoords(DIRECTION_MAP.get(
 		//	nextDir).move((int) currentLocation.getX(), (int) currentLocation.getY()));
 		//if(danger > 0.0)
 		//	return Direction.STAYPUT;
 		return nextDir;
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
 			double dub = lst.get(i).getDub();
 			if (dub > 0){
 				runningSum += dub;
 			}
 			intLst.add(i, runningSum);
 		}
 		
 //		Object val = random.nextInt(runningSum);
 		double myRand = random.nextDouble() * runningSum;
 		//System.out.println(myRand);
 
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
 		Square square = squareFor(x, y);
 		if (square == null){
 			return Double.NEGATIVE_INFINITY;
 		}
 		else{
 			return square.getExpectedHappiness();
 		}
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
         
 		for (int i = 0; i < sideLength; i++) {
 			for (int j = 0; j < sideLength; j++) {
 				output.append(String.format(
                             "%1$#10s", numberFormat.format(
                                 mapStructure[j][i].getExpectedHappiness())));
 				output.append(" ");
 			}
 			output.append("\n");
 		}
 
 		/*
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
 		*/
 		return output.toString();
 	}
 
 	private class CreatureRecord {
         public final int id;
 		public final Point2D location;
 		public final SeaLifePrototype seaLife;
 		public final int confirmedAt;
 
 		public CreatureRecord(
                 int id, Point2D location, SeaLifePrototype seaLife) {
             this.id = id;
 			this.location = location;
 			this.seaLife = seaLife;
 			this.confirmedAt = ticks;
 		}
 	}
 }
