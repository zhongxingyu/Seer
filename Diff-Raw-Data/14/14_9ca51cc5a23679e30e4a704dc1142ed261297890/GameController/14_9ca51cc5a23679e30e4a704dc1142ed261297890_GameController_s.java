 /**
  * This class will control the flow of the game and enforce rules
  */
 
 import java.util.HashMap;
 
 public class GameController {
     private boolean gameReady;
     private int numPlayers;
     private int numShips;
     private AI singlePlayerAI;
     private final GameView gv;
     private final PlayerBoard[] playerBoards;
     private final Radar[] playerRadars;
     private final HashMap<Integer, Ship> ships;
     private final int PLAYER_ONE = 1;
     private final int PLAYER_TWO = 2;
    private final int AI = 2;
 
     /* This is required because if we access index PLAYER_TWO in the
      * playerBoards or playerRadars array we will get an
      * IndexOutOfBoundsException. We don't want that. */
     private final int PLAYER_ONE_BOARD = 0;
     private final int PLAYER_TWO_BOARD = 1;
     private final int AI_BOARD = 1;
 
 
     public GameController() {
         this.gameReady = false;
         this.gv = new GameView();
         this.ships = new HashMap<Integer, Ship>();
         this.numPlayers = -1;
         this.numShips = 0;
 
         this.playerBoards = new PlayerBoard[2];
         for(int i = 0; i < this.playerBoards.length; i++)
             playerBoards[i] = new PlayerBoard();
 
         this.playerRadars = new Radar[2];
         for(int i = 0; i < this.playerRadars.length; i++)
             playerRadars[i] = new Radar();
 
         shipsInit();
     }
 
     private void shipsInit() {
         this.ships.put(0, new Ship("Patrol Boat", 2));      this.numShips++;
         this.ships.put(1, new Ship("Destroyer", 3));        this.numShips++;
         this.ships.put(2, new Ship("Submarine", 3));        this.numShips++;
         this.ships.put(3, new Ship("Battleship", 4));       this.numShips++;
         this.ships.put(4, new Ship("Aircraft Carrier", 5)); this.numShips++;
     }
 
     /**
      * Do the initial setup of the board required before play begins
      */
     public void gameSetup() {
         this.gv.displayWelcome();
 
         /* Loop til they get the point you get 1 or 2 players */
         while(this.numPlayers < 1 || this.numPlayers > 2)
             this.numPlayers = gv.getNumPlayers();
 
         switch(this.numPlayers) {
             /* Default to single player mode */
             default:
             case 1:
                 setupPlayerBoard(PLAYER_ONE);
                 singlePlayerAI = new AI();
                 setupAIBoard();
                 break;
             case 2:
                 setupPlayerBoard(PLAYER_ONE);
                 this.gv.clearScreen();
                 setupPlayerBoard(PLAYER_TWO);
                 this.gv.clearScreen();
                 break;
         }
 
         this.gameReady = true;
     }
 
     /* Get the player to put all their ships on the board */
     private void setupPlayerBoard(int playerNum) {
         int boardNum = playerNum-1; // player 1 has board 0
 
         for(int i = 0; i < this.numShips; i++) {
             /* Display their current board setup and get next ship */
             System.out.println(String.format("Player %d's board", playerNum));
             gv.displayBoard(this.playerBoards[boardNum]);
 
             Coordinate shipLocation = new Coordinate();
             char orientation = '\0';
             boolean validSpot = true;
             Ship ship = ships.get(i);
 
             while(shipLocation.row < 1 || shipLocation.row > 10 || orientation == '\0' ||
                   shipLocation.col < 1 || shipLocation.col > 10 || !validSpot) {
                 shipLocation = gv.getShipPlacement(playerNum, ship);
                 orientation = gv.getShipOrientation();
                 validSpot = this.playerBoards[boardNum].placeShip(shipLocation.row, shipLocation.col, ship.size, orientation, false);
             }
         }
 
         System.out.println("Your final board setup is");
         gv.displayBoard(this.playerBoards[boardNum]);
         System.out.println();
     }
 
     /* Set up the board for the AI. Will not print information regarding the
      * placement or attempts at placement to the screen. It will just display a
      * message once it is done */
     private void setupAIBoard() {
         for(int i = 0; i < this.numShips; i++) {
             Coordinate shipLocation = new Coordinate();
             char orientation = '\0';
             boolean validSpot = true;
             Ship ship = ships.get(i);
 
             while(shipLocation.row < 1 || shipLocation.row > 10 || orientation == '\0' ||
                   shipLocation.col < 1 || shipLocation.col > 10 || !validSpot) {
                 shipLocation = singlePlayerAI.getShipPlacement();
                 orientation = singlePlayerAI.getShipOrientation();
                 validSpot = this.playerBoards[AI_BOARD].placeShip(shipLocation.row, shipLocation.col, ship.size, orientation, true);
             }
         }
     }
 
 
     /**
      * Play the game that has been setup
      */
     public void play() {
         /* We cant play if we havent setup the boards */
         if(!this.gameReady) {
             System.err.println("The game has not yet been setup. Please call the gameSetup method before calling play");
             return;
         }
 
         switch(this.numPlayers) {
             /* Default to single player mode */
             default:
             case 1:
                 playSinglePlayer();
                 break;
             case 2:
                 playTwoPlayer();
                 break;
         }
     }
 
     private void playSinglePlayer() {
         boolean playerOnesTurn = true; // could use a counter and do the % 2 trick but meh
 
         while(playerBoards[0].hasShipsLeft() && playerBoards[1].hasShipsLeft()) {
             if(playerOnesTurn) {
                 /* Display the options to the user and see if they want to
                  * display the board or radar, or if they want to fire */
                 char playerOption = 'z';
                 while(playerOption != 'f') {
                     playerOption = gv.getPlayerOption(PLAYER_ONE);
 
                     if(playerOption == 'b')
                         this.gv.displayBoard(playerBoards[PLAYER_ONE_BOARD]);
                     else if(playerOption == 'r')
                         this.gv.displayBoard(playerRadars[PLAYER_ONE_BOARD]);
                 }
 
                 Coordinate shotLoc = new Coordinate();
 
                 /* Loop til we get a valid shot from the user */
                 while(shotLoc.row < 1 || shotLoc.row > 10 || shotLoc.col < 1 || shotLoc.col > 10) {
                     shotLoc = this.gv.fire(PLAYER_ONE);
                 }
 
                 boolean shotHit = this.playerBoards[PLAYER_TWO_BOARD].checkShot(shotLoc.row, shotLoc.col);
                 char hitOrMiss = shotHit ? 'h' : 'm'; // 'h' if hit 'm' if miss
                 this.playerRadars[PLAYER_ONE_BOARD].markRadar(shotLoc.row, shotLoc.col, hitOrMiss);
                 this.gv.showLastShot(shotLoc.row, shotLoc.col, hitOrMiss);
             }
             else { // AI goes
                 Coordinate shotLoc = new Coordinate();
 
                 /* Loop til we get a valid shot from the user */
                 while(shotLoc.row < 1 || shotLoc.row > 10 || shotLoc.col < 1 || shotLoc.col > 10) {
                     shotLoc = this.singlePlayerAI.fire();
                 }
 
                 boolean shotHit = this.playerBoards[PLAYER_ONE_BOARD].checkShot(shotLoc.row, shotLoc.col);
                 char hitOrMiss = shotHit ? 'h' : 'm'; // 'h' if hit 'm' if miss
                 this.playerRadars[AI_BOARD].markRadar(shotLoc.row, shotLoc.col, hitOrMiss);
                 this.gv.showLastAIShot(shotLoc.row, shotLoc.col, hitOrMiss);
             }
 
             /* If it was player ones turn now it is player twos. If it was player twos
              * turn then it is not player ones. */
             playerOnesTurn = playerOnesTurn ? false : true;
         }
 
         int winner = playerBoards[0].hasShipsLeft() ? 1 : 2;
         System.out.println(String.format("Congratulations player %d you win! Thank you for playing Battleship.", winner));
     }
 
     private void playTwoPlayer() {
         boolean playerOnesTurn = true; // could use a counter and do the % 2 trick but meh
 
         while(playerBoards[0].hasShipsLeft() && playerBoards[1].hasShipsLeft()) {
             if(playerOnesTurn) {
                 /* Display the options to the user and see if they want to
                  * display the board or radar, or if they want to fire */
                 char playerOption = 'z';
                 while(playerOption != 'f') {
                     playerOption = gv.getPlayerOption(PLAYER_ONE);
 
                     if(playerOption == 'b')
                         this.gv.displayBoard(playerBoards[PLAYER_ONE_BOARD]);
                     else if(playerOption == 'r')
                         this.gv.displayBoard(playerRadars[PLAYER_ONE_BOARD]);
                 }
 
                 Coordinate shotLoc = new Coordinate();
 
                 /* Loop til we get a valid shot from the user */
                 while(shotLoc.row < 1 || shotLoc.row > 10 || shotLoc.col < 1 || shotLoc.col > 10) {
                     shotLoc = this.gv.fire(PLAYER_ONE);
                 }
 
                 boolean shotHit = this.playerBoards[PLAYER_TWO_BOARD].checkShot(shotLoc.row, shotLoc.col);
                 char hitOrMiss = shotHit ? 'h' : 'm'; // 'h' if hit 'm' if miss
                 this.playerRadars[PLAYER_ONE_BOARD].markRadar(shotLoc.row, shotLoc.col, hitOrMiss);
 
                 this.gv.clearScreen();
                 this.gv.showLastShot(shotLoc.row, shotLoc.col, hitOrMiss);
             }
             else { // player two goes
                 /* Display the options to the user and see if they want to
                  * display the board or radar, or if they want to fire */
                 char playerOption = 'z';
                 while(playerOption != 'f') {
                     playerOption = gv.getPlayerOption(PLAYER_TWO);
 
                     if(playerOption == 'b')
                         this.gv.displayBoard(playerBoards[PLAYER_TWO_BOARD]);
                     else if(playerOption == 'r')
                         this.gv.displayBoard(playerRadars[PLAYER_TWO_BOARD]);
                 }
 
                 Coordinate shotLoc = new Coordinate();
 
                 /* Loop til we get a valid shot from the user */
                 while(shotLoc.row < 1 || shotLoc.row > 10 || shotLoc.col < 1 || shotLoc.col > 10) {
                     shotLoc = this.gv.fire(PLAYER_TWO);
                 }
 
                 boolean shotHit = this.playerBoards[PLAYER_ONE_BOARD].checkShot(shotLoc.row, shotLoc.col);
                 char hitOrMiss = shotHit ? 'h' : 'm'; // 'h' if hit 'm' if miss
                 this.playerRadars[PLAYER_TWO_BOARD].markRadar(shotLoc.row, shotLoc.col, hitOrMiss);
 
                 this.gv.clearScreen();
                 this.gv.showLastShot(shotLoc.row, shotLoc.col, hitOrMiss);
             }
 
             /* If it was player ones turn now it is player twos. If it was player twos
              * turn then it is not player ones. */
             playerOnesTurn = playerOnesTurn ? false : true;
         }
 
         int winner = playerBoards[0].hasShipsLeft() ? 1 : 2;
         System.out.println(String.format("Congratulations player %d you win! Thank you for playing Battleship.", winner));
     }
 }
