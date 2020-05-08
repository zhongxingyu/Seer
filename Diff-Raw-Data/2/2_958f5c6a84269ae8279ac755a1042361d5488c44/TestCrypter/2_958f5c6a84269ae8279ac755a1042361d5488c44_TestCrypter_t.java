 package test.java.RSAEngine;
 
 import junit.framework.TestCase;
 import java.math.BigInteger;
 import main.java.RSAEngine.*;
 
 public class TestCrypter extends TestCase {
 
     public void testEncryptInteger_with_m_5_e_13_n_667()
     {
 		Crypter c = new Crypter();
         assertEquals(c.EncryptInteger(BigInteger.valueOf(5), BigInteger.valueOf(13), BigInteger.valueOf(667)), BigInteger.valueOf(412));
     }
     
 	public void testEncryptInteger_with_m_7_e_13_n_667()
     {
 		Crypter c = new Crypter();
         assertEquals(c.EncryptInteger(BigInteger.valueOf(7), BigInteger.valueOf(13), BigInteger.valueOf(667)), BigInteger.valueOf(112));
     }
     
 	public void testDecryptInteger_with_c_412_d_237_n_667()
     {
 		Crypter c = new Crypter();
         assertEquals(c.DecryptInteger(BigInteger.valueOf(412), BigInteger.valueOf(237), BigInteger.valueOf(667)), BigInteger.valueOf(5));
     }
     
 	public void testDecryptInteger_with_m_112_d_237_n_667()
     {
 		Crypter c = new Crypter();
         assertEquals(c.DecryptInteger(BigInteger.valueOf(112), BigInteger.valueOf(237), BigInteger.valueOf(667)), BigInteger.valueOf(7));
     }
 
 	public void testEncryptString() {
 		// Set up n, e and the plaintext string
 		BigInteger n = new BigInteger("A9E167983F39D55FF2A093415EA6798985C8355D9A915BFB1D01DA197026170FBDA522D035856D7A986614415CCFB7B7083B09C991B81969376DF9651E7BD9A93324A37F3BBBAF460186363432CB07035952FC858B3104B8CC18081448E64F1CFB5D60C4E05C1F53D37F53D86901F105F87A70D1BE83C65F38CF1C2CAA6AA7EB", 16);
 		BigInteger e = BigInteger.valueOf(65537);
 		String plaintext = new String(new BigInteger("4E636AF98E40F3ADCFCCB698F4E80B9F", 16).toByteArray());
 		
 		// Test the string encryption
 		Crypter c = new Crypter();
		assertEquals(c.EncryptString(plaintext, e, n), new String(new BigInteger("3D2AB25B1EB667A40F504CC4D778EC399A899C8790EDECEF062CD739492C9CE58B92B9ECF32AF4AAC7A61EAEC346449891F49A722378E008EFF0B0A8DBC6E621EDC90CEC64CF34C640F5B36C48EE9322808AF8F4A0212B28715C76F3CB99AC7E609787ADCE055839829E0142C44B676D218111FFE69F9D41424E177CBA3A435B", 16).toByteArray()));
 	}
 	
 	public void testDecryptString() {
 		// Set up n, d and the plaintext string
 		BigInteger n = new BigInteger("A9E167983F39D55FF2A093415EA6798985C8355D9A915BFB1D01DA197026170FBDA522D035856D7A986614415CCFB7B7083B09C991B81969376DF9651E7BD9A93324A37F3BBBAF460186363432CB07035952FC858B3104B8CC18081448E64F1CFB5D60C4E05C1F53D37F53D86901F105F87A70D1BE83C65F38CF1C2CAA6AA7EB", 16);
 		BigInteger d = new BigInteger("67CD484C9A0D8F98C21B65FF22839C6DF0A6061DBCEDA7038894F21C6B0F8B35DE0E827830CBE7BA6A56AD77C6EB517970790AA0F4FE45E0A9B2F419DA8798D6308474E4FC596CC1C677DCA991D07C30A0A2C5085E217143FC0D073DF0FA6D149E4E63F01758791C4B981C3D3DB01BDFFA253BA3C02C9805F61009D887DB0319", 16);
 
 		String ciphertext = new String(new BigInteger("1A6820F8546A1F114727D6151B58AD87D77E49C0AABC9B779F30285B65E590E42FC3F2A5A9A03A5A07AC1141FE3E6F2C1E78AAE9ECBCB1527FAA273BFDB12679D534446F457781E55754C837945926B7418FD2502D2AB4F96E317A1212741A0F6D7886279BE27B73492DB9BEEBEFEB4BC01C1EFDCC5A8BD8B19A36008A4FF338", 16).toByteArray());
 
 		
 		// Test the string decryption
 		Crypter c = new Crypter();
 		assertEquals(c.DecryptString(ciphertext, d, n), "Not sure yet");
 	}
 }
