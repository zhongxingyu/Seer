 package net.bodz.bas.cdata.chars;
 
 public class CharFeature {
 
     public static final byte[] c2n = new byte[] {//
     // 0 1 2 3 4 5 6 7 8 9 A B C D E F
             -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, //
             -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, //
             -1, 33, -1, -1, -1, -1, -1, -1, -1, -1, -1, 45, -1, 43, -1, 92, // +-, /->\, !->!
             0, 1, 2, 3, 4, 5, 6, 7, 8, 9, -1, -1, 62, -1, 60, 63, // <>, ?->?
             -1, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, // A-O
             25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 93, 47, 91, -1, -1, // P-Z, [], \->/
             -1, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, // a-o
             25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, -1, -1, -1, -1, -1, // p-z
             -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, //
             -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, //
             -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, //
             -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, //
             -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, //
             -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, //
             -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, //
             -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, //
     };
 
     private static final String alphabet = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
     public static final char[] n2cu = alphabet.toCharArray();
     public static final char[] n2cl = alphabet.toLowerCase().toCharArray();
 
     public static int getDigits(int radix, int num) {
        return getDigits(radix, num, false);
    }

    public static int getDigits(int radix, int num, boolean fullUsedOnly) {
         if (radix < 2)
             throw new IllegalArgumentException("radix must be >= 2");
         int digits = 0;
         num = Math.abs(num);
         while (num != 0) {
             digits++;
             num /= radix;
         }
         return digits;
     }
 
     public static int getDigits(int radix, long num) {
         if (radix < 2)
             throw new IllegalArgumentException("radix must be >= 2");
         int digits = 0;
         num = Math.abs(num);
         while (num != 0) {
             digits++;
             num /= radix;
         }
         return digits;
     }
 
     public static final int maxRadix = 32;
     public static final int intDigits[];
     public static final int longDigits[];
     static {
         intDigits = new int[] { 0, -2147483647, -32, 20, -16, 13, 12, 11, 10, 10, 9, 9, 8, 8, 8, 8, -8, 7, 7, 7, 7, 7,
                 7, 7, 6, 6, 6, 6, 6, 6, 6, 6, 6 };
         longDigits = new int[] { 0, -2147483647, -64, 40, -32, 27, 24, 22, 21, 20, 19, 18, 17, 17, 16, 16, -16, 15, 15,
                 15, 14, 14, 14, 14, 13, 13, 13, 13, 13, 13, 13, 12, 12 };
     }
 
     public static int _getDigits(int radix, int num) {
         if (num == Integer.MAX_VALUE)
             return intDigits[radix];
         num = Math.abs(num);
         if (num <= 1)
             return 1;
         double r = Math.log(num) / Math.log(radix);
         return 1 + (int) r;
     }
 
     public static int _getDigits(int radix, long maxValue) {
         if (maxValue == Long.MAX_VALUE)
             return longDigits[radix];
         double r = Math.log(maxValue) / Math.log(radix);
         return (int) r;
     }
 
     public static void main(String[] args) {
         for (int radix = 2; radix < 20; radix++) {
             for (int i = 0; i < 10000; i++) {
                 int n1 = getDigits(radix, i);
                 int n2 = _getDigits(radix, i);
                 if (n1 != n2) {
                     System.out.printf("r=%d %d = %d, %d\n", radix, i, n1, n2);
                 }
             }
         }
     }
 
 }
