 package elements;
 
 import basic.Layout;
 import basic.Oriented;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.List;
 import values.BrickColor;
 import values.Orientation;
 
 /**
  * Class stores and manage collection of a bricks.
  * 
  * @author X-Stranger
  */
 @SuppressWarnings("serial")
 public class BrickCollection extends Oriented {
    
     private List<Brick> list = new ArrayList<Brick>();
     private List<Brick> listBackup = new ArrayList<Brick>();
     private Brick[][] bricks;
     private Brick[][] bricksBackup;
     private int maxX;
     private int maxY;
     private int level;
     private boolean arcade;
 
     /**
      * Default constructor.
      * 
      * @param level - max color index
      * @param orientation - collection orientation
      * @param arcade - boolean game type flag
      */
     public BrickCollection(int level, boolean arcade, Orientation orientation) {
         this.level = level;
         this.arcade = arcade;
 
         if (orientation.isHorizontal()) {
             maxX = Layout.CORNER;
             maxY = Layout.FIELD;
         }
         
         if (orientation.isVertical()) {
             maxX = Layout.FIELD;
             maxY = Layout.CORNER;
         }
         
         this.bricks = new Brick[maxX][maxY];
         this.bricksBackup = new Brick[maxX][maxY];
 
         this.fill(level, arcade);
         this.setOrientation(orientation);
     }
     
     /**
      * Method stores collection state to stream.
      * 
      * @param out - stream to store state to
      * @throws IOException if any occurs
      */
     public void saveToStream(OutputStream out) throws IOException {
         out.write(level);
         for (int i = 0; i < maxX; i++) {
             for (int j = 0; j < maxY; j++) {
                 out.write(bricks[i][j].getColor().getIndex());
             }
         }
     }
     
     /**
      * Method loads collection state from stream.
      * 
      * @param in - stream to load state ftom
      * @throws IOException if any occurs
      */
     public void loadFromStream(InputStream in) throws IOException {
         level = in.read();
         
         int color;
         list.clear();
 
         for (int i = 0; i < maxX; i++) {
             for (int j = 0; j < maxY; j++) {
 
                 color = in.read(); 
                if (color < 0 || color > 9) {
                     bricks[i][j] = new Brick();
                 } else {
                     bricks[i][j] = new Brick(new BrickColor(color));
                 }
                 bricks[i][j].setParentCollection(this);
                 list.add(bricks[i][j]);
             }
         }
     }
     
     /**
      * Saves bricks copy.
      */
     public void save() {
         for (int i = 0; i < maxX; i++) {
             for (int j = 0; j < maxY; j++) {
                 bricksBackup[i][j] = bricks[i][j]; 
             }
         }
         listBackup.clear();
         listBackup.addAll(list);
     }
     
     /**
      * Restores bricks from copy.
      */
     public void restore() {
         for (int i = 0; i < maxX; i++) {
             for (int j = 0; j < maxY; j++) {
                 bricks[i][j] = bricksBackup[i][j];
                 bricks[i][j].setOrientation(Orientation.NONE);
             }
         }
         list.clear();
         list.addAll(listBackup);
     }
     
     /**
      * Method fills collection with bricks.
      * 
      * @param level - max color index
      * @param arcade - boolean game type flag
      */
     private void fill(int level, boolean arcade) {
         Brick brick;
         for (int i = 0; i < maxX; i++) {
             for (int j = 0; j < maxY; j++) {
                 brick = new Brick(level, arcade);
                 brick.setParentCollection(this);
                 bricks[i][j] = brick; 
                 list.add(brick);
             }
         }
     }
     
     /**
      * Getter for bricks.
      * 
      * @param x - x-position
      * @param y - y-position
      * @return Brick element
      */
     public Brick getBrick(int x, int y) {
         return bricks[x][y];
     }
     
     /**
      * Getter for bricks.
      * 
      * @param x - x-position
      * @return Brick array
      */
     public Brick[] getBrickCol(int x) {
         Brick[] bricks = new Brick[maxY];
         for (int i = 0; i < maxY; i++) {
             bricks[i] = this.bricks[x][i];
         }
         return bricks;
     }
     
     /**
      * Getter for bricks.
      * 
      * @param y - y-position
      * @return Brick array
      */
     public Brick[] getBrickRow(int y) {
         Brick[] bricks = new Brick[maxX];
         for (int i = 0; i < maxX; i++) {
             bricks[i] = this.bricks[i][y];
         }
         return bricks;
     }
     
     /**
      * Returns true if collection contains brick.
      * 
      * @param brick to check for
      * @return boolean value
      */
     public boolean contains(Brick brick) {
         return list.contains(brick);
     }
     
     /**
      * Method shifts selected line or column of a collection to one brick depending
      * on orientation and generates a new brick instead of shifted off.
      * 
      * @param pos - detects line or column to shift
      * @return new Brick object
      */
     public Brick shift(int pos) {
         if (this.getOrientation() == Orientation.NONE) {
             return null;
         }
         
         Brick brick = new Brick(this.level, this.arcade);
         brick.setParentCollection(this);
         
         if (this.getOrientation() == Orientation.LEFT) {
             list.remove(bricks[maxX - 1][pos]);
 
             for (int i = maxX - 1; i > 0; i--) {
                 bricks[i][pos] = bricks[i - 1][pos];
             }
             
             bricks[0][pos] = brick;
 
         } else if (this.getOrientation() == Orientation.RIGHT) {
             list.remove(bricks[0][pos]);
 
             for (int i = 0; i < maxX - 1; i++) {
                 bricks[i][pos] = bricks[i + 1][pos];
             }
             
             bricks[maxX - 1][pos] = brick;
             
         } else if (this.getOrientation() == Orientation.TOP) {
             list.remove(bricks[pos][maxY - 1]);
 
             for (int i = maxY - 1; i > 0; i--) {
                 bricks[pos][i] = bricks[pos][i - 1];
             }
             
             bricks[pos][0] = brick;
             
         } else if (this.getOrientation() == Orientation.BOTTOM) {
             list.remove(bricks[pos][0]);
 
             for (int i = 0; i < maxY - 1; i++) {
                 bricks[pos][i] = bricks[pos][i + 1];
             }
             
             bricks[pos][maxY - 1] = brick;
         }
         
         list.add(brick);
         return brick;
     }
 
     /**
      * Method shifts selected line or column of a collection to one brick depending
      * on orientation and add the passed brick instead of shifted off.
      * 
      * @param pos - detects line or column to shift
      * @param brick - Brick object to add
      */
     public void shiftBack(int pos, Brick brick) {
         if (this.getOrientation() == Orientation.NONE) {
             return;
         }
         
         brick.setOrientation(Orientation.NONE); 
         brick.setParentCollection(this);
         
         if (this.getOrientation() == Orientation.RIGHT) {
             list.remove(bricks[maxX - 1][pos]);
 
             for (int i = maxX - 1; i > 0; i--) {
                 bricks[i][pos] = bricks[i - 1][pos];
             }
             
             bricks[0][pos] = brick;
 
         } else if (this.getOrientation() == Orientation.LEFT) {
             list.remove(bricks[0][pos]);
 
             for (int i = 0; i < maxX - 1; i++) {
                 bricks[i][pos] = bricks[i + 1][pos];
             }
             
             bricks[maxX - 1][pos] = brick;
             
         } else if (this.getOrientation() == Orientation.BOTTOM) {
             list.remove(bricks[pos][maxY - 1]);
 
             for (int i = maxY - 1; i > 0; i--) {
                 bricks[pos][i] = bricks[pos][i - 1];
             }
             
             bricks[pos][0] = brick;
             
         } else if (this.getOrientation() == Orientation.TOP) {
             list.remove(bricks[pos][0]);
 
             for (int i = 0; i < maxY - 1; i++) {
                 bricks[pos][i] = bricks[pos][i + 1];
             }
             
             bricks[pos][maxY - 1] = brick;
         }
         
         list.add(brick);
     }
 }
