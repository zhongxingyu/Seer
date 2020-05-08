 // hwRSA.java
 // Houses all required RSA *and* all required 0-Knowledge code.
 import java.math.*;
 import java.util.*;
 import java.io.*;
 
 public class hwRSA
 {
     private static final int numberOfBits = 32768; // 2^15
     private static final BigInteger ONE = new BigInteger("1");
     private static final String AUTHFILE = "auth.txt";
     
     public Random savedRandom = null;
     public BigInteger p;
     public BigInteger q;
     public BigInteger n; // n = p * q
     
     // RSA only?
     public BigInteger phiN;
     public BigInteger e; // for encryption; compute based on above.
     public BigInteger d; // for decryption; compute based on e.
     
     // 0 Knowledge only?
     public BigInteger S;
     public BigInteger V;
     
     public hwRSA(Random random)
     {
         savedRandom = random;
     
         p = getPrime(random); // p and q are arbitrary large primes
         q = getPrime(random);
         n = p.multiply(q);
         phiN = (p.subtract(ONE)).multiply(q.subtract(ONE));
         
         S = getPrime(random); // s is an arbitrary secret; we'll use a prime because they're BA!!!
         V = (S.multiply(S)).mod(n);
     }
     
     public void initialize(Random random)
     {
         // initialize all the variables
     }
 
    public void save()
     {
         // save out the RSA information
         PrintWriter fileout = new PrintWriter(new FileWriter(AUTHFILE));
         fileout.flush();
         fileout.println(p);
         fileout.println(q);
         fileout.println(n);
         fileout.println(phiN);
 //        fileout.println(e);
 //        fileout.println(d);
         fileout.println(S);
         fileout.println(V);
         fileout.close();
     }
     
    public void load()
     {
         // load all the variables from a saved source
         BufferedReader filein = new BufferedReader(new FileReader(AUTHFILE));
         // assume filein.ready() is true for the moment
        p = filein.readln();
        q = filein.readln();
        n = filein.readln();
        phiN = filein.readln();
        //e = filein.readln();
        //d = filein.readln();
        S = filein.readln();
        V = filein.readln();
         filein.close();
     }
     
     private BigInteger getPrime(Random random)
     {
         return BigInteger.probablePrime(numberOfBits, random);
     }
 }
