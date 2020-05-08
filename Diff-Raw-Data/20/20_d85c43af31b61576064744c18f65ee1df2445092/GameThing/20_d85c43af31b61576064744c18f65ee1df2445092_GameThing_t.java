 package game;
 
 import util.*;
 import java.util.*;
 
 public interface GameThing {
 	public Location location();
 	public Location location(Location l);
 	public Area area();
 	public String renderer();
 	// TODO: not this!
 	public Level getLevel();
 	public int gid();
 	public List<String> interactions();
	public void interact(String inter, game.things.Player who);
 	public Map<String, Object> userArguments();
	public boolean canWalkInto(Position p, Direction d, game.things.Player who);
	public boolean didWalkInto(Position p, Direction d, game.things.Player who);
 }
