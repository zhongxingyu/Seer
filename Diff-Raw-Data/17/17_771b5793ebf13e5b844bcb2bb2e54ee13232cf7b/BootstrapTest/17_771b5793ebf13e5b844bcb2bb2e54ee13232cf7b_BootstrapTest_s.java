 package fnug.servlet;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 public class BootstrapTest {
 
     @Test
     public void testEscape() throws Exception {
 
         String s = "0123456789abcdef";
         s = Bootstrap.escape(s, 24);
         Assert.assertEquals("\"0123456789abcdef\"", s);
 
         s = "0123456789abcdef";
         s = Bootstrap.escape(s, 16);
         Assert.assertEquals("\"0123456789abcdef\"", s);
 
         s = "0123456789abcdef";
         s = Bootstrap.escape(s, 15);
         Assert.assertEquals("\"0123456789abcde\"+\n" +
                 "\"f\"", s);
 
         s = "0123456789abcdef0123456789abcdef";
         s = Bootstrap.escape(s, 15);
         Assert.assertEquals("\"0123456789abcde\"+\n" +
                 "\"f0123456789abcd\"+\n" +
                 "\"ef\"", s);
 
         s = "0123456789abcde\\f";
         s = Bootstrap.escape(s, 15);
         Assert.assertEquals("\"0123456789abcd\"+\n" +
                 "\"e\\\\f\"", s);
 
         s = "0123456789abcde\"f";
         s = Bootstrap.escape(s, 15);
         Assert.assertEquals("\"0123456789abcd\"+\n" +
                 "\"e\\\"f\"", s);
 
         s = "0123456789abcdef\"";
         s = Bootstrap.escape(s, 15);
         Assert.assertEquals("\"0123456789abcde\"+\n" +
                 "\"f\\\"\"", s);
 
         s = "0123456789abcde\"f0123456789abcdef";
         s = Bootstrap.escape(s, 15);
         Assert.assertEquals("\"0123456789abcd\"+\n" +
                 "\"e\\\"f0123456789a\"+\n" +
                 "\"bcdef\"", s);
 
     }
 
 }
