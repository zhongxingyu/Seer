 package fr.iutvalence.java.mp.BrickBreaker;
 
 /**
  * Called when a game is created
  * @author ochiers soulierc
  * 
  * */
 public class Game
 {
     // TODO (FIXED) declare field visibility
     /**
      * Number of life
      */
     public int nbBalls; 
 
     // TODO (fixed) rename field 
    /**
     * The ball that shoots the bricks
     */
     private Ball theBall;
     
     // TODO (fixed) declare field visibility
     // TODO (FIXED) write a comment that really describe what this field is
     /**
      * This tab contains all bricks of the game
      * That save their position, their state
      *
      */
     //TODO next time : create Get and Set methods
     public Brick tabBrick[];
     
     // TODO (FIXED) rename field (game has no width)
     /**
      * Width size of the container where we play
      */
     private final int WIDTH_OF_GAME_PANEL = 500;
 
     // TODO (FIXED) rename field (game has no height)
     /**
      * Height size of the container where we play
      */
     private final int HEIGHT_OF_GAME_PANEL = 440;
     
     /**
      * This number is the number of bricks in the level,
      * But it's a temp constant because this number can change depending 
      * on the level
      */
     public final int NUMBER_OF_BRICKS = 10;
     
     /**
      * this is a test to create a line of bricks with y position who equals to 44
      * 
      */
     private final int Y_POSITION_BRICKS = 44;
     /**
      * This is the procedure where the game find his start
      * It's the initialization of the game
      */
     // TODO (FIXED) write a comment
     public Game()
     {
         super();
         this.nbBalls = 3;
         this.theBall = new Ball(50,50);
         this.tabBrick = new Brick[this.NUMBER_OF_BRICKS];
         for(int i = 0; i<this.NUMBER_OF_BRICKS; i++)
         {
             this.tabBrick[i] = new Brick(i*Brick.WIDTH_BRICK,this.Y_POSITION_BRICKS);
         }
         
         go();
     }
     
     /**
      * method who move the ball and take care of the game's outlines
      */
     private void go()
     {
         /**
         * It is the test to know if the ball arrive at the outline 
          */
         for(int i=0; i<1000; i++)
         {
             /**
              *  
              */
             if(this.theBall.getPositionBall().getPosX() >= this.WIDTH_OF_GAME_PANEL || this.theBall.getPositionBall().getPosX() <= 0 )
             {
                 this.theBall.setB(-1*this.theBall.getB());
             }
             /**
              * 
              */
             else if (this.theBall.getPositionBall().getPosY() >= this.HEIGHT_OF_GAME_PANEL || this.theBall.getPositionBall().getPosY() <= 0 )
             {
                 this.theBall.setA(-1*this.theBall.getA());
             }
             
             this.theBall.setPositionBall(this.theBall.getPositionBall().getPosX() + this.theBall.getB(),this.theBall.getPositionBall().getPosY() + this.theBall.getA() );
 
             
             
             System.out.println("{"+this.theBall.getPositionBall().getPosX()+", "+this.theBall.getPositionBall().getPosY()+"}");
             
         }
         
     
     }
     
 }
