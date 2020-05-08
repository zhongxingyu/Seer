 package test.java.RSAEngine;
 
 import junit.framework.TestCase;
 import java.math.BigInteger;
 import main.java.RSAEngine.*;
 
 public class TestKeyGen extends TestCase {
 
     public void testExtendedGCD_with_m_28_e_5()
     {
 		KeyGen k = new KeyGen();
 		BigInteger[] ans = new BigInteger[2];
 		ans[0] = BigInteger.valueOf(2);
 		ans[1] = BigInteger.valueOf(-11);
        assertEquals(k.ExtendedGCD(BigInteger.valueOf(28), BigInteger.valueOf(5)), ans);
     }
     
 	public void testEncryptInteger_with_m_616_e_13()
     {
 		KeyGen k = new KeyGen();
 		BigInteger[] ans = new BigInteger[2];
 		ans[0] = BigInteger.valueOf(-5);
 		ans[1] = BigInteger.valueOf(237);
        assertEquals(k.ExtendedGCD(BigInteger.valueOf(616), BigInteger.valueOf(13)), ans);
     }
     
 	public void testFindCoprime_with_x_616()
     {
 		KeyGen k = new KeyGen();
         assertEquals(k.FindCoprime(BigInteger.valueOf(616)), BigInteger.valueOf(3));
     }
     
 	public void testFindCoprime_with_x_12()
     {
 		KeyGen k = new KeyGen();
         assertEquals(k.FindCoprime(BigInteger.valueOf(12)), BigInteger.valueOf(5));
     }
 }
