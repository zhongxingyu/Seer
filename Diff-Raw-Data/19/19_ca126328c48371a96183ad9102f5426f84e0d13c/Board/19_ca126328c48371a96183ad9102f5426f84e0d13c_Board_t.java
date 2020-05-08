 package roborally.model;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
import java.util.Iterator;
 import java.util.Random;
 import java.util.Set;
 
 import be.kuleuven.cs.som.annotate.Basic;
 import be.kuleuven.cs.som.annotate.Immutable;
 
 import roborally.basics.Energy;
 import roborally.basics.Orientation;
 import roborally.basics.Position;
 import roborally.utils.Calculator;
 
 public class Board{
 
 	//TODO: documentatie en annotations & Helaas veel meer dan enkel da de puts en gets werken niet... Die rare structuur snap ik ni van hashmap
 
 	private final long width;
 	private final long height;
 	private HashMap <Position, HashSet<Entity>> map;
 
 	private static final long UPPER_BOUND_WIDTH = Long.MAX_VALUE;
 	private static final long LOWER_BOUND_WIDTH = 0;
 	private static final long UPPER_BOUND_HEIGTH = Long.MAX_VALUE;
 	private static final long LOWER_BOUND_HEIGTH = 0;
 
 	/**
 	 * Deze constructor maakt een nieuw board aan 
 	 * @param height
 	 * @param width
 	 */
 	public Board (long height, long width){
 		this.height = height;
 		this.width = width;
 		this.map = new HashMap<Position, HashSet<Entity>>();
 	}
 
 
 	public HashSet<Entity> getEntityOnPosition(Position pos){
 		return this.map.get(pos);
 	}
 
 	public void removeEntity(Entity ent){
 		if (this.map.containsValue(ent)){
 			Collection<HashSet<Entity>> c = this.map.values();
 			for (HashSet<Entity> val  : c){
 				if (val.contains(ent)){
 					val.remove(ent);
 				}
 			}
 		}
 
 	}
 
 	public boolean isPlacableOnPosition(Position pos){
 		if(this.map == null)
 			return true;
 		if (this.map.get(pos) == null)
 			return true;
 		if (this.map.get(pos).isEmpty())
 			return true;
 		if (this.map.get(pos).size() == 1){
 			Entity obj = this.map.get(pos).iterator().next();
 			if (obj instanceof Wall)
 				return false;
 		}
 		return true;
 	}
 
 	public void putEntity(Position key, Entity entity){
 		if (entity instanceof Battery || entity instanceof Robot){
 			if (isPlacableOnPosition(key)){
 				HashSet<Entity> set = this.map.get(key);
 				if (set == null){
 					HashSet<Entity> input = new HashSet<Entity>();
 					input.add(entity);
 					map.put(key, input);
 				}else{
 					set.add(entity);
 				}
 			}
 		}else if(entity instanceof Wall){
 			if (isPlacableOnPosition(key)){
 				HashSet<Entity> set = map.get(key);
 				HashSet<Entity> input = new HashSet<Entity>();
 				input.add(entity);
 				map.put(key, input);
 			}
 
 		}
 	}
 
 	public Set<Robot> getRobots() {
 		Collection<HashSet<Entity>> c = map.values();
 		HashSet<Robot> rob = new HashSet<Robot>();
 		for (Set<Entity> values : c ){
			Iterator<Entity> i = values.iterator();
			while (i.hasNext()){
				Entity obj = i.next();
 				if (obj instanceof Robot){
 					rob.add((Robot) obj);
 				}
 			}
 		}
 		return rob;
 	}
 
 	public Set<Battery> getBatteries() {
 		Collection<HashSet<Entity>> c = map.values();
 		HashSet<Battery> bat = new HashSet<Battery>();
 		for (Set<Entity> values : c ){
			Iterator<Entity> i = values.iterator();
			while (i.hasNext()){
				Entity obj = i.next();
 				if (obj instanceof Battery){
 					bat.add((Battery) obj);
 				}
 			}
 
 		}
 		return bat;
 	}
 
 	public Set<Wall> getWalls() {
 		Collection<HashSet<Entity>> c = map.values();
 		HashSet<Wall> wall = new HashSet<Wall>();
 		for (Set<Entity> values : c ){
			Iterator<Entity> i = values.iterator();
			while (i.hasNext()){
				Entity obj = i.next();
 				if (obj instanceof Wall){
 					wall.add((Wall) obj);
 				}
 			}
 
 		}
 		return wall;
 	}
 
 	/**
 	 * 
 	 * @param height
 	 * @return
 	 */
 	@Basic
 	public static boolean isValidHeight(long height){
 		return (height > LOWER_BOUND_HEIGTH) && (height <= UPPER_BOUND_HEIGTH);
 	}
 
 	/**
 	 * 
 	 * @param width
 	 * @return
 	 */
 	@Basic
 	public static boolean isValidWidth(long width){
 		return (width > LOWER_BOUND_WIDTH) && (width <= UPPER_BOUND_WIDTH);
 	}
 
 	/**
 	 * 
 	 * @return
 	 */
 	@Basic
 	@Immutable
 	public long getWidth() {
 		return this.width;
 	}
 
 	/**
 	 * 
 	 * @return
 	 */
 	@Basic
 	@Immutable
 	public long getHeight() {
 		return this.height;
 	}
 
 	/**
 	 * 
 	 * @param position
 	 * @return
 	 */
 	@Basic
 	public boolean isValidBoardPosition(Position position){
 		if (position.getX() > this.getWidth() || position.getX() < LOWER_BOUND_WIDTH || position.getY() > this.getHeight() || position.getY() < LOWER_BOUND_HEIGTH)
 			return false;
 		return true;
 	}
 
 	/**
 	 * Methode die het de random de eerst entity teruggeeft die geraakt wordt door de laser van de robot.
 	 * 
 	 * @param 	robot
 	 * 			De Robot wiens orientation en positie bepaalt welke entity geraakt wordt.
 	 * 
 	 * @return	Entity
 	 * 			De entity die geraakt en vervolgens vernietigd zal worden.
 	 */
 	public Entity getFirstHit(Robot robot) {
 		// bepaalt welke entity door een laser gaat geraakt worden 
 		Position pos = Calculator.getNextPosition(robot.getPosition(), robot.getOrientation());
 		if (map.containsKey(pos)){
 			Set hits = map.get(pos);
 			Random rndm = new Random();
 			Object hit = hits.toArray()[rndm.nextInt(hits.toArray().length)];
 			return (Entity) hit;
 		}else{
 			//dummy Robot maken met de nieuwe positie en dez orientatie en dan dez methode oproepen tot wnn we result hebben
 			Robot dummy = new Robot(robot.getOrientation(), new Energy(0));
 			dummy.setPosition(pos);
 			return getFirstHit(dummy);
 		}
 	}
 }
