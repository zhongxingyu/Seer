// $Id: TriGReaderTest.java,v 1.6 2008/08/21 16:36:06 hartig Exp $
 package de.fuberlin.wiwiss.ng4j.trig;
 
 import java.io.InputStream;
 
 import junit.framework.TestCase;
 
 import com.hp.hpl.jena.graph.Node;
 
 import de.fuberlin.wiwiss.ng4j.NamedGraphSet;
 import de.fuberlin.wiwiss.ng4j.NamedGraphSetReader;
 import de.fuberlin.wiwiss.ng4j.Quad;
 import de.fuberlin.wiwiss.ng4j.impl.NamedGraphSetImpl;
 
 /**
  * Tests {@link TriGReader}.
  *
  * @author Richard Cyganiak (richard@cyganiak.de)
  */
 public class TriGReaderTest extends TestCase {
 	private static final String BASE = "http://example.com/base";
 	private static final String DEFAULT = "http://example.com/default";
 	private static final String NS = "http://example.com/ns#";
 	private static final Node a = Node.createURI(NS + "a");
 	private static final Node b = Node.createURI(NS + "b");
// 	private static final Node c = Node.createURI(NS + "c");
// 	private static final Node d = Node.createURI(NS + "d");
 	private static final Node e = Node.createURI(BASE + "#e");
 	private static final Node graph1 = Node.createURI(NS + "graph1");
 	private static final Node graph2 = Node.createURI(NS + "graph2");
// 	private static final Node defaultGraph = Node.createURI(DEFAULT);
 
 	private NamedGraphSet ngs;
 
 	public void setUp() {
 		InputStream in = this.getClass().getResourceAsStream("tests/test.trig");
 		NamedGraphSetReader reader = new TriGReader();
 		this.ngs = new NamedGraphSetImpl();
 		reader.read(this.ngs, in, BASE, DEFAULT);		
 	}
 
 	public void testGraphQuads() {
 		assertTrue(this.ngs.containsQuad(new Quad(graph1, a, a, a)));
 		assertTrue(this.ngs.containsQuad(new Quad(graph2, b, b, b)));
 	}
 
 	public void testBaseURI() {
 		assertTrue(this.ngs.containsQuad(new Quad(Node.ANY, e, e, e)));
 	}
 	
 	public void testEncoding() {
 		assertTrue(this.ngs.containsQuad(new Quad(Node.ANY, Node.ANY, Node.ANY,
 				Node.createLiteral("\u00e4\u00f6\u00fc", null, null))));
 	}
 
 	public void testNoAdditionalQuads() {
 		assertEquals(5, this.ngs.countQuads());
 	}
 }
