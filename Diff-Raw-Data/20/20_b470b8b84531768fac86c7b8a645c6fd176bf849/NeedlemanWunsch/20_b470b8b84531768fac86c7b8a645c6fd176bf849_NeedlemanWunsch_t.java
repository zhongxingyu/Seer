 package de.bioinformatikmuenchen.pg4.alignment;
 
import com.google.common.collect.Lists;
 import de.bioinformaikmuenchen.pg4.common.alignment.AlignmentResult;
 import de.bioinformaikmuenchen.pg4.common.alignment.SequencePairAlignment;
 import de.bioinformaikmuenchen.pg4.common.distance.IDistanceMatrix;
 import de.bioinformatikmuenchen.pg4.alignment.gap.ConstantGapCost;
 import de.bioinformatikmuenchen.pg4.alignment.gap.IGapCost;
 import de.bioinformatikmuenchen.pg4.alignment.io.IAlignmentOutputFormatter;
 import de.bioinformatikmuenchen.pg4.common.Sequence;
import java.util.Collections;
 import java.util.LinkedList;
 
 public class NeedlemanWunsch extends AlignmentProcessor {
 
     private double[][] matrix;
     //Matrices that save whether any given field got its value from the specified direction
     private boolean[][] leftTopArrows;
     private boolean[][] leftArrows;
     private boolean[][] topArrows;
     private int xSize = -1;
     private int ySize = -1;
     private String seq1;
     private String seq2;
 
     @Override
     public AlignmentResult align(Sequence seq1, Sequence seq2) {
         initMatrix(seq1.getSequence().length(), seq2.getSequence().length());
         fillMatrix(seq1.getSequence(), seq2.getSequence());
         AlignmentResult result = new AlignmentResult();
        //Calculate the alignment and add it to the result
        result.setAlignments(Collections.singletonList(oneAlignmentOnly()));
         return result;
     }
 
     /**
      * Initialize an alignment processor with a score-only output formatter
      *
      * @param mode
      * @param algorithm
      */
     public NeedlemanWunsch(AlignmentMode mode, AlignmentAlgorithm algorithm, IDistanceMatrix distanceMatrix, IGapCost gapCost) {
         super(mode, algorithm, distanceMatrix, gapCost);
         //AlignmentResult result = new AlignmentResult();
     }
 
     public NeedlemanWunsch(AlignmentMode mode, AlignmentAlgorithm algorithm, IDistanceMatrix distanceMatrix, IGapCost gapCost, IAlignmentOutputFormatter outputFormatter) {
         super(mode, algorithm, distanceMatrix, gapCost, outputFormatter);
         assert gapCost instanceof ConstantGapCost : "Classic Needleman Wunsch can't use affine gap cost";
         assert algorithm == AlignmentAlgorithm.NEEDLEMAN_WUNSCH;
     }
 
     public void initMatrix(int xSize, int ySize) {
         this.xSize = xSize;
         this.ySize = ySize;
         matrix = new double[xSize][ySize];
         leftArrows = new boolean[xSize][ySize];
         leftTopArrows = new boolean[xSize][ySize];
         topArrows = new boolean[xSize][ySize];
         for (int i = 0; i < xSize; i++) {
             matrix[i][0] = gapCost.getGapCost(i);
         }
         for (int i = 0; i < ySize; i++) {
             matrix[0][i] = gapCost.getGapCost(i);
         }
         //EXPLICITLY init the matrices
         for (int x = 0; x < xSize; x++) {
             for (int y = 0; y < ySize; y++) {
                 leftArrows[x][y] = false;
                 leftTopArrows[x][y] = false;
                 topArrows[x][y] = false;
             }
         }
     }
 
     public void fillMatrix(String seq1, String seq2) {
         final double compareThreshold = 0.0000001;
         for (int x = 1; x < xSize; x++) {
             for (int y = 1; y < ySize; y++) {
                 char A = seq1.charAt(x - 1);
                 char B = seq2.charAt(y - 1);
                 double leftTopScore = matrix[x - 1][y - 1] + distanceMatrix.distance(A, B);
                 double leftScore = matrix[x - 1][y] + gapCost.getGapCost(1);
                 double topScore = matrix[x][y - 1] + gapCost.getGapCost(1);
                 //Calculate the max score
                 matrix[x][y] = Math.max(leftTopScore,
                         Math.max(leftScore, topScore));
                 double maxScore = matrix[x][y];
                 //Check which 'arrows' are set for the current field
                 leftTopArrows[x][y] = Math.abs(leftTopScore - maxScore) < compareThreshold;
                 leftArrows[x][y] = Math.abs(leftScore - maxScore) < compareThreshold;
                 topArrows[x][y] = Math.abs(topScore - maxScore) < compareThreshold;
                 //Assert this field has at least one arrow
                 assert leftTopArrows[x][y] || leftArrows[x][y] || topArrows[x][y];
             }
         }
     }
 
     public String printMatrix(boolean sysout) {
         StringBuffer stringBuffer = new StringBuffer();
         for (int x = 0; x < xSize; x++) {
             for (int y = 0; y < ySize; y++) {
                 stringBuffer.append(matrix[x][y]).append("\t");
             }
             stringBuffer.append("\n");
         }
         if (sysout) {
             System.out.println(stringBuffer.toString());
         }
         return stringBuffer.toString();
     }
 
     public SequencePairAlignment oneAlignmentOnly() {
         SequencePairAlignment spa = new SequencePairAlignment();
         int x = xSize-1;
         int y = ySize-1;
         for (int i = 0; i < seq1.length()-1 + seq2.length()-1; i++) {
             if (leftTopArrows[x][y]) {
                 spa.queryAlignment += seq1.charAt(x - 1);
                 spa.targetAlignment += seq2.charAt(y - 1);
                 x--; y--;
             }
             if (leftArrows[x][y]) {
                 spa.queryAlignment += seq1.charAt(x - 1);
                 spa.targetAlignment += '-';
                 y--;
             }
             if (topArrows[x][y]) {
                 spa.queryAlignment += '-';
                 spa.targetAlignment += seq2.charAt(y - 1);
                 x--;
             }
         }
         assert(x==0 && y==0);
         return spa;
     }
 }
