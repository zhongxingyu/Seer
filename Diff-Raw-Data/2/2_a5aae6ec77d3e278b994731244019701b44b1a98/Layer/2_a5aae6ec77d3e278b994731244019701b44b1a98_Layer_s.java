 package amber.data.map;
 
 import amber.data.sparse.SparseMatrix;
 import amber.data.sparse.SparseVector;
 
 /**
  *
  * @author Tudor
  */
 public class Layer implements Cloneable {
 
     protected int width, length;
     protected SparseVector<SparseMatrix<Tile>> tiles = new SparseVector<SparseMatrix<Tile>>();
     protected SparseVector<SparseMatrix<Flag>> flags = new SparseVector<SparseMatrix<Flag>>();
     protected String name;
 
     public Layer(String name, int width, int length) {
         this.width = width;
         this.length = length;
         this.name = name;
     }
 
     public Layer clone() {
         Layer clone = new Layer(name, width, length);
         clone.tiles = tiles.clone();
         clone.flags = flags.clone();
         return clone;
     }
 
     public Tile getTile(int x, int y, int z) {
         SparseMatrix<Tile> alt = tiles.get(z);
         return alt == null ? null : alt.get(x, y);
     }
 
     public void setTile(int x, int y, int z, Tile t) {
         SparseMatrix<Tile> alt = tiles.get(z);
         if (alt == null) {
             tiles.set(z, alt = new SparseMatrix<Tile>(Math.max(width, length)));
         }
         alt.put(x, y, t);
     }
 
     public Flag getFlag(int x, int y, int z) {
         SparseMatrix<Flag> alt = flags.get(z);
         return alt == null ? null : alt.get(x, y);
     }
 
     public void setFlag(int x, int y, int z, Flag f) {
         SparseMatrix<Flag> alt = flags.get(z);
         if (alt == null) {
             flags.set(z, alt = new SparseMatrix<Flag>(Math.max(width, length)));
         }
         alt.put(x, y, f);
     }
 
     public SparseVector<SparseMatrix<Tile>> tileMatrix() {
         return tiles;
     }
 
     public SparseVector<SparseMatrix<Flag>> flagMatrix() {
         return flags;
     }
 
     /**
      * @return the width
      */
     public int getWidth() {
         return width;
     }
 
     /**
      * @return the length
      */
     public int getLength() {
         return length;
     }
 
     /**
      * @return the length
      */
     public int getHeight() {
        return tiles.size();
     }
 
     public String getName() {
         return name; // Temporary
     }
 }
