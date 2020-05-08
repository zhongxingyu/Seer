 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package blockmover;
 
 import blockmover.Block;
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.geom.AffineTransform;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 import javax.swing.JPanel;
 import javax.swing.Timer;
 import org.omg.CORBA.INTERNAL;
 
 /**
  *
  * @author Niki
  */
 public class BlockMoverGameField extends JPanel implements KeyListener, ActionListener {
 
     private Timer timer;
     private int framerate = 25;
    private int rows = 22;
     private int columns = 10;
     //22 rows with 10 elements (columns)
     private ShapeColor[][] field = new ShapeColor[rows][columns];
     private Random rd;
     private int blocksize = 25;
     private int GamerPositionRow = 3;
     private int GamerPositionColumn = 4;
     private List<Block> gravityblocks = new ArrayList<Block>();
     private boolean[][] fieldIsBlocked = new boolean[rows][columns];
     private double gravityPixelPerFramerate = (double) blocksize / (double) framerate;
     private long gravityTimeForOneBlockInMillis = 200;
     //time in millis
     private long time = 0;
     //new Row implementation
     private long timeForNewRowInMillis = 10000;
     // One row consistens of x columns!
     private ShapeColor[] newRow = new ShapeColor[columns];
     private int distForNewRow = 0;
     private long startTimeOfNewRow = System.currentTimeMillis();
 
    @SuppressWarnings("LeakingThisInConstructor")
     public BlockMoverGameField() {
         setFocusable(true);
         addKeyListener(this);
 
         timer = new Timer(40, this);
         timer.start();
         //Creating the Field
         rd = new Random();
         int rd_num = 0;
 
         //Test implementation
         /*
          for (int i = 0; i < rows; i++) {
          for (int j = 0; j < columns; j++) {
          field[i][j] = ShapeColor.NONE;
          }
          }
          field[0][3] = ShapeColor.YELLOW;
          field[1][3] = ShapeColor.GREEN;
          field[2][3] = ShapeColor.YELLOW;
          field[3][3] = ShapeColor.RED;
          field[4][3] = ShapeColor.GRAY;
          field[5][3] = ShapeColor.YELLOW;
          */
         for (int i = 0; i < rows; i++) {
             for (int j = 0; j < columns; j++) {
                 if (i > rows / 2) {
                     field[i][j] = ShapeColor.NONE;
                     continue;
                 }
                 rd_num = rd.nextInt(ShapeColor.values().length);
                 field[i][j] = ShapeColor.values()[rd_num];
             }
         }
         createNewRow();
 
         //sort the field - no empty elements
         int point_top;
         int point_bottom;
         for (int j = 0; j < columns; j++) {
             point_top = rows - 1;
             point_bottom = 0;
             while (point_top > point_bottom) {
                 while (field[point_bottom][j] != ShapeColor.NONE) {
                     point_bottom++;
                 }
                 while (field[point_top][j] == ShapeColor.NONE) {
                     point_top--;
                 }
                 if (point_top > point_bottom) {
                     field[point_bottom][j] = field[point_top][j];
                     field[point_top][j] = ShapeColor.NONE;
                 }
             }
         }
     }
 
     private void createNewRow() {
         int rd_num;
         for (int i = 0; i < columns; i++) {
             rd_num = rd.nextInt(ShapeColor.values().length - 1);
             rd_num++;
             newRow[i] = ShapeColor.values()[rd_num];
         }
     }
 
     @Override
     public void paint(Graphics g) {
         super.paint(g);
         Graphics2D g2D = (Graphics2D) g;
         g2D.translate(this.getWidth(), this.getHeight() - distForNewRow);
         g2D.transform(AffineTransform.getRotateInstance(Math.PI));
         //draw the field
         for (int i = 0; i < rows; i++) {
             for (int j = 0; j < columns; j++) {
                 if (field[i][j] == ShapeColor.NONE) {
                     continue;
                 }
                 g.setColor(Color.BLACK);
                 g.drawRect(blocksize * j, blocksize * i, blocksize, blocksize);
                 g.setColor(field[i][j].getColor());
                 g.fillRect(blocksize * j, blocksize * i, blocksize - 1, blocksize - 1);
             }
         }
         for (Block b : gravityblocks) {
             g.setColor(Color.BLACK);
             g.drawRect(blocksize * b.getShape_position_column(),
                     blocksize * b.getShape_position_row() - b.getDist_falling(),
                     blocksize - 1, blocksize - 1);
             g.setColor(b.getShapecolor().getColor());
             if (b.getShapecolor() == ShapeColor.NONE) {
                 System.out.println("Shapecolor of falling block is none!");
                 System.exit(2);
             }
             g.fillRect(blocksize * b.getShape_position_column(),
                     blocksize * b.getShape_position_row() - b.getDist_falling(),
                     blocksize - 1, blocksize - 1);
         }
         //draw the new Row
         g.setColor(Color.BLACK);
         g.drawLine(0, 0, blocksize * rows, 0);
         g.drawLine(0, 1, blocksize * rows, 1);
         g.drawLine(0, 2, blocksize * rows, 2);
         for (int i = 0; i < columns; i++) {
             g.setColor(Color.BLACK);
             g.drawRect(blocksize * i, 0 - blocksize, blocksize, blocksize);
             g.setColor(new Color((int) (newRow[i].getColor().getRGB())));
             g.fillRect(blocksize * i, 0 - blocksize, blocksize - 1, blocksize - 1);
         }
 
         //draw the gamer area
         g2D.setStroke(new BasicStroke(3));
         g2D.setColor(Color.BLACK);
         g2D.drawRect(blocksize * GamerPositionColumn,
                 blocksize * GamerPositionRow,
                 blocksize, blocksize);
         g2D.drawRect(blocksize * (GamerPositionColumn + 1),
                 blocksize * GamerPositionRow,
                 blocksize, blocksize);
 
         //For Testing
        for (int i = 0; i < fieldIsBlocked.length; i++) {
             for (int j = 0; j < fieldIsBlocked[0].length; j++) {
                 if (fieldIsBlocked[i][j]) {
                     g2D.drawLine(blocksize * j, blocksize * i, blocksize * (j + 1), blocksize * (i + 1));
                 }
             }
         }
    }
 
     private int checkComboRows(int checked_field[][]) {
         int combo = 0;
         ShapeColor refShape;
         //ShapeColor[rows][columns];
         for (int i = 0; i < rows; i++) {
             for (int j = 0; j < columns; j++) {
                 refShape = field[i][j];
                 if (refShape.equals(ShapeColor.NONE)) {
                     continue;
                 }
 
                 int k = 1;
                 while (j + k < columns && field[i][j + k].equals(refShape)) {
                     k++;
                 }
                 //due to while, k might be one too much.
                 k--;
                 if (k >= 2) {
                     //System.out.println("combo(row)! " + k);
                     combo++;
                     while (k >= 0) {
                         checked_field[i][j + k] = 1;
                         k--;
                     }
                 }
             }
         }
         return combo;
     }
 
     private int checkComboColumns(int checked_field[][]) {
         int combo = 0;
         //ShapeColor[rows][columns];
         ShapeColor refShape;
         for (int i = 0; i < columns; i++) {
             for (int j = 0; j < rows; j++) {
                 refShape = field[j][i];
                 if (refShape.equals(ShapeColor.NONE)) {
                     continue;
                 }
 
                 int k = 1;
                 while (j + k < rows && field[j + k][i].equals(refShape)) {
                     k++;
                 }
                 k--;
                 //3 means, at least 2 more with same shape.
                 if (k >= 2) {
                     //System.out.println("combo column! " + k);
                     combo++;
                     while (k >= 0) {
                         checked_field[j + k][i] = 1;
                         k--;
                     }
                 }
             }
         }
         return combo;
     }
 
     private void update() {
         time = System.currentTimeMillis();
         gravity();
         checkCombo();
         updateNewRow();
     }
 
     private void updateNewRow() {
         //Update of the new Row comming
         distForNewRow = (int) (((double) (time - startTimeOfNewRow) / timeForNewRowInMillis)
                 * blocksize);
         if (distForNewRow >= blocksize) {
             startTimeOfNewRow = time;
             timeForNewRowInMillis -= 100;
             distForNewRow = 0;
             for (int i = rows - 1; i > 0; i--) {
                 field[i] = field[i - 1];
             }
             field[0] = newRow;
             newRow = new ShapeColor[columns];
            int rd_num;
             createNewRow();
             GamerPositionRow++;
             for (Block b : gravityblocks) {
                 b.setShape_position_row(b.getShape_position_row() + 1);
             }
         }
     }
 
     private void checkCombo() {
         int combo = 0;
         int checked_field[][] = new int[rows][columns];
         combo += checkComboRows(checked_field);
         combo += checkComboColumns(checked_field);
 
         for (int i = 0; i < rows; i++) {
             for (int j = 0; j < columns; j++) {
                 if (checked_field[i][j] >= 1) {
                     field[i][j] = ShapeColor.NONE;
                 }
             }
         }
 
         if (combo > 0) {
             timeForNewRowInMillis -= combo * 25;
             System.out.println("timeForNewRowInMillis: " + timeForNewRowInMillis);
             combo = 0;
         }
     }
 
     private ShapeColor getShapeAt(int r, int c) {
         if (r < 0 || c < 0 || r > (rows - 1) || c > (columns - 1)) {
             return ShapeColor.NONE;
         } else {
             return field[r][c];
         }
     }
 
     private void gravity() {
         ShapeColor swap;
         Block block = null;
         for (int j = 0; j < columns; j++) {
             for (int i = 0; i < rows - 1; i++) {
                 if (field[i][j] == ShapeColor.NONE && field[i + 1][j] != ShapeColor.NONE) {
                     block = new Block(this, i + 1, j);
                     block.setTime_start_falling(time);
                     block.setShapecolor(field[i + 1][j]);
                     gravityblocks.add(block);
                     //Mark blocked fields
                     fieldIsBlocked[i + 1][j] = true;
                     fieldIsBlocked[i][j] = true;
                     field[i + 1][j] = ShapeColor.NONE;
                 }
             }
         }
 
         List<Block> block_del = new ArrayList<Block>();
         for (Block b : gravityblocks) {
             long time_dist = time - b.getTime_start_falling();
             double dist_falling = (double) time_dist / (double) gravityTimeForOneBlockInMillis;
             if (dist_falling >= 1.0) {
                 if (b.getShape_position_row() > 0) {
                     fieldIsBlocked[b.getShape_position_row()][b.getShape_position_column()] = false;
                     b.setShape_position_row(b.getShape_position_row() - 1);
                     fieldIsBlocked[b.getShape_position_row()][b.getShape_position_column()] = true;
                 }
 
                 //check if they are 'landed'
                 if (b.getShape_position_row() == 0
                         || field[b.getShape_position_row() - 1][b.getShape_position_column()] != ShapeColor.NONE) {
                     field[b.getShape_position_row()][b.getShape_position_column()] = b.getShapecolor();
                     //Unmark blocked fields
                     fieldIsBlocked[b.getShape_position_row()][b.getShape_position_column()] = false;
                     fieldIsBlocked[b.getShape_position_row() + 1][b.getShape_position_column()] = false;
                     block_del.add(b);
                 } else {
                     b.setTime_start_falling(b.getTime_start_falling() + gravityTimeForOneBlockInMillis);
                     //Mark the field
                     fieldIsBlocked[b.getShape_position_row()][b.getShape_position_column()] = true;
                     fieldIsBlocked[b.getShape_position_row() - 1][b.getShape_position_column()] = true;
                     dist_falling -= 1.0;
                 }
             }
             b.setDist_falling((int) ((double) blocksize * dist_falling));
         }
         for (Block b : block_del) {
             gravityblocks.remove(b);
         }
     }
 
     private void switchShapes() {
         if (fieldIsBlocked[GamerPositionRow][GamerPositionColumn]
                 || fieldIsBlocked[GamerPositionRow][GamerPositionColumn + 1]) {
             System.out.println("can't swap, its blocked!");
             return;
         }
         ShapeColor swap = field[GamerPositionRow][GamerPositionColumn];
         field[GamerPositionRow][GamerPositionColumn] = field[GamerPositionRow][GamerPositionColumn + 1];
         field[GamerPositionRow][GamerPositionColumn + 1] = swap;
     }
 
     @Override
     public void keyTyped(KeyEvent e) {
     }
 
     @Override
     public void keyPressed(KeyEvent e) {
         if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
             if (GamerPositionColumn > 0) {
                 GamerPositionColumn--;
             }
         }
         if (e.getKeyCode() == KeyEvent.VK_LEFT) {
             if (GamerPositionColumn < (columns - 2)) {
                 GamerPositionColumn++;
             }
         }
         if (e.getKeyCode() == KeyEvent.VK_UP) {
             if (GamerPositionRow < (rows - 2)) {
                 GamerPositionRow++;
             }
         }
         if (e.getKeyCode() == KeyEvent.VK_DOWN) {
             if (GamerPositionRow > 0) {
                 GamerPositionRow--;
             }
         }
 
         if (e.getKeyChar() == KeyEvent.VK_SPACE) {
             switchShapes();
         }
     }
 
     @Override
     public void keyReleased(KeyEvent e) {
     }
 
     @Override
     public void actionPerformed(ActionEvent e) {
         update();
         this.repaint();
     }
 }
