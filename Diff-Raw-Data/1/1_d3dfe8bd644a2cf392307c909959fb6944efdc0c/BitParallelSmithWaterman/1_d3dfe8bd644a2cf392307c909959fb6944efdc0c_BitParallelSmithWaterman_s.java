 /*--------------------------------------------------------------------------
  *  Copyright 2011 utgenome.org
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  *--------------------------------------------------------------------------*/
 //--------------------------------------
 // genome-weaver Project
 //
 // BitParallelSmithWaterman.java
 // Since: 2011/07/28
 //
 // $URL$ 
 // $Author$
 //--------------------------------------
 package org.utgenome.weaver.align;
 
 import java.util.Arrays;
 
 import org.utgenome.weaver.align.SmithWatermanAligner.Alignment;
 import org.utgenome.weaver.align.SmithWatermanAligner.Trace;
 import org.utgenome.weaver.align.record.SWResult;
 import org.xerial.util.log.Logger;
 
 /**
  * Bit-parallel algorithm for Smith-Waterman alignment
  * 
  * <h3>References</h3>
  * 
  * <ul>
  * <li>Myers, G. (1999). A fast bit-vector algorithm for approximate string
  * matching based on dynamic programming. Journal of the ACM (JACM).</li>
  * <li>H. Hyyr&ouml; and G. Navarro. Faster Bit-parallel Approximate String
  * Matching In Proc. 13th Combinatorial Pattern Matching (CPM'2002), LNCS 2373
  * http://sunsite.dcc.uchile.cl/ftp/users/gnavarro/hn02.ps.gz</li>
  * </ul>
  * 
  * @author leo
  * 
  */
 public class BitParallelSmithWaterman
 {
     private static Logger _logger = Logger.getLogger(BitParallelSmithWaterman.class);
 
     public static void align64(ACGTSequence ref, ACGTSequence query, int numAllowedDiff) {
         new Align64(ref, query).globalMatch(numAllowedDiff);
     }
 
     public static void localAlign64(ACGTSequence ref, ACGTSequence query, int numAllowedDiff) {
         new Align64(ref, query).localMatch(numAllowedDiff);
     }
 
     public static class Align64
     {
         private final ACGTSequence ref;
         private final ACGTSequence query;
         private final long[]       pm;
         private final int          m;
         private final int          n;
 
         public Align64(ACGTSequence ref, ACGTSequence query) {
             this.ref = ref;
             this.query = query;
             this.m = (int) query.textSize();
             this.n = (int) ref.textSize();
             // Preprocessing
             pm = new long[ACGT.values().length];
             int m = Math.min(64, (int) query.textSize());
             for (int i = 0; i < m; ++i) {
                 pm[query.getACGT(i).code] |= 1L << i;
             }
         }
 
         public void globalMatch(int k) {
             align(m, ~0L, 0L, k);
         }
 
         public void localMatch(int k) {
             align(0, 0L, 0L, k);
         }
 
         protected void align(int score, long vp, long vn, int k) {
 
             //            if (_logger.isDebugEnabled()) {
             //                for (ACGT ch : ACGT.exceptN) {
             //                    _logger.debug("peq[%s]:%s", ch, toBinary(pm[ch.code], m));
             //                }
             //            }
 
             for (int j = 0; j < n; ++j) {
                 long x = pm[ref.getACGT(j).code];
                 long d0 = ((vp + (x & vp)) ^ vp) | x | vn;
                 long hp = (vn | ~(vp | d0));
                 long hn = vp & d0;
                 x = (hp << 1);
                 vn = x & d0;
                 vp = (hn << 1) | ~(x | d0);
                 // diff represents the last row (C[m, j]) of the DP matrix
                 score += (int) ((hp >>> (m - 1)) & 1L);
                 score -= (int) ((hn >>> (m - 1)) & 1L);
                 //                if (_logger.isDebugEnabled()) {
                 //                    _logger.debug("[%s] j:%2d, score:%2d %1s hp:%s, hn:%s, vp:%s, vn:%s, d0:%s", ref.getACGT(j), j,
                 //                            score, score <= k ? "*" : "", toBinary(hp, m), toBinary(hn, m), toBinary(vp, m),
                 //                            toBinary(vn, m), toBinary(d0, m));
                 //                }
             }
         }
     }
 
     public static String toBinary(long v, int m) {
         StringBuilder s = new StringBuilder();
         for (int i = m - 1; i >= 0; --i) {
             s.append((v & (1L << i)) > 0 ? "1" : "0");
         }
         return s.toString();
     }
 
     public static SWResult alignBlock(String ref, String query, int k) {
         return alignBlock(new ACGTSequence(ref), new ACGTSequence(query), k);
     }
 
     public static SWResult alignBlock(ACGTSequence ref, ACGTSequence query, int k) {
         AlignBlocks a = new AlignBlocks((int) query.textSize(), k);
         SWResult sw = a.align(ref, query);
         return sw;
     }
 
     public static Alignment alignBlockDetailed(String ref, String query, int k) {
         return alignBlockDetailed(new ACGTSequence(ref), new ACGTSequence(query), k);
     }
 
     public static Alignment alignBlockDetailed(ACGTSequence ref, ACGTSequence query, int k) {
         AlignBlocksDetailed a = new AlignBlocksDetailed((int) query.textSize(), k);
         SWResult bestHit = a.align(ref, query);
         return a.traceback(ref, query, bestHit);
     }
 
     public static void alignBlockDetailedNoTraceBack(ACGTSequence ref, ACGTSequence query, int k) {
         AlignBlocksDetailed a = new AlignBlocksDetailed((int) query.textSize(), k);
         SWResult bestHit = a.align(ref, query);
         //return a.traceback(ref, query, bestHit);
     }
 
     static SWResult alignBlock(ACGTSequence ref, ACGTSequence query, int k, int w) {
         AlignBlocks a = new AlignBlocks(w, (int) query.textSize(), k);
         return a.align(ref, query);
     }
 
     /**
      * Block-based aligment for more than 64-bit queries
      * 
      * @author leo
      * 
      */
     public static class AlignBlocks
     {
         private static final int Z = ACGT.values().length; // alphabet size
 
         private final int        w;                       // word size
         private final int        k;
         private final int        m;
         private final int        bMax;
         private long[]           vp;
         private long[]           vn;
 
         private long[][]         peq;                     // [A, C, G, T][# of block]
         private int[]            D;                       // D[block]
 
         public AlignBlocks(int m, int k) {
             this(64, m, k);
         }
 
         public AlignBlocks(int w, int m, int k) {
             this.w = w;
             this.m = m;
             this.k = k;
             bMax = Math.max(1, (m + w - 1) / w);
             vp = new long[bMax];
             vn = new long[bMax];
             peq = new long[Z][bMax];
             D = new int[bMax];
         }
 
         public void clear() {
             Arrays.fill(vp, 0L);
             Arrays.fill(vn, 0L);
             for (int i = 0; i < Z; ++i)
                 for (int j = 0; j < bMax; ++j)
                     peq[i][j] = 0L;
         }
 
         public SWResult align(ACGTSequence ref, ACGTSequence query) {
             QueryMask qm = new QueryMask(query);
             return align(ref, qm);
         }
 
         public SWResult align(ACGTSequence ref, QueryMask qMask) {
             // Peq bit-vector holds flags of the character occurrence positions in the query
             for (ACGT ch : ACGT.exceptN) {
                 for (int i = 0; i < bMax; ++i) {
                     peq[ch.code][i] = qMask.getPatternMaskIn64bit(ch, i, w);
                 }
             }
 
             // Fill the flanking region with 1s
             {
                 int f = m % w;
                 long mask = f == 0 ? 0L : (~0L << f);
                 for (int i = 0; i < Z; ++i) {
                     //peq[i][bMax - 1] |= mask;
                 }
             }
             //            if (_logger.isTraceEnabled()) {
             //                _logger.trace("peq:%s", qMask);
             //            }
             return align(ref);
         }
 
         private SWResult align(ACGTSequence ref) {
 
             SWResult bestHit = null;
 
             final int N = (int) ref.textSize();
             final int W = w - (int) ref.textSize() % w;
 
             // Initialize the vertical input
             for (int r = 0; r < bMax; ++r) {
                 vp[r] = ~0L; // all 1s
                 vn[r] = 0L; // all 0s
             }
             // Init the score
             D[0] = w;
 
             int b = Math.max(1, (k + w - 1) / w);
             for (int j = 0; j < N; ++j) {
                 ACGT ch = ref.getACGT(j);
                 int carry = 0;
                 for (int r = 0; r < b; ++r) {
                     int nextScore = alignBlock(j, ch, r, carry);
                     D[r] += nextScore;
                     //                    if (_logger.isTraceEnabled()) {
                     //                        _logger.trace("j:%d[%s], hin:%2d, hout:%2d, D%d:%d", j, ref.getACGT(j), carry, nextScore, r,
                     //                                D[r]);
                     //                    }
                     carry = nextScore;
                 }
 
                 if (b < bMax && D[b - 1] - carry <= k && (((peq[ch.code][b] & 1L) != 0L) | carry < 0)) {
                     b++;
                     int nextScore = alignBlock(j, ch, b - 1, carry);
                     D[b - 1] = D[b - 2] + w - carry + nextScore;
                     //                    if (_logger.isTraceEnabled()) {
                     //                        _logger.trace("j:%d[%s], hin:%2d, hout:%2d, D%d:%d", j, ref.getACGT(j), carry, nextScore,
                     //                                b - 1, D[b - 1]);
                     //                    }
                 }
                 else {
                     while (b > 1 && D[b - 1] >= k + w) {
                         --b;
                     }
                 }
 
                 if (b == bMax) {
                     //                    if (D[b - 1] <= W + k) {
                     //                        if (_logger.isTraceEnabled())
                     //                            _logger.trace("match at %d", j);
                     //                    }
 
                     if (bestHit == null) {
                         bestHit = new SWResult(j, D[b - 1] - W);
                         continue;
                     }
 
                     if (bestHit.diff > D[b - 1] - W) {
                         bestHit = new SWResult(j, D[b - 1] - W);
                     }
                 }
             }
             return bestHit;
         }
 
         private int alignBlock(int j, ACGT ch, int r, int hin) {
             long vp = this.vp[r];
             long vn = this.vn[r];
             long x = this.peq[ch.code][r];
             if (hin < 0)
                 x |= 1L;
             long d0 = ((vp + (x & vp)) ^ vp) | x | vn;
             long hn = vp & d0;
             long hp = (vn | ~(vp | d0));
             int hout = 0;
 
             hout += (int) ((hp >>> (w - 1)) & 1L);
             hout -= (int) ((hn >>> (w - 1)) & 1L);
 
             long hp2 = (hp << 1);
             long hn2 = (hn << 1);
             if (hin < 0)
                 hn2 |= 1L;
             if (hin > 0)
                 hp2 |= 1L;
 
             this.vp[r] = hn2 | ~(hp2 | d0);
             this.vn[r] = hp2 & d0;
 
             //            if (_logger.isTraceEnabled()) {
             //                _logger.trace("[%s] j:%2d, block:%d, hin:%2d, hout:%2d, hp:%s, hn:%s, vp:%s, vn:%s, d0:%s", ch, j, r,
             //                        hin, hout, toBinary(hp, w), toBinary(hn, w), toBinary(this.vp[r], w), toBinary(this.vn[r], w),
             //                        toBinary(d0, w));
             //            }
 
             return hout;
         }
 
     }
 
     /**
      * Extension of the AlignBlock algorithm to calculate the actual alignment
      * (CIGAR)
      * 
      * @author leo
      * 
      */
     public static class AlignBlocksDetailed
     {
         private static final int Z = ACGT.values().length; // alphabet size
 
         private final int        w;                       // word size
         private final int        k;
         private final int        m;
         private final int        bMax;
 
         private long[][]         peq;                     // [A, C, G, T][# of block]
         private int[]            D;                       // D[block]
 
         private long[][]         vp;
         private long[][]         vn;
 
         public AlignBlocksDetailed(int m, int k) {
             this(64, m, k);
         }
 
         public AlignBlocksDetailed(int w, int m, int k) {
             this.w = w;
             this.m = m;
             this.k = k;
             bMax = Math.max(1, (m + w - 1) / w);
             peq = new long[Z][bMax];
             D = new int[bMax];
         }
 
         public SWResult align(ACGTSequence ref, ACGTSequence query) {
             QueryMask qm = new QueryMask(query);
             return align(ref, qm);
         }
 
         public SWResult align(ACGTSequence ref, QueryMask qMask) {
             // Peq bit-vector holds flags of the character occurrence positions in the query
             for (ACGT ch : ACGT.exceptN) {
                 for (int i = 0; i < bMax; ++i) {
                     peq[ch.code][i] = qMask.getPatternMaskIn64bit(ch, i, w);
                 }
             }
 
             return align(ref);
         }
 
         private SWResult align(ACGTSequence ref) {
 
             SWResult bestHit = null;
 
             final int N = (int) ref.textSize();
             final int W = w - (int) ref.textSize() % w;
 
             // Prepare the score matrix
             vp = new long[bMax][N + 1];
             vn = new long[bMax][N + 1];
 
             // Initialize the vertical input
             for (int r = 0; r < bMax; ++r) {
                 vp[r][0] = ~0L; // all 1s
                 vn[r][0] = 0L; // all 0s
             }
             // Init the score
             D[0] = w;
 
             int b = Math.max(1, (k + w - 1) / w);
             for (int j = 0; j < N; ++j) {
                 ACGT ch = ref.getACGT(j);
                 int carry = 0;
                 for (int r = 0; r < b; ++r) {
                     int nextScore = alignBlock(j, ch, r, carry);
                     D[r] += nextScore;
                     carry = nextScore;
                 }
 
                 if (b < bMax && D[b - 1] - carry <= k && (((peq[ch.code][b] & 1L) != 0L) | carry < 0)) {
                     b++;
                     int nextScore = alignBlock(j, ch, b - 1, carry);
                     D[b - 1] = D[b - 2] + w - carry + nextScore;
                 }
                 else {
                     while (b > 1 && D[b - 1] >= k + w) {
                         --b;
                     }
                 }
 
                 if (b == bMax) {
                     if (bestHit == null) {
                         bestHit = new SWResult(j, D[b - 1] - W);
                         continue;
                     }
 
                     if (bestHit.diff > D[b - 1] - W) {
                         bestHit = new SWResult(j, D[b - 1] - W);
                     }
                 }
             }
             return bestHit;
         }
 
         private int alignBlock(int j, ACGT ch, int r, int hin) {
             long vp = this.vp[r][j];
             long vn = this.vn[r][j];
             long x = this.peq[ch.code][r];
             if (hin < 0)
                 x |= 1L;
             long d0 = ((vp + (x & vp)) ^ vp) | x | vn;
             long hn = vp & d0;
             long hp = (vn | ~(vp | d0));
             int hout = 0;
 
             hout += (int) ((hp >>> (w - 1)) & 1L);
             hout -= (int) ((hn >>> (w - 1)) & 1L);
 
             long hp2 = (hp << 1);
             long hn2 = (hn << 1);
             if (hin < 0)
                 hn2 |= 1L;
             if (hin > 0)
                 hp2 |= 1L;
 
             this.vp[r][j + 1] = hn2 | ~(hp2 | d0);
             this.vn[r][j + 1] = hp2 & d0;
 
             return hout;
         }
 
         public Alignment traceback(ACGTSequence ref, ACGTSequence query, SWResult bestHit) {
             if (bestHit == null)
                 return null;
 
             final int N = (int) ref.textSize();
             int maxRow = m - 1;
             int maxCol = bestHit.tailPos;
 
             StringBuilder cigar = new StringBuilder();
             StringBuilder a1 = new StringBuilder();
             StringBuilder a2 = new StringBuilder();
 
             int leftMostPos = 0; // in reference seq 
 
             // Append soft-clipped part in the query sequence
             for (int i = m - 1; i > maxRow; --i) {
                 cigar.append("S");
             }
 
             int row = m - 1;
             int col = N - 1;
 
             // Append clipped sequences
             while (col > maxCol) {
                 a1.append(ref.charAt(col - 1));
                 col--;
             }
             while (row > maxRow) {
                 a2.append(query.getACGT(row - 1).toChar());
                 row--;
             }
 
             // Trace back 
             int diff = 0;
             traceback: for (col = maxCol, row = maxRow;;) {
                 Trace path = Trace.NONE;
                 // Calculate path
                 if (col >= 0 && row >= 0) {
                     int block = row / w;
                     int offset = row % w;
 
                     long vpf = vp[block][col] & (1L << offset);
                     long vnf = vn[block][col] & (1L << offset);
 
                     if (ref.getACGT(col) == query.getACGT(row)) {
                         path = Trace.DIAGONAL;
                     }
                     else if (vpf != 0L) {
                         path = Trace.UP;
                         diff++;
                     }
                     else if (vnf == 0L) {
                         path = Trace.DIAGONAL;
                         diff++;
                     }
                     else {
                         path = Trace.LEFT;
                         diff++;
                     }
                 }
 
                 switch (path) {
                 case DIAGONAL:
                     // match
                     cigar.append("M");
                     a1.append(ref.charAt(col));
                     a2.append(query.charAt(row));
                     leftMostPos = col;
                     col--;
                     row--;
                     break;
                 case UP:
                     // insertion
                     cigar.append("I");
                     a1.append("-");
                     a2.append(query.charAt(row));
                     leftMostPos = col + 1;
                     row--;
                     break;
                 case LEFT:
                     cigar.append("D");
                     a1.append(ref.charAt(col));
                     a2.append("-");
                     col--;
                     break;
                 case NONE:
                     while (col >= 0 || row >= 0) {
                         if (row >= 0) {
                             cigar.append("S");
                             a1.append(col >= 0 ? ref.charAt(col) : ' ');
                             a2.append(Character.toLowerCase(query.charAt(row)));
                         }
                         else {
                             a1.append(col >= 0 ? ref.charAt(col) : ' ');
                             a2.append(' ');
                         }
                         col--;
                         row--;
                     }
 
                     break traceback; // exit the loop
                 }
             }
 
             String cigarStr = cigar.reverse().toString();
             {
                 // Remove indels at both of the read ends
                 int left = 0, right = 0;
                 for (int i = 0; i < cigarStr.length(); ++i) {
                     char t = cigarStr.charAt(i);
                     if (t == 'S' || t == 'I' || t == 'D') {
                         left++;
                     }
                     else
                         break;
                 }
 
                 for (int i = cigarStr.length() - 1; i >= left; --i) {
                     char t = cigarStr.charAt(i);
                     if (t == 'S' || t == 'I' || t == 'D') {
                         right++;
                     }
                     else
                         break;
                 }
 
                 StringBuilder newCigar = new StringBuilder();
                 for (int i = 0; i < left; ++i)
                     newCigar.append('S');
                 newCigar.append(cigarStr.substring(left, cigarStr.length() - right));
                 for (int i = 0; i < right; ++i)
                     newCigar.append('S');
                 cigarStr = newCigar.toString();
             }
 
             // create cigar string
             char prev = cigarStr.charAt(0);
             int count = 1;
             StringBuilder compactCigar = new StringBuilder();
             for (int i = 1; i < cigarStr.length(); ++i) {
                 char c = cigarStr.charAt(i);
                 if (prev == c) {
                     count++;
                 }
                 else {
                     compactCigar.append(Integer.toString(count));
                     compactCigar.append(prev);
 
                     prev = c;
                     count = 1;
                 }
             }
             if (count > 0) {
                 compactCigar.append(Integer.toString(count));
                 compactCigar.append(prev);
             }
 
             return new Alignment(compactCigar.toString(), m - diff, a1.reverse().toString(), leftMostPos, a2.reverse()
                     .toString());
         }
 
     }
 
 }
