 package javachallenge.server;
 
 import java.io.Serializable;
 
 import javachallenge.common.Point;
 
 public class Flag implements Serializable {
 	private static final int TURNS_DOWN = 2, TURNS_UP = 3;
 	
 	private static final long serialVersionUID = -3703632960727476328L;
 	private Point location;
 	private int id;
 	private Team owner = null;
 	
 	private FlagState state = FlagState.STABLE;
 	private int counter = 0;
 	/**
 	 * Which team stands on the flag on previous turn
 	 */
 	private Team prevStandingTeam = null;
 	
 	public Flag(Point location, int id) {
 		this.location = location ;
 		this.id = id ;
 	}
 
 	public Point getLocation(){
 		return location ;
 	}
 	
 	public int getId(){
 		return id ;
 	}
 
 	public Team getOwner() {
 		return owner;
 	}
 	
 	public int getPercent() {
 		if (state == FlagState.UP)
 			return 100 * (TURNS_UP - counter) / TURNS_UP;
 		if (state == FlagState.DOWN)
 			return 100 * (TURNS_DOWN - counter) / TURNS_DOWN;
 		return 0;
 	}
 
 	/**
 	 * @param t Team standing on the flag in this turn
 	 */
 	public void step(Team t) {
 		if (t == null || t == owner || prevStandingTeam != t) {
 			counter = 0;
			state = FlagState.STABLE;
 		}
 
		if (t != null && t != owner) {
 			// opponent standing on the flag
 			counter++;
			
 			if (state == FlagState.STABLE)
 				state = owner == null ? FlagState.UP : FlagState.DOWN;
 			
 			if (state == FlagState.UP && counter == TURNS_UP) {
 				owner = t;
				state = FlagState.STABLE;
 			}
			
 			if (state == FlagState.DOWN && counter == TURNS_DOWN) {
 				owner = null;
				state = FlagState.STABLE;
 			}
 		}
 		
 		prevStandingTeam = t;
 	}

	@Override
	public String toString() {
		return "Flag [location=" + location + ", id=" + id + ", owner=" + owner + ", state=" + state + ", counter=" + counter
				+ ", prevStandingTeam=" + prevStandingTeam + "]";
	}
	
 }
