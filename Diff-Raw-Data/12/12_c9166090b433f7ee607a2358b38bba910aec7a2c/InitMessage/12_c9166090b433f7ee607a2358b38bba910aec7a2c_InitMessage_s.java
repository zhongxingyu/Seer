 package javachallenge.common;
 
 import java.io.Serializable;
 
 public class InitMessage implements Serializable {
 	private static final long serialVersionUID = -8951571199681260311L;
 	
 	Map map;
 	int teamId;
 	
 	public InitMessage(Map map, int teamId) {
 		super();
		//---------------
 		this.map = map;
		this.map.setFlagLocations(null) ;
 		//---------------
 		this.teamId = teamId;
 	}
 
 	public Map getMap() {
 		return map;
 	}
 
 	public int getTeamId() {
 		return teamId;
 	}
 }
