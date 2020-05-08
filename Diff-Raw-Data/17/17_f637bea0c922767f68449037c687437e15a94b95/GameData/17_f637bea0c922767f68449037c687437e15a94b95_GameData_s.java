 import java.awt.Color;
 import java.util.ArrayList;
 import java.util.List;
 
 public class GameData {
 	public List<Player> playerList;
 	private Player whoseTurn;
 	//Colors to initialize the first 6 players with, in order
 	private static final Color[] playerColors = 
 	{
 		new Color(95,95,255),//blue
 		new Color(255,95,95),//red
 		new Color(95,255,95),//green
 		new Color(255,255,0),//yellow
 		new Color(255,127,0),//orange
 		new Color(0xF0,0xF0,0xF0)//grey
 	}; 
 	
 	/**
 	 * Creates GameData object
 	 * @param int Number of human players
 	 * @param int Number of computer players 
 	 */
 	public GameData(int humanCount, int computerCount){
 		playerList = new ArrayList<Player>();
 		for(int i=0;i<humanCount;i++)
 		{
 			Player player = new PlayerHuman(i,playerColors[i]);
 			playerList.add(player);
 		}
 		for(int i=0;i<computerCount;i++)
 		{
			Player player = new PlayerComputer(humanCount+i,playerColors[i]);
 			playerList.add(player);
 		}
 	}
 	
 	public void gameRun(){
 		while(true){
 			for(Player player : playerList){
 				player.turn();
 			}
 		}
 	}
 	
 	public void setupGameboard(List<Territory> territoryList)
 	{
 		//List<List<Territory>> playerTerritoryLists = new ArrayList<List<Territory>>(playerList.size());
 		List<Territory> tempTerritoryList=new ArrayList<Territory>(territoryList);
 		int nextPlayerIndex=0;
 		int numberOfPlayers=playerList.size();
 		int random=0;
 		while (tempTerritoryList.size()>0)
 		{
 			random=(int) (Math.random()*tempTerritoryList.size());
 			tempTerritoryList.get(random).setOwner(getPlayer(nextPlayerIndex));
 			tempTerritoryList.remove(random);
 			nextPlayerIndex=++nextPlayerIndex%numberOfPlayers;			
 		}
 		for(Territory t:territoryList)
 		{
 			t.getOwner().reinforce(t, 3);
 		}
 		while(true)
 		{
 			int playerIndex = indexOfTurn(); //indexOfTurn shifts turn to next player and returns index of present mover
 			whoseTurn = playerList.get(playerIndex);
 			whoseTurn.turn();
 		}
 	}
 	
 	private int indexOfTurn()
 	{
 		int present = playerList.indexOf(whoseTurn);
 		present++;
 		present %= playerList.size(); //restart if it was too high
 		return present;
 	}
 	
 	
 	public Player getPlayer(int playerID)
 	{
 		return playerList.get(playerID);
 	}
 	
 	public void onClick(Territory clickedOn)
 	{
 		System.out.println(clickedOn.getOwner());
 		if (whoseTurn instanceof PlayerHuman)
 			((PlayerHuman)whoseTurn).onClick(clickedOn);
 		System.out.println(clickedOn.getUnitCount());
 		RiskAI.window.repaint();
 	}
 	public void onKeyPress()
 	{
 		System.out.println("key pressed");
 		if (whoseTurn instanceof PlayerHuman)
 			((PlayerHuman)whoseTurn).onKeyPress();
 		RiskAI.window.repaint();
 	}
 }
