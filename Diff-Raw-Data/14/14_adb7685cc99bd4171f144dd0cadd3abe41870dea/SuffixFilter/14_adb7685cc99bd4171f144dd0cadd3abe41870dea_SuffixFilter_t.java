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
 import org.utgenome.weaver.parallel.Reporter;
 import org.xerial.lens.SilkLens;
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
             return getSearchDirection().isForward && cursorF < m ? q[strand].getACGT(cursorF) : q[strand]
                     .getACGT(cursorB - 1);
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
 
         public Cursor split() {
             int cursor = cursorF;
             if (getSearchDirection() == SearchDirection.Backward)
                 cursor = cursorB;
 
             return new Cursor(flag, fmIndex.wholeSARange(), fmIndex.wholeSARange(), cursor, cursor, this);
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
 
             return new Cursor(flag, nextSiF, nextSiB, nextF, nextB, split);
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
         // 32 bit = searchFlag (5) + hasHit(1) + minK (8) 
         private int          state;
 
         SearchState(Cursor cursor, long[] automaton, int minK, boolean hasHit) {
             this.cursor = cursor;
             this.automaton = automaton;
             this.state = ((hasHit ? 1 : 0) << 5) | (minK << 6);
         }
 
         /**
          * Initial forward search state
          * 
          * @param strand
          * @param searchDirection
          */
         public SearchState(Strand strand, SearchDirection searchDirection) {
             this(new Cursor(strand, searchDirection, fmIndex.wholeSARange(), null, 0, 0, null), new long[k + 1], 0,
                     false);
             // Activate the diagonal states 
             for (int i = 0; i <= k; ++i) {
                 automaton[i] = 1L << (k + i);
             }
         }
 
         public String showNFAState(long[] automaton) {
             int w = (k - getLowerBoundOfK()) * 2 + 1;
             StringBuilder s = new StringBuilder();
             for (int j = 0; j < automaton.length; ++j) {
                 for (int i = 0; i < w; ++i) {
                     s.append(((automaton[j] >>> i) & 1) == 0 ? "0" : "1");
                 }
                 if (j != automaton.length - 1) {
                     s.append(" \n");
                 }
             }
             return s.toString();
         }
 
         @Override
         public String toString() {
             return String.format("%sk%d%s \n%s", hasHit() ? "*" : "", getLowerBoundOfK(), cursor,
                     showNFAState(automaton));
         }
 
         public int getStrandIndex() {
             return cursor.flag & 1;
         }
 
         public int getIndex() {
             return cursor.getIndex();
         }
 
         public int getLowerBoundOfK() {
             return (state >>> 6) & 0xFF;
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
 
         public void updateSplitFlag() {
             this.state |= 1 << 4;
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
                return new SearchState(cursor.split(), nextAutomaton, minK, hasHit());
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
             final long qeq = queryMask.getPatternMaskIn64bit(ch, colStart);
 
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
             for (int i = 0; i < height; ++i) {
                 if ((next[i] & (1L << mPos)) != 0L) {
                     foundMatch = true;
                     break;
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
             return new SearchState(nextCursor, trimmed, getLowerBoundOfK() + nm, foundMatch);
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
             // prefer longer match
             int diff = o2.getIndex() - o1.getIndex();
             if (diff != 0)
                 return diff;
 
             int kDiff = o1.getLowerBoundOfK() - o2.getLowerBoundOfK();
             return kDiff;
         }
     }
 
     public class AlignmentProcess
     {
 
         private ACGTSequence[]             q         = new ACGTSequence[2];
         private QueryMask[]                queryMask = new QueryMask[2];
         private PriorityQueue<SearchState> queue     = new PriorityQueue<SearchState>(11, new StatePreference());
         private Reporter                   out;
 
         public AlignmentProcess(ACGTSequence query, Reporter out) {
             this.q[0] = query;
             this.q[1] = query.complement();
             this.queryMask[0] = new QueryMask(q[0]);
             this.queryMask[1] = new QueryMask(q[1]);
             this.out = out;
         }
 
         public void align() throws Exception {
 
             if (q[0].fastCount(ACGT.N, 0, m) > config.maximumEditDistances) {
                 return; // skip this alignment
             }
 
             // Add states for both strands
             queue.add(new SearchState(Strand.FORWARD, SearchDirection.Forward));
             queue.add(new SearchState(Strand.REVERSE, SearchDirection.Forward));
 
             while (!queue.isEmpty()) {
                 SearchState c = queue.poll();
                 if (c.isFinished()) {
                     continue;
                 }
 
                 int allowedMismatches = k - c.getLowerBoundOfK();
                 if (c.hasHit()) {
                     // TODO verification
                     reportAlignment(c);
                     continue;
                 }
 
                 if (allowedMismatches == 0) {
                     // do exact match
                     SearchState matchState = exactMatch(c);
                     if (matchState != null)
                         reportAlignment(matchState);
                     continue;
                 }
 
                 ACGT nextBase = c.cursor.nextACGT(q);
                 if (!c.isChecked(nextBase)) {
                     // search for a base in the read
                     step(c, nextBase);
                     if (!c.isFinished())
                         queue.add(c); // preserve the state for backtracking
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
                 {
                     int index = c.getIndex();
                     c.updateSplitFlag();
                     if (index > config.indelEndSkip && m - index > config.indelEndSkip) {
                         SearchState nextState = c.nextStateAfterSplit();
                         if (nextState != null)
                             queue.add(nextState);
                     }
                 }
 
                 assert (c.isFinished());
             }
         }
 
         private void reportAlignment(SearchState c) throws Exception {
             out.emit(c);
         }
 
         /**
          * @param c
          * @param ch
          * @return has match
          */
         private boolean step(SearchState c, ACGT ch) {
             c.updateFlag(ch);
             Cursor nextCursor = c.cursor.next(ch);
             if (nextCursor == null)
                 return false; // no match
 
             SearchState nextState = c.nextState(ch, nextCursor, queryMask[c.getStrandIndex()]);
             if (nextState != null)
                 queue.add(nextState);
             return true;
         }
 
         private SearchState exactMatch(SearchState c) {
             Cursor cursor = c.cursor;
             final int n = cursor.getRemainingBases();
             final int strandIndex = c.getStrandIndex();
             int numExtend = 0;
             while (numExtend < n) {
                 cursor = cursor.next(cursor.nextACGT(q));
                 if (cursor == null)
                     return null;
                 numExtend++;
             }
             return new SearchState(cursor, null, c.getLowerBoundOfK(), true);
         }
 
     }
 
 }
