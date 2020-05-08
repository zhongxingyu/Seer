 package y2013;
 import java.util.*;
 import java.io.*;
 
 class P7 {
     private static Scanner in;
     private static int [][] arr;
     public static void main (String [] args) throws IOException {
         in = new Scanner (System.in);
         PrintWriter out = new PrintWriter (System.out, true);
         
         int test = 1;
         while (in.hasNext()) {
             String [] s = new String [] {in.next(), in.next(), in.next(), in.next()};
             arr = new int [4][];
             int sum = 0;
             int [] lens = new int [4];
             for (int i = 0; i < 4; i++) {
                 lens [i] = s[i].length();
                 arr [i] = new int [s[i].length() + 1];
                 for (int j = 1; j < arr[i].length; j++) {
                     arr[i][j] = conv (s[i].charAt(j - 1));
                     sum += arr[i][j];
                 }
             }
             dp = new int [lens[0] + 1][lens[1] + 1][lens[2] + 1][lens[3] + 1];
             for (int i = 0; i <= lens [0]; i++)
                 for (int j = 0; j <= lens[1]; j++)
                     for (int k = 0; k <= lens[2]; k++)
                         Arrays.fill (dp[i][j][k], -1);
             
            // whoops forgot a base case here during live contest. guess test cases were weak. Got lucky here
            dp[0][0][0][0] = 0;
            
             out.printf ("Deal %d: First player wins %d out of %d\n", test++, 
                     dp(lens[0], lens[1], lens[2], lens[3], true), sum);
         }
         out.close();
         System.exit(0);
     }
     
     private static int [][][][] dp;
     
     private static int dp (int a, int b, int c, int d, boolean max) {
         if (dp[a][b][c][d] != -1) return dp[a][b][c][d];
         if (max) {
             int res = 0;
             if (a != 0) {
                 res = Math.max(res, arr[0][a] + dp (a - 1, b, c, d, false));
             }
             if (b != 0) {
                 res = Math.max(res, arr[1][b] + dp (a, b - 1, c, d, false));
             }
             if (c != 0) {
                 res = Math.max(res, arr[2][c] + dp (a, b, c - 1, d, false));
             }
             if (d != 0) {
                 res = Math.max(res, arr[3][d] + dp (a, b, c, d - 1, false));
             }
             return dp[a][b][c][d] = res;
         } else {
             int min = Integer.MAX_VALUE;
             if (a != 0) {
                 min = Math.min(min, dp (a - 1, b, c, d, true));
             }
             if (b != 0) {
                 min = Math.min(min, dp (a, b - 1, c, d, true));
             }
             if (c != 0) {
                 min = Math.min(min, dp (a, b, c - 1, d, true));
             }
             if (d != 0) {
                 min = Math.min(min, dp (a, b, c, d - 1, true));
             }
             return dp[a][b][c][d] = min;
         }
     }
     
     private static int conv (char c) {
         if (c >= '0' && c <= '9')
             return c - '0';
         switch (c) {
         case 'K':
             return 13;
         case 'Q':
             return 12;
         case 'J':
             return 11;
         case 'T':
             return 10;
         case 'A':
             return 1;
         }
         return -1;
     }
 }
