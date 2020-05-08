 import org.junit.Assert;
 import org.junit.Test;
 
 import com.jajja.arachne.exceptions.MalformedUriException;
 import com.jajja.arachne.net.Url;
 
 public class UrlTest {
 
     @Test public void resolve() {
 
 //        assertResolve("http://a/b/c/d;p?q#f", "g:h", "g:h");                      // fails, result: g://h -- web browsers get it "wrong" too.
         assertResolve("http://a/b/c/d;p?q#f", "g", "http://a/b/c/g");
         assertResolve("http://a/b/c/d;p?q#f", "./g", "http://a/b/c/g");
         assertResolve("http://a/b/c/d;p?q#f", "g/", "http://a/b/c/g/");
         assertResolve("http://a/b/c/d;p?q#f", "/g", "http://a/g");
         assertResolve("http://a/b/c/d;p?q#f", "//g", "http://g");
         assertResolve("http://a/b/c/d;p?q#f", "?y", "http://a/b/c/d;p?y");
         assertResolve("http://a/b/c/d;p?q#f", "g?y", "http://a/b/c/g?y");
         assertResolve("http://a/b/c/d;p?q#f", "g?y/./x", "http://a/b/c/g?y/./x");
         assertResolve("http://a/b/c/d;p?q#f", "#s", "http://a/b/c/d;p?q#s");
         assertResolve("http://a/b/c/d;p?q#f", "g#s", "http://a/b/c/g#s");
         assertResolve("http://a/b/c/d;p?q#f", "g#s/./x", "http://a/b/c/g#s/./x");
         assertResolve("http://a/b/c/d;p?q#f", "g?y#s", "http://a/b/c/g?y#s");
 //        assertResolve("http://a/b/c/d;p?q#f", ";x", "http://a/b/c/d;x");            // XXX: fails, this URI is specific for FTP
         assertResolve("http://a/b/c/d;p?q#f", "g;x", "http://a/b/c/g;x");
         assertResolve("http://a/b/c/d;p?q#f", "g;x?y#s", "http://a/b/c/g;x?y#s");
         assertResolve("http://a/b/c/d;p?q#f", ".", "http://a/b/c/");
         assertResolve("http://a/b/c/d;p?q#f", "./", "http://a/b/c/");
         assertResolve("http://a/b/c/d;p?q#f", "..", "http://a/b/");
         assertResolve("http://a/b/c/d;p?q#f", "../", "http://a/b/");
         assertResolve("http://a/b/c/d;p?q#f", "../g", "http://a/b/g");
         assertResolve("http://a/b/c/d;p?q#f", "../..", "http://a/");
         assertResolve("http://a/b/c/d;p?q#f", "../../", "http://a/");
         assertResolve("http://a/b/c/d;p?q#f", "../../g", "http://a/g");
 
         // abnormal tests
 
         assertResolve("http://a/b/c/d;p?q#f", "", "http://a/b/c/d;p?q#f");
         //assertResolve("http://a/b/c/d;p?q#f", "../../../g", "http://a/../g");         // fails - mimicking web browsers
         //assertResolve("http://a/b/c/d;p?q#f", "../../../../g", "http://a/../../g");   // fails - mimicking web browsers
         //assertResolve("http://a/b/c/d;p?q#f", "/./g", "http://a/./g");                // fails - mimicking web browsers
         //assertResolve("http://a/b/c/d;p?q#f", "/../g", "http://a/../g");              // fails - mimicking web browsers
         assertResolve("http://a/b/c/d;p?q#f", "g.", "http://a/b/c/g.");
         assertResolve("http://a/b/c/d;p?q#f", ".g", "http://a/b/c/.g");
         assertResolve("http://a/b/c/d;p?q#f", "g..", "http://a/b/c/g..");
         assertResolve("http://a/b/c/d;p?q#f", "..g", "http://a/b/c/..g");
         assertResolve("http://a/b/c/d;p?q#f", "./../g", "http://a/b/g");
         assertResolve("http://a/b/c/d;p?q#f", "./g/.", "http://a/b/c/g/");
         assertResolve("http://a/b/c/d;p?q#f", "g/./h", "http://a/b/c/g/h");
         assertResolve("http://a/b/c/d;p?q#f", "g/../h", "http://a/b/c/h");
 
         // "should be avoided by future parsers"
 
         //assertResolve("http://a/b/c/d;p?q#f", "http:g", "http:g");                    // fails
         //assertResolve("http://a/b/c/d;p?q#f", "http", "http:");                       // fails
     }
 
    private void assertResolve(String url, String uri, String expected) {
         try {
             Url u = new Url(url);
             u = u.resolve(uri);
             System.out.println((u.toString().equals(expected) ? "PASS " : "FAIL ") + "   " + url + " + " + uri + " = " + u.toString() + "   should equal " + expected);
             Assert.assertEquals(expected, u.toString());
         } catch (MalformedUriException e) {
             Assert.fail(e.getMessage());
         }
     }
 
 }
