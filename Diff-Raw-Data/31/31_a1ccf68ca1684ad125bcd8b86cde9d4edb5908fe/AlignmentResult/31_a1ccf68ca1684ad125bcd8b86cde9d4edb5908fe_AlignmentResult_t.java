 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package de.bioinformaikmuenchen.pg4.common.alignment;
 
 import java.util.Collection;
 
 /**
  *
  * @author koehleru
  */
 public class AlignmentResult {
 
     private double score;
     private Collection<SequencePairAlignment> alignments;
 
     public AlignmentResult() {
     }
 
     public AlignmentResult(double score) {
         this.score = score;
     }
 
     public double getScore() {
         return score;
     }
 
     public void setScore(double score) {
         this.score = score;
     }
 
     public AlignmentResult(double score, Collection<SequencePairAlignment> alignments) {
         this.score = score;
         this.alignments = alignments;
     }

    public Collection<SequencePairAlignment> getAlignments() {
        return alignments;
    }

    public void setAlignments(Collection<SequencePairAlignment> alignments) {
        this.alignments = alignments;
    }
 }
