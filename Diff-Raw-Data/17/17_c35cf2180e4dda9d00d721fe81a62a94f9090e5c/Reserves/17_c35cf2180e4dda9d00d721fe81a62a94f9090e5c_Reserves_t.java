 package gipfj;
 
 import java.util.Random;
 
 /*
  * Gipf count: how many are on the board
  * Piece count: how many are in reserve.
  * 
  * What about: piece-on-field count??.
  * not important
  *  
  */
 public class Reserves {
     public static boolean DEBUG = false;
     private final static int[][] hashArray = makeHashArray();
     public final static int MAX_CAPACITY = 18;
     public final static int MAX_GIPFS_ON_BOARD = 9;

     public static Reserves change(Reserves r, int player,
             int delta_reserve_pieces, int delta_board_pieces,
             int delta_board_gipfs) {
         return r.applyDelta(player, delta_reserve_pieces, delta_board_pieces,
                 delta_board_gipfs);
     }

     /**
      * Are these reserves equal?
      * 
      * @param a
      * @param b
      * @return
      */
     public static boolean equiv(Reserves a, Reserves b) {
         return (a == b)
                 || ((a.p1 == b.p1) && (a.p2 == b.p2) && (a.g1 == b.g1)
                         && (a.g2 == a.g2) && (a.o1 == b.o1) && (a.o2 == a.o2));
     }

     public static long getGipfs(Reserves r, long player) {
         if (player > 0) {
             return r.g1;
         } else {
             return r.g2;
         }
     }
 
     public static long getPieces(Reserves r, long player) {
         if (player > 0) {
             return r.o1;
         } else {
             return r.o2;
         }
     }

     public static long getReserves(Reserves r, long player) {
         if (player > 0) {
             return r.p1;
         } else {
             return r.p2;
         }
     }
 
     public static long getTotalPieces(Reserves r, long player) {
         if (player > 0) {
             return r.o1 + r.p1;
         } else {
             return r.o1 + r.p1;
         }
     }
 
     private static int[][] makeHashArray() {
         // some fields are unused - whatever, it is a once-only cost
         int[][] a = new int[6][MAX_CAPACITY + 1];
         Random rng = new Random(7987897987987897L);
         for (int z = 0; z < a.length; z++) {
             for (int q = 0; q < a[z].length; q++) {
                 a[z][q] = rng.nextInt();
             }
         }
         return a;
     }
 
     public static Reserves makeReserves(long r, long p, long g) {
         return new Reserves((int) r, (int) r, (int) p, (int) p, (int) g,
                 (int) g);
     }
 
     public static void mutateArray(int[] arr, int player,
             int delta_reserve_pieces, int delta_board_pieces,
             int delta_board_gipfs) {
         if (player > 0) {
             // it isn't worth it to incrementally calculate the hash code.
             // case 1: we do. 6 XORS (3,3)
             // case 2: we don't. 6 XORS (6)
             arr[0] += delta_reserve_pieces;
             arr[2] += delta_board_pieces;
             arr[4] += delta_board_gipfs;
         } else {
             arr[1] += delta_reserve_pieces;
             arr[3] += delta_board_pieces;
             arr[5] += delta_board_gipfs;
         }
     }
 
     /**
      * Were any pieces taken between these these two moves? (do they have the
      * same number of total pieces, and same number of GIPFs on field?)
      * 
      * @param a
      * @param b
      * @return
      */
    public static boolean wasTaken(Reserves prev, Reserves next) {
        return (prev.g1 > next.g1) || (prev.g2 > next.g2)
                || ((prev.o1 + prev.p1) != (prev.o1 + prev.p1));
     }
 
     public final int g1;
 
     public final int g2;
 
     // Statics...
 
     public final int hashCode;
 
     public final int o1;
 
     public final int o2;
 
     public final int p1;
 
     public final int p2;
 
     /**
      * 
      * @param p1
      *            Pieces in reserve
      * @param p2
      * @param o1
      *            Pieces on board
      * @param o2
      * @param g1
      *            GIPF pieces on board
      * @param g2
      */
     public Reserves(int p1, int p2, int o1, int o2, int g1, int g2) {
         this.p1 = p1;
         this.p2 = p2;
         this.g1 = g1;
         this.g2 = g2;
         this.o1 = o1;
         this.o2 = o2;
         hashCode = calcHashCode(p1, p2, g1, g2, o1, o2);
     }
 
     public Reserves(int p1, int p2, int o1, int o2, int g1, int g2, int hc) {
         this.p1 = p1;
         this.p2 = p2;
         this.g1 = g1;
         this.g2 = g2;
         this.o1 = o1;
         this.o2 = o2;
         hashCode = hc;
     }
 
     public Reserves(int[] arr) {
         this.p1 = arr[0];
         this.p2 = arr[1];
         this.o1 = arr[2];
         this.o2 = arr[3];
         this.g1 = arr[4];
         this.g2 = arr[5];
         hashCode = calcHashCode(p1, p2, g1, g2, o1, o2);
     }
 
     /**
      * Change reserve amounts
      * 
      * @param in
      * @param player
      * @param del_reserve_pieces
      *            Change in pieces in reserve
      * @param del_board_pieces
      *            Change in pieces on board
      * @param del_board_gipfs
      *            Change in GIPF pieces on board
      * @return
      */
     public Reserves applyDelta(int player, int delta_reserve_pieces,
             int delta_board_pieces, int delta_board_gipfs) {
         if (player > 0) {
             // it isn't worth it to incrementally calculate the hash code.
             // case 1: we do. 6 XORS (3,3)
             // case 2: we don't. 6 XORS (6)
 
             return new Reserves(p1 + delta_reserve_pieces, p2, o1
                     + delta_board_pieces, o2, g1 + delta_board_gipfs, g2);
         } else {
             return new Reserves(p1, p2 + delta_reserve_pieces, o1, o2
                     + delta_board_pieces, g1, g2 + delta_board_gipfs);
         }
     }
 
     private int calcHashCode(int p1, int p2, int g1, int g2, int o1, int o2) {
 
         // debug on crash
         if (DEBUG) {
             System.out.format("%d, %d, %d, %d, %d, %d\n", p1, p2, o1, o2, g1,
                     g2);
         }
 
         int r = hashArray[0][p1];
         r ^= hashArray[1][p2];
         r ^= hashArray[2][o1];
         r ^= hashArray[3][o2];
         r ^= hashArray[4][g1];
         r ^= hashArray[5][g2];
 
         return r;
     }
 
     @Override
     public int hashCode() {
         return hashCode;
     }
 
     public boolean losingReserve(int player, boolean gipfphase) {
         if (gipfphase) {
             if (player > 0) {
                 return (p1 <= 0);
             } else {
                 return (p2 <= 0);
             }
         } else {
             if (player > 0) {
                 return (p1 <= 0) || (g1 <= 0);
             } else {
                 return (p2 <= 0) || (g2 <= 0);
             }
         }
     }
 
     public int numReserves(int player) {
         if (player > 0) {
             return p1;
         } else {
             return p2;
         }
     }
 
     /**
      * 
      * 
      * @param player
      * @return
      */
    public boolean overexetended(int player) {
         if (player > 0) {
             return g1 < 0;
         } else {
             return g2 < 0;
         }
     }
 
     public int[] toArray() {
         int[] r = { p1, p2, o1, o2, g1, g2 };
         return r;
     }
 
     @Override
     public String toString() {
         return String.format("reserves r %d %d : p %d %d : g %d %d", p1, p2,
                 o1, o2, g1, g2);
     }
 }
