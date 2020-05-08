 package ww10;
 
 import game.CashGameDescription;
 import game.GameIDGenerator;
 import game.GameRunner;
 import game.PublicGameInfo;
 import game.TableSeater;
 import game.deck.DeckFactory;
 import game.deck.RandomDeck;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import ww10.gui.DataModel;
 import bots.BotRepository;
 
 import com.biotools.meerkat.GameObserver;
 
 public class Table {
 	
 	private String tableName;
 	private String password;
 	private int nbPlayers;
 	private PublicGameInfo gameInfo;
 	private DataModel dataModel;
 	private GameRunner gameRunner;
 	private BotRepository botRepository;
 	private GameParameters gameParam;
 	
 	public Table(String tableName, int nbPlayers, String password, GameParameters gameParam){
 		this.tableName = tableName;
 		this.nbPlayers = nbPlayers;
 		this.password = password;
 		this.gameParam = gameParam;
 	}
 	
 	public String getTableName(){
 		return tableName;
 	}
 	
 	public boolean passwordValid(String pw){
 		return pw.equals(password);
 	}
 	
 	public PublicGameInfo getGameInfo(){
 		return gameInfo;
 	}
 	
 	public DataModel getDataModel(){
 		return dataModel;
 	}
 		
 	public BotRepository getBotRepository(){
 		return botRepository;
 	}
 	
 
 	public void run(){
 
 		// number of games
 		final int numGames = Integer.MAX_VALUE;
 		//final int nbPlayers = 3;
 
 		// four Bots fight against each other
 		// valid BotNames can be obtained from the botRepository
 		String[] botNames = new String[nbPlayers];
 		String[] ingameNames = new String[nbPlayers];
 		for (int i = 0; i < nbPlayers; i++) {
 			//			if (i == nbPlayers - 1) {
 			//				botNames[i] = "DemoBot/SimpleBot";
 			//				ingameNames[i] = "SimpleBot";
 			//			} else {
 			botNames[i] = "PrologBot/PrologBot";
 			ingameNames[i] = "#" + (i + 1);
 			//			}
 		}
 
 		botRepository = new BotRepository();
 		TableSeater tableSeater = new TableSeater(botRepository, false);
 		GameIDGenerator gameIDGenerator = new GameIDGenerator(System.nanoTime());
 		//		HandHistoryWriter handHistoryWriter = new HandHistoryWriter();
 		//		String simulationFileName = new SimpleDateFormat("yyMMdd-hhmm").format(new Date());
 		//		handHistoryWriter.setWriter(new FileWriter("./data/" + simulationFileName + "-history.txt"));
 
 		// in the future created via GUI, and persisted via XML to the ./data/games dir
 		CashGameDescription cashGameDescription = new CashGameDescription();
 		cashGameDescription.setSmallBlind(gameParam.getSmallBlind());
 		cashGameDescription.setBigBlind(gameParam.getBigBlind());
 		cashGameDescription.setInitialBankRoll(gameParam.getInitialBankroll());
 		cashGameDescription.setNumGames(numGames);
 
 		cashGameDescription.setBotNames(botNames);
 		cashGameDescription.setInGameNames(ingameNames);
 
 		// start the game
 		gameRunner = cashGameDescription.createGameRunner();
 		dataModel = new DataModel();
 		gameRunner.addBankrollObserver(dataModel);
 		DeckFactory deckFactory = RandomDeck.createFactory();
 		List<GameObserver> observers = new ArrayList<GameObserver>();
 		//		observers.add(handHistoryWriter);
 		observers.add(dataModel);
 		gameInfo = gameRunner.asyncRunGame(deckFactory, tableSeater, gameIDGenerator, observers);
 	}
 	
 	public void stop(){
 		gameRunner.stopGame();
 	}
 	
 	public void resume(){
 		gameRunner.resumeGame();
 	}
 	
 	public void terminate(){
 		gameRunner.stopGame();
 		gameRunner.terminateGame();
 	}
 	
 	public boolean isRunning(){
 		return gameRunner.isRunning();
 	}
 	
 	
 }
