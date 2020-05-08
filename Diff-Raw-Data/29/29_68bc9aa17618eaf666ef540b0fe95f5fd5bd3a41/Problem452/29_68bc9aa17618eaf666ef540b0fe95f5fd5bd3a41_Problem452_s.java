 package problems;
 
 import utils.Euler;
 import utils.T;
 
 import java.math.BigInteger;
 import java.util.*;
 
 public class Problem452 {
 
     static int m = (int)Math.pow(10, 6);
     static int n = m;
     public static void main(String[] args) {
         T t = new T();
         recurse(1,2, new ArrayList<Integer>());
        System.out.println(count.mod(BigInteger.valueOf(1234567891)) + " " + t);
     }
 
     private static void recurse(long n, int k, List<Integer> tuple) {
         registerTuple(tuple);
         for(int j = k; n*j <= m; j++) {
             List<Integer> newTuple = new ArrayList<Integer>(tuple);
             if (j == k) {
                 newTuple.add(newTuple.size() != 0 ? newTuple.remove(newTuple.size() - 1) + 1 : 1);
             } else {
                 newTuple.add(1);
             }
 
             recurse(n * j, j, newTuple);
         }
     }
 
    static BigInteger count = BigInteger.ZERO;
    static Map<String, BigInteger> memoize = new HashMap<String, BigInteger>();
     private static void registerTuple(List<Integer> tuple) {
        Collections.sort(tuple);
         String memo = tuple.toString();
         if (memoize.containsKey(memo)) {
            Problem452.count = Problem452.count.add(memoize.get(memo));
             return;
         }
 
        System.out.println(tuple);
         int others = 0;
         for(int value : tuple) {
             others += value;
         }
 
         BigInteger count = getCount(others);
 
         for(int value : tuple) {
             count = count.divide(Euler.factorial(value));
         }
 
        memoize.put(memo, count);
        Problem452.count = Problem452.count.add(count);
     }
 
     static Map<Integer, BigInteger> countMemoize = new HashMap<Integer, BigInteger>();
     private static BigInteger getCount(int others) {
         if (countMemoize.containsKey(others))
             return countMemoize.get(others);
 
         BigInteger count = BigInteger.ONE;
         for(int i = n; i > n-others; i--) {
             count = count.multiply(BigInteger.valueOf(i));
         }
         countMemoize.put(others, count);
         return count;
     }
 }
