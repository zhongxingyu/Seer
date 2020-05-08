 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package week5;
 
 import java.io.IOException;
 import java.math.BigInteger;
 import java.util.HashMap;
 import java.util.Properties;
 
 /**
  * Programming Assignment Week 5
  *
  * Your goal this week is to write a program to compute discrete log modulo a
  * prime p. Let g be some element in Z*p and suppose you are given h in Z*p such
  * that h=g^x where 1≤x≤2^40. Your goal is to find x. More precisely, the input to
  * your program is p,g,h and the output is x.
  * The trivial algorithm for this problem is to try all 2^40 possible values of x
  * until the correct one is found, that is until we find an x satisfying h=g^x in
  * Zp. This requires 2^40 multiplications. In this project you will implement an
  * algorithm that runs in time roughly 2^20 using a meet in the middle
  * attack.
  * Let B=2^20. Since x is less than B^2 we can write the unknown x base B as
  * x=x_0*B+x_1 where x_0,x_1 are in the range [0,B−1]. Then
  *
  * h=g^x=g^(x_0*B+x_1)=(g^B)^x_0 * g^x_1 in Zp.
  *
  * By moving the term g^x_1 to the other side we obtain
  *
  * h/g^x_1=(g^B)^x_0 in Zp.
  *
  * The variables in this equation are x_0,x_1 and everything else is known: you
  * are given g,h and B=2^20. Since the variables x_0 and x_1 are now on different
  * sides of the equation we can find a solution using meet in the middle
  * (Lecture 3.3): First build a hash table of all possible values of the left
  * hand side h/g^x_1 for x_1=0,1,…,2^20. Then for each value x_0=0,1,2,…,2^20 check if
  * the right hand side (g^B)^x_0 is in this hash table. If so, then you have found
  * a solution (x_0,x_1) from which you can compute the required x as x=x_0*B+x_1. The
  * overall work is about 2^20 multiplications to build the table and another 2^20
  * lookups in this table.
  *
  */
 public class Main {

     private static BigInteger p;
     private static BigInteger g;
     private static BigInteger h;
     private static final BigInteger B = BigInteger.valueOf(2).pow(20);
     private static HashMap<BigInteger, Integer> leftTable = new HashMap<>();
     
     public static void main(String[] args) throws IOException {
         getInput();
         buildLeftTable();
         BigInteger[] result = computeRigthTable();
         if (result != null) {
             System.out.println("x0 (right table) = " + result[0]);
             System.out.println("x1 (left table) = " + result[1]);
             BigInteger answer = B.multiply(result[0]).add(result[1]).mod(p);
             System.out.println("Answer = " + answer);
         } else {
             System.out.println("Answer not found");
         }
     }
     
     private static void getInput() throws IOException {
         Properties props = new Properties();
         props.load(ClassLoader.getSystemResourceAsStream("input"));
         for (String propName : props.stringPropertyNames()) {
             switch (propName) {
                 case "p": p = new BigInteger(props.getProperty(propName));
                     break;
                 case "g": g = new BigInteger(props.getProperty(propName));
                     break;
                 case "h": h = new BigInteger(props.getProperty(propName));
                     break;
             }
         }
         System.out.println("Input loaded!");
         System.out.println("p = " + p);
         System.out.println("g = " + g);
         System.out.println("h = " + h);
     }
     
     private static void buildLeftTable() {
         BigInteger currentValue = BigInteger.valueOf(1);
        for (int i = 0; i <= B.longValue(); i++) {
             if (i != 0) {
                 currentValue = currentValue.multiply(g).mod(p);
             }
             leftTable.put(h.multiply(currentValue.modInverse(p)).mod(p), i);
         }
         System.out.println("Left table built!");
     }
     
     private static BigInteger[] computeRigthTable() {
        BigInteger currentValue = g.modPow(B, p);
        for (int i = 0; i <= B.longValue(); i++) {
             if (i != 0) {
                currentValue = currentValue.multiply(g).mod(p);
             }
             if (leftTable.containsKey(currentValue)) {
                 return new BigInteger[] {BigInteger.valueOf(i), BigInteger.valueOf(leftTable.get(currentValue))};
             }
         }
         return null;
     }
 }
