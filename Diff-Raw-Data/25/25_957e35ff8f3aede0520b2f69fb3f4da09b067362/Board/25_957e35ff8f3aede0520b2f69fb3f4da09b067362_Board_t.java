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
 
 /**
  * Een klasse om borden voor te stellen.
  * 
  * @author 	Bavo Goosens (1e bachelor informatica, r0297884), Samuel Debruyn (1e bachelor informatica, r0305472)
  * 
  * @version 1.1
  */
 public class Board{
 	//TODO: invarianten
 	//TODO: documentatie en annotations
 	//TODO:	kan getermineerd worden (met alles in zich)
 
 	/**
 	 * De breedte van het bord.
 	 */
 	private final long width;
 	/**
 	 * De hoogte van het bord.
 	 */
 	private final long height;
 	/**
 	 * Deze HashMap houdt een lijst van HashSets bij per Position met in elke HashSet alle objecten op die plaats.
 	 */
 	private final HashMap <Position, HashSet<Entity>> map;
 
 	/**
 	 * De maximale breedte die een bord kan hebben.
 	 */
 	private static final long UPPER_BOUND_WIDTH = Long.MAX_VALUE;
 	/**
 	 * De minimale breedte die een bord kan hebben.
 	 */
 	private static final long LOWER_BOUND_WIDTH = 0;
 	/**
 	 * De maximale hoogte die een bord kan hebben.
 	 */
 	private static final long UPPER_BOUND_HEIGTH = Long.MAX_VALUE;
 	/**
 	 * De minimale hoogte die een bord kan hebben.
 	 */
 	private static final long LOWER_BOUND_HEIGTH = 0;
 
 	/**
 	 * Deze constructor maakt een nieuw bord aan.
 	 * 
 	 * @param	height
 	 * 			De hoogte van het nieuwe bord.
 	 * 
 	 * @param 	width
 	 * 			De breedte van het nieuwe bord.
 	 * 
 	 * @post	Het bord heeft een map gekregen.
 	 * 			|new.getMap() != null
 	 * 
 	 * @post	Het bord heeft een hoogte gekregen.
 	 * 			|new.getHeight() == height
 	 * 
 	 * @post	Het bord heeft een breedte gekregen.
 	 * 			|new.getWidth() == width
 	 */
 	public Board (long height, long width){
 		this.height = height;
 		this.width = width;
 		this.map = new HashMap<Position, HashSet<Entity>>();
 	}
 
 	/**
 	 * Deze methode geeft alle objecten terug op een positie.
 	 * 
 	 * @param	pos
 	 * 			De positie waar gekeken moet worden.
 	 * 
 	 * @return	De HashSet met alle objecten op deze positie.
 	 * 			|this.getMap().get(pos)
 	 */
 	public HashSet<Entity> getEntityOnPosition(Position pos){
 		return this.getMap().get(pos);
 	}
 
 	/**
 	 * Deze methode verwijdert een object uit het bord.
 	 * 
 	 * @param 	ent
 	 * 			De entity die verwijderd moet worden.
 	 * 
 	 * @throws	IllegalStateException
 	 * 			Het object bevindt zich niet op een juiste positie in dit bord.
 	 * 			|ent.getBoard().isValidBoardPosition(ent.getPosition())
 	 */
 	public void removeEntity(Entity ent) throws IllegalStateException{
 		Position pos = ent.getPosition();
 		if(!ent.getBoard().isValidBoardPosition(pos)){
 			throw new IllegalStateException("Dit object bevindt zich niet op een geldige positie.");
 		}
 		this.getMap().get(pos).remove(ent);
 		this.cleanBoardPosition(pos);
 	}
 
 	/**
 	 * Deze methode kijkt na of het bord leeg is op deze positie, indien dat zo is wordt een mogelijke opgeslagen lege HashSet verwijderd.
 	 * 
 	 * @param	pos
 	 * 			De positie in het bord die moet nagekeken worden.
 	 * 
 	 * @post	De positie is leeg indien er geen objecten meer stonden.
 	 * 			|if(this.getMap().containsKey(pos) && this.getMap().get(pos).isEmpty())
 	 * 			|	new.getMap().containsKey(pos) == false
 	 */
 	public void cleanBoardPosition(Position pos){
 		if(this.getMap().containsKey(pos)){
 			if(this.getMap().get(pos).isEmpty()){
 				this.getMap().remove(pos);
 			}
 		}
 	}
	
	/**
	 * Deze methode kijkt na of een object op een plaats geplaatst kan worden.
	 * 
	 * @param	pos
	 * 			De positie die moet nagekeken worden.
	 * 
	 * @param	ent
	 * 			Het object dat voor deze positie in aanmerking komt.
	 * 
	 * @return	
	 */
	public boolean isPlacableOnPosition(Position pos, Entity ent){
		if (!this.getMap().containsKey(pos))
 			return true;
		if (this.getMap().get(pos).isEmpty())
 			return true;
		if (this.getMap().get(pos).size() == 1){
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
 
 
 	public static Entity getFirstHit(Robot robot) {
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
 
 
 	public HashMap<Position, HashSet<Entity>> getMap() {
 		return this.map;
 	}
 }
