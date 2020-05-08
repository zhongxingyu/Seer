 package com.trumpetx.codejam;
 
 import org.apache.commons.io.FileUtils;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.math.BigDecimal;
 import java.math.BigInteger;
 import java.math.RoundingMode;
 import java.util.ArrayList;
 import java.util.List;
 
 public class FairAndSquare {
 
     static BufferedReader r;
     static File outputFile;
    static List<BigInteger> FAIR_AND_SQUARE_TEN_TO_14TH;
 
 
     public static void main(String[] args) throws Exception {
         File f = new File(args.length > 0 ? args[0] : "test.txt");
         r = new BufferedReader(new FileReader(f));
         outputFile = new File(args.length > 1 ? args[1] : "results.txt");
         outputFile.delete();
 
         int testCases = Integer.parseInt(r.readLine());
 
        FAIR_AND_SQUARE_TEN_TO_14TH = fairAndSquareFigureItOut(-1, new String[]{"1","100000000000000"});
 
         for(int i=1; i<=testCases; i++){
             fairAndSquareFast(i);
         }
 
     }
 
     private static void fairAndSquareFast(int i) throws Exception {
         String[] boundaries = r.readLine().split(" ");
         BigInteger lowerBound = new BigInteger(boundaries[0]);
         BigInteger upperBound =  new BigInteger(boundaries[1]);
         int numberOfFairAndSquare = 0;
 
         for(BigInteger fairAndSquare : FAIR_AND_SQUARE_TEN_TO_14TH){
             if(fairAndSquare.compareTo(lowerBound) > -1 && fairAndSquare.compareTo(upperBound) < 1){
                 numberOfFairAndSquare++;
            } else if (fairAndSquare.compareTo(upperBound) == 1){
                break;
             }
         }
 
         output(new StringBuffer().append("Case #").append(i).append(": ").append(numberOfFairAndSquare));
     }
 
     private static List<BigInteger> fairAndSquareFigureItOut(int i, String[] boundaries) throws Exception {
         List<BigInteger> bigInts = new ArrayList<BigInteger>();
         BigInteger lowerBound = new BigInteger(boundaries[0]);
         BigInteger upperBound =  new BigInteger(boundaries[1]);
         int numberOfFairAndSquare = 0;
 
 
         BigInteger sqrtFloor = sqrt(new BigDecimal(boundaries[0]), 5).toBigInteger();
         BigInteger upperBoundFloor = sqrt(new BigDecimal(boundaries[1]), 5).toBigInteger();
 
         if(sqrtFloor.multiply(sqrtFloor).compareTo(lowerBound) == -1){
             sqrtFloor = sqrtFloor.add(BigInteger.ONE);
             if ( sqrtFloor.multiply(sqrtFloor).compareTo(upperBound) == 1){
                 output(new StringBuffer().append("Case #").append(i).append(": 0"));
                 return bigInts;
             }
         }
 
         while(sqrtFloor.compareTo(upperBoundFloor) <= 0){
             if(isPalindrome(sqrtFloor.toString())){
                 BigInteger mommaSquare = sqrtFloor.multiply(sqrtFloor);
                 if(isPalindrome(mommaSquare.toString()) && mommaSquare.compareTo(upperBound) < 1){
                     numberOfFairAndSquare++;
                     bigInts.add(mommaSquare);
                 }
             }
             sqrtFloor = sqrtFloor.add(BigInteger.ONE);
         }
 
         if(i > 0){
             output(new StringBuffer().append("Case #").append(i).append(": ").append(numberOfFairAndSquare));
         }
         return bigInts;
     }
 
     private static boolean isPalindrome(String str){
         char[] potential = str.toCharArray();
         for(int i=0; i< str.length()/2; i++){
            if(potential[i] != potential[potential.length-1-i]){
                 return false;
             }
         }
         return true;
     }
 
     private static void output(StringBuffer out) throws Exception {
         FileUtils.write(outputFile, out.append("\n"), "UTF-8", true);
         System.out.print(out);
     }
 
     /**
      * Code taken from:
      *
      * http://stackoverflow.com/questions/13649703/square-root-of-bigdecimal-in-java
      *
      * @param in
      * @param scale
      * @return
      */
     public static BigDecimal sqrt(BigDecimal in, int scale){
         BigDecimal sqrt = new BigDecimal(1);
         sqrt.setScale(scale + 3, RoundingMode.FLOOR);
         BigDecimal store = new BigDecimal(in.toString());
         boolean first = true;
         do{
             if (!first){
                 store = new BigDecimal(sqrt.toString());
             }
             else first = false;
             store.setScale(scale + 3, RoundingMode.FLOOR);
             sqrt = in.divide(store, scale + 3, RoundingMode.FLOOR).add(store).divide(
                     BigDecimal.valueOf(2), scale + 3, RoundingMode.FLOOR);
         }while (!store.equals(sqrt));
         return sqrt.setScale(scale, RoundingMode.FLOOR);
     }
 
 }
