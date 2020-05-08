 package de.bioinformatikmuenchen.pg4.alignment;
 
 import de.bioinformatikmuenchen.pg4.alignment.gap.ConstantGapCost;
 import de.bioinformatikmuenchen.pg4.alignment.gap.IGapCost;
 import de.bioinformatikmuenchen.pg4.alignment.io.DPMatrixExporter;
 import de.bioinformatikmuenchen.pg4.alignment.io.IAlignmentOutputFormatter;
 import de.bioinformatikmuenchen.pg4.alignment.io.IDPMatrixExporter;
 import de.bioinformatikmuenchen.pg4.common.Sequence;
 import de.bioinformatikmuenchen.pg4.common.alignment.AlignmentResult;
 import de.bioinformatikmuenchen.pg4.common.alignment.SequencePairAlignment;
 import de.bioinformatikmuenchen.pg4.common.distance.IDistanceMatrix;
 import java.io.IOException;
 import java.util.Collections;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author tobias
  */
 public class Gotoh extends AlignmentProcessor {
 
     private double[][] matrixA;
     private double[][] matrixIn;
     private double[][] matrixDel;
     private double score;
     private int xSize = -1;
     private int ySize = -1;
     private String querySequence;
     private String targetSequence;
     private String querySequenceId;
     private String targetSequenceId;
     private boolean freeshift = false;
     private boolean local = false;
     boolean[][] leftPath;
     boolean[][] leftTopPath;
     boolean[][] topPath;
     boolean[][] hasPath;
 
     public Gotoh(AlignmentMode mode, AlignmentAlgorithm algorithm, IDistanceMatrix distanceMatrix, IGapCost gapCost) {
         super(mode, algorithm, distanceMatrix, gapCost);
         assert algorithm == AlignmentAlgorithm.GOTOH;
         //AlignmentResult result = new AlignmentResult();
     }
 
     public Gotoh(AlignmentMode mode, AlignmentAlgorithm algorithm, IDistanceMatrix distanceMatrix, IGapCost gapCost, IAlignmentOutputFormatter outputFormatter) {
         super(mode, algorithm, distanceMatrix, gapCost, outputFormatter);
         assert gapCost instanceof ConstantGapCost : "Classic Needleman Wunsch can't use affine gap cost";
         assert algorithm == AlignmentAlgorithm.NEEDLEMAN_WUNSCH;
     }
 
     @Override
     public AlignmentResult align(Sequence seq1, Sequence seq2) {
         assert seq1 != null && seq2 != null;
         assert seq1.getSequence().length() > 0;
         assert seq2.getSequence().length() > 0;
         this.querySequence = seq1.getSequence();
         this.targetSequence = seq2.getSequence();
         this.querySequenceId = seq1.getId();
         this.targetSequenceId = seq2.getId();
         this.xSize = querySequence.length();
         this.ySize = targetSequence.length();
         AlignmentResult result = new AlignmentResult();
         initMatrix(seq1.getSequence().length(), seq2.getSequence().length());
         fillMatrix(seq1.getSequence(), seq2.getSequence(), result);////////////////////// SCORE übergeben!
         this.score = matrixA[xSize - 1][ySize - 1];
         //Calculate the alignment and add it to the result
         if (mode == AlignmentMode.GLOBAL) {
             result.setAlignments(Collections.singletonList(backTrackingGlobal()));
         } else if (mode == AlignmentMode.LOCAL) {
             result.setAlignments(Collections.singletonList(backTrackingLocal()));
         } else if (mode == AlignmentMode.FREESHIFT) {
             result.setAlignments(Collections.singletonList(backTrackingFreeShift()));
         } else {
             throw new IllegalArgumentException("Unknown alignment mode: " + mode);
         }
         result.setQuerySequenceId(seq1.getId());
         result.setTargetSequenceId(seq2.getId());
 //        System.out.println(printMatrix());
         return result;
     }
 
     public void initMatrix(int xSize, int ySize) {
         this.xSize = xSize;
         this.ySize = ySize;
         xSize++;
         ySize++;
         //Create the matrices
         matrixA = new double[xSize][ySize];
         matrixIn = new double[xSize][ySize];
         matrixDel = new double[xSize][ySize];
         leftPath = new boolean[xSize][ySize];
         leftTopPath = new boolean[xSize][ySize];
         topPath = new boolean[xSize][ySize];
         hasPath = new boolean[xSize][ySize];
         matrixDel[0][0] = Double.NEGATIVE_INFINITY;//NaN;
         matrixIn[0][0] = Double.NEGATIVE_INFINITY;//NaN;
         if (!(mode == AlignmentMode.FREESHIFT || mode == AlignmentMode.LOCAL)) {// " == if(global)"
             for (int i = 1; i < xSize; i++) {
                 matrixA[i][0] = gapCost.getGapCost(i);
             }
             for (int i = 1; i < ySize; i++) {
                 matrixA[0][i] = gapCost.getGapCost(i);
             }
         }
         for (int i = 1; i < xSize; i++) {
             matrixIn[i][0] = Double.NEGATIVE_INFINITY;
             matrixDel[i][0] = Double.NEGATIVE_INFINITY;//NaN;
         }
         for (int i = 1; i < ySize; i++) {
             matrixIn[0][i] = Double.NEGATIVE_INFINITY;//NaN;
             matrixDel[0][i] = Double.NEGATIVE_INFINITY;
         }
         //init the boolean[][] arrays which store the path taken by the backtracking algorithm
         for (int x = 0; x < xSize; x++) {
             for (int y = 0; y < ySize; y++) {
                 leftPath[x][y] = false;
                 leftTopPath[x][y] = false;
                 topPath[x][y] = false;
                 hasPath[x][y] = false;
             }
         }
     }
 
     public double[] findMaximumInMatrix() {//returns the coordinates (x,y) and entry of the cell with the maximum entry (in this order)
         int x = -1;
         int y = -1;
         double maxCell = Double.NEGATIVE_INFINITY;
         for (int i = 0; i < xSize; i++) {
             for (int j = 0; j < ySize; j++) {
                 if (matrixA[i][j] >= maxCell) {
                     x = i;
                     y = j;
                     maxCell = matrixA[i][j];
                 }
             }
         }
         assert (x >= 0 && y >= 0 && maxCell > Double.NEGATIVE_INFINITY);
         return new double[]{x, y, maxCell};
     }
 
     public void fillMatrix(String seq1, String seq2, AlignmentResult result) {
         assert ((gapCost != null) && (distanceMatrix != null));
         for (int x = 1; x < xSize + 1; x++) {
             for (int y = 1; y < ySize + 1; y++) {
                 matrixIn[x][y] = Math.max(matrixA[x - 1][y] + gapCost.getGapCost(1), matrixIn[x - 1][y] + gapCost.getGapExtensionPenalty(0, 1));
                 matrixDel[x][y] = Math.max(matrixA[x][y - 1] + gapCost.getGapCost(1), matrixDel[x][y - 1] + gapCost.getGapExtensionPenalty(0, 1));
                 matrixA[x][y] = Math.max(Math.max(matrixIn[x][y], matrixDel[x][y]), matrixA[x - 1][y - 1] + distanceMatrix.distance(seq1.charAt(x - 1), seq2.charAt(y - 1)));
             }
         }
         if (mode == AlignmentMode.GLOBAL) {
             result.setScore(matrixA[xSize][ySize]);
         } else if (mode == AlignmentMode.LOCAL) {
             result.setScore(findMaximumInMatrix()[2]);
         }
     }
 
     public SequencePairAlignment backTrackingLocal() {
         StringBuilder queryLine = new StringBuilder();
         StringBuilder targetLine = new StringBuilder();
         //find the cell with the greatest entry:
         double[] maxEntry = findMaximumInMatrix();
         int x = (int) maxEntry[0];
         int y = (int) maxEntry[1];
         double maxCell = maxEntry[2];
        while (matrixA[x][y] != 0 && x > 0 && y > 0) {
             assert (x >= 0 && y >= 0 && maxCell > Double.NEGATIVE_INFINITY);
             while (matrixA[x][y] != 0 && x > 0 && y > 0) {
                 char A = querySequence.charAt(x - 1);
                 char B = targetSequence.charAt(y - 1);
                 if (matrixA[x][y] == matrixA[x - 1][y - 1] + distanceMatrix.distance(A, B)) {
                     leftTopPath[x][y] = true;
                     hasPath[x][y] = true;
                     queryLine.append(A);
                     targetLine.append(B);
                     x--;
                     y--;
                 } else if (matrixA[x][y] == matrixIn[x][y]) {
                     int shift = findK(matrixA[x][y], x, y, true);
                     for (int i = x; i >= (x - shift) && i > 0; i--) {
                         leftPath[i][y] = true;
                         hasPath[i][y] = true;
                         queryLine.append(querySequence.charAt(i - 1));
                         targetLine.append('-');
                     }
                     x -= shift;
                 } else if (matrixA[x][y] == matrixDel[x][y]) {
                     int shift = findK(matrixA[x][y], x, y, false);
                     for (int i = y; i >= (y - shift) && i > 0; i--) {
                         topPath[x][i] = true;
                         hasPath[x][i] = true;
                         queryLine.append('-');
                         targetLine.append(targetSequence.charAt(i - 1));
                     }
                     y -= shift;
                 } else {
                     throw new AlignmentException("No possibility found to move on (indicates a sure failure)");
                 }
             }
         }
         return new SequencePairAlignment(queryLine.reverse().toString(), targetLine.reverse().toString());
     }
 
     public SequencePairAlignment backTrackingFreeShift() {
         return backTrackingGlobal();
     }
 
     public SequencePairAlignment backTrackingGlobal() {
         int x = xSize;
         int y = ySize;
         StringBuilder queryLine = new StringBuilder();
         StringBuilder targetLine = new StringBuilder();
         while (x >= 0 && y >= 0) {//while the rim of the matrix or its left upper corner is not reached
 //            System.out.println("Stuff " + x);
             // => ab hier: x > 0  &&  y > 0
             char A = (x == 0 ? '?' : querySequence.charAt(x - 1));
             char B = (y == 0 ? '?' : targetSequence.charAt(y - 1));
             if (x == 0) {
                 //System.out.println("x==0");
                 while (y > 0) {
                     //System.out.println("x==0 x,y: " + x + ", " + y);
                     topPath[x][y] = true;
                     hasPath[x][y] = true;
                     queryLine.append('-');
                     targetLine.append(targetSequence.charAt(y - 1));
                     y--;
                 }
                 break;
             } else if (y == 0) {
                 //System.out.println("y==0");
                 while (x > 0) {
                     //System.out.println("y==0 left x,y: " + x + ", " + y);
                     leftPath[x][y] = true;
                     hasPath[x][y] = true;
                     queryLine.append(querySequence.charAt(x - 1));
                     targetLine.append('-');
                     x--;
                 }
                 break;
             } else if (Math.abs((matrixA[x][y]) - (matrixA[x - 1][y - 1] + distanceMatrix.distance(A, B))) < 0.0000000001) {//leftTop
                 leftTopPath[x][y] = true;
                 hasPath[x][y] = true;
                 queryLine.append(A);
                 targetLine.append(B);
                 x--;
                 y--;
             } else if (Math.abs(matrixA[x][y] - matrixIn[x][y]) < 0.0000000001) {// Insertion -> to the left
                 int xShift = 1;
                 while (Math.abs(matrixA[x][y] - (matrixA[x - xShift][y] + gapCost.getGapCost(xShift))) > 0.0000000001) {
                     leftPath[x - xShift][y] = true;
                     hasPath[x - xShift][y] = true;
                     queryLine.append(querySequence.charAt(x - xShift - 1));
                     targetLine.append('-');
                     xShift++;
                 }
                 leftPath[x - xShift][y] = true;
                 hasPath[x - xShift][y] = true;
                 queryLine.append(querySequence.charAt(x - 1));
                 targetLine.append('-');
                 x -= xShift;
             } else if (Math.abs(matrixA[x][y] - matrixDel[x][y]) < 0.0000000001) {// Deletion -> to the right
                 int yShift = 1;
                 while (Math.abs(matrixA[x][y] - (matrixA[x][y - yShift] + gapCost.getGapCost(yShift))) > 0.0000000001) {
                     topPath[x][y - yShift] = true;
                     hasPath[x][y - yShift] = true;
                     queryLine.append('-');
                     targetLine.append(targetSequence.charAt(y - yShift - 1));
                     yShift++;
                 }
                 topPath[x][y - yShift] = true;
                 hasPath[x][y - yShift] = true;
                 queryLine.append('-');
                 targetLine.append(targetSequence.charAt(y - 1));
                 y -= yShift;
             } else {
                 throw new AlignmentException("No possibility found to move on (indicates a sure failure)");
             }
         }
         return new SequencePairAlignment(queryLine.reverse().toString(), targetLine.reverse().toString());
     }
 
     private int findK(double entry, int x, int y, boolean insertion) {//not neccessary anymore
         int shift = 0;
         if (insertion) {
             while (x != 0) {
                 if ((matrixA[x - 1][y] + gapCost.getGapCost(shift + 1)) == entry) {
                     shift++;
                     break;
                 } else {
                     shift++;
                     x--;
                 }
             }
         } else {//Deletion
             while (y != 0) {
                 if ((matrixA[x][y - 1] + gapCost.getGapCost(shift + 1)) == entry) {
                     shift++;
                     break;
                 } else {
                     shift++;
                     y--;
                 }
             }
         }
         assert shift > 0;
         return shift;
     }
 
     public boolean setFreeshift(boolean freeshift) {
         this.freeshift = freeshift;
         return this.freeshift;
     }
 
     public boolean setLocal(boolean local) {
         this.local = local;
         return this.local;
     }
 
     public String printMatrix() {
         StringBuilder builder = new StringBuilder();
         builder.append("\t\t");
         for (int x = 0; x < querySequence.length(); x++) {
             builder.append(querySequence.charAt(x)).append("\t");
         }
         builder.append("\n");
         for (int y = 0; y <= targetSequence.length(); y++) {
             builder.append(y == 0 ? ' ' : targetSequence.charAt(y - 1)).append("\t");
             for (int x = 0; x <= querySequence.length(); x++) {
                 builder.append(matrixA[x][y]).append("\t");
             }
             builder.append("\n");
         }
         return builder.toString();
     }
 
     @Override
     public void writeMatrices(IDPMatrixExporter exporter) {
         DPMatrixExporter.DPMatrixInfo info = new DPMatrixExporter.DPMatrixInfo();
         //Set sequences
         info.query = querySequence;
         info.target = targetSequence;
         //Set IDs
         info.queryId = querySequenceId;
         info.targetId = targetSequenceId;
         info.xSize = xSize;
         info.ySize = ySize;
         info.matrix = this.matrixA;
         info.matrixPostfix = "Gotoh alignment matrix";
         try {
             exporter.write(info);
         } catch (IOException ex) {
             Logger.getLogger(Gotoh.class.getName()).log(Level.SEVERE, null, ex);
         }
         info.matrix = this.matrixIn;
         info.matrixPostfix = "Gotoh insertions matrix";
         try {
             exporter.write(info);
         } catch (IOException ex) {
             Logger.getLogger(Gotoh.class.getName()).log(Level.SEVERE, null, ex);
         }
         info.matrix = this.matrixDel;
         info.matrixPostfix = "Gotoh deletions matrix";
         try {
             exporter.write(info);
         } catch (IOException ex) {
             Logger.getLogger(Gotoh.class.getName()).log(Level.SEVERE, null, ex);
         }
         info.score = score;
     }
 }
