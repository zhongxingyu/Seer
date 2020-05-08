 import static org.junit.Assert.*;
 
 import java.io.IOException;
 
 import org.antlr.runtime.RecognitionException;
 import org.junit.Test;
 
 
 public class TestParser {
 
 	@Test
 	public void testParse() {
 		Main m = new Main();
 		try {
 			int foo = m.parse("tests\\test1.txt");
			assertEquals(1, foo);
 		} catch (IOException e) {
 			fail("test\\test1.txt not found");
 		}
 		
 	}
 
 }
