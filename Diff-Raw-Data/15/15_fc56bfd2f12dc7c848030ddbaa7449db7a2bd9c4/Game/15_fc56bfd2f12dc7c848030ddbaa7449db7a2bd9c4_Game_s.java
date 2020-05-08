 import java.awt.EventQueue;
 import java.awt.event.WindowEvent;
 import java.io.ByteArrayOutputStream;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Random;
 
 import javax.swing.JFrame;
 
 /**
  * @author Team 7
  * Essentially represents the runner for the MULE game
  */
 public class Game implements Serializable{
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1234L;
 	public static final List<String> COLORS = Arrays.asList("Red", "Orange", "Blue", "Yellow");
 	public static final Race[] RACES = Race.values();
 	private static Exception Throwable;
 	private GamePanel gamePanel;
 	boolean playing = false;
 	private NotificationPanel notificationPanel;
 	private List<Player> players;
 	private Round currentRound;
 	private String difficulty;
 	private String mapType;
 	private Login window;
 	private TownView town;
 	private JFrame frame;
 	private int roundNumber;
 	private int[] foodRequirements={3,3,3,3,4,4,4,4,5,5,5,5};
 	public static final char[][] STANDARD_MAP = {{'P', 'P', '1', 'P', 'R', 'P', '3', 'P', 'P'},
 	                                             {'P', '1', 'P', 'P', 'R', 'P', 'P', 'P', '3'},
 	                                             {'3', 'P', 'P', 'P', 'T', 'P', 'P', 'P', '1'},
 	                                             {'P', '2', 'P', 'P', 'R', 'P', '2', 'P', 'P'},
                                                  {'P', 'P', '2', 'P', 'R', 'P', 'P', 'P', '2'}};
 	
 	/**
 	 * Constructor for the game class
 	 */
 	public Game() {
 		players = new ArrayList<Player>();
 	//	gamePanel = new GamePanel(mapParser(STANDARD_MAP), this);
 	}
 	
 	public JFrame getFrame()
 	{
 		return frame;
 	}
 	
 	//TODO figure out a better way to have components visible everywhere to repaint
 	/**
 	 * This method sets the notification panel for the game.
 	 * @param notification panel to set
 	 */
 	public void setNotificationPanel(NotificationPanel p) {
 		notificationPanel = p;
 	}
 	
 	/**
 	 * returns the notification panel used in the Game class
 	 * @return the notification panel being used
 	 */
 	public NotificationPanel getNotificationPanel() {
 		return notificationPanel;
 	}
 	
 	public String getDifficulty()
 	{
 		return difficulty;
 	}
 	
 	public String getMapType()
 	{
 		return mapType;
 	}
 	
 	/**
 	 * Calls the login class and initialized the login sequence
 	 */
 	public void loginBegin() {
 		EventQueue.invokeLater(new Runnable(){			
 			public void run() {
 				try {
 					window = new Login(Game.this);	
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		}); 
 	}
 
 	public void loadOldGame(JFrame login)
 	{
 		login.setVisible(false);
 		frame.setVisible(true);
 		gamePanel.beginDisplay(frame);
 		currentRound.setNotificationPanel(notificationPanel);
 		if (getCurrentTurn() instanceof LandSelectionTurn)
 		{
 			System.out.println("land selection round");
 			LandSelectionTurn currentTurn = ((LandSelectionTurn)getCurrentTurn());
 			currentTurn.pass();
 			currentTurn = ((LandSelectionTurn)getCurrentTurn());
 			currentTurn.pass();
 			nextRound();
 			town = new TownView(getCurrentTurn());
 			town.displayTownSquare();
 			getCurrentTurn().start();
 		}
 		else
 		{
 			currentRound.setNotificationPanel(notificationPanel);
 			getCurrentTurn().endTurn();
 			Turn t = new Turn(getCurrentTurn().getRound(), getCurrentTurn().getPlayer());
 			getCurrentTurn().getRound().setCurrentTurn(t);
 			town = new TownView(getCurrentTurn());
 			town.displayTownSquare();
 			getCurrentTurn().start();
 			//getCurrentTurn().getRound().resetUp();
 			//getCurrentTurn().start();
 			/*
 			Turn t = new Turn(getCurrentTurn().getRound(), getCurrentTurn().getPlayer());
 			getCurrentTurn().getRound().setCurrentTurn(t);
 			getCurrentTurn().start();
 			*/
 		}
 		
 		/*
 		else
 		{
 			town.displayTownSquare();
 		}
 		
 		*/
 	}
 	
 	/**
 	 *  Called by Login class when the Login has finished
 	 * @param frame Represents the frame on which to display the visuals
 	 */
 	public void loginComplete(JFrame frame,String mapType,String difficulty){
 		System.out.println("Login Completed...");
 		this.difficulty = window.getDifficulty();
 		this.mapType = window.getMapType();
 		players = window.getPlayers();
 		this.frame=frame;
 		this.mapType=mapType;
 		if(mapType=="Standard"){
 			gamePanel = new GamePanel(mapParser(STANDARD_MAP), this);
 		}
 		else{
 			gamePanel = new GamePanel(mapParser(randomMap()), this);
 		}
 		this.difficulty=difficulty;
 		int food=4;
 		int energy=2;
 		
 		if(difficulty=="Standard"||difficulty=="Tournament"){
 			Store store = new Store(false);
 			for(Player p:players)
 			{
 				p.setFood(food);
 				p.setEnergy(energy);
 			}			
 		}
 		else
 		{
 			Store store = new Store(true);
 		}
 		
 		play();
 	}
 	
 	/**
 	 * Starts the land selection phase
 	 */
 	private void landSelectionBegin() {
 		getCurrentTurn().start();
 	}	
 	
 	/**
 	 * Initializes the land selection round and shows the gamepanel, notification panel
 	 */
 	public boolean play() { 
 		LandSelectionRound round = new LandSelectionRound(players, this);
 		currentRound = round;
 		gamePanel.beginDisplay(frame);
 		round.setNotificationPanel(notificationPanel);
 		landSelectionBegin();
 		return false; 
 		
 	}
 	
 	/**
 	 * Adds a player to the game
 	 * Between 2 and 4 players may play at once 
 	 * @param p The player to add to the game
 	 */
 	public void addPlayer(Player p){
 		players.add(p);
 	}
 	
 	/**
 	 * @return The list of players currently added to the game
 	 */
 	public List<Player> getPlayers() {
 		return players;
 	}
 	
 	/**
 	 * Returns the current round.
 	 * @return Current round of the game
 	 */
 	public Round getCurrentRound() {
 		return currentRound;
 	}
 	
 	/**
 	 * Returns the current round.
 	 * @return Current turn of the game
 	 */
 	public Turn getCurrentTurn() {
 		return currentRound.getCurrentTurn();
 	}
 	
 	/**
 	 * Returns the round that should be next in the game sequence
 	 * @return the next round
 	 */
 	public Round nextRound() 
 	{
 		currentRound = new Round(roundNumber, players, foodRequirements[roundNumber], 0, this);
 		roundNumber++;
 		return currentRound;
 	}
 	
 	public ArrayList<Mule> getMulesOnProperty()
 	{
 		return gamePanel.getMulesOnMap();
 	}
 	
 	
 	/**
 	 * Main method for the Game class that creates an Game
 	 * object and initializes the login sequence.
 	 */
 	public static void main(String[] args) 
 	{
 		Game game = new Game();
 		game.loginBegin();
 	}
 	
 	public static Game LoadGame()
 	{
 		Game g = null;
 		try
 		{
 			System.out.println("Attempt to load game...");
 			FileInputStream str = new FileInputStream("game.ser");
 			ObjectInputStream reader = new ObjectInputStream(str);
 			g = (Game) reader.readObject();
 			reader.close();	
 			System.out.println("Current player: " + g.getCurrentTurn().getPlayer().toString());
 			
 			return g;
 		}
 		catch (Exception e)
 		{
 			System.out.println("No old game found. New game created.");
 			return null;
 		}
 	}
 	
 	public static void SaveGame(Game game) throws Exception
 	{
 		try
 		{
 			System.out.println("Attempt to save game");
 			
 			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("game.ser"));
 			out.writeObject(game);
 			out.close();
 			
 			ByteArrayOutputStream bos = new ByteArrayOutputStream();
 			out = new ObjectOutputStream(bos);
 			out.writeObject(game);
 			out.close();
 			
 			//byte[] buf = bos.toByteArray();
 		}
 		catch (Exception e)
 		{
 			throw new Exception();
 		}
 	}
 	
 	public char[][] randomMap()
 	{
 		Random r = new Random();
 		char[][] map=new char[5][9];
 		for(int i=0;i<map.length;i++){
 			for(int j=0;j<map[0].length;j++){
 				map[i][j]='P';
 			}
 		}
 		char[] m={'1','2','3'};
 		int numMountians = 10+ r.nextInt(4);
 		for(int i=0;i<numMountians;i++){
 			map[r.nextInt(map.length)][r.nextInt(map[0].length)]=m[r.nextInt(m.length)];
 		}
 		
 		int riverIndex=r.nextInt(map[0].length);
 		for(int i=0;i<map.length;i++){
 			map[i][riverIndex]='R';
 		} 
 		map[2][4]='T';
 		return map;
 	}
 	
 	/**
 	 * Private method that parses 2D char array into a 2D tile array.
 	 * @param 2D char array representing map. 'T' represents town, 'P' represents plain, '1' is mountain one, '2' is mountain 2, and '3' is mountain three.
 	 * @return tile representation of the char array
 	 */
 	private static Tile[][] mapParser(char[][] map) {
 	    Tile[][] parsedMap = new Tile[map.length][map[0].length];
 	    for (int i = 0; i < map.length; i++) {
 	        for (int j = 0; j < map[i].length; j++) {
 	            if (map[i][j] == 'R')
 	                parsedMap[i][j] = new River();
 	            else if (map[i][j] == 'P')
 	                parsedMap[i][j] = new Plain();
 	            else if (map[i][j] == '1')
 	                parsedMap[i][j] = new Mountain(Mountain.TYPE_1);
 	            else if (map[i][j] == '2')
 	                parsedMap[i][j] = new Mountain(Mountain.TYPE_2);
 	            else if (map[i][j] == '3')
 	                parsedMap[i][j] = new Mountain(Mountain.TYPE_3);
 	            else if (map[i][j] == 'T')
 	                parsedMap[i][j] = new Town();
 
 	        }
 	    }
 	    return parsedMap;
 	}
 	
 	/**
      * Prepares the game for mule placement
      * 
      * @param the mule to place
      */
 	public void setMule(Mule mule)
 	{
 		gamePanel.turnMuleEmplacementOn(mule);
 		town.getFrame().setVisible(false);
 	}
 	
 	/**
      * Causes player to return to the townsquare after placing a mule
      */
 	public void endMuleEmplacement() {
 		town.returnToTown(town.getFrame());
 	}
     
     /**
      * Causes the game to go to the next turn
      */
 	public void nextTurn() {
 		if(town!=null){
 			town.removeSquare();
 		}
 		getCurrentTurn().stop();
 		if (getCurrentRound().hasNextTurn()) {
 			getCurrentRound().nextTurn();
 			
 			getCurrentTurn().start();
 		} else {
 			nextRound();
 			RandomEvent.roundEventOccur(frame, players);
 			getCurrentTurn().start();
 		}
 		if (getCurrentTurn() instanceof LandSelectionTurn)
 		{
 			return;
 		}
 		
 		RandomEvent.turnEventOccur(frame, players, getCurrentTurn().getPlayer());
 		town = new TownView(getCurrentTurn());
 		town.displayTownSquare();
 	}
 	
 	public TownView getTownView() {
 	    return town;
 	}
 	
 	/**
 	 * @return TownNotificationPanel
 	 */
 	public TownNotificationPanel getTownNotificationPanel()
 	{
 		if (town == null)
 		{
 			return null;
 		}
 		
 		return town.getTownNotificationPanel();
 	}
 
 }
