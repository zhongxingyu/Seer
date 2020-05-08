 package nl.nikhef.jgridstart.util;
 
 import java.util.Arrays;
 
 import org.bouncycastle.openssl.PasswordFinder;
 
 import junit.framework.Assert;
 import junit.framework.TestCase;
 
 public class PasswordCacheTest extends TestCase {
     
     private PasswordCache cache = null;
     
     public void setUp() {
 	cache = PasswordCache.getInstance();
 	cache.setUI(PasswordCache.UI_NONE);
     }
     
     public void testGetInstance() {
 	Assert.assertEquals(cache, PasswordCache.getInstance());
     }
 
     public void testSet() {
 	cache.clear();
 	char[] pw = "Xyz123zyX".toCharArray();
 	cache.set("foo", pw);
 	// check this entry
 	Assert.assertNotNull(cache.getForDecrypt("", "foo"));
 	Assert.assertTrue(Arrays.equals(cache.getForDecrypt("", "foo"), pw));
 	// make sure another entry isn't set
 	Assert.assertNull(cache.getForDecrypt("", "bar"));
     }
 
     public void testInvalidate() {
 	cache.clear();
 	cache.set("blah", "test".toCharArray());
 	Assert.assertNotNull(cache.getForDecrypt("", "blah"));
 	cache.invalidate("blah");
 	Assert.assertNull(cache.getForDecrypt("", "blah"));
     }
     
     public void testClear() {
 	cache.set("foobar", "faosdifj".toCharArray());
 	cache.set("barfoo", "asduiofs".toCharArray());
 	cache.clear();
 	Assert.assertNull(cache.getForDecrypt("", "foobar"));
 	Assert.assertNull(cache.getForDecrypt("", "barfoo"));
     }
 
    public void testGetDencryptPasswordFinder() {
 	cache.clear();
 	char[] pw = "aoksdJLKASjkl".toCharArray();
 	cache.set("bar", pw);
 	PasswordFinder f = cache.getDecryptPasswordFinder("", "bar");
 	Assert.assertTrue(Arrays.equals(f.getPassword(), pw));
     }
     
     public void testTimeout() throws InterruptedException {
 	int timeout = 1;
 	cache.clear();
 	cache.setTimeout(timeout);
 	cache.set("blah", "test".toCharArray());
 	Assert.assertNotNull(cache.getForDecrypt("", "blah"));
 	Thread.sleep(timeout*1000 + 10);
 	Assert.assertNull(cache.getForDecrypt("", "blah"));
     }
 
 }
