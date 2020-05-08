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
 // FMIndexOnGenome.java
 // Since: 2011/04/28
 //
 // $URL$ 
 // $Author$
 //--------------------------------------
 package org.utgenome.weaver.align;
 
 import java.io.IOException;
 
 import org.utgenome.UTGBException;
 import org.utgenome.weaver.align.BWTransform.BWT;
 import org.utgenome.weaver.align.SequenceBoundary.PosOnGenome;
 import org.utgenome.weaver.align.strategy.SearchDirection;
 import org.xerial.util.StopWatch;
 import org.xerial.util.log.Logger;
 
 /**
  * FM indexes for forward/reverse genome sequences
  * 
  * @author leo
  * 
  */
 public class FMIndexOnGenome
 {
     private static Logger           _logger    = Logger.getLogger(FMIndexOnGenome.class);
 
     private static final int        windowSize = 128;                                     // Occ table window size 
 
     public final FMIndex            forwardIndex;
     public final FMIndex            reverseIndex;
     private final SparseSuffixArray forwardSA;
     private final SparseSuffixArray reverseSA;
     private final SequenceBoundary  index;
 
     private final long              N;
     private final int               K;
 
     private final SuffixInterval    wholeRange;
     private final SuffixInterval[]  initRange;
 
     public static FMIndexOnGenome load(String fastaFilePrefix) throws UTGBException, IOException {
 
         StopWatch sw = new StopWatch();
         _logger.info("Preparing FM-indexes");
         BWTFiles forwardDB = new BWTFiles(fastaFilePrefix, Strand.FORWARD);
         BWTFiles backwardDB = new BWTFiles(fastaFilePrefix, Strand.REVERSE);
 
         // Load the boundary information of the concatenated chr sequences 
         SequenceBoundary index = SequenceBoundary.load(fastaFilePrefix);
         long N = index.totalSize;
         int K = ACGT.values().length;
 
         // Load sparse suffix arrays
         _logger.debug("Loading sparse suffix arrays");
         SparseSuffixArray forwardSA = SparseSuffixArray.loadFrom(forwardDB.sparseSuffixArray());
         SparseSuffixArray backwardSA = SparseSuffixArray.loadFrom(backwardDB.sparseSuffixArray());
 
         _logger.debug("Loading BWT files");
         ACGTSequence seqF = ACGTSequence.loadFrom(forwardDB.bwt());
         ACGTSequence seqR = ACGTSequence.loadFrom(backwardDB.bwt());
 
         _logger.debug("Constructing Occ Tables");
         FMIndex forwardIndex = new FMIndexOnOccTable(seqF, windowSize);
         FMIndex reverseIndex = new FMIndexOnOccTable(seqR, windowSize);
        _logger.info("done. %.2f sec.", sw.getElapsedTime());
         return new FMIndexOnGenome(forwardIndex, reverseIndex, forwardSA, backwardSA, index, N, K);
     }
 
     private FMIndexOnGenome(FMIndex forwardIndex, FMIndex reverseIndex, SparseSuffixArray forwardSA,
             SparseSuffixArray backwardSA, SequenceBoundary index, long n, int k) {
         this.forwardIndex = forwardIndex;
         this.reverseIndex = reverseIndex;
         this.forwardSA = forwardSA;
         this.reverseSA = backwardSA;
         this.index = index;
         N = n;
         K = k;
         this.wholeRange = new SuffixInterval(0L, N);
         this.initRange = forwardSearch(Strand.FORWARD, wholeRange);
     }
 
     public static FMIndexOnGenome buildFromSequence(String name, String seq) {
         return buildFromSequence(name, new ACGTSequence(seq));
     }
 
     public static FMIndexOnGenome buildFromSequence(String name, ACGTSequence refF) {
         ACGTSequence refR = refF.reverse();
         BWT bwtF = BWTransform.bwt(refF);
         BWT bwtR = BWTransform.bwt(refR);
 
         SequenceBoundary sequenceBoundary = SequenceBoundary.createFromSingleSeq(name, refF);
         FMIndex forwardIndex = new FMIndexOnOccTable(bwtF.bwt, windowSize);
         FMIndex reverseIndex = new FMIndexOnOccTable(bwtR.bwt, windowSize);
         return new FMIndexOnGenome(forwardIndex, reverseIndex, bwtF.ssa, bwtR.ssa, sequenceBoundary, refF.textSize(),
                 ACGT.values().length);
     }
 
     public SuffixInterval wholeSARange() {
         return wholeRange;
     }
 
     public SiSet initSet(SearchDirection d) {
         switch (d) {
         case Forward:
             return new SiSet.ForwardSiSet(initRange);
         case Backward:
             return new SiSet.BackwardSiSet(initRange);
 
         case BidirectionalForward:
         default:
             return new SiSet.BidirectionalSiSet(initRange, initRange);
         }
     }
 
     public long textSize() {
         return N;
     }
 
     public SuffixInterval forwardSearch(Strand strand, ACGT nextBase, SuffixInterval si) {
         FMIndex fm = strand == Strand.FORWARD ? reverseIndex : forwardIndex;
         return fm.backwardSearch(nextBase, si);
     }
 
     public SuffixInterval backwardSearch(Strand strand, ACGT nextBase, SuffixInterval si) {
         FMIndex fm = strand == Strand.FORWARD ? forwardIndex : reverseIndex;
         return fm.backwardSearch(nextBase, si);
     }
 
     public SiSet bidirectionalSearch(Strand strand, SuffixInterval siF, SuffixInterval siB) {
         SuffixInterval[] nextSiF = null, nextSiB = null;
         if (siF != null) {
             // forward search
             FMIndex fm = (strand == Strand.FORWARD) ? reverseIndex : forwardIndex;
             long[] occLowerBound = fm.rankACGTN(siF.lowerBound);
             long[] occUpperBound = fm.rankACGTN(siF.upperBound);
             nextSiF = forwardSearch(fm, occLowerBound, occUpperBound);
 
             if (siB == null)
                 return new SiSet.ForwardSiSet(nextSiF);
 
             {
                 nextSiB = new SuffixInterval[K];
                 // backward search (shrink SA range)
                 for (int i = 0; i < K; ++i) {
                     if (nextSiF[i] == null)
                         continue;
 
                     // Count the occurrences of characters smaller than ACGT[i] in bwt[F.lowerbound, F.upperBound)
                     long x = 0;
                     for (int j = 0; j < i; ++j) {
                         x += occUpperBound[j] - occLowerBound[j];
                     }
                     // Count the occurrences of ACGT[i] in bwt[F.lowerbound, F.upperBound)
                     long y = occUpperBound[i] - occLowerBound[i];
                     // Narrow down the backward suffix interval 
                     nextSiB[i] = new SuffixInterval(siB.lowerBound + x, siB.lowerBound + x + y);
                 }
 
                 return new SiSet.BidirectionalSiSet(nextSiF, nextSiB);
             }
         }
         else if (siB != null) { // F==null
             // backward search 
             FMIndex fm = (strand == Strand.FORWARD) ? forwardIndex : reverseIndex;
             long[] occLowerBound = fm.rankACGTN(siB.lowerBound);
             long[] occUpperBound = fm.rankACGTN(siB.upperBound);
             nextSiB = forwardSearch(fm, occLowerBound, occUpperBound);
 
             return new SiSet.BackwardSiSet(nextSiB);
         }
         return SiSet.empty;
     }
 
     public SuffixInterval[] backwardSearch(Strand strand, SuffixInterval si) {
         FMIndex fm = (strand == Strand.FORWARD) ? forwardIndex : reverseIndex;
         long[] occLowerBound = fm.rankACGTN(si.lowerBound);
         long[] occUpperBound = fm.rankACGTN(si.upperBound);
         return forwardSearch(fm, occLowerBound, occUpperBound);
     }
 
     public SuffixInterval[] forwardSearch(Strand strand, SuffixInterval si) {
         FMIndex fm = (strand == Strand.FORWARD) ? reverseIndex : forwardIndex;
 
         long[] occLowerBound = fm.rankACGTN(si.lowerBound);
         long[] occUpperBound = fm.rankACGTN(si.upperBound);
         return forwardSearch(fm, occLowerBound, occUpperBound);
     }
 
     private SuffixInterval[] forwardSearch(FMIndex fm, long[] occLowerBound, long[] occUpperBound) {
         // forward search
         CharacterCount C = fm.getCharacterCount();
 
         SuffixInterval[] nextSiF = new SuffixInterval[K];
         for (int i = 0; i < K; ++i) {
             ACGT ch = ACGT.decode((byte) i);
             long lb = C.getCharacterCountSmallerThan(ch) + occLowerBound[i];
             long ub = C.getCharacterCountSmallerThan(ch) + occUpperBound[i];
             if (lb < ub)
                 nextSiF[i] = new SuffixInterval(lb, ub);
         }
 
         return nextSiF;
     }
 
     public long toCoordinate(long saIndex, Strand strand, SearchDirection searchDirection) {
         //  strand(+:0,-:1)/search direction(F:0,B:1) -> fmIndex
         //  0/0 -> 1 (reverse index)
         //  0/1 -> 0 (forward index)
         //  1/0 -> 0 (forward index)
         //  1/1 -> 1 (reverse index)
         int fm = ~(strand.index ^ (searchDirection.isForward ? 0 : 1)) & 1;
         if (fm == 0)
             return forwardSA.get(saIndex, forwardIndex);
         else
             return N - reverseSA.get(saIndex, reverseIndex);
     }
 
     public long toCoordinate(long saIndex, Strand strand) {
         long pos = -1;
         switch (strand) {
         case FORWARD:
             long sa = reverseSA.get(saIndex, reverseIndex);
             pos = reverseIndex.textSize() - sa;
             break;
         case REVERSE:
             pos = forwardSA.get(saIndex, forwardIndex);
             break;
         }
         return pos;
     }
 
     public PosOnGenome translate(long pos, Strand strand) throws UTGBException {
         return index.translate(pos, strand);
     }
 
     public PosOnGenome toGenomeCoordinate(long saIndex, long querySize, Strand strand) throws UTGBException {
         long pos = toCoordinate(saIndex, strand);
         if (strand == Strand.FORWARD) {
             pos -= querySize;
         }
         pos += 1;
         if (pos >= 0) {
             PosOnGenome p = index.translate(pos, strand);
             return p;
         }
         return null;
     }
 
     public SequenceBoundary getSequenceBoundary() {
         return index;
     }
 }
