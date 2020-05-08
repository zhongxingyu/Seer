 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package game;
 
 import gui.MenuGUI;
 
 /**
  *
  * @author Michael
  */
 public class Progression implements Runnable{
     
     private GameState gameState;
     private boolean istwoPlayer = false;
     public boolean playerOneTurn = true;
     
     private int player1Score = 0;
     private int player2Score = 0;
     
     public Progression(GameState gs, boolean twoPlayer){
         gameState = gs;
         istwoPlayer = twoPlayer;
     }
 
     @Override
     public void run() {
         checkGameProgress();
         spawnAlien();
     }
 
     private void spawnAlien() {
         //check if alien does not already exist and difficulty says to spawn one
         //Note: asteroid heading != 0 looks really strange, so always set to 0.
        if (isAlienDestroyed() && Difficulty.spawnAlien()) {
             gameState.addAlienShip(new AlienShip(new float[]{Difficulty.randomAlienVelocity(), Difficulty.randomAlienVelocity()}, 0, 
                     new int[]{Difficulty.randomXPos(), Difficulty.randomYPos()}, gameState));
         }
     }
     
     private void checkGameProgress() {
         //single player
         if (!istwoPlayer) {
             if (isPlayerDead()) {
                 gameState.setPlayer1Score(gameState.getCurrentScore());
                 Logic.stopTimer();
                 Logic.displayGameOver();
             } //if the player is not dead, check for level completion and move to next level
             else if (allAsteroidsDestroyed() && isAlienDestroyed()) {
                 setupLevel(gameState.getLevel() + 1);
             }
         }
         //two player
         else
         {
            //if a player dies, need to find out if first or second player.
             if (isPlayerDead())
             {
                 if (playerOneTurn)
                 {
                     //restart game for player 2, save player 1 score
                     player1Score = gameState.getCurrentScore();
                     Logic.displayPlayerTwoTurn();
                     setupInitialLevel();
                     gameState.setPlayer1Score(player1Score);
                     gameState.setPlayerTwoTurn(true);
                     playerOneTurn = false;
                 }
                 else
                 {
                     //game over: save player 2 score, put it in the game state, and stop updating
                     player2Score = gameState.getCurrentScore();
                     gameState.setPlayer1Score(player1Score);
                     gameState.setPlayer2Score(player2Score);
                     Logic.stopTimer();
                     Logic.displayWinner();
                 }
             }
             //same as 1 player
             else if (allAsteroidsDestroyed() && isAlienDestroyed()) {
                  setupLevel(gameState.getLevel() + 1);
             }
         }
     }
     
     
     private boolean isAlienDestroyed()
     {
         return gameState.getAlienShip() == null;
     }
     
     private boolean allAsteroidsDestroyed()
     {
         return gameState.getAsteroids().isEmpty();
     }
     
     private boolean isPlayerDead()
     {
         return gameState.isPlayerDead();
     }
     
     public void setupInitialLevel()
     {
         //start at level 1 (note: player ship needed, as setupLevel has as precondition that player ship != null
         gameState.addPlayerShip(new PlayerShip(new float[]{0, 0}, 0, new int[]{MenuGUI.WIDTH/2, MenuGUI.HEIGHT/2}, gameState, 3, 99, 3));
         setupLevel(1);
     }
     
     //player ship != null assumed.  if null, won't do anything (will try again next turn)
     private void setupLevel(int levelNumber) {
         PlayerShip player = gameState.getPlayerShip();
         if (player != null) {
             //save score, is player two's turn
             int oldScore = gameState.getCurrentScore();
             //player can't be null here
             int oldPlayerLives = player.getLives();
             int oldPlayerBomb = player.getBomb();
 
             boolean playerTwo = gameState.isPlayerTwoTurn();
 
             gameState.resetToDefaults();
             gameState.addPlayerShip(new PlayerShip(new float[]{0, 0}, 0, new int[]{MenuGUI.WIDTH / 2, MenuGUI.HEIGHT / 2}, gameState, oldPlayerLives, oldPlayerBomb, 3));
             addAsteroids(levelNumber);
             gameState.setLevel(levelNumber);
             gameState.addToCurrentScore(oldScore);
             gameState.setPlayerTwoTurn(playerTwo);
         }
     }
     
     private void addAsteroids(int levelNumber)
     {
         int NumberOfAsteroids = Difficulty.spawnAsteroids(levelNumber);
 
         for (int i = 0; i < NumberOfAsteroids; i++) {
             float xVel = Difficulty.randomAsteroidVelocity(levelNumber);
             float yVel = Difficulty.randomAsteroidVelocity(levelNumber);
             float heading = Difficulty.randomHeading();
             int xCoord = Difficulty.randomXPos();
             int yCoord = Difficulty.randomYPos();
             int size = Difficulty.randomAsteroidSize();
             gameState.addAsteroid(new Asteroid(new float[]{xVel, yVel}, heading, new int[]{xCoord, yCoord}, gameState, size));
         }
     }
 
 }
