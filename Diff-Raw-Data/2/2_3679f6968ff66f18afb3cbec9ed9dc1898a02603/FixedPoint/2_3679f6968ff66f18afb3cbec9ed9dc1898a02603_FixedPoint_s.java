 package de.bioinformatikmuenchen.pg4.alignment;
 
 import de.bioinformatikmuenchen.pg4.alignment.gap.ConstantGapCost;
 import de.bioinformatikmuenchen.pg4.alignment.gap.IGapCost;
 import de.bioinformatikmuenchen.pg4.alignment.io.IDPMatrixExporter;
 import de.bioinformatikmuenchen.pg4.common.Sequence;
 import de.bioinformatikmuenchen.pg4.common.alignment.AlignmentResult;
 import de.bioinformatikmuenchen.pg4.common.distance.IDistanceMatrix;
 
 /**
  *
  * @author harrert
  */
 public class FixedPoint extends AlignmentProcessor{
     
     private double[][] matrixA;
     private double[][] matrixInA;
     private double[][] matrixDelA;
     private double[][] matrixB;
     private double[][] matrixInB;
     private double[][] matrixDelB;
     private int xSize = -1;
     private int ySize = -1;
     private String querySequence;
     private String targetSequence;
     private String querySequenceId;
     private String targetSequenceId;
     private double[][] fixedPointMatrix;
     
     public FixedPoint(AlignmentAlgorithm algorithm, IDistanceMatrix distanceMatrix, IGapCost gapCost) {
         super(AlignmentMode.GLOBAL, algorithm, distanceMatrix, gapCost);
         assert gapCost instanceof ConstantGapCost;
         //AlignmentResult result = new AlignmentResult();
     }
 
     @Override
     public AlignmentResult align(Sequence seq1, Sequence seq2) {
         this.xSize = seq1.getSequence().length();
         this.ySize = seq2.getSequence().length();
         this.querySequence = seq1.getSequence();
         this.targetSequence = seq2.getSequence();
         this.querySequenceId = seq1.getId();
         this.targetSequenceId = seq2.getId();
     }
     
     public void initAndFillNeedlemanWunsch(){
         //initialize:
         matrixA = new double[xSize][ySize];
         matrixB = new double[xSize][ySize];
         for (int i = 1; i < xSize; i++) {
             matrixA[i][0] = gapCost.getGapCost(i);
             matrixB[i][0] = gapCost.getGapCost(i);
         }
         for (int i = 0; i < ySize; i++) {
             matrixA[0][i] = gapCost.getGapCost(i);
             matrixB[0][i] = gapCost.getGapCost(i);
         }
         //fill:
         String queryReverse = new StringBuilder(querySequence).reverse().toString();
         String targetReverse = new StringBuilder(targetSequence).reverse().toString();
         for (int x = 1; x < xSize; x++) {
             for (int y = 0; y < ySize; y++) {
                 char A_fwd = querySequence.charAt(x-1);
                 char B_fwd = targetSequence.charAt(x-1);
                 char A_rev = queryReverse.charAt(x-1);
                 char B_rev = targetReverse.charAt(x-1);
                 matrixA[x][y] = Math.max(matrixA[x-1][y-1] + distanceMatrix.distance(A_fwd, B_fwd), Math.max(matrixA[x-1][y]+gapCost.getGapCost(1), matrixA[x][y-1]+gapCost.getGapCost(1)));
                 matrixB[x][y] = Math.max(matrixB[x-1][y-1] + distanceMatrix.distance(A_rev, B_rev), Math.max(matrixB[x-1][y]+gapCost.getGapCost(1), matrixB[x][y-1]+gapCost.getGapCost(1)));
             }
         }
     }
     
     public void initAndFillGotoh(){
         //init:
         int x = xSize+1;
         int y = ySize+1;
         //Create the matrices
         matrixA = new double[x][y];
         matrixInA = new double[x][y];
         matrixDelA = new double[x][y];
         matrixB = new double[x][y];
         matrixDelA[0][0] = Double.NEGATIVE_INFINITY;
         matrixDelB[0][0] = Double.NEGATIVE_INFINITY;
         matrixInA[0][0] = Double.NEGATIVE_INFINITY;
         matrixInB[0][0] = Double.NEGATIVE_INFINITY;
         for (int i = 1; i < x; i++) {
             matrixA[i][0] = gapCost.getGapCost(i);
             matrixB[i][0] = gapCost.getGapCost(i);
         }
         for (int i = 1; i < y; i++) {
             matrixA[0][i] = gapCost.getGapCost(i);
             matrixB[0][i] = gapCost.getGapCost(i);
         }
         for (int i = 1; i < x; i++) {
             matrixInA[i][0] = Double.NEGATIVE_INFINITY;
             matrixInB[i][0] = Double.NEGATIVE_INFINITY;
             matrixDelA[i][0] = Double.NEGATIVE_INFINITY;
             matrixDelB[i][0] = Double.NEGATIVE_INFINITY;
         }
         for (int i = 1; i < y; i++) {
             matrixInA[0][i] = Double.NEGATIVE_INFINITY;
             matrixInB[0][i] = Double.NEGATIVE_INFINITY;
             matrixDelA[0][i] = Double.NEGATIVE_INFINITY;
             matrixDelB[0][i] = Double.NEGATIVE_INFINITY;
         }
         //fill
         String queryReverse = new StringBuilder(querySequence).reverse().toString();
         String targetReverse = new StringBuilder(targetSequence).reverse().toString();
         for (x = 1; x < xSize + 1; x++) {
             for (y = 1; y < ySize + 1; y++) {
                 matrixInA[x][y] = Math.max(matrixA[x - 1][y] + gapCost.getGapCost(1), matrixInA[x - 1][y] + gapCost.getGapExtensionPenalty(0, 1));
                 matrixInB[x][y] = Math.max(matrixB[x - 1][y] + gapCost.getGapCost(1), matrixInB[x - 1][y] + gapCost.getGapExtensionPenalty(0, 1));
                 matrixDelA[x][y] = Math.max(matrixA[x][y - 1] + gapCost.getGapCost(1), matrixDelA[x][y - 1] + gapCost.getGapExtensionPenalty(0, 1));
                 matrixDelB[x][y] = Math.max(matrixB[x][y - 1] + gapCost.getGapCost(1), matrixDelB[x][y - 1] + gapCost.getGapExtensionPenalty(0, 1));
                 matrixA[x][y] = Math.max(Math.max(matrixInA[x][y], matrixDelA[x][y]), matrixA[x - 1][y - 1] + distanceMatrix.distance(querySequence.charAt(x - 1), targetSequence.charAt(y - 1)));
                 matrixB[x][y] = Math.max(Math.max(matrixInB[x][y], matrixDelB[x][y]), matrixB[x - 1][y - 1] + distanceMatrix.distance(queryReverse.charAt(x - 1), targetReverse.charAt(y - 1)));
             }
         }
     }
     
     public void fixedPointAlignment(){
         fixedPointMatrix = new double[xSize][ySize];
         for (int i = 0; i < xSize; i++) {
             double[] ds = matrixA[i];
             
         }
     }
 
     @Override
     public void writeMatrices(IDPMatrixExporter exporter) {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
     
 }
