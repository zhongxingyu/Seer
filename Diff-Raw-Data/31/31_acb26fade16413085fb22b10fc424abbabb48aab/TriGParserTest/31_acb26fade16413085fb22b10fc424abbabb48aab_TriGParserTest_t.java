// $Id: TriGParserTest.java,v 1.10 2008/08/21 16:36:06 hartig Exp $
 package de.fuberlin.wiwiss.ng4j.trig;
 
 import java.io.Reader;
 import java.io.StringReader;
 
 import junit.framework.TestCase;
 
 import com.hp.hpl.jena.graph.Node;
 
 import de.fuberlin.wiwiss.ng4j.NamedGraphSet;
 import de.fuberlin.wiwiss.ng4j.NamedGraphSetReader;
 import de.fuberlin.wiwiss.ng4j.Quad;
 import de.fuberlin.wiwiss.ng4j.impl.NamedGraphSetImpl;
 
 /**
  * Tests for TriG parsing
  *
  * @author Richard Cyganiak (richard@cyganiak.de)
  */
 public class TriGParserTest extends TestCase {
 	private static final String EX = "http://example.com/ex#";
 	private static final String BASE = "http://example.com/base";
 	private static final String DEFAULT = "http://example.com/default";
//	private static final Node defaultNode = Node.createURI(DEFAULT);
 	private static final Node aNode = Node.createURI(EX + "a");
 	private static final Node bNode = Node.createURI(EX + "b");
 	private static final Node cNode = Node.createURI(EX + "c");
 	private static final Node graph1Node = Node.createURI(EX + "graph1");
 	private static final Node graph2Node = Node.createURI(EX + "graph2");
 	private static final Node graph3Node = Node.createURI(EX + "graph3");
 
 	private NamedGraphSet parseTriG(String triG) throws Exception {
 		Reader r = new StringReader(triG);
 		NamedGraphSetImpl ngs = new NamedGraphSetImpl();
 		NamedGraphSetPopulator h = 
 				new NamedGraphSetPopulator(ngs, BASE, DEFAULT);
 		TriGParser p = new TriGParser(r, h);
 		p.parse();
 		return ngs;
 	}
 
 	public void testSimpleN3() throws Exception {
 		String n3 = "@prefix ex: <http://example.com/ex#> .\n" +
 				"ex:a ex:b ex:c .";
 		try {
 			parseTriG(n3);
 			fail();
 		} catch (TriGException ex) {
 			// statements must be enclosed in graph
 		}
 	}
 	
 	public void testUnlabelledGraph() throws Exception {
 		String triG = "@prefix ex: <http://example.com/ex#> .\n" +
 				"{ ex:a ex:b ex:c }";
 		try {
 			parseTriG(triG);
 			fail();
 		} catch (TriGException ex) {
 			// unlabelled graphs are not allowed
 		}
 	}
 	
 	public void testQuadFormula() throws Exception {
 		String triG = "@prefix ex: <http://example.com/ex#> .\n" +
 				"ex:graph1 :- { ex:a ex:b ex:c }";
 		NamedGraphSet ngs = parseTriG(triG);
 		assertTrue(ngs.containsQuad(new Quad(graph1Node, aNode, bNode, cNode)));
 		assertEquals(1, ngs.countQuads());
 	}
 	
 	public void testIllegalNamingOperator() throws Exception {
 		String triG = "@prefix ex: <http://example.com/ex#> .\n" +
 		"ex:graph ex:foo { ex:a ex:b ex:c }";
 		try {
 			parseTriG(triG);
 			fail();
 		} catch (TriGException ex) {
 			// is expected since ex:foo is not the graph naming operator :-
 		}
 	}
 	
 	public void testIllegalGraphName() throws Exception {
 		String triG = "@prefix ex: <http://example.com/ex#> .\n" +
 		"foo :- { ex:a ex:b ex:c }";
 		try {
 			parseTriG(triG);
 			fail();
 		} catch (TriGException ex) {
 			// is expected since "foo" is not a legal graph name
 		}
 	}
 	
 	public void testNoNestedGraphs() throws Exception {
 		String triG = "@prefix ex: <http://example.com/ex#> .\n" +
 		"ex:graph :- { ex:graph :- { ex:a ex:b ex:c } }";
 		try {
 			parseTriG(triG);
 			fail();
 		} catch (TriGException ex) {
 			// is expected because nesting graphs is not allowed
 		}		
 	}
 	
 	public void testMultipleGraphs() throws Exception {
 		String triG = "@prefix ex: <http://example.com/ex#> .\n" +
 				"ex:graph1 :- { ex:a ex:b ex:c }\n" +
 				"ex:graph2 :- { ex:b ex:c ex:a }\n" +
 				"ex:graph3 :- { ex:c ex:a ex:b }\n";
 		NamedGraphSet ngs = parseTriG(triG);
 		assertTrue(ngs.containsQuad(new Quad(graph1Node, aNode, bNode, cNode)));
 		assertTrue(ngs.containsQuad(new Quad(graph2Node, bNode, cNode, aNode)));
 		assertTrue(ngs.containsQuad(new Quad(graph3Node, cNode, aNode, bNode)));
 		assertEquals(3, ngs.countQuads());
 	}	
 
 	public void testBaseURI() throws Exception {
 		String triG = "<> { <> <> <> }";
 		NamedGraphSet ngs = parseTriG(triG);
 		Node base = Node.createURI(BASE);
 		assertTrue(ngs.containsQuad(new Quad(base, base, base, base)));
 		assertEquals(1, ngs.countQuads());
 	}
 
 	public void testWithoutGraphNamingOperator() throws Exception {
 		String triG = "@prefix ex: <http://example.com/ex#> .\n" +
 				"ex:graph1 { ex:a ex:b ex:c }";
 		NamedGraphSet ngs = parseTriG(triG);
 		assertTrue(ngs.containsQuad(new Quad(graph1Node, aNode, bNode, cNode)));
 		assertEquals(1, ngs.countQuads());
 	}
 
 	public void testGraphWithoutDot() throws Exception {
 		String triG = "@prefix ex: <http://example.com/ex#> .\n" +
 				"ex:graph1 { ex:a ex:b ex:c }";
 		NamedGraphSet ngs = parseTriG(triG);
 		assertTrue(ngs.containsQuad(new Quad(graph1Node, aNode, bNode, cNode)));
 		assertEquals(1, ngs.countQuads());
 	}
 	
 	public void testGraphWithDot() throws Exception {
 		String triG = "@prefix ex: <http://example.com/ex#> .\n" +
 				"ex:graph1 { ex:a ex:b ex:c } .";
 		try {
 			parseTriG(triG);
 			fail();
 		} catch (TriGException ex) {
 			// there must be no dot after a graph
 		}	
 	}
 
 	public void testEmptyGraph() throws Exception {
 		String triG = "@prefix ex: <http://example.com/ex#> .\n" +
 				"ex:graph1 {}";
 		NamedGraphSet ngs = parseTriG(triG);
 		assertEquals(0, ngs.countQuads());
 		assertTrue(ngs.containsGraph(graph1Node));
 	}
 
 	public void testURIGraphName() throws Exception {
 		String triG = "@prefix ex: <http://example.com/ex#> .\n" +
 				"<http://example.com/ex#graph1> { ex:a ex:a ex:a }";
 		NamedGraphSet ngs = parseTriG(triG);
 		assertTrue(ngs.containsGraph(graph1Node));
 	}
 
 	public void testBlankGraphName() throws Exception {
 		String triG = "@prefix ex: <http://example.com/ex#> .\n" +
 				"_:foo { ex:a ex:b ex:c }";
 		try {
 			parseTriG(triG);
 			fail();
 		} catch (TriGException ex) {
 			// blank nodes are not allowed as graph names
 		}	
 	}
 
 	public void testLiteralGraphName() throws Exception {
 		String triG = "@prefix ex: <http://example.com/ex#> .\n" +
 				"\"abc\" { ex:a ex:b ex:c }";
 		try {
 			parseTriG(triG);
 			fail();
 		} catch (TriGException ex) {
 			// literal nodes are not allowed as graph names
 		}	
 	}
 
 	public void testEmptyDocument() throws Exception {
 		String triG = "";
 		NamedGraphSet set = parseTriG(triG);
 		assertTrue(set.isEmpty());
 	}
 
 	public void testReadFromReader() {
 		String trig = "@prefix : <http://example.com/ns#> .\n" +
 				":graph1 { :a :a \"\" . }";
 		Reader r = new StringReader(trig);
 		NamedGraphSetReader reader = new TriGReader();
 		NamedGraphSet set = new NamedGraphSetImpl();
 		reader.read(set, r, BASE, DEFAULT);
 		assertTrue(set.containsQuad(new Quad(Node.ANY, Node.ANY, Node.ANY,
 				Node.createLiteral("", null, null))));
 		assertEquals(1, set.countQuads());
 	}
 
 	public void testDuplicateGraphName() throws Exception {
 		String triG = "@prefix : <http://example.com/ex#> .\n" +
 				":graph { :a :b :c }\n" +
 				":graph { :d :e :f }";
 		try {
 			parseTriG(triG);
 			fail();
 		} catch (TriGException ex) {
 			// Graph names must be unique
 		}	
 	}
 }
