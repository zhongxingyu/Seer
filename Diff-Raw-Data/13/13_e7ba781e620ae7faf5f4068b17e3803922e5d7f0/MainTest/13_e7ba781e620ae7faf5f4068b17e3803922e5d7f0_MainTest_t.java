 package problem2685;
 
 import java.io.ByteArrayInputStream;
 import java.io.InputStream;
 import java.util.Arrays;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import static org.junit.Assert.*;
 import static org.hamcrest.CoreMatchers.*;
 
/**
 * Numeral System.Test.
 * @author hiro
 *
 */
 public class MainTest {
     /**
      * Test target.
      */
     private Main target;
 
     /**
      * 文字列をcharのLinkedListに変換.
      * 
      * @param str 元文字列.
      * @return 変換されたLinkedList.
      */
     private LinkedList<Character> chars(String str) {
         LinkedList<Character> list = new LinkedList<Character>();
         for (char c : str.toCharArray())
             list.add(c);
         return list;
     }
 
     @Before
     public void setUp() {
         target = new Main();
     }
 
     @Test
     public void testMcxiToInteger_with_1m2c3x4i() throws Exception {
         assertThat(target.mcxiToInteger(chars("1m2c3x4i")), is(1234));
     }
 
     @Test
     public void testMcxiToInteger_with_4m5c5x6i() throws Exception {
         assertThat(target.mcxiToInteger(chars("4m5c5x6i")), is(4556));
     }
 
     @Test
     public void testMcxiToInteger_with_5c6i() throws Exception {
         assertThat(target.mcxiToInteger(chars("5c6i")), is(506));
     }
 
     @Test
     public void testMcxiToInteger_with_abc() throws Exception {
         assertThat(target.mcxiToInteger(target.completeMcxi("mcx")), is(1110));
     }
 
     @Test
     public void testCompleteMcxi_mcxi() throws Exception {
         assertThat(target.completeMcxi("mcxi"), is(chars("1m1c1x1i")));
     }
 
     @Test
     public void testCompleteMcxi_mcx2i() throws Exception {
         assertThat(target.completeMcxi("mcx2i"), is(chars("1m1c1x2i")));
     }
 
     @Test
     public void testIntegerToMcxi_2345() throws Exception {
         assertThat(target.IntegerToMcxi(2345), is("2m3c4x5i"));
     }
 
     @Test
     public void testIntegerToMcxi_145() throws Exception {
         assertThat(target.IntegerToMcxi(145), is("c4x5i"));
     }
 
     @Test
     public void testAdd_with_xi_x9i() {
         assertThat(target.add("xi", "x9i"), is("3x"));
     }
 
     @Test
     public void testAdd_with_i_9i() {
         assertThat(target.add("i", "9i"), is("x"));
     }
 
     @Test
     public void testSolve1() {
         String input =  "10\n" + 
                         "xi x9i\n" + 
                         "i 9i\n" + 
                         "c2x2i 4c8x8i\n" + 
                         "m2ci 4m7c9x8i\n" + 
                         "9c9x9i i\n" + 
                         "i 9m9c9x8i\n" + 
                         "m i\n" + 
                         "i m\n" + 
                         "m9i i\n" + 
                         "9m8c7xi c2x8i\n";
         InputStream in = new ByteArrayInputStream(input.getBytes());
         List<String> expecteds = Arrays.asList("3x", "x", "6cx", "5m9c9x9i", "m", "9m9c9x9i", "mi", "mi", "mx", "9m9c9x9i");
 
         assertThat(target.solve(in), is(expecteds));
     }
 
     @Test
     public void testSolve2() {
         String input =  "1\n" + 
                         "mcxi mcxi\n";
         InputStream in = new ByteArrayInputStream(input.getBytes());
         List<String> expecteds = Arrays.asList("2m2c2x2i");
 
         assertThat(target.solve(in), is(expecteds));
     }
 }
