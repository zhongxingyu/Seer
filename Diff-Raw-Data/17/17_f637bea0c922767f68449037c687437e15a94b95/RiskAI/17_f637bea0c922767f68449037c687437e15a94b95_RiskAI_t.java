 import java.util.List;
 
 public class RiskAI{
 	static RiskAI riskAI;
 	static GameData currentGame;
 	static List<Continent> continentData;
 	static List<Territory> territoryData;
 	public static Gfx window;
 	
 	public static HandleClick clickHandler;
 	public static Gfx gfx;
 	
	private static final int PLAYERS_HUMAN = 1;
	private static final int PLAYERS_COMP = 4;
 	
 	public static void main(String[] args)
 	{
 		//Initialize system information
 		init();
 		currentGame.setupGameboard(territoryData);
 		currentGame.gameRun();
 		
 		/*terrName("Ural").setOwner(currentGame.getPlayer(1));
 		terrName("Afghanistan").setOwner(currentGame.getPlayer(2));
 		currentGame.getPlayer(1).reinforce(terrName("Ural"),3);
 		currentGame.getPlayer(2).reinforce(terrName("Afghanistan"),3);
 		currentGame.getPlayer(1).attack(terrName("Ural"),terrName("Afghanistan"));		*/
 		
 	}
 	private static void init()
 	{
 		clickHandler = new HandleClick();
 		riskAI = new RiskAI();
 		currentGame = new GameData(PLAYERS_HUMAN, PLAYERS_COMP);
 		continentData = ContinentData.init();
 		territoryData = TerritoryData.init(continentData);
 		window = new Gfx();
 	}
 	public RiskAI()
 	{
 	}
 	
 	private static Territory terrName(String name)
 	{
 		return TerritoryData.findTerritoryByName(name, territoryData);
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
