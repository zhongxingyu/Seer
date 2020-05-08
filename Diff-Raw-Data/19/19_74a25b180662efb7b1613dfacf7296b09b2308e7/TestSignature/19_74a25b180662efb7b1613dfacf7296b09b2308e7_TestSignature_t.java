//package test.java.CryptoEngine;
 
 import junit.framework.TestCase;
 import java.math.BigInteger;
//import main.java.CryptoEngine.*;
import java.security.NoSuchAlgorithmException;

import org.json.JSONException;
 
 public class TestSignature extends TestCase {
 
     private Signature s;
 
     private String username;
     private String message;
     private String signature;
 
     private BigInteger e;
     private BigInteger d;
     private BigInteger n;
 
     public void setUp() {
         try {
			s = new Signature();
		} catch (NoSuchAlgorithmException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
 
          username = "chetan51";
          message = "This is a the message to be signed (a String)";
 
          n = new BigInteger("A9E167983F39D55FF2A093415EA6798985C8355D9A915BFB1D01DA197026170FBDA522D035856D7A986614415CCFB7B7083B09C991B81969376DF9651E7BD9A93324A37F3BBBAF460186363432CB07035952FC858B3104B8CC18081448E64F1CFB5D60C4E05C1F53D37F53D86901F105F87A70D1BE83C65F38CF1C2CAA6AA7EB", 16);
          d = new BigInteger("67CD484C9A0D8F98C21B65FF22839C6DF0A6061DBCEDA7038894F21C6B0F8B35DE0E827830CBE7BA6A56AD77C6EB517970790AA0F4FE45E0A9B2F419DA8798D6308474E4FC596CC1C677DCA991D07C30A0A2C5085E217143FC0D073DF0FA6D149E4E63F01758791C4B981C3D3DB01BDFFA253BA3C02C9805F61009D887DB0319", 16);
          e = BigInteger.valueOf(65537);
     }
 
    public void testGenerateAndVerifySignature() throws JSONException {
         assertEquals(s.verifySignature(s.generateSignature(username, message, d, n), e, n), true);
     }
 
 }
