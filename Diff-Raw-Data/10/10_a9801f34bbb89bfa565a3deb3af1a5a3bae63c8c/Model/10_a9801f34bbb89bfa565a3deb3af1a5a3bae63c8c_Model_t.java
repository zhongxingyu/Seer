 /**
  * Created by IntelliJ IDEA.
  * User: miguel
  * Date: Jan 17, 2010
  * Time: 11:09:46 AM
  * To change this template use File | Settings | File Templates.
  */
 public class Model {
 
     Matrix grid;
     int numberColumns;
     int numberRows;
     float speed;
 
     PTetris parent;
     Piece currentPiece;
 
 
     /**
      * @param parent
      * @param numberColumns
      * @param numberRows
      */
     public Model(PTetris parent, int numberColumns, int numberRows) {
         this.numberColumns = numberColumns;
         this.numberRows = numberRows;
         this.parent = parent;
 
         grid = new Matrix(this.numberRows, this.numberColumns);
         grid.setElement(1,1,167);
         grid.setElement(9,1,  1);
         
         newPiece();
     }
 
     /**
      *
      */
     private void newPiece() {
         int x = (int) Math.floor(parent.random(0, numberColumns));
         System.out.println(x);
         int y = 0;
         currentPiece = new Piece(x, y);
     }
 
     /**
      * @param speed
      */
     public void setSpeed(float speed) {
         this.speed = speed;
     }
 
 
     /**
      *
      */
     public void draw() {
         // draw grid
         float eps = (float) 1.0e-8;
         for (int c = 0; c < this.numberColumns; c++)
             for (int l = 0; l < this.numberRows; l++) {
 
                 try {
                     if (grid.getElement(l, c) > eps)
                         // draw it!
 //                        parent.draw(c, l, 1);
                        parent.drawRectangle(c, l, 1);
                 } catch (java.lang.ArrayIndexOutOfBoundsException e) {
                     System.out.println("C: " + c + " - l: " + l);
                 }
             }
 
         parent.drawPiece(currentPiece);
     }
 
     /**
      *
      */
     public void update() {
         // move the current piece down
         currentPiece.moveDown(speed);
 
         // check whether the current piece has reached the end
         if (currentPiece.getY() >= grid.getNrow() - 1) {
            System.out.println( " >" + currentPiece.getY() + " >= " + ( grid.getNrow() - 1) );
            System.out.println( " >" + currentPiece.getX() + " >= " + ( grid.getNcol() - 1) );

            grid.setElement( (int) Math.floor( currentPiece.getY() ), currentPiece.getX(), 1.0);
             newPiece();
         }
 
 //        System.out.println( currentPiece.getY() );
 //        currentPiece.
 //        if( y >= (parent.height - height) ) {
 //            TODO add object to grid
 //            if (speed > 0)
 //                parent.newPiece();
 //            speed = 0;
 //        }
     }
 
     public void moveLeft() {
         currentPiece.setX(currentPiece.getX() - 1);
     }
 
     public void moveRight() {
         currentPiece.setX(currentPiece.getX() + 1);
     }
 
     public void changeConfiguration() {
         System.out.println("Change");
     }
 
     public void moveDown() {
         System.out.println("Down");
     }
 }
