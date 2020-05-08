// $Id: TriGWriterTest.java,v 1.3 2005/07/01 09:27:06 cyganiak Exp $
 package de.fuberlin.wiwiss.ng4j.trig;
 
 import java.io.StringWriter;
 
 import junit.framework.TestCase;
 
 import com.hp.hpl.jena.graph.Node;
 
 import de.fuberlin.wiwiss.ng4j.NamedGraphSet;
 import de.fuberlin.wiwiss.ng4j.impl.NamedGraphSetImpl;
 
 /**
  * TODO: Describe this type
  *
  * @author Richard Cyganiak (richard@cyganiak.de)
  */
 public class TriGWriterTest extends TestCase {
 	private final static String EX = "http://example.org/#";
 	private final static Node graph1 = Node.createURI(EX + "graph1");
 	private final static Node graph2 = Node.createURI(EX + "graph2");
	private final static Node foo = Node.createURI(EX + "foo");
	private final static Node bar = Node.createURI(EX + "bar");
	private final static Node baz = Node.createURI(EX + "baz");
 	private NamedGraphSet set;
 	
 	public void setUp() {
 		this.set = new NamedGraphSetImpl();
 	}
 
 	public void testEmptySet() {
 		TriGWriter writer = new TriGWriter();
 		StringWriter s = new StringWriter();
 		writer.write(this.set, s, null);
 		assertEquals("", s.toString());
 	}
 	
 	public void testOneEmptyGraph() {
 		this.set.createGraph(graph1);
 		TriGWriter writer = new TriGWriter();
 		StringWriter s = new StringWriter();
 		writer.write(this.set, s, null);
 		assertEquals("<" + EX + "graph1> { }\n\n", s.toString());
 	}
 	
 	public void testTwoEmptyGraphs() {
 		this.set.createGraph(graph1);
 		this.set.createGraph(graph2);
 		TriGWriter writer = new TriGWriter();
 		StringWriter s = new StringWriter();
 		writer.write(this.set, s, null);
 		assertEquals("<" + EX + "graph1> { }\n\n<" + EX + "graph2> { }\n\n",
 				s.toString());
 	}
 
 	public void testAddNamespace() {
 		this.set.createGraph(graph1);
 		TriGWriter writer = new TriGWriter();
 		writer.addNamespace("ex", EX);
 		StringWriter s = new StringWriter();
 		writer.write(this.set, s, null);
 		assertEquals("@prefix ex:      <" + EX + "> .\n\nex:graph1 { }\n\n",
 				s.toString());
 	}
 }
