 package dfh.grammar;
 
 import static org.junit.Assert.*;
 
 import java.io.IOException;
 
 import org.junit.Test;
 
 /**
  * Tests whether tagging of expressions and match manipulation by labels works.
  * 
  * <b>Creation date:</b> Jun 10, 2011
  * 
  * @author David Houghton
  * 
  */
 public class TaggingTest {
 
 	@Test
 	public void compilationTest() throws GrammarException, IOException {
 		String[] rules = {
 		//
 		"<ROOT> = [{foo} 'a' /\\s++/ 'b']++ [{bar} 'c'] [{baz,quux} 'd' | 'e']",//
 		};
 		try {
 			new Grammar(rules);
 		} catch (GrammarException e) {
 			fail(e.getMessage());
 		}
 	}
 
 	@Test
 	public void patternTest() throws GrammarException, IOException {
 		String[] rules = {
 		//
 		"<ROOT> = [{foo} 'a' /\\s++/ 'b']++ [{bar} 'c'] [{baz,quux} 'd' | 'e']",//
 		};
 		Grammar g = new Grammar(rules);
 		String s = "a  ba\tbcd";
 		assertNotNull(g.matches(s).match());
 	}
 
 	@Test
 	public void tagTest1() throws GrammarException, IOException {
 		String[] rules = {
 		//
 		"<ROOT> = [{foo} 'a' /\\s++/ 'b']++ [{bar} 'c'] [{baz,quux} 'd' | 'e']",//
 		};
 		Grammar g = new Grammar(rules);
 		String s = "a  ba\tbcd";
 		Match m = g.matches(s).match();
		assertNotNull(m.getFirst("foo"));
 	}
 
 	@Test
 	public void tagTest2() throws GrammarException, IOException {
 		String[] rules = {
 		//
 		"<ROOT> = [{foo} 'a' /\\s++/ 'b']++ [{bar} 'c'] [{baz,quux} 'd' | 'e']",//
 		};
 		Grammar g = new Grammar(rules);
 		String s = "a  ba\tbcd";
 		Match m = g.matches(s).match();
		assertNotNull(m.getFirst("bar"));
 	}
 
 	@Test
 	public void tagTest3() throws GrammarException, IOException {
 		String[] rules = {
 		//
 		"<ROOT> = [{foo} 'a' /\\s++/ 'b']++ [{bar} 'c'] [{baz,quux} 'd' | 'e']",//
 		};
 		Grammar g = new Grammar(rules);
 		String s = "a  ba\tbcd";
 		Match m = g.matches(s).match();
		assertNotNull(m.getFirst("baz"));
 	}
 
 	@Test
 	public void tagTest4() throws GrammarException, IOException {
 		String[] rules = {
 		//
 		"<ROOT> = [{foo} 'a' /\\s++/ 'b']++ [{bar} 'c'] [{baz,quux} 'd' | 'e']",//
 		};
 		Grammar g = new Grammar(rules);
 		String s = "a  ba\tbcd";
 		Match m = g.matches(s).match();
		assertNotNull(m.getFirst("quux"));
 	}
 
 	@Test
 	public void labelTest1() throws GrammarException, IOException {
 		String[] rules = {
 				//
 				"<ROOT> = <a> /\\s++/ <b>",//
 				"<a> = 'a'",//
 				"<b> = 'a'",//
 		};
 		Grammar g = new Grammar(rules);
 		String s = "a  a";
 		Match m = g.matches(s).match();
		assertNotNull(m.getFirst("a"));
 	}
 
 	@Test
 	public void labelTest2() throws GrammarException, IOException {
 		String[] rules = {
 				//
 				"<ROOT> = <a> /\\s++/ <b>",//
 				"<a> = 'a'",//
 				"<b> = 'a'",//
 		};
 		Grammar g = new Grammar(rules);
 		String s = "a  a";
 		Match m = g.matches(s).match();
		assertNotNull(m.getFirst("b"));
 	}
 
 	@Test
 	public void labelTest3() throws GrammarException, IOException {
 		String[] rules = {
 				//
 				"<ROOT> = [ <c> | <a> ] /\\s++/ <b>",//
 				"<a> = 'a'",//
 				"<b> = 'a'",//
 				"<c> = 'a'",//
 		};
 		Grammar g = new Grammar(rules);
 		String s = "a  a";
 		Match m = g.matches(s).match();
		assertNotNull(m.getFirst("a"));
 	}
 
 	@Test
 	public void labelTest4() throws GrammarException, IOException {
 		String[] rules = {
 				//
 				"<ROOT> = [ <c> | 'floon' ] /\\s++/ <b>",//
 				"<b> = 'a'",//
 				"<c> = 'a'",//
 		};
 		Grammar g = new Grammar(rules);
 		String s = "a  a";
 		Match m = g.matches(s).match();
		assertNotNull(m.getFirst("c"));
 	}
 
 	@Test
 	public void labelCountTest1() throws GrammarException, IOException {
 		String[] rules = {
 				//
 				"<ROOT> = <a> /\\s++/ <b>",//
 				"<a> = 'a'",//
 				"<b> = 'a'",//
 		};
 		Grammar g = new Grammar(rules);
 		String s = "a  a";
 		Match m = g.matches(s).match();
 		assertTrue(m.get("a").size() == 1);
 	}
 
 	@Test
 	public void labelCountTest2() throws GrammarException, IOException {
 		String[] rules = {
 				//
 				"<ROOT> = <a> /\\s++/ <b>",//
 				"<a> = 'a'",//
 				"<b> = 'a'",//
 		};
 		Grammar g = new Grammar(rules);
 		String s = "a  a";
 		Match m = g.matches(s).match();
 		assertTrue(m.get("b").size() == 1);
 	}
 
 	@Test
 	public void simpleCaptureTest1() throws GrammarException, IOException {
 		String[] rules = {
 				//
 				"<ROOT> = [{a}'a'] /\\s++/ <b>",//
 				"<b> = 'a'",//
 		};
 		Grammar g = new Grammar(rules);
 		String s = "a  a";
 		Match m = g.matches(s).match();
 		assertTrue(m.get("a").size() == 1);
 	}
 
 	@Test
 	public void simpleCaptureTest2() throws GrammarException, IOException {
 		String[] rules = {
 				//
 				"<ROOT> = [{a}'a'] /\\s++/ <b>",//
 				"<b> = 'a'",//
 		};
 		Grammar g = new Grammar(rules);
 		String s = "a  a";
 		Match m = g.matches(s).match();
 		assertTrue(m.get("b").size() == 1);
 	}
 
 	@Test
 	public void repetitionTest1() throws GrammarException, IOException {
 		String[] rules = {
 		//
 		"<ROOT> = [{a}'a']++",//
 		};
 		Grammar g = new Grammar(rules);
 		String s = "aa";
 		Match m = g.matches(s).match();
 		assertTrue(m.get("a").size() == 2);
 	}
 }
