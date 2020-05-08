 package com.github.kkckkc.syntaxpane.style;
 
 import junit.framework.TestCase;
 import kkckkc.syntaxpane.model.Scope;
 import kkckkc.syntaxpane.parse.grammar.SimpleContext;
 import kkckkc.syntaxpane.style.BasicScopeSelectorParser;
 import kkckkc.syntaxpane.style.ScopeSelector;
 import kkckkc.syntaxpane.style.ScopeSelectorManager;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.StringTokenizer;
 
 
 public class ScopeSelectorManagerTest extends TestCase {
 	public void testComplex() {
 		ScopeSelectorManager manager = new ScopeSelectorManager();
 		Map<ScopeSelector, ScopeSelector> m = new HashMap<ScopeSelector, ScopeSelector>();
         for (ScopeSelector s1 : Arrays.asList(
         						BasicScopeSelectorParser.parse("string"),
         						BasicScopeSelectorParser.parse("source string"),
         						BasicScopeSelectorParser.parse("string.quoted"),
         						BasicScopeSelectorParser.parse("source.php"))) {
         	m.put(s1, s1);
         }
 
 		Iterable<ScopeSelector> it = manager.getMatches(mockScope("source.php string.quoted"), m);
 
 		for (ScopeSelector s : it) {
 			assertEquals("string.quoted ", s.toString());
 		}
 	}
 
 	private static Scope mockScope(final String string) {
 		StringTokenizer tok = new StringTokenizer(string, " ");
 
 		Scope parent = null;
 		while (tok.hasMoreElements()) {
 			final String s = tok.nextToken();
 			SimpleContext c = new SimpleContext();
 			c.setId(s);
 			c.setName(s);
 			
 			parent = new Scope(0, 0, c, parent);
 		}
 
 		return parent;
 	}
 }
