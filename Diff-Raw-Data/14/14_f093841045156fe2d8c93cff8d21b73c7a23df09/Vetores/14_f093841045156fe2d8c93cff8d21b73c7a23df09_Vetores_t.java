 class Vetores {
 
     static int[] naturals(int howMany)
     {
         int[] naturals = new int[howMany];
         int i = 0;
 
         while (i < howMany) {
             naturals[i] = i + 1;
             i++;
         }
 
         return naturals;
     }
 
     static int sum(int[] v)
     {
         int sum = 0,
             i = 0;
 
         while (i < v.length) {
             sum += v[i];
             i++;
         }
 
         return sum;
     }
 
     static int[] randomDigits(int howMany)
     {
         int[] rd = new int[howMany];
        int maxDigit = 10;
 
         while (howMany > 0) {
             howMany--;
             rd[howMany] = (int) (Math.random() * maxDigit);
         }
 
         return rd;
     }
     // A
     static double mean(int[] v)
     {
         return sum(v) / (double) v.length;
     }
     // B
     static int[] copy(int[] v, int length)
     {
         if (length == v.length)
             return v;

         int[] copy = new int[length];
         int i = 0;
 
         while (i < v.length) {
             copy[i] = v[i];
             i++;
         }
 
         return copy;
     }
     // C
     static int[] copy(int[] v)
     {
         return copy(v, v.length);
     }
     // D
     static boolean exists(int compare, int[] v)
     {
         int i = 0;
 
         while (i < v.length) {
             if (compare == v[i])
                 return true;
 
             i++;
         }
 
         return false;
     }
     // E
     static int count(int compare, int[] v)
     {
         int i = 0,
             count = 0;
 
         while (i < v.length) {
             if (compare == v[i])
                 count++;
 
             i++;
         }
 
         return count;
     }
     // F
     static int max(int[] v)
     {
         int i = 0,
             max = 0;
 
         while (i < v.length) {
             if (max < v[i])
                 max = v[i];
 
             i++;
         }
 
        return max;
     }
     // G
     static int[] subArray(
         int leftBound,
         int rightBound,
         int[] v
     ) {
         int[] subArray = new int[rightBound - leftBound + 1];
         int i = 0;
 
         while (leftBound <= rightBound) {
            subArray[i] = v[leftBound];
             leftBound++;
             i++;
         }
 
         return subArray;
     }
     // H
     static int[] firstHalf(
         int[] v,
         boolean includeMiddle
     ) {
         int half;
 
         if (includeMiddle && v.length % 2 != 0)
             half = v.length / 2;
         else
             half = v.length / 2 - 1;
 
 
         return subArray(0, half, v);
     }
     // I
     static int[] secondHalf(
         int[] v,
         boolean includeMiddle
     ) {
         int half;
 
         if (includeMiddle || v.length % 2 == 0)
             half = v.length / 2;
         else
             half = v.length / 2 + 1;
 
 
         return subArray(half, v.length - 1, v);
     }
     // J
     static int[] merge(int[] lv, int[] rv)
     {
         int[] v = new int[lv.length + rv.length];
         int i = 0,
             lPosition = 0;
 
         while (i < lv.length) {
             v[i] = lv[i];
             i++;
         }
 
         lPosition = i;
         i = 0;
 
         while (i < rv.length) {
             v[lPosition + i] = rv[i];
             i++;
         }
 
         return v;
     }
     // K
     static int[] invert(int[] v)
     {
         int i = 0,
             length = v.length;
         int invert[] = new int[length];
 
         while (length > 0) {
             length--;
             invert[i] = v[length];
             i++;
         }
 
         return invert;
     }
     // L
     static int random(int[] v)
     {
         return v[(int) (Math.random() * v.length)];
     }
 
     // Extra
     // A
     static int[] duplicateEveryElement(int[] v)
     {
         int[] doubleV = new int[v.length * 2];
         int i = 0,
             j = 0;
 
         while (i < v.length) {
             doubleV[j] = v[i];
             j++;
             doubleV[j] = v[i];
             j++;
             i++;
         }
 
         return doubleV;
     }
     // B
     static int[] duplicateInverted(int[] v)
     {
         return merge(v, invert(v));
     }
     // C
     static int[] copyWithoutMiddleElement(int[] v)
     {
         return merge(firstHalf(v, false), secondHalf(v, false));
     }
     // D
     static int[] fibonacciSequence(int n)
     {
         int i = 0;
         int[] fib = new int[n];
 
         while (i < n) {
             if (i > 1)
                 fib[i] = fib[i-1] + fib[i-2];
             else if (i == 1)
                 fib[1] = 1;
             else
                 fib[0] = 0;
 
             i++;
         }

         return fib;
     }
 
 }
 
 
 
 
 
 
