 package de.bioinformatikmuenchen.pg4.alignment;
 
 import de.bioinformaikmuenchen.pg4.common.alignment.AlignmentResult;
 import de.bioinformaikmuenchen.pg4.common.Sequence;
 import de.bioinformaikmuenchen.pg4.common.distance.IDistanceMatrix;
 import de.bioinformatikmuenchen.pg4.alignment.gap.ConstantGapCost;
 import de.bioinformatikmuenchen.pg4.alignment.gap.IGapCost;
 import de.bioinformatikmuenchen.pg4.alignment.recursive.io.IAlignmentOutputFormatter;
 
 public class NeedlemanWunsch extends AlignmentProcessor {
 
     private double[][] matrix;
     //Matrices that save whether any given field got its value from the specified direction
     private boolean[][] leftTopArrows;
     private boolean[][] leftArrows;
     private boolean[][] topArrows;
     private int xSize = -1;
     private int ySize = -1;
 
     @Override
     public AlignmentResult align(Sequence seq1, Sequence seq2) {
         initMatrix(seq1.getSequence().length(), seq2.getSequence().length());
         fillMatrix(seq1.getSequence(), seq2.getSequence());
         AlignmentResult result = new AlignmentResult();
         return result;
     }
 
     public NeedlemanWunsch(AlignmentMode mode, AlignmentAlgorithm algorithm, IDistanceMatrix distanceMatrix, IGapCost gapCost, IAlignmentOutputFormatter outputFormatter) {
         super(mode, algorithm, distanceMatrix, gapCost, outputFormatter);
         assert gapCost instanceof ConstantGapCost : "Classic Needleman Wunsch can't use affine gap cost";
         assert algorithm == AlignmentAlgorithm.NEEDLEMAN_WUNSCH;
     }
 
     /**
      * Initialize an alignment processor with a score-only output formatter
      *
      * @param mode
      * @param algorithm
      */
     public NeedlemanWunsch(AlignmentMode mode, AlignmentAlgorithm algorithm, IDistanceMatrix distanceMatrix, IGapCost gapCost) {
         super(mode, algorithm, distanceMatrix, gapCost);
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
 
     public String printMatrix() {
         StringBuilder stringBuilder = new StringBuilder();
         for (int i = 0; i < xSize; i++) {
             for (int j = 0; j < ySize; j++) {
                 stringBuilder.append(matrix[i][j]).append("\t");
             }
             stringBuilder.append("\n");
         }
         System.out.println(stringBuilder.toString());
         return stringBuilder.toString();
     }
 
     public void opt_Alignment(DualString tmp, int i, int j, int[][] M, String s, String t, int Straffaktor, int Matchfaktor, int Mismatchfaktor) {
         String s_ = "";
         String t_ = "";
         String m_ = "";
         DualString tmp2;
        if (this.counter < this.compare_counter)//Hört auf Alignments auszugeben, wenn die vorgegebene Anzahl erreicht wird.
        {
             if (i == 0 && j == 0) {
                this.counter++;
                 System.out.println(tmp.s + "\n" + tmp.m + "\n" + tmp.t);
                this.out.setText(this.out.getText() + "### Alignment " + counter + " ###\n" + tmp.s + "\n" + tmp.m + "\n" + tmp.t + "\n\n");
                 return;
             }
             if (i > 0 && M[i][j] == M[i - 1][j] + Straffaktor) {
                 s_ = s.substring(i - 1, i) + tmp.s;
                 t_ = "-" + tmp.t;
                 m_ = " " + tmp.m;
                 tmp2 = new DualString();
                 tmp2.s = s_;
                 tmp2.t = t_;
                 tmp2.m = m_;
 
                 //System.out.println("####################\n"+tmp2.s +"  "+i+"  "+j+"\n"+ tmp2.m +"\n"+ tmp2.t);
                 this.opt_Alignment(tmp2, i - 1, j, M, s, t, Straffaktor, Matchfaktor, Mismatchfaktor);
             }
             if (j > 0 && M[i][j] == M[i][j - 1] + Straffaktor) {
                 s_ = "-" + tmp.s;
                 t_ = t.substring(j - 1, j) + tmp.t;
                 m_ = " " + tmp.m;
                 tmp2 = new DualString();
                 tmp2.s = s_;
                 tmp2.t = t_;
                 tmp2.m = m_;
                 //System.out.println("####################\n"+tmp2.s +"  "+i+"  "+j+"\n"+ tmp2.m +"\n"+ tmp2.t);
                 this.opt_Alignment(tmp2, i, j - 1, M, s, t, Straffaktor, Matchfaktor, Mismatchfaktor);
             }
             if (i > 0 && j > 0) {
                 if (M[i][j] == M[i - 1][j - 1] + Matchfaktor) {
                     s_ = s.substring(i - 1, i) + tmp.s;
                     t_ = t.substring(j - 1, j) + tmp.t;
                     m_ = "|" + tmp.m;
                     tmp2 = new DualString();
                     tmp2.s = s_;
                     tmp2.t = t_;
                     tmp2.m = m_;
                     //System.out.println("####################\n"+tmp2.s +"  "+i+"  "+j+"\n"+ tmp2.m +"\n"+ tmp2.t);
                     this.opt_Alignment(tmp2, i - 1, j - 1, M, s, t, Straffaktor, Matchfaktor, Mismatchfaktor);
                 } else {
                     s_ = s.substring(i - 1, i) + tmp.s;
                     t_ = t.substring(j - 1, j) + tmp.t;
                     m_ = " " + tmp.m;
                     tmp2 = new DualString();
                     tmp2.s = s_;
                     tmp2.t = t_;
                     tmp2.m = m_;
                     //System.out.println("####################\n"+tmp2.s +"  "+i+"  "+j+"\n"+ tmp2.m +"\n"+ tmp2.t);
                     this.opt_Alignment(tmp2, i - 1, j - 1, M, s, t, Straffaktor, Matchfaktor, Mismatchfaktor);
                 }
             }
        }
     }
 
     public static void main(String[] args) {
         String seq1 = "TATAAT";//vertikale
         String seq2 = "TTACGTAAGC";//horizontale
         int gap = -4;
         int match = 3;
         int mismatch = -2;
        NeedlemanWunsch alignment = new NeedlemanWunsch(seq1.length() + 1, seq2.length() + 1);
        alignment.initMatrix(gap);
        alignment.printMatrix();
        alignment.fillMatrix(gap, match, mismatch, seq1, seq2);
        alignment.printMatrix();
     }
 
     class DualString {
 
         public String s;
         public String t;
         public String m = "";
 
         public String[] tmp_out() {
             String[] temp = new String[3];
             temp[0] = s;
             temp[1] = t;
             temp[2] = m;
             return temp;
         }
     }
 }
