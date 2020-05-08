 package Client.Entities;
 
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.TreeMap;
 
 import common.entities.CompanyResult;
 
 public class Player implements Comparable<Player> {
 
 	private int id;
 	private String name;
 	private List<CompanyResult> resultList = new LinkedList<CompanyResult>();
 	private boolean insolvent;
 	private boolean leftGame;
 	
 	public Player(int ID, String Name) {
 		id = ID;
 		name = Name;
 		
 		playerDict.put(id, this);
 	}
 	
 	public int getID() {
 		return id;
 	}
 	
 	public String getName() {
 		return name;
 	}
 	
 	public void becameInsolvent() {
 		insolvent = true;
 	}
 	
 	public boolean isInsolvent() {
 		return insolvent;
 	}
 	
 	public void leaveGame() {
 		leftGame = true;
 	}
 	
 	public boolean hasLeftGame() {
 		return leftGame;
 	}
 	
 	
 	//PlayerList
 	private static TreeMap<Integer, Player> playerDict = new TreeMap<Integer, Player>();
 	
 	public static List<Player> getPlayers() {
 		List<Player> retList = new LinkedList<Player>();
 		retList.addAll(playerDict.values());
 		return retList;
 	}
 	
 	public static Player getPlayer(Integer ID) {
 		return playerDict.get(ID);
 	}
 	
 	public static void removePlayer(Integer ID) {
 		playerDict.remove(ID);
 	}
 
 	public CompanyResult getCompanyResult(int period) {
 		return resultList.get(period);
 	}
 
 	public void addCompanyResult(CompanyResult result) {
 		this.resultList.add(result);
 	}
 	
 	private static boolean isHost;
 	
 	public static void setHost(boolean value){
 		isHost = value;
 	}
 	
 	public static boolean isHost(){
 		return isHost;
 	}
 
 	@Override
 	public int compareTo(Player arg0) {
 		double res1 = 0;
 		for(Iterator<CompanyResult> resit = resultList.iterator(); resit.hasNext();){
 			res1 += resit.next().profit;
 		}
 		
 		double res2 = 0;
 		for(Iterator<CompanyResult> resit2 = arg0.resultList.iterator(); resit2.hasNext();){
 			res2 += resit2.next().profit;
 		}
		return Double.compare(res2, res1);
 	}
 }
