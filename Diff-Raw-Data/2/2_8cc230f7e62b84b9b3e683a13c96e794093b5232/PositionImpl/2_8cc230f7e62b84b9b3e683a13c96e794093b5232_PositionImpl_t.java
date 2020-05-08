 package battleship;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Martin Martin
  * Date: 28/01/13
  * Time: 22:26
  * To change this template use File | Settings | File Templates.
  */
 public class PositionImpl implements Position {
 
     private int x;
     private int y;
 
    // test comment

     /**
      *
      * @param x
      * @param y
      */
     PositionImpl(int x, int y){
         this.x = x;
         this.y = y;
     }
 
     /**
      *
      * @return
      */
     @Override
     public int getX(){
         return x;
     }
 
     /**
      *
      * @return
      */
     @Override
     public int getY() {
         return y;
     }
 
     /**
      *
      * @param x
      */
     @Override
     public void setX(int x) {
         this.x = x;
     }
 
     /**
      *
      * @param y
      */
     @Override
     public void setY(int y) {
         this.y = y;
     }
 }
