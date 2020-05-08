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
 
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.PriorityQueue;
 
 import org.utgenome.UTGBException;
 import org.utgenome.weaver.align.ACGT;
 import org.utgenome.weaver.align.ACGTSequence;
 import org.utgenome.weaver.align.AlignmentConfig;
 import org.utgenome.weaver.align.BitParallelSmithWaterman;
 import org.utgenome.weaver.align.FMIndexOnGenome;
 import org.utgenome.weaver.align.QueryMask;
 import org.utgenome.weaver.align.SequenceBoundary.PosOnGenome;
 import org.utgenome.weaver.align.SiSet;
 import org.utgenome.weaver.align.SmithWatermanAligner.Alignment;
 import org.utgenome.weaver.align.Strand;
 import org.utgenome.weaver.align.SuffixInterval;
 import org.utgenome.weaver.align.record.AlignmentRecord;
 import org.utgenome.weaver.align.record.Read;
 import org.utgenome.weaver.align.record.ReadHit;
 import org.utgenome.weaver.align.record.SingleEndRead;
 import org.utgenome.weaver.align.strategy.FMSearchNFA.NextState;
 import org.utgenome.weaver.parallel.Reporter;
 import org.xerial.lens.SilkLens;
 import org.xerial.util.StopWatch;
 import org.xerial.util.StringUtil;
 import org.xerial.util.log.Logger;
 
 /**
  * Alignment algorithm using suffix filter on NFA and FM index.
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
     private static Logger                     _logger               = Logger.getLogger(SuffixFilter.class);
 
     private final FMIndexOnGenome             fmIndex;
     private final AlignmentConfig             config;
     private final ACGTSequence                reference;
     private final int                         k;                                                              // maximum number of mismatches allowed
 
     private HashMap<Integer, StaircaseFilter> staircaseFilterHolder = new HashMap<Integer, StaircaseFilter>();
 
     public static enum Diff {
         Match, Mismatch, Insertion, Deletion, Split
     };
 
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
         public final Cursor         cursor;
         public final SuffixInterval currentSi;
         public final SiSet          siTable;
         private FMSearchNFA         automaton;
         // 32 bit = searchFlag(5) + currentBase(3) + minK (8) + priority(8) + hasHit(1) 
         private int                 state;
         public final SearchState    split;
 
         public int getLowerBoundOfK() {
             return (state >>> 8) & 0xFF;
         }
 
         public void setLowerBoundOfK(int diff) {
             state &= ~(0xFF << 8);
             state |= (diff & 0xFF) << 8;
         }
 
         /**
          * Lower value has higher priority
          * 
          * @return
          */
         public int getPriority() {
             return (state >>> 16) & 0xFF;
         }
 
         public void lowerThePrioity(int margin) {
             int priority = getPriority() + margin;
             state &= ~(0xFF << 16);
             state |= (priority & 0xFF) << 16;
         }
 
         public boolean hasHit() {
             return ((state >>> 24) & 1L) != 0;
         }
 
         public boolean isFinished() {
             return (state & 0x1F) == 0x1F; // ACGT + split
         }
 
         public ACGT currentACGT() {
             return ACGT.decode((byte) ((state >>> 5) & 0x7));
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
 
         public boolean isSplitChecked() {
             return (state & (1 << 4)) != 0;
         }
 
         public boolean isChecked(ACGT ch) {
             return (state & (1 << ch.code)) != 0;
         }
 
         SearchState(SuffixInterval currentSi, ACGT ch, Cursor cursor, SiSet si, FMSearchNFA automaton,
                 boolean hasMatch, int minK, int expectedK, SearchState split) {
             this.currentSi = currentSi;
             this.cursor = cursor;
             this.siTable = si;
             this.automaton = automaton;
             this.state = ((ch.code & 0x7) << 5) | ((minK & 0xFF) << 8) | ((expectedK & 0xFF) << 16)
                     | ((hasMatch ? 1 : 0) << 24);
             this.split = split;
         }
 
         /**
          * Initial forward search state
          * 
          * @param strand
          * @param searchDirection
          */
         public SearchState(SuffixInterval currentSi, Cursor cursor, int expectedK) {
             this(currentSi, ACGT.N, cursor, fmIndex.initSet(cursor.getSearchDirection()), new FMSearchNFA(k), false, 0,
                     expectedK, null);
             automaton.activateDiagonalStates();
         }
 
         String getUpdateFlag() {
             StringBuilder s = new StringBuilder();
             for (ACGT ch : ACGT.exceptN) {
                 s.append(isChecked(ch) ? ch.name() : ch.name().toLowerCase());
             }
             if (isSplitChecked())
                 s.append("^");
             return s.toString();
         }
 
         @Override
         public String toString() {
            return String.format("%sk%dp%d%s %s %s %s %s", hasHit() ? "*" : "", getLowerBoundOfK(), getPriority(),
                    cursor, currentACGT(), currentSi, getUpdateFlag(), siTable);
         }
 
         public int score() {
             int nm = getLowerBoundOfK();
             int mm = cursor.getIndex() - nm;
             return mm * config.matchScore - nm * config.mismatchPenalty;
         }
 
        public SearchState nextStateAfterSplit() {
             updateSplitFlag();
             // use the same automaton state
             int minK = getLowerBoundOfK();
             if (minK < k) {
                Cursor newCursor = cursor.split();
                return new SearchState(null, ACGT.N, newCursor, fmIndex.initSet(newCursor.getSearchDirection()),
                        automaton.nextStateAfterSplit(k), false, minK, getPriority(), this);
             }
             else
                 return null;
         }
 
         public SearchState nextState(ACGT ch, SiSet nextSi, QueryMask queryMask, StaircaseFilter staircaseFilter) {
 
             NextState next = automaton.nextState(cursor, ch, queryMask, staircaseFilter);
             if (next == null)
                 return null; // no match
 
             int index = cursor.getIndex();
             int nextMinK = next.nextState.kOffset;
 
             SuffixInterval si = cursor.isForwardSearch() ? this.siTable.getForward(ch) : this.siTable.getBackward(ch);
             return new SearchState(si, ch, cursor.next(), nextSi, next.nextState, next.hasMatch, nextMinK,
                     getPriority(), split);
         }
 
     }
 
     private SiSet next(SearchState c, ACGT ch) {
         return c.cursor.nextSi(fmIndex, c.siTable, ch);
     }
 
     /**
      * Prepare a suffix filter
      * 
      * @param fmIndex
      *            FM index
      * @param config
      *            alignment score config
      * @param m
      *            read length
      */
     public SuffixFilter(FMIndexOnGenome fmIndex, ACGTSequence reference, AlignmentConfig config) {
         this.fmIndex = fmIndex;
         this.reference = reference;
         this.config = config;
         this.k = config.maximumEditDistances;
     }
 
     public void align(ACGTSequence seq) throws Exception {
         align(new SingleEndRead("read", seq, null));
     }
 
     public void align(Read read) throws Exception {
         new AlignmentProcess(read, new Reporter() {
             @Override
             public void emit(Object result) throws Exception {
                 if (AlignmentRecord.class.isInstance(result)) {
                     AlignmentRecord r = (AlignmentRecord) result;
                     _logger.debug(SilkLens.toSilk("alignment", r));
                 }
             }
         }).align();
     }
 
     public void align(Read read, Reporter out) throws Exception {
         new AlignmentProcess(read, out).align();
     }
 
     /**
      * Alignment state queue
      * 
      * @author leo
      * 
      */
     private static class StateQueue extends PriorityQueue<SearchState>
     {
         private static final long serialVersionUID = 1L;
 
         public StateQueue() {
             super(11, new Comparator<SearchState>() {
                 // Comparator for selecting next target state to search
                 @Override
                 public int compare(SearchState o1, SearchState o2) {
                     int diff = 0;
                     diff = o1.getPriority() - o2.getPriority();
                     if (diff == 0)
                         diff = -(o1.score() - o2.score());
                     if (diff == 0)
                         diff = -(o1.cursor.getIndex() - o2.cursor.getIndex());
 
                     return diff;
                 }
             });
         }
 
         @Override
         public boolean add(SearchState e) {
             if (e == null)
                 return false;
             return super.add(e);
         }
 
         @Override
         public String toString() {
             return StringUtil.join(this, "\n");
         }
     }
 
     /**
      * Get the staircase filter of NFA for the specified query length
      * 
      * @param queryLength
      * @return
      */
     private StaircaseFilter getStairCaseFilter(int queryLength) {
         if (!staircaseFilterHolder.containsKey(queryLength)) {
             StaircaseFilter filter = new StaircaseFilter(queryLength, k);
             staircaseFilterHolder.put(queryLength, filter);
         }
 
         return staircaseFilterHolder.get(queryLength);
     }
 
     /**
      * Alignment procedure
      * 
      * @author leo
      * 
      */
     class AlignmentProcess
     {
 
         private final Read            read;
         private final int             m;
         private final StaircaseFilter staircaseFilter;
         private ACGTSequence[]        q             = new ACGTSequence[2];
         private QueryMask[]           queryMask     = new QueryMask[2];
         private StateQueue            queue         = new StateQueue();
 
         private AlignmentResultHolder resultHolder  = new AlignmentResultHolder();
         private Reporter              out;
 
         private int                   minMismatches = k + 1;
         private int                   bestScore     = -1;
 
         public AlignmentProcess(Read read, Reporter out) {
             this.read = read;
             this.m = (int) read.getRead(0).textSize();
             this.staircaseFilter = getStairCaseFilter(m);
             this.q[0] = read.getRead(0);
             this.q[1] = q[0].complement();
             this.out = out;
         }
 
         public void align() throws Exception {
 
             StopWatch s = new StopWatch();
             try {
                 align_internal();
                 boolean hasHit = minMismatches <= k || !resultHolder.hitList.isEmpty();
                 ReadHit besthit = null;
                 if (hasHit)
                     besthit = resultHolder.hitList.get(0);
                 boolean isUnique = resultHolder.isUnique();
 
                 if (_logger.isDebugEnabled()) {
                     _logger.debug(
                             "query:%s %s %2s k:%d, FM Search:%,d, SW:%d, Exact:%d, CutOff:%d, Filtered:%d, %.5f sec.",
                             read.name(), hasHit ? besthit.strand.symbol : " ", hasHit ? besthit.getAlignmentState()
                                     : " ", minMismatches, numFMIndexSearches, numSW, numExactSearchCount, numCutOff,
                             numFiltered, s.getElapsedTime());
 
                     if (minMismatches > k || numFMIndexSearches > 500) {
                         _logger.debug("query:%s", q[0]);
                     }
                 }
                 if (_logger.isTraceEnabled())
                     _logger.trace("qual :%s", read.getQual(0));
 
                 // Issue 28 
                 if (hasHit) {
                     switch (config.reportType) {
                     case ALLHITS:
                         for (ReadHit each : resultHolder.hitList) {
                             report(each, 0);
                         }
                         break;
                     case BESTHIT:
                         report(besthit, 0);
                         break;
                     case TOPL: {
                         int max = Math.min(config.topL, resultHolder.hitList.size());
                         for (int i = 0; i < max; ++i) {
                             report(resultHolder.hitList.get(i), 0);
                         }
                         break;
                     }
                     }
                 }
                 else {
                     // report unmapped read
                     report(new ReadHit("*", 0, 0, -1, Strand.FORWARD, "", 0, null), 0);
                 }
 
             }
             catch (Exception e) {
                 _logger.error("error at query: %s", q[0]);
                 throw e;
             }
         }
 
         public void report(ReadHit hit, int numTotalHits) throws Exception {
             if (_logger.isTraceEnabled())
                 _logger.trace(SilkLens.toSilk("hit", hit));
             AlignmentRecord r = AlignmentRecord.convert(hit, read, numTotalHits);
             out.emit(r);
         }
 
         private ACGTSequence replaceN_withA(ACGTSequence seq) {
             ACGTSequence newSeq = new ACGTSequence(seq);
             for (int i = 0; i < seq.length(); ++i) {
                 if (seq.getACGT(i) == ACGT.N) {
                     newSeq.set(i, ACGT.A);
                 }
             }
             return newSeq;
         }
 
         /**
          * @throws Exception
          */
         public void align_internal() throws Exception {
 
             // Check whether the read contains too many Ns
             {
                 long countN = q[0].fastCount(ACGT.N, 0, m);
                 if (countN > config.maximumEditDistances) {
                     return; // skip this alignment
                 }
 
                 if (countN > 0) {
                     q[0] = replaceN_withA(q[0]);
                     q[1] = replaceN_withA(q[1]);
                 }
             }
 
             // quick scan for k=0 (exact match)
             {
                 // Search forward strand
                 FMQuickScan scanF = FMQuickScan.scanMismatchLocations(fmIndex, q[0], Strand.FORWARD);
                 if (scanF.numMismatches == 0) {
                     minMismatches = 0;
                     reportExactMatchAlignment(scanF);
                     return;
                 }
                 // Search reverse strand
                 FMQuickScan scanR = FMQuickScan.scanMismatchLocations(fmIndex, q[1], Strand.REVERSE);
                 if (scanR.numMismatches == 0) {
                     minMismatches = 0;
                     reportExactMatchAlignment(scanR);
                     return;
                 }
 
                 if (_logger.isTraceEnabled()) {
                     _logger.trace(SilkLens.toSilk("scanF", scanF));
                     _logger.trace(SilkLens.toSilk("scanR", scanR));
                 }
 
                 if (k == 0)
                     return;
 
                 this.queryMask[0] = new QueryMask(q[0]);
                 this.queryMask[1] = new QueryMask(q[1]);
 
                 SearchState sF = null;
                 SearchState sR = null;
                 // Add states for both strands
                 if (scanF.numMismatches <= k) {
                     if (scanF.longestMatch.start != 0 && scanF.longestMatch.start < m) {
                         // add bidirectional search state
                         sF = new SearchState(null, new Cursor(Strand.FORWARD, SearchDirection.BidirectionalForward, m,
                                 scanF.longestMatch.start, scanF.longestMatch.start, null), scanF.numMismatches);
                     }
                     else {
                         sF = new SearchState(null, new Cursor(Strand.FORWARD, SearchDirection.Forward, m, 0, 0, null),
                                 scanF.numMismatches);
                     }
                 }
 
                 if (scanR.numMismatches <= k) {
                     if (scanR.longestMatch.start != 0 && scanR.longestMatch.start < m) {
                         // add bidirectional search state
                         sR = new SearchState(null, new Cursor(Strand.REVERSE, SearchDirection.BidirectionalForward, m,
                                 scanR.longestMatch.start, scanR.longestMatch.start, null), scanR.numMismatches);
                     }
                     else {
                         sR = new SearchState(null, new Cursor(Strand.REVERSE, SearchDirection.Forward, m, 0, 0, null),
                                 scanR.numMismatches);
                     }
                 }
 
                 queue.add(sF);
                 queue.add(sR);
 
             }
 
             // Iterative search for k>=0
             while (!queue.isEmpty()) {
                 SearchState c = queue.poll();
                 if (_logger.isTraceEnabled()) {
                     _logger.trace("state: %s, FMIndex:%d, CutOff:%d", c, numFMIndexSearches, numCutOff);
                 }
 
                 // When a hit is found, report the alignment
                 if (c.hasHit() || c.cursor.getRemainingBases() == 0) {
                     reportAlignment(c);
                     continue;
                 }
 
                 // No more child states
                 if (c.isFinished())
                     continue;
 
                 // lower bound of the number of mismatches
                 int nm = c.getLowerBoundOfK();
                 if (nm > minMismatches) {
                     ++numCutOff;
                     continue;
                 }
 
                 int allowedMismatches = minMismatches - nm;
                 if (allowedMismatches < 0)
                     continue;
 
                 final int strandIndex = c.cursor.getStrand().index;
                 // Step forward the cursor
                 // Match 
                 ACGT nextBase = c.cursor.nextACGT(q);
                 {
                     if (!c.isChecked(nextBase)) {
                         c.updateFlag(nextBase);
                         if (!c.siTable.isEmpty(nextBase)) {
                             SiSet nextSi = next(c, nextBase);
                             ++numFMIndexSearches;
                             SearchState nextState = c.nextState(nextBase, nextSi, queryMask[strandIndex],
                                     staircaseFilter);
                             if (nextState != null)
                                 queue.add(nextState);
                             else
                                 ++numFiltered;
                             continue;
                         }
                     }
                 }
 
                 // Traverse the suffix arrays for all of A, C, G and T                 
                 // Add states for every bases
                 for (ACGT ch : ACGT.exceptN) {
                     if (ch == nextBase)
                         continue;
 
                     if (!c.isChecked(ch)) {
                         c.updateFlag(ch);
 
                         if (!c.siTable.isEmpty(ch)) {
                             SiSet nextSi = next(c, ch);
                             ++numFMIndexSearches;
                             SearchState nextState = c.nextState(ch, nextSi, queryMask[strandIndex], staircaseFilter);
                             if (nextState != null)
                                 queue.add(nextState);
                             else
                                 ++numFiltered;
                         }
                     }
                 }
 
                 // Split alignment
                 c.updateSplitFlag();
                 if (!c.cursor.hasSplit() && config.numSplitAlowed > 0 && nm + 1 <= minMismatches) {
                     int index = c.cursor.getIndex();
                     if (index > config.indelEndSkip && m - index >= config.indelEndSkip) {
                        SearchState nextState = c.nextStateAfterSplit();
                         if (nextState != null)
                             queue.add(nextState);
                         else
                             ++numFiltered;
                     }
                 }
             }
 
         }
 
         private void reportExactMatchAlignment(FMQuickScan f) throws Exception {
             PosOnGenome pos = fmIndex.toGenomeCoordinate(f.si.lowerBound, m, f.strand);
             resultHolder.add(new ReadHit(pos.chr, pos.pos, m, 0, f.strand, String.format("%dM", m), (int) f.si.range(),
                     null));
         }
 
         public ReadHit verify(SearchState s, int fragmentLength, boolean isSplit) {
 
             if (_logger.isTraceEnabled())
                 _logger.trace("verify state: %s", s);
 
             SuffixInterval si = s.currentSi;
             int nm = s.getLowerBoundOfK();
             Cursor cursor = s.cursor;
 
             // Verification phase
             if (si == null)
                 return null;
 
             ReadHit hit = null;
 
             long candidateSi = si.lowerBound;
             long seqIndex = fmIndex.toCoordinate(candidateSi, cursor.getStrand(), cursor.getSearchDirection());
 
             long x = seqIndex;
             int offset = 0;
             offset = cursor.getOffsetOfSearchHead(isSplit, fragmentLength);
             x -= offset;
 
             if (x < 0 || x + fragmentLength > fmIndex.textSize()) {
                 return null; // ignore the match at cycle boundary
             }
 
             long refStart = Math.max(0, x - nm);
             long refEnd = x + fragmentLength + nm;
             ACGTSequence ref = reference.subSequence(refStart, refEnd);
             ACGTSequence query = q[cursor.getStrandIndex()];
             if (isSplit) {
                 query = query.subSequence(cursor.cursorB, cursor.cursorB + fragmentLength);
             }
             if (cursor.getStrand() == Strand.REVERSE)
                 query = query.reverse();
 
             if (_logger.isTraceEnabled())
                _logger.trace("Bit-parallel alignment:\n%10d %s\n           %s", refStart, ref, query);
 
             Alignment alignment = BitParallelSmithWaterman.alignBlockDetailed(ref, query, config.bandWidth);
             ++numSW;
             if (alignment != null && alignment.numMismatches <= nm) {
                 if (_logger.isTraceEnabled())
                     _logger.trace("Found an alignment: %s", alignment);
 
                 try {
                     PosOnGenome p = fmIndex.getSequenceBoundary().translate(refStart + alignment.pos + 1,
                             Strand.FORWARD);
                     hit = new ReadHit(p.chr, p.pos, fragmentLength, alignment.numMismatches, cursor.getStrand(),
                             alignment.cigar, (int) si.range(), null);
                 }
                 catch (UTGBException e) {
                     _logger.error(e);
                 }
             }
 
             return hit;
         }
 
         private void reportAlignment(SearchState c) throws Exception {
 
             // Verification phase
             if (c.split != null) {
                 // split alignment
                 int splitLen = c.split.cursor.getProcessedBases();
                 int m1 = Math.max(c.cursor.getProcessedBases(), m) - splitLen;
                 ReadHit alignment = verify(c, m1, true);
                 ReadHit splitAlignment = verify(c.split, splitLen, true);
                 if (alignment == null || splitAlignment == null)
                     return; // no match within k mismaches
 
                 int diff = alignment.diff + splitAlignment.diff + 1;
 
                 // update the lower bound of mismatches
                 c.setLowerBoundOfK(diff);
 
                 if (alignment.pos <= splitAlignment.pos)
                     resultHolder.add(alignment.addSplit(splitAlignment));
                 else
                     resultHolder.add(splitAlignment.addSplit(alignment));
             }
             else {
                 // single hit
                 ReadHit alignment = verify(c, c.cursor.getFragmentLength(), false);
                 if (alignment == null)
                     return; // no match within k mismatches
 
                 // update the lower bound of mismatches
                 c.setLowerBoundOfK(alignment.diff);
 
                 resultHolder.add(alignment);
             }
 
         }
 
         private int numFMIndexSearches  = 0;
         private int numSW               = 0;
         private int numCutOff           = 0;
         private int numFiltered         = 0;
         private int numExactSearchCount = 0;
 
         //        private void reportExactMatch(SearchState c) {
         //            ++numExactSearchCount;
         //            Cursor cursor = c.cursor;
         //            final int n = cursor.getRemainingBases();
         //            int numExtend = 0;
         //
         //            SuffixInterval si = c.currentSi;
         //            SiSet siSet = c.siTable;
         //            ACGT ch = null;
         //            while (numExtend < n) {
         //                ch = cursor.nextACGT(q);
         //                if (siSet.equals(ch))
         //                    return; // no match
         //                siSet = cursor.nextSi(fmIndex, siSet, ch);
         //                ++numFMIndexSearches;
         //                cursor = cursor.next();
         //                ++numExtend;
         //            }
         //
         //            long candidateSi = si.lowerBound;
         //            long seqIndex = fmIndex.toCoordinate(candidateSi, cursor.getStrand(), cursor.getSearchDirection());
         //            int offset = cursor.getOffsetOfSearchHead(cursor.hasSplit(), cursor.getReadLength());
         //            long x = seqIndex - offset;
         //            if (x < 0 || x + cursor.getFragmentLength() > fmIndex.textSize()) {
         //                return; // ignore the match at cycle boundary
         //            }
         //
         //            try {
         //                PosOnGenome pos = fmIndex.getSequenceBoundary().translate(x + 1, Strand.FORWARD);
         //                ReadHit hit = new ReadHit(pos.chr, pos.pos, cursor.getReadLength(), c.getLowerBoundOfK(),
         //                        c.cursor.getStrand(), (int) si.range(), null);
         //                resultHolder.add(hit);
         //            }
         //            catch (UTGBException e) {
         //                _logger.error(e);
         //            }
         //        }
 
         private class AlignmentResultHolder
         {
             ArrayList<ReadHit> hitList = new ArrayList<ReadHit>();
 
             public int totalHits() {
                 int count = 0;
                 for (ReadHit each : hitList) {
                     count += each.numHits;
                 }
                 return count;
             }
 
             public void add(ReadHit hit) {
                 int k = hit.getK();
                 if (k >= 0 && k < minMismatches) {
                     minMismatches = k;
                     if (hit.nextSplit == null) {
                         bestScore = config.matchScore * (hit.matchLength - k) - config.mismatchPenalty * k;
                     }
                     else {
                         bestScore = config.matchScore * (hit.matchLength - k - 1) - config.mismatchPenalty * (k - 1)
                                 - config.splitOpenPenalty;
                     }
                 }
 
                 ArrayList<ReadHit> newList = new ArrayList<ReadHit>();
                 for (ReadHit each : hitList) {
                     if (each.getK() <= minMismatches) {
                         newList.add(each);
                     }
                 }
                 newList.add(hit);
                 hitList = newList;
             }
 
             public boolean isUnique() {
                 if (hitList.size() == 1) {
                     return hitList.get(0).numHits == 1;
                 }
                 return false;
             }
 
         }
 
     }
 
     private static String reverse(String s) {
         if (s == null)
             return null;
         StringBuilder out = new StringBuilder(s.length());
         for (int i = s.length() - 1; i >= 0; --i)
             out.append(s.charAt(i));
         return out.toString();
     }
 
 }
