 package org.utgenome.weaver.align.strategy;
 
 import java.util.ArrayList;
 import java.util.PriorityQueue;
 
 import org.utgenome.gwt.utgb.client.bio.IUPAC;
 import org.utgenome.weaver.align.AlignmentSA;
 import org.utgenome.weaver.align.AlignmentScoreConfig;
 import org.utgenome.weaver.align.CharacterCount;
 import org.utgenome.weaver.align.FMIndexOnGenome;
 import org.utgenome.weaver.align.IUPACSequence;
 import org.utgenome.weaver.align.Strand;
 import org.utgenome.weaver.align.SuffixInterval;
 import org.utgenome.weaver.align.record.RawRead;
 import org.utgenome.weaver.align.record.ReadSequence;
 import org.xerial.util.ObjectHandler;
 
 /**
  * Alignment process using FM-Index.
  * 
  * This class implements BWA-style alignment, breadth-first search with pruning
  * the edges.
  * 
  * @author leo
  * 
  */
 public class BWAStrategy
 {
     private final FMIndexOnGenome      fmIndex;
 
     private final int                  numMismatchesAllowed = 1;
     private final AlignmentScoreConfig config               = new AlignmentScoreConfig();
     private final ArrayList<IUPAC>     lettersInGenome      = new ArrayList<IUPAC>();
 
     public BWAStrategy(FMIndexOnGenome fmIndex) {
         this.fmIndex = fmIndex;
 
         CharacterCount C = fmIndex.fmIndexF.getCharacterCount();
         for (IUPAC base : IUPAC.values()) {
             if (base == IUPAC.None)
                 continue;
 
             if (C.getCount(base) > 0) {
                 lettersInGenome.add(base);
             }
         }
     }
 
     public void align(RawRead read, ObjectHandler<AlignmentSA> out) throws Exception {
 
         if (ReadSequence.class.isAssignableFrom(read.getClass())) {
             ReadSequence s = ReadSequence.class.cast(read);
             align(s, out);
         }
     }
 
     /**
      * 
      * @param seq
      * @param cursor
      * @param numMismatchesAllowed
      * @param si
      * @throws Exception
      */
     public void align(ReadSequence read, ObjectHandler<AlignmentSA> out) throws Exception {
         IUPACSequence seq = new IUPACSequence(read.seq);
 
         int minScore = (int) (seq.textSize() - numMismatchesAllowed) * config.matchScore - config.mismatchPenalty
                 * numMismatchesAllowed;
         if (minScore < 0)
             minScore = 1;
 
         PriorityQueue<AlignmentSA> alignmentQueue = new PriorityQueue<AlignmentSA>();
         int bestScore = -1;
 
         long N = fmIndex.fmIndexF.textSize();
         alignmentQueue.add(AlignmentSA.initialState(read.name, seq, Strand.FORWARD, N));
         alignmentQueue.add(AlignmentSA.initialState(read.name, seq.complement(), Strand.REVERSE, N));
 
         while (!alignmentQueue.isEmpty()) {
 
             AlignmentSA current = alignmentQueue.poll();
             if (current.numMismatches > numMismatchesAllowed) {
                 continue;
             }
 
             if (current.wordIndex >= seq.textSize()) {
                 if (current.alignmentScore >= minScore) {
                     if (bestScore < current.alignmentScore)
                         bestScore = current.alignmentScore;
                     out.handle(current);
                 }
                 continue;
             }
 
             // Search for deletion
             alignmentQueue.add(current.extendWithDeletion(config));
 
             IUPAC currentBase = current.common.query.getIUPAC(current.wordIndex);
             // Traverse for each A, C, G, T, ... etc.
             for (IUPAC nextBase : lettersInGenome) {
                 SuffixInterval next = fmIndex.backwardSearch(current.strand, nextBase, current.suffixInterval);
                 if (next.isValidRange()) {
                     // Search for insertion
                    if (current.wordIndex > 0 && current.wordIndex < seq.textSize() - 2) {
                         alignmentQueue.add(current.extendWithInsertion(config));
                     }
                     if ((nextBase.bitFlag & currentBase.bitFlag) != 0) {
                         // match
                         alignmentQueue.add(current.extendWithMatch(next, config));
                     }
                     else {
                         // mismatch
                         alignmentQueue.add(current.extendWithMisMatch(next, config));
                     }
                 }
             }
         }
 
     }
 }
