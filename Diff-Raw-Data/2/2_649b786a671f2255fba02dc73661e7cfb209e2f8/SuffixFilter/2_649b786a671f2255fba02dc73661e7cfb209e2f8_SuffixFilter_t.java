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
 import java.util.List;
 import java.util.PriorityQueue;
 
 import org.utgenome.UTGBException;
 import org.utgenome.weaver.align.ACGT;
 import org.utgenome.weaver.align.ACGTSequence;
 import org.utgenome.weaver.align.AlignmentConfig;
 import org.utgenome.weaver.align.BitParallelSmithWaterman;
 import org.utgenome.weaver.align.CIGAR;
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
 import org.utgenome.weaver.align.strategy.ReadAlignmentNFA.NextState;
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
     private static Logger                   _logger               = Logger.getLogger(SuffixFilter.class);
 
     private final FMIndexOnGenome           fmIndex;
     private final AlignmentConfig           config;
     private final ACGTSequence              reference;
     //private final int                       k;                                                            // maximum number of mismatches allowed
 
     /**
      * query length -> staircase filter of this query length
      */
     private HashMap<SFKey, StaircaseFilter> staircaseFilterHolder = new HashMap<SFKey, StaircaseFilter>();
 
     private static class SFKey
     {
         public final int numMismatches;
         public final int queryLen;
         public int       hash = 0;
 
         public SFKey(int numMismatches, int queryLen) {
             this.numMismatches = numMismatches;
             this.queryLen = queryLen;
         }
 
         @Override
         public int hashCode() {
             if (hash == 0) {
                 hash = 3;
                 hash += numMismatches * 17;
                 hash += queryLen * 17;
                 hash = hash / 1997;
             }
             return hash;
         }
 
         @Override
         public boolean equals(Object obj) {
             if (obj instanceof SFKey) {
                 SFKey other = (SFKey) obj;
                 return this.numMismatches == other.numMismatches && this.queryLen == other.queryLen;
             }
             return false;
         }
 
     }
 
     /**
      * Compute next suffix intervals
      * 
      * @param c
      * @param ch
      * @return
      */
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
         //this.k = config.maximumEditDistances;
     }
 
     public List<AlignmentRecord> align(ACGTSequence seq) throws Exception {
         return align(new SingleEndRead("read", seq, null));
     }
 
     public List<AlignmentRecord> align(Read read) throws Exception {
         final List<AlignmentRecord> alignmentResult = new ArrayList<AlignmentRecord>();
         new AlignmentProcess(read, new Reporter() {
             @Override
             public void emit(Object result) throws Exception {
                 if (AlignmentRecord.class.isInstance(result)) {
                     AlignmentRecord r = (AlignmentRecord) result;
                     _logger.debug(SilkLens.toSilk("alignment", r));
                     alignmentResult.add(r);
                 }
             }
         }).align();
         return alignmentResult;
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
                         diff = -(o1.cursor.getProcessedBases() - o2.cursor.getProcessedBases());
 
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
      * Alignment procedure
      * 
      * @author leo
      * 
      */
     class AlignmentProcess
     {
         private final Read            read;                                        // original read
         private final int             m;                                           // read length
         private ACGTSequence[]        q              = new ACGTSequence[2];        // forward/reverse query sequence
         private QueryMask[]           queryMask      = new QueryMask[2];           // bit mask of ACGT occurrence positions
         private StateQueue            queue          = new StateQueue();           // priority queue holding search states
 
         private AlignmentResultHolder resultHolder   = new AlignmentResultHolder();
         private Reporter              out;
 
         private final int             k;
         private int                   minMismatches;
         private int                   maxMatchLength = 0;
         private int                   bestScore      = -1;
 
         public AlignmentProcess(Read read, Reporter out) {
             this.read = read;
             this.m = (int) read.getRead(0).textSize();
             this.q[0] = read.getRead(0);
             this.q[1] = q[0].complement();
             this.out = out;
 
             if (config.maximumEditDistances > 0 && config.maximumEditDistances < 1) {
                 this.k = (int) Math.floor(m * config.maximumEditDistances);
             }
             else {
                 this.k = (int) config.maximumEditDistances;
             }
             this.minMismatches = k + 1;
         }
 
         /**
          * Get the staircase filter of NFA for the specified query length
          * 
          * @param queryLength
          * @return
          */
         private StaircaseFilter getStairCaseFilter(int queryLength) {
             int kk = minMismatches;
             SFKey key = new SFKey(kk, queryLength);
             if (!staircaseFilterHolder.containsKey(key)) {
                 StaircaseFilter filter = new StaircaseFilter(queryLength, kk);
                 staircaseFilterHolder.put(key, filter);
             }
 
             return staircaseFilterHolder.get(key);
         }
 
         public void align() throws Exception {
 
             StopWatch s = new StopWatch();
             try {
                 align_internal();
                 boolean hasHit = minMismatches <= k && !resultHolder.hitList.isEmpty();
                 ReadHit besthit = null;
                 String cigar = "";
                 if (hasHit) {
                     besthit = resultHolder.hitList.get(0);
                     cigar = besthit.getCigarConcatenated();
                 }
                 boolean isUnique = resultHolder.isUnique();
 
                 if (_logger.isDebugEnabled()) {
                     _logger.debug(
                             "query:%s %s %2s %s k:%d, FM Search:%,d, SW:%d, Exact:%d, CutOff:%d, Filtered:%d, %.5f sec.",
                             read.name(), hasHit ? besthit.strand.symbol : " ", hasHit ? besthit.getAlignmentState()
                                     : " ", cigar, minMismatches, numFMIndexSearches, numSW, numExactSearchCount,
                             numCutOff, numFiltered, s.getElapsedTime());
 
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
                     report(new ReadHit("*", 0, 0, -1, Strand.FORWARD, new CIGAR(), 0, null), 0);
                 }
 
             }
             catch (Exception e) {
                 _logger.error("error at query: %s", q[0]);
                 throw e;
             }
         }
 
         public void report(ReadHit hit, int numTotalHits) throws Exception {
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
                if (countN > k) {
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
                         sF = new SearchState(k, null, new Cursor(Strand.FORWARD, SearchDirection.BidirectionalForward,
                                 0, m, scanF.longestMatch.start, scanF.longestMatch.start), scanF.numMismatches);
                     }
                     else {
                         sF = new SearchState(k, null, new Cursor(Strand.FORWARD, SearchDirection.Forward, 0, m, 0, 0),
                                 scanF.numMismatches);
                     }
                 }
 
                 if (scanR.numMismatches <= k) {
                     if (scanR.longestMatch.start != 0 && scanR.longestMatch.start < m) {
                         // add bidirectional search state
                         sR = new SearchState(k, null, new Cursor(Strand.REVERSE, SearchDirection.BidirectionalForward,
                                 0, m, scanR.longestMatch.start, scanR.longestMatch.start), scanR.numMismatches);
                     }
                     else {
                         sR = new SearchState(k, null, new Cursor(Strand.REVERSE, SearchDirection.Forward, 0, m, 0, 0),
                                 scanR.numMismatches);
                     }
                 }
 
                 queue.add(sF);
                 queue.add(sR);
 
                 //minMismatches = Math.min(scanF.numMismatches, Math.min(scanR.numMismatches, k));
             }
 
             final int fmIndexSearchUpperBound = m * 20;
             // Iterative search for k>=0
             queue_loop: while (!queue.isEmpty()) {
                 if (numFMIndexSearches > fmIndexSearchUpperBound)
                     break queue_loop;
 
                 SearchState baseState = queue.poll();
                 SearchState c = baseState;
                 if (_logger.isTraceEnabled()) {
                     _logger.trace("state: %s, FMSearch:%d, CutOff:%d, Filtered:%d", c, numFMIndexSearches, numCutOff,
                             numFiltered);
                 }
 
                 // When a hit is found, report the alignment
                 {
                     SearchState next = c;
                     while (next.hasHit() || next.cursor.getRemainingBases() == 0) {
                         if (next.split == null) {
                             reportAlignment(c);
                             continue queue_loop;
                         }
                         else {
                             // switch to the next split
                             next = next.split;
                         }
                     }
                     c = next;
                 }
 
                 // No more child states
                 if (c.isFinished())
                     continue;
 
                 // nm: lower bound of the number of mismatches
                 int nm = c.getLowerBoundOfK();
                 if (nm > minMismatches) {
                     ++numCutOff;
                     continue;
                 }
 
                 int allowedMismatches = minMismatches - nm;
                 if (allowedMismatches < 0)
                     continue;
 
                 {
                     int scoreUB = baseState.upperBoundOfScore();
                     if (scoreUB < bestScore) {
                         ++numCutOff;
                         if (_logger.isTraceEnabled())
                             _logger.trace("cutoff: score upper bound: %d, current best score: %d", scoreUB, bestScore);
                         continue;
                     }
                 }
 
                 final int strandIndex = c.cursor.getStrand().index;
 
                 // Step forward the cursor
                 // Match 
                 ACGT nextBase = c.cursor.nextACGT(q);
                 {
                     ACGT ch = nextBase;
                     if (!c.isChecked(ch)) {
                         c.updateFlag(ch);
 
                         if (!c.siTable.isEmpty(ch)) {
                             SiSet nextSi = next(c, ch);
                             ++numFMIndexSearches;
                             SearchState nextState = c.nextState(ch, nextSi, queryMask[strandIndex],
                                     getStairCaseFilter(m));
                             if (nextState != null) {
                                 queue.add(baseState.update(c, nextState));
                                 continue queue_loop;
                             }
                             else
                                 ++numFiltered;
                         }
                     }
                 }
                 //                ACGT[] searchOrder = new ACGT[4];
                 //                {
                 //                    searchOrder[0] = nextBase;
                 //                    int i = 1;
                 //                    for (ACGT ch : ACGT.exceptN) {
                 //                        if (ch == nextBase)
                 //                            continue;
                 //                        searchOrder[i++] = ch;
                 //                    }
                 //                }
 
                 // Traverse the suffix arrays for all of A, C, G and T                 
                 // Add states for every bases
                 {
                     StaircaseFilter sf = getStairCaseFilter(m);
                     for (ACGT ch : ACGT.exceptN) {
                         if (!c.isChecked(ch)) {
                             c.updateFlag(ch);
                             if (!c.siTable.isEmpty(ch)) {
                                 SiSet nextSi = next(c, ch);
                                 ++numFMIndexSearches;
                                 SearchState nextState = c.nextState(ch, nextSi, queryMask[strandIndex], sf);
                                 if (nextState != null) {
                                     queue.add(baseState.update(c, nextState));
                                 }
                                 else
                                     ++numFiltered;
                             }
                         }
                     }
                 }
 
                 // Split alignment
                 c.updateSplitFlag();
                 if (baseState.getNumSplit() < config.numSplitAlowed && nm + 1 <= minMismatches) {
                     final int index = c.cursor.getNextACGTIndex();
                     if (index > config.indelEndSkip && m - index >= config.indelEndSkip) {
                         SearchState nextState = c.nextStateAfterSplit(k);
                         if (nextState != null) {
                             queue.add(baseState.update(c, nextState));
                         }
                         else
                             ++numFiltered;
                     }
                 }
             }
 
         }
 
         private void reportExactMatchAlignment(FMQuickScan f) throws Exception {
             PosOnGenome pos = fmIndex.toGenomeCoordinate(f.si.lowerBound, m, f.strand);
             resultHolder.add(new ReadHit(pos.chr, pos.pos, m, 0, f.strand, new CIGAR(String.format("%dM", m)),
                     (int) f.si.range(), null));
         }
 
         public ReadHit verify(SearchState s) {
 
             if (_logger.isTraceEnabled())
                 _logger.trace("verify state: %s", s);
 
             SuffixInterval si = s.currentSi;
 
             Cursor cursor = s.cursor;
 
             // Verification phase
             if (si == null)
                 return ReadHit.noHit(cursor.getStrand());
 
             ReadHit hit = null;
 
             long candidateSi = si.lowerBound;
             long seqIndex = fmIndex.toCoordinate(candidateSi, cursor.getStrand(), cursor.getSearchDirection());
 
             long x = seqIndex;
             int offset = 0;
             offset = cursor.getOffsetOfSearchHead();
             x -= offset;
 
             int fragmentLength = cursor.getFragmentLength();
             //            if (x < 0 || x + fragmentLength > fmIndex.textSize()) {
             //                return ReadHit.noHit(cursor.getStrand()); // ignore the match at cycle boundary
             //            }
 
             long refStart = Math.max(0, x - k);
             long refEnd = Math.min(refStart + fragmentLength + k, fmIndex.textSize());
             ACGTSequence ref = reference.subSequence(refStart, refEnd);
             ACGTSequence query = q[cursor.getStrandIndex()].subSequence(cursor.start, cursor.end);
             if (cursor.getStrand() == Strand.REVERSE)
                 query = query.reverse();
 
             if (_logger.isTraceEnabled())
                 _logger.trace("Bit-parallel alignment:\n%10d %s\n           %s", refStart, ref, query);
 
             Alignment alignment = BitParallelSmithWaterman.alignBlockDetailed(ref, query, config.bandWidth);
             ++numSW;
             if (alignment == null)
                 hit = ReadHit.noHit(cursor.getStrand());
             else {
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
             ReadHit alignment = verify(c);
             {
                 ReadHit nextHit = alignment;
                 for (SearchState next = c.split; next != null; next = next.split) {
                     ReadHit result = verify(next);
                     nextHit.nextSplit = result;
                 }
             }
             if (alignment.getTotalMatchLength() == 0) {
                 return; // no match
             }
 
             int newK = alignment.getTotalDifferences();
             if (newK > k)
                 return; //  no match within k mismatches
 
             // TODO allow clipped alignment (issue 40)
             c.setLowerBoundOfK(newK);
 
             // Reorder splits in ascending order
             resultHolder.add(alignment.sortSplits());
         }
 
         private int numFMIndexSearches  = 0;
         private int numSW               = 0;
         private int numCutOff           = 0;
         private int numFiltered         = 0;
         private int numExactSearchCount = 0;
 
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
                 int newK = hit.getTotalDifferences();
                 int matchLen = hit.getTotalMatchLength();
                 if (matchLen > 0 && newK < minMismatches) {
                     minMismatches = newK;
                     bestScore = hit.getTotalScore(config);
                 }
                 if (maxMatchLength < matchLen) {
                     maxMatchLength = matchLen;
                 }
 
                 ArrayList<ReadHit> newList = new ArrayList<ReadHit>();
                 for (ReadHit each : hitList) {
                     if (each.getTotalDifferences() <= minMismatches) {
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
 
     /**
      * Holder of an NFA state and an alignment cursor
      * 
      * This NFA holds 2k+1 columns and k+1 rows
      * 
      * @author leo
      * 
      */
     public class SearchState
     {
         public final Cursor         cursor;
         public final SuffixInterval currentSi;
         public final SiSet          siTable;
         private ReadAlignmentNFA    automaton;
         // 32 bit = searchFlag(5) + currentBase(3) + minK (8) + priority(8) + hasHit(1) 
         private int                 state;
         public SearchState          split;
 
         public int getLowerBoundOfK() {
             return (state >>> 8) & 0xFF;
         }
 
         public void setLowerBoundOfK(int diff) {
             state &= ~(0xFF << 8);
             state |= (diff & 0xFF) << 8;
         }
 
         public int getNumSplit() {
             return split == null ? 0 : 1 + split.getNumSplit();
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
 
         SearchState(SuffixInterval currentSi, ACGT ch, Cursor cursor, SiSet si, ReadAlignmentNFA automaton,
                 boolean hasMatch, int minK, int priority, SearchState split) {
             this.currentSi = currentSi;
             this.cursor = cursor;
             this.siTable = si;
             this.automaton = automaton;
             this.state = ((ch.code & 0x7) << 5) | ((minK & 0xFF) << 8) | ((priority & 0xFF) << 16)
                     | ((hasMatch ? 1 : 0) << 24);
             this.split = split;
         }
 
         /**
          * Initial forward search state
          * 
          * @param strand
          * @param searchDirection
          */
         public SearchState(int k, SuffixInterval currentSi, Cursor cursor, int priority) {
             this(currentSi, ACGT.N, cursor, fmIndex.initSet(cursor.getSearchDirection()), new ReadAlignmentNFA(k),
                     false, 0, priority, null);
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
             return String.format("%s%s %s%s:%,d %s %s", cursorState(),
                     split == null ? "" : String.format(" split(%s) ", split.cursorState()), currentACGT(),
                     currentSi != null ? currentSi : "", currentSi != null ? currentSi.range() : fmIndex.textSize(),
                     getUpdateFlag(), siTable);
         }
 
         public String cursorState() {
             return String.format("%sk%dp%d%s", hasHit() ? "*" : "", getLowerBoundOfK(), getPriority(), cursor);
         }
 
         public int score() {
             int numSplits = getNumSplit();
             int nm = getLowerBoundOfK() - numSplits;
             int mm = cursor.getProcessedBases() - nm;
             int score = mm * config.matchScore - nm * config.mismatchPenalty - numSplits * config.splitOpenPenalty;
             if (split == null)
                 return score;
             else
                 return score + split.score();
         }
 
         public int upperBoundOfScore() {
             int numSplits = getNumSplit();
             int nm = getLowerBoundOfK() - numSplits;
             int mm = cursor.getProcessedBases() + cursor.getRemainingBases() - nm;
             int score = mm * config.matchScore - nm * config.mismatchPenalty - numSplits * config.splitOpenPenalty;
             if (split == null)
                 return score;
             else
                 return score + split.upperBoundOfScore();
         }
 
         public SearchState nextStateAfterSplit(int k) {
             updateSplitFlag();
             // use the same automaton state
             int minK = getLowerBoundOfK();
             if (minK < k) {
                 Cursor[] newCursor = cursor.split();
 
                 SearchState newState = new SearchState(currentSi, currentACGT(), newCursor[0], siTable,
                         automaton.nextStateAfterSplit(k), hasHit(), minK, getPriority(), null);
                 newState.split = new SearchState(null, ACGT.N, newCursor[1], fmIndex.initSet(newCursor[1]
                         .getSearchDirection()), new ReadAlignmentNFA(k).activateDiagonalStates(), false, minK,
                         getPriority(), null);
                 return newState;
             }
             else
                 return null;
         }
 
         public SearchState nextState(ACGT ch, SiSet nextSi, QueryMask queryMask, StaircaseFilter staircaseFilter) {
 
             NextState next = automaton.nextState(cursor, ch, queryMask, staircaseFilter);
             if (next == null)
                 return null; // no match
 
             int nextMinK = next.nextState.kOffset;
             Cursor nextCursor = cursor.next();
             SuffixInterval si = nextCursor.isForwardSearch() ? this.siTable.getForward(ch) : this.siTable
                     .getBackward(ch);
             return new SearchState(si, ch, nextCursor, nextSi, next.nextState, next.hasMatch, nextMinK, getPriority(),
                     split);
         }
 
         /**
          * Update with new state
          * 
          * @param oldState
          * @param newState
          * @return
          */
         public SearchState update(SearchState oldState, SearchState newState) {
             if (oldState == this) {
                 return newState;
             }
             else {
                 SearchState prev = this;
                 while (prev.split != oldState) {
                     prev = prev.split;
                     if (prev == null)
                         return null;
                 }
                 prev.split = newState;
                 return this;
             }
         }
 
     }
 
 }
