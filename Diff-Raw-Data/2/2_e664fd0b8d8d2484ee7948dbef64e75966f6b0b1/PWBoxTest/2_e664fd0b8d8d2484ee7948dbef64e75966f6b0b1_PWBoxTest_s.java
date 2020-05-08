 package org.scode.pwbox;
 
 import org.testng.Assert;
 import org.testng.annotations.Test;
 
 import java.io.UnsupportedEncodingException;
 
 public class PWBoxTest {
     @Test
     public void encryptDecrypt() throws PWBoxException, UnsupportedEncodingException {
         byte[] encrypted = PWBox.encrypt(PWBox.Format.DEFAULT, "passphrase", "secret".getBytes("UTF-8"));
         byte[] plain = PWBox.decrypt("passphrase", encrypted);
 
        Assert.assertEquals(plain, "secret");
     }
 }
