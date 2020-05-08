 import greenfoot.*;
 import java.io.IOException;
 
 public class SkyscraperWorld extends World {
     private final int WINNING_LEVEL = 5;
     private boolean addScore;
     public Counter scoreCounter;
     private Player player;
     private Player.PlayerType playerType;
     private int levelCompletePoints;
     private int timeCount = 0;
     private int currentLevel;
     private Ground[] Ground;
     private MovingBrick[] MovingBricks;
     private MovingBrick2[] MovingBricks2;
     private MovingBrickUp[] MovingBricksUp;
     private Brick[] Bricks;
     private Coin[] Coins;
     private MovingWater movingWater;
 
     //GreenfootSound backgroundMusic = new GreenfootSound("zeerstoer.mp3");
 
     public SkyscraperWorld(Player.PlayerType type)  {
         super(80, 80, 10);
         playerType = type;
         currentLevel = 1;
         levelCompletePoints = loadLevel(currentLevel);
         //backgroundMusic.playLoop();
         
                 setPaintOrder(GameOverScreen.class, /*Overlay.class,*/ Counter.class, MenuBar.class, MovingWater.class, Player.class, Coin.class, Surface.class);
         
         
         
         /**General stuff**/
         
         addObject(new MenuBar(), 39, 75);
 
         scoreCounter = new Counter("Score: ");
         addObject(scoreCounter, 6, 74);
     }
     
     public void act(){
 
         
         // Check for death, level completion and game completion:
         
         if((currentLevel == 1)&&(addScore == false)&&((player.getY() <= 11)&&(player.getX() >=62))){
                 scoreCounter.add(10);
                 addScore = true;
             } 
             
         if((currentLevel == 2)&&(addScore == false)&&((player.getY() <= 10)&&(player.getX() >=63))){
                 scoreCounter.add(10);
                 addScore = true;
             } 
         
         if (checkLevelComplete())
         {
             // If level is complete, call purge() to remove all objects
             purge();
             // Increase level
             currentLevel++;
             // Set level clear goal
             levelCompletePoints = loadLevel(currentLevel);
         }
         // Crude way to "win" the game
         else if (currentLevel == WINNING_LEVEL)
         {
             winGame();
         }
         // Increment counter
         timeCount++;
     }
 
     public boolean checkLevelComplete()
     {
         if (scoreCounter.getValue() >= levelCompletePoints)
             return true;
         else
             return false;
     }
 
     public void gameOver() {
         saveHighScore();
         Greenfoot.setWorld(new GameOverWorld(Game.SKYSCRAPER_GAME));
     }
     
     public void winGame() {
         Greenfoot.setWorld(new HighScoreWorld(Game.SKYSCRAPER_GAME));
     }
     
     private void saveHighScore() {
         HighScore highScore = HighScore.askName(scoreCounter.getValue());
         try {
             highScore.save(HighScore.defaultFilenameForGame(Game.SKYSCRAPER_GAME));
         } catch (IOException e) {
             e.printStackTrace();
         }        
     }
     
     public void purge()
     {
         if (Ground != null)
         {
             for (int i = 0; i < Ground.length; i++)
             {
                 removeObject(Ground[i]);
             }
         }
         if (Bricks != null)
         {
             for (int i = 0; i < Bricks.length; i++)
             {
                 removeObject(Bricks[i]);
             }
         }
         if (MovingBricks != null)
         {
             for (int i = 0; i < MovingBricks.length; i++)
             {
                 removeObject(MovingBricks[i]);
             }
         }
         if (MovingBricks2 != null)
         {
             for (int i = 0; i < MovingBricks2.length; i++)
             {
                 removeObject(MovingBricks2[i]);
             }
         }
         if (MovingBricksUp != null)
         {
             for (int i = 0; i < MovingBricksUp.length; i++)
             {
                 removeObject(MovingBricksUp[i]);
             }
         }
         if (Coins != null)
         {
             for (int i = 0; i < Coins.length; i++)
             {
                 removeObject(Coins[i]);
             }
         }
         removeObject(player);
        
        movingWater.setLevel(20);
     }
 
     public int loadLevel (int lvl)
     {
         if (lvl == 1)
         {
             
             Ground = new Ground[15];
             MovingBricks = new MovingBrick[2];
             Bricks = new Brick[30];
             Coins = new Coin[4];
             
             //MovingBricks distance's
             MovingBricks[0] = new MovingBrick(5, 36);
             MovingBricks[1] = new MovingBrick(17, 58);
             
         
 
             for (int i = 0; i < Ground.length; i++)
             {
                 Ground[i] = new Ground ();
             }   
             
             for (int i = 0; i < Bricks.length; i++)
             {
                 Bricks[i] = new Brick();
             }
             
             for (int i = 0; i < Coins.length; i++)
             {
                 Coins[i] = new Coin();
             }
             
             //first floor
             addObject(Ground[0], 0, 69);
             addObject(Ground[1], 3, 69);
             addObject(Ground[2], 7, 69);
             addObject(Ground[3], 11, 69);
             addObject(Ground[4], 15, 69);
             addObject(Ground[5], 19, 69);
             addObject(Ground[6], 23, 69);
             addObject(Ground[7], 27, 69);
             addObject(Ground[8], 31, 69);
             addObject(Ground[9], 35, 69);
             
             //stairs
             addObject(Bricks[0], 35, 49);
             addObject(Bricks[1], 35, 53);
             addObject(Bricks[2], 35, 57);
             addObject(Bricks[3], 35, 61);
             addObject(Bricks[4], 35, 65);
             
             addObject(Bricks[5], 31, 65);
             addObject(Bricks[6], 31, 61);
             addObject(Bricks[7], 31, 57);
             addObject(Bricks[8], 31, 53);
             
             addObject(Bricks[9], 27, 65);
             addObject(Bricks[10], 27, 61);
             addObject(Bricks[11], 27, 57);
             
             addObject(Bricks[12], 23, 65);
             addObject(Bricks[13], 23, 61);
             
             addObject(Bricks[14], 19, 65);
             
             //jump parts
             addObject(Bricks[15], 45, 49);
             addObject(Bricks[16], 56, 49);
             addObject(Bricks[17], 67, 49);
             addObject(Bricks[18], 78, 49);
             addObject(Bricks[19], 78, 45);
             addObject(Bricks[20], 72, 33);
             addObject(Bricks[21], 62, 33);
             addObject(Bricks[22], 51, 33);
             addObject(Bricks[23], 40, 31);
             addObject(Bricks[24], 1, 27);
             addObject(Bricks[25], 25, 23);
             addObject(Bricks[26], 5, 23);
             addObject(Bricks[27], 1, 23);
             addObject(Bricks[28], 1, 14);
             addObject(Bricks[29], 13, 11);
             
             //MovingBricks locations
             addObject(MovingBricks[0], 25, 27);
             addObject(MovingBricks[1], 50, 11);
             
             //Coins
             addObject(Coins[0], 45, 44);
             addObject(Coins[1], 62, 40);
             addObject(Coins[2], 57, 27);
             addObject(Coins[3], 38, 2);
             
             //Finish
             addObject(Ground[10], 62, 11);
             addObject(Ground[11], 66, 11);
             addObject(Ground[12], 70, 11);
             addObject(Ground[13], 74, 11);
             addObject(Ground[14], 78, 11);
             
             
             addObject(player = Player.createPlayer(playerType), 4, 65);
             
             addObject(movingWater = new MovingWater(), 40, 71);
             
             return 50;
         }
         
         if(lvl == 2){
             Ground = new Ground[11];
             MovingBricks = new MovingBrick[1];
             MovingBricksUp = new MovingBrickUp[1];
             Bricks = new Brick[56];
             Coins = new Coin[4];
             
             for (int i = 0; i < Ground.length; i++)
             {
                 Ground[i] = new Ground ();
             }   
             
             for (int i = 0; i < Bricks.length; i++)
             {
                 Bricks[i] = new Brick();
             }
             
             for (int i = 0; i < Coins.length; i++)
             {
                 Coins[i] = new Coin();
             }
             
             //first floor
             addObject(Ground[0], 0, 69);
             addObject(Ground[1], 3, 69);
             addObject(Ground[2], 7, 69);
             addObject(Ground[3], 11, 69);
             addObject(Ground[4], 15, 69);
             addObject(Ground[5], 19, 69);
             addObject(Ground[6], 23, 69);
             
             addObject(Bricks[0], 15, 65);
             addObject(Bricks[1], 19, 65);
             addObject(Bricks[2], 23, 65);
             addObject(Bricks[3], 19, 61);
             addObject(Bricks[4], 23, 61);
             addObject(Bricks[5], 23, 57);
             addObject(Bricks[6], 23, 53);
             addObject(Bricks[7], 27, 53);
             addObject(Bricks[8], 31, 53);
             addObject(Bricks[9], 35, 53);
             addObject(Bricks[10], 39, 53);
             addObject(Bricks[11], 35, 49);
             addObject(Bricks[12], 39, 49);
             addObject(Bricks[13], 39, 45);
             
             addObject(Bricks[14], 0, 45);
             addObject(Bricks[15], 0, 41);
             addObject(Bricks[16], 0, 37);
             addObject(Bricks[17], 3, 45);
             addObject(Bricks[18], 3, 41);
             addObject(Bricks[19], 3, 37);
             addObject(Bricks[20], 7, 41);
             addObject(Bricks[21], 7, 37);
             addObject(Bricks[22], 11, 37);
             addObject(Bricks[23], 15, 37);
             addObject(Bricks[24], 19, 37);
             addObject(Bricks[25], 23, 37);
             addObject(Bricks[26], 27, 37);
             addObject(Bricks[27], 31, 37);
             addObject(Bricks[28], 31, 33);
             addObject(Bricks[29], 35, 33);
             addObject(Bricks[30], 35, 29);
             addObject(Bricks[31], 35, 25);
             addObject(Bricks[32], 35, 21);
             addObject(Bricks[33], 35, 17);
             addObject(Bricks[34], 35, 13);
             
             addObject(Bricks[35], 39, 29);
             addObject(Bricks[36], 43, 45);
             addObject(Bricks[37], 47, 45);
             addObject(Bricks[38], 51, 45);
             addObject(Bricks[39], 55, 45);
             addObject(Bricks[40], 47, 41);
             addObject(Bricks[41], 51, 41);
             addObject(Bricks[42], 55, 41);
             addObject(Bricks[43], 51, 37);
             addObject(Bricks[44], 55, 37);
             addObject(Bricks[45], 55, 33);
             addObject(Bricks[46], 55, 29);
             addObject(Bricks[47], 55, 25);
             addObject(Bricks[49], 55, 21);
             addObject(Bricks[50], 59, 21);
             addObject(Bricks[51], 63, 21);
             addObject(Bricks[52], 71, 21);
             addObject(Bricks[53], 71, 9);
             addObject(Bricks[54], 63, 9);
             addObject(Bricks[55], 55, 9);
             
             
             
             
             
             //MovingBricks distance's
             MovingBricks[0] = new MovingBrick(21, 39);
             MovingBricksUp[0] = new MovingBrickUp(9, 21);
             
             //MovingBricks locations
            addObject(MovingBricks[0], 45, 21);
            addObject(MovingBricksUp[0], 79, 15);
             
             //Finish
             addObject(Ground[7], 47, 9);
             addObject(Ground[8], 43, 9);
             addObject(Ground[9], 39, 9);
             addObject(Ground[10], 35, 9);
             
             
             addObject(player = Player.createPlayer(playerType), 4, 65);
             
             return 50;
         }
             
         
         if (lvl == 3) {
             Ground = new Ground[2];
             MovingBricks = new MovingBrick[7];
             MovingBricks2 = new MovingBrick2[7];
             Coins = new Coin[3];
 
             for (int i = 0; i < Ground.length; i++)
             {
                 Ground[i] = new Ground ();
             }   
             
             for (int i = 0; i < Coins.length; i++)
             {
                 Coins[i] = new Coin();
             }
             
             
             //first floor
             addObject(Ground[0], 8, 69);
             
             //MovingBricks distance's
             MovingBricks[0] = new MovingBrick(22, 79);
             MovingBricks[1] = new MovingBrick(22, 79);
             MovingBricks[2] = new MovingBrick(22, 79);
             MovingBricks[3] = new MovingBrick(22, 79);
             MovingBricks[4] = new MovingBrick(22, 79);
             MovingBricks[5] = new MovingBrick(22, 79);
             MovingBricks[6] = new MovingBrick(22, 79);
            
             MovingBricks2[0] = new MovingBrick2(22, 79);
             MovingBricks2[1] = new MovingBrick2(22, 79);
             MovingBricks2[2] = new MovingBrick2(22, 79);
             MovingBricks2[3] = new MovingBrick2(22, 79);
             MovingBricks2[4] = new MovingBrick2(22, 79);
             MovingBricks2[5] = new MovingBrick2(22, 79);
             MovingBricks2[6] = new MovingBrick2(22, 79);
             
             //MovingBricks locations
             addObject(MovingBricks[0], 50, 61);
             addObject(MovingBricks[1], 50, 53);
             addObject(MovingBricks[2], 50, 45);
             addObject(MovingBricks[3], 50, 37);
             addObject(MovingBricks[4], 50, 29);
             addObject(MovingBricks[5], 50, 21);
             addObject(MovingBricks[6], 50, 13);
             
             addObject(MovingBricks2[0], 50, 57);
             addObject(MovingBricks2[1], 50, 49); 
             addObject(MovingBricks2[2], 50, 41);
             addObject(MovingBricks2[3], 50, 33);
             addObject(MovingBricks2[4], 50, 33);
             addObject(MovingBricks2[5], 50, 25);
             addObject(MovingBricks2[6], 50, 17);
             
             //Finish
             addObject(Ground[1], 10, 11);
             
             addObject(player = Player.createPlayer(playerType), 4, 65);
             return 50;
         }
         return 0;
     }
     
     public int getWaterLevel() {
         return movingWater.getLevel();
     }
     
     public Counter getScoreCounter() {
         return scoreCounter;
     }
 
     public Player getPlayer() {
         return player;
     }
 }
