 package info.mikaelsvensson.ftpbackup.util;
 
 import org.junit.Test;
 
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.assertThat;
 
 public class NumberPrefixFormatTest {
 
     public static final long VALUE_1024 = 1024L;
 
     @Test
     public void test1() throws Exception {
         performTest(123456789L, "_k", "123,456.789 k");
     }
     @Test
     public void test2() throws Exception {
         performTest(123456789L, "", "123.457M");
     }
     @Test
     public void test3a() throws Exception {
         performTest(123456789L, "M", "123.457M");
     }
     @Test
     public void test3b() throws Exception {
         performTest(123456789L, "mega", "123.457mega");
     }
     @Test
     public void test4() throws Exception {
         performTest(123456789L, ".2 M", "123.46 M");
     }
     @Test
     public void test5() throws Exception {
         performTest(123456789L, ".2", "123.46M");
     }
     @Test
    public void test6() throws Exception {
         performTest(1024L, "i", "1Ki");
     }
     @Test
     public void test7() throws Exception {
         performTest(1024L, ".4_Mi", "0.0010 Mi");
     }
     @Test
     public void test9a() throws Exception {
         performTest(1024L, "i", "1Ki");
     }
     @Test
     public void test9b() throws Exception {
         performTest(1024L, "", "1.024k");
     }
     @Test
     public void test8() throws Exception {
         performTest(8*1024L + 512, "3.2_Ki", "008.50 Ki");
     }
 
     private void performTest(long value, String format, String expected) {
         NumberPrefixFormat numberPrefixFormat = new NumberPrefixFormat(format);
         String actual = numberPrefixFormat.format(value);
         assertThat(actual, is(expected));
     }
 }
