 package se.fnord.jamon;
 
 import static org.junit.Assert.assertEquals;
import static se.fnord.jamon.NodeMatchers.value;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.junit.Test;
 
 public final class TestNodeMatchers {
 	public static final class ValueLister implements PathVisitor {
 		public final List<String> values = new ArrayList<>();
 		@Override
         public boolean visit(Path path) {
 			values.add(path.leaf().value());
 	        return true;
         }
 	}
 	
 	private static Node tree() {
 		Nodes n = new Nodes();
 		return n.node("a",
 			n.node("aa",
 				n.node("aaa",
 					n.node("aaaa"),
 					n.node("aaab")),
 				n.node("aab"),
 				n.node("aac",
 					n.node("aaca"),
 					n.node("aacb"))),
 			n.node("ab",
 				n.node("aba"),
 				n.node("abb"),
 				n.node("abc"))).build();
 	}
 
 	private static Node tree2() {
 		Nodes n = new Nodes();
 		return n.node("a", 101,
 			n.node("aa", 201,
 				n.node("aa", 301,
 					n.node("aaa", 401),
 					n.node("aab", 402)),
 				n.node("aaa", 302),
 				n.node("aa", 303,
 					n.node("aaa", 403),
 					n.node("aab", 404))),
 			n.node("aa", 202,
 				n.node("aa", 304,
 					n.node("aaa", 405),
 					n.node("aab", 406)),
 				n.node("ab", 305),
 				n.node("aa", 306,
 					n.node("aaa", 407),
 					n.node("aac", 408)))).build();
 	}
 
 	@Test
 	public void testBreadthFirst() throws ParseException, FatalParseException {
 		Node tree = tree();
 		ValueLister lister = new ValueLister();
 		NodeMatchers.traverseBreadthFirst(Path.path(tree), lister);
 		assertEquals(Arrays.asList("a", "aa", "ab", "aaa", "aab", "aac", "aba", "abb", "abc", "aaaa", "aaab", "aaca", "aacb"), lister.values);
 	}
 
 	@Test
 	public void testDepthFirst() throws ParseException, FatalParseException {
 		Node tree = tree();
 		ValueLister lister = new ValueLister();
 		NodeMatchers.traverseDepthFirst(Path.path(tree), lister);
 		assertEquals(Arrays.asList("a", "aa", "aaa", "aaaa", "aaab", "aab", "aac", "aaca", "aacb", "ab", "aba", "abb", "abc"), lister.values);
 	}
 
 	@Test
 	public void testFindFirst() {
		Node tree = tree2();
 		//System.err.println(NodeMatchers.findFirstBF(value("aaa"), Path.path(tree)).leaf().attachment());
 	}
	
 }
