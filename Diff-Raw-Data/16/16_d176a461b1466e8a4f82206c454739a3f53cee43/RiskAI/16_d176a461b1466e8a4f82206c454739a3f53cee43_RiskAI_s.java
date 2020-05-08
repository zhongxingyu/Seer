 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.util.ArrayList;
 import java.util.List;
 
 public class RiskAI{
 	static RiskAI riskAI;
 	static GameData currentGame;
 	static List<Continent> continentData;
 	static List<Territory> territoryData;
 	
 	public static HandleClick clickHandler;
 	
 	private static final int PLAYERS_HUMAN = 0;
 	private static final int PLAYERS_COMP = 4;
 	
 	public static void main(String[] args)
 	{
 		//Initialize system information
 		clickHandler = new HandleClick();
<<<<<<< HEAD
 		
 		riskAI = new RiskAI();
 		continentData = new ArrayList<Continent>(ContinentData.init());
 		territoryData = new ArrayList<Territory>(TerritoryData.init(continentData));		
 		Gfx testwindow = new Gfx();
		
=======
		continentData = new ArrayList<Continent>(ContinentData.init());
		territoryData = new ArrayList<Territory>(TerritoryData.init(continentData));
		
		riskAI = new RiskAI();
>>>>>>> 942950b2cff394fd0cedc0325afe954e8b2ee688
 	}
 	public RiskAI()
 	{
<<<<<<< HEAD

=======
		Gfx testwindow = new Gfx();
 		currentGame = new GameData(PLAYERS_HUMAN, PLAYERS_COMP);
>>>>>>> 942950b2cff394fd0cedc0325afe954e8b2ee688
 	}
 	
 	public static GameData getCurrentGameData()
 	{	
 		return currentGame;
 	}
 	public static List<Player> getPlayerList()
 	{
 		return getCurrentGameData().playerList;
 	}
 }
