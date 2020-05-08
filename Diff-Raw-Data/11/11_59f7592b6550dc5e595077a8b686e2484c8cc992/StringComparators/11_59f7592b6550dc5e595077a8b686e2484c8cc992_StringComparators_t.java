 package norm.jvm;
 
 public class StringComparators {
   public static int commonPrefixLength (final String a, final String b) {
     int l = 0;
     while (l < Math.min(a.length(), b.length()) && a.charAt(l) == b.charAt(l)) l++;
     return l;
   }
 
   public static int commonSuffixLength (final String a, final String b) {
     int l = 0;
     int i = a.length()-1;
     int j = b.length()-1;
     while (i >= 0 && j >= 0 && a.charAt(i) == b.charAt(j)) {
       i--;
       j--;
       l++;
     }
     return l;
   }
 
   public static int longestCommonSubsequence (final String a, final String b) {
     int[] previousRow = new int[a.length() + 1];
     int[] currentRow  = new int[a.length() + 1];
     
     for (int i=0;i<b.length();i++) {
       for (int j=1;j<a.length() + 1;j++) {
         currentRow[j] = b.charAt(i) == a.charAt(j-1) ?
                        previousRow[j-1] + 1
                      : Math.max(currentRow[j-1], previousRow[j]);
       }
       System.arraycopy(currentRow, 0, previousRow, 0, a.length() + 1);
     }
 
     int max = 0;
     for (int x : currentRow) {
       max = Math.max(max, x);
     }
     return max;
   }
 
   public static int levenshteinDistance (final String a, final String b) {
     
     final int n = a.length();
     final int m = b.length();
 
     final int nplusone = n + 1; // yeah the compiler should optimize this anyway
                                 // just making it explicit
 
     if (n == 0 || m == 0) return n + m;
 
     int[] previousRow = new int[n + 1];
     int[] currentRow = new int[n + 1];
 
 
     int i = 0;
     int j = 0;
 
 
     for (i = 0; i < nplusone; i++) {
       previousRow[i] = i;
     }
 
     for (i = 0; i < m; i++) {
       currentRow[0] = i + 1;
       for (j = 1; j < nplusone; j++) {
         currentRow[j] = 
           Math.min(
             Math.min(
               // previous best match + penalty if chars are different
               previousRow[j-1] + (a.charAt(j-1) == b.charAt(i) ? 0 : 1),
               // add a space
               currentRow[j-1] + 1
             ),
             // remove the character
             previousRow[j] + 1);
       }
       
       System.arraycopy(currentRow, 0, previousRow, 0, nplusone);
     }
 
     return currentRow[n];
   }
}
