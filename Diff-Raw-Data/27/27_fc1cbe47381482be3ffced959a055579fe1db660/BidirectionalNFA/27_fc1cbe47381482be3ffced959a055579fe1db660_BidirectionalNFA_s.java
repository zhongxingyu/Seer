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
 // BidirectionalNFA.java
 // Since: Aug 5, 2011
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
 import org.utgenome.weaver.align.FMIndexOnGenome;
 import org.utgenome.weaver.align.Strand;
 import org.utgenome.weaver.align.SuffixInterval;
 import org.utgenome.weaver.parallel.Reporter;
 import org.xerial.lens.SilkLens;
 import org.xerial.util.log.Logger;
 
 /**
  * NFA for forward and backward traversal of suffix arrays
  * 
  * @author leo
  * 
  */
 public class BidirectionalNFA
 {
     private static Logger              _logger   = Logger.getLogger(BidirectionalNFA.class);
 
     private final FMIndexOnGenome      fmIndex;
     private final ACGTSequence         qF;
     private final ACGTSequence         qC;
     private final int                  m;                                                   // query length
     private final AlignmentScoreConfig config;
     private final Reporter             out;
     private int                        bestScore = -1;
     private int                        minMismatches;
 
     private CursorQueue                queue     = new CursorQueue();
     private CursorQueue                nextQueue = new CursorQueue();
 
     private StaircaseFilter            staircaseFilter;
 
     /**
      * Priority queue holding alignment cursors
      */
     private static class CursorQueue extends PriorityQueue<BidirectionalCursor>
     {
         /**
          * 
          */
         private static final long serialVersionUID = 1L;
 
         public CursorQueue() {
             // Prefer the cursor with smaller remaining bases
             super(11, new Comparator<BidirectionalCursor>() {
                 @Override
                 public int compare(BidirectionalCursor o1, BidirectionalCursor o2) {
                     return o1.getRemainingBases() - o2.getRemainingBases();
                 }
             });
         }
 
     }
 
     public BidirectionalNFA(FMIndexOnGenome fmIndex, ACGTSequence query) {
         this(fmIndex, query, new Reporter() {
             @Override
             public void emit(Object result) throws Exception {
                 _logger.debug(SilkLens.toSilk("result", result));
             }
         });
     }
 
     public BidirectionalNFA(FMIndexOnGenome fmIndex, ACGTSequence query, Reporter out) {
         this.fmIndex = fmIndex;
         this.qF = query;
         this.qC = query.complement();
         this.m = (int) query.textSize();
         this.config = new AlignmentScoreConfig();
         this.out = out;
         this.staircaseFilter = new StaircaseFilter(m, config.maximumEditDistances);
         this.minMismatches = config.maximumEditDistances;
     }
 
     //
     //    /**
     //     * Holding states of a row in NFA
     //     * 
     //     * @author leo
     //     * 
     //     */
     //    private static class CursorContainer
     //    {
     //        private List<Optional<List<BidirectionalCursor>>> containerF;
     //        private List<Optional<List<BidirectionalCursor>>> containerR;
     //
     //        public CursorContainer(int m) {
     //            containerF = new ArrayList<Optional<List<BidirectionalCursor>>>(m);
     //            containerR = new ArrayList<Optional<List<BidirectionalCursor>>>(m);
     //            for (int i = 0; i < m; ++i) {
     //                containerF.add(new Optional<List<BidirectionalCursor>>());
     //                containerR.add(new Optional<List<BidirectionalCursor>>());
     //            }
     //        }
     //
     //        private List<Optional<List<BidirectionalCursor>>> getContainer(Strand strand) {
     //            return strand.isForward() ? containerF : containerR;
     //        }
     //
     //        public void add(BidirectionalCursor c) {
     //            Optional<List<BidirectionalCursor>> l = getContainer(c.strand).get(c.getIndex());
     //            if (l.isUndefined()) {
     //                l.set(new ArrayList<BidirectionalCursor>());
     //            }
     //            l.get().add(c);
     //        }
     //
     //        @Override
     //        public String toString() {
     //            return String.format("%s\n%s", containerF, containerR);
     //        }
     //    }
 
     /**
      * @param out
      * @throws Exception
      */
     public void align() throws Exception {
 
         // Row-wise simulation of NFA
         //CursorContainer prevState = new CursorContainer(m + 1);
         //CursorContainer currentState = new CursorContainer(m + 1);
 
         // Add search states for both strand
         {
             BidirectionalCursor initF = new BidirectionalCursor(Score.initial(), qF, Strand.FORWARD,
                     SearchDirection.Forward, ExtensionType.MATCH, fmIndex.wholeSARange(), null, 0, 0, null);
             BidirectionalCursor initC = new BidirectionalCursor(Score.initial(), qC, Strand.REVERSE,
                     SearchDirection.Forward, ExtensionType.MATCH, fmIndex.wholeSARange(), null, 0, 0, null);
             queue.add(initF);
             queue.add(initC);
         }
 
         // k=0;
         while (!queue.isEmpty()) {
             BidirectionalCursor c = queue.poll();
             if (doCutOff(c, 0)) {
                 continue;
             }
 
             // Proceed to next base
             BidirectionalCursor next = next(c);
             if (next != null) {
                 queue.add(next);
             }
         }
 
         // k > 1
         for (int k = 1; k < minMismatches; ++k) {
             // Transit the states to the next row
             _logger.debug("transit k from %d to %d", k - 1, k);
             queue = nextQueue;
             nextQueue = new CursorQueue();
 
             while (!queue.isEmpty()) {
                 BidirectionalCursor c = queue.poll();
                 if (doCutOff(c, k))
                     continue;
 
                 // No more mismatches are allowed in this layer
                 if (c.score.layer() == k) {
                     // search for exact match
                     BidirectionalCursor next = next(c);
                     if (next != null)
                        queue.add(c);
                     continue;
                 }
 
                 {
                     // extend the search with indels 
                     switch (c.extensionType) {
                     case MATCH:
                         if (gapOpenIsAllowed(c)) {
                             // insertion to reference
                             queue.add(extendWithInsertion(c));
                         }
                         // deletion from reference
                         for (ACGT ch : ACGT.exceptN) {
 
                         }
                         // extend the search with read split
                         if (c.score.numSplit < config.numSplitAlowed) {
                             queue.add(extendWithSplit(c));
                         }
                         break;
                     case DELETION:
                         break;
                     case INSERTION:
                         if (c.score.numGapExtend < config.numGapExtensionAllowed) {
                             queue.add(extendWithInsertion(c));
                         }
                         break;
                     }
 
                 }
 
                 {
                     // extend the search with mismatches
                     ACGT nextBase = c.nextACGT();
                     for (ACGT ch : ACGT.exceptN) {
                         if (nextBase == ch)
                             continue;
                         BidirectionalCursor next = next(c, ch);
                        if (next != null)
                             queue.add(next);
                     }
                 }
             }
 
         }
 
         _logger.debug("# of FM-index searches: %,d", this.fmIndexSearchCount);
         _logger.debug("# of exact match searches: %,d", this.exactSearchCount);
         _logger.debug("# of cut off: %,d", this.numCutOff);
 
     }
 
     public boolean gapOpenIsAllowed(BidirectionalCursor c) {
         return c.score.numGapOpens < config.numGapOpenAllowed;
     }
 
     public boolean doCutOff(BidirectionalCursor c, int k) throws Exception {
         if (c.getUpperBoundOfScore(config) < bestScore) {
             // This cursor never produces a better alignment. Skip
             numCutOff++;
             return true;
         }
 
         if (c.getRemainingBases() == 0) {
             // Update the best score
             if (c.score.score > bestScore) {
                 bestScore = c.score.score;
                 int nm = c.score.layer();
                 if (nm < minMismatches) {
                     minMismatches = nm;
                 }
             }
             // Found a match
             out.emit(c);
             return true;
         }
 
         if (k < config.maximumEditDistances) {
             int pos = m - c.getRemainingBases();
             if (staircaseFilter.getStaircaseMask(k + 1).get(pos)) {
                 nextQueue.add(c);
             }
         }
         return false;
     }
 
     Score nextScore(BidirectionalCursor c, ACGT ch) {
         ACGT nextBase = c.nextACGT();
         if (ch == nextBase) {
             return c.score.extendWithMatch(config);
         }
         else
             return c.score.extendWithMismatch(config);
     }
 
     BidirectionalCursor next(BidirectionalCursor c) {
         return next(c, c.nextACGT());
     }
 
     private int fmIndexSearchCount = 0;
     private int exactSearchCount   = 0;
     private int numCutOff          = 0;
 
     BidirectionalCursor extendWithSplit(BidirectionalCursor c) {
         Score nextScore = c.score.extendWithSplit(config);
         CursorBase next = c.cursor.next();
        return new BidirectionalCursor(nextScore, next, ExtensionType.MATCH, c.siF, c.siB, c);
     }
 
     BidirectionalCursor extendWithInsertion(BidirectionalCursor c) {
 
         ExtensionType e = ExtensionType.MATCH;
         CursorBase next = c.cursor.next();
         Score nextScore;
         switch (c.extensionType) {
         case INSERTION:
             e = ExtensionType.INSERTION;
             nextScore = c.score.extendWithGapExtend(config);
             break;
         case MATCH:
         default:
             nextScore = c.score.extendWithGapOpen(config);
             break;
         }
         return new BidirectionalCursor(nextScore, next, e, c.siF, c.siB, c.split);
     }
 
     BidirectionalCursor next(BidirectionalCursor c, ACGT nextBase) {
         fmIndexSearchCount++;
         switch (c.cursor.searchDirection) {
         case Forward: {
             SuffixInterval nextF = fmIndex.forwardSearch(c.strand(), nextBase, c.siF);
             if (nextF.hasEntry()) {
                 // extend the search to forward
                 return new BidirectionalCursor(nextScore(c, nextBase), c.cursor.next(), c.extensionType, nextF, c.siB,
                         c.split);
             }
 
             // switch to bidirectional search when k=0
             if (c.score.layer() == 0) {
                 // Set high priority to the search beginning with this cursor for k>0 
                 return new BidirectionalCursor(Score.initial(), c.cursor.newBidirectionalFowardCursor(),
                         c.extensionType, fmIndex.wholeSARange(), fmIndex.wholeSARange(), c.split);
             }
             else
                 return null;
         }
         case Backward: {
             SuffixInterval nextB = fmIndex.backwardSearch(c.strand(), nextBase, c.siB);
             if (nextB.hasEntry()) {
                 // extend the search for backward
                 return new BidirectionalCursor(nextScore(c, nextBase), c.cursor.next(), c.extensionType, c.siF, nextB,
                         c.split);
             }
 
             // no match
             return null;
         }
         case BidirectionalForward: {
             if (!c.reachedForwardEnd()) {
                 // Bidirectional search
                 BidirectionalSuffixInterval next = fmIndex.bidirectionalForwardSearch(c.strand(), nextBase,
                         new BidirectionalSuffixInterval(c.siF, c.siB));
                 if (next != null) {
                     return new BidirectionalCursor(nextScore(c, nextBase), c.cursor.next(), c.extensionType,
                             next.forwardSi, next.backwardSi, c.split);
                 }
 
                 // Start new bidirectional search
                 if (c.score.layer() == 0) {
                     // Set high priority to the search beginning with this cursor for k>0 
                     return new BidirectionalCursor(Score.initial(), c.cursor.next(), c.extensionType,
                             fmIndex.wholeSARange(), fmIndex.wholeSARange(), c.split);
                 }
                 else
                     return null;
             }
             else {
                 // Switch to backward search
                 SuffixInterval nextB = fmIndex.backwardSearch(c.strand(), nextBase, c.siB);
                 if (nextB.isEmpty()) {
                     return null;
                 }
 
                 return new BidirectionalCursor(nextScore(c, nextBase), c.cursor.next(), c.extensionType, null, nextB,
                         c.split);
             }
         }
         default:
             throw new IllegalStateException("cannot reach here");
         }
 
     }
 
     BidirectionalCursor exactMatch(BidirectionalCursor c) {
         exactSearchCount++;
         while (c != null) {
             if (c.getRemainingBases() == 0)
                 return c;
 
             c = next(c);
         }
         return c;
     }
 }
