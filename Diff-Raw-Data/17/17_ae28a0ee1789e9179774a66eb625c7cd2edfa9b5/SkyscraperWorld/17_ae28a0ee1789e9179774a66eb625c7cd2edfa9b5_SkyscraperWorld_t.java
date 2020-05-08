 import greenfoot.*;
 import java.io.IOException;
 import java.awt.Color;
 
 public class SkyscraperWorld extends World {
     private final int WINNING_LEVEL = 6;
     private boolean addScore;
     private boolean isLoaded = true;
     private boolean lostLife = false;
     private boolean doTime = false;
     public Counter scoreCounter;
     public TimeCounter time;
     private Player player;
     private int levelCompletePoints;
     private int currentLevel;   
     private int timeActs = 0;
     private Ground[] Ground;
     private MovingBrickRight[] MovingBricksRight;
     private MovingBrickLeft[] MovingBricksLeft;
     private MovingBrickUp[] MovingBricksUp;
     private Brick[] Bricks;
     private Coin[] Coins;
     private Life[] lifes;
     private MovingWater movingWater;
     private int countLifes = 3;
     public LifesText text;
 
    GreenfootSound backgroundMusic = new GreenfootSound("Escape.mp3");
 
     public SkyscraperWorld()  {
         super(80, 80, 10);
         currentLevel = 1;
         
         player = new Player();
         
         levelCompletePoints = loadLevel(currentLevel);
        backgroundMusic.playLoop();
         
         setPaintOrder(TimeCounter.class, Counter.class, Life.class, LifesText.class, MenuBar.class, MovingWater.class, Player.class, Coin.class, Surface.class);
         
         
         /**General stuff**/
         addObject(movingWater = new MovingWater(), 40, 71);
         
         addObject(new MenuBar(), 39, 75);
 
         scoreCounter = new Counter("Score: ");
         addObject(scoreCounter, 6, 74);
         
         time = new TimeCounter("Time: ");
         addObject(time, 6, 77);
         
         lifes = new Life[3];
         for (int i = 0; i < lifes.length; i++)
             {
                 lifes[i] = new Life();
             }
         addObject(lifes[0] = new Life(), 65, 74);
         addObject(lifes[1], 69, 74);
         addObject(lifes[2], 73, 74);
         
         text = new LifesText("Lifes: ");
         addObject(text, 62, 75);
     }
     
     public void act(){
         
         // Check for death, level completion and game completion:
 
         if((currentLevel == 1)&&(addScore == false)&&((player.getY() <= 11)&&(player.getX() >=62))){
                 scoreCounter.add(10);
                 addScore = true;
         } 
             
         if((currentLevel == 2)&&(addScore == false)&&((player.getY() <= 6)&&(player.getX() <=15))){
                 scoreCounter.add(10);
                 addScore = true;
         } 
         
         if((currentLevel == 3)&&(addScore == false)&&((player.getY() <= 6)&&(player.getX() <=15))){
                 scoreCounter.add(10);
                 addScore = true;
         } 
         
         if((currentLevel == 4)&&(addScore == false)&&((player.getY() == 28)&&(player.getX() >=62))){
                 scoreCounter.add(10);
                 addScore = true;
         } 
         
         if((currentLevel == 5)&&(addScore == false)&&((player.getY() <= 9)&&(player.getX() <=15))){
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
         if (currentLevel == WINNING_LEVEL)
         {
          scoreCounter.add(time.getTimeScore());
             doTime = true;
         }
         
         if(doTime == true){
             timeActs++;
             if(timeActs == 250){
                 winGame();
             }
         }
     }
 
     public boolean checkLevelComplete()
     {
         if (scoreCounter.getValue() >= levelCompletePoints)
             return true;
         else
             return false;
     }
 
     public void gameOver() {
         Greenfoot.setWorld(new GameOverWorld(Game.SKYSCRAPER_GAME, scoreCounter.getValue()));
     }
     
     public void winGame() {
        Greenfoot.setWorld(new GameOverWorld(Game.SKYSCRAPER_GAME, scoreCounter.getValue()));
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
         if (MovingBricksRight != null)
         {
             for (int i = 0; i < MovingBricksRight.length; i++)
             {
                 removeObject(MovingBricksRight[i]);
             }
         }
         if (MovingBricksLeft != null)
         {
             for (int i = 0; i < MovingBricksLeft.length; i++)
             {
                 removeObject(MovingBricksLeft[i]);
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
             addScore = false;
             Ground = new Ground[15];
             MovingBricksRight = new MovingBrickRight[2];
             Bricks = new Brick[30];
             Coins = new Coin[4];
             
             //MovingBricks distance's
             MovingBricksRight[0] = new MovingBrickRight(5, 36);
             MovingBricksRight[1] = new MovingBrickRight(17, 58);
             
         
 
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
             addObject(MovingBricksRight[0], 25, 27);
             addObject(MovingBricksRight[1], 50, 11);
             
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
             
             addObject(player, 4, 65);
             
             return 50;
         }
         
         if(lvl == 2){
             addScore = false;
             Ground = new Ground[12];
             MovingBricksRight = new MovingBrickRight[1];
             MovingBricksLeft = new MovingBrickLeft[1];
             MovingBricksUp = new MovingBrickUp[1];
             Bricks = new Brick[47];
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
             addObject(Bricks[26], 23, 33);
             addObject(Bricks[27], 27, 33);
             addObject(Bricks[28], 27, 29);
             //
             addObject(Bricks[29], 31, 29);
             addObject(Bricks[30], 35, 29);
             addObject(Bricks[31], 39, 29);
             addObject(Bricks[32], 43, 29);
 
             
             addObject(Bricks[33], 39, 29);
             addObject(Bricks[34], 43, 45);
             addObject(Bricks[35], 47, 45);
             addObject(Bricks[36], 51, 45);
             addObject(Bricks[37], 55, 45);
             addObject(Bricks[38], 47, 41);
             addObject(Bricks[39], 51, 41);
             addObject(Bricks[40], 55, 41);
             addObject(Bricks[41], 51, 37);
             addObject(Bricks[42], 55, 37);
             addObject(Bricks[43], 55, 33);
             addObject(Bricks[44], 59, 21);
             addObject(Bricks[45], 67, 21);
             addObject(Bricks[46], 75, 21);    
             
             //MovingBricks distance's
             MovingBricksRight[0] = new MovingBrickRight(49, 79);
             MovingBricksLeft[0] = new MovingBrickLeft(19, 48);
             MovingBricksUp[0] = new MovingBrickUp(9, 21);
             
             //MovingBricks locations
             addObject(MovingBricksRight[0], 49, 6);
             addObject(MovingBricksLeft[0], 48, 10);
             addObject(MovingBricksUp[0], 79, 15);
             
             //Coins
             addObject(Coins[0], 3, 27);
             addObject(Coins[1], 3, 32);
             addObject(Coins[2], 11, 27);
             addObject(Coins[3], 11, 32);
             
             //Finish
             addObject(Ground[7], 15, 6);
             addObject(Ground[8], 11, 6);
             addObject(Ground[9], 7, 6);
             addObject(Ground[10], 3, 6);
             addObject(Ground[11], 0, 6);
             
             
             addObject(player, 4, 65);
             
             return 100;
         }
             
         
         if (lvl == 3) {
             addScore = false;
             Ground = new Ground[12];
             MovingBricksRight = new MovingBrickRight[7];
             MovingBricksLeft = new MovingBrickLeft[7];
             Bricks = new Brick[6];
             Coins = new Coin[4];
 
             for (int i = 0; i < Ground.length; i++)
             {
                 Ground[i] = new Ground ();
             }   
             
             for (int i = 0; i < Coins.length; i++)
             {
                 Coins[i] = new Coin();
             }
             
             for (int i = 0; i < Bricks.length; i++)
             {
                 Bricks[i] = new Brick();
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
             
             //MovingBricks distance's
             MovingBricksRight[0] = new MovingBrickRight(22, 79);
             MovingBricksRight[1] = new MovingBrickRight(22, 79);
             MovingBricksRight[2] = new MovingBrickRight(22, 79);
             MovingBricksRight[3] = new MovingBrickRight(22, 79);
             MovingBricksRight[4] = new MovingBrickRight(22, 79);
             MovingBricksRight[5] = new MovingBrickRight(22, 79);
             MovingBricksRight[6] = new MovingBrickRight(22, 79);
            
             MovingBricksLeft[0] = new MovingBrickLeft(22, 79);
             MovingBricksLeft[1] = new MovingBrickLeft(22, 79);
             MovingBricksLeft[2] = new MovingBrickLeft(22, 79);
             MovingBricksLeft[3] = new MovingBrickLeft(22, 79);
             MovingBricksLeft[4] = new MovingBrickLeft(22, 79);
             MovingBricksLeft[5] = new MovingBrickLeft(22, 79);
             MovingBricksLeft[6] = new MovingBrickLeft(22, 79);
             
             //MovingBricks locations
             addObject(MovingBricksRight[0], 50, 61);
             addObject(MovingBricksRight[1], 50, 53);
             addObject(MovingBricksRight[2], 50, 45);
             addObject(MovingBricksRight[3], 50, 37);
             addObject(MovingBricksRight[4], 50, 29);
             addObject(MovingBricksRight[5], 50, 21);
             addObject(MovingBricksRight[6], 50, 13);
             
             addObject(MovingBricksLeft[0], 50, 57);
             addObject(MovingBricksLeft[1], 50, 49); 
             addObject(MovingBricksLeft[2], 50, 41);
             addObject(MovingBricksLeft[3], 50, 33);
             addObject(MovingBricksLeft[4], 50, 33);
             addObject(MovingBricksLeft[5], 50, 25);
             addObject(MovingBricksLeft[6], 50, 17);
             
             //Coins
             addObject(Coins[0], 15, 1);
             addObject(Coins[1], 11, 1);
             addObject(Coins[2], 7, 1);
             addObject(Coins[3], 3, 1);
             
             //Finish
             addObject(Ground[7], 15, 6);
             addObject(Ground[8], 11, 6);
             addObject(Ground[9], 7, 6);
             addObject(Ground[10], 3, 6);
             addObject(Ground[11], 0, 6);
             
             addObject(player, 4, 65);
             return 150;
         }
         
         
         if (lvl == 4) {
             addScore = false;
             Ground = new Ground[12];
             MovingBricksUp = new MovingBrickUp[5];
             Bricks = new Brick[4];
             Coins = new Coin[4];
 
             for (int i = 0; i < Ground.length; i++)
             {
                 Ground[i] = new Ground ();
             }   
             
             for (int i = 0; i < Coins.length; i++)
             {
                 Coins[i] = new Coin();
             }
             
             for (int i = 0; i < Bricks.length; i++)
             {
                 Bricks[i] = new Brick();
             }
             
             
             //first floor
             addObject(Ground[0], 0, 69);
             addObject(Ground[1], 3, 69);
             addObject(Ground[2], 7, 69);
             addObject(Ground[3], 11, 69);
             addObject(Ground[4], 15, 69);
             addObject(Ground[5], 19, 69);
             addObject(Ground[6], 23, 69);
             
             
             addObject(Bricks[0], 11, 12);
             addObject(Bricks[1], 15, 12);
             addObject(Bricks[2], 19, 12);
             addObject(Bricks[3], 23, 12);
             
             //MovingBricks distance's
             MovingBricksUp[0] = new MovingBrickUp(12, 69);
             MovingBricksUp[1] = new MovingBrickUp(1, 33);
             MovingBricksUp[2] = new MovingBrickUp(1, 33);
             MovingBricksUp[3] = new MovingBrickUp(1, 33);
             MovingBricksUp[4] = new MovingBrickUp(1, 33);
             
             //MovingBricks locations
             addObject(MovingBricksUp[0], 7, 65);
             addObject(MovingBricksUp[1], 31, 2);
             addObject(MovingBricksUp[2], 39, 12);
             addObject(MovingBricksUp[3], 47, 22);
             addObject(MovingBricksUp[4], 55, 32);
             
             //Coins
             addObject(Coins[0], 11, 7);
             addObject(Coins[1], 19, 7);
             addObject(Coins[2], 70, 27);
             addObject(Coins[3], 78, 27);
             
             
             //Finish
             addObject(Ground[7], 62, 32);
             addObject(Ground[8], 66, 32);
             addObject(Ground[9], 70, 32);
             addObject(Ground[10], 74, 32);
             addObject(Ground[11], 78, 32);
             
             addObject(player, 7, 65);
             return 200;
         }
         
         if (lvl == 5) {
             addScore = false;
             Ground = new Ground[13];
             MovingBricksLeft = new MovingBrickLeft[4];
             MovingBricksRight = new MovingBrickRight[3];
             MovingBricksUp = new MovingBrickUp[5];
             Bricks = new Brick[2];
             Coins = new Coin[4];
 
             for (int i = 0; i < Ground.length; i++)
             {
                 Ground[i] = new Ground ();
             }   
             
             for (int i = 0; i < Coins.length; i++)
             {
                 Coins[i] = new Coin();
             }
             
             for (int i = 0; i < Bricks.length; i++)
             {
                 Bricks[i] = new Brick();
             }
             
             
             //first floor
             addObject(Ground[0], 0, 69);
             addObject(Ground[1], 3, 69);
             addObject(Ground[2], 7, 69);
             addObject(Ground[3], 11, 69);
             addObject(Ground[4], 15, 69);
             addObject(Ground[5], 19, 69);
             addObject(Ground[6], 23, 69);
             
             
             addObject(Bricks[0], 35, 9);
             addObject(Bricks[1], 27, 9);
 
             //MovingBricks distance's           
             MovingBricksLeft[0] = new MovingBrickLeft(0, 78);
             MovingBricksLeft[1] = new MovingBrickLeft(0, 78);
             MovingBricksLeft[2] = new MovingBrickLeft(0, 78);
             MovingBricksLeft[3] = new MovingBrickLeft(0, 78);
             
             MovingBricksRight[0] = new MovingBrickRight(0, 78);
             MovingBricksRight[1] = new MovingBrickRight(0, 78);
             MovingBricksRight[2] = new MovingBrickRight(0, 78);
             
             MovingBricksUp[0] = new MovingBrickUp(9, 37);
             MovingBricksUp[1] = new MovingBrickUp(9, 37);
             MovingBricksUp[2] = new MovingBrickUp(9, 37);
             MovingBricksUp[3] = new MovingBrickUp(9, 37);
             MovingBricksUp[4] = new MovingBrickUp(9, 37);
             
             //MovingBricks locations
             addObject(MovingBricksLeft[0], 19, 61);
             addObject(MovingBricksLeft[1], 35, 53);
             addObject(MovingBricksLeft[2], 51, 45);
             addObject(MovingBricksLeft[3], 67, 37);
             
             addObject(MovingBricksRight[0], 27, 57);
             addObject(MovingBricksRight[1], 43, 49); 
             addObject(MovingBricksRight[2], 59, 41);
             
             addObject(MovingBricksUp[0], 78, 31);
             addObject(MovingBricksUp[1], 67, 25);
             addObject(MovingBricksUp[2], 59, 21);
             addObject(MovingBricksUp[3], 51, 17);
             addObject(MovingBricksUp[4], 43, 13);
 
             
             //Coins
             addObject(Coins[0], 19, 57);
             addObject(Coins[1], 75, 27);
             addObject(Coins[2], 15, 5);
             addObject(Coins[3], 3, 5);
             
             
             //Finish
             addObject(Ground[7], 19, 9);
             addObject(Ground[8], 15, 9);
             addObject(Ground[9], 11, 9);
             addObject(Ground[10], 7, 9);
             addObject(Ground[11], 3, 9);
             addObject(Ground[12], 0, 9);
             
             addObject(player, 4, 65);
             return 250;
         }
         if (lvl == 6) {
            setBackground("gameOver_game4.png");
            movingWater.setLevel(1);
         }
         return 0;
     }
     
     /** Deletes a life object after a miss */
     public void loseLife() {
         if((lostLife == false)&&(isLoaded == true)){
         countLifes--;
         }
         if ((countLifes == 2)&&(lostLife == false)) 
         {
            removeObject(lifes[0]);
            lostLife = true;
            purge();
            loadLevel(currentLevel);
            isLoaded = false;
            
            if((lostLife == true)&&(isLoaded == false)){
             lostLife = false;
             isLoaded = true;
         }
         }
        
         else if ((countLifes == 1)&&(lostLife == false)) 
         {
             removeObject(lifes[1]);
             lostLife = true;
             purge();
             loadLevel(currentLevel);
             isLoaded = false;
             
             if((lostLife == true)&&(isLoaded == false)){
             lostLife = false;
             isLoaded = true;
         }
         }
         
         else if ((countLifes == 0)&&(lostLife == false)) 
         {
             // Game Over
             removeObject(lifes[2]);
             lostLife = true;
             purge();
             gameOver();
         }
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
