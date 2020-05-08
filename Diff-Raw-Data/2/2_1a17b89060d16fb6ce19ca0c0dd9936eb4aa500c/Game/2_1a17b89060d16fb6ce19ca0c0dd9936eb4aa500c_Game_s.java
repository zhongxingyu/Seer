 package javachallenge.server;
 
 import java.util.ArrayList;
 
 import javachallenge.common.BlockType;
 import javachallenge.common.Direction;
 import javachallenge.common.Point;
 import javachallenge.common.VisionPoint;
 
 public class Game {
 	private Agent[][] agents;
 	private ArrayList<Flag> flags;
 	private Map map;
 	
 	public Game(Map map) {
 		this.map = map;
 		agents = new Agent[map.getWid()][map.getHei()];
 		//------------------------------------
 		flags = new ArrayList<Flag>() ;
 		for(int i = 0 ; i < map.getFlagLocations().size() ; i++){
 			flags.add(new Flag(map.getFlagLocations().get(i), i)) ;
 		}
 	}
 	
 	public Flag getFlag(int id){
 		return flags.get(id) ;
 	}
 	
 	public Agent getAgent(Point p){
 		if (!map.isInsideMap(p))
 			return null ;
 		return agents[p.x][p.y];
 	}
 	
 	public void spawnAgent(Agent a){
 		Point p = a.getLocation() ;
 		agents[p.x][p.y] = a ;
 	}
 	
 	public void moveAgent(Agent a, Point p, Point p2){
 		agents[p.x][p.y] = null ;
 		agents[p2.x][p2.y] = a ;
 	}
 	
 	public boolean hasFlag(Point dest) {
 		return getFlagByLocation(dest) != null;
 	}
 
 	public void setAgent(Point p, Agent agent){
 		agents[p.x][p.y] = agent;
 	}
 	
 	public Flag getFlagByLocation(Point dest) {
 		for (Flag flag : flags) {
 			if (flag.getLocation().equals(dest))
 				return flag ;
 		}
 		return null;
 	}
 
 	public Map getMap() {
 		return this.map;
 	}
 	
 	public ArrayList<Flag> getFlags(){
 		return flags;
 	}
 
 	public VisionPoint getVision(Point p) {
 		BlockType[] blockTypes = map.getBlockTypes(p) ;
 		int[] agentTeamId = new int[Direction.values().length];
 		for (Direction dir : Direction.values()) {
 			Agent opAgent = this.getAgent(p.applyDirection(dir));
 			agentTeamId[dir.ordinal()] = (opAgent != null ? opAgent.getTeamId() : -1);
 		}
 		VisionPoint ret = new VisionPoint(p, blockTypes, agentTeamId);  
 		return ret;
 	}
 
 	public ArrayList<Integer> getFlagOwners() {
 		ArrayList<Integer> ret = new ArrayList<Integer>() ;
 		for (Flag flag : getFlags()) 
			ret.add(flag.getOwner().getId()) ;
 		return ret;
 	}
 }
