 package gipfj;
 
 import java.util.Iterator;
 
 /**
  * Clone of IncrementalGameCalc; signs its moves, uses a move ordering.
  * 
  * @author msto
  * 
  */
 public class MoveSignedIGC implements Iterator<MoveSignedGS> {
 
     private final int[] order;
     private final Board[] g1 = new Board[3];
     private final Reserves[] g1r1 = new Reserves[3];
     private final Reserves[] g1r2 = new Reserves[3];
     private final MoveSignedGS[] g3 = new MoveSignedGS[3];
     private int plo;
     private int g3_pos;
     private int g3_end;
     private int g1_pos;
     private int g1_end;
     private byte player;
     private boolean ready;
     private int origHash;
     private boolean gipfs;
     private boolean oppg;
     private int glvl;
 
     private MoveSignedIGC(GameState g, long p, int[] ordering) {
         this.player = (byte) p;
         order = ordering;
 
         g1_pos = 0;
         g1_end = 0;
         for (GameState q : GameCalc.getLineTakingResults(g, player)) {
             if (!q.losingGameState(player)) {
                 g1[g1_end] = q.b;
                 g1r1[g1_end] = q.r.applyDelta(player, -1, 1, 0);
                 if (player > 0) {
                    if (q.r.p1 >= 2) {
                         g1r2[g1_end] = q.r.applyDelta(player, -2, 0, 1);
                     }
                 } else {
                    if (q.r.p2 >= 2) {
                         g1r2[g1_end] = q.r.applyDelta(player, -2, 0, 1);
                     }
                 }
                 g1_end++;
             }
         }
 
         if (player > 0) {
             gipfs = g.gphase1;
             oppg = g.gphase2;
         } else {
             gipfs = g.gphase2;
             oppg = g.gphase1;
         }
 
         if (g1_end == 0) {
             ready = false;
             return;
         }
 
         origHash = g.b.hashCode;
 
         plo = 0;
         glvl = 1;
         boostG3();
     }
 
     private void boostG3() {
         MoveSignedGS f = getNextMoveResult();
         if (f == null) {
             ready = false;
         } else {
             ready = true;
             g3_pos = 0;
             g3_end = 0;
             for (GameState q : GameCalc.getLineTakingResults(f, player)) {
                 g3[g3_end] = (MoveSignedGS) q;
                 g3_end++;
             }
         }
     }
 
     @Override
     public MoveSignedGS next() {
 
         MoveSignedGS result = g3[g3_pos];
 
         g3_pos++;
 
         if (g3_pos == g3_end) {
             boostG3();
         }
 
         return result;
     }
 
     @Override
     public boolean hasNext() {
         return ready;
     }
 
     private MoveSignedGS getNextMoveResult() {
         // works off the g1 buffer; writes two at a time into g2
 
         int[] pd = null;
         while (plo < 42) {
             pd = HistoryTable.listOfPushPoints[order[plo]];
             byte[] orig = g1[g1_pos].data;
 
             boolean select = false;
 
             if (glvl == 2 && g1r2[g1_pos] == null) {
                 select = false;
             } else {
                 for (int i = 0; i < pd.length; i++) {
                     if (orig[pd[i]] == 0) {
                         select = true;
                         break;
                     }
                 }
             }
 
             if (select) {
                 break;
             }
 
             glvl++;
             if (!gipfs || glvl == 3) {
                 glvl = 1;
                 g1_pos++;
                 if (g1_pos == g1_end) {
                     g1_pos = 0;
                     plo++;
                 }
             }
         }
 
         if (plo == 42) {
             return null;
         }
 
         // we want to ensure resource sharing among reserves
         byte[] source = g1[g1_pos].data;
         Reserves ruk;
         if (glvl == 1) {
             ruk = g1r1[g1_pos];
         } else {
             ruk = g1r2[g1_pos];
         }
 
         byte[] r = new byte[Board.SIZE];
         System.arraycopy(source, 0, r, 0, Board.SIZE);
         int hcr = origHash;
 
         byte last = player;
         for (int j = 0; j < pd.length; j++) {
             int ind = pd[j];
             byte v = r[ind];
             hcr ^= Board.hashArray[ind][v + 2] ^ Board.hashArray[ind][last + 2];
             r[ind] = last;
             if (v == 0) {
                 break;
             }
             last = v;
         }
 
         MoveSignedGS result;
         if (player > 0) {
             result = new MoveSignedGS(new Board(r, hcr), ruk, glvl == 2, oppg,
                     order[plo]);
         } else {
             result = new MoveSignedGS(new Board(r, hcr), ruk, oppg, glvl == 2,
                     order[plo]);
         }
         plo++;
         return result;
     }
 
     /**
      * Won't do.
      */
     @Override
     public void remove() {
         throw new UnsupportedOperationException();
     }
 
     // CLJR INTEROP
 
     private static final int[] DEFAULT_MOVE_ORDER = gdfmo();
 
     private static int[] gdfmo() {
         int[] q = new int[42];
         for (int i = 0; i < 42; i++) {
             q[i] = i;
         }
         return q;
     }
 
     public static MoveSignedIGC makeIncrementalGameCalc(GameState g, long player) {
         return new MoveSignedIGC(g, player, DEFAULT_MOVE_ORDER);
     }
 
     public static MoveSignedIGC makeMSIGC(GameState g, long player,
             int[] ordering) {
         return new MoveSignedIGC(g, player, ordering);
     }
 
 }
