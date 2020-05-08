 package gipfj;
 
 // LONGTERM: create instances of this for passable
 // use inside threads... That way allocation, buffers 
 // still are minimized, and we do not have interference
 // or synchonization issues.
 
 public class GameCalc {
 
     public static boolean lineFull(Board b, int start, int delta) {
         int le = Geometry.lend(start, delta);
 
         while (start != le) {
             if (b.data[start] == 0) {
                 return false;
             }
             start = Geometry.padd(start, delta);
         }
         if (b.data[start] == 0) {
             return false;
         }
         return true;
     }
 
     public static boolean lineFull(Board b, Line l) {
         int q = Line.getStart(l);
         int delta = Line.getDelta(l);
         return lineFull(b, q, delta);
     }
 
     public static final Line[] listOfLines = getLoL();
     public static final int[][] listOfLinePoints = getLolP(listOfLines);
 
     private static Line[] getLoL() {
         Line[] lol = new Line[21];
 
         // why I love macros/regexps!
         lol[0] = Line.makeSignedLine(0, 25, 1);
         lol[2] = Line.makeSignedLine(2, 26, 1);
         lol[3] = Line.makeSignedLine(3, 27, 1);
         lol[4] = Line.makeSignedLine(4, 28, 1);
         lol[5] = Line.makeSignedLine(5, 29, 1);
         lol[6] = Line.makeSignedLine(6, 30, 1);
         lol[7] = Line.makeSignedLine(7, 31, 1);
         lol[8] = Line.makeSignedLine(8, 22, 6);
         lol[9] = Line.makeSignedLine(9, 23, 6);
         lol[10] = Line.makeSignedLine(10, 24, 6);
         lol[11] = Line.makeSignedLine(11, 25, 6);
         lol[12] = Line.makeSignedLine(12, 26, 6);
         lol[13] = Line.makeSignedLine(13, 27, 6);
         lol[14] = Line.makeSignedLine(14, 28, 6);
         lol[15] = Line.makeSignedLine(15, 19, 5);
         lol[16] = Line.makeSignedLine(16, 20, 5);
         lol[17] = Line.makeSignedLine(17, 21, 5);
         lol[18] = Line.makeSignedLine(18, 22, 5);
         lol[19] = Line.makeSignedLine(19, 23, 5);
         lol[20] = Line.makeSignedLine(20, 24, 5);
         lol[1] = Line.makeSignedLine(1, 25, 5);
 
         return lol;
     }
 
     private static int[][] getLolP(Line[] lol) {
         int[][] foo = new int[21][];
         // no lines longer than this...
         int[] buffer = new int[8];
 
         int x = 0;
         for (Line l : lol) {
             int s = Line.getStart(l);
             int d = Line.getDelta(l);
 
             int e = Geometry.lend(s, d);
             int length = Geometry.pdistance(e, s);
 
             for (int k = 0; k <= length; k++) {
                 buffer[k] = s;
 
                 s = Geometry.padd(s, d);
             }
 
             int[] fin = new int[length + 1];
             System.arraycopy(buffer, 0, fin, 0, length + 1);
             foo[x] = fin;
 
             x++;
         }
 
         return foo;
     }
 
     private static Line[] gblres = new Line[21];
 
     public static Line[] getBoardLines(Board b) {
         int found = 0;
 
         for (int xx = 0; xx < 21; xx++) {
             int[] l = listOfLinePoints[xx];
             int sign = 0;
             int count = 1;
             int len = l.length;
             for (int kk = 0; kk < len; kk++) {
                 int v = b.data[l[kk]];
                 if (v == 0 || (v * sign <= 0)) {
                     count = 1;
                     sign = v;
                 } else {
                     count++;
                 }
 
                 if (count == 4) {
                     if (sign > 0) {
                         gblres[found] = Line.sign(listOfLines[xx], 1);
                     } else {
                         gblres[found] = Line.sign(listOfLines[xx], -1);
                     }
                     found++;
                     break;
                 }
             }
         }
 
         Line[] foo = new Line[found];
         System.arraycopy(gblres, 0, foo, 0, found);
 
         return foo;
     }
 
     public static String toString(Board b) {
         return "<board ...>";
     }
 
     private static Line[] buff = new Line[21];
 
     public static Line[] filterLines(Line[] ls, int player) {
         int yy = 0;
         for (Line l : ls) {
             if (Line.getSig(l) == player) {
                 buff[yy] = l;
                 yy++;
             }
         }
 
         Line[] foo = new Line[yy];
         System.arraycopy(buff, 0, foo, 0, yy);
 
         return foo;
     }
 
     private static Line[] tried = new Line[84];
 
     public static GameState[] getLineTakingResults(GameState g, int player) {
         // returns a sequence of stuff like this:
         // ( [[[prot] take1 take2 ...] ...)
         // but typically just:
         //
         // ( [[[prot] take 1 take 2..]]
         //
         // i.e., the cheapo take-first way
 
         GameState[] results = new GameState[1];
 
         GameState curr = g;
 
         int t = 0;
 
         Line[] found;
         while (true) {
             found = filterLines(getBoardLines(curr.b), player);
             boolean k = false;
             for (Line f : found) {
                 boolean c = false;
                 for (int i = 0; i < t; i++) {
                     if (Line.same(tried[i], f)) {
                         c = true;
                         break;
                     }
                 }
 
                 if (c) {
                     continue;
                 }
 
                 curr = simpleLineEmpty(curr, f, player);
                 tried[t] = f;
                 t++;
                 k = true;
                 break;
             }
             if (k == false) {
                 break;
             }
         }
         results[0] = curr;
 
         return results;
     }
 
     private static int[] gicbuf = new int[7];
 
     public static int[] getImpactedCells(Board b, int loc, int delta) {
         byte[] d = b.data;
         int m = 0;
         while (true) {
             int ind = loc;
             gicbuf[m] = loc;
             m++;
             if (d[ind] == 0) {
                 break;
             }
 
             loc = Geometry.padd(loc, delta);
         }
 
         int[] res = new int[m];
         System.arraycopy(gicbuf, 0, res, 0, m);
         return res;
     }
 
     /*
      * Takes a well adjusted (start radius 3) line as `shove`
      * 
      * THIS SHOULD ONLY BE CALLED EXTERNALLY. It sets the GameState gphase.
      */
     public static GameState placeAndShove(GameState foo, long pieceval,
             Line shove) {
         byte[] cdata = new byte[Board.SIZE];
         System.arraycopy(foo.b.data, 0, cdata, 0, Board.SIZE);
         // yay! I can mutate!
 
         int q = Line.getStart(shove);
         int d = Line.getDelta(shove);
 
         // TODO: make this return a MoveSignedGameState, by means of a
         // line-lookup-hook
 
         int hc = foo.b.hashCode;
 
         byte last = (byte) pieceval;
         while (true) {
             byte v = cdata[q];
             hc ^= Board.hashArray[q][v + 2] ^ Board.hashArray[q][last + 2];
             cdata[q] = last;
 
             // should occur before radius == 4
             if (v == 0) {
                 break;
             }
 
             last = v;
 
             q = Geometry.padd(q, d);
         }
 
         if (pieceval > 0) {
             if (foo.gphase1 && pieceval == 2) {
                 return new GameState(new Board(cdata, hc), foo.r.applyDelta(
                        (int) pieceval, -2, 0, 1), true, foo.gphase2);
             } else {
                 return new GameState(new Board(cdata, hc), foo.r.applyDelta(
                         (int) pieceval, -1, 1, 0), false, foo.gphase2);
             }
         } else {
             if (foo.gphase2 && pieceval == -2) {
                 return new GameState(new Board(cdata, hc), foo.r.applyDelta(
                        (int) pieceval, -2, 0, 1), foo.gphase1, true);
             } else {
                 return new GameState(new Board(cdata, hc), foo.r.applyDelta(
                         (int) pieceval, -1, 1, 0), foo.gphase1, false);
             }
         }
     }
 
     private static GameState simpleLineEmpty(GameState curr, Line found,
             int player) {
         // basically, over the line we do .... something.
 
         byte[] cdata = new byte[Board.SIZE];
         System.arraycopy(curr.b.data, 0, cdata, 0, Board.SIZE);
         Reserves rr = curr.r;
         int hc = curr.b.hashCode;
 
         // I could, theoretically, have a lookup table
         // of qq[hex4][hex1] -> int[up to 7].
         // Why? because there is a _lot_
         // of inefficiency with repeated padds & lends
         // best is one lookup on a large table, then
         // almost no calcs - 1/5th the cost
 
         int q = Line.getStart(found);
         int d = Line.getDelta(found);
         int le = Geometry.lend(q, d);
 
         while (true) {
             int v = player * cdata[q];
             // System.out.format("v: %d p:%d d:%d d:%d q:%d %s\n", v, player,
             // cdata[q], d, q, rr.toString());

            // / System.out.format("%d %d, %d %d, %d %d\n", rr.p1, rr.p2, rr.o1,
            // rr.o2, rr.g1, rr.g2);

             if (v < 0) {
                 cdata[q] = 0;
                 hc ^= Board.hashArray[q][2] ^ Board.hashArray[q][v + 2];
                 if (v == -2) {
                     rr = rr.applyDelta(-player, 0, 0, -1);
                 } else {
                     rr = rr.applyDelta(-player, 0, -1, 0);
                 }
             } else if (v == 1) {
                 hc ^= Board.hashArray[q][2] ^ Board.hashArray[q][3];
                 cdata[q] = 0;
                 rr = rr.applyDelta(player, 1, -1, 0);
             }
 
             if (q == le) {
                 break;
             }
 
             q = Geometry.padd(q, d);
         }
 
         return curr
                 .change(new Board(cdata, hc), rr, curr.gphase1, curr.gphase2);
     }
 
     private static final GameState[] bgk = new GameState[84];
 
     private static GameState[] getMoveMakingResults(GameState gs, int player) {
 
         // FORTRAN IN ANY LANGUAGE!
 
         int mm = 0;
 
         // we iterate over LOL. forwards AND backwards
 
         byte[] orig = gs.b.data;
         Reserves[] decced = new Reserves[3];
         decced[1] = gs.r.applyDelta(player, -1, 1, 0);
         int q;
         if (gs.getPhase(player) && gs.r.numReserves(player) > 2) {
             decced[2] = gs.r.applyDelta(player, -2, 0, 1);
             q = 2;
         } else {
             q = 1;
         }
 
         for (int jkl = 1; jkl <= q; jkl++) {
             boolean alpha;
             boolean beta;
             if (player > 0) {
                 alpha = (jkl == 2);
                 beta = gs.gphase2;
             } else {
                 alpha = gs.gphase1;
                 beta = (jkl == 2);
             }
 
             for (int[] n : listOfLinePoints) {
                 // question: iterate twice, or allocate and discard?
                 // answer: iterate 1 1/2 times!
 
                 boolean skip = true;
                 for (int i = 0; i < n.length; i++) {
                     if (orig[n[i]] == 0) {
                         skip = false;
                         break;
                     }
                 }
 
                 if (skip) {
                     continue;
                 }
 
                 int hcu = gs.b.hashCode;
                 int hcd = gs.b.hashCode;
                 byte[] up = new byte[Board.SIZE];
                 System.arraycopy(orig, 0, up, 0, Board.SIZE);
                 byte[] down = new byte[Board.SIZE];
                 System.arraycopy(orig, 0, down, 0, Board.SIZE);
 
                 byte last = (byte) (jkl * player);
                 for (int j = 0; j < n.length; j++) {
                     int ind = n[j];
                     byte v = up[ind];
                     hcu ^= Board.hashArray[ind][v + 2]
                             ^ Board.hashArray[ind][last + 2];
                     up[ind] = last;
                     if (v == 0) {
                         break;
                     }
                     last = v;
                 }
 
                 last = (byte) (jkl * player);
                 for (int j = n.length - 1; j >= 0; j--) {
                     int ind = n[j];
                     byte v = down[ind];
                     hcd ^= Board.hashArray[ind][v + 2]
                             ^ Board.hashArray[ind][last + 2];
                     down[ind] = last;
                     if (v == 0) {
                         break;
                     }
                     last = v;
                 }
 
                 bgk[mm] = new GameState(new Board(up, hcu), decced[jkl], alpha,
                         beta);
                 mm++;
                 bgk[mm] = new GameState(new Board(down, hcd), decced[jkl],
                         alpha, beta);
                 mm++;
             }
         }
 
         GameState[] foo = new GameState[mm];
         System.arraycopy(bgk, 0, foo, 0, mm);
         return foo;
     }
 
     // these limit must be increased once
     // we start branching on the taking of pieces.
     private static final GameState[] mpm1 = new GameState[3];
     private static final GameState[] mpm2 = new GameState[120];
     private static final GameState[] mpm3 = new GameState[360];
 
     /**
      * 
      * TODO: deprecate in favor of the lazier, iterator version...
      * 
      * @param g
      * @param player
      * @return
      */
     public static GameState[] listPossibleBoards(GameState g, int player) {
 
         int i = 0, j = 0, k = 0;
 
         for (GameState q : getLineTakingResults(g, player)) {
             mpm1[i] = q;
             i++;
         }
 
         for (int x = 0; x < i; x++) {
             if (mpm1[x].losingGameState(player)) {
                 continue;
             }
 
             for (GameState q : getMoveMakingResults(mpm1[x], player)) {
                 mpm2[j] = q;
                 j++;
             }
         }
 
         // java style map. I want a macro for this.
         for (int y = 0; y < j; y++) {
             for (GameState q : getLineTakingResults(mpm2[y], player)) {
                 mpm3[k] = q;
                 k++;
             }
         }
 
         GameState[] foo = new GameState[k];
         System.arraycopy(mpm3, 0, foo, 0, k);
 
         return foo;
     }
 
 }
