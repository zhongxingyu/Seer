 package se.chalmers.dat255.risk.model;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Random;
 
 import sun.security.action.GetBooleanAction;
 
 public class MissionHandler {
 	private Player winner;
 	private ArrayList<Player> eliminatedPlayers;
 	private final HashMap<Player, Mission> missionMap;
 	private ArrayList<Mission> missionsInGame;
 //	private ArrayList<Mission> listOfMissions;
 	
 	public MissionHandler(ArrayList<Player> players){
 		eliminatedPlayers = new ArrayList<Player>();
 		missionsInGame = new ArrayList<Mission>();
 		Random randGen = new Random();
 		int nextRandInt;
 		ArrayList<Mission> listOfMissions = buildMissions(players);
 		HashMap<Player, Mission> tempMissionMap = new HashMap<Player, Mission>();
 
 		for(Player player : players){
 			nextRandInt = randGen.nextInt(listOfMissions.size());
 			giveMission(player, nextRandInt, tempMissionMap, listOfMissions);
 			
 			
 			
 			System.out.println("UPPDRAG: " + player.getName() + " har i uppdrag att eliminera " + tempMissionMap.get(player).getVictim().getName() + ".");
 		}
 		
 		
 		
 		missionMap = tempMissionMap;
 	}
 	
 	private Mission getMission(Player player){
 		return missionMap.get(player);
 	}
 	
 	/**
 	 * Return true if there is a winner.
 	 */
 	public boolean winner(){
 		for(Mission mission : missionsInGame){
 			if(mission.fullFilled()){
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	public Player getWinner(){
 		return winner;
 	}
 	
 	public static enum MissionType {
 		ELIMINATE, CONQUER;
 	}
 	
 	public void playerEliminated(Player player){
 		eliminatedPlayers.add(player);
 	}
 	
 	public void giveMission(Player player, int intRand, HashMap<Player, Mission> tempMissionsMap, ArrayList<Mission> listOfMissions){
 		Mission mission = listOfMissions.remove(intRand);
		boolean gotAMission = false;
		/*
		 * Worth knowing her is that until QONQUER-missions is implemented, there is a chance of
		 * a never-ending loop.
		 */
		while(!gotAMission){
			if(mission.getVictim() == player){
				listOfMissions.add(mission);
			}
			else{
				mission.setOwner(player);
				tempMissionsMap.put(player, mission);
				missionsInGame.add(mission);
				gotAMission=true;
			}
		}
 	}
 	
 	public ArrayList<Mission> buildMissions(ArrayList<Player> players){
 		ArrayList<Mission> missions = new ArrayList<Mission>();
 		addEliminateMissions(missions, players);
 		addConquerMissions();
 		
 		return missions;
 	}
 	
 	public void addEliminateMissions(ArrayList<Mission> missions, ArrayList<Player> players){
 		ArrayList<Player> notVictims = players;
 		for(Player victim : players)
 			missions.add(new Mission(victim, MissionType.ELIMINATE));
 	}
 	
 	public void addConquerMissions(){
 		
 	}
 	
 	private class Mission{
 		
 		private MissionType type;
 		private Player owner;
 		private Player victim;
 		
 		public Mission(Player vicitim, MissionType type){
 	//		this.owner=owner;
 			this.victim=vicitim;
 			this.type=type;
 		}
 		
 		
 		
 		public boolean fullFilled(){
 			if(type == MissionType.ELIMINATE){
 				if(eliminatedPlayers.contains(victim)){
 					winner=owner;
 					return true;
 				}
 			}
 			return false;
 		}
 		
 		public Player getVictim(){
 			return victim;
 		}
 		 
 		public Player getOwner(){
 			return owner;
 		}
 		public MissionType getType(){
 			return type;
 		}
		
		public void setOwner(Player owner){
			this.owner = owner;
		}
 
 	}
 	
 }
