 package languish.interpreter;
 
import static languish.testing.TestConstants.FOUR;

 import java.io.InputStream;
 import java.io.InputStreamReader;
 
 import junit.framework.TestCase;
 
 public class ShellTest extends TestCase {
   public void testTestLish() {
     InputStream stream =
         getClass().getClassLoader().getResourceAsStream("languish/test.lish");
 
     Shell s = new Shell(new InputStreamReader(stream));
 
    assertEquals(FOUR, s.getLast());
   }
 
   public void testBaseGrammarLish() {
     InputStream stream =
         getClass().getClassLoader().getResourceAsStream(
             "languish/base_grammar.lish");
 
     Shell s = new Shell(new InputStreamReader(stream));
 
    assertEquals(FOUR, s.getLast());
   }
 }
