 package stralg13;
 
 import static org.junit.Assert.*;
 
 import org.junit.Test;
 
 public class SuffixTreeTest {
 
 	@Test
 	public void testCase() {
 		SuffixTree actual = new SuffixTree("a");
 		SuffixTree expected = new SuffixTree();
 		expected.string = "a" + SuffixTree.STRING_END;
 		expected.root.addEdgeAndNewNode(0, 2);
 		expected.root.addEdgeAndNewNode(1, 2);
 		expected.root.suffixLink = expected.root;
 
 		assertTrue(actual.equals(expected));
 	}
 	
 	@Test
 	public void testCaseTwoCharacters() {
 		SuffixTree actual = new SuffixTree("ab");
 
 		SuffixTree expected = new SuffixTree();
 		expected.string = "ab" + SuffixTree.STRING_END;
 		expected.root.addEdgeAndNewNode(0, 3);
 		expected.root.addEdgeAndNewNode(1, 3);
 		expected.root.addEdgeAndNewNode(2, 3);
 		expected.root.suffixLink = expected.root;
 
 		assertTrue(actual.equals(expected));
 	}
 
 	@Test
 	public void testSlowScanReturnsNode() {
 		SuffixTree tree = new SuffixTree();
 		tree.string = "ab" + SuffixTree.STRING_END;
 		tree.root.addEdgeAndNewNode(0, 2);
 		
 		ScanResult actual = tree.slowscan(tree.root, 1, tree.string.length());
 		assertNull(actual.edge);
 		assertTrue(actual.node.equals(tree.root));
 		
 	}
 	
 	@Test
 	public void testSlowScanReturnsEdge() {
 		SuffixTree tree = new SuffixTree();
 		tree.string = "aba" + SuffixTree.STRING_END;
 		tree.root.addEdgeAndNewNode(0, 3); // aba$
 		tree.root.addEdgeAndNewNode(1, 3); // ba$
 		
 		ScanResult actual = tree.slowscan(tree.root, 2, tree.string.length());
 
 		ScanResult expected 
 			= ScanResult.makeEdgeResult(tree.root, new Tuple(0,3), 1);
 		
 		assertTrue(actual.equals(expected));
 	}
 	
 	@Test
 	public void testAAB() {
 		SuffixTree actual = new SuffixTree("aba");
 
 		SuffixTree expected = new SuffixTree();
 		expected.string = "aba" + SuffixTree.STRING_END;
 		expected.root.addEdgeAndNewNode(3, 4); // $
 		Node nodeA = expected.root.addEdgeAndNewNode(0, 1); // a
 		expected.root.edges.get(new Tuple(0,1)).addEdgeAndNewNode(3,4); //$
 		expected.root.edges.get(new Tuple(0,1)).addEdgeAndNewNode(1,4); //ba$
 		expected.root.addEdgeAndNewNode(1, 4); // ba$
 		
 		expected.root.suffixLink = expected.root;
 		//nodeA.suffixLink = expected.root;
 
 		assertTrue(actual.equals(expected));
 	}
 	
 	
 	@Test
 	public void testEdgeStartsWithString() {
 		SuffixTree tree = new SuffixTree("abcde");
 
 		assertTrue(tree.edgeStartsWithString(1, new Tuple(1, 5)));
 	}
 
 	@Test
 	public void testABAABTreeConstruction() {
 		// a b a a b
 		// 0 1 2 3 4
 		SuffixTree actual = new SuffixTree("abaab");
 		
 		SuffixTree expected = new SuffixTree();
 		expected.string = "abaab$";
 		Node nodeA = expected.root.addEdgeAndNewNode(0, 1); // a
 		Node nodeB = expected.root.addEdgeAndNewNode(1, 2); // b
 		expected.root.addEdgeAndNewNode(5, 6); // $
 		
 		Node nodeAB = nodeA.addEdgeAndNewNode(1, 2); // a b
 		nodeA.addEdgeAndNewNode(3, 6); // a ab$
 		
 		nodeAB.addEdgeAndNewNode(2, 6); // a b aab$
 		nodeAB.addEdgeAndNewNode(5, 6); // a b $
 		nodeB.addEdgeAndNewNode(2, 6); // b aab$
 		nodeB.addEdgeAndNewNode(5, 6); // b $
 		
 		expected.root.suffixLink = expected.root;
 		nodeA.suffixLink = expected.root;
 		nodeAB.suffixLink = nodeB;
 		//nodeB.suffixLink = expected.root;
 		
 		assertTrue(actual.equals(expected));
 	}
 	
 	@Test
 	public void testABAABABA () {
 		SuffixTree actual = new SuffixTree("abaababa");
 		
 		SuffixTree expected = new SuffixTree();
 		expected.string ="abaababa$";
 		
 		Node nodeA = expected.root.addEdgeAndNewNode(0,  1);	
 		nodeA.addEdgeAndNewNode(8, 9);
		nodeA.addEdgeAndNewNode(2, 9);
 	
 		Node nodeABA = nodeA.addEdgeAndNewNode(1, 3);
 		
 		nodeABA.addEdgeAndNewNode(6, 9);
 		nodeABA.addEdgeAndNewNode(8, 9);
 		nodeABA.addEdgeAndNewNode(3, 9);
 		
 		Node nodeBA = expected.root.addEdgeAndNewNode(1, 3);
 		nodeBA.addEdgeAndNewNode(3, 9);
 		nodeBA.addEdgeAndNewNode(8, 9);
 		nodeBA.addEdgeAndNewNode(6, 9);
 		
 		assertTrue(expected.equals(actual));
		
		
		}
 	
 	@Test
 	public void testToString(){
 		SuffixTree expected = new SuffixTree();
 		expected.string = "aba" + SuffixTree.STRING_END;
 		expected.root.addEdgeAndNewNode(3, 4); // $
 		Node nodeA = expected.root.addEdgeAndNewNode(0, 1); // a
 		expected.root.edges.get(new Tuple(0,1)).addEdgeAndNewNode(3,4); //$
 		expected.root.edges.get(new Tuple(0,1)).addEdgeAndNewNode(1,4); //ba$
 		expected.root.addEdgeAndNewNode(1, 4); // ba$
 		
 		System.out.println(expected);
 	}
 }
