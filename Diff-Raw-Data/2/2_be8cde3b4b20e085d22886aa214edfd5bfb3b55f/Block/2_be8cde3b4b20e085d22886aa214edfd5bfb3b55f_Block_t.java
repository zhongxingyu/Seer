 package epsilon.tools.mapcreator;
 
 import java.awt.Graphics;
 import javax.swing.JPanel;
 
 /**
  * Floor block repesentations.
  *
  * @author vz
  */
 public class Block extends JPanel {
 
     private int row;
     private int col;
 
     /**
      * Create new block.
      *
      * @param row row in the grid
      * @param col col in the grid
      */
     public Block(int row, int col) {
         this.row = row;
         this.col = col;
     }
 
     /**
      * Prints out java source code in the syntax Epsilon for adding blocks.
      *
      * @see epsilon.map.WorldStore
      */
     public void printJava(int offset) {
        System.out.println("worldstore.add(new Floor("+(((row*50)-400)+offset) +","+col*40+"),this);");
     }
 
     @Override
     public void paint(Graphics g) {
         g.fillRect(row*20, col*20, 20, 20);
     }
 
     /**
      * Returns which of the rows the block is assigned to.
      *
      * @return
      */
     public int getRow() {
         return row;
     }
 
     /**
      * Returns which of the cols the block is assigned to.
      *
      * @return
      */
     public int getCol() {
         return col;
     }
 }
