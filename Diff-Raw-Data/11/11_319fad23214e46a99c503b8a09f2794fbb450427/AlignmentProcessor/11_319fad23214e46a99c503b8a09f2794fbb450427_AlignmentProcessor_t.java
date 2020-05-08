 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package de.bioinformatikmuenchen.pg4.alignment;
 
 import de.bioinformatikmuenchen.pg4.common.alignment.AlignmentResult;
 import de.bioinformatikmuenchen.pg4.common.Sequence;
 import de.bioinformatikmuenchen.pg4.common.distance.IDistanceMatrix;
 import de.bioinformatikmuenchen.pg4.alignment.gap.IGapCost;
 import de.bioinformatikmuenchen.pg4.alignment.io.IAlignmentOutputFormatter;
 import de.bioinformatikmuenchen.pg4.alignment.io.ScoreOnlyAlignmentOutputFormatter;
 
 /**
 * An object that can process alignments. AlignmentProcessors may not save
 * state, align() calls shall be expected more than once per instance.
 *
  * @author koehleru
  */
 public abstract class AlignmentProcessor {
 
     protected AlignmentMode mode;
     protected AlignmentAlgorithm algorithm;
     protected IAlignmentOutputFormatter outputFormatter;
     protected IDistanceMatrix distanceMatrix;
     protected IGapCost gapCost;
 
     public AlignmentProcessor() {
     }

     public AlignmentProcessor(AlignmentMode mode, AlignmentAlgorithm algorithm, IDistanceMatrix distanceMatrix, IGapCost gapCost, IAlignmentOutputFormatter outputFormatter) {
         this.mode = mode;
         this.algorithm = algorithm;
         this.gapCost = gapCost;
         this.distanceMatrix = distanceMatrix;
         this.outputFormatter = outputFormatter;
     }
 
     /**
      * Initialize an alignment processor with a score-only output formatter
      *
      * @param mode
      * @param algorithm
      */
     public AlignmentProcessor(AlignmentMode mode, AlignmentAlgorithm algorithm, IDistanceMatrix distanceMatrix, IGapCost gapCost) {
         this.mode = mode;
         this.algorithm = algorithm;
         this.gapCost = gapCost;
         this.distanceMatrix = distanceMatrix;
         this.outputFormatter = new ScoreOnlyAlignmentOutputFormatter();
     }
 
     /**
      * Align two sequences
      *
      * @param seq1
      * @param seq2
      * @return
      */
     public abstract AlignmentResult align(Sequence seq1, Sequence seq2);
 
    public abstract double[][] getMatrix();

     /**
      * Aligns two sequences and formats the
      */
     public String alignAndFormat(Sequence seq1, Sequence seq2) {
         return outputFormatter.format(align(seq1, seq2));
     }
 }
