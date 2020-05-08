 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package hashJoin.gammaSupport;
 
 /**
  *
  * @author Don
  */
 public class BMap implements GammaConstants {
    static char FWALSE = 'f';
    static char TWUE = 't';
     boolean[][] map;
 
     // constructors and factory methods
     private BMap() {
         map = new boolean[splitLen][mapSize];
     }
 
     // make a BMap from a serialized string produced by getBloomFilter()
     public static BMap makeBMap(String b) {
         BMap bm = new BMap();
         checksize(b);
 
         for (int i = 0; i < splitLen; i++) {
             for (int j = 0; j < mapSize; j++) {
                bm.map[i][j] = (b.charAt(index(i, j)) == TWUE);
             }
         }
         return bm;
     }
 
     // create and initialize a BMap
     public static BMap makeBMap() {
         BMap bm = new BMap();
         for (int i = 0; i < splitLen; i++) {
             for (int j = 0; j < mapSize; j++) {
                 bm.map[i][j] = false;
             }
         }
         return bm;
     }
 
     // return the disjunction of BMaps m1 and m2
     // this method is used in MMerge
     public static BMap or(BMap m1, BMap m2) {
         BMap bm = new BMap();
 
         for (int i = 0; i < splitLen; i++) {
             for (int j = 0; j < mapSize; j++) {
                 bm.map[i][j] = m1.map[i][j] | m2.map[i][j];
             }
         }
         return bm;
     }
 
     // simple check to see if String is of correct length for a BMap encoding
     private static void checksize(String b) {
         if (b.length() != mapSize * splitLen) {
             ReportError.msg("map of incorrect length used " + b.length() + "!=" + mapSize * splitLen);
         }
     }
 
     private static int index(int i, int j) {
         return i * mapSize + j;
     }
 
     // this method splits a BMAP (or really its string representation)
     // into its constituent strings.  It is the "inverse" of the or() method above
     // see getBloomFilter (below) for coding of rows
     public static String mask(String s, int m) {
         int j;
 
         char[] ss = new char[mapSize * splitLen];
         for (int i = 0; i < splitLen; i++) {
             // either copy row i=m or zero it out
             if (i == m) { // copy row
                 for (j = 0; j < mapSize; j++) {
                     ss[index(i, j)] = s.charAt(index(i, j));
                 }
             } else {  // zero out row
                 for (j = 0; j < mapSize; j++) {
                    ss[index(i, j)] = FWALSE;
                 }
             }
         }
         String result = new String(ss);
         //System.err.println(">>> " + s + ">>> " + result + "<<<" + m);
         return result;
     }
 
     //---------------- object methods -------
     // return the BMap value for the hash of x
     public boolean getValue(String x) {
         int h = hsh(x);
         int i = h % splitLen;
         int j = h % mapSize;
         return map[i][j];
     }
 
     // set the BMap value for the hash of x to be v
     public void setValue(String x, boolean v) {
         int h = hsh(x);
         int i = h % splitLen;
         int j = h % mapSize;
         map[i][j] = v;
     }
 
     private int length() {
         return map.length;
     }
 
     // this method serializes a Bloom filter into a string
     public String getBloomFilter() {
         char[] array = new char[mapSize * splitLen];
 
         for (int i = 0; i < splitLen; i++) {
             for (int j = 0; j < mapSize; j++) {
                array[index(i, j)] = map[i][j] ? TWUE : FWALSE;
             }
         }
 
         return new String(array);
     }
 
     // this method takes a joinkey and determines which substream
     // the corresponding tuple is to be mapped.  It is used primarily
     // if not exclusively by SPLIT
     public static int myhash(String x) {
         int h = hsh(x);
         return h % splitLen;
     }
 
     private static int hsh(String x) {
         return Math.abs(x.hashCode());
     }
 }
