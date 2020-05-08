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
 import org.utgenome.weaver.align.record.AlignmentRecord;
 import org.utgenome.weaver.align.strategy.Alignment;
 import org.xerial.lens.SilkLens;
 import org.xerial.util.ObjectHandler;
 import org.xerial.util.log.Logger;
 
 public class FMIndexOnGenome
 {
     private static Logger           _logger    = Logger.getLogger(FMIndexOnGenome.class);
 
     private static final int        windowSize = 64;                                     // Occ table window size 
 
     public final FMIndex            forwardIndex;
     public final FMIndex            reverseIndex;
     private final SparseSuffixArray forwardSA;
     private final SparseSuffixArray backwardSA;
     private final SequenceBoundary  index;
 
     private final long              N;
     private final int               K;
 
     public FMIndexOnGenome(String fastaFilePrefix) throws UTGBException, IOException {
 
         _logger.info("Preparing FM-indexes");
         BWTFiles forwardDB = new BWTFiles(fastaFilePrefix, Strand.FORWARD);
         BWTFiles backwardDB = new BWTFiles(fastaFilePrefix, Strand.REVERSE);
 
         // Load the boundary information of the concatenated chr sequences 
         index = SequenceBoundary.loadSilk(forwardDB.pacIndex());
         N = index.totalSize;
         K = ACGT.values().length;
 
         // Load sparse suffix arrays
         _logger.debug("Loading sparse suffix arrays");
         forwardSA = SparseSuffixArray.loadFrom(forwardDB.sparseSuffixArray());
         backwardSA = SparseSuffixArray.loadFrom(backwardDB.sparseSuffixArray());
 
         _logger.debug("Loading BWT files");
         ACGTSequence seqF = ACGTSequence.loadFrom(forwardDB.bwt());
         ACGTSequence seqR = ACGTSequence.loadFrom(backwardDB.bwt());
 
         _logger.debug("Constructing Occ Tables");
         forwardIndex = new FMIndexOnOccTable(seqF, windowSize);
         reverseIndex = new FMIndexOnOccTable(seqR, windowSize);
         _logger.info("done.");
     }
 
     private FMIndexOnGenome(FMIndex forwardIndex, FMIndex reverseIndex, SparseSuffixArray forwardSA,
             SparseSuffixArray backwardSA, SequenceBoundary index, long n, int k) {
         this.forwardIndex = forwardIndex;
         this.reverseIndex = reverseIndex;
         this.forwardSA = forwardSA;
         this.backwardSA = backwardSA;
         this.index = index;
         N = n;
         K = k;
     }
 
     public static FMIndexOnGenome buildFromSequence(String name, String seq) {
         ACGTSequence refF = new ACGTSequence(seq);
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
         return new SuffixInterval(0, N);
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
 
     /**
      * Narrow down suffix range via backward-search
      * 
      * @param strand
      * @param nextBase
      * @param forwardSi
      * @param backwardSi
      * @return new backward Si
      */
     public BidirectionalSuffixInterval bidirectionalForwardSearch(Strand strand, ACGT nextBase,
             BidirectionalSuffixInterval si) {
 
         FMIndex fm;
         SuffixInterval F = si.forwardSi, B = si.backwardSi;
         if (strand == Strand.FORWARD) {
             fm = reverseIndex;
         }
         else {
             fm = forwardIndex;
         }
 
         // forward search
         SuffixInterval nextF = fm.backwardSearch(nextBase, F);
         if (nextF.isEmpty())
             return null;
 
         long x = 0;
         for (ACGT ch : ACGT.values()) {
             if (ch.code < nextBase.code) {
                 x += fm.count(ch, F.lowerBound, F.upperBound);
             }
         }
         long y = fm.count(nextBase, F.lowerBound, F.upperBound);
         SuffixInterval nextB = new SuffixInterval(B.lowerBound + x, B.lowerBound + x + y);
         return new BidirectionalSuffixInterval(nextF, nextB);
     }
 
     public long toForwardSequenceIndex(long saIndex, Strand strand) {
         long pos = -1;
         switch (strand) {
         case FORWARD:
             long sa = backwardSA.get(saIndex, reverseIndex);
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
         long pos = toForwardSequenceIndex(saIndex, strand);
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
 
     public void toGenomeCoordinate(AlignmentSA result, ObjectHandler<AlignmentRecord> reporter) throws Exception {
         if (_logger.isTraceEnabled())
             _logger.info(SilkLens.toSilk("alignment", result));
 
         final long querySize = result.common.query.textSize();
 
         for (long i = result.suffixInterval.lowerBound; i < result.suffixInterval.upperBound; ++i) {
             PosOnGenome p = toGenomeCoordinate(i, querySize, result.strand);
             if (p != null) {
                 AlignmentRecord rec = new AlignmentRecord();
                 rec.chr = p.chr;
                 rec.start = p.pos;
                 rec.strand = result.strand;
                 rec.score = result.alignmentScore;
                 rec.numMismatches = result.numMismatches;
                 // workaround for Picard tools, which cannot accept base character other than ACGT 
                 rec.querySeq = result.strand == Strand.FORWARD ? result.common.query.toString() : result.common.query
                         .reverse().toString();
                 rec.readName = result.common.queryName;
                 rec.end = p.pos + result.wordIndex;
                 rec.setCIGAR(result.cigar().toCIGARString());
                 reporter.handle(rec);
             }
         }
 
     }
 
     public void toGenomeCoordinate(Alignment result, ObjectHandler<AlignmentRecord> reporter) throws Exception {
         if (_logger.isTraceEnabled())
             _logger.info(SilkLens.toSilk("alignment", result));
 
         final long querySize = result.read.textSize();
 
         for (long i = result.si.lowerBound; i < result.si.upperBound; ++i) {
             PosOnGenome p = toGenomeCoordinate(i, querySize, result.strand);
             if (p != null) {
                 AlignmentRecord rec = new AlignmentRecord();
                 rec.chr = p.chr;
                 rec.start = p.pos;
                 rec.strand = result.strand;
                 rec.score = result.score.score;
                 rec.numMismatches = result.score.numMismatches;
                 // workaround for Picard tools, which cannot accept base character other than ACGT 
                 rec.querySeq = result.strand == Strand.FORWARD ? result.read.toString() : result.read.reverse()
                         .toString();
                 // TODO obtain read name
                 rec.readName = result.read.toString();
                 rec.end = p.pos + result.cursorF;
                 // TODO output correct CIGAR string
                 //rec.setCIGAR(result.cigar().toCIGARString());
                 reporter.handle(rec);
             }
         }
 
     }
 
 }
