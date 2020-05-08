 package game;
 
 import util.*;
 import java.util.*;
 
 public abstract class AbstractGameThing implements GameThing {
 	private Location location;
 	private final static Area singleSpot = new Area(new Position(0, 0), 1, 1);
 	private final int gid;
 	private final Map<String, Object> userArguments = new HashMap<String, Object>();
 	private GameModel modelblah;
 
 	public AbstractGameThing(GameModel model){
 		location = null; //= new Location(new Position(0, 0), Direction.NORTH);
 		gid = model.introduce(this);
 		modelblah = model;
 	}
 
 	public Location location(){
 		return location;
 	}
 
 	public Location location(Location s){
 		return location = s;
 	}
 
 	public Area area(){
 		return singleSpot;
 	}
 
 	public String renderer(){
 		return "null";
 	}
 
 	public int gid(){
 		return gid;
 	}
 
 	public List<String> interactions(){
 		return Collections.emptyList();
 	}
 
	public void interact(String inter, game.things.Player who){}
 
	public boolean canWalkInto(Position p, Direction d, game.things.Player who){
 		return true;
 	}
 
	public boolean didWalkInto(Position p, Direction d, game.things.Player who){
 		return true;
 	}
 
 	public Level getLevel(){
 		return modelblah.getLevelFor(this);
 	}
 
 	public Map<String, Object> userArguments(){
 		return userArguments;
 	}
 
 	public String toString(){
 		return "(" + renderer() + ")";
 	}
 }
