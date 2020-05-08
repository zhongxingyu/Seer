 package dfh.grammar;
 
 import java.io.IOException;
 
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  * Tests whether recursive pattern matching works.
  * <p>
  * <b>Creation date:</b> Mar 25, 2011
  * 
  * @author David Houghton
  * 
  */
 public class RecursiveTest {
 
 	private static Grammar g;
 
 	@BeforeClass
 	public static void setUpBeforeClass() throws Exception {
 		String[] rules = {
 				//
 				"<ROOT> = <element>", //
 				"<element> = <single> | <double>",//
 				"<single> = '<' (tag) [ (s) <attribute> ]* '/>'",//
 				"<double> = '<' (tag) [ (s) <attribute> ]* '>' <element>* '</' 2 '>'",//
 				"<attribute> = (tag) '=\"' <content>++ '\"'",//
 				"<content> = (nq) | (esc)",//
 				"(nq) =[^\"]",//
 				"(esc) =\\\\.",//
 				"(tag) =\\w++",//
 				"(s) =\\s++",//
 		};
 		g = new Grammar(rules);
 	}
 
 	@Test
 	public void smallTest() throws GrammarException, IOException {
 		String s = "<test/>";
 		Matcher m = g.matches(s);
 		Match n = m.match();
 		System.out.println(n);
 		assertNotNull("parsed minimal document", n);
 	}
 
 	@Test
 	public void bigTest() throws GrammarException, IOException {
 		String s = "<test><foo/></test>";
 		Matcher m = g.matches(s);
 		Match n = m.match();
 		System.out.println(n);
 		assertNotNull("parsed minimal document", n);
 	}
 
 	@Test
 	public void biggerTest() throws GrammarException, IOException {
 		String s = "<test><foo jolly=\"roger\"/></test>";
 		Matcher m = g.matches(s);
 		Match n = m.match();
 		System.out.println(n);
 		assertNotNull("parsed minimal document", n);
 	}
 
 	@Test
 	public void failureTest() throws GrammarException, IOException {
 		String s = "<test><foo jolly=\"roger\"/></ROO>";
 		Matcher m = g.matches(s);
 		Match n = m.match();
 		System.out.println(n);
 		assertNull("parsed minimal document", n);
 	}
 }
