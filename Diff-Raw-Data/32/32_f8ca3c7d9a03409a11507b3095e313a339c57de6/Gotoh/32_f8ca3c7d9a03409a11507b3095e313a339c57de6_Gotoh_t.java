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
 import java.text.DecimalFormat;
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
     private String querySequenceStruct;
     private String targetSequenceStruct;
     private boolean freeshift = false;
     private boolean local = false;
     boolean[][] leftPath;
     boolean[][] leftTopPath;
     boolean[][] topPath;
     boolean[][] leftArrows;
     boolean[][] leftTopArrows;
     boolean[][] topArrows;
     boolean[][] hasPath;
     private double[][] secStructMatrix = new double[][]{{2.0, -15.0, -4.0}, {-15.0, 4.0, -4.0}, {-4.0, -4.0, 2.0}};//H-E-C
 
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
         this.querySequenceStruct = seq1.getSs();
         this.targetSequenceStruct = seq2.getSs();
         if (secStructAided) {
             if (querySequence.length() != querySequenceStruct.length()) {
                throw new SSAADataInvalidException("Query sequence length does not match with query SS length, difference (" + querySequence.length() + " vs " + querySequenceStruct.length() + ")");
             } else if (targetSequence.length() != targetSequenceStruct.length()) {
                throw new SSAADataInvalidException("Target sequence length does not match with target SS length, difference (" + querySequence.length() + " vs " + querySequenceStruct.length() + ")");
             }
         }
         AlignmentResult result = new AlignmentResult();
         initMatrix(seq1.getSequence().length(), seq2.getSequence().length());
         fillMatrix(seq1.getSequence(), seq2.getSequence(), result);////////////////////// SCORE übergeben!
         //this.score = matrixA[xSize - 1][ySize - 1];
         //Calculate the alignment and add it to the result
         if (mode == AlignmentMode.GLOBAL) {
             result.setAlignments(Collections.singletonList(backTrackingGlobal(xSize, ySize)));
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
         leftArrows = new boolean[xSize][ySize];
         leftTopArrows = new boolean[xSize][ySize];
         topArrows = new boolean[xSize][ySize];
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
                 leftArrows[x][y] = false;
                 leftTopArrows[x][y] = false;
                 topArrows[x][y] = false;
                 hasPath[x][y] = false;
                 //init the edges
                 if (x == 0 && y != 0) {
                     topArrows[x][y] = true;
                 } else if (y == 0 && x != 0) {
                     leftArrows[x][y] = true;
                 }
             }
         }
     }
 
     public double[] findMaxInMatrixLocal() {//returns the coordinates (x,y) and entry of the cell with the maximum entry (in this order)
         int x = -1;
         int y = -1;
         double maxValue = Double.NEGATIVE_INFINITY;
         for (int i = 0; i <= xSize; i++) {
             for (int j = 0; j <= ySize; j++) {
                 if (matrixA[i][j] > maxValue) {
                     x = i;
                     y = j;
                     maxValue = matrixA[i][j];
                 }
             }
         }
 //        System.out.println("Found max: " + maxValue);
         assert (x >= 0 && y >= 0 && maxValue > Double.NEGATIVE_INFINITY);
         return new double[]{x, y, maxValue};
     }
 
     public double[] findMaxInMatrixFreeShift() {//look for maxEntry only in last column and line
         int x = -1;
         int y = -1;
         double maxValue = Double.NEGATIVE_INFINITY;
         for (int i = 0; i <= xSize; i++) {
             if (matrixA[i][ySize] > maxValue) {
                 maxValue = matrixA[i][ySize];
                 x = i;
                 y = -1;
             }
         }
         //calc last column
         for (int i = 0; i <= ySize; i++) {
             if (matrixA[xSize][i] > maxValue) {
                 maxValue = matrixA[xSize][i];
                 y = i;
                 x = -1;
             }
         }
         assert (((x > -1) || (y > -1)) && (maxValue > Double.NEGATIVE_INFINITY));
         return new double[]{x, y, maxValue};
     }
 
     public double distanceScore(int x, int y) {
         double distance = distanceMatrix.distance(querySequence.charAt(x), targetSequence.charAt(y));
         //Set to 0 if not sec struct aided
         double secStructDistance = (secStructAided ? secStructMatrix[getSecStructIndex(querySequenceStruct.charAt(x))][getSecStructIndex(targetSequenceStruct.charAt(y))] : 0);
         return distance + secStructDistance;
     }
 
     public int getSecStructIndex(char bla) {
         if (bla == 'H') {
             return 0;
         } else if (bla == 'E') {
             return 1;
         } else if (bla == 'C') {
             return 2;
         } else {
             throw new IllegalArgumentException(bla + " is no valid secondary structure specified");
         }
     }
 
     public void fillMatrix(String seq1, String seq2, AlignmentResult result) {
         assert ((gapCost != null) && (distanceMatrix != null));
         for (int x = 1; x <= xSize; x++) {
             for (int y = 1; y <= ySize; y++) {
                 matrixIn[x][y] = Math.max(matrixA[x - 1][y] + gapCost.getGapCost(1), matrixIn[x - 1][y] + gapCost.getGapExtensionPenalty(0, 1));
                 matrixDel[x][y] = Math.max(matrixA[x][y - 1] + gapCost.getGapCost(1), matrixDel[x][y - 1] + gapCost.getGapExtensionPenalty(0, 1));
                 double match = matrixA[x - 1][y - 1] + distanceScore(x - 1, y - 1);
                 double in = matrixIn[x][y];
                 double del = matrixDel[x][y];
                 double max = Math.max(Math.max(in, del), match);
                 if (mode == AlignmentMode.LOCAL) {
                     matrixA[x][y] = Math.max(0, max);
                     leftArrows[x][y] = (Math.abs(max - in) < 0.000000001);
                     leftTopArrows[x][y] = (Math.abs(max - match) < 0.000000001);
                     topArrows[x][y] = (Math.abs(max - del) < 0.000000001);
                 } else {
                     matrixA[x][y] = max;
                     leftArrows[x][y] = (Math.abs(max - in) < 0.000000001);
                     leftTopArrows[x][y] = (Math.abs(max - match) < 0.000000001);
                     topArrows[x][y] = (Math.abs(max - del) < 0.000000001);
                 }
             }
         }
         if (mode == AlignmentMode.GLOBAL) {
             result.setScore(matrixA[xSize][ySize]);
         } else if (mode == AlignmentMode.LOCAL) {
             result.setScore(findMaxInMatrixLocal()[2]);
         } else if (mode == AlignmentMode.FREESHIFT) {
             result.setScore(findMaxInMatrixFreeShift()[2]);
         }
     }
 
     public SequencePairAlignment backTrackingLocal() {
         StringBuilder queryLine = new StringBuilder();
         StringBuilder targetLine = new StringBuilder();
         //find the cell with the greatest entry:
         double[] maxEntry = findMaxInMatrixLocal();
         int x = (int) maxEntry[0];
         int y = (int) maxEntry[1];
         double maxCell = maxEntry[2];
         int yStart = ySize;
         int xStart = xSize;
         while (yStart > y) {
             queryLine.append('-');
             targetLine.append(targetSequence.charAt(yStart - 1));
             yStart--;
         }
         while (xStart > x) {
             targetLine.append('-');
             queryLine.append(querySequence.charAt(xStart - 1));
             xStart--;
         }
         while (matrixA[x][y] > 0.0000000001) {//&& x > 0 && y > 0
             char A = (x == 0 ? '?' : querySequence.charAt(x - 1));//;querySequence.charAt(x - 1);
             char B = (y == 0 ? '?' : targetSequence.charAt(y - 1));
             if (Math.abs(matrixA[x][y] - (matrixA[x - 1][y - 1] + distanceScore(x - 1, y - 1))) < 0.0000000001) {//leftTop
                 leftTopPath[x][y] = true;
                 hasPath[x][y] = true;
                 queryLine.append(A);
                 targetLine.append(B);
                 x--;
                 y--;
             } else if (Math.abs(matrixA[x][y] - matrixIn[x][y]) < 0.0000000001) {//Insertion -> left
                 int xShift = 1;
                 while (Math.abs(matrixA[x][y] - (matrixA[x - xShift][y] + gapCost.getGapCost(xShift))) > 0.0000000001) {
                     leftPath[x - xShift + 1][y] = true;
                     hasPath[x - xShift + 1][y] = true;
                     queryLine.append(querySequence.charAt(x - xShift));
                     targetLine.append('-');
                     xShift++;
                 }
                 leftPath[x - xShift][y] = true;
                 hasPath[x - xShift][y] = true;
                 queryLine.append(querySequence.charAt(x - xShift));
                 targetLine.append('-');
                 x -= xShift;
             } else if (Math.abs(matrixA[x][y] - matrixDel[x][y]) < 0.0000000001) {//Deletion -> right
                 int yShift = 1;
                 while (Math.abs(matrixA[x][y] - (matrixA[x][y - yShift] + gapCost.getGapCost(yShift))) > 0.0000000001) {
                     topPath[x][y - yShift + 1] = true;
                     hasPath[x][y - yShift + 1] = true;
                     queryLine.append('-');
                     targetLine.append(targetSequence.charAt(y - yShift));
                     yShift++;
                 }
                 topPath[x][y - yShift] = true;
                 hasPath[x][y - yShift] = true;
                 queryLine.append('-');
                 targetLine.append(targetSequence.charAt(y - yShift));
                 y -= yShift;
             } else {
                 throw new AlignmentException("No possibility found to move on (indicates a sure failure)");
             }
         }
         while (y > 0) {
             queryLine.append('-');
             targetLine.append(targetSequence.charAt(y - 1));
             y--;
         }
         while (x > 0) {
             targetLine.append('-');
             queryLine.append(querySequence.charAt(x - 1));
             x--;
         }
         return new SequencePairAlignment(queryLine.reverse().toString(), targetLine.reverse().toString());
     }
 
     public SequencePairAlignment backTrackingFreeShift() {
         StringBuilder queryLineFreeShift = new StringBuilder();
         StringBuilder targetLineFreeShift = new StringBuilder();
         double[] maxEntry = findMaxInMatrixFreeShift();
         boolean maxIsInLastColumn = (maxEntry[1] != -1);
         int x = xSize;
         int y = ySize;
         if (maxIsInLastColumn) {
             for (y = ySize; y > maxEntry[1]; y--) {
                 topPath[x][y] = true;
                 hasPath[x][y] = true;
                 queryLineFreeShift.append('-');
                 targetLineFreeShift.append(targetSequence.charAt(y - 1));
             }
         } else {//maxEntry is in the last line
             for (x = xSize; x > maxEntry[0]; x--) {
                 leftPath[x][y] = true;
                 hasPath[x][y] = true;
                 queryLineFreeShift.append(querySequence.charAt(x - 1));
                 targetLineFreeShift.append('-');
             }
         }
         SequencePairAlignment remainingGlobal = backTrackingGlobal(x, y);
         return new SequencePairAlignment(remainingGlobal.queryAlignment + queryLineFreeShift.reverse().toString(), remainingGlobal.targetAlignment + targetLineFreeShift.reverse().toString());
     }
 
     public SequencePairAlignment backTrackingGlobal(int x, int y) {
         //int x = xSize; int y = ySize;
         StringBuilder queryLine = new StringBuilder();
         StringBuilder targetLine = new StringBuilder();
         while (x >= 0 && y >= 0) {//while the rim of the matrix or its left upper corner is not reached
 //            System.out.println("Stuff " + x);
             // => ab hier: x > 0  &&  y > 0
             char A = (x == 0 ? '?' : querySequence.charAt(x - 1));
             char B = (y == 0 ? '?' : targetSequence.charAt(y - 1));
             if (x == 0) {
                 while (y > 0) {
                     topPath[x][y] = true;
                     hasPath[x][y] = true;
                     queryLine.append('-');
                     targetLine.append(targetSequence.charAt(y - 1));
                     y--;
                 }
                 break;
             } else if (y == 0) {
                 while (x > 0) {
                     leftPath[x][y] = true;
                     hasPath[x][y] = true;
                     queryLine.append(querySequence.charAt(x - 1));
                     targetLine.append('-');
                     x--;
                 }
                 break;
             } else if (Math.abs((matrixA[x][y]) - (matrixA[x - 1][y - 1] + distanceScore(x - 1, y - 1))) < 0.0000000001) {//leftTop
                 leftTopPath[x][y] = true;
                 hasPath[x][y] = true;
                 queryLine.append(A);
                 targetLine.append(B);
                 x--;
                 y--;
             } else if (Math.abs(matrixA[x][y] - matrixIn[x][y]) < 0.0000000001) {// Insertion -> to the left --> Gap in target alignment part
                 int xShift = 1;
                 while (Math.abs(matrixA[x][y] - (matrixA[x - xShift][y] + gapCost.getGapCost(xShift))) > 0.0000000001) {
                     leftPath[x - xShift][y] = true;
                     hasPath[x - xShift][y] = true;
                     queryLine.append(querySequence.charAt(x - xShift));
                     targetLine.append('-');
                     xShift++;
                 }
                 leftPath[x - xShift + 1][y] = true;
                 hasPath[x - xShift + 1][y] = true;
                 queryLine.append(querySequence.charAt(x - xShift));
                 targetLine.append('-');
                 x -= xShift;
             } else if (Math.abs(matrixA[x][y] - matrixDel[x][y]) < 0.0000000001) {// Deletion -> to the right --> Gap in query alignment part
                 int yShift = 1;
                 while (Math.abs(matrixA[x][y] - (matrixA[x][y - yShift] + gapCost.getGapCost(yShift))) > 0.0000000001) {
                     topPath[x][y - yShift + 1] = true;
                     hasPath[x][y - yShift + 1] = true;
                     queryLine.append('-');
                     targetLine.append(targetSequence.charAt(y - yShift));
                     yShift++;
                 }
                 topPath[x][y - yShift + 1] = true;
                 hasPath[x][y - yShift + 1] = true;
                 queryLine.append('-');
                 targetLine.append(targetSequence.charAt(y - yShift));
                 y -= yShift;
             } else {
                 throw new AlignmentException("No possibility found to move on (indicates a sure failure)");
             }
         }
         return new SequencePairAlignment(queryLine.reverse().toString(), targetLine.reverse().toString());
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
 //        DecimalFormat format = new DecimalFormat();
 //        format.setMaximumFractionDigits(2);
 //        format.setMinimumIntegerDigits(2);
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
         info.leftArrows = leftArrows;
         info.topArrows = topArrows;
         info.topLeftArrows = leftTopArrows;
         info.topPath = topPath;
         info.leftPath = leftPath;
         info.topLeftPath = leftTopPath;
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
