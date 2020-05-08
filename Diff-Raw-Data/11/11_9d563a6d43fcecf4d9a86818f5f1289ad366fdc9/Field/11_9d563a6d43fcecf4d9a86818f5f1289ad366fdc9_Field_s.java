 package elements;
 
 import basic.Layout;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 import values.BrickColor;
 import values.Orientation;
 
 /**
  * Game field.
  * 
  * @author X-Stranger
  */
 public class Field {
 
     private int level;
     private List<Brick> list = new ArrayList<Brick>();
     private List<Brick> listBackup = new ArrayList<Brick>();
     private Brick[][] bricks = new Brick[Layout.FIELD][Layout.FIELD];
     private Brick[][] bricksBackup = new Brick[Layout.FIELD][Layout.FIELD];
     private Orientation[][] orientBackup = new Orientation[Layout.FIELD][Layout.FIELD];
     private static Random generator = new Random(System.currentTimeMillis());
     private BrickCollection leftBricks;
     private BrickCollection rightBricks;
     private BrickCollection topBricks;
     private BrickCollection bottomBricks;
     private int[][] matrix1 = new int[Layout.FIELD][Layout.FIELD];
     private int[][] matrix2 = new int[Layout.FIELD][Layout.FIELD];
 
     /**
      * Default constructor.
      * 
      * @param level - max color index
      * @param add - additional number of bricks
      * @param left - left brick collection
      * @param right - right brick collection
      * @param top - top brick collection
      * @param bottom - bottom brick collection
      */
     public Field(int level, int add, 
             BrickCollection left, BrickCollection right, BrickCollection top, BrickCollection bottom) {
         this.level = level;
         this.fill(add);
         this.leftBricks = left;
         this.rightBricks = right;
         this.topBricks = top;
         this.bottomBricks = bottom;
     }
     
     /**
      * Method stores field state to stream.
      * 
      * @param out - stream to store state to
      * @throws IOException if any occurs
      */
     public void saveToStream(OutputStream out) throws IOException {
         for (int i = 0; i < Layout.FIELD; i++) {
             for (int j = 0; j < Layout.FIELD; j++) {
                 out.write(bricks[i][j].getColor().getIndex());
                 out.write(bricks[i][j].getOrientation().ordinal());
             }
         }
     }
     
     /**
      * Method loads field state from stream.
      * 
      * @param in - stream to load state ftom
      * @throws IOException if any occurs
      */
     public void loadFromStream(InputStream in) throws IOException {
         int color;
         list.clear();
 
         for (int i = 0; i < Layout.FIELD; i++) {
             for (int j = 0; j < Layout.FIELD; j++) {
 
                 color = in.read(); 
                 if (color < 0 || color > 9) {
                     bricks[i][j] = new Brick();
                 } else {
                     bricks[i][j] = new Brick(new BrickColor(color));
                 }
                 bricks[i][j].setOrientation(Orientation.values()[in.read()]);
                 list.add(bricks[i][j]);
             }
         }
     }
     
     /**
      * Saves bricks copy.
      */
     public void save() {
         for (int i = 0; i < Layout.FIELD; i++) {
             for (int j = 0; j < Layout.FIELD; j++) {
                 bricksBackup[i][j] = bricks[i][j];
                 orientBackup[i][j] = bricks[i][j].getOrientation();
             }
         }
         listBackup.clear();
         listBackup.addAll(list);
     }
     
     /**
      * Restores bricks from copy.
      */
     public void restore() {
         for (int i = 0; i < Layout.FIELD; i++) {
             for (int j = 0; j < Layout.FIELD; j++) {
                 bricks[i][j] = bricksBackup[i][j]; 
                 bricks[i][j].setOrientation(orientBackup[i][j]);
             }
         }
         list.clear();
         list.addAll(listBackup);
     }
     
     /**
      * Method fills field with bricks.
      * 
      * @param add - additional number of bricks
      */
     private void fill(int add) {
         int x, y;
         
         bricks[6][4] = new Brick(level); list.add(bricks[6][4]);
         bricks[6][5] = new Brick(level); list.add(bricks[6][5]);
         bricks[5][6] = new Brick(level); list.add(bricks[5][6]);
         bricks[4][6] = new Brick(level); list.add(bricks[4][6]);
         bricks[3][5] = new Brick(level); list.add(bricks[3][5]);
 
         if (level > 5) {
             bricks[3][4] = new Brick(level); list.add(bricks[3][4]);
         }
         if (level > 6) {
             bricks[4][3] = new Brick(level); list.add(bricks[4][3]);
         }
         if (level > 7) {
             bricks[5][3] = new Brick(level); list.add(bricks[5][3]);
         }
         if (level > 8) {
             bricks[5][4] = new Brick(level); list.add(bricks[5][4]);
         }
         if (level > 9) {
             bricks[4][5] = new Brick(level); list.add(bricks[4][5]);
         }
         
         for (int cnt = 0; cnt < add; cnt++) {
             do {
                 x = generator.nextInt(Layout.FIELD);
                 y = generator.nextInt(Layout.FIELD);
             } while (bricks[x][y] != null);
 
             bricks[x][y] = new Brick(level);
             list.add(bricks[x][y]);
         }
 
         for (int i = 0; i < Layout.FIELD; i++) {
             for (int j = 0; j < Layout.FIELD; j++) {
                 if (bricks[i][j] == null) { 
                     bricks[i][j] = new Brick(); 
                     list.add(bricks[i][j]);
                 }
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
      * Setter for bricks.
      * 
      * @param x - x-position
      * @param y - y-position
      * @param brick - new Brick element
      * @return old Brick element
      */
     public Brick setBrick(int x, int y, Brick brick) {
         Brick old = bricks[x][y];
         bricks[x][y] = brick;
 
         list.remove(old);
         list.add(brick);
 
         return old;
     }
     
     /**
      * Returns true if field contains brick.
      * 
      * @param brick to check for
      * @return boolean value
      */
     public boolean contains(Brick brick) {
         return list.contains(brick);
     }
     
     /**
      * Returns >=0 position of the brick if there is any on selected row or column. 
      * 
      * @param pos - selected row or column, depending on orientation
      * @param orientation - direction to check for brick existance at
      * @return brick position in selected direction or -1 if there is no brick
      */
     public int check(int pos, Orientation orientation) {
         if ((pos < 0) || (pos >= Layout.FIELD)) { return -1; } 
 
         if (orientation == Orientation.LEFT) {
             for (int i = 0; i < Layout.FIELD; i++) {
                 if (!bricks[i][pos].hasSameColor(Brick.BLACK)) {
                     return i;
                 }
             }
         } else if (orientation == Orientation.RIGHT) {
             for (int i = Layout.FIELD - 1; i >= 0; i--) {
                 if (!bricks[i][pos].hasSameColor(Brick.BLACK)) {
                     return i;
                 }
             }
         } else if (orientation == Orientation.TOP) {
             for (int i = 0; i < Layout.FIELD; i++) {
                 if (!bricks[pos][i].hasSameColor(Brick.BLACK)) {
                     return i;
                 }
             }
         } else if (orientation == Orientation.BOTTOM) {
             for (int i = Layout.FIELD - 1; i >= 0; i--) {
                 if (!bricks[pos][i].hasSameColor(Brick.BLACK)) {
                     return i;
                 }
             }
         }
         
         return -1;
     }
 
     /**
      * Method analizes field and processes "special" bricks.
      *
      * @return true if "special" brick was processed 
      */
     private boolean analizeSpecial() {
         for (int i = 0; i < Layout.FIELD; i++) {
             for (int j = 0; j < Layout.FIELD; j++) {
                 if (matrix1[i][j] == BrickColor.SPECIAL_COLORS) {
                     if ((i != 0) && (j != 0) && (matrix1[i - 1][j - 1] >= 0)) {
                         setBrick(i - 1, j - 1, new Brick(
                                 new BrickColor(bricks[i - 1][j - 1].getColor(), level),
                                 bricks[i - 1][j - 1].getOrientation()
                         ));
                     }
                     if ((i != 0) && (matrix1[i - 1][j] >= 0)) {
                         setBrick(i - 1, j, new Brick(
                                 new BrickColor(bricks[i - 1][j].getColor(), level),
                                 bricks[i - 1][j].getOrientation()
                         ));
                     }
                     if ((i != 0) && (j != Layout.FIELD - 1) && (matrix1[i - 1][j + 1] >= 0)) {
                         setBrick(i - 1, j + 1, new Brick(
                                 new BrickColor(bricks[i - 1][j + 1].getColor(), level),
                                 bricks[i - 1][j + 1].getOrientation()
                         ));
                     }
 
                     if ((j != 0) && (matrix1[i][j - 1] >= 0)) {
                         setBrick(i, j - 1, new Brick(
                                 new BrickColor(bricks[i][j - 1].getColor(), level),
                                 bricks[i][j - 1].getOrientation()
                         ));
                     }
                     addBlack(i, j);
                     if ((j != Layout.FIELD - 1) && (matrix1[i][j + 1] >= 0)) {
                         setBrick(i, j + 1, new Brick(
                                 new BrickColor(bricks[i][j + 1].getColor(), level),
                                 bricks[i][j + 1].getOrientation()
                         ));
                     }
 
                     if ((i != Layout.FIELD - 1) && (j != 0) && (matrix1[i + 1][j - 1] >= 0)) {
                         setBrick(i + 1, j - 1, new Brick(
                                 new BrickColor(bricks[i + 1][j - 1].getColor(), level),
                                 bricks[i + 1][j - 1].getOrientation()
                         ));
                     }
                     if ((i != Layout.FIELD - 1) && (matrix1[i + 1][j] >= 0)) {
                         setBrick(i + 1, j, new Brick(
                                 new BrickColor(bricks[i + 1][j].getColor(), level),
                                 bricks[i + 1][j].getOrientation()
                         ));
                     }
                     if ((i != Layout.FIELD - 1) && (j != Layout.FIELD - 1) && (matrix1[i + 1][j + 1] >= 0)) {
                         setBrick(i + 1, j + 1, new Brick(
                                 new BrickColor(bricks[i + 1][j + 1].getColor(), level),
                                 bricks[i + 1][j + 1].getOrientation()
                         ));
                     }
 
                     return true;
 
                 } else if (matrix1[i][j] == BrickColor.SPECIAL_ARROWS) {
                     if ((i != 0) && (j != 0) && (matrix1[i - 1][j - 1] >= 0)) {
                         bricks[i - 1][j - 1].rotate();
                     }
                     if ((i != 0) && (matrix1[i - 1][j] >= 0)) {
                         bricks[i - 1][j].rotate();
                     }
                     if ((i != 0) && (j != Layout.FIELD - 1) && (matrix1[i - 1][j + 1] >= 0)) {
                         bricks[i - 1][j + 1].rotate();
                     }
 
                     if ((j != 0) && (matrix1[i][j - 1] >= 0)) {
                         bricks[i][j - 1].rotate();
                     }
                     addBlack(i, j);
                     if ((j != Layout.FIELD - 1) && (matrix1[i][j + 1] >= 0)) {
                         bricks[i][j + 1].rotate();
                     }
 
                     if ((i != Layout.FIELD - 1) && (j != 0) && (matrix1[i + 1][j - 1] >= 0)) {
                         bricks[i + 1][j - 1].rotate();
                     }
                     if ((i != Layout.FIELD - 1) && (matrix1[i + 1][j] >= 0)) {
                         bricks[i + 1][j].rotate();
                     }
                     if ((i != Layout.FIELD - 1) && (j != Layout.FIELD - 1) && (matrix1[i + 1][j + 1] >= 0)) {
                         bricks[i + 1][j + 1].rotate();
                     }
 
                     return true;
 
                 } else if (matrix1[i][j] == BrickColor.SPECIAL_LIGHTNING) {
                     switch (bricks[i][j].getOrientation()) {
                         case TOP:
                             for (int k = j; k >= 0; k--) {
                                 this.addBlack(i, k);
                             }
                             break;
                         case BOTTOM:
                             for (int k = j; k < Layout.FIELD; k++) {
                                 this.addBlack(i, k);
                             }
                             break;
                         case LEFT:
                             for (int k = i; k >= 0; k--) {
                                 this.addBlack(k, j);
                             }
                             break;
                         case RIGHT:
                             for (int k = i; k < Layout.FIELD; k++) {
                                 this.addBlack(k, j);
                             }
                             break;
                         default:
                     }
                     return true;
 
                 } else if (matrix1[i][j] == BrickColor.SPECIAL_UNIVERSAL) {
                     Brick brick = new Brick();
                     brick.setOrientation(bricks[i][j].getOrientation());
                     switch (brick.getOrientation()) {
                         case TOP:
                             brick.setColor(bricks[i][j - 1].getColor()); break;
                         case BOTTOM:
                             brick.setColor(bricks[i][j + 1].getColor()); break;
                         case LEFT:
                             brick.setColor(bricks[i - 1][j].getColor()); break;
                         case RIGHT:
                             brick.setColor(bricks[i + 1][j].getColor()); break;
                         default:
                     }
                     this.setBrick(i, j, brick);
                     return true;
 
                 } else if (matrix1[i][j] == BrickColor.SPECIAL_BOMB) {
                     if ((i != 0) && (j != 0) && (matrix1[i - 1][j - 1] >= 0)) {
                         matrix1[i - 1][j - 1] = -1; addBlack(i - 1, j - 1);
                     }
                     if ((i != 0) && (matrix1[i - 1][j] >= 0)) {
                         matrix1[i - 1][j] = -1; addBlack(i - 1, j);
                     }
                     if ((i != 0) && (j != Layout.FIELD - 1) && (matrix1[i - 1][j + 1] >= 0)) {
                         matrix1[i - 1][j + 1] = -1; addBlack(i - 1, j + 1);
                     }
 
                     if ((j != 0) && (matrix1[i][j - 1] >= 0)) {
                         matrix1[i][j - 1] = -1; addBlack(i, j - 1);
                     }
                     matrix1[i][j] = -1; addBlack(i, j);
                     if ((j != Layout.FIELD - 1) && (matrix1[i][j + 1] >= 0)) {
                         matrix1[i][j + 1] = -1; addBlack(i, j + 1);
                     }
 
                     if ((i != Layout.FIELD - 1) && (j != 0) && (matrix1[i + 1][j - 1] >= 0)) {
                         matrix1[i + 1][j - 1] = -1; addBlack(i + 1, j - 1);
                     }
                     if ((i != Layout.FIELD - 1) && (matrix1[i + 1][j] >= 0)) {
                         matrix1[i + 1][j] = -1; addBlack(i + 1, j);
                     }
                     if ((i != Layout.FIELD - 1) && (j != Layout.FIELD - 1) && (matrix1[i + 1][j + 1] >= 0)) {
                         matrix1[i + 1][j + 1] = -1; addBlack(i + 1, j + 1);
                     }
 
                     return true;
                 }
             }
         }
         return false;
     }
 
     /**
      * Method analizes field to remove >=3 same color bricks and calculate scores.
      * 
      * @return scores int value
      */
     public int analize() {
         int cnt;
 
         // constructing original matrix
         for (int i = 0; i < Layout.FIELD; i++) {
             for (int j = 0; j < Layout.FIELD; j++) {
                 matrix1[i][j] = bricks[i][j].getColor().getIndex();
                 matrix2[i][j] = 0;
             }
         }
 
         // processing special bricks
         boolean sp = analizeSpecial();
 
         // calculating neighsbourhood
         for (int i = 0; i < Layout.FIELD; i++) {
             for (int j = 0; j < Layout.FIELD; j++) {
                 if (matrix1[i][j] >= 0) {
                     cnt = 0;
                     if ((i != 0) && (matrix1[i - 1][j] == matrix1[i][j])) {
                         cnt = cnt + 1;
                     }
                     if ((i != Layout.FIELD - 1) && (matrix1[i + 1][j] == matrix1[i][j])) {
                         cnt = cnt + 1;
                     }
                     if ((j != 0) && (matrix1[i][j - 1] == matrix1[i][j])) {
                         cnt = cnt + 1;
                     }
                     if ((j != Layout.FIELD - 1) && (matrix1[i][j + 1] == matrix1[i][j])) {
                         cnt = cnt + 1;
                     }
                     matrix2[i][j] = cnt;
                 }
             }
         }
         
         // removing less than 3-brick chains
         for (int i = 0; i < Layout.FIELD; i++) {
             for (int j = 0; j < Layout.FIELD; j++) {
                 if (matrix2[i][j] == 1) {
                     cnt = 0;
                     if ((i != 0) && (matrix1[i - 1][j] == matrix1[i][j]) && (matrix2[i - 1][j] > 1)) {
                         cnt = cnt + 1;
                     }
                     if ((i != Layout.FIELD - 1) && (matrix1[i + 1][j] == matrix1[i][j]) && (matrix2[i + 1][j] > 1)) {
                         cnt = cnt + 1;
                     }
                     if ((j != 0) && (matrix1[i][j - 1] == matrix1[i][j]) && (matrix2[i][j - 1] > 1)) {
                         cnt = cnt + 1;
                     }
                     if ((j != Layout.FIELD - 1) && (matrix1[i][j + 1] == matrix1[i][j]) && (matrix2[i][j + 1] > 1)) {
                         cnt = cnt + 1;
                     }
                     if (cnt == 0) { matrix2[i][j] = 0; }
                 }
             }
         }
         
         // calculating bricks to remove count
         cnt = 0;
         for (int i = 0; i < Layout.FIELD; i++) {
             for (int j = 0; j < Layout.FIELD; j++) {
                 if (matrix2[i][j] > 0) {
                     addBlack(i, j);
                     cnt = cnt + 1;
                 }
             }
         }
         
         // calculating and returning scores value
         cnt = (cnt - 2) * 3;
         cnt = cnt < 0 ? 0 : cnt;
         if ((cnt == 0) && sp) { return -1; }
         return cnt;
     }
     
     /**
      * Method changes two brick positions to each other`s.
      * 
      * @param x1 - first brick X-pos
      * @param y1 - first brick Y-pos
      * @param x2 - second brick X-pos
      * @param y2 - second brick Y-pos
      */
     private void swap(int x1, int y1, int x2, int y2) {
         Brick tmp = bricks[x1][y1];
         bricks[x1][y1] = bricks[x2][y2];
         bricks[x2][y2] = tmp;
     }
     
     /**
      * Add black-colored brick to selected position.
      * 
      * @param x - X-pos
      * @param y - Y-pos
      */
     private void addBlack(int x, int y) {
         list.remove(bricks[x][y]);
         bricks[x][y] = new Brick();
         list.add(bricks[x][y]);
     }
     
     /**
      * Method moves oriented bricks if possible.
      * 
      * @return int count of moved bricks
      */
     public int move() {
         boolean[][] moved = new boolean[Layout.FIELD][Layout.FIELD]; 
         for (int y = 0; y < Layout.FIELD; y++) {
             for (int x = 0; x < Layout.FIELD; x++) {
                 moved[x][y] = false;                
             }
         }
 
         int cnt = 0;
         for (int y = 0; y < Layout.FIELD; y++) {
             for (int x = 0; x < Layout.FIELD; x++) {
                 if (!moved[x][y]) {
 
                     Orientation orientation = bricks[x][y].getOrientation();
                     if (orientation != Orientation.NONE) {
     
                         if (orientation == Orientation.RIGHT) {
                             if (x + 1 == Layout.FIELD) {
                                 rightBricks.shiftBack(y, bricks[x][y]);
                                 addBlack(x, y);
                                 cnt += 1;
                             } else if (bricks[x + 1][y].hasSameColor(BrickColor.BLACK)) {
                                 swap(x, y, x + 1, y);
                                 cnt += 1;
                                 moved[x + 1][y] = true;
                             }
                         } 
                         
                         if (orientation == Orientation.LEFT) {
                             if (x == 0) {
                                 leftBricks.shiftBack(y, bricks[x][y]);
                                 addBlack(x, y);
                                 cnt += 1;
                             } else if (bricks[x - 1][y].hasSameColor(BrickColor.BLACK)) {
                                 swap(x, y, x - 1, y);
                                 cnt += 1;
                                 moved[x - 1][y] = true;
                             }
                         } 
                         
                         if (orientation == Orientation.BOTTOM) {
                             if (y + 1 == Layout.FIELD) {
                                 bottomBricks.shiftBack(x, bricks[x][y]);
                                 addBlack(x, y);
                                 cnt += 1;
                             } else if (bricks[x][y + 1].hasSameColor(BrickColor.BLACK)) {
                                 swap(x, y + 1, x, y);
                                 cnt += 1;
                                 moved[x][y + 1] = true;
                             }
                         } 
     
                         if (orientation == Orientation.TOP) {
                             if (y == 0) {
                                 topBricks.shiftBack(x, bricks[x][y]);
                                 addBlack(x, y);
                                 cnt += 1;
                             } else if (bricks[x][y - 1].hasSameColor(BrickColor.BLACK)) {
                                 swap(x, y, x, y - 1);
                                 cnt += 1;
                                 moved[x][y - 1] = true;
                             }
                         } 
                     }
                 }
             }
         }
         
         return cnt;
     }
     
     /**
      * Method checks for playing possibility: if there is no possible 
      * moves from brick collections to field then game finished. 
      * 
      * @return true if game has been finished
      */
     public boolean canPlay() {
         int d;
         for (int i = 0; i < Layout.FIELD; i++) {
             d = check(i, Orientation.LEFT);
            if (d > 0 && d < 9) { return true; }
             d = check(i, Orientation.RIGHT);
            if (d > 0 && d < 9) { return true; }
             d = check(i, Orientation.TOP);
            if (d > 0 && d < 9) { return true; }
             d = check(i, Orientation.BOTTOM);
            if (d > 0 && d < 9) { return true; }
         }
         return false;
     }
 }
