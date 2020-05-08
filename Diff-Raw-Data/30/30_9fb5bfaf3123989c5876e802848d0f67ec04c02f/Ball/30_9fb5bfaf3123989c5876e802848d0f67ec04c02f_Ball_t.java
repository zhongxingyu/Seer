 package fr.iutvalence.java.mp.BrickBreaker;
 
 /**
  * This class defines the ball used to destroy bricks
  * 
  * @author ochiers soulierc
  **/
 public class Ball
 {
     // TODO (FIXED) move this couple of int in a class named Position
 
     /**
      * It is the future position of the ball
      */
     private Position positionBall;
     
     /**
      * a is the director coefficient of the ball's trajectory (y=ax+b)
      */
     private int a;
     
     /**
      * b from (y=ax+b)
      */
     private int b;
     
     /**
      * It create a ball at position (x,y)
      */
 
     public Ball(int x, int y)
     {
         super();
         this.positionBall = new Position(x,y);
         this.a=1;
         this.b=1;
     }
     
     public void setPositionBall(int x, int y)
     {
         
         this.positionBall = new Position(x,y);
         
     }
     
     public Position getPositionBall()
     {
         
        return this.positionBall; 
         
     }
 
     public int getA()
     {
         return a;
     }
 
     public void setA(int a)
     {
         this.a = a;
     }
 
     public int getB()
     {
         return b;
     }
 
     public void setB(int b)
     {
         this.b = b;
     }
     
     
     
 }
