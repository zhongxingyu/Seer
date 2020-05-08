 package ee.ttu;
 
 import java.util.Arrays;
 import java.util.List;
 
 public class Matrix {
     final double[][] matrix;
 
     public Matrix(List<String> rows) {
         matrix = new double[rows.size()][];
         for (int i = 0; i < rows.size(); i++) {
             matrix[i] = createRow(rows.get(i).split("\\s"));
         }
     }
 
     public Matrix(double[][] matrix) {
         this.matrix = matrix;
     }
     
     public int rowCount() {
         return matrix.length;
     }
 
     public int columnCount() {
         return matrix[0].length;
     }
 
     public double getValue(int row, int column) {
         return matrix[row][column];
     }
     
     public double[] getRow(int row) {
         return matrix[row].clone();
     }
 
     public double[] getColumn(int column) {
         double[] result = new double[matrix[0].length];
         for (int i = 0; i < matrix.length; i++) {
             result[i] = matrix[i][column];
         }
         return result;
     }
     
     private double[] createRow(String[] values) {
         double[] row = new double[values.length];
         for (int j = 0; j < values.length; j++) {
             row[j] = Double.valueOf(values[j]);
         }
         return row;
     }
 
     @Override
     public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        double[][] thatMatrix = ((Matrix) o).matrix;
        if (!(o instanceof Matrix) || !(thatMatrix.length == matrix.length)) {
            return false;
        }
        for (int i = 0; i < matrix.length; i++) {
            if (!Arrays.equals(thatMatrix[i], matrix[i])) {
                return false;
            }
        }
        return true;
     }
 
     @Override
     public int hashCode() {
         return Arrays.hashCode(matrix);
     }
 
     @Override
     public String toString() {
         StringBuilder builder = new StringBuilder();
         for (double[] doubles : matrix) {
             for (double d : doubles) {
                 builder.append(d);
             }
             builder.append("\n");
         }
         return builder.toString();
     }
 }
