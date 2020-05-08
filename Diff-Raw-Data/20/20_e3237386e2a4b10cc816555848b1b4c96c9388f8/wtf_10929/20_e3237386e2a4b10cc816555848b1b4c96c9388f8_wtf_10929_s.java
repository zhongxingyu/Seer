import java.math.BigInteger;
 import java.util.*;
 
 public class wtf_10929
 {
     public static void main(String[] args)
     {
         BigInteger n = new BigInteger("1");
         BigInteger elv = new BigInteger("11");
         Scanner in = new Scanner(System.in);
        while (n.intValue() != 0)
         {
            n = in.nextBigInteger();
            if (n.intValue() == 0) break;
            if (n.mod(elv).intValue() == 0)
                System.out.println(n + " is a multiple of 11.");
             else
                System.out.println(n + " is not a multiple of 11.");
         }
 
     }
 }
