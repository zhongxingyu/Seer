 package de.bioinformatikmuenchen.pg4.alignment;
 
 import de.bioinformatikmuenchen.pg4.alignment.gap.IGapCost;
 import de.bioinformatikmuenchen.pg4.alignment.io.IDPMatrixExporter;
 import de.bioinformatikmuenchen.pg4.common.Sequence;
 import de.bioinformatikmuenchen.pg4.common.alignment.AlignmentResult;
 import de.bioinformatikmuenchen.pg4.common.distance.IDistanceMatrix;
 import java.io.BufferedInputStream;
 import java.io.BufferedWriter;
 import java.io.ByteArrayInputStream;
 import java.io.DataInputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 //import for gnuplot:
 
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
     
     public FixedPoint(AlignmentMode mode, AlignmentAlgorithm algorithm, IDistanceMatrix distanceMatrix, IGapCost gapCost) {
         super(mode, algorithm, distanceMatrix, gapCost);
         //assert gapCost instanceof ConstantGapCost;
         //AlignmentResult result = new AlignmentResult();
     }
 
     @Override
     public AlignmentResult align(Sequence seq1, Sequence seq2) {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
     
     public void makePlot(Sequence seq1, Sequence seq2, boolean minAsThreshold, String path){
         //initialize some of the the declared global variables:
         this.xSize = seq1.getSequence().length();
         this.ySize = seq2.getSequence().length();
         this.querySequence = seq1.getSequence();
         this.targetSequence = seq2.getSequence();
         this.querySequenceId = seq1.getId();
         this.targetSequenceId = seq2.getId();
         //initialize and fill the matrices A and B, depending on Gotoh or NW:
         if(algorithm == AlignmentAlgorithm.NEEDLEMAN_WUNSCH){
             initAndFillNeedlemanWunsch();
         }
         else if(algorithm == AlignmentAlgorithm.GOTOH){
             initAndFillGotoh();
         }
         else{
             throw new UnsupportedOperationException("Fixed point alignments are implemented for global scoring matrices only");
         }
         //do the fixed point alignment:
         fixedPointAlignment();
         AlignmentResult ret = new AlignmentResult();
         //return ret;
         double[] minMax = getMinMaxAverage();
         //put matrix to file as inout for gnuplot:
        String s = matrixToString();
         putToFile(matrixToString(), "./matrix.txt");
         String gnuPlot = "set terminal png\n" +
        "set output \""+path+"/fpa_"+seq1.getId()+"_"+seq2.getId()+".png\"\n" +
         "set size ratio 0.5\n" +
         "set title \"Fixed Point Alignment"+seq1.getId()+" vs. "+seq2.getId()+"\"\n" +
         "\n" +
         "set xlabel \""+seq1.getId()+"\"\n" +
         "set ylabel \""+seq2.getId()+"\"\n" +
         "\n" +
         "set tic scale 0\n" +
         "\n" +
         "set palette rgbformulae 22,13,10\n" +
         "set palette negative\n" +
         "\n" +
         "set cbrange ["+(minAsThreshold ? minMax[0] : minMax[2])+":"+minMax[1]+"]\n" +//decides the minimum threshold for the plot (min or average value of fpaMatrix)
         "#unset cbtics\n" +
         "\n" +
         "set xrange [0:"+xSize+"]\n" +
         "set yrange [0:"+ySize+"]\n" +
         "\n" +
         "set view map\n" +
         "\n" +
         "splot 'matrix.txt' matrix with image";
         putToFile(gnuPlot, "./plot.gp");
         Runtime rt = Runtime.getRuntime();
         try {
             rt.exec(new String[]{"mkdir",path});
             rt.exec(new String[]{"gnuplot","plot.gp"});
         } catch (IOException ex) {
             Logger.getLogger(FixedPoint.class.getName()).log(Level.SEVERE, null, ex);
         }
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
             for (int y = 1; y < ySize; y++) {
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
         matrixInB = new double[x][y];
         matrixDelB = new double[x][y];
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
         double [][] fixedPointMatrixTemp = new double[xSize+1][ySize+1];
         for (int i = 1; i <= xSize; i++) {
             for (int j = 1; j <= ySize; j++) {
                 fixedPointMatrixTemp[i][j] = (matrixA[i-1][j-1] + distanceMatrix.distance(querySequence.charAt(i-1), targetSequence.charAt(j-1))+ matrixB[xSize-i][ySize-j]);
             }
         }
         fixedPointMatrix = new double[xSize][ySize];
         //put values to fixedPointMatrix, since fPMTemp contains 0s in first line and column
         for (int i = 0; i < xSize; i++) {
             for (int j = 0; j < ySize; j++) {
                 fixedPointMatrix[i][j] = fixedPointMatrixTemp[i+1][j+1];
             }
         }
     }
     
     public double[] getMinMaxAverage(){
         double min = Double.POSITIVE_INFINITY;
         double max = Double.NEGATIVE_INFINITY;
         double average = 0;
         for (int i = 0; i < xSize; i++) {
             for (int j = 0; j < ySize; j++) {
                 average += fixedPointMatrix[i][j];
                 min = (fixedPointMatrix[i][j] < min ? fixedPointMatrix[i][j] : min);
                 max = (fixedPointMatrix[i][j] > max ? fixedPointMatrix[i][j] : max);
             }
         }
         assert min<Double.POSITIVE_INFINITY && max>Double.NEGATIVE_INFINITY;
         return new double[]{min, max, average/((double)xSize*ySize)};
     }
     
     public String matrixToString(){
         StringBuilder out = new StringBuilder();
         for (int i = 0; i < xSize; i++) {
             for (int j = 0; j < ySize; j++) {
                 out.append(fixedPointMatrix[i][j]).append("\t");
             }
             out.append("\n");
         }
         return out.toString();
     }
     
     public void putToFile(String input,String path){
 		byte[] bytes = input.getBytes();
 		InputStream is = new ByteArrayInputStream(bytes);
 		try {
 			DataInputStream dInStream = new DataInputStream(new BufferedInputStream(is));
 			BufferedWriter bw = new BufferedWriter(new FileWriter(path));
 			while((input = dInStream.readLine())!=null){
 				bw.append(input);
 				bw.newLine();
 			}
 			bw.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		finally{
 			try {
 				is.close();
 			} catch (IOException e2) {
 				e2.printStackTrace();
 			}
 		}
 	}
 
     @Override
     public void writeMatrices(IDPMatrixExporter exporter) {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
 }
