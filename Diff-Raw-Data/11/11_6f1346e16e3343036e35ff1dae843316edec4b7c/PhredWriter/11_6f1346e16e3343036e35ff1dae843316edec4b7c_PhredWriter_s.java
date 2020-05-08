 package edu.toronto.bb.core;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.Collection;
 import java.util.List;
 
 public class PhredWriter {
 
     OutputStream os;
     Collection<PhredScoreSequence> sequences;
     private int scoresPerLine = 60;
     byte[] lineSep = System.getProperty("line.separator").getBytes();
     String delimiter = " "; // single space to separate the socres
 
     public PhredWriter(OutputStream os, Collection<PhredScoreSequence> sequences) {
         this.os = os;
         this.sequences = sequences;
     }
 
     public void setScorePerLine(int scorePerLine) {
         this.scoresPerLine = scorePerLine; 
     }
 
     /**
      * Allow to overwrite the system's line separator
      * @param newLineSeparator
      */
     public void setLineSeparator(String newLineSeparator) {
         this.lineSep = newLineSeparator.getBytes();
     }
 
 
     /**
      * Write the sequence to the OutputStream os
      * @throws IOException 
      */
     public void process() throws IOException {				
         for (PhredScoreSequence sequence : sequences) {			
             buildHeader(sequence);
             buildSequenceContents(sequence);            
         }
     }
 
     private void buildHeader(PhredScoreSequence sequence) throws IOException {        
         os.write('>');
         os.write(sequence.getAccession().getID().getBytes());
         os.write(lineSep);
     }
 
     private void buildSequenceContents(PhredScoreSequence sequence) throws IOException {
         List<PhredScore> scores = sequence.getScores();
         int seqSize = scores.size();
         int scoresWrittenSoFar = 0;
 
         for (int i = 0; i < seqSize; i++) {
             scoresWrittenSoFar++;
            if (scoresWrittenSoFar % scoresPerLine == 0) {
                 os.write(scores.get(i).toString().getBytes());
                 os.write(lineSep);
             } else {
                 os.write(scores.get(i).toString().getBytes());
                 os.write(delimiter.getBytes());
             }
 
         }
         os.write(lineSep);  // end of the current sequence
     }
 
 
 
 
 
 }
