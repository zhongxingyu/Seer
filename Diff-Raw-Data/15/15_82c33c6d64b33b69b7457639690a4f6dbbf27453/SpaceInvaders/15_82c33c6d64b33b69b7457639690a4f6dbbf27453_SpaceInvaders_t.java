 package fr.iutvalence.java.mp.SpaceInvaders;
 
 import java.util.Random;
 
 /**
  * class SpaceInvaders : A new game
  * @author thevenim
  */
 public class SpaceInvaders
 {
     /**
      * size of the grid ( 15 per 15 )
      */
     public final static int CONSTANT_GRID = 15;
 
     /**
      * constant which represent the Spacecraft
      */
     public final static int CONSTANT_SPACECRAFT = 7;
 
     /**
      * constant which represent the Enemy
      */
     public final static int CONSTANT_ENEMY = 6;
 
     /**
      * constant which represent the Fire
      */
     public final static int CONSTANT_FIRE = 1;
 
     /**
      * constant which represent the Void
      */
     public final static int CONSTANT_EMPTY = 0;
 
     /**
      * constant which represent where the enemy is down
      */
     public final static int CONSTANT_ENEMY_DOWN = 9;
 
     /**
      * Declaration of the place of Spacecraft on the grid at the beginning (Line)
      */
     public final static int CONSTANT_PLACE_SPACECRAFT_X = 14;
 
     /**
      * Declaration of the place of Spacecraft on the grid at he beginning (Column)
      */
     public final static int CONSTANT_PLACE_SPACECRAFT_Y = 7;
 
     /**
      * Declaration of the Grid to put our elements in it, like Spacecraft, enemies etc ..
      */
     private int[][] grid;
 
     /**
      * declaration of empty grid to erase a line
      */
     private int[] gridEmpty;
 
     /**
      * Declaration of the current enemies line
      */
     private int currentEnemiesLine;
 
     /**
      * Declaration of enemies line limit
      */
     private int nextLineAfterEnemies;
 
     /**
      * Position Y of the Space Craft during the game
      */
     private int POS_Y;
 
     /**
      * Position X of the fire during the game
      */
     private int POS_X_Fire;
 
     // TODO (fix) rewrite comment, say how is the game once created
     /**
      * Declaration of the SpaceInvaders constructor
      */
     public SpaceInvaders()
     {
         this.grid = new int [CONSTANT_GRID][CONSTANT_GRID];
         this.currentEnemiesLine = 0;
         this.nextLineAfterEnemies = 3;
         this.gridEmpty = new int [CONSTANT_GRID];
         this.fillGrid();
         this.putEnemies();
         this.POS_Y=CONSTANT_PLACE_SPACECRAFT_Y;
         this.POS_X_Fire=CONSTANT_PLACE_SPACECRAFT_X-1;
         for(int x = 0; x<15; x++)
         {
             this.gridEmpty[x] = CONSTANT_EMPTY;
         }
 
     }
 
     /**
      * Method to place elements in the grid cell
      */
     // TODO (fixed) rename method
     private void fillGrid()
     {
         for(int x=0; x<15; x++)
         {
             for (int y=0; y<15; y++)
             {
                 this.grid[x][y]= CONSTANT_EMPTY;
             }
         }
         this.grid[CONSTANT_PLACE_SPACECRAFT_X][CONSTANT_PLACE_SPACECRAFT_Y]=CONSTANT_SPACECRAFT;
     }
 
     /**
      * Method to display the grid with elements
      */
     private void displayGrid()
     {
         for(int x=0; x<15; x++)
         {
             for(int y = 0; y<15; y++)
             {
                if (this.grid[x][y]==CONSTANT_EMPTY ) /* if we have some empty box, we put V */
                 {
                     System.out.print("V");
                 }
                 else
                 {
                     System.out.print(this.grid[x][y]); // display the grid normally //
                 }
 
 
             }
             System.out.println();
         }
     }
     /**
      * to place the enemies in the grid
      */
     private void putEnemies()
     {
         for(int x=this.currentEnemiesLine; x<this.nextLineAfterEnemies; x++)
         {
             for(int y = 0; y<13; y++)
             {
                 this.grid[x][y]=CONSTANT_ENEMY;
             }
         }
     }
 
     /**
      * Method to move elements to the left
      */
     private void left()
     {
         for(int x=this.currentEnemiesLine; x<this.nextLineAfterEnemies; x++)
         {
             for(int y = 0; y<13; y++)
             {
                 this.grid[x][y]=this.grid[x][y+1];
             }
             this.grid[x][13]=CONSTANT_EMPTY;
         }
     }
 
     /**
      * Method to move elements to the center from the left
      */
     private void centerFromLeft()
     {
         for(int x=this.currentEnemiesLine; x<this.nextLineAfterEnemies; x++)
         {
             for(int y = 14; y>0; y--)
             {
                 this.grid[x][y]=this.grid[x][y-1];
             }
             this.grid[x][0]=CONSTANT_EMPTY;
         }
     }
 
     /**
      * Method to move elements to the center from the right
      */
     private void centerFromRight()
     {
         for(int x=this.currentEnemiesLine; x<this.nextLineAfterEnemies; x++)
         {
             for(int y = 1; y<14; y++)
             {
                 this.grid[x][y]=this.grid[x][y+1];
             }
             this.grid[x][14]=CONSTANT_EMPTY;
         }
     }
 
     /**
      * Method to move elements to the right
      */
     private void right() // problem when an extrem left enemies is dead, he comes back
     {
         for(int x=this.currentEnemiesLine; x<this.nextLineAfterEnemies; x++)
         {
             for(int y = 14; y>2; y--)
             {
                 this.grid[x][y]=this.grid[x][y-1];
             }
             this.grid[x][1]=CONSTANT_EMPTY;
         }
     }
 
     /**
      * Method to down : erase all enemies and create enemies on down line
      */
     private void changeLine()
     {
         int x=this.currentEnemiesLine; 
         this.grid[x+3] = this.grid[x+2];
         this.grid[x+2] = this.grid[x+1];
         this.grid[x+1] = this.grid[x];
         this.grid[x] = this.gridEmpty;
         this.currentEnemiesLine ++;
         this.nextLineAfterEnemies ++;
     }
 
     /**
      * Method to shoot enemies randomly
      */
     private void randomShoot()
     {
         int POS_Y_Fire = this.POS_Y; // To save the column of the fire
         Random t = new Random();
 
         if (t.nextInt(2)==1)
         {
            while(this.grid[this.POS_X_Fire][POS_Y_Fire]!=CONSTANT_ENEMY && this.POS_X_Fire>this.currentEnemiesLine-1 )
             {
                 this.grid[this.POS_X_Fire][POS_Y_Fire]=CONSTANT_FIRE;
 
                 this.POS_X_Fire--;
                 
                 this.displayGrid();
                 this.grid[this.POS_X_Fire+1][POS_Y_Fire]=CONSTANT_EMPTY;
                if (this.grid[this.POS_X_Fire][POS_Y_Fire]<this.currentEnemiesLine)
                {
                    this.grid[this.POS_X_Fire][POS_Y_Fire]=CONSTANT_EMPTY;
                }
                 this.pauseShoot();
 
             }
            
             if(this.grid[this.POS_X_Fire][POS_Y_Fire]==CONSTANT_ENEMY)
             {
                this.grid[this.POS_X_Fire][POS_Y_Fire]=CONSTANT_EMPTY;
             }
             if (this.POS_X_Fire == 0)
             {
                 this.grid[this.POS_X_Fire][POS_Y_Fire]=CONSTANT_EMPTY;
             }
             this.POS_X_Fire=CONSTANT_PLACE_SPACECRAFT_X-1;
         }
     }
 
     /**
      * Method to move Space Craft randomly
      */
     private void randomMovement()
     {
         Random r = new Random();
 
         if (r.nextInt(2)==0)
         {
             if (this.POS_Y == 0)
             {
                 this.grid[CONSTANT_PLACE_SPACECRAFT_X][this.POS_Y+1]=CONSTANT_SPACECRAFT;
                 this.grid[CONSTANT_PLACE_SPACECRAFT_X][this.POS_Y]=CONSTANT_EMPTY;
                 this.POS_Y++;
             }
             else
             {
                 this.grid[CONSTANT_PLACE_SPACECRAFT_X][this.POS_Y-1]=CONSTANT_SPACECRAFT;
                 this.grid[CONSTANT_PLACE_SPACECRAFT_X][this.POS_Y]=CONSTANT_EMPTY;
                 this.POS_Y--;
             }
 
         }
         else
         {
             if (this.POS_Y == 14)
             {
                 this.grid[CONSTANT_PLACE_SPACECRAFT_X][this.POS_Y-1]=CONSTANT_SPACECRAFT;
                 this.grid[CONSTANT_PLACE_SPACECRAFT_X][this.POS_Y]=CONSTANT_EMPTY;
                 this.POS_Y--;
             }
             else
             {
                 this.grid[CONSTANT_PLACE_SPACECRAFT_X][this.POS_Y+1]=CONSTANT_SPACECRAFT;
                 this.grid[CONSTANT_PLACE_SPACECRAFT_X][this.POS_Y]=CONSTANT_EMPTY;
                 this.POS_Y++;
             }
         }
     }
 
     /**
      * Method to pause
      */
     private void pause()
     {
         try
         {
             Thread.sleep(1500); // wait 3s //
         }
         catch (InterruptedException e)
         {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
     }
 
     /**
      * Method to pause the shoot
      */
     private void pauseShoot()
     {
         try
         {
             Thread.sleep(200); // wait 0,2 sec
         }
         catch (InterruptedException e)
         {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
     }
 
 
     // TODO (fixED) this method should be the only public method in this class
     /**
      * Method to play
      */
     public void play()
     {
         // TODO (fix) go on with realistic game algorithm
         // where the player is asked to move or shoot ?
         this.displayGrid();
         this.randomMovement();
         this.randomShoot();
         this.pause();
         this.centerFromLeft();
         this.displayGrid();
         this.randomMovement();
         this.randomShoot();
         this.pause();
         this.right();
         this.displayGrid();
         this.randomMovement();
         this.randomShoot();
         this.pause();
         this.centerFromRight();
         this.displayGrid();
         this.randomMovement();
         this.randomShoot();
         this.pause();
         this.left();
         this.displayGrid();
         this.pause();
         this.changeLine();
     }
 }
