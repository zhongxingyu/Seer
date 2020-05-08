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
 
     private static int[] gfbl_buf = new int[21];
 
     private static int[] getFilteredBoardLines(Board b, int player) {
         int m = 0;
         for (int num = 0; num < 21; num++) {
             int[] line = listOfLinePoints[num];
             int sign = 0;
             int count = 1;
             int len = line.length;
             for (int kk = 0; kk < len; kk++) {
                 int v = b.data[line[kk]];
                 if (v == 0 || (v * sign <= 0)) {
                     count = 1;
                     sign = v;
                 } else {
                     count++;
                 }
 
                 if (count == 4 && (sign * player) > 0) {
                     gfbl_buf[m] = num;
                     m++;
                     break;
                 }
             }
         }
 
         int[] foo = new int[m];
         System.arraycopy(gfbl_buf, 0, foo, 0, m);
         return foo;
     }
 
     private static int[] tried = new int[21];
 
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
 
         int[] found;
         while (true) {
             found = getFilteredBoardLines(g.b, player);
             boolean k = false;
             for (int f : found) {
                 boolean c = false;
                 for (int i = 0; i < t; i++) {
                     if (tried[i] == f) {
                         c = true;
                         break;
                     }
                 }
 
                 if (c) {
                     continue;
                 }
 
                 curr = lineEmpty(curr, f, player);
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
 
        System.out.format("%s %b %b %d\n", foo.r.toString(), foo.gphase1,
                foo.gphase2, pieceval);
         if (pieceval > 0) {
             if (foo.gphase1 && pieceval == 2) {
                 return foo.change(new Board(cdata, hc),
                         foo.r.applyDelta((int) pieceval, -2, 0, 1), true,
                         foo.gphase2);
             } else {
                 return foo.change(new Board(cdata, hc),
                         foo.r.applyDelta((int) pieceval, -1, 1, 0), false,
                         foo.gphase2);
             }
         } else {
             if (foo.gphase2 && pieceval == -2) {
                 return foo.change(new Board(cdata, hc),
                         foo.r.applyDelta((int) pieceval, -2, 0, 1),
                         foo.gphase1, true);
             } else {
                 return foo.change(new Board(cdata, hc),
                         foo.r.applyDelta((int) pieceval, -1, 1, 0),
                         foo.gphase1, false);
             }
         }
     }
 
     private static GameState lineEmpty(GameState curr, int found, int player) {
         byte[] cdata = new byte[Board.SIZE];
         System.arraycopy(curr.b.data, 0, cdata, 0, Board.SIZE);
         int hc = curr.b.hashCode;
         int[] res = curr.r.toArray();
 
         for (int q : listOfLinePoints[found]) {
             int v = player * cdata[q];
             // System.out.format("v: %d p:%d d:%d d:%d q:%d %s\n", v, player,
             // cdata[q], d, q, rr.toString());
 
             // / System.out.format("%d %d, %d %d, %d %d\n", rr.p1, rr.p2, rr.o1,
             // rr.o2, rr.g1, rr.g2);
 
             if (v < 0) {
                 cdata[q] = 0;
                 hc ^= Board.hashArray[q][2] ^ Board.hashArray[q][v + 2];
                 if (v == -2) {
                     Reserves.mutateArray(res, -player, 0, 0, -1);
                 } else {
                     Reserves.mutateArray(res, -player, 0, -1, 0);
                 }
             } else if (v == 1) {
                 hc ^= Board.hashArray[q][2] ^ Board.hashArray[q][3];
                 cdata[q] = 0;
                 Reserves.mutateArray(res, player, 1, -1, 0);
             }
         }
 
         // .change preserves superclass metadata
         return curr.change(new Board(cdata, hc), new Reserves(res),
                 curr.gphase1, curr.gphase2);
     }
 }
