 /* 1.6 Given an image represented by an N*N matrix, where each pixel in the 
  * image is 4 bytes, write a method to rotate the image by 90 degrees.
  * Can you do this in place?
  * */
 
 public class P0106 {
    /* 4 bytes = 32 bits = 1 int */
    /* Solution 1: touch each element once */
    public static int[][] rotate(int[][] matrix) {
       int n = matrix.length;
       int start, end;
      for (int layer = 0, len = n; layer < n / 2; layer++, len -= 2) {
          start = layer;
         end = len + start - 1;
         for (int offset = 0; offset < len - 1; offset++) {
             int tmp = matrix[start + offset][start];
             matrix[start + offset][start] = matrix[start][end - offset];
             matrix[start][end - offset] = matrix[end - offset][end];
             matrix[end - offset][end] = matrix[end][start + offset];
             matrix[end][start + offset] = tmp;
          }
 
       }
       return matrix;
    }
 
    /* Solution 2: touch each element twice */
    public static int[][] rotate2(int[][] matrix) {
       int n = matrix.length;
       matrix = transpose(matrix);
 
       for (int i = 0; i < n / 2; i++)
          for (int j = 0; j < n; j++) {
             int tmp = matrix[i][j];
             matrix[i][j] = matrix[n - 1 - i][j];
             matrix[n - 1 - i][j] = tmp;
          }
 
       return matrix;
    }
 
    /* swap for matrix elements */
    private static int[][] transpose(int[][] matrix) {
       int n = matrix.length;
       for (int i = 1; i < n; i++)
          for (int j = 0; j < i; j++) {
             int tmp = matrix[i][j];
             matrix[i][j] = matrix[j][i];
             matrix[j][i] = tmp;
          }
       return matrix;
    }
 
    public static void main(String[] args) {
       int[][] matrix = {{1,2,3,4}, {5,6,7,8}, {9,10,11,12}, {13,14,15,16}};
      matrix = P0106.rotate(matrix);
       for (int i = 0; i < matrix.length; i++) {
          for (int j = 0; j < matrix[0].length; j++)
             System.out.print(matrix[i][j] + " ");
          System.out.println();
       }
    }
 }
