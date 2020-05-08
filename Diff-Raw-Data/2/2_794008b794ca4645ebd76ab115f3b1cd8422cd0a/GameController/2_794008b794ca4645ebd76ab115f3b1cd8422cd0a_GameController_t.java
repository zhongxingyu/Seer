 
 import java.util.ArrayList;
 
 public class GameController {
     /**
      * Main method of the MULE game
      * Initializes the environment and starts the game
      *
      * Currently a stub method 
      *
      * @param args command line arguments - currently don't do anything
      */
     public static void main(String[] args) {
         System.out.println("Main Method!");
         GameController game = new GameController();
     }
 	
 	public static final int EASY_DIFFICULTY = 1;
 	public static final int MEDIUM_DIFFICULTY = 2;
 	public static final int HARD_DIFFICULTY = 3;
 	public static final int DIFFICULTY_NOT_SET = 0;
 
 	private int difficulty;
    private int roundNumber = 1;
 	private Renderer renderer;
     private int currPlayer;
     private int numPlayers;
     private Map map;
     private String state;
     private ArrayList<Player> players;
     private long startTime;
     private long stopTime;
     private Integer elapsedTime;
 
 	public GameController() {
 		renderer = new Renderer();
         currPlayer = 0;
         numPlayers = 1;
         state = "";
         players = new ArrayList<Player>();
         playGame();
 	}
 
     private void playGame() {
         startGame();
         //landSelection();
         mainGame();
     }
 
 	private void startGame() {
 
         state = "intro";
         boolean initializing = true;
 
         // variables we are collecting
         int difficulty = 0;
         map = null;
         ArrayList<String> takenColors = new ArrayList<String>();
 
         while(initializing) {
 
             System.out.println("State: " + state);
             // Introduction Screen
             if (state.equals("intro")) {
                 String action = renderer.drawIntroScreen()[0];
                 if (action.equals("quit")) {
                     state = "quit";
                 }
                 else if (action.equals("load")) {
                     state = "load";
                 }
                 else {
                     state = "setup";
                 }
             }
 
             // Setup Screen
             else if (state.equals("setup")) {
                String[] results = renderer.drawSetupScreen();
                String action = results[0];
                difficulty = Integer.parseInt(results[1]);
                numPlayers = Integer.parseInt(results[2]);
                if (action.equals("okay")) {
                    state = "map";
                }
                else {
                    state = "intro";
                }
             }
 
             // Map Screen
             else if (state.equals("map")) {
                 String[] results = renderer.drawMapScreen();
                 String action = results[0];
                 map = new Map(Integer.parseInt(results[1]));
                 System.out.println("Map created with num " + Integer.parseInt(results[1]));
                 if (action.equals("okay")) {
                     state = "player";
                 }
                 else {
                     state = "setup";
                 }
             }
 
             // Character selection screen
             else if (state.equals("player")) {
                 String[] results = renderer.drawCharacterScreen(players);
                 String action = results[0];
                 if (action.equals("back")) {
                     state = "map";
                 }
                 else {
                     if (takenColors.contains(results[3])) {
                         System.out.println("That color is taken!");
                         continue;
                     }
 
                     try {
                         players.add(new Player(results[2], results[1], results[3], difficulty));
                         takenColors.add(results[3]);
                     }
                     catch (Exception exception) {
                         System.out.println(exception);
                         continue;
                     }
 
                     // only move on if we have all the players
                     if (--numPlayers == 0) {
                         state = "game";
                     }
                 }
             }
 
             else if (state.equals("game")) {
                 initializing = false;
             }
 
             // quit state
             else {
                 System.out.println("State: " + state);
                 System.exit(0);
                 initializing = false;
             }
         }
         System.out.println("Game initialization complete with paramaters:\n");
         numPlayers = players.size();
         System.out.println("Difficulty: " + difficulty);
         System.out.println("NumPlayers: " + numPlayers);
         System.out.println("Map: " + map.getMapNum());
         for (Player p : players) {
             System.out.println(p);
         }
         System.out.println("Starting game...\n\n");
 	}
 
     private void mainGame() {
 
         boolean initializing = true;
         renderer.startTimer(getTime());
         startTime = System.currentTimeMillis();
 
         while(initializing) {
             if (state.equals("game")){
                 String[] results = renderer.drawMainGameScreen(map, players, currPlayer);
 
                 if (results[0].equals("time")) {
                     System.out.println("Time's up, switching player");
                     switchPlayer();
                 }
 
                 else {
                     int tileSelection = Integer.parseInt(results[0]);
                     if (!(map.getTiles()[tileSelection].getType().equals("town"))) {
                         landSelection(tileSelection, map);
                     }
                     else {
                         state = "town";
                     }
                 }
             }
 
             else if (state.equals("town")) {
                 String[] results = renderer.drawTownScreen(players, currPlayer);
 
                 if (results[0].equals("time")) {
                     System.out.println("Time's up, switching player");
                     switchPlayer();
                 }
 
                 else{
                     if (results[0].equals("pub")){
                         pub();
                     }
                     else {
                         state = "game";
                     }
                 } 
             }
 
             else {
                 System.out.println("done");
                 initializing = false;
             }
         }
     }
 
     private void switchPlayer() {
         if(currPlayer == (numPlayers-1)){
             this.roundNumber++;
             reorderPlayers();
             checkForEnd();
         }
         currPlayer = (currPlayer + 1) % numPlayers;
         renderer.restartTimer(getTime());
         startTime = System.currentTimeMillis();
         state = "game";
     }
 
     private int getTime() {
         int[] foodReqs = {3, 3, 3, 3, 4, 4, 4, 4, 5, 5, 5, 5};
         Player player = players.get(currPlayer);
         if (player.getFood() >= foodReqs[roundNumber] && player.getEnergy() >= player.getMulesPlaced()) {
             System.out.println("Timer set to: " + 50000);
             return 50000;
         }
 
         else if (player.getFood() > 0) {
             System.out.println("Timer set to: " + 30000);
             return 30000;
         }
         System.out.println("Timer set to: " + 5000);
         return 5000;
     }
 
     private void reorderPlayers() {
         // insertion sort is the fastest sort for < 30
         System.out.println("Reordering players");
         for (int i = 0; i < players.size(); i++) {
             Player playerToAdd = players.get(i);
             int index = i;
             while (index > 0 && (playerToAdd.getMoney() < players.get(index - 1).getMoney())) {
                 players.set(index, players.get(index - 1));
                 index--;
             }
             players.set(index, playerToAdd);
         }
     }
 
     private void checkForEnd() {
         if (roundNumber < 12) {
             return;
         }
 
         String winningPlayer = players.get(numPlayers - 1).getName();
         System.out.println(winningPlayer + " is the winner!");
         System.exit(0);
     }
 
     /**
      * landSelection takes land clicked and assigns it to currPlayer
      * @param tileSelection The tile current player selected
      * @param map Used to set owner of tile to currPlayer
      */
     private void landSelection(int tileSelection, Map map) {
         if(map.getOwnerOfTile(tileSelection) != null){
             System.out.println("Sorry, tile already owned by " + map.getOwnerOfTile(tileSelection));
         }
         else{
             LandOffice landOffice = new LandOffice(roundNumber);
             boolean bought = landOffice.buyProperty(tileSelection, players, currPlayer, map);
             //stopTime = System.currentTimeMillis();
 
             if (bought) {
                 switchPlayer();
             }
         }
     }
 
     /**
      * pub allows currPlayer to gamble and make more $$$$.
      */
     private void pub(){
         stopTime = System.currentTimeMillis();
         Integer elapsedTime = ((int)(long)(stopTime - startTime))/1000;
         int timeRemaining = 50 - elapsedTime;
         System.out.println("Elapsed time was " + elapsedTime + " seconds.");
         System.out.println("Player had " + timeRemaining + " seconds remaining.");
 
         Pub pub = new Pub(roundNumber, timeRemaining);
         pub.gamble(players, currPlayer);
         switchPlayer();
     }
 
 	private void showLoadGameSavePartial(Save savedGame){
 
 	}
 	
 	private void showLoadScreen(){
 		Save[] savedGames = getSavedGames();
 		LoadScreenModel model = new LoadScreenModel();
 		model.setSavedGames(savedGames);
 		renderer.drawLoadScreen(model);
 	}
     /**
      * Getter method for the game save
      *
      * @return null
      */
 	private Save[] getSavedGames() {
 		return null;
 		//Query database for saved games
 	}
 
     /**
      * Getter method for the game's difficulty setting
      *
      * @return Game's difficulty setting
      */
 	public int getDifficulty() {
 		return difficulty;
 	}
 
     /**
      * Getter method for current round
      *
      * @return Game's current round
      */
     public int getRoundNumber() {
         return roundNumber;
     }
 }
