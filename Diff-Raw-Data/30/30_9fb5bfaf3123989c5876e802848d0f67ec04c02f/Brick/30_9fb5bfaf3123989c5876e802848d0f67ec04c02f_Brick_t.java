 package fr.iutvalence.java.mp.BrickBreaker;
 
 // TODO (fix) write a "real" comment
 /**
  * 
  * Define what is a brick
  * That fix the position of a brick,
  * and this state
  * @author ochiers soulierc
 * 
  */
 public class Brick
 {
     // TODO (fixed) this intends to be a constant but it is not
     /**
      * Width size of the brick (in pixels)
      */
     public final static int WIDTH_BRICK = 50;
 
     // TODO (fixed) this intends to be a constant but it is not
     /**
      * Height size of the brick (in pixels)
      */
     public final static int HEIGHT_BRICK = 44;
     
     
     
     /**
      * That is the normal state of a brick
      */
     private final int STATE_NORMAL = 3;
     
     /**
      * That is the second state of a brick
      * That is when the brick was hit 
      */
     private final int STATE_LITTLE_BROCKEN = 2;
     
     /**
      * That is the third state of a brick
      * That is when the brick was hit 
      */
     private final int STATE_LOT_BROCKEN = 1;
     
     /**
      * That is the laster state of a brick
      * That is when the brick was hit for the last time
      * And then, it is delete of the screen
      */
     private final int STATE_DESTROYED = 0;
     
     // TODO (fixed) move this couple of int in a class named Position
     /**
      * Position of the top left corner (in pixels)
      */
     private Position brickPosition;
     
     // TODO (FIXED) declare constants for brick states
     /**
      * State of the brick (normal, a bit broken, really broken, destroyed)
      */
     private int state;
     
     // TODO (FIXED) write a "real" comment (how is the resulting brick)
     /**
      * This is the initialization of a brick who ask the position of the brick
      */
     public Brick(int posX, int posY)
     {
         super();
         this.brickPosition = new Position(posX,posY);
         this.state = this.STATE_NORMAL;
     }
     
     /**
      * @see java.lang.Object#toString()
      */
     public String toString()
     {
         return"{x : " + this.brickPosition.getPosX() +", y : " + this.brickPosition.getPosY() + ", state :"+ this.state +"}";
     }
 
     // TODO (FIXED) write comment
     /**
      * 
      * @return the position of the brick
      */
     public Position getBrickPosition()
     {
         return this.brickPosition;
     }
 
     /**
      * @return the state of the brick
      */
     // TODO (FIXED) write comment
     public int getState()
     {
         return this.state;
     }
 
     // TODO (FIXED) write comment
     /**
      * set the current state of the brick
      * @param state 
      */
     public void setState(int state)
     {
         this.state = state;
     }
     
 
 }
