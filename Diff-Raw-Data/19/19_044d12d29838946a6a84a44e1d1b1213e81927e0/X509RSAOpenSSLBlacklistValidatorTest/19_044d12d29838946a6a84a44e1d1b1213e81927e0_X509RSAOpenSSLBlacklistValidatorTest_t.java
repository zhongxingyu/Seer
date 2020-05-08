 
 package uk.org.ukfederation.mda.validate;
 
 import java.security.cert.X509Certificate;
 
 import junit.framework.Assert;
 import net.shibboleth.metadata.Item;
 
 import org.testng.annotations.Test;
 
 import uk.org.ukfederation.mda.MockItem;
 
 public class X509RSAOpenSSLBlacklistValidatorTest extends BaseX509ValidatorTest {
     
     /** Constructor sets class under test. */
     public X509RSAOpenSSLBlacklistValidatorTest() throws Exception {
         super(X509RSAOpenSSLBlacklistValidator.class);
     }
 
     @Test
     public void testNotBlacklisted() throws Exception {
         final X509RSAOpenSSLBlacklistValidator val = new X509RSAOpenSSLBlacklistValidator();
         val.setBlacklistResource(getClasspathResource("1024.txt"));
         val.initialize();
         Assert.assertEquals(0, val.getKeySize()); // no key size restriction
         
         final Item<String> item = new MockItem("foo");
         final X509Certificate cert = getCertificate("ok.pem");
         val.validate(cert, item, "stage");
         errorsAndWarnings(item, 0, 0);
     }
 
     @Test
     public void test1024on1024noRestriction() throws Exception {
         final X509RSAOpenSSLBlacklistValidator val = new X509RSAOpenSSLBlacklistValidator();
         val.setBlacklistResource(getClasspathResource("1024.txt"));
         val.initialize();
         Assert.assertEquals(0, val.getKeySize()); // no key size restriction
         
         final Item<String> item = new MockItem("foo");
         final X509Certificate cert = getCertificate("1024.pem");
         val.validate(cert, item, "stage");
         errorsAndWarnings(item, 1, 0);
     }
 
     @Test
     public void test1024on1024Restricted() throws Exception {
         final X509RSAOpenSSLBlacklistValidator val = new X509RSAOpenSSLBlacklistValidator();
         val.setBlacklistResource(getClasspathResource("1024.txt"));
         val.setKeySize(1024);
         val.initialize();
         
         final Item<String> item = new MockItem("foo");
         final X509Certificate cert = getCertificate("1024.pem");
         val.validate(cert, item, "stage");
         errorsAndWarnings(item, 1, 0);
     }
 
     @Test
     public void test1024on1024Restricted2() throws Exception {
         final X509RSAOpenSSLBlacklistValidator val = new X509RSAOpenSSLBlacklistValidator();
         val.setBlacklistResource(getClasspathResource("1024.txt"));
         val.setKeySize(2048); // untrue, but should prevent any matches
         val.initialize();
         
         final Item<String> item = new MockItem("foo");
         final X509Certificate cert = getCertificate("1024.pem");
         val.validate(cert, item, "stage");
         errorsAndWarnings(item, 0, 0);
     }
 
     @Test
     public void test2048on1024noRestriction() throws Exception {
         final X509RSAOpenSSLBlacklistValidator val = new X509RSAOpenSSLBlacklistValidator();
         val.setBlacklistResource(getClasspathResource("1024.txt"));
         val.initialize();
         Assert.assertEquals(0, val.getKeySize()); // no key size restriction
         
         final Item<String> item = new MockItem("foo");
         final X509Certificate cert = getCertificate("2048.pem");
         val.validate(cert, item, "stage");
         errorsAndWarnings(item, 0, 0);
     }
 
     @Test
     public void test2048on2048noRestriction() throws Exception {
         final X509RSAOpenSSLBlacklistValidator val = new X509RSAOpenSSLBlacklistValidator();
         val.setBlacklistResource(getClasspathResource("2048.txt"));
         val.initialize();
         Assert.assertEquals(0, val.getKeySize()); // no key size restriction
         
         final Item<String> item = new MockItem("foo");
         final X509Certificate cert = getCertificate("2048.pem");
         val.validate(cert, item, "stage");
         errorsAndWarnings(item, 1, 0);
     }
 
     @Test
     public void test2048on2048Restricted() throws Exception {
         final X509RSAOpenSSLBlacklistValidator val = new X509RSAOpenSSLBlacklistValidator();
         val.setBlacklistResource(getClasspathResource("2048.txt"));
         val.setKeySize(2048);
         val.initialize();
         
         final Item<String> item = new MockItem("foo");
         final X509Certificate cert = getCertificate("2048.pem");
         val.validate(cert, item, "stage");
         errorsAndWarnings(item, 1, 0);
     }
 
     @Test
     public void test2048on2048Restricted2() throws Exception {
         final X509RSAOpenSSLBlacklistValidator val = new X509RSAOpenSSLBlacklistValidator();
         val.setBlacklistResource(getClasspathResource("2048.txt"));
         val.setKeySize(1024); // untrue, but should prevent any matches
         val.initialize();
         
         final Item<String> item = new MockItem("foo");
         final X509Certificate cert = getCertificate("2048.pem");
         val.validate(cert, item, "stage");
         errorsAndWarnings(item, 0, 0);
     }
 
    @Test
    public void testBlankLineIssue9() throws Exception {
        final X509RSAOpenSSLBlacklistValidator val = new X509RSAOpenSSLBlacklistValidator();
        val.setBlacklistResource(getClasspathResource("issue9.txt"));
        val.initialize();
    }
    
 }
