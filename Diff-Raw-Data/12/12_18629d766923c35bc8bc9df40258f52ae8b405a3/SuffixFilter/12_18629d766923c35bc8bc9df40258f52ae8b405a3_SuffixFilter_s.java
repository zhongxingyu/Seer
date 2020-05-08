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
 // SuffixFilter.java
 // Since: 2011/07/27
 //
 // $URL$ 
 // $Author$
 //--------------------------------------
 package org.utgenome.weaver.align.strategy;
 
 import java.util.Comparator;
 import java.util.PriorityQueue;
 
 import org.utgenome.weaver.align.ACGT;
 import org.utgenome.weaver.align.ACGTSequence;
 import org.utgenome.weaver.align.AlignmentScoreConfig;
 import org.utgenome.weaver.align.BidirectionalSuffixInterval;
 import org.utgenome.weaver.align.BitVector;
 import org.utgenome.weaver.align.FMIndexOnGenome;
 import org.utgenome.weaver.align.Strand;
 import org.utgenome.weaver.align.SuffixInterval;
 import org.utgenome.weaver.align.strategy.BidirectionalBWT.QuickScanResult;
 import org.utgenome.weaver.parallel.Reporter;
 import org.xerial.lens.SilkLens;
 import org.xerial.util.StopWatch;
 import org.xerial.util.log.Logger;
 
 /**
  * Suffix filter
  * 
  * <pre>
  *   *---*---*---*
  *   | \ | \ | \ |
  *   *---*---*---*
  *   | \ | \ | \ |
  *   *---*---*---*
  * 
  * </pre>
  * 
  * @author leo
  * 
  */
 public class SuffixFilter
 {
     private static Logger              _logger = Logger.getLogger(SuffixFilter.class);
 
     private final FMIndexOnGenome      fmIndex;
     private final AlignmentScoreConfig config;
     private final int                  k;
     private final int                  m;
 
     private StaircaseFilter            staircaseFilter;
 
     public static enum Diff {
         Match, Mismatch, Insertion, Deletion, Split
     };
 
     /**
      * FM-index cursor for bidirectional search
      * 
      * @author leo
      * 
      */
     public class Cursor
     {
         // flag(8bit) :=  strand(1), searchDirection(2)
         private final byte          flag;
         //private Score      score;
         public final SuffixInterval siF;
         public final SuffixInterval siB;
         public final int            cursorF;
         public final int            cursorB;
         public final Cursor         split;
 
         private Cursor(byte flag, SuffixInterval siF, SuffixInterval siB, int cursorF, int cursorB, Cursor split) {
             this.flag = flag;
             this.siF = siF;
             this.siB = siB;
             this.cursorF = cursorF;
             this.cursorB = cursorB;
             this.split = split;
         }
 
         public Cursor(Strand strand, SearchDirection searchDirection, SuffixInterval siF, SuffixInterval siB,
                 int cursorF, int cursorB, Cursor split) {
             this((byte) (strand.index | (searchDirection.index << 1)), siF, siB, cursorF, cursorB, split);
         }
 
         @Override
         public String toString() {
             StringBuilder s = new StringBuilder();
             s.append(String.format("%s%s:%d/%d:%s", getStrand().symbol, getSearchDirection().symbol, cursorF, cursorB,
                     siF));
             if (siB != null)
                 s.append(String.format(" %s", siB));
             if (split != null)
                 s.append(String.format(" split(%s)", split));
             return s.toString();
         }
 
         public int getProcessedBases() {
             int p = cursorF - cursorB;
             if (split == null)
                 return p;
             else
                 return p + (split.cursorF - split.cursorB);
         }
 
         public int getRemainingBases() {
             int r = (m - cursorF) + cursorB;
             if (split == null)
                 return r;
             else
                 return r - (split.cursorF - split.cursorB);
         }
 
         public Strand getStrand() {
             return Strand.decode(flag & 1);
         }
 
         public ACGT nextACGT(ACGTSequence[] q) {
             int strand = flag & 1;
             return q[strand].getACGT(getNextACGTIndex());
         }
 
         public int getNextACGTIndex() {
             return getSearchDirection().isForward && cursorF < m ? cursorF : cursorB - 1;
         }
 
         public int getIndex() {
             if (split == null)
                 return cursorF - cursorB;
             else
                 return cursorF - cursorB + (split.cursorF - split.cursorB);
         }
 
         public SearchDirection getSearchDirection() {
             return SearchDirection.decode((flag >>> 1) & 0x03);
         }
 
         public ExtensionType getExtentionType() {
             return ExtensionType.decode((flag >>> 3) & 0x03);
         }
 
         Cursor split() {
             int cursor = cursorF;
             if (getSearchDirection() == SearchDirection.Backward)
                 cursor = cursorB;
 
             return new Cursor(flag, fmIndex.wholeSARange(), fmIndex.wholeSARange(), cursor, cursor, this);
         }
 
         public boolean hasSplit() {
             return split != null;
         }
 
         public Cursor next(ACGT ch) {
             Strand strand = getStrand();
             SuffixInterval nextSiF = siF;
             SuffixInterval nextSiB = siB;
             int nextF = cursorF;
             int nextB = cursorB;
             SearchDirection d = getSearchDirection();
             switch (d) {
             case Forward:
                 nextSiF = fmIndex.forwardSearch(strand, ch, siF);
                 if (nextSiF.isEmpty())
                     return null;
                 ++nextF;
                 break;
             case Backward:
                 nextSiB = fmIndex.backwardSearch(strand, ch, siB);
                 if (nextSiB.isEmpty())
                     return null;
                 --nextB;
                 break;
             case BidirectionalForward:
                 if (nextF < m) {
                     ++nextF;
                     BidirectionalSuffixInterval bSi = fmIndex.bidirectionalForwardSearch(strand, ch,
                             new BidirectionalSuffixInterval(siF, siB));
                     if (bSi == null)
                         return null;
                     nextSiF = bSi.forwardSi;
                     nextSiB = bSi.backwardSi;
                 }
                 else {
                     // switch to backward search
                     d = SearchDirection.Backward;
                     --nextB;
                     nextSiB = fmIndex.backwardSearch(strand, ch, siB);
                     if (nextSiB.isEmpty())
                         return null;
                 }
                 break;
             }
 
             return new Cursor(strand, d, nextSiF, nextSiB, nextF, nextB, split);
         }
     }
 
     /**
      * NFA state and alignment cursor holder.
      * 
      * This NFA holds 2k+1 columns and k rows
      * 
      * @author leo
      * 
      */
     public class SearchState
     {
         public final Cursor  cursor;
         // bit flags holding states at column [index - k, index + k + 1], where k is # of allowed mismatches 
         private final long[] automaton;
         // 32 bit = searchFlag (5) + hasHit(1) + isInit(0:true, 1:false)  + minK (8) 
         private int          state;
 
         SearchState(Cursor cursor, long[] automaton, boolean isInit, int minK, boolean hasHit) {
             this.cursor = cursor;
             this.automaton = automaton;
             this.state = ((hasHit ? 1 : 0) << 5) | ((isInit ? 0 : 1) << 6) | (minK << 7);
         }
 
         /**
          * Initial forward search state
          * 
          * @param strand
          * @param searchDirection
          */
         public SearchState(Cursor cursor) {
             this(cursor, new long[k + 1], true, 0, false);
             // Activate the diagonal states 
             for (int i = 0; i <= k; ++i) {
                 automaton[i] = 1L << (k + i);
             }
         }
 
         String showNFAState(long[] automaton) {
             int w = (k - getLowerBoundOfK()) * 2 + 1;
             StringBuilder s = new StringBuilder();
             for (int j = 0; j < automaton.length; ++j) {
                 s.append(toBinary(automaton[j], w));
                 if (j != automaton.length - 1) {
                     s.append(" \n");
                 }
             }
             return s.toString();
         }
 
         String toBinary(long val, int w) {
             StringBuilder s = new StringBuilder();
             for (int i = 0; i < w; ++i) {
                 s.append(((val >>> i) & 1L) == 0 ? "0" : "1");
             }
             return s.toString();
         }
 
         @Override
         public String toString() {
             //          return String.format("%sk%d%s \n%s", hasHit() ? "*" : "", getLowerBoundOfK(), cursor,   showNFAState(automaton));
             return String.format("%sk%d%s", hasHit() ? "*" : "", getLowerBoundOfK(), cursor);
         }
 
         public int score() {
             int nm = getLowerBoundOfK();
             int mm = getIndex() - nm;
             return mm * config.matchScore - nm * config.mismatchPenalty;
         }
 
         public int getStrandIndex() {
             return cursor.flag & 1;
         }
 
         public int getIndex() {
             return cursor.getIndex();
         }
 
         public int getLowerBoundOfK() {
             return (state >>> 7) & 0xFF;
         }
 
         public boolean hasHit() {
             return ((state >>> 5) & 0x01) == 1L;
         }
 
         public boolean isFinished() {
             return (state & 0x1F) == 0x1F; // ACGT + split
         }
 
         public void updateFlag(ACGT ch) {
             this.state |= 1 << ch.code;
         }
 
         public void fillSearchFlags() {
             this.state |= 0x1F;
         }
 
         public void updateSplitFlag() {
             this.state |= 1 << 4;
         }
 
         public boolean isInitialState() {
             return ((state >>> 6) & 1) == 1L;
         }
 
         public int getInitFlag() {
             return (this.state >>> 6) & 1;
         }
 
         public void setInitFlag() {
             this.state |= 1 << 6;
         }
 
         public boolean isChecked(ACGT ch) {
             return (state & (1 << ch.code)) != 0;
         }
 
         public SearchState nextStateAfterSplit() {
             updateSplitFlag();
             // use the same automaton state
             if (getLowerBoundOfK() < k) {
                 int minK = getLowerBoundOfK() + 1;
                 int height = k - minK + 1;
                 long[] nextAutomaton = new long[height];
                 for (int i = 0; i < height; ++i) {
                     nextAutomaton[i] = 1L << (height + i - 1);
                 }
                 return new SearchState(cursor.split(), nextAutomaton, false, minK, hasHit());
             }
             else
                 return null;
         }
 
         public SearchState nextState(ACGT ch, Cursor nextCursor, QueryMask queryMask) {
 
             final int minK = getLowerBoundOfK();
             final int height = k - minK + 1;
 
             long[] prev = automaton;
             long[] next = new long[height];
 
             final int index = getIndex();
             final int colStart = index - height + 1;
             final long qeq = queryMask.getPatternMaskIn64bit(ch, cursor.getNextACGTIndex() - height + 1);
             // Update the automaton
             // R'_0 = ((R_0 & P[ch]) << 1) & (suffix filter)
             next[0] = ((prev[0] & qeq) << 1) & staircaseFilter.getStairCaseMask64bit(minK, colStart);
             for (int i = 1; i < height; ++i) {
                 // R'_{i+1} = ((R_{i+1} & P[ch]) << 1) | R_i | (R_i << 1) | (R'_i << 1)   
                 next[i] = ((prev[i] & qeq) << 1) | prev[i - 1] | (prev[i - 1] << 1) | (next[i - 1] << 1);
                 // Apply a suffix filter (staircase mask)
                 next[i] &= staircaseFilter.getStairCaseMask64bit(minK + i, colStart);
             }
 
             // Find a match at query position m 
             boolean foundMatch = false;
             final int mPos = height + m - index - 1;
             if (mPos < 64) {
                 for (int i = 0; i < height; ++i) {
                     if ((next[i] & (1L << mPos)) != 0L) {
                         foundMatch = true;
                         break;
                     }
                 }
             }
             // Find a match at next step 
             final int nextIndex = height;
             for (int i = 0; i < height; ++i) {
                 if ((next[i] & (1L << nextIndex)) != 0L) {
                     return createNextState(nextCursor, next, i, foundMatch);
                 }
             }
 
             // no match
             return null;
         }
 
         private SearchState createNextState(Cursor nextCursor, long[] nextAutomaton, int nm, boolean foundMatch) {
             int index = getIndex();
             int nextHeight = automaton.length - nm;
             long[] trimmed = new long[nextHeight];
             for (int i = 0; i < nextHeight; ++i) {
                 trimmed[i] = nextAutomaton[i + nm] >>> 1;
             }
             //_logger.debug("\n" + showNFAState(trimmed));
             return new SearchState(nextCursor, trimmed, false, getLowerBoundOfK() + nm, foundMatch);
         }
 
     }
 
     /**
      * A set of bit flags of ACGT characters in a query sequence
      * 
      * @author leo
      * 
      */
     public static class QueryMask
     {
         private int         m;
         private BitVector[] patternMaskF;
         private BitVector[] patternMaskR; // reverse pattern
 
         public QueryMask(ACGTSequence query) {
             m = (int) query.textSize();
             patternMaskF = new BitVector[ACGT.exceptN.length];
             patternMaskR = new BitVector[ACGT.exceptN.length];
             for (int i = 0; i < patternMaskF.length; ++i) {
                 patternMaskF[i] = new BitVector(m);
                 patternMaskR[i] = new BitVector(m);
             }
 
             for (int i = 0; i < m; ++i) {
                 ACGT ch = query.getACGT(i);
                 if (ch == ACGT.N) {
                     for (ACGT each : ACGT.exceptN) {
                         patternMaskF[each.code].set(i);
                         patternMaskR[each.code].set(m - i - 1);
                     }
                 }
                 else {
                     patternMaskF[ch.code].set(i);
                     patternMaskR[ch.code].set(m - i - 1);
                 }
             }
         }
 
         public long getPatternMaskIn64bit(ACGT ch, int start) {
             if (start < 0) {
                 long p = patternMaskF[ch.code].substring64(0, 64);
                 return p << (-start);
             }
             long p = patternMaskF[ch.code].substring64(start, m);
             if (start + 64 >= m) {
                 // combine forward and reverse pattern mask
                 p |= (patternMaskR[ch.code].substring64(0, start + 64 - m)) << (m - start);
             }
             return p;
         }
     }
 
     /**
      * @param fmIndex
      * @param config
      * @param m
      *            read length
      */
     public SuffixFilter(FMIndexOnGenome fmIndex, AlignmentScoreConfig config, long m) {
         this.fmIndex = fmIndex;
         this.config = config;
         this.k = config.maximumEditDistances;
         this.m = (int) m;
         this.staircaseFilter = new StaircaseFilter(this.m, k);
     }
 
     public void align(ACGTSequence query) throws Exception {
         new AlignmentProcess(query, new Reporter() {
             @Override
             public void emit(Object result) throws Exception {
                 _logger.debug(SilkLens.toSilk("result", result));
 
             }
         }).align();
     }
 
     public void align(ACGTSequence query, Reporter out) throws Exception {
         new AlignmentProcess(query, out).align();
     }
 
     private static class StatePreference implements Comparator<SearchState>
     {
         @Override
         public int compare(SearchState o1, SearchState o2) {
 
             return o2.score() - o1.score();

            //            int diff = 0;
            //            // prefer a state with smaller mismatches
            //            diff = o1.getLowerBoundOfK() - o2.getLowerBoundOfK();
            //
            //            if (diff != 0)
            //                return diff;
            //
            //            // prefer longer match
            //            diff = o2.getIndex() - o1.getIndex();
            //
            //            return diff;
         }
     }
 
     public class AlignmentProcess
     {
 
         private ACGTSequence[]             q             = new ACGTSequence[2];
         private QueryMask[]                queryMask     = new QueryMask[2];
         private PriorityQueue<SearchState> queue         = new PriorityQueue<SearchState>(11, new StatePreference());
 
         private Reporter                   out;
 
         private int                        minMismatches = k + 1;
 
         public AlignmentProcess(ACGTSequence query, Reporter out) {
             this.q[0] = query;
             this.q[1] = query.complement();
             this.out = out;
         }
 
         public void align() throws Exception {
 
             StopWatch s = new StopWatch();
             align_internal();
 
             if (_logger.isDebugEnabled()) {
                 _logger.debug("stat: %s min K:%d, FM Search:%,d, Exact:%d, CutOff:%d, Filtered:%d, %.5f sec.",
                         minMismatches <= k ? "(*)" : "   ", minMismatches, numFMIndexSearches, numExactSearchCount,
                         numCutOff, numFiltered, s.getElapsedTime());
 
                 if (minMismatches == 0 && numFMIndexSearches > 500) {
                     _logger.debug("query: %s", q[0]);
                 }
             }
 
         }
 
         public void align_internal() throws Exception {
 
             if (q[0].fastCount(ACGT.N, 0, m) > config.maximumEditDistances) {
                 return; // skip this alignment
             }
 
             // k=0
             QuickScanResult scanF = BidirectionalBWT.scanMismatchLocations(fmIndex, q[0], Strand.FORWARD);
             if (scanF.numMismatches == 0) {
                 minMismatches = 0;
                 out.emit(scanF);
                 return;
             }
             QuickScanResult scanR = BidirectionalBWT.scanMismatchLocations(fmIndex, q[1], Strand.REVERSE);
             if (scanR.numMismatches == 0) {
                 minMismatches = 0;
                 out.emit(scanR);
                 return;
             }
 
             if (_logger.isTraceEnabled())
                 _logger.trace(SilkLens.toSilk("scanF", scanF));
             if (_logger.isTraceEnabled())
                 _logger.trace(SilkLens.toSilk("scanR", scanR));
 
             if (k == 0)
                 return;
 
             this.queryMask[0] = new QueryMask(q[0]);
             this.queryMask[1] = new QueryMask(q[1]);
 
             // Add states for both strands
             if (scanF.numMismatches <= k) {
                 queue.add(new SearchState(new Cursor(Strand.FORWARD, SearchDirection.Forward, fmIndex.wholeSARange(),
                         null, 0, 0, null)));
 
                 if (scanF.longestMatch.start != 0 && scanF.longestMatch.start < m) {
                     // add bidirectional search state
                     queue.add(new SearchState(new Cursor(Strand.FORWARD, SearchDirection.BidirectionalForward, fmIndex
                             .wholeSARange(), fmIndex.wholeSARange(), scanF.longestMatch.start,
                             scanF.longestMatch.start, null)));
                 }
             }
 
             if (scanR.numMismatches <= k) {
                 queue.add(new SearchState(new Cursor(Strand.REVERSE, SearchDirection.Forward, fmIndex.wholeSARange(),
                         null, 0, 0, null)));
                 if (scanF.numMismatches > scanR.numMismatches) {
                     if (scanR.longestMatch.start != 0 && scanR.longestMatch.start < m) {
                         // add bidirectional search state
                         queue.add(new SearchState(new Cursor(Strand.REVERSE, SearchDirection.BidirectionalForward,
                                 fmIndex.wholeSARange(), fmIndex.wholeSARange(), scanR.longestMatch.start,
                                 scanR.longestMatch.start, null)));
                     }
                 }
             }
 
             while (!queue.isEmpty()) {
                 SearchState c = queue.poll();
                 if (_logger.isTraceEnabled()) {
                     _logger.trace("cursor: %s", c);
                 }
                 if (c.isFinished()) {
                     continue;
                 }
 
                 if (c.hasHit()) {
                     // TODO verification
                     reportAlignment(c);
                     break;
                 }
 
                 if (c.cursor.getRemainingBases() == 0) {
                     reportAlignment(c);
                     break;
                 }
 
                 int nm = c.getLowerBoundOfK();
                 int allowedMismatches = k - nm;
                 if (allowedMismatches < 0)
                     continue;
 
                 if (allowedMismatches == 0) {
                     // do exact match
                     SearchState matchState = exactMatch(c);
                     if (matchState != null) {
                         reportAlignment(matchState);
                         break;
                     }
                     continue;
                 }
 
                 ACGT nextBase = c.cursor.nextACGT(q);
                 if (!c.isChecked(nextBase)) {
                     // search for a base in the read
                     c.setInitFlag();
                     boolean hasNextStep = step(c, nextBase);
                     if (!c.isFinished())
                         queue.add(c); // preserve the state for backtracking
 
                     continue;
                 }
 
                 if (nm + 1 > minMismatches) {
                     ++numCutOff;
                     continue;
                 }
 
                 if (!staircaseFilter.getStaircaseMask(nm + 1).get(c.getIndex())) {
                     numFiltered++;
                     continue;
                 }
 
                 // search for the other bases
                 for (ACGT ch : ACGT.exceptN) {
                     if (ch == nextBase)
                         continue;
 
                     if (!c.isChecked(ch)) {
                         step(c, ch);
                     }
                 }
 
                 // split
                 if (config.numSplitAlowed > 0) {
                     int index = c.getIndex();
                     c.updateSplitFlag();
                     if (!c.cursor.hasSplit() && index > config.indelEndSkip && m - index > config.indelEndSkip) {
                         SearchState nextState = c.nextStateAfterSplit();
                         if (nextState != null)
                             queue.add(nextState);
                         else
                             ++numFiltered;
                     }
                 }
 
                 assert (c.isFinished());
             }
 
         }
 
         private void reportAlignment(SearchState c) throws Exception {
 
             if (c.getLowerBoundOfK() < minMismatches) {
                 minMismatches = c.getLowerBoundOfK();
             }
 
             out.emit(c);
         }
 
         private int numFMIndexSearches  = 0;
         private int numCutOff           = 0;
         private int numFiltered         = 0;
         private int numExactSearchCount = 0;
 
         /**
          * @param c
          * @param ch
          * @return has match
          */
         private boolean step(SearchState c, ACGT ch) {
             c.updateFlag(ch);
 
             Cursor nextCursor = c.cursor.next(ch);
             ++numFMIndexSearches;
             if (nextCursor == null)
                 return false; // no match
 
             SearchState nextState = c.nextState(ch, nextCursor, queryMask[c.getStrandIndex()]);
             if (nextState != null) {
                 queue.add(nextState);
                 return true;
             }
             else {
                 ++numFiltered;
                 return false;
             }
         }
 
         private SearchState exactMatch(SearchState c) {
             ++numExactSearchCount;
 
             Cursor cursor = c.cursor;
             final int n = cursor.getRemainingBases();
             final int strandIndex = c.getStrandIndex();
             int numExtend = 0;
             while (numExtend < n) {
                 cursor = cursor.next(cursor.nextACGT(q));
                 ++numFMIndexSearches;
                 if (cursor == null)
                     return null;
                 numExtend++;
             }
             return new SearchState(cursor, null, false, c.getLowerBoundOfK(), true);
         }
 
     }
 
 }
